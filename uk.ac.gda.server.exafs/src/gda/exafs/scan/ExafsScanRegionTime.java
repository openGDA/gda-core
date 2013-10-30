/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.exafs.scan;

public class ExafsScanRegionTime {

	private String regionName;
	private int stepsCount;
	private double[] time;

	public ExafsScanRegionTime(String regionName, int stepsCount, double[] time) {
		super();
		this.regionName = regionName;
		this.stepsCount = stepsCount;
		this.time = time;
	}
	
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	
	public String getRegionName() {
		return regionName;
	}
	
	public void setStepsCount(int stepsCount) {
		this.stepsCount = stepsCount;
	}
	
	public int getStepsCount() {
		return stepsCount;
	}
	
	public void setTime(double[] time) {
		this.time = time;
	}
	
	public double[] getTime() {
		return time;
	}
}
