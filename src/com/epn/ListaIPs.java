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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.swing.JOptionPane;

public class ListaIPs implements Serializable {

	private ArrayList<String> iplist;

	public ListaIPs() {
		super();
		iplist = new ArrayList<String>();
		BufferedReader input = null;
		String ip;
		try {
			File inputFile = new File("iplist.txt");
			input = new BufferedReader(new FileReader(inputFile));
			while (input.ready()) {
				ip = input.readLine();
				if (ip.compareTo("127.0.0.1") == 0)
					continue;
				if (ip.compareTo(InetAddress.getLocalHost().getHostAddress()) == 0)
					continue;
				iplist.add(ip);
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "ERROR: Could not find 'iplist.txt'.", "ERROR",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public ArrayList<String> getIplist() {
		return iplist;
	}

}
