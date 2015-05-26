/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rasterautomaticpublication;

import java.io.IOException;
import java.util.List;
import mtdtautomaticpublication.GeoNetworkPostReq;

/**
 * Hlavní třída programu pro publikaci metadat a dat
 * @author jhettler
 */
public class RasterAutomaticPublication {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        //Nacteni konfigurace ze souboru AppConfig.xml, ve stejne slozce jako samotna aplikace
        XMLparsing appConfig = new XMLparsing();
        String[] configParams = appConfig.ParseAppConfigXML("AppConfig.xml");

        //Parametr s cestou ke slozce s rastry
        String AbsolutePath = configParams[0];
        //Parametr nazev slozky s rastry k importu
        String RasterDir = configParams[1];
        //Parametr k datovym slozkam Geoserveru
        String GeoServerDataDir = configParams[2];
        //Prihlasovaci udaje ke Geoserveru
        String geoserverUserName = configParams[3]; String geoserverPassword = configParams[4];
        //Prihlasovaci udaje ke GeoNetworku
        String geonetworkUserName = configParams[5]; String geonetworkPassword = configParams[6];
        //Prihlasovaci udaje k PostgreSQL
        String postgresConn = configParams[7]; String postgresUserName = configParams[8]; String postgresPassword = configParams[9];
        //Prihlasovaci udaje k PostgreSQL
        String geoserverLocation = configParams[10];        
        //Prihlasovaci udaje k PostgreSQL
        String geonetworkLocation = configParams[11];
        
        //StringBuilder pro ukladani vypublikovanych vrstev
        StringBuilder PublishedLayers = new StringBuilder();

        //Objekt pro manipulaci s rastry na urovni file systemu
        FilesInDir DirOperation = new FilesInDir();

        //Zalozeni seznamu s overenymi rastry pro import
        List VerifiedRasters = DirOperation.VerifiedRaster2Imp(RasterDir, AbsolutePath);

