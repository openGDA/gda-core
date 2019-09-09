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

import static gda.device.enumpositioner.ValvePosition.CLOSE;
import static gda.device.enumpositioner.ValvePosition.OPEN;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Control devices using the Epics Valve/Shutter template.
 * <P>
 * This class operates two Epics records: a record which controls the device and a record which holds the status. The
 * positions are: "Open", "Close" and "Reset". There are 5 values for the status: "Open", "Opening", "Closed", "Closing"
 * and "Fault".
 * <P>
 * The stop method in this class does nothing as the valves operate too fast for such a method to be meaningful.
 */
@ServiceInterface(EnumPositioner.class)
public class EpicsValve extends EnumPositionerBase implements MonitorListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsValve.class);

	private String templateName;

	protected EpicsController controller;

	protected Channel currentPositionChnl;

	protected Channel currentStatusChnl;

	protected EpicsChannelManager channelManager;

	//Valve status enum - same order as in Epics
	private enum ValveStatus { FAULT, OPEN, OPENING, CLOSED, CLOSING }
	private boolean checkDemandInStatus;
	private String lastDemandPosition = "";

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
		if (!isConfigured()) {
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
						addPosition(positionName);
					}
				}
			} catch (Exception e) {
				logger.error("Error while trying to configure: " + getName() + " : " + e.getMessage());
			}
			setConfigured(true);
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		try {
			lastDemandPosition = "";
			// check top ensure a correct string has been supplied
			if (containsPosition(position.toString())) {
				controller.caput(currentPositionChnl, position.toString(), channelManager);
				lastDemandPosition = position.toString();
				return;
			}
			// if get here then wrong position name supplied
			throw new DeviceException(getName() + ": demand position " + position.toString()
					+ " not acceptable. Should be one of: " + ArrayUtils.toString(getPositionsList()));
		} catch (Exception e) {
			throw new DeviceException("failed to move to" + position.toString(), e);
		}
	}

	@Override
	public String getPosition() throws DeviceException {
		try {
			// get the position
			ValveStatus valveStatus = getValveStatus();
			switch(valveStatus) {
				case OPEN :
				case OPENING :
					return OPEN;
				case CLOSED :
				case CLOSING :
					return CLOSE;
				default :
					return "UNKNOWN";
			}

		} catch (Exception e) {
			throw new DeviceException("failed to get position", e);
		}
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return fetchEpicsStatus();
	}

	protected EnumPositionerStatus fetchEpicsStatus() throws DeviceException {
		try {
			ValveStatus valveStatus = getValveStatus();

			// Return 'moving' state if current position does not match the demand
			if (checkDemandInStatus &&
					((lastDemandPosition.equals(OPEN) && valveStatus != ValveStatus.OPEN) ||
					(lastDemandPosition.equals(CLOSE) && valveStatus != ValveStatus.CLOSED))) {
				return EnumPositionerStatus.MOVING;
			}

			// map the status to the EnumPositioner status's
			switch(valveStatus) {
				case OPEN :
				case CLOSED :
					return EnumPositionerStatus.IDLE;
				case OPENING :
				case CLOSING :
					return EnumPositionerStatus.MOVING;
				default :
					return EnumPositionerStatus.ERROR;
			}
		} catch (Exception e) {
			throw new DeviceException("failed to get status", e);
		}
	}

	// Get ValveStatus enum for current value of the valve status
	public ValveStatus getValveStatus() throws TimeoutException, CAException, InterruptedException {
		int status = controller.cagetEnum(currentStatusChnl);
		if (status < 0 || status > ValveStatus.values().length) {
			return ValveStatus.FAULT;
		}
		else {
			return ValveStatus.values()[status];
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

			if (status == EnumPositionerStatus.IDLE && currentPositionChnl != null && getNumberOfPositions() > 0) {
				notifyIObservers(this, getPosition());
			}
		} catch (DeviceException e) {
			logger.debug(e.getClass() + " while updating EpicsPositioner " + getName() + " : " + e.getMessage());
		}
	}

	public boolean isCheckDemandInStatus() {
		return checkDemandInStatus;
	}

	/**
	 * If set to 'true', the demand and current valve positions will be checked in {@link #getStatus()} -
	 * if the current position != demand position then state is set to {@link EnumPositionerStatus#MOVING}.
	 *
	 * @param checkDemandInStatus
	 */
	public void setCheckDemandInStatus(boolean checkDemandInStatus) {
		this.checkDemandInStatus = checkDemandInStatus;
	}
}
