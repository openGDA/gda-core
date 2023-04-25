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

import java.util.Collections;
import java.util.Set;

import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.api.points.models.IAxialModel;

import uk.ac.diamond.daq.osgi.OsgiService;

@OsgiService(TensorTomoScanBean.class)
public class TensorTomoScanBean {

	// Note: x and y scannable names come from the Mapping Stage Info
	private IMappingScanRegionShape gridRegionModel; // TODO should be RectangularMappingRegion but that's not API
	private AbstractTwoAxisGridModel gridPathModel;
	private IScanModelWrapper<IAxialModel> angle1Model; // an AxialStepModel (start, stop step), AxialArrayModel (list of points) or AxialMultiStepModel
	private IScanModelWrapper<IAxialModel> angle2Model; // an AxialPointsModel (start, stop, numPoints) or AxialArrayModel (list of points) for 0 degrees of angle1.
	private String malcolmDeviceName;
	private IMalcolmModel malcolmModel;
	private String sampleName;
	private String backgroundFilePath;
	private Set<String> excludedDetectorNames; // detector names to always exclude (TODO: is this the best place for this property? These values won't change. Hardcode in UI code?

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

	public IScanModelWrapper<IAxialModel> getAngle1Model() {
		return angle1Model;
	}

	public void setAngle1Model(IScanModelWrapper<IAxialModel> angle1Model) {
		this.angle1Model = angle1Model;
	}

	public IScanModelWrapper<IAxialModel> getAngle2Model() {
		return angle2Model;
	}

	public void setAngle2Model(IScanModelWrapper<IAxialModel> angle2Model) {
		this.angle2Model = angle2Model;
	}

	public String getMalcolmDeviceName() {
		return malcolmDeviceName;
	}

	public void setMalcolmDeviceName(String malcolmDeviceName) {
		this.malcolmDeviceName = malcolmDeviceName;
	}

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public IMalcolmModel getMalcolmModel() {
		return malcolmModel;
	}

	public void setMalcolmModel(IMalcolmModel malcolmModel) {
		this.malcolmModel = malcolmModel;
	}

	public String getBackgroundFilePath() {
		return backgroundFilePath;
	}

	public void setBackgroundFilePath(String backgroundFilePath) {
		this.backgroundFilePath = backgroundFilePath;
	}

	public Set<String> getExcludedDetectorNames() {
		if (excludedDetectorNames == null) {
			return Collections.emptySet();
		}

		return excludedDetectorNames;
	}

	public void setExcludedDetectorNames(Set<String> excludedDetectorNames) {
		this.excludedDetectorNames = excludedDetectorNames;
	}

}
