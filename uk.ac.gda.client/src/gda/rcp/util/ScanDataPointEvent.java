/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.rcp.util;

import gda.scan.IScanDataPoint;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;

/**
 *
 */
public class ScanDataPointEvent extends EventObject {

	private Collection<IScanDataPoint> dataPoints;
	private IScanDataPoint             currentPoint;

	/**
	 * @param dataPoints 
	 * @param currentPoint 
	 */
	public ScanDataPointEvent(Collection<IScanDataPoint> dataPoints, IScanDataPoint currentPoint) {
		super(currentPoint!=null?currentPoint:dataPoints);
		this.dataPoints   = dataPoints;
		this.currentPoint = currentPoint;
	}

	/**
	 * 
	 * @param points
	 */
	public ScanDataPointEvent(List<IScanDataPoint> points) {
		this(points,null);
	}

	/**
	 * @return Returns the dataPoints.
	 */
	public Collection<IScanDataPoint> getDataPoints() {
		return dataPoints;
	}

	/**
	 * @param dataPoints The dataPoints to set.
	 */
	public void setDataPoints(Collection<IScanDataPoint> dataPoints) {
		this.dataPoints = dataPoints;
	}

	/**
	 * @return Returns the currentPoint.
	 */
	public IScanDataPoint getCurrentPoint() {
		return currentPoint;
	}

	/**
	 * @param currentPoint The currentPoint to set.
	 */
	public void setCurrentPoint(IScanDataPoint currentPoint) {
		this.currentPoint = currentPoint;
	}

}
