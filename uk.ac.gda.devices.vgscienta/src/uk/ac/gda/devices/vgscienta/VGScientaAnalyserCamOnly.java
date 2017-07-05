/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.vgscienta;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.dataset.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cosylab.epics.caj.CAJChannel;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.DeviceException;
import gda.device.MotorStatus;
import gda.device.Scannable;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.NXDetectorData;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.impl.ADBaseImpl;
import gda.device.detector.nxdetector.roi.PlotServerROISelectionProvider;
import gda.device.scannable.ScannableBase;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

@CorbaAdapterClass(DeviceAdapter.class)
@CorbaImplClass(DeviceImpl.class)
public class VGScientaAnalyserCamOnly extends ADDetector implements MonitorListener, IVGScientaAnalyserRMI {
	private static final Logger logger = LoggerFactory.getLogger(VGScientaAnalyserCamOnly.class);

	protected boolean inScan = false;

	private VGScientaController controller;
	private VGScientaAnalyserEnergyRange energyRange;
	private int[] fixedModeRegion;
	private int[] sweptModeRegion;

	private EntranceSlitInformationProvider entranceSlitInformationProvider;

	private PlotServerROISelectionProvider cpsRoiProvider;
	private RectangularROI cpsRoi;

	private boolean kineticEnergyChangesDuringScan = false;

	private double acquireTimeRBV;
	private int[] dataShape;

	/**
	 * This is the energy covered by one pixel in pass energy 1 in meV
	 * <p>
	 * To find the energy step per pixel this value should be multiplied by the pass energy. To find the fixed mode energy width this value should be multiplied
	 * by the pass energy and the number of energy channels.
	 * <p>
	 * This should be set equal to the value used in SES which can be found in: Calibration -> Detector -> Energy Scale [meV/channel]
	 * <p>
	 * This value should <b>not</b> be used to calculate energy scales.
	 */
	private double energyStepPerPixel = 0;

	/**
	 * This is the fall-back maximum kinetic energy (KE) if the energyRange object can't provide a correct energy range
	 */
	private double maxKE = 1000.0;

	final public Scannable centre_energy = new ScannableBase() {

		@Override
		public void atScanStart() throws DeviceException {
			kineticEnergyChangesDuringScan = true;
		}

		@Override
		public Double getPosition() throws DeviceException {
			try {
				if (controller.getAcquisitionMode().equalsIgnoreCase("Fixed")) {
					return getCentreEnergy();
				}
				return 0.5 * (getStartEnergy() + getEndEnergy());
			} catch (Exception e) {
				throw new DeviceException("error getting to EPICS", e);
			}
		}

		@Override
		public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
			Double energy = ((Number) externalPosition).doubleValue();
			try {
				if (controller.getAcquisitionMode().equalsIgnoreCase("Fixed")) {
					setCentreEnergy(energy);
					return;
				}
				Double moveby = this.getPosition() - energy;
				setStartEnergy(getStartEnergy() - moveby);
				setEndEnergy(getEndEnergy() - moveby);
				return;
			} catch (Exception e) {
				throw new DeviceException("error getting to EPICS", e);
			}
		}

