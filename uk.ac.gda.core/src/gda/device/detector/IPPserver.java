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

package gda.device.detector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.scannable.ScannableUtils;

/**
 * Interface to the ImageProPlus package for data collection from a Photonics Science CCD.
 */
public class IPPserver extends DetectorBase implements Detector {

	private static final Logger logger = LoggerFactory.getLogger(IPPserver.class);

	// Valid values for input parameter destVri in method IpAcqSnap
	// private final int ACQ_CURRENT = 0;
	// private final int ACQ_FILE = 1;
	private final int ACQ_NEW = 2;

	// private final int ACQ_SEQUENCE_APPEND = 3;

	private int mode = ACQ_NEW;

	// private final int ACQ_CMD_CAPTURE_AREA = 48;
	private final int ACQ_CMD_EXPOSURE_TIME = 49;

	// private final int ACQ_CMD_BINNING = 52;
	// private final int ACQ_CMD_GAIN = 53;
	// private final int ACQ_CMD_EXPOSURE_TIME_AS_SINGLE = 84;

	private String outputFolderRoot = new String("c:\\data");

	private String fileNameRoot = new String("test");

	private String fileFormat = new String("tif");

	private String host;

	private int port = -1;

	private ClientSocketHandlerAdapter ippServerSocket = null;

	private String lastImagePathName = "";

	private boolean busyFlag = false;

	// TODO - implement non-blocking commands so Jython not blocked? - ie
	// put
	// commands in queue and schedule on worker thread
	private class ClientSocketHandlerAdapter {
		private Socket theSocket = null; // input/output raw socket

		private BufferedWriter out = null; // buffered socket output

		private BufferedReader in = null; // buffered socket input

		private boolean connected = false;

		// sleep time interval for blocking reads
		private int waitTime = 25; // (rdw) changed from 1000!

		private int ackTimeout = 0;

		/**
		 * Creates the socket and its associated input and output streams
		 */
		public void openSocket() {
			/*
			 * Creating the Socket or getting the input or output streams from it can cause an IOException which is
			 * thrown on. Creating the Socket can also cause an UnknownHostException which is caught here.
			 */
			try {
				logger.debug("IPPserver::ClientSocketHandlerAdapter opening socket");

				theSocket = new Socket(getHost(), getPort());

				logger.debug("IPPserver::ClientSocketHandlerAdapter socket opened ok");

				out = new BufferedWriter(new OutputStreamWriter(theSocket.getOutputStream()));
				in = new BufferedReader(new InputStreamReader(theSocket.getInputStream()));

				boolean test = in.ready();
				if (test == true) {
					logger.debug("IPPserver::ClientSocketHandlerAdapter socket ready");
				}

				// make sure connected message comes through
				if (!theSocket.isClosed() && theSocket.isConnected()) {
					connected = true;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						logger.error("IPPServer.openSocket() caught InteruptedException: ", e);
					}
					String connectedMessage = readNB();
					logger.debug("IPPserver plugin connection reply:\r\n" + connectedMessage);
				} else {
					connected = false;

					logger.debug("Failed to receive connection acknowledge from IPPserver plugin");
					return;
				}
			} catch (IOException ioe) {
				/*
				 * FIXME: Possibly UnknownHostException should be terminal because there is no way to recover.
				 */
				logger.error("Failed to connect to {}:{}", getHost(), getPort(), ioe);
			}

		}

		/**
		 * Closes the socket and associated input and output streams.
		 */
		public void closeSocket() {
			if (theSocket != null) {
				logger.debug("IPPserver::ClientSocketHandlerAdapter closing socket");
				try {
					connected = false;
					out.close();
					in.close();
					theSocket.close();

					logger.debug("IPPserver::ClientSocketHandlerAdapter closed socket ok");
				} catch (IOException ioe) {
					logger.error("Could not close socket", ioe);
				} finally {
					out = null;
					in = null;
					theSocket = null;
				}
			}
		}

