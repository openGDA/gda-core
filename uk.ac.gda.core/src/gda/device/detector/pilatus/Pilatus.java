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

	void setCollectionPeriod(double t);

	double getCollectionPeriod();

	void setMode(String modeName) throws DeviceException;

	void setGain(String gainName) throws DeviceException;

	String getMode() throws DeviceException;

	String getGain() throws DeviceException;

	String[] getModeLabels() throws DeviceException;

	String[] getGainLabels() throws DeviceException;

	void setFilename(String filename) throws DeviceException;

	void setFileformat(String fileformat) throws DeviceException;

	void setFileHeader(String fileHeader) throws DeviceException;

	void setFilepath(String filepath) throws DeviceException;

	void setFilenumber(int n) throws DeviceException;

	int getFilenumber() throws DeviceException;

	void updateFilenumber() throws DeviceException;

	void setNumberImages(int n) throws DeviceException;

	double getThresholdEnergy() throws DeviceException;

	void setThresholdEnergy(double energyValue) throws DeviceException;

	void setDelayTime(double delaySec) throws DeviceException;

	double getDelayTime() throws DeviceException;

	void setGeneralText(String toSend);

	void sendGeneralText();
}