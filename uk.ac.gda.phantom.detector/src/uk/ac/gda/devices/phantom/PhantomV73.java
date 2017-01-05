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

import java.io.IOException;
import java.net.UnknownHostException;

import javax.naming.TimeLimitExceededException;

import org.eclipse.dawnsci.analysis.api.io.IFileSaver;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

import gda.analysis.ScanFileHolder;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.factory.FactoryException;

/**
 * The main interface for the Phantom Camera, this is set on top of a set of hardware simulations and real classes.
 */
public class PhantomV73 extends DetectorBase implements Phantom {

	private static final long serialVersionUID = -3181866508302337047L;

	IPhantomV73Controller hardware = new PhantomV73Simulation();

	String IP = "";

	// All these should be helper functions, which get information on or off the camera

	/**
	 * This method will check to see if a status is active on the camera.
	 *
	 * @param cineNumber
	 *            The number of the cine which will be checked
	 * @param cineState
	 *            The String name of the state we are interested in
	 * @return true if the cine has that status, and false if it dosn't
	 * @throws DeviceException
	 *             if anything goes wrong, this should be explanitary
	 */
	public boolean isCineStatus(int cineNumber, String cineState) throws DeviceException {

		// get the string from the camera first
		String returnData = hardware.command("get c" + cineNumber + ".state");

		// split the string by spaces
		String[] splitString = returnData.split(" ");


		for (int i = 0; i < splitString.length; i++) {
			if (splitString[i].compareToIgnoreCase(cineState) == 0) {
				return true;
			}
		}

		return false;
	}

	/**
	 * This method tries to add the supplied flag to the cine in question.
	 *
	 * @param cineNumber
	 *            The cine which is being modified
	 * @param cineState
	 *            The state flag to apply
	 * @throws DeviceException
	 */
	public void addCineStatus(int cineNumber, String cineState) throws DeviceException {

		// first check to see if the flag is already on the cine, if it is then simply return
		if (isCineStatus(cineNumber, cineState)) {
			return;
		}

		// if it isn't then it needs to be added, this can be done most easily at the beginning of the string
		// first get the data
		String returnData = hardware.command("get c" + cineNumber + ".state");

		String compositeString = "{ " + cineState + " " + returnData.substring(returnData.indexOf("{") + 1);

		// now we have a string which contains the extra value, so this should be set on the cine
		compositeString = "set c" + cineNumber + ".state " + compositeString;
		String errorMessage = hardware.command(compositeString);

		// check to see if the response is positive
		if (errorMessage.compareToIgnoreCase("Ok!") != 0) {
			// The process has failed so return an error
			throw new DeviceException("Phantom Camera has failed to complete the command '" + compositeString
					+ "' returning with the error message :" + errorMessage);
		}

		// otherwise return
		return;

	}

	/**
	 * Method that removes a flag from the specified cines status
	 *
	 * @param cineNumber
	 *            The number of the cine to be modified
	 * @param cineState
	 *            The state flag to be removed
	 * @throws DeviceException
	 *             returns this exception if anything goes wrong with the communications.
	 */
	public void delCineStatus(int cineNumber, String cineState) throws DeviceException {

		// first check to see if the flag is actually on the cine
		if (isCineStatus(cineNumber, cineState)) {

			// find where it is
			// first get the data
			String returnData = hardware.command("get c" + cineNumber + ".state");

			// now create the composite string
			String compositeString = returnData.substring(0, returnData.indexOf(cineState) - 1)
					+ returnData.substring(returnData.lastIndexOf(cineState) + 2, returnData.length());

			// now we have a string which contains the extra value, so this should be set on the cine
			compositeString = "set c" + cineNumber + ".state " + compositeString;
			String errorMessage = hardware.command(compositeString);

			// check to see if the response is positive
			if (errorMessage.compareToIgnoreCase("Ok!") != 0) {
				// The process has failed so return an error
				throw new DeviceException("Phantom Camera has failed to complete the command '" + compositeString
						+ "' returning with the error message :" + errorMessage);
			}

		}

		// just return as the flag isn't set on the status
		return;

	}

