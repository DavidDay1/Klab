package klab.app;

import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static klab.app.Node.logger;

/**
 * Class for downloading files
 * @version 1.0
 */

public class DownloadService {
    private ExecutorService executor = Executors.newFixedThreadPool(4);

    private HashMap<String, String> fileIDToNameList = new HashMap<String, String>();


/**
     * Set the file ID to name list
     * @param directory directory
     */
    public void setFileIDToNameList(File directory) {

        for (File f : directory.listFiles()) {
            String fileIdString = "";
            byte[] fileID = ByteBuffer.allocate(4).putInt(f.hashCode()).array();
            for (int i = 0; i < fileID.length; i++) {
                fileIdString += String.format("%02X", fileID[i]);
            }
            logger.info("File ID: " + fileIdString + " File Name: " + f.getName());
            fileIDToNameList.put(fileIdString, f.getName());
        }
    }


/**
     * Get the file ID to name list
     * @return the file ID to name list
     */
    public HashMap<String, String> getFileIDToNameList() {
        return fileIDToNameList;
    }


/**
     * Get the executor
     * @return the executor
     */
    public ExecutorService getExecutor() {
        return executor;
    }



/**
     * Download a file
     * @param args arguments
     * @param s socket
     * @return runnable
     */
    public Runnable download(String[] args, Socket s) {
        return () -> {
            try {
                InputStream is = s.getInputStream();
                OutputStream os = s.getOutputStream();
                //create file output stream with filename
                File file = new File(args[4]);
                os.write((args[3] + "\n").getBytes());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String response = reader.readLine();
                int i;
                logger.info("Received OK/Error: " + response);
                if ("OK".equals(response)) {
                    FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile() + "\\");
                    is.transferTo(fos);
                } else if ("ERROR".equals(response)) {
                    while ((i = is.read()) != -1) {
                        System.out.print( (char) i);
                    }
                }
                s.close();
            } catch (IOException e) {
                logger.info("Error downloading file: " + e.getMessage() + Arrays.toString(e.getStackTrace()));
            }
        };
    }

/**
     * Upload a file
     * @param out message output
     * @param fileID file ID
     * @param s socket
     * @param directory directory
     * @return runnable
     */

    public Runnable upload(OutputStream out, String fileID, Socket s, File directory) {
        return () -> {
            try {
                logger.info("Downloading file: inside upload" );
                setFileIDToNameList(directory);
                HashMap<String, String> fileIDToNameList = getFileIDToNameList();
                String filename = fileIDToNameList.get(fileID);
                logger.info("Downloading file with filename: " + filename);

                if (filename != null) {
                    List<File> downloadSearch = FileSearch.searchByName(directory, filename);
                    logger.info("uploading file: " + downloadSearch.get(0));
                    out.write("OK\n\n".getBytes());
                    FileInputStream fis = new FileInputStream(downloadSearch.get(0));
                    byte[] buffer = new byte[4096];
                    int j;
                    while ((j = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, j);
                    }
                    logger.info("Finished uploading file");
                    fis.close();
                    s.close();
                } else {
                    out.write("ERROR\n\n".getBytes());
                    out.write("Bad File ID: ".getBytes());
                    out.write(fileID.getBytes());
                    out.write("\n".getBytes());
                    s.close();
                }

            } catch (IOException e) {
                logger.info("Error uploading file: " + e.getMessage());
            }
        };
    }
}