		@Override
		public boolean isBusy() throws DeviceException {
			return false;
		}
	};

	// Fields for performance logging
	private boolean inAcquire;
	private long collectionStartTime;
	private long colllectionEndTime;
	private long acquisitonsCompleted;
	private long timeSpentWithEpicsAcquiring;
	private long timeSpentGettingDataBackOverEpics;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		setExtraNames(new String[] {"cps"});

		try {
		// For updates to GUI
		// FIXME This is messy there must be a better way to observe this, but for now this works and is how it's done in the old implementation
		final EpicsController epicsController = EpicsController.getInstance();
		epicsController.setMonitor(epicsController.createChannel(((ADBaseImpl) getAdBase()).getBasePVName() + ADBase.Acquire_RBV), this);
		}
		catch (Exception e) {
			logger.error("Error setting up EPICS monitors", e);
		}
	}

	public VGScientaController getController() {
		return controller;
	}

	public void setController(VGScientaController controller) {
		this.controller = controller;
	}

	@Override
	public void atScanStart() throws DeviceException {
		try {
			getAdBase().stopAcquiring();
		} catch (Exception e) {
			// if the thing wasn't acquiring in the first place nothing is lost
			// if this is an important problem we'll hit that later, so no need to rethrow
			logger.error("error stopping acquisition before running scan", e);
		}
		try {
			// don't ask, but getRoi doesn't return what you expect
			if (controller.getAcquisitionMode().equalsIgnoreCase("Fixed")) {
				cpsRoi = cpsRoiProvider.getScisoftRoiListFromSDAPlotter().get(0);
			} else {
				cpsRoi = null;
			}
		} catch (Exception e) {
			logger.info("no cps roi could be retireved, cps will be calculated over entire active detector");
			cpsRoi = null;
		}
		super.atScanStart();
		inScan = true;
		// Reset counters for performance logging
		acquisitonsCompleted = 0;
		timeSpentWithEpicsAcquiring = 0;
		timeSpentGettingDataBackOverEpics = 0;
	}

	@Override
	public void atScanEnd() throws DeviceException {
		inScan = false;
		super.atScanEnd();
		zeroSuppliesIgnoreErrors();
		kineticEnergyChangesDuringScan = false;

		// Do performance logging
		inAcquire = false;
		// Convert ns to ms
		final double totalTimeAcquiring = timeSpentWithEpicsAcquiring / 1.0E6;
		final double timeAcquiringPerAcquire = totalTimeAcquiring / acquisitonsCompleted;
		// acquireTimeRBV is in sec convert to ms
		final double exposureTime = acquireTimeRBV * 1000;
		final double deadTimeAcquiring = timeAcquiringPerAcquire - exposureTime;

		if (Double.isNaN(exposureTime) || Double.isNaN(timeAcquiringPerAcquire) || Double.isNaN(deadTimeAcquiring)) {
			// Protect against NaN getting logged for stats
			logger.warn("Analyser Performance - Failed to calculate analyser timings");
		} else {
			logger.info("Analyser Performance - Exposure Time {} ms, Actual time per acquire {} ms, Dead time per acquire {} ms", exposureTime,
					timeAcquiringPerAcquire, deadTimeAcquiring);
		}

		// Log time spent getting the data from EPICS
		final double timeGettingDataPerAcquisition = (timeSpentGettingDataBackOverEpics / 1.0E6) / acquisitonsCompleted;
		if (Double.isNaN(timeGettingDataPerAcquisition)) { // Protect against NaN getting logged for stats
			logger.warn("Analyser Performance - Failed to calculate time getting data from EPICS");
		} else {
			logger.info("Analyser Performance - Time getting data from EPICS {} ms per acquire", timeGettingDataPerAcquisition);
		}
	}

	public int getNumberOfSweeptSteps() throws Exception {
		return controller.getEnergyChannels();
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
		// if (currentstatus == running)
		// throw new DeviceException("analyser being read out while acquiring - we do not expect that");
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

		if (kineticEnergyChangesDuringScan || firstReadoutInScan) {
			double[] axis = getEnergyAxis();
			data.addAxis(getName(), "energies", new NexusGroupData(axis), 2, 1, "eV", kineticEnergyChangesDuringScan);
		}

		if (firstReadoutInScan) { // place in entry1/instrument/analyser(NXdetector) group.
			data.addData(getName(), "kinetic_energy_start", new NexusGroupData(getStartEnergy()), "eV", null);
			data.addData(getName(), "kinetic_energy_center", new NexusGroupData((Double) centre_energy.getPosition()), "eV", null);
			data.addData(getName(), "kinetic_energy_end", new NexusGroupData(getEndEnergy()), "eV", null);

			String aunit, aname;
			if ("Transmission".equals(getLensMode())) {
				aname = "location";
				aunit = "mm";
			} else {
				aname = "angles";
				aunit = "degree";
			}
			double[] axis = getAngleAxis();
			data.addAxis(getName(), aname, new NexusGroupData(axis), 1, 1, aunit, false);

			acquireTimeRBV = controller.getExposureTime();
			data.addData(getName(), "time_per_channel", new NexusGroupData(acquireTimeRBV), "s", null, null, true);
			data.addData(getName(), "lens_mode", new NexusGroupData(getLensMode()), null, null);
			data.addData(getName(), "acquisition_mode", new NexusGroupData(controller.getAcquisitionMode()), null, null);
			data.addData(getName(), "pass_energy", new NexusGroupData(getPassEnergy()), "eV", null);
			data.addData(getName(), "psu_mode", new NexusGroupData(getPsuMode()), null, null);
			data.addData(getName(), "number_of_frames", new NexusGroupData(controller.getFrames()), null, null);
			data.addData(getName(), "time_for_frames", new NexusGroupData(getAdBase().getAcquireTime_RBV()), "s", null);
			data.addData(getName(), "sensor_size", new NexusGroupData(getAdBase().getMaxSizeX_RBV(), getAdBase().getMaxSizeY_RBV()), null, null);
			data.addData(getName(), "region_origin", new NexusGroupData(getAdBase().getMinX_RBV(), getAdBase().getMinY_RBV()), null, null);
			data.addData(getName(), "region_size", new NexusGroupData(getAdBase().getSizeX_RBV(), getAdBase().getSizeY_RBV()), null, null);

			if (cpsRoi != null) {
				data.addData(getName(), "cps_region_origin", new NexusGroupData(cpsRoi.getIntPoint()), null, null);
				data.addData(getName(), "cps_region_size", new NexusGroupData(cpsRoi.getIntLengths()), null, null);
			} else {
				NexusGroupData groupData = data.getData(getName(), "data", NexusExtractor.SDSClassName);
				data.addData(getName(), "cps_region_origin", new NexusGroupData(0, 0), null, null);
				data.addData(getName(), "cps_region_size", new NexusGroupData(groupData.dimensions), null, null);
			}

			if (entranceSlitInformationProvider != null) {
				data.addData(getName(), "entrance_slit_size", new NexusGroupData(entranceSlitInformationProvider.getSizeInMM()), "mm", null);
				data.addData(getName(), "entrance_slit_setting",
						new NexusGroupData(String.format("%03d", entranceSlitInformationProvider.getRawValue().intValue())), null, null);
				data.addData(getName(), "entrance_slit_shape", new NexusGroupData(entranceSlitInformationProvider.getShape().toLowerCase()), null, null);
				data.addData(getName(), "entrance_slit_direction", new NexusGroupData(entranceSlitInformationProvider.getDirection().toLowerCase()), null,
						null);
			}
		}
	}

	@Override
	protected void appendNXDetectorDataFromCollectionStrategy(NXDetectorData data) throws Exception {

		// Get the dataset as doubles
		Dataset dataset = data.getData(getName(), "data", NexusExtractor.SDSClassName).asDouble().toDataset();

		// Calculate the total CPS in the ROI if it exists otherwise over the whole image
		if (cpsRoi != null) { // Take the ROI out of the dataset
			dataset = ROIProfile.box(dataset, cpsRoi)[0];
		}

		// Get the buffer as summing that is much faster than dataset.sum() see SCI-5688
		double[] datasetBuffer = (double[]) dataset.getBuffer();

		double sum = 0;
		long startTime = System.nanoTime();
		for (double d : datasetBuffer) {
			sum += d;
		}
		long endTime = System.nanoTime();
		logger.trace("Sum = {}, Time to sum buffer: {} ms", sum, (endTime-startTime)/1.0E6);

		addDoubleItem(data, "cps", sum / acquireTimeRBV, "Hz");
	}

	protected void addDoubleItem(NXDetectorData data, String name, double d, String units) {
		INexusTree valdata = data.addData(getName(), name, new NexusGroupData(d), units, null, null, true);
		valdata.addChildNode(
				new NexusTreeNode("local_name", NexusExtractor.AttrClassName, valdata, new NexusGroupData(String.format("%s.%s", getName(), name))));
		data.setPlottableValue(name, d);
	}

	public boolean isFixedMode() throws Exception {
		return "Fixed".equalsIgnoreCase(controller.getAcquisitionMode());
	}

	public void setFixedMode(boolean fixed) throws Exception {
		int[] region = fixedModeRegion;
		if (fixed) {
			controller.setAcquisitionMode("Fixed");
		} else {
			controller.setAcquisitionMode("Swept");
			if (sweptModeRegion != null) {
				region = sweptModeRegion;
			}
		}
		getAdBase().setMinX(region[0]);
		getAdBase().setMinY(region[1]);
		getAdBase().setSizeX(region[2]);
		getAdBase().setSizeY(region[3]);
		controller.setSlices(region[3]);
		getAdBase().setImageMode(0);
		getAdBase().setTriggerMode(0);
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

	@Override
	public void setLensMode(String value) throws Exception {
		if (inScan)
			throw new DeviceException("change of lens mode prohibited during scan");
		boolean restart = false;
		if (getAdBase().getAcquireState() == 1) {
			getAdBase().stopAcquiring();
			restart = true;
		}
		controller.setLensMode(value);
		if (restart)
			getAdBase().startAcquiring();
	}

	@Override
	public String getLensMode() throws Exception {
		return controller.getLensMode();
	}

	@Override
	public String getPsuMode() throws Exception {
		return controller.getPsuMode();
	}

	@Override
	public void setPassEnergy(Integer value) throws Exception {
		if (inScan)
			throw new DeviceException("change of pass energy prohibited during scan");
		boolean restart = false;
		if (getAdBase().getAcquireState() == 1) {
			getAdBase().stopAcquiring();
			restart = true;
		}
		controller.setPassEnergy(value);
		if (restart)
			getAdBase().startAcquiring();
	}

	@Override
	public Integer getPassEnergy() throws Exception {
		return controller.getPassEnergy();
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

	public void setEnergyStep(Double value) throws Exception {
		controller.setEnergyStep(value);
	}

	public Double getEnergyStep() throws Exception {
		return controller.getEnergyStep();
	}

	@Override
	public void zeroSupplies() throws Exception {
		controller.zeroSupplies();
	}

	public void zeroSuppliesIgnoreErrors() {
		try {
			zeroSupplies();
		} catch (Exception e) {
			logger.error("error zeroing power supplies", e);
		}
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		if (((CAJChannel) arg0.getSource()).getName().endsWith(ADBase.Acquire_RBV)) {
			logger.debug("been informed of some sort of change to acquire status");
			DBR_Enum en = (DBR_Enum) arg0.getDBR();
			short[] no = (short[]) en.getValue();
			final MotorStatus currentstatus;
			if (no[0] == 0) {
				logger.info("been informed of a stop");
				currentstatus = MotorStatus.READY;
			} else {
				logger.info("been informed of a start");
				currentstatus = MotorStatus.BUSY;
			}
			notifyIObservers(this, currentstatus);
		}
	}

	public EntranceSlitInformationProvider getEntranceSlitInformationProvider() {
		return entranceSlitInformationProvider;
	}

	public void setEntranceSlitInformationProvider(EntranceSlitInformationProvider entranceSlitInformationProvider) {
		this.entranceSlitInformationProvider = entranceSlitInformationProvider;
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		inScan = false;
		zeroSuppliesIgnoreErrors();
		super.atCommandFailure();
	}

	@Override
	public void stop() throws DeviceException {
		inScan = false;
		super.stop();
	}

	public PlotServerROISelectionProvider getCpsRoiProvider() {
		return cpsRoiProvider;
	}

	public void setCpsRoiProvider(PlotServerROISelectionProvider cpsRoiProvider) {
		this.cpsRoiProvider = cpsRoiProvider;
	}

	public Scannable getCentreEnergyScannable() {
		return centre_energy;
	}

	@Override
	protected NXDetectorData createNXDetectorData() throws Exception, DeviceException {
		NXDetectorData data = new NXDetectorData(this);

		// Only readout the shape once per scan to save on EPICS communications
		if (firstReadoutInScan) {
			int energyChannels = controller.getEnergyChannels();
			int yChannels = controller.getSlice();
			dataShape = new int[] { yChannels, energyChannels };
		}

		long startTime = System.nanoTime();
		float[] imageData = controller.getImageAsFloat(dataShape[0] * dataShape[1]);
		long endTime = System.nanoTime();
		timeSpentGettingDataBackOverEpics += endTime - startTime;

		NexusGroupData ngd = new NexusGroupData(dataShape, imageData);
		ngd.isDetectorEntryData = true;

		data.addData(getName(), "data", ngd, null, 1);

		appendDataAxes(data);
		appendNXDetectorDataFromCollectionStrategy(data);

		return data;
	}

	@Override
	public void collectData() throws DeviceException {
		inAcquire = true;
		collectionStartTime = System.nanoTime();
		super.collectData();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		super.waitWhileBusy();
		// Because waitWhileBusy is called when the acquire is not running need to isolate that case.
		if (inAcquire) {
			colllectionEndTime = System.nanoTime();
			inAcquire = false;
			acquisitonsCompleted += 1;
			timeSpentWithEpicsAcquiring += colllectionEndTime - collectionStartTime;
		}
	}

	@Override
	public VGScientaAnalyserEnergyRange getEnergyRange() {
		return energyRange;
	}

	public void setEnergyRange(VGScientaAnalyserEnergyRange energyRange) {
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
		return sweptModeRegion[2];
	}

	@Override
	public void changeRequestedIterations(int max) {
		throw new UnsupportedOperationException("Can not chnage iterations on this implementation");
	}

	@Override
	public void startContinuious() throws Exception {
		logger.info("Starting continious acquisition");
		// For continuous acquisition in alignment use fixed mode
		setFixedMode(true);
		// Change to continuous
		getAdBase().setImageMode(ImageMode.CONTINUOUS);
		// Change to 1 iteration
		controller.setIterations(1);
		// Start acquiring
		getAdBase().startAcquiring();
	}

	@Override
	public int getIterations() throws Exception {
		return controller.getIterations();
	}

	@Override
	public void setIterations(int iterations) throws Exception {
		if (inScan) {
			throw new IllegalStateException("Cannot set the number of iterations during a scan");
		}
		controller.setIterations(iterations);
	}

	/**
	 * Gets the number of slices. i.e the number of non-energy (Y channels) that will be recorded. The data is binned in the Y direction.
	 *
	 * @return Current number of slices
	 * @throws Exception
	 *             If there is a problem at the EPICS level
	 */
	public int getSlices() throws Exception {
		return controller.getSlice();
	}

	/**
	 * Sets the number of slices to record i.e the number of non-energy (Y channels) that will be recorded. The data is binned in the Y direction.
	 *
	 * @param slices
	 *            The requested number of slices
	 * @throws Exception
	 *             If there is a problem at the EPICS level
	 */
	public void setSlices(int slices) throws Exception {
		// TODO validate the slices value it should be < the detector region Y size
		controller.setSlices(slices);
	}
}