package klab.app;

import klab.serialization.MessageInput;
import klab.serialization.MessageOutput;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static klab.app.Node.logger;

public class DownloadService {
    private Executor executor = Executors.newFixedThreadPool(4);

    public Executor getExecutor() {
        return executor;
    }


    public Runnable download(MessageOutput out, MessageInput in, String[] args, Socket s) {
        return () -> {
            try {
                //create file output stream with filename
                logger.info("Downloading file: inside download");
                File file = new File(args[4]);
                logger.info("Downloading file: " + file);
                FileOutputStream fos = new FileOutputStream(file.getAbsoluteFile());
                logger.info("Creating FileOutputStream");
                byte[] fileID = args[3].getBytes();
                logger.info("Downloading file: " + file + " with fileID: " + Arrays.toString(fileID));
                out.write(fileID);
                int i;
                String result = in.readString();
                if (result.equals("OK\n")){
                    while ((i = in.read()) != -1) {
                        fos.write(i);
                    }
                } else if (result.equals("ERROR\n")) {
                    while ((i = in.read()) != -1) {
                        System.out.print((char) i);
                    }
                    s.close();
                }
            } catch (IOException e) {
                logger.info("Error downloading file: " + e.getMessage());
            }
        };
    }

    public Runnable upload(MessageOutput out, byte[] fileID, Socket s, File directory) {
        return () -> {
            try {
                logger.info("Downloading file: inside upload" );
                File downloadSearch = FileSearch.searchByID(directory, fileID);

                if (downloadSearch != null) {
                    logger.info("uploading file: " + downloadSearch);
                    out.writeString("OK\n");
                    FileInputStream fis = new FileInputStream(downloadSearch.getAbsoluteFile());
                    int j;
                    while ((j = fis.read()) != -1) {
                        out.write(j);
                    }
                    fis.close();
                    s.close();
                } else {
                    out.writeString("ERROR\n");
                    out.writeString("Bad File ID: " + Arrays.toString(fileID) + "\n");
                    s.close();
                }

            } catch (IOException e) {
                logger.info("Error uploading file: " + e.getMessage());
            }
        };
    }
}
