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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;
import gda.factory.FactoryException;

public class DataSocketDetector extends DetectorBase implements BufferedDetector {
	private static final Logger logger = LoggerFactory.getLogger(DataSocketDetector.class);

	private DataSocket dataSocket;
	private String socketIpAddress = "bl20j-ts-panda-02";

	private int socketPort = 8889;
	private int frameCount;
	private int maxNumRetries = 19;
	private List<String> dataNames = Collections.emptyList();
	private String dataFormatString = "%.6g";
	private int maximumReadFrames = 50;

	public DataSocketDetector() {
		setInputNames(new String[] {});
	}

	@Override
	public void configure() throws FactoryException {
		try {
			connect();
			setConfigured(true);
		} catch (DeviceException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		dataSocket = null;
		setConfigured(false);
		configure();
	}

	@Override
	public String[] getExtraNames() {
		return dataNames.toArray(new String[] {});
	}

	@Override
	public String[] getOutputFormat() {
		return Collections.nCopies(dataNames.size(), dataFormatString).toArray(new String[] {});
	}

	private void connect() throws DeviceException {
		if (dataSocket == null) {
			try {
				dataSocket = new DataSocket(socketIpAddress, socketPort);
			} catch (IOException e) {
				throw new DeviceException("Problem creating connection to data socket on "+socketIpAddress);
			}
		}
	}

	@Override
	public void atScanStart() throws DeviceException {
		logger.debug("Clearing socket data at scan start");
		connect(); // make a new connection if necessary
		dataSocket.clearData();
		frameCount = -1;
	}

	@Override
	public void collectData() throws DeviceException {
		frameCount++; // the index of the frame being collected (0 for first).
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public Object readout() throws DeviceException {
		try {
			logger.debug("Reading frame {}", frameCount);
			dataSocket.updateValueData();
			waitForNumFrames(frameCount);
			return dataSocket.getFrame(frameCount, dataNames);
		} catch (IOException | InterruptedException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	public String getDataFormatString() {
		return dataFormatString;
	}

	public void setDataFormatString(String dataFormatString) {
		this.dataFormatString = dataFormatString;
	}

	public List<String> getDataNames() {
		return dataNames;
	}

	/**
	 * Set the names of the data to be read from the stream.
	 * These need to match up with the available fields, in the stream or exception
	 * will be thrown in {@link #readout()} and {@link #readFrames(int, int)}
	 *
	 * @param dataNames
	 */
	public void setDataNames(List<String> dataNames) {
		this.dataNames = dataNames;
	}

	/**
	 * @return List of data field names present in the stream
	 */
	public List<String> getAvailableFieldNames() {
		return dataSocket.fieldNames();
	}

	/**
	 * Set the dataNames to match all available ones present in the stream
	 * e.g. do this after running a scan, so that subsequent scans readout all the data.
	 * (Convenience method to avoid doing {@link #getAvailableFieldNames()} then {@link #setDataNames(List)})
	 */
	public void updateDataNamesFromStream() {
		this.dataNames = dataSocket.fieldNames();
	}

	@Override
	public void clearMemory() throws DeviceException {
		// Nothing needed here  - atScanStart clears any cached values
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		// passive detector, nothing to prepare
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return false;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		// passive detector, nothing to prepare
	}

	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return null;
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		try {
			dataSocket.updateValueData();
		} catch (IOException e) {
			throw new DeviceException("Problem getting number of frames from data socket", e);
		}
		return dataSocket.getNumFrames();
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		try {
			logger.debug("Reading frames : {} ...  {}", startFrame, finalFrame);
			dataSocket.updateValueData();
			waitForNumFrames(finalFrame);
			return dataSocket.getFrames(startFrame, finalFrame, dataNames).toArray();
		} catch (IOException | InterruptedException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		return readFrames(0, getNumberFrames());
	}

	/**
	 * Wait until specified number of data frames are available to read from socket.
	 * The number of available frames is checked every 0.5 sec; up to {@link #maxNumRetries}
	 * attempts will be made to wait for the requested number of frames
	 *
	 * @param numFrames
	 * @throws IOException
	 * @throws InterruptedException if requested number of frames is not available within 5 seconds.
	 */
	private void waitForNumFrames(int numFrames) throws IOException, InterruptedException {
		int numRetries = 0;
		while (numFrames >= dataSocket.getNumFrames() && numRetries < maxNumRetries) {
			Thread.sleep(500);
			dataSocket.updateValueData();
			numRetries++;
		}
		if (numRetries == maxNumRetries) {
			throw new IOException("Timed out waiting for frame "+numFrames+" on data socket. Highest frame reached = "+dataSocket.getNumFrames());
		}
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		return maximumReadFrames;
	}

	public void setMaximumReadFrames(int maximumReadFrames) {
		this.maximumReadFrames = maximumReadFrames;
	}

	public String getSocketIpAddress() {
		return socketIpAddress;
	}

	public void setSocketIpAddress(String socketIpAddress) {
		this.socketIpAddress = socketIpAddress;
	}

	public int getSocketPort() {
		return socketPort;
	}

	public void setSocketPort(int socketPort) {
		this.socketPort = socketPort;
	}

}
