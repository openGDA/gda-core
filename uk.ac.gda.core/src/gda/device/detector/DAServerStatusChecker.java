/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.device.detector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;


public class DAServerStatusChecker extends ScannableBase {
	private static final Logger logger = LoggerFactory.getLogger(DAServerStatusChecker.class);
	private Optional<DAServer> daServer = Optional.empty();
	private String statusMessage = "";
	private int timeoutMs = 5000; // Connection timeout [milliseconds]

	@Override
	public void configure() {
		// Set to empty lists to avoid exceptions when formatting the position
		setOutputFormat(new String[]{});
		setInputNames(new String[]{});
	}

	/**
	 * @throws DeviceException if DAServer is not available
	 */
	@Override
	public void atScanStart() throws DeviceException {
		if (!checkStatus()) {
			throw new DeviceException("Problem found when checking DAServer : "+statusMessage);
		}
	}

	/**
	 * Check DAServer is available and running correctly, by running {@link #checkStatus(String, int)} using host and port information from
	 * {@link DAServer} reference (set using {@link #setDaServer(DAServer)}).
	 * @return true if DAServer is available
	 */
	public boolean checkStatus() {
		if (daServer.isPresent()) {
			return checkStatus(daServer.get().getHost(), daServer.get().getPort());
		}
		setStatusMessage("Can't check status, DAServer has not been set");
		logger.info("Can't check status, DAServer has not been set");
		return true;
	}

	/**
	 * Check DAServer running on specified host and port to ensure it is available and produces correct response.
	 * statusMessage contains more information about the result of the check.
	 * @param host
	 * @param port
	 * @return true if DAServer is running ok, false otherwise
	 */
	public boolean checkStatus(String host, int port) {
		InetSocketAddress inetAddr = new InetSocketAddress(host, port);
		if (inetAddr.isUnresolved()) {
			setStatusMessage("Could not resolve IP address for "+host);
			logger.warn("Could not resolve IP address for {}", host);
			return false;
		}

		try(Socket socket = new Socket()) {
			socket.connect(inetAddr, timeoutMs);
			socket.setSoTimeout(timeoutMs);
			return testResponse(socket);
		} catch (IOException e) {
			// UnknownhostException - Error making connection (wrong IP address)
			// ConnectException - Correct IP address, Connection refused (incorrect socket and not allow to connect OR DAServer not running)
			setStatusMessage("Error making connection to DAServer on "+host+" on port "+port);
			logger.error("Error making connection to DAServer on {} on port {}", host, port);
			if (e instanceof ConnectException) {
				setStatusMessage("Error making connection - DAServer might not be running, or port "+port+" not accessible");
				logger.info("DAServer might not be running, or port {} not accessible", port);
			}
			return false;
		}
	}

	/**
	 * Test DAServer response by getting it to do some simple maths.
	 * @param socket
	 * @return true if DAServer server response is correct, false otherwise
	 */
	private boolean testResponse(Socket socket) {
		logger.info("Testing DAServer response...");
		String testInput = "2*3";
		String expectedResult = "> * 6";

		try( BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())) )  {

			logger.info("Sending '{}' to DAserver", testInput);

			out.write(testInput+'\n');
			out.flush();

			String result = in.readLine();
			logger.info("DAServer response : '{}'", result);
			if (!result.equals(expectedResult)) {
				setStatusMessage("DAServer response was not correct. Expected '"+expectedResult+"' but got '"+result+"'");					logger.warn("Response was not correct - expected '{}'", expectedResult);
				return false;
			} else {
				setStatusMessage("DAServer running ok");
				logger.info("Response ok");
				return true;
			}
		} catch (IOException e) {
			setStatusMessage("Problem reading from DAServer");
			logger.error("Problem reading from DAServer", e);
			return false;
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public void asynchronousMoveTo(Object position) {
		// do nothing
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return null;
	}

	public DAServer getDaServer() {
		return daServer.orElse(null);
	}

	public void setDaServer(DAServer daServer) {
		this.daServer = Optional.of(daServer);
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
}