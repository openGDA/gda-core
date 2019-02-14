/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.analyser;

import static javax.measure.unit.NonSI.ELECTRON_VOLT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.apache.commons.lang.ArrayUtils;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.MCAStatus;
import gda.device.epicsdevice.EpicsDevice;
import gda.device.epicsdevice.EpicsMonitorEvent;
import gda.device.epicsdevice.EpicsRegistrationRequest;
import gda.device.epicsdevice.FindableEpicsDevice;
import gda.device.epicsdevice.IEpicsChannel;
import gda.device.epicsdevice.ReturnType;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.util.converters.CoupledConverterHolder;
import gda.util.converters.IQuantitiesConverter;
import gda.util.converters.IQuantityConverter;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * Class to communicate with an epics MCA record. The MCA record controls and acquires data from a multichannel analyser
 * (MCA). It connects to the Epics channels via an EpicsDevice whose name is set by the method setEpicsDeviceName.
 * Observers are notified of change of status - either MCAStatus.READY or MCAStatus.BUSY getStatus - returns either
 * Detector.IDLE or Detector.BUSY eraseStartAcquisition - starts acquisition
 */
public class EpicsMCASimple extends AnalyserBase implements IEpicsMCASimple {

	private static final Logger logger = LoggerFactory.getLogger(EpicsMCASimple.class);

	private static final long serialVersionUID = 1L;

	private static final String SINGLE_RECORD = "";

	// Prefix & suffixes for the ROI fields
	private static final String NAME_FIELD = "NM";
	private static final String PRESET_COUNT_FIELD = "P";
	private static final String NET_COUNT_FIELD = "N";
	private static final String PRESET_FIELD = "IP";
	private static final String BACKGROUND_FIELD = "BG";
	private static final String HIGH_FIELD = "HI";
	private static final String LOW_FIELD = "LO";
	private static final String ROI_PREFIX = ".R";

	private static final String READ_FIELD = ".READ";
	private static final String ACQUIRING_FIELD = ".ACQG";
	private static final String STOP_ACQ_FIELD = ".STOP";
	private static final String START_ACQ_FIELD = ".STRT";
	private static final String ERASE_START_ACQ_FIELD = ".ERST";
	private static final String MAX_NUMBER_OF_CHANNELS_TO_USE_FIELD = ".NMAX";
	private static final String NUMBER_OF_CHANNELS_TO_USE_FIELD = ".NUSE";
	private static final String PROC_FIELD = ".PROC";
	private static final String READING_FIELD = ".RDNG";
	private static final String SEQ_FIELD = ".SEQ";

	private static final String ELAPSED_LIVE_TIME_FIELD = ".ELTM";
	private static final String ELAPSED_REAL_TIME_FIELD = ".ERTM";
	private static final String DATA_FIELD = ".VAL";
	private static final String TWO_THETA_FIELD = ".TTH";
	private static final String CALIBRATION_QUADRATIC_FIELD = ".CALQ";
	private static final String CALIBRATION_SLOPE_FIELD = ".CALS";
	private static final String CALIBRATION_OFFSET_FIELD = ".CALO";
	private static final String UNITS_FIELD = ".EGU";
	private static final String ERASE_FIELD = ".ERAS";

	private static final String PRESET_SWEEP_FIELD = ".PSWP";
	private static final String PRESET_COUNT_HIGH_FIELD = ".PCTH";
	private static final String PRESET_COUNT_LOW_FIELD = ".PCTL";
	private static final String PRESET_COUNTS_FIELD = ".PCT";
	private static final String PRESET_LIVE_TIME_FIELD = ".PLTM";
	private static final String PRESET_REAL_TIME_FIELD = ".PRTM";
	private static final String DWELL_TIME_FIELD = ".DWEL";

	private static final String CHANNEL_TO_ENERGY_PREFIX = "channelToEnergy:";
	private static final String NUMBER_OF_CHANNELS_ATTR = "NumberOfChannels";
	private static final String ENERGY_TO_CHANNEL_PREFIX = "energyToChannel";

	private static final int NUM_OF_BINS_IN_DUMMY_MODE = 2048;

	private boolean readNetCounts = true;

	/*
	 * Lockable object that is used to inform the thread executing WaitWhileBusy that the
	 * value of doneReading has been changed by an Epics monitor
	 */
	private Object doneLock= new Object();

	/*
	 * When first developed I found that in the ACQG callback I need to perform
	 * a Read request to ensure the data was correct. However when using this class with the
	 * Epics DXP module that also support MCA it was not needed. The default value
	 * gives the old behaviour
	 */
	private boolean readingDoneIfNotAquiring=false;

