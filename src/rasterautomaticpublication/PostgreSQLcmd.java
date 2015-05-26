/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rasterautomaticpublication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Trida pro praci v databazi PostgreSQL
 * @author jhettler
 */
public class PostgreSQLcmd {

        /**
         * Metoda pro spousteni prikazu v databazi
         * @param queries
         * @param postgresConn
         * @param postgresUserName
         * @param postgresPassword 
         */
        public void RunSQLqueries (List queries, String postgresConn, String postgresUserName, String postgresPassword) {

        Connection con = null;
        Statement st = null;

        String url = postgresConn;
        String user = postgresUserName;
        String password = postgresPassword;

        try {

          con = DriverManager.getConnection(url, user, password);
          st = con.createStatement();       
          con.setAutoCommit(false);
          
          for (int i = 0;i< queries.size();i++){
              st.addBatch(queries.get(i).toString());
          }
          
          int counts[] = st.executeBatch();

          con.commit();

          System.out.println("Committed " + counts.length + " updates");

        } catch (SQLException ex) {

            System.out.println(ex.getNextException());
            
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex1) {
                    Logger lgr = Logger.getLogger(PostgreSQLcmd.class.getName());
                    lgr.log(Level.WARNING, ex1.getMessage(), ex1);
                }
            }

            Logger lgr = Logger.getLogger(PostgreSQLcmd.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);

        } finally {

            try {
 
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                Logger lgr = Logger.getLogger(PostgreSQLcmd.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }
        
        /**
         * Metoda modifikujici databazi a importujici rastry
         * @param ActualRaster
         * @param TileLevels
         * @param LayerName
         * @param AbsolutePath
         * @param RasterDir
         * @param SRID
         * @param postgresConn
         * @param postgresUserName
         * @param postgresPassword
         * @throws IOException 
         */
        public void ImportRasterModifDB (String ActualRaster, String[] TileLevels, String LayerName, String AbsolutePath, String RasterDir, String SRID, String postgresConn, String postgresUserName, String postgresPassword) throws IOException {
            
            String[] ActualRasterNoExtArray = ActualRaster.split("\\.");
            String ActualRasterNoExt = ActualRasterNoExtArray[0].toString();
            String Extension = ActualRasterNoExtArray[1].toString().toLowerCase();
            String AsGdalRaster;

            if (Extension.equals("tif") || Extension.equals("tiff")) {
            AsGdalRaster = "ST_asTIFF(rast,ARRAY[1,2,3])";
            } else {
            AsGdalRaster = "ST_asPNG(rast)";
            }
            
            PostgreSQLcmd SQLcmd = new PostgreSQLcmd();

            List QueriesList = new ArrayList();

            QueriesList.add("DROP TABLE IF EXISTS " + ActualRasterNoExt);

            for (int k = 0; k < TileLevels.length; k++) {
                QueriesList.add("DROP TABLE IF EXISTS o_" + TileLevels[k] + "_" + ActualRasterNoExt);
            }

            QueriesList.add("CREATE TABLE IF NOT EXISTS mastertable(name CHARACTER (64)  NOT NULL,SpatialTable VARCHAR (128)  NOT NULL,TileTable VARCHAR (128)  NOT NULL,resX FLOAT8,resY FLOAT8,minX FLOAT8,minY FLOAT8,maxX FLOAT8,maxY FLOAT8,CONSTRAINT MASTERTABLE_PK PRIMARY KEY (name,SpatialTable,TileTable))");
            QueriesList.add("DELETE from mastertable where \"name\" = '" + LayerName + "'");
            QueriesList.add("INSERT INTO mastertable (name, spatialtable, tiletable) values ('" + LayerName + "','" + ActualRasterNoExt + "','" + ActualRasterNoExt + "')");

            for (int l = 0; l < TileLevels.length; l++) {
                QueriesList.add("INSERT INTO mastertable (name, spatialtable, tiletable) values ('" + LayerName + "','o_" + TileLevels[l] + "_" + ActualRasterNoExt + "','o_" + TileLevels[l] + "_" + ActualRasterNoExt + "')");
            }
         
            //String[] queries = {QueriesList};

            System.out.println("\r\n"
                    + "|------------------------------------------------------------------|\r\n"
                    + "|******************* Preparing database ***************************|\r\n"
                    + "|==================================================================|");               
            SQLcmd.RunSQLqueries(QueriesList, postgresConn, postgresUserName, postgresPassword);
            
            QueriesList.clear();

            RasterCmdOperation CmdOp = new RasterCmdOperation();

            System.out.println("\r\n"
                    + "|------------------------------------------------------------------|\r\n"
                    + "|******************* Importing raster(s) **************************|\r\n"
                    + "|==================================================================|");   
       
            CmdOp.CmdCommand(AbsolutePath + RasterDir, RasterDir, "raster2pgsql -C -e -Y -F -s "+ SRID +" -t 256x256 -l 2,4 " + ActualRaster + " | psql -d "+postgresConn.substring(postgresConn.lastIndexOf("/")+1)+" -U "+postgresUserName);
            
            QueriesList.add("ALTER TABLE " + ActualRasterNoExt + " add column data bytea, add column geom geometry(MULTIPOLYGON," + SRID + ")");
            QueriesList.add("DROP INDEX IF EXISTS IX_" + ActualRasterNoExt + "");
            QueriesList.add("CREATE INDEX IX_"+ ActualRasterNoExt + " ON " + ActualRasterNoExt + " USING gist(geom)");
            QueriesList.add("update "+ ActualRasterNoExt + " set data = "+AsGdalRaster);
            QueriesList.add("update "+ ActualRasterNoExt + " set geom = ST_Multi(ST_Envelope(rast))");
            
            for (int m=0;m<TileLevels.length;m++){
                QueriesList.add("ALTER TABLE o_"+ TileLevels[m] + "_" + ActualRasterNoExt + " add column data bytea, add column geom geometry(MULTIPOLYGON," + SRID + ")");
                QueriesList.add("DROP INDEX IF EXISTS IX_o_"+ TileLevels[m] + "_" + ActualRasterNoExt);
                QueriesList.add("CREATE INDEX IX_o_"+ TileLevels[m] + "_" + ActualRasterNoExt + " ON o_"+ TileLevels[m] + "_" + ActualRasterNoExt + " USING gist(geom)");
                QueriesList.add("update o_"+ TileLevels[m] + "_"+ ActualRasterNoExt + " set data = "+AsGdalRaster);
                QueriesList.add("update o_"+ TileLevels[m] + "_"+ ActualRasterNoExt + " set geom = ST_Multi(ST_Envelope(rast))");
           }

            System.out.println("\r\n"
                    + "|------------------------------------------------------------------|\r\n"
                    + "|******************* PostProcessing raster data *******************|\r\n"
                    + "|==================================================================|"); 
            SQLcmd.RunSQLqueries(QueriesList, postgresConn, postgresUserName, postgresPassword);
        }
    }