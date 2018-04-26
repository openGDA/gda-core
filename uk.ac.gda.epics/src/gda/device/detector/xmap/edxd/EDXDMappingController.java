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

package gda.device.detector.xmap.edxd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.analyser.EpicsMCASimple;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.epicsdevice.ReturnType;
import gov.aps.jca.TimeoutException;

/**
 * This class describes the EDXD detector on I12, it is made up of 24 subdetectors:
 * EDXDMappingElement instances, each wrapping an EpicsMCASimple instance.
 */
public class EDXDMappingController extends EDXDController implements IEDXDMappingController {

	// Setup the logging facilities
	private static final Logger logger = LoggerFactory.getLogger(EDXDMappingController.class);

	private int elementOffset = 0;
	private boolean aquisitionTimeOn = false;

	private static final String AUTOPIXELSPERBUFFER = "AUTOPIXELSPERBUFFER";
	private static final String COLLECTMODE = "COLLECTMODE";
	private static final String ERASEALL = "ERASEALL";
	private static final String ERASESTART = "ERASESTART";
	private static final String IGNOREGATE = "IGNOREGATE";
	private static final String PIXELADVANCEMODE = "PIXELADVANCEMODE";
	private static final String PIXELSPERBUFFER ="PIXELSPERBUFFER";
	private static final String PIXELSPERRUN ="PIXELSPERRUN";
	private static final String STOPALL = "STOPALL";

	private static final String NEXUS_CAPTURE = "NEXUS:Capture";
	private static final String NEXUS_ENABLECALLBACKS = "NEXUS:EnableCallbacks";
	private static final String NEXUS_FILENAME = "NEXUS:FileName";
	private static final String NEXUS_FILENUMBER = "NEXUS:FileNumber";
	private static final String NEXUS_FILEPATH = "NEXUS:FilePath";
	private static final String NEXUS_FILETEMPLATE = "NEXUS:FileTemplate";
	private static final String NEXUS_FILEWRITEMODE = "NEXUS:FileWriteMode";
	private static final String NEXUS_TEMPLATEFILENAME = "TemplateFileName";
	private static final String NEXUS_TEMPLATEFILEPATH = "TemplateFilePath";

	protected NDFileHDF5 hdf5;

	// Add all the EDXD Elements to the detector (called by configure())
	@Override
	protected void addElements() {
		for (int i = (0 + elementOffset); i < (numberOfElements + elementOffset); i++)
			subDetectors.add(new EDXDMappingElement(xmap, i, new EpicsMCASimple()));
	}

