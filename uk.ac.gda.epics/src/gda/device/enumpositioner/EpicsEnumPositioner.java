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

package gda.device.enumpositioner;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.STSHandler;
import gda.epics.util.JCAUtils;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver class for Epics Positioners
 */
public class EpicsEnumPositioner extends EnumPositionerBase implements EnumPositioner, MonitorListener,
		ConnectionListener, Scannable {

	private static final Logger logger = LoggerFactory.getLogger(EpicsEnumPositioner.class);

	private String recordName;

	private EpicsController controller;

	private Channel currentPositionChnl;

	private Channel inPositionChnl;

	private Channel doneMovingChnl;

	private Channel statusChnl;

	private Channel stopChnl;

	private EpicsRecord epicsRecord;

	private String epicsRecordName;

	private HashSet<Channel> monitorInstalledSet;

	private EpicsChannelManager channelManager;

	/**
	 * Constructor.
	 */
	public EpicsEnumPositioner() {
		super();
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager();
	}

	@Override
	public void configure() throws FactoryException {

		if (!configured) {
			if ((epicsRecord = (EpicsRecord) Finder.getInstance().find(epicsRecordName)) != null) {
				monitorInstalledSet = new HashSet<Channel>();

				recordName = epicsRecord.getFullRecordName();

				if (recordName.endsWith("SELECT") || recordName.endsWith("SELECT:")) {
					recordName = recordName.substring(0, recordName.indexOf("SELECT"));
				}

				// remove any final :
				if (recordName.endsWith(":")) {
					recordName = recordName.substring(0, recordName.lastIndexOf(":"));
				}

				try {
					// create required channels asynchronously.
					currentPositionChnl = controller.createChannel(recordName + ":SELECT.VAL", this);
					inPositionChnl = controller.createChannel(recordName + ":INPOS.VAL", this);
					doneMovingChnl = controller.createChannel(recordName + ":DMOV.VAL", this);
					statusChnl = controller.createChannel(recordName + ":SELECT.STAT", this);

					// alarmSeverityChnl = controller.createChannel(templateName
					// + "SELECT.SEVR");

					stopChnl = controller.createChannel(recordName + ":STOP.VAL", this);
				} catch (Throwable th) {
					throw new FactoryException("failed to crate chanenl", th);
				}

				// get the list of positions from the SELECT record and fill the
				// positions attribute
				String[] channelNames = new String[12];
				channelNames[0] = "ZRST";
				channelNames[1] = "ONST";
				channelNames[2] = "TWST";
				channelNames[3] = "THST";
				channelNames[4] = "FRST";
				channelNames[5] = "FVST";
				channelNames[6] = "SXST";
				channelNames[7] = "SVST";
				channelNames[8] = "EIST";
				channelNames[9] = "NIST";
				channelNames[10] = "TEST";
				channelNames[11] = "ELST";

				// loop over the pv's in the record
				for (int i = 0; i < 12; i++) {
					try {
						Channel thisStringChannel = controller.createChannel(recordName + ":SELECT." + channelNames[i]);
						String positionName = controller.cagetString(thisStringChannel);
						controller.destroy(thisStringChannel);

						// if the string is not "" then save it to the array
						if (positionName.compareTo("") != 0) {
							super.positions.add(positionName);
						}
					} catch (Throwable th) {
						logger.error("failed to get position name for " + this.getName());
					}
				}

				// create required channel monitors
				// controller.setMonitor(doneMovingChnl, this);
				// controller.setMonitor(statusChnl, this);
			}// end of if(epicspv)
			configured = true;
		}// end of if(!configured)
	}

	/**
	 * Returns the name of the Epics Positioner template this object is using
	 * 
	 * @return the name of the Epics Positioner template
	 */
	public String getEpicsRecordName() {
		return epicsRecordName;
	}

	/**
	 * Sets the name of the Epics Positioner template to use.
	 * 
	 * @param recordName
	 */
	public void setEpicsRecordName(String recordName) {
		this.epicsRecordName = recordName;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// find in the positionNames array the index of the string
		if (positions.contains(position.toString())) {
			int target = positions.indexOf(position.toString());
			try {
				// update the VAL field of the SELECT record asynchronously
				controller.caput(currentPositionChnl, target, channelManager);
			} catch (Throwable th) {
				throw new DeviceException("failed to moveTo", th);
			}
			return;
		}

		// if get here then wrong position name supplied
		throw new DeviceException("Position called: " + position + " not found.");

	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		// no matter what message is received, send observers latest status
		try {
			if (currentPositionChnl != null && positions != null) {
				notifyIObservers(getPosition(), getStatus());
			} else {
				notifyIObservers(null, getStatus());
			}
		} catch (DeviceException e) {
			logger.debug(e.getClass() + " while updating EpicsPositioner " + getName() + " : " + e.getMessage());
		}

	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		try {
			// first check if its moving
			if (controller.cagetDouble(doneMovingChnl) == 0.0) {
				return EnumPositionerStatus.MOVING;
			}
			// check if in position
			if (controller.cagetDouble(inPositionChnl) == 1.0) {
				// and status is NO_ALARM
				if (controller.cagetEnum(statusChnl) == 0) {
					return EnumPositionerStatus.IDLE;
				}
				logger.error("EpicsPositioner: " + getName() + " completed move but has error status.");
				notifyIObservers(this, EnumPositionerStatus.ERROR);
				return EnumPositionerStatus.ERROR;
			}
			// else its an error

			logger.error("EpicsPositioner: " + getName() + " failed to successfully move to required location.");
			return EnumPositionerStatus.ERROR;

		} catch (Exception e) {
			throw new DeviceException(e.getClass() + " while updating EpicsPositioner " + getName() + " : "
					+ e.getMessage());
		}

	}

	@Override
	public void stop() throws DeviceException {
		try {
			controller.caput(stopChnl, 1);
		} catch (Throwable th) {
			throw new DeviceException("failed to stop", th);
		}
	}

	@Override
	public String getPosition() throws DeviceException {
		try {
			// int position = controller.cagetInt(currentPositionChnl);
			short test = controller.cagetEnum(currentPositionChnl);
			return positions.get(test);
		} catch (Throwable th) {
			throw new DeviceException("failed to get position", th);
		}
	}

	@Override
	public void connectionChanged(ConnectionEvent ev) {
		onConnectionChanged(ev);
	}

	/**
	 * Connection callback
	 * 
	 * @param ev
	 */
	private void onConnectionChanged(ConnectionEvent ev) {
		Channel ch = (Channel) ev.getSource();
		boolean installMonitor = false;

		if (ev.isConnected()) {
			synchronized (monitorInstalledSet) {
				installMonitor = !monitorInstalledSet.contains(ch);
			}
		}

		// start a monitor on the first connection
		if (installMonitor) {
			try {
				// Print some information
				logger.info(JCAUtils.timeStamp() + " Search successful for: " + ch.getName());
				// ch.printInfo();
				// Add a monitor listener on every successful connection
				// The following is commented out to solve scan pyException
				// problem
				// - need to track down the real cause.
				controller.setMonitor(ch, STSHandler.getSTSType(ch), Monitor.VALUE | Monitor.ALARM, this);

				synchronized (monitorInstalledSet) {
					monitorInstalledSet.add(ch);
				}

			} catch (Throwable ex) {
				logger.error("Add Monitor failed for Channel: " + ch.getName() + " : " + ex);
				return;
			}
		}

		// print connection state
		logger.info(JCAUtils.timeStamp() + " ");
		if (ch.getConnectionState() == Channel.CONNECTED) {
			logger.info(ch.getName() + " is connected");
		} else if (ch.getConnectionState() == Channel.DISCONNECTED) {
			logger.info(ch.getName() + " is disconnected");
		} else if (ch.getConnectionState() == Channel.CLOSED) {
			logger.info(ch.getName() + " is closed");
		}
	}
}
