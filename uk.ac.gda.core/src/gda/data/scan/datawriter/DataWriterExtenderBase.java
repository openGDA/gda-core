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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for DataWriterExtenders that helps with extending the extenders
 * 
 * extending classes need to call super.completeCollection and super.addData last in their
 * respective methods
 */
public class DataWriterExtenderBase implements IDataWriterExtender {

	private static final Logger logger = LoggerFactory.getLogger(DataWriterExtenderBase.class);

	private List<IDataWriterExtender> dataWriterExtenders = new ArrayList<IDataWriterExtender>();	

	@Override
	public void addDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		if (dataWriterExtender != null) this.dataWriterExtenders.add(dataWriterExtender);
	}

	@Override
	public void removeDataWriterExtender(IDataWriterExtender dataWriterExtender) {
		this.dataWriterExtenders.remove(dataWriterExtender);
	}

	/**
	 * Convenience method that calls addData(this, dataPoint) 
	 * 
	 * @param dataPoint
	 */
	public void addData(IScanDataPoint dataPoint) {
		try {
			addData(this, dataPoint);
		} catch (Exception e) {
			// we ignore them here, they should have been logged already
			// careful users of this class should use addData(this, dataPoint) directly
		}
	}
	
	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		Exception savedException = null;
		for(IDataWriterExtender dwe : dataWriterExtenders) {
			try {
				dwe.addData(parent, dataPoint);
			} catch (Exception e) {
				logger.error("Exception seen in DataWriterExtener: ", e);
				if (savedException == null)
					savedException = new Exception("Exception in "+dwe.toString()+": ",e);
			}
		}		
		
		if (savedException != null) {
			// rethrowing first exception seen:
			throw savedException;
		}
	}
	
	/**
	 * Convenience method that calls completeCollection(this) 
	 * 
	 */
	public void completeCollection() {
		completeCollection(this);
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		for(IDataWriterExtender dwe : dataWriterExtenders) {
			dwe.completeCollection(this);
		}			
	}
}