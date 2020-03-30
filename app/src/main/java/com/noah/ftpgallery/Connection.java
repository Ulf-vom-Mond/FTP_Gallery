package com.noah.ftpgallery;

import android.util.Log;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

public class Connection implements Serializable {
	private String connectionName;
	private String ipAddress;
	private int port;
	private String username;
	private String password;
	private String directory;
	private FTPClient ftp = null;

	public Connection(String connectionName, String ipAddress, int port, String username, String password, String directory){
		this.connectionName = connectionName;
		this.ipAddress = ipAddress;
		this.port = port;
		this.username = username;
		this.password = password;
		this.directory = directory;
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

	public void setDirectory(String directory) {
		this.directory = directory;
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

	public String getDirectory() {
		return directory;
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

                    try {
                        ftp.connect(ipAddress, port);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }



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
					directoryListing[0] = ftp.listFiles(directory);
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

	public void downloadFile(final String fileName, final String localFilePath) {
		final Thread mThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
					ftp.retrieveFile(directory + "/" + fileName, fos);
				} catch (IOException e) {
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
}
