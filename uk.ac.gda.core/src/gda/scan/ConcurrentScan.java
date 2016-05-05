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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.python.core.PyException;
import org.python.core.PyTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.jython.InterfaceProvider;
import gda.jython.commands.ScannableCommands;

/**
 * Similar to 'scan' in CLAM (but order of arguments not the same). This moves several scannable objects simultaneously,
 * the same number of steps. After each movement data is collected from items in the allDetectors vector.
 * <p>
 * Expect arguments in the following format:
 * <p>
 * scannable1 start stop step scannable2 [start] [[stop] step] scannable3 [start] [[stop] step]
 * <P>
 * The number of steps is calculated from scannable1.
 * <p>
 * For subsequent scannables: if only 'start' then they are moved only to that position. If no start value given then
 * the current position will be used (so this scannable will not be moved, but will be included in any output from the
 * scan.
 * <p>
 * If a step value given then the scannable will be moved each time
 * <p>
 * If a stop value is also given then this is treated as a nested scan containing one scannable. This scan will be run
 * in full at each node of the main scan. If there are multiple nested scans then they are nested inside each other to
 * create a multidimensional scan space (rasta scan).
 * <p>
 * If {@link LocalProperties#GDA_SCAN_CONCURRENTSCAN_READOUT_CONCURRENTLY} is set to true Scannables will be moved to
 * the next point in a scan while detectors from the current point are read out (e.g. if a scan contains motors and
 * detectors, the motors will be moved to the next point while the detectors are read out).
 * <p>
 * CAUTION: If this feature is enabled then <b>all the detectors on the beamline</b> must latch counts somewhere before
 * {@link Detector#waitWhileBusy()} returns so that {@link Detector#readout()} is not effected by any concurrent motor
 * or shutter movements.
 * <p>
 * More precisely, when moving to the next point, Scannables at each subsequent level are moved until a level which
 * contains a detector is encountered. The scan command thread then waits until the detectors from the current point
 * have read out completely and the resulting ScanDataPoint has been added to the pipeline before continuing to operate
 * devices at the level containing the a detector. The scan command thread will also wait at the end of each line for
 * the Detector readout to complete before calling the atScanEnd() hooks and starting the next line.
 * <p>
 * NOTE that as a consequence of using this feature, an exception thrown while reading out a detector won't be thrown in
 * the main scan command thread until after the next point's 'motors' have moved.
 * <p>
 * Detectors must work as follows to take advantage of this feature:
 * <ul>
 * <li>{@link Detector#collectData()} should cause the hardware to start collecting immediately and as its interface
 * says return immediately. If there is any delay then detectors used in the same scan would collect over different
 * times when beam conditions may differ.</li>
 * <li>
 * {@link Detector#waitWhileBusy()} should return as soon as the exposure completes and it is safe to move motors.
 * i.e. counts <b>must</b> be safely latched either in hardware or software before returning.</li>
 * <li>
 * {@link Detector#readout()} should block until the detector is ready to start collecting again. The value returned
 * <b>must</b> not be effected by any concurrent motor or shutter movements.</li>
 * </ul>
 */
public class ConcurrentScan extends ConcurrentScanChild implements Scan {

	private static final Logger logger = LoggerFactory.getLogger(ConcurrentScan.class);

	/**
	 * Name of flag in Jython namespace which, if set to true, then this scan will return all the scannables to their
	 * initial positions at the end of this type of scan
	 */
	public static final String RETURNTOSTARTINGPOSITION = "scansReturnToOriginalPositions";

	/**
	 * The number of steps or movements (this dimension only)
	 */
	protected int numberSteps = 0;

	/**
	 * Number of points visited by scan (or multidimensional scan)
	 */
	private int numberPoints = 0;

	/**
	 * attribute to allow scannables to return to their original positions after a scan
	 */
	boolean returnScannablesToOrginalPositions = false;

	/**
	 * control whether EventType.UPDATED events are sent during scanning This is here to handle a particular issue with some scans (notably
	 * ConstantVelocityScanLine), where update messages from "dummy" pre-scans can swamp the GUI. Scans for which an issue can set this property false to
	 * suppress these messages. This is not a particularly nice solution, but the problem will disappear when the new scanning mechanism is active.
	 */
	boolean sendUpdateEvents = true;

