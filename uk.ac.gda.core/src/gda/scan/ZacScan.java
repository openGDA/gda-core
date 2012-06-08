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


import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;

import java.util.TreeMap;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FastScan extends ScanBase to implement scans in which the Scannable
 * moves continuously from beginning to end, not step by step. The timing is
 * under the control of detectors, not the scan itself. In addition
 * all detectors are assumed capable of storing data frame by frame so that
 * it can be read out at the end of the scan.
 */
public class ZacScan extends ScanBase implements Scan {
	//private static final long serialVersionUID = 6245061265060159179L;

	private static final Logger logger = LoggerFactory.getLogger(ZacScan.class);
	//Vector<ScanObject> allScanObjects = new Vector<ScanObject>();

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


	/**
	 * 
	 */
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
		checkForInterruption();
		
		if ( !buildFastScan(null) ){
			JythonServerFacade.getInstance().print("building fast scan failed.");
			}
		else{
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
	
	/**
	 * 
	 */
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
	 * This method should be called before every task in the run method of a concrete scan class which takes a long
	 * period of time (e.g. collecting data, moving a motor).
	 * <P>
	 * If, since the last time this method was called, interrupted was set to true, an interrupted exception is thrown
	 * which should be used by the scan to end its run method.
	 * <P>
	 * If pause was set to true, then this method will loop endlessly until paused has been set to false.
	 * <P>
	 * As these variable are in the base class, these interrupts will effect all scans running.
	 * 
	 * @throws InterruptedException
	 */
	public void checkForInterruption() throws InterruptedException {
		try {
			if (paused & !interrupted) {
				JythonServerFacade.getInstance().setScanStatus(Jython.PAUSED);
				JythonServerFacade.getInstance().print("Server stops processing next command.");
				JythonServerFacade.getInstance().print(
						"Current constant velocity scan will continue on XPS server until it completes.");
				JythonServerFacade.getInstance().print("To stop CVScan, press Halt or StopAll");
				while (paused) {
					Thread.sleep(1000);
				}
				JythonServerFacade.getInstance().setScanStatus(Jython.RUNNING);
				JythonServerFacade.getInstance().print("Server resumes processing next command.");
			}
		} catch (InterruptedException ex) {
			interrupted = true;
		}

		if (interrupted) {
			try {
				for (Scannable scannable : allScannables) {
					scannable.stop();
					}
				for (Detector detector : allDetectors) {
					detector.stop();
					}
				try {
					Thread.sleep(100);
					}
				catch (InterruptedException e) {
					// no op
					}

				}
			catch (DeviceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			JythonServerFacade.getInstance().setScanStatus(Jython.IDLE);
			throw new InterruptedException();
		}
	}

	
	
	
	
	/**
	 * Structures to hold information about each object that will be scanned.
	 * 
	 *
	 */
	public class ScanObject {
		private Scannable scannable;
		private double start;
		private double stop;

		/**
		 * @param fastScannable 
		 * @param start
		 * @param stop
		 */
		public ScanObject(Scannable fastScannable, double start, double stop) {
			this.scannable = fastScannable;
			this.start = start;
			this.stop = stop;
		}

		/**
		 * @return start
		 */
		public double getStart() {
			return start;
		}

		/**
		 * @param start
		 */
		public void setStart(double start) {
			this.start = start;
		}

		/**
		 * @return stop
		 */
		public double getStop() {
			return stop;
		}

		/**
		 * @param stop
		 */
		public void setStop(double stop) {
			this.stop = stop;
		}

		/**
		 * @return step time
		 */
		public double getStepTime() {
			return stepTime;
		}

		/**
		 * @return scannable
		 */
		public Scannable getScannable() {
			return scannable;
		}

		/**
		 * @param scannable
		 */
		public void setScannable(Scannable scannable) {
			this.scannable = scannable;
		}

		/**
		 * @return total time
		 */
		public double getTotaltime() {
			return totalTime;
		}
	}
	
}
