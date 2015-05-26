package rasterautomaticpublication;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Trida pro parsovani XML konfiguracnich souboru
 * @author jhettler
 */
public class XMLparsing {
    
    /**
     * Parsovani konfigurace aplikace
     * @param AppConfigDir
     * @return 
     */
    public String[] ParseAppConfigXML(String AppConfigDir) {
        String[] appConfigParams = new String[12];

        try {
            File fXmlFile = new File(AppConfigDir);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            appConfigParams[0] = doc.getElementsByTagName("absolutePath").item(0).getChildNodes().item(0).getNodeValue();
            appConfigParams[1] = doc.getElementsByTagName("rasterDir").item(0).getChildNodes().item(0).getNodeValue();
            appConfigParams[2] = doc.getElementsByTagName("geoserverDataDir").item(0).getChildNodes().item(0).getNodeValue();
            appConfigParams[3] = doc.getElementsByTagName("geoserverUserName").item(0).getChildNodes().item(0).getNodeValue();
            appConfigParams[4] = doc.getElementsByTagName("geoserverPassword").item(0).getChildNodes().item(0).getNodeValue();
            appConfigParams[5] = doc.getElementsByTagName("geonetworkUserName").item(0).getChildNodes().item(0).getNodeValue();
            appConfigParams[6] = doc.getElementsByTagName("geonetworkPassword").item(0).getChildNodes().item(0).getNodeValue();
            appConfigParams[7] = doc.getElementsByTagName("postgresConection").item(0).getChildNodes().item(0).getNodeValue();
            appConfigParams[8] = doc.getElementsByTagName("postgresUserName").item(0).getChildNodes().item(0).getNodeValue();
            appConfigParams[9] = doc.getElementsByTagName("postgresPassword").item(0).getChildNodes().item(0).getNodeValue();
            appConfigParams[10] = doc.getElementsByTagName("geoserverLocation").item(0).getChildNodes().item(0).getNodeValue();
            appConfigParams[11] = doc.getElementsByTagName("geonetworkLocation").item(0).getChildNodes().item(0).getNodeValue();

        } catch (ParserConfigurationException | SAXException | IOException | DOMException e) {
            System.out.print("Unable to read application config, check if AppConfig.xml exist in application folder\n\r" + e);
            System.exit(1);
        }

        return appConfigParams;
    }

    /**
     * Parsovani mapoveho XML
     * @param MapConfigPath
     * @return 
     */
    public String[] ParseMapXML(String MapConfigPath) {
        String[] mapConfigParams = new String[3];

        try {
            File fXmlFile = new File(MapConfigPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            mapConfigParams[0] = doc.getElementsByTagName("Title").item(0).getChildNodes().item(0).getNodeValue();
            mapConfigParams[1] = doc.getElementsByTagName("WorkSpace").item(0).getChildNodes().item(0).getNodeValue();
            mapConfigParams[2] = doc.getElementsByTagName("EPSG").item(0).getChildNodes().item(0).getNodeValue();

        } catch (ParserConfigurationException | SAXException | IOException | DOMException e) {
            System.out.print("Unable to read map config, check if RasterName.mxml exist in raster import folder\n\r" + e);
            System.exit(2);
        }
        
        return mapConfigParams;
    }
}