	private HashMap<Scannable, Object> scannablesOriginalPositions = new HashMap<Scannable, Object>();

	private Vector<Scannable> userListedScannablesToScan = new Vector<Scannable>();

	private Vector<Scannable> userListedScannables = new Vector<Scannable>();

	/**
	 * For inheriting classes
	 */
	protected ConcurrentScan() {
		super();
	}

	/**
	 * Constructor.
	 *
	 * @param args
	 *            Object[]
	 * @throws IllegalArgumentException
	 */
	public ConcurrentScan(Object[] args) throws IllegalArgumentException {
		super();

		// Reconstruct the command line...
		if (command.isEmpty()) {
			command = "scan";
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof Scannable) {
				command = command + " " + ((Scannable) args[i]).getName();
			} else if (args[i] instanceof IConcurrentScanChild) {
				command += " " + ((IConcurrentScanChild) args[i]).getCommand();
			} else {
				command = command + " " + args[i];
			}
		}

		logger.info("Command to run: " +command);

		try {
			// the first argument should be a scannable else a syntax error
			if (args[0] instanceof Scannable) {
				int i = 0;
				Scannable firstScannable = (Scannable) args[0];
				ScanObject firstScanObject = null;

				// eg: scan m1 (0,3,5, ...)
				if (args.length <= 2 && args[1] instanceof PyTuple) {
					numberSteps = ((PyTuple) args[1]).__len__() - 1;
					firstScanObject = new ExplicitScanObject(firstScannable, ScanPositionProviderFactory
							.create(((PyTuple) args[1])));
					i = 2;
				}

				// eg: scan m1 (0,3,5, ...) m2 ...
				else if (args[1] instanceof PyTuple && !(args[2] instanceof PyTuple)) {
					numberSteps = ((PyTuple) args[1]).__len__() - 1;
					firstScanObject = new ExplicitScanObject(firstScannable, ScanPositionProviderFactory
							.create(((PyTuple) args[1])));
					i = 2;
				} else if ((args[1] instanceof ScanPositionProvider)) {
					ScanPositionProvider posList = (ScanPositionProvider) args[1];
					if (posList.size() < 1)
						throw new IllegalArgumentException("Number of positions in list < 1 for "
								+ firstScannable.getName());
					numberSteps = posList.size() - 1;
					firstScanObject = new ExplicitScanObject(firstScannable, posList);
					i = 2;
				}

				// eg: scan m1 0 10 1 m2 ...
				else {
					// ensure step is in the right direction
					args[3] = ScanBase.sortArguments(args[1], args[2], args[3]);

					numberSteps = ScannableUtils.getNumberSteps(firstScannable, args[1], args[2], args[3]);
					firstScanObject = new ImplicitScanObject(firstScannable, args[1], args[2], args[3]);
					i = 4;
				}
				userListedScannablesToScan.add((Scannable) args[0]);
				userListedScannables.add((Scannable) args[0]);

				// add this first scannable to the vector of all scannables
				allScannables.add(firstScannable);
				getAllScanObjects().add(firstScanObject);

				while (i < args.length) {// more scannables left
					boolean posToChangeDuringTheScan = false;
					int currentScannableIndex = i;

					// so you may pass a scan object as a parameter
					if (args[i] instanceof IConcurrentScanChild) {
						ConcurrentScanChild thisScan = (ConcurrentScanChild) args[i];
						thisScan.setIsChild(true);
						getAllChildScans().add(thisScan);
						numberOfChildScans++;
						thisScan.setNumberOfChildScans(numberOfChildScans);
						posToChangeDuringTheScan = true;

						// add to this parent the contents of this child scan
						for (Scannable subScannable : thisScan.getScannables()) {
							if (!allScannables.contains(subScannable)) {
								allScannables.add(subScannable);
							}
						}
						for (Detector subScannable : thisScan.getDetectors()) {
							if (!allScannables.contains(subScannable)) {
								allDetectors.add(subScannable);
							}
						}
						i++;
					} else if (args[i] instanceof Scannable) {

						Scannable thisScannable = (Scannable) args[i];

						// have nothing so only readout the scannable during the scan
						// eg: scan ... m2 m3 ... or scan ... m2
						if (i == args.length - 1 || (args[i + 1] instanceof Scannable)
								|| (args[i + 1] instanceof IConcurrentScanChild)) {
							allScannables.add(thisScannable);
							allScanObjects.add(new ImplicitScanObject(thisScannable, null, null, null));
							i++;
						}

						// have one arg: either just a start or an array of places
						// eg: scan ... m2 0 m3 ... or scan ... m2 0
						else if (i == args.length - 2 || (args[i + 2] instanceof Scannable)
								|| (args[i + 2] instanceof IConcurrentScanChild)) {

							ScanObject scanObject = null;
							// eg: scan ... m2 (0 1 2) m3 or scan ... m2 (0 1 2)
							if (args[i + 1] instanceof PyTuple || args[i + 1] instanceof ScanPositionProvider) {
								// then create the child scan
								Object[] childArgs = new Object[2];
								childArgs[0] = args[i];
								childArgs[1] = args[i + 1];
								ConcurrentScan newChildScan = new ConcurrentScan(childArgs);
								newChildScan.setIsChild(true);
								allChildScans.add(newChildScan);
								numberOfChildScans++;
								newChildScan.setNumberOfChildScans(numberOfChildScans);
								posToChangeDuringTheScan = true;

							}
							// eg: scan ... m2 0 m3 or scan ... m2 0
							else {
								scanObject = new ImplicitScanObject(thisScannable, args[i + 1], null, null);
							}
							allScannables.add(thisScannable);
							if (scanObject != null) {
								allScanObjects.add(scanObject);
							}
							i += 2;
						}

						// have start,step
						// eg: scan ... m2 0 1 m3 ... or scan ... m2 0 1
						else if (i == args.length - 3 || (args[i + 3] instanceof Scannable)
								|| (args[i + 3] instanceof IConcurrentScanChild)) {
							ScanObject scanObject = new ImplicitScanObject(thisScannable, args[i + 1], null,
									args[i + 2]);
							allScannables.add(thisScannable);
							allScanObjects.add(scanObject);
							i += 3;
							posToChangeDuringTheScan = true;
						}

						// have start,stop,step
						// eg: scan ... m2 0 1 10 m3 ... or scan ... m2 0 1 10
						else if (i == args.length - 4 || (args[i + 4] instanceof Scannable)
								|| (args[i + 4] instanceof IConcurrentScanChild)) {
							// keep this parent scan's list of all scannables complete
							allScannables.add((Scannable) args[i]);
							// then create the child scan
							Object[] childArgs = new Object[4];
							childArgs[0] = args[i];
							childArgs[1] = args[i + 1];
							childArgs[2] = args[i + 2];
							childArgs[3] = args[i + 3];
							ConcurrentScan newChildScan = new ConcurrentScan(childArgs);
							newChildScan.setIsChild(true);
							allChildScans.add(newChildScan);
							numberOfChildScans++;
							newChildScan.setNumberOfChildScans(numberOfChildScans);
							i += 4;
							posToChangeDuringTheScan = true;
						} else {
							throw new IllegalArgumentException(
									"scan usage: scannablename1 start stop step [scannablename2] [start] [ [stop] step]");
						}

						if (posToChangeDuringTheScan) {
							userListedScannablesToScan.add((Scannable) args[currentScannableIndex]);
						}

						userListedScannables.add((Scannable) args[currentScannableIndex]);
					} else {
						throw new IllegalArgumentException("Illegal argument for scan! Object "
								+ args[i].getClass().getName() + " is neither a Scannable nor a Scan.");
					}
				}// end of while
			} else {
				throw new IllegalArgumentException(
						"scan usage: scannablename1 start stop step [scannablename2] [start] [ [stop] step]");
			}

			super.setUp();

			// Calculate the number of points this ConcurrentScan and its child dimensions define.
			numberPoints = calculateNumberPoints();
			TotalNumberOfPoints = numberPoints;

			// work out all the points in the scan to check if they are all allowed
			generateScanPositions();

			// create a structure of child scans from the vector of scans
			nestChildScans();

			// check if return to original positions flag has been set
			checkReturnToOriginalPositionsFlag();

		} catch (Exception e) {
			//Log here as the exception will not be passed fully to GDA from Jython
			String message = e.getMessage();
			if (message == null){
				message = e.getClass().getSimpleName();
			}
			message = "Error while creating scan: " + message;
			logger.error(message, e);
			throw new IllegalArgumentException(message, e);
		}

