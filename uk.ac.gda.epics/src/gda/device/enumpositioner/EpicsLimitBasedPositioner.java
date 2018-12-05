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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

	private String unknownValue = "UNKNOWN";

	private EpicsController controller;

	private List<String> preConfigurePositions;

	private List<Channel> limitChannels = new ArrayList<>();

	private List<String> limitChannelPvs;

	private Channel controlChannel;

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
		// check all limit pvs and return the position corresponding to the raised limit
		// returns the unknown value if none, or more than one, are set.
		try {
			int highIndex = -1;
			for (int i = 0; i < limitChannels.size(); i++) {
				Channel limitChannel = limitChannels.get(i);
				boolean high = controller.cagetInt(limitChannel) > 0;
				if (high) {
					if (0 <= highIndex) {
						// multiple limits are set - undetermined state
						return unknownValue;
					} else {
						highIndex = i;
					}
				}
			}
			return 0 <= highIndex ? getPosition(highIndex) : unknownValue;
		} catch (Exception e) {
			throw new DeviceException("Error getting position for '" + getName() + "'", e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		// the control PV can be "desynced" from the actual device state, e.g. if there's no air in a pneumatic system
		// move the control back to wherever the limits indicate the device actually is
		String position = getPosition();
		List<String> positions = getPositionsList();
		if (0 <= positions.indexOf(position)) {
			try {
				controller.caput(controlChannel, position);
			} catch (Exception e) {
				logger.error("Error putting to control pv in device '" + getName() + "' for a stop");
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
			limitChannels.stream().filter(Objects::nonNull).forEach(c -> controller.destroy(c));
			limitChannels.clear();

			controlChannel = controller.createChannel(controlChannelPv);
			String[] controlLabels = controller.cagetLabels(controlChannel);
			if (preConfigurePositions == null || preConfigurePositions.isEmpty()) {
				preConfigurePositions = Arrays.asList(controlLabels);
			} else if (controlLabels.length > preConfigurePositions.size()) {
				logger.warn(String.format("Using %d labels out of possible %d for device '%s' - extra labels will be hidden",
						preConfigurePositions.size(), controlLabels.length, getName()));
			} else if (controlLabels.length < preConfigurePositions.size()) {
				throw new IllegalArgumentException("More positions specified than avaiable by controller");
			}

			if (preConfigurePositions.contains(unknownValue)) {
				throw new IllegalArgumentException("Unknown position value '" + unknownValue + "' conflicts with an actual positions");
			}

			if (limitChannelPvs.size() != preConfigurePositions.size()) {
				throw new IllegalArgumentException(String.format("%d limit channels set, but %d positions to configure",
						limitChannelPvs.size(), preConfigurePositions.size()));
			}

			for (String pv : limitChannelPvs) {
				limitChannels.add(controller.createChannel(pv));
			}
			addPositions(preConfigurePositions);

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

	public void setLimitPvs(Collection<String> pvs) {
		this.limitChannelPvs = new ArrayList<>(pvs);
	}

	public List<String> getLimitPvs() {
		return new ArrayList<>(this.limitChannelPvs);
	}

	@Override
	public String[] getPositions() throws DeviceException {
		if (isConfigured()) {
			return super.getPositions();
		} else {
			return preConfigurePositions.toArray(new String[] {});
		}
	}

	public void setPositions(String[] positions) {
		// will require a reconfigure to take effect
		preConfigurePositions = Arrays.asList(positions);
	}

	public void setUnknownValue(String unknownValue) {
		this.unknownValue = unknownValue;
	}

	public String getUnknownValue() {
		return unknownValue;
	}
}
