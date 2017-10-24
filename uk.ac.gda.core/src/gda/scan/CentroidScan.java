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

import java.lang.reflect.Array;
import java.util.Vector;

import org.python.core.PyFloat;
import org.python.core.PyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for the ConcurrentScan, except it takes centroid, width, step as arguments.
 */
public class CentroidScan extends ScanBase implements Scan {

	private static final Logger logger = LoggerFactory.getLogger(CentroidScan.class);

	// the object this class wraps
	ConcurrentScan concurrentScan = null;

	/**
	 * Expect arguments in the format:
	 * <p>
	 * scannbleObject1 centroid [width step] scannbleObject2 centroid [width step] scannbleObject3 centroid [width step]
	 * <p>
	 * If width defined, then step must be defined, else an error is thrown.
	 * <P>
	 * The number of steps for each scannable must be the same, or an error will be thrwon.
	 *
	 * @param args
	 *            Object[]
	 * @throws IllegalArgumentException
	 */
	public CentroidScan(Object[] args) throws IllegalArgumentException {
		// work through the commands and translate into start stop step
		// then go through each in turn to work out what are args and what are
		// Scannables
		Vector<Object> newArgs = new Vector<Object>();
		try {
			for (int i = 0; i < args.length;) {
				if (args[i] instanceof Scannable) {
					// check to see if there are four more args
					if (args.length >= i + 4) {
						// is the arg three ahead a scannable? then use the
						// current
						// position as the centroid
						if (args[i + 3] instanceof Scannable) {
							Object[] startStop = calculateStartStop((Scannable) args[i], ((Scannable) args[i])
									.getPosition(), args[i + 1]);

							newArgs.add(args[i]); // object
							newArgs.add(startStop[0]); // start
							newArgs.add(startStop[1]); // stop
							newArgs.add(args[i + 2]); // step
							i += 3;
						}
						// is the arg two ahead a scannable? therefore only the
						// centroid(start) defined
						else if (args[i + 2] instanceof Scannable) {
							newArgs.add(args[i]); // object
							newArgs.add(args[i + 1]); // start
							i += 2;
						}
						// else next three arguments must be related to this
						// scannable
						else if (args.length >= i + 4) {
							Object[] startStop = calculateStartStop((Scannable) args[i], args[i + 1], args[i + 2]);

							newArgs.add(args[i]); // object
							newArgs.add(startStop[0]); // start
							newArgs.add(startStop[1]); // stop
							newArgs.add(args[i + 3]); // step
							i += 3;
						}
					}
					// if there are two more args and we're at the start
					// then use the
					// current position as the centroid
					else if (args.length == i + 3 && i == 0) {
						Object[] startStop = calculateStartStop((Scannable) args[i], ((Scannable) args[i])
								.getPosition(), args[i + 1]);

						newArgs.add(args[i]); // object
						newArgs.add(startStop[0]); // start
						newArgs.add(startStop[1]); // stop
						newArgs.add(args[i + 2]); // step
						i += 2;
					}
					// if there are two more args then it must be a start
					// and step
					// values
					else if (args.length == i + 3) {
						newArgs.add(args[i]); // object
						newArgs.add(args[i + 1]); // start
						newArgs.add(args[i + 2]); // step
						i += 2;
					}
					// if there is at least one more arg then it must be a
					// start
					// value
					else if (args.length == i + 2) {
						newArgs.add(args[i]); // object
						newArgs.add(args[i + 1]); // start
						i++;
					}
					// if there is one more arg then use its current
					// position as its
					// fixed value
					else if (args.length == i + 1) {
						Object startPosition = ((Scannable) args[i]).getPosition();

						newArgs.add(args[i]); // object
						newArgs.add(startPosition); // start
						i++;
					} else {
						i++;
					}
				} else {
					i++;
				}
			}

			// create the internal ConcurrentScan object using the converted
			// list
			// of arguments
			this.concurrentScan = new ConcurrentScan(newArgs.toArray());
		} catch (Exception ex) {
			throw new IllegalArgumentException(
					"cscan scannableName [centroid] width step [scannablename2] [centroid] [width]");
		}

	}

	@Override
	public Vector<Detector> getDetectors() {
		return concurrentScan.getDetectors();
	}

