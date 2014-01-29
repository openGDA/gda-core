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
	 * @return sessionID
	 */
	public abstract long getSessionForVisit(String visitname) throws SQLException;

	/**
	 * I'd keep that for one run of my spreadsheet, i.e. normally for one set of samples loaded.
	 * 
	 * @param experimentID
	 * @return saxsDataCollectionID
	 */
	public abstract long createSaxsDataCollection(long experimentID) throws SQLException;

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
	public abstract long createBufferMeasurement(long dataCollectionId, long specimenId,
			short plate, short row, short column, float exposureTemperature, int numFrames,
			double timePerFrame, double flow, double volume, double energyInkeV, String viscosity) throws SQLException;

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
	public long createSampleMeasurement(long dataCollectionId, long specimenId, short plate,
			short row, short column, float exposureTemperature, int numFrames, double timePerFrame,
			double flow, double volume, double energyInkeV, String viscosity)
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
	public abstract long createRun(long measurementId, double timePerFrame, float storageTemperature,
			float exposureTemperature, double energy, int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization, String filename, String internalPath);

	//FIXME how to re re-use the runs between datacollections?
	
	/**
	 * Update ISpyB to indicate that a measurement did not complete succesfully, implemented as runId being updated
	 * with a failure text (timeEnd is a VARCHAR)
	 * 
	 * @param measurementId
	 */
	public void setMeasurementFailed(long measurementId, String message);

	/**
	 * check ISpyB to determine whether the measurement has finished
	 * 
	 * @param measurementId
	 * @return true when done or successful
	 */
	public Status getMesurementStatus(long measurementId);
	
	/**
	 * Method to close the database connection once it's no longer needed.
	 */
	public abstract void disconnect() throws SQLException;

	/**
	 * @param saxsDataCollectionId
	 * @return list of samples measured
	 * @throws SQLException
	 */
	public List<SampleInfo> getExperimentInfo(long experimentId) throws SQLException;

	/**
	 * retrieve all data collection ids for a session (visit)
	 * 
	 * @param blsessionId
	 * @return all ids
	 * @throws SQLException
	 */
	public List<Long> getExperimentsForSession(long blsessionId) throws SQLException;

	public List<Long> getDataCollectionsForExperiments(long experiemnt) throws SQLException;
	
	/**
	 * @param sessionId
	 * @param name
	 * @param experimentType
	 *            - TEMPLATE, HPLC, STATIC
	 * @param comments
	 * @return experimentId
	 * @throws SQLException
	 */
	public long createExperiment(long sessionId, String name, String experimentType, String comments)
			throws SQLException;

	/**
	 * Call this method when data reduction is started so that its status can be recorded
	 * 
	 * @param dataCollectionId
	 * @return SubtractionId
	 * @throws SQLException
	 */
	public long createDataReduction(long dataCollectionId) throws SQLException;

	/**
	 * Sets the state that the data reduction has failed
	 * 
	 * @param subtractionId
	 * @throws SQLException
	 */
	public void setDataReductionStatus(long dataCollectionId, Status status, String resultsFilename) throws SQLException;

	/**
	 * True when data reduction is not currently running and the process has resulted in complete data in the ISPyB
	 * database
	 * 
	 * @param dataCollectionId
	 * @param subtractionId
	 * @return data reduction has succeeded
	 * @throws SQLException
	 */
	public Status getDataReductionStatus(long dataCollectionId) throws SQLException;

	/**
	 * Returns the measurements from ISpyB for a session id
	 * 
	 * @return list of progress statuses for samples in the ISpyB database
	 * @throws SQLException
	 */
	public List<ISampleProgress> getBioSAXSMeasurements(long blSessionId) throws SQLException;


	/**
	 * Updates a measurement with an analysis status, status can be one of the following 1. NOT STARTED 2. STARTED 3.
	 * SUCCESSFUL 4. NOT SUCCESSFUL
	 * 
	 * @param measurementId
	 * @param analysisStatus
	 * @throws SQLException
	 */
	public void setAnalysisStatus(long dataCollectionId, Status analysisStatus) throws SQLException;
}
