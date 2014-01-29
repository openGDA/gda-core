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

import uk.ac.gda.devices.bssc.beans.ISampleProgress;

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
	 * @param dataCollectionId
	 *            The ID of the data collection
	 * @param specimenId
	 *            The ID of the specimen
	 * @param beforeSample
	 *            Boolean value indicating if the buffer measurement is taken before or after the sample
	 * @param plate
	 *            does not seem to be in the database. it is 1,2,3 and you could create a SamplePlate for each per
	 *            Experiment
	 * @param row
	 *            i have row as char, i don't mind which way
	 * @param column
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
	public abstract long createBufferMeasurement(long blsessionId, long dataCollectionId, long specimenId,
			boolean beforeSample, short plate, short row, short column, float exposureTemperature, int numFrames,
			double timePerFrame, double flow, double volume, double energyInkeV, String viscosity, String fileName,
			String internalPath) throws SQLException;

	/**
	 * @param blsessionId
	 *            The ID of the visit
	 * @param dataCollectionId
	 *            The ID of the data collection
	 * @param specimenId
	 *            The ID of the specimen
	 * @param plate
	 * @param row
	 * @param column
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
	public abstract long createSampleMeasurement(long blsessionId, long dataCollectionId, long specimenId, short plate,
			short row, short column, float exposureTemperature, int numFrames, double timePerFrame,
			double flow, double volume, double energyInkeV, String viscosity, String fileName, String internalPath)
			throws SQLException;

	/**
	 * Creates a run, updates measurement.runId, returns runId.
	 * @param measurementId
	 * @param timePerFrame
	 * @param storageTemperature
	 * @param exposureTemperature
	 * @param energy
	 * @param frameCount
	 * @param transmission
	 * @param beamCenterX
	 * @param beamCenterY
	 * @param pixelSizeX
	 * @param pixelSizeY
	 * @param radiationRelative
	 * @param radiationAbsolute
	 * @param normalization
	 * @return runId
	 */
	public abstract long measurementStarted(long measurementId, double timePerFrame, float storageTemperature,
			float exposureTemperature, double energy, int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization);

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
	 * Update ISPyB to indicate that a measurement completed successfully, implemented as runId being updated with a
	 * number (i.e. universal time)
	 * 
	 * @param measurementId
	 */
	public void measurementDone(long measurementId);

	/**
	 * Update ISpyB to indicate that a measurement did not complete succesfully, implemented as runId being updated
	 * with a failure text (timeEnd is a VARCHAR)
	 * 
	 * @param measurementId
	 */
	public void measurementFailed(long measurementId);

	/**
	 * check ISpyB to determine whether the measurement has finished
	 * 
	 * @param measurementId
	 * @return true when done or successful
	 */
	public boolean isMeasurementDone(long measurementId);
	
	/**
	 * Check ISpyB to determine whether the measurement was completed and successful
	 * @param measurementId
	 * @return true if measurement was completed and successful
	 */
	public boolean isMeasurementSuccessful(long measurementId);
	
	/**
	 * Check ISpyB to determine whether the measurement was started but did not complete successfully
	 * @param measurementId
	 * @return true if the measurement was started but did not complete successfully
	 */
	public boolean isMeasurementFailed(long measurementId);
	
	/**
	 * Method to close the database connection once it's no longer needed.
	 */
	public abstract void disconnect() throws SQLException;

	/**
	 * @param saxsDataCollectionId
	 * @return list of samples measured
	 * @throws SQLException
	 */
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
	 * 
	 * @return list of progress statuses for samples in the ISpyB database
	 * @throws SQLException
	 */
	public List<ISampleProgress> getBioSAXSMeasurements(long blSessionId) throws SQLException;

	/**
	 * Updates a measurement with a collection status, status can be one of the following 1. NOT STARTED 2. STARTED 3.
	 * SUCCESSFUL 4. NOT SUCCESSFUL
	 * 
	 * @param measurementId
	 * @param collectionStatus
	 * @throws SQLException
	 */
	public void setMeasurementCollectionStatus(long measurementId, String collectionStatus) throws SQLException;

	/**
	 * Updates a measurement with a reduction status, status can be one of the following 1. NOT STARTED 2. STARTED 3.
	 * SUCCESSFUL 4. NOT SUCCESSFUL
	 * 
	 * @param measurementId
	 * @param reductionStatus
	 * @throws SQLException
	 */
	public void setMeasurementReductionStatus(long measurementId, String reductionStatus) throws SQLException;

	/**
	 * Updates a measurement with an analysis status, status can be one of the following 1. NOT STARTED 2. STARTED 3.
	 * SUCCESSFUL 4. NOT SUCCESSFUL
	 * 
	 * @param measurementId
	 * @param analysisStatus
	 * @throws SQLException
	 */
	public void setMeasurementAnalysisStatus(long measurementId, String analysisStatus) throws SQLException;

	/**
	 * Sets the start time for a measurement
	 * 
	 * @param startTime
	 * @throws SQLException
	 */
	public void setMeasurementStartTime(long startTime) throws SQLException;

	public abstract long createSpecimenForMeasurement();
}
