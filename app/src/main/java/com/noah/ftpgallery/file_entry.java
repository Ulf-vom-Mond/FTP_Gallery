package com.noah.ftpgallery;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link file_entry#newInstance} factory method to
 * create an instance of this fragment.
 */
public class file_entry extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private View view;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public file_entry() {
        // Required empty public constructor
        Log.i("yeet", "5");
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment file_entry.
     */
    // TODO: Rename and change types and number of parameters
    public static file_entry newInstance(String param1, String param2) {
        Log.i("yeet", "6");
        file_entry fragment = new file_entry();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.i("yeet", "9"   );
        return inflater.inflate(R.layout.fragment_file_entry, container, false);
    }


    @Override
    public void onViewCreated (View view, @Nullable Bundle savedInstanceState) {
       // view = inflater.inflate(R.layout.fragment_file_entry, container,false);

        Log.i("yeet", "8");
        Bundle hello = getArguments();
        ArrayList<String> atribute = hello.getStringArrayList("file_attribute");
        Log.i("yeet", atribute.get(1));

        TextView file_name = getView().findViewById(R.id.file_name);
        TextView file_size = getView().findViewById(R.id.file_size);
        TextView file_time = getView().findViewById(R.id.file_date);

        file_name.setText(atribute.get(0));
        file_size.setText(atribute.get(1));
        file_time.setText(atribute.get(2));
    }
}
