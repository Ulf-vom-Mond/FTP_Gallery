package com.noah.ftpgallery.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.noah.ftpgallery.Connection;
import com.noah.ftpgallery.R;
import com.noah.ftpgallery.fileExplorer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class HomeFragment extends Fragment implements View.OnClickListener {

	private HomeViewModel homeViewModel;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		homeViewModel =
				ViewModelProviders.of(this).get(HomeViewModel.class);
		View root = inflater.inflate(R.layout.fragment_home, container, false);
		//final TextView textView = root.findViewById(R.id.text_home);
		homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
			@Override
			public void onChanged(@Nullable String s) {
				//textView.setText(s);
			}
		});
		ArrayList<Connection> connectionSettings = new ArrayList<Connection>();
		try {
			FileInputStream fileIn = new FileInputStream(getContext().getFilesDir() + "/connectionSettings.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			connectionSettings = (ArrayList<Connection>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
		}
		LinearLayout connectionList = root.findViewById(R.id.connectionList);
		for (int i = 0; i < connectionSettings.size(); i++) {
			Button newButton = new Button(getContext());
			newButton.setId(i + 1);
			newButton.setText(connectionSettings.get(i).getConnectionName());
			newButton.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
			newButton.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
			newButton.setOnClickListener(this);
			connectionList.addView(newButton);
		}
		if(connectionSettings.size() != 0) {
			((TextView) root.findViewById(R.id.noConnectionWarning)).setText("");
		}
		return root;
	}

    @Override
    public void onClick(View v) {
        Log.i("yeet", ((Button) v).getText().toString());
        Intent intent = new Intent(getContext(), fileExplorer.class);
        intent.putExtra(EXTRA_MESSAGE, ((Button) v).getText().toString());
        startActivity(intent);
    }
}
