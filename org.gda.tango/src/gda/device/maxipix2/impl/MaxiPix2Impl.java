/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.maxipix2.impl;

import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.ErrSeverity;
import gda.device.Scannable;
import gda.device.base.impl.BaseImpl;
import gda.device.maxipix2.MaxiPix2EnergyCalibration;
import gda.device.maxipix2.MaxiPix2;
import gda.factory.FactoryException;

public class MaxiPix2Impl extends BaseImpl implements MaxiPix2 {

	private static final String READY_MODE_EXPOSURE_READOUT = "EXPOSURE_READOUT";
	private static final String READY_MODE_EXPOSURE = "EXPOSURE";
	private static final String GATE_MODE_ACTIVE = "ACTIVE";
	private static final String GATE_MODE_INACTIVE = "INACTIVE";
	private static final String LEVEL_LOW_FALL = "LOW_FALL";
	private static final String LEVEL_HIGH_RISE = "HIGH_RISE";
	private static final String FILL_MODE_RAW = "RAW";
	private static final String FILL_MODE_ZERO = "ZERO";
	private static final String FILL_MODE_DISPATCH = "DISPATCH";
	private static final String FILL_MODE_MEAN = "MEAN";
	private static final String ATTRIBUTE_CONFIG_NAME = "config_name";
	private static final String ATTRIBUTE_CONFIG_PATH = "config_path";
	private static final String ATTRIBUTE_ENERGY_CALIBRATION = "energy_calibration";
	private static final String ATTRIBUTE_ENERGY_THRESHOLD = "energy_threshold";
	private static final String ATTRIBUTE_THRESHOLD = "threshold";
	private static final String ATTRIBUTE_THRESHOLD_NOISE = "threshold_noise";
	private static final String ATTRIBUTE_ESPIA_DEV_NB = "espia_dev_nb";
	private static final String ATTRIBUTE_FILL_MODE = "fill_mode";
	private static final String ATTRIBUTE_GATE_LEVEL = "gate_level";
	private static final String ATTRIBUTE_GATE_MODE = "gate_mode";
	private static final String ATTRIBUTE_READY_MODE = "ready_mode";
	private static final String ATTRIBUTE_SHUTTER_LEVEL = "shutter_level";
	private static final String ATTRIBUTE_TRIGGER_LEVEL = "trigger_level";

