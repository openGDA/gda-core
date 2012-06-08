/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

/**
 * Class that is called by a DataWriter after it has processed addData
 */
public interface IDataWriterExtender {

	/**
	 * called by a DataWriter after it has processed addData
	 * @param parent 
	 * @param dataPoint
	 * @throws Exception 
	 */
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception;

	/**
	 * called by a DataWriter after it has processed completeCollection
	 * @param parent 
	 */
	public void completeCollection(IDataWriterExtender parent);
	
	/**
	 * Allows additional datawriters to handle data writer events
	 * @param dataWriterExtender
	 */
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender);	
	
	/**
	 * deletes the DataWriterExtenderof the list
	 * @param dataWriterExtender 
	 */
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender);
}
