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

import gda.device.Detector;
import gda.device.DeviceException;
import gda.factory.FactoryException;

/**
 * A Dummy class that will create data with the specified dimensions.
 */
public class DummyDetector extends DetectorBase implements Detector {

	protected int[] dims/* = {100, 100}*/;
	protected double[] data;
	protected int status;
	
	/**
	 * 
	 */
	public DummyDetector(){
	}

	/**
	 * With name and dimensions of output
	 * 
	 * @param string
	 * @param is
	 */
	public DummyDetector(String string, int[] is) {
		setName(string);
		dims = is;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	/**
	 * Set the data dimensions for this dummy detector
	 * 
	 * @param dims
	 */
	public void setDataDimensions(int[] dims) {
		this.dims = dims;
	}


	@Override
	public void reconfigure() throws FactoryException {
		configure();
	}

	@Override
	public void configure() throws FactoryException {
		status = Detector.IDLE;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return dims;
	}

	@Override
	public void collectData() throws DeviceException {
		status = Detector.BUSY;
		// generate data here

		int len = 1;

		if (dims != null) {
			for (int d : dims) {
				len *= d;
			}
		} else {
			len = extraNames.length;
		}
		data = new double[len];

		for (int i = 0; i < len; i++) {
			data[i] = i;
		}

		status = Detector.IDLE;
	}

	@Override
	public int getStatus() throws DeviceException {
		return status;
	}

	@Override
	public Object readout() throws DeviceException {
		Object returnvalue = data;
		data = null;
		return returnvalue;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Dummy Arbitrary Dimensional Detector";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "dumbdumb-1";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Dummy";
	}
}