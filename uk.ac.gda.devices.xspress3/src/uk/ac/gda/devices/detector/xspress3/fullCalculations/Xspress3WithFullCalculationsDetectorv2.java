/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.devices.detector.xspress3.Xspress3;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;

/**
 * Performs all data reductions at the GDA level (e.g. ROIs, all element sum)
 * <p>
 * A version of Xspress3Detector in which EPICS is used to simply record all the deadtime corrected MCAs to file. Only at the end of a scan (or a line in a
 * multidimensional scan) is the temporary HDF5 file written by EPICS readout and all the data reduced and correctly stored in a NexusTreeProvider object. This
 * class is used for testing Xspress3 v2 and will replace in the long run Xspress3WithFullCalculationsDetector. A new class was needed in order not to interfere
 * with other beamlines that are using Xspress3.
 *
 * @author rjw82
 */
public class Xspress3WithFullCalculationsDetectorv2 extends DetectorBase implements Xspress3 {

	private static final int MCA_SIZE = 4096;
	private static final int MAX_NUMBER_OF_ROIS = Integer.MAX_VALUE; // calculations are done in software so no limit to number of ROIs

	Xspress3Controller controller;
	private Xspress3ScanOperationsv2 scanOperations;
	private Xspress3DataOperationsv2 dataOperations;
	private int firstChannelToRead = 0;
	private boolean readDataFromFile = false;

	public Xspress3WithFullCalculationsDetectorv2() {
	}

	@Override
	public void configure() throws gda.factory.FactoryException {
		scanOperations = new Xspress3ScanOperationsv2(controller, getName());
		dataOperations = new Xspress3DataOperationsv2(controller, firstChannelToRead);
	}

	@Override
	public void atScanStart() throws DeviceException {
		// cannot tell if we are running a step scan or another type where file
		// writing will be necessary, so need an attribute which is set
		// externally to this class.
		scanOperations.atScanStart(readDataFromFile);
		dataOperations.atScanStart(readDataFromFile);
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		scanOperations.atScanLineStart();
		dataOperations.atScanLineStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		stop();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		atScanEnd();
	}

	@Override
	public void atPointEnd() throws DeviceException {
		dataOperations.atPointEnd();
	}

	@Override
	public void stop() throws DeviceException {
		scanOperations.atScanEnd();
		dataOperations.atScanEnd();
	}

	@Override
	public void collectData() throws DeviceException {
		// do nothing here as the detector is passive: it sets up time frames at
		// the start of the scan and then is triggered by a TFG (or another
		// detector driving the TFG) which must be
		// included in the same scan
	}

	@Override
	public int getStatus() throws DeviceException {
		int controllerStatus = controller.getStatus();
		if (controllerStatus == Detector.FAULT || controllerStatus == Detector.STANDBY) {
			return controllerStatus;
		}
		// This detector class is completely passive and is dependent on being
		// triggered by the TFG, so always return idle and let whatever other
		// Detector object driving the TFG to control the status
		return Detector.IDLE;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		return dataOperations.readoutLatest(getName());
	}

	/*
	 * The detectorName is the string used in the Nexus tree returned. Allows for composition where the xspress3 is a component of another detector e.g.
	 * Xspress3BufferedDetector
	 */
	public NXDetectorData[] readFrames(int startFrame, int finalFrame, String detectorName) throws DeviceException {
		return dataOperations.readoutFrames(startFrame, finalFrame, detectorName);
	}

	@Override
	public NXDetectorData[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		return readFrames(startFrame, finalFrame, getName());
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	@Deprecated
	public int[][] getMCData(double time) throws DeviceException {
		return dataOperations.getMCData(time);
	}

	@Override
	public double[][] getMCAData(double time) throws DeviceException {
		return dataOperations.getMCAData(time);
	}

	@Override
	public void clearAndStart() throws DeviceException {
		scanOperations.clearAndStart();
	}

	@Override
	public String getConfigFileName() {
		return dataOperations.getConfigFileName();
	}

	@Override
	public void setConfigFileName(String configFileName) {
		dataOperations.setConfigFileName(configFileName);
	}

	@Override
	public void loadConfigurationFromFile() throws Exception {
		dataOperations.loadConfigurationFromFile();
	}

	@Override
	public Xspress3Controller getController() {
		return controller;
	}

	public void setController(Xspress3Controller controller) {
		this.controller = controller;
	}

	@Override
	public double readoutFF() throws DeviceException {
		return dataOperations.readoutFF();
	}

	@Override
	public String[] getExtraNames() {
		return dataOperations.getExtraNames();
	}

	@Override
	public String[] getOutputFormat() {
		return dataOperations.getOutputFormat();
	}

	public boolean isReadDataFromFile() {
		return readDataFromFile;
	}

	public void setReadDataFromFile(boolean readDataFromFile) {
		this.readDataFromFile = readDataFromFile;
	}

	/**
	 * @deprecated Use getConfigurationParameters() instead
	 */
	@Override
	@Deprecated
	public DetectorROI[] getRegionsOfInterest() throws DeviceException {
		return dataOperations.getRegionsOfInterest();
	}

	/**
	 * @deprecated Use applyConfigurationParameters() instead
	 */
	@Override
	@Deprecated
	public void setRegionsOfInterest(DetectorROI[] regionList) throws DeviceException {
		dataOperations.setRegionsOfInterest(regionList);
	}

	@Override
	public int getNumberOfElements() {
		return controller.getNumberOfChannels();
	}

	@Override
	public int getMCASize() {
		return MCA_SIZE;
	}

	@Override
	public int getMaxNumberOfRois() {
		return MAX_NUMBER_OF_ROIS;
	}

	@Override
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception {
		dataOperations.applyConfigurationParameters(parameters);
	}

	@Override
	public FluorescenceDetectorParameters getConfigurationParameters() {
		Xspress3Parameters parameters = dataOperations.getConfigurationParameters();
		parameters.setDetectorName(getName());
		return parameters;
	}

}

