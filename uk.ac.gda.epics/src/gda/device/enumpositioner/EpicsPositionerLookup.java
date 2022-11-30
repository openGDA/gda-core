/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.device.enumpositioner;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * Allows us to look up the positions of the underlying motor record programmed in EPICS
 * against each labelled position of the multipositioner.
 */
public class EpicsPositionerLookup {

	private static final Logger logger = LoggerFactory.getLogger(EpicsPositionerLookup.class);
	private final EpicsController epicsController = EpicsController.getInstance();
	private final Map<Channel, Channel> nameToPosition;
	private String pvPrefix;

	public EpicsPositionerLookup(String pvPrefix) {
		this.pvPrefix = pvPrefix.endsWith(":") ? pvPrefix : pvPrefix + ":";
		nameToPosition = new HashMap<>();
		try {
			createMappedChannels("MP:SELECT.ZRST", "P:VALA");
			createMappedChannels("MP:SELECT.ONST", "P:VALB");
			createMappedChannels("MP:SELECT.TWST", "P:VALC");
			createMappedChannels("MP:SELECT.THST", "P:VALD");
			createMappedChannels("MP:SELECT.FRST", "P:VALE");
			createMappedChannels("MP:SELECT.FVST", "P:VALF");
			createMappedChannels("MP:SELECT.SXST", "P:VALG");
			createMappedChannels("MP:SELECT.SVST", "P:VALH");
			createMappedChannels("MP:SELECT.EIST", "P:VALI");
			createMappedChannels("MP:SELECT.NIST", "P:VALJ");
			createMappedChannels("MP:SELECT.TEST", "P:VALK");
			createMappedChannels("MP:SELECT.ELST", "P:VALL");
			createMappedChannels("MP:SELECT.TVST", "P:VALM");
			createMappedChannels("MP:SELECT.TTST", "P:VALN");
			createMappedChannels("MP:SELECT.FTST", "P:VALO");
			createMappedChannels("MP:SELECT.FFST", "P:VALP");
		} catch (Exception e) {
			logger.error("Error creating EPICS channels", e);
		}
	}

	private void createMappedChannels(String namePv, String valuePv) throws TimeoutException, CAException {
		var nameChannel = epicsController.createChannel(pvPrefix + namePv);
		var positionChannel = epicsController.createChannel(pvPrefix + valuePv);
		nameToPosition.put(nameChannel, positionChannel);
	}

	/**
	 * Returns the absolute position the given labelled position is mapped against.
	 */
	public double lookup(String labelledPosition) throws TimeoutException, CAException, InterruptedException {
		for (var entry : nameToPosition.entrySet()) {
			if (epicsController.cagetString(entry.getKey()).equals(labelledPosition)) {
				return epicsController.cagetDouble(entry.getValue());
			}
		}
		throw new NoSuchElementException("Could not lookup position '" + labelledPosition + "' from EPICS");
	}

}
