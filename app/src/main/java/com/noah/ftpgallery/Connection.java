package com.noah.ftpgallery;

import android.util.Log;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

public class Connection implements Serializable {
	private String connectionName;
	private String ipAddress;
	private int port;
	private String username;
	private String password;
	private String standardDirectory;
	private FTPClient ftp = null;
	private String currentDirectory;

	public Connection(String connectionName, String ipAddress, int port, String username, String password, String standardDirectory){
		this.connectionName = connectionName;
		this.ipAddress = ipAddress;
		this.port = port;
		this.username = username;
		this.password = password;
		this.standardDirectory = standardDirectory;
		this.currentDirectory = standardDirectory;
	}

	public Connection(){

	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setStandardDirectory(String standardDirectory) {
		this.standardDirectory = standardDirectory;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getPort() {
		return port;
	}

	public String getConnectionName() {
		return connectionName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getPassword() {
		return password;
	}

	public String getStandardDirectory() {
		return standardDirectory;
	}

	public String getUsername() {
		return username;
	}

	public void connect() {
		final Thread mThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					ftp = new FTPClient();
					ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
					int reply;
					Log.i ("yeet", "reply");
					ftp.connect(ipAddress); //errrrororo //ToDO fixen
					Log.i ("yeet", "ftpconnect");
					reply = ftp.getReplyCode();

					if (!FTPReply.isPositiveCompletion(reply)) {
						ftp.disconnect();
						throw new Exception("Exception in connecting to FTP Server");
					}

					ftp.login(username, password);
					ftp.setFileType(FTP.BINARY_FILE_TYPE);
					ftp.enterLocalPassiveMode();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		mThread.start();
		try {
			mThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void disconnect () {
		final Thread mThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					ftp.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		mThread.start();
		try {
			mThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public FTPFile[] listDirectory() {
		final FTPFile[][] directoryListing = new FTPFile[1][];
		final Thread mThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					directoryListing[0] = ftp.listFiles(currentDirectory);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		mThread.start();
		try {
			mThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return directoryListing[0];
	}

	public void setListHiddenFiles(Boolean setting) {
		ftp.setListHiddenFiles(setting);
	}
}
