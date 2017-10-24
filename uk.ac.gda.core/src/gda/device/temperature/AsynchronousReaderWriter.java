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

package gda.device.temperature;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Serial;
import gda.util.BusyFlag;

/**
 * Class to communicate asynchronously with a Serial device FIXME is this temperature device specific or should there be
 * a general class shared by all serial devices ?
 */
public class AsynchronousReaderWriter implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(AsynchronousReaderWriter.class);

	private String debugName = "AsynchronousReaderWriter";

	private BusyFlag busyFlag;

	private Thread deviceReadThread;

	private boolean bufferContainsReply = false;

	private boolean stopThread = false;

	private StringBuffer replyBuffer;

	private Serial device = null;

	private String replyEndString;

	private String commandEndString;

	private final String REPLYOKEND = "\r";

	private final int REPLYBUFFERLENGTH = 1024;

	private ReplyChecker replyChecker = null;

	/**
	 * Create the reader/writer.
	 *
	 * @param device
	 *            the serial communication device.
	 */
	public AsynchronousReaderWriter(Serial device) {
		logger.debug("AsynchronousReaderWriter constructor called\n");

		this.device = device;

		replyBuffer = new StringBuffer(REPLYBUFFERLENGTH);

		busyFlag = new BusyFlag();

		commandEndString = "\r";
		replyEndString = REPLYOKEND;

		deviceReadThread = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
		deviceReadThread.start();
	}

	/**
	 * Used by the run() thread to append characters to the reply buffer. Synchronized because it sets
	 * bufferContainsReply flag. Throws IOException if the buffer overflows (usually this indicates something seriously
	 * wrong).
	 *
	 * @param cchar
	 *            the character to be appended
	 * @throws IOException
	 */
	private synchronized void appendToReplyBuffer(char cchar) throws IOException {
		// check buffer not full
		if (replyBuffer.length() < REPLYBUFFERLENGTH) {
			replyBuffer.append(cchar);
		} else {
			// clear buffer and signal error
			clearReplyBuffer();
			throw new IOException("run thread : reply buffer full at " + REPLYBUFFERLENGTH + " chars");
		}

		// Check whether there is a complete reply and if there is
		// set the flag.

		if (replyChecker != null) {
			bufferContainsReply = replyChecker.bufferContainsReply(replyBuffer);
		} else if (replyBuffer.toString().indexOf(replyEndString) >= 0) {
			bufferContainsReply = true;
		}
	}

	/**
	 * Set the reply string termination
	 *
	 * @param replyEndString
	 *            temination of reply
	 */
	public void setReplyEndString(String replyEndString) {
		this.replyEndString = replyEndString;
	}

	/**
	 * Set the string termination for a command
	 *
	 * @param commandEndString
	 *            termination of command
	 */
	public void setCommandEndString(String commandEndString) {
		this.commandEndString = commandEndString;
	}

	/**
	 * Set the reply checker
	 *
	 * @param replyChecker
	 */
	public void setReplyChecker(ReplyChecker replyChecker) {
		this.replyChecker = replyChecker;
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
	 * Handles commands which do not need a reply.
	 *
	 * @param command
	 *            the command to send
	 */
	public void handleCommand(String command) {
		try {
			sendCommandAndGetReply(command);
		} catch (DeviceException de) {
			logger.error("Error sending command '{}'", command, de);
		}
	}

	/**
	 * Locks use of this for current thread
	 *
	 * @see BusyFlag
	 */
	public void lock() {
		busyFlag.getBusyFlag();
	}

	/**
	 * Extracts reply from replyBuffer. Must be synchronized because accesses replyBuffer and bufferContainsReply and
	 * also calls wait.
	 *
	 * @return the reply string
	 * @throws DeviceException
	 */
	private synchronized String getReply() throws DeviceException {
		String reply = null;
		long timeOut = 10000;
		long waitTime = 100;
		long waitedSoFar = 0;

		// Wait for reply. The waiting will end either if the time is up
		// or if the run() method reads enough to set bufferContainsReply
		// to true.
		while (!bufferContainsReply && waitedSoFar < timeOut) {
			waitedSoFar += waitTime;
			try {
				wait(waitTime);
			} catch (InterruptedException ie) {
				// FIXME: because Poller uses interrupt() to change pollTime
				// any class also using a Poller might get interrupted
				// here. So ignore InterruptedException
				// throw new DeviceException(debugName + " caught " + ie);
			}
		}

		// If we get to this point then EITHER the buffer contains a
		// reply which we can get OR the timeOut time has been
		// exceeded and we throw a DeviceException.
		if (bufferContainsReply) {
			reply = replyBuffer.toString();
			clearReplyBuffer();
		} else {
			throw new DeviceException("Timed out in " + debugName + ".getReply()");
		}

		logger.debug(debugName + ".getReply() returning " + reply);
		return reply;
	}

	/**
	 * The run method of deviceReadThread loops continuously (until stopThread flag is set to true) getting single
	 * characters from the real controller and appending them to the reply buffer.
	 */
	@Override
	public void run() {
		// Loop until stopThread is set to true (by disconnect())
		while (!stopThread) {
			try {
				// NB device.readChar() blocks if there is no input so
				// run() must NOT be synchronized.
				appendToReplyBuffer(device.readChar());

				// If there is now a reply then notify and wait until
				// another thread removes it. (The busyFlag mechanism
				// guarantees that there can be at most one other thread
				// waiting on (this)).
				// NB If it is possible that there is no thread waiting
				// for replies then there should be a timeout here.
				// FIXME
				synchronized (this) {
					if (bufferContainsReply) {
						notify();
						while (bufferContainsReply)
							wait();
					}
				}
			}
			// An IOException would indicate that:
			// EITHER there is a real connection problem in in.read()
			// OR the buffer is full in appendToReplyBuffer()
			// In either case we want to reconnect.
			catch (IOException ioe) {
				logger.error("run() error. Will attempt to reconnect", ioe);
			}
			// An InterruptedException would come from the wait() call.
			// This should not happen and perhaps is so serious that the
			// program should terminate.
			catch (InterruptedException ie) {
				logger.error("run() interrupted", ie);
			}
			// When the read returns 0 bytes (which it shouldn't)
			// SerialComm throws an DeviceException
			// As even with non blocking reads this code seems to work
			// We only log as DEBUG now and wait a bit to reduce polling
			// See trac #1174
			catch (DeviceException de) {
				logger.debug("Error while running", de);
				try {
					Thread.sleep(75);
				} catch (InterruptedException e) {
					logger.error("Thread interrupted", e);
					Thread.currentThread().interrupt();
					break;
				}
			}
		} // end of while loop
	}

	/**
	 * Actually writes a string to the device.
	 *
	 * @param command
	 *            the command
	 * @throws DeviceException
	 */
	private void sendCommand(String command) throws DeviceException {
		logger.debug(debugName + " writing " + command + " to device");

		for (int i = 0; i < command.length(); i++) {
			device.writeChar(command.charAt(i));
		}

		for (int i = 0; i < commandEndString.length(); i++) {
			device.writeChar(commandEndString.charAt(i));
		}
	}

	/**
	 * Sends a command to the hardware and reads back a data reply.
	 *
	 * @param command
	 *            the command sent to the hardware
	 * @return the reply
	 * @throws DeviceException
	 */
	public String sendCommandAndGetReply(String command) throws DeviceException {
		String reply = null;

		lock();
		sendCommand(command);
		reply = getReply();
		unLock();
		return reply;
	}

	/**
	 * Releases this so that other threads can access it
	 *
	 * @see BusyFlag
	 */
	public void unLock() {
		busyFlag.freeBusyFlag();
	}
}
