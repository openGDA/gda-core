package uk.ac.diamond.daq.detectors.vgscienta.electronanalyser;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.EpicsAreaDetectorConstants.TriggerMode;
import gda.device.detector.areadetector.v17.ImageMode;
import uk.ac.diamond.daq.detectors.addetector.ServiceHolder;
import uk.ac.diamond.daq.detectors.vgscienta.electronanalyser.api.DA30LensMode;
import uk.ac.diamond.daq.detectors.vgscienta.electronanalyser.api.ElectronAnalyserRunnableDeviceModel;
import uk.ac.gda.devices.vgscienta.VGScientaAnalyserCamOnly;

/**
 * <p>
 * This is an implementation of a VG Scienta electron analyser for use with the Solstice scanning framework.
 * </p>
 * <p>
 * The area detector directly writes its own HDF5 file which is linked to the GDA NeXus file.
 * </p>
 *
 * @author James Mudd
 */
public class ElectronAnalyserRunnableDevice extends AbstractRunnableDevice<ElectronAnalyserRunnableDeviceModel>
		implements IWritableDetector<ElectronAnalyserRunnableDeviceModel>, INexusDevice<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(ElectronAnalyserRunnableDevice.class);

	private VGScientaAnalyserCamOnly analyser;

	// This implementation only supports 2D
	private int[] dataDimensions;

	private static final String UNITS = "units";
	private static final String ANGLES_ENTRY_NAME = "angles";
	private static final String ENERGIES_ENTRY_NAME = "energies";

	private static final String FIELD_NAME_TOTAL = "total";

	private ILazyWriteableDataset energyAxis;
	private ILazyWriteableDataset yAxis;
	private ILazyWriteableDataset data;
	private ILazyWriteableDataset total;

	public ElectronAnalyserRunnableDevice() {
		super(ServiceHolder.getRunnableDeviceService());
	}

	protected ElectronAnalyserRunnableDevice(IRunnableDeviceService dservice) {
		super(dservice);
	}

	@Override
	public void configure(ElectronAnalyserRunnableDeviceModel model) throws ScanningException {

		// Setup the underlying area detector the same
		super.configure(model);
		// TODO Leaves DeviceState = READY
		setDeviceState(DeviceState.CONFIGURING);

		try {
			// Setup analyser specific parameters
			// TODO Look at adding swept mode support, need a more complex model
			analyser.setFixedMode(true);
			// Set pass energy
			analyser.setPassEnergy(model.getPassEnergy());
			// Set lens mode
			analyser.setLensMode(model.getLensMode().toString());
			// Set iterations which is also exposures for this device
			analyser.getAdBase().setNumExposures(model.getIterations());
			// Set centre energy
			analyser.setCentreEnergy(model.getCentreEnergy());

			// Set the exposure time
			analyser.getAdBase().setAcquireTime(model.getExposureTime());
			// Set image mode to multiple
			analyser.getAdBase().setImageMode(ImageMode.MULTIPLE);
			// Set triggering to internal
			analyser.getAdBase().setTriggerMode(TriggerMode.Internal.ordinal());

			// Setup the data dimensions this might not always work!
			dataDimensions = new int[] { analyser.getFixedModeRegion()[2], analyser.getFixedModeRegion()[3] };

		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Configuring detector failed", e);
		}

		setDeviceState(DeviceState.READY);
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) {
		final NXdetector nxDetector = createNexusObject(info);

		// Write detector metadata
		try {
			// Exposure
			nxDetector.setField("exposure_time", analyser.getCollectionTime());
			nxDetector.setAttribute("exposure_time", UNITS, "seconds");
			// This is the number of frames as determined by the frame rate of the camera
			nxDetector.setField("number_of_frames", analyser.getController().getFrames());
			// This is also the number of iterations should be equal to model.getItterations()
			nxDetector.setField("number_of_cycles", analyser.getAdBase().getNumExposures_RBV());
			// Pass energy
			nxDetector.setField("pass_energy", analyser.getPassEnergy());
			nxDetector.setAttribute("pass_energy", UNITS, "eV");
			// Lens mode
			nxDetector.setField("lens_mode", analyser.getLensMode());
			// Acquisition Mode
			nxDetector.setField("acquisition_mode", analyser.isFixedMode() ? "Fixed" : "Swept");
			// Entrance slit
			nxDetector.setField("entrance_slit_direction",
					analyser.getEntranceSlitInformationProvider().getDirection());
			nxDetector.setField("entrance_slit_setting", analyser.getEntranceSlitInformationProvider().getRawValue());
			nxDetector.setField("entrance_slit_shape", analyser.getEntranceSlitInformationProvider().getShape());
			nxDetector.setField("entrance_slit_size", analyser.getEntranceSlitInformationProvider().getSizeInMM());
			nxDetector.setAttribute("entrance_slit_size", UNITS, "mm");
			// Energy range
			nxDetector.setField("kinetic_energy_start", analyser.getStartEnergy());
			nxDetector.setAttribute("kinetic_energy_start", UNITS, "eV");
			nxDetector.setField("kinetic_energy_end", analyser.getEndEnergy());
			nxDetector.setAttribute("kinetic_energy_end", UNITS, "eV");
			nxDetector.setField("kinetic_energy_center", analyser.getCentreEnergy());
			nxDetector.setAttribute("kinetic_energy_center", UNITS, "eV");
			// PSU Mode
			nxDetector.setField("psu_mode", analyser.getPsuMode());

		} catch (Exception e) {
			logger.error("Reading electron analyser parameters to write to file failed", e);
		}

		// FIXME These should be read from the device in the first call to write once EPICS allows it.
		energyAxis = nxDetector.initializeLazyDataset(ENERGIES_ENTRY_NAME, 1, Double.class);
		nxDetector.setAttribute(ENERGIES_ENTRY_NAME, UNITS, "eV");
		yAxis = nxDetector.initializeLazyDataset(ANGLES_ENTRY_NAME, 1, Double.class);
		if (getModel().getLensMode() == DA30LensMode.TRANSMISSION) {
			nxDetector.setAttribute(ANGLES_ENTRY_NAME, UNITS, "mm");
		} else {
			nxDetector.setAttribute(ANGLES_ENTRY_NAME, UNITS, "deg");
		}

		int scanRank = info.getRank();

		// Get the NexusOjbectWrapper wrapping the detector
		NexusObjectWrapper<NXdetector> nexusObjectWrapper = new NexusObjectWrapper<>(
				getName(), nxDetector);

		// "data" is the name of the primary data field (i.e. the 'signal' field of the default NXdata)
		nexusObjectWrapper.setPrimaryDataFieldName(NXdetector.NX_DATA);
		// An additional NXdata group with "total" as the signal to hold the total data
		nexusObjectWrapper.addAdditionalPrimaryDataFieldName(FIELD_NAME_TOTAL);

		// Add the additional axis to the detector image
		nexusObjectWrapper.addAxisDataFieldForPrimaryDataField(ANGLES_ENTRY_NAME, NXdetector.NX_DATA, scanRank);
		nexusObjectWrapper.addAxisDataFieldForPrimaryDataField(ENERGIES_ENTRY_NAME, NXdetector.NX_DATA, scanRank + 1);

		return nexusObjectWrapper;
	}

	public NXdetector createNexusObject(NexusScanInfo scanInfo) {

		final NXdetector nxDetector = NexusNodeFactory.createNXdetector();
		nxDetector.setCount_timeScalar(model.getExposureTime());

		// We add 2 to the scan rank to include the image
		data = nxDetector.initializeLazyDataset(NXdetector.NX_DATA, scanInfo.getRank() + 2, Double.class);
		// The total should just have the rank of the scan
		total = nxDetector.initializeLazyDataset(FIELD_NAME_TOTAL, scanInfo.getRank(), Double.class);

		// Set the chunking
		data.setChunking(scanInfo.createChunk(dataDimensions));

		return nxDetector;
	}

	@Override
	public void run(IPosition position)
			throws ScanningException, InterruptedException, TimeoutException, ExecutionException {
		setDeviceState(DeviceState.RUNNING);
		try {
			analyser.collectData();
			analyser.waitWhileBusy();
		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Acquiring from detector failed", e);
		}
	}

	@Override
	public boolean write(IPosition pos) throws ScanningException {
		// Get the data from the detector array plugin
		Object image;
		try {
			image = analyser.getController().getImage();

			// Create a dataset from the data
			Dataset dataset = DatasetFactory.createFromObject(image, dataDimensions);

			// Write the image data
			IScanSlice scanSlice = IScanRankService.getScanRankService().createScanSlice(pos, dataDimensions);
			SliceND sliceND = new SliceND(data.getShape(), data.getMaxShape(), scanSlice.getStart(),
					scanSlice.getStop(), scanSlice.getStep());
			data.setSlice(null, dataset, sliceND);
			// Write the total data
			scanSlice = IScanRankService.getScanRankService().createScanSlice(pos);
			sliceND = new SliceND(total.getShape(), total.getMaxShape(), scanSlice.getStart(), scanSlice.getStop(),
					scanSlice.getStep());
			total.setSlice(null, DatasetFactory.createFromObject(dataset.sum()), sliceND);

		} catch (Exception e) {
			throw new ScanningException("Error getting image from analyser", e);
		}

		// Check if we are writing the first point because at this point the axis in EPICS should be correct.
		if (pos.getStepIndex() == 0) {
			try {
				// Energy axis
				Dataset energyAxisDataset = DatasetFactory.createFromObject(analyser.getEnergyAxis());
				SliceND energyAxisSlice = new SliceND(energyAxisDataset.getShape());
				energyAxis.setSlice(null, energyAxisDataset, energyAxisSlice);
				// Y axis
				Dataset yAxisDataset = DatasetFactory.createFromObject(analyser.getAngleAxis());
				SliceND angleAxisSlice = new SliceND(yAxisDataset.getShape());
				yAxis.setSlice(null, yAxisDataset, angleAxisSlice);
			} catch (Exception e) {
				throw new ScanningException("Failed to write analyser axis data", e);
			}
		}
		return true;
	}

	public VGScientaAnalyserCamOnly getAnalyser() {
		return analyser;
	}

	public void setAnalyser(VGScientaAnalyserCamOnly analyser) {
		this.analyser = analyser;
	}

}