        //Overeni, zda existuji nejake rastry, ktere splnuji podminky importu
        if (VerifiedRasters.size() < 1) {

            System.out.println("No valid raster for import - check if necessary raster files exist *.wld,*.mxml,*.xml");

        } else {

        //Objekt pro praci s GeoServerem pres REST API    
        CurlCmdOperation CurlCmdOp = new CurlCmdOperation();
        
        //Obejkt GeoNetworku
        GeoNetworkPostReq GeoNetwork = new GeoNetworkPostReq();
        
        //Objekt mapove konfigurace
        MapConfig Mc = new MapConfig();
        
        // Objekt pro praci s rastry pres prikazovou radku
        RasterCmdOperation CmdOp = new RasterCmdOperation();
        
        //Objekt pro praci s databazi
        PostgreSQLcmd impRasters = new PostgreSQLcmd();
        
        //Prihlaseni uzivatele GeoNetworku
        String SID = GeoNetwork.userLogin(geonetworkUserName,geonetworkPassword, geonetworkLocation);
            
            //Cyklus prochazejici pres vsechny platne rastry
            for (int i = 0; i < VerifiedRasters.size(); i++) {
                
                //Nacteni mapove konfigurace prislusne pro kazdou vrstvu
                String ActualRaster = VerifiedRasters.get(i).toString();
                String[] ActualRasterNoExtArray = ActualRaster.split("\\.");                
                String ActualRasterNoExt = ActualRasterNoExtArray[0].toString();
                
                String [] MapConfigParams = appConfig.ParseMapXML(AbsolutePath+RasterDir+"\\"+ActualRasterNoExt+".mxml");
                String WorkSpace = MapConfigParams[1]; 
                
                //TODO dynamicky dopocitat pyramidy
                String[] TileLevels = {"2", "4"};
                String SRID = MapConfigParams[2];
                String LayerName = "layer_" + ActualRasterNoExt;
                
                
                System.out.println(""
                        + "|#################################################################################################################################################|\r\n"
                        + "|#################################################################################################################################################|\r\n"
                        + "|******************* GeoServer Part **************************************************************************************************************|\r\n"
                        + "|#################################################################################################################################################|\r\n");
                
                //Zalozeni noveho Workspacu pres curl REST API, pokud existuje nebude zalozen znovu
                CurlCmdOp.CmdCommand("curl -u "+geoserverUserName+":"+geoserverPassword+" -v -XPOST -H \"Content-type: text/xml\" -d \"<workspace><name>" + WorkSpace + "</name></workspace>\" "+geoserverLocation+"/rest/workspaces");

                //Ziskava rozsah daneho rastru
                String[] Extent = CmdOp.GdalInfo(AbsolutePath + RasterDir, ActualRaster);

                // Vytvoreni nazvu DataStore pro GeoServer
                String CoverageStore = "PostGISraster_" + ActualRasterNoExt;    

                // Import rastru a modifikace databaze
                impRasters.ImportRasterModifDB(ActualRaster, TileLevels, LayerName, AbsolutePath, RasterDir, SRID, postgresConn, postgresUserName, postgresPassword);
                
                //Vytvoreni docasneho souboru pro mapovou konfiguraci GeoServeru
                String TmpMapConfigFile = (AbsolutePath + RasterDir+"\\"+CoverageStore + ".imagemosaicjdbc");
                
                //Zapsani rastru docasne na disk
                Mc.writeStringBuilder2File(TmpMapConfigFile, LayerName, SRID,postgresUserName,postgresPassword, postgresConn);
                
                //Publikace mapove sluzby nad importovanymi daty - tato moznost nebyla znama, neni dokumentovana moznost imagemosaicjdbc
                CurlCmdOp.CmdCommand("curl -u "+geoserverUserName+":"+geoserverPassword+" -v -XPUT -H \"Content-type: text/xml\" -d @"+ TmpMapConfigFile +" "+geoserverLocation+"/rest/workspaces/" + WorkSpace + "/coveragestores/" + CoverageStore + "/file.imagemosaicjdbc");
                
                //Smazani docasneho souboru mapove konfigurace
                DirOperation.deleteFile(TmpMapConfigFile);
                
                // Vytvoreni vrstvy v GeoServeru - objeveni moznosti odeslani Background Color a Output Transparency Color
                CurlCmdOp.CmdCommand("curl -u "+geoserverUserName+":"+geoserverPassword+" -v -XPUT -H \"Content-type: text/xml\" "
                        + "-d \"<coverage><enabled>true</enabled><name>" + LayerName + "</name><title>" + LayerName + "</title><parameters><entry><string>OutputTransparentColor</string><string>#FFFFFF</string></entry><entry><string>BackgroundColor</string><string>#FFFFFF</string></entry></parameters></coverage>\" "
                        + geoserverLocation+"/rest/workspaces/" + WorkSpace + "/coveragestores/" + CoverageStore + "/coverages/"+LayerName+".xml");

                PublishedLayers.append(geoserverLocation+"/wms?service=WMS&version=1.3&request=GetMap&layers=" + WorkSpace + ":" + LayerName + "&styles=&bbox="+Extent[0]+","+Extent[1]+","+Extent[2]+","+Extent[3]+"&width=1000&height=500&srs=EPSG:"+SRID+"&format=image/jpeg\r\n");

                System.out.println("\r\n\r\n"
                        + "|#################################################################################################################################################|\r\n"
                        + "|#################################################################################################################################################|\r\n"
                        + "|******************* GeoNetwork Part *************************************************************************************************************|\r\n"
                        + "|#################################################################################################################################################|");


                try {        
                    //Zalozeni metadat v metadatovem katalogu GeoNetwork
                    GeoNetwork.multiFormPost(SID, ActualRasterNoExt, LayerName, WorkSpace, Extent, geonetworkLocation);
                } catch (Exception e) {
                    System.out.println("Error Multiform" + e);
                }
                
                //Presun vypublikovanych souboru do slozky imported, pokud existuje
                DirOperation.MoveFile(ActualRasterNoExt,ActualRaster, RasterDir, AbsolutePath);


            }
            //Odhlaseni GeoNetworku
            GeoNetwork.userLogout(SID, geonetworkLocation);

            //Vypsání vypublikovaných vrstev
            System.out.println("\r\n\r\n|------------------------------------------------------------------|\r\n"
                    + "|******************* Vypublikované vrstvy *************************|");
            System.out.println(PublishedLayers.toString());



        }
    }
}
