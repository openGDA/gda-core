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

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gov.aps.jca.Channel;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * Control devices using the Epics Valve/Shutter template.
 * <P>
 * This class operates two Epics records: a record which controls the device and a record which holds the status. The
 * positions are: "Open", "Close" and "Reset". There are 5 values for the status: "Open", "Opening", "Closed", "Closing"
 * and "Fault".
 * <P>
 * The stop method in this class does nothing as the valves operate too fast for such a method to be meaningful.
 */
public class EpicsValve extends ValveBase implements EnumPositioner, MonitorListener, Scannable {

	private static final Logger logger = LoggerFactory.getLogger(EpicsValve.class);

	private String templateName;

	protected EpicsController controller;

	protected Channel currentPositionChnl;

	protected Channel currentStatusChnl;

	protected EpicsChannelManager channelManager;

	/**
	 * Constructor.
	 */
	public EpicsValve() {
		super();
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager();
	}

	@Override
	public void configure() {
		if (!configured) {
			try {

				// remove any final :
				if (templateName.endsWith(":")) {
					templateName = templateName.substring(0, templateName.lastIndexOf(":"));
				}

				// create channel
				currentPositionChnl = controller.createChannel(templateName + ":CON.VAL");
				currentStatusChnl = controller.createChannel(templateName + ":STA.VAL");

				// create required channel monitors
				controller.setMonitor(currentStatusChnl, this, EpicsController.MonitorType.STS);

				// loop over the pv's in the record
				for (int i = 0; i < 3; i++) {
					Channel thisStringChannel = controller.createChannel(templateName + ":CON." + EpicsEnumConstants.CHANNEL_NAMES[i]);
					String positionName = controller.cagetString(thisStringChannel);
					thisStringChannel.destroy();

					// if the string is not "" then save it to the array
					if (positionName.compareTo("") != 0) {
						super.positions.add(positionName);
					}
				}
			} catch (Exception e) {
				logger.error("Error while trying to configure: " + getName() + " : " + e.getMessage());
			}
			configured = true;
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		try {
			// check top ensure a correct string has been supplied
			if (positions.contains(position.toString())) {
				controller.caput(currentPositionChnl, position.toString(), channelManager);
				return;
			}
			// if get here then wrong position name supplied
			throw new DeviceException(getName() + ": demand position " + position.toString()
					+ " not acceptable. Should be one of: " + ArrayUtils.toString(positions));
		} catch (Throwable th) {
			throw new DeviceException("failed to move to" + position.toString(), th);
		}
	}

	@Override
	public String getPosition() throws DeviceException {
		try {
			// get the position
			int position = controller.cagetEnum(currentStatusChnl);

			// map the position to the equivalent string
			if (position == 1 || position == 4) {
				return OPEN;
			} else if (position == 3 || position == 2) {
				return CLOSE;
			} else {
				return "UNKNOWN"; // or throw an error
			}
		} catch (Throwable th) {
			throw new DeviceException("failed to get position", th);
		}
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return fetchEpicsStatus();
	}

	protected EnumPositionerStatus fetchEpicsStatus() throws DeviceException {
		try {
			// get the status
			int status = controller.cagetEnum(currentStatusChnl);

			// map the status to the EnumPositioner status's
			if (status == 1 || status == 3) {
				return EnumPositionerStatus.IDLE;
			} else if (status == 2 || status == 4) {
				return EnumPositionerStatus.MOVING;
			} else {
				return EnumPositionerStatus.ERROR;
			}
		} catch (Throwable th) {
			throw new DeviceException("failed to get status", th);
		}
	}

	@Override
	public void stop() throws DeviceException {
		// not meaningful to implement
	}

	/**
	 * Returns the name of the Epics valve/shutter template this object is using
	 *
	 * @return the name of the Epics valve/shutter template
	 */
	public String getEpicsRecordName() {
		return templateName;
	}

	/**
	 * Sets the name of the Epics valve/shutter template to use.
	 *
	 * @param recordName
	 */
	public void setEpicsRecordName(String recordName) {
		this.templateName = recordName;
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		try {
			// no matter what message is received, send observers latest status and position
			EnumPositionerStatus status = getStatus();
			notifyIObservers(this, status);

			if (status == EnumPositionerStatus.IDLE && currentPositionChnl != null && positions != null) {
				notifyIObservers(this, getPosition());
			}
		} catch (DeviceException e) {
			logger.debug(e.getClass() + " while updating EpicsPositioner " + getName() + " : " + e.getMessage());
		}
	}
}
