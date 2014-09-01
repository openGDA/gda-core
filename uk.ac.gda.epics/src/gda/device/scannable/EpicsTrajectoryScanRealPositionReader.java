/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.scan.TrajectoryScanController.ReadStatus;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns the actual motor readback positions in an EPICS trajectory scan.
 * <p>
 * This buffers all the motor positions in the scan. This will only work in 1D or 2D scans properly.
 * <p>
 * As the motor positions can only be read out at the end of a trajectory this returns a RealPositionCallable for a
 * delayed retrun of the motor positions.
 */
public class EpicsTrajectoryScanRealPositionReader extends EpicsSingleTrajectoryScannable implements RealPositionReader {

	private static final Logger logger = LoggerFactory.getLogger(EpicsTrajectoryScanRealPositionReader.class);

	private int lastReadPointIndex = -1;
	private int indexOfLastPointInRow = -1;
	public ArrayList<double[]> positions = new ArrayList<double[]>();

	@Override
	public void atScanStart() throws DeviceException {
		// reset the arrays
		positions = new ArrayList<double[]>();
		this.lastReadPointIndex = -1;
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		try {
			if (tracController.getReadStatus() == ReadStatus.UNDEFINED) {
				// depending on which type of fly-scanning mechanism we are using we may or may not have to call this to
				// ensure that the readback has been done.
				// If its a ContinuousScan then this will have already been done, if its a ConstantVelocityScan then not.
				continuousMoveComplete();
			} else {
				actualPulses = tracController.getActualPulses();
			}

			double tempPositions[] = tracController.getMActual(trajectoryIndex);
			indexOfLastPointInRow = actualPulses - 1;
			int lineIndex = ((lastReadPointIndex) / indexOfLastPointInRow);
			if (positions.size() == 0)
				positions.add(tempPositions);
			else if (positions.size() < lineIndex + 1)
				positions.add(lineIndex, tempPositions);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in atScanLineEnd", e);
		}
	}

	@Override
	public Object getPosition() {
		return new RealPositionCallable(this, ++lastReadPointIndex);
	}

	@Override
	public Object get(int index) {
		try {
			int lineNumber = index / indexOfLastPointInRow;
			int pointNumber = index % indexOfLastPointInRow;
			if (lineNumber < 0)
				lineNumber = 0;

			// wait here as getPosition() will be called before atScanLineEnd() fills the array
			while (lineNumber >= positions.size()) {
				Thread.sleep(100);
				lineNumber = index / indexOfLastPointInRow;
				pointNumber = index % indexOfLastPointInRow;
			}

			// return null if there are no positions to return.
			if (positions.size() > lineNumber)
				return positions.get(lineNumber)[pointNumber];

			return null;

		} catch (InterruptedException e) {
			logger.error("InterruptedException waiting for positions to be retrieved for this row. Returning null for the real motor position.");
		}
		return null;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		this.setInputNames(new String[] { this.getName() });
	}
}
