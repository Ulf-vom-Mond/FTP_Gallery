package com.noah.ftpgallery;

import android.app.AlertDialog;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
		Button saveSettings = (Button) view.findViewById(R.id.saveSettings);
		saveSettings.setOnClickListener(this);
		Button showLogs = (Button) view.findViewById(R.id.deleteConnection);
		showLogs.setOnClickListener(this);
        connectionName = (EditText) view.findViewById(R.id.edit_name);
        ipAddress = (EditText) view.findViewById(R.id.edit_ip);
        port = (EditText) view.findViewById(R.id.edit_port);
        username = (EditText) view.findViewById(R.id.edit_user);
        password = (EditText) view.findViewById(R.id.edit_password);
        standardDirectory = (EditText) view.findViewById(R.id.edit_directory);
		String selectedServer = mCallback.getSelectedServer();
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
		ArrayList<Connection> connectionSettings = readConnectionSettings();
    	breakpoint:
		switch(v.getId()){
			case R.id.saveSettings:
				String connectionName = this.connectionName.getText().toString();
				String ipAddress = this.ipAddress.getText().toString();
				String port = this.port.getText().toString();
				String username = this.username.getText().toString();
				String password = this.password.getText().toString();
				String standardDirectory = this.standardDirectory.getText().toString();

				String tag = "";
				if (connectionName == null || connectionName.length() == 0) {
					//tag = tag + "connectionName ";
					connectionName = "connection " + connectionSettings.size();
				}
				if (ipAddress == null || ipAddress.length() == 0) {
					tag = tag + "ipaddress";
				}
				if (port == null || port.length() == 0) {
					//tag = tag + "port ";
					port = "21";
				}
				if (username == null || username.length() == 0) {
					tag = tag + "username";
				}
				if (password == null || password.length() == 0) {
					//tag = tag + "password ";
					password = "";
				}
				if (standardDirectory == null || standardDirectory.length() == 0) {
					//tag = tag + "standardDirectory";
					standardDirectory = "/";
				}

				end:
				for (int i = 0; i < connectionSettings.size(); i++) {
					if (connectionSettings.get(i).getConnectionName().equals(connectionName)) {
						tag = tag + "samename";
					}
				}

				if (tag.length() != 0) {
					EmptyServerSettingDialog emptyServerSettingsDialog = new EmptyServerSettingDialog();
					emptyServerSettingsDialog.show(getFragmentManager(), tag);
					return;
				}

                if(mCallback.getSelectedServer().equals("Add server")){
                	connectionSettings.add(new Connection(connectionName, ipAddress, Integer.parseInt(port), username, password, standardDirectory));
					mCallback.getMainDrawerMenu().add(R.id.addServerGroup, connectionSettings.size(), 0, connectionSettings.get(connectionSettings.size() - 1).getConnectionName());
					//mCallback.getMainDrawerMenu().getItem(connectionSettings.size() - 1).setIcon(R.drawable.ic_single_server);
				}else {
                	int i;
                	exit:
                	for (i = 0; i < connectionSettings.size(); i++){
                		if (connectionSettings.get(i).getConnectionName().equals(mCallback.getSelectedServer())) {break exit;}
					}
                	connectionSettings.set(i, new Connection(connectionName, ipAddress, Integer.parseInt(port), username, password, standardDirectory));
                	mCallback.getMainDrawerMenu().getItem(i + 1).setTitle(connectionSettings.get(i).getConnectionName());
				}
				break;
			case R.id.deleteConnection:
				int i;
				exit:
				for (i = 0; i < connectionSettings.size(); i++){
					if (connectionSettings.get(i).getConnectionName().equals(mCallback.getSelectedServer())) {break exit;}
				}
				connectionSettings.remove(i);
				break;
		}
		writeConnectionSettings(connectionSettings);
		Intent intent = new Intent(getContext(), MainActivity.class);
		startActivity(intent);
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

	private void writeConnectionSettings(ArrayList<Connection> connectionSettings) {
		try {
			FileOutputStream fileOut = new FileOutputStream(getContext().getFilesDir() + "/connectionSettings.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(connectionSettings);
			out.close();
			fileOut.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
}
