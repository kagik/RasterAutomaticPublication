package rasterautomaticpublication;

import java.io.*;

/**
 * Trida pro praci s rastry v prikazove radce z prostedi Java - casem nahrazeno GDAL Java
 * @author jhettler
 */
public class RasterCmdOperation {

    /**
     * Metoda pro spusteni prikazu gdalinfo (informace o rastru)
     * @param directory
     * @param rasterName
     * @return
     * @throws IOException 
     */
    public String[] GdalInfo(String directory, String rasterName) throws IOException {
        String[] Extent = new String[4];

        try {
            Runtime rt = Runtime.getRuntime();

            String command = "cmd /c gdalinfo " + rasterName;
            String[] env = null;

            File raster2impdir = new File(directory);

            Process pr = rt.exec(command, env, raster2impdir);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line = null;

            System.out.println("\r\n\r\n"
                    + "|------------------------------------------------------------------|\r\n"
                    + "|******************* GDALInfo *************************************|\r\n"
                    + "|==================================================================|");

            while ((line = input.readLine()) != null) {
                System.out.println(line);
                if (line.contains("Lower Left")) {
                    String[] ExtentTmp = line.substring(13).replaceAll("\\)", "").split(",");
                    Extent[0] = ExtentTmp[0].trim();
                    Extent[1] = ExtentTmp[1].trim().substring(0, 10);
                } else if (line.contains("Upper Right")) {
                    String[] ExtentTmp = line.substring(13).replaceAll("\\)", "").split(",");
                    String UR1 = ExtentTmp[0].trim();
                    String UR2 = ExtentTmp[1].trim().substring(0, 10);
                    Extent[2] = UR1;
                    Extent[3] = UR2;
                }
            }

            int exitVal = pr.waitFor();
            //System.out.println("Exited with error code " + exitVal);

            RasterCmdOperation Errors = new RasterCmdOperation();
            Errors.CmdErrors(exitVal, "gdalinfo");

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        return Extent;
    }
    
    /**
     * Metoda pro operace s chybami v prikazove radce
     * @param exitVal
     * @param dialogText
     * @throws IOException 
     */
    private void CmdErrors(int exitVal, String dialogText) throws IOException {

        if (exitVal != 0) {
            System.out.println("Error in module " + dialogText);
            System.exit(3);
        }
    }
    
    /**
     * Metoda pro obecne spousteni prikazu v prikazove radce 
     * @param directory
     * @param rasterName
     * @param command
     * @throws IOException 
     */
    public void CmdCommand(String directory, String rasterName, String command) throws IOException {
        try {

            File raster2impdir = new File(directory);

            ProcessBuilder pb = new ProcessBuilder(
                    "cmd.exe",
                    "/u",
                    "/C",
                    command);
            pb.directory(raster2impdir);

            pb.redirectErrorStream(true);

            Process p = pb.start();

            InputStream consoleOutput = new BufferedInputStream(p.getInputStream(), 10000);

            int ch;

            System.out.println("\r\n"
                    + "|------------------------------------------------------------------|\r\n"
                    + "|******************* Raster CMD operation *************************|\r\n"
                    + "|==================================================================|");
            System.out.println("Output:");
            while ((ch = consoleOutput.read()) != -1) {
                System.out.print((char) ch);
            }

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }
}
