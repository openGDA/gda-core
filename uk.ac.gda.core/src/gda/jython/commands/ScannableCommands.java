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

package gda.jython.commands;

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.DetectorSnapper;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.detector.BufferedDetector;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.scannablegroup.IScannableGroup;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer;
import gda.jython.JythonServerFacade;
import gda.scan.AxisSpecProviderImpl;
import gda.scan.CentroidScan;
import gda.scan.ConcurrentScan;
import gda.scan.ContinuousScan;
import gda.scan.GridScan;
import gda.scan.IScanDataPoint;
import gda.scan.PointsScan;
import gda.scan.Scan;
import gda.scan.ScanBase;
import gda.scan.ScanPlotSettings;
import gda.scan.ScanPlotSettingsUtils;
import gda.scan.StaticScan;
import gda.scan.TestScan;
import gda.scan.TimeScan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.python.core.PyException;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holder for a series of static methods to operate Scannable objects
 */
public class ScannableCommands {

	private static final Logger logger = LoggerFactory.getLogger(ScannableCommands.class);

	private static boolean posCommandIsInTheProcessOfListingAllScannables = false;

	/**
	 * The pos command. Reports the current position of a scannable or moves one or more scannables concurrently.
	 *
	 * @param args
	 * @return String reporting final positions
	 * @throws Exception
	 *             - any exception within this method
	 */
	public static String pos(Object... args) throws Exception {
		if (args.length == 1) {
			if (args[0] == null) {// example: pos None, Jython command: pos([None])
				throw new Exception(
						"Usage: pos [ScannableName] - returns the position of all Scannables [or the given scannable]");
			} else if (args[0] instanceof IScannableGroup) {
				return ScannableUtils.prettyPrintScannableGroup((IScannableGroup) args[0]);
			} else if (args[0] instanceof ScannableBase) {// example: pos pseudoDeviceName, Jython command:
				// pos([pseudoDeviceName])
				return ((ScannableBase) args[0]).__str__().toString();
			}
			return args[0].toString();
		} else if (args.length >= 2) {// example pos pseudoDeviceName newPosition, Jython command:
						// pos([pseudoDeviceName, newPosition]
			// identify scannables and the positions to move them to
			Scannable[] scannableList = new Scannable[args.length / 2];
			HashMap<Scannable, Object> positionMap = new HashMap<Scannable, Object>();
			int j = 0;
			for (int i = 0; i < args.length; i += 2) {
				if (args[i] instanceof Scannable) {
					scannableList[j] = (Scannable) args[i];
					positionMap.put((Scannable) args[i],args[i + 1]);
					j++;
				}
			}

			// Check positions valid
			for (Scannable scannable : scannableList) {
				Object target = positionMap.get(scannable);
				if (!(scannable instanceof Detector)) {
					raiseDeviceExceptionIfPositionNotValid(scannable, target);
				}
			}

			try {
				// Group by level
				TreeMap<Integer, List<Scannable>> scannablesByLevel= new TreeMap<Integer, List<Scannable>>();
				for (Scannable scn: scannableList){
					Integer level = scn.getLevel();
					if (!scannablesByLevel.containsKey(level)){
						scannablesByLevel.put( level, new ArrayList<Scannable>() );
					}
					scannablesByLevel.get(level).add(scn);
				}

				// Move scannables of the same level concurrently
				String output = "Move completed: "; // KLUDGE

				for (Entry<Integer, List<Scannable>> currentLevelScannables:scannablesByLevel.entrySet() ){
					for (Scannable scn1: currentLevelScannables.getValue()){
						scn1.atLevelStart();
					}
					for (Scannable scn1: currentLevelScannables.getValue()){
						scn1.atLevelMoveStart();
					}
					// asynchronousMoveTo()
					for (Scannable scn1 : currentLevelScannables.getValue()) {
						if (scn1 instanceof DetectorSnapper) {
							Double collectionTime = PositionConvertorFunctions.toDouble(positionMap.get(scn1));
							((DetectorSnapper) scn1).prepareForAcquisition(collectionTime);
							((DetectorSnapper) scn1).acquire();
						} else if (scn1 instanceof Detector) {
							Double collectionTime = PositionConvertorFunctions.toDouble(positionMap.get(scn1));
							((Detector) scn1).setCollectionTime(collectionTime);
							((Detector) scn1).prepareForCollection();
							((Detector) scn1).collectData();
						} else {
							scn1.asynchronousMoveTo(positionMap.get(scn1));
						}
					}
					// **isBusy()**
					// Wait for all moves to complete. If there is a problem with one, then stop the Scannables
					// which may still be moving (those that have not yet been waited for), and _then_ throw the
					// exception.
					Exception exceptionCaughtWhileWaitingOnIsBusy = null;
					for (Scannable scn1 : currentLevelScannables.getValue()) {
						if (exceptionCaughtWhileWaitingOnIsBusy == null) {
							// normal behaviour
							try {
								scn1.waitWhileBusy();
							} catch (Exception e) {
								logger.error("Exception occured while waiting for {} to complete move:", scn1.getName(), e);
								// cause future passes through the loop to stop Scannables
								exceptionCaughtWhileWaitingOnIsBusy = e;
							}
						} else {
							// stop any motors that may be still moving
							try {
								logger.info("Stopping {} due to problem moving previous scannable.", scn1.getName());
								scn1.stop();
							} catch (DeviceException e) {
								logger.error("Caught exception while stopping {}: ", scn1.getName(), e);
							}
						}
					}
					if (exceptionCaughtWhileWaitingOnIsBusy != null) {
						throw exceptionCaughtWhileWaitingOnIsBusy;
					}
					for (Scannable scn1 : currentLevelScannables.getValue()) {
						scn1.atLevelEnd();
					}
				}

				if (scannableList.length > 1){
					output += "\n";
				}

				// return position
				for (Scannable scn3: scannableList){
					output += ScannableUtils.getFormattedCurrentPosition(scn3) + "\n";
				}
				return output.trim();
			} catch (Exception e) {
				// Call the atCommandFailure() hooks
				for (Scannable scn: scannableList){
					scn.atCommandFailure();
				}
				throw e;
			}
		}
		return "";
	}

