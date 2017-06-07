/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.XmapDetector;
import gda.device.scannable.ScannableBase;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import uk.ac.gda.beans.xspress.XspressDetector;
/**
 * Class to to provide data used for detector rate view (e.g. XspressMonitorView}, XmapMonitorView})
 * An instance of this class runs on the server and returns data from {@link CounterTimer}, {@link XspressDetector},
 * , {@link XmapDetector} detectors vis the {@link #getIonChamberValues(COLLECTION_TYPES)}
 * and {@link #getFluoDetectorCountRatesAndDeadTimes(COLLECTION_TYPES)} methods.
 * Set {@link #setCollectionAllowed} to False to prevent collection of data. This can be used to be sure that no data is
 * being collected by the detector rate view whilst a scan is running. <p>
 * This class can also be used in a scan to stop detector rate collection :
 * the {@link #atScanStart} sets the collection flag allowed flag to false, and blocks until the current collection has finished.
 *
 */
public class DetectorMonitorDataProvider extends ScannableBase implements DetectorMonitorDataProviderInterface {

	protected static final Logger logger = LoggerFactory.getLogger(DetectorMonitorDataProvider.class);

	public enum COLLECTION_TYPES {XSPRESS2, XMAP, XMAP_I1};

	private volatile boolean collectionInProgress = false;

	private boolean collectionAllowed = true;

	private double collectionTime = 1.0;

	// References to the detectors - set these by injection using Spring or Jython.
	private XspressDetector xspress2Detector;
	private CounterTimer ionchambers;
	private XmapDetector xmapDetector;

	private String name;

	public DetectorMonitorDataProvider() {
		// Set to empty lists to avoid exceptions when formatting the position
		setOutputFormat(new String[]{});
		setInputNames(new String[]{});
	}

	public static COLLECTION_TYPES getCollectionTypeFromString(String typeString) {
		try {
			logger.debug("Getting collection type from {}", typeString);
			return COLLECTION_TYPES.valueOf(typeString.toUpperCase());
		} catch(IllegalArgumentException e) {
			logger.warn("Collection type {} not recognised", typeString);
		}
		return null;
	}

	@Override
	public double getCollectionTime() {
		return collectionTime;
	}

	@Override
	public void setCollectionTime(double collectionTime) {
		this.collectionTime = collectionTime;

	}

	private String getMissingDetectors(COLLECTION_TYPES collectionType) {
		String missingDetectors = "";
		if (ionchambers==null) {
			missingDetectors += "ionchambers ";
		}
		if (collectionType == COLLECTION_TYPES.XSPRESS2 && xspress2Detector==null) {
			missingDetectors += "xspress2Detector ";
		}
		if ((collectionType == COLLECTION_TYPES.XMAP || collectionType == COLLECTION_TYPES.XMAP_I1) && xmapDetector==null) {
			missingDetectors += "xmapDetector";
		}
		return missingDetectors;
	}

	/**
	 * Check that detectors have been set for required collection type
	 * @param collectionType
	 * @throws DeviceException if required detector(s) are missing.
	 * 			This can be caught up by the detector rates gui and displayed as a warning message to user
	 */
	private void checkDetectors(COLLECTION_TYPES collectionType) throws DeviceException{
		String missingDetectors = getMissingDetectors(collectionType);
		if (missingDetectors.length()>0) {
			throw new DeviceException("Cannot collect data - "+missingDetectors+" have not been set ");
		}
	}

	/**
	 * Setup ion chamber to record one frame of data and return the data collected.
	 * Also trigger collection of xspress, xmap data at the same time.
	 * @param type
	 * @return ionchamber counts [counts per second]
	 * @throws Exception
	 */
	@Override
	public Double[] getIonChamberValues(COLLECTION_TYPES type) throws Exception {
		logger.debug("Collect ion chamber values for {}", type.toString());
		if(!checkCollectionAllowedStatus()) {
			return null;
		}
		checkDetectors(type);
		waitWhileBusy();
		collectionInProgress=true;
		try {
			switch(type) {
				case XSPRESS2 : return getIonChambersForXspress2();
				case XMAP 	  : return getIonChambersForXmap();
				case XMAP_I1  : return getIonChambersForXmapI1();
				default 	  : return null;
			}
		} finally {
			collectionInProgress=false;
			logger.debug("Collect ion chamber values finished");
		}
	}

