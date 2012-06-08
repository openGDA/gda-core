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

package gda.data.srs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <b>Title: </b>SRS stream writer/reader.
 * </p>
 * <p>
 * <b>Description: </b>Use this class to read and write SRS streams. The SRS data must be provided, or read, via a
 * SrsBuffer object.
 * </p>
 * 
 * @see gda.data.srs.SrsBuffer
 */

public class SrsFile {
	private static final Logger logger = LoggerFactory.getLogger(SrsFile.class);

	/**
	 * Write a SRS file
	 * 
	 * @param buf
	 *            The SRS buffer to use.
	 * @param filename
	 *            The filename to use
	 * @throws IOException
	 */
	public void writeFile(SrsBuffer buf, String filename) throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(filename));
			// Write into the stream (this will also flush it.)
			this.writeStream(buf, out);
		} catch (IOException e) {
			logger.error("ERROR: Could not write to " + filename + " in SrsFile#writeFile.");
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	/**
	 * Read a SRS file.
	 * 
	 * @param filename
	 *            The filename to read.
	 * @return The SRS buffer.
	 * @throws IOException
	 */
	public SrsBuffer readFile(String filename) throws IOException {
		BufferedReader in = null;
		SrsBuffer buf = null;
		try {
			in = new BufferedReader(new FileReader(filename));
			buf = this.readStream(in);
		} catch (FileNotFoundException e) {
			logger.error("ERROR: Could not open file to read in SrsFile#readFile");
		} finally {
			if (in != null) {
				in.close();
			}
		}

		return buf;
	}

	/**
	 * Write this buffer to the stream. The stream will be flushed, but not closed.
	 * 
	 * @param buf
	 *            The SrsBuffer.
	 * @param out
	 *            The BufferedWriter to use.
	 */
	public void writeStream(SrsBuffer buf, BufferedWriter out) {
		if ((out != null) && (buf != null)) {
			try {
				// Write the header
				if (buf.getHeader() != null) {
					logger.debug("*********Writing header: " + buf.getHeader());
					out.write(buf.getHeader());
					out.flush();

				}
				// Write the variables, and tab seperate them.
				for (String i : buf.getVariables()) {
					logger.debug("*********Write variable: " + i);
					out.write(i + "\t");
				}
				// Find the number of data points and write them, row wise, into
				// the
				// stream.
				int numberOfDataPoints = (buf.getData(buf.getVariables().elementAt(0))).size();
				logger.debug("buf.getVariables().elementAt(0): " + buf.getVariables().elementAt(0));
				logger.debug("buf.getData(buf.getVariables().elementAt(0)): "
						+ buf.getData(buf.getVariables().elementAt(0)));
				logger.debug("numberOfDataPoints: " + numberOfDataPoints);
				for (int dataPoints = 0; dataPoints < numberOfDataPoints; dataPoints++) {
					out.write("\n");
					logger.debug("datapoint: " + dataPoints);
					for (String i : buf.getVariables()) {
						logger.debug("buf.getData(" + i + "): " + buf.getData(i));
						logger.debug("Writing i: " + i + "  data: " + buf.getData(i).elementAt(dataPoints));
						out.write(buf.getData(i).elementAt(dataPoints) + "\t");
						logger.debug("Wrote it.");
					}
				}
				out.write("\n");
				out.flush();
				if (buf.getTrailer() != null) {
					logger.debug("Writing trailer...");
					out.write(buf.getTrailer());
				}
				out.flush();
			} catch (IOException e) {
				logger.error("ERROR: Caught IOException when trying to write SRS buffer.");
			}
		}
	}

	/**
	 * @param in
	 *            The input stream BufferedReader
	 * @return The SRS buffer
	 */
	public SrsBuffer readStream(BufferedReader in) {
		// Read the header.
		String line = null;
		String headerString = new String();
		int limit = 10000;
		int count = 0;
		do {
			try {
				line = in.readLine();
				// Message.debug("header: " + line);
			} catch (IOException e) {
				logger.error("ERROR: Could not read from SRS stream in SrsFile.");
				break;
			}
			if ((!line.contains("&SRS")) && (count == 0)) {
				break;
			}
			// System.err.println("header: " + line);
			headerString = headerString.concat(line + '\n');
			count++;
		} while ((!line.contains("&END")) && (count < limit));

		// Add the header to the SrsBuffer.
		SrsBuffer buf = new SrsBuffer();
		// System.err.println("header: " + headerString);
		buf.setHeader(headerString);

		// Read the data variables
		Vector<String> vars = null;
		// If the first line read was the variable line, then we don't need to
		// read another line.
		if (line != null) {
			if ((!line.contains("&SRS")) && (count == 0)) {
				vars = this.searchForTabbedElements(line);
			} else {
				try {
					line = in.readLine();
				} catch (IOException e) {
					logger.error(("ERROR: Could not read variable line from SRS stream in SrsFile."));
				}
				vars = this.searchForTabbedElements(line);
			}
		}

		// Now read the data lines
		Vector<Vector<String>> data = new Vector<Vector<String>>();
		while (true) {
			try {
				line = in.readLine();
				// Message.debug("line: " + line);
			} catch (IOException e) {
				logger.error(("ERROR: Could not read data line from SRS stream in SrsFile."));
			}
			if (line == null) {
				break;
			}
			// Vector<String> a = this.searchForTabbedElements(line);
			// Message.debug("a: " + a);
			// data.add(a);
			data.add(this.searchForTabbedElements(line));
		}

		// Now create variable specific data arrays for putting into an SRS data
		// buffer.
		// Vector<Vector<String>> newData = new
		// Vector<Vector<String>>(data.elementAt(0).size());
		// Add empty vectors to the newData
		// for (int i=0; i<data.elementAt(0).size(); i++) {
		// newData.add(new Vector<String>());
		// }
		// for (int i=0; i<data.size(); i++) {
		// for (int j=0; j<data.elementAt(0).size(); j++) {
		// newData.elementAt(j).add(data.elementAt(i).elementAt(j));
		// }
		// }
		// Now put the variable / data pairs into the SRS buffer
		// Message.debug("vars.size(): " + vars.size());
		// Message.debug("newData.size(): " + newData.size());
		for (int datasize = 0; datasize < data.size(); datasize++) {
			if (vars != null) {
				for (int i = 0; i < vars.size(); i++) {
					// Message.debug("vars.elementAt(" + i + "): " +
					// vars.elementAt(i));
					// Message.debug("data.elementAt(" + i + "): " +
					// newData.elementAt(i));
					// for (int j=0; j<newData.elementAt(i).size(); j++) {
					buf.setData(vars.elementAt(i), data.elementAt(datasize).elementAt(i));
					// }
				}
			}
		}

		// Now read any trailer.
		// quite pointless, as EOF was seen above already
		try {
			line = in.readLine();
		} catch (IOException e) {
			logger.debug(("ERROR: Could not read trailer line from SRS stream in SrsFile."));
		}
		if (line != null) {
			buf.setTrailer(line);
		}

		return buf;
	}

	/**
	 * Method which reads all the tabbed separated values from a String.
	 * 
	 * @param line
	 * @return The found eleemnts in a Vector<String>
	 */
	private Vector<String> searchForTabbedElements(String line) {
		Vector<String> vars = new Vector<String>();

		// Check the line
		if ((line == null) || (line.length() == 0)) {
			return null;
		}
		// Add an end-of-line
		String newLine = line + '\n';

		char ch = '0';
		int index = 0;
		StringBuffer var = new StringBuffer(newLine.length());
		// Get the first character
		ch = newLine.charAt(index);
		var.append(ch);
		// Loop to get the next characters, and test to find tabs and
		// end-of-lines.
		while (true) {
			index++;
			if ((ch == '\t') || (ch == '\n')) {
				vars.add(var.substring(0, var.length() - 1).toString());
				var.delete(0, var.length());
			}
			// System.err.println("var: " + var);
			if (index == line.length()) {
				vars.add(var.substring(0, var.length()).toString());
				break;
			}
			ch = newLine.charAt(index);
			var.append(ch);
		}

		// Get rid of any excess tabs.
		for (int i = 0; i < vars.size(); i++) {
			if (vars.elementAt(i).trim().length() == 0) {
				vars.remove(i);
			}
		}

		// System.err.println("vars: " + vars);

		return vars;
	}

}