	/**
	 * If the Scannable has the notion of a valid position, then check if the position is valid.
	 * @param scannable
	 * @param target
	 * @throws DeviceException if the position is not valid.
	 */
	private static void raiseDeviceExceptionIfPositionNotValid(
			Scannable scannable, Object target) throws DeviceException {
		if (scannable instanceof ScannableMotion){
			String reply = ((ScannableMotion)scannable).checkPositionValid(target);
			if (reply != null) {
				throw new DeviceException(
						scannable.getName() + ": invalid asynchMoveTo() position; problem is: " + reply);
			}
		}
	}


	/**
	 * The pos command. Reports the current position of a scannable or moves one or more scannables concurrently.
	 *
	 * @param args
	 * @throws Exception
	 *             - any exception within this method
	 */
	public static void upos(Object... args) throws Exception {
		if (args.length >= 2) {
			// identify scannables and the positions to move them to
			Scannable[] scannables = new Scannable[args.length / 2];
			Object[] positions = new Object[args.length / 2];
			int j = 0;
			for (int i = 0; i < args.length; i += 2) {
				if (args[i] instanceof Scannable) {
					scannables[j] = (Scannable) args[i];
					positions[j] = args[i + 1];
					j++;
				}
			}

			// send commands
			for (int i = 0; i < j; i++) {
				raiseDeviceExceptionIfPositionNotValid(scannables[i], positions[i]);
				scannables[i].asynchronousMoveTo(positions[i]);
			}

			// construct print out command
			String jythonCommand = "print \"\\rMove in progress: ";
			for (Scannable scannable : scannables) {
				jythonCommand += scannable.getName() + ":\" + str(" + scannable.getName() + ".getPosition()"
						+ ") + \" ";
			}
			jythonCommand = jythonCommand.substring(0, jythonCommand.length() - 5);

			// wait
			try {
				while (areAnyBusy(scannables)) {
					JythonServerFacade.getInstance().runCommand(jythonCommand);
					Thread.sleep(500);
				}
			} catch (Exception e) {
				// print out exception, but carry on in this method
				// to report final positions
				logger.info("Exception while waiting for move '{}' to finish: ", jythonCommand, e);
			}

			String output = "Move completed:  ";
			for (int i = 0; i < j; i++) {
				output += scannables[i].getName();
				Object position = scannables[i].getPosition();
				if (position != null) {
					output += ":" + position.toString() + " ";
				}
			}
			InterfaceProvider.getTerminalPrinter().print("\\n" + output);
		}
	}

