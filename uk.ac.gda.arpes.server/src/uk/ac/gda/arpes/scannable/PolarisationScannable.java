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

package uk.ac.gda.arpes.scannable;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

/**
 * This is a simple scannable to allow the ID to be moved to a new polarisation, as is the typical requirement of users.
 * The ID currently needs to be moved to a energy and polarisation therefore this scannable uses the PGM to find out the
 * current energy when about to move the ID. This scannable will report busy when ID reports busy. This scannable only
 * implements basic input validation, but if the ID can't deliver the requested setting it will throw causing this
 * device to throw. Currently the valid input values are "LH", "LV", "CR", "CL".
 * 
 * @author James Mudd
 */
@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
public class PolarisationScannable extends ScannableBase {

	private Scannable pgm;
	private I05Apple id;

	@Override
	public boolean isBusy() throws DeviceException {
		return id.isBusy();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		String polarisation;
		
		if (position instanceof String) {
			polarisation = (String) position;
		} else {
			throw new DeviceException("Expecting polarisation string! Valid options are: \"LH\", \"LV\". \"CR\", \"CL\"");
		}

		// Get the energy from the pgm
		Object energy = pgm.getPosition();
		
		// Move the ID to the new gap and polarisation
		id.asynchronousMoveTo(new Object[] {energy, polarisation});
	}
	
	@Override
	public Object rawGetPosition() throws DeviceException {
		// For the polarisation just return it from the ID
		Object[] idPosition = (Object[]) id.getPosition();
		return idPosition[1];
	}

	public Scannable getPgm() {
		return pgm;
	}

	public void setPgm(Scannable pgm) {
		this.pgm = pgm;
	}

	public I05Apple getId() {
		return id;
	}

	public void setId(I05Apple id) {
		this.id = id;
	}

}