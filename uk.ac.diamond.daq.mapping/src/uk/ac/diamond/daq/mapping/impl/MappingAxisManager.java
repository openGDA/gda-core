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

package uk.ac.diamond.daq.mapping.impl;

import uk.ac.diamond.daq.mapping.api.IMappingAxisManager;

/**
 * A class which holds information about the current axes used for mapping experiments. The intention is that a single
 * instance of this, created and initialised by Spring, will be a single point of reference for any parts of the system
 * that need to know or check the axis names.
 */
// TODO broadcast events (over OSGi EventAdmin?) when the scan axes are changed
// TODO find some way of changing the axis names from the server so scripts can change it
public class MappingAxisManager implements IMappingAxisManager {

	private String activeFastScanAxis;
	private String activeSlowScanAxis;

	@Override
	public String getActiveFastScanAxis() {
		return activeFastScanAxis;
	}

	@Override
	public void setActiveFastScanAxis(String activeFastScanAxis) {
		this.activeFastScanAxis = activeFastScanAxis;
	}

	@Override
	public String getActiveSlowScanAxis() {
		return activeSlowScanAxis;
	}

	@Override
	public void setActiveSlowScanAxis(String activeSlowScanAxis) {
		this.activeSlowScanAxis = activeSlowScanAxis;
	}
}