	/**
	 * Gets the width of the images to be captured from the Phantom Camera
	 *
	 * @param cineNumber
	 *            The number of the cine in question
	 * @return the width that that cine is set to
	 * @throws DeviceException
	 *             if there any communication problems
	 */
	public int getWidth(int cineNumber) throws DeviceException {

		// get the data from the camera
		String returnData = hardware.command("get c" + cineNumber + ".res");

		// split the data by "x"
		String[] splitData = returnData.split(" ");

		// return the width
		int result = Integer.parseInt(splitData[2].trim());

		return result;

	}

	/**
	 * Gets the width of the images to be captured from the Phantom Camera
	 *
	 * @param cineNumber
	 *            The number of the cine in question
	 * @return the height that that cine is set to
	 * @throws DeviceException
	 *             if there any communication problems
	 */
	public int getHeight(int cineNumber) throws DeviceException {

		// get the data from the camera
		String returnData = hardware.command("get c" + cineNumber + ".res");

		// split the data by " "
		String[] splitData = returnData.split(" ");

		// return the width
		int result = Integer.parseInt(splitData[4].trim());

		return result;
	}

	/**
	 * Gets the width of the images to be captured from the Phantom Camera
	 *
	 * @param cineNumber
	 *            The number of the cine in question
	 * @param width
	 *            The width to set on the cine
	 * @throws DeviceException
	 *             if there any communication problems
	 */
	public void setWidth(int cineNumber, int width) throws DeviceException {

		// get the data from the camera
		String returnData = hardware.command("get c" + cineNumber + ".res");

		// split the data by " "
		String[] splitData = returnData.split(" ");

		// construct the string to send to the camera
		String composite = "set c" + cineNumber + ".res " + width + " x " + splitData[4].trim();

		String errorMessage = hardware.command(composite);

		// check to see if the response is positive
		if (errorMessage.compareToIgnoreCase("Ok!") != 0) {
			// The process has failed so return an error
			throw new DeviceException("Phantom Camera has failed to complete the command '" + composite
					+ "' returning with the error message :" + errorMessage);
		}

	}

	/**
	 * Gets the width of the images to be captured from the Phantom Camera
	 *
	 * @param cineNumber
	 *            The number of the cine in question
	 * @param height
	 *            The width to set on the cine
	 * @throws DeviceException
	 *             if there any communication problems
	 */
	public void setHeight(int cineNumber, int height) throws DeviceException {

		// get the data from the camera
		String returnData = hardware.command("get c" + cineNumber + ".res");

		// split the data by "x"
		String[] splitData = returnData.split(" ");

		// construct the string to send to the camera
		String composite = "set c" + cineNumber + ".res " + splitData[2].trim() + " x " + height;

		String errorMessage = hardware.command(composite);

		// check to see if the response is positive
		if (errorMessage.compareToIgnoreCase("Ok!") != 0) {
			// The process has failed so return an error
			throw new DeviceException("Phantom Camera has failed to complete the command '" + composite
					+ "' returning with the error message :" + errorMessage);
		}

	}

	/**
	 * gets the image size being produced buy the camera of the speicfied cine
	 *
	 * @param cineNumber
	 *            the number of the cine in question
	 * @return the size in pixels of the image, i.e. 800x600 = 480000;
	 * @throws DeviceException
	 *             If there are any communications errors.
	 */
	public int getImageSize(int cineNumber) throws DeviceException {

		// get the data from the camera
		String returnData = hardware.command("get c" + cineNumber + ".res");

		// split the data by "x"
		String[] splitData = returnData.split(" ");

		// return the width
		int result = Integer.parseInt(splitData[2].trim()) * Integer.parseInt(splitData[4].trim());

		return result;

	}

	/**
	 * Gets the number of frames available from a particular cine
	 * @param cineNumber the cine number which the information is to be reqquested from
	 * @return the number of total frames recorded in the cine
	 * @throws DeviceException if there are any device exceptions
	 */
	public int getNumberOfFrames(int cineNumber) throws DeviceException {
		try {
			return Integer.parseInt(hardware.command("get c" + cineNumber + ".frcount").split(" ")[2]);
		} catch (NumberFormatException e) {
			throw new DeviceException("Phantom Camera returned an odd data format from the command " + "get c"
					+ cineNumber + ".frcount");
		}
	}

