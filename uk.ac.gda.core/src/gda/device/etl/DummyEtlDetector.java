/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.etl;

import java.util.Random;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.EtlDetector;
import gda.device.detector.DetectorBase;

public class DummyEtlDetector extends DetectorBase implements EtlDetector {

	private static final Random RNG = new Random();
	private int hv;
	private int actualHv;
	private int upperThreshold;
	private int lowerThreshold;

	@Override
	public void collectData() throws DeviceException {
		// No action needed to start collection
	}

	@Override
	public int getStatus() throws DeviceException {
		return Detector.IDLE;
	}

	@Override
	public Object readout() throws DeviceException {
		// Random number but within thresholds
		return RNG.nextInt(upperThreshold - lowerThreshold) + lowerThreshold;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public void setHV(int mv) throws DeviceException {
		this.hv = mv;
	}

	@Override
	public int getActualHV() throws DeviceException {
		return actualHv;
	}

	@Override
	public int getHV() throws DeviceException {
		return hv;
	}

	@Override
	public void setUpperThreshold(int ulim) throws DeviceException {
		upperThreshold = ulim;
	}

	@Override
	public int getUpperThreshold() throws DeviceException {
		return upperThreshold;
	}

	@Override
	public void setLowerThreshold(int llim) throws DeviceException {
		lowerThreshold = llim;
	}

	@Override
	public int getLowerThreshold() throws DeviceException {
		return lowerThreshold;
	}


}
