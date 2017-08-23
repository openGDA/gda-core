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

package gda.device.enumpositioner;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

public class EpicsAirBearingControl extends EnumPositionerBase implements EnumPositioner, InitializationListener {

	private static final Logger logger=LoggerFactory.getLogger(EpicsAirBearingControl.class);
	private String setPV;
	private String readPV;
	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private Channel setChannel;
	private Channel readChannel;
	private ReadPvMonitorListener readPvMonitor;
	private PutCallbackListener pcbl;
	private boolean initialised = false;
	private String targetPosition;
	private Vector<String> readpositions=new Vector<String>();
	private String currentPosition="";

	public EpicsAirBearingControl() {
		super();
		channelManager = new EpicsChannelManager(this);
		controller = EpicsController.getInstance();
		readPvMonitor = new ReadPvMonitorListener();
		pcbl = new PutCallbackListener();
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (getSetPV() != null && getReadPV() !=null) {
				createChannelAccess(getSetPV(), getReadPV());
				channelManager.tryInitialize(100);
			}
			configured = true;
		}
	}
	@Override
	public Object rawGetPosition() throws DeviceException {
		try {
			short test = controller.cagetEnum(readChannel);
			//refresh currentPosition to ensure it is up to date. I21 something the monitor listener fails to update this - reason not known.
			currentPosition=positions.get(test);
			return currentPosition;
		} catch (Throwable th) {
			throw new DeviceException("failed to get position from " + readChannel.getName(), th);
		}
	}
	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		if (positions.contains(position.toString())) {
			targetPosition=position.toString();
			int target = positions.indexOf(targetPosition);
			try {
				positionerStatus = EnumPositionerStatus.MOVING;
				controller.caput(setChannel, target, pcbl);
			} catch (Throwable th) {
				positionerStatus = EnumPositionerStatus.ERROR;
				throw new DeviceException(readChannel.getName() + " failed to moveTo " + position.toString(), th);
			}
		} else {
			// if get here then wrong position name supplied
			throw new DeviceException("Position called: " + position.toString() + " not found.");
		}
	}
	@Override
	public boolean isBusy() throws DeviceException {
		return !targetPosition.equals(currentPosition);
	}
	@Override
	public String[] getPositions() throws DeviceException {
		try {
			return controller.cagetLabels(setChannel);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in getPositions",e);
		}
	}

	private String[] getReadPositions() throws DeviceException {
		try {
			return controller.cagetLabels(readChannel);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in getPositions",e);
		}
	}
	private void createChannelAccess(String setPV2, String readPV2) throws FactoryException {
		try {
			setChannel = channelManager.createChannel(setPV2, false);
			readChannel=channelManager.createChannel(readPV2, readPvMonitor, false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();

		} catch (Throwable th) {
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		String[] position = getPositions();
		for (int i = 0; i < position.length; i++) {
			if (position[i] != null || position[i] != "") {
				super.positions.add(position[i]);
				logger.info("{} has available position: {}", getName(), position[i]);
			}
		}
		currentPosition=(String)rawGetPosition();
		logger.info("{} is initialised. Number of positions: {} ", getName(), positions.size());
		String[] readposition = getReadPositions();
		for (int i = 0; i < readposition.length; i++) {
			if (readposition[i] != null || readposition[i] != "") {
				readpositions.add(position[i]);
				logger.info("{} has available read position: {}", getName(), position[i]);
			}
		}
		targetPosition=readpositions.get(controller.cagetEnum(setChannel));
		initialised = true;
		logger.info("{} is initialised. Number of read positions: {} ", getName(), readpositions.size());
	}
	/**
	 * InPos monitor listener
	 */

	private class ReadPvMonitorListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			if (initialised) {
				if (!readpositions.isEmpty()) {

					int value = -1;
					DBR dbr = arg0.getDBR();
					if (dbr.isENUM()) {
						value = ((DBR_Enum) dbr).getEnumValue()[0];
						currentPosition = readpositions.get(value);
						if (!currentPosition.isEmpty()) {
							positionerStatus = EnumPositionerStatus.IDLE;
							notifyIObservers(EpicsAirBearingControl.this, new ScannablePositionChangeEvent(currentPosition));
						}
						logger.info("{} is at {}", getName(), currentPosition);
					}
				}
			}
		}
	}

	private class PutCallbackListener implements PutListener {
		volatile PutEvent event = null;

		@Override
		public void putCompleted(PutEvent ev) {
			logger.debug("caputCallback complete for {}", getName());
			event = ev;

			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
						.getStatus());
				positionerStatus = EnumPositionerStatus.ERROR;
			} else {
				logger.info("{} move done", getName());
				positionerStatus = EnumPositionerStatus.IDLE;
			}
		}

	}

	public String getReadPV() {
		return readPV;
	}

	public void setReadPV(String readPV) {
		this.readPV = readPV;
	}

	public String getSetPV() {
		return setPV;
	}

	public void setSetPV(String setPV) {
		this.setPV = setPV;
	}

	public String getTargetPosition() {
		return targetPosition;
	}

	public String getCurrentPosition(){
		return currentPosition;
	}
}
