/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.zebra.model;

import gda.device.zebra.controller.Zebra;

public class ZebraPosnComp {
	public ZebraPosNCompArm arm;
	public ZebraPosnCompGate gate;
	public ZebraPosnCompPulse pulse;
	public ZebraPosnComp(Zebra zebra) {
		super();
		arm = new ZebraPosNCompArm();
		arm.setZebra(zebra);
		gate = new ZebraPosnCompGate();
		gate.setZebra(zebra);
		pulse = new ZebraPosnCompPulse();
		pulse.setZebra(zebra);
	}
	
	
	
}
