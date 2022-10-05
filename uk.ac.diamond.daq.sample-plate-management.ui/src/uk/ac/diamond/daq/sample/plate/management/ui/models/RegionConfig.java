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

package uk.ac.diamond.daq.sample.plate.management.ui.models;

import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.swt.widgets.Group;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class RegionConfig {

	private Group group;

	private IMappingScanRegionShape mappingRegion;

	private IMapPathModel pointGeneratorModel;

	public RegionConfig() {}

	public RegionConfig(Group group, IMappingScanRegionShape mappingRegion, IMapPathModel pointGeneratorModel) {
		this.group = group;
		this.mappingRegion = mappingRegion;
		this.pointGeneratorModel = pointGeneratorModel;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public IMappingScanRegionShape getMappingRegion() {
		return mappingRegion;
	}

	public void setMappingRegion(IMappingScanRegionShape mappingRegion) {
		this.mappingRegion = mappingRegion;
	}

	public IMapPathModel getPointGeneratorModel() {
		return pointGeneratorModel;
	}

	public void setPointGeneratorModel(IMapPathModel pointGeneratorModel) {
		this.pointGeneratorModel = pointGeneratorModel;
	}
}