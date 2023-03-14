package com.example.demo_intent_service;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button btnDownloadFile;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDownloadFile = findViewById(R.id.btnDownloadFile);
        tvStatus = findViewById(R.id.tvStatus);

        btnDownloadFile.setOnClickListener(v -> {
            Intent i = new Intent(this, DownloadFileService.class);
            /*i.putExtra(DownloadFileService.FILENAME, "winrarFile.exe");
            i.putExtra(DownloadFileService.URL_OF_FILE, "https://fc.getpedia.net/data/?q===gM4QDN3ATO2kjN1YTM0EDOzYDfwUjM1wXZ4VmLxIjNtQjN41ichJnbpd3LxAzLzAzLzIDMy8SZslmZvEGdhR2L&e=o");*/
            i.putExtra(DownloadFileService.FILENAME, "file1234.jpg");
            i.putExtra(DownloadFileService.URL_OF_FILE, "https://cdn.pixabay.com/photo/2023/02/28/03/42/ibex-7819817_960_720.jpg");
            startService(i);
        });
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Bundle bundle = intent.getExtras();
            if (intent != null) {
                int value = intent.getIntExtra(DownloadFileService.PROGRESS, -1);
                if (value == 100) {
                    tvStatus.setText("Download file completed.");
                } else {
                    tvStatus.setText("Downloading file, progress: " + value + "%");
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(DownloadFileService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}