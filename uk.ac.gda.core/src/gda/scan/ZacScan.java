/*-
 * Copyright © 2014 Diamond Light Source Ltd., Science and Technology
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


import java.util.TreeMap;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.Scannable;
import gda.jython.JythonServerFacade;

/**
 * Zac stands for Zero Acceleration
 * FastScan extends ScanBase to implement scans in which the Scannable
 * moves continuously from beginning to end, not step by step. The timing is
 * under the control of detectors, not the scan itself. In addition
 * all detectors are assumed capable of storing data frame by frame so that
 * it can be read out at the end of the scan.
 */
public class ZacScan extends ScanBase {

	private static final Logger logger = LoggerFactory.getLogger(ZacScan.class);

	/**
	 * The total time of the whole fast scan
	 */
	double totalTime;

	/**
	 * The time spend passing each point
	 */
	double stepTime;

	/**
	 * The number of points during the whole fast scan
	 */
	int numberPoints = 0;


	Vector<ScanObject> allScanObjects = new Vector<ScanObject>();

	TreeMap<Integer, Integer> scannableLevels;

	public ZacScan() {
		super();
	}

	/**
	 * Constructor.
	 * @param start
	 * @param end
	 * @param time
	 * @param step
	 *
	 * @throws IllegalArgumentException
	 */
	public ZacScan(double start, double end, double time, double step) throws IllegalArgumentException {
		JythonServerFacade.getInstance().print("Zero Acceleration (Constant Velocity) Scan on I06 Photon Energy...");
		String strCom="cvscan(" + start + ", "+ end +", " +time+", " + step +")";
		logger.debug("Invoking Jython function: " + strCom);
		JythonServerFacade.getInstance().evaluateCommand(strCom);
	}

	/**
	 * Constructor.
	 * @param scannable
	 * @param start
	 * @param end
	 * @param time
	 * @param step
	 *
	 * @throws IllegalArgumentException
	 */
	public ZacScan(Scannable scannable, double start, double end, double time, double step) throws IllegalArgumentException {
		super();
		allScannables.add(scannable);
		allScanObjects.add(new ScanObject(scannable, start, end));
		totalTime = time;
		stepTime = step;
		numberPoints = (int)(totalTime/stepTime + 1);

		super.setUp();

	}

	/**
	 * Constructor.
	 * @param scannable
	 * @param start
	 * @param end
	 * @param time
	 * @param step
	 * @param detector
	 *
	 * @throws IllegalArgumentException
	 */
	public ZacScan(Scannable scannable, double start, double end, double time, double step, Detector detector) throws IllegalArgumentException {
		super();

		allScannables.add(detector);
		allScannables.add(scannable);
		allScanObjects.add(new ScanObject(scannable, start, end));
		totalTime = time;
		stepTime = step;
		numberPoints = (int)(totalTime/stepTime + 1);

		super.setUp();

	}

	@Override
	public void prepareForCollection() throws Exception {
		JythonServerFacade.getInstance().print("FastScan: prepareForCollection.");
		super.prepareForCollection();

		JythonServerFacade.getInstance().print("setting up detectors ......");

		if (!buildFastScan(null)) {
			JythonServerFacade.getInstance().print("building fast scan failed.");
		} else {
			JythonServerFacade.getInstance().print("fast scan built successfully.");
		}
	}

	/**
	 * @param parameter
	 * @return true
	 */
	public boolean buildFastScan(@SuppressWarnings("unused") Object parameter) {
		return true;
	}

	public void startFastScan() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doCollection() throws Exception {
		JythonServerFacade.getInstance().print("FastScan: doForCollection.");
	}

	@Override
	public void collectData() {
		return;
	}

	/**
	 * Structures to hold information about each object that will be scanned.
	 */
	public class ScanObject {
		private Scannable scannable;
		private double start;
		private double stop;

		public ScanObject(Scannable fastScannable, double start, double stop) {
			this.scannable = fastScannable;
			this.start = start;
			this.stop = stop;
		}

		public double getStart() {
			return start;
		}

		public void setStart(double start) {
			this.start = start;
		}

		public double getStop() {
			return stop;
		}

		public void setStop(double stop) {
			this.stop = stop;
		}

		public double getStepTime() {
			return stepTime;
		}

		public Scannable getScannable() {
			return scannable;
		}

		public void setScannable(Scannable scannable) {
			this.scannable = scannable;
		}

		public double getTotaltime() {
			return totalTime;
		}
	}

}
