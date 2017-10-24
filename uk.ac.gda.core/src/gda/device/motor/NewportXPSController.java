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

package gda.device.motor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.factory.Findable;
import gda.jython.JythonServerFacade;
import gda.util.BusyFlag;
import gda.util.SocketBundle;

/**
 * Used by NewportXPSMotor as the point of communication with the actual hardware. It is important to note that this
 * driver only works reliably with Newport XPS Firmware version 1.6.1
 */
public class NewportXPSController extends DeviceBase implements Findable {

	private static final Logger logger = LoggerFactory.getLogger(NewportXPSController.class);

	private String xpsresult;

	private String host;

	private int port;

	private String errnum;

	private String retValue[];

	private int readtimeout = 2; /* Default timeout of 2 seconds */

	private static HashMap<Integer, String> errorString = new HashMap<Integer, String>();

	private String writeTerminator = "\r";

	private BusyFlag busyFlag = new BusyFlag();
	static {
		errorString.put(1, "Controller failed to create socket connection");
		errorString.put(0, "SUCCESS Successful command");
		errorString.put(-1, "ERR_BUSY_SOCKET Busy socket : previous command not yet finished");
		errorString.put(-2, "ERR_TCP_TIMEOUT TCP timeout");
		errorString.put(-3, "ERR_STRING_TOO_LONG String command too long");
		errorString.put(-4, "ERR_UNKNOWN_COMMAND Unknown command");
		errorString.put(-5, "ERR_HARDWARE_STATUS Hardware status error");
		errorString.put(-7, "ERR_WRONG_FORMAT Wrong format in the command string");
		errorString.put(-8, "ERR_WRONG_OBJECT_TYPE Wrong object type for this command");
		errorString.put(-9, " ERR_WRONG_PARAMETERS_NUMBER Wrong number of parameters in the command");
		errorString.put(-10, "ERR_WRONG_TYPE Wrong parameter type in the command string");
		errorString.put(-11, " ERR_WRONG_TYPE_BIT_WORD Wrong parameters type in the command string :"
				+ " word or word * expected");
		errorString.put(-12, "ERR_WRONG_TYPE_BOOL Wrong parameter type in the command string :"
				+ " bool or bool *  expected");
		errorString.put(-13, "ERR_WRONG_TYPE_CHAR Wrong parameter type in the command string : " + "char * expected");

		errorString.put(-14, "ERR_WRONG_TYPE_DOUBLE Wrong parameter type in the command string :"
				+ " double or  double * expected");
		errorString.put(-15, "ERR_WRONG_TYPE_INT Wrong parameter type in the command string :"
				+ " int, short, int *  or short * expected");
		errorString.put(-16, "ERR_WRONG_TYPE_UNSIGNEDINT Wrong parameter type in the command string :"
				+ " unsigned int,unsigned short, unsigned int * or unsigned short * expected");
		errorString.put(-17, "ERR_PARAMETER_OUT_OF_RANGE Parameter out of range");
		errorString.put(-18, "ERR_POSITIONER_NAME Positioner Name doesn't exist");
		errorString.put(-19, "ERR_GROUP_NAME GroupName doesn't exist or unknown command");
		errorString.put(-20, "ERR_FATAL_INIT Fatal Error during initialization, read the error.log"
				+ " file for more details");
		errorString.put(-21, " ERR_IN_INITIALIZATION Controller in initialization");
		errorString.put(-22, " ERR_NOT_ALLOWED_ACTION Not allowed action");
		errorString.put(-23, "ERR_POSITION_COMPARE_NOT_SET Position compare not set");
		errorString.put(-24, " ERR_POSITION_COMPARE_NO_ENCODER Position compare not available "
				+ "without a position encoder");
		errorString.put(-25, " ERR_FOLLOWING_ERROR Following Error");
		errorString.put(-26, " ERR_EMERGENCY_SIGNAL Emergency signal");
		errorString.put(-27, " ERR_GROUP_ABORT_MOTION Move Aborted");
		errorString.put(-28, " ERR_GROUP_HOME_SEARCH_TIMEOUT Home search timeout");
		errorString.put(-29, " ERR_MNEMOTYPEGATHERING Mnemonique gathering type doesn't exist");
		errorString.put(-30, " ERR_GATHERING_NOT_STARTED Gathering not started");
		errorString.put(-31, " ERR_HOME_OUT_RANGE Home position is out of user travel limits");
		errorString.put(-32, " ERR_GATHERING_NOT_CONFIGURED Gathering not configurated");
		errorString.put(-33, " ERR_GROUP_MOTION_DONE_TIMEOUT Motion done timeout");
		errorString.put(-35, " ERR_TRAVEL_LIMITS Not allowed: home preset outside travel limits");
		errorString.put(-36, " ERR_UNKNOWN_TCL_FILE Unknown TCL file");
		errorString.put(-37, "ERR_TCL_INTERPRETOR TCL interpretor doesn't run");
		errorString.put(-38, " ERR_TCL_SCRIPT_KILL TCL script can't be killed");
		errorString.put(-39, " ERR_MNEMO_ACTION Mnemonique action doesn't exist");
		errorString.put(-40, " ERR_MNEMO_EVENT Mnemonique event doesn't exist");
		errorString.put(-41, " ERR_SLAVE_CONFIGURATION Slave-Master mode not configurated");
		errorString.put(-42, " ERR_JOG_OUT_OF_RANGE Jog value out of range");
		errorString.put(-43, " ERR_GATHERING_RUNNING Gathering running");
		errorString.put(-44, " ERR_SLAVE Slave error disabling master");

		errorString.put(-45, "ERR_END_OF_RUN End of run activated");
		errorString.put(-46, " ERR_NOT_ALLOWED_BACKLASH Not allowed action due to backlash");
		errorString.put(-47, "ERR_WRONG_TCL_TASKNAME Wrong TCL task name : each TCL task name must be different");
		errorString.put(-48, " ERR_BASE_VELOCITY BaseVelocity must be null");
		errorString.put(-49, " ERR_GROUP_HOME_SEARCH_ZM_ERROR Inconsistent " + "mechanical zero during home search ");
		errorString.put(-50, " ERR_MOTION_INITIALIZATION_ERROR Motion Initialization"
				+ " error: check initialization acceleration");
		errorString.put(-51, " ERR_SPIN_OUT_OF_RANGE Spin value out of range");
		errorString.put(-60, "ERR_WRITE_FILE Error during file writing or file doesn't exist");
		errorString.put(-61, " ERR_READ_FILE Error during file reading or file doesn't exist");
		errorString.put(-62, " ERR_TRAJ_ELEM_TYPE Wrong trajectory element type");
		errorString.put(-63, "ERR_TRAJ_ELEM_RADIUS Wrong XY trajectory element arc radius");
		errorString.put(-64, "ERR_TRAJ_ELEM_SWEEP Wrong XY trajectory element sweep angle");
		errorString.put(-65, "ERR_TRAJ_ELEM_LINE Trajectory line element discontinuity error or "
				+ "new element is too small");
		errorString.put(-66, "ERR_TRAJ_EMPTY Trajectory doesn't content any element or not loaded");
		errorString.put(-68, "ERR_TRAJ_VEL_LIMIT Velocity on trajectory is too big");
		errorString.put(-69, "ERR_TRAJ_ACC_LIMIT Acceleration on trajectory is too big");
		errorString.put(-70, "ERR_TRAJ_FINAL_VELOCITY Final velocity on trajectory is not zero");
		errorString.put(-71, " ERR_READ_MSG_QUEUE Error read message queue");
		errorString.put(-72, "ERR_WRITE_MSG_QUEUE Error write message queue");
		errorString.put(-73, "ERR_END_OF_FILE End of file");
		errorString.put(-74, " ERR_READ_FILE_PARAMETER_KEY Error file parameter key not found");
		errorString.put(-75, "ERR_TRAJ_TIME Time delta of trajectory element is negative or null");
		errorString.put(-99, " ERR_FATAL_EXTERNAL_MODULE_LOAD Fatal external module load : see error.log");
	}

