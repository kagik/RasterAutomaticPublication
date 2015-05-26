/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rasterautomaticpublication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

/**
 * Trida pro zapsani mapConfig souboru na do adresare s importovanymi rastry a nasledne odeslani pres curl REST API na GeoServer
 * @author jhettler
 */

public class MapConfig {
    
    StringBuilder mapConfig;
    /**
     * Vytvori string s konfiguracnim souborem mapy
     * @param Path
     * @param layerName
     * @param SRID
     * @param postgresUserName
     * @param postgresPassword
     * @param postgresConn
     * @return 
     */
    public boolean writeStringBuilder2File(String Path, String layerName, String SRID, String postgresUserName,String postgresPassword,  String postgresConn) {
        
        try {
            StringBuilder ImageMosaicDef = new StringBuilder();

            File file = new File(Path);
            try (Writer writer = new BufferedWriter(new FileWriter(file, true))) {
                ImageMosaicDef.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                        + "<config version=\"1.0\">"
                        +   "<coverageName name=\"").append(layerName).append("\"/>"
                        +   "<coordsys name=\"EPSG:").append(SRID).append("\"/>"
                        +   "<!-- interpolation 1 = nearest neighbour, 2 = bilinear, 3 = bicubic -->"
                        +   "<scaleop  interpolation=\"1\"/><verify cardinality=\"false\"/>"
                        +   "<!-- possible values: universal,postgis,db2,mysql,oracle -->"
                        +   "<spatialExtension name=\"postgis\"/>"
                        +   "<mapping>"
                        +       "<masterTable name=\"mastertable\" >"
                        +           "<coverageNameAttribute name=\"name\"/>"
                        +           "<maxXAttribute name=\"maxX\"/><maxYAttribute name=\"maxY\"/><minXAttribute name=\"minX\"/><minYAttribute name=\"minY\"/>"
                        +           "<resXAttribute name=\"resX\"/><resYAttribute name=\"resY\"/>"
                        +           "<tileTableNameAtribute  name=\"TileTable\" />"
                        +           "<spatialTableNameAtribute name=\"SpatialTable\" />"
                        +       "</masterTable>"
                        +        "<tileTable>"
                        +           "<blobAttributeName name=\"data\" />"
                        +           "<keyAttributeName name=\"rid\" />"
                        +       "</tileTable>"
                        +       "<spatialTable>"
                        +           "<keyAttributeName name=\"rid\" />"
                        +           "<geomAttributeName name=\"geom\" />"
                        +           "<tileMaxXAttribute name=\"maxX\"/><tileMaxYAttribute name=\"maxY\"/><tileMinXAttribute name=\"minX\"/><tileMinYAttribute name=\"minY\"/>"
                        +       "</spatialTable>"
                        +   "</mapping>"
                        +   "<connect><!-- value DBCP or JNDI --><dstype value=\"DBCP\"/><!--<jndiReferenceName value=\"\"/>-->"
                        +       "<username value=\""+postgresUserName+"\" />"
                        +       "<password value=\""+postgresPassword+"\" />"
                        +       "<jdbcUrl value=\""+postgresConn+"\" />"
                        +       "<driverClassName value=\"org.postgresql.Driver\"/>"
                        +       "<maxActive value=\"10\"/>"
                        +       "<maxIdle value=\"0\"/>"
                        +   "</connect>"
                        + "</config>");
                String mapConfig = ImageMosaicDef.toString();
                writer.append(mapConfig);
                writer.close();
            }

            ImageMosaicDef.setLength(0);

            return true;

        } catch (Exception e) {

            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("## !!! ## Can't write output ImageMosaicDef, check the destination folder and repair it. Caused by " + e);
            System.out.println("==================================================================================");
            return false;
        }
    }
    /**
     * Zapise mapConfig docasne na disk
     * @param Path
     * @return 
     */
    public boolean writeStringBuilder2File(String Path) {

        try {

            File file = new File(Path);
            Writer writer = new BufferedWriter(new FileWriter(file,true));

            String iXML = mapConfig.toString();
            writer.append(iXML);
            writer.close();
            
            mapConfig.setLength(0);

            return true;

        } catch (Exception e) {

            System.out.println("----------------------------------------------------------------------------------");
            System.out.println("## !!! ## Can't write output mapConfig file, check the destination folder and repair it. Caused by " + e);
            System.out.println("==================================================================================");
            return false;
        }
    }
}