		/**
		 * Write a string to socket. tries once to connect if not connected. Gives up if doesnt succeed.
		 *
		 * @param s
		 *            string to write to socket
		 * @return true if write succeeded (ie no IOException occurred). false if socket not connected.
		 * @throws IOException
		 */
		public boolean write(String s) throws IOException {
			if (connected == false) {
				logger.debug("IPPserver::ClientSocketHandlerAdapter write - socket not connected");
				openSocket();
				logger.debug("IPPserver::ClientSocketHandlerAdapter write - socket reconnected");
			}

			if (connected == true) {
				logger.debug("IPPserver::ClientSocketHandlerAdapter writing: " + s);

				out.write(s, 0, s.length());
				out.flush();

				logger.debug("IPPserver::ClientSocketHandlerAdapter wrote ok");

				return true;
			}

			return false;
		}

		private String readWithTimeout(int timeout)throws IOException,InterruptedException
		{
			String returnString ="";
			ackTimeout = timeout;
			returnString  = read();
			ackTimeout = 0;
			return returnString;
		}

		/**
		 * Blocking read of a string from a socket. Tries once to connect if not connected. Gives up if doesnt succeed.
		 * Blocks read until ready.
		 *
		 * @return string read from socket with terminators stripped.
		 * @throws IOException
		 * @throws InterruptedException
		 */
		public String read() throws IOException, InterruptedException {
			int timeWaitingForReady = 0;
			if (connected == false) {
				logger.debug("IPPserver::ClientSocketHandlerAdapter read - socket not connected");
				openSocket();
				logger.debug("IPPserver::ClientSocketHandlerAdapter read - socket reconnected");
			}

			if (connected == true) {
				StringBuffer sb = new StringBuffer(256);
				char c;

				do {
					boolean hasBeenWaiting = false;
					while (!in.ready()) {
						if (ackTimeout>0){
							if (timeWaitingForReady>ackTimeout){
								throw new IOException("IPPServer.readWithTimout(), timed out after " + ackTimeout + "s.");
							}
						}
						if (!hasBeenWaiting) {
							hasBeenWaiting = true;
							logger
									.debug("IPPserver::ClientSocketHandlerAdapter read - socket not ready - polling socket in loop...");
						}
						Thread.sleep(waitTime);
						timeWaitingForReady += waitTime;
					}
					if (hasBeenWaiting) {
						logger.debug("IPPserver::ClientSocketHandlerAdapter read - socket now ready");
					}
					c = (char) in.read();
					sb.append(c);
				} while (c != '\n');

				logger.debug("IPPserver::ClientSocketHandlerAdapter read - read from socket: " + sb);
				return (sb.toString()).trim();
			}

			return null;
		}

