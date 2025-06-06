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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Attenuator;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FindableConfigurableBase;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

public abstract class EpicsAttenuatorBase extends FindableConfigurableBase implements Attenuator, MonitorListener, InitializationListener{

	private static final Logger logger = LoggerFactory.getLogger(EpicsAttenuatorBase.class);

	protected EpicsController controller;
	protected EpicsChannelManager channelManager;
	protected Channel desiredEnergy;
	protected Channel desiredTransmission;
	protected Channel actualTransmission;
	protected Channel change;
	protected Channel actualEnergy;
	protected Channel useCurrentEnergy;
	private final Long timeoutForReadyMs = Long.valueOf(LocalProperties.getInt("gda.px.attenuator.timeout", 5*60*1000));

	protected EpicsAttenuatorBase(EpicsController controller) {
		this.controller = controller;
	}

	protected EpicsAttenuatorBase() {
		this(EpicsController.getInstance());
		channelManager = new EpicsChannelManager(this);
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
	public ClosestMatchTransmission getClosestMatchTransmission(double transmission, double energyInKev) throws DeviceException {
		try {
			controller.caput(desiredTransmission, transmission);
			controller.caput(desiredEnergy, energyInKev);
			Thread.sleep(500);
			var cmt = new ClosestMatchTransmission();
			var readTransmission = controller.cagetDouble(actualTransmission);
			cmt.setClosestAchievableTransmission(readTransmission);
			var actualEnergyInKev = controller.cagetDouble(actualEnergy);
			cmt.setEnergy(actualEnergyInKev);
			return cmt;
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in getClosestMatchTransmission()", e);
		}
	}

	@Override
	public double setTransmission(double transmission) throws DeviceException {
		try {
			logger.info("Using current energy.");
			controller.caput(useCurrentEnergy, 1);
			logger.info("Setting desired transmission to " + transmission);
			controller.caput(desiredTransmission, transmission);
			logger.info("Sending change filter command.");
			controller.caputWait(change, 1);
			var expiry = Instant.now().plusMillis(timeoutForReadyMs);
			waitUntilReadyOrTimeoutAt(expiry);
			return controller.cagetDouble(actualTransmission);
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in setTransmission()", e);
		}
	}

	private void waitUntilReadyOrTimeoutAt(Instant expiryTime) throws InterruptedException, DeviceException, TimeoutException {
        var pollingIntervalMs = 50L;

        while (!isReady()) {
			Thread.sleep(pollingIntervalMs);
			var remainingMillis = ChronoUnit.MILLIS.between(Instant.now(),expiryTime);
			if (remainingMillis < 0L) {
				throw new TimeoutException();
			}
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
			return controller.cagetDouble(desiredEnergy);
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

	@Override
	public boolean isReady() throws DeviceException {
		try {
			boolean[] desiredPositions = getDesiredFilterPositions();
			boolean[] actualPositions = getFilterPositions();
			return IntStream.range(0, desiredPositions.length).allMatch(i -> desiredPositions[i] == actualPositions[i]);
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in isReady()", e);
		}
	}
}
