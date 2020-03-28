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
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class fileExplorer extends AppCompatActivity implements file_entry.EntryOnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        selectedConnection.connect();

        selectedConnection.setListHiddenFiles(true);
        FTPFile[] directory = selectedConnection.listDirectory();



        for (int i = 0; i < directory.length; i++) { //füllen von applets

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            file_entry fragment = new file_entry();
            Bundle testbundle = new Bundle();
            ArrayList<String> attributes = new ArrayList<String>();
            attributes.add(directory[i].getName());
            attributes.add(formatSize(directory[i].getSize()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm ");
            attributes.add(sdf.format(directory[i].getTimestamp().getTime()));
            attributes.add(directory[i].isDirectory() ? "directory" : "file");
            testbundle.putStringArrayList("file_attribute" , attributes);

            fragment.setArguments(testbundle);
            fragmentTransaction.add(R.id.file_entry_container, fragment);
            fragmentTransaction.commit();

        }


    }

    private static String formatSize (long size) {
        double sizeDouble = (double) size;
        String[] sizeAbbreviations = {" B", " kB", " MB", " GB", " TB", " EB"};
        for (int i = 0; i < (size + "").length(); i++) {
            if(sizeDouble >= 1000){
                sizeDouble = (int)(sizeDouble / 10);
            }else {
                if (i % 3 == 0) {

                    return (sizeDouble + "").substring(0, (sizeDouble + "").length() > 4 ? 5 : (sizeDouble + "").length()) + sizeAbbreviations[(i + 1) / 3];
                }else {
                    sizeDouble = (sizeDouble / 10.0);
                }
            }
        }
        return size + " B";
    }

    @Override
    public void entryOnClickListener(String fileName) {
        Log.i("yeet", fileName);
    }
}
