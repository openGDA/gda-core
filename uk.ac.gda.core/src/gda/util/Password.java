/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class for the encryption/decyption of password string.
 */
public class Password {

	/**
	 * Encrypt a password string.
	 * 
	 * @param password
	 *            The ascii password string.
	 * @return The encrypted password string.
	 */
	public static String encrypt(String password) {
		byte[] ba = password.getBytes();

		for (int i = 0; i < ba.length; i++) {
			ba[i] += 5;
		}

		return Base64.encodeBytes(ba);
	}

	/**
	 * Decrypt a password string.
	 * 
	 * @param password
	 *            The encrypted password string
	 * @return The decrypted password string.
	 */
	public static String decrypt(String password) {
		byte[] ba = password.getBytes();

		ba = Base64.decode(ba, 0, ba.length);
		for (int i = 0; i < ba.length; i++) {
			ba[i] -= 5;
		}

		return new String(ba);
	}

	/**
	 * Decrypt a password string.
	 * 
	 * @param passwordFile
	 *            The encrypted password string
	 * @return The decrypted password string.
	 */
	public static String readFromFile(String passwordFile) {
		byte[] ba = null;
		try {
			FileInputStream fi = new FileInputStream(passwordFile);
			DataInputStream di = new DataInputStream(fi);

			ba = di.readUTF().getBytes();
			ba = Base64.decode(ba, 0, ba.length);
			for (int i = 0; i < ba.length; i++) {
				ba[i] -= 5;
			}

			di.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new String(ba);
	}

	/**
	 * @param password
	 * @param passwordFile
	 */
	public static void writeToFile(String password, String passwordFile) {
		byte[] ba = password.getBytes();

		for (int i = 0; i < ba.length; i++) {
			ba[i] += 5;
		}

		FileOutputStream fo;
		try {
			fo = new FileOutputStream(passwordFile);

			DataOutputStream dos = new DataOutputStream(fo);
			dos.writeUTF(Base64.encodeBytes(ba));
			dos.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String password = null;
		// char[] ca = null;
		String passwordFile = "";
		System.out.println("Writing password file " + passwordFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		try {

			System.out.print("Enter password file path: ");

			if (!(passwordFile = in.readLine()).equals(""))
				System.out.println("fileLocation " + passwordFile);
			else
				return;
			System.out.print("Enter password : ");

			if (!(password = in.readLine()).equals(""))
				System.out.println("Password is  " + password);
			else
				return;

			writeToFile(password, passwordFile);

			System.out.println("the password from file is  " + readFromFile(passwordFile));

		} catch (IOException ex) {
			System.out.println("Error writing " + passwordFile);
		}
	}
}
