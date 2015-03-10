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

package gda.device.maxipix2;

import fr.esrf.Tango.DevFailed;
import gda.device.Scannable;
import gda.device.base.Base;
import gda.factory.FactoryException;

public interface MaxiPix2 extends Base{

	/*
	 * Attributes
	 */
	String getConfigName() throws DevFailed;
	
	void setConfigName(String configName) throws DevFailed;
	
	String getConfigPath() throws DevFailed;
	
	void setConfigPath(String configPath) throws DevFailed;

	MaxiPix2EnergyCalibration getMaxiPix2EnergyCalibration() throws DevFailed;
	
	void setMaxiPix2EnergyCalibration(MaxiPix2EnergyCalibration maxiPix2EnergyCalibration)  throws DevFailed;

	double getEnergyThreshold() throws DevFailed;
	
	void setEnergyThreshold(double energyThreshold) throws DevFailed;
	
	int getThreshold() throws DevFailed;
	
	void setThreshold(int threshold) throws DevFailed;

	/*
	 * Length equals number of chips
	 */
	int[] getThresholdNoise() throws DevFailed;
	
	void setThresholdNoise(int[] thresholdNoise) throws DevFailed;
	
	int getNumberOfChips();
	
	short getESPIABoardNumber() throws DevFailed;
	
	void setESPIABoardNumber(short eSPIABoardNumber) throws DevFailed;

	
	enum FillMode {
		RAW, ZERO, DISPATCH, MEAN
	}
	
	FillMode getFillMode() throws DevFailed;
	
	void setFillMode(FillMode fillMode) throws DevFailed;

	enum Level {
		HIGH_RISE, LOW_FALL
	}
	
	Level getGateLevel() throws DevFailed;
	
	void setGateLevel(Level gateLevel) throws DevFailed;
	
	enum GateMode {
		INACTIVE, ACTIVE
	}

	GateMode getGateMode() throws DevFailed;
	
	void setGateMode(GateMode getMode) throws DevFailed;
	
	enum ReadyMode {
		EXPOSURE, EXPOSURE_READOUT
	}

	ReadyMode getReadyMode() throws DevFailed;
	
	void setReadyMode(ReadyMode readyMode) throws DevFailed;
	
	enum ShutterLevel {
		EXPOSURE, EXPOSURE_READOUT
	}

	Level getShutterLevel() throws DevFailed;
	
	void setShutterLevel(Level shutterLevel) throws DevFailed;

	Level getTriggerLevel() throws DevFailed;
	
	void setTriggerLevel(Level triggerLevel) throws DevFailed;


	public Scannable getThresholdScannable() throws FactoryException;

	public Scannable getEnergyThresholdScannable() throws FactoryException;
}
