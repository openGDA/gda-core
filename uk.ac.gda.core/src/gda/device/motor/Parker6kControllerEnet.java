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

import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.Configurable;
import gda.util.BusyFlag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Ethernet communications for Parker6kController.
 *
 * @see gda.device.motor.Parker6kMotor
 * @see gda.device.motor.Parker6kController
 * @see gda.util.BusyFlag
 */
public class Parker6kControllerEnet extends Parker6kController implements Runnable, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(Parker6kControllerEnet.class);

	private String host;

	private int port = -1;

	private int timeout = 10; // watchdog port timeout secs

	private Socket parkerSocket = null; // input/output raw 6k socket

	private BufferedWriter out = null; // buffered socket output

	private BufferedReader in = null; // buffered socket input

	// thread to read single reply chars from command port
	private Thread socketReadThread;

	// flags daemon reply thread to return i.e. end itself
	private volatile boolean stopThread = false;

	// limit of reply buffer size shared buffer for replies
	private final int maxReplyBuf = 1024;

	private StringBuffer replyBuffer = new StringBuffer(maxReplyBuf);

	private final String replyOKEnd = "\r\n> ";

	private final String replyErrorEnd = "\r\n? ";

	private BusyFlag busyFlag = new BusyFlag();

	private boolean bufferContainsReply = false;

	private boolean connected = false;

	/**
	 * Set the host name of the controller. Used by castor for instantiation.
	 *
	 * @param host
	 *            the IP host name of the controller
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Returns the host name of the controller.
	 *
	 * @return the host name.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Set the socket number for ethernet communications. By default this is 5002 for the Parker Controllers.
	 *
	 * @param port
	 *            the socket number.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Get the socket number for ethernet communications. By default this is 5002 for the Parker Controllers.
	 *
	 * @return the port number.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Used by the run() thread to append characters to the reply buffer. Synchronized because it sets
	 * bufferContainsReply flag.
	 *
	 * @param cchar
	 *            the character to be appended
	 * @exception IOException
	 *                if the buffer overflows (usually this indicates something seriously wrong).
	 */
	private synchronized void appendToReplyBuffer(char cchar) throws IOException {
		// check buffer not full
		if (replyBuffer.length() < maxReplyBuf) {
			replyBuffer.append(cchar);
		} else {
			// clear buffer and signal error
			clearReplyBuffer();
			throw new IOException("run thread : reply buffer full at " + maxReplyBuf + " chars");
		}

		// Check whether there is a complete reply and if there is set the flag.
		String reply = replyBuffer.toString();

		if (reply.indexOf(replyOKEnd) >= 0 || reply.indexOf(replyErrorEnd) >= 0) {
			bufferContainsReply = true;
		}
	}

	/**
	 * Clears the reply buffer. Must be synchronized because it sets the bufferContainsReply flag.
	 */
	private synchronized void clearReplyBuffer() {
		replyBuffer.delete(0, replyBuffer.length());
		bufferContainsReply = false;
		notify();
	}

	/**
	 * Closes the socket and associated input and output streams.
	 */
	private void closeSocket() {
		if (parkerSocket != null) {
			try {
				connected = false;
				out.close();
				out = null;
				in.close();
				in = null;
				parkerSocket.close();
				parkerSocket = null;
			} catch (IOException ioe) {
				logger.error("{} caught IOException while closing socket", getName(), ioe);
			}
		}
	}

	@Override
	public void configure() {
		// README: See Parker6kWatchDog for reasons why we start a watchdog.
		startWatchDog();

		try {
			openSocket();
		} catch (IOException ioe) {
			logger.error("{} caught IOException opening socket", getName(), ioe);
		}
		/*
		 * Even if the openSocket throws an IOException still start up socketReadThread because run() will continue to
		 * try to reconnect (which might eventually work).
		 */
		finally {
			socketReadThread = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName() + " " + getName());
			socketReadThread.setDaemon(true);
			socketReadThread.start();
			sendStartupCommands();
		}
	}

	/**
	 * Replaces all <cr>and <lf>in input string with "{" and "}" for use in displaying Parker replies.
	 *
	 * @param inStr
	 *            the input string
	 * @return the modified string
	 */
	private String crLfString(String inStr) {
		return (inStr.replace('\r', '{').replace('\n', '}'));
	}

	/**
	 * Creates a Parker6kCommand from a string and sends it. FIXME: This exists as a stop-gap to allow controllers to
	 * send startup commands - normally commands are created and sent by a Parker6kMotor.
	 *
	 * @param stringCommand
	 *            the string part of the command
	 */
	private void createCommandAndSend(String stringCommand) {
		String command = getControllerNo() + "_" + stringCommand + "\r";
		logger.debug("createCommandAndSend command: " + command);

		/* Catch the MotorException here because there is nowhere to throw */
		/* it to. FIXME: Possibly should exit if it occurs since it is serious. */
		try {
			sendCommand(command);
		} catch (MotorException me) {
			logger.error("{} caught MotorException in createCommandAndSend", getName(), me);
		}

	}

	@Override
	public void debug() {
		super.debug();
		logger.debug("host : " + getHost());
		logger.debug("portNo : " + getPort());
	}

	/**
	 * Sets the stopThread flag which will stop the thread which talks to the controller (and then close the socket).
	 * Call this to start a disconnect.
	 */
	public void disconnect() {
		/* stopThread is volatile, this should be sufficient since it */
		/* is write-only in this thread and read-only in the run() */
		/* thread. */
		stopThread = true;
	}

	/**
	 * Waits for the reply to a command and returns it as a Parker6kReply. All execptions are converted to
	 * MotorExceptions. Synchronized because it needs safe access to bufferContainsReply and because it calls wait.
	 *
	 * @param command
	 *            the parker command
	 * @return the reply
	 * @throws MotorException
	 */
	private synchronized String getReply(String command) throws MotorException {
		String reply = "";
		long timeOut = 10000; // timeout in milliseconds
		long waitTime = 100;
		long waitedSoFar = 0;

		/* Wait for reply. The waiting will end either if the time is up */
		/* or if the run() method reads enough to set bufferContainsReply */
		/* to true. */
		while (!bufferContainsReply && waitedSoFar < timeOut) {
			waitedSoFar += waitTime;
			try {
				wait(waitTime);
			} catch (InterruptedException ie) {
				throw new MotorException(MotorStatus.UNKNOWN, "Error while waiting for reply to command " + command, ie);
			}
		}

		/*
		 * If we get to this point then EITHER the buffer contains a reply which we can get OR the timeOut time has been
		 * exceeded and we throw an IOException.
		 */
		if (bufferContainsReply) {
			reply = replyBuffer.toString();
			clearReplyBuffer();
			// If the reply is an error throw an exception instead of
			// returning it.
			if (reply.indexOf(replyErrorEnd) >= 0) {
				throw (new MotorException(MotorStatus.UNKNOWN, "Parker controller reply error " + reply));
			}
		} else {
			throw (new MotorException(MotorStatus.UNKNOWN, "Reply timeout after command " + command + " after "
					+ timeOut + " msecs"));
		}

		// show reply with <cr><lf> as "{}"
		logger.debug("Reply :" + crLfString(reply) + ":");

		return reply;
	}

	/**
	 * Uses the BusyFlag to lock this object for use by one thread only. Other threads which try to lock will wait() on
	 * the BusyFlag object
	 */
	private void lock() {
		busyFlag.getBusyFlag();
	}

	/**
	 * Creates the socket and its associated input and output streams
	 *
	 * @throws IOException
	 */
	private void openSocket() throws IOException {
		/*
		 * Creating the Socket or getting the input or output streams from it can cause an IOException which is thrown
		 * on. Creating the Socket can also cause an UnknownHostException which is caught here.
		 */
		try {
			parkerSocket = new Socket(getHost(), getPort());
			out = new BufferedWriter(new OutputStreamWriter(parkerSocket.getOutputStream()));
			in = new BufferedReader(new InputStreamReader(parkerSocket.getInputStream()));
			connected = true;
		} catch (UnknownHostException uhe) {
			/*
			 * FIXME: Possibly this should be terminal because there is no way to recover.
			 */
			logger.error("{} caught UnknownHostException", getName(), uhe);
		}
	}

	/**
	 * Closes open socket and then connects again.
	 */
	private void reconnect() {
		try {
			closeSocket();
			openSocket();
		} catch (IOException ioe) {
			logger.error("{} caught IOException while reconnecting", getName(), ioe);
		}
	}

	/**
	 * The run method of socketReadThread loops continuously (until stopThread flag is set to true) getting single
	 * characters from the real controller and appending them to the reply buffer.
	 */
	@Override
	public void run() {
		// Loop until stopThread is set to true (by disconnect())
		while (!stopThread) {
			try {
				/*
				 * NB in.read() blocks if there is no input so run () must NOT be synchronized.
				 */
				appendToReplyBuffer((char) in.read());

				/*
				 * If there is now a reply then notify and wait until another thread removes it. (The busyFlag mechanism
				 * guarantees that there can be at most one other thread waiting on (this)). NB If it is possible that
				 * there is no thread waiting for replies then there should be a timeout here.
				 */
				synchronized (this) {
					if (bufferContainsReply) {
						notify();
						while (bufferContainsReply)
							wait();
					}
				}
			} catch (NullPointerException | IOException e) {
				/*
				 * Normally we do not catch NullPointerExceptions however in this case it is possible that in (the
				 * BufferedInputStream) is null because of an initial connection failure and we want this method to keep on
				 * trying to reconnect.
				 *
				 * An IOException would indicate that: EITHER there is a real connection problem in in.read() OR the buffer
				 * is full in appendToReplyBuffer() In either case we want to reconnect.
				 */
				logger.error("{}: Error while running controller. Will attempt to reconnect", getName(), e);
				reconnect();
			} catch (InterruptedException ie) {
				/*
				 * An InterruptedException would come from the wait() call. This should not happen and perhaps is so serious
				 * that the program should terminate.
				 */
				logger.error("{} interrupted while running", getName(), ie);
			}
		} // end of while loop

		/*
		 * The while loop will only exit if a disconnect has really been requested so close the socket.
		 */
		closeSocket();
	}

	/**
	 * Sends a List of commands to the 6k controller.
	 *
	 * @param command
	 *            the parker command
	 * @return a reply string
	 * @throws MotorException
	 */
	@Override
	public String sendCommand(String command) throws MotorException {
		String reply = "";

		/*
		 * The BusyFlag lock mechanism allows us to ensure that only one thread at a time can be sending a command and
		 * waiting for a reply. This means that there will be always at most two threads waiting/notifying on this
		 * controller - the one in run() which reads from the actual controller and whichever one has managed to call
		 * lock() in this method Any other threads trying to call lock() will be waiting on the BusyFlag Placing the
		 * lock() here and the unlock() in the finally{} means that the whole List of commands is done as one.
		 */
		lock();

		try {
			int commandLength = command.length();

			if (commandLength > 0) {
				/*
				 * It is important to clear the input buffer for the very first command as there will be an initial '>'.
				 * It is easier just to clear it every time rather than deal with the first time as a special case.
				 */
				clearReplyBuffer();
				if (out != null) {
					out.write(command, 0, commandLength);
					out.flush();
				} else {
					throw (new MotorException(MotorStatus.UNKNOWN, "Controller not connected"));
				}
			}
			reply = getReply(command);
		}

		/*
		 * getReply can throw MotorExceptions which are thrown on out.write can throw IOExceptions which are converted
		 * to MotorExceptions here.
		 */
		catch (IOException ioe) {
			logger.error("{}: Error sending command '{}'", getName(), command, ioe);
			throw new MotorException(MotorStatus.UNKNOWN, "Failed to send command", ioe);
		}
		// Must free the busy flag whether or not a reply is obtained
		finally {
			unlock();
		}
		return reply;
	}

	/**
	 * Sends a set of commands which apply to all motors on the controller and which should be done on starting up.
	 */
	private void sendStartupCommands() {
		// The startup commands really should come from the XML file
		if (connected) {
			createCommandAndSend("@MA1");
			createCommandAndSend("@COMEXL1");
			createCommandAndSend("@DRIVE1");
		}
	}

	/**
	 * Creates and starts a Parker6kWatchDog
	 *
	 * @see Parker6kWatchDog
	 */
	private void startWatchDog() {
		/*
		 * Parker6kWatchDog.setup probably will not return until it succeeds in sending a heartbeat (not guaranteed in
		 * current version).
		 */
		try {
			new Parker6kWatchDog(getHost(), timeout).setup();
		} catch (Exception e) {
			/*
			 * Parker6kWatchDog throws Exception though it is only actually possible for it to throw RunTime Exceptions
			 * (which should not be caught really).
			 */
		}
	}

	/**
	 * Parker6kMotor calls this from its panicStop method to do a definite disconnect.
	 */
	@Override
	public void tidyup() {
		disconnect();
	}

	/**
	 * Unlocks the controller after receiving reply..
	 */
	private void unlock() {
		busyFlag.freeBusyFlag();
	}
}