	@Override
	public void configure(){
		// no configuration required
	}

	/**
	 * Asynchronous write
	 *
	 * @param message
	 */
	public synchronized void xpswritenowait(String message) {
		message = message + writeTerminator;
		lock();
		logger.debug("XPS COMMAND SENT:" + message);
		// Start read thread that now starts actively waiting for characters
		// back
		// from the XPS
		SocketBundle s = new SocketBundle();
		try {
			s.openSocket(host, port);
			Thread xpsread = new PipeR(s);
			xpsread.start();

			try {
				BufferedWriter writer = s.getWriter();
				writer.write(message, 0, message.length());
				// Flush the buffer just in case
				writer.flush();
				logger.debug("CONTROLLER WRITE NO WAIT OK" + s.getId());
			} catch (IOException ex) {
				logger.error("Could not write {} to socket", message, ex);
				s.closeSocket();
			}
		} catch (IOException ex) {
			// failed to open socket
			errnum = "1";
			logger.error("Failed to create socket in xpswrite", ex);
		} finally {
			unlock();
		}
	}

	/**
	 * Asynchronous write
	 *
	 * @param message
	 */
	public synchronized void xpswritenowaitWithHalt(String message) {
		message = message + writeTerminator;
		lock();
		logger.debug("XPS COMMAND SENT:" + message);
		// Start read thread that now starts actively waiting for characters
		// back
		// from the XPS
		SocketBundle s = new SocketBundle();
		try {
			s.openSocket(host, port);
			Thread xpsread = new PipeRwithHalt(s);
			xpsread.start();

			try {
				BufferedWriter writer = s.getWriter();
				writer.write(message, 0, message.length());
				// Flush the buffer just in case
				writer.flush();
				logger.debug("CONTROLLER WRITE NO WAIT OK" + s.getId());
			} catch (IOException ex) {
				logger.error("Error writing '{}'", message, ex);
				s.closeSocket();
			}
		} catch (IOException ex) {
			// failed to open socket
			errnum = "1";
			logger.error("Failed to create socket in xpswrite", ex);
		} finally {
			unlock();
		}
	}

