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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ServerThread implements Runnable {
	private Thread runner;
	private Socket soc;
	private String clientIP;

	public ServerThread(Socket ss) {
		runner = new Thread(this);
		soc = ss;
		clientIP = soc.getInetAddress().getHostAddress();
		System.out.println("Initializing ServerThread...");
		runner.run();
	}

	@Override
	public void run() {
		try {
			Chunk d = null;
			InputStream o = null;
			ObjectInput s = null;
			FileOutputStream fos = null;
			o = soc.getInputStream();
			s = new ObjectInputStream(o);
			try {
				d = (Chunk) s.readObject();

				if (d.getId() == -2) { // initial sync for a client
					ArrayList<String> clientFileList = new ArrayList<String>();
					ArrayList<String> serverFileList = new ArrayList<String>();
					for (String file : d.getToSyncList()) {
						clientFileList.add(file);
					}
					for (String file : p2p.getActualFileList()) {
						serverFileList.add(file);
					}
					ArrayList<String> unsyncedFileList = (ArrayList<String>) serverFileList.clone();
					unsyncedFileList.removeAll(clientFileList);
					Chunk toSend = null;
					if (unsyncedFileList.size() > 0) {
						boolean complete = false;
						for (String file : unsyncedFileList) {
							Path path = Paths.get(p2p.getSharedfolder() + "/" + file);
							byte[] data;
							File archivo = new File(String.valueOf(path));
							do {
								System.out.println("File is being copied, waiting...");
								try {
									complete = p2p.isCompletelyWritten(archivo);
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
								toSend.setName(String.valueOf(file));
								toSend.setId(0);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							new ClientThread(clientIP, p2p.getPort(), toSend);
						}
					}
					clientFileList.removeAll(serverFileList);
					if (clientFileList.size() > 0) {
						for (String file : clientFileList) {
							toSend = new Chunk();
							toSend.setName(String.valueOf(file));
							toSend.setId(-1);
							new ClientThread(clientIP, p2p.getPort(), toSend);
						}
					}

				} else if (d.getId() == -1) { // delete file
					File toDelete = new File(p2p.getSharedfolder() + "/" + d.getName());

					if (toDelete.delete()) {
						p2p.setListRequiresUpdate(true);
						System.out.println(d.getName() + " has been deleted!");
					} else {
						System.out.println("Delete operation has failed.");
					}
				} else { // new file
					System.out.println("The file '" + d.getName() + "' has been Received ");

					fos = new FileOutputStream(p2p.getSharedfolder() + "/" + d.getName());
					p2p.setListRequiresUpdate(true);
					fos.write(d.getInfo());
					fos.close();
				}
				System.out.println("Terminating ServerThread...");
				soc.close();
				runner.join();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// s.close();
	}
}
