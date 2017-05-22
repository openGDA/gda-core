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

import gda.device.DeviceBase;
import gda.device.MotorStatus;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class interfacing to a motord daemon process (version 1.0), running on a VME system. Interprets reply strings in a
 * separate monitoring thread.
 */
public class MotordController extends DeviceBase implements Configurable, Findable {

	private static final Logger logger = LoggerFactory.getLogger(MotordController.class);

	private MonitorThread monitorThread;

	private int MONITORING_TIMEOUT = 1000;

	private int DISCONNECT_COUNT = 10;

	private String servername = null;

	private int serverport = 8000;

	private boolean persistentConnection = false;

	private int version = 1;

	private final String GET_MOTORS_CMD = "S 0";

	private final String HALT_MOTORS_CMD = "H";

	private final String SET_POSITION_CMD = "X ";

	private final String GET_POSITION_CMD = "P ";

	private final String GET_ITEM_CMD = "I "; // Version2 only

	private final String SET_SPEED_CMD = "C ";

	private final String GET_SPEED_CMD = "G ";

	private final String MOVE_BY_CMD = "M ";

	private final String MOVE_TO_CMD = "B ";

	private final String QUIT_CMD = "Q";

	// private final int SUCCESS = 100;
	private final int STATUS = 200;

	// private final int WARN = 300;
	// private final int ERROR = 400;
	// private final int FATAL = 500;
	private final int MENU = 600;

	// private final int HALTED = 0x1;
	private final int MOVING = 0x2;

	// private final int SECURE = 0x4;
	private final int ONLIMIT = 0x8;

	private final int MENU_POSITION_START = 18;

	private final int MENU_POSITION_END = 27;

	private final int CONTINUOUS_STEPS = 5000000;

	private InetAddress serveraddr;

	private Socket motordSocket = null;

	private BufferedReader motordin;

	private PrintWriter motordout;

	private static MotordController motordController = null;

	private boolean connected = false;

	private boolean motorsKnown = false;

	private boolean monitorMotord = false;

	private String supportedMotors = null;

	private int numberOfMotors = -1;

	// private String name;

	// A hash table of status information is kept for all connected motors,
	// one entry for each motor, referenced by their mnemonic with the
	// motord
	// daemon.
	private Hashtable<String, MotorData> motorDataTable = null;

	/**
	 * No arguments constructor. Used by factory construction.
	 */
	public MotordController() {
		motordController = this;
	}

	@Override
	public void configure() throws FactoryException {
		try {
			init();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			String msg = getName() + " interrupted during initialisation";
			logger.error(msg, e);
			throw new FactoryException(msg, e);
		}
	}

	/**
	 * @param servername
	 */
	public void setMotordHost(String servername) {
		this.servername = servername;
	}

	/**
	 * @return server name
	 */
	public String getMotordHost() {
		return servername;
	}

	/**
	 * @param serverport
	 */
	public void setMotordPort(int serverport) {
		this.serverport = serverport;
	}

	/**
	 * @return port
	 */
	public int getMotordPort() {
		return serverport;
	}

	/**
	 * @param persistentConnection
	 */
	public void setPersistentConnection(boolean persistentConnection) {
		this.persistentConnection = persistentConnection;
	}

	/**
	 * @return boolean
	 */
	public boolean getPersistentConnection() {
		return persistentConnection;
	}

	/**
	 * @param monitoringTimeout
	 */
	public void setMonitoringTimeout(int monitoringTimeout) {
		MONITORING_TIMEOUT = monitoringTimeout;
	}

	/**
	 * @return MONITORING_TIMEOUT
	 */
	public int getMonitoringTimeout() {
		return MONITORING_TIMEOUT;
	}

	/**
	 * @param disconnectCount
	 */
	public void setDisconnectCount(int disconnectCount) {
		DISCONNECT_COUNT = disconnectCount;
	}

	/**
	 * @return DISCONNECT_COUNT
	 */
	public int getDisconnectCount() {
		return DISCONNECT_COUNT;
	}

