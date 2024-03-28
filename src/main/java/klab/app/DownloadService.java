package klab.app;

import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static klab.app.Node.logger;

/**
 * Class for downloading files
 * @version 1.0
 */

public class DownloadService {
    private Executor executor = Executors.newFixedThreadPool(4);

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
    public Executor getExecutor() {
        return executor;
    }



/**
     * Download a file
     * @param out message output
     * @param in message input
     * @param args arguments
     * @param s socket
     * @return runnable
     */
    public Runnable download(MessageOutput out, MessageInput in, String[] args, Socket s) {
        return () -> {
            try {
                //create file output stream with filename
                logger.info("Downloading file: inside download");
                File file = new File(args[4]);
                logger.info("Downloading file: " + file);
                FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile() + "\\");
                logger.info("Creating FileOutputStream");
                logger.info("Downloading file: " + file + " with fileID: " + args[3]);
                out.writeString(args[3] + "\n");
                int i;
                String result = in.readString();
                logger.info("Received OK/Error: " + result);
                if (result.equals("OK")){
                    logger.info("Downloading file: inside download OK");
                    //get rid of /n's
                    in.read();
                    in.read();
                    while ((i = in.read()) != -1) {
                        fos.write(i);
                    }
                } else if (result.equals("ERROR")) {
                    logger.info("Downloading file: inside download ERROR");
                    while ((i = in.read()) != -1) {
                        System.out.print((char) i);
                    }
                }
                logger.info("Finished downloading file");
                s.close();
            } catch (IOException e) {
                logger.info("Error downloading file: " + e.getMessage());
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

    public Runnable upload(MessageOutput out, String fileID, Socket s, File directory) {
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
                    out.writeString("OK\n\n");
                    FileInputStream fis = new FileInputStream(downloadSearch.get(0));
                    int j;
                    while ((j = fis.read()) != -1) {
                        out.write(j);
                    }
                    logger.info("Finished uploading file");
                    fis.close();
                    s.close();
                } else {
                    out.writeString("ERROR\n\n");
                    out.writeString("Bad File ID: " + fileID + "\n");
                    s.close();
                }

            } catch (IOException e) {
                logger.info("Error uploading file: " + e.getMessage());
            }
        };
    }
}
