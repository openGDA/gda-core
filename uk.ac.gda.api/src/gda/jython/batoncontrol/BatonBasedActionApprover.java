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

package gda.jython.batoncontrol;

import java.util.function.BooleanSupplier;

import gda.device.ActionApprover;
import gda.jython.JythonServerFacade;

public final class BatonBasedActionApprover implements ActionApprover {

	private static final String BATON_NOT_HELD_DENIAL = "Baton was not held";
	private static final String NO_DENIAL_WHEN_BATON_HELD = "";

	private final transient BooleanSupplier queryIsBatonHeld;
	private String denial = BATON_NOT_HELD_DENIAL;

	public BatonBasedActionApprover(JythonServerFacade jsf) {
		queryIsBatonHeld = jsf::isBatonHeld;
	}

	@Override
	public boolean actionApproved() {
		var approved = queryIsBatonHeld.getAsBoolean();
		denial = approved ? NO_DENIAL_WHEN_BATON_HELD : BATON_NOT_HELD_DENIAL;
		return approved;
	}

	@Override
	public String getDenialReason() {
		return denial;
	}

}
