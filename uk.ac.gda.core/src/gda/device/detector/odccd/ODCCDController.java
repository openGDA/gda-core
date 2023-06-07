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

package gda.device.detector.odccd;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.commons.lang.NotImplementedException;

import gda.device.DeviceException;
import gda.device.ODCCD;
import gda.device.detector.DetectorBase;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * <p>
 * <b>Title: </b>Class to communicate with Oxford Diffraction IS CCD control software.
 * </p>
 * <p>
 * <b>Description: </b> This class provides an API to the IS software for Oxford Diffraction CCD detectors. It expects
 * that the IS software is up and running on a Windows remote host (IS only runs on Windows). This class opens a socket
 * on port <b>9120</b> of the remote host (this can be configured in the IS startup scripts on the remote host). The IS
 * software login name used by this class is 'gda'.
 * </p>
 */
@Deprecated(since="GDA 9.31", forRemoval=true)
public class ODCCDController extends DetectorBase implements ODCCD {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(ODCCDController.class);

	// Private data members. These may contain some hard coded parameters
	// to enable communication to IS software.

	/** The end of line string sent from the IS server. */
	private String mEndChar = "\012\015";

	/** Reference to the socket wrapper. */
	private ODCCDNativeSock _mSock = null;

	/** Login name. */
	private String mLoginName = "gda";

	/** IS binary header information */
	public ISBinaryHeader mBinaryHeader = new ISBinaryHeader();

	/** Name of data. */
	private String mDataName = null;

	/** Boolean flag to indicate if this object is connected to IS or not. */
	private boolean mConnected = false;

	private int Port=9120;//9120
	/**
	 * @return mloginName
	 */
	public String getMLoginName() {
		return mLoginName;
	}

	/**
	 * @param loginName
	 */
	public void setMLoginName(String loginName) {
		logger.trace("setMLoginName({})", loginName);
		mLoginName = loginName;
	}

	/**
	 * @return port
	 */
	public int getPort() {
		return Port;
	}

	/**
	 * @param port
	 */
	public void setPort(int port) {
		logger.trace("setPort({})", port);
		Port = port;
	}

	/**
	 * Constructor.
	 */
	public ODCCDController() {
		logger.deprecatedClass("9.33", "nothing, Crysalis/ODCCD is no longer supported in GDA");
		// Create a socket using NativeSock.
		_mSock = new ODCCDNativeSock();
	}

	/**
	 * @return the ODCCDNativeSock in use
	 */
	public ODCCDNativeSock getODCCDNativeSock() {
		return _mSock;
	}

	/**
	 * Connect to the IS software on remote host.
	 *
	 * @param host
	 *            The remote host IS is running on.
	 * @throws IOException
	 */
	@Override
	public synchronized void connect(String host) throws IOException {
		logger.trace("connect({})", host);
		// String to store the initial response from the IS server.
		String theInput = null;

		try {
			// Connect to IS software on remote host at 9120.
			getODCCDNativeSock().connect(host, Port);

			// Read first line
			theInput = getODCCDNativeSock().readUntil(mEndChar);
			// String loginString = "Welcome client " + mLoginName;
			String loginString = "Welcome client ";
			if (theInput.contains(loginString + mLoginName) == true) {
				theInput = theInput + getODCCDNativeSock().readUntil("Connection restored.");
				mConnected = true;
				// Switch the IS server to binary mode if it already isn't in
				// it.
				writeCommand("binary");
			} else if (theInput.contains(loginString + "Client_")) {
				// Read next line
				theInput = getODCCDNativeSock().readUntil(mEndChar);
				if (theInput.contains("Connection restored.")) { //always logout
					writeCommand("logout gda");
					// Connect to IS software on remote host at 9120.
					try {
						Thread.sleep(500); // Wait here for a little while,
						// otherwise it is too fast.
					} catch (InterruptedException e) {
						logger.error("ERROR: Caught InteruptedException when waiting to connect to IS.");
					}
					getODCCDNativeSock().connect(host, Port);
					// Read first line
					theInput = getODCCDNativeSock().readUntil(mEndChar);
					newConnectProcedure();
				} else {
					newConnectProcedure();
				}
			} else {
				newConnectProcedure();
			}
		} catch (IOException e) {
			logger.error("ERROR: Caught IOException when trying to log into IS software. host - " + host);
			mConnected = false;
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("ERROR: Caught IOException when trying to log into IS software. host - " + host,
					e);
		}
	}

