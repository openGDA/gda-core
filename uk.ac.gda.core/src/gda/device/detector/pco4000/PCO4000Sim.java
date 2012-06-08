/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector.pco4000;

import gda.device.DeviceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Simulation for the PCO 4000 camera.
 *
 */
public class PCO4000Sim implements IPCO4000Hardware {

	byte[] data = null;
	String inData = "data/gda/device/detector/pco4000/pco.tif";

	/**
	 * This constructor needs to load in some data and store it internaly, 
	 * so the objects saves out the data
	 * @throws DeviceException 
	 */
	public PCO4000Sim() throws DeviceException {

		// get the size of the data
		File in = new File(inData);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(in);
		} catch (FileNotFoundException e) {
			throw new DeviceException("File '"+inData+"' not found, simulation failed ", e);
		}
		// set the ammount of memory required to store the file
		data = new byte[(int) in.length()];

		try {
			fis.read(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new DeviceException("File '"+inData+"' not found, simulation failed ", e);
		}

		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new DeviceException("File '"+inData+"' not found, simulation failed ", e);
		}

	}

	/**
	 * Implementation of the expose detector method
	 * waits for a fake amount of time, and then copys a file to the 
	 * output location
	 * @param fileName 
	 * @param exposureTime 
	 * @throws DeviceException 
	 */
	@Override
	public void exposeDetector(String fileName, Double exposureTime) throws DeviceException{
		// start by simulating the exposure time.
		try {
			Thread.sleep((long) (exposureTime * 1000));
		} catch (InterruptedException e) {
			throw new DeviceException("Threadsleep in simulation failed ", e);
		}

		// now write out the data to the filename specified
		File out = new File(fileName);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(out);
		} catch (FileNotFoundException e) {
			throw new DeviceException("Output to file '"+fileName+"' in simulation failed ", e);
		}

		try {
			fos.write(data);
		} catch (IOException e) {
			throw new DeviceException("Output to file '"+fileName+"' in simulation failed ", e);
		}

		try {
			fos.close();
		} catch (IOException e) {
			throw new DeviceException("Output to file '"+fileName+"' in simulation failed ", e);
		}

	}

	/**
	 * This method simply returns the value to let the user know that they are 
	 * using the simulator
	 * @return "PCO4000 Simualtor"
	 */
	@Override
	public String getDetectorID() {
		return "PCO4000 Simulator";
	}

}
