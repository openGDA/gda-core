/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;

/**
 * Pneumatic control that checks limit switches to determine current position
 */
public class EpicsLimitBasedPositioner extends EnumPositionerBase {

	private static final Logger logger = LoggerFactory.getLogger(EpicsLimitBasedPositioner.class);

	private String outValue = null;

	private String inValue = null;

	private String unknownValue = "UNKNOWN";

	private EpicsController controller;

	private Channel inLimitChannel;

	private Channel outLimitChannel;

	private Channel controlChannel;

	private String outLimitChannelPv;

	private String inLimitChannelPv;

	private String controlChannelPv;

	private String lastDemandedPosition = null;

	public EpicsLimitBasedPositioner() {
		super();
		controller = EpicsController.getInstance();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		if (lastDemandedPosition != null && !lastDemandedPosition.equals(getPosition())) {
			return true;
		}
		return false;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		String positionString = position.toString();
		if (!containsPosition(positionString)) {
			throw new DeviceException(
					"Unrecognised position '" + positionString + "' for device " + getName());
		}
		try {
			// put with no callback due to VMXI-219
			controller.caput(controlChannel, positionString);
			lastDemandedPosition = positionString;
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public String getPosition() throws DeviceException {
		try {
			boolean inLimit = controller.cagetInt(inLimitChannel) > 0;
			boolean outLimit = controller.cagetInt(outLimitChannel) > 0;
			// exactly one of these limits should be high to be in a defined position
			if (inLimit == outLimit) return unknownValue;
			if (inLimit) return inValue;
			if (outLimit) return outValue;
			return unknownValue; // unreachable, but compiler complains
		} catch (Exception e) {
			throw new DeviceException("Error getting position for " + getName(), e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		// the control PV can be "desynced" from the actual device state, particularly if there is no air in the system
		// move the control back to wherever the limits indicate the device actually is
		String position = getPosition();
		if (position.equals(inValue) || position.equals(outValue)) {
			try {
				controller.caput(controlChannel, position);
			} catch (Exception e) {
				logger.error("Error putting to control pv in device " + getName(), e);
			}
		}
		lastDemandedPosition = null;
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		try {
			if (controlChannel != null) {
				controller.destroy(controlChannel);
				controlChannel = null;
			}
			if (outLimitChannel != null) {
				controller.destroy(outLimitChannel);
				outLimitChannel = null;
			}
			if (inLimitChannel != null) {
				controller.destroy(inLimitChannel);
				inLimitChannel = null;
			}
			controlChannel = controller.createChannel(controlChannelPv);
			outLimitChannel = controller.createChannel(outLimitChannelPv);
			inLimitChannel = controller.createChannel(inLimitChannelPv);

			String[] labels = controller.cagetLabels(controlChannel);
			if (labels.length != 2) {
				throw new FactoryException(String.format("Expected 2 labels from '%s', found %d", controlChannelPv, labels.length));
			}
			outValue = labels[0];
			inValue = labels[1];

			if (inValue.equals(unknownValue) || outValue.equals(unknownValue) || inValue.equals(outValue)) {
				throw new FactoryException(String.format("Positions must be unique - have %s, %s, %s", outValue, inValue, unknownValue));
			}
			addPosition(outValue);
			addPosition(inValue);
		} catch (Exception e) {
			throw new FactoryException("Failed to configure device " + getName(), e);
		}
		setConfigured(true);
	}

	@Override
	public void reconfigure() throws FactoryException {
		setConfigured(false);
		clearPositions();
		configure();
	}

	public void setControlChannelPv(String controlChannelPv) {
		this.controlChannelPv = controlChannelPv;
	}

	public String getControlChannelPv() {
		return controlChannelPv;
	}

	public void setInLimitChannelPv(String inLimitChannelPv) {
		this.inLimitChannelPv = inLimitChannelPv;
	}

	public String getInLimitChannelPv() {
		return inLimitChannelPv;
	}

	public void setOutLimitChannelPv(String outLimitChannelPv) {
		this.outLimitChannelPv = outLimitChannelPv;
	}

	public String getOutLimitChannelPv() {
		return outLimitChannelPv;
	}

	public String getInValue() {
		return inValue;
	}

	public String getOutValue() {
		return outValue;
	}

	public void setUnknownValue(String unknownValue) {
		this.unknownValue = unknownValue;
	}

	public String getUnknownValue() {
		return unknownValue;
	}
}
