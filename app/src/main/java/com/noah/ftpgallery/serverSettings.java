package com.noah.ftpgallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class serverSettings extends Fragment implements View.OnClickListener {

	private ServerSettingsViewModel mViewModel;
	private View view;
	private EditText connectionName;
	private EditText ipAddress;
    private EditText port;
    private EditText username;
    private EditText password;
    private EditText standardDirectory;
    private serverSettings.Communication mCallback;

    public static serverSettings newInstance() {
		return new serverSettings();
	}

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.server_settings_fragment, container, false);
		Log.i("yeet", "bist du jetzt wirklich dran?");
		Button saveSettings = (Button) view.findViewById(R.id.saveSettings);
		saveSettings.setOnClickListener(this);
		Button showLogs = (Button) view.findViewById(R.id.showLogs);
		showLogs.setOnClickListener(this);
        connectionName = (EditText) view.findViewById(R.id.edit_name);
        ipAddress = (EditText) view.findViewById(R.id.edit_ip);
        port = (EditText) view.findViewById(R.id.edit_port);
        username = (EditText) view.findViewById(R.id.edit_user);
        password = (EditText) view.findViewById(R.id.edit_password);
        standardDirectory = (EditText) view.findViewById(R.id.edit_directory);
		String selectedServer = mCallback.getSelectedServer();
		Log.i("yeet", "youuu sülüctäd le serve du " + selectedServer);
		ArrayList<Connection> connectionSettings = readConnectionSettings();
		if(!mCallback.getSelectedServer().equals("Add server")){  //
			int i;
			exit:
			for (i = 0; i < connectionSettings.size(); i++){
				if (connectionSettings.get(i).getConnectionName().equals(mCallback.getSelectedServer())) {break exit;}
			}
			connectionName.setText(connectionSettings.get(i).getConnectionName());
			ipAddress.setText(connectionSettings.get(i).getIpAddress());
			port.setText("" + connectionSettings.get(i).getPort());
			username.setText(connectionSettings.get(i).getUsername());
			password.setText(connectionSettings.get(i).getPassword());
			standardDirectory.setText(connectionSettings.get(i).getDirectory());
		}
		return view;

	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mViewModel = ViewModelProviders.of(this).get(ServerSettingsViewModel.class);
		// TODO: Use the ViewModel
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.saveSettings:
                Log.i("yeet", "" + username.getText());
				ArrayList<Connection> connectionSettings = readConnectionSettings();
                if(mCallback.getSelectedServer().equals("Add server")){
                	connectionSettings.add(new Connection(connectionName.getText().toString(), ipAddress.getText().toString(), Integer.parseInt(port.getText().toString()), username.getText().toString(), password.getText().toString(), standardDirectory.getText().toString()));
					mCallback.getMainDrawerMenu().add(R.id.addServerGroup, connectionSettings.size(), 0, connectionSettings.get(connectionSettings.size() - 1).getConnectionName());
					//mCallback.getMainDrawerMenu().getItem(connectionSettings.size() - 1).setIcon(R.drawable.ic_single_server);
				}else {
                	int i;
                	exit:
                	for (i = 0; i < connectionSettings.size(); i++){
                		if (connectionSettings.get(i).getConnectionName().equals(mCallback.getSelectedServer())) {break exit;}
					}
                	connectionSettings.set(i, new Connection(connectionName.getText().toString(), ipAddress.getText().toString(), Integer.parseInt(port.getText().toString()), username.getText().toString(), password.getText().toString(), standardDirectory.getText().toString()));
                	mCallback.getMainDrawerMenu().getItem(i + 1).setTitle(connectionSettings.get(i).getConnectionName());
				}
				try {
					FileOutputStream fileOut = new FileOutputStream(getContext().getFilesDir() + "/connectionSettings.ser");
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(connectionSettings);
					out.close();
					fileOut.close();
					Log.i("yeet", "created and serialized empty arrayList");
				} catch (IOException i) {
					i.printStackTrace();
				}
				Intent intent = new Intent(getContext(), MainActivity.class);
				startActivity(intent);
				break;
			case R.id.showLogs:
				Log.i("yeet", "show logs");
				break;
		}
	}

	public interface Communication {
    	public String getSelectedServer();
    	public Menu getMainDrawerMenu();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			mCallback = (Communication) context;
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

	private ArrayList<Connection> readConnectionSettings(){
		ArrayList<Connection> connectionSettings = new ArrayList<Connection>();
		try {
			FileInputStream fileIn = new FileInputStream(getContext().getFilesDir() + "/connectionSettings.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			connectionSettings = (ArrayList<Connection>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			return null;
		}
		return connectionSettings;
	}
}