	/**
	 * Method to connect for the first time the GDA client.
	 *
	 * @throws IOException
	 */
	private synchronized void newConnectProcedure() throws IOException {
		logger.trace("newConnectProcedure()");
		//from IS_v2_0_1096 we no longer get the message Script init completed
		String theInput = "";//getODCCDNativeSock().readUntil("Script init completed.");
		mConnected = true;
		String loginString = "login " + mLoginName;
		writeCommand(loginString);
		theInput = theInput + getODCCDNativeSock().readUntil("OK");
		// Switch the IS server to binary mode.
		writeCommand("binary");
	}

	/**
	 * Use this method to disconnect from older versions of the IS software.
	 *
	 * See also {@link ODCCDController#logout}
	 */
	@Override
	public synchronized void disconnect() {
		if(!mConnected){
			logger.warn("Disconnect method called when not connected");
			return;
		}
		try {
			this.writeCommand("disconnect");
		} finally {
			getODCCDNativeSock().disconnect();
		}
		mConnected = false;
	}

	/**
	 * Use this method to disconnect from newer versions of the IS software.
	 *
	 * See also {@link ODCCDController#disconnect}
	 */
	public synchronized void logout() {
		if(!mConnected){
			logger.warn("Logout method called when not connected");
			return;
		}
		try {
			this.writeCommand("logout");
			logger.debug("Flushed {} characters", this.flush(true));
		} finally {
			if (mConnected) {
				logger.error("Flush completed but still connected!");
			}
			mConnected = false;
			getODCCDNativeSock().disconnect();
		}
	}

	/**
	 * Is the CCD control object connected to the CCD?
	 *
	 * @return true or false
	 */
	@Override
	public boolean isConnected() {
		return mConnected;
	}

	/**
	 * Send a command to IS. All commands sent to IS go through this method.
	 *
	 * @param command
	 */
	private synchronized void writeCommand(String command) {
		logger.trace("writeCommand({})...", command);
		// Check is we connected successfully to IS
		if (!mConnected) {
			throw new IllegalStateException("ERROR: ODCCDController is trying to use IS and is not connected.");
		}
		// Now try to send command.
		try {
			logger.debug("Sending IS command " + command);
			getODCCDNativeSock().write(command);
		} catch (Exception e) {
			throw new RuntimeException("ERROR when trying to write command in ODCCDController. " + "command: "
					+ command, e);
		}
		logger.trace("...writeCommand({})", command);
	}

	/**
	 * Read input stream until <code>str</code> is found.
	 *
	 * @param str
	 *            The string to search for.
	 * @return input data
	 * @throws IOException
	 */
	public synchronized String readInputUntil(String str) throws IOException {
		return getODCCDNativeSock().readUntil(str, null);
	}

	public synchronized String readInputUntil(String str, String altError) throws IOException {
		logger.trace("readInputUntil({}, {})", str, altError);
		return getODCCDNativeSock().readUntil(str, altError);
	}

	/**
	 * What command do we want to use here? Don't forget to call super.collectData()
	 *
	 * @throws DeviceException
	 */
	@Override
	public synchronized void collectData() throws DeviceException {
		logger.trace("collectData() called, ignoring...");
	}

	/**
	 * How do we decide when we are in what status? Don't forget to call super.getStatus()
	 *
	 * @return the status
	 * @throws DeviceException
	 */
	@Override
	public synchronized int getStatus() throws DeviceException {
		return 0;
	}

	/**
	 * What is this used for? Is this used with this.collectData() in a scan? Don't forget to call super.readout();
	 *
	 * @return the data
	 * @throws DeviceException
	 */
	@Override
	public synchronized Object readout() throws DeviceException {
		logger.trace("readout() called, returning null");
		return null;
	}

	/**
	 * Returns the name of the last data read from the CCD.
	 *
	 * @return The data name.
	 */
	@Override
	public String getDataName() {
		return mDataName;
	}

