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

import uk.ac.gda.devices.excalibur.ExcaliburNodeWrapper.ReadoutNodeWrapper;
import uk.ac.gda.devices.excalibur.ExcaliburReadoutNodeFem;
import uk.ac.gda.devices.excalibur.NodeFix;

/**
 *
 */
public class ReadoutNodeWrapperImpl extends ExcaliburNodeImpl implements ReadoutNodeWrapper {

	private NodeFix nodeFix;
	private ExcaliburReadoutNodeFem adBase;

	@Override
	public ExcaliburReadoutNodeFem getAdBase() {
		return adBase;
	}

	public void setAdBase(ExcaliburReadoutNodeFem adBase) {
		this.adBase = adBase;
	}

	@Override
	public NodeFix getFix() {
		return nodeFix;
	}

	public void setFix(NodeFix fix) {
		this.nodeFix = fix;
	}

}