	/**
	 * Set up synchronous read/write
	 *
	 * @param message
	 */
	public synchronized void xpswrite(String message) {
		message = message + writeTerminator;
		lock();
		logger.debug("XPS COMMAND SENT:" + message);

		// Start read thread that now starts actively waiting for characters
		// back
		// from the XPS
		SocketBundle s = new SocketBundle();
		try {
			s.openSocket(host, port);
			Thread xpsread = new PipeR(s);
			xpsread.start();

			try {
				BufferedWriter writer = s.getWriter();
				writer.write(message, 0, message.length());
				// Flush the buffer just in case
				writer.flush();
				logger.debug("CONTROLLER WRITE OK" + s.getId());
			} catch (IOException ex) {
				logger.error("Could not write {} to socket", message, ex);
				s.closeSocket();
			}

			// Now wait for stuff back from the XPS
			try {
				xpsread.join();
			} catch (InterruptedException ex) {
			}
		} catch (IOException ex) {
			// failed to open socket
			errnum = "1";
			logger.error("Failed to create socket in xpswrite", ex);
		} finally {
			unlock();
		}
	}

	class PipeR extends Thread {
		private SocketBundle socketBundle;

		/**
		 * @param socketBundle
		 */
		public PipeR(SocketBundle socketBundle) {
			this.socketBundle = socketBundle;
		}

		@Override
		public void run() {
			char cc;
			StringBuffer sb = new StringBuffer();
			Pattern patt = Pattern.compile(".*EndOfAPI.*", Pattern.DOTALL);
			Matcher m = null;

			// Somewhat painfully you have to wait for the string EndOfAPI
			// !?
			try {
				int l = 0;
				BufferedReader reader = socketBundle.getReader();
				do {
					if (reader != null) {
						l = reader.read();
						cc = (char) l;
						sb.append(cc);
						m = patt.matcher(sb.toString());
						logger.debug("Newport xps the readback value is {}", sb);
					}
				} while ((m == null) || (!m.matches() && l != -1));
			} catch (IOException e) {
				logger.error("Error while running PipeR", e);
				errnum = "-999";
				retValue = new String[] { e.toString() };
				return;
			}

			xpsresult = sb.toString();

			if (xpsresult.compareTo("") != 0) {
				xpsresult = xpsresult.substring(0, xpsresult.indexOf("EndOfAPI"));
				StringTokenizer st = new StringTokenizer(xpsresult, ",");

				// Return the components of the returned string having parsed
				// them
				// on
				// ","
				errnum = st.nextToken();
				retValue = new String[st.countTokens()];
				int i = 0;
				while (st.hasMoreTokens()) {
					retValue[i] = st.nextToken();
					i++;
				}
			}
			logger.debug("End of thread run  for socket bundle id " + socketBundle.getId());

			this.socketBundle.closeSocket();
			logger.debug("*** End PipR.run() ***");
			logger.debug("xpsresult: " + xpsresult);
			logger.debug("errnum: " + errnum);
			logger.debug("retValue: " + retValue);

			logger.debug("End of thrread run  for socket bundle id " + socketBundle.getId());
		}
	}

