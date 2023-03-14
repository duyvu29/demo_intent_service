package com.example.demo_intent_service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadFileService extends IntentService {
    private static final String CHANNEL_ID = "2000";
    private NotificationManager notificationManager;
    public static final String URL_OF_FILE = "urlPath";
    public static final String FILENAME = "fileName";
    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder progressBuilder = null;
    private final int notificationId = 15;
    public static final String PROGRESS = "progress";
    public static final String NOTIFICATION = "INTENT SERVICE";

    public DownloadFileService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String urlPath = intent.getStringExtra(URL_OF_FILE);
        String fileName = intent.getStringExtra(FILENAME);

        createNotificationChannel();

        notificationManagerCompat = NotificationManagerCompat.from(this);
        progressBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        progressBuilder.setContentTitle("Download file")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_download)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        progressBuilder.setOngoing(true);

        // Xử lý đọc và ghi file từ url
        BufferedInputStream bfInputStream = null; // Luồng để đọc file từ URL
        OutputStream outputStream = null; // Luồng để ghi file vào thư mục DOWNLOAD
        HttpURLConnection httpURLConnection = null; // Giao thức HTTP để kết nối tới url

        try {
            URL url = new URL(urlPath);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            /*
            * - Khởi tạo đường dẫn thư mục chứa file và tạo mới file đó trong thư mục DOWNLOAD
            * - Ghi file từ BufferedInputStream bằng vòng lặp while
            * - Cập nhật giá trị hiện tại của progress qua việc tính toán
            * */
            int size = httpURLConnection.getContentLength();

            bfInputStream = new BufferedInputStream(url.openStream());

            File directory = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS + "/" + "zip_folder"
            );

            if (!directory.exists()) {
                directory.mkdir();
            }

            String destinationFilePath = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS + "/" + "zip_folder" + "/" + fileName
            ).getAbsolutePath();

            File file = new File(destinationFilePath);
            file.createNewFile();

            outputStream = new FileOutputStream(destinationFilePath);

            byte[] data = new byte[1024];
            int count;
            double sumCount = 0.0;

            while ((count = bfInputStream.read(data, 0, 1024)) != -1) {
                outputStream.write(data, 0, count);
                sumCount += count;
                if (size > 0) {
                    progressChange((int) (sumCount / size * 100.0));
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bfInputStream != null) {
                try {
                    bfInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Intent Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private void progressChange(int value) {
        if (value == 100) {
            progressBuilder.setProgress(100, 100, false);
            progressBuilder.setContentText("Download file completed.");
            notificationManager.notify(notificationId, progressBuilder.build());
        } else {
            progressBuilder.setProgress(100, value, false);
            progressBuilder.setContentText("File is downloading ..." + value + "%");
            notificationManager.notify(notificationId, progressBuilder.build());
        }
        Intent i = new Intent(NOTIFICATION);
        i.putExtra(PROGRESS, value);
        sendBroadcast(i);
    }
}
