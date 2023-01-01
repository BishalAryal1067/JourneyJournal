package com.example.journeyjournal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    ProgressBar progressBar;
    int progressCounter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //checking sign in status
        sharedPreferences = getSharedPreferences("rememberUser", MODE_PRIVATE);
        String uid = sharedPreferences.getString("uid", String.valueOf(MODE_PRIVATE));
        boolean check = sharedPreferences.getBoolean("signInState", Boolean.valueOf(String.valueOf(MODE_PRIVATE)));


        //Making splash screen occupy full screen size
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //tracking progress and redirecting to login activity
        progressBar = findViewById(R.id.progressBar);
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                progressCounter++;
                progressBar.setProgress(progressCounter);

                if (progressCounter == 100) {
                    timer.cancel();
                    if (check) {
                        startActivity(new Intent(SplashActivity.this, DashboardActivity.class).putExtra("uid", uid));
                    } else {
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    }
                    finish();
                }
            }
        };
        timer.schedule(timerTask, 0, 18);
    }
}