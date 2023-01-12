/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.client.pixium.views;


public class PUMode {
	private int puModeID;
	private String resolution;
	private String minimumExposure;
	private String maximumRate;
	
	public PUMode(int pumode, String resolution, String minExp, String maxRate) {
		this.setPuModeID(pumode);
		this.resolution=resolution;
		this.minimumExposure=minExp;
		this.maximumRate=maxRate;
	}

	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	public String getMinimumExposure() {
		return minimumExposure;
	}
	public void setMinimumExposure(String minimumExposure) {
		this.minimumExposure = minimumExposure;
	}
	public String getMaximumRate() {
		return maximumRate;
	}
	public void setMaximumRate(String maximumRate) {
		this.maximumRate = maximumRate;
	}
	public int getPuModeID() {
		return puModeID;
	}

	public void setPuModeID(int puModeID) {
		this.puModeID = puModeID;
	}
}
