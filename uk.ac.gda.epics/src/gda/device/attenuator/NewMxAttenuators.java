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

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;

public class NewMxAttenuators extends EpicsAttenuatorBase {

	private static int NUMBER_OF_FILTERS = 16;
	
	private static final String[] FILTER_NAMES = new String[] {
		"25 Ag",
		"50 Ag",
		"75 Ag",
		"100 Ag",
		"200 Ag",
		"2.4 Al",
		"4.8 Al",
		"6.5 Al",
		"12 Al",
		"20 Al",
		"40 Al",
		"80 Al",
		"100 Al",
		"200 Al",
		"400 Al",
		"800 Al"
	};
	
	private String pvPrefix;
	
	public void setPvPrefix(String pvPrefix) {
		this.pvPrefix = pvPrefix;
	}
	
	@Override
	public void configure() throws FactoryException {
		createChannelAccess();
		channelManager.tryInitialize(100);
	}
	
	protected Channel[] filterChannels;
	
	protected Channel[] calculatedChannels;
	
	private void createChannelAccess() throws FactoryException {
		try {
			desiredEnery = channelManager.createChannel(pvPrefix + ":E2WL:SETVAL1", true);
			desiredTransmission = channelManager.createChannel(pvPrefix + ":T2A:SETVAL1", true);
			actualEnergy = channelManager.createChannel(pvPrefix + ":ENERGYMATCH", true);
			actualTransmission = channelManager.createChannel(pvPrefix + ":MATCH", true);
			change = channelManager.createChannel(pvPrefix + ":FANOUT", true);
			useCurrentEnergy = channelManager.createChannel(pvPrefix + ":E2WL:USECURRENTENERGY.PROC", true);
			
			filterChannels = new Channel[NUMBER_OF_FILTERS];
			calculatedChannels = new Channel[NUMBER_OF_FILTERS];
			
			for (int i=0; i<NUMBER_OF_FILTERS; i++) {
				final String statusPv = pvPrefix + ":FILTER" + (i+1) + ":CTRL";
				filterChannels[i] = channelManager.createChannel(statusPv, true);
				final String calculatedPv = pvPrefix + ":DEC_TO_BIN.B" + Integer.toHexString(i).toUpperCase();
				calculatedChannels[i] = channelManager.createChannel(calculatedPv, true);
			}
			
			channelManager.creationPhaseCompleted();
		} catch (CAException e) {
			throw new FactoryException("Unable to create channels", e);
		}
	}
	
	@Override
	public boolean[] getFilterPositions() throws DeviceException {
		boolean[] positions = new boolean[NUMBER_OF_FILTERS];
		try {
			for (int i=0; i<NUMBER_OF_FILTERS; i++) {
				int val = controller.cagetInt(filterChannels[i]);
				positions[i] = (val == 1);
			}
		} catch (Exception e) {
			throw new DeviceException("Could not read filter states", e);
		} 
		return positions;
	}
	
	@Override
	public boolean[] getDesiredFilterPositions() throws DeviceException {
		boolean[] positions = new boolean[NUMBER_OF_FILTERS];
		try {
			for (int i=0; i<NUMBER_OF_FILTERS; i++) {
				int val = controller.cagetInt(calculatedChannels[i]);
				positions[i] = (val == 1);
			}
		} catch (Exception e) {
			throw new DeviceException("Could not read filter states", e);
		}
		return positions;
	}
	
	@Override
	public int getNumberFilters() throws DeviceException {
		return NUMBER_OF_FILTERS;
	}
	
	@Override
	public String[] getFilterNames() throws DeviceException {
		return FILTER_NAMES.clone();
	}

}
