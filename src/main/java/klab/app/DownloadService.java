package klab.app;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DownloadService {
    private Executor executor = Executors.newFixedThreadPool(4);


    public void downloadFile(String downloadNode, String downloadPort, String fileID, String fileName) {
        executor.execute(() -> {
            try {
                // Download file
            } catch (Exception e) {
                // Handle exception
            }
        });
    }

    public void uploadFile(String fileID, String fileName) {
        executor.execute(() -> {
            try {
                // Upload file
            } catch (Exception e) {
                // Handle exception
            }
        });
    }
}
