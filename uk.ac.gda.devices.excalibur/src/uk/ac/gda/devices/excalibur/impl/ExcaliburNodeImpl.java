/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.excalibur.impl;

import uk.ac.gda.devices.excalibur.ExcaliburNodeWrapper;
import uk.ac.gda.devices.excalibur.Fix;
import uk.ac.gda.devices.excalibur.Gap;
import uk.ac.gda.devices.excalibur.Master;

/**
 *
 */
public abstract class ExcaliburNodeImpl implements ExcaliburNodeWrapper {

	protected Master mst;
	protected Gap gap;
	protected Fix fix;

	@Override
	public Master getMst() {
		return mst;
	}

	public void setMst(Master mst) {
		this.mst = mst;
	}

	@Override
	public Gap getGap() {
		return gap;
	}

	public void setGap(Gap gap) {
		this.gap = gap;
	}

	@Override
	public Fix getFix() {
		return fix;
	}

}
