package com.noah.ftpgallery.ui.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.noah.ftpgallery.Connection;
import com.noah.ftpgallery.MainActivity;
import com.noah.ftpgallery.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        final Spinner spinner = (Spinner) root.findViewById(R.id.criteria); //array mit werten auswählen
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.sorting_criteria, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final EditText autoDownload = root.findViewById(R.id.autoDownload);
        final Switch order = root.findViewById(R.id.order);
        final Switch displayHidden = root.findViewById(R.id.displayHidden);
        final Switch dirsBeforeFiles = root.findViewById(R.id.dirsBeforeFiles);


        // 0: (int) auto download
        // 1: (int) criteria [0, 1, 2]
        // 2: (String) order [ascending, descending]
        // 3: (Boolean) display hidden [yes, no]
        // 4: (Boolean) dirs before files [yes, no]

        File configFile = new File(getContext().getFilesDir() + "/settings.ser");
        if(configFile.exists()){
            ArrayList<String> settings = new ArrayList<>();
            try {
                FileInputStream fileIn = new FileInputStream(getContext().getFilesDir() + "/settings.ser");
                ObjectInputStream in = new ObjectInputStream(fileIn);
                settings = (ArrayList<String>) in.readObject();
                in.close();
                fileIn.close();
            } catch (IOException i) {
                i.printStackTrace();
                return null;
            } catch (ClassNotFoundException c) {
                c.printStackTrace();
                return null;
            }
            autoDownload.setText(settings.get(0));
            spinner.setSelection(Integer.parseInt(settings.get(1)));
            order.setText(settings.get(2));
            order.setChecked(settings.get(2).equals("descending "));
            displayHidden.setText(settings.get(3));
            displayHidden.setChecked(settings.get(3).equals("yes "));
            dirsBeforeFiles.setText(settings.get(4));
            dirsBeforeFiles.setChecked(settings.get(4).equals("yes "));
        }else {
            ArrayList<String> settings = new ArrayList<>();
            settings.add("6");
            settings.add("name");
            settings.add("ascending ");
            settings.add("no ");
            settings.add("no ");
            try {
                FileOutputStream fileOut = new FileOutputStream(getContext().getFilesDir() + "/settings.ser");
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(settings);
                out.close();
                fileOut.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }

        root.findViewById(R.id.autoDownloadInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new settingsToolTips()).show(getFragmentManager(), "When you look at a file like a picture or a video, the application can simultaneously download the media files next to it. That way, they will load faster if you navigate left and right. Here, you can set the total amount of files to download during looking at one.");
            }
        });
        root.findViewById(R.id.defaultSortingInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new settingsToolTips()).show(getFragmentManager(), "Here you can set how to sort files if you connect to a server.");
            }
        });
        root.findViewById(R.id.displayHiddenInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new settingsToolTips()).show(getFragmentManager(), "Here you can set if the file explorer should list hidden files and directories.");
            }
        });
        root.findViewById(R.id.dirsBeforeFilesInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new settingsToolTips()).show(getFragmentManager(), "Here you can set if the directories should be listed in front of the files in the file explorer.");
            }
        });
        root.findViewById(R.id.introduction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new Introduction()).show(getFragmentManager(), "");
            }
        });
        root.findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new About()).show(getFragmentManager(), "");
            }
        });
        root.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> settings = new ArrayList<>();
                settings.add(autoDownload.getText().toString());
                settings.add(spinner.getSelectedItemPosition() + "");
                settings.add(order.getText().toString());
                settings.add(displayHidden.getText().toString());
                settings.add(dirsBeforeFiles.getText().toString());
                try {
                    FileOutputStream fileOut = new FileOutputStream(getContext().getFilesDir() + "/settings.ser");
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(settings);
                    out.close();
                    fileOut.close();
                } catch (IOException i) {
                    i.printStackTrace();
                }
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch order = (Switch) v;
                order.setText(order.getText().equals("ascending ") ? "descending " : "ascending ");
            }
        });
        displayHidden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch displayHidden = (Switch) v;
                displayHidden.setText(displayHidden.getText().equals("yes ") ? "no " : "yes ");
            }
        });
        dirsBeforeFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch dirsBeforeFiles = (Switch) v;
                dirsBeforeFiles.setText(dirsBeforeFiles.getText().equals("yes ") ? "no " : "yes ");
            }
        });

        return root;
    }
}
