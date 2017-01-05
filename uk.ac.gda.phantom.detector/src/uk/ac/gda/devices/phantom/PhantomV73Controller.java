/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.phantom;

import gda.device.DeviceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class actually talks to the real phantom camera
 */
public class PhantomV73Controller implements IPhantomV73Controller {

	/**
	 * Setup the logging facilities
	 */
	private static final Logger logger = LoggerFactory.getLogger(PhantomV73Controller.class);

	Socket soc = null;
	InputStream in;
	OutputStream out;
	String IP = "";

	// thread handling
	final Boolean[] finished = new Boolean[1];
	CircularBuffer circularBuffer;
	StreamReader streamReader;

	ServerSocket recServ;

	/**
	 * @param inputIP
	 *            the IP address or name of the camera, generaly written on the side of the hardware
	 */
	public PhantomV73Controller(String inputIP) {
		IP = inputIP;
		finished[0] = false;
	}

	@Override
	public void connectToCamera() throws UnknownHostException, IOException {
		soc = new Socket(IP, 7115);
		in = soc.getInputStream();
		out = soc.getOutputStream();
	}

	@Override
	public String command(String commandString) throws DeviceException {

		// check to see if the camera is connected.
		if (out == null) {
			try {
				connectToCamera();
			} catch (UnknownHostException e) {
				throw new DeviceException("Could not find Phantom Camera", e);
			} catch (IOException e) {
				throw new DeviceException("Failed to connect to the Phantom Camera", e);
			}
		}

		// make a second check to make sure things are all ok
		if (out != null) {

			// set up the streams to get information in and out of the camera on its main port
			PrintStream pout = new PrintStream(out);
			InputStreamReader iread = new InputStreamReader(in);
			BufferedReader pin = new BufferedReader(iread);

			// send the commandString to the camera
			pout.println(commandString);

			// now get the result
			String result = "";

			try {
				// count and this while loop are to timeout the command
				// TODO improve this timeout method, to real time inputed in the properties, or by the user
				int count = 0;
				while (count < 100) {

					// if the input stream has some data
					if (pin.ready()) {
						// read everything into the string, there may well be multiple lines
						while (pin.ready()) {
							result = result + pin.readLine() + "\n";
						}
						// return all the streamed data
						return result;
					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						throw new DeviceException("Failure in the Phantom timout Thread.sleep command", e);
					}
				}

				// if we get to the end here, then there has been a timeout
				throw new DeviceException("Camera failed to respond to a command, connection timed out");
			} catch (IOException e) {
				// if we get to the end here, then there has been a timeout
				throw new DeviceException("Camera failed to respond to a command due to an IO error", e);
			}

		}
		// i cant see how this would get to here, but if it does, then return a clear warning
		return "command not correctly called";
	}

	@Override
	public boolean isConneted() {
		try {
			command("clean");
			return true;
		} catch (DeviceException e) {
			return false;
		}
	}

