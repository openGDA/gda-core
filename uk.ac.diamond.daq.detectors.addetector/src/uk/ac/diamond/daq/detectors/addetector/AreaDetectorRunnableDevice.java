package uk.ac.diamond.daq.detectors.addetector;

import org.eclipse.dawnsci.analysis.api.dataset.Dtype;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;

import gda.device.detector.EpicsAreaDetectorConstants.TriggerMode;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.ImageMode;
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
public class AreaDetectorRunnableDevice extends AbstractRunnableDevice<AreaDetectorRunnableDeviceModel> implements
		IWritableDetector<AreaDetectorRunnableDeviceModel>, INexusDevice<NXdetector> {

	private static final String FIELD_NAME_TOTAL = "total";
	private ILazyWriteableDataset data;
	private ADDetector detector;
	// This implementation only supports 2D area detectors, this might not be all cases
	private int[] dataDimensions = new int[2];
	private ILazyWriteableDataset total;
	private DataType dataType;

	@Override
	public void run(IPosition position) throws ScanningException, InterruptedException {
		setDeviceState(DeviceState.RUNNING);
		try {
			detector.getAdBase().startAcquiring();
			detector.getAdBase().waitWhileStatusBusy();
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Acquiring from detector failed", e);
		}
	}

	@Override
	public void configure(AreaDetectorRunnableDeviceModel model) throws ScanningException {
		setDeviceState(DeviceState.CONFIGURING);

		// Get the detector by name defined in the model
		detector = Finder.getInstance().find(model.getName());
		if (detector == null) {
			throw new ScanningException("Could not find detector: " + model.getName());
		}

		try {
			// Get the data size so we know how big to write in the file and cache it here so we don't
			// need to go to EPICS all the time
			dataDimensions[0] = detector.getAdBase().getSizeY_RBV();
			dataDimensions[1] = detector.getAdBase().getSizeX_RBV();
			// Get the dataType to expect
			dataType = detector.getAdBase().getDataType_RBV2();

			// Set the exposure time
			detector.getAdBase().setAcquireTime(model.getExposureTime());
			// Set image mode to single
			detector.getAdBase().setImageMode(ImageMode.SINGLE);
			// Set triggering to internal
			detector.getAdBase().setTriggerMode(TriggerMode.Internal.ordinal());
			// FIXME Need to configure the plugin chain here
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Configuring detector failed", e);
		}
		setDeviceState(DeviceState.READY);
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) {
		NXdetector detector = createNexusObject(info);
		NexusObjectWrapper<NXdetector> nexusProvider = new NexusObjectWrapper<NXdetector>(
				getName(), detector);

		// "data" is the name of the primary data field (i.e. the 'signal' field of the default NXdata)
		nexusProvider.setPrimaryDataFieldName(NXdetector.NX_DATA);
		// An additional NXdata group with "total" as the signal to hold the total data
		nexusProvider.addAdditionalPrimaryDataFieldName(FIELD_NAME_TOTAL);

		return nexusProvider;
	}

	public NXdetector createNexusObject(NexusScanInfo scanInfo) {

		final NXdetector nxDetector = NexusNodeFactory.createNXdetector();

		// We add 2 to the scan rank to include the image
		data = nxDetector.initializeLazyDataset(NXdetector.NX_DATA, scanInfo.getRank() + 2, convertDataType(dataType));
		// The total should just have the rank of the scan
		total = nxDetector.initializeLazyDataset(FIELD_NAME_TOTAL, scanInfo.getRank(), Dataset.FLOAT64);

		// Set the chunking
		data.setChunking(scanInfo.createChunk(dataDimensions));

		return nxDetector;
	}

	@Override
	public boolean write(IPosition pos) throws ScanningException {

		try {
			// Get the data from the detector array plugin
			Object image = detector.getNdArray().getImageData(dataDimensions[0] * dataDimensions[1]);

			// Create a dataset from the data
			DoubleDataset dataset = DoubleDataset.createFromObject(image);
			// Write the image data
			SliceND sliceND = NexusScanInfo.createLocation(data, pos.getNames(), pos.getIndices(), dataDimensions);
			data.setSlice(null, dataset, sliceND);
			// Write the total data
			sliceND = NexusScanInfo.createLocation(total, pos.getNames(), pos.getIndices());
			total.setSlice(null, DoubleDataset.createFromObject(dataset.sum()), sliceND);
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Getting the data from the detector failed", e);
		}

		setDeviceState(DeviceState.READY);
		// FIXME why ever return false? If this fails you can throw, in what case is it good for this method to finish
		// saying I failed?
		return true;
	}

	private int convertDataType(DataType epicsType) {
		switch (epicsType) {
		// Unsigned could overflow signed so return one type bigger
		case UINT8:
			return Dtype.INT16;
		case UINT16:
			return Dtype.INT32;
		case UINT32:
			return Dtype.INT64;
		case INT8:
			return Dtype.INT8;
		case INT16:
			return Dtype.INT16;
		case INT32:
			return Dtype.INT32;
		case FLOAT32:
			return Dtype.FLOAT32;
		case FLOAT64:
			return Dtype.FLOAT64;
		default:
			// If somehow not matched return float 64 to be safe (e.g. new values added to enum)
			return Dtype.FLOAT64;
		}
	}

}
