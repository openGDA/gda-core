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

package gda.device.attenuator;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.epics.interfaces.XiaArrayType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.event.MonitorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Attenuator for Xia array attenuators.
 */
public class EpicsAttenuator extends EpicsAttenuatorBase implements MonitorListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsAttenuator.class);

	private String deviceName;
	private XiaArrayType xiaConfig;
	private Channel[] actualPositions = new Channel[8];
	private Channel[] desiredPositions = new Channel[8];
	private String[] names;

	@Override
	public void configure() throws FactoryException {
		if (getDeviceName() != null) {

			try {
				xiaConfig = Configurator.getConfiguration(getDeviceName(), gda.epics.interfaces.XiaArrayType.class);
				createChannelAccess();
				channelManager.tryInitialize(100);
			} catch (ConfigurationNotFoundException e) {
				logger.error(
						"Can NOT find EPICS configuration for xiaArray " + getDeviceName() + ". " + e.getMessage(), e);
			}

		}
	}

	private void createChannelAccess() throws FactoryException {
		try {
			actualPositions[0] = channelManager.createChannel(xiaConfig.getAA1F1().getPv(), true);
			actualPositions[1] = channelManager.createChannel(xiaConfig.getAA1F2().getPv(), true);
			actualPositions[2] = channelManager.createChannel(xiaConfig.getAA1F3().getPv(), true);
			actualPositions[3] = channelManager.createChannel(xiaConfig.getAA1F4().getPv(), true);
			actualPositions[4] = channelManager.createChannel(xiaConfig.getAA2F1().getPv(), true);
			actualPositions[5] = channelManager.createChannel(xiaConfig.getAA2F2().getPv(), true);
			actualPositions[6] = channelManager.createChannel(xiaConfig.getAA2F3().getPv(), true);
			actualPositions[7] = channelManager.createChannel(xiaConfig.getAA2F4().getPv(), true);
			desiredPositions[0] = channelManager.createChannel(xiaConfig.getDA1F1().getPv(), true);
			desiredPositions[1] = channelManager.createChannel(xiaConfig.getDA1F2().getPv(), true);
			desiredPositions[2] = channelManager.createChannel(xiaConfig.getDA1F3().getPv(), true);
			desiredPositions[3] = channelManager.createChannel(xiaConfig.getDA1F4().getPv(), true);
			desiredPositions[4] = channelManager.createChannel(xiaConfig.getDA2F1().getPv(), true);
			desiredPositions[5] = channelManager.createChannel(xiaConfig.getDA2F2().getPv(), true);
			desiredPositions[6] = channelManager.createChannel(xiaConfig.getDA2F3().getPv(), true);
			desiredPositions[7] = channelManager.createChannel(xiaConfig.getDA2F4().getPv(), true);

			desiredEnery = channelManager.createChannel(xiaConfig.getDEN().getPv(), true);
			desiredTransmission = channelManager.createChannel(xiaConfig.getDTRANS().getPv(), true);
			actualEnergy = channelManager.createChannel(xiaConfig.getAEN().getPv(),true);
			actualTransmission = channelManager.createChannel(xiaConfig.getATRANS().getPv(),true);
			change = channelManager.createChannel(xiaConfig.getCHANGE().getPv(), true);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (CAException e) {
			throw new FactoryException(getName() + " had CAException in configure()", e);
		}
	}

	@Override
	public String[] getFilterNames() {
		if (names == null) {
			names = new String[8];
			names[0] = xiaConfig.getAA1F1().getDesc();
			names[1] = xiaConfig.getAA1F2().getDesc();
			names[2] = xiaConfig.getAA1F3().getDesc();
			names[3] = xiaConfig.getAA1F4().getDesc();
			names[4] = xiaConfig.getAA2F1().getDesc();
			names[5] = xiaConfig.getAA2F2().getDesc();
			names[6] = xiaConfig.getAA2F3().getDesc();
			names[7] = xiaConfig.getAA2F4().getDesc();
		}
		return names;
	}

	@Override
	public boolean[] getFilterPositions() throws DeviceException {
		try {
			boolean[] result = new boolean[8];
			for (int i = 0; i < 8; i++) {
				result[i] = controller.cagetInt(actualPositions[i]) == 1;
			}
			return result;
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in getFilterPositions()", e);
		}
	}
	
	@Override
	public boolean[] getDesiredFilterPositions() throws DeviceException {
		try {
			boolean[] result = new boolean[8];
			for (int i = 0; i < 8; i++) {
				result[i] = controller.cagetInt(desiredPositions[i]) == 1;
			}
			return result;
		} catch (Exception e) {
			throw new DeviceException(getName() + " had Exception in getDesiredFilterPositions()", e);
		}
	}

	@Override
	public int getNumberFilters() {
		return 8;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getDeviceName() {
		return deviceName;
	}

}
