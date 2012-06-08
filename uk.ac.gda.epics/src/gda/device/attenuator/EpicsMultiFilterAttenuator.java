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

import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link MultiFilterAttenuator} that controls independent filters through EPICS.
 */
public class EpicsMultiFilterAttenuator extends MultiFilterAttenuator implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsMultiFilterAttenuator.class);
	
	@Override
	public void setFilters(List<AttenuatorFilter> filters) {
		
		// Check that every filter is an EpicsAttenuatorFilter
		if (!collectionContainsOnly(filters, EpicsAttenuatorFilter.class)) {
			final String msg = String.format("Every filter of an %s must be an %s",
				getClass().getSimpleName(),
				EpicsAttenuatorFilter.class.getSimpleName());
			throw new IllegalArgumentException(msg);
		}
		
		super.setFilters(filters);
	}
	
	protected static boolean collectionContainsOnly(Collection<?> collection, Class<?> clazz) {
		for (Object o : collection) {
			if (!(clazz.isAssignableFrom(o.getClass()))) {
				return false;
			}
		}
		return true;
	}
	
	private EpicsController controller;
	
	private EpicsChannelManager channelManager;
	
	private Channel[] channels;
	
	@Override
	public void configure() throws FactoryException {
		prepareTransmissionLookupTable();
		createChannels();
	}
	
	protected void createChannels() throws FactoryException {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		int numFilters = filters.size();
		channels = new Channel[numFilters];
		try {
			for (int i=0; i<numFilters; i++) {
				EpicsAttenuatorFilter filter = (EpicsAttenuatorFilter) filters.get(i);
				channels[i] = channelManager.createChannel(filter.getPv());
			}
			channelManager.creationPhaseCompleted();
		} catch (CAException e) {
			throw new FactoryException("Could not create channels", e);
		}
	}
	
	@Override
	protected void setFilterStates(Transmission transmission) {
		logger.info("Setting filters to {}", Arrays.toString(transmission.getFilterStates()));
		final int numFilters = filters.size();
		boolean[] states = transmission.getFilterStates();
		try {
			for (int i=0; i<numFilters; i++) {
				final int ctrl = (states[i] ? 1 : 0);
				controller.caput(channels[i], ctrl);
			}
		} catch (Exception e) {
			logger.error("Unable to set state of attenuator filters", e);
		}
	}

	@Override
	public void initializationCompleted() {
		logger.info("Channels connected");
		readInitialState();
	}
	
	protected void readInitialState() {
		logger.info("Reading initial state of filters...");
		final int numFilters = filters.size();
		boolean[] initialState = new boolean[numFilters];
		try {
			for (int i=0; i<numFilters; i++) {
				int ctrl = controller.cagetInt(channels[i]);
				initialState[i] = (ctrl == 1);
			}
		} catch (Exception e) {
			logger.error("Unable to read initial state of attenuator filters", e);
		}
		logger.info("Initial state of filters is {}", Arrays.toString(initialState));
		
		for (Transmission t : transmissions) {
			if (Arrays.equals(t.getFilterStates(), initialState)) {
				actualTransmission = t;
				break;
			}
		}
		logger.info("Initial transmission is {}", actualTransmission);
	}
	
}
