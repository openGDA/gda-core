/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.device.detector.xmap.edxd.NDHDF5PVProvider;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import uk.ac.gda.beans.DetectorROI;

/**
 * Drive the XIA Xmap card using hardware triggers, returning an MCA and Region of Interest totals at each point.
 * <p>
 * As the XIA Xmap card writes all data to HDF5, and that is the only way that data is available, this class can only
 * work by running the XMap for each row of a scan and returning the data at that row.
 * <p>
 * Each row is a new run, and produces a new file, for the XMap. So the raw Xmap HDF5 files will be written to their own
 * sub-directory and will not be linked from the GDA Nexus file.
 * <p>
 * This class will derive the ROI totals and so this must hold the detector's configuration for these.
 */
public class HardwareTriggeredWithOnlineDataReductionXmap extends XmapSimpleAcquire {

	private ScanInformation scanInfo;
	private DetectorROI[] rois;
	private NDHDF5PVProvider ndHDF5PVProvider;
	private int nextRowToBeCollected;
	private int pixelsReadSoFar;
	private XMapNXDetectorDataAppenderInputStream[] dataAppenders;

	public HardwareTriggeredWithOnlineDataReductionXmap(EDXDMappingController xmap,
			NDHDF5PVProvider nDHDF5PVProvider) throws DeviceException {
		super(xmap, -1);
		this.ndHDF5PVProvider = nDHDF5PVProvider;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		this.scanInfo = scanInfo;
		getXmap().setCollectionMode(COLLECTION_MODES.MCA_MAPPING);
		nextRowToBeCollected = 0;
		pixelsReadSoFar = -1;
		getXmap().setPixelsPerRun(scanInfo.getDimensions()[1]);
		ndHDF5PVProvider.setNumberOfPixels(scanInfo.getDimensions()[1]);
		ndHDF5PVProvider.setNumExtraDims(0);

		// make a subfolder
		String dataDir = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		dataDir += scanInfo.getScanNumber();
		File tempFolder = new File(dataDir);
		tempFolder.mkdirs();

		// tell the hdf writer the subfolder
		dataDir = dataDir.replace("X:", "/dls/i08/");
		ndHDF5PVProvider.setFilePath(dataDir);

		// set the prefix to be the filename
		String prefix = "xmap-"+scanInfo.getScanNumber();
		getXmap().setFilenamePrefix(prefix);

		dataAppenders = new XMapNXDetectorDataAppenderInputStream[scanInfo.getDimensions()[0]];
	}

	@Override
	public void prepareForLine() throws Exception {
		getXmap().setFileNumber(nextRowToBeCollected);
		getXmap().startRecording();
		getXmap().start();  // this might not need to be done here
	}

	@Override
	public void completeLine() throws Exception {
		String filename = getXmap().getHDFFileName();
		boolean isRecording = getXmap().getCaptureStatus();
		if (isRecording){
			throw new DeviceException("the line should have finished!");
		}

		dataAppenders[nextRowToBeCollected] = new XMapNXDetectorDataAppenderInputStream(filename,rois);
		nextRowToBeCollected++;
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}

	@Override
	public List<String> getInputStreamNames() {
		// work this our from ROIs
		List<String> extraNames = new Vector<>();
		for (DetectorROI roi : rois) {
			extraNames.add(roi.getRoiName());
		}
		extraNames.add("FF");
		return extraNames;
	}

	@Override
	public void atCommandFailure() throws Exception {
		getXmap().endRecording();
		getXmap().stop();
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> extraNames = getInputStreamNames();
		List<String> formats = new Vector<>();
		for(int i = 0; i < extraNames.size(); i++){
			formats.add("%.3f");
		}
		return formats;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {

		// FIXME error here when running map.
		int firstPixel = pixelsReadSoFar + 1;
		int rowSize = scanInfo.getDimensions()[1];
		int rowOfFirstPixel = (int) Math.floor(firstPixel / rowSize);

		if (rowOfFirstPixel < nextRowToBeCollected){
			return dataAppenders[rowOfFirstPixel].read(maxToRead);
		}
		throw new DeviceException("Data not collected yet");
	}

	public DetectorROI[] getRois() {
		return rois;
	}

	public void setRois(DetectorROI[] rois) {
		this.rois = rois;
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime)
			throws Exception {
		// do nothing here
	}
}
