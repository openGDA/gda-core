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

import gda.device.Detector;
import gda.device.DeviceException;

public interface Pilatus extends Detector {

	public abstract void setCollectionPeriod(double t);

	public abstract double getCollectionPeriod();

	public abstract void setMode(String modeName) throws DeviceException;

	public abstract void setGain(String gainName) throws DeviceException;

	public abstract String getMode() throws DeviceException;

	public abstract String getGain() throws DeviceException;

	public abstract String[] getModeLabels() throws DeviceException;

	public abstract String[] getGainLabels() throws DeviceException;

	public abstract void setFilename(String filename) throws DeviceException;

	public abstract void setFileformat(String fileformat) throws DeviceException;

	public abstract void setFileHeader(String fileHeader) throws DeviceException;

	public abstract void setFilepath(String filepath) throws DeviceException;

	public abstract void setFilenumber(int n) throws DeviceException;

	public abstract int getFilenumber() throws DeviceException;

	public abstract void updateFilenumber() throws DeviceException;

	public abstract void setNumberImages(int n) throws DeviceException;

	public abstract double getThresholdEnergy() throws DeviceException;

	public abstract void setThresholdEnergy(double energyValue) throws DeviceException;

	public abstract void setDelayTime(double delaySec) throws DeviceException;

	public abstract double getDelayTime() throws DeviceException;
	
	public void setGeneralText(String toSend);
	
	public void sendGeneralText();

}