		/**
		 * Non-blocking read of a string from a socket. Tries once to connect if not connected. Gives up if doesnt
		 * succeed. If not ready returns null.
		 *
		 * @return string read from socket. Returns null if socket not ready.
		 * @throws IOException
		 */
		public String readNB() throws IOException {
			if (connected == false) {
				logger.debug("IPPserver::ClientSocketHandlerAdapter readNB - socket not connected");
				openSocket();
				logger.debug("IPPserver::ClientSocketHandlerAdapter readNB - socket reconnected");
			}

			if (connected == true) {
				if (!in.ready()) {
					logger.debug("readNB: socket not ready to read() from");

					return null;
				}

				logger.debug("IPPserver::ClientSocketHandlerAdapter readNB - reading from socket");

				StringBuffer sb = new StringBuffer(256);
				char c;

				do {
					c = (char) in.read();
					sb.append(c);
				} while (c != '\n');

				logger.debug("IPPserver::ClientSocketHandlerAdapter readNB - read from socket: " + sb);

				return (sb.toString()).trim();
			}

			return null;
		}

	}

	/**
	 * The Constructor.
	 */
	public IPPserver() {
		// README - Empty constructor required by Castor
	}

	@Override
	public void finalize() {
		// FIXME - should there be a better way of GDA destroying objects
		// explictly?
		if (ippServerSocket != null) {
			ippServerSocket.closeSocket();
		}
		unlock();
	}

	@Override
	public void configure() {
		logger.debug("IPPserver.configure");

		ippServerSocket = new ClientSocketHandlerAdapter();
		if (ippServerSocket != null) {
			ippServerSocket.openSocket();

			logger.debug("IPPserver configured ok");
		}
	}

	/**
	 * Finalises the object (closing the port and streams) and then configures the object again (reopening them all).
	 * {@inheritDoc}
	 * @see gda.device.DeviceBase#reconfigure()
	 */
	@Override
	public void reconfigure() {
		logger.info("IPPServer.reconfigure() is closing connection.");
		this.finalize();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			logger.error("IPPserver.reconfigure() caught InteruptedException: ", e);
		}
		logger.info("IPPServer.reconfigure() is calling configure.");
		this.configure();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.error("IPPserver.reconfigure() caught InteruptedException: ", e);
		}
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		Double collectionTime = ScannableUtils.objectToArray(position)[0];
		this.setCollectionTime(collectionTime);
		this.collectData();
	}

	@Override
	public void collectData() throws DeviceException {
		acquireImage(); // this command will have attempted to secure a connection if required
		saveData(fileNameRoot, fileFormat);
	}

	/**
	 * Call ImagePro ipAcqSnap command to acquire an Image, eg using a CCD camera. If collectionTime has been set to
	 * non-zero value, the ipAcqControl command is used to set an exposure time, before the image is aquired. N.B.
	 * ipAcqSnap blocks until image acquisition is complete. If an error occurs, a DeviceException is generated and
	 * thrown.
	 *
	 * @throws DeviceException
	 * @throws DeviceBusyException
	 *             If a request to send a command is made while a request is already being handled.
	 */
	public void acquireImage() throws DeviceException, DeviceBusyException {
		logger.debug("acquireImage acquiring image using ImagePro camera");
		// 1) Set exposure time
		if (collectionTime != 0.0) {
			sendCommandToSetExposureTime(); // This command will attempt to secure a connection if required
		}
		// 2) Expose
		sendCommandToTriggerExposureAndWaitForCompletion(); // This command will attempt to secure a connection if
		// required

	}

	private void sendCommandToSetExposureTime() throws DeviceException {
		String reply;
		int err;
		try {
			reply = sendCommandAttemptingToSecureAConnectionIfRequired("ipAcqControl "
					+ Integer.toString(ACQ_CMD_EXPOSURE_TIME) + " " + Integer.toString(1) + " "
					+ Integer.toString((int) collectionTime));
			err = parseErrCodeFromReply(reply);
			if (err < 0) {
				throw new DeviceException("Error: failed to set exposure time. IPP Reply was: " + reply);
			}
		} catch (InterruptedException e) {
			throw new DeviceException("Error setting exposure time", e);
		}
	}

	private void sendCommandToTriggerExposureAndWaitForCompletion() throws DeviceException {
		String reply;
		int err;
		try {
			reply = sendCommandAttemptingToSecureAConnectionIfRequired("ipAcqSnap " + Integer.toString(mode));
			err = parseErrCodeFromReply(reply);
			if (err < 0) {
				throw new DeviceException("Error: failed to exposure. IPP Reply was: " + reply);
			}
		} catch (InterruptedException e) {
			throw new DeviceException("Error triggering exposure", e);
		}
	}

	/**
	 * Call ImagePro ipWsSaveAs to save current image to specified path/filename, using specified image format. N.B.
	 * only supported ImagePro output formats are permitted. eg BMP, TIF, JPG, TGA, GIF, etc. (See ImagePro manual).
	 *
	 * @param theFileName
	 *            filename. If file exists it will be oeverwritten.
	 * @param theFileFormat
	 *            should be chosen from a list of valid file format given on p2-442 of the Image ProPlus manual.
	 * @throws DeviceException
	 * @throws DeviceBusyException
	 */
	public void saveData(String theFileName, String theFileFormat) throws DeviceException, DeviceBusyException {
		String reply = "";
		int err = -1;

		lastImagePathName = "";

		try {
			logger.debug("saveData ensuring output folder " + outputFolderRoot + " exists");

			// Ensure a folder for todays date exists under the
			// outputFolderRoot folder. If not, this command creates the folder.
			reply = sendCommandAndGetReply("createFolder " + outputFolderRoot);

			logger.debug("saveData createFolder returned: " + reply);

			// make sure format string is uppercase
			theFileFormat = theFileFormat.toUpperCase();

			// convert unix forward slashes to windows backslashes
			theFileName = theFileName.replace('/', '\\');
			// theFileName += "." + theFileFormat;

			logger.debug(theFileName);
			logger.debug(fileFormat);

			reply = sendCommandAndGetReply("ipWsSaveAs " + theFileName + " " + theFileFormat);

			err = parseErrCodeFromReply(reply);

			logger.debug("saveData ipWsSaveAs err: " + Integer.toString(err));

			if (err < 0)
				throw new DeviceException("Error: failed to save image: " + outputFolderRoot +"/DATE/"+  theFileName + "/NUM." + theFileFormat + " IPP Error code: "+ err);

			// fetch full pathname for last aquired image
			lastImagePathName = sendCommandAndGetReply("getLastImagePath");

			// strip off reply header to get actual returned pathname
			lastImagePathName = lastImagePathName.substring(8).trim();

			logger.debug("saveData getLastImagePath: " + lastImagePathName);
		} catch (InterruptedException e) {
			throw new DeviceException("Error saving data", e);
		}
	}

	private int parseErrCodeFromReply(String reply) throws DeviceException {
		int err;
		try {
			err = Integer.parseInt(reply.substring(8).trim());
		} catch (Exception e) {
			throw new DeviceException(
					"<< Could not find an err number in the ack reply '"
							+ reply
							+ "'. This implies that the communication is out of synch.\nYou MUST stop and RESTART the IPPlugin in ImageProPlus >>\n");
		}

		return err;
	}

	/**
	 * Wraps up sendCommandAndGetReply(). Makes up to two attempst to send the message. If the first fails the object is
	 * reconfigured and a second attempt is made. If this fails instructions are presented to the user on how to
	 * configure IPP to accept connections.
	 *
	 * @param command
	 * @return The returned string from the underlying ImagePro API.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	private String sendCommandAttemptingToSecureAConnectionIfRequired(String command) throws DeviceException,
			InterruptedException {
		String reply;
		try {
			// *First attempt*
			reply = sendCommandAndGetReply(command);
		} catch (DeviceBusyException e) {
			throw new DeviceException("\n<< Did not send command '" + command + "' to ImagePro as the"
					+ "IPPServer is still sending and waiting for a response from the last command. >>", e);
		} catch (DeviceException e) {
			// *Second attempt*
			// The first attempt failed, so disconnect and reconnect to IPP
			// and try once again.
			logger.info("IPPServer: DeviceException caught while talking to IPP. Reconfiguring...");
			this.reconfigure();
			try {
				reply = sendCommandAndGetReply(command);
			} catch (DeviceBusyException e2) {
				throw new DeviceException("\n<< Did not send command '" + command + "' to ImagePro as the"
						+ "IPPServer is still sending and waiting for a response from the last command. >>", e2);
			} catch (DeviceException e2) {
				throw new DeviceException("\n<< Unexpected (or lack of) acknowledgement from ImagePro IPPlugin."
						+ "\n1) Ensure ImagePro has been first configured such that pressing snap takes"
						+ "an image.\n2) Ensure that the GDA's IPPlugin has then been started by selecting"
						+ " IPPlugin from the Image Pro Plus Acquire menu. >>", e2);
			}
		}
		return reply;
	}

	/**
	 * Send an ImagePro command string over socket to GDA ImagePro plugin and then waits first for an acknowledgement
	 * from the plugin and then for a reply from the underlying ImagePro API.
	 *
	 * @param command
	 * @return return value from API call. >= 0 = OK. -4 = function not found. -7 = invalid arguments. See ImagePro
	 *         manual Appendix F p9-2 .
	 * @throws DeviceException
	 *             or for any communication error lower in the stack.
	 * @throws DeviceBusyException
	 *             if method is already busy waiting for ack or reply,
	 * @throws InterruptedException
	 */
	private String sendCommandAndGetReply(String command) throws DeviceException, DeviceBusyException,
			InterruptedException {

		String reply = "";

		// Lock access to this method to avoid simultaneous attempts at communication over the single socket.
		if (!tryToLock()) {
			throw new DeviceBusyException(
					"Request ignored: IPPserver is already sending a command and waiting for a reply");
		}

		logger.debug("IPPserver sendCommand: " + command);
		try {
			boolean result = ippServerSocket.write(command + "\r\n");

			// 1) Send command, raise DeviceException if fails
			logger.debug("IPPserver sendCommand sent: " + Boolean.toString(result));
			if (result == false) { // send failed
				unlock();
				throw new DeviceException("Failed to connect to socket.");
			}

			// 2) Wait for acknowledgement (from plugin), raise device exception if interupted while sleeping
			try {
				String ack = ippServerSocket.readWithTimeout(1000);
				logger.debug("IPPserver sendCommand ack: " + ack);
				if (!ack.equals("MSG_ACK")){
					logger.error("IPPserver.sendCommandAndGetReply() expected 'MSG_ACK', but received: " + ack + " Raising DeviceException...");
					unlock();
					throw new DeviceException("IPPserver.sendCommandAndGetReply() expected 'MSG_ACK', but received: " + ack);

				}
			} catch (InterruptedException e) {
				unlock();
				throw new InterruptedException("\n<< InterruptedException while IPPserver.sendCommand('" + command
						+ "') was waiting for its initial ack.\n"
						+ "You MUST stop and RESTART the IPPlugin in ImageProPlus >>\n");
			}

			// 3) Wait for reply (from API via plugin), raise device exception if interupted while sleeping
			try {
				reply = ippServerSocket.read();
				logger.debug("IPPserver sendCommand reply: " + reply);
			} catch (InterruptedException e) {
				unlock();
				throw new InterruptedException("\n<< InterruptedException while IPPserver.sendCommand('" + command
						+ "') was waiting for its reply (first ack received already).\n"
						+ "You MUST stop and RESTART the IPPlugin in ImageProPlus >>\n");
			}

		} catch (IOException e) {
			unlock();
			throw new DeviceException("Low level communication problem with IPP plugin", e);
		}
		unlock();
		return reply;
	}

	@Override
	public Object readout() throws DeviceException {
		return lastImagePathName;
	}

	/**
	 * @return the output folder root pathname
	 */
	public String getOutputFolderRoot() {
		return outputFolderRoot;
	}

	/**
	 * @param outputFolderRoot
	 *            the output folder root pathname
	 */
	public void setOutputFolderRoot(String outputFolderRoot) {
		this.outputFolderRoot = outputFolderRoot;
	}

	/**
	 * @return Returns the base for the filename.
	 */
	public String getFileNameRoot() {
		return fileNameRoot;
	}

	/**
	 * @param fileNameRoot
	 *            The base of the filename to set.
	 */
	public void setFileNameRoot(String fileNameRoot) {
		logger.debug("IPPserver set FileName root" + fileNameRoot);
		this.fileNameRoot = fileNameRoot;
	}

	/**
	 * @return Returns the file format.
	 */
	public String getFileFormat() {
		return fileFormat;
	}

	/**
	 * @param fileFormat
	 *            The file format to set.
	 */
	public void setFileFormat(String fileFormat) {
		logger.debug("IPPserver set File Format" + fileFormat);
		this.fileFormat = fileFormat;
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	/**
	 * Set the host name of the Windows PC running ImagePro. Used by castor for instantiation.
	 *
	 * @param host
	 *            the IP host name of the controller
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Returns the host name of the Windows PC running ImagePro.
	 *
	 * @return the host name.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Set the socket number for ethernet communications.
	 *
	 * @param port
	 *            the socket number.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Get the socket number for ethernet communications.
	 *
	 * @return the port number.
	 */
	public int getPort() {
		return port;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// Creates its own files.
		return true;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Detector interfaced through ImagePro Plus";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "CCD";
	}

	/**
	 * Trys once to get a lock via the busy flag and hence lock access to the send. Returns true if succesful or false
	 * if another thread has the lock.
	 *
	 * @return true if succesful
	 */
	private synchronized boolean tryToLock() {
		if (busyFlag) {
			return false;
		}
		// else
		busyFlag = true;
		return true;
	}

	/**
	 * Can be called from inside Jython Terminal/Script to run specific ImagePro API. N.B. Only API calls supported by
	 * GDA ImagePro plugin possible.
	 *
	 * @param command
	 * @return command result
	 * @throws DeviceException
	 */
	public String sendCommandJython(String command) throws DeviceException {
		logger.debug("IPPserver sendCommandJython: " + command);

		String result;
		try {
			result = sendCommandAndGetReply(command);
			logger.debug("IPPserver sendCommandJython result: " + result);
		} catch (InterruptedException e) {
			throw new DeviceException("Error sending jython command", e);
		}
		return result;
	}

	/**
	 * Unlocks the controller after receiving
	 */
	public synchronized void unlock() {
		busyFlag = false;
	}

	private class DeviceBusyException extends DeviceException {

		protected DeviceBusyException(String message) {
			super(message);
		}

		protected DeviceBusyException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
