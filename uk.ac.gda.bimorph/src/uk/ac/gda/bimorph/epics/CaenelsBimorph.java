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

package uk.ac.gda.bimorph.epics;

import static java.lang.Math.abs;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatusException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.SHORT;
import gov.aps.jca.event.MonitorEvent;
import uk.ac.gda.bimorph.BimorphException;
import uk.ac.gda.bimorph.BimorphMirrorController;

/**
 * Bimorph Mirror Controller for control via Epics of Caenels Bimorph PSUs
 */
public class CaenelsBimorph extends DeviceBase implements BimorphMirrorController {

	private static final Logger logger = LoggerFactory.getLogger(CaenelsBimorph.class);
	private static final short BUSY_STATUS = 1;

	private EpicsChannelManager channelManager = new EpicsChannelManager();
	private EpicsController controller;

	/** The base PV of the controller - eg BL22I-OP-KBM-01 */
	private String basePV;
	/** The suffix to append to the base for this group - eg GROUP0 */
	private String groupSuffix;

	/** Index of first channel - channels are often 1 indexed */
	private int offset = 1;
	/** Time in seconds to wait when setting target values and triggering moves */
	private int putTimeoutMS = 5_000;

	private double maxDelta;
	private double maxVoltage;
	private double minVoltage;

	private Channel channelCount;
	private Channel status;
	private Channel target;

	/** Main busy PV for the whole PSU, not just this section/group */
	private Channel busy;

	/** The status of the group - true while voltages are being set */
	private volatile boolean groupBusy;
	/** The busy status of the overall psu - true while targets and voltages are being set */
	private volatile boolean psuBusy;

