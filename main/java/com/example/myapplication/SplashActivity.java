package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import com.example.myapplication.ui.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setTheme(R.style.Theme_MyApplication);
        setContentView(R.layout.activity_splash);
        mProgressBar = findViewById(R.id.progressBar);

        // The progress bar animation
        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(2800);
        animator.addUpdateListener(valueAnimator -> {
            int progress = (int) valueAnimator.getAnimatedValue();
            mProgressBar.setProgress(progress);
        });
        animator.start();

     new Handler().postDelayed(() -> {
         Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
         startActivity(intent);
         finish();
     }, 3000);
}
}