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

import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.ScriptAdapter;

import org.python.core.PyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to control the stepped movement of a Scannable object.
 * <p>
 * At each step, after movement, the readout() method of all object in the DetectorBase.activeDetectors arraylist is
 * called.
 */
public class GridScan extends ScanBase implements Scan {

	private static final Logger logger = LoggerFactory.getLogger(GridScan.class);

	protected Object start;

	protected Object stop;

	protected Object step;

	protected Object time = new Double(1000.0);

	protected Object units;

	protected Object period;

	protected ScanBase childScan;

	protected ScriptAdapter scriptAdapter = null;

	protected Scannable theScannable = null;

	private String attrName;

	private Object attrValue;

	/**
	 *
	 */
	public GridScan() {
		super();
	}

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
	public GridScan(Scannable ve, Object start, Object stop, Object step) {
		allScannables.add(ve);
		theScannable = ve;
		this.start = start;
		this.stop = stop;
		this.step = step;
		this.childScan = null;
		setUp();
		setupGridScan();
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
	 *            Object
	 */
	public GridScan(Scannable ve, Object start, Object stop, Object step, Object time) {
		allScannables.add(ve);
		theScannable = ve;
		this.start = start;
		this.stop = stop;
		this.step = step;
		this.time = time;
		this.childScan = null;
		setUp();
		setupGridScan();
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
	 */
	public GridScan(Scannable ve, Object start, Object stop, Object step, Object time, Object units) {
		allScannables.add(ve);
		theScannable = ve;
		this.start = start;
		this.stop = stop;
		this.step = step;
		this.time = time;

		// somewhere between the tempScript2 file (which contains the
		// correct string) and the attempt by jython to create a GridScan the
		// string for micrometers loses its mu and gains an unprintable
		// character.
		// This superkludge gets round this but urgently needs fixing properly.
		// See bug #352.

		String string = (String) units;
		if (string != null && string.length() == 2 && string.charAt(0) == 65533 && string.charAt(1) == 'm') {
			// NB this (00B5) is 181 the code for mu as used for micro in
			// the Latin
			// encoding and not 956 (03BC) which is the proper Greek letter
			// mu.
			this.units = "\u00b5m";
		} else {
			this.units = units;
		}
		this.childScan = null;
		setUp();
		setupGridScan();
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
	public GridScan(Scannable ve, Object start, Object stop, Object step, Object time, Object units,
			DataWriter datahandler) {
		allScannables.add(ve);
		theScannable = ve;
		this.start = start;
		this.stop = stop;
		this.step = step;
		this.time = time;
		this.units = units;
		this.childScan = null;
		setDataWriter(datahandler);
		setUp();
		setupGridScan();
	}

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
	public GridScan(Scannable ve, Object start, Object stop, Object step, Scan childScan) {
		allScannables.add(ve);
		theScannable = ve;
		this.start = start;
		this.stop = stop;
		this.step = step;
		this.childScan = (ScanBase) childScan;
		setUp();
		setupGridScan();
	}

	/**
	 * A static method to create a scan which has a nested scan inside it. To run the scan, call the doScan() method.
	 *
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
	public GridScan(Scannable ve, Object start, Object stop, Object step, Object time, Object units,
			ScriptAdapter scriptAdapter, Object period) {
		allScannables.add(ve);
		theScannable = ve;
		this.start = start;
		this.stop = stop;
		this.step = step;
		this.time = time;
		this.units = units;
		this.childScan = null;
		this.scriptAdapter = scriptAdapter;
		this.period = period;
		setUp();
		setupGridScan();
	}

	@Override
	public void doCollection() throws Exception {
		// Determine if we are stepping over Quantities or PyList.
		// If a PyList then the scannable uses more complex quantities to define
		// the movement, so make the scannable calculate how many steps to do.
		int numberSteps = 0;
		if (this.start instanceof PyList) {
			numberSteps = ScannableUtils.getNumberSteps(theScannable, start, stop, step);
		} else {
			// get the numerical values of the arguements
			double start = Double.parseDouble(this.start.toString());
			double stop = Double.parseDouble(this.stop.toString());
			double step = Double.parseDouble(this.step.toString());
			// check that step is negative when moving downwards to stop
			double difference = stop - start;
			if (difference < 0 && step > 0) {
				step = -step;
			}
			// add half a step to round to neartest integer
			numberSteps = (int) ((difference / step) + 0.5);
		}

		double time = Double.parseDouble(this.time.toString());
		for (Detector detector : allDetectors) {
			detector.setCollectionTime(time);
		}

		// make first step
		logger.debug("Started a scan over " + theScannable.getName() + "\n");

		moveToStart();
		checkThreadInterrupted();
		if (this.childScan != null) {
			// The following line is required to ensure that for nested
			// scans
			// the addData is called by the outer scan first in order to
			// setup
			// the required columns and headers.
			ScanDataPoint point = new ScanDataPoint();
			point.setUniqueName(name);
			point.setHasChild(hasChild());
			getDataWriter().addData(point);
			runChildScan();
		} else {
			// then collect data
			currentPointCount++;
			collectData();
		}

		// make subsequent steps
		for (int i = 1; i <= numberSteps; ++i) {

			// test no reason to exit or wait
			if (isFinishEarlyRequested()){
				return;
			}
			waitIfPaused();
			checkThreadInterrupted();

			moveStepIncrement(i);
			checkThreadInterrupted();

			if (this.childScan != null) {
				// The following line is required to ensure that for
				// nested scans
				// the addData is called by the outer scan first in
				// order to
				// setup
				// the required columns and headers.
				ScanDataPoint point = new ScanDataPoint();
				point.setUniqueName(name);
				point.setHasChild(hasChild());
				getDataWriter().addData(point);
				runChildScan();
			} else {
				currentPointCount++;
				collectData();
			}
		}
	}

	/**
	 * @param currentStep
	 *            the number of the current step. NB This is not used here but it IS used by subclasses. Do not remove
	 *            it.
	 * @throws Exception
	 */

	// public void moveStepIncrement(int currentStep) throws Exception {
	// try {
	// String moveString = "" + step;
	// if (units != null)
	// moveString += " " + (String) units;
	//
	// theScannable.moveBy(moveString);
	// } catch (Exception e) {
	// if (e instanceof InterruptedException) {
	// throw e;
	// }
	// throw new Exception("GridScan.moveStepIncrement(): "
	// + e.getMessage());
	// }
	// }
	/**
	 * @param stepNos
	 *            int
	 * @throws Exception
	 */
	public void moveStepIncrement(int stepNos) throws Exception {
		try {
			// done this way as moveBy removed from the interface

			double currentPosition = Double.parseDouble(start.toString());
			double dblStart = Double.parseDouble(start.toString());
			double dblStep = Double.parseDouble(step.toString());

			currentPosition = dblStart + (stepNos * dblStep);
			if (units != null) {
				String moveString = Double.toString(currentPosition);
			    moveString += " " + (String) units;
                allScannables.get(0).moveTo(moveString);
			} else {
				allScannables.get(0).moveTo(currentPosition);
			}
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				throw e;
			}
			throw new Exception("Error in moveStepIncrement, stepNos: " + stepNos, e);
		}
	}

	/**
	 * Move the object of this scan to its initial position.
	 *
	 * @throws Exception
	 */
	public void moveToStart() throws Exception {
		try {
			if (units != null) {
				String moveString = "" + start;
				moveString += " " + (String) units;
				theScannable.moveTo(moveString);
			} else {
				theScannable.moveTo(start);
			}

			// savedUnits = theScannable.getReportingUnits();
			// theScannable.setReportingUnits(QuantityFactory
			// .createUnitFromString((String) units));

			if (attrName!=null) theScannable.setAttribute(attrName, attrValue);

		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				throw e;
			}
			throw new Exception("Couldn't move to start", e);
		}
	}

	/**
	 * Run the nested scan
	 * @throws Exception
	 */
	protected void runChildScan() throws Exception {
		// before running the child scan, make sure it is sharing the same
		// datahandler and lists
		childScan.setDataWriter(getDataWriter());
		childScan.isChild = true;

		for (Scannable scannable : allScannables) {
			if (!childScan.allScannables.contains(scannable)) {
				childScan.allScannables.add(scannable);
			}
		}

		// and in the same way build a list of all detectors
		for (Detector detector : allDetectors) {
			if (!childScan.allDetectors.contains(detector)) {
				childScan.allDetectors.add(detector);
			}
		}

		// run the child scan
		this.childScan.run();
	}

	/**
	 * Extra setup commands for grid scans. These are needed as the hierachy of parent\child scans in a
	 * multi-dimensional gridscan need to share the same datahandler - and this datahandler would want to see the same
	 * list of scannables and detectors.
	 * <p>
	 * This should be run after the base class setup() method.
	 */
	protected void setupGridScan() {


		try {
			if (start instanceof Number && stop instanceof Number && step instanceof Number) {
				final double s = ((Number)start).doubleValue();
				final double e = ((Number)stop).doubleValue();
				final double i = ((Number)step).doubleValue();
			    TotalNumberOfPoints = (int)((e-s)/i)+1;
				if (this.childScan != null) {
					final int n = childScan.TotalNumberOfPoints;
					if (n>0) TotalNumberOfPoints = TotalNumberOfPoints*n;
				}
			}
		} catch (Exception ex) {
			logger.error("Number format problem could not calculate number of points on GridScan", ex);
		}

		// if this scan has a child scan, then it should collect from that child
		// its data handler and list of scannables and detectors
		if (this.childScan != null) {
			// inform the nested scan that it is a child scan
			this.childScan.setIsChild(true);

			// add to the list of scannables all the scannables in the child
			// scan
			// in this way, the top level scan has a list of all the
			// dimensions
			// which the collection of scan use
			// this will not affect the movement in this scan as the
			// scannable
			// (dimension)
			// which this object scans over will still be at index position
			// 0.

			for (Scannable scannable : childScan.allScannables) {
				if (!allScannables.contains(scannable)) {
					allScannables.add(scannable);
				}
			}

			// and in the same way build a list of all detectors
			for (Detector detector : childScan.allDetectors) {
				if (!allDetectors.contains(detector)) {
					allDetectors.add(detector);
				}
			}
		}
	}

	/**
	 * Returns whether a child scan has been requested.
	 *
	 * @return a boolean indicating whether the gridscan has an associated childscan
	 */
	public boolean hasChild() {
		return childScan != null;
	}
}
