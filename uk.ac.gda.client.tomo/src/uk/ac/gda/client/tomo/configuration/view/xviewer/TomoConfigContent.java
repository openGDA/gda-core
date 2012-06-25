/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.configuration.view.xviewer;


/**
 *
 */
public class TomoConfigContent implements ITomoConfigContent {

	private double sampleExposureTime;
	private double flatExposureTime;
	private double sampleDetectorDistance;
	private String sampleDescription;

	@Override
	public double getSampleExposureTime() {
		return sampleExposureTime;
	}

	public void setSampleExposureTime(double sampleExposureTime) {
		this.sampleExposureTime = sampleExposureTime;
	}

	@Override
	public double getFlatExposureTime() {
		return flatExposureTime;
	}

	public void setFlatExposureTime(double flatExposureTime) {
		this.flatExposureTime = flatExposureTime;
	}

	@Override
	public double getSampleDetectorDistance() {
		return sampleDetectorDistance;
	}

	public void setSampleDetectorDistance(double sampleDetectorDistance) {
		this.sampleDetectorDistance = sampleDetectorDistance;
	}

	@Override
	public String getSampleDescription() {
		return sampleDescription;
	}

	public void setSampleDescription(String sampleDescription) {
		this.sampleDescription = sampleDescription;
	}
}
