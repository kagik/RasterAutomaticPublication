/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rasterautomaticpublication;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Docasna trida pro operace pres CURL v prikazove radce. Tato metoda bude nahrazena pravdepodobne knihovnou javacurl nebo knihovna apache.http.client
 * @author jhettler
 */
public class CurlCmdOperation {
    
        /**
         * Metoda pro operace CURL v prikazove radce - vyuzivana pro komunikaci s GeoServerem pres curl REST API - bude nahrazeno pravdepodobne knihovnou javacurl nebo apache.http.client
         * @param command
         * @throws IOException 
         */
        public void CmdCommand(String command) throws IOException {

        try {

            ProcessBuilder pb = new ProcessBuilder(
                    "cmd.exe",
                    "/u",
                    "/C",
                    command);

            pb.redirectErrorStream(false);

            Process p = pb.start();

            InputStream errorOutput = new BufferedInputStream(p.getErrorStream(), 10000);
            InputStream consoleOutput = new BufferedInputStream(p.getInputStream(), 10000);

            int ch;

            System.out.println("\r\n"
                    + "|------------------------------------------------------------------|\r\n"
                    + "|******************* Running CURL command *************************|\r\n"
                    + "|==================================================================|");
            System.out.println(command);
            System.out.println("\r\n|******************* Output ***************************************|");
            while ((ch = consoleOutput.read()) != -1) {
                System.out.print((char) ch );
            }

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }   
}
