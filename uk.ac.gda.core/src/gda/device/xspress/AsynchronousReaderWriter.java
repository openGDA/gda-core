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

package gda.device.xspress;

import gda.device.temperature.ReplyChecker;
import gda.util.BusyFlag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to communicate asynchronously with a Socket
 */

public class AsynchronousReaderWriter implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(AsynchronousReaderWriter.class);

	private String debugName = "AsynchronousReaderWriter";

	private BusyFlag busyFlag;

	private Thread socketReadThread;

	private boolean bufferContainsReply = false;

	private boolean stopThread = false;

	private StringBuffer replyBuffer;

	// private Socket socket = null;
	// buffered socket output
	private BufferedWriter out = null;

	// buffered socket input
	private BufferedReader in = null;

	private String replyEndString = "\r\n";

	private String commandEndString = "\r\n";

	private final int REPLYBUFFERLENGTH = 1024;

	private ReplyChecker replyChecker = null;

	private long timeOut = 10000;

	/**
	 * @param socket
	 */
	public AsynchronousReaderWriter(Socket socket) {
		// this.socket = socket;
		try {
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException ioe) {
			logger.error(debugName + " caught IOException " + ioe.getMessage());
		}

		replyBuffer = new StringBuffer(REPLYBUFFERLENGTH);

		busyFlag = new BusyFlag();

		socketReadThread = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
		socketReadThread.start();
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
		if (replyBuffer.length() < REPLYBUFFERLENGTH) {
			replyBuffer.append(cchar);
		} else {
			clearReplyBuffer();
			throw new IOException("run thread : reply buffer full at " + REPLYBUFFERLENGTH + " chars");
		}

		// Check whether there is a complete reply and if there is
		// set the flag bufferContainsReply.
		if (replyChecker != null) {
			bufferContainsReply = replyChecker.bufferContainsReply(replyBuffer);
		} else if (replyBuffer.toString().indexOf(replyEndString) >= 0) {
			bufferContainsReply = true;
		}
	}

	/**
	 * @param replyEndString
	 */
	public void setReplyEndString(String replyEndString) {
		this.replyEndString = replyEndString;
	}

	/**
	 * @param commandEndString
	 */
	public void setCommandEndString(String commandEndString) {
		this.commandEndString = commandEndString;
	}

	/**
	 * Sets the ReplyChecker. If there is a ReplyChecker then it does the checking in appendToReplyBuffer.
	 * 
	 * @param replyChecker
	 *            the reply checker
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
	 * @return the reply
	 */
	public synchronized String getReply() {
		String reply = "";
		// timeout in milliseconds

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

				// FIXME - throw or not throw ?
				// throw (new MotorException (MotorStatus.UNKNOWN, "Exception
				// \"" +
				// ie.getMessage() + "\" caught while waiting for reply" + " to
				// command "

			}
		}

		// If we get to this point then EITHER the buffer contains a
		// reply which we can get OR the timeOut time has been
		// exceeded and we throw an IOException.
		if (bufferContainsReply) {
			reply = replyBuffer.toString();
			clearReplyBuffer();
			// FIXME
			// If the reply is an error throw an exception instead of
			// returning it.
			// if (reply.indexOf(replyErrorEnd) >= 0)
			// {
			// throw (new MotorException (MotorStatus.UNKNOWN, "Parker
			// controller
			// reply error "
			// }
		} else {
			// FIXME - throw (new MotorException (MotorStatus.UNKNOWN,
			// "Reply
			// timeout after command " + parker6kCommand.toString() +
		}

		// show reply with <cr><lf> as "{}"
		// Message.out ("Reply :"+ crLfString(reply) + ":", Message.Level.TWO);

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
	 * The run method of socketReadThread loops continuously (until stopThread flag is set to true) getting single
	 * characters from the real controller and appending them to the reply buffer.
	 */
	@Override
	public void run() {
		// Loop until stopThread is set to true (by stop))
		while (!stopThread) {
			try {
				// README in.read() blocks if there is no input so run () must
				// NOT be synchronized.
				appendToReplyBuffer((char) in.read());

				// If there is now a reply then notify and wait until
				// another thread removes it. (The busyFlag mechanism
				// guarantees that there can be at most one other thread
				// waiting on (this)).
				// NB If it is possible that there is no thread waiting
				// for replies then there should be a timeout here.
				synchronized (this) {
					if (bufferContainsReply) {
						notify();
						while (bufferContainsReply)
							wait();
					}
				}
			}
			// README - Normally we do not catch NullPointerExceptions
			// however in
			// this case it is possible that in (the BufferedInputStream)
			// is null because of an initial connection failure and we want
			// this method to keep on trying to reconnect.
			catch (NullPointerException npe) {
				logger.error(debugName + " run() caught NullPointerException: " + npe.getMessage());
				// FIXME -either use reconnect or change comment
				// reconnect();
			}
			// An IOException would indicate that:
			// EITHER there is a real connection problem in in.read()
			// OR the buffer is full in appendToReplyBuffer()
			// In either case we want to reconnect.
			catch (IOException ioe) {
				logger.error(debugName + " run() caught IOException " + ioe.getMessage());
				// FIXME -either use reconnect or change comment
				// reconnect();
			}
			// An InterruptedException would come from the wait() call.
			// This should not happen and perhaps is so serious that the
			// program should terminate.
			catch (InterruptedException ie) {
				logger.error(debugName + " run() caught InterrupedException " + ie.getMessage());
			}
		} // end of while loop

		// FIXME -
		// The while loop will only exit if a disconnect has really
		// been requested so close the socket.
		// closeSocket();
		// System.exit(0);
	}

	/**
	 * Sends a command down the socket and waits for a reply
	 * 
	 * @param command
	 *            the command to send
	 * @return reply
	 */
	public String sendCommandAndGetReply(String command) {
		String reply = "";
		String toSend = command + commandEndString;

		lock();

		try {
			// Message.out ("toSend :" + crLfString(toSend) + ":",
			// Message.Level.TWO);

			int toSendLen = toSend.length();

			if (toSendLen > 0) {
				clearReplyBuffer();
				out.write(toSend, 0, toSendLen);
				out.flush();
			}

			reply = getReply();
		} catch (IOException ioe) {
			logger.error(debugName + " sendCommand caught exception " + ioe.getMessage() + " sending command: "
					+ command);

			// FIXME - throw (new MotorException (MotorStatus.UNKNOWN, "
			// Operation
			// failed
			// due to IOException "
		} finally {
			unlock();
		}

		return reply;
	}

	/**
	 * Sends a command down the socket and constructs an ArrayList<String> of replies continuing until one contains
	 * replyTerminator.
	 * 
	 * @param command
	 *            the command to send
	 * @param replyTerminator
	 *            the string to look for in replies
	 * @return the list of replies (including the one containing replyTerminator)
	 */
	public ArrayList<String> sendCommandAndGetReply(String command, String replyTerminator) {
		String reply = "";
		ArrayList<String> replyList = new ArrayList<String>();

		String toSend = command + commandEndString;

		// The BusyFlag lock mechanism allows us to ensure that only
		// one thread at a time can be sending a command and waiting
		// for a reply. This means that there will be always at most
		// two threads waiting/notifying on this controller - the
		// one in run() which reads from the actual controller and
		// whichever one has managed to call lock() in this method.
		// Any other threads trying to call lock() will be waiting
		// on the BusyFlag.

		// Placing the lock() here and the unlock() in the finally{}
		// means that the whole List of commands is done as one.
		lock();

		try {
			int toSendLength = toSend.length();

			if (toSendLength > 0) {
				// It is important to clear the input buffer for the
				// very first toSend as there will be an initial >.
				// It is easier just to clear it every time rather
				// than deal with the first time as a special case.
				clearReplyBuffer();
				out.write(toSend, 0, toSendLength);
				out.flush();
			}

			do {
				reply = getReply();
				logger.debug("adding reply to list: " + reply);
				replyList.add(reply);
			}
			// A timeout will lead to an empty reply which is another reason
			// to
			// terminate.
			while (reply.length() > 0 && reply.indexOf(replyTerminator) < 0);
		}
		// FIXME - comment wrong (throws IOException ?)
		// getReply can throw MotorExceptions which are thrown on
		// out.write can throw IOExceptions which are converted to
		// MotorExceptions here.
		// NB the original version of this attempted a reconnect if
		// there was an IOException - this should not be necessary
		// the run() thread is in charge of reconnection - check this
		catch (IOException ioe) {
			logger.error(debugName + " sendCommand caught exception " + ioe.getMessage() + " sending command: "
					+ command);

			// FIXME - change comment or throw excepion
			// throw (new MotorException (MotorStatus.UNKNOWN, " Operation
			// failed
			// due to IOException "

		}
		// Must free the busy flag (unlock) whether or not a reply is obtained
		finally {
			unlock();
		}

		return replyList;
	}

	/**
	 * Stops the reading thread.
	 */
	public void stop() {
		stopThread = true;
	}

	/**
	 * Frees the BusyFlag, any threads waiting to lock will receive a notify.
	 */
	private void unlock() {
		busyFlag.freeBusyFlag();
	}

	/**
	 * @return timeout
	 */
	public long getTimeOut() {
		return timeOut;
	}

	/**
	 * @param i
	 */
	public void setTimeOut(long i) {
		timeOut = i;
	}

}