	private CaenelsBimorphChannel[] electrodes;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (basePV == null) {
			throw new IllegalStateException("basePv is required but not set");
		}
		if (groupSuffix == null) {
			throw new IllegalStateException("groupSuffix required but not set (eg GROUP0)");
		}
		controller = EpicsController.getInstance();
		try {
			createChannels();
		} catch (CAException | DeviceException e) {
			throw new FactoryException("Could not configure " + getName(), e);
		}
		channelManager.creationPhaseCompleted();
		setConfigured(true);
	}

	private void createChannels() throws CAException, DeviceException {
		String base = basePV + ":" + groupSuffix;
		channelCount = channelManager.createChannel(base + ":CHANNELS");
		status = channelManager.createChannel(base + ":STATUS", this::statusChanged);
		target = channelManager.createChannel(base + ":TARGET");

		busy = channelManager.createChannel(basePV + ":BUSY", this::busyChanged);
		electrodes = range(offset, offset + getNumberOfChannels())
				.mapToObj(i -> new CaenelsBimorphChannel(base, i))
				.toArray(CaenelsBimorphChannel[]::new);
	}

	public void setBasePV(String pv) {
		basePV = pv;
	}

	public void setOffset(int off) {
		offset = off;
	}

	/**
	 * Get the current voltage of a single channel
	 */
	@Override
	public double getVoltage(int channel) throws DeviceException {
		try {
			return electrodes[channel].getVoltage();
		} catch (BimorphException e) {
			throw new DeviceException(getName() + ": Could not read voltage from channel " + channel);
		}
	}

	/**
	 * Get voltages of all channels
	 */
	@Override
	public double[] getVoltages() throws DeviceException {
		try {
			return stream(electrodes)
					.mapToDouble(CaenelsBimorphChannel::getVoltage)
					.toArray();
		} catch (BimorphException be) {
			throw new DeviceException(getName() + ": Could not read voltages", be);
		}
	}

	/**
	 * Set the voltage of a single voltage - channels indexed according to offset
	 */
	@Override
	public void setVoltage(int channel, double voltage) throws DeviceException {
		channel = channel-offset;
		if (channel < 0 || channel >= getNumberOfChannels()-1) {
			throw new IllegalArgumentException(getName() + " channel " + (channel + offset) + " is not a valid channel");
		}
		try {
			if (channel != 0) checkDelta(getVoltage(channel - 1), voltage);
			if (channel != getNumberOfChannels() - 1) checkDelta(voltage, getVoltage(channel + 1));
			electrodes[channel].setTarget(voltage);
		} catch (BimorphException be) {
			throw new DeviceException(getName() + ": Could not set target voltage for channel " + channel, be);
		}
		// If target voltages have been set in EPICS but the PSU has not been set to change voltages,
		// this may move more than one channel.
		triggerMove();
	}

	/**
	 * Set the voltages of all voltages
	 */
	@Override
	public void setVoltages(double... voltages) throws DeviceException {
		checkVoltages(voltages);
		try {
			range(0, getNumberOfChannels())
					.forEach(i -> electrodes[i].setTarget(voltages[i]));
		} catch (BimorphException be) {
			throw new DeviceException(getName() + ": Could not set target voltages", be);
		}
		triggerMove();
	}

	/**
	 * Get the number of channels controlled by this power supply
	 */
	@Override
	public int getNumberOfChannels() throws DeviceException {
		try {
			return controller.cagetInt(channelCount);
		} catch (TimeoutException | CAException | InterruptedException e) {
			throw new DeviceException(getName() + ": Could not get number of channels", e);
		}
	}

	/**
	 * Start all channels moving towards their target positions
	 * @throws DeviceException
	 */
	private void triggerMove() throws DeviceException {
		logger.debug("Setting voltages for channels");
		try {
			controller.caput(target, 0);
			waitForBusy(true, putTimeoutMS);
		} catch (CAException e) {
			throw new DeviceException(getName(), ": Could not trigger channel voltage update", e);
		} catch (InterruptedException e) {
			logger.error("Interrupted while triggering move", e);
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public boolean isBusy() {
		return psuBusy || groupBusy;
	}

	private void checkVoltages(double[] voltages) throws DeviceException {
		if (voltages.length != getNumberOfChannels()) {
			throw new IllegalArgumentException(getName() + ": Incorrect number of voltages given (" + voltages.length + ")");
		}
		checkLimits(voltages);
		checkDeltas(voltages);
	}

	private void checkLimits(double... voltages) {
		stream(voltages).forEach(this::checkLimits);
	}

	/** Check that a given voltage is between the min and max voltages allowed */
	private void checkLimits(double voltage) {
		if (voltage < minVoltage || voltage > maxVoltage) {
			throw new IllegalArgumentException(getName() + ": Voltage " + voltage + " is outside valid range ("
					+ minVoltage + ", " + maxVoltage + ")");
		}
	}

	/** Checks that the difference betweem any consecutive pair of voltages is below {@link #getMaxDelta()} */
	private void checkDeltas(double...voltages) {
		double previous = 0; // for first voltage the delta is from 0
		for (double voltage : voltages) {
			checkDelta(previous, voltage);
			previous = voltage;
		}
		checkDelta(previous, 0); // for final voltage delta is back to 0
	}

	/** Check that the difference between two voltages is below maxDelta */
	private void checkDelta(double voltage1, double voltage2) {
		if (abs(voltage1 - voltage2) > maxDelta) {
			throw new IllegalArgumentException(getName() + ": Difference between consecutive voltages "
					+ voltage1 + " and " + voltage2 + " is greated than max delta (" + maxDelta + ")");
		}
	}

	private void statusChanged(MonitorEvent evt) {
		try {
			SHORT status = (SHORT)evt.getDBR().convert(DBRType.SHORT);
			groupBusy = status.getShortValue()[0] == BUSY_STATUS;
		} catch (CAStatusException e) {
			logger.error("{} - Unexpected DBR type from status monitor ({}={})", getName(), evt, evt.getDBR(), e);
		}
		logger.trace("{} - busy changed to {}", getName(), groupBusy);
	}

	private void busyChanged(MonitorEvent evt) {
		try {
			SHORT status = (SHORT)evt.getDBR().convert(DBRType.SHORT);
			psuBusy = status.getShortValue()[0] == BUSY_STATUS;
		} catch (CAStatusException e) {
			logger.error("{} - Unexpected DBR type from psu busy monitor ({}={})", getName(), evt, evt.getDBR(), e);
		}
		logger.trace("{} - busy changed to {}", getName(), psuBusy);
	}

	private void waitForBusy(boolean requiredBusyState, int timeoutMS) throws InterruptedException {
		logger.trace("{} - Waiting for {}", getName(), requiredBusyState);
		int interval = 200;
		int limit = timeoutMS / interval;
		while (isBusy() != requiredBusyState) {
			logger.trace("{} - Waiting for {}, currently group: {}, psu: {}", getName(), requiredBusyState, groupBusy, psuBusy);
			Thread.sleep(interval);
			if (limit-- <= 0) {
				logger.warn("{} - Group {} still not busy after {}ms", getName(), groupSuffix, timeoutMS);
				return;
			}
		}
	}

	public void refreshState() {
		try {
			groupBusy = controller.cagetShort(status) == BUSY_STATUS;
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error("Couldn't refresh group busy", e);
		}
		try {
			psuBusy = controller.cagetShort(busy) == BUSY_STATUS;
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error("Couldn't refresh PSU busy", e);
		}
	}

	public void setMaxDelta(double maxDelta) {
		this.maxDelta = maxDelta;
	}

	@Override
	public double getMaxDelta() {
		return maxDelta;
	}

	public void setMaxVoltage(double maxVoltage) {
		this.maxVoltage = maxVoltage;
	}

	@Override
	public double getMaxVoltage() {
		return maxVoltage;
	}

	public void setMinVoltage(double minVoltage) {
		this.minVoltage = minVoltage;
	}

	@Override
	public double getMinVoltage() {
		return minVoltage;
	}

	public void setPutTimeout(int putTimeout) {
		this.putTimeoutMS = putTimeout;
	}

	public String getGroupSuffix() {
		return groupSuffix;
	}

	public void setGroupSuffix(String groupSuffix) {
		this.groupSuffix = groupSuffix;
	}

	/**
	 * Controller for a single channel of a Caenels Bimorph PSU
	 */
	class CaenelsBimorphChannel {
		private static final double POSITION_DELTA = 1e-6;

		private final String basePv;
		private final int channelNumber;
		private final Channel targetChannel;
		private final Channel targetRBVChannel;
		private final Channel rbvChannel;
		private EpicsChannelManager channelManager;

		public CaenelsBimorphChannel(String pv, int i) {
			channelManager = new EpicsChannelManager();
			basePv = pv;
			channelNumber = i;
			targetChannel = createChannel(basePv + ":CH" + channelNumber + ":VTRGT");
			targetRBVChannel = createChannel(basePv + ":CH" + channelNumber + ":VTRGT_RBV");
			rbvChannel = createChannel(basePv + ":CH" + channelNumber + ":VOUT_RBV");
			channelManager.creationPhaseCompleted();
		}

		private Channel createChannel(String pv) {
			try {
				return channelManager.createChannel(pv);
			} catch (CAException e) {
				throw new IllegalStateException("Could not create channel to " + pv);
			}
		}

		public void setTarget(double target) {
			logger.trace("Setting target of channel {} to {}", channelNumber, target);
			try {
				checkLimits(target);
				if (!needsToMoveFor(target)) {
					logger.debug("{} - Channel {} - not setting target: {}",
							getName(), channelNumber, target);
					return;
				}
				controller.caputWait(targetChannel, target, putTimeoutMS/1_000);
				waitForBusy(true, putTimeoutMS);
				waitForBusy(false, putTimeoutMS);
				// The Caenels PSU is a bit tempermental and inconsistent with busy states. In theory the busy/non-busy
				// states should be enough, but occasionally it goes through the cycle twice for a single channel
				logger.trace("{}:CH{} - finished waiting for busy, waiting 1500ms anyway", getName(), channelNumber);
				Thread.sleep(1500);
			} catch (CAException | TimeoutException e) {
				throw new BimorphException("Could not set target for channel " + channelNumber, e);
			} catch (InterruptedException e) {
				logger.error("Interrupted while setting target voltage for channel: {}", channelNumber);
			}
		}

		public double getVoltage() {
			try {
				double voltage = controller.cagetDouble(rbvChannel);
				logger.trace("Getting voltage of channel {}: {}", channelNumber, voltage);
				return voltage;
			} catch (Exception e) {
				throw new BimorphException("Could not get voltage for channel " + channelNumber, e);
			}
		}

		private boolean needsToMoveFor(double target){
			try {
				double currentVoltage = controller.cagetDouble(rbvChannel);
				double currentTargetRBV = controller.cagetDouble(targetRBVChannel);
				double currentTarget = controller.cagetDouble(targetChannel);
				if (abs(currentTarget - currentTargetRBV) > POSITION_DELTA) {
					logger.warn("{} - Channel {} is in inconsistent state. Target: {}, TargetRBV: {}",
							getName(), channelNumber, currentTarget, currentTargetRBV);
				}
				if (abs(currentTarget - target) < POSITION_DELTA) {
					if (abs(target - currentVoltage) < POSITION_DELTA) {
						// at correct voltage
						logger.debug("{} - Channel {} is already at target {}",
								getName(), channelNumber, target);
					} else {
						// will move to correct voltage
						logger.debug("{} - Channel {} is already set to move to target {}",
								getName(), channelNumber, target);
					}
					return false;
				}
				return true;
			} catch (TimeoutException | CAException | InterruptedException e) {
				throw new BimorphException("Could not check target position for channel "+ channelNumber, e);
			}
		}
	}
}
