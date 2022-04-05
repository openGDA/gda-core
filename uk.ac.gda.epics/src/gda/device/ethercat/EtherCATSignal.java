/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.device.ethercat;

import gda.device.DeviceException;
import gda.device.monitor.MonitorBase;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * Produces a scaled value from the raw, scale and offset signals
 * of a single channel of an EtherCAT module.
 */
public class EtherCATSignal extends MonitorBase {

	private String pvRaw;
	private String pvScale;
	private String pvOffset;

	private Channel rawChannel;
	private Channel scaleChannel;
	private Channel offsetchannel;

	public void setPvRaw(String pvRaw) {
		this.pvRaw = pvRaw;
	}

	public void setPvScale(String pvScale) {
		this.pvScale = pvScale;
	}

	public void setPvOffset(String pvOffset) {
		this.pvOffset = pvOffset;
	}

	@Override
	public Object getPosition() throws DeviceException {
		var controller = EpicsController.getInstance();
		try {
			var raw = controller.cagetDouble(getRawChannel());
			var scale = controller.cagetDouble(getScaleChannel());
			var offset = controller.cagetDouble(getOffsetChannel());
			return raw * scale + offset;
		} catch (TimeoutException | CAException e) {
			throw new DeviceException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException(e);
		}
	}

	private Channel getRawChannel() throws CAException, TimeoutException {
		if (rawChannel == null) {
			rawChannel = EpicsController.getInstance().createChannel(pvRaw);
		}
		return rawChannel;
	}

	private Channel getScaleChannel() throws CAException, TimeoutException {
		if (scaleChannel == null) {
			scaleChannel = EpicsController.getInstance().createChannel(pvScale);
		}
		return scaleChannel;
	}

	private Channel getOffsetChannel() throws CAException, TimeoutException {
		if (offsetchannel == null) {
			offsetchannel = EpicsController.getInstance().createChannel(pvOffset);
		}
		return offsetchannel;
	}

	/**
	 * Read-only!
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		throw new DeviceException("Cannot move read-only monitor '" + getName() + "'");
	}

}
