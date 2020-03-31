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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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
public class mediaViewer extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    Connection selectedConnection = null;
    String[] fileList;
    int currentFile;

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
        left.setOnClickListener(this);
        right.setOnClickListener(this);
        findViewById(R.id.media_viewer).setOnLongClickListener(this);

        // Set up the user interaction to manually show or hide the system UI.

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");
        String selectedConnectionName = intent.getStringExtra("selectedConnectionName");
        fileList = intent.getStringArrayExtra("fileList");

        ArrayList<Connection> connectionSettings = new ArrayList<Connection>();
        try {
            FileInputStream fileIn = new FileInputStream(getFilesDir() + "/connectionSettings.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            connectionSettings = (ArrayList<Connection>) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            Log.i ("yeet", "catch");
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
        delayedHide(100);
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

        ImageView imageView = findViewById(R.id.imageView);
        if (!new File(getCacheDir() + "/" + fileList[currentFile]).exists()) {
            selectedConnection.downloadFile(fileList[currentFile], getCacheDir() + "/" + fileList[currentFile]);
        }
        imageView.setImageBitmap(BitmapFactory.decodeFile(getCacheDir() + "/" + fileList[currentFile]));
        final Thread mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int autoDownload = 3;
                for (int i = 0; i <= autoDownload * 2; i++) {
                    String newFile;
                    int index = (int) Math.ceil(i / 2);
                    if ((currentFile - index) >= 0 && (currentFile + index) < (fileList.length)) {
                        if (i % 2 == 0) {
                            newFile = fileList[currentFile + index];
                        }else {
                            newFile = fileList[currentFile - index];
                        }
                        if (!new File(getCacheDir() + "/" + newFile).exists()) {
                            Log.i("yeet", "downloading " + newFile);
                            selectedConnection.downloadFile(newFile, getCacheDir() + "/" + newFile);
                            Log.i("yeet", "downloaded " + newFile);
                        }
                    }
                }
            }
        });
        mThread.start();

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
}
