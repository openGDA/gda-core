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

package gda.jython.externalclients;

import gda.factory.FactoryException;
import gda.jython.JythonServerFacade;
import gda.jython.JythonServerStatus;
import gda.jython.Terminal;
import gda.scan.ScanDataPoint;
import gda.util.ObjectServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class communicates with the Jython Server via Corba and can act as a terminal to receive Jython output. This class is intended to be used for clients of
 * the CommandServer other than the GDA client.
 */
public class ExternalJavaClient implements Terminal

{

	private static final Logger logger = LoggerFactory.getLogger(ExternalJavaClient.class);

	private JythonServerFacade facade = null;

	Vector<String[]> dataBuffer = new Vector<String[]>();

	private HashMap<String, String> scanFileMap = new HashMap<String, String>();

	private String filename;

	private String scanUniqueName;

	private int status;

	/**
	 * @throws FactoryException
	 */
	public ExternalJavaClient() throws FactoryException {

		ObjectServer.createClientImpl();
		try {
			facade = JythonServerFacade.getInstance();
			facade.addIObserver(this); //FIXME: potential race condition
			logger.debug("Server facde " + facade);
		} catch (NullPointerException e) {
			logger.error("Can't find a command_server. Exiting.");
			// System.exit(0);
		}
	}

	@Override
	public void write(byte[] output) {
		write(new String(output));
	}

	@Override
	public void write(String output) {
		logger.debug(output);
	}

	/**
	 * Run a command.
	 *
	 * @param comm
	 *            - the command to run.
	 */
	public void send(String comm) {
		dataBuffer.clear();
		facade.runCommand(comm, "Facade");
	}

	/**
	 * Retrieve an object of the data from the last scan in string form.
	 *
	 * @return a array of array of string.
	 */
	public String[][] getData() {

		// convert vector into an array of string arrays
		String[][] output = new String[dataBuffer.size()][];
		for (int i = 0; i < dataBuffer.size(); i++) {
			output[i] = dataBuffer.get(i);
		}
		return output;

	}

	@Override
	public void update(Object name, Object data) {
		if (data instanceof ScanDataPoint) {
			ScanDataPoint pt = (ScanDataPoint) data;
			if (pt.getCreatorPanelName().equals("facade") && !pt.getUniqueName().equals(scanUniqueName)) {
				scanUniqueName = pt.getUniqueName();
				filename = pt.getCurrentFilename();
				scanFileMap.put(scanUniqueName, filename);
			}
			logger.debug(pt.getUniqueName());
			String point = ((ScanDataPoint) data).toString();
			if (point.lastIndexOf("\t") > -1) {
				String[] temp = point.split("\t");
				dataBuffer.add(temp);
			}
			// Message.debug(point);
		}
		if (data instanceof JythonServerStatus) {
			logger.debug("the server status is " + ((JythonServerStatus) data).scanStatus);
			status = ((JythonServerStatus) data).scanStatus;
		}
	}

	/**
	 * @return the status of the Command Server
	 */
	public int getStatus() {
		return status;

	}

	/**
	 * @return the filename of the latest scan.
	 */
	public String getFileName() {
		logger.debug("The scan id is " + scanUniqueName);
		return filename;
	}

	/**
	 * @param filename
	 * @return array of array of data in string form.
	 */
	public String[][] getDataFromFile(String filename) {
		String fileURL = "C:\\users\\data" + File.separator + filename;
		boolean dataReady = false;
		Vector<String[]> buffer = new Vector<String[]>();
		String[][] data = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(fileURL))) {
			while (true) {
				String line = reader.readLine();
				// Message.debug("The String is " + line);
				if (line == null)
					break;
				line = line.trim();
				if (dataReady) {
					// Message.debug("The String in data array is " + line);
					StringTokenizer str = new StringTokenizer(line);
					String[] array = new String[str.countTokens()];
					int i = 0;
					while (str.hasMoreTokens()) {
						array[i] = str.nextToken();
						i++;
					}
					buffer.add(array);

				}
				if (line.startsWith("&SRS"))
					continue;
				if (line.startsWith("&END")) {
					dataReady = true;
					reader.readLine();
				}

			}
			data = new String[buffer.size()][];
			for (int i = 0; i < buffer.size(); i++) {
				data[i] = buffer.get(i);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
}
