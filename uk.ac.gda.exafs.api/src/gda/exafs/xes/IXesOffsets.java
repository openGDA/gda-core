/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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
package gda.exafs.xes;

import java.io.IOException;

import gda.device.DeviceException;
import gda.factory.Findable;

/**
 * Interface used for Jython XESOffsets class (xes_offsets.py). This allows the XESOffsets object created in
 * localStation.py to be injected into XesScanFactory, XesScan and the methods called when required during scans.
 */
public interface IXesOffsets extends Findable {

	static final String OFFSET_UPDATE_EVENT = "offsets_update";

	/** Apply offset parameters stored in file to XES spectrometer motors.
	 * @throws IOException */
	void apply(String filename) throws IOException;

	/** Re-apply offset parameters previously set using {@link #apply(String)}
	 * @throws IOException */
	void reApply() throws IOException;

	/** Save the offset values for each motor into an xml file
	 * @throws IOException */
	void saveAs(String filename) throws IOException;

	/** @return Name of current offset file */
	String getCurrentFile();

	/** Save current motor offsets to temporary file
	 * @throws IOException */
	void saveToTemp() throws IOException;

	/** Apply motor offsets using values in temporary file
	 * @throws IOException */
	void applyFromTemp() throws IOException;

	/** Set name of temporary file */
	void setTempSaveName(String filename);

	/** Get name of temporary file */
	String getTempSaveName();

	/** Remove all offsets i.e. set all the offset for each motor to zero */
	void removeAll();

	/** Calibrate the spectrometer, calculate the offsets for given energy
	 * @throws IOException */
	void applyFromLive(double fluoEnergy) throws DeviceException, IOException;

	String getSpectrometerGroupName();
}