	/**
	 * Return data from xspress2, xmap detector. Note, this does not trigger collection of new data -
	 * it only returns data last recorded (e.g. during last call to {@link #getIonChamberValues(COLLECTION_TYPES)}.
	 * @param type
	 * @return xspress2, xmap data for each element of the detector
	 * @throws DeviceException
	 */
	@Override
	public Double[] getFluoDetectorCountRatesAndDeadTimes(COLLECTION_TYPES type) throws DeviceException {
		logger.debug("Collect fluorescence detector values for {}", type.toString());
		if(!checkCollectionAllowedStatus()) {
			return null;
		}
		checkDetectors(type);
		waitWhileBusy();
		collectionInProgress=true;
		try {
			switch(type) {
				case XSPRESS2 : return getXSpress2Data();
				case XMAP 	  :
				case XMAP_I1  : return getXmapData();
				default 	  : return null;
			}
		} finally {
			collectionInProgress=false;
			logger.debug("Collect fluorescence detector values finished");
		}
	}

	/**
	 * Return number of elements on the xmap, xspress2 detector
	 * @param type
	 * @return number of detector elements
	 * @throws DeviceException
	 */
	@Override
	public int getNumElements(COLLECTION_TYPES type) throws DeviceException {
		checkDetectors(type);
		switch(type) {
			case XSPRESS2 : return xspress2Detector.getNumberOfDetectors();
			case XMAP :
			case XMAP_I1 : return xmapDetector.getNumberOfMca();
			default 	  : return 0;
		}
	}

	private boolean checkCollectionAllowedStatus() {
		logger.debug("Collection allowed = {}", collectionAllowed);
		if (!collectionAllowed) {
			logger.info("Detector rate collection currently disabled");
		}
		return collectionAllowed;
	}

	public boolean getScriptOrScanIsRunning() {
		return JythonServerFacade.getInstance().getScanStatus() != Jython.IDLE ||
			   JythonServerFacade.getInstance().getScriptStatus() != Jython.IDLE;
	}

	// TODO refactor to make getIonChambersForXMap and getIonChambersForXSpress2 use same function.
	// Currently, the two functions are just copied from XspressMonitorView, XmapMonitorView without attempting to tidy up...
	private Double[] getIonChambersForXspress2() throws Exception {
		logger.debug("Collect values from {} and {}", ionchambers.getName(), xspress2Detector.getName());

		if ( !getScriptOrScanIsRunning() && !xspress2Detector.isBusy()
				&& !ionchambers.isBusy()) {
			xspress2Detector.collectData();
			ionchambers.setCollectionTime(collectionTime);
			ionchambers.clearFrameSets();
			ionchambers.collectData();
			xspress2Detector.waitWhileBusy();
			ionchambers.waitWhileBusy();
		} else {
			throw new Exception("Script/scan already running");
		}

		// read the latest frame
		int currentFrame = ionchambers.getCurrentFrame();
		if (currentFrame % 2 != 0)
			currentFrame--;

		if (currentFrame > 0) {
			currentFrame /= 2;
			currentFrame--;
		}

		// assumes an column called I0
		double[] ion_results = (double[]) ionchambers.readout();
		// Why do this - we already know the collection time, having just set it... imh
		Double collectionTime = (Double) ionchambers.getAttribute("collectionTime");

		String[] extraNames = ionchambers.getExtraNames();
		int i0Index = ArrayUtils.indexOf(extraNames, "I0");
		if (collectionTime != null) {
			ion_results[i0Index] /= collectionTime;
			ion_results[i0Index + 1] /= collectionTime;
			ion_results[i0Index + 2] /= collectionTime;
		}
		return new Double[] { ion_results[i0Index + 0], ion_results[i0Index + 1], ion_results[i0Index + 2] };
	}

	protected Double[] getXSpress2Data() throws DeviceException {
		logger.debug("Collect values from {}", xspress2Detector.getName());
		Double[] rates = (Double[]) xspress2Detector.getAttribute("liveStats");
		return rates;
	}

	protected Double[] getIonChambersForXmapI1() throws Exception {
		// Collect data in same way as regular xmap
		getIonChambersForXmap();

		// read 4th channel of ionchamber - this has counts for I1
		double[] ion_results = ionchambers.readFrame(4, 1, 0);
		return new Double[] {ion_results[0] / collectionTime};
	}

