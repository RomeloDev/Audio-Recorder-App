package com.example.audiorecorderapp;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.io.File;
import java.io.IOException;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button btnRecord, btnPause, btnStop, btnPlay;
    private TextView timerTextView;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private Handler timerHandler;
    private long startTime, pauseOffset = 0;
    private boolean isRecording = false;
    private boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRecord = findViewById(R.id.button);
        btnPause = findViewById(R.id.buttonPause);
        btnStop = findViewById(R.id.buttonStop);
        btnPlay = findViewById(R.id.buttonPlay);
        timerTextView = findViewById(R.id.timerTextView);

        timerHandler = new Handler();

        btnRecord.setOnClickListener(this::btnRecordPressed);
        btnPause.setOnClickListener(this::btnPausePressed);
        btnStop.setOnClickListener(this::btnStopPressed);
        btnPlay.setOnClickListener(this::btnPlayPressed);
    }

    private void btnRecordPressed(View v) {
        if (!isRecording) {
            startRecording();
            isRecording = true;
            btnRecord.setEnabled(false);
            btnPause.setEnabled(true);
            btnStop.setEnabled(true);
        }
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(getRecordingFilePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            startTime = System.currentTimeMillis();
            timerHandler.post(updateTimerRunnable);
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void btnPausePressed(View v) {
        if (isRecording) {
            if (!isPaused) {
                mediaRecorder.pause();
                pauseOffset += System.currentTimeMillis() - startTime;
                timerHandler.removeCallbacks(updateTimerRunnable);
                btnPause.setText("Resume");
                isPaused = true;
            } else {
                mediaRecorder.resume();
                startTime = System.currentTimeMillis();
                timerHandler.post(updateTimerRunnable);
                btnPause.setText("Pause");
                isPaused = false;
            }
        }
    }

    private void btnStopPressed(View v) {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            isPaused = false;
            pauseOffset = 0;
            btnRecord.setEnabled(true);
            btnPause.setEnabled(false);
            btnStop.setEnabled(false);
            btnPause.setText("Pause");
            timerHandler.removeCallbacks(updateTimerRunnable);
            timerTextView.setText("00:00");
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void btnPlayPressed(View v) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(getRecordingFilePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                Toast.makeText(this, "Playback started", Toast.LENGTH_SHORT).show();
                mediaPlayer.setOnCompletionListener(mp -> stopPlayback());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            stopPlayback();
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(this, "Playback stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private Runnable updateTimerRunnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            long elapsedMillis = currentTime - startTime + pauseOffset;
            int seconds = (int) (elapsedMillis / 1000) % 60;
            int minutes = (int) (elapsedMillis / 1000) / 60;
            timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    private String getRecordingFilePath() {
        File musicDirectory = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, "testRecording.mp3");
        return file.getPath();
    }
}


