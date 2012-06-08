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

import gda.data.scan.datawriter.DataWriterBase;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.jython.JythonServerFacade;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a scannable step through, and a detector to observe, finds the peak value from the detector in that region.
 * <p>
 * This class expects arguments in the format:
 * <p>
 * scannable1 start stop step [algorithm] [scannable2 [position]] detector [channel]
 * <p>
 * Further scannables may be included in the format of scannable2. They will be moved to a fixed location at the start
 * of the scan and remain at that point during the scan. If no position is given then the position at the start of the
 * scan is used - however the scannable will be included in any output from this scan.
 * <p>
 * The detector is assumed to produce numerical output. If no channel number is given, then the first (or only) channel
 * will be used in the algorithm.
 * <p>
 * [algorithm] relates to which algorithm will be used to determine the peak's properties. If no algorithm is given,
 * then no peak finding calculation will be made and the position of the maximum detector readout will be the peak.
 */
public class PeakScan extends ConcurrentScan implements Scan {
	
	private static final Logger logger = LoggerFactory.getLogger(PeakScan.class);
	
	/**
	 * no peak fitting algorithm used - just use highest point
	 */
	public static final int NONE = 0;

	// public static final int GAUSSIAN = 1;
	// public static final int BINOMIAL = 2;
	// public static final int POISSON = 3;
	// public static final int LORENTZIAN = 4;

	// objects being moved during scan
	Scannable theScannable = null;

	// the detector
	Detector theDetector = null;

	int detectorChannel = 0;

	// data points
	int algorithm = 0;

	Vector<String> yValues = new Vector<String>();

	Vector<String> xValues = new Vector<String>();

	// results
	/**
	 * Position of the peak
	 */
	public double peak = 0.0;

	/**
	 * FWHM of the peak
	 */
	public double fwhm = 0.0;

	/**
	 * Sigma of the peak
	 */
	public double sigma = 0.0;

	/**
	 * @param args
	 * @throws IllegalArgumentException
	 */
	public PeakScan(Object[] args) throws IllegalArgumentException {
		try {
			// there must be at least 4 args
			if (args.length < 4) {
				logger.info("Not enough arguments to define the scan!");
			}

			// find start, stop, step
			theScannable = (Scannable) args[0];
			ScanObject firstObj = new ImplicitScanObject(theScannable, args[1], args[2], args[3]);
			allScannables.add(theScannable);
			allScanObjects.add(firstObj);
			numberSteps = ScannableUtils.getNumberSteps(theScannable, args[1], args[2], args[3]);

			// is arg[4] the algorithm?
			int index = 4;
			if (!(args[index] instanceof Scannable)) {
				algorithm = Integer.parseInt(args[index].toString());
				index = 5;
			}

			// loop until the detector is found
			while (!(args[index] instanceof Detector) && index < args.length - 1) {
				// assume a scannable
				Scannable nextObj = (Scannable) args[index];
				Object objStart = null;
				// is the next either a Detector or a Scannable?
				if (!(args[index + 1] instanceof Scannable) && !(args[index + 1] instanceof Detector)) {
					objStart = args[index + 1];
					index++;
				} else {
					// no position given so use the current one
					objStart = nextObj.getPosition();
				}
				ScanObject addObj = new ImplicitScanObject(nextObj, objStart, null, null);
				// add this object to the vector of ScanObjects
				allScanObjects.add(addObj);
				allScannables.add(nextObj);
				index++;
			}
			// next arg must be the detectr
			theDetector = (Detector) args[index];

			// determine the detector's channel
			if (index == args.length - 2) {
				detectorChannel = Integer.parseInt(args[index + 1].toString());
			}

			super.setUp();
		} catch (Exception e) {
			throw new IllegalArgumentException("peak scan usage: scannable1 start stop step");
		}
	}

	@Override
	public void doCollection() throws InterruptedException {
		try {
			logger.debug("Starting scan...\n");
			// move to initial movements
			acquirePoint(true, false);
			// then collect data
			collect();
			// loop through the number of steps
			for (int step = 0; step < numberSteps; step++) {
				// make all these increments
				acquirePoint(false, false);
				// then collect data
				collect();
			}
			// perform calculation
			determinePeak();
		} catch (Exception ex1) {
			interrupted = true;
			if (ex1 instanceof InterruptedException) {
				throw (InterruptedException) ex1;
			}
		}
		// at end of scan remove all the observers of the detectors which were
		// registered through this scan
		finally {
			try {
				endScan();
			} catch (DeviceException ex) {
				String message = "Exception raised of type: " + ex.getClass() + " during scan.";
				logger.error(message);
				JythonServerFacade.getInstance().print(message);
				interrupted = true;
			}
		}
	}

	private void collect() throws Exception {

		// collect
		theDetector.collectData();
		// write data to data handler
		ScanDataPoint point = new ScanDataPoint();
		point.setScanIdentifier(name);
		point.addScannablesAndDetectors(allScannables, allDetectors);
		point.setCurrentFilename(getDataWriter().getCurrentFileName());
		getDataWriter().addData(point);
		// notify IObservers of this scan (e.g. GUI panels)
		// notifyIObservers(this, currentData);
		notifyServer(point);
		// extract the numbers for the calculation
		xValues.add(theScannable.getPosition().toString());
		yValues.add(DataWriterBase.getDetectorData(point.getDetectorData().get(0), true));
	}

	/**
	 * Fills the public variables of this object which hold the results of the peak fitting algorithms.
	 */
	private void determinePeak() {
		if (algorithm == 0) {
			// loop through the yValues and find the max
			double max = getChannelValue(yValues.get(0));
			int maxPos = 0;
			int j = 0;
			for (String yValue : yValues) {
				double newValue = getChannelValue(yValue);
				if (newValue > max) {
					max = newValue;
					maxPos = j;
				}
				j++;
			}
			peak = Double.parseDouble(xValues.get(maxPos));
		} else if (algorithm == 1) {
			// do Gaussian fit
			fitGaussian();
		}
	}

	/**
	 * The elements of the yValues vector are stored as strings produced by the DataWriterBase getDetectorData method.
	 * Whatever the original form of the data, the data would now be delimited strings. This method splits the string by
	 * the delimiter and returns the appropriate channel convert to a double.
	 * 
	 * @param yval
	 *            String
	 * @return double
	 */
	private double getChannelValue(String yval) {
		// break yval by tabs
		String[] elements = yval.split(DataWriterBase.delimiter);
		// return channel
		return Double.parseDouble(elements[detectorChannel]);
	}

	/**
	 * Tries to fit a Gaussian function to the data collected in this scan. Places the results in the public variables.
	 */
	private void fitGaussian() {
	}
}