	class PipeRwithHalt extends Thread {
		private SocketBundle socketBundle;

		/**
		 * Constructor
		 *
		 * @param socketBundle
		 */
		public PipeRwithHalt(SocketBundle socketBundle) {
			this.socketBundle = socketBundle;
		}

		@Override
		public void run() {
			char cc;
			StringBuffer sb = new StringBuffer();
			Pattern patt = Pattern.compile(".*EndOfAPI.*", Pattern.DOTALL);
			Matcher m = null;

			// Somewhat painfully you have to wait for the string EndOfAPI
			// !?
			try {
				int l = 0;
				BufferedReader reader = socketBundle.getReader();
				do {
					if (reader != null) {
						l = reader.read();
						cc = (char) l;
						sb.append(cc);
						m = patt.matcher(sb.toString());
						logger.debug("Newport xps the readback value is {}", sb);
					}
				} while ((m == null) || (!m.matches() && l != -1));
			} catch (IOException e) {
				logger.error("Error running PipeRwithHalt", e);
				errnum = "-999";
				retValue = new String[] { e.toString() };
				return;
			}

			xpsresult = sb.toString();

			if (xpsresult.compareTo("") != 0) {
				xpsresult = xpsresult.substring(0, xpsresult.indexOf("EndOfAPI"));
				StringTokenizer st = new StringTokenizer(xpsresult, ",");

				// Return the components of the returned string having parsed
				// them
				// on
				// ","
				errnum = st.nextToken();
				retValue = new String[st.countTokens()];
				int i = 0;
				while (st.hasMoreTokens()) {
					retValue[i] = st.nextToken();
					i++;
				}
			}
			logger.debug("End of thrread run  for socket bundle id " + socketBundle.getId());

			this.socketBundle.closeSocket();
			logger.debug("*** End PipR.run() ***");
			logger.debug("xpsresult: " + xpsresult);
			logger.debug("errnum: " + errnum);
			logger.debug("retValue: " + retValue);

			logger.debug("End of thrread run  for socket bundle id " + socketBundle.getId());

			// Throw an exception if error number is no zero
			if (!errnum.equals("0")) {
				// FUDGE:
				//
				// A bit of a hack as you can't throw exception outside a
				// thread.
				// This will trigger an exception in the
				// running jython script at least. There may instances where
				// this is
				// not good enougth: during a scan for example
				JythonServerFacade.getInstance().print(
						"XPS Motor failure. Error code (" + errnum + "): " + errorString.get(Integer.valueOf(errnum))
								+ " ");
				JythonServerFacade.getInstance().beamlineHalt();
				// JythonServerFacade.getInstance().haltCurrentScan();
				// JythonServerFacade.getInstance().haltCurrentScript();
				// JythonServerFacade.getInstance().print("Panic Stopping");
				// .getInstance().panicStop();

				// JythonServerFacade.getInstance().runCommand("raise
				// Exception('XPS
				// Motor failure. Error code ("
				// + errnum + "): " + errorString.get(Integer.valueOf(errnum))+"
				// ')");
			}

		}
	}

	/**
	 * @return String xpsresult
	 */
	public String getXpsresult() {
		return xpsresult;
	}

	/**
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @param readtimeout
	 */
	public void setReadtimeout(int readtimeout) {
		this.readtimeout = readtimeout;
	}

	/**
	 * @return host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return error number
	 */
	public String getErrnum() {
		return errnum;
	}

	/**
	 * @return ret value
	 */
	public String[] getRetValue() {
		return retValue;
	}

	/**
	 * @return read timeout
	 */
	public int getReadtimeout() {
		return readtimeout;
	}

	/**
	 * @param error
	 * @return error message
	 */
	public String getErrorMessage(int error) {
		String errorStr = errorString.get(error);
		return errorStr;
		// xpswrite("PositionerErrorStringGet(" + error+", char *)");
	}

	/**
	 * Uses the BusyFlag to lock this object for use by one thread only. Other threads which try to lock will wait() on
	 * the BusyFlag object
	 */
	private void lock() {
		busyFlag.getBusyFlag();
	}

	/**
	 * Unlocks the controller after receiving reply..
	 */
	private void unlock() {
		busyFlag.freeBusyFlag();
	}
}
