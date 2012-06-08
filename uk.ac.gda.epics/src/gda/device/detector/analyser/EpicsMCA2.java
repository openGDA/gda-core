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

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.MCAStatus;
import gda.device.detector.DetectorBase;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import gda.util.converters.CoupledConverterHolder;
import gda.util.converters.IQuantitiesConverter;
import gda.util.converters.IQuantityConverter;

import java.lang.reflect.Array;
import java.util.Vector;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to communicate with an epics MCA record. The MCA record controls and acquires data from a multi-channel
 * analyser (MCA).
 */
public class EpicsMCA2 extends DetectorBase implements Detector, IObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsMCA2.class);

	private static final long serialVersionUID = 1L;

	private IQuantitiesConverter channelToEnergyConverter = null;

	private String converterName = "mca_roi_conversion";

	private EpicsMcaController controller;

	/**
	 * phase I interface GDA-EPICS link name
	 */
	private String epicsMcaRecordName;

	/**
	 * EPICS record name for a MCA - the long name e.g.BL11I-....
	 */
	private String mcaRecordName;

	/**
	 * phase II interface GDA-EPICS link parameter
	 */
	private String deviceName;

	private Object timeLock;

	private double elapsedRealTimeValue;

	private double elapsedLiveTimeValue;

	private double[] roiLowValues;

	private double[] roiHighValues;

	private int[] roiBackgroundValues;

	private double[] roiCountValues;

	private double[] roiNetCountValues;

	private double[] roiPresetCountValues;

	private String[] roiNameValues;

	private int numberOfRegion;

	private int status = Detector.STANDBY;

	private int[] data;

	private Double averageDeadTimeValue;

	private Double instantaneousDeadTimeValue;

	/**
	 * Constructor
	 */
	public EpicsMCA2() {
		controller = new EpicsMcaController();
		numberOfRegion = controller.getNumberOfRegions();
		roiLowValues = new double[numberOfRegion];
		roiHighValues = new double[numberOfRegion];
		roiBackgroundValues = new int[numberOfRegion];
		roiCountValues = new double[numberOfRegion];
		roiNetCountValues = new double[numberOfRegion];
		roiPresetCountValues = new double[numberOfRegion];
		roiNameValues = new String[numberOfRegion];
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (getEpicsMcaRecordName() != null) {
				controller.setEpicsMcaRecordName(epicsMcaRecordName);
			} else if (getDeviceName() != null) {
				controller.setDeviceName(deviceName);
			} else if (getMcaRecordName() != null) {
				controller.setMcaRecordName(mcaRecordName);
			} else {
				logger.error("Missing EPICS configuration for MCA {}", getName());
				throw new FactoryException("Missing EPICS configuration for MCA " + getName());
			}

			controller.configure();
			controller.addIObserver(this);
			configured = true;
		}
	}

	@Override
	public void collectData() throws DeviceException {
		controller.clear();

		// check that a preset time has been set
		EpicsMCAPresets presets = getPresets();

		// if it has, then begin collection
		if (presets.getPresetLiveTime() > 0.0 && this.collectionTime > 0.0) {
			controller.startAcquisition();
		}
		// otherwise, stop collection and tell user, so any scans do not
		// continue
		// forever
		else {
			controller.stopAcquisition(); // throws an exception?
		}

	}

	/**
	 * adds a region of interest.
	 * 
	 * @param regionIndex
	 * @param regionLow
	 * @param regionHigh
	 * @param regionBackground
	 * @param regionPreset
	 * @param regionName
	 * @throws DeviceException
	 */
	public void addRegionOfInterest(int regionIndex, int regionLow, int regionHigh, int regionBackground,
			int regionPreset, String regionName) throws DeviceException {
		try {
			controller.setRegionLowChannel(regionIndex, regionLow);
			controller.setRegionHighChannel(regionIndex, regionHigh);
			controller.setRegionBackground(regionIndex, regionBackground);
			if (regionPreset <= 0) {
				controller.setRegionPreset(regionIndex, false);
			} else {
				controller.setRegionPreset(regionIndex, true);
				controller.setRegionPresetCount(regionIndex, regionPreset);

			}
			if (regionName != null) {
				controller.setRegionName(regionIndex, regionName);
			}

		} catch (Throwable th) {
			throw new DeviceException("failed to add region of interest", th);
		}
	}

	/**
	 * deletes a region of interest
	 * 
	 * @param regionIndex
	 * @throws DeviceException
	 */
	public void deleteRegionOfInterest(int regionIndex) throws DeviceException {
		try {
			controller.setRegionLowChannel(regionIndex, -1);
			controller.setRegionHighChannel(regionIndex, -1);
			controller.setRegionBackground(regionIndex, -1);
			controller.setRegionPreset(regionIndex, false);
			controller.setRegionPresetCount(regionIndex, 0);
			controller.setRegionName(regionIndex, "");
		} catch (Throwable th) {
			throw new DeviceException("failed to delete region of interest", th);
		}
	}

	/**
	 * returns calibration parameters - unit name, offset, slope, quadratic, and two theta angle of the detector
	 * 
	 * @return calibration parameters
	 * @throws DeviceException
	 */
	public Object getCalibrationParameters() throws DeviceException {
		try {
			String egu = controller.getCalibrationUnitsName();
			double calo = controller.getCalibrationOffset();
			double cals = controller.getCalibrationSlope();
			double calq = controller.getCalibrationQuadratic();
			double tth = controller.getTwoTheta();

			return new EpicsMCACalibration(egu, (float) calo, (float) cals, (float) calq, (float) tth);
		} catch (Throwable th) {
			throw new DeviceException("failed to get calibration parameters", th);
		}
	}

	/**
	 * @return EpicsMCAPresets
	 * @throws DeviceException
	 */
	public EpicsMCAPresets getPresets() throws DeviceException {
		try {
			float prtm = controller.getPresetRealTime();
			float pltm = controller.getPresetLiveTime();
			int pct = controller.getPresetCounts();
			int pctl = controller.getPresetCountLow();
			int pcth = controller.getPresetCountHigh();
			int pswp = controller.getPresetSweep();

			return new EpicsMCAPresets(prtm, pltm, pct, pctl, pcth, pswp);
		} catch (Throwable th) {
			throw new DeviceException("failed to get presets", th);
		}
	}

	/**
	 * Clears the mca, but does not return until the clear has been done.
	 * 
	 * @throws DeviceException
	 */
	public void clear() throws DeviceException {
		try {
			// you cannot call caPutWait unless the PROC field is being fired in
			// a different thread or independently e.g. by EPICS
			controller.clear();

		} catch (Throwable th) {
			throw new DeviceException("EpicsMCA.clearWaitForCompletion: exception seen - " + th.getMessage(), th);
		}
	}

	/**
	 * gets elapsed parameters
	 * 
	 * @return elapsed parameters
	 * @throws DeviceException
	 */
	public Object getElapsedParameters() throws DeviceException {
		try {

			double[] elapsed = new double[2];
			elapsed[0] = controller.getElapsedRealTime();
			elapsed[1] = controller.getElapsedLiveTime();

			synchronized (timeLock) {
				elapsedRealTimeValue = elapsed[0];
				elapsedLiveTimeValue = elapsed[1];
			}
			return elapsed;
		} catch (Throwable th) {
			throw new DeviceException("failed to get elapsed parameters", th);
		}
	}

	private Object getCachedElapsedParameters() {
		double[] elapsed = new double[2];
		synchronized (timeLock) {
			// TODO fix this
			elapsed[0] = elapsedRealTimeValue;
			elapsed[1] = elapsedLiveTimeValue;
		}
		return elapsed;
	}

	/**
	 * gets regions of interest
	 * 
	 * @return region of interest
	 * @throws DeviceException
	 */
	public Object getRegionsOfInterest() throws DeviceException {
		return getRegionsOfInterest(controller.getNumberOfRegions());
	}

	/**
	 * gets the total and net counts for each region of interest
	 * 
	 * @return total and net counts for regions of interest
	 * @throws DeviceException
	 */
	public double[][] getRegionsOfInterestCount() throws DeviceException {
		try {

			double[][] regionsCount = new double[numberOfRegion][2];
			for (int i = 0; i < regionsCount.length; i++) {
				regionsCount[i][0] = controller.getRegionCounts(i);
				roiCountValues[i] = regionsCount[i][0];
				regionsCount[i][1] = controller.getRegionNetCounts(i);
				roiNetCountValues[i] = regionsCount[i][1];
			}
			return regionsCount;
		} catch (Throwable th) {
			throw new DeviceException("EpicsMCA.getRegionsOfInterestCount:failed to get region of interest count", th);
		}
	}

	/**
	 * sets calibration fields for MCA
	 * 
	 * @param calibrate
	 * @throws DeviceException
	 */
	public void setCalibration(Object calibrate) throws DeviceException {
		try {
			EpicsMCACalibration calib = (EpicsMCACalibration) calibrate;

			controller.setCalibrationUnitsName(calib.getEngineeringUnits());
			controller.setCalibrationOffset(calib.getCalibrationOffset());
			controller.setCalibrationSlope(calib.getCalibrationSlope());
			controller.setCalibrationQuadratic(calib.getCalibrationQuadratic());
			controller.setTwoTheta(calib.getTwoThetaAngle());

		} catch (Throwable th) {
			throw new DeviceException("EpicsMCA.setCalibration: failed to set calibration", th);
		}
	}

	/**
	 * sets the preset parameters for MCA
	 * 
	 * @param data
	 * @throws DeviceException
	 */
	public void setPresets(Object data) throws DeviceException {
		try {
			EpicsMCAPresets preset = (EpicsMCAPresets) data;
			controller.setPresetRealTime(preset.getPresetRealTime());
			controller.setPresetLiveTime(preset.getPresetLiveTime());
			controller.setPresetCounts((int) preset.getPresetCounts());
			controller.setPresetCountLow((int) preset.getPresetCountlow());
			controller.setPresetCountHigh((int) preset.getPresetCountHigh());
			controller.setPresetSweep((int) preset.getPresetSweeps());

		} catch (Throwable th) {
			throw new DeviceException("failed to set presets", th);
		}
	}

	/**
	 * sets region of interest
	 * 
	 * @param highLow
	 * @throws DeviceException
	 */
	public void setRegionsOfInterest(Object highLow) throws DeviceException {
		try {
			EpicsMCARegionOfInterest[] rois = (EpicsMCARegionOfInterest[]) highLow;
			for (int i = 0; i < rois.length; i++) {
				int regionIndex = rois[i].getRegionIndex();
				controller.setRegionLowChannel(regionIndex, (int) rois[i].getRegionLow());
				controller.setRegionHighChannel(regionIndex, (int) rois[i].getRegionHigh());
				controller.setRegionBackground(regionIndex, rois[i].getRegionBackground());
				double regionPreset = rois[i].getRegionPreset();
				if (regionPreset <= 0)
					controller.setRegionPreset(regionIndex, false);
				else {
					controller.setRegionPreset(regionIndex, true);
				}
				controller.setRegionPresetCount(regionIndex, regionPreset);
				controller.setRegionName(regionIndex, rois[i].getRegionName());

			}

		} catch (Throwable th) {
			throw new DeviceException("failed to set region of interest", th);
		}
	}

	@SuppressWarnings("unused")
	private Object getNthRegionOfInterest(int regionIndex) throws DeviceException {
		try {

			EpicsMCARegionOfInterest rois = new EpicsMCARegionOfInterest();

			rois.setRegionIndex(regionIndex);

			roiLowValues[regionIndex] = controller.getRegionLowChannel(regionIndex);
			rois.setRegionLow(roiLowValues[regionIndex]);
			roiHighValues[regionIndex] = controller.getRegionHighChannel(regionIndex);
			rois.setRegionHigh(roiHighValues[regionIndex]);
			roiBackgroundValues[regionIndex] = controller.getRegionBackground(regionIndex);
			rois.setRegionBackground(roiBackgroundValues[regionIndex]);
			roiPresetCountValues[regionIndex] = controller.getRegionPresetCount(regionIndex);
			rois.setRegionPreset(roiPresetCountValues[regionIndex]);
			roiNameValues[regionIndex] = controller.getRegionName(regionIndex);
			rois.setRegionName(roiNameValues[regionIndex]);
			return rois;
		} catch (Throwable th) {
			throw new DeviceException("failed to get Nth region of interest", th);
		}
	}

	@SuppressWarnings("unused")
	private Object getCachedNthRegionsOfInterest(int regionIndex) {
		EpicsMCARegionOfInterest rois = new EpicsMCARegionOfInterest();

		rois.setRegionIndex(regionIndex);
		rois.setRegionLow(roiLowValues[regionIndex]);
		rois.setRegionHigh(roiHighValues[regionIndex]);
		rois.setRegionBackground(roiBackgroundValues[regionIndex]);
		rois.setRegionPreset(roiPresetCountValues[regionIndex]);
		rois.setRegionName(roiNameValues[regionIndex]);
		return rois;
	}

	@SuppressWarnings("unused")
	private double[] getNthRegionOfInterestCount(int regionIndex) {
		try {

			double[] regionsCount = new double[2];
			roiCountValues[regionIndex] = controller.getRegionCounts(regionIndex);
			roiNetCountValues[regionIndex] = controller.getRegionNetCounts(regionIndex);
			regionsCount[0] = roiCountValues[regionIndex];
			regionsCount[1] = roiNetCountValues[regionIndex];
			return regionsCount;
		} catch (Throwable th) {
			throw new RuntimeException("failed to get Nth region of interest count", th);
		}
	}

	@SuppressWarnings("unused")
	private double[] getCachedNthRegionOfInterestCount(int regionIndex) {
		double[] regionsCount = new double[2];
		regionsCount[0] = roiCountValues[regionIndex];
		regionsCount[1] = roiNetCountValues[regionIndex];
		return regionsCount;
	}

	@Override
	public int getStatus() throws DeviceException {

		MCAStatus s = controller.getStatus();
		if (s == MCAStatus.BUSY) {
			status = Detector.BUSY;
		} else if (s == MCAStatus.READY) {
			status = Detector.IDLE;
		} else if (s == MCAStatus.FAULT) {
			status = Detector.FAULT;
		}
		return status;
	}

	@Override
	public Object readout() throws DeviceException {
		return controller.getData();
	}

	private Object getRegionsOfInterest(int noOfRegions) throws DeviceException {

		try {

			Vector<EpicsMCARegionOfInterest> roiVector = new Vector<EpicsMCARegionOfInterest>();
			for (int regionIndex = 0; regionIndex < noOfRegions; regionIndex++) {
				EpicsMCARegionOfInterest rois = new EpicsMCARegionOfInterest();

				rois.setRegionIndex(regionIndex);

				int low = controller.getRegionLowChannel(regionIndex);
				if (low >= 0) {
					rois.setRegionLow(low);
					roiLowValues[regionIndex] = low;
				}
				int high = controller.getRegionHighChannel(regionIndex);
				if (high >= rois.getRegionLow()) {
					rois.setRegionHigh(high);
					roiHighValues[regionIndex] = high;
				}
				roiBackgroundValues[regionIndex] = controller.getRegionBackground(regionIndex);
				rois.setRegionBackground(roiBackgroundValues[regionIndex]);
				roiPresetCountValues[regionIndex] = controller.getRegionPresetCount(regionIndex);
				rois.setRegionPreset(roiPresetCountValues[regionIndex]);
				roiNameValues[regionIndex] = controller.getRegionName(regionIndex);
				rois.setRegionName(roiNameValues[regionIndex]);
				roiVector.add(rois);
			}
			if (roiVector.size() != 0) {
				EpicsMCARegionOfInterest[] selectedrois = new EpicsMCARegionOfInterest[roiVector.size()];
				for (int j = 0; j < selectedrois.length; j++) {
					selectedrois[j] = roiVector.get(j);
				}
				return selectedrois;
			}
			return null;
		} catch (Throwable th) {
			throw new DeviceException("failed get regions of interest", th);
		}

	}

	/**
	 * Gets the MCA record name - shared name between GDA and EPICS for phase I beamlines.
	 * 
	 * @return the Epics MCA record name.
	 */
	public String getEpicsMcaRecordName() {
		return epicsMcaRecordName;
	}

	/**
	 * Sets the MCA record name - shared name between GDA and EPICS for phase I beamlines.
	 * 
	 * @param epicsMcaRecordName
	 */
	public void setEpicsMcaRecordName(String epicsMcaRecordName) {
		this.epicsMcaRecordName = epicsMcaRecordName;
	}

	/**
	 * gets the device name shared by GDA and EPICS
	 * 
	 * @return device name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * sets the device name which is shared by GDA and EPICS
	 * 
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * gets MCA record name - the actual EPICS Record name after remove field name
	 * 
	 * @return MCA record name
	 */
	public String getMcaRecordName() {
		return mcaRecordName;
	}

	/**
	 * sets MCA record name - the actual EPICS Record name after remove field name
	 * 
	 * @param mcaRecordName
	 */
	public void setMcaRecordName(String mcaRecordName) {
		this.mcaRecordName = mcaRecordName;
	}

	/**
	 * 
	 */
	public static final String channelToEnergyPrefix = "channelToEnergy:";
	/**
	 * 
	 */
	public static final String numberOfChannelsAttr = "NumberOfChannels";
	/**
	 * 
	 */
	public static final String energyToChannelPrefix = "energyToChannel";

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {

		if (attributeName.startsWith(channelToEnergyPrefix)) {
			String energy = null;
			if (channelToEnergyConverter == null && converterName != null) {
				channelToEnergyConverter = CoupledConverterHolder.FindQuantitiesConverter(converterName);
			}
			if (channelToEnergyConverter != null && channelToEnergyConverter instanceof IQuantityConverter) {
				String channelString = attributeName.substring(channelToEnergyPrefix.length());
				Quantity channel = Quantity.valueOf(channelString);
				try {
					energy = ((IQuantityConverter) channelToEnergyConverter).toSource(channel).toString();
					return energy;
				} catch (Exception e) {
					throw new DeviceException("EpicsMCA.getAttribute exception", e);
				}
			}
			throw new DeviceException(
					"EpicsMCA : unable to find suitable converter to convert channel to energy. converterName  "
							+ converterName == null ? "not given" : converterName);
		} else if (attributeName.startsWith(energyToChannelPrefix)) {
			String channel = null;
			if (channelToEnergyConverter == null && converterName != null) {
				channelToEnergyConverter = CoupledConverterHolder.FindQuantitiesConverter(converterName);
			}
			if (channelToEnergyConverter != null && channelToEnergyConverter instanceof IQuantityConverter) {
				String energyString = attributeName.substring(energyToChannelPrefix.length());
				Quantity energy = Quantity.valueOf(energyString);
				try {
					channel = ((IQuantityConverter) channelToEnergyConverter).toTarget(energy).toString();
					return channel;
				} catch (Exception e) {
					throw new DeviceException("EpicsMCA.getAttribute exception", e);
				}
			}
			throw new DeviceException(
					"EpicsTCA : unable to find suitable converter to convert energy to channel. converterName  "
							+ converterName == null ? "not given" : converterName);
		} else if (attributeName.equals(numberOfChannelsAttr)) {
			return 1024;// getNumberOfChannels();
		} else {
			return super.getAttribute(attributeName);
		}
	}


	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved == controller) {
			if (theObserved instanceof EpicsMcaController.RDNGMonitorListener) {
				MCAStatus s = (MCAStatus) changeCode;
				if (s == MCAStatus.BUSY) {
					status = Detector.BUSY;
				} else if (s == MCAStatus.READY) {
					status = Detector.IDLE;
				} else if (s == MCAStatus.FAULT) {
					status = Detector.FAULT;
				}
				notifyIObservers(this, s);
			} else if (theObserved instanceof EpicsMcaController.VALMonitorListener) {
				data = (int[]) changeCode;
				notifyIObservers(this, data);
			} else if (theObserved instanceof EpicsMcaController.RealTimeMonitorListener) {
				elapsedRealTimeValue = (Double) changeCode;
				notifyIObservers(this, getCachedElapsedParameters());
			} else if (theObserved instanceof EpicsMcaController.LiveTimeMonitorListener) {
				elapsedLiveTimeValue = (Double) changeCode;
				notifyIObservers(this, getCachedElapsedParameters());
			} else if (theObserved instanceof EpicsMcaController.DeadTimeMonitorListener) {
				averageDeadTimeValue = (Double) changeCode;
				notifyIObservers(this, averageDeadTimeValue);
			} else if (theObserved instanceof EpicsMcaController.InstDeadTimeMonitorListener) {
				instantaneousDeadTimeValue = (Double) changeCode;
				notifyIObservers(this, instantaneousDeadTimeValue);
			} else if (theObserved instanceof EpicsMcaController.RoiCountsMonitorListener) {
				int n = Array.getInt(changeCode, 0);
				roiCountValues[n] = Array.getDouble(changeCode, 1);
				notifyIObservers(this, roiCountValues);
			} else if (theObserved instanceof EpicsMcaController.RoiNetCountsMonitorListener) {
				int n = Array.getInt(changeCode, 0);
				roiCountValues[n] = Array.getDouble(changeCode, 1);
				notifyIObservers(this, roiNetCountValues);
			}
		}
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

}
