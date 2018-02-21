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
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class ClientThread implements Runnable {
	Thread runner;
	Socket soc;
	Chunk toSend;

	public ClientThread(String ip, int port, Chunk toSend) throws ConnectException {
		runner = new Thread(this);
		try {
			soc = new Socket(ip, port);
		} catch (ConnectException e) {
			System.out.println("Host at " + ip + "is down.");
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.toSend = toSend;
		System.out.println("Initializing ClientThread to " + ip + "...");
		runner.run();
	}

	@Override
	public void run() {
		try {
			OutputStream o = soc.getOutputStream();
			ObjectOutput s = new ObjectOutputStream(o);
			s.writeObject(toSend);
			System.out.println(
					"Chunk with id '" + toSend.getId() + "' of file '" + toSend.getName() + "' has been sent!");
			s.flush();
			s.close();
			System.out.println("Terminating ClientThread...");
			runner.join();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("Error during serialization");
			System.exit(1);
		}
	}

}