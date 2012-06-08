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
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for reading values from terminal
 */
public class ConsoleReader extends BufferedReader {
	private static final Logger logger = LoggerFactory.getLogger(ConsoleReader.class);

	private static ConsoleReader instance = new ConsoleReader();

	private ConsoleReader() {
		super(new InputStreamReader(System.in));
	}

	/**
	 * Returns the instance (ConsoleReader is a singleton). Unnecessary since all methods are static.
	 * 
	 * @return instance the one and only ConsoleReader
	 */
	public static ConsoleReader getInstance() {
		return instance;
	}

	/**
	 * Creates the instance.
	 */
	public static void initialize() {
		if (instance == null)
			instance = new ConsoleReader();
	}

	/**
	 * Reads a string (up to CR/LF) from console and returns it.
	 * 
	 * @return valueString the String read
	 */
	public static String readString() {
		String valueString = null;

		initialize();

		try {
			valueString = instance.readLine();
		} catch (IOException ioe) {
			valueString = " ";
			logger.debug("IOException caught " + ioe);
		}
		return (valueString);
	}

	/**
	 * Prints a prompt and reads a double from the console.
	 * 
	 * @param prompt
	 *            string for console
	 * @return the double read
	 */
	public static double readDouble(String prompt) {
		System.out.print(prompt);
		String valueString = readString();
		return (Double.valueOf(valueString).doubleValue());
	}

	/**
	 * Prints a prompt and reads an integer from the console.
	 * 
	 * @param prompt
	 *            string for console
	 * @return the integer read
	 */
	public static int readInteger(String prompt) {
		System.out.print(prompt);
		String valueString = readString();
		return (Integer.valueOf(valueString).intValue());
	}

	/**
	 * Prints a prompt and reads a character from the console.
	 * 
	 * @param prompt
	 *            string for console
	 * @return the character read
	 */
	public static char readCharacter(String prompt) {
		System.out.print(prompt);

		String valueString = readString();

		if (valueString.length() > 0) {
			return (valueString.charAt(0));
		}
		return (' ');
	}

	/**
	 * Prints a prompt and reads a String from the console.
	 * 
	 * @param prompt
	 *            string for console
	 * @return the String read
	 */
	public static String readString(String prompt) {
		System.out.print(prompt);
		return (readString());
	}
}