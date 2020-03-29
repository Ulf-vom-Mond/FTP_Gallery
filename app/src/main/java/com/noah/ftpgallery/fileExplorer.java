package com.noah.ftpgallery;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;

import org.apache.commons.net.ftp.FTPFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class fileExplorer extends AppCompatActivity implements file_entry.EntryOnClickListener {

    ArrayList<Fragment> fragmentList = new ArrayList<>();
    FTPFile[] directory;
    Connection selectedConnection = null;
    String selectedConnectionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_explorer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        selectedConnectionName = intent.getStringExtra(EXTRA_MESSAGE);
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
        selectedConnection.connect();
        display();

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
        int iterator = 0;
        while (!directory[iterator].getName().equals(fileName)) {
            iterator++;
        }
        if (!directory[iterator].isFile()) {
            for (int i = 0; i < fragmentList.size(); i++) {
                FragmentManager fragmentManager = fragmentList.get(i).getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(fragmentList.get(i)).commit();
            }
            selectedConnection.setDirectory(selectedConnection.getDirectory() + "/" + fileName);
            display();
        }else {
            int length = directory[iterator].getName().split("[.]").length;
            if (length >= 1) {
                String fileType = directory[iterator].getName().split("[.]")[length - 1].toLowerCase();
                if (fileType.equals("png") || fileType.equals("jpg") || fileType.equals("jpeg") || fileType.equals("bmp")) {
                    Intent intent = new Intent(this, mediaViewer.class);
                    intent.putExtra("fileName", fileName);
                    intent.putExtra("selectedConnectionName", selectedConnectionName);
                    startActivity(intent);
                }else {

                }
            }else {

            }
        }

    }

    private void display() {

        fragmentList.clear();
        selectedConnection.setListHiddenFiles(true);
        directory = selectedConnection.listDirectory();

        FragmentManager fragmentManager = getSupportFragmentManager();

        for (int i = 0; i < directory.length; i++) { //füllen von fragment
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentList.add(new file_entry());
            Bundle testbundle = new Bundle();
            ArrayList<String> attributes = new ArrayList<String>();
            attributes.add(directory[i].getName());
            attributes.add(formatSize(directory[i].getSize()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm ");
            attributes.add(sdf.format(directory[i].getTimestamp().getTime()));
            attributes.add(directory[i].isDirectory() ? "directory" : "file");
            testbundle.putStringArrayList("file_attribute" , attributes);

            fragmentList.get(fragmentList.size() - 1).setArguments(testbundle);
            fragmentTransaction.add(R.id.file_entry_container, fragmentList.get(fragmentList.size() - 1));
            fragmentTransaction.commit();

            findViewById(R.id.file_entry_scroll).scrollTo(0, 0);
        }
    }
}