	/**
	 * This function waits for the cine specified to finish recording, and then returns
	 * @param cineNumber The number of the cine to wait for
	 * @param timeOutLength the length of time to wait for before returning an error, in Millis
	 * @throws DeviceException if the device is unobtainable
	 * @throws TimeLimitExceededException if the device times out.
	 */
	public void waitForStoredCine(int cineNumber, int timeOutLength) throws DeviceException, TimeLimitExceededException {
		long startTime = System.currentTimeMillis();
		long timeSpent = 0;
		while (timeSpent < timeOutLength) {

			// check to see if the camera is ready
			if(isCineStatus(cineNumber, "STR")) {
				return;
			}

			timeSpent = System.currentTimeMillis()-startTime;

		}

		throw new TimeLimitExceededException("cine "+cineNumber+ " is still not in a stored status, even after the timeout of "+timeOutLength+"mS");

	}

	protected void prepareCineSize(int numberOfFrames, int framesPerSecond, int width, int height)
			throws DeviceException {

		// first work out how much space is needed
		// TODO this may need to include information on the bitdepth
		int size = (width * height * numberOfFrames * 2) / 1000000;

		// TODO this should probably be wrappered properly
		hardware.command("alloc " + size);

		// TODO this should also be wrappered correctly
		hardware.command("set c1.res " + width + "x" + height);

		// TODO also needs to be wrappered correctly
		hardware.command("set c1.rate " + framesPerSecond);

		// TODO also needs to be wrappered correctly
		System.out.println(hardware.command("set c1.ptframes " + numberOfFrames));

		hardware.command("set c1.state { ABL ACT }");

		// TODO this should work
		// CineStatus states = getCineStatus(1)
		//
		// states.setDEF(false);
		//
		// setCineStatus(1, states);

	}

	protected void startAquisition(int cineNumber) throws DeviceException {
		hardware.command("rec " + cineNumber);
	}

	protected ScanFileHolder grabCollectedData(int cineNumber) throws DeviceException {

		// first wait till the process is complete
		boolean waiting = true;
		while (waiting) {
			// TODO need some way of breaking this look for a forced timout.

			if (isCineStatus(cineNumber, "STR")) {
				waiting = false;
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new DeviceException("Thread sleep failed in grabColectedData");
			}
		}

		// set up the return values
		ScanFileHolder output = new ScanFileHolder();

		// now grab the data
		// get the number of frames from the cine
		String out = hardware.command("get c"+cineNumber+".ptframes");
		String[] splitOut = out.split(" ");

		int numberOfFrames = Integer.parseInt(splitOut[2].trim());

		// TODO this port should also be set in the XML
		hardware.prepareForDataTransfer(12364, false);

		System.out.println(hardware.command("img { cine:"+cineNumber+", start:0, cnt:"+numberOfFrames+" }"));

		boolean finished = false;

		Dataset dataSet;

		while (!finished) {
			try {
				dataSet = DatasetFactory.createFromObject(hardware.getDataBlock(600 * 800), 600, 800);
				output.addDataSet("Image" + 0, dataSet);
				finished = true;
			} catch (OutOfMemoryError e) {
				//TODO this pause should be left for an amount of time specified in the XML too.
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					throw new DeviceException("Thread sleep error in the data grabbing routine.");
				}
			} catch (ScanFileHolderException e) {
				throw new DeviceException("cannot buffer that many images in memory", e);
			}
		}

		// once we are done, we should be able to send the communications thread the termination request.
		hardware.finishDataTransfer();

