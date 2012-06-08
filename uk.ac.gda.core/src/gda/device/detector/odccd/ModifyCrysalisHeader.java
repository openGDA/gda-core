/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.odccd;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ModifyCrysalisHeader implements Closeable {

	int ASCII_HEADER_SIZE = 256;
	int BIN_HEADER_SIZE = 4864;
	int COMBINED_HEADER_SIZE = ASCII_HEADER_SIZE + BIN_HEADER_SIZE;
	RandomAccessFile raf;
	String inputFileName;

	public ModifyCrysalisHeader(String newInputFileName) {

		inputFileName = newInputFileName;

		File inputFile = new File(inputFileName);
		try {
			raf = new RandomAccessFile(inputFile, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	

	public void editIntHeader(String field, int newVal) throws IOException {
		int generalOffset = 256;

		if (field.equals("bx")) {
			raf.seek(generalOffset);
			raf.write((byte) newVal);
		}

		if (field.equals("by")) {
			raf.seek(generalOffset + 2);
			raf.write((byte) newVal);
		}

		if (field.equals("x1")) {
			raf.seek(generalOffset + 4);
			raf.write((byte) newVal);
		}

		if (field.equals("y1")) {
			raf.seek(generalOffset + 6);
			raf.write((byte) newVal);
		}

		if (field.equals("x2")) {
			byte[] b = intToByteArray(newVal, 2);
			raf.seek(generalOffset + 22);
			raf.write(b[1]);
			raf.seek(generalOffset + 23);
			raf.write(b[0]);
		}

		if (field.equals("y2")) {
			byte[] b = intToByteArray(newVal, 2);
			raf.seek(generalOffset + 24);
			raf.write(b[1]);
			raf.seek(generalOffset + 25);
			raf.write(b[0]);
		}
	}

	public void editStringHeader(String field, String newVal) throws IOException {

		if (field.equals("NX")) {
			for (int i = 0; i < 256; i+=2) {
				raf.seek(i);
				char c1 = (char) raf.read();
				raf.seek(i+1);
				char c2 = (char) raf.read();
				if (c1 == 'N' && c2 == 'X') {
					String rubyWidth = newVal;
					raf.seek(i + 3);
					raf.write((byte) rubyWidth.charAt(0));
					raf.seek(i + 4);
					raf.write((byte) rubyWidth.charAt(1));
					raf.seek(i + 5);
					raf.write((byte) rubyWidth.charAt(2));

					if (Integer.parseInt(newVal) >= 1024) {
						raf.seek(i + 6);
						raf.write((byte) rubyWidth.charAt(3));
					} else {
						raf.seek(i + 6);
						raf.write(' ');
					}
				}
			}
		}

		if (field.equals("NY")) {
			for (int i = 0; i < 256; i++) {
				raf.seek(i);
				char c1 = (char) raf.read();
				raf.seek(i+1);
				char c2 = (char) raf.read();
				if (c1 == 'N' && c2 == 'Y') {
					String rubyHeight = newVal;

					raf.seek(i + 3);
					raf.write((byte) rubyHeight.charAt(0));
					raf.seek(i + 4);
					raf.write((byte) rubyHeight.charAt(1));
					raf.seek(i + 5);
					raf.write((byte) rubyHeight.charAt(2));

					if (Integer.parseInt(newVal) >= 1024) {
						raf.seek(i + 6);
						raf.write((byte) rubyHeight.charAt(3));
					} else {
						raf.seek(i + 6);
						raf.write(' ');
					}
				}
			}
		}

		if (field.equals("NSUPPLEMENT")) {
			for (int i = 0; i < 256; i++) {
				raf.seek(i);
				char c1 = (char) raf.read();
				raf.seek(i+1);
				char c2 = (char) raf.read();
				raf.seek(i+2);
				char c3 = (char) raf.read();
				if (c1 == 'N' && c2 == 'S' && c3 == 'U') {
					raf.seek(i + 18);
					raf.write(newVal.getBytes()[0]);
				}
			}
		}

		if (field.equals("NHEADER")) {
			for (int i = 0; i < 256; i++) {
				raf.seek(i);
				raf.seek(i);
				char c1 = (char) raf.read();
				raf.seek(i+1);
				char c2 = (char) raf.read();
				raf.seek(i+2);
				char c3 = (char) raf.read();
				if (c1 == 'N' && c2 == 'H' && c3 == 'E') {
					raf.seek(i + 11);
					raf.write(newVal.getBytes()[0]);
					raf.seek(i + 12);
					raf.write(newVal.getBytes()[1]);
					raf.seek(i + 13);
					raf.write(newVal.getBytes()[2]);
					raf.seek(i + 14);
					raf.write(newVal.getBytes()[3]);
				}
			}
		}

		if (field.equals("COMPRESSION")) {
			for (int i = 0; i < 256; i++) {
				raf.seek(i);
				raf.seek(i);
				char c1 = (char) raf.read();
				raf.seek(i+1);
				char c2 = (char) raf.read();
				raf.seek(i+2);
				char c3 = (char) raf.read();
				if (c1 == 'C' && c2 == 'O' && c3 == 'M') {
					raf.seek(i+12);
					raf.write(newVal.getBytes()[0]);
					raf.seek(i+13);
					raf.write(newVal.getBytes()[1]);
					raf.seek(i+14);
					raf.write(newVal.getBytes()[2]);
					raf.seek(i+15);
					raf.write(newVal.getBytes()[3]);
					raf.seek(i+16);
					raf.write(newVal.getBytes()[4]);
					raf.seek(i+17);
					raf.write(newVal.getBytes()[5]);
					raf.seek(i+18);
					raf.write(newVal.getBytes()[6]);
					raf.seek(i+19);
					raf.write(newVal.getBytes()[7]);
					raf.seek(i+20);
					raf.write(newVal.getBytes()[8]);
					raf.seek(i+21);
					raf.write(newVal.getBytes()[9]);
				}
			}
		}
	}

	public void editDoubleHeader(String field, double newVal) throws IOException {

		int specialOffset = 768;

		if (field.equals("imon1")) {
			byte[] b = intToByteArray((int) newVal, 4);
			raf.seek(specialOffset + 528);
			raf.write(b[3]);
			raf.seek(specialOffset + 529);
			raf.write(b[2]);
			raf.seek(specialOffset + 530);
			raf.write(b[1]);
			raf.seek(specialOffset + 531);
			raf.write(b[0]);
		}

		if (field.equals("imon2")) {
			byte[] b = intToByteArray((int) newVal, 4);
			raf.seek(specialOffset + 532);
			raf.write(b[3]);
			raf.seek(specialOffset + 533);
			raf.write(b[2]);
			raf.seek(specialOffset + 534);
			raf.write(b[1]);
			raf.seek(specialOffset + 535);
			raf.write(b[0]);
		}

		if (field.equals("dexposuretimeinsec")) {
			int[] b = doubleToHex(newVal);
			raf.seek(specialOffset + 487);
			raf.write(b[0]);
			raf.seek(specialOffset + 486);
			raf.write(b[1]);
			raf.seek(specialOffset + 485);
			raf.write(b[2]);
			raf.seek(specialOffset + 484);
			raf.write(b[3]);
			raf.seek(specialOffset + 483);
			raf.write(b[4]);
			raf.seek(specialOffset + 482);
			raf.write(b[5]);
			raf.seek(specialOffset + 581);
			raf.write(b[6]);
			raf.seek(specialOffset + 580);
			raf.write(b[7]);
		}
	}

	public static byte[] intToByteArray(int value, int length) {
		byte[] b = new byte[length];
		for (int i = 0; i < length; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}

	public static int[] doubleToHex(double val) {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		DataOutputStream datastream = new DataOutputStream(bytestream);

		try {
			datastream.writeDouble(val);
			datastream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] bytes = bytestream.toByteArray();

		int[] hex = new int[8];

		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] < 0)
				hex[i] = bytes[i] + 256;
			else
				hex[i] = bytes[i];
		}

		return hex;
	}

	@Override
	public void close() throws IOException {
		raf.close();
	}
}
