/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.epn;

/**
 *
 * @author CARLOS OSORIO
 */
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class p2p {

	private static int port = 60020;
	private static String sharedfolder = "./SharedFolder";
	private static String[] actualFileList;
	private static String[] newFileList;
	private static boolean listRequiresUpdate;
	private static File sharedFolder;

	public static String getSharedfolder() {
		return sharedfolder;
	}

	public static String[] getActualFileList() {
		return actualFileList;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		p2p.port = port;
	}

	public static void setListRequiresUpdate(boolean flag) {
		p2p.listRequiresUpdate = flag;
	}

	public static void main(String args[]) throws IOException {
		sharedFolder = new File(sharedfolder);
		if (!sharedFolder.exists()) {
			sharedFolder.mkdirs();
			System.out.println("No shared folder detected, creating.....Done!");

		} else {
			System.out.println("Shared folder detected.");
		}
		startServer();
		startClient();
	}

	private static ArrayList<String> difference(String[] before, String[] after) {
		ArrayList<String> b = new ArrayList<String>();
		ArrayList<String> a = new ArrayList<String>();
		Collections.addAll(b, before);
		Collections.addAll(a, after);
		a.removeAll(b);
		return a;
	}

	private static void startClient() {
		(new Thread() {

			@Override
			public void run() {
				ListaIPs iplist = new ListaIPs();
				boolean complete = true;
				Chunk toSend = null;

				actualFileList = sharedFolder.list();
				System.out.println("Files in the shared folder:");
				for (String file : actualFileList) {
					System.out.println("> " + file);
				}

				System.out.println("Syncing...");
				toSend = new Chunk();
				toSend.setId(-2); // startup sync
				toSend.setName("");
				toSend.setToSyncList(actualFileList);
				for (String ip : iplist.getIplist()) {
					try {
						new ClientThread(ip, port, toSend);
						break;
					} catch (ConnectException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

				while (true) {
					if (listRequiresUpdate) {
						actualFileList = sharedFolder.list();
						listRequiresUpdate = false;
					}
					newFileList = sharedFolder.list();
					if (actualFileList.length < newFileList.length) { // new
																		// file
						System.out.println("Change detected in the folder, syncing...");
						ArrayList<String> unsyncedFiles = difference(actualFileList, newFileList);
						System.out.println(unsyncedFiles.get(0));
						Path path = Paths.get(sharedfolder + "/" + unsyncedFiles.get(0));
						byte[] data;
						File archivo = new File(String.valueOf(path));
						do {
							System.out.println("File is being copied, waiting...");
							try {
								complete = isCompletelyWritten(archivo);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} while (!complete);
						System.out.println("File ready for transfer, syncing...");
						try {
							data = Files.readAllBytes(path);
							toSend = new Chunk();
							toSend.setInfo(data);
							toSend.setName(String.valueOf(unsyncedFiles.get(0)));
							toSend.setId(0);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						for (String ip : iplist.getIplist()) {
							try {
								new ClientThread(ip, port, toSend);
							} catch (ConnectException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else if (actualFileList.length > newFileList.length) { // delete
						// file
						System.out.println("Change detected in the folder......syncing");
						ArrayList<String> unsyncedFiles = difference(newFileList, actualFileList);
						System.out.println(unsyncedFiles.get(0));
						toSend = new Chunk();
						toSend.setName(String.valueOf(unsyncedFiles.get(0)));
						toSend.setId(-1);
						for (String ip : iplist.getIplist()) {
							try {
								new ClientThread(ip, port, toSend);
							} catch (ConnectException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					actualFileList = newFileList;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private static void startServer() throws IOException {
		(new Thread() {

			@Override
			public void run() {
				ServerSocket ser = null;
				try {
					ser = new ServerSocket(port);
				} catch (IOException e) {
					System.err.println("Could not listen on port: " + port + ".");
					System.exit(1);
				}

				Socket clientSocket = null;
				try {
					while (true) {
						clientSocket = ser.accept();
						new ServerThread(clientSocket);
					}
				} catch (IOException e) {
					System.err.println("Accept failed.");
					System.exit(1);
				}

				try {
					ser.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static boolean isCompletelyWritten(File file) throws InterruptedException {
		RandomAccessFile stream = null;
		try {
			stream = new RandomAccessFile(file, "rw");
			return true;
		} catch (Exception e) {
			Thread.sleep(1000);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					System.out.println("Exception closing file " + file.getName());
				}
			}
		}
		return false;
	}
}