		return output;

	}

	protected void writeCollectedData(int cineNumber, IFileSaver saver ) throws DeviceException {

		// first wait till the process is complete
		boolean waiting = true;
		while (waiting) {
			// TODO need some way of breaking this look for a forced timeout.

			if (isCineStatus(cineNumber, "STR")) {
				waiting = false;
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				throw new DeviceException("Thread sleep failed in grabColectedData");
			}
		}

		// set up the return values
		ScanFileHolder output = new ScanFileHolder();

		// now grab the data

		// TODO this port should also be set in the XML
		hardware.prepareForDataTransfer(12364,false);

		System.out.println(hardware.command("img { cine:1, start:" + cineNumber + ", cnt:1 }"));

		Dataset dataSet = DatasetFactory.createFromObject(hardware.getDataBlock(600 * 800), 600, 800);

		try {
			output.addDataSet("Image" + 0, dataSet);
		} catch (ScanFileHolderException e) {
			throw new DeviceException("cannot buffer that many images in memory", e);
		}

		try {
			output.save(saver);
		} catch (ScanFileHolderException e) {
			throw new DeviceException("Scan File holder Save failed", e);
		}

	}


	@Override
	public int getStatus() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object readout() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {

		return null;
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {

	}

	/**
	 * @return the hardware
	 */
	public IPhantomV73Controller getHardware() {
		return hardware;
	}

	/**
	 * @param hardware
	 *            the hardware to set
	 */
	public void setHardware(IPhantomV73Controller hardware) {
		this.hardware = hardware;
	}

	@Override
	public void configure() throws FactoryException {

		try {
			hardware.connectToCamera();
		} catch (UnknownHostException e) {
			throw new FactoryException("Unknown host for the Phantom Camera", e);
		} catch (IOException e) {
			throw new FactoryException("Cannot connect to the Phantom Camera", e);
		}

	}

	@Override
	public void reconfigure() throws FactoryException {
		// TODO free the old port, and then gc.
		configure();
	}

	// The main methods which are in the Phantom interface, which will be used in a scan

	/**
	 * This is curently a test implementation only {@inheritDoc}
	 */
	@Override
	public void setUpForCollection(int numberOfFrames, int framesPerSecond, int width, int height)
			throws DeviceException {

	}

	/**
	 * This is currently a test implementation only {@inheritDoc}
	 */
	@Override
	public Object retrieveData(int cineNumber, int start, int count) throws DeviceException {

		// first we need to work out the size of the images which will be returned
		int imageSize = getImageSize(cineNumber);

		// next open the port to receive the data,
		// TODO the start point should be specific by the XML, but the number should be able to increase if the port is in use
		int portNumber = 12364;
		int maxPortNumber = 12399;
		boolean failed = true;
		while(failed) {
			try {
				hardware.prepareForDataTransfer(portNumber, true);
				failed=false;
			} catch (DeviceException e) {
				// try incrementing the port number
				portNumber++;
				if (portNumber > maxPortNumber) {
					throw new DeviceException("No free ports were available to connect to the camera on", e);
				}
			}
		}

		// tell the camera to release the data to our waiting port
		hardware.command("img { cine:"+cineNumber+", start:"+start+", cnt:"+count+", fmt:16 }");

		// check to make sure that the camera returned a sencible output.
		//TODO add in the check here.

		// see if the collection has finished
		//TODO this should also be read in from the XML
		try {
			waitForStoredCine(cineNumber, 10000);
		} catch (TimeLimitExceededException e) {
			throw new DeviceException("timeout occured in the Phantom Camera retrieveDataMethod",e);
		}


		// now, lets grab the data off the camera in handy chunks, each the image size.
		// this should allow the circular buffer plenty of time to refill, and then grab a new chunk.

		ScanFileHolder result = new ScanFileHolder();

		for(int i = 0; i < count ;i ++ ) {

			// now collect the data off the camera
			try {
				//TODO need to sort out getting the height and width correctly
				result.addDataSet("Frame" + i, DatasetFactory.createFromObject(hardware.getDataBlock(imageSize), 600, 800));
			} catch (IndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ScanFileHolderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// close the data connection to the camera
		hardware.finishDataTransfer();


		return result;

	}

	// CASTOR elements of the CLASS for xml instanciation

	/**
	 * This is for the creation in CASTOR
	 *
	 * @param setter
	 */
	public void setIPAddress(String setter) {
		IP = setter;
		hardware = new PhantomV73Controller(setter);
	}

	/**
	 * getter for CASTOR.
	 *
	 * @return the IP of the camera
	 */
	public String getIPAddress() {
		return IP;
	}

	@Override
	public void collectData() throws DeviceException {
		// TODO Auto-generated method stub

	}

	/**
	 * this just passes straight through to the hardware.
	 * {@inheritDoc}
	 */
	@Override
	public String command(String commandString) throws DeviceException {
		return hardware.command(commandString);
	}



}