	private static final int INDEX_FOR_RAW_ROI = 0;

	private String epicsDeviceName;

	private IQuantityConverter channelToEnergyConverter = null;

	private String converterName = "mca_roi_conversion";

	private volatile boolean acquisitionDone = true;
	private volatile boolean readingDone = true;

	private FindableEpicsDevice epicsDevice = null;

	private static final int MAX_NUMBER_OF_REGIONS = 32;

	private final double[] roiCountValues = new double[MAX_NUMBER_OF_REGIONS];
	private final double[] roiNetCountValues = new double[MAX_NUMBER_OF_REGIONS];

	private static final String[] roiLowFields = new String[MAX_NUMBER_OF_REGIONS];
	private static final String[] roiHighFields = new String[MAX_NUMBER_OF_REGIONS];
	private static final String[] roiBackgroundFields = new String[MAX_NUMBER_OF_REGIONS];
	private static final String[] roiPresetFields = new String[MAX_NUMBER_OF_REGIONS];
	private static final String[] roiCountFields = new String[MAX_NUMBER_OF_REGIONS];
	private static final String[] roiNetCountFields = new String[MAX_NUMBER_OF_REGIONS];
	private static final String[] roiPresetCountFields = new String[MAX_NUMBER_OF_REGIONS];
	private static final String[] roiNameFields = new String[MAX_NUMBER_OF_REGIONS];
	static {
		for (int i = 0; i < roiLowFields.length; i++) {
			roiLowFields[i] = ROI_PREFIX + i + LOW_FIELD;
			roiHighFields[i] = ROI_PREFIX + i + HIGH_FIELD;
			roiBackgroundFields[i] = ROI_PREFIX + i + BACKGROUND_FIELD;
			roiPresetFields[i] = ROI_PREFIX + i + PRESET_FIELD;
			roiCountFields[i] = ROI_PREFIX + i;
			roiNetCountFields[i] = ROI_PREFIX + i + NET_COUNT_FIELD;
			roiPresetCountFields[i] = ROI_PREFIX + i + PRESET_COUNT_FIELD;
			roiNameFields[i] = ROI_PREFIX + i + NAME_FIELD;
		}
	}

	private String mcaPV = null; // pv if not using a FindableEpicsDevice

