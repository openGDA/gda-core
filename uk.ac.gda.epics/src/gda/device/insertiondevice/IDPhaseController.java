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

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.IInsertionDevicePhaseControl;
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

public class IDPhaseController extends DeviceBase implements InitializationListener, IInsertionDevicePhaseControl {
	private final static Logger logger = LoggerFactory.getLogger(IDPhaseController.class);
	private static final double CIRCULAR_LEFT_TOP_OUTER_MOTOR_POSITION = -15.0;
	private static final double CIRCULAR_LEFT_BOTTOM_INNER_MOTOR_POSITION = -15.0;
	private static final double CIRCULAR_RIGHT_TOP_OUTER_MOTOR_POSITION = 15.0;
	private static final double CIRCULAR_RIGHT_BOTTOM_INNER_MOTOR_POSITION = 15.0;
	private String basePVName;
	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private PutCallbackListener pcbl;
	public volatile boolean busy;
	private Channel modeChannel;
	private Channel topOuterChannel;
	private Channel topInnerChannel;
	private Channel bottomOuterChannel;
	private Channel bottomInnerChannel;
	private Channel moveChannel;

	public IDPhaseController() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		pcbl = new PutCallbackListener();
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (getBasePVName()!=null) {
				createChannelAccess(getBasePVName());
				channelManager.tryInitialize(100);
				setConfigured(true);
			} else {
				logger.error("Root part of PV name is not specified for {}.", getName());
				throw new FactoryException("Root part of PV name is not specified for " + getName());
			}
		}
	}

	private void createChannelAccess(String pvRootName) throws FactoryException {
		try {
			topOuterChannel = channelManager.createChannel(pvRootName + PV_SEPARATOR + TOP_OUTER_AXIS + ".VAL", false);
			topInnerChannel = channelManager.createChannel(pvRootName + PV_SEPARATOR + TOP_INNER_AXIS + ".VAL", false);
			bottomOuterChannel = channelManager.createChannel(pvRootName + PV_SEPARATOR + BOTTOM_OUTER_AXIS + ".VAL", false);
			bottomInnerChannel = channelManager.createChannel(pvRootName + PV_SEPARATOR + BOTTOM_INNER_AXIS + ".VAL", false);
		modeChannel = channelManager.createChannel(pvRootName+PV_SEPARATOR+ID_MODE, false);
		moveChannel = channelManager.createChannel(pvRootName+PV_SEPARATOR+ID_MOVE, false);

		// acknowledge that creation phase is completed
		channelManager.creationPhaseCompleted();
		} catch(CAException e) {
			throw new FactoryException("failed to create all channels for "+getName(), e);
		}
	}

	@Override
	public void hortizontal() throws DeviceException {
		try {
			controller.caput(modeChannel, GAP_AND_PHASE_MODE);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set ID mode to {}",getName(),GAP_AND_PHASE_MODE, e);
			throw new DeviceException(getName()+" Failed to set ID mode to "+ GAP_AND_PHASE_MODE, e);
		}
		try {
			controller.caput(topOuterChannel, LINEAR_HORIZONTAL_TOP_OUTER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Top-Outer motor position to {}",getName(),LINEAR_HORIZONTAL_TOP_OUTER_MOTOR_POSITION, e);
			throw new DeviceException(getName()+" Failed to set Top-Outer motor position to "+ LINEAR_HORIZONTAL_TOP_OUTER_MOTOR_POSITION, e);
		}
		try {
			controller.caput(topInnerChannel, LINEAR_HORIZONTAL_TOP_INNER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Top-Inner motor position to {}",getName(),LINEAR_HORIZONTAL_TOP_INNER_MOTOR_POSITION, e);
			throw new DeviceException(getName()+" Failed to set Top-Inner motor position to "+ LINEAR_HORIZONTAL_TOP_INNER_MOTOR_POSITION, e);
		}
		try {
			controller.caput(bottomOuterChannel, LINEAR_HORIZONTAL_BOTTOM_OUTER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Bottom-Outer motor position to {}",getName(),LINEAR_HORIZONTAL_BOTTOM_OUTER_MOTOR_POSITION, e);
			throw new DeviceException(getName()+" Failed to set Bottom-Outer motor position to "+ LINEAR_HORIZONTAL_BOTTOM_OUTER_MOTOR_POSITION, e);
		}
		try {
			controller.caput(bottomInnerChannel, LINEAR_HORIZONTAL_BOTTOM_INNER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Bottom-Inner motor position to {}",getName(),LINEAR_HORIZONTAL_BOTTOM_INNER_MOTOR_POSITION, e);
			throw new DeviceException(getName()+" Failed to set Bottom-Inner motor position to "+ LINEAR_HORIZONTAL_BOTTOM_INNER_MOTOR_POSITION, e);
		}
	}

	@Override
	public void vertical() throws DeviceException {
		try {
			controller.caput(modeChannel, GAP_AND_PHASE_MODE);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set ID mode to {}",getName(),GAP_AND_PHASE_MODE, e);
			throw new DeviceException(getName()+" Failed to set ID mode to "+ GAP_AND_PHASE_MODE, e);
		}
		try {
			controller.caput(topOuterChannel, LINEAR_VERTICAL_TOP_OUTER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Top-Outer motor position to {}",getName(),LINEAR_VERTICAL_TOP_OUTER_MOTOR_POSITION, e);
			throw new DeviceException(getName()+" Failed to set Top-Outer motor position to "+ LINEAR_VERTICAL_TOP_OUTER_MOTOR_POSITION, e);
		}
		try {
			controller.caput(topInnerChannel, LINEAR_VERTICAL_TOP_INNER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Top-Inner motor position to {}",getName(),LINEAR_VERTICAL_TOP_INNER_MOTOR_POSITION, e);
			throw new DeviceException(getName()+" Failed to set Top-Inner motor position to "+ LINEAR_VERTICAL_TOP_INNER_MOTOR_POSITION, e);
		}
		try {
			controller.caput(bottomOuterChannel, LINEAR_VERTICAL_BOTTOM_OUTER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Bottom-Outer motor position to {}",getName(),LINEAR_VERTICAL_BOTTOM_OUTER_MOTOR_POSITION, e);
			throw new DeviceException(getName()+" Failed to set Bottom-Outer motor position to "+ LINEAR_VERTICAL_BOTTOM_OUTER_MOTOR_POSITION, e);
		}
		try {
			controller.caput(bottomInnerChannel, LINEAR_VERTICAL_BOTTOM_INNER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Bottom-Inner motor position to {}",getName(),LINEAR_VERTICAL_BOTTOM_INNER_MOTOR_POSITION, e);
			throw new DeviceException(getName()+" Failed to set Bottom-Inner motor position to "+ LINEAR_VERTICAL_BOTTOM_INNER_MOTOR_POSITION, e);
		}
	}

	public void circular_left() throws DeviceException {

		try {
			controller.caput(topOuterChannel, CIRCULAR_LEFT_TOP_OUTER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Top-Outer motor position to {}", getName(), CIRCULAR_LEFT_TOP_OUTER_MOTOR_POSITION, e);
			throw new DeviceException(getName() + " Failed to set Top-Outer motor position to " + CIRCULAR_LEFT_TOP_OUTER_MOTOR_POSITION, e);
		}

		try {
			controller.caput(bottomInnerChannel, CIRCULAR_LEFT_BOTTOM_INNER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Bottom-Inner motor position to {}", getName(), CIRCULAR_LEFT_BOTTOM_INNER_MOTOR_POSITION, e);
			throw new DeviceException(getName() + " Failed to set Bottom-Inner motor position to " + CIRCULAR_LEFT_BOTTOM_INNER_MOTOR_POSITION, e);
		}
	}

	public void circular_right() throws DeviceException {

		try {
			controller.caput(topOuterChannel, CIRCULAR_RIGHT_TOP_OUTER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Top-Outer motor position to {}", getName(), CIRCULAR_RIGHT_TOP_OUTER_MOTOR_POSITION, e);
			throw new DeviceException(getName() + " Failed to set Top-Outer motor position to " + CIRCULAR_RIGHT_TOP_OUTER_MOTOR_POSITION, e);
		}

		try {
			controller.caput(bottomInnerChannel, CIRCULAR_RIGHT_BOTTOM_INNER_MOTOR_POSITION);
		} catch (CAException | InterruptedException e) {
			logger.error("{}: Failed to set Bottom-Inner motor position to {}", getName(), CIRCULAR_RIGHT_BOTTOM_INNER_MOTOR_POSITION, e);
			throw new DeviceException(getName() + " Failed to set Bottom-Inner motor position to " + CIRCULAR_RIGHT_BOTTOM_INNER_MOTOR_POSITION, e);
		}
	}

	@Override
	public void moveToPhase(double phaseInDegree) throws DeviceException {
		if (phaseInDegree==0.0) {
			hortizontal();
		} else if (phaseInDegree==90.0) {
			vertical();
		} else {
			//TODO other phases require a lookup table to interpret phase value to motors' positions.
			throw new NotImplementedException("Apart from Linear Horizontal (0 degree) and Linear Vertical (90 degree), otehr phases are not implemented yet.");
		}
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		logger.info("{} initialisation completed.",getName());
	}

	private class PutCallbackListener implements PutListener {
		volatile PutEvent event = null;

		@Override
		public void putCompleted(PutEvent ev) {
			event = ev;

			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(),	event.getStatus());
			} else {
				// notifyIObservers(this, jobDone);
				logger.debug("{}: move completed at {}", getName(), System.currentTimeMillis());
			}
			busy = false;
		}
	}

	@Override
	public boolean isBusy() {
		return busy;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}
}