	@Override
	public double[] getDataBlock(int sizeOfArray){
		// need to check at this point, and wait if the data isnt available

		//TODO put some kind of timeout in here, specified in the XML
		while(true) {

			try {
				return circularBuffer.readDouble(sizeOfArray);
			} catch (OutOfMemoryError e) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {
					logger.error("Thread sleep failure");
				}
				logger.debug("data not yet available for read");
			}
		}
			
	}

	@Override
	public void prepareForDataTransfer(int portNumber, boolean sixteenBit) throws DeviceException {

		// set up the socket which will be recieved on
		try {
			recServ = new ServerSocket(portNumber);
		} catch (IOException e1) {
			throw new DeviceException("Data Capture socket number " + portNumber + " failed to initialize", e1);
		}

		finished[0] = false;

		// first we need to get a thread running, which can pick up data as is required
		// set up the thread to read the data once it comes off

		// prepare the buffer to be used by the system
		// TODO this should be a variable that is set in the XML
		circularBuffer = new CircularBuffer(4800000);

		// now set the thread going which will get all the data
		streamReader = new StreamReader(circularBuffer, recServ, sixteenBit);

		// run the thread up , so it waits for the input
		Thread runner = uk.ac.gda.util.ThreadManager.getThread(streamReader, this.getClass().getName());
		runner.start();

		// send the command to open the interface
		String composite = "startdata " + portNumber;
		String errorMessage = command(composite);

		// check to see if the response is positive
		if (!errorMessage.contains("Ok!")) {
			// The process has failed so return an error
			throw new DeviceException("Phantom Camera has failed to complete the command '" + composite
					+ "' returning with the error message :" + errorMessage);
		}

	}

	@Override
	public void finishDataTransfer() {

		streamReader.terminate();
		try {
			recServ.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * This class is to provide a circular buffer to store data read in from the Phantom camera into It includes some
	 * special methods which will be useful for the specifics of what we want to do, such as saving all the data in
	 * shorts, but allowing the readout to be in doubles.
	 */
	private class CircularBuffer {

		short[] buffer;
		int writePoint = 0;
		int readPoint = 0;
		int bufferSize;

		/**
		 * @param size
		 *            The size in shorts of the buffer.
		 */
		public CircularBuffer(int size) {
			// FIXME this needs some checks to make sure we don't run out of heap.
			buffer = new short[size];
			bufferSize = size;
		}

		/**
		 * Increments the write pointer, taking into consideration the circular buffer loops
		 */
		private void incWrite() {
			writePoint++;
			if (writePoint >= bufferSize) {
				writePoint = 0;
			}
		}

		/**
		 * Increments the read pointer, taking into consideration the circular buffer loops
		 */
		private void incRead() {
			readPoint++;
			if (readPoint >= bufferSize) {
				readPoint = 0;
			}
		}

		/**
		 * Writes the data in the given array into the circular buffer
		 * 
		 * @param vals
		 *            The data to be added to the buffer
		 * @throws OutOfMemoryError
		 *             if there isn't enough space in the buffer to hold the data
		 */
		public void write(short[] vals) throws OutOfMemoryError {
			// work out how much space is available
			int space = 0;

			if (writePoint >= readPoint) {
				space = (bufferSize - writePoint) + readPoint;
			} else {
				space = (readPoint - writePoint);
			}

			// check to make sure there is enough space to write to the buffer
			if (space < vals.length) {
				throw new OutOfMemoryError("There is not enough space in the buffer to hold the data, space = " + space
						+ " : write point = " + writePoint + " : read point = " + readPoint);
			}

			// as there is enough memory available then write in this new data
			for (int i = 0; i < vals.length; i++) {
				buffer[writePoint] = vals[i];
				incWrite();
			}

		}

		/**
		 * Writes the data in the given array into the circular buffer
		 * 
		 * @param val
		 *            The data to be added to the buffer
		 * @throws OutOfMemoryError
		 *             if there isn't enough space in the buffer to hold the data
		 */
		@SuppressWarnings("unused")
		public void write(short val) throws OutOfMemoryError {
			// work out how much space is available
			int space = 0;

			if (writePoint >= readPoint) {
				space = (bufferSize - writePoint) + readPoint;
			} else {
				space = (readPoint - writePoint);
			}

			// check to make sure there is enough space to write to the buffer
			if (space < 1) {
				throw new OutOfMemoryError("There is not enough space in the buffer to hold the data, space = " + space
						+ " : write point = " + writePoint + " : read point = " + readPoint);
			}

			// as there is enough memory available then write in this new data
			buffer[writePoint] = val;
			incWrite();

		}

		/**
		 * Reads data from the circular buffer
		 * 
		 * @param sizeOfRead
		 *            The size of the array to read out of the buffer
		 * @return The read data, in a short array
		 * @throws OutOfMemoryError
		 *             if there is not enough data in the buffer to preform the operation
		 */
		@SuppressWarnings("unused")
		public short[] read(int sizeOfRead) throws OutOfMemoryError {
			// work out how much data is available to read
			int data = 0;

			if (writePoint < readPoint) {
				data = (bufferSize - readPoint) + writePoint;
			} else {
				data = (writePoint - readPoint);
			}

			// check to make sure there is enough data available to read
			if (data < sizeOfRead) {
				throw new OutOfMemoryError("There is not enough data in the buffer to read out the requested ammount");
			}

			// create the output array
			short[] array = new short[sizeOfRead];

			// as there is enough data available get it out
			for (int i = 0; i < sizeOfRead; i++) {
				array[i] = buffer[readPoint];
				incRead();
			}

			return array;

		}

		/**
		 * Reads data from the circular buffer
		 * 
		 * @param sizeOfRead
		 *            The size of the array to read out of the buffer
		 * @return The read data, in a double array
		 * @throws OutOfMemoryError
		 *             if there is not enough data in the buffer to preform the operation
		 */
		public double[] readDouble(int sizeOfRead) throws OutOfMemoryError {
			// work out how much data is available to read
			int data = 0;

			if (writePoint < readPoint) {
				data = (bufferSize - readPoint) + writePoint;
			} else {
				data = (writePoint - readPoint);
			}

			// check to make sure there is enough data available to read
			if (data < sizeOfRead) {
				throw new OutOfMemoryError("There is not enough data in the buffer to read out the requested ammount");
			}

			// create the output array
			double[] array = new double[sizeOfRead];

			// as there is enough data available get it out
			for (int i = 0; i < sizeOfRead; i++) {
				array[i] = buffer[readPoint];
				incRead();
			}

			return array;

		}

		/**
		 * gets the ammount of space available on the circular buffer.
		 * @return the size in shorts left in the buffer
		 */
		public int sizeOfAvaiableWriteSpace() {
			// work out how much space is available
			int space = 0;

			if (writePoint >= readPoint) {
				space = (bufferSize - writePoint) + readPoint;
			} else {
				space = (readPoint - writePoint);
			}

			return space;
		}

	}

	private class StreamReader implements Runnable {

		private boolean burstOut = false;
		// Circular buffer that can hold 10 full images
		private CircularBuffer buf = new CircularBuffer(4800000);
		// socket where the data will come from
		ServerSocket conection = null;
		// tag to say weather the camera is reading out 16bit values
		boolean sixteenBit;

		/**
		 * @param writeBuffer
		 *            The circular buffer that this thread will write to.
		 * @param cameraConnectionSocket
		 *            This is the recieving socket for the data, it should be already opened and waiting for data
		 * @param sixteenBitReader
		 *            True if the camera is set to read back 16bit data, this should always be set to aligned LOW, or
		 *            the short wont be big enough to hold the data
		 */
		public StreamReader(CircularBuffer writeBuffer, ServerSocket cameraConnectionSocket, boolean sixteenBitReader) {
			buf = writeBuffer;
			conection = cameraConnectionSocket;
			sixteenBit = sixteenBitReader;
		}

		/**
		 * Stops the thread from running
		 */
		public void terminate() {
			burstOut = true;
		}

		private short[] byteToShort(byte[] data, int numRead, boolean sixteenBit) {

			if (sixteenBit) {

				short[] result = new short[numRead / 2];

				for (int i = 0; i < numRead/2; i++) {
					result[i] = (short) (((0xFF & data[(i * 2)+1]) << 8) + (0xFF & data[(i * 2)]));
				}

				return result;

			}

			short[] result = new short[numRead];

			for (int i = 0; i < numRead; i++) {
				result[i] = (short) (0xFF & data[i]);
			}

			return result;

		}

		/**
		 * This is the main method which will sit and read any data which comes its way {@inheritDoc}
		 */
		@Override
		public void run() {

			// first open the socket to the socket server
			Socket rec;
			try {
				rec = conection.accept();
			} catch (IOException e) {
				logger.error("Unthrowable exception occured in the Streamreader Thread");
				logger.error(e.toString());
				return;
			}

			InputStream streamIn;
			
			try {
				streamIn = rec.getInputStream();
			} catch (IOException e) {

				logger.error("Unthrowable exception occured in the Streamreader Thread");
				logger.error(e.toString());
				return;

			}

			
			int totalRead = 0;
			
			
			while (true) {
				
				// pause if there is no data to read
				boolean paused = true;

				// Amount of data available to read
				int amountAvaiableToRead = 0;
				

				
				while (paused) {
					// check to see if there is any data to write
					try {
						
						
						amountAvaiableToRead = streamIn.available();
						logger.debug("ammount available to read is "+amountAvaiableToRead);
						if (amountAvaiableToRead > 0) {
							// then there is data
							paused = false;
							break;
						}
						
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// last check to see if there are any problems,
					if (burstOut) {
						return;
					}
				}

				// read as much data from the stream as we can, so check how much this is
				int amountToRead = amountAvaiableToRead;


				// check to see if there is enough room in the buffer to take all this data
				int bufSpace = buf.sizeOfAvaiableWriteSpace();
				if (bufSpace < amountToRead) {
					amountToRead = bufSpace;
				}
				
				// if its in 16 bit mode truncate this to a length of multiple 2
				amountToRead = 2 * (amountToRead / 2);
				
				
				if ((amountToRead % 2) == 1) {
					logger.error("Ammount to read out is a non even number " + amountToRead);
					return;
				}

				logger.debug("ammount i want to read "+ amountToRead);
				
				// read in the next piece of data from the stream
				byte[] readValues = new byte[amountToRead];
				int numRead = 0;
				try {
					numRead = streamIn.read(readValues);
					totalRead += numRead;
					logger.debug("number of values actualy read is "+numRead + " total " + totalRead);
				} catch (IOException e2) {
					logger.error("Unthrowable exception occured in the Streamreader Thread");
					logger.error(e2.toString());
					return;
				}
				
				

				// now convert the bytearray to a shortarray
				
				short[] writeValues = byteToShort(readValues, numRead, sixteenBit);
				// testing value, to see for speed.
				//short[] writeValues = new short[numRead*2];
				
				buf.write(writeValues);

			}
		}
	}
}