	@Override
	public Vector<Scannable> getScannables() {
		return concurrentScan.getScannables();
	}

	/**
	 * Creates and runs a scan.
	 *
	 * @param args
	 *            String[]
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public static void runScan(Object[] args) throws InterruptedException, Exception {
		CentroidScan thisScan = new CentroidScan(args);
		thisScan.runScan();
	}

	@Override
	public void prepareForCollection() throws Exception {
		this.concurrentScan.prepareForCollection();
		// prepareForCollection creates the data handler, so get a reference.
		this.setScanDataPointPipeline(concurrentScan.getScanDataPointPipeline());
	}

	@Override
	public void doCollection() throws Exception {
		// make sure the concurrent scan has the same observers
		try {
			this.concurrentScan.doCollection();
		} catch (Exception e) {
			if (!(e instanceof InterruptedException)) {
				this.moveToCentroids();
			}
			throw e;
		}
	}

	@Override
	protected void endScan() throws DeviceException, InterruptedException {
		if (!concurrentScan.returnScannablesToOrginalPositions) {
			try {
				this.moveToCentroids();
			} catch (Exception e) {
				// do want scan to fail now we have got to the end
				logger.warn("Centroid scan: exception while moving scannables back to starting positions", e);
			}
		}

		concurrentScan.endScan();
	}


	/**
	 * Move all objects involved in the scan back to their initial positions.
	 *
	 * @throws Exception
	 */
	private void moveToCentroids() throws Exception {
		checkThreadInterrupted();
		waitIfPaused();
		for (ScanObject j : this.concurrentScan.allScanObjects) {
			checkThreadInterrupted();
			j.moveToStart();
		}
		// pause here until all the movement has finished
		for (ScanObject j : this.concurrentScan.allScanObjects) {
			while (j.scannable.isBusy()) {
				Thread.sleep(250);
			}
		}
	}

	// very similar logic to ScannableBase.getNumberSteps
	private Object[] calculateStartStop(Scannable theScannable, Object centroid, Object width) {
		// how complex is this scannable?
		int numArgs = theScannable.getInputNames().length;

		if (numArgs == 1) {
			double centroidValue = Double.parseDouble(centroid.toString());
			double widthValue = Double.parseDouble(width.toString());

			Double[] result = new Double[2];
			result[0] = centroidValue - widthValue;
			result[1] = centroidValue + widthValue;
			return result;
		}
		// assume that the objects are all arrays of some form, then loop
		// through each input name and determine the number of steps for that
		// input name. The maximum number of steps identified is the returned
		// value

		// e.g. say an object takes positions in the form [h k l] then the
		// command:
		// start [1 1 1] stop [3 4 5] step [1 1 1] would return the value 4

		try {
			// some form of Java array class
			if (centroid.getClass().isArray()) {
				double[] start = new double[theScannable.getInputNames().length];
				double[] stop = new double[theScannable.getInputNames().length];

				Object[] result = new Object[2];
				result[0] = start;
				result[1] = stop;

				for (int i = 0; i < theScannable.getInputNames().length; i++) {
					double centroidValue = Array.getDouble(centroid, i);
					double widthValue = Array.getDouble(width, i);

					start[i] = centroidValue - widthValue;
					stop[i] = centroidValue + widthValue;
				}

				return result;
			}
			// if it comes from a Python command it will be a PyList
			else if (centroid instanceof PyList) {
				PyList centroid_list = (PyList) centroid;
				PyList width_list = (PyList) width;
				PyList start = new PyList();
				PyList stop = new PyList();

				Object[] result = new Object[2];
				result[0] = start;
				result[1] = stop;

				for (int i = 0; i < theScannable.getInputNames().length; i++) {

					double centroidValue = Double.parseDouble(centroid_list.__finditem__(i).toString());
					double widthValue = Double.parseDouble(width_list.__finditem__(i).toString());

					start.append(new PyFloat(centroidValue - widthValue));
					stop.append(new PyFloat(centroidValue + widthValue));
				}
				return result;
			}
		} catch (Exception e) {
			logger.debug(e.getStackTrace().toString());
			return null;
		}
		return null;
	}

}
