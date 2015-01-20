package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import uk.ac.gda.devices.detector.xspress3.Xspress3;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;

/**
 * Performs all data reductions at the GDA level (e.g. ROIs, all element sum)
 * <p>
 * A version of Xspress3Detector in which EPICS is used to simply record all the
 * deadtime corrected MCAs to file. Only at the end of a scan (or a line in a
 * multidimensional scan) is the temporary HDF5 file written by EPICS readout
 * and all the data reduced and correctly stored in a NexusTreeProvider object.
 * 
 * @author rjw82
 * 
 */
public class Xspress3WithFullCalculationsDetector extends DetectorBase implements Xspress3 {

	Xspress3Controller controller;
	private Xspress3ScanOperations scanOperations;
	private Xspress3DataOperations dataOperations;
	private int firstChannelToRead = 0;
	private boolean readDataFromFile = false;

	public Xspress3WithFullCalculationsDetector() {
	}

	public void configure() throws gda.factory.FactoryException {
		scanOperations = new Xspress3ScanOperations(controller, getName());
		dataOperations = new Xspress3DataOperations(controller, getName(), firstChannelToRead);
	}

	public void atScanStart() throws DeviceException {
		// cannot tell if we are running a step scan or another type where file
		// writing will be necessary, so need an attribute which is set
		// externally to this class.
		scanOperations.atScanStart(readDataFromFile);
		dataOperations.atScanStart(readDataFromFile);
	}

	public void atScanLineStart() throws DeviceException {
		scanOperations.atScanLineStart();
		dataOperations.atScanLineStart();
	}

	public void atScanEnd() throws DeviceException {
		scanOperations.atScanEnd();
	}

	@Override
	public void atPointEnd() throws DeviceException {
		dataOperations.atPointEnd();
	}

	@Override
	public void stop() throws DeviceException {
		controller.doStop();
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
		return dataOperations.readoutLatest();
	}

	@Override
	public NXDetectorData[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		return dataOperations.readoutFrames(startFrame, finalFrame);
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public double[][] getMCData(double time) throws DeviceException {
		return dataOperations.getMCData(time);
	}

	@Override
	public int[][] getData() throws DeviceException {
		return dataOperations.getData();
	}

	@Override
	public void clearAndStart() throws DeviceException {
		scanOperations.clearAndStart();
	}

	@Override
	public Object getCountRates() throws DeviceException {
		// is this ever called??? Should it be removed from the interface???
		return null;
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

}