	/**
	 * Sets the dynamic range of the detector
	 * @param dynamicRange the dynamic range in KeV
	 * @return the actual value which has been set
	 * @throws DeviceException
	 */
	@Override
	public double setDynamicRange(double dynamicRange) throws DeviceException {
		xmap.setValue(SETDYNRANGE, "", dynamicRange);
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE, GETDYNRANGE + elementOffset, "");
	}

	/**
	 * get the maximum number of ROI allowed per mca element
	 * @return number of rois
	 * @throws DeviceException
	 */
	@Override
	public int getMaxAllowedROIs() throws DeviceException {
		//TODO not sure about the number
		return 32;
	}

	/**
	 * Activate the ROI mode in the controller
	 * @throws DeviceException
	 */
	@Override
	public void activateROI() throws DeviceException {
		// Nothing to do
	}

	/**
	 * Disable the ROI mode in the Controller
	 *
	 * @throws DeviceException
	 */
	@Override
	public void deactivateROI() throws DeviceException {
		// Nothing to do
	}

	/**
	 * Start data acquisition in the controller. Uses the exisiting resume mode
	 * @throws DeviceException
	 */
	@Override
	public void start() throws DeviceException {
		xmap.setValueNoWait(ERASESTART, "", 1);
	}

	@Override
	public void stop() throws DeviceException {
		if (xmap != null && xmap.isConfigured()) {
			xmap.setValueNoWait(STOPALL, "", 1);
		}
	}

	/**
	 * Controller has two modes of operation.
	 * clear on start or resume acquiring into the same spectrum
	 * @param resume
	 * @throws DeviceException
	 */
	@Override
	public void setResume(boolean resume) throws DeviceException {
		if (!resume) {
			xmap.setValueNoWait(ERASEALL, "", 1);
		}
	}

	public void clear() throws DeviceException {
		xmap.setValueNoWait(ERASEALL, "", 1);
	}

	public void clearAndStart() throws DeviceException {
		xmap.setValueNoWait(ERASESTART, "", 1);
	}

	@Override
	public void setCollectionMode(COLLECTION_MODES mode) throws DeviceException{
		xmap.setValueNoWait(COLLECTMODE, "", mode.ordinal());
	}

	@Override
	public void setAquisitionTime(double collectionTime) throws DeviceException {
		if (aquisitionTimeOn) {
			super.setAquisitionTime(collectionTime);
		}

		// removed as some versions of Epics interface does not have this SETPRESETREAL
		// xmap.setValue(SETPRESETREAL ,"",collectionTime);
	}

	public void setPixelAdvanceMode(PIXEL_ADVANCE_MODE mode) throws DeviceException {
		xmap.setValueNoWait(PIXELADVANCEMODE, "", mode.ordinal());
	}

	@Override
	public void setIgnoreGate(boolean yes) throws DeviceException {
		final int value = yes ? 1 : 0;
		xmap.setValueNoWait(IGNOREGATE, "", value);
	}

	public void setAutoPixelsPerBuffer(boolean auto) throws DeviceException {
		final int value = auto ? 1 : 0;
		xmap.setValueNoWait(AUTOPIXELSPERBUFFER, "", value);
	}

	public void setPixelsPerBuffer(int number) throws DeviceException {
		xmap.setValueNoWait(PIXELSPERBUFFER, "", number);
	}

	public void setPixelsPerRun(int number) throws DeviceException {
		xmap.setValue(PIXELSPERRUN, "", number);
	}

	public int getPixelsPerRun() throws DeviceException {
		return (int) xmap.getValue(ReturnType.DBR_NATIVE, PIXELSPERRUN, "");
	}

	// hdf5 commands
	public void resetCounters() throws Exception {
		hdf5.getFile().getPluginBase().setDroppedArrays(0);
		hdf5.getFile().getPluginBase().setArrayCounter(0);
	}

	public void startRecording() throws Exception {
		if (hdf5.getCapture() == 1) {
			throw new DeviceException("detector found already saving data when it should not be");
		}
		hdf5.startCapture();
		final int totalmillis = 60 * 1000;
		final int grain = 25;
		for (int i = 0; i < totalmillis / grain; i++) {
			if (hdf5.getCapture() == 1) {
				return;
			}
			Thread.sleep(grain);
		}
		throw new TimeoutException("Timeout waiting for hdf file creation.");
	}

	public void endRecording() throws Exception {
		// writing the buffers can take a long time
		final int totalmillis = 1* 1000;
		final int grain = 25;
		for (int i = 0; i < totalmillis/grain; i++) {
			if (hdf5.getFile().getCapture_RBV() == 0) {
				return;
			}
			Thread.sleep(grain);
		}
		hdf5.stopCapture();
		logger.warn("Waited very long for hdf writing to finish, still not done. Hope all we be ok in the end.");
		if (hdf5.getFile().getPluginBase().getDroppedArrays_RBV() > 0) {
			throw new DeviceException("sorry, we missed some frames");
		}
	}

	public String getHDFFileName() throws Exception {
		return hdf5.getFullFileName_RBV();
	}

	public void setDirectory(String dataDir) throws Exception {
		hdf5.setFilePath(dataDir);
		if (!hdf5.getFile().filePathExists()) {
			throw new DeviceException("Path does not exist on IOC '" + dataDir + "'");
		}
	}

	public void setFileNumber(Number scanNumber) throws Exception {
		hdf5.setFileNumber(scanNumber.intValue());
	}

	public void setFilenamePrefix(String beamline) throws Exception {
		hdf5.setFileName(beamline);
	}

	public void setFilenamePostfix(String name) throws Exception {
		hdf5.setFileTemplate(String.format("%%s%%s-%%d-%s.h5", name));
	}

	//Nexus related commands
	public void setNexusCapture(int number) throws DeviceException {
		xmap.setValueNoWait(NEXUS_CAPTURE, "", number);
	}

	public void setHdfNumCapture(int number) throws DeviceException {
		try {
			hdf5.setNumCapture(number);
		} catch (Exception e) {
			throw new DeviceException("Error setting hdf5 Numcapture", e);
		}
	}

	public void setNexusFileFormat(String format) throws DeviceException {
		xmap.setValueNoWait(NEXUS_FILETEMPLATE, "", format);
	}

	public void setFileWriteMode(NEXUS_FILE_MODE mode) throws DeviceException {
		xmap.setValueNoWait(NEXUS_FILEWRITEMODE, "", mode.ordinal());
	}

	public void setCallback(boolean yes) throws DeviceException {
		final int value = yes ? 1 : 0;
		xmap.setValueNoWait(NEXUS_ENABLECALLBACKS, "", value);
	}

	public void setNexusFileName(String filename) throws DeviceException {
		xmap.setValueNoWait(NEXUS_FILENAME, "", filename);
	}

	public String getNexusFileName() throws DeviceException {
		return xmap.getValueAsString(NEXUS_FILENAME, "");
	}

	public String getNexusFilePath() throws DeviceException {
		return xmap.getValueAsString(NEXUS_FILEPATH, "");
	}

	public void setNexusFilePath(String filepath) throws DeviceException {
		xmap.setValueNoWait(NEXUS_FILEPATH, "", filepath);
	}

	public int getFileNumber() throws DeviceException {
		return (int) xmap.getValue(ReturnType.DBR_NATIVE, NEXUS_FILENUMBER, "");
	}

	public void setTemplateFileName(String templateFileName) throws DeviceException {
		xmap.setValueNoWait(NEXUS_TEMPLATEFILENAME, "", templateFileName);
	}

	public void setTemplateFilePath(String tempFilePath) throws DeviceException {
		xmap.setValueNoWait(NEXUS_TEMPLATEFILEPATH, "", tempFilePath);
	}

	public boolean getCaptureStatus() {
		return hdf5.getStatus() == 1;
	}

	public boolean isBufferedArrayPort() throws Exception {
		return hdf5.getArrayPort().equals("xbuf");
	}

	public NDFileHDF5 getHdf5() {
		return hdf5;
	}

	public void setHdf5(NDFileHDF5 hdf5) {
		this.hdf5 = hdf5;
	}

	public int getElementOffset() {
		return elementOffset;
	}

	public void setElementOffset(int elementOffset) {
		this.elementOffset = elementOffset;
	}

	public boolean getAquisitionTimeOn() {
		return aquisitionTimeOn;
	}

	public void setAquisitionTimeOn(boolean aquisitionTimeOn) {
		this.aquisitionTimeOn = aquisitionTimeOn;
	}

}
