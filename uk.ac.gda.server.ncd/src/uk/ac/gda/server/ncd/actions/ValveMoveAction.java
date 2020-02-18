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

package uk.ac.gda.server.ncd.actions;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.enumpositioner.ValvePosition;

public class ValveMoveAction extends UnsafeNcdAction {

	private EnumPositioner valve;
	private String targetPositition = ValvePosition.OPEN;

	public ValveMoveAction() {
		super(true);
	}

	public ValveMoveAction(boolean propagate) {
		super(propagate);
	}

	@Override
	public void runUnsafe() throws DeviceException {
		valve.moveTo(targetPositition);
	}

	public String getTargetPositition() {
		return targetPositition;
	}

	public void setTargetPositition(String position) {
		this.targetPositition = position;
	}

	public EnumPositioner getValve() {
		return valve;
	}

	public void setValve(EnumPositioner valve) {
		this.valve = valve;
	}

	@Override
	public String toString() {
		return getName() + " - " + valve.getName();
	}
}
