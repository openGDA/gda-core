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

import java.util.List;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.IScanDefinition;
import uk.ac.diamond.daq.mapping.api.IScanPathModelWrapper;

public class ExampleScanDefinition implements IScanDefinition {

	private IMappingScanRegion mappingScanRegion;
	private List<IScanPathModelWrapper> outerScannables;

	public ExampleScanDefinition() {
		mappingScanRegion = new ExampleMappingScanRegion();
	}

	@Override
	public IMappingScanRegion getMappingScanRegion() {
		return mappingScanRegion;
	}

	@Override
	public void setMappingScanRegion(IMappingScanRegion mappingScanRegion) {
		this.mappingScanRegion = mappingScanRegion;
	}

	@Override
	public List<IScanPathModelWrapper> getOuterScannables() {
		return outerScannables;
	}

	@Override
	public void setOuterScannables(List<IScanPathModelWrapper> outerScannables) {
		this.outerScannables = outerScannables;
	}
}
