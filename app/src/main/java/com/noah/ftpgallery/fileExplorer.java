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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
        Log.i ("yeet", "1");
        try {
            FileInputStream fileIn = new FileInputStream(getFilesDir() + "/connectionSettings.ser");
            Log.i ("yeet", "file");
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
        Connection selectedConnection = null;

        for (int i = 0; i < connectionSettings.size(); i++) {

            if(connectionSettings.get(i).getConnectionName().equals(selectedConnectionName)){
                selectedConnection = connectionSettings.get(i);
            }
        }

        selectedConnection.connect(); //klappt nicht oder villeicht doch?

        selectedConnection.setListHiddenFiles(true);
        FTPFile[] directory = selectedConnection.listDirectory();
        Log.i ("yeet", "ftp");

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =fragmentManager.beginTransaction();

        for (int i = 0; i < directory.length; i++) { //füllen von applets
            file_entry fragment = new file_entry();
            fragmentTransaction.add(R.id.file, fragment);
            fragmentTransaction.commit();
            TextView file_name = findViewById(R.id.file_name);
            TextView file_size = findViewById(R.id.file_size);
            TextView file_date = findViewById(R.id.file_date);
            file_name.setText(directory[i].getName());
            file_size.setText(directory[i].getSize() + "");
            file_date.setText(directory[i].getTimestamp() + "");

            try {
                Log.i("datei", directory[i].toString());
                //anzeigen von dateien
            }catch (Exception e) {
                Log.e("yeet", e.toString());
            }

        }
        Log.i("yeet", "nachftp");
    }
}