	private Integer numberOfRegions = MAX_NUMBER_OF_REGIONS;

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (epicsDevice == null) {
				if (epicsDeviceName != null) {
					final Findable object = Finder.getInstance().find(epicsDeviceName);
					if (object != null && object instanceof FindableEpicsDevice) {
						epicsDevice = (FindableEpicsDevice) object;
						epicsDevice.configure();
					}
				} else if (mcaPV != null) {
					EpicsDevice mcaEpicsDevice;
					try {
						final Map<String, String> recordPVs = new HashMap<>();
						recordPVs.put("", mcaPV);
						mcaEpicsDevice = new EpicsDevice(getName(), recordPVs, false);
					} catch (DeviceException e) {
						throw new FactoryException("Unable to create EpicsDEvice", e);
					}
					epicsDevice = new FindableEpicsDevice(getName() + mcaPV, mcaEpicsDevice);
				}
			}
			if (epicsDevice == null) {
				throw new FactoryException("Unable to find epics device");
			}
			if (!epicsDevice.getDummy()) {
				final List<EpicsRegistrationRequest> requests = new ArrayList<>();
				requests.add(new EpicsRegistrationRequest(ReturnType.DBR_NATIVE, SINGLE_RECORD, ACQUIRING_FIELD,
						SINGLE_RECORD, 1.0, false));
				requests.add(new EpicsRegistrationRequest(ReturnType.DBR_NATIVE, SINGLE_RECORD, READING_FIELD,
						SINGLE_RECORD, 1.0, false));
				try {
					for (EpicsRegistrationRequest request : requests) {
						final IEpicsChannel chan = epicsDevice.createEpicsChannel(request.returnType, request.record, request.field);
						chan.addIObserver(this::update);
					}
				} catch (Exception ex) {
					logger.error("Error registering for Epics updates", ex);
				}
			} else {
				try {
					// set configured so that we can use the set commands to initialise values
					setConfigured(true);
					for (Integer i = 0; i < numberOfRegions; i++) {
						addRegionOfInterest(i, -1, -1, 0, 1.0, i.toString()); // set regionPreset to 1.0 to ensure
						// value
						// is set in dummy mode
					}
					setDoubleFieldValue(ELAPSED_LIVE_TIME_FIELD, 1);
					setDoubleFieldValue(ELAPSED_REAL_TIME_FIELD, 1.1);

					final EpicsMCACalibration calib = new EpicsMCACalibration(
							"EGU", (float) 1.0, (float) 1.0, (float) 0., (float) 0.);
					setCalibration(calib);
					setDwellTime(1.0);
					final EpicsMCAPresets preset = new EpicsMCAPresets((float) 1.0, (float) 1.0, 1, 1, 1, 1);
					setPresets(preset);
					setSequence(1);
					setIntFieldValue(MAX_NUMBER_OF_CHANNELS_TO_USE_FIELD, NUM_OF_BINS_IN_DUMMY_MODE);
					setNumberOfChannels(NUM_OF_BINS_IN_DUMMY_MODE);

					for (int i = 0; i < MAX_NUMBER_OF_REGIONS; i++) {
						_setRegionsOfInterestCount(i, i * 1000.);
						_setRegionsOfInterestNetCount(i, i * 1000.);
					}
					final int[] data = new int[NUM_OF_BINS_IN_DUMMY_MODE];
					for (int i = 0; i < data.length; i++) {
						data[i] = i;
					}
					setData(data);
				} catch (DeviceException ex) {
					setConfigured(false);
					throw new FactoryException("Error initialising the device:"+getName(),ex);
				}
			}
			setConfigured(true);
		}
	}

	/**
	 * Helper function to set a Double in the field of the single record in the epicsdevice
	 *
	 * @param field
	 *            - suffix used to construct the pv name
	 * @param value
	 *            - value to set
	 * @throws DeviceException
	 */
	private void setDoubleFieldValue(String field, double value) throws DeviceException {
		epicsDevice.setValue(SINGLE_RECORD, field, value);
	}

	/**
	 * Helper function to set an Integer in the field of the single record in the epicsdevice
	 *
	 * @param field
	 *            - suffix used to construct the pv name
	 * @param value
	 *            - value to set
	 * @throws DeviceException
	 */
	private void setIntFieldValue(String field, int value) throws DeviceException {
		epicsDevice.setValue(SINGLE_RECORD, field, value);
	}

	private void setIntFieldValueNoWait(String field, int value) throws DeviceException {
		epicsDevice.setValueNoWait(SINGLE_RECORD, field, value);
	}

	/**
	 * Helper function to set a short in the field of the single record in the epicsdevice
	 *
	 * @param field
	 *            - suffix used to construct the pv name
	 * @param value
	 *            - value to set
	 * @throws DeviceException
	 */
	private void setShortFieldValue(String field, short value) throws DeviceException {
		epicsDevice.setValue(SINGLE_RECORD, field, value);
	}

	/**
	 * Helper function to set String in the field of the single record in the epicsdevice
	 *
	 * @param field
	 *            - suffix used to construct the pv name
	 * @param value
	 *            - value to set
	 * @throws DeviceException
	 */
	private void setStringFieldValue(String field, String value) throws DeviceException {
		epicsDevice.setValue(SINGLE_RECORD, field, value);
	}

	/**
	 * Helper function to set the value in the field of the single record in the epicsdevice
	 *
	 * @param field
	 *            - suffix used to construct the pv name
	 * @param value
	 *            - value to set
	 * @throws DeviceException
	 */
	private void setObjectFieldValue(String field, Object value) throws DeviceException {
		epicsDevice.setValue(SINGLE_RECORD, field, value);
	}

	/**
	 * Helper function to get the value of the field of the single record in the epicsdevice as a double
	 *
	 * @param field
	 *            - suffix used to construct the pv name
	 * @return double
	 * @throws DeviceException
	 */
	private double getDoubleFromField(String field) throws DeviceException {
		try {
			return (double) epicsDevice.getValue(ReturnType.DBR_NATIVE, SINGLE_RECORD, field);
		} catch (Exception e) {
			throw new DeviceException("getDoubleFromField - error for " + field, e);
		}
	}

	/**
	 * Helper function to get the value of the field of the single record in the epicsdevice as an int
	 *
	 * @param field
	 *            - suffix used to construct the pv name
	 * @return int
	 * @throws DeviceException
	 */
	private int getIntFromField(String field) throws DeviceException {
		try {
			return (int) epicsDevice.getValue(ReturnType.DBR_NATIVE, SINGLE_RECORD, field);
		} catch (Exception e) {
			throw new DeviceException("getIntFromField - error for " + field, e);
		}
	}

	/**
	 * Helper function to get the value of the field of the single record in the epicsdevice as a short
	 *
	 * @param field
	 *            - suffix used to construct the pv name
	 * @return short
	 * @throws DeviceException
	 */
	private short getShortFromField(String field) throws DeviceException {
		try {
			return (short) epicsDevice.getValue(ReturnType.DBR_NATIVE, SINGLE_RECORD, field);
		} catch (Exception e) {
			throw new DeviceException("getIntFromField - error for " + field, e);
		}
	}

	/**
	 * Helper function to get the value of the field of the single record in the epicsdevice as a string
	 *
	 * @param field
	 *            - suffix used to construct the pv name
	 * @return String
	 * @throws DeviceException
	 */
	private String getStringFromField(String field) throws DeviceException {
		if (epicsDevice.getDummy()) {
			return (String) epicsDevice.getValue(ReturnType.DBR_NATIVE, SINGLE_RECORD, field);
		}
		return epicsDevice.getValueAsString(SINGLE_RECORD, field);
	}

	public FindableEpicsDevice getEpicsDevice() {
		return epicsDevice;
	}

	public void setEpicsDevice(FindableEpicsDevice epicsDevice) {
		this.epicsDevice = epicsDevice;
	}

	@Override
	public void addRegionOfInterest(int regionIndex, double regionLow, double regionHigh, int regionBackground,
			double regionPreset, String regionName) throws DeviceException {
		try {
			setIntFieldValue(roiLowFields[regionIndex], (int) regionLow);
			setIntFieldValue(roiHighFields[regionIndex], (int) regionHigh);
			setShortFieldValue(roiBackgroundFields[regionIndex], (short) regionBackground);
			if (regionPreset <= 0) {
				setIntFieldValue(roiPresetFields[regionIndex], 0);
			} else {
				setIntFieldValue(roiPresetFields[regionIndex], 1);
				setDoubleFieldValue(roiPresetCountFields[regionIndex], regionPreset);
			}
			if (regionName != null) {
				setStringFieldValue(roiNameFields[regionIndex], regionName);
			}

		} catch (Exception e) {
			throw new DeviceException("failed to add region of interest", e);
		}
	}

	@Override
	public void clear() throws DeviceException {
		setIntFieldValue(ERASE_FIELD, 1);
	}

	/**
	 * Clears the mca, but does not return until the clear has been done.
	 *
	 * @throws DeviceException
	 */
	@Override
	public void clearWaitForCompletion() throws DeviceException {
		clear();
	}

	@Override
	public void deleteRegionOfInterest(int regionIndex) throws DeviceException {
		try {
			setIntFieldValue(roiLowFields[regionIndex], -1);
			setIntFieldValue(roiHighFields[regionIndex], -1);
			setShortFieldValue(roiBackgroundFields[regionIndex], (short)-1);
			setIntFieldValue(roiPresetFields[regionIndex], 0);
			setDoubleFieldValue(roiPresetCountFields[regionIndex], 0);
			setStringFieldValue(roiNameFields[regionIndex], SINGLE_RECORD);
		} catch (Exception e) {
			throw new DeviceException("failed to delete region of interest", e);
		}

	}

	@Override
	public Object getCalibrationParameters() throws DeviceException {
		try {
			final String egu = getStringFromField(UNITS_FIELD);
			final double calo = getDoubleFromField(CALIBRATION_OFFSET_FIELD);
			final double cals = getDoubleFromField(CALIBRATION_SLOPE_FIELD);
			final double calq = getDoubleFromField(CALIBRATION_QUADRATIC_FIELD);
			final double tth = getDoubleFromField(TWO_THETA_FIELD);

			return new EpicsMCACalibration(egu, (float) calo, (float) cals, (float) calq, (float) tth);
		} catch (Exception e) {
			throw new DeviceException("failed to get calibration parameters", e);
		}
	}

	@Override
	public Object getData() throws DeviceException {
		try {
			return epicsDevice.getValue(ReturnType.DBR_NATIVE, SINGLE_RECORD, DATA_FIELD);
		} catch (Exception e) {
			throw new DeviceException("failed to get data", e);
		}
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		//TODO get value of .NUSE
		return new int[] { ArrayUtils.getLength(getData()) };
	}

	/**
	 * Closes currently unused epics channels. Only run this if you suspect you need to as the next attempt to read a
	 * value will re-create the channel.
	 *
	 * @throws DeviceException
	 */
	public void dispose() throws DeviceException {
		if (epicsDevice != null) {
			epicsDevice.dispose();
		}
	}

	@Override
	public Object getElapsedParameters() throws DeviceException {
		try {
			final float[] elapsed = new float[2];
			elapsed[0] = (float) getDoubleFromField(ELAPSED_REAL_TIME_FIELD);
			elapsed[1] = (float) getDoubleFromField(ELAPSED_LIVE_TIME_FIELD);
			return elapsed;
		} catch (Exception e) {
			throw new DeviceException("failed to get elapsed parameters", e);
		}
	}

	/**
	 * Gets the Dwell Time (DWEL).
	 *
	 * @return Dwell Time
	 * @throws DeviceException
	 */
	@Override
	public double getDwellTime() throws DeviceException {
		return getDoubleFromField(DWELL_TIME_FIELD);
	}

	@Override
	public int getNumberOfRegions() throws DeviceException {
		return numberOfRegions;
	}

	@Override
	public Object getPresets() throws DeviceException {
		try {
			final double prtm = getDoubleFromField(PRESET_REAL_TIME_FIELD);
			final double pltm = getDoubleFromField(PRESET_LIVE_TIME_FIELD);
			final int pct = getIntFromField(PRESET_COUNTS_FIELD);
			final int pctl = getIntFromField(PRESET_COUNT_LOW_FIELD);
			final int pcth = getIntFromField(PRESET_COUNT_HIGH_FIELD);
			final int pswp = getIntFromField(PRESET_SWEEP_FIELD);
			return new EpicsMCAPresets((float) prtm, (float) pltm, pct, pctl, pcth, pswp);
		} catch (Exception e) {
			throw new DeviceException("failed to get presets", e);
		}

	}

	@Override
	public Object getRegionsOfInterest() throws DeviceException {
		return getRegionsOfInterest(numberOfRegions);
	}

	public int getNumberOfValsPerRegionOfInterest() {
		return readNetCounts ? 2 : 1;
	}

	public int getIndexForRawROI() {
		return INDEX_FOR_RAW_ROI;
	}

	public boolean isReadNetCounts() {
		return readNetCounts;
	}

	public void setReadNetCounts(boolean readNetCounts) {
		this.readNetCounts = readNetCounts;
	}

	@Override
	public double getRoiCount(int index) throws DeviceException{
		return getDoubleFromField(roiCountFields[index]);
	}
	@Override
	public double getRoiNetCount(int index) throws DeviceException{
		return getDoubleFromField(roiNetCountFields[index]);
	}
	@Override
	public double[][] getRegionsOfInterestCount() throws DeviceException {
		try {
			final double[][] regionsCount = new double[numberOfRegions][getNumberOfValsPerRegionOfInterest()];
			for (int i = 0; i < regionsCount.length; i++) {
				regionsCount[i][0] = getDoubleFromField(roiCountFields[i]);
				roiCountValues[i] = regionsCount[i][0];
				if (isReadNetCounts()) {
					regionsCount[i][1] = getDoubleFromField(roiNetCountFields[i]);
					roiNetCountValues[i] = regionsCount[i][1];
				}
			}
			return regionsCount;
		} catch (Exception e) {
			throw new DeviceException("EpicsMCA.getRegionsOfInterestCount:failed to get region of interest count", e);
		}
	}

	@Override
	public long getSequence() throws DeviceException {
		try {
			return getIntFromField(SEQ_FIELD);
		} catch (Exception e) {
			throw new DeviceException("EpicsMCA.getSequence:failed to get sequence", e);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		try {
			// we need to fire the PROC to ensure the RDGN field is updated
			setIntFieldValueNoWait(PROC_FIELD, 1);
			return (acquisitionDone && readingDone) ? Detector.IDLE : Detector.BUSY;
		} catch (Exception e) {
			throw new DeviceException("EpicsMCA.getStatus: failed to get status", e);
		}
	}

	@Override
	public void setCalibration(EpicsMCACalibration calibrate) throws DeviceException {
		setCalibration((Object)calibrate);
	}
	@Override
	public void setCalibration(Object calibrate) throws DeviceException {
		try {
			final EpicsMCACalibration calib = (EpicsMCACalibration) calibrate;
			setStringFieldValue(UNITS_FIELD, calib.getEngineeringUnits());
			setDoubleFieldValue(CALIBRATION_OFFSET_FIELD, calib.getCalibrationOffset());
			setDoubleFieldValue(CALIBRATION_SLOPE_FIELD, calib.getCalibrationSlope());
			setDoubleFieldValue(CALIBRATION_QUADRATIC_FIELD, calib.getCalibrationQuadratic());
			setDoubleFieldValue(TWO_THETA_FIELD, calib.getTwoThetaAngle());
		} catch (Exception e) {
			throw new DeviceException("EpicsMCA.setCalibration: failed to set calibration", e);
		}
	}

	@Override
	public void setData(Object data) throws DeviceException {
		try {
			setObjectFieldValue(DATA_FIELD, data);
		} catch (Exception e) {
			throw new DeviceException("EpicsMCA.setData: failed to set data", e);
		}
	}

	/**
	 * Sets the dwell time (DWEL)
	 *
	 * @param time
	 * @throws DeviceException
	 */
	@Override
	public void setDwellTime(double time) throws DeviceException {
		// The dwell time appears to be changed automatically to 0
		setDoubleFieldValue(DWELL_TIME_FIELD, time);
	}

	@Override
	public void setNumberOfRegions(int numberOfRegions) throws DeviceException {
		if (isConfigured()) {
			throw new DeviceException("Unable to set numberOfRegions once configured");
		}
		if (numberOfRegions > MAX_NUMBER_OF_REGIONS || numberOfRegions < 1) {
			throw new DeviceException("numberOfRegions must be between 1 and " + MAX_NUMBER_OF_REGIONS);
		}
		this.numberOfRegions = numberOfRegions;
	}

	@Override
	public void setPresets(Object data) throws DeviceException {
		try {
			final EpicsMCAPresets preset = (EpicsMCAPresets) data;
			setDoubleFieldValue(PRESET_REAL_TIME_FIELD, preset.getPresetRealTime());
			setDoubleFieldValue(PRESET_LIVE_TIME_FIELD, preset.getPresetLiveTime());
			setIntFieldValue(PRESET_COUNTS_FIELD, (int) preset.getPresetCounts());
			setIntFieldValue(PRESET_COUNT_LOW_FIELD, (int) preset.getPresetCountlow());
			setIntFieldValue(PRESET_COUNT_HIGH_FIELD, (int) preset.getPresetCountHigh());
			setIntFieldValue(PRESET_SWEEP_FIELD, (int) preset.getPresetSweeps());
		} catch (Exception e) {
			throw new DeviceException("failed to set presets", e);
		}
	}

	@Override
	public void setRegionsOfInterest(EpicsMCARegionOfInterest[] epicsMcaRois) throws DeviceException {
		setRegionsOfInterest((Object)epicsMcaRois);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see gda.device.Analyser#setRegionsOfInterest(java.lang.Object) * the input parameter highLow object should
	 *      actually be an array of EpicsMCARegionsOfInterest objects
	 */
	@Override
	public void setRegionsOfInterest(Object highLow) throws DeviceException {
		try {
			final EpicsMCARegionOfInterest[] rois = (EpicsMCARegionOfInterest[]) highLow;
			for (int i = 0; i < rois.length; i++) {
				final int regionIndex = rois[i].getRegionIndex();
				setIntFieldValue(roiLowFields[regionIndex], (int)rois[i].getRegionLow());
				setIntFieldValue(roiHighFields[regionIndex], (int)rois[i].getRegionHigh());
				setShortFieldValue(roiBackgroundFields[regionIndex], (short)rois[i].getRegionBackground());
				final double regionPreset = rois[i].getRegionPreset();
				if (regionPreset <= 0)
					setIntFieldValue(roiPresetFields[regionIndex], 0);
				else {
					setIntFieldValue(roiPresetFields[regionIndex], 1);
				}
				setDoubleFieldValue(roiPresetCountFields[regionIndex], regionPreset);
				setStringFieldValue(roiNameFields[regionIndex], rois[i].getRegionName());
			}
		} catch (Exception e) {
			throw new DeviceException("failed to set region of interest", e);
		}
	}

	@Override
	public void setSequence(long sequence) throws DeviceException {
		try {
			setIntFieldValue(SEQ_FIELD, (int) sequence);
		} catch (Exception e) {
			throw new DeviceException("failed to set sequence", e);
		}
	}

	/**
	 * Activates the MCA using the Erase & Start button.
	 *
	 * @throws DeviceException
	 */
	@Override
	public void eraseStartAcquisition() throws DeviceException {
		try {
			readingDone = false;
			acquisitionDone = false;
			setIntFieldValueNoWait(ERASE_START_ACQ_FIELD, 1);
			if (epicsDevice.getDummy()) {
				Thread.sleep((long) (getCollectionTime() * 1000));
				_fireReadingDone();
			}
		} catch (Exception e) {
			throw new DeviceException("failed to start acquisition", e);
		}
	}

	@Override
	public void startAcquisition() throws DeviceException {
		try {
			setIntFieldValueNoWait(START_ACQ_FIELD, 1);
			acquisitionDone = false;
			readingDone = false;
			if (epicsDevice.getDummy()) {
				Thread.sleep((long) (getCollectionTime() * 1000));
				_fireReadingDone();
			}
		} catch (Exception e) {
			throw new DeviceException("failed to start acquisition", e);
		}
	}

	@Override
	public void stopAcquisition() throws DeviceException {
		try {
			setIntFieldValue(STOP_ACQ_FIELD, 1);
			if (epicsDevice.getDummy()) {
				_fireReadingDone();
			}
		} catch (Exception e) {
			throw new DeviceException("failed to stop acquisition", e);
		}

	}

	/**
	 * method used for testing only
	 */
	public void _fireReadingDone() {
		acquisitionDone = true;
		setReadingDone(true);
		notifyIObservers(this, (acquisitionDone && readingDone) ? MCAStatus.READY : MCAStatus.BUSY);
	}

	/**
	 * method used for testing only
	 */
	public void _setRegionsOfInterestCount(int index, Double val) throws DeviceException {
		setObjectFieldValue(roiCountFields[index], val);
	}

	/**
	 * method used for testing only
	 */
	public void _setRegionsOfInterestNetCount(int index, Double val) throws DeviceException {
		setObjectFieldValue(roiNetCountFields[index], val);
	}

	private void setReadingDone(boolean readingDone){
		synchronized (doneLock) {
			this.readingDone = readingDone;
			doneLock.notifyAll();
		}
	}

	private void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof EpicsRegistrationRequest && changeCode instanceof EpicsMonitorEvent
				&& ((EpicsMonitorEvent) changeCode).epicsDbr instanceof DBR) {
			final EpicsMonitorEvent event = (EpicsMonitorEvent) changeCode;
			final DBR dbr = (DBR) event.epicsDbr;
			if (dbr != null) {
				if (((EpicsRegistrationRequest) theObserved).field.equals(ACQUIRING_FIELD) && dbr.isENUM()) {
					acquisitionDone = ((DBR_Enum) dbr).getEnumValue()[0] == 0;
					logger.debug("update acquisitionDone = {}", acquisitionDone);
					if (acquisitionDone) {
						if(readingDoneIfNotAquiring){
							setReadingDone(true);
						} else {
							readingDone = false;
							logger.debug("readingDone set to false");
							// don't do the CA put on the JCA event dispatch thread
							Async.execute(() -> {
								try {
									// now ask for a read and set ReadingDone false
									logger.debug("Requesting read");
									setIntFieldValue(READ_FIELD, 1);
								} catch (Exception e) {
									logger.error("Error setting read to 1 in response to acquisition done", e);
								}
							});
						}
					}
				} else if (((EpicsRegistrationRequest) theObserved).field.equals(READING_FIELD) && dbr.isENUM()) {
					setReadingDone(((DBR_Enum) dbr).getEnumValue()[0] == 0);
					logger.debug("update readingDone = {}", readingDone);
					notifyIObservers(this, (acquisitionDone && readingDone) ? MCAStatus.READY : MCAStatus.BUSY);
				}
			}
		}
	}

	@Override
	public EpicsMCARegionOfInterest getNthRegionOfInterest(int regionIndex) throws DeviceException {
		final EpicsMCARegionOfInterest rois = new EpicsMCARegionOfInterest();
		rois.setRegionIndex(regionIndex);
		rois.setRegionLow(getIntFromField(roiLowFields[regionIndex]));
		rois.setRegionHigh(getIntFromField(roiHighFields[regionIndex]));
		rois.setRegionBackground(getShortFromField(roiBackgroundFields[regionIndex]));
		rois.setRegionPreset(getDoubleFromField(roiPresetCountFields[regionIndex]));
		rois.setRegionName(getStringFromField(roiNameFields[regionIndex]));
		return rois;
	}

	private Object getRegionsOfInterest(int noOfRegions) throws DeviceException {
		final List<EpicsMCARegionOfInterest> roiVector = new ArrayList<>();
		for (int regionIndex = 0; regionIndex < noOfRegions; regionIndex++) {
			final EpicsMCARegionOfInterest rois = getNthRegionOfInterest(regionIndex);
			roiVector.add(rois);
		}
		if (!roiVector.isEmpty()) {
			final EpicsMCARegionOfInterest[] selectedrois = new EpicsMCARegionOfInterest[roiVector.size()];
			for (int j = 0; j < selectedrois.length; j++) {
				selectedrois[j] = roiVector.get(j);
			}
			return selectedrois;
		}
		return null;
	}

	@Override
	public long getNumberOfChannels() throws DeviceException {
		return getIntFromField(NUMBER_OF_CHANNELS_TO_USE_FIELD);
	}

	@Override
	public void setNumberOfChannels(long channels) throws DeviceException {
		final long max = getIntFromField(MAX_NUMBER_OF_CHANNELS_TO_USE_FIELD);
		if (channels > max) {
			throw new DeviceException("Invalid number of channels," + " Maximum channels allowed is  " + max);
		}
		setIntFieldValue(NUMBER_OF_CHANNELS_TO_USE_FIELD, (int) channels);
	}

	@Override
	public void collectData() throws DeviceException {
		clear();

		final EpicsMCAPresets presets = (EpicsMCAPresets) getPresets();

		if (presets.getPresetLiveTime() > 0.0 && this.collectionTime > 0.0) {
			startAcquisition();
		} else {
			stopAcquisition(); // throws an exception?
		}

	}

	@Override
	public Object readout() throws DeviceException {
		return getData();
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if (attributeName.startsWith(CHANNEL_TO_ENERGY_PREFIX)) {
			final String channelString = attributeName.substring(CHANNEL_TO_ENERGY_PREFIX.length());
			final double energy = getEnergyForChannel(Integer.parseInt(channelString));
			return String.format("%f %s", energy, ELECTRON_VOLT);
		} else if (attributeName.startsWith(ENERGY_TO_CHANNEL_PREFIX)) {
			final String energyString = attributeName.substring(ENERGY_TO_CHANNEL_PREFIX.length());
			final double energy = Amount.valueOf(energyString).getEstimatedValue();
			return Integer.toString(getChannelForEnergy(energy));

		} else if (attributeName.equals(NUMBER_OF_CHANNELS_ATTR)) {
			return getNumberOfChannels();

		} else {
			return super.getAttribute(attributeName);
		}
	}

	public double getEnergyForChannel(int channel) throws DeviceException {
		ensureQuantityConverterExists();
		try {
			return channelToEnergyConverter.toSource(Amount.valueOf(channel, Unit.ONE)).getEstimatedValue();
		} catch (Exception e) {
			throw new DeviceException(String.format("Error getting energy for channel %d", channel), e);
		}
	}

	public int getChannelForEnergy(double energy) throws DeviceException {
		ensureQuantityConverterExists();
		try {
			final Amount<? extends Quantity> channel = channelToEnergyConverter.toTarget(Amount.valueOf(energy, ELECTRON_VOLT));
			return (int) Math.max(Math.min(channel.getExactValue(), getNumberOfChannels() - 1), 0);
		} catch (Exception e) {
			throw new DeviceException(String.format("Error getting channel for energy %s", energy), e);
		}
	}

	private void ensureQuantityConverterExists() throws DeviceException {
		if (channelToEnergyConverter != null) {
			return;
		}
		if (converterName == null || converterName.length() == 0) {
			throw new DeviceException("Cannot create channel/energy converter: no name specified");
		}
		final IQuantitiesConverter converter = CoupledConverterHolder.FindQuantitiesConverter(converterName);
		if (converter == null) {
			throw new DeviceException(String.format("Cannot create channel/energy converter: %s not found", converterName));
		}
		if (converter instanceof IQuantityConverter) {
			channelToEnergyConverter = (IQuantityConverter) converter;
		} else {
			throw new DeviceException(String.format("Cannot create channel/energy converter: %s is of the wrong type", converterName));
		}
	}

	/**
	 * @return converter name
	 */
	public String getCalibrationName() {
		return converterName;
	}

	/**
	 * @param calibrationName
	 */
	public void setCalibrationName(String calibrationName) {
		this.converterName = calibrationName;
	}

	/**
	 * @return String
	 */
	public String getEpicsDeviceName() {
		return epicsDeviceName;
	}

	/**
	 * @param deviceName
	 */
	public void setEpicsDeviceName(String deviceName) {
		this.epicsDeviceName = deviceName;
	}

	/**
	 * @return String
	 */
	public String getMcaPV() {
		return mcaPV;
	}

	/**
	 * @param mcaPV
	 */
	@Override
	public void setMcaPV(String mcaPV) {
		this.mcaPV = mcaPV;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "EPICS Mca";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "EPICS";
	}


	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		synchronized (doneLock) {
			while (!(acquisitionDone && readingDone)) {
				doneLock.wait();
			}
		}
	}

	public boolean isReadingDoneIfNotAquiring() {
		return readingDoneIfNotAquiring;
	}

	public void setReadingDoneIfNotAquiring(boolean readingDoneIfNotAquiring) {
		this.readingDoneIfNotAquiring = readingDoneIfNotAquiring;
	}
}
