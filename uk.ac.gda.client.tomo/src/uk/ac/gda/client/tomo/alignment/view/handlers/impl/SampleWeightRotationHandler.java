/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.handlers.ISampleWeightLookupTableHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.ISampleWeightRotationHandler;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.client.tomo.i12.ThetaServoSet;

/**
 * Sample weight rotation handler set for i12 - handles the setting of velocity, acceleration and SERVOSET attribute.
 */
public class SampleWeightRotationHandler implements ISampleWeightRotationHandler {

	private static final Logger logger = LoggerFactory.getLogger(SampleWeightRotationHandler.class);

	private static EpicsController EPICS_CONTROLLER;

	private ISampleWeightLookupTableHandler sampleWeightLookupTableHandler;

	private String velocityPV;

	private String acclPV;

	private String servoSetPV;

	protected Map<String, Channel> channelMap;

	public void setSampleWeightLookupTableHandler(ISampleWeightLookupTableHandler sampleWeightLookupTableHandler) {
		this.sampleWeightLookupTableHandler = sampleWeightLookupTableHandler;
	}

	public void setVelocityPV(String velocityPV) {
		this.velocityPV = velocityPV;
	}

	public void setAcclPV(String acclPV) {
		this.acclPV = acclPV;
	}

	public void setServoSetPV(String servoSetPV) {
		this.servoSetPV = servoSetPV;
	}

	public SampleWeightRotationHandler() {
		channelMap = new HashMap<String, Channel>();
		EPICS_CONTROLLER = EpicsController.getInstance();
	}

	@Override
	public void dispose() {
		logger.debug("Include dispose methods");
	}

	@Override
	public void handleSampleWeight(SAMPLE_WEIGHT sampleWeight) throws Exception {
		double bigStepVelocity = sampleWeightLookupTableHandler.getBigStepVelocity(sampleWeight);
		double bigStepAccl = sampleWeightLookupTableHandler.getBigStepAccl(sampleWeight);
		ThetaServoSet bigStepServoSet = sampleWeightLookupTableHandler.getBigStepServoSet(sampleWeight);

		EPICS_CONTROLLER.caput(createChannel(velocityPV), bigStepVelocity);
		EPICS_CONTROLLER.caput(createChannel(acclPV), bigStepAccl);
		EPICS_CONTROLLER.caput(createChannel(servoSetPV), bigStepServoSet.toString());
	}

	public Channel createChannel(String fullPvName) throws CAException, TimeoutException, InterruptedException {
		Channel channel;
		synchronized (channelMap) {
			channel = channelMap.get(fullPvName);
			if (channel == null) {
				logger.debug(String.format("creating channel for :%1$s", fullPvName));
				try {
					channel = EPICS_CONTROLLER.createChannel(fullPvName);
					int i = 0;
					while (Channel.CONNECTED != channel.getConnectionState()) {
						Thread.sleep(50);
						if (i > 10) {
							break;
						}
						i++;
					}
				} catch (CAException cae) {
					logger.warn("Problem creating channel", cae);
					throw cae;
				}
				if (Channel.CONNECTED == channel.getConnectionState()) {
					channelMap.put(fullPvName, channel);
				}
			}
		}
		return channel;
	}

}
