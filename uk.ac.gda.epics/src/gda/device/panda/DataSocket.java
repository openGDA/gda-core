/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package gda.device.panda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.scannable.ScannableUtils;

public class DataSocket {
	private static final Logger logger = LoggerFactory.getLogger(DataSocket.class);

	private Socket socket;
	private BufferedReader inputStream;
	private BufferedWriter outputStream;
	private List<String> allData = new ArrayList<>();
	private List<String> fieldNames = Collections.emptyList();
	private List<Double[]> valuesData = new ArrayList<>();

	private int dataStartIndex = -1;
	private boolean connected;
	private String connectionOptions = "\n"; // Default connection options

	private static final String FIELD_STRING = "fields:";
	private static final String END_OF_DATA_STRING = "END";

	public DataSocket(String ipAddress, int port) throws IOException {
		connect(ipAddress, port);
	}

	public void connect(String ipAddress, int port) throws IOException {
		logger.info("Connecting to data socket on {}, port = {}", ipAddress, port);
		socket = new Socket(ipAddress, port);
		outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // send command to socket
		inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream())); // read from socket
		connected = true;
		sendCommand(connectionOptions);
	}

	public void disconnect() throws IOException {
		logger.info("Disconnecting from socket on {}",socket.getInetAddress().getCanonicalHostName());
		connected = false;
		inputStream.close();
		outputStream.close();
		socket.close();
	}

	public void checkConnected() throws IOException {
		if (!connected) {
			throw new IOException("Data socket stream is not connected");
		}
	}

	public void updateFromSocket() throws IOException {
		logger.debug("Updating data from socket");
		checkConnected();
		while(inputStream.ready()) {
			allData.add(inputStream.readLine());
		}
	}

	public void sendCommand(String command) throws IOException {
		checkConnected();
		outputStream.write(command);
		outputStream.flush();
	}

	public int getNumFrames() {
		return valuesData.size();
	}

	public Double[] getFrame(int frameIndex) {
		return valuesData.get(frameIndex);
	}

	public List<double[]> getFrames(int startFrame, int endFrame, List<String> dataNames) {
		List<Integer> nameIndices = dataNames.stream()
				.map(name -> fieldNames.indexOf(name)).toList();

		if (nameIndices.contains(-1)) {
			List<String> badNames = dataNames.stream().filter(n->!fieldNames.contains(n)).toList();
			throw new IllegalArgumentException("Data name '"+badNames+"' is not available from socket data. Data name should be one of "+fieldNames.toString());
		}

		List<double[]> selectedData = new ArrayList<>();
		List<Double[]> rangeData = valuesData.subList(startFrame, endFrame+1);
		for(var dataLine : rangeData) {
			double[] selectedRowData = nameIndices.stream().mapToDouble(index -> dataLine[index]).toArray();
			selectedData.add(selectedRowData);
		}
		return selectedData;
	}

	public double[] getFrame(int frameIndex, List<String> dataNames) {
		Double[] frameData = getFrame(frameIndex);
		List<Double> doubleVals = new ArrayList<>();
		for(var n : dataNames) {
			int ind = fieldNames.indexOf(n);
			if (ind == -1) {
				throw new IllegalArgumentException("Data name '"+n+"' is not available from socket data. Data name should be one of "+fieldNames.toString());
			}
			doubleVals.add(frameData[ind]);
		}
		return doubleVals.stream().mapToDouble(i->i).toArray();
	}

	public void clearData() {
		allData = new ArrayList<>();
		fieldNames = new ArrayList<>();
		valuesData = new ArrayList<>();
		dataStartIndex = -1;
	}

	public void updateValueData() throws IOException {
		logger.info("Extracting values from socket data");

		updateFromSocket();

		if (fieldNames.isEmpty()) {
			parseStreamData();
		}

		if (dataStartIndex < 0) {
			logger.warn("Data not yet available");
			return;
		}

		int startFrame = dataStartIndex + valuesData.size();
		for(int i=startFrame; i<allData.size(); i++) {
			String lineString = allData.get(i).trim();

			// exit the loop if there is the end of capture marker
			if (lineString.contains(END_OF_DATA_STRING)) {
				break;
			}
			String[] values = lineString.split("\\s+");
			Double[] dblValues = ScannableUtils.objectToArray(values);
			valuesData.add(dblValues);
		}
		logger.debug("Number of data values available : {}", valuesData.size());
	}

	public void showStreamData() {
		for(var l : allData) {
			System.out.println(l);
		}
	}

	public void parseStreamData() {
		logger.info("Extracting field names from data stream");

		if (allData.contains(FIELD_STRING)) {
			// there may be several consecutive sets of data in the stream - find the header for the last one
			// (i.e. data collection just started)
			int headerStartIndex = allData.lastIndexOf(FIELD_STRING)+1;

			int index = headerStartIndex;
			logger.debug("Field name start index : {}", index);
			fieldNames = new ArrayList<>();
			while(index < allData.size() && !allData.get(index).equals("")) {
				String[] splitLine = allData.get(index).strip().split("\\s+");
				String fieldName = splitLine[0]+"."+splitLine[2]; // add capture type (Diff, Value, Mean etc) after the name. e.g. COUNTER1.OUT_Diff
				fieldNames.add(fieldName);
				index++;
			}
			logger.info("Field names : {}", fieldNames);
			// index now points to empty line; the next line is the start of the data

			dataStartIndex = index+1;
			logger.debug("Data start index : {}", dataStartIndex);
		}
	}

	public List<String> fieldNames() {
		return fieldNames;
	}
}
