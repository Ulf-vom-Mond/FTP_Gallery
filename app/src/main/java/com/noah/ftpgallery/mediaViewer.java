package com.noah.ftpgallery;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class mediaViewer extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {

    private Connection selectedConnection = null;
    private String[] fileList;
    private int currentFile;
    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Boolean video = false;
    private ImageView imageView;
    private int autoDownload = 6;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
          /*  mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);*/
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_media_viewer);

        mVisible = true;

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        Button left = findViewById(R.id.left);
        Button right = findViewById(R.id.right);
        imageView = findViewById(R.id.imageView);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        findViewById(R.id.media_viewer).setOnLongClickListener(this);
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        //surfaceHolder.setFixedSize(500, 500);

        // Set up the user interaction to manually show or hide the system UI.

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");
        String selectedConnectionName = intent.getStringExtra("selectedConnectionName");
        fileList = intent.getStringArrayExtra("fileList");

        ArrayList<String> settings = new ArrayList<>();
        try {
            FileInputStream fileIn = new FileInputStream(getFilesDir() + "/settings.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            settings = (ArrayList<String>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
        autoDownload = Integer.parseInt(settings.get(0));

        ArrayList<Connection> connectionSettings = new ArrayList<Connection>();
        try {
            FileInputStream fileIn = new FileInputStream(getFilesDir() + "/connectionSettings.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            connectionSettings = (ArrayList<Connection>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
            return;
        }

        selectedConnection = null;

        for (int i = 0; i < connectionSettings.size(); i++) {

            if(connectionSettings.get(i).getConnectionName().equals(selectedConnectionName)){
                selectedConnection = connectionSettings.get(i);
            }
        }

        selectedConnection.setDirectory(intent.getStringExtra("path"));

        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].equals(fileName)){
                currentFile = i;
            }
        }

        selectedConnection.connect();
        showMedia();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(0);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onClick(View v) {
        imageView.setImageResource(android.R.color.transparent);
        //surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        //surfaceHolder.setFormat(PixelFormat.OPAQUE);
        try {
            mediaPlayer.stop();
            mediaPlayer.release();
        }catch (Exception e){}
        switch (v.getId()) {
            case R.id.left:
                if (currentFile > 0) {
                    currentFile--;
                }
                break;
            case R.id.right:
                if (currentFile < (fileList.length - 1)) {
                    currentFile++;
                }
                break;
        }
        showMedia();
    }

    private void showMedia () {
        if (!new File(getCacheDir() + "/" + fileList[currentFile]).exists()) {
            selectedConnection.downloadFile(fileList[currentFile], getCacheDir() + "/" + fileList[currentFile]);
        }else {
            if (selectedConnection.getFileSize(fileList[currentFile]) != new File(getCacheDir() + "/" + fileList[currentFile]).length()) {
                selectedConnection.downloadFile(fileList[currentFile], getCacheDir() + "/" + fileList[currentFile]);
            }
        }
        /*long localSize = 0;
        long serverSize = selectedConnection.getFileSize(fileList[currentFile]);
        while (localSize < serverSize) {
            localSize = new File(getCacheDir() + "/" + fileList[currentFile]).length();
        }*/
        int length = fileList[currentFile].split("[.]").length;
        if (length >= 1) {
            String fileType = fileList[currentFile].split("[.]")[length - 1].toLowerCase();
            switch (fileType) {
                case "png":
                case "jpg":
                case "jpeg":
                case "bmp":
                    video = false;
                    imageView.setImageBitmap(BitmapFactory.decodeFile(getCacheDir() + "/" + fileList[currentFile]));
                    imageView.setBackgroundColor(0xff000000);
                    break;
                case "gif":
                case "webm":
                case "mp4":
                case "avi":
                case "mkv":
                    video = true;
                    imageView.setBackgroundColor(0x00000000);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            playVideo();
                        }
                    });
                    break;
            }
        }
        final Thread mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= autoDownload; i++) {
                    String newFile;
                    int index = (int) Math.ceil(i / 2);
                    if ((currentFile - index) >= 0 && (currentFile + index) < (fileList.length)) {
                        if (i % 2 == 0) {
                            newFile = fileList[currentFile + index];
                        }else {
                            newFile = fileList[currentFile - index];
                        }
                        if (!new File(getCacheDir() + "/" + newFile).exists()) {
                            selectedConnection.downloadFile(newFile, getCacheDir() + "/" + newFile);
                        }else {
                            if (selectedConnection.getFileSize(newFile) != new File(getCacheDir() + "/" + newFile).length()) {
                                selectedConnection.downloadFile(newFile, getCacheDir() + "/" + newFile);
                            }
                        }
                    }
                }
            }
        });
        mThread.start();
    }

    private void playVideo () {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setDisplay(surfaceView.getHolder());
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(getCacheDir() + "/" + fileList[currentFile]));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        File picturesDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/FTP gallery/");
        if (!picturesDir.exists()) {
            picturesDir.mkdirs();
        }
        selectedConnection.downloadFile(fileList[currentFile], Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/FTP gallery/" + fileList[currentFile]);
        Snackbar.make(findViewById(R.id.media_viewer), "Downloaded to " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/FTP gallery/" + fileList[currentFile], Snackbar.LENGTH_LONG).show();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        selectedConnection.disconnect();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (video) {
            runOnUiThread(new Runnable() {
                public void run() {
                    playVideo();
                }
            });
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
