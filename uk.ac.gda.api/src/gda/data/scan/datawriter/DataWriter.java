/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data.scan.datawriter;

import gda.scan.IScanDataPoint;

import java.util.ArrayList;

/**
 * Interface for objects which hold references to data.
 * <p>
 * The actions of these objects will be beamline dependent. Each DataWriter type is format specific.
 * <p>
 * The data handler could be used for more then writing data to an ascii file. It could be used to create new folders,
 * determine file names etc. etc.
 * <P>
 * This class is normally client-side for use during scans. This should be taken into consideration whether to use this
 * class to actually write files, or simply to hold file references.
 */
public interface DataWriter {

	/**
	 * for incremental addition of data
	 * 
	 * @param newData
	 *            Object
	 * @throws Exception 
	 */
	public void addData(IScanDataPoint newData) throws Exception;

	/**
	 * Called when data collection has been completed
	 */
	public void completeCollection() throws Exception;

	/**
	 * Get the absFilePath for this scan
	 * 
	 * @return String
	 */
	public String getCurrentFileName();

	/**
	 * Get the current scan identifier (ie scan number)
	 * 
	 * @return String
	 */
	public String getCurrentScanIdentifier();

	/**
	 * header is a publicly accessible string which will be written to the file (or saved to a database) during the
	 * prepareForCollection method. Before this method is called, the header object may be added to using these accessor
	 * methods.
	 * 
	 * @return an ArrayList containing header information
	 */
	public ArrayList<String> getHeader();

	/**
	 * Sets the header of the datahandler
	 * 
	 * @param header
	 *            String
	 */
	public void setHeader(String header);

	/**
	 * Sets the header of the datahandler
	 * 
	 * @param header
	 *            ArrayList
	 */
	public void setHeader(ArrayList<String> header);
	
	/**
	 * Remove a IDataWriterExtender to handle DataWriter events.
	 * @param dataWriterExtender 
	 */
	void removeDataWriterExtender(IDataWriterExtender dataWriterExtender);	
	
	/**
	 * Adds an additional IDataWriterExtender to handle DataWriter events
	 * @param dataWriterExtender
	 */
	void addDataWriterExtender(IDataWriterExtender dataWriterExtender);
	
	/**
	 * method used to allow filewriter to configure the scanNumber based on the 
	 * supplied value or internally if null
	 * It is called by the ScanBase.prepareScanForCollection
	 * @param scanNumber
	 * @throws Exception 
	 */
	void configureScanNumber(Long scanNumber) throws Exception;
}