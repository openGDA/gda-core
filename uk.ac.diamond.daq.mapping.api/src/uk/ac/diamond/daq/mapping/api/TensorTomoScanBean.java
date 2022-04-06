/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;

import uk.ac.diamond.daq.osgi.OsgiService;

@OsgiService(TensorTomoScanBean.class)
public class TensorTomoScanBean {

	// Note: x and y scannable names come from the Mapping Stage Info
	private String angle1ScannableName = "angle1";
	private String angle2ScannableName = "angle2";
	private IMappingScanRegionShape gridRegionModel; // TODO should be RectangularMappingRegion but that's not API
	private AbstractTwoAxisGridModel gridPathModel;
	private IMappingScanRegionShape angleRegionModel; // TODO should be RectangularMappingRegion but that's not API
	private AbstractTwoAxisGridModel anglePathModel;
	private double exposureTime = 0.1;
	private String malcolmDeviceName;

	public IMappingScanRegionShape getGridRegionModel() {
		return gridRegionModel;
	}

	public void setGridRegionModel(IMappingScanRegionShape gridRegionModel) {
		this.gridRegionModel = gridRegionModel;
	}

	public AbstractTwoAxisGridModel getGridPathModel() {
		return gridPathModel;
	}

	public void setGridPathModel(AbstractTwoAxisGridModel gridPathModel) {
		this.gridPathModel = gridPathModel;
	}

	public String getAngle1ScannableName() {
		return angle1ScannableName;
	}

	public void setAngle1ScannableName(String angle1ScannableName) {
		this.angle1ScannableName = angle1ScannableName;
	}

	public String getAngle2ScannableName() {
		return angle2ScannableName;
	}

	public void setAngle2ScannableName(String angle2ScannableName) {
		this.angle2ScannableName = angle2ScannableName;
	}

	public IMappingScanRegionShape getAngleRegionModel() {
		return angleRegionModel;
	}

	public void setAngleRegionModel(IMappingScanRegionShape angleRegionModel) {
		this.angleRegionModel = angleRegionModel;
	}

	public AbstractTwoAxisGridModel getAnglePathModel() {
		return anglePathModel;
	}

	public void setAnglePathModel(AbstractTwoAxisGridModel anglePathModel) {
		this.anglePathModel = anglePathModel;
	}

	public double getExposureTime() {
		return exposureTime;
	}

	public void setExposureTime(double exposureTime) {
		this.exposureTime = exposureTime;
	}

	public String getMalcolmDeviceName() {
		return malcolmDeviceName;
	}

	public void setMalcolmDeviceName(String malcolmDeviceName) {
		this.malcolmDeviceName = malcolmDeviceName;
	}

}
