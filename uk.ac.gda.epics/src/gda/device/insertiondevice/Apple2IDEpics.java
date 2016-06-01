/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.insertiondevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

public class Apple2IDEpics extends Apple2IDBase {

	private static final String TOP_OUTER_AXIS = "BLPUOMTR";
	private static final String TOP_INNER_AXIS = "BLPUIMTR";
	private static final String BOTTOM_OUTER_AXIS = "BLPLOMTR";
	private static final String BOTTOM_INNER_AXIS = "BLPLIMTR";
	private static final String ID_MODE = "MODESEL";
	private static final String ID_MOVE = "BLGSETP";
	private static final String ID_GAP = "BLGAPMTR";
	private static final String ID_ENABLED = "IDBLENA";
	private static final String PV_SEPARATOR = ":";
	private static final String VAL = "VAL";
	private static final String RBV = "RBV";

	private static final String ID_MOVE_START = "1";
	private static final String STATE_ENABLED = "ENABLED";

	private static final Logger logger = LoggerFactory.getLogger(Apple2IDEpics.class);

	private String basePVName;

	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private PutListener pcbl;

	// Axis demand channels
	private Channel topOuterValChannel;
	private Channel topInnerValChannel;
	private Channel bottomOuterValChannel;
	private Channel bottomInnerValChannel;
	private Channel gapValChannel;

	// Axis position channels
	private Channel topOuterRbvChannel;
	private Channel topInnerRbvChannel;
	private Channel bottomOuterRbvChannel;
	private Channel bottomInnerRbvChannel;
	private Channel gapRbvChannel;

	private Channel modeChannel;
	private Channel moveChannel;
	private Channel enabledChannel;

	// Delay (in ms) to allow motor position request values to propagate
	private int motorPosDelay = 250;

	public Apple2IDEpics() {
		super();
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(new InitializationListener() {
			@Override
			public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
				logger.info("{} initialisation completed.", getName());
			}
		});

		pcbl = new PutListener() {
			@Override
			public void putCompleted(PutEvent ev) {
				if (ev.getStatus() == CAStatus.NORMAL) {
					logger.debug("{}: move completed at {}", getName(), System.currentTimeMillis());
				} else {
					logger.error("Put failed. Channel {} : Status {}", ((Channel) ev.getSource()).getName(), ev.getStatus());
				}
				onMoveFinished();
			}
		};
	}

	// DeviceBase overrides

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}

		if (basePVName == null) {
			throw new FactoryException("base PV name is not specified");
		}

		try {
			topOuterValChannel = createAxisChannel(TOP_OUTER_AXIS, VAL);
			topInnerValChannel = createAxisChannel(TOP_INNER_AXIS, VAL);
			bottomOuterValChannel = createAxisChannel(BOTTOM_OUTER_AXIS, VAL);
			bottomInnerValChannel = createAxisChannel(BOTTOM_INNER_AXIS, VAL);
			gapValChannel = createAxisChannel(ID_GAP, VAL);

			topOuterRbvChannel = createAxisChannel(TOP_OUTER_AXIS, RBV);
			topInnerRbvChannel = createAxisChannel(TOP_INNER_AXIS, RBV);
			bottomOuterRbvChannel = createAxisChannel(BOTTOM_OUTER_AXIS, RBV);
			bottomInnerRbvChannel = createAxisChannel(BOTTOM_INNER_AXIS, RBV);
			gapRbvChannel = createAxisChannel(ID_GAP, RBV);

			modeChannel = channelManager.createChannel(createPVName(ID_MODE), false);
			moveChannel = channelManager.createChannel(createPVName(ID_MOVE), false);
			enabledChannel = channelManager.createChannel(createPVName(ID_ENABLED), false);

			channelManager.creationPhaseCompleted();
			channelManager.tryInitialize(100);
			setConfigured(true);
		} catch (Exception e) {
			final String message = String.format("%s: %s", getName(), "failed to create all channels");
			logger.error(message);
			throw new FactoryException(message, e);
		}
	}

	private Channel createAxisChannel(final String axis, final String suffix) throws CAException {
		final String pvName = createPVName(axis + "." + suffix);
		return channelManager.createChannel(pvName, false);
	}

	private String createPVName(final String suffix) {
		String result = basePVName;
		if (!result.endsWith(PV_SEPARATOR)) {
			result += PV_SEPARATOR;
		}
		result += suffix;
		return result;
	}

	// Abstract base class methods

	@Override
	protected double getMotorPosition(IDMotor motor) throws DeviceException {
		switch (motor) {
		case GAP:
			return getMotorPosition(gapRbvChannel);
		case TOP_OUTER:
			return getMotorPosition(topOuterRbvChannel);
		case TOP_INNER:
			return getMotorPosition(topInnerRbvChannel);
		case BOTTOM_OUTER:
			return getMotorPosition(bottomOuterRbvChannel);
		case BOTTOM_INNER:
			return getMotorPosition(bottomInnerRbvChannel);
		}
		throw new DeviceException("Invalid motor " + motor + " requested");
	}

	private double getMotorPosition(final Channel channel) throws DeviceException {
		try {
			return controller.cagetDouble(channel);
		} catch (Exception e) {
			final String message = "failed to get position from " + channel.getName();
			logger.error(message);
			throw new DeviceException(message, e);
		}
	}

	@Override
	protected void doMove(Apple2IDPosition position) throws DeviceException {
		try {
			// Set motor positions
			// A write to the process PV is necessary to initiate the move, but the process PV does not give a callback
			// when the move in completed. Instead, we must monitor one of the soft motors: it doesn't matter which one.
			setMotorPosition(topOuterValChannel, position.topOuterPos, "Top-Outer", false);
			setMotorPosition(topInnerValChannel, position.topInnerPos, "Top-Inner", false);
			setMotorPosition(bottomOuterValChannel, position.bottomOuterPos, "Bottom-Outer", false);
			setMotorPosition(bottomInnerValChannel, position.bottomInnerPos, "Bottom-Inner", true);

			// Set gap & do the move.
			controller.caput(gapValChannel, position.gap);
			Thread.sleep(motorPosDelay);
			controller.caput(moveChannel, ID_MOVE_START);
		} catch (Exception e) {
			final String message = String.format("failed to move ID phase to %.3f : %.3f : %.3f : %.3f", position.topOuterPos, position.topInnerPos,
					position.bottomOuterPos, position.bottomInnerPos);
			logger.error(message);
			throw new DeviceException(message, e);
		}
	}

	private void setMotorPosition(final Channel channel, final double position, final String motorName, final boolean listen) throws DeviceException {
		try {
			if (listen) {
				controller.caput(channel, position, pcbl);
			} else {
				controller.caput(channel, position);
			}
		} catch (Exception e) {
			final String message = String.format("%s: failed to set %s motor position to %.3f", getName(), motorName, position);
			logger.error(message);
			throw new DeviceException(message, e);
		}
	}

	@Override
	public boolean isEnabled() throws DeviceException {
		try {
			final String enabledState = controller.cagetString(enabledChannel);
			return enabledState.equals(STATE_ENABLED);
		} catch (Exception e) {
			throw new DeviceException("failed to get ID enabled flag", e);
		}
	}

	@Override
	public String getIDMode() throws DeviceException {
		try {
			return controller.cagetString(modeChannel);
		} catch (Exception e) {
			throw new DeviceException("failed to get ID mode", e);
		}
	}

	// Class configuration

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public int getMotorPosDelay() {
		return motorPosDelay;
	}

	public void setMotorPosDelay(int motorPosDelay) {
		this.motorPosDelay = motorPosDelay;
	}
}
