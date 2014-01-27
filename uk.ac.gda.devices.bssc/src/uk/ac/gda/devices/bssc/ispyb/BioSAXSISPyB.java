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
import java.util.List;

import uk.ac.gda.devices.bssc.ISampleProgress;

public interface BioSAXSISPyB {

	/**
	 * @param visitname
	 *            e.g. sm9999-9
	 * @return proposalID
	 */
	public abstract long getProposalForVisit(String visitname) throws SQLException;

	/**
	 * @param visitname
	 *            e.g. sm9999-9
	 * @return sessionID
	 */
	public abstract long getSessionForVisit(String visitname) throws SQLException;

	/**
	 * I'd keep that for one run of my spreadsheet, i.e. normally for one set of samples loaded.
	 * 
	 * @param sessionID
	 * @param experimentID
	 * @return saxsDataCollectionID
	 */
	public abstract long createSaxsDataCollection(long sessionID, long experimentID) throws SQLException;

	/**
	 * @param blsessionId
	 *            The ID of the visit
	 * @param experimentId
	 *            The ID of the experiment
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
	public abstract long createBufferMeasurement(long blsessionId, long experimentId, short plate, short row,
			short column, float storageTemperature, float exposureTemperature, int numFrames, double timePerFrame,
			double flow, double volume, double energyInkeV, String viscosity, String fileName, String internalPath)
			throws SQLException;

	/**
	 * @param blsessionId
	 *            The ID of the visit
	 * @param experimentId
	 *            The ID of the experiment
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
	public abstract long createSampleMeasurement(long blsessionId, long experimentId, short plate, short row,
			short column, String name, double concentration, float storageTemperature, float exposureTemperature,
			int numFrames, double timePerFrame, double flow, double volume, double energyInkeV, String viscosity,
			String fileName, String internalPath) throws SQLException;

	/**
	 * Retrieve a dataCollectionId from SaxsDataCollection that matches the desired experimentId
	 * 
	 * @param experimentId
	 * @return first dataCollectionId
	 * @throws SQLException
	 */
	public long getDataCollectionForExperiment(long experimentId) throws SQLException;

	/**
	 * @param saxsDataCollectionId
	 *            from that call
	 * @param measurementId
	 *            can be buffer or sample measurement
	 * @return measurementToSaxsCollectionId
	 * @throws SQLException
	 */
	public long createMeasurementToDataCollection(long saxsDataCollectionId, long measurementId) throws SQLException;

	/**
	 * Method to close the database connection once it's no longer needed.
	 */
	public abstract void disconnect() throws SQLException;

	public List<SampleInfo> getSaxsDataCollectionInfo(long saxsDataCollectionId) throws SQLException;

	/**
	 * retrieve all data collection ids for a session (visit)
	 * 
	 * @param blsessionId
	 * @return all ids
	 * @throws SQLException
	 */
	public List<Long> getSaxsDataCollectionsForSession(long blsessionId) throws SQLException;

	/**
	 * @param proposalId
	 * @param name
	 * @param experimentType
	 *            - TEMPLATE, HPLC, STATIC
	 * @param comments
	 * @return experimentId
	 * @throws SQLException
	 */
	public long createExperiment(long proposalId, String name, String experimentType, String comments)
			throws SQLException;

	/**
	 * Call this method when data reduction is started so that its status can be recorded
	 * 
	 * @param dataCollectionId
	 * @return SubtractionId
	 * @throws SQLException
	 */
	public long createDataReductionStarted(long dataCollectionId) throws SQLException;

	/**
	 * @param subtractionId
	 * @return whether data reduction is still running or not
	 * @throws SQLException
	 */
	public boolean isDataReductionRunning(long subtractionId) throws SQLException;

	/**
	 * Clear the flag that indicates that the data reduction is running. Run this method when data reduction has
	 * completed but before results are put into the ISPyB database
	 * 
	 * @param subtractionId
	 * @return success of clearing procedure
	 * @throws SQLException
	 */
	public boolean clearDataReductionStarted(long subtractionId) throws SQLException;

	/**
	 * Checks whether the data reduction failed to complete at all
	 * 
	 * @param subtractionId
	 * @return whether data reduction failed to complete
	 * @throws SQLException
	 */
	public boolean isDataReductionFailedToComplete(long subtractionId) throws SQLException;

	/**
	 * Sets the state that the data reduction has failed
	 * 
	 * @param subtractionId
	 * @throws SQLException
	 */
	public void setDataReductionFailedToComplete(long subtractionId) throws SQLException;

	/**
	 * The data reduction has returned some information, but it may not be complete.
	 * 
	 * @param dataCollectionId
	 * @return whether data reduction failed (incomplete results) or not
	 * @throws SQLException
	 */
	public boolean isDataReductionFailed(long dataCollectionId) throws SQLException;

	/**
	 * True when data reduction is not currently running and the process has resulted in complete data in the ISPyB
	 * database
	 * 
	 * @param dataCollectionId
	 * @param subtractionId
	 * @return data reduction has succeeded
	 * @throws SQLException
	 */
	public boolean isDataReductionSuccessful(long dataCollectionId, long subtractionId) throws SQLException;

	/**
	 * Returns the measurements from ISpyB for a session id
	 * @return list of progress statuses for samples in the ISpyB database
	 * @throws SQLException
	 */
	public List<ISampleProgress> getBioSAXSMeasurements(long blSessionId) throws SQLException;
	
	/**
	 * Updates a measurement with a status, status can be one of the following
	 * 1. NOT STARTED
	 * 2. STARTED
	 * 3. SUCCESSFUL
	 * 4. NOT SUCCESSFUL
	 * 
	 * @param measurementId
	 * @param status
	 * @throws SQLException
	 */
	public void setMeasurementCollectionStatus(long measurementId ,String status) throws SQLException;

}
