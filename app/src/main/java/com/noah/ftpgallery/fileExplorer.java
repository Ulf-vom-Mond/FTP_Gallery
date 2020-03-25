package com.noah.ftpgallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;

import org.apache.commons.net.ftp.FTPFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class fileExplorer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);
        Log.i("yeet", "yeete die nachricht");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Intent intent = getIntent();
        String selectedConnectionName = intent.getStringExtra(EXTRA_MESSAGE);
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
        Connection selectedConnection = null;
        for (int i = 0; i < connectionSettings.size(); i++) {
            if(connectionSettings.get(i).getConnectionName().equals(selectedConnectionName)){
                selectedConnection = connectionSettings.get(i);
            }
        }
        selectedConnection.connect();
        selectedConnection.setListHiddenFiles(true);
        FTPFile[] directory = selectedConnection.listDirectory();
        for (int i = 0; i < directory.length; i++) {
            try {
                Log.i("yeet", directory[i].toString());
            }catch (Exception e) {
                Log.e("yeet", e.toString());
            }

        }

    }
}
