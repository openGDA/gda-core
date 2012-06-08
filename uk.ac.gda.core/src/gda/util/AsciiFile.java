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

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class to handle writing of ascii files
 */
public class AsciiFile {
	private BufferedReader in = null;

	private BufferedWriter out = null;

	private static String string = null;

	/**
	 * Append String line to ascii file
	 * 
	 * @param file
	 *            String file name of file
	 * @param text
	 *            String text to be appended to file
	 * @throws IOException
	 */
	public static void appendln(String file, String text) throws IOException {
		text += "\n";
		append(file, text);
	}

	/**
	 * Append single integer as line in ascii file
	 * 
	 * @param file
	 *            String file name of file
	 * @param value
	 *            int value of integer to be written to file as ascii string
	 * @throws IOException
	 */
	public static void appendln(String file, int value) throws IOException {
		appendln(file, (new Integer(value)).toString());
	}

	/**
	 * Append single double value as line in ascii file
	 * 
	 * @param file
	 *            String file name of file
	 * @param value
	 *            double value of double to be written to file as ascii string
	 * @throws IOException
	 */
	public static void appendln(String file, double value) throws IOException {
		appendln(file, (new Double(value)).toString());
	}

	/**
	 * Append single java.awt.Dimension value as 2 lines in ascii file
	 * 
	 * @param file
	 *            String file name of file
	 * @param d
	 *            java.awt.Dimension value of java.awt.Dimension to be written to file as ascii string
	 * @throws IOException
	 */
	public static void appendln(String file, Dimension d) throws IOException {
		appendln(file, d.width);
		appendln(file, d.height);
	}

	/**
	 * Append String to ascii file without formatting (& without EOL)
	 * 
	 * @param file
	 *            String file name of file
	 * @param text
	 *            String text to be appended to file
	 * @throws IOException
	 */
	public static void append(String file, String text) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
		out.write(text);
		out.close();
	}

	/**
	 * Opens file for input. Note that there is only one BufferedReader per AsciiFile.
	 * 
	 * @param file
	 *            String name of file
	 * @throws IOException
	 */
	public void openForInput(String file) throws IOException {
		in = new BufferedReader(new FileReader(file));
	}

	/**
	 * Read String from file previously opened for input
	 * 
	 * @see gda.util.AsciiFile#openForInput
	 * @see gda.util.AsciiFile#readString
	 * @return String read from file
	 * @throws IOException
	 */
	public String read() throws IOException {
		return readString();
	}

	/**
	 * Read lines of data from file to end of file (one line per arrary element of String[])
	 * 
	 * @return String[]
	 * @throws IOException
	 */
	public String[] readLines() throws IOException {
		ArrayList<String> list = new ArrayList<String>();

		while ((string = in.readLine()) != null)
			list.add(string);

		return list.toArray(new String[0]);
	}

	/**
	 * Read String from file using {@link java.io.BufferedReader#readLine()} up to EOL
	 * 
	 * @return string String read from file or null
	 * @throws IOException
	 */
	public String readString() throws IOException {
		string = null;

		if (in != null) {
			string = in.readLine();
		}

		return string;
	}

	/**
	 * Read next line from file and interprets as integer
	 * 
	 * @return value int parsed from file, zero if file not open!
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public int readInt() throws IOException, NumberFormatException {
		int value = 0;

		if (in != null) {
			string = in.readLine();
			if (string != null)
				value = Integer.parseInt(string);
		}

		return value;
	}

	/**
	 * Read next line from file and interprets as double
	 * 
	 * @return value double parsed from file, zero if file not open!
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public double readDouble() throws IOException, NumberFormatException {
		double value = 0.0;

		if (in != null) {
			string = in.readLine();
			if (string != null)
				value = Double.parseDouble(string);
		}

		return value;
	}

	/**
	 * Read next line from file and interprets as java.awt.Dimension
	 * 
	 * @return value java.awt.Dimension parsed from file, zero, zero if file not open!
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public Dimension readDimension() throws IOException, NumberFormatException {
		String string;
		int width = 0, height = 0;

		string = readString();
		if (string != null)
			width = Integer.parseInt(string);

		string = readString();
		if (string != null)
			height = Integer.parseInt(string);

		return new Dimension(width, height);
	}

	/**
	 * Close file
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (in != null) {
			in.close();
			in = null;
		}
		if (out != null) {
			out.close();
			out = null;
		}
	}
}
