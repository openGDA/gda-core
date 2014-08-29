package gda.device.detector.xmap;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.VortexROI;

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
	private VortexROI[] rois;
	private NDHDF5PVProvider ndHDF5PVProvider;
	private int nextRowToBeCollected;
	private File hdf5Folder;
	private String hdf5Prefix;
	private ArrayList<DetectorElement> detectorElements;

	public XmapReadbackHdf5NXPlugin(EDXDMappingController xmap, NDHDF5PVProvider ndHDF5PVProvider, int numberElements, List<Double> eventProcessingTimes)
			throws DeviceException {
		this.xmap = xmap;
		this.ndHDF5PVProvider = ndHDF5PVProvider;
		this.numberElements = numberElements;
		this.eventProcessingTimes = eventProcessingTimes;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection,
			ScanInformation scanInfo) throws Exception {
		this.scanInfo = scanInfo;
		nextRowToBeCollected = 0;

		// make a subfolder
		String dataDir = PathConstructor.createFromDefaultProperty();
		dataDir += scanInfo.getScanNumber();
		hdf5Folder = new File(dataDir);
		hdf5Folder.mkdirs();

		// tell the hdf writer the subfolder
		dataDir = dataDir.replace("X:", "/dls/i08/");
		ndHDF5PVProvider.setFilePath(dataDir);

		// set the prefix to be the filename
		hdf5Prefix = "xmap-" + scanInfo.getScanNumber();
		xmap.setFilenamePrefix(hdf5Prefix);
	}

	@Override
	public void prepareForLine() throws Exception {
		xmap.setFileNumber(nextRowToBeCollected);
		xmap.startRecording();
	}

	@Override
	public void completeLine() throws Exception {
		// we need to block the main scan thread here as this is the only way to
		// know that the row has been completed and the Xmap is ready to move on
		// to the next row of the scan
		waitForNextHDF5();
		nextRowToBeCollected++;
	}

	@Override
	public void atCommandFailure() throws Exception {
		xmap.endRecording();
	}

	@Override
	public List<String> getInputStreamNames() {
		// work this our from ROIs
		List<String> extraNames = new Vector<String>();
		for (VortexROI roi : rois) {
			extraNames.add(roi.getRoiName());
		}
		extraNames.add("FF");
		return extraNames;
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> extraNames = getInputStreamNames();
		List<String> formats = new Vector<String>();
		for (int i = 0; i < extraNames.size(); i++) {
			formats.add("%.3f");
		}
		return formats;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead)
			throws NoSuchElementException, InterruptedException,
			DeviceException {

		File nextFileToRead = waitForNextHDF5();

		
		XmapNexusFileLoader dataLoader = new XmapNexusFileLoader(nextFileToRead.getPath(),4);
		try {
			dataLoader.loadFile();
		} catch (Exception e) {
			throw new DeviceException("Exception reading raw Xmap HDF5 file",e);
		}
		
		ArrayList<NXDetectorDataAppender> output = new ArrayList<NXDetectorDataAppender>();

		for (int i = 0; i < scanInfo.getDimensions()[1]; i++) {
			
			XmapNXDetectorDataCreator dataCreator = new XmapNXDetectorDataCreator(
					dataLoader, detectorElements, getInputStreamNames()
							.toArray(new String[] {}), getInputStreamFormats()
							.toArray(new String[] {}), getName(), true,
					eventProcessingTimes, true);

			NXDetectorData nexusData = dataCreator.writeToNexusFile(i, dataLoader.getData(i));
	
			NXDetectorDataChildNodeAppender nexusAppender = new NXDetectorDataChildNodeAppender(nexusData.getDetTree(getName()));
			
			NXDetectorDataDoubleAppender roiAppender = new NXDetectorDataDoubleAppender(
					getInputStreamNames(), Arrays.asList(nexusData.getDoubleVals()));

			ArrayList<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
			appenders.add(roiAppender);
			appenders.add(nexusAppender);

			NXDetectorSerialAppender serialAppender = new NXDetectorSerialAppender(
					appenders);

			output.add(serialAppender);
		}

		return output;
	}

	private File waitForNextHDF5() throws InterruptedException, DeviceException {
		File nextHDF5File = deriveNextHDF5File();

		while (!nextHDF5File.exists()) {
			Thread.sleep(100);
		}
		
		new XmapFileUtil(nextHDF5File.getAbsolutePath()).waitForFileToBeReadable();
		
		return nextHDF5File;
	}

	private File deriveNextHDF5File() {
		String fileName = hdf5Folder + File.separator + hdf5Prefix + "-" + nextRowToBeCollected + ".hdf";
		return new File(fileName);
	}

	public VortexROI[] getRois() {
		return rois;
	}

	public void setRois(VortexROI[] rois) {
		this.rois = rois;
		
		detectorElements = new ArrayList<DetectorElement>();
		
		for(int i = 0; i < numberElements; i++){
			DetectorElement newElement = new DetectorElement();
			newElement.setExcluded(false);
			newElement.setRegionList(Arrays.asList(rois));
			newElement.setNumber(i);
			newElement.setName("Element " + i);
			detectorElements.add(newElement);
		}
	}
}
