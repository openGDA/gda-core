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

package gda.device.scannable;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DummyDAServer;
import gda.factory.Finder;

/**
 * Used for controlling the operations for each scan point in an XAS step scan where a motor is to be moved and
 * afterwards a series of detectors are to be operated for the same period of time.
 * <p>
 * As the step size and collection time varies during XAS scans this Scannable is used so it can operate over a 2D
 * PyTuple of the data points (an explicit scan object in ConcurrentScan).
 */
public class XasScannable extends ScannableBase {

	protected Scannable energyScannable;
	protected Scannable[] theDetectors;
	protected double lastCollectionTimeUsed = 0;

	public XasScannable() {
		super();
		setOutputFormat(new String[] {"%.4f", "%.4f"});
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		Double[] positions = ScannableUtils.objectToArray(position);
		// this move must complete first
		energyScannable.moveTo(positions[0]);
		lastCollectionTimeUsed = positions[1];
		for (Scannable detector : theDetectors)
			if (detector instanceof Detector detWithTime) {
				detWithTime.setCollectionTime(positions[1]);
			}
	}

	@Override
	public Object getPosition() throws DeviceException {
		return new double[] { ScannableUtils.getCurrentPositionArray(energyScannable)[0], lastCollectionTimeUsed };
	}

	@Override
	public void atPointStart() throws DeviceException {
		energyScannable.atPointStart();
	}

	 @Override
	public void atScanLineStart() throws DeviceException {
		 energyScannable.atScanLineStart();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return energyScannable.isBusy();
	}

	@Override
	public String checkPositionValid(Object position) {
		Double[] positions = ScannableUtils.objectToArray(position);
		if (positions.length != 2)
			return "target position array wrong length. Should be 2.";
		return null;
	}

	/**
	 * @return Returns the energyScannable.
	 */
	public Scannable getEnergyScannable() {
		return energyScannable;
	}

	/**
	 * @param energyScannable
	 *            The energyScannable to set.
	 */
	public void setEnergyScannable(Scannable energyScannable) {
		this.energyScannable = energyScannable;
	}

	@Override
	public String[] getInputNames() {
		return new String[] { energyScannable.getName(), "Time" };
	}

	/**
	 * @return Returns the theDetectors.
	 */
	public Scannable[] getDetectors() {
		return theDetectors;
	}

	/**
	 * @param theDetectors
	 *            The theDetectors to set.
	 */
	public void setDetectors(Scannable[] theDetectors) {
		this.theDetectors = theDetectors;
	}

	@Override
	public void atScanStart() throws DeviceException {
		final Object server = Finder.find("DAServer");
		if (server instanceof DummyDAServer daServer) {
			daServer.resetScanPointCount();
		}
	}

}
