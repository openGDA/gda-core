/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import java.util.List;

import org.eclipse.scanning.api.points.models.IMapPathModel;

public interface IMappingRegionManager {

	/**
	 * @return a copy of the configured regions
	 */
	public List<IMappingScanRegionShape> getTemplateRegions();

	/**
	 * Get the default/template instance of the given type
	 * @param regionClass
	 * @return instance with default parameters
	 */
	public <T extends IMappingScanRegionShape> T getTemplateRegion(Class<T> regionClass);

	public List<IMapPathModel> getValidPaths(IMappingScanRegionShape scanRegion);
}