	/**
	 * Initialize the workings of the object.
	 * @throws InterruptedException
	 */
	public void init() throws InterruptedException {
		if (connect()) {
			monitorMotord = true;
			monitorThread = new MonitorThread();
			monitorThread.start();

			// Request list of supported motors.
			sendMessage(GET_MOTORS_CMD);

			// Ensure response has been received.
			for (int i = 0; i < 10; i++) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
				}

				if (motorsKnown)
					break;
			}
		}
	}

	/**
	 * Attempt to connect to the motord daemon. If something is already connected this will produce an IOException.
	 *
	 * @return true if connected
	 */
	private boolean reconnect() {
		connected = false;

		if (servername != null) {
			try {
				serveraddr = InetAddress.getByName(servername);
				motordSocket = new Socket(serveraddr, serverport);

				motordin = new BufferedReader(new InputStreamReader(motordSocket.getInputStream()));
				motordout = new PrintWriter(motordSocket.getOutputStream());
				connected = true;
			} catch (UnknownHostException uhe) {
				logger.debug("Unknown host " + servername);
			} catch (IOException ex) {
				logger.debug("Problem connecting to motord.");
			}
		}

		return connected;
	}

	/**
	 * Attempt to connect to the motord daemon. If something is already connected this will produce an IOException.
	 *
	 * @return true if connected
	 */
	private boolean connect() {
		connected = false;
		motorsKnown = false;

		if (servername != null) {
			try {
				serveraddr = InetAddress.getByName(servername);
				motordSocket = new Socket(serveraddr, serverport);

				motordin = new BufferedReader(new InputStreamReader(motordSocket.getInputStream()));
				motordout = new PrintWriter(motordSocket.getOutputStream());
				connected = true;
			} catch (UnknownHostException uhe) {
				logger.debug("Unknown host " + servername);
			} catch (IOException ex) {
				logger.debug("Problem connecting to motord.");
			}
		}

		return connected;
	}

	/**
	 * Dis-connect from the motord daemon.
	 * @throws InterruptedException
	 */
	private void disconnect() throws InterruptedException {
		try {
			if (motordSocket != null) {
				sendMessage(QUIT_CMD);
				motordSocket.close();
				motordSocket = null;
				motordin = null;
				motordout = null;
				connected = false;
			}
		} catch (IOException ex) {
			logger.debug("Cannot close motord connection.");
		}
	}

	/**
	 * Send a message to the motord daemon.
	 *
	 * @param msg
	 *            The message to be sent.
	 * @throws InterruptedException
	 */
	private void sendMessage(String msg) throws InterruptedException {
		String sendmsg = new String(msg + '\015');

		if (motordout == null) {
			reconnect();
			do {
				Thread.sleep(100);
			} while (!connected);
		}

		if (motordout != null) {
			motordout.println(sendmsg);
			logger.debug("Sending " + sendmsg);
			motordout.flush();
		}

		// This should ensure that replies are received before more
		// messages are sent.
		try {
			Thread.sleep(50);
		} catch (InterruptedException ex) {
		}
	}

	/**
	 * Receive a message from the motord daemon.
	 *
	 * @return the reply string
	 */
	/*
	 * private String receiveMessage() { try { while (!motordin.ready()) continue; return motordin.readLine(); } catch
	 * (IOException ex) { return null; } }
	 */

	/**
	 * Receive a message from the motord daemon, with a timeout.
	 *
	 * @param time_out
	 *            Socket read time out.
	 * @return the reply string
	 */
	private String receiveMessage(int time_out) {
		try {
			while (time_out > 0) {
				Thread.sleep(10);
				if (motordin != null && motordin.ready())
					return motordin.readLine();
				time_out -= 10;
			}
			return null;
		} catch (IOException ioe) {
			return null;
		} catch (InterruptedException ix) {
			return null;
		}
	}

	/**
	 * Checks if a motor is known by this motor daemon.
	 *
	 * @param mnemonic
	 *            Mnemonic of the required motor.
	 * @return true if motor is supported
	 */
	public boolean isMotorSupported(String mnemonic) {
		return (supportedMotors.indexOf(mnemonic) != -1);
	}

	/**
	 * Gets the drive step rate of a motor.
	 *
	 * @param mnemonic
	 *            Mnemonic of the required motor.
	 * @return the drive step rate (speed) of a motor
	 * @throws InterruptedException
	 */
	public int getSpeed(String mnemonic) throws InterruptedException {
		sendMessage(GET_SPEED_CMD + mnemonic);
		return -1;
	}

	/**
	 * Sets the drive step rate of a motor.
	 *
	 * @param mnemonic
	 *            Mnemonic of the required motor.
	 * @param stepsPerSecond
	 *            Required step drive rate.
	 * @return -1
	 * @throws InterruptedException
	 */
	public int setSpeed(String mnemonic, int stepsPerSecond) throws InterruptedException {
		int speedRegister;
		float divisor = (float) 40.0;

		// Speed goes from 40 to 10240 in steps of 40 steps/second,
		// starting at zero.
		if (stepsPerSecond <= 59)
			speedRegister = 0;
		else if (stepsPerSecond >= 10220)
			speedRegister = 255;
		else
			speedRegister = Math.round(stepsPerSecond / divisor) - 1;

		sendMessage(SET_SPEED_CMD + mnemonic + " " + speedRegister);
		return -1;
	}

	/**
	 * Sets the step position of a motor.
	 *
	 * @param mnemonic
	 *            Mnemonic of the required motor.
	 * @param steps
	 *            Required step position.
	 * @throws InterruptedException
	 */
	public void setPosition(String mnemonic, int steps) throws InterruptedException {
		sendMessage(SET_POSITION_CMD + mnemonic + " Position=" + steps);

		// To ensure we update local copy.
		if (version == 1)
			sendMessage(GET_POSITION_CMD + mnemonic);
		else
			sendMessage(GET_ITEM_CMD + mnemonic);
	}

	/**
	 * Obtains the step position of a motor.
	 *
	 * @param mnemonic
	 *            Mnemonic of the required motor.
	 * @throws InterruptedException
	 */
	public void loadPosition(String mnemonic) throws InterruptedException {
		if (version == 1)
			sendMessage(GET_POSITION_CMD + mnemonic);
		else
			sendMessage(GET_ITEM_CMD + mnemonic);
	}

	/**
	 * Obtains the step position of a motor.
	 *
	 * @param mnemonic
	 *            Mnemonic of the required motor.
	 * @return the step position of a motor
	 */
	public double getPosition(String mnemonic) {
		MotorData md = motorDataTable.get(mnemonic);
		return md.getPosition();
	}

	/**
	 * Obtains the status of a motor.
	 *
	 * @param mnemonic
	 *            Mnemonic of the required motor.
	 * @return the MotorStatus of the motor
	 */
	public MotorStatus getStatus(String mnemonic) {
		MotorData md = null;
		MotorStatus ms = MotorStatus.UNKNOWN;

		if (motorDataTable != null) {
			md = motorDataTable.get(mnemonic);

			if (md != null)
				ms = md.getMotorStatus();
		}

		logger.debug("Motor " + mnemonic + " status = " + ms.value());

		return ms;
	}

	/**
	 * Moves a motor by a requested step increment.
	 *
	 * @param mnemonic
	 *            Mnemonic of the required motor.
	 * @param steps
	 *            Required step increment.
	 * @throws InterruptedException
	 */
	public void moveBy(String mnemonic, int steps) throws InterruptedException {
		sendMessage(MOVE_BY_CMD + mnemonic + " " + steps);

		MotorData md = motorDataTable.get(mnemonic);
		md.setMoving(true);
		md.setMotorStatus(MotorStatus.BUSY);
		motorDataTable.put(mnemonic, md);
	}

	/**
	 * Moves a motor to a requested steps position.
	 *
	 * @param mnemonic
	 *            Mnemonic of the required motor.
	 * @param steps
	 *            Required step position.
	 * @throws InterruptedException
	 */
	public void moveTo(String mnemonic, int steps) throws InterruptedException {
		sendMessage(MOVE_TO_CMD + mnemonic + " " + steps);

		MotorData md = motorDataTable.get(mnemonic);
		md.setMoving(true);
		md.setMotorStatus(MotorStatus.BUSY);
		motorDataTable.put(mnemonic, md);
	}

	/**
	 * Simulates a continuous move by sending a large number of steps to a connected motor.
	 *
	 * @param mnemonic
	 *            Mnemonic of the required motor.
	 * @param direction
	 *            Direction to move in.
	 * @throws InterruptedException
	 */
	public void moveContinuously(String mnemonic, int direction) throws InterruptedException {
		int signMultiplier = 1;
		if (direction < 0)
			signMultiplier = -1;

		sendMessage(MOVE_BY_CMD + mnemonic + " " + signMultiplier * CONTINUOUS_STEPS);

		MotorData md = motorDataTable.get(mnemonic);
		md.setMoving(true);
		md.setMotorStatus(MotorStatus.BUSY);
		motorDataTable.put(mnemonic, md);
	}

	/**
	 * Returns whether or not motor is actually moving.
	 *
	 * @param mnemonic
	 *            Mnemonic of the required motor.
	 * @return true if motor is moving.
	 */
	public boolean isMoving(String mnemonic) {
		boolean isMoving = false;

		MotorData md = motorDataTable.get(mnemonic);
		if (md != null)
			isMoving = md.getMoving();

		return isMoving;
	}

	/**
	 * Halts all motors supported by this motor daemon.
	 * @throws InterruptedException
	 */
	public void halt() throws InterruptedException {
		sendMessage(HALT_MOTORS_CMD);
	}

	/**
	 * Returns status of connection to motord daemon.
	 *
	 * @return true if motor is connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Returns whether or not a list of supported motors has been established.
	 *
	 * @return true if motors are supported
	 */
	public boolean areMotorsSupported() {
		return motorsKnown;
	}

	/**
	 * A class responsible for monitoring motord replies and updating relevent data, for instance, motor status,
	 * position etc.
	 */
	public class MonitorThread extends Thread {
		/**
		 * Constructor
		 */
		public MonitorThread() {
		}

		/**
		 * The monitoring thread get replys and interprets each in turn. {@inheritDoc}
		 *
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				runController();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("Thread interrupted while running {}", getName(), e);
			}
		}
		private void runController() throws InterruptedException {
			String message = new String();
			int idleCount = 0;
			while (monitorMotord) {
				idleCount++;

				if (connected) {
					message = receiveMessage(MONITORING_TIMEOUT);
					if (message != null) {
						idleCount = 0;
						analyseMotordReply(message);
					} else {
						if (monitorMovingMotors() > 0) {
							idleCount = 0;
						}
					}

					if (!persistentConnection && (idleCount > DISCONNECT_COUNT))
						disconnect();
				} else {
					try {
						Thread.sleep(MONITORING_TIMEOUT);
					} catch (InterruptedException e) {
						logger.error("Thread interrupted while running", e);
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}

		/**
		 * Repsonsible for interpreting the motor daemon reply messages.
		 *
		 * @param message
		 *            Motor daemon reply message.
		 * @throws InterruptedException
		 */
		private synchronized void analyseMotordReply(String message) {
			int messageCode = -1;
			int messageCodeRoot = -1;
			int statusCode = -1;
			String mnemonic;

			// To begin with we are unitialised until a connection is made
			// to the motor daemon. Immediately upon connecting we get a
			// reply
			// of the type "100 dlvme95 motor server V1.0 ready".
			// Having connected we send a command to get supported motors.
			// This will respond with a string of motor mnemonics.
			logger.debug("AnalyseReply got message: " + message);
			if (connected && !motorsKnown) {
				if (message.contains("ready")) {
					version = (message.contains("V2.2")) ? 2 : 1;
				} else {
					logger.debug("MOTORD controller connected, supported motors " + message);
					motorsKnown = true;
					supportedMotors = message;
					numberOfMotors = supportedMotors.length();
					createMotorTable();

					// Make sure all motors are halted and request
					// list of supported motors.
					try {
						halt();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						logger.error("Thread interrupted during halt of {}", getName(), e);
						return;
					}
				}
			} else {
				mnemonic = message.substring(4, 5);
				// Extract the message code number from the motord reply string.
				// This is contained in the first three characters of the
				// message,
				try {
					messageCode = (new Integer(message.substring(0, 3))).intValue();
				} catch (NumberFormatException ex) {
					logger.debug("A position reply returned an invalid code.");
				}
				messageCodeRoot = (messageCode / 100) * 100;

				if (messageCodeRoot == MENU) {
					logger.debug(message);

					// Extract the motor status code from the message code.
					statusCode = messageCode - messageCodeRoot;
					// Extract the position portion of the message.
					String sp = message.substring(MENU_POSITION_START, MENU_POSITION_END).trim();
					try {
						int ip = new Double(sp).intValue();
						MotorData md = motorDataTable.get(mnemonic);
						md.setPosition(ip);
						motorDataTable.put(mnemonic, md);
					} catch (NumberFormatException ex) {
						logger.debug("A position reply returned an invalid " + "value.");
					}
					updateMotorStatus(mnemonic, statusCode);
				} else if (messageCodeRoot == STATUS) {
					// Status messages seem to come at the end of moves.
					// They contain information upon motor status at that
					// time.
					updateMotorStatus(mnemonic, message);

					// Get position one last time.
					//
					// Sleeps a while to ensure the last message has been
					// dealt
					// with properly i.e. updates of Observers. This seemed
					// to
					// cause problems when got limits otherwise (e.g. oemove
					// would
					// not report them), I think due to being superceeded by
					// status
					// from the final update before updating complete.
					//
					try {
						sleep(1000);
						if (version == 1)
							motordController.sendMessage(GET_POSITION_CMD + mnemonic);
						else
							motordController.sendMessage(GET_ITEM_CMD + mnemonic);
					} catch (InterruptedException iex) {
						logger.error("!!! InterruptedException GET_POSITION/ITEM not sent");
					}
				} else {
					logger.debug("!!! :" + message);
				}
			}
		}

		/**
		 * Initialises an internal table of those motors supported by the VME motor daemon.
		 */
		private void createMotorTable() {
			motorDataTable = new Hashtable<String, MotorData>(numberOfMotors);
			String mnemonic;

			// Initialise the data table.
			logger.debug("Initialising MotordController table");
			for (int i = 0; i < numberOfMotors; i++) {
				mnemonic = supportedMotors.substring(i, i + 1);
				motorDataTable.put(mnemonic, new MotorData());
			}
			logger.debug("MotordController table initialised");
		}

		/**
		 * Updates positions of moving motors.
		 *
		 * @return the number of moving motors
		 * @throws InterruptedException
		 */
		private int monitorMovingMotors() throws InterruptedException {
			String mnemonic;
			int movingMotorCount = 0;

			// For all moving motors send a get position.
			for (int i = 0; i < numberOfMotors; i++) {
				mnemonic = supportedMotors.substring(i, i + 1);
				MotorData md = motorDataTable.get(mnemonic);
				if (md.getMoving()) {
					movingMotorCount++;
					if (version == 1)
						motordController.sendMessage(GET_POSITION_CMD + mnemonic);
					else
						motordController.sendMessage(GET_ITEM_CMD + mnemonic);
				}
			}

			return movingMotorCount;
		}

		/**
		 * Utilise string contained in STATUS type responses to update the motor's status.
		 *
		 * @param mnemonic
		 *            Motor's mnemonic with motord daemon.
		 * @param message
		 *            Motor's mesage associated with STATUS reply.
		 */
		private void updateMotorStatus(String mnemonic, String message) {
			MotorData md = motorDataTable.get(mnemonic);
			md.setMoving(false);

			if (message.indexOf("Home limit") != -1)
				md.setMotorStatus(MotorStatus.LOWERLIMIT);
			else if (message.indexOf("FS limit") != -1)
				md.setMotorStatus(MotorStatus.UPPERLIMIT);
			else if (message.indexOf("External fault") != -1)
				md.setMotorStatus(MotorStatus.FAULT);
			else if (message.indexOf("Spurious interrupt") != -1)
				// Spurious Interrupt seems to be produced before a limit, if at
				// all. However we have no information on the limit. Since the
				// motor should have stopped lets just set it ready. If the user
				// continues to drive in the same direction hopefully we should
				// then get a limit message.
				// md.setMotorStatus(MotorStatus.UNKNOWN);
				md.setMotorStatus(MotorStatus.READY);
			else if (message.indexOf("Unknown status") != -1)
				md.setMotorStatus(MotorStatus.UNKNOWN);
			else if (message.indexOf("Panic stop") != -1)
				md.setMotorStatus(MotorStatus.READY);

			motorDataTable.put(mnemonic, md);
		}

		/**
		 * Utilise code contained in MENU type responses to update the motor's status.
		 *
		 * @param mnemonic
		 *            Motor's mnemonic with motord daemon.
		 * @param statusCode
		 *            Motor's status code associated with MENU reply.
		 */
		private void updateMotorStatus(String mnemonic, int statusCode) {
			MotorData md = motorDataTable.get(mnemonic);

			if ((statusCode & ONLIMIT) == 1) {
				// Version 1.0 motord doesn't seem to define which limit
				// in these messages. Leave motor status unchanged and
				// rely upon the status message produced at the end of
				// move to update this correctly.
				// md.setMoving(false);
				// md.setMotorStatus(MotorStatus.UNKNOWN);
				// motorDataTable.put(mnemonic, md);
			} else if (version == 1 && (statusCode & MOVING) == 0) {
				md.setMoving(false);
				md.setMotorStatus(MotorStatus.READY);
				motorDataTable.put(mnemonic, md);
			} else if (version == 2 && (statusCode & 0x10) == 0) {
				md.setMoving(false);
				md.setMotorStatus(MotorStatus.READY);
				motorDataTable.put(mnemonic, md);
			}
		}
	}

	/**
	 * A class for holding data on a per controlled motor basis. Instances will be updated by the monitoring thread.
	 * SRS122Motor methods requiring feedback (e.g. getPosition) will go into these directly rather than sending a
	 * message then waiting for a reply.
	 */
	public class MotorData {
		private boolean moving = false;

		private int position = -1;

		private int speed = -1;

		private MotorStatus status = MotorStatus.READY;

		/**
		 * @param moving
		 */
		public void setMoving(boolean moving) {
			this.moving = moving;
		}

		/**
		 * @return boolean true if moving
		 */
		public boolean getMoving() {
			return moving;
		}

		/**
		 * @param position
		 */
		public void setPosition(int position) {
			this.position = position;
		}

		/**
		 * @return position
		 */
		public int getPosition() {
			return position;
		}

		/**
		 * @param stepsPerSecond
		 */
		public void setSpeed(int stepsPerSecond) {
			this.speed = stepsPerSecond;
		}

		/**
		 * @return speed
		 */
		public int getSpeed() {
			return speed;
		}

		/**
		 * @param status
		 */
		public void setMotorStatus(MotorStatus status) {
			this.status = status;
		}

		/**
		 * @return status
		 */
		public MotorStatus getMotorStatus() {
			return status;
		}
	}
}
