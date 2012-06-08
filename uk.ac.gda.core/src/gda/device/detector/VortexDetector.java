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
import gda.observable.IObserver;

/**
 * The Class VortexDetector.
 */
public class VortexDetector extends DetectorBase implements Detector, IObserver {
	private static final long serialVersionUID = -4974673105670476398L;

	/**
	 * Returns the time the detector collects for during a call to collectData()
	 * 
	 * @return double
	 */
	@Override
	public double getCollectionTime() {
		return collectionTime;
	}

	@Override
	public void setCollectionTime(double collectionTime) {
		this.collectionTime = collectionTime;
		// set the collection times in the scaler and the mca

	}

	@Override
	public void collectData() throws DeviceException {
		// start data collection in the mca first , wait for it finish
		// then start counting in the scaler
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public Object readout() throws DeviceException {
		return null;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Vortex Detector";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Si Drift Diode";
	}

}
