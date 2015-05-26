/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rasterautomaticpublication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
/**
 * Trida pro praci se soubory
 * @author jhettler
 */
public class FilesInDir {
    /**
     * Metoda ziskavajici vsechny soubory ve slozce
     * @param directory
     * @throws IOException 
     */
    public void GetFiles(String directory) throws IOException {

        File dir = new File(directory);

        List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        for (File file : files) {
        }
    }

    /**
     * Metoda ziskavaji rastry podle pripony rastru vyjmenovane v poli extensions
     * @param directory
     * @return
     * @throws IOException 
     */
    public List GetRasters(String directory) throws IOException {

        File dir = new File(directory);
        String[] extensions = {"png", "jpg", "TIF", "tif"};
        List RasterList = new ArrayList();

        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, false);

        for (File file : files) {
            int i = files.indexOf(file);
            RasterList.add(file.getName());
        }

        return RasterList;
    }

    /**
     * Metoda kontrolujici vsechny potrebne soubory, ktere by mely byt pritomne s rastrem, kontroluje se podle pripony vyjmenovane v checkExtensions
     * @param AbsolutePath
     * @param rasterName
     * @return
     * @throws IOException 
     */
    public String[] ExistNecessaryFiles(String AbsolutePath, String rasterName) throws IOException {

        String[] booleanResult = new String[3];

        String checkName = rasterName.split("\\.")[0];
        String[] checkExtensions = {".mxml", ".xml", ".wld"};


        File mtdtFile = new File(AbsolutePath + "\\" + checkName + checkExtensions[0]);
        if (!mtdtFile.exists()) {
            booleanResult[0] = "false";
        } else {
            booleanResult[0] = "true";
        }

        File wldFile = new File(AbsolutePath + "\\" + checkName + checkExtensions[1]);
        if (!wldFile.exists()) {
            booleanResult[1] = "false";
        } else {
            booleanResult[1] = "true";
        }

        File mxmlFile = new File(AbsolutePath + "\\" + checkName + checkExtensions[1]);
        if (!mxmlFile.exists()) {
            booleanResult[2] = "false";
        } else {
            booleanResult[2] = "true";
        }

        return booleanResult;
    }

    /**
     * Metoda vracejici seznam rastru, ktere jsou platne a maji vsechny potrebne soubory
     * @param RasterDir
     * @param AbsolutePath
     * @return
     * @throws IOException 
     */
    public List VerifiedRaster2Imp(String RasterDir, String AbsolutePath) throws IOException {

        FilesInDir RasterList = new FilesInDir();
        List Rastry = RasterList.GetRasters(AbsolutePath + RasterDir);

        List VerifiedRasters = new ArrayList();
        List UnVerifiedRasters = new ArrayList();

        for (int i = 0; i < Rastry.size(); i++) {

            boolean wldOk = Boolean.parseBoolean(RasterList.ExistNecessaryFiles(AbsolutePath + RasterDir, Rastry.get(i).toString())[1]);
            boolean mtdtOk = Boolean.parseBoolean(RasterList.ExistNecessaryFiles(AbsolutePath + RasterDir, Rastry.get(i).toString())[0]);
            boolean mapConfigOk = Boolean.parseBoolean(RasterList.ExistNecessaryFiles(AbsolutePath + RasterDir, Rastry.get(i).toString())[2]);

            if (!wldOk || !mtdtOk || !mapConfigOk) {
                UnVerifiedRasters.add(Rastry.get(i).toString());
            } else {
                VerifiedRasters.add(Rastry.get(i).toString());
            }
        }

        //System.out.println("Rastry bez WLD souborů:" + UnVerifiedRasters);
        return (List) VerifiedRasters;
    }

    /**
     * Metoda presouvajici importovane rastry do slozky imported, pokud existuje
     * @param ActualRasterNoExt
     * @param ActualRasterDest
     * @param RasterDir
     * @param AbsolutePath
     * @throws IOException 
     */
    public void MoveFile(String ActualRasterNoExt, String ActualRasterDest, String RasterDir, String AbsolutePath) throws IOException {

        String[] RasterFiles = {".mxml", ".xml", ".wld"};

        File file = new File(AbsolutePath + RasterDir + "\\" + ActualRasterDest);

        File dir = new File(AbsolutePath + RasterDir + "\\imported\\");

        boolean success = file.renameTo(new File(dir, file.getName()));
        if (!success) {
            System.out.println("Unable to move imported files, check if folder imported exists, or if folder \"imported\" does not contain imported files of the same name");
        }

        for (int i = 0; i < RasterFiles.length; i++) {
            File file2 = new File(AbsolutePath + RasterDir + "\\" + ActualRasterNoExt + RasterFiles[i]);

            boolean success2 = file2.renameTo(new File(dir, file2.getName()));
            if (!success2) {
                System.out.println("Unable to move imported files, check if folder imported exists, or if folder \"imported\" does not contain imported files of the same name");
            }
        }
    }
    
        public void deleteFile(String Path) {
        String fileName = "file.txt";
        // A File object to represent the filename
        File f = new File(Path);

        // Make sure the file or directory exists and isn't write protected
        if (!f.exists()) {
            throw new IllegalArgumentException(
                    "Delete: no such file or directory: " + fileName);
        }

        if (!f.canWrite()) {
            throw new IllegalArgumentException("Delete: write protected: "
                    + fileName);
        }

        // If it is a directory, make sure it is empty
        if (f.isDirectory()) {
            String[] files = f.list();
            if (files.length > 0) {
                throw new IllegalArgumentException(
                        "Delete: directory not empty: " + fileName);
            }
        }

        // Attempt to delete it
        boolean success = f.delete();

        if (!success) {
            throw new IllegalArgumentException("Delete: deletion failed");
        }
    }
}
