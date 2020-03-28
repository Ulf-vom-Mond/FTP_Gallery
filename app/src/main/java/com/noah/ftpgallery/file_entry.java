package com.noah.ftpgallery;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link file_entry#newInstance} factory method to
 * create an instance of this fragment.
 */
public class file_entry extends Fragment implements View.OnClickListener {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private View view;

	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

    private file_entry.EntryOnClickListener mCallback;

	public file_entry() {
		// Required empty public constructor

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
		return inflater.inflate(R.layout.fragment_file_entry, container, false);
	}


	@Override
	public void onViewCreated (View view, @Nullable Bundle savedInstanceState) {
	    Bundle hello = getArguments();
		ArrayList<String> attributes = hello.getStringArrayList("file_attribute");

		TextView file_name = getView().findViewById(R.id.file_name);
		TextView file_size = getView().findViewById(R.id.file_size);
		TextView file_time = getView().findViewById(R.id.file_date);
		LinearLayout entry = getView().findViewById(R.id.entry);

		entry.setOnClickListener(this);
		ImageView icon = getView().findViewById(R.id.icon);
		file_name.setText(attributes.get(0));
		file_size.setText(attributes.get(1));
		file_time.setText(attributes.get(2));

		icon.setImageResource(R.drawable.unknown);
		if (attributes.get(3).equals("directory")) {
			icon.setImageResource(R.drawable.folder);
		}
		int length = attributes.get(0).split("[.]").length;
		if (length >= 1) {
			switch (attributes.get(0).split("[.]")[length - 1].toLowerCase()) {
				case "png":
					icon.setImageResource(R.drawable.png);
					break;
				case "jpg":
					icon.setImageResource(R.drawable.jpeg);
					break;
				case "jpeg":
					icon.setImageResource(R.drawable.jpeg);
					break;
				case "svg":
					icon.setImageResource(R.drawable.svg);
					break;
				case "pdf":
					icon.setImageResource(R.drawable.pdf);
					break;
				case "txt":
					icon.setImageResource(R.drawable.text);
					break;
				case "mp4":
					icon.setImageResource(R.drawable.webm);
					break;
				case "wmv":
					icon.setImageResource(R.drawable.wmv);
					break;
				case "avi":
					icon.setImageResource(R.drawable.webm);
					break;
				case "xcf":
					icon.setImageResource(R.drawable.xcf);
					break;
			}
		}
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (EntryOnClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IFragmentToActivity");
        }
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        mCallback.entryOnClickListener(getArguments().getStringArrayList("file_attribute").get(0));
    }

    public interface EntryOnClickListener {
        public void entryOnClickListener (String fileName);
    }
}
