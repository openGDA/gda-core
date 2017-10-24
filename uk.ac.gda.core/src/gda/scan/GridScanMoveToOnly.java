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

package gda.scan;

import gda.device.Scannable;

/**
 * Avoids a grid scan having to use moveBy. This is a temporary hack and this class should be deleted when moveBy is
 * able to be used: the fact that moveBy doesnt work properly in this specific situation is the problem!
 * <p>
 * Generally, this class should not be used.
 */
public class GridScanMoveToOnly extends GridScan {
	/**
	 * Creates a scan object
	 *
	 * @param ve
	 *            the scannable
	 * @param start
	 *            double
	 * @param stop
	 *            double
	 * @param step
	 *            double
	 */
	// public GridScanMoveToOnly(Scannable ve, Object start, Object stop,
	// Object step) {
	// super(ve, start, stop, step);
	// }
	/**
	 * Creates a scan object. This constructor added to allow detector counting time to be set. Probably this should be
	 * done in ScanBase somehow.
	 *
	 * @param ve
	 *            the scannable
	 * @param start
	 *            double
	 * @param stop
	 *            double
	 * @param step
	 *            double
	 * @param time
	 *            Object
	 */
	// public GridScanMoveToOnly(Scannable ve, Object start, Object stop,
	// Object step, Object time) {
	// super(ve, start, stop, step, time);
	// }
	/**
	 * Creates a scan object. This constructor added to allow detector counting time to be set. Probably this should be
	 * done in ScanBase somehow.
	 *
	 * @param ve
	 *            the scannable
	 * @param start
	 *            double
	 * @param stop
	 *            double
	 * @param step
	 *            double
	 * @param time
	 *            double
	 * @param units
	 *            String
	 */

	public GridScanMoveToOnly(Scannable ve, Object start, Object stop, Object step, Object time, Object units) {
		super(ve, start, stop, step, time, units);
	}

	/**
	 * Creates a scan object. This constructor added to allow detector counting time to be set. Probably this should be
	 * done in ScanBase somehow.
	 *
	 * @param ve
	 *            the scannable
	 * @param start
	 *            double
	 * @param stop
	 *            double
	 * @param step
	 *            double
	 * @param time
	 *            double
	 * @param units
	 *            String
	 * @param datahandler
	 *            DataWriter
	 */
	// public GridScanMoveToOnly(Scannable ve, Object start, Object stop,
	// Object step, Object time, Object units, DataWriter datahandler) {
	// super(ve, start, stop, step, time, units, datahandler);
	// }
	/**
	 * A static method to create a scan which has a nested scan inside it. To run the scan, call the doScan() method.
	 * For example, in Jython: from gda.scan import Scan; myScan = Scan.create(tth,10,12,0.1,Scan.create(phi,20,30,1));
	 * myScan.doScan()
	 *
	 * @param ve
	 *            the scannable
	 * @param start
	 *            The position of the first data point
	 * @param stop
	 *            The position of the final data point
	 * @param step
	 *            The increment between data points
	 * @param childScan
	 *            ScanBase
	 */
	// public GridScanMoveToOnly(Scannable ve, Object start, Object stop,
	// Object step, Scan childScan) {
	// super(ve, start, stop, step, childScan);
	// }
	/**
	 * @param ve
	 *            Scannable
	 * @param start
	 *            Object
	 * @param stop
	 *            Object
	 * @param step
	 *            Object
	 * @param time
	 *            Object
	 * @param units
	 *            Object
	 * @param scriptAdapter
	 *            ScriptAdapter
	 * @param period
	 *            Object
	 */
	// public GridScanMoveToOnly(Scannable ve, Object start, Object stop,
	// Object step, Object time, Object units,
	// ScriptAdapter scriptAdapter, Object period) {
	// super(ve, start, stop, step, time, units, scriptAdapter, period);
	// }
	/**
	 * A static method to create a scan which has a nested scan inside it. To run the scan, call the doScan() method.
	 * For example, in Jython: from gda.scan import Scan; myScan = Scan.create(tth,10,12,0.1,Scan.create(phi,20,30,1));
	 * myScan.doScan()
	 *
	 * @param ve
	 *            the scannable
	 * @param start
	 *            The position of the first data point
	 * @param stop
	 *            The position of the final data point
	 * @param step
	 *            The increment between data points
	 * @param units
	 *            Object
	 * @param childScan
	 *            ScanBase
	 */
	// public GridScanMoveToOnly(Scannable ve, Object start, Object stop,
	// Object step, Object units, Scan childScan) {
	// super(ve, start, stop, step, units, childScan);
	// }
	/**
	 * @param stepNos
	 *            int
	 * @throws Exception
	 */
	@Override
	public void moveStepIncrement(int stepNos) throws Exception {
		try {
			double currentPosition = Double.parseDouble(start.toString());
			double dblStart = Double.parseDouble(start.toString());
			double dblStep = Double.parseDouble(step.toString());

			currentPosition = dblStart + (stepNos * dblStep);
			String moveString = Double.toString(currentPosition);
			if (units != null)
				moveString += " " + (String) units;

			allScannables.get(0).moveTo(moveString);
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				throw e;
			}
			throw new Exception("Couldn't move step increment " + stepNos, e);
		}
	}
}