	protected Double[] getIonChambersForXmap() throws Exception {
		logger.debug("Collect values from {} and {}", ionchambers.getName(), xmapDetector.getName());

		if ( !getScriptOrScanIsRunning() && !xmapDetector.isBusy() && !ionchambers.isBusy() ){
			xmapDetector.collectData();
			ionchambers.setCollectionTime(collectionTime);
			ionchambers.collectData();
			ionchambers.clearFrameSets();
			ionchambers.waitWhileBusy();
			xmapDetector.stop();
			xmapDetector.waitWhileBusy();
		} else {
			throw new Exception("Script/scan already running");
		}

		// read the latest frame
		int currentFrame = ionchambers.getCurrentFrame();
		if (currentFrame % 2 != 0) {
			currentFrame--;
		}
		if (currentFrame > 0) {
			currentFrame /= 2;
			currentFrame--;
		}

		int numChannels = ionchambers.getExtraNames().length;
//		// works for TFG2 only where time if the first channel
		double[] ion_results = ionchambers.readFrame(1, numChannels, currentFrame);

//		double[] ion_results = (double[]) ionchambers.readout();
		Double collectionTime = (Double) ionchambers.getAttribute("collectionTime");
		int i0Index = -1;
		String[] eNames = ionchambers.getExtraNames();
		// find the index for I0
		for (String s : eNames) {
			i0Index++;
			if (s.equals("I0"))
				break;

		}
		if (collectionTime != null) {
			ion_results[i0Index] /= collectionTime;
			ion_results[i0Index + 1] /= collectionTime;
			ion_results[i0Index + 2] /= collectionTime;
		}
		return new Double[] { ion_results[i0Index + 0], ion_results[i0Index + 1], ion_results[i0Index + 2] };
	}

	private Double[] getXmapData() throws DeviceException {
		return (Double[]) xmapDetector.getAttribute("liveStats");
	}

	public XspressDetector getXspress2Detector() {
		return xspress2Detector;
	}

	public void setXspress2Detector(XspressDetector xspress2Detector) {
		this.xspress2Detector = xspress2Detector;
	}

	public CounterTimer getIonChambers() {
		return ionchambers;
	}

	public void setIonChambers(CounterTimer ionchambers) {
		this.ionchambers = ionchambers;
	}

	public XmapDetector getXmapDetector() {
		return xmapDetector;
	}

	public void setXmapDetector(XmapDetector xmapDetector) {
		this.xmapDetector = xmapDetector;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addIObserver(IObserver observer) {
	}

	@Override
	public void deleteIObserver(IObserver observer) {
	}

	@Override
	public void deleteIObservers() {
	}

	@Override
	public boolean getCollectionAllowed() {
		return collectionAllowed;
	}

	@Override
	public void setCollectionAllowed(boolean collectionAllowed) {
		this.collectionAllowed = collectionAllowed;
	}

	// ScannableBase overrides
	@Override
	public void asynchronousMoveTo(Object position) {
		return; // do nothing
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return null;
	}

	/**
	 * Set collectionAllowed flag to false and block until current collection has finished.
	 */
	@Override
	public void atScanStart() {
		logger.debug("atScanStart");
		collectionAllowed = false;
		waitWhileBusy();
	}

	@Override
	public boolean isBusy() {
		return collectionInProgress;
	}

	/**
	 * Wait for current collection to finish.
	 */
	@Override
	public void waitWhileBusy() {
		logger.debug("waitWhileBusy started");
		try {
			super.waitWhileBusy();
		} catch (DeviceException|InterruptedException e) {
			logger.error("Thread interrupted in waitWhileBusy", e);
		}
		logger.debug("waitWhileBusy finished");
	}

	/** Whether collection of detector rates is taking place and {@link #getIonChamberValues(COLLECTION_TYPES)} and
	 * {@link #getFluoDetectorCountRatesAndDeadTimes(COLLECTION_TYPES)}
	 * are being called periodically by gui thread.
	 * (i.e runCollection() method of MonitorViewBase is being run by a view) */
	private boolean collectionIsRunning;

	@Override
	public boolean getCollectionIsRunning() {
		return collectionIsRunning;
	}

	@Override
	public void setCollectionIsRunning(boolean collectionIsRunning) {
		this.collectionIsRunning = collectionIsRunning;
	}
}
