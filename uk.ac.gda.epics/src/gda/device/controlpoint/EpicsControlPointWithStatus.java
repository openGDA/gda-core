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

package gda.device.controlpoint;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;

/**
 * Extension to the EpicsControlPoint class which uses a PV for the status of the control point.
 */
public class EpicsControlPointWithStatus extends EpicsControlPoint {

	String statusPVName;
	int busyValue = 1;
	private Channel theStatusChannel;

	public EpicsControlPointWithStatus() {
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();

		if (!statusPVName.isEmpty()) {
			try {
				theStatusChannel = channelManager.createChannel(statusPVName, false);
				channelManager.creationPhaseCompleted();
			} catch (CAException e) {
				throw new FactoryException("failed to create Channel for Control Point Status PV", e);
			}
		}
	}

	/**
	 * @return the value of the status pv
	 */
	public String getStatusPVName() {
		return statusPVName;
	}

	/**
	 * @return the value of the status pv meaning that the control point is operating
	 */
	public int getBusyValue() {
		return busyValue;
	}

	/**
	 * Sets the value of the status pv when the control point is operating.
	 * <p>
	 * Default is 1.
	 * 
	 * @param statusPVName
	 */
	public void setStatusPVName(String statusPVName) {
		this.statusPVName = statusPVName;
	}

	public void setBusyValue(int busyValue) {
		this.busyValue = busyValue;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		
		if (theStatusChannel ==null){
			return false;
		}
		
		try {
			Integer latestValue = controller.cagetInt(theStatusChannel);
			return latestValue == busyValue;
		} catch (Exception e) {
			throw new DeviceException("ControlPoint can not get " + theStatusChannel.getName(), e);
		}
	}

}