	/**
	 * Read the CCD temperature.
	 *
	 * @return The CCD temperature.
	 */
	@Override
	public synchronized double temperature() {
		throw new NotImplementedException();
	}

	/**
	 * Read the chiller unit water temperature.
	 *
	 * @return The water temperature.
	 */
	@Override
	public synchronized double waterTemperature() {
		throw new NotImplementedException();
	}

	/**
	 * Take a single image.
	 *
	 * @param secs
	 *            Time of exposure.
	 * @return The image data
	 */
	@Deprecated(since="at least 2012")
	public synchronized byte[] smi(int secs) {
		throw new NotImplementedException();
	}

	/**
	 * Take a darkcurrent
	 *
	 * @param secs
	 *            Time of exposure
	 * @return The dark current image data.
	 */
	@Deprecated(since="at least 2012")
	public synchronized byte[] darkcurrent(int secs) {
		throw new NotImplementedException();
	}

	/**
	 * Use this method to call a user script on the IS host. Example 1: call save_dark 1.0 2 \"d:/dark2.img\" Example 3:
	 * call dark_cor 10.0 2 "//root/Darks/"
	 *
	 * @param command
	 *            The command to run on IS
	 */
	@Override
	public synchronized void runScript(String command) {
		this.writeCommand(command);
	}

	/**
	 * Reads the data from an IS database node.
	 *
	 * @param pathname
	 *            The location of the data
	 * @return The data in a <code>byte[]</code> array.
	 */
	@Override
	@Deprecated(since="at least 2012")
	public synchronized ODCCDImage readDataFromISDataBase(String pathname) {
		throw new NotImplementedException();
	}

	/**
	 * Read an image by reading several data frames from IS and appending the data buffers.
	 *
	 * @return The image in a ODCCDImage object.
	 */
	public synchronized ODCCDImage readImage() {
		throw new NotImplementedException();
	}

	/**
	 * @return int
	 */
	public synchronized int flush(boolean expectEOF){
		logger.trace("flush()");

		StringBuffer sb = new StringBuffer();
		if(mConnected){
			ODCCDNativeSock sock = getODCCDNativeSock();
			int timeout = sock.getSocketTimeOut();
			try{
				sock.setSocketTimeOut(1000);
				while(true){
					sb.append((char)sock.readByte());
				}
			}
			catch(SocketTimeoutException ex){
				//do nothing
			}
			catch(EOFException ex) {
				logger.info("EOF exception while flushing(). Socket must have been disconnected. isConnected={}, isClosed={}",
						getODCCDNativeSock().socket.isConnected(), getODCCDNativeSock().socket.isClosed());
				mConnected = false;
				getODCCDNativeSock().disconnect();
			}
			catch(IOException ex){
				logger.error("Error reading from socket", ex);
			}
			finally{
				logger.trace("Flushed: {}", sb.toString());
				sock.setSocketTimeOut(timeout);
			}
		} else {
			logger.warn("Flush method called when not connected");
		}
		return sb.length();
	}
	/**
	 * Read the shutter status
	 *
	 * @return OPEN or CLOSED
	 */
	@Override
	public synchronized String shutter() {
		this.writeCommand("shutter");
		try {
			// Read until we see the response from the detector IS object.
			String response = getODCCDNativeSock().readUntil("detector:", null);
			// Read the rest of the line
			response = getODCCDNativeSock().readLine();

			if (response.contains("OPEN")) {
				return new String("OPEN");
			} else if (response.contains("CLOSED")) {
				return new String("CLOSED");
			} else {
				logger.error("ERROR: Incorrect response to shutter command in ODCCDController.");
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException("Error when reading shutter status.", e);
		}
	}

	/**
	 * Method to open the shutter. It returns the status of the shutter.
	 *
	 * @return OPEN
	 */
	@Override
	public synchronized String openShutter() {
		this.writeCommand("sh o");
		return this.shutter();
	}

	/**
	 * Method to close the shutter. It returns the status of the shutter.
	 *
	 * @return CLOSED
	 */
	@Override
	public synchronized String closeShutter() {
		this.writeCommand("sh c");
		return this.shutter();
	}

	@Override
	public synchronized boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "OD CCD Controller";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "unknown";
	}

}