		ScannableCommands.configureScanPipelineParameters(this);
	}

	private void checkReturnToOriginalPositionsFlag() {
		// check variable in Jython environment
		Object flag = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(RETURNTOSTARTINGPOSITION);
		if (flag != null && flag.toString().compareTo("1") == 0) {
			setReturnScannablesToOrginalPositions(true);
		} else {
			setReturnScannablesToOrginalPositions(false);
		}
	}

	public String reportDevicesByLevel() {
		TreeMap<Integer, Scannable[]> devicesToMoveByLevel = generateDevicesToMoveByLevel(scannableLevels, allDetectors);
		// e.g. "| lev4 | lev4a lev4b | *det9 || mon1, mon2
		String sMoved = "";
		List<String> toMonitor= new ArrayList<String>();
		for (Integer level : devicesToMoveByLevel.keySet()) {
			sMoved += " | ";
			List<String> toMove= new ArrayList<String>();
			for (Scannable scn : devicesToMoveByLevel.get(level)) {
				if (scn instanceof Detector) {
					toMove.add("*" + scn.getName());
				} else if (isScannableActuallyToBeMoved(scn)) {
					toMove.add(scn.getName());
				} else {
					// Scannable is acting like a monitor
					toMonitor.add(scn.getName());
				}
			}
			sMoved += StringUtils.join(toMove, ", ");
		}
		String sMonitor =  StringUtils.join(toMonitor, ", ");
		return (sMoved + ((sMonitor.equals("")) ? " |" : (" || " + sMonitor + " |"))).trim();
	}

	@Override
	public void doCollection() throws Exception {
		try {
			if (!this.isChild) {
				logger.info("Starting scan: '" + getName() + "' (" + getCommand() + ")" );
			}
			reportDetectorsThatWillBeMovedConcurrentlyWithSomeOtherScannables();
			logger.info("Concurrency: " + reportDevicesByLevel());

			// if true, then make a note of current position of all scannables to use at the end
			if (!isChild && isReturnScannablesToOrginalPositions()) {
				recordOriginalPositions();
			}

			// *** First point in this scan ***
			setPointPositionInLine(PointPositionInLine.FIRST);
			if (getChild() == null) {
				callAtPointStartHooks();
				// move to initial movements
				currentPointCount++;
				acquirePoint(true, true);  // start point, collect detectors
				checkThreadInterrupted();

				readDevicesAndPublishScanDataPoint();
				callAtPointEndHooks();
				if (sendUpdateEvents) {
					sendScanEvent(ScanEvent.EventType.UPDATED);
				}

				checkThreadInterrupted();
				waitIfPaused();
				if (isFinishEarlyRequested()) {
					return;
				}
			} else {
				// move the Scannable operated by this scan and then run the child scan
				ScanObject principleScanObject = this.allScanObjects.get(0);
				// There will only be one scannable moved in this parent scan, so no
				// need to sort by level!
				principleScanObject.scannable.atLevelStart();
				principleScanObject.scannable.atLevelMoveStart();
				stepId = principleScanObject.moveToStart();
				checkThreadInterrupted();
				checkAllMovesComplete();
				waitIfPaused();
				if (isFinishEarlyRequested()) {
					return;
				}
				principleScanObject.scannable.atLevelEnd();
				runChildScan();
				checkThreadInterrupted();
				// note that some scan hooks not called (atPointStart,atLevelMoveStart,atPointEnd) as this scannable is ot part of the child scan
			}

			// *** Subsequent points in this scan ***

			for (int step = 0; step < numberSteps; step++) {
				waitIfPaused();
				if (isFinishEarlyRequested()) {
					return;
				}

				setPointPositionInLine((step == (numberSteps - 1)) ? PointPositionInLine.LAST : PointPositionInLine.MIDDLE);

				if (getChild() == null) {
					callAtPointStartHooks();
					// make all these increments
					currentPointCount++;
					acquirePoint(false, true);  // step point, collect detectors
					checkThreadInterrupted();
					readDevicesAndPublishScanDataPoint();
					checkThreadInterrupted();
					callAtPointEndHooks();
					if (sendUpdateEvents) {
						sendScanEvent(ScanEvent.EventType.UPDATED);
					}
				} else {
					ScanObject principleScanObject = this.allScanObjects.get(0);
					principleScanObject.scannable.atLevelStart();
					principleScanObject.scannable.atLevelMoveStart();
					stepId = principleScanObject.moveStep();
					checkAllMovesComplete();
					checkThreadInterrupted();
					principleScanObject.scannable.atLevelEnd();
					runChildScan();
					checkThreadInterrupted();
				}
			}
		} catch (InterruptedException e) {
			setStatus(ScanStatus.TIDYING_UP_AFTER_STOP);
			throw new ScanInterruptedException(e.getMessage(),e.getStackTrace());
		} catch (Exception e) {
			setStatus(ScanStatus.TIDYING_UP_AFTER_FAILURE);
			throw e;
		}
	}

	private void reportDetectorsThatWillBeMovedConcurrentlyWithSomeOtherScannables() {
		Integer highestScannableLevel = scannableLevels.lastKey(); // i.e. lowest priority
		if (allDetectors.size() > 0) {
			for (Detector det : allDetectors) {
				if (det.getLevel() <= highestScannableLevel) {
					String info = MessageFormat
							.format("The level {0} detector {1} will be acquiring concurrently with the movement of some Scannables",
									det.getLevel(), det.getName());
					logger.info(info);
				}
			}
		}

	}

	private void callAtPointEndHooks() throws DeviceException {
		for (Scannable scannable : this.allScannables) {
			scannable.atPointEnd();
		}

		// With concurrent readout enabled detectors will be readout and atPointEnds called for
		// the next point within the readout thread. Therefore only call atPointEnds if
		// concurrent readout is disabled.
		if (!isReadoutConcurrent()) {
			for (Scannable scannable : this.allDetectors) {
				scannable.atPointEnd();
			}
		}

	}

	protected void callAtPointStartHooks() throws DeviceException {
		for (Scannable scannable : this.allScannables) {
			scannable.atPointStart();
		}

		// With concurrent readout enabled detectors will be readout and atPointStarts called for
		// for the next point within the readout thread. Therefore only call atPointStarts concurrent
		// readout is disabled or, if enabled if this is the first point in the scan line.

		if (!isReadoutConcurrent() || (getPointPositionInLine() == PointPositionInLine.FIRST)){
			for (Scannable detector : this.allDetectors) {
				detector.atPointStart();
			}
		}
	}

	@Override
	protected void endScan() throws DeviceException, InterruptedException {
		if (!isChild && isReturnScannablesToOrginalPositions()) {
			// return all scannables to original positions
			try {
				logger.info("End of scan, so returning scannables back to their initial positions.");
				for (Scannable thisOne : allScannables) {
					if (thisOne.getInputNames().length > 0) {
						thisOne.moveTo(this.scannablesOriginalPositions.get(thisOne));
					}
				}
			} catch (Exception e) {
				String message;
				if (e instanceof PyException) {
					message = e.toString();
				} else {
					message = e.getMessage();
				}
				logger.error("Exception whilst returning scannables back to original positions at end of scan: "
						+ message);
				logger.debug(message, e);
			}
		}
		super.endScan();
	}

	/**
	 * Gets the number of points visited by this scan
	 *
	 * @return the number of points in visited by this scan
	 */
	public int getNumberPoints() {
		return numberPoints;
	}

	/**
	 * @return List of scannables to be scanned in the order presented to the constructor
	 */
	public List<Scannable> getUserListedScannablesToScan() {
		return userListedScannablesToScan;
	}

	/**
	 * @return List of scannables in the order presented to the constructor
	 */
	public List<Scannable> getUserListedScannables() {
		return userListedScannables;
	}

	/*
	 * Run the nested scan @throws InterruptedException @throws Exception
	 */
	private void runChildScan() throws Exception {
		// give the child scan the dataHandler reference
		IConcurrentScanChild child = getChild();
		child.setScanDataPointPipeline(this.scanDataPointPipeline);

		// share out the list of scannables and detectors as compiled when scans were added
		child.setAllDetectors(this.allDetectors);
		child.setAllScannables(this.allScannables);

		// if declared a child, then the scan will not close the datahandler or return the baton when it finishes.
		child.setIsChild(true);

		// run the child scan, based on innerscanstatus
		child.run();
	}

	/*
	 * Using the vector allChildScans, nest the scans inside each other and place the top most scan in the childScan
	 * variable of this object. This creates a hierarchy of scans to be run at each node of this objects doCollection
	 * method.
	 */

	private void nestChildScans() {

		// reorder scans according to this rule: there must be only one child scans marked as final and this must be the
		// innermost scan
		boolean foundFinal = false;
		int finalIndex = -1;
		for (IConcurrentScanChild childScan : allChildScans) {
			if (childScan.isMustBeFinal() && foundFinal) {
				throw new IllegalArgumentException(
						"illegal multidimensional scan - only one child scan can be a 'final' scan");
			}
			if (childScan.isMustBeFinal()) {
				foundFinal = true;
				finalIndex = allChildScans.indexOf(childScan);
			}
		}
		// move scan to the end of the list
		if (foundFinal) {
			IConcurrentScanChild finalScan = allChildScans.get(finalIndex);
			allChildScans.remove(finalIndex);
			allChildScans.addElement(finalScan);
		}

		// Starting at the end of the vector allChildScans, make each one the child scan of the previous.

		IConcurrentScanChild parent = this;
		Enumeration<IConcurrentScanChild> em = allChildScans.elements();
		while (em.hasMoreElements()) {
			IConcurrentScanChild child = em.nextElement();
			child.setParent(parent);
			parent.setChild(child);
			child.setScanDataPointPipeline(parent.getScanDataPointPipeline());
			child.setScannableLevels(parent.getScannableLevels());
			child.setAllScannables(parent.getAllScannables());
			child.setAllDetectors(parent.getAllDetectors());
			child.setCommand(parent.getCommand());
			child.setTotalNumberOfPoints(parent.getTotalNumberOfPoints());

			// share with the child scan all the ScanObjects that do not define other childScans
			for (ScanObject scanObject : this.allScanObjects) {
				if (!scanObject.hasStop()) {
					child.getAllScanObjects().add(scanObject);
				}
			}
			parent = child;
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
			String reason = scanObj.arePointsValid();
			if (reason != null) {
				throw new IllegalArgumentException(reason);
			}
		}

	}

	@Override
	public int getDimension() {
		return allScanObjects.get(0).getNumberPoints();
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
		pointTally = allScanObjects.get(0).getNumberPoints();

		// Multiply the tally by the number of points in each of the other dimensions
		for (IConcurrentScanChild child : allChildScans) {
			pointTally = pointTally * (child.getAllScanObjects().get(0).getNumberPoints());
		}
		return pointTally;
	}

	/**
	 * Rebuild the scannablesOriginalPositions array with the current location of every scannable. This should be done
	 * at the start of doCollection if the scan is to return all scannables to their original locations at the end of
	 * the scan.
	 *
	 * @throws DeviceException
	 *             - thrown if a scannable getPosition fails
	 */
	private void recordOriginalPositions() throws DeviceException {
		// reset the vector
		scannablesOriginalPositions.clear();

		for (Scannable thisOne : allScannables) {
			if (thisOne.getInputNames().length > 0){
				scannablesOriginalPositions.put(thisOne, thisOne.getPosition());
			}
		}
	}

	public boolean isReturnScannablesToOrginalPositions() {
		return returnScannablesToOrginalPositions;
	}

	public void setReturnScannablesToOrginalPositions(boolean returnScannablesToOrginalPositions) {
		this.returnScannablesToOrginalPositions = returnScannablesToOrginalPositions;
	}

	public boolean isSendUpdateEvents() {
		return sendUpdateEvents;
	}

	public void setSendUpdateEvents(boolean sendUpdateEvents) {
		this.sendUpdateEvents = sendUpdateEvents;
	}
}
