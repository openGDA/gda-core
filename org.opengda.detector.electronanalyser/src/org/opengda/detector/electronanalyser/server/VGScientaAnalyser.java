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

import org.opengda.detector.electronanalyser.api.SESRegion;
import org.opengda.detector.electronanalyser.utils.AnalyserRegionConstants;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.factory.FactoryException;
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

	private String cachedEnergyMode;

	private double energyStepPerPixel = 0.0;
	private double maxKE;

	private double cachedExcitationEnergy;

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
			String aname = AnalyserRegionConstants.ENERGIES;
			String aunit = AnalyserRegionConstants.ELECTRON_VOLTS;
			double[] axis = getEnergyAxis();
			data.addAxis(getName(), aname, new NexusGroupData(axis), i + 1, 1, aunit, false);
			i = 0;
			if ("Transmission".equals(getLensMode())) {
				aname = "location";
				aunit = AnalyserRegionConstants.PIXEL;
			} else {
				aname = AnalyserRegionConstants.ANGLES;
				aunit = "degree";
			}
			axis = getAngleAxis();
			data.addAxis(getName(), aname, new NexusGroupData(axis), i + 1, 1, aunit, false);
			data.addData(getName(), AnalyserRegionConstants.LENS_MODE, new NexusGroupData(getLensMode()));
			data.addData(getName(), AnalyserRegionConstants.PASS_ENERGY, new NexusGroupData(getPassEnergy()));
			data.addData(getName(), AnalyserRegionConstants.ACQUISITION_MODE, new NexusGroupData(getAcquisitionMode()));
			data.addData(getName(), AnalyserRegionConstants.ENERGY_MODE, new NexusGroupData(getEnergyMode()));
			data.addData(getName(), AnalyserRegionConstants.DETECTOR_MODE, new NexusGroupData(getDetectorMode()));
			data.addData(getName(), AnalyserRegionConstants.SENSOR_SIZE, new NexusGroupData(getAdBase().getMaxSizeX_RBV(), getAdBase().getMaxSizeY_RBV()));
			data.addData(getName(), AnalyserRegionConstants.REGION_ORIGIN, new NexusGroupData(getAdBase().getMinX_RBV(), getAdBase().getMinY_RBV()));
			data.addData(getName(), AnalyserRegionConstants.REGION_SIZE, new NexusGroupData(getAdBase().getSizeX_RBV(), getAdBase().getSizeY_RBV()));
			data.addData(getName(), AnalyserRegionConstants.NUMBER_OF_ITERATIONS, new NexusGroupData(getNumberIterations()));
		}
	}

	@Override
	protected void appendNXDetectorDataFromCollectionStrategy(NXDetectorData data) throws Exception {
		super.appendNXDetectorDataFromCollectionStrategy(data);
		// add additional data (image/array data are already added by the framework createNXDetectorData() by default)
		double[] spectrum=null;
		spectrum = getSpectrum();
		if (spectrum!=null) {
			data.addData(getName(), AnalyserRegionConstants.SPECTRUM, new NexusGroupData(spectrum), "counts");
		}
		double[] externalIO=null;
		externalIO = getExternalIOData();
		if (externalIO!=null) {
			data.addData(getName(), "externalIO", new NexusGroupData(externalIO));
		}
	}

	public void configureWithNewRegion(SESRegion region, double beamEnergy) throws DeviceException, FactoryException {
		//If analyser was connected after server start, it won't have been configured on server start.
		//This allows it to recover rather than having to force a server restart.
		if (!isConfigured()) configure();
		if (!getController().isConfigured()) getController().configure();

		logger.debug("Configuring analyser with region data {}", region.getName());
		try {
			setExcitationEnergy(beamEnergy);
			if (region.isEnergyModeBinding()) {
				setStartEnergy(beamEnergy - region.getHighEnergy());
				setEndEnergy(beamEnergy - region.getLowEnergy());
				setCentreEnergy(beamEnergy - region.getFixEnergy());
			} else {
				setStartEnergy(region.getLowEnergy());
				setEndEnergy(region.getHighEnergy());
				setCentreEnergy(region.getFixEnergy());
			}
			setRegionName(region.getName());
			setCameraMinX(region.getFirstXChannel());
			setCameraMinY(region.getFirstYChannel());
			setCameraSizeX(region.getLastXChannel() - region.getFirstXChannel() + 1);
			setCameraSizeY(region.getLastYChannel() - region.getFirstYChannel() + 1);
			setSlices(region.getSlices());
			setDetectorMode(region.getDetectorMode());
			setLensMode(region.getLensMode());
			setPassEnergy(region.getPassEnergy());
			// Hack to fix EPICS does not support bind energy input values, energy values in EPICS are kinetic energy only
			setCachedEnergyMode(region.getEnergyMode());
			setEnergyStep(region.getEnergyStep());
			setStepTime(region.getStepTime());
			setNumberInterations(region.getIterations());

			setImageMode(ImageMode.SINGLE);
			setAcquisitionMode(region.getAcquisitionMode());
		} catch (Exception e) {
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

	public String getAcquisitionMode() throws Exception {
		return controller.getAcquisitionMode();
	}

	@Override
	public List<AcquisitionMode> getSupportedAcquisitionModes() {
		return List.of(AcquisitionMode.FIXED, AcquisitionMode.SWEPT);
	}

	public String getEnergyMode() {
		return "Kinetic";
	}

	public void setDetectorMode(String value) throws Exception {
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

	public Double getStartEnergy() throws Exception {
		return controller.getStartEnergy();
	}

	@Override
	public void setCentreEnergy(Double value) throws Exception {
		controller.setCentreEnergy(value);
	}

	@Override
	public Double getCentreEnergy() throws Exception {
		return controller.getCentreEnergy();
	}

	public void setEndEnergy(Double value) throws Exception {
		controller.setEndEnergy(value);
	}

	public Double getEndEnergy() throws Exception {
		return controller.getEndEnergy();
	}

	@Override
	public void setEnergyStep(double value) throws Exception {
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

	public double getStepTime() throws Exception {
		return controller.getExposureTime();
	}

	public void setSlices(int value) throws Exception {
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

	public String getCachedEnergyMode() {
		return cachedEnergyMode;
	}

	public void setCachedEnergyMode(String energyMode) {
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
}