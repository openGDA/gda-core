/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device.robot;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import gda.epics.LazyPVFactory;
import gda.epics.PVWithSeparateReadback;
import gda.epics.PV;
import gda.factory.Configurable;
import gda.factory.FactoryException;

/** Robot state check that checks a PV is set to the correct value and attempts to set it if not */
public class PVCheck implements RobotExternalStateCheck, Configurable {
	private String pvName;
	private String rbvPvName;
	private boolean correctIfInvalid;

	private int value;
	private PV<Integer> pv;


	@Override
	public void check() throws InvalidExternalState {
		try {
			if (correctIfInvalid && pv.get() != value) {
				pv.putWait(value);
			}
			if (pv.get() != value) {
				throw new InvalidExternalState("PV " + pvName + " could not be set to " + value);
			}
		} catch (IOException e) {
			throw new InvalidExternalState("Could not get or set value for PV " + pvName, e);
		}
	}

	@Override
	public void configure() throws FactoryException {
		requireNonNull(pvName, "pvName must be set");
		if (rbvPvName == null) {
			pv = LazyPVFactory.newIntegerPV(pvName);
		} else {
			pv = new PVWithSeparateReadback<Integer>(
					LazyPVFactory.newIntegerPV(pvName),
					LazyPVFactory.newReadOnlyIntegerPV(rbvPvName)
			);
		}
	}

	@Override
	public boolean isConfigured() {
		return pv != null;
	}

	@Override
	public void reconfigure() throws FactoryException {
		configure();
	}

	@Override
	public boolean isConfigureAtStartup() {
		return true;
	}

	public void setPvName(String pv) {
		pvName = pv;
	}

	public String getPvName() {
		return pvName;
	}

	public void setRbvPvName(String pv) {
		rbvPvName = pv;
	}

	public String getRbvPvName() {
		return rbvPvName;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setCorrectIfInvalid(boolean correct) {
		correctIfInvalid = correct;
	}

	public boolean isCorrectIfInvalid() {
		return correctIfInvalid;
	}
}
