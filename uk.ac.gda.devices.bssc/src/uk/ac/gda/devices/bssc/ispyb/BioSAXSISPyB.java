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

public interface BioSAXSISPyB {

	/**
	 * @param visitname
	 *            e.g. sm9999-9
	 * @return sessionID
	 */
	public abstract long getSessionForVisit(String visitname) throws SQLException;

	/**
	 * This method creates a SAXSDATACOLLECTION which corresponds to one row on the experiement spreadsheet This method
	 * should also create the measurements (Buffer Before, Sample, Buffer After) and associate the measurements with a
	 * data collection
	 * 
	 * @param experimentID
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
	 * @return saxsDataCollectionID
	 */
	public long createSaxsDataCollection(long experimentID, short plate, short row, short column, String sampleName, double sampleConcentrationMgMl,
			double molecularMassKda, short bufferPlate, short bufferRow, short bufferColumn, float exposureTemperature, int numFrames,
			double timePerFrame, double flow, double volume, double energyInkeV, String viscosity) throws SQLException;

	/**
	 * This method creates a SAXSDATACOLLECTION which corresponds to one row on the experiement spreadsheet. This method
	 * should use the Buffer After measurement of the @previousDataCollectionId as the Buffer Before for this experiment, then
	 * creates the remaining measurements (Sample, Buffer After) and associates all three measurements with this data collection.
	 * 
	 * @param experimentID
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
	 * @param previousDataCollectionId
	 * @return saxsDataCollectionID
	 */
	public long createSaxsDataCollectionUsingPreviousBuffer(long experimentID, short plate, short row, short column, String sampleName, double sampleConcentrationMgMl,
			double molecularMassKda, short bufferPlate, short bufferRow, short bufferColumn, float exposureTemperature, int numFrames,
			double timePerFrame, double flow, double volume, double energyInkeV, String viscosity, long previousDataCollectionId) throws SQLException;

	/**
	 * Creates a buffer run, updates measurement.runId, returns runId.
	 * 
	 * @param currentDataCollectionId - id of current data collection
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
	public long createBufferRun(long currentDataCollectionId, double timePerFrame,
			float storageTemperature, float exposureTemperature, double energy, int frameCount, double transmission,
			double beamCenterX, double beamCenterY, double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization, String filename, String internalPath);

	/**
	 * Creates a sample run
	 * 
	 * @param dataCollectionId
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
	 * @param filename
	 * @param internalPath
	 * @return runId
	 */
	public long createSampleRun(long dataCollectionId, double timePerFrame, float storageTemperature,
			float exposureTemperature, double energy, int frameCount, double transmission, double beamCenterX,
			double beamCenterY, double pixelSizeX, double pixelSizeY, double radiationRelative,
			double radiationAbsolute, double normalization, String filename, String internalPath);

	/**
	 * Method to close the database connection once it's no longer needed.
	 */
	public abstract void disconnect() throws SQLException;

	/**
	 * @param dataCollectionId
	 * @return list of samples measured
	 * @throws SQLException
	 */
	public List<SampleInfo> getSaxsDataCollectionInfo(long dataCollectionId) throws SQLException;

	/**
	 * // * @param saxsDataCollectionId
	 * 
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

	public List<Long> getDataCollectionsForExperiments(long experiment) throws SQLException;

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
	 * Sets the status of a data collection
	 * 
	 * @param dataCollectionId
	 * @param status
	 */
	public void setDataCollectionStatus(long dataCollectionId, ISpyBStatusInfo status);

	/**
	 * Changes the status of the data collection from NOT_STARTED to RUNNING
	 * @param dataCollectionId
	 * @throws SQLException 
	 */
	public void setDataCollectionStarted(long dataCollectionId) throws SQLException;
	
	/**
	 * Returns the status of the data collection for the sample/data collection
	 * 
	 * @param dataCollectionId
	 * @return ProgressStatus
	 * @throws SQLException
	 */
	public ISpyBStatusInfo getDataCollectionStatus(long dataCollectionId) throws SQLException;

	/**
	 * Call this method when data reduction is started so that its status can be recorded
	 * 
	 * @param dataCollectionId
	 * @return SubtractionId
	 * @throws SQLException
	 */
	public long createDataReduction(long dataCollectionId) throws SQLException;

	/**
	 * Sets the data reduction status of the data collection
	 * 
	 * @param dataCollectionId
	 * @param status
	 * @throws SQLException
	 */
	public void setDataReductionStatus(long dataCollectionId, ISpyBStatusInfo status)
			throws SQLException;

	/**
	 * Returns the status of the data reduction for a sample/data collection
	 * 
	 * @param dataCollectionId
	 * @return ProgressStatus
	 * @throws SQLException
	 */
	public ISpyBStatusInfo getDataReductionStatus(long dataCollectionId) throws SQLException;

	/**
	 * Returns the data collections from ISPyB in creation order
	 * 
	 * @return list of progress statuses for samples in the ISpyB database
	 * @throws SQLException
	 */
	public List<ISAXSDataCollection> getSAXSDataCollections(long blSessionId) throws SQLException;

	/**
	 * 
	 * @param dataCollectionId
	 * @return ISAXSDataCollection
	 */
	public ISAXSDataCollection getSAXSDataCollection(long dataCollectionId);

	public void setExperimentFinished(long experimentId);

	public void setExperimentAborted(long experimentId);
}