	private static boolean areAnyBusy(Scannable[] scannables) throws DeviceException {
		for (int i = 0; i < scannables.length; i++) {
			if (scannables[i].isBusy()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Single argument version of pos - used by the pos.py script
	 *
	 * @param args
	 * @return String reporting final positions
	 */
	public static String pos(Object args) {
		if (args instanceof IScannableGroup){
			return ScannableUtils.prettyPrintScannableGroup((IScannableGroup) args);
		} else if (args instanceof Scannable){
			return ((Scannable) args).toFormattedString();
		}
		return args.toString();
	}

	/**
	 * Gets a list of names of all the scannables on the server
	 */
	public static List<String> getScannableNames() throws DeviceException {

		List<String> scannables = new ArrayList<String>();

		Map<String, Object> map = InterfaceProvider.getJythonNamespace().getAllFromJythonNamespace();
		for (String objName : map.keySet()) {
			Object obj = map.get(objName);
			if (obj instanceof Scannable) {
				scannables.add(((Scannable) obj).getName());
			}
		}

		return scannables;

	}

	/**
	 * prints to console all the scannables and their current position in the system
	 * @throws DeviceException
	 */
	public static void pos() throws DeviceException {
		posCommandIsInTheProcessOfListingAllScannables = true;
		try {
			Map<String, Object> map = InterfaceProvider.getJythonNamespace().getAllFromJythonNamespace();

			// create a list of all the members of scannable groups
			String[] scannableGroupMembers =new String[0];
			for (String objName : map.keySet()) {
				Object obj = map.get(objName);
				if (obj instanceof IScannableGroup) {
					scannableGroupMembers = (String[]) ArrayUtils.addAll(scannableGroupMembers, ((IScannableGroup) obj)
							.getGroupMemberNames());
				}
			}

			// remove those members from the global list, if they are there
			for (String groupMember : scannableGroupMembers){
				if (map.keySet().contains(groupMember))
				{
					map.remove(groupMember);
				}
			}

			// find the longest name, to help with formatting the output
			int longestName = 0;
			for (String objName : map.keySet()){
				Object obj = map.get(objName);
				if (obj instanceof Scannable && objName.length() > longestName){
					longestName = objName.length();
				}
			}


			// then loop over the reduced list and print each item separately, logging any errors if they occur
			for (String objName : map.keySet()) {
				Object obj = map.get(objName);
				try {
					if (obj instanceof IScannableGroup) {
						InterfaceProvider.getTerminalPrinter().print(((IScannableGroup) obj).toFormattedString());
					} else if (obj instanceof Scannable) {
						InterfaceProvider.getTerminalPrinter().print(ScannableUtils.prettyPrintScannable((Scannable) obj,longestName + 1));
					}
				} catch (PyException e) {
					InterfaceProvider.getTerminalPrinter().print(objName);
					logger.warn("PyException while getting position of {} : ", objName, e);
				} catch (Exception e) {
					InterfaceProvider.getTerminalPrinter().print(objName);
					logger.warn("Exception while getting position of {} : ", objName, e);
				}
			}
		} finally {
			posCommandIsInTheProcessOfListingAllScannables = false;
		}
	}

	public static boolean isPosCommandIsInTheProcessOfListingAllScannables() {
		return posCommandIsInTheProcessOfListingAllScannables;
	}

	/**
	 * Relative move version of pos.
	 *
	 * @param args
	 * @return String - reporting final positions
	 * @throws Exception
	 */
	public static String inc(Object... args) throws Exception {
		if (args.length == 1) {
			return args[0].toString();
		} else if (args.length >= 2) {
			// identify scannables and the positions to move them to
			Scannable[] scannables = new Scannable[args.length / 2];
			Object[] positions = new Object[args.length / 2];
			int j = 0;
			for (int i = 0; i < args.length; i += 2) {
				if (args[i] instanceof Scannable) {
					scannables[j] = (Scannable) args[i];
					positions[j] = args[i + 1];
					j++;
				}
			}

			// send commnds
			for (int i = 0; i < j; i++) {
				// get the positions as an array of doubles
				double[] currentPosition = ScannableUtils.getCurrentPositionArray_InputsOnly(scannables[i]);
				Double[] stepArray = ScannableUtils.objectToArray(positions[i]);

				// if it only contains one element then do the addition and give
				// to the object
				if (currentPosition.length == 1) {
					raiseDeviceExceptionIfPositionNotValid(scannables[i], currentPosition[0] + stepArray[0]);
					scannables[i].asynchronousMoveTo(currentPosition[0] + stepArray[0]);
				}
				// otherwise do element by element addition and pass the array
				// to the object
				else {
					Object target = ScannableUtils.calculateNextPoint(currentPosition, stepArray);
					raiseDeviceExceptionIfPositionNotValid(scannables[i], target);
					scannables[i].asynchronousMoveTo(target);
				}
			}

			// wait
			for (int i = 0; i < j; i++) {
				scannables[i].waitWhileBusy();
			}

			// return status
			String output = "Relative move complete: ";
			for (int i = 0; i < j; i++) {
				output += scannables[i].toFormattedString() + " ";
			}
			return output;
		}
		return "";
	}

	/**
	 * Relative move version of pos.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void uinc(Object... args) throws Exception {
		if (args.length >= 2) {
			// identify scannables and the positions to move them to
			Scannable[] scannables = new Scannable[args.length / 2];
			Object[] positions = new Object[args.length / 2];
			int j = 0;
			for (int i = 0; i < args.length; i += 2) {
				if (args[i] instanceof Scannable) {
					scannables[j] = (Scannable) args[i];
					positions[j] = args[i + 1];
					j++;
				}
			}

			// send commnds
			for (int i = 0; i < j; i++) {
				// get the positions as an array of doubles
				double[] currentPosition = ScannableUtils.getCurrentPositionArray_InputsOnly(scannables[i]);
				Double[] stepArray = ScannableUtils.objectToArray(positions[i]);

				// if it only contains one element then do the addition and give
				// to the object as the object might not cope with an array
				if (currentPosition.length == 1) {
					raiseDeviceExceptionIfPositionNotValid(scannables[i], currentPosition[0] + stepArray[0]);
					scannables[i].asynchronousMoveTo(currentPosition[0] + stepArray[0]);
				}
				// otherwise do element by element addition and pass the array
				// to the object
				else {
					Object target = ScannableUtils.calculateNextPoint(currentPosition, stepArray);
					raiseDeviceExceptionIfPositionNotValid(scannables[i], target);
					scannables[i].asynchronousMoveTo(target);
				}
			}

			// construct print out command
			String jythonCommand = "print \"\\rMove in progress: ";
			for (Scannable scannable : scannables) {
				jythonCommand += scannable.getName() + ":\" + str(" + scannable.getName() + ".getPosition()"
						+ ") + \" ";
			}
			jythonCommand = jythonCommand.substring(0, jythonCommand.length() - 5);

			// wait
			try {
				while (areAnyBusy(scannables)) {
					JythonServerFacade.getInstance().runCommand(jythonCommand);
					Thread.sleep(500);
				}
			} catch (Exception e) {
				// print out exception, but carry on in this method
				// to report final positions
				logger.info("Exception while waiting for move '{}' to finish: ", jythonCommand, e);
			}

			String output = "Relative move completed:  ";
			for (int i = 0; i < j; i++) {
				output += scannables[i].getName();

				Object position = scannables[i].getPosition();
				if (position != null) {
					output += ":" + position.toString() + " ";
				}
			}
			InterfaceProvider.getTerminalPrinter().print("\\n" + output);
		}
	}

	/**
	 * Name of Property read by LocalPropties.check. If True then the scanPlotSettings are set within the scan command
	 */
	public static final String GDA_SCAN_SET_SCAN_PLOT_SETTINGS = "gda.scan.useScanPlotSettings";
	/**
	 * Name of Property read by LocalPropties.get. List of indices ( space separated) to scannable fields to be plotted
	 * not and made invisible
	 */
	public static final String GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES_INVISIBLE = "gda.plot.ScanPlotSettings.YFieldIndicesInvisible";
	/**
	 * Name of Property read by LocalPropties.get. List of indices ( space separated) to scannable fields to be plotted
	 */
	public static final String GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES = "gda.plot.ScanPlotSettings.YFieldIndicesVisible";
	/**
	 * Name of Property read by LocalPropties.get. Index of scannable fields of scannables being scanned that is to be
	 * used as the x axis
	 */
	public static final String GDA_PLOT_SCAN_PLOT_SETTINGS_XAXIS = "gda.plot.ScanPlotSettings.XFieldIndex";
	/**
	 * Name of Property read by LocalPropties.check. If True the above indices are to the list of scannables as passed
	 * to the scan command. Else they are indices to the list of scannables when re-ordered to match the levels
	 */
	public static final String GDA_PLOT_SCAN_PLOT_SETTINGS_FROM_USER_LIST = "gda.plot.ScanPlotSettings.fromUserList";

	/**
	 * Name of Property read by LocalPropties.check. If TRUE a separate YAxis is used for each visible to be plotted
	 */
	public static final String GDA_PLOT_SCAN_PLOT_SETTINGS_SEPARATE_YAXES = "gda.plot.ScanPlotSettings.separateYAxes";

	/**
	 * Sets up and operates a ConcurrentScan. The scan which should be used most of the time.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void scan(Object... args) throws Exception {
		ConcurrentScan scan = createConcurrentScan(args);
		scan.runScan();
	}

	public static ConcurrentScan createConcurrentScan(Object... args) throws Exception {
		ConcurrentScan scan = new ConcurrentScan(args);
		doCommandLineSpecificConcurrentScanSetup(scan);
		return scan;
	}

	/**
	 * Warning: anything done in this method will not be done for scans created programmatically (e.g. in scripts).
	 */
	private static void doCommandLineSpecificConcurrentScanSetup(ConcurrentScan scan) throws Exception {
		if (LocalProperties.check(GDA_SCAN_SET_SCAN_PLOT_SETTINGS)) {
			scan.setScanPlotSettings(createScanPlotSettings(scan));
		}
	}

	/**
	 * @param theScan
	 * @return ScanPlotSettings with Xaxis and YAxes based on several properties
	 * @throws Exception
	 */
	public static ScanPlotSettings createScanPlotSettings(ConcurrentScan theScan) throws Exception {
		// If true fromUserList will cause the x-axis and y-axis field indices to refer to the
		// scannables as typed by the user (ignoring levels, and default scannables) rather
		// than to the list generated internally by ConcurrentScan.
		boolean fromUserList = LocalProperties.check(GDA_PLOT_SCAN_PLOT_SETTINGS_FROM_USER_LIST, true);

		// get index of x-axis field, defaulting to -1 indicating the last)
		Integer XaxisIndex = LocalProperties.getAsInt(GDA_PLOT_SCAN_PLOT_SETTINGS_XAXIS, -1);

		// get index of y-axis fields to make visible, defaulting to [-1]
		List<Integer> YAxesShownIndices = LocalProperties.getAsIntList(GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES,
				new Integer[] { -1 });

		// Get index of y-axis fields to show but make invisible, defaulting to null which will cause
		// all y axes to be shown and made invisible, except those set explicitly visible
		List<Integer> YAxesNotShownIndices = LocalProperties.getAsIntList(GDA_PLOT_SCAN_PLOT_SETTINGS_YAXES_INVISIBLE);

		ScanPlotSettings sps;
		if (fromUserList) {
			sps = ScanPlotSettingsUtils.createSettings(theScan.getUserListedScannablesToScan(), theScan
					.getUserListedScannables(), theScan.getNumberOfChildScans(), XaxisIndex, YAxesShownIndices,
					YAxesNotShownIndices);
		} else {
			sps = ScanPlotSettingsUtils.createSettingsWithDetector(theScan.getScannables(), theScan.getDetectors(), theScan
					.getNumberOfChildScans(), XaxisIndex, YAxesShownIndices, YAxesNotShownIndices);
		}
		if( LocalProperties.check(GDA_PLOT_SCAN_PLOT_SETTINGS_SEPARATE_YAXES,false)){
			sps.setAxisSpecProvider(new AxisSpecProviderImpl(true));
		}
		return sps;
	}

	public static void configureScanPipelineParameters(ScanBase scan) {
		int scanDataPointQueueLength = LocalProperties.getInt(
				LocalProperties.GDA_SCAN_MULTITHREADED_SCANDATA_POINT_PIPElINE_LENGTH, 4);
		int pointsToComputeSimultaneousely = LocalProperties.getInt(
				LocalProperties.GDA_SCAN_MULTITHREADED_SCANDATA_POINT_PIPElINE_POINTS_TO_COMPUTE_SIMULTANEOUSELY, 3);
		int positionCallableThreadPoolSize = pointsToComputeSimultaneousely *
				scan.numberOfScannablesThatCanProvidePositionCallables();

		/*
		 * This is called when creating a scan. An inner scan may already have set the
		 * threadPoolSize and Queue length appropriately. Only increase size here
		 */
		Scan childScan = scan.getChild();
		if( childScan != null && childScan instanceof ScanBase){
			ScanBase scBase =(ScanBase)childScan;
			scanDataPointQueueLength = Math.max( scanDataPointQueueLength, scBase.getScanDataPointQueueLength());
			positionCallableThreadPoolSize = Math.max( positionCallableThreadPoolSize, scBase.getPositionCallableThreadPoolSize());
		}

		if( scan.getPositionCallableThreadPoolSize() < positionCallableThreadPoolSize){
			scan.setPositionCallableThreadPoolSize(positionCallableThreadPoolSize);
		}
		if( scan.getScanDataPointQueueLength() < scanDataPointQueueLength){
			scan.setScanDataPointQueueLength(scanDataPointQueueLength);
		}
	}

	/**
	 * Sets up and operates a PointsScan.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void pscan(Object... args) throws Exception {
		PointsScan theScan = new PointsScan(args);
		theScan.runScan();
	}

	/**
	 * Sets up and operates a CentroidScan.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void cscan(Object... args) throws Exception {
		CentroidScan theScan = new CentroidScan(args);
		theScan.runScan();
	}


	/**
	 * Sets up and operates a StaticScan.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void staticscan(Scannable... args) throws Exception {
		StaticScan theScan = new StaticScan(args);
		theScan.runScan();
	}

	/**
	 * Creates and runs a grid scan.
	 * <p>
	 * I'm not sure how much this is used so I haven't included all GridScan constructors.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void gscan(Object... args) throws Exception {
		int numberArgs = args.length;
		GridScan theScan = null;
		switch (numberArgs) {
		case 4:
			theScan = new GridScan((Scannable) args[0], args[1], args[2], args[3]);
			break;
		case 5:
			theScan = new GridScan((Scannable) args[0], args[1], args[2], args[3], args[4]);
			break;
		case 6:
			theScan = new GridScan((Scannable) args[0], args[1], args[2], args[3], args[4], args[5]);
			break;
		}
		if (theScan != null) {
			theScan.runScan();
		}

	}

	/**
	 * Time scan
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void timescan(Object... args) throws Exception {
		int numberArgs = args.length;
		TimeScan theScan = null;
		switch (numberArgs) {
		case 3:
			theScan = new TimeScan(Integer.parseInt(args[0].toString()), Double.parseDouble(args[1].toString()), Double
					.parseDouble(args[2].toString()));
			break;
		case 4:
			if (args[0] instanceof Detector) {
				theScan = new TimeScan(args[0], Integer.parseInt(args[1].toString()), Double.parseDouble(args[2]
						.toString()), Double.parseDouble(args[3].toString()));
			} else if (args[3] instanceof DataWriter) {
				theScan = new TimeScan(Integer.parseInt(args[0].toString()), Double.parseDouble(args[1].toString()),
						Double.parseDouble(args[2].toString()), (DataWriter) args[3]);
			} else {
				throw new IllegalArgumentException(
						"tscan() usage: tscan numberOfPoints pauseTime collectTime [datahandler] or tscan detectors numberOfPoints pauseTime collectTime [datahandler]");
			}
			break;
		}
		if (numberArgs > 4) {
			ArrayList<Detector> detectors = new ArrayList<Detector>();
			DataWriter dw = null;
			int j = 0;
			Object[] value = new Object[3];
			for (int i = 0; i < numberArgs; i++) {
				if (args[i] instanceof Detector) {
					detectors.add((Detector) args[i]);
				} else if (args[i] instanceof DataWriter) {
					dw = (DataWriter) args[i];
				} else if (args[i] instanceof Number) {
					value[j] = args[i];
					j = j + 1;
				} else {
					throw new IllegalArgumentException(
							"tscan() usage: tscan numberOfPoints pauseTime collectTime [datahandler] or tscan detectors numberOfPoints pauseTime collectTime [datahandler]");
				}

			}

			if (dw == null) {
				theScan = new TimeScan(detectors, Integer.parseInt(value[0].toString()), Double.parseDouble(value[1]
						.toString()), Double.parseDouble(value[2].toString()));
			} else {
				theScan = new TimeScan(detectors, Integer.parseInt(value[0].toString()), Double.parseDouble(value[1]
						.toString()), Double.parseDouble(value[2].toString()), dw);
			}
		}
		if (theScan != null) {
			theScan.runScan();
		} else {
			throw new IllegalArgumentException(
					"tscan() usage: tscan numberOfPoints pauseTime collectTime [datahandler] or tscan detectors numberOfPoints pauseTime collectTime [datahandler]");
		}
	}

	/**
	 * Time scan
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void tscan(Object... args) throws Exception {
		if (args.length < 3)
			throw new IllegalArgumentException("timescan numberOfPoints pauseTime [collectTime] scannable... ");
		int points = Integer.parseInt(args[0].toString());
		double pause = Double.parseDouble(args[1].toString());
		Scannable timer = new gda.device.scannable.TimeTravelScannable();
		timer.setLevel(0);
		Object[] newargs;

		if ((args[2] instanceof Scannable)) {
			// then it is not collectTime
			newargs = new Object[args.length + 2];
			newargs[0] = timer;
			newargs[1] = 0;
			newargs[2] = (points - 1) * pause;
			newargs[3] = pause;
			for (int i = 2; i < args.length; i++) {
				newargs[i + 2] = args[i];
			}
		} else {
			double collect = Double.parseDouble(args[2].toString());
			for (Object foo : args) {
				if (foo instanceof Detector) {
					((Detector) foo).setCollectionTime(collect);
				}
			}
			newargs = new Object[args.length + 1];
			newargs[0] = timer;
			newargs[1] = 0;
			newargs[2] = (points - 1) * pause;
			newargs[3] = pause;
			for (int i = 3; i < args.length; i++) {
				newargs[i + 1] = args[i];
			}
		}

		ConcurrentScan theScan = new ConcurrentScan(newargs);
		theScan.runScan();
	}

	/**
	 * Sets up and operates a TestScan.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void testscan(Object... args) throws Exception {
		TestScan theScan = new TestScan(args);
		theScan.runScan();
	}

	/**
	 * Creates a ContinuousScan object which is ready to be run.
	 * <p>
	 * This command can be used within a 'scan' command so that a continuousscan can be used within a multi-dimension
	 * step scan.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void cv(Object... args) throws Exception {

		String usageMessage = "continuousscan continuouslyScannable start stop numberPoints totaltime bufferedDetector(s) ";

		//test correct number of args
		if (args.length < 6 || !(args[0] instanceof ContinuouslyScannable)) {
			throw new IllegalArgumentException(usageMessage);
		}

		//unpack array of detectors. This may be in an array of Java objects or a Jython list
		BufferedDetector[] detectors;
		if (args[5] instanceof BufferedDetector) {

			detectors = new BufferedDetector[args.length - 5];

			try {
				for (int i = 5; i < args.length; i++) {
					detectors[i - 5] = (BufferedDetector) args[i];
				}
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(usageMessage);
			}
		} else if (args[5] instanceof PyList) {
			PyList arg5 = (PyList) args[5];
			detectors = new BufferedDetector[arg5.__len__()];
			try {
				for (int i = 0; i < arg5.__len__(); i++) {
					detectors[i] = (BufferedDetector) arg5.get(i);
				}
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(usageMessage);
			}
		} else {
			throw new IllegalArgumentException(usageMessage);
		}


		double start = ScannableUtils.objectToArray(args[1])[0];
		double stop = ScannableUtils.objectToArray(args[2])[0];
		int numberSteps = ScannableUtils.objectToArray(args[3])[0].intValue();
		double time = ScannableUtils.objectToArray(args[4])[0];
		ContinuousScan theScan = new ContinuousScan((ContinuouslyScannable) args[0], start, stop, numberSteps, time,
				detectors);
		theScan.runScan();
	}

	/**
	 * Accessor method for the level attribute of Scannables. This dictates the order in which Scannables are operated
	 * at each node of a scan.
	 *
	 * @param args
	 * @return null or the level of the scannable
	 * @throws IllegalArgumentException
	 */
	public static Object level(Object... args) throws IllegalArgumentException {
		if (args.length == 0 || (!(args[0] instanceof Scannable))) {
			throw new IllegalArgumentException("level() usage: level scannable [new level]");
		}

		if (args.length == 2) {
			try {
				int newLevel = (Integer) args[1];
				((Scannable) args[0]).setLevel(newLevel);
				return ((Scannable) args[0]).getName() + " set to level: " + newLevel;
			} catch (ClassCastException e) {
				if (!(args[1] instanceof PyInteger)) {
					throw new IllegalArgumentException("level() usage: level scannable [new level]");
				}
				((Scannable) args[0]).setLevel(((PyInteger) args[1]).getValue());
				return ((Scannable) args[0]).getName() + " set to level: " + ((PyInteger) args[1]).getValue();
			}
		}
		return ((Scannable) args[0]).getLevel();

	}

	/**
	 * List all the Scannable objects used implicitly in every scan
	 */
	public static void list_defaults() {
		String output = "";

		Vector<Scannable> vector = ((JythonServer) Finder.getInstance().find(JythonServer.SERVERNAME))
				.getDefaultScannables();
		for (Findable thisFindable : vector) {
			output += thisFindable.getName() + "\\n";
		}

		// print out to the Jython interpreter directly (so output is formatted correctly)
		try {
			JythonServerFacade.getInstance().runCommand(
					"current_default_to_print='" + output
							+ "';print current_default_to_print;del(current_default_to_print)");
		} catch (Exception e) {
			// do nothing
		}
	}

	/**
	 * Set a Scannable to be used by default
	 */
	public static void add_default(Object... args) {
		for (Object arg : args) {
			if (arg instanceof Scannable) {
				Scannable scannable = (Scannable) arg;
				((JythonServer) Finder.getInstance().find(JythonServer.SERVERNAME)).addDefault(scannable);
				JythonServerFacade
						.getInstance()
						.print(scannable.getName()
								+ " added to the list of default Scannables. Remove from the list by using command: remove_default "
								+ scannable.getName());
			}
		}
	}

	/**
	 * Remove a Scannable from the list of defaults
	 */
	public static void remove_default(Object... args) {
		for (Object arg : args) {
			if (arg instanceof Scannable) {
				Scannable scannable = (Scannable) arg;
				((JythonServer) Finder.getInstance().find(JythonServer.SERVERNAME)).removeDefault(scannable);
				JythonServerFacade
						.getInstance()
						.print(scannable.getName()
								+ " removed from list of default Scannables. Add back to the list by using command: add_default "
								+ scannable.getName());
			}
		}
	}

	public static IScanDataPoint lastScanDataPoint() {
		return InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint();
	}

}
