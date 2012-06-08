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

package gda.jython;

import gda.scan.IScanDataPoint;


/**
 * 
 */
public interface IScanDataPointProvider{
	/**
	 * Add an object to this objects's list of IObservers.
	 * 
	 * @param anObserver
	 *            object that implement IObserver and wishes to be notified by this object
	 */
	void addIScanDataPointObserver(IScanDataPointObserver anObserver);

	/**
	 * Delete an object from this objects's list of IObservers.
	 * 
	 * @param anObserver
	 *            object that implement IObserver and wishes to be notified by this object
	 */
	void deleteIScanDataPointObserver(IScanDataPointObserver anObserver);
	
	/**
	 * @param dataSource
	 * @param data
	 */
	public void update(Object dataSource, Object data);
	
	public IScanDataPoint getLastScanDataPoint();

}
