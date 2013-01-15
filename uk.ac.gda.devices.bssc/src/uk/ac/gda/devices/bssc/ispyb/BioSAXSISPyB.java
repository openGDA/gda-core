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

public interface BioSAXSISPyB {

	/**
	 * I would not care if that creates/gets a BLSession or an Experiment. So if the structure for that changes I would
	 * not be affected.
	 * 
	 * @param visitname
	 *            e.g. sm9999-9
	 * @return sessionID
	 */
	public long getSessionForVisit(String visitname);

	/**
	 * I'd keep that for one run of my spreadsheet, i.e. normally for one set of samples loaded.
	 * 
	 * @param sessionID
	 * @return experimentID
	 */
	public long createExperiment(long sessionID);

	/**
	 * @param experimentID
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
	public long createBufferMeasurement(long experimentID, short plate, short row, short column, float storageTemperature,
			float exposureTemperature, int numFrames, double timePerFrame, double flow, double volume,
			double energyInkeV, String viscosity, String fileName, String internalPath);

	/**
	 * @param experimentID
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
	public long createSampleMeasurement(long experimentID, short plate, short row, short column, String name,
			double concentration, float storageTemperature, float exposureTemperature, int numFrames,
			double timePerFrame, double flow, double volume, double energyInkeV, String viscosity, 
			String fileName, String internalPath);

	/**
	 * @param sample
	 *            sampleMeasurementId
	 * @param buffer
	 *            bufferMeasurementId
	 */
	public void registerBufferForSample(long sample, long buffer);

}