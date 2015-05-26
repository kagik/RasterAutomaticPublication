package mtdtautomaticpublication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Trida pro operace GeoNetworku
 * @author jhettler
 */
public class GeoNetworkPostReq {

    /**
     * Metoda pro prihlaseni uzivatele do GeoNetworku pomoci GeoNetwork metadata services operace "user.login"
     * @param UserName
     * @param Password
     * @param geonetworkLocation
     * @return GeoNetwork SID
     */
    public String userLogin(String UserName, String Password, String geonetworkLocation) {
        String CookieValue = "";

        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(geonetworkLocation + "/srv/en/xml.user.login?");

            StringEntity input = new StringEntity("<request><username>" + UserName + "</username><password>" + Password + "</password></request>");
            input.setContentType("application/xml");
            postRequest.setEntity(input);

            HttpResponse response = httpClient.execute(postRequest);
            CookieStore GNCookie = httpClient.getCookieStore();
            String CookieName = httpClient.getCookieStore().getCookies().get(0).getName().toString();
            CookieValue = httpClient.getCookieStore().getCookies().get(0).getValue().toString();

            System.out.println("\r\n"
                    + "|------------------------------------------------------------------|\r\n"
                    + "|******************* GeoNetwork User SID **************************|\r\n"
                    + "|==================================================================|");
            System.out.println(CookieName + ":" + CookieValue);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));

            String output;

            System.out.println("\r\n"
                    + "|------------------------------------------------------------------|\r\n"
                    + "|******************* GeoNetwork User Login ************************|\r\n"
                    + "|==================================================================|");

            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            httpClient.getConnectionManager().shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return CookieValue;
    }
    
    /**
    * Metoda pro odhlaseni aktualni uzivatele
    * @param SID
    * @param geonetworkLocation 
    */
    public void userLogout(String SID, String geonetworkLocation) {

        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost(
                    geonetworkLocation + "/srv/en/xml.user.logout?");

            postRequest.setHeader("JSESSIONID", SID);

            HttpResponse response = httpClient.execute(postRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String output;
            System.out.println(""
                    + "|------------------------------------------------------------------|\r\n"
                    + "|******************* GeoNetwork User Logout ***********************|\r\n"
                    + "|==================================================================|");
            //System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            httpClient.getConnectionManager().shutdown();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
/**
 * Metoda pro odeslani metadt pro operaci metadata.insert s prihlasenym uzivatelem
 * @param SID
 * @param ActualRasterNoExt
 * @param ActualPublishLayer
 * @param WorkSpace
 * @param Extent
 * @param geonetworkLocation
 * @throws Exception 
 */
    public void multiFormPost(String SID, String ActualRasterNoExt, String ActualPublishLayer, String WorkSpace, String[] Extent, String geonetworkLocation) throws Exception {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(geonetworkLocation + "/srv/en/xml.metadata.insert");
        System.out.println("Requesting : " + httppost.getURI());

        GeoNetworkPostReq mtdtstr = new GeoNetworkPostReq();
        String data = mtdtstr.prepareMtdt(ActualRasterNoExt, ActualPublishLayer, WorkSpace, Extent);

        try {
            StringEntity entity = new StringEntity(data, "text/xml", "ISO-8859-1");

            httppost.addHeader("Accept", "text/xml");
            httppost.setEntity(entity);
            httppost.setHeader("Cookie", "JSESSIONID=" + SID);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httppost, responseHandler);

            System.out.println("\r\n"
                    + "|------------------------------------------------------------------|\r\n"
                    + "|******************* GeoNetwork Metadata insert ***********************|\r\n"
                    + "|==================================================================|");
            System.out.println("responseBody : " + responseBody);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

    /**
     * Prozatimni string pro publikaci metadat, externi soubor metadat prislusny k rastru zatim neni pouzivan, presto, ze je validovana jeho existence na zacatku aplikace. Externi soubor s metadaty muze obsahovat v teto verzi aplikace cokoliv
     * @param ActualRasterNoExt
     * @param ActualPublishLayer
     * @param WorkSpace
     * @param Extent
     * @return 
     * @see mtdtautomaticpublication.GeoNetworkPostReq
     */
    public String prepareMtdt(String ActualRasterNoExt, String ActualPublishLayer, String WorkSpace, String[] Extent) {

        //Aktualni metadatovy zaznam, ve kterem se meni obsah posle ziskanych informaci z rastru a aktualni informace o vrstve
        String MTDT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<gmd:MD_Metadata xmlns:gts=\"http://www.isotc211.org/2005/gts\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\">"
                + "<gmd:fileIdentifier xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" xmlns:srv=\"http://www.isotc211.org/2005/srv\">"
                + "<gco:CharacterString>dc3669ee-843d-4d9a-9b6f-693ea61b7576_" + ActualPublishLayer + "</gco:CharacterString>"
                + "</gmd:fileIdentifier>"
                + "<gmd:language><gco:CharacterString>cze</gco:CharacterString></gmd:language>"
                + "<gmd:characterSet><gmd:MD_CharacterSetCode codeListValue=\"utf8\" codeList=\"./resources/codeList.xml#MD_CharacterSetCode\"/></gmd:characterSet>"
                + "<gmd:contact><gmd:CI_ResponsibleParty><gmd:individualName><gco:CharacterString>Jakub Hettler</gco:CharacterString></gmd:individualName>"
                + "<gmd:organisationName gco:nilReason=\"missing\"><gco:CharacterString/></gmd:organisationName>"
                + "<gmd:positionName gco:nilReason=\"missing\"><gco:CharacterString/></gmd:positionName>"
                + "<gmd:contactInfo><gmd:CI_Contact><gmd:phone><gmd:CI_Telephone><gmd:voice gco:nilReason=\"missing\"><gco:CharacterString/></gmd:voice><gmd:facsimile gco:nilReason=\"missing\"><gco:CharacterString/></gmd:facsimile></gmd:CI_Telephone></gmd:phone><gmd:address><gmd:CI_Address><gmd:deliveryPoint gco:nilReason=\"missing\"><gco:CharacterString/></gmd:deliveryPoint><gmd:city gco:nilReason=\"missing\"><gco:CharacterString/></gmd:city><gmd:administrativeArea gco:nilReason=\"missing\"><gco:CharacterString/></gmd:administrativeArea><gmd:postalCode gco:nilReason=\"missing\"><gco:CharacterString/></gmd:postalCode><gmd:country gco:nilReason=\"missing\"><gco:CharacterString/></gmd:country><gmd:electronicMailAddress><gco:CharacterString>jakub.hetter@gmail.com</gco:CharacterString></gmd:electronicMailAddress></gmd:CI_Address></gmd:address></gmd:CI_Contact></gmd:contactInfo><gmd:role><gmd:CI_RoleCode codeListValue=\"pointOfContact\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode\"/></gmd:role></gmd:CI_ResponsibleParty></gmd:contact><gmd:dateStamp><gco:DateTime xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" xmlns:srv=\"http://www.isotc211.org/2005/srv\">2012-07-28T14:37:38</gco:DateTime></gmd:dateStamp><gmd:metadataStandardName><gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString></gmd:metadataStandardName><gmd:metadataStandardVersion><gco:CharacterString>1.0</gco:CharacterString></gmd:metadataStandardVersion><gmd:referenceSystemInfo><gmd:MD_ReferenceSystem><gmd:referenceSystemIdentifier><gmd:RS_Identifier><gmd:code><gco:CharacterString>WGS 1984</gco:CharacterString></gmd:code></gmd:RS_Identifier></gmd:referenceSystemIdentifier></gmd:MD_ReferenceSystem></gmd:referenceSystemInfo><gmd:identificationInfo><gmd:MD_DataIdentification><gmd:citation><gmd:CI_Citation><gmd:title><gco:CharacterString>Metadata vrstvy " + ActualPublishLayer + "</gco:CharacterString></gmd:title><gmd:date><gmd:CI_Date><gmd:date><gco:DateTime>2012-07-27T22:02:00</gco:DateTime></gmd:date><gmd:dateType><gmd:CI_DateTypeCode codeListValue=\"publication\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode\"/></gmd:dateType></gmd:CI_Date></gmd:date><gmd:edition gco:nilReason=\"missing\"><gco:CharacterString/></gmd:edition><gmd:presentationForm><gmd:CI_PresentationFormCode codeListValue=\"mapDigital\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#CI_PresentationFormCode\"/></gmd:presentationForm></gmd:CI_Citation></gmd:citation><gmd:abstract><gco:CharacterString>The ISO19115 metadata standard is the preferred metadata standard to use. If unsure what templates to start with, use this one.</gco:CharacterString></gmd:abstract><gmd:purpose gco:nilReason=\"missing\"><gco:CharacterString/></gmd:purpose><gmd:status><gmd:MD_ProgressCode codeListValue=\"onGoing\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_ProgressCode\"/></gmd:status><gmd:pointOfContact><gmd:CI_ResponsibleParty><gmd:individualName><gco:CharacterString>Jakub Hettler</gco:CharacterString></gmd:individualName><gmd:organisationName gco:nilReason=\"missing\"><gco:CharacterString/></gmd:organisationName><gmd:positionName gco:nilReason=\"missing\"><gco:CharacterString/></gmd:positionName><gmd:contactInfo><gmd:CI_Contact><gmd:phone><gmd:CI_Telephone><gmd:voice gco:nilReason=\"missing\"><gco:CharacterString/></gmd:voice><gmd:facsimile gco:nilReason=\"missing\"><gco:CharacterString/></gmd:facsimile></gmd:CI_Telephone></gmd:phone><gmd:address><gmd:CI_Address><gmd:deliveryPoint gco:nilReason=\"missing\"><gco:CharacterString/></gmd:deliveryPoint><gmd:city gco:nilReason=\"missing\"><gco:CharacterString/></gmd:city><gmd:administrativeArea gco:nilReason=\"missing\"><gco:CharacterString/></gmd:administrativeArea><gmd:postalCode gco:nilReason=\"missing\"><gco:CharacterString/></gmd:postalCode><gmd:country gco:nilReason=\"missing\"><gco:CharacterString/></gmd:country><gmd:electronicMailAddress><gco:CharacterString>jakub.hettler@gmail.com</gco:CharacterString></gmd:electronicMailAddress></gmd:CI_Address></gmd:address></gmd:CI_Contact></gmd:contactInfo><gmd:role><gmd:CI_RoleCode codeListValue=\"originator\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode\"/></gmd:role></gmd:CI_ResponsibleParty></gmd:pointOfContact><gmd:resourceMaintenance><gmd:MD_MaintenanceInformation><gmd:maintenanceAndUpdateFrequency><gmd:MD_MaintenanceFrequencyCode codeListValue=\"asNeeded\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_MaintenanceFrequencyCode\"/></gmd:maintenanceAndUpdateFrequency></gmd:MD_MaintenanceInformation></gmd:resourceMaintenance><gmd:graphicOverview xmlns:srv=\"http://www.isotc211.org/2005/srv\"><gmd:MD_BrowseGraphic><gmd:fileName><gco:CharacterString>start_rgb_s.png</gco:CharacterString></gmd:fileName><gmd:fileDescription><gco:CharacterString>thumbnail</gco:CharacterString></gmd:fileDescription><gmd:fileType><gco:CharacterString>png</gco:CharacterString></gmd:fileType></gmd:MD_BrowseGraphic></gmd:graphicOverview><gmd:graphicOverview xmlns:srv=\"http://www.isotc211.org/2005/srv\"><gmd:MD_BrowseGraphic><gmd:fileName><gco:CharacterString>start_rgb.png</gco:CharacterString></gmd:fileName><gmd:fileDescription><gco:CharacterString>large_thumbnail</gco:CharacterString></gmd:fileDescription><gmd:fileType><gco:CharacterString>png</gco:CharacterString></gmd:fileType></gmd:MD_BrowseGraphic></gmd:graphicOverview><gmd:descriptiveKeywords><gmd:MD_Keywords><gmd:keyword><gco:CharacterString>Test keyword</gco:CharacterString></gmd:keyword><gmd:type><gmd:MD_KeywordTypeCode codeListValue=\"theme\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode\"/></gmd:type></gmd:MD_Keywords></gmd:descriptiveKeywords><gmd:descriptiveKeywords><gmd:MD_Keywords><gmd:keyword><gco:CharacterString>World</gco:CharacterString></gmd:keyword><gmd:type><gmd:MD_KeywordTypeCode codeListValue=\"place\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode\"/></gmd:type></gmd:MD_Keywords></gmd:descriptiveKeywords><gmd:resourceConstraints><gmd:MD_LegalConstraints><gmd:accessConstraints><gmd:MD_RestrictionCode codeListValue=\"copyright\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode\"/></gmd:accessConstraints><gmd:useConstraints><gmd:MD_RestrictionCode codeListValue=\"\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode\"/></gmd:useConstraints><gmd:otherConstraints gco:nilReason=\"missing\"><gco:CharacterString/></gmd:otherConstraints></gmd:MD_LegalConstraints></gmd:resourceConstraints><gmd:spatialRepresentationType><gmd:MD_SpatialRepresentationTypeCode codeListValue=\"vector\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_SpatialRepresentationTypeCode\"/></gmd:spatialRepresentationType><gmd:spatialResolution><gmd:MD_Resolution><gmd:equivalentScale><gmd:MD_RepresentativeFraction><gmd:denominator><gco:Integer>750000</gco:Integer></gmd:denominator></gmd:MD_RepresentativeFraction></gmd:equivalentScale></gmd:MD_Resolution></gmd:spatialResolution><gmd:language><gco:CharacterString>eng</gco:CharacterString></gmd:language><gmd:characterSet><gmd:MD_CharacterSetCode codeListValue=\"utf8\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode\"/></gmd:characterSet><gmd:topicCategory><gmd:MD_TopicCategoryCode>boundaries</gmd:MD_TopicCategoryCode></gmd:topicCategory><gmd:extent><gmd:EX_Extent><gmd:geographicElement><gmd:EX_GeographicBoundingBox><gmd:westBoundLongitude><gco:Decimal>" + Extent[0] + "</gco:Decimal></gmd:westBoundLongitude><gmd:eastBoundLongitude><gco:Decimal>" + Extent[2] + "</gco:Decimal></gmd:eastBoundLongitude><gmd:southBoundLatitude><gco:Decimal>" + Extent[1] + "</gco:Decimal></gmd:southBoundLatitude><gmd:northBoundLatitude><gco:Decimal>" + Extent[3] + "</gco:Decimal></gmd:northBoundLatitude></gmd:EX_GeographicBoundingBox></gmd:geographicElement></gmd:EX_Extent></gmd:extent></gmd:MD_DataIdentification></gmd:identificationInfo>"
                + "<gmd:distributionInfo><gmd:MD_Distribution><gmd:distributionFormat><gmd:MD_Format><gmd:name>"
                + "<gco:CharacterString>WMS, KML</gco:CharacterString></gmd:name><gmd:version><gco:CharacterString>1.1.1,2.0</gco:CharacterString></gmd:version></gmd:MD_Format>"
                + "</gmd:distributionFormat><gmd:transferOptions>"
                + "<gmd:MD_DigitalTransferOptions>"
                + "<gmd:onLine>"
                + "<gmd:CI_OnlineResource><gmd:linkage>"
                + "<gmd:URL><![CDATA[http://89.233.190.150:8082/geoserver/" + WorkSpace + "/wms?version=1.1.1&amp;layer=" + ActualPublishLayer + "]]></gmd:URL>"
                + "</gmd:linkage><gmd:protocol><gco:CharacterString>WWW:LINK-1.0-http--link</gco:CharacterString></gmd:protocol><gmd:name gco:nilReason=\"missing\"><gco:CharacterString/></gmd:name><gmd:description gco:nilReason=\"missing\"><gco:CharacterString/></gmd:description>"
                + "</gmd:CI_OnlineResource>"
                + "</gmd:onLine>"
                + "<gmd:onLine><gmd:CI_OnlineResource><gmd:linkage xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" xmlns:srv=\"http://www.isotc211.org/2005/srv\"><gmd:URL>http://localhost:8080/geonetwork/srv/en/resources.get?id=39amp;fname=amp;access=private</gmd:URL></gmd:linkage><gmd:protocol><gco:CharacterString>WWW:DOWNLOAD-1.0-http--download</gco:CharacterString></gmd:protocol><gmd:name xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" xmlns:srv=\"http://www.isotc211.org/2005/srv\"><gmx:MimeFileType type=\"\"/></gmd:name><gmd:description><gco:CharacterString/></gmd:description></gmd:CI_OnlineResource></gmd:onLine>"
                + "<gmd:onLine><gmd:CI_OnlineResource><gmd:linkage><gmd:URL><![CDATA[http://89.233.190.150:8082/geoserver/" + WorkSpace + "/wms?version=1.1.1&amp;layer=" + ActualPublishLayer + "]]></gmd:URL></gmd:linkage><gmd:protocol><gco:CharacterString>OGC:WMS-1.1.1-http-get-map</gco:CharacterString></gmd:protocol><gmd:name gco:nilReason=\"missing\"><gco:CharacterString/></gmd:name><gmd:description gco:nilReason=\"missing\"><gco:CharacterString/></gmd:description></gmd:CI_OnlineResource></gmd:onLine>"
                + "<gmd:onLine><gmd:CI_OnlineResource><gmd:linkage><gmd:URL>http://89.233.190.150:8082/geoserver/" + WorkSpace + "/wms/kml?layers=" + ActualPublishLayer + "</gmd:URL></gmd:linkage><gmd:protocol><gco:CharacterString>OGC:KML</gco:CharacterString></gmd:protocol><gmd:name gco:nilReason=\"missing\"><gco:CharacterString/></gmd:name><gmd:description gco:nilReason=\"missing\"><gco:CharacterString/></gmd:description></gmd:CI_OnlineResource></gmd:onLine>"
                + "</gmd:MD_DigitalTransferOptions>"
                + "</gmd:transferOptions>"
                + "</gmd:MD_Distribution>"
                + "</gmd:distributionInfo><gmd:dataQualityInfo><gmd:DQ_DataQuality><gmd:scope><gmd:DQ_Scope><gmd:level><gmd:MD_ScopeCode codeListValue=\"dataset\" codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode\"/></gmd:level></gmd:DQ_Scope></gmd:scope><gmd:lineage><gmd:LI_Lineage><gmd:statement><gco:CharacterString>Test</gco:CharacterString></gmd:statement></gmd:LI_Lineage></gmd:lineage></gmd:DQ_DataQuality></gmd:dataQualityInfo></gmd:MD_Metadata>";

        //String, ktery je predavan operaci metadata.insert v metode MultiFormPost. Ovlicneni vlastnosti operace metadata.insert
        String requestData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<request>"
                + "<group>2</group>"
                + "<category>_none_</category>"
                + "<styleSheet>_none_</styleSheet>"
                + "<uuidAction>overwrite</uuidAction>" /*
                 * generateUUID
                 */
                + "<data>" + (MTDT.replaceAll("<", "&lt;")).replaceAll(">", "&gt;&#xD;") + "</data>"
                + "</request>";

        return requestData;
    }
}
