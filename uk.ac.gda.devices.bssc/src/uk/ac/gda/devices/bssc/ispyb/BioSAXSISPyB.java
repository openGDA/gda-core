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

import uk.ac.gda.devices.bssc.beans.LocationBean;

public interface BioSAXSISPyB {
	
	public class SampleInfo {
		public LocationBean location;
		public String name;
		public String sampleFileName, bufferBeforeFileName, bufferAfterFileName;
	}
	
	/**
	 *  
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
	 * @param proposalId
	 *            The ID of the proposal
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
	public abstract long createBufferMeasurement(long proposalId, short plate, short row, short column, float storageTemperature,
			float exposureTemperature, int numFrames, double timePerFrame, double flow, double volume,
			double energyInkeV, String viscosity, String fileName, String internalPath) throws SQLException;

	/**
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
	public abstract long createSampleMeasurement(long experimentId, short plate, short row, short column, String name,
			double concentration, float storageTemperature, float exposureTemperature, int numFrames,
			double timePerFrame, double flow, double volume, double energyInkeV, String viscosity, 
			String fileName, String internalPath) throws SQLException;

	/**
	 * 
	 * @param saxsDataCollectionId from that call
	 * @param measurementId can be buffer or sample measurement
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
	 * retrieve all data collection ids for a proposal
	 * 
	 * @param proposalId
	 * @return all ids
	 * @throws SQLException
	 */
	public List<Long> getSaxsDataCollectionsForProposal(long proposalId) throws SQLException;
	
	/**
	 * @param proposalId
	 * @param name
	 * @param experimentType - TEMPLATE, HPLC, STATIC
	 * @param comments
	 * @return experimentId
	 * @throws SQLException
	 */
	public long createExperiment(long proposalId, String name, String experimentType, String comments) throws SQLException;

}
