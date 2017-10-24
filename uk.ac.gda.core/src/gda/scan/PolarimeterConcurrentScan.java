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

package gda.scan;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;

import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.python.core.PyTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This moves several scannable objects simultaneously, the same number of steps. After each movement data is collected
 * from items in the allDetectors vector. Separate polarimeter class required to allow unique flux monitoring regime and
 * specific setting of measurement time.
 * <p>
 * Expect arguments in the following format:
 * <p>
 * scannable1 start stop step time [f pinhole] scannable2 [start] [[stop] step] scannable3 [start] [step]
 * <P>
 * The number of steps is calculated from scannable1.
 * <p>
 * For subsequent scannables: if only 'start' then they are moved only to that position. If no start value given then
 * the current position will be used (so this scannable will not be moved, but will be included in any output from the
 * scan.
 * <p>
 * If a step value given then the scannable will be moved each time
 * <p>
 */
public class PolarimeterConcurrentScan extends PolarimeterGridScan implements Scan {
	private static final Logger logger = LoggerFactory.getLogger(PolarimeterConcurrentScan.class);

	int numberSteps = 0; // The number of steps or movements (this dimension
	// only)
	int numberPoints; // Number of points visited by scan (or multidimensional
	// scan)
	Vector<ScanObject> allScanObjects = new Vector<ScanObject>();
	Vector<PolarimeterConcurrentScan> allChildScans = new Vector<PolarimeterConcurrentScan>();
	TreeMap<Integer, Integer> scannableLevels;

	/**
*
*/
	public PolarimeterConcurrentScan() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param args
	 *            Object[]
	 * @throws IllegalArgumentException
	 */
	public PolarimeterConcurrentScan(Object[] args) throws IllegalArgumentException {
		super();

		try {
			// the first argument should be a scannable else a syntax error
			if (args[0] instanceof Scannable) {
				int i = 0;
				Scannable firstScannable = (Scannable) args[0];
				ScanObject firstScanObject = null;

				// ensure step is in the right direction
				args[3] = ScanBase.sortArguments(args[1], args[2], args[3]);
				numberSteps = ScannableUtils.getNumberSteps(firstScannable, args[1], args[2], args[3]);
				firstScanObject = new ImplicitScanObject(firstScannable, args[1], args[2], args[3]);
				// Store count time
				this.time = args[4];
				i = 5;

				// Flux monitoring bit
				this.pinholeNumber = args[5];
				if (pinholeNumber.equals(0)) {
					this.monitorFlux = false;
				} else {
					this.monitorFlux = true;
				}
				i = 6;

				// add this first scannable to the vector of all scannables
				allScannables.add(firstScannable);
				allScanObjects.add(firstScanObject);

				while (i < args.length) {
					Scannable thisScannable = (Scannable) args[i];

					// have nothing so only readout the scannable during the scan
					if ((i < args.length - 1 && args[i + 1] instanceof Scannable) || i == args.length - 1) {
						allScannables.add(thisScannable);
						allScanObjects.add(new ImplicitScanObject(thisScannable, null, null, null));
						i++;
					}

					// have one agr: either just a start or an array of places
					else if ((i < args.length - 2 && args[i + 2] instanceof Scannable) || i == args.length - 2) {

						ScanObject scanObject;
						if (args[i + 1] instanceof PyTuple) {
							scanObject = new ExplicitScanObject(thisScannable, ScanPositionProviderFactory.create(((PyTuple) args[i + 1])));
						} else {
							scanObject = new ImplicitScanObject(thisScannable, args[i + 1], null, null);
						}
						allScannables.add(thisScannable);
						allScanObjects.add(scanObject);
						i += 2;
					}

					// have start,step
					else if ((i < args.length - 3 && args[i + 3] instanceof Scannable) || i == args.length - 3) {
						ScanObject scanObject = new ImplicitScanObject(thisScannable, args[i + 1], null, args[i + 2]);
						allScannables.add(thisScannable);
						allScanObjects.add(scanObject);
						i += 3;
					}

					// have start,stop,step
					else if ((i < args.length - 4 && args[i + 4] instanceof Scannable) || i == args.length - 4) {
						// keep this parent scan's list of all scannables complete
						allScannables.add((Scannable) args[i]);
						// then create the child scan
						Object[] childArgs = new Object[4];
						childArgs[0] = args[i];
						childArgs[1] = args[i + 1];
						childArgs[2] = args[i + 2];
						childArgs[3] = args[i + 3];
						PolarimeterConcurrentScan newChildScan = new PolarimeterConcurrentScan(childArgs);
						newChildScan.setIsChild(true);
						allChildScans.add(newChildScan);
						i += 4;
					} else {
						throw new IllegalArgumentException(
								"scan usage: scannablename1 start stop step [scannablename2] [start] [ [stop] step]");
					}
				}
			} else {
				throw new IllegalArgumentException(
						"scan usage: scannablename1 start stop step [scannablename2] [start] [ [stop] step]");
			}
			super.setUp();
			this.setupGridScan();

			// setUp has reordered the allScannables vector and removed objects
			// using
			// the Detector interface. This scan needs its allScanObjects vector to
			// keep to the same order as allScannables
			reorderAllScanObjects();

			// Calculate the number of points this ConcurrentScan and its child
			// dimensions define.
			numberPoints = calculateNumberPoints();

			// work out all the points in the scan to check if they are all allowed
			generateScanPositions();

			// create a structure of child scans from the vector of scans
			nestChildScans();

		} catch (Exception e) {
			throw new IllegalArgumentException("Error while creating scan", e);
		}

	}

	/*
	 * Using the vector allChildScans, nest the scans inside each other and place the top most scan in the childScan
	 * variable of this object. This creates a hierachcy of scans to be run at each node of this objects doCollection
	 * method.
	 */
	private void nestChildScans() {
		// Starting at the end of the vector allChildScans, make each one the
		// child
		// scan of the previous.
		PolarimeterConcurrentScan penultimateScan = this;
		while (allChildScans.size() > 0) {
			// get the first scan off the list and share out the filehandler
			PolarimeterConcurrentScan lastScan = allChildScans.firstElement();
			allChildScans.remove(lastScan);
			lastScan.setDataWriter(getDataWriter());
			// then share all of the fixed vectors
			lastScan.allScannables = this.allScannables;
			lastScan.allDetectors = this.allDetectors;
			lastScan.scannableLevels = this.scannableLevels;

			// set the latest scan to be the child of the scan before
			penultimateScan.childScan = lastScan;

			// store reference to last scan
			penultimateScan = lastScan;
		}
	}

	private void generateScanPositions() throws Exception {

		// loop through all scan object to check if number of steps is consistent
		int numberPoints = Integer.MIN_VALUE;
		for (ScanObject scanObj : this.allScanObjects) {
			int thisScanNumber = scanObj.getNumberPoints();

			if (thisScanNumber != 0) {
				if (numberPoints == Integer.MIN_VALUE) {
					numberPoints = thisScanNumber;
				} else if (numberPoints != thisScanNumber) {
					throw new IllegalArgumentException(
							"Error while setting up scan: objects must have the same number of scan points");
				}
			}
		}
		for (ScanObject scanObj : this.allScanObjects) {
			scanObj.setNumberPoints(numberPoints);
		}

		// generate the scan positions for implicit scan objects
		for (ScanObject scanObj : this.allScanObjects) {
			if (scanObj instanceof ImplicitScanObject) {
				((ImplicitScanObject) scanObj).calculateScanPoints();
			}
		}

		// then check if all scan objects' positions are valid
		for (ScanObject scanObj : this.allScanObjects) {
			String invalidPoint = scanObj.arePointsValid();
			if (invalidPoint != null) {
				throw new IllegalArgumentException("Error while preparing scan: position " + invalidPoint + " unacceptable for: "
						+ scanObj.scannable.getName());
			}
		}

	}

	/**
	 * Calculates the number of points this concurrent scan would visit. Works on single dimensional scans and on
	 * multidimensional scans containing children.
	 *
	 * @return integer - the number of points in the scans
	 */
	private int calculateNumberPoints() {
		int pointTally;

		// Get the number of points visited within this dimension
		// (ScanObject.getNumberPoints returns one less than it might).
		pointTally = allScanObjects.get(0).getNumberPoints() + 1;

		// Multiply the tally by the number of points in each of the other
		// dimensions
		for (PolarimeterConcurrentScan child : allChildScans) {
			pointTally = pointTally * (child.allScanObjects.get(0).getNumberPoints() + 1);
		}
		return pointTally;
	}

	@Override
	public void doCollection() throws Exception {
		if (!this.isChild) {
			logger.info("Starting scan.");
		}
		// Set detector count times
		double time = Double.parseDouble(this.time.toString());
		for (Detector detector : allDetectors) {
			detector.setCollectionTime(time);
		}

		// move to initial movements
		moveToStarts();
		// then collect data
		if (childScan != null) {
			// The following line is required to ensure that for nested scans
			// the addData is called by the outer scan first in order to setup
			// the required columns and headers.
			ScanDataPoint point = new ScanDataPoint();
			point.setUniqueName(name);
			point.addScannablesAndDetectors(allScannables, allDetectors);
			point.setHasChild(hasChild());
			getDataWriter().addData(point);
			runChildScan();
		} else {
			// first need to read and store flux value here if required
			if (monitorFlux) {
				this.measureFluxValue();
			}
			collectData();
		}
		// loop through the number of steps
		for (int step = 0; step < numberSteps; step++) {
			// make all these increments
			moveBySteps();
			// then collect data
			if (childScan != null) {
				runChildScan();
			} else {
				collectData();
			}
		}
	}

	/**
	 * Moves all the scannable objects to their start positions.
	 *
	 * @throws Exception
	 */
	protected void moveToStarts() throws Exception {
		// then loop over all Scannable objects and move then to their start
		// positions

		// loop over the groups of scannables
		Integer scannablesSoFar = 0;
		for (Integer thisLevel : scannableLevels.keySet()) {
			Integer numberAtThisLevel = scannableLevels.get(thisLevel);
			List<Scannable> scannablesAtThisLevel = allScannables.subList(scannablesSoFar, scannablesSoFar
					+ numberAtThisLevel);

			// loop over all scannables at this level and move them
			for (Scannable scannable : scannablesAtThisLevel) {
				// does this scan (is a heirarchy of nested scans) operate this
				// scannable?
				ScanObject scanObject = isScannableToBeMoved(scannable);

				if (scanObject != null) {
					checkThreadInterrupted();
					scanObject.moveToStart();
				}
			}

			// pause here until all the scannables at this level have finished
			// moving
			checkAllMovesComplete();

			scannablesSoFar += numberAtThisLevel;
		}
	}

	/*
	 * Asks if the given scannable is part of the array of scannables which this scan is to operate in its moveToStarts
	 * and moveBySteps methods. If true returns the ScanObject else returns null. @param scannable @return the
	 * ScanObject
	 */
	private ScanObject isScannableToBeMoved(Scannable scannable) {
		for (ScanObject scanObject : allScanObjects) {
			if (scanObject.scannable == scannable) {
				return scanObject;
			}
		}
		return null;
	}

	/*
	 * Waits until all the scannables of this scan are no longer moving. @throws InterruptedException
	 */
	private void checkAllMovesComplete() throws InterruptedException, DeviceException {
		for (ScanObject scanObject : allScanObjects) {
			// only check those objects which we have moved are no longer busy
			while (scanObject.hasStart() && scanObject.scannable.isBusy()) {
				Thread.sleep(100);
			}
		}
	}

	/**
	 * This is called at each subsequent step in the scan.
	 *
	 * @throws Exception
	 */
	protected void moveBySteps() throws Exception {
		// loop over the groups of scannables
		Integer scannablesSoFar = 0;
		for (Integer thisLevel : scannableLevels.keySet()) {
			Integer numberAtThisLevel = scannableLevels.get(thisLevel);
			List<Scannable> scannablesAtThisLevel = allScannables.subList(scannablesSoFar, numberAtThisLevel
					+ scannablesSoFar);

			// loop though the list of scannables, and set each one to move
			for (Scannable scannable : scannablesAtThisLevel) {
				// does this scan (is a heirarchy of nested scans) operate this
				// scannable?
				ScanObject scanObject = isScannableToBeMoved(scannable);
				if (scanObject != null) {
					checkThreadInterrupted();
					scanObject.moveStep();
				}
			}

			// pause here until all the scannables at this level have finished
			// moving
			checkAllMovesComplete();

			scannablesSoFar += numberAtThisLevel;
		}
	}

	/*
	 * Called during instantiation but after the setUp method has been called. The setUp method will have edited and
	 * reordered the allScannables vector. The allScanObjects vector must now be reordered to keep track of this. <p> In
	 * doing this, any objects using the Detector interface in the allScanObjects vector will be removed.
	 */
	private void reorderAllScanObjects() {
		Vector<ScanObject> sortedAllScanObjects = new Vector<ScanObject>();
		int i = 0;
		for (Object nextObject : allScannables) {
			for (ScanObject nextScanObject : allScanObjects) {
				if (nextScanObject.scannable.equals(nextObject)) {
					sortedAllScanObjects.add(i, nextScanObject);
					i++;
				}
			}
		}
		allScanObjects = sortedAllScanObjects;

		// now save information about how many scannables are at each level
		Integer currentLevel = 0;
		int numberAtThisLevel = 0;
		scannableLevels = new TreeMap<Integer, Integer>();

		// loop through all levels saving the amount of scannables at each level
		for (Scannable scannable : allScannables) {
			Integer thisObjectsLevel = scannable.getLevel();
			if (thisObjectsLevel.compareTo(currentLevel) != 0 && numberAtThisLevel != 0) {
				scannableLevels.put(currentLevel, numberAtThisLevel);
				numberAtThisLevel = 1;
			} else {
				numberAtThisLevel++;
			}
			currentLevel = thisObjectsLevel;
		}

		// if the last level was not a new one
		scannableLevels.put(currentLevel, numberAtThisLevel);
	}

}
