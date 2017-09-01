package uk.ac.diamond.daq.detectors.addetector;

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.NDPluginBase.DataType;
import gda.factory.Finder;
import uk.ac.diamond.daq.detectors.addetector.api.AreaDetectorRunnableDeviceModel;

/**
 * <p>
 * This is an implementation of a new style GDA detector that can connects to an area detector.
 * </p>
 * <p>
 * It gets the data back from the AD using the array plugin and then writes it out inside GDA.
 * </p>
 *
 * @author James Mudd
 */
public class AreaDetectorRunnableDevice extends AbstractAreaDetectorRunnableDevice {

	private final static Logger logger= LoggerFactory.getLogger(AreaDetectorRunnableDevice.class);
	private static final String FIELD_NAME_TOTAL = "total";
	private ILazyWriteableDataset data;
	protected ADDetector adDetector;
	// This implementation only supports 2D area detectors, this might not be all cases
	private final int[] dataDimensions = new int[2];
	private ILazyWriteableDataset total;
	private DataType dataType;
	//detector data size may be different from array size when EPICS roi plugin is used in AD pipeline
	private final int[] imageDimensions= new int[2];
	private boolean firstPointInScan;

	public AreaDetectorRunnableDevice() {
		super(ServiceHolder.getRunnableDeviceService());
	}

	@Override
	public void run(IPosition position) throws ScanningException, InterruptedException {
		setDeviceState(DeviceState.RUNNING);
		try {
			adDetector.collectData();
			adDetector.waitWhileBusy();
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Acquiring from detector failed", e);
		}
	}

	@Override
	public void configure(AreaDetectorRunnableDeviceModel model) throws ScanningException {
		setDeviceState(DeviceState.CONFIGURING);

		// Cache the model so it can be used in other methods (createNexusObject). This seems a bit messy
		this.model = model;

		// Get the detector by name defined in the model
		adDetector = Finder.getInstance().find(model.getName());
		if (adDetector == null) {
			throw new ScanningException("Could not find detector: " + model.getName());
		}

		try {
			// Get the data size so we know how big to write in the file and cache it here so we don't
			// need to go to EPICS all the time
			dataDimensions[0] = adDetector.getAdBase().getArraySizeY_RBV();
			dataDimensions[1] = adDetector.getAdBase().getArraySizeX_RBV();
			// Get the dataType to expect
			dataType = adDetector.getNdArray().getDataType(); //image data type setting before 1st frame being collected.

			// Set the exposure time in GDA detector object but not apply it to hardware yet.
			adDetector.setCollectionTime(model.getExposureTime());

		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Configuring detector failed", e);
		}
		setDeviceState(DeviceState.ARMED);
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
		final NXdetector nxDetector = createNexusObject(info);
		final NexusObjectWrapper<NXdetector> nexusProvider = new NexusObjectWrapper<>(getName(), nxDetector);

		// "data" is the name of the primary data field (i.e. the 'signal' field of the default NXdata)
		nexusProvider.setPrimaryDataFieldName(NXdetector.NX_DATA);
		// An additional NXdata group with "total" as the signal to hold the total data
		nexusProvider.addAdditionalPrimaryDataFieldName(FIELD_NAME_TOTAL);

		return nexusProvider;
	}

	private NXdetector createNexusObject(NexusScanInfo scanInfo) throws NexusException {

		final NXdetector nxDetector = NexusNodeFactory.createNXdetector();
		nxDetector.setCount_timeScalar(model.getExposureTime());
		DataType imageDataType;
		try {
			// Get the data size so we know how big to write in the file and cache it here so we don't
			// need to go to EPICS all the time
			imageDimensions[0]=adDetector.getNdArray().getPluginBase().getArraySize1_RBV();
			imageDimensions[1]=adDetector.getNdArray().getPluginBase().getArraySize0_RBV();
			imageDataType = adDetector.getNdArray().getDataType();
		} catch (Exception e) {
			throw new NexusException("Getting the image diamensions and data type from the detector failed", e);
		}
		if (imageDimensions[0]==0 || imageDimensions[1]==0) {
			throw new NexusException("Detector array plugin is not initialised! Please initialise it by collecting one dummy frame in EPICS.");
		}
		if (dataDimensions[0] != imageDimensions[0] || dataDimensions[1] != imageDimensions[1]) {
			//This is possible if EPICS roi plugin is used to set region of interest in AD pipeline
			logger.info("Detector diamensions {} are different from Array diamensions {}", dataDimensions, imageDimensions);
		}
		if (imageDataType != dataType) {
			//This is possible if EPICS proc plugin is used to process image data in AD pipeline
			logger.info("Detector data type {} is different from Array data type {}", dataType, imageDataType);
		}
		// We add 2 to the scan rank to include the image
		data = nxDetector.initializeLazyDataset(NXdetector.NX_DATA, scanInfo.getRank() + 2, convertDataType(imageDataType));
		// The total should just have the rank of the scan
		total = nxDetector.initializeLazyDataset(FIELD_NAME_TOTAL, scanInfo.getRank(), Double.class);

		// Set the chunking
		data.setChunking(scanInfo.createChunk(imageDimensions));

		return nxDetector;
	}

