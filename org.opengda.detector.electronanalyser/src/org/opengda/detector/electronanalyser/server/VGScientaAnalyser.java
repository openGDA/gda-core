/**
 * Copyright © 2012 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NXDetectorData;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDProcess;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.diamond.daq.pes.api.AcquisitionMode;
import uk.ac.diamond.daq.pes.api.AnalyserEnergyRangeConfiguration;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;
import uk.ac.gda.devices.vgscienta.VGScientaController;

@ServiceInterface(IVGScientaAnalyserRMI.class)
public class VGScientaAnalyser extends ADDetector implements IVGScientaAnalyserRMI {

	private static final long serialVersionUID = -2907729482321978030L;

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(VGScientaAnalyser.class);

	private transient VGScientaController controller;
	private int[] fixedModeRegion;
	private int[] sweptModeRegion;

	private AnalyserEnergyRangeConfiguration energyRange;

	private transient NDProcess ndProc;

	private String regionName;

	private ENERGY_MODE cachedEnergyMode;

	private double energyStepPerPixel = 0.0;
	private double maxKE;

	private transient Scannable dcmenergy;
	private transient Scannable pgmenergy;
	private double cachedExcitationEnergy;
	private transient RegionDefinitionResourceUtil regionDefinitionResourceUtil;

	public static final String ELECTRON_VOLTS = "eV";

	public static final String REGION_NAME = "region_name";
	public static final String LENS_MODE_STR = "lens_mode";
	public static final String ACQUISITION_MODE_STR = "acquisiton_mode";
	public static final String ENERGY_MODE_STR = "energy_mode";
	public static final String DETECTOR_MODE_STR = "detector_mode";
	public static final String PASS_ENERGY = "pass_energy";
	public static final String NUMBER_OF_SLICES = "number_of_slices";
	public static final String NUMBER_OF_ITERATIONS = "number_of_iterations";
	public static final String DETECTOR_X_FROM = "detector_x_from";
	public static final String DETECTOR_X_TO = "detector_x_to";
	public static final String DETECTOR_X_SIZE = "detector_x_region_size";
	public static final String DETECTOR_Y_FROM = "detector_y_from";
	public static final String DETECTOR_Y_TO = "detector_y_to";
	public static final String DETECTOR_Y_SIZE = "detector_y_region_size";
	public static final String LOW_ENERGY = "low_energy";
	public static final String FIXED_ENERGY = "fixed_energy";
	public static final String HIGH_ENERGY = "high_energy";
	public static final String ENERGY_STEP = "energy_step";
	public static final String STEP_TIME   = "step_time";
	public static final String TOTAL_STEPS = "total_steps";
	public static final String TOTAL_TIME  = "total_time";
	public static final String SENSOR_SIZE = 	"sensor_size";
	public static final String REGION_ORIGIN = 	"region_origin";
	public static final String REGION_SIZE = 	"region_size";
	public static final String REGION_VALID = 	"region_valid";

	public static final String IMAGE = "image_data";
	public static final String SPECTRUM = "spectrum_data";
	public static final String EXTERNAL_IO = "external_io_data";
	public static final String EXCITATION_ENERGY = "excitation_energy";
	public static final String INTENSITY = "total_intensity";
	public static final String ANGLES = "angles";
	public static final String PIXEL = "pixel";
	public static final String ENERGIES = "energies";

	private static final double ANALYSER_TIMEOUT_TIME = 10.;

	public VGScientaController getController() {
		return controller;
	}

	public void setController(VGScientaController controller) {
		this.controller = controller;
	}

	public int getNumberOfSweeptSteps() throws Exception {
		return controller.getTotalDataPoints();
	}

	@Override
	public double[] getEnergyAxis() throws Exception {
		return controller.getEnergyAxis();
	}

	@Override
	public double[] getAngleAxis() throws Exception {
		return controller.getYAxis();
	}

	@Override
	protected void appendDataAxes(NXDetectorData data) throws Exception {
		short state = getAdBase().getDetectorState_RBV();
		switch (state) {
			case 6:
				throw new DeviceException("analyser in error state during readout");
			case 1:
				// The IOC can report acquiring for quite a while after being stopped
				logger.debug("analyser status is acquiring during readout although we think it has stopped");
				break;
			case 10:
				logger.warn("analyser in aborted state during readout");
				break;
			default:
				break;
		}
		if (firstReadoutInScan) {
			int i = 1;
			String aname = ENERGIES;
			String aunit = ELECTRON_VOLTS;
			double[] axis = getEnergyAxis();

			data.addAxis(getName(), aname, new NexusGroupData(axis), i + 1, 1, aunit, false);

			i = 0;
			if ("Transmission".equals(getLensMode())) {
				aname = "location";
				aunit = "pixel";
			} else {
				aname = "angles";
				aunit = "degree";
			}
			axis = getAngleAxis();

			data.addAxis(getName(), aname, new NexusGroupData(axis), i + 1, 1, aunit, false);

			data.addData(getName(), LENS_MODE_STR, new NexusGroupData(getLensMode()));

			data.addData(getName(), PASS_ENERGY, new NexusGroupData(getPassEnergy()));

			data.addData(getName(), ACQUISITION_MODE_STR, new NexusGroupData(getAcquisitionMode()));

			data.addData(getName(), ENERGY_MODE_STR, new NexusGroupData(getEnergyMode()));

			data.addData(getName(), DETECTOR_MODE_STR, new NexusGroupData(getDetectorMode()));

			data.addData(getName(), SENSOR_SIZE, new NexusGroupData(getAdBase().getMaxSizeX_RBV(), getAdBase().getMaxSizeY_RBV()));

			data.addData(getName(), REGION_ORIGIN, new NexusGroupData(getAdBase().getMinX_RBV(), getAdBase().getMinY_RBV()));

			data.addData(getName(), REGION_SIZE, new NexusGroupData(getAdBase().getSizeX_RBV(), getAdBase().getSizeY_RBV()));

			data.addData(getName(), NUMBER_OF_ITERATIONS, new NexusGroupData(getNumberIterations()));
		}
	}

	@Override
	protected void appendNXDetectorDataFromCollectionStrategy(NXDetectorData data) throws Exception {
		super.appendNXDetectorDataFromCollectionStrategy(data);
		// add additional data (image/array data are already added by the framework createNXDetectorData() by default)
		double[] spectrum=null;
		spectrum = getSpectrum();
		if (spectrum!=null) {
			data.addData(getName(), SPECTRUM, new NexusGroupData(spectrum), "counts");
		}
		double[] externalIO=null;
		externalIO = getExternalIOData();
		if (externalIO!=null) {
			data.addData(getName(), "externalIO", new NexusGroupData(externalIO));
		}

	}

	public double calculateBeamEnergy(Region region) throws DeviceException {
		// fix the EPICS IOC issue - excitation energy does not update in EPICS during energy scan
		Double excitationEnergy;
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			if (region.getExcitationEnergy() < regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
				excitationEnergy = Double.valueOf(getPgmenergy().getPosition().toString());
			} else {
				excitationEnergy = Double.valueOf(getDcmenergy().getPosition().toString()) * 1000;
			}
		}
		else {
			excitationEnergy = Double.valueOf(getDcmenergy().getPosition().toString()) * 1000;
		}
		return excitationEnergy;
	}

	public void configureWithNewRegion(Region region) throws DeviceException {

		logger.debug("Configuring analyser with region data {}", region.getName());
		try {
			double beamEnergy = calculateBeamEnergy(region);
			setExcitationEnergy(beamEnergy);
			ENERGY_MODE energyMode = region.getEnergyMode();
			if (energyMode == ENERGY_MODE.BINDING) {
				setStartEnergy(cachedExcitationEnergy - region.getHighEnergy(), ANALYSER_TIMEOUT_TIME);
				setEndEnergy(cachedExcitationEnergy - region.getLowEnergy(), ANALYSER_TIMEOUT_TIME);
				setCentreEnergy(cachedExcitationEnergy - region.getFixEnergy(), ANALYSER_TIMEOUT_TIME);
			} else {
				setStartEnergy(region.getLowEnergy(), ANALYSER_TIMEOUT_TIME);
				setEndEnergy(region.getHighEnergy(), ANALYSER_TIMEOUT_TIME);
				setCentreEnergy(region.getFixEnergy(), ANALYSER_TIMEOUT_TIME);
			}
			setRegionName(region.getName());
			setCameraMinX(region.getFirstXChannel(), ANALYSER_TIMEOUT_TIME);
			setCameraMinY(region.getFirstYChannel(), ANALYSER_TIMEOUT_TIME);
			setCameraSizeX(region.getLastXChannel() - region.getFirstXChannel() + 1, ANALYSER_TIMEOUT_TIME);
			setCameraSizeY(region.getLastYChannel() - region.getFirstYChannel() + 1, ANALYSER_TIMEOUT_TIME);
			setSlices(region.getSlices(), ANALYSER_TIMEOUT_TIME);
			setDetectorMode(region.getDetectorMode().getLiteral(), ANALYSER_TIMEOUT_TIME);
			setLensMode(region.getLensMode(), ANALYSER_TIMEOUT_TIME);
			setPassEnergy(region.getPassEnergy(), ANALYSER_TIMEOUT_TIME);
			// Hack to fix EPICS does not support bind energy input values, energy values in EPICS are kinetic energy only
			setCachedEnergyMode(energyMode);
			setEnergyStep(region.getEnergyStep() / 1000.0, ANALYSER_TIMEOUT_TIME);
			double collectionTime = region.getStepTime();
			setStepTime(collectionTime, ANALYSER_TIMEOUT_TIME);
			if (!region.getRunMode().isRepeatUntilStopped()) {
				setNumberInterations(region.getRunMode().getNumIterations(), ANALYSER_TIMEOUT_TIME);
			} else {
				setNumberInterations(1000000, ANALYSER_TIMEOUT_TIME);
			}
			setImageMode(ImageMode.SINGLE, ANALYSER_TIMEOUT_TIME);
			setAcquisitionMode(region.getAcquisitionMode().getLiteral(), ANALYSER_TIMEOUT_TIME);
		}
		catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			getAdBase().stopAcquiring();
		} catch (Exception e) {
			throw new DeviceException("Failed to stop acquiring", e);
		}
		super.stop();
	}

	public double[] getExternalIODataFormatted() throws TimeoutException, CAException, InterruptedException, Exception {

		int i = getAcquisitionMode().equalsIgnoreCase("Fixed") ? 1 : getEnergyAxis().length;

		return controller.getExtIO(i);
	}

	public double[] getExternalIOData(int i) throws TimeoutException, CAException, InterruptedException, Exception {
		return controller.getExtIO(i);
	}

	public double[] getImage(int i) throws Exception {
		return controller.getImage(i);
	}

	@Override
	public void setupAcquisitionMode(AcquisitionMode acquisitionMode) throws Exception {
		switch (acquisitionMode) {
		case FIXED:
			setupFixedMode();
			break;
		case SWEPT:
			setupSweptMode();
			break;
		default:
			throw new UnsupportedOperationException(acquisitionMode.toString() + " mode is not supported by this analyser");
		}

		getAdBase().setImageMode(0);
		getAdBase().setTriggerMode(0);
	}

	private void setupFixedMode() throws Exception {
		controller.setAcquisitionMode("Fixed");
		setRegion(fixedModeRegion);
	}

	private void setupSweptMode() throws Exception {
		controller.setAcquisitionMode("Swept");
		setRegion(sweptModeRegion != null ? sweptModeRegion : fixedModeRegion);
	}

	private void setRegion (int[] region) throws Exception {
		getAdBase().setMinX(region[0]);
		getAdBase().setMinY(region[1]);
		getAdBase().setSizeX(region[2]);
		getAdBase().setSizeY(region[3]);
		controller.setSlices(region[3]);
	}

	public int[] getSweptModeRegion() {
		return sweptModeRegion;
	}

	public void setSweptModeRegion(int[] sweptModeRegion) {
		this.sweptModeRegion = sweptModeRegion;
	}

	public int[] getFixedModeRegion() {
		return fixedModeRegion;
	}

	public void setFixedModeRegion(int[] fixedModeRegion) {
		this.fixedModeRegion = fixedModeRegion;
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		try {
			return getAdBase().getAcquireTime();
		} catch (Exception e) {
			throw new DeviceException("error getting collection time", e);
		}
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		try {
			getAdBase().setAcquireTime(collectionTime);
		} catch (Exception e) {
			throw new DeviceException("error setting collection time", e);
		}
	}

	public void setNumberInterations(int value) throws Exception {
		getAdBase().setNumExposures(value);
	}

	public void setNumberInterations(int value, double timeout) throws Exception {
		getAdBase().setNumExposures(value);
	}

	public Integer getNumberIterations() throws Exception {
		return getAdBase().getNumExposures_RBV();
	}

	public void setCameraMinX(int value) throws Exception {
		getAdBase().setMinX(value);
	}

	public void setCameraMinX(int value, double timeout) throws Exception {
		getAdBase().setMinXWait(value, timeout);
	}

	public int getCameraMinX() throws Exception {
		return getAdBase().getMinX_RBV();
	}

	public void setCameraMinY(int value) throws Exception {
		getAdBase().setMinY(value);
	}

	public void setCameraMinY(int value, double timeout) throws Exception {
		getAdBase().setMinYWait(value, timeout);
	}

	public int getCameraMinY() throws Exception {
		return getAdBase().getMinY_RBV();
	}

	public void setCameraSizeX(int value) throws Exception {
		getAdBase().setSizeX(value);
	}

	public void setCameraSizeX(int value, double timeout) throws Exception {
		getAdBase().setSizeXWait(value, timeout);
	}

	public int getCameraSizeX() throws Exception {
		return getAdBase().getSizeX_RBV();
	}

	public void setCameraSizeY(int value) throws Exception {
		getAdBase().setSizeY(value);
	}

	public void setCameraSizeY(int value, double timeout) throws Exception {
		getAdBase().setSizeYWait(value, timeout);
	}

	public void setImageMode(ImageMode imagemode) throws Exception {
		getAdBase().setImageMode(imagemode);
	}

	public void setImageMode(ImageMode imagemode, double timeout) throws Exception {
		getAdBase().setImageModeWait(imagemode, timeout);
	}

	public int getCameraSizeY() throws Exception {
		return getAdBase().getSizeY_RBV();
	}

	@Override
	public void setLensMode(String value) throws Exception {
		controller.setLensMode(value);
	}

	public void setLensMode(String value, double timeout) throws Exception {
		controller.setLensMode(value);
	}

	@Override
	public String getLensMode() throws Exception {
		return controller.getLensMode();
	}

	@Override
	public List<String> getLensModes() {
		return controller.getLensModes();
	}

	@Override
	public void setAcquisitionMode(String value) throws Exception {
		controller.setAcquisitionMode(value);
	}

	public void setAcquisitionMode(String value, double timeout) throws Exception {
		controller.setAcquisitionMode(value);
	}

	public String getAcquisitionMode() throws Exception {
		return controller.getAcquisitionMode();
	}

	@Override
	public List<AcquisitionMode> getSupportedAcquisitionModes() {
		return List.of(AcquisitionMode.FIXED, AcquisitionMode.SWEPT);
	}

	public String getEnergyMode() {
		return ENERGY_MODE.BINDING.toString();
	}

	public void setDetectorMode(String value) throws Exception {
		controller.setDetectorMode(value);
	}

	public void setDetectorMode(String value, double timeout) throws Exception {
		controller.setDetectorMode(value);
	}

	public String getDetectorMode() throws Exception {
		return controller.getDetectorMode();
	}

	@Override
	public void setPsuMode(String value) throws Exception {
		controller.setPsuMode(value);
	}

	@Override
	public String getPsuMode() throws Exception {
		return controller.getPsuMode();
	}

	@Override
	public List<String> getPsuModes() {
		return new ArrayList<>(controller.getPsuModes());
	}

	@Override
	public void setPassEnergy(Integer value) throws Exception {
		controller.setPassEnergy(value);
	}

	public void setPassEnergy(Integer value, double timeout) throws Exception {
		controller.setPassEnergy(value);
	}

	@Override
	public Integer getPassEnergy() throws Exception {
		return controller.getPassEnergy();
	}

	@Override
	public List<String> getPassEnergies() {
		return controller.getPassEnergies();
	}

	public void setStartEnergy(Double value) throws Exception {
		controller.setStartEnergy(value);
	}

	public void setStartEnergy(Double value, double timeout) throws Exception {
		controller.setStartEnergy(value);
	}

	public Double getStartEnergy() throws Exception {
		return controller.getStartEnergy();
	}

	@Override
	public void setCentreEnergy(Double value) throws Exception {
		controller.setCentreEnergy(value);
	}

	public void setCentreEnergy(Double value, double timeout) throws Exception {
		controller.setCentreEnergy(value);
	}

	@Override
	public Double getCentreEnergy() throws Exception {
		return controller.getCentreEnergy();
	}

	public void setEndEnergy(Double value) throws Exception {
		controller.setEndEnergy(value);
	}

	public void setEndEnergy(Double value, double timeout) throws Exception {
		controller.setEndEnergy(value);
	}

	public Double getEndEnergy() throws Exception {
		return controller.getEndEnergy();
	}

	@Override
	public void setEnergyStep(double value) throws Exception {
		controller.setEnergyStep(value);
	}

	public void setEnergyStep(Double value, double timeout) throws Exception {
		controller.setEnergyStep(value);
	}

	@Override
	public double getEnergyStep() throws Exception {
		return controller.getEnergyStep();
	}


	public void setFrames(Integer value) throws Exception {
		controller.setFrames(value);
	}

	@Override
	public int getFrames() throws Exception {
		return controller.getFrames();
	}

	public void setStepTime(double value) throws Exception {
		controller.setExposureTime(value);
	}

	public void setStepTime(double value, double timeout) throws Exception {
		controller.setExposureTime(value);
	}

	public double getStepTime() throws Exception {
		return controller.getExposureTime();
	}

	public void setSlices(int value) throws Exception {
		controller.setSlices(value);
	}

	public void setSlices(int value, double timeout) throws Exception {
		controller.setSlices(value);
	}

	@Override
	public int getSlices() throws Exception {
		return controller.getSlice();
	}

	public Integer getTotalSteps() throws Exception {
		return controller.getTotalSteps();
	}

	@Override
	public void zeroSupplies() throws Exception {
		controller.zeroSupplies();
	}

	public int getNdarrayXsize() throws Exception {
		return getNdArray().getPluginBase().getArraySize0_RBV();
	}

	public int getNdarrayYsize() throws Exception {
		return getNdArray().getPluginBase().getArraySize1_RBV();
	}


	public double[] getExternalIOData() throws Exception {
		return controller.getExtIO();
	}

	@Override
	public double[] getSpectrum() throws Exception {
		return controller.getSpectrum();
	}

	@Override
	public double[] getImage() throws Exception {
		return controller.getImage();
	}

	public NDProcess getNdProc() {
		return ndProc;
	}

	public void setNdProc(NDProcess ndProc) {
		this.ndProc = ndProc;
	}

	@Override
	public void startAcquiring() throws Exception {
		getAdBase().startAcquiring();
	}

	@Override
	public double getExcitationEnergy() throws Exception {
		return this.cachedExcitationEnergy;
	}

	public void setExcitationEnergy(double energy) throws IllegalArgumentException {
		if (energy < 0) {
			throw new IllegalArgumentException("Excitation energy must be greater than or equal to 0");
		}
		this.cachedExcitationEnergy = energy;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionname) {
		this.regionName = regionname;
	}

	public ENERGY_MODE getCachedEnergyMode() {
		return cachedEnergyMode;
	}

	public void setCachedEnergyMode(ENERGY_MODE energyMode) {
		this.cachedEnergyMode = energyMode;
	}

	public double getTotalIntensity() {
		try {
			return Arrays.stream(controller.getSpectrum()).sum();
		} catch (Exception e) {
			logger.error("Error getting intensity value from analyser. {}", e.getMessage());
			return 0.;
		}
	}

	@Override
	public double[] getExtIO(int length) throws Exception {
		return controller.getExtIO(length);
	}

	@Override
	public AnalyserEnergyRangeConfiguration getEnergyRange() {
		return energyRange;
	}

	public void setEnergyRange(AnalyserEnergyRangeConfiguration energyRange) {
		this.energyRange = energyRange;
	}

	@Override
	public double getEnergyStepPerPixel() {
		return energyStepPerPixel;
	}

	public void setEnergyStepPerPixel(double energyStepPerPixel) {
		this.energyStepPerPixel = energyStepPerPixel;
	}

	@Override
	public double getMaxKE() {
		return maxKE;
	}

	public void setMaxKE(double maxKE) {
		this.maxKE = maxKE;
	}

	@Override
	public int getFixedModeEnergyChannels() {
		return fixedModeRegion[2];
	}

	@Override
	public int getSweptModeEnergyChannels() {
		return sweptModeRegion[3];
	}

	@Override
	public void changeRequestedIterations(int newScheduledIterations) {
		throw new UnsupportedOperationException("Can not chnage iterations on this implementation");
	}

	@Override
	public void startContinuous() throws Exception {
		logger.info("Starting continuous acquisition");
		// For continuous acquisition in alignment use fixed mode
		setupAcquisitionMode(AcquisitionMode.FIXED);
		// Change to continuous
		getAdBase().setImageMode(ImageMode.CONTINUOUS);
		// Change to 1 iteration
		controller.setIterations(1);
		// Start acquiring
		getAdBase().startAcquiring();

	}

	@Override
	public void stopAfterCurrentIteration() throws Exception {
		controller.stopAfterCurrentIteration();
	}

	@Override
	public int getIterations() throws Exception {
		return controller.getIterations();
	}

	@Override
	public int getCompletedIterations() throws Exception {
		return controller.getCompletedIterations();
	}

	@Override
	public int getCurrentIteration() throws Exception {
		return controller.getCurrentIterations();
	}

	@Override
	public void setIterations(int iterations) throws Exception {
		controller.setIterations(iterations);
	}

	@Override
	public short getDetectorState() throws Exception {
		return getAdBase().getDetectorState_RBV();
	}

	@Override
	public void setSingleImageMode() throws Exception {
		getAdBase().setImageMode(0);
	}

	@Override
	public int getMaximumNumberOfSteps() {
		return Integer.MAX_VALUE;
	}

	public Scannable getDcmenergy() {
		return dcmenergy;
	}

	public void setDcmenergy(Scannable dcmenergy) {
		this.dcmenergy = dcmenergy;
	}

	public Scannable getPgmenergy() {
		return pgmenergy;
	}

	public void setPgmenergy(Scannable pgmenergy) {
		this.pgmenergy = pgmenergy;
	}

	public RegionDefinitionResourceUtil getRegionDefinitionResourceUtil() {
		return regionDefinitionResourceUtil;
	}

	public void setRegionDefinitionResourceUtil(RegionDefinitionResourceUtil regionDefinitionResourceUtil) {
		this.regionDefinitionResourceUtil = regionDefinitionResourceUtil;
	}
}