	@Override
	public String getConfigName() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_CONFIG_NAME);
	}

	@Override
	public void setConfigName(String configName) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_CONFIG_NAME, configName);
	}

	@Override
	public String getConfigPath() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_CONFIG_PATH);
	}

	@Override
	public void setConfigPath(String configPath) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_CONFIG_PATH, configPath);
	}

	@Override
	public MaxiPix2EnergyCalibration getMaxiPix2EnergyCalibration() throws DevFailed {
		double[] val = getTangoDeviceProxy().getAttributeAsDoubleArray(ATTRIBUTE_ENERGY_CALIBRATION);
		if (val.length < 3) // 2 plus null at the end
		{
			throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : "
					+ ATTRIBUTE_ENERGY_CALIBRATION + " returned length < 3", ErrSeverity.ERR, "", "") });
		}
		MaxiPix2EnergyCalibration maxiPix2EnergyCalibration = new MaxiPix2EnergyCalibrationImpl();
		maxiPix2EnergyCalibration.setThresholdSetPoint(val[MaxiPix2EnergyCalibration.THRESHOLD_SETPOINT_INDEX]);
		maxiPix2EnergyCalibration.setThresholdStepSize(val[MaxiPix2EnergyCalibration.THRESHOLD_STEPSIZE_INDEX]);
		return maxiPix2EnergyCalibration;

	}

	@Override
	public void setMaxiPix2EnergyCalibration(MaxiPix2EnergyCalibration maxiPix2EnergyCalibration) throws DevFailed {
		double[] val = new double[2];
		val[MaxiPix2EnergyCalibration.THRESHOLD_SETPOINT_INDEX] = maxiPix2EnergyCalibration.getThresholdSetPoint();
		val[MaxiPix2EnergyCalibration.THRESHOLD_STEPSIZE_INDEX] = maxiPix2EnergyCalibration.getThresholdStepSize();
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_ENERGY_CALIBRATION, val, 2, 1);
	}

	@Override
	public double getEnergyThreshold() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsDouble(ATTRIBUTE_ENERGY_THRESHOLD);
	}

	@Override
	public void setEnergyThreshold(double energyThreshold) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_ENERGY_THRESHOLD, energyThreshold);
	}

	@Override
	public int getThreshold() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsInt(ATTRIBUTE_THRESHOLD);
	}

	@Override
	public void setThreshold(int threshold) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_THRESHOLD, threshold);
	}

	@Override
	public int[] getThresholdNoise() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsIntArray(ATTRIBUTE_THRESHOLD_NOISE);
	}

	@Override
	public void setThresholdNoise(int[] thresholdNoise) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_THRESHOLD_NOISE, thresholdNoise, thresholdNoise.length, 1);
	}

	/*
	 * There does not appear to be a way of determining the nubmer of chips so make it configurable
	 */
	int numberOfChips = 0;

	@Override
	public int getNumberOfChips() {
		return numberOfChips;
	}

	public void setNumberOfChips(int numberOfChips) {
		this.numberOfChips = numberOfChips;
	}

	@Override
	public short getESPIABoardNumber() throws DevFailed {
		return getTangoDeviceProxy().getAttributeAsShort(ATTRIBUTE_ESPIA_DEV_NB);
	}

	@Override
	public void setESPIABoardNumber(short eSPIABoardNumber) throws DevFailed {
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_ESPIA_DEV_NB, eSPIABoardNumber);
	}

	@Override
	public FillMode getFillMode() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_FILL_MODE);
		if (val.equals(FILL_MODE_RAW))
			return FillMode.RAW;
		if (val.equals(FILL_MODE_ZERO))
			return FillMode.ZERO;
		if (val.equals(FILL_MODE_DISPATCH))
			return FillMode.DISPATCH;
		if (val.equals(FILL_MODE_MEAN))
			return FillMode.MEAN;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : "
				+ ATTRIBUTE_FILL_MODE + " returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setFillMode(FillMode fillMode) throws DevFailed {
		String val = "";
		switch (fillMode) {
		case RAW:
			val = FILL_MODE_RAW;
			break;
		case ZERO:
			val = FILL_MODE_ZERO;
			break;
		case DISPATCH:
			val = FILL_MODE_DISPATCH;
			break;
		case MEAN:
			val = FILL_MODE_MEAN;
			break;
		}
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_FILL_MODE, val);
	}

	private Level getLevel(String attributeName) throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(attributeName);
		if (val.equals(LEVEL_HIGH_RISE))
			return Level.HIGH_RISE;
		if (val.equals(LEVEL_LOW_FALL))
			return Level.LOW_FALL;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : " + attributeName
				+ " returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	private void setLevel(String attributeName, Level gateLevel) throws DevFailed {
		String val = "";
		switch (gateLevel) {
		case HIGH_RISE:
			val = LEVEL_HIGH_RISE;
			break;
		case LOW_FALL:
			val = LEVEL_LOW_FALL;
			break;
		}
		getTangoDeviceProxy().setAttribute(attributeName, val);
	}

	@Override
	public Level getGateLevel() throws DevFailed {
		return getLevel(ATTRIBUTE_GATE_LEVEL);
	}

	@Override
	public void setGateLevel(Level gateLevel) throws DevFailed {
		setLevel(ATTRIBUTE_GATE_LEVEL, gateLevel);
	}

	@Override
	public GateMode getGateMode() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_GATE_MODE);
		if (val.equals(GATE_MODE_INACTIVE))
			return GateMode.INACTIVE;
		if (val.equals(GATE_MODE_ACTIVE))
			return GateMode.ACTIVE;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : "
				+ ATTRIBUTE_GATE_MODE + " returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setGateMode(GateMode getMode) throws DevFailed {
		String val = "";
		switch (getMode) {
		case ACTIVE:
			val = GATE_MODE_ACTIVE;
			break;
		case INACTIVE:
			val = GATE_MODE_INACTIVE;
			break;
		}
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_GATE_MODE, val);
	}

	@Override
	public ReadyMode getReadyMode() throws DevFailed {
		String val = getTangoDeviceProxy().getAttributeAsString(ATTRIBUTE_READY_MODE);
		if (val.equals(READY_MODE_EXPOSURE))
			return ReadyMode.EXPOSURE;
		if (val.equals(READY_MODE_EXPOSURE_READOUT))
			return ReadyMode.EXPOSURE_READOUT;
		throw new DevFailed(new DevError[] { new DevError(getTangoDeviceProxy().toString() + " : "
				+ ATTRIBUTE_READY_MODE + " returned unknown value :" + val, ErrSeverity.ERR, "", "") });
	}

	@Override
	public void setReadyMode(ReadyMode readyMode) throws DevFailed {
		String val = "";
		switch (readyMode) {
		case EXPOSURE:
			val = READY_MODE_EXPOSURE;
			break;
		case EXPOSURE_READOUT:
			val = READY_MODE_EXPOSURE_READOUT;
			break;
		}
		getTangoDeviceProxy().setAttribute(ATTRIBUTE_READY_MODE, val);
	}

	@Override
	public Level getShutterLevel() throws DevFailed {
		return getLevel(ATTRIBUTE_SHUTTER_LEVEL);
	}

	@Override
	public void setShutterLevel(Level shutterLevel) throws DevFailed {
		setLevel(ATTRIBUTE_SHUTTER_LEVEL, shutterLevel);
	}

	@Override
	public Level getTriggerLevel() throws DevFailed {
		return getLevel(ATTRIBUTE_TRIGGER_LEVEL);
	}

	@Override
	public void setTriggerLevel(Level triggerLevel) throws DevFailed {
		setLevel(ATTRIBUTE_TRIGGER_LEVEL, triggerLevel);
	}
	
	@Override
	public Scannable getThresholdScannable() throws FactoryException {
		return getControlScannable("threshold", ATTRIBUTE_THRESHOLD, "");
	}

	@Override
	public Scannable getEnergyThresholdScannable() throws FactoryException {
		return getControlScannable("energyThreshold", ATTRIBUTE_ENERGY_THRESHOLD, "");
	}	
	
}
