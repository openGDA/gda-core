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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Currently a very simple simulation of the camera
 *
 */
public class PhantomV73Simulation implements IPhantomV73Controller {

	boolean isConnected = false;

	@Override
	public String command(String commandString) throws DeviceException {
		//TODO this really needs to be more accurate, as this will fail a lot of methods in the driver class
		return "Ok!";
	}

	@Override
	public void connectToCamera() throws UnknownHostException, IOException {
		//TODO would be nice to simulate random crasheds and dissconnections
		isConnected = true;
	}

	@Override
	public boolean isConneted() {
		//TODO would be nice to simulate random crasheds and dissconnections
		return isConnected;
	}

	/**
	 * Currently a minimal application, this just returns random numbers as a datablock
	 * {@inheritDoc}
	 */
	@Override
	public double[] getDataBlock(int sizeOfArray) throws DeviceException, IndexOutOfBoundsException {
		double[] result = new double[sizeOfArray];
		Random rand = new Random(24);
		for(int i = 0; i < sizeOfArray ; i++) {
			result[i] = rand.nextInt(256);
		}
		
		return result;
	}

	/**
	 * Dosent need to clean up.
	 * {@inheritDoc}
	 */
	@Override
	public void finishDataTransfer() {
		return;
		
	}

	@Override
	public void prepareForDataTransfer(int portNumber, boolean sixteenBit) throws DeviceException {
		// TODO Auto-generated method stub		
	}

}
