/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.edxd.common;

public interface IEdxdAlignment {

	/**
	 * @return full fileName of the last calibration the last saved energy calibration file from the cached
	 *         configuration.
	 */
	String getLastSavedEnergyCalibrationFile();

	/**
	 * @return datetime when the last energy calibration file was written
	 */
	String getLastSaveEnergyCalibrationDateTime();

	/**
	 * @return full filename of the last Q calibration that was done
	 */
	String getLastSavedQCalibrationFile();

	/**
	 * @return datetime of the last saved Q calibration
	 */
	String getLastSaveQCalibrationDateTime();

	/**
	 * @return whether Hutch1 or Hutch2 when the last calibration was performed
	 */
	String getLastSavedHutch();

	/**
	 * @return the collimator that was used to perform the last calibration.
	 */
	String getLastSavedCollimator();

	/**
	 * Request to run the pre-amp gain procedure.
	 */
	void runPreampGain();

	/**
	 * Request to run the energy calibration
	 * 
	 * @return filename of the saved energy calibration once it is performed.
	 */
	String runEnergyCalibration();

	/**
	 * Request to perform the detector XY Alignment.
	 */
	void runDetectorXYAlignment();

	/**
	 * Request to perform the collimator XYZ Alignment.
	 */
	void runCollimatorXYZAlignment();

	/**
	 * Request to perform the collimator angular Alignment.
	 */
	void runCollimatorAngularAlignment();

	/**
	 * Request to run the Q Calibration.
	 * 
	 * @return the filename of the Q Calibration once the Q calibration is performed.
	 */
	String runQAxisCalibration();

	/**
	 * Request to load the energy calibration file into the server memory.
	 * 
	 * @param fileName
	 *            - of the file that contains energy calibration values
	 */
	void loadEnergyCalibrationFile(String fileName);

	/**
	 * Request to load the Q calibration file
	 * 
	 * @param fileName
	 *            - of the file that contains Q Calibration values
	 */
	void loadQCalibrationFile(String fileName);

	/**
	 * @return URL for the mpeg stream for EH1 front end camera
	 * @throws Exception
	 */
	String getEh1MpegUrl() throws Exception;

	/**
	 * @return URL for the mpeg stream for EH2 front end camera
	 * @throws Exception
	 */
	String getEh2MpegUrl() throws Exception;

	/**
	 * Request to start the eh1 camera
	 * 
	 * @throws Exception
	 */
	void startEh1Camera() throws Exception;

	/**
	 * request to start the eh2 camera
	 * 
	 * @throws Exception
	 */
	void startEh2Camera() throws Exception;

}
