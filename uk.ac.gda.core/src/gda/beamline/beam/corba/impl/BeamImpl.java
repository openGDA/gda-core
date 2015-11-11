/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.beamline.beam.corba.impl;

import gda.beamline.BeamInfo;
import gda.beamline.beam.corba.CorbaBeamInfoPOA;
import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.factory.corba.util.EventDispatcher;
import gda.factory.corba.util.EventService;
import gda.observable.IObserver;

/**
 * A server side implementation for a distributed BeamInfo interface.
 */
public class BeamImpl extends CorbaBeamInfoPOA implements IObserver {

	private BeamInfo theObject = null;

	private org.omg.PortableServer.POA poa;

	private EventDispatcher dispatcher;

	private String name;

	/**
	 * Create server side implementation to the CORBA package.
	 *
	 * @param object
	 *            the BeamInfo implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public BeamImpl(BeamInfo object, org.omg.PortableServer.POA poa) {
		this.theObject = object;
		this.poa = poa;

		name = theObject.getName();
		dispatcher = EventService.getInstance().getEventDispatcher();
		theObject.addIObserver(this); //FIXME: potential race condition
	}

	/**
	 * Get the implementation object
	 *
	 * @return the BeamInfo implementation object
	 */
	public BeamInfo _delegate() {
		return theObject;
	}

	/**
	 * Set the implementation object.
	 *
	 * @param theobject
	 *            set the BeamInfo implementation object
	 */
	public void _delegate(BeamInfo theobject) {
		this.theObject = theobject;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void update(Object o, Object arg) {
		dispatcher.publish(name, arg);
	}

	@Override
	public void setWavelength(double value) {
		theObject.setWavelength(value);
	}

	@Override
	public double getWavelength() {
		return theObject.getWavelength();
	}

	@Override
	public void setEnergy(double energy) {
		theObject.setEnergy(energy);
	}

	@Override
	public double getEnergy() {
		return theObject.getEnergy();
	}

	/**
	 * @see #isCalibrated()
	 */
	@Override
	public boolean isCalibrated() {
		return theObject.isCalibrated();
	}

	@Override
	public void setEnergyFromDCM() throws CorbaDeviceException {
		try {
			theObject.setEnergy();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

}
