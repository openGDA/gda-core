package gda.device.detector.xmap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataChildNodeAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdata.NXDetectorSerialAppender;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.device.detector.xmap.edxd.NDHDF5PVProvider;
import gda.device.detector.xmap.util.XmapNexusFileLoader;
import gda.scan.ScanInformation;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;

/**
 * In the NXPlugin framework for NXDetectors, this reads the raw HDF5 files
 * produced by Xmap at the end of each row, extracts the ROIs and deadtime
 * corrcted MCAs.
 *
 * @author rjw82
 *
 */
public class XmapReadbackHdf5NXPlugin extends NullNXPlugin {

	final private EDXDMappingController xmap;
	final private int numberElements;
	final private List<Double> eventProcessingTimes;
	private ScanInformation scanInfo;
	private DetectorROI[] rois;
	private NDHDF5PVProvider ndHDF5PVProvider;
	private int nextRowToBeCollected;
	private int nextRowToBeRead;
	private File hdf5Folder;
	private String hdf5Prefix;
	private ArrayList<DetectorElement> detectorElements;

	public XmapReadbackHdf5NXPlugin(EDXDMappingController xmap,
			NDHDF5PVProvider ndHDF5PVProvider,List<Double> eventProcessingTimes) {
		this.xmap = xmap;
		this.ndHDF5PVProvider = ndHDF5PVProvider;
		this.numberElements = eventProcessingTimes.size();
		this.eventProcessingTimes = eventProcessingTimes;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection,
			ScanInformation scanInfo) throws Exception {
		this.scanInfo = scanInfo;
		nextRowToBeCollected = 0;
		nextRowToBeRead = 0;
		// captureStarted = false;

		// make a subfolder
		String dataDir = PathConstructor.createFromDefaultProperty();
		dataDir += scanInfo.getScanNumber();
		hdf5Folder = new File(dataDir);
		hdf5Folder.mkdirs();

		// tell the hdf writer the subfolder
		dataDir = dataDir.replace("/dls/i08/", "X:/");
		ndHDF5PVProvider.setFilePath(dataDir);

		// set the prefix to be the filename
		hdf5Prefix = "xmap-" + scanInfo.getScanNumber();
		xmap.setFilenamePrefix(hdf5Prefix);
	}

	@Override
	public void prepareForLine() throws Exception {
		xmap.setFileNumber(nextRowToBeCollected);
		xmap.startRecording();
		xmap.start();
	}

	@Override
	public void completeLine() throws Exception {
		// we need to block the main scan thread here as this is the only way to
		// know that the row has been completed and the Xmap is ready to move on
		// to the next row of the scan
		while (xmap.getCaptureStatus()) {
			Thread.sleep(100);
		}
		// TODO log here the time that the file is ready (so the next collection can start)
		nextRowToBeCollected++;
	}

	@Override
	public void atCommandFailure() throws Exception {
		xmap.endRecording();
	}

	@Override
	public List<String> getInputStreamNames() {
		// work this our from ROIs
		List<String> extraNames = new ArrayList<>();
		for (DetectorElement element : detectorElements) {
			extraNames.add(element.getName());
			for (DetectorROI roi : rois) {
				extraNames.add(element.getName() + "_" + roi.getRoiName());
			}
		}
		extraNames.add("FF");
		return extraNames;
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> extraNames = getInputStreamNames();
		List<String> formats = new ArrayList<>();
		for (int i = 0; i < extraNames.size(); i++) {
			formats.add("%.3f");
		}
		return formats;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead)
			throws NoSuchElementException, InterruptedException,
			DeviceException {

		File nextFileToRead;
		try {
			nextFileToRead = waitForNextHDF5();
		} catch (Exception e1) {
			throw new DeviceException("IO error talking to EPICS", e1);
		}

		XmapNexusFileLoader dataLoader = new XmapNexusFileLoader(
				nextFileToRead.getPath(), detectorElements.size());
		try {
			dataLoader.loadFile();
		} catch (Exception e) {
			throw new DeviceException("Exception reading raw Xmap HDF5 file", e);
		}

		ArrayList<NXDetectorDataAppender> output = new ArrayList<>();

		for (int i = 0; i < scanInfo.getDimensions()[1]; i++) {

			XmapNXDetectorDataCreator dataCreator = new XmapNXDetectorDataCreator(
					dataLoader, detectorElements, getInputStreamNames()
							.toArray(new String[] {}), getInputStreamFormats()
							.toArray(new String[] {}), getName(), true,
					eventProcessingTimes, true);

			NXDetectorData nexusData = dataCreator.writeToNexusFile(i,
					dataLoader.getData(i));

			NXDetectorDataChildNodeAppender nexusAppender = new NXDetectorDataChildNodeAppender(
					nexusData.getDetTree(getName()));

			NXDetectorDataDoubleAppender roiAppender = new NXDetectorDataDoubleAppender(
					getInputStreamNames(), Arrays.asList(nexusData
							.getDoubleVals()));

			ArrayList<NXDetectorDataAppender> appenders = new ArrayList<>();
			appenders.add(roiAppender);
			appenders.add(nexusAppender);

			NXDetectorSerialAppender serialAppender = new NXDetectorSerialAppender(
					appenders);

			output.add(serialAppender);
		}

		nextRowToBeRead++;
		return output;
	}

	/*
	 * For the thread reading back the data only.
	 */
	private File waitForNextHDF5() throws Exception {
		File expectedFile = deriveNextHDF5File();

		// xmap is clearly collecting a later file than what this is wanting to
		// read
		if (ndHDF5PVProvider.getFileNumber() > nextRowToBeRead + 1) {
			// if get here then readback is falling behind (which is not a
			// problem for this class)
			return expectedFile;
		} else {
			// else wait for the capture to finish as the xmap is currently
			// writing the file this thread is interested in
			while (xmap.getCaptureStatus() && ndHDF5PVProvider.getFileNumber() <= nextRowToBeRead + 1) {
				Thread.sleep(100);
			}
		}

		return expectedFile;
	}

	/*
	 * For the thread reading back the data only.
	 */
	private File deriveNextHDF5File() {
		String fileName = hdf5Folder + File.separator + hdf5Prefix + "-"
				+ nextRowToBeRead + ".hdf";
		return new File(fileName);
	}

	public DetectorROI[] getRois() {
		return rois;
	}

	public void setRois(DetectorROI[] rois) {
		this.rois = rois;

		detectorElements = new ArrayList<>();

		for (int i = 0; i < numberElements; i++) {
			DetectorElement newElement = new DetectorElement();
			newElement.setExcluded(false);
			newElement.setRegionList(Arrays.asList(rois));
			newElement.setNumber(i);
			newElement.setName("Element" + i);
			detectorElements.add(newElement);
		}
	}
}
