package com.noah.ftpgallery;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
//import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.apache.commons.net.ftp.FTPFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import java.util.Collections;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class fileExplorer extends AppCompatActivity implements file_entry.EntryOnClickListener, View.OnClickListener {

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

        findViewById(R.id.backArrow).setOnClickListener(this);

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


        Spinner spinner = (Spinner) findViewById(R.id.sorting_way); //array mit werten auswählen
        Switch sorting_direction_switch = (Switch)findViewById(R.id.richtung);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.sorting_criteria, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

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

    private  FTPFile[] sorting (FTPFile[] directory){
        Spinner spinner = (Spinner) findViewById(R.id.sorting_way); //array mit werten auswählen
        Switch sorting_direction_switch = (Switch)findViewById(R.id.richtung);

        String sorting_way = spinner.getSelectedItem().toString();  //werte ablesen
        String sorting_direction;
        if (!sorting_direction_switch.isChecked()){
            sorting_direction = "asc";
        }else{
            sorting_direction = "desc";
        }       //sortieren

        for (int i = 0; i < directory.length-1; i++){
            for (int b = 0; b < (directory.length)-i-1; b++){
                if (true == vergleich(b, directory,sorting_way, sorting_direction)){
                    directory = change(b,directory);
                }
            }
        }

        return directory;
    }

    private static FTPFile[] change (int b, FTPFile[] directory){
        FTPFile[] help = new FTPFile[1];
        help[0] = directory[b];
        directory[b] = directory[b+1];
        directory[b+1] = help[0];
        return directory;
    }

    private static boolean vergleich (int b,FTPFile[] directory, String sorting_way, String sorting_direction){
       boolean test = false;
       String first = "",second  = ""; //true wenn second > first

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        //sorting_way = "name";
        switch (sorting_way){       //je nach art sortieren
            case "Size":
                if (directory[b+1].getSize() > directory[b].getSize()){
                test = true;
                    first = directory[b].getSize() + "";
                    second = directory[b+1].getSize() + "";
                }
                break;
            case "Date":
                first = sdf.format(directory[b].getTimestamp().getTime()) + "";
                second = sdf.format(directory[b+1].getTimestamp().getTime()) + "";
                if (first.compareToIgnoreCase(second) >= 0){  //wenn second größer ist, dann positive zahl
                    test = true;
                }
                break;
            default:
                first = directory[b].getName();
                second = directory[b+1].getName();
                if (first.compareToIgnoreCase(second) >= 0){  //wenn second größer ist, dann positive zahl
                    test = true;
                }
        }

                if (sorting_direction == "desc"){   //bei absteigender sortierung alles umkehren
                    return test;
                }else{
                    return !test;
                }
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

                    int fileCount = 0;
                    for (int i = 0; i < directory.length; i++) {
                        if (directory[i].isFile()) { fileCount++; }
                    }
                    String[] fileList = new String[fileCount];
                    int fileListIterator = 0;
                    for (int i = 0; i < directory.length; i++) {
                        if (directory[i].isFile()) {
                            fileList[fileListIterator] = directory[i].getName();
                            fileListIterator++;
                        }
                    }

                    intent.putExtra("fileList", fileList);
                    startActivity(intent);
                }else {

                }
            }else {

            }
        }

    }
    private static String[] file_names (FTPFile[] directory){
        int length;
        String fileType = "";

        String[] filenames = new String[]{"png","jpg"};
        List<String> list = Arrays.asList(filenames);
        ArrayList<Integer> places = new ArrayList<>();

        for (int i=0; i<directory.length; i++){ //zählen wie viele datein von einem typ da sind
            length = directory[i].getName().split("[.]").length;
            if (length >= 1) {
                 fileType = directory[i].getName().split("[.]")[length - 1].toLowerCase();
            }
            if (list.contains(fileType)){
                places.add(i);
            }
        }
        String[] file_names = new String[places.size()];

        for (int i=0; i<places.size(); i++) file_names[i] = directory[places.get(i)].getName();//dateinamen speichern

        return file_names;
    }

    private void display() {

        fragmentList.clear();
        selectedConnection.setListHiddenFiles(true);
        directory = sorting(selectedConnection.listDirectory());


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

    @Override
    public void onClick(View v) {
        String[] directories = selectedConnection.getDirectory().split("[/]");
        if (directories.length >= 2) {
            for (int i = 0; i < fragmentList.size(); i++) {
                FragmentManager fragmentManager = fragmentList.get(i).getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(fragmentList.get(i)).commit();
            }
            String newPath = "";
            for (int i = 0; i < (directories.length - 1); i++) {
                newPath = newPath + "/" + directories[i];
            }
            Log.i("yeet", newPath);
            selectedConnection.setDirectory(newPath);
            display();
        }
    }

    private String cleanUpPath (String path) {

        return path;
    }
}
