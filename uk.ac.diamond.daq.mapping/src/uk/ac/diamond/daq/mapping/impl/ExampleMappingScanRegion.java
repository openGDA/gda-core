/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.impl;

import org.eclipse.scanning.api.points.models.IScanPathModel;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

public class ExampleMappingScanRegion implements IMappingScanRegion {

	private IMappingScanRegionShape region;
	private IScanPathModel scanPath;

	// private IDataset regionContext;

	@Override
	public IMappingScanRegionShape getRegion() {
		return region;
	}

	@Override
	public void setRegion(IMappingScanRegionShape region) {
		this.region = region;
	}

	@Override
	public IScanPathModel getScanPath() {
		return scanPath;
	}

	@Override
	public void setScanPath(IScanPathModel scanPath) {
		this.scanPath = scanPath;
	}

	// @Override
	// public IDataset getRegionContext() {
	// return regionContext;
	// }
	//
	// @Override
	// public void setRegionContext(IDataset regionContext) {
	// this.regionContext = regionContext;
	// }
}
