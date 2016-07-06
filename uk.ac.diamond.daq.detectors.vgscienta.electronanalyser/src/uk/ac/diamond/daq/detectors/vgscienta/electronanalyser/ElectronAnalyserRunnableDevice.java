package uk.ac.diamond.daq.detectors.vgscienta.electronanalyser;

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.EpicsAreaDetectorConstants.TriggerMode;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.factory.Finder;
import uk.ac.diamond.daq.detectors.addetector.AreaDetectorWritingFilesRunnableDevice;
import uk.ac.diamond.daq.detectors.vgscienta.electronanalyser.api.DA30LensMode;
import uk.ac.diamond.daq.detectors.vgscienta.electronanalyser.api.ElectronAnalyserRunnableDeviceModel;
import uk.ac.gda.devices.vgscienta.VGScientaAnalyser;

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
public class ElectronAnalyserRunnableDevice extends AreaDetectorWritingFilesRunnableDevice {

	private static final Logger logger = LoggerFactory.getLogger(ElectronAnalyserRunnableDevice.class);

	private static final String ANGLES_ENTRY_NAME = "angles";
	private static final String ENERGIES_ENTRY_NAME = "energies";

	private VGScientaAnalyser analyser;
	private ElectronAnalyserRunnableDeviceModel electronAnalyserModel;
	private ILazyWriteableDataset energyAxis;
	private ILazyWriteableDataset yAxis;

	public void configure(ElectronAnalyserRunnableDeviceModel model) throws ScanningException {

		this.electronAnalyserModel = model;

		// Setup the underlying area detector the same
		super.configure(model);
		// TODO Leaves DeviceState = READY
		setDeviceState(DeviceState.CONFIGURING);

		try {
			// Get the detector by name defined in the model
			analyser = Finder.getInstance().find(model.getName());
			if (analyser == null) {
				throw new ScanningException("Could not find detector: " + model.getName());
			}

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

		} catch (Exception e) {
			setDeviceState(DeviceState.FAULT);
			throw new ScanningException("Configuring detector failed", e);
		}

		setDeviceState(DeviceState.READY);
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) {

		NXdetector detector = createNexusObject(info);
		final NexusObjectWrapper<NXdetector> nexusProvider =  new NexusObjectWrapper<>(getName(), detector);

		int scanRank = info.getRank();

		// Add the additional axis to the detector image
		nexusProvider.addAxisDataFieldForPrimaryDataField(ANGLES_ENTRY_NAME, NXdetector.NX_DATA, scanRank);
		nexusProvider.addAxisDataFieldForPrimaryDataField(ENERGIES_ENTRY_NAME, NXdetector.NX_DATA, scanRank + 1);

		return nexusProvider;
	}

	@Override
	public NXdetector createNexusObject(NexusScanInfo scanInfo) {

		// Call the super class for most of the setup
		final NXdetector nxDetector = NexusNodeFactory.createNXdetector();

		// Write detector metadata
		try {
			// Exposure
			nxDetector.setField("exposure_time", analyser.getCollectionTime());
			nxDetector.setAttribute("exposure_time", "units", "seconds");
			// This is the number of frames as determined by the frame rate of the camera
			nxDetector.setField("number_of_frames", analyser.getController().getFrames());
			// This is also the number of iterations should be equal to model.getItterations()
			nxDetector.setField("number_of_cycles", analyser.getAdBase().getNumExposures_RBV());
			// Pass energy
			nxDetector.setField("pass_energy", analyser.getPassEnergy());
			nxDetector.setAttribute("pass_energy", "units", "eV");
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
			nxDetector.setAttribute("entrance_slit_size", "units", "mm");
			// Energy range
			nxDetector.setField("kinetic_energy_start", analyser.getStartEnergy());
			nxDetector.setAttribute("kinetic_energy_start", "units", "eV");
			nxDetector.setField("kinetic_energy_end", analyser.getEndEnergy());
			nxDetector.setAttribute("kinetic_energy_end", "units", "eV");
			nxDetector.setField("kinetic_energy_center", analyser.getCentreEnergy());
			nxDetector.setAttribute("kinetic_energy_center", "units", "eV");
			// PSU Mode
			nxDetector.setField("psu_mode", analyser.getPsuMode());

		} catch (Exception e) {
			logger.error("Reading electron analyser parameters to write to file failed", e);
		}

		// FIXME These should be read from the device in the first call to write once EPICS allows it.
		energyAxis = nxDetector.initializeLazyDataset(ENERGIES_ENTRY_NAME, 1, Double.class);
		nxDetector.setAttribute(ENERGIES_ENTRY_NAME, "units", "eV");
		yAxis = nxDetector.initializeLazyDataset(ANGLES_ENTRY_NAME, 1, Double.class);
		if (electronAnalyserModel.getLensMode() == DA30LensMode.TRANSMISSION) {
			nxDetector.setAttribute(ANGLES_ENTRY_NAME, "units", "mm");
		} else {
			nxDetector.setAttribute(ANGLES_ENTRY_NAME, "units", "deg");
		}

		return nxDetector;
	}

	@Override
	public boolean write(IPosition pos) throws ScanningException {
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

}
