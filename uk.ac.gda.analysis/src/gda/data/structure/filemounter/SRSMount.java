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

package gda.data.structure.filemounter;

import gda.data.structure.Folder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.MatchResult;

/**
 * Mounting Files
 * <p>
 * The idea of mounting files came about because data files are getting more complex with multiple elements, large no.'s
 * of columns and a range of data. Nexus, for example can store scan, detector and image data in one large file. For
 * analysis plotting etc. we needed a way of looking at a file, determining its contents, and then pulling out what we
 * needed. This will save on memory.
 */
public class SRSMount extends Folder {

	/**
	 * SRS Specific
	 */
	// columnames in the event they aren't present in the data file
	String defaultColumn = new String("ABCDEFGHIJKLMNOPQRSTUVWXYZ");

	String filename = null;

	// The SRS header
	String header;

	// Column names
	Vector<String> columnNames = new Vector<String>();

	// Column count
	int columnCount = 0;

	// the data
	double[] data;

	int mountDepth;

	String[] prefix;

	/**
	 * Constructor.
	 * 
	 * @param filename
	 */
	public SRSMount(String filename) {
		this.filename = filename;
		Scanner sc = null;
		try {
			sc = new Scanner(new File(filename));
			while (sc.hasNext()) {
				String test = sc.next();
				if (test.contains("END")) {
					break;
				}

			}
			// Now read until we find some data
			String columns = null;
			String dataColumns = null;
			for (int i = 0; i < 20; i++) {
				columns = sc.nextLine();
				dataColumns = sc.nextLine();
				Scanner s = new Scanner(dataColumns);
				s.findInLine("(\\d+)");

				MatchResult result = s.match();
				if (result.groupCount() == 1) {
					break;
				}
				s.close();
			}
			if (columns == null && dataColumns == null) {
				throw new IllegalArgumentException("No data in this file");
			}
			// There is a preceding column list
			if (columns != null && columns.length() > 2) {
				Scanner s = new Scanner(columns);
				columnCount = 0;
				while (s.hasNext()) {
					columnCount++;
					columnNames.add(s.next());
				}
			} else {
				// read next line (data)
				//
				// columns = sc.nextLine();

				if (dataColumns != null && dataColumns.length() > 1) {
					Scanner s = new Scanner(dataColumns);
					columnCount = 0;
					while (s.hasNext()) {
						s.next();
						columnCount++;
						columnNames.add(this.defaultColumn.substring((columnCount - 1), columnCount));
					}
				} else {
					throw new IllegalArgumentException("No data in this file");
				}
			}
			sc.close();
		} catch (IllegalStateException e) {

		} catch (FileNotFoundException e1) {
			System.out.println("File not found :\t" + filename);
			e1.printStackTrace();
		}
	}

	@Override
	public Object getChild(String name) {
		Vector<Object> data = new Vector<Object>();

		if (columnNames.contains(name)) {
			Scanner sc = null;
			try {
				sc = new Scanner(new File(filename));
				while (sc.hasNext()) {
					String test = sc.next();
					if (test.contains("END")) {
						break;
					}

				}
				// Now read until we find some data
				String dataColumns = null;
				while (sc.hasNext()) {
					dataColumns = sc.nextLine();
					Scanner s = new Scanner(dataColumns);
					s.findInLine("(\\d+)");
					MatchResult result = s.match();
					if (result.groupCount() == 1) {
						int columncounter = 0;
						while (s.hasNext()) {
							Object o = s.next();
							if (columncounter == columnNames.indexOf(name)) {
								data.add(o);
							}
							columncounter++;
						}
					}
					s.close();
				}
				sc.close();
			} catch (IllegalStateException e) {

			} catch (FileNotFoundException e1) {
				System.out.println("File not found :\t" + filename);
				e1.printStackTrace();
			}

			return data;
		}

		return null;
	}

	@Override
	public Object getChild(int index) {
		Vector<Object> data = new Vector<Object>();

		if (index > 0 && index < columnNames.size()) {
			Scanner sc = null;
			try {
				sc = new Scanner(new File(filename));
				while (sc.hasNext()) {
					String test = sc.next();
					if (test.contains("END")) {
						break;
					}

				}
				// Now read until we find some data
				String dataColumns = null;
				for (int i = 0; i < 20; i++) {
					dataColumns = sc.nextLine();
					Scanner s = new Scanner(dataColumns);
					s.findInLine("(\\d+)");

					MatchResult result = s.match();
					if (result.groupCount() == 1) {
						break;
					}
					s.close();
				}
				// ok so we've found some numbers
				// The first of these is in dataColumns string

				while (sc.hasNext("(\\n)")) {
					Scanner s = new Scanner(dataColumns);
					int columncounter = 0;
					while (s.hasNext()) {
						Object o = s.next();
						if (columncounter == index) {
							columncounter++;
							data.add(o);
						}
					}

					dataColumns = sc.nextLine();

				}
				sc.close();
			} catch (IllegalStateException e) {

			} catch (FileNotFoundException e1) {
				System.out.println("File not found :\t" + filename);
				e1.printStackTrace();
			}
			return data;
		}

		return null;
	}

	/**
	 * Returns no of data columns in the SRS file
	 * 
	 * @return Number of columns
	 */
	@Override
	public int getChildCount() {
		return this.columnCount;
	}

	/**
	 * @param i
	 * @return Returns column name corresponding to index i
	 */
	@Override
	public String getChildName(int i) {
		return columnNames.get(i);
	}

}
