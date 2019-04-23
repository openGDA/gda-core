/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.dxp.client.views.model;

/**
 * Dummy mode replacement for {@link DXPModel} using area detector simulator
 * <p>
 * Currently, no values are updated dynamically: there is no straightforward way to do this.
 */
public class DummyDXPModel implements IDXPModel {

	@Override
	public short getAcquireState() throws Exception {
		return 0;
	}

	@Override
	public int getInstantDeadTime() throws Exception {
		return 0;
	}

	@Override
	public double getDeadTime() throws Exception {
		return 0.001;
	}

	@Override
	public double getRealTime() throws Exception {
		return 0.24;
	}

	@Override
	public double getLiveTime() throws Exception {
		return 0.387;
	}
}
