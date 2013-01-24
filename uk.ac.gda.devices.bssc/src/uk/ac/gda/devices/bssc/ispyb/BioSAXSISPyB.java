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

package uk.ac.gda.devices.bssc.ispyb;

import java.sql.SQLException;

public abstract class BioSAXSISPyB {

	public enum RDBMSTYPE {Oracle, MySQL, PostgreSQL};
	public enum MODE {live, testing};
	
	
	/**
	 * I would not care if that creates/gets a BLSession or an Experiment. So if the structure for that changes I would
	 * not be affected.
	 * 
	 * @param visitname
	 *            e.g. sm9999-9
	 * @return sessionID
	 */
	public abstract long getSessionForVisit(String visitname) throws SQLException;

	/**
	 * I'd keep that for one run of my spreadsheet, i.e. normally for one set of samples loaded.
	 * 
	 * @param sessionID
	 * @return experimentID
	 */
	public abstract long createExperiment(long sessionID) throws SQLException;

	/**
	 * @param blsessionId
	 *            The ID of the visit
	 * @param plate
	 *            does not seem to be in the database. it is 1,2,3 and you could create a SamplePlate for each per
	 *            Experiment
	 * @param row
	 *            i have row as char, i don't mind which way
	 * @param column
	 * @param storageTemperature
	 * @param exposureTemperature
	 * @param numFrames
	 * @param timePerFrame
	 * @param flow
	 * @param volume
	 * @param energyInkeV
	 * @param viscosity
	 * @param fileName
	 *            "/dls/i22/data/2013/sm999-9/i22-9999.nxs"
	 * @param internalPath
	 *            "/entry1/detector/data"
	 * @return bufferMeasurementId
	 */
	public abstract long createBufferMeasurement(long blsessionId, short plate, short row, short column, float storageTemperature,
			float exposureTemperature, int numFrames, double timePerFrame, double flow, double volume,
			double energyInkeV, String viscosity, String fileName, String internalPath) throws SQLException;

	/**
	 * @param blsessionId
	 *            The ID of the visit
	 * @param plate
	 * @param row
	 * @param column
	 * @param name
	 * @param concentration
	 * @param storageTemperature
	 * @param exposureTemperature
	 * @param numFrames
	 * @param timePerFrame
	 * @param flow
	 * @param volume
	 * @param energyInkeV
	 * @param viscosity
	 * @param fileName
	 * @param internalPath
	 * @return sampleMeasurementId
	 */
	public abstract long createSampleMeasurement(long blsessionId, short plate, short row, short column, String name,
			double concentration, float storageTemperature, float exposureTemperature, int numFrames,
			double timePerFrame, double flow, double volume, double energyInkeV, String viscosity, 
			String fileName, String internalPath) throws SQLException;

	/**
	 * @param blsessionId
	 *            The ID of the visit
	 * @param sampleMeasurementId
	 *            The ID of the sample's measurement
	 * @param bufferMeasurementId
	 *            The ID of the buffer's measurement
	 */
	public abstract void registerBufferForSample(long blsessionId, long sampleMeasurementId, long bufferMeasurementId) 
			throws SQLException;

}