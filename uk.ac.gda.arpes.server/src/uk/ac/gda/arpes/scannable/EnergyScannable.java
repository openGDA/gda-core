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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.factory.FactoryException;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This is a simple scannable to allow the PGM and ID to be moved to a new energy, (specified in eV) together, as is the
 * typical requirement of users. This scannable will report busy when either the PGM or ID report busy. This scannable
 * will pass through updates received from the PGM. This scannable does not implement it's own limits but if the ID or
 * PGM can't deliver the requested settings they will throw causing this device to throw.
 *
 * @author James Mudd
 */
@ServiceInterface(Scannable.class)
public class EnergyScannable extends ScannableBase {
	
	private static Logger logger = LoggerFactory.getLogger(EnergyScannable.class);
	private Scannable pgm;
	private I05Apple id;
	private double user_llm;
	private double user_hlm;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		super.configure();

		// Make this device observe the PGM so it will fire events when the PGM starts moving.
		pgm.addIObserver(this::notifyIObservers);
		
		setConfigured(true);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		// If either the PGM or the ID is busy this device is also busy.
		return pgm.isBusy() || id.isBusy();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// Convert to double handling all the cases. Fixes I05-37 where PyDouble is passed in
		final double energy = PositionConvertorFunctions.toDouble(position);
		
		// Check user limits
		userLimitsCheck(energy);

		// Move the ID first. If it can't deliver the required settings it will throw and neither the PGM
		// or the ID will be moved.

		// Move the ID to the new gap, leave phase unchanged (pass null).
		id.asynchronousMoveTo(new Object[] { energy, null });

		// Move the PGM to the new energy
		pgm.asynchronousMoveTo(energy);
	}

	private void userLimitsCheck(double energy) throws DeviceException {
		if (((user_llm != 0) && (energy < user_llm)) || ((user_hlm != 0) && (energy > user_hlm))) {
			throw new DeviceException("Target energy position violates user limits " + user_llm + " ... " + user_hlm);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		// For the energy just return the PGM position which really defines the energy.
		return pgm.getPosition();
	}

	@Override
	public void stop() {
		try {
			pgm.stop();
		} catch (DeviceException e) {
			logger.error("Failed to stop pgm scannable", e);
		}
		// not using id.stop as it is discouraged https://confluence.diamond.ac.uk/display/OPS/Controls+I05+Recovery+Information
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

	public double getUser_llm() {
		return user_llm;
	}

	public void setUser_llm(double user_llm) {
		this.user_llm = user_llm;
	}

	public double getUser_hlm() {
		return user_hlm;
	}

	public void setUser_hlm(double user_hlm) {
		this.user_hlm = user_hlm;
	}

}