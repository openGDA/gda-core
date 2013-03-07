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

package gda.device.attenuator;

import gda.device.Attenuator;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gov.aps.jca.Channel;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EpicsAttenuatorBase implements Attenuator, MonitorListener, InitializationListener{

	private static final Logger logger = LoggerFactory.getLogger(EpicsAttenuatorBase.class);

	protected EpicsController controller;
	protected EpicsChannelManager channelManager;
	protected Channel desiredEnery;
	protected Channel desiredTransmission;
	protected Channel actualTransmission;
	protected Channel change;
	protected Channel actualEnergy;

	public EpicsAttenuatorBase() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);

	}
	
	private String name;
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public double getClosestMatchTransmission(double transmission) throws DeviceException {
		try {
			controller.caputWait(desiredTransmission, transmission);
			//this is crap but the caputcallback will not work in the same method... 
			Thread.sleep(250);
			return controller.cagetDouble(actualTransmission);
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in getClosestMatchTransmission()", e);
		}
	}

	@Override
	public ClosestMatchTransmission getClosestMatchTransmission(double transmission, double energyInKeV) throws DeviceException {
		try {
			controller.caput(desiredTransmission, transmission);
			controller.caput(desiredEnery, energyInKeV);
			Thread.sleep(500);
			ClosestMatchTransmission cmt = new ClosestMatchTransmission();
			cmt.closestAchievableTransmission = controller.cagetDouble(actualTransmission);
			cmt.energy = controller.cagetDouble(actualEnergy);
			return cmt;
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in getClosestMatchTransmission()", e);
		}
	}

	@Override
	public double setTransmission(double transmission) throws DeviceException {
		try {
			logger.info("Setting desired transmission to " + transmission);
			controller.caput(desiredTransmission, transmission);
			logger.info("Sending change filter command.");
			controller.caputWait(change, 1);
			Thread.sleep(250);
			return controller.cagetDouble(actualTransmission);
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in setTransmission()", e);
		}
	}

	@Override
	public double getTransmission() throws DeviceException {
		try {
			return controller.cagetDouble(actualTransmission);
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in getTransmission()", e);
		}
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		// inform observers about change in filters or actual transmission??
	}

	@Override
	public void initializationCompleted() {
		logger.info(getName() + " received initializationCompleted message");
	}

	@Override
	public double getClosestMatchEnergy() throws DeviceException {
		try {
			return controller.cagetDouble(actualEnergy);
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in getClosestMatchEnergy()", e);
		}
	}

	@Override
	public double getDesiredEnergy() throws DeviceException {
		try {
			return controller.cagetDouble(desiredEnery);
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in getDesiredEnergy()", e);
		}
	}

	@Override
	public double getDesiredTransmission() throws DeviceException {
		try {
			return controller.cagetDouble(desiredTransmission);
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in getDesiredTransmission()", e);
		}
	}

}
