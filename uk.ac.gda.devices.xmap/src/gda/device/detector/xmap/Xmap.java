/*-
 * Copyright © 2012 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector.xmap;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.Timer;
import gda.device.XmapDetector;
import gda.device.detector.DetectorBase;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.observable.IObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.util.CorrectionUtils;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Implementation of the Vortex XMAP detector. XMAP detector works in two modes. FAst MApping mode and Normal mode. This
 * implementation is only for the Normal mode.
 */
public class Xmap extends DetectorBase implements XmapDetector, Detector, Scannable, Configurable, IObserver {
	private static final long serialVersionUID = 2780213150490777588L;

	private static final Logger logger = LoggerFactory.getLogger(Xmap.class);

	public double elapsedRealTimeValue;

	private String name;
	private String xmapControllerName;
	protected XmapController controller;
	private String configFileName;

	protected VortexParameters vortexParameters;
	protected Timer tfg;
	// flag to temporarily stop performing deadtime corrections (for diagnostic purposes).
	protected boolean saveRawSpectrum = false;
	/**
	 * These are the fit parameters used in dead time correction.
	 */
	protected List<Double> eventProcessingTimes;

	public Xmap() {
		this.inputNames = new String[0];
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (controller == null) {
				if ((controller = (XmapController) Finder.getInstance().find(xmapControllerName)) != null) {
					logger.debug("controller {} found", xmapControllerName);
				} else {
					logger.error("XmapController {} not found", xmapControllerName);
					throw new FactoryException("XmapController " + xmapControllerName + " not found");
				}
			}

			try {
				loadConfigurationFromFile();
			} catch (Exception e) {
				throw new FactoryException("Cannot load xml file " + getConfigFileName(), e);
			}

			controller.addIObserver(this);

			configured = true;
		}
	}

	/**
	 * Call this method to ask the detector to load the current template XML file. If the detector was configured
	 * without a configFileName set, then nothing will happen.
	 *
	 * @throws Exception
	 */
	public void loadConfigurationFromFile() throws Exception {
		if (getConfigFileName() == null)
			return;

		this.vortexParameters = (VortexParameters) XMLHelpers.createFromXML(VortexParameters.mappingURL,
				VortexParameters.class, VortexParameters.schemaURL, getConfigFileName());
		// Number of ROIs defined in XML file.
		configureRegionsOfInterest(vortexParameters);
		configureChannelLabels(vortexParameters);
	}

	protected List<String> channelLabels = Collections.emptyList();

	private boolean inAScan;

	/**
	 * Returns the region of interest names based on the first elements ROI names.
	 *
	 * @param vp
	 */
	protected void configureChannelLabels(VortexParameters vp) {
		channelLabels = new ArrayList<String>(7);

		int roiNum = 0;
		for (DetectorROI roi : vp.getDetectorList().get(0).getRegionList()) {
			String name = roi.getRoiName();
			if (name == null)
				name = "ROI " + roiNum;
			channelLabels.add(name);
			++roiNum;
		}

	}

	/**
	 * Works for different regions in each element.
	 *
	 * @param vp
	 * @throws Exception
	 */
	protected void configureRegionsOfInterest(final VortexParameters vp) throws Exception {

		try {
			final List<DetectorElement> dl = vp.getDetectorList();

			int mcaIndex = 0;
			for (DetectorElement e : dl) {
				final List<DetectorROI> regions = e.getRegionList();
				final double[][] rois = new double[regions.size()][2];
				int iregion = 0;
				for (DetectorROI roi : regions) {
					rois[iregion][0] = roi.getRoiStart();
					rois[iregion][1] = roi.getRoiEnd();
					++iregion;
				}
				setROI(rois, mcaIndex);
				++mcaIndex;
			}
		} catch (DeviceException e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Cannot configure vortex regions of interest.", e);
		}
	}

	/**
	 * Retuns the label or 'ROI <i>N</i>' if the user did not specify an roi for the channel.
	 */
	@Override
	public List<String> getChannelLabels() {
		return channelLabels;
	}

	@Override
	public void collectData() throws DeviceException {
		this.clearAndStart();
	}

	@Override
	public int getStatus() throws DeviceException {
		// if in a scan then
		if (tfg != null && inAScan){
			return tfg.getStatus();
		}
		return controller.getStatus();
	}


	@Override
	public void atScanStart() throws DeviceException {
		inAScan = true;
		controller.stop();
		super.atScanStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		inAScan = false;
		super.atScanEnd();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		inAScan = false;
		super.atCommandFailure();
	}

	/**
	 * @return Returns the tfg.
	 */
	public Timer getTfg() {
		return tfg;
	}

	/**
	 * @param tfg
	 *            The tfg to set.
	 */
	public void setTfg(Timer tfg) {
		this.tfg = tfg;
	}

	@Override
	public Object readout() throws DeviceException {
		return controller.getData();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int getNumberOfMca() {
		int nMca = 1;
		try {
			nMca = controller.getNumberOfElements();
		} catch (DeviceException e) {
			logger.error("Cannot get number of ROIs", e);
		}
		return nMca;
	}

	/**
	 * @param numberOfMca
	 */
	public void setNumberOfMca(int numberOfMca) {
		try {
			controller.setNumberOfElements(numberOfMca);
		} catch (DeviceException e) {
			logger.error("Cannot set number of MCAs", e);
		}
	}

	@Override
	public void clear() throws DeviceException {
	}

	@Override
	public void clearAndStart() throws DeviceException {
		controller.clearAndStart();
		try {
			Thread.sleep(15);
		} catch (InterruptedException e) {
			throw new DeviceException("interruption during clear and start");
		}
	}

	@Override
	public double getAcquisitionTime() throws DeviceException {
		return controller.getAcquisitionTime();
	}

	@Override
	public double getCollectionTime() {
		try {
			return getAcquisitionTime();
		} catch (DeviceException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int[] getData(int mcaNumber) throws DeviceException {
		try {
			waitWhileControllerBusy();
		} catch (InterruptedException e) {
			throw new DeviceException("InterruptedException while waiting for Xmap controller to return to Done state",e);
		}
		return controller.getData(mcaNumber);

	}

	@Override
	public int[][] getData() throws DeviceException {
		try {
			waitWhileControllerBusy();
		} catch (InterruptedException e) {
			throw new DeviceException("InterruptedException while waiting for Xmap controller to return to Done state",e);
		}
		return controller.getData();
	}

	@Override
	public int getNumberOfBins() throws DeviceException {
		return controller.getNumberOfBins();
	}

	@Override
	// does not work
	public double getReadRate() throws DeviceException {
		return controller.getReadRate();

	}

	@Override
	public double getRealTime() throws DeviceException {
		return controller.getRealTime();

	}

	@Override
	public double getStatusRate() throws DeviceException {
		return controller.getStatusRate();

	}

	/**
	 * @return xmap controller name
	 */
	public String getXmapControllerName() {
		return xmapControllerName;
	}

	/**
	 * @param xmapControllerName
	 */
	public void setXmapControllerName(String xmapControllerName) {
		this.xmapControllerName = xmapControllerName;
	}

	/**
	 * @return Returns the controller.
	 */
	public XmapController getController() {
		return controller;
	}

	/**
	 * @param controller
	 *            The controller to set.
	 */
	public void setController(XmapController controller) {
		this.controller = controller;
	}

	@Override
	public void setAcquisitionTime(double time) throws DeviceException {
		controller.setAcquisitionTime(time);
	}

	@Override
	public void setCollectionTime(double collectionTime) {
		try {
			setAcquisitionTime(collectionTime);
		} catch (DeviceException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setNumberOfBins(int numberOfBins) throws DeviceException {
		controller.setNumberOfBins(numberOfBins);
	}

	/**
	 * sets a new status update rate
	 *
	 * @param value
	 * @throws DeviceException
	 */
	public void setStatusRate(String value) throws DeviceException {
		controller.setStatusRate(value);
	}

	/**
	 * sets a new read update rate for DlsMcsSIS3820.
	 *
	 * @param value
	 * @throws DeviceException
	 */
	public void setReadRate(String value) throws DeviceException {
		controller.setReadRate(value);

	}

	@Override
	public void setReadRate(double readRate) throws DeviceException {
		controller.setReadRate(readRate);
	}

	@Override
	public void setStatusRate(double statusRate) throws DeviceException {
		controller.setStatusRate(statusRate);

	}

	@Override
	public void start() throws DeviceException {
		controller.start();
	}

	@Override
	public void stop() throws DeviceException {
		controller.stop();
	}

	/**
	 * @return Returns the numberOfROIs.
	 */
	@Override
	public int getNumberOfROIs() {
		return controller.getNumberOfROIs();
	}

	/**
	 * @param numberOfROIs
	 *            The numberOfROIs to set.
	 */
	public void setNumberOfROIs(int numberOfROIs) {
		controller.setNumberOfROIs(numberOfROIs);
	}

	/**
	 * This method cannot currently deal with elements which are disabled.
	 */
	@Override
	public double[] getROIsSum() throws DeviceException {
		return controller.getROIsSum();
	}

	/**
	 * Returns a count for each mca for a given roi number For instance if roi=0 the first roi. If a given element is
	 * disabled the counts for it will be set to zero. Therefore when setting up ROIs this method should not be used. It
	 * is just for the data collection. The VortexParameters UI will plot all MCA counts.
	 *
	 * @param roiIndex
	 * @return double array of roi count for all mca
	 * @throws DeviceException
	 */
	@Override
	public double[] getROICounts(int roiIndex) throws DeviceException {
		final double[] countsPerMCA = controller.getROICounts(roiIndex);
		/*
		 * for (int i = 0; i < countsPerMCA.length; i++) { try { final DetectorElement ele =
		 * vortexParameters.getDetectorList().get(i); if (ele.isExcluded()) countsPerMCA[i] = 0; } catch
		 * (IndexOutOfBoundsException e) { // Happens if the XML file has different element count to that in EPICS //
		 * This is allowed but elements not in the XML are also treated as having // no counts. countsPerMCA[i] = 0; } }
		 */

		return countsPerMCA;
	}

	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		controller.setROIs(rois);
	}

	/**
	 * @param rois
	 * @param mcaIndex
	 * @throws DeviceException
	 */
	public void setROI(double[][] rois, int mcaIndex) throws DeviceException {
		controller.setROI(rois, mcaIndex);

	}

	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {
		controller.setNthROI(rois, roiIndex);

	}

	@Override
	public void update(Object theObserved, Object changeCode) {
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		int[] dims = { getNumberOfROIs() + 1 };
		return dims;
	}

	/**
	 * @return the configFileName
	 */
	public String getConfigFileName() {
		return configFileName;
	}

	/**
	 * @param configFileName
	 *            the configFileName to set
	 */
	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	/**
	 * Reads out a sum of all regions and dead time corrects as it goes.
	 */
	@Override
	public double readoutScalerData() throws DeviceException {

		Double[] rois = readoutScalers();

		double ff = 0;
		for (double roi : rois) {
			ff += roi;
		}

		return ff;
	}

	public Double[] readoutScalers() throws DeviceException {

		if (controller.getStatus() == Detector.BUSY) {

			// We must call stop before reading out.
			controller.stop();

			// We must wait here if the controller is still busy.
			int total = 0;
			while (controller.getStatus() == Detector.BUSY) {
				try {
					Thread.sleep(100);
					total += 100;
					// We don't wait more than 5seconds.
					if (total >= 5000)
						break;
				} catch (InterruptedException e) {
					logger.error("Sleep interrupted", e);
				}
			}
		}

		// NOTE A single region cannot be too large. The entire
		// MCA is needed for the deadtime probably. This cannot
		// be read from a region.

		// check that rois have been set!
		final int numRois = getNumberOfROIs();
		if (numRois == 0) {
			throw new DeviceException("ROIs have not been set!");
		}

		// Get the dead time correction factors
		final double[] k = new double[vortexParameters.getDetectorList().size()];
		for (int i = 0; i < k.length; i++) {
			Double correctionFactor = getK(i);
			if (correctionFactor.isInfinite() || correctionFactor.isNaN() || correctionFactor == 0.0) {
				correctionFactor = 1.0;
			}
			k[i] = correctionFactor;
		}

		// Correct mca counts using K as we go
		double[][] allElementROIs = new double[controller.getNumberOfElements()][numRois];
		for(int elementCt =0; elementCt < allElementROIs.length;elementCt++)
		{
			allElementROIs[elementCt] = controller.getROIs(elementCt);
		}
		Double[] rois = new Double[numRois];
		for (int i = 0; i < rois.length; i++) {
			rois[i] = 0.0;
			//final double[] mcas = getROICounts(i);
			for (int j = 0; j < allElementROIs.length; j++) {
				if (j>=vortexParameters.getDetectorList().size()) continue;
				DetectorElement element = vortexParameters.getDetectorList().get(j);
				if (element.isExcluded()) continue;
				double correctedMCA = allElementROIs[j][i]*k[j];
				rois[i]+=correctedMCA;
			}
 		}
		/*Double[] rois = new Double[numRois];
		for (int i = 0; i < rois.length; i++) {
			rois[i] = 0.0;
			final double[] mcas = getROICounts(i);
			for (int j = 0; j < mcas.length; j++) {
				if (j >= vortexParameters.getDetectorList().size())
					continue;
				DetectorElement element = vortexParameters.getDetectorList().get(j);
				if (element.isExcluded())
					continue;
				double correctedMCA = mcas[j] * k[j];
				rois[i] += correctedMCA;
			}

 		}*/
		return rois;
	}

	/**
	 * Reads the fast filter rate for the element and calculates the K scaling factor used in the dead time correction.
	 *
	 * @param element
	 * @return K
	 */
	protected double getK(int element) throws DeviceException {

		final double ffr = controller.getICR(element);
		final double sfr = controller.getOCR(element);
		final double ppdt = eventProcessingTimes.get(element);
		return CorrectionUtils.getK(ppdt, ffr, sfr);
	}

	/**
	 * @return Returns the eventProcessingTimes.
	 */
	public List<Double> getEventProcessingTimes() {
		return eventProcessingTimes;
	}

	/**
	 * @param eventProcessingTimes
	 *            The eventProcessingTimes to set.
	 */
	public void setEventProcessingTimes(List<Double> eventProcessingTimes) {
		this.eventProcessingTimes = eventProcessingTimes;
	}

	/**
	 * @param roiIndex
	 * @return deadtime corrected ROICounts
	 * @throws DeviceException
	 */
	public double[] getDeadTimeCorrectedROICounts(int roiIndex) throws DeviceException {
		double roiCounts[] = this.getROICounts(roiIndex);
		for (int i = 0; i < roiCounts.length; i++) {
			roiCounts[i] = roiCounts[i] * getK(i);
		}
		return roiCounts;
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if (attributeName.equals("liveStats")) {
			return calculateLiveStats();
		} else if (attributeName.equals("countRates")) {
			return getCountRates();
		}
		return null;
	}

	public boolean isSaveRawSpectrum() {
		return saveRawSpectrum;
	}

	public void setSaveRawSpectrum(boolean saveawSpectrum) {
		this.saveRawSpectrum = saveawSpectrum;
	}

	public void waitWhileControllerBusy() throws DeviceException, InterruptedException {
		if (controller.getStatus() != 0) {
			logger.warn("getData() called when getData is not idle. Waiting for it to be idle before returning results");
			while (controller.getStatus() != 0) {
				Thread.sleep(100);
			}
			logger.warn("Now idle, results can be readout.");
		}
	}

	/**
	 * @return double[] - for every element, return the total count rate, deadtime correction factor, in window counts
	 * @throws DeviceException
	 */
	private Object calculateLiveStats() throws DeviceException {
		Double results[] = new Double[3 * this.getNumberOfMca()];
		int noOfMca = this.getNumberOfMca();
		int nRois = this.getNumberOfROIs();
		double[] inWindowCounts = new double[this.getNumberOfMca()];
		for (int k = 0; k < nRois; k++) {
			double[] rois = this.getROICounts(k);
			for (int f = 0; f < rois.length; f++) {
				inWindowCounts[f] += rois[f];
			}

		}
		for (int i = 0; i < noOfMca; i++) {
			results[i * 3] = controller.getICR(i);
			results[i * 3 + 1] = getK(i);
			results[i * 3 + 2] = inWindowCounts[i];
		}
		return results;
	}

	public Object getCountRates() throws DeviceException {
		Double results[] = new Double[2 * this.getNumberOfMca()];
		int noOfMca = this.getNumberOfMca();
		for (int i = 0; i < noOfMca; i++) {
			results[i * 2] = controller.getICR(i);
			results[i * 2 + 1] = controller.getOCR(i);
		}
		return results;
	}
}
