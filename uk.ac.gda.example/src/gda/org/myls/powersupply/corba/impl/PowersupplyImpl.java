/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.org.myls.powersupply.corba.impl;

import gda.org.myls.CorbaImplBase;
import gda.org.myls.powersupply.ICorbarisablePowerSupply;
import gda.org.myls.powersupply.corba.CorbaPowerSupplyPOA;


/**
 *
 */
public class PowersupplyImpl extends CorbaPowerSupplyPOA  {
	protected CorbaImplBase base;
	protected ICorbarisablePowerSupply powerSupply;
	protected org.omg.PortableServer.POA poa;

	/**
	 * @param powerSupply
	 * @param poa
	 */
	public PowersupplyImpl(ICorbarisablePowerSupply powerSupply, org.omg.PortableServer.POA poa) {
		this.powerSupply = powerSupply;
		this.poa = poa;
		base = new CorbaImplBase(powerSupply);
	}

	@Override
	public boolean getOn() {
		return powerSupply.getOn();
	}

	@Override
	public void setOn(boolean on) {
		powerSupply.setOn(on);
	}
}
