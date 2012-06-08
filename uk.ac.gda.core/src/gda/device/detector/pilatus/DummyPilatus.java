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

package gda.device.detector.pilatus;

import gda.device.DeviceException;
import gda.device.detector.DetectorBase;

public class DummyPilatus extends DetectorBase implements Pilatus {

	private String gain = "Medium - 10-14 KeV";
	private String mode = "Internal";
	
	private double thresholdEnergy = 6340;
	private double delayTime;
	private String[] gainLabels;
	private String[] modeLabels;

	@Override
	public void collectData() throws DeviceException {
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public Object readout() throws DeviceException {
		return null;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return null;
	}

	@Override
	public void setCollectionPeriod(double t) {
	}

	@Override
	public double getCollectionPeriod() {
		return 0;
	}

	@Override
	public void setMode(String modeName) throws DeviceException {
		this.mode = modeName;
	}

	@Override
	public void setGain(String gainName) throws DeviceException {
		this.gain = gainName;
	}

	@Override
	public String getMode() throws DeviceException {
		return mode;
	}

	@Override
	public String getGain() throws DeviceException {
		return gain;
	}

	@Override
	public String[] getModeLabels() throws DeviceException {
		return modeLabels;
	}

	@Override
	public String[] getGainLabels() throws DeviceException {
		return gainLabels;
	}

	@Override
	public void setFilename(String filename) throws DeviceException {
	}

	@Override
	public void setFileformat(String fileformat) throws DeviceException {
	}

	@Override
	public void setFileHeader(String fileHeader) throws DeviceException {
	}

	@Override
	public void setFilepath(String filepath) throws DeviceException {
	}

	@Override
	public void setFilenumber(int n) throws DeviceException {
	}

	@Override
	public int getFilenumber() throws DeviceException {
		return 0;
	}

	@Override
	public void updateFilenumber() throws DeviceException {
	}

	@Override
	public void setNumberImages(int n) throws DeviceException {
	}

	@Override
	public double getThresholdEnergy() throws DeviceException {
		return thresholdEnergy;
	}

	@Override
	public void setThresholdEnergy(double energyValue) throws DeviceException {
		thresholdEnergy = energyValue;
	}

	@Override
	public void setDelayTime(double delaySec) throws DeviceException {
		this.delayTime = delaySec;
	}

	@Override
	public double getDelayTime() throws DeviceException {
		return delayTime;
	}
	
	@Override
	public void setGeneralText(String toSend) {
	}
	
	@Override
	public void sendGeneralText() {
	}
	
}