	private void firstFrame() throws ScanningException {
		DataType imageDataType;
		try {
			// Get the data size so we know how big to write in the file and cache it here so we don't
			// need to go to EPICS all the time
			imageDimensions[0]=adDetector.getNdArray().getPluginBase().getArraySize1_RBV();
			imageDimensions[1]=adDetector.getNdArray().getPluginBase().getArraySize0_RBV();
			imageDataType = adDetector.getNdArray().getDataType();
		} catch (Exception e) {
			throw new ScanningException("Getting the image diamensions and data type from the detector failed", e);
		}
		if (imageDimensions[0]==0 || imageDimensions[1]==0) {
			//this unlikely to occur as the 1st frame already collected, unless EPICS Array plugin is disabled after IOC restarts.
			throw new ScanningException("Detector array plugin is not initialised! Please initialise it by collecting one dummy frame in EPICS.");
		}
		if (dataDimensions[0] != imageDimensions[0] || dataDimensions[1] != imageDimensions[1]) {
			//This is possible if EPICS roi plugin is used to set region of interest in AD pipeline
			logger.info("Detector diamensions {} are different from Array diamensions {}", dataDimensions, imageDimensions);
		}
		if (imageDataType != dataType) {
			//This is possible if EPICS proc plugin is used to process image data in AD pipeline
			logger.error("Image data type {} is different from Detector source data type {}", dataType, imageDataType);
			throw new ScanningException("Image data type changed in EPICS at 1st Frame. Please re-run your command or data collection again!");
		}
	}

	@Override
	public boolean write(IPosition pos) throws ScanningException {
		if (firstPointInScan) {
			firstFrame();
			firstPointInScan=false;
		}
		try {
			// Get the data from the detector array plugin
			final Object image = adDetector.getNdArray().getImageData(imageDimensions[0] * imageDimensions[1]);

			// Create a dataset from the data
			final Dataset dataset = DatasetFactory.createFromObject(image);
			// Write the image data
			IScanSlice scanSlice = IScanRankService.getScanRankService().createScanSlice(pos, imageDimensions);
			SliceND sliceND = new SliceND(data.getShape(), data.getMaxShape(), scanSlice.getStart(), scanSlice.getStop(), scanSlice.getStep());
			data.setSlice(null, dataset, sliceND);
			// Write the total data
			scanSlice = IScanRankService.getScanRankService().createScanSlice(pos);
			sliceND = new SliceND(total.getShape(), total.getMaxShape(), scanSlice.getStart(), scanSlice.getStop(), scanSlice.getStep());
			total.setSlice(null, DatasetFactory.createFromObject(dataset.sum()), sliceND);
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Getting the data from the detector failed", e);
		}

		setDeviceState(DeviceState.ARMED);
		// FIXME why ever return false? If this fails you can throw, in what case is it good for this method to finish
		// saying I failed?
		return true;
	}

	/**
	 * add call to {@link ADDetector#atScanStart()} so decorators in collection strategy work to save detector state.
	 */
	@Override
	@ScanStart
	public void scanStart(ScanInformation info) throws ScanningException {
		super.scanStart(info);
		try {
			adDetector.atScanStart();
			firstPointInScan=true;
		} catch (DeviceException e) {
			throw new ScanningException("Error calling atScanStart", e);
		}
	}

	/**
	 * add call to {@link ADDetector#atScanEnd()} so decorators in collection strategy work to restore detector state.
	 */
	@Override
	@ScanEnd
	public void scanEnd(ScanInformation info) throws ScanningException {
		super.scanEnd(info);
		try {
			adDetector.atScanEnd();
			firstPointInScan=false;
		} catch (DeviceException e) {
			throw new ScanningException("Error calling atScanEnd", e);
		}
	}

	private Class<?> convertDataType(DataType epicsType) {
		switch (epicsType) {
		// Unsigned could overflow signed so return one type bigger
		case UINT8:
			return Short.class;
		case UINT16:
			return Integer.class;
		case UINT32:
			return Long.class;
		case INT8:
			return Byte.class;
		case INT16:
			return Short.class;
		case INT32:
			return Integer.class;
		case FLOAT32:
			return Float.class;
		case FLOAT64:
			return Double.class;
		default:
			// If somehow not matched return float 64 to be safe (e.g. new values added to enum)
			return Double.class;
		}
	}

}
