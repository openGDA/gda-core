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

package gda.device.timer;

import static gda.device.timer.Etfg.INVERSION;
import static java.util.Objects.requireNonNull;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.Timer;
import gda.device.enumpositioner.EnumPositionerBase;
import gda.device.timer.Tfg.AttributeChange;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(EnumPositioner.class)
/** EnumPositioner whose position is controlled by the state (high/low) of an output channel of a TFG2 */
public class TfgChannelEnum extends EnumPositionerBase implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(TfgChannelEnum.class);

	/** The Etfg instance controlling this device */
	private Etfg tfg;

	/** The output channel of the TFG controlling this positioner*/
	private int channel;

	/** The position name corresponding to the channel idling high */
	private String highName = "Open";
	/** The position name corresponding to the channel idling low */
	private String lowName = "Close";

	@Override
	public boolean isBusy() throws DeviceException {
		return tfg.getStatus() != Timer.IDLE;
	}

	@Override
	public String[] getPositions() throws DeviceException {
		return new String[] { highName, lowName };
	}

	@Override
	public List<String> getPositionsList() {
		return List.of(highName, lowName);
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		switch (tfg.getStatus()) {
		case Timer.IDLE:
			return EnumPositionerStatus.IDLE;
		case Timer.ACTIVE:
		case Timer.ARMED:
		case Timer.PAUSED:
			return EnumPositionerStatus.MOVING;
		default:
			// Unknown status -> return an error
			return EnumPositionerStatus.ERROR;
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		var pos = requireNonNull(position, "Position cannot be null").toString();
		if (pos.equalsIgnoreCase(highName)) {
			setIdle(true);
		} else if (pos.equalsIgnoreCase(lowName)) {
			setIdle(false);
		} else {
			throw new DeviceException(getName() + " - '" + pos + "' is not a valid position");
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		var inv = currentState();

		var idleHigh = ((inv >> channel) & 1) > 0;
		return idleHigh ? highName: lowName;
	}

	private void setIdle(boolean idleState) throws DeviceException {
		if (tfg.getStatus() != Timer.IDLE) {
			throw new DeviceException("Can't move fast shutter while TFG is running");
		}
		logger.debug("{} - Setting {} channel {} idle to {} ({})",
				getName(), tfg.getName(), channel, idleState ? "high" : "low", idleState ? highName : lowName);
		var inv = currentState();
		int req = idleState
				? inv | (1 << channel) // set the 'channel'th bit to 1
				: inv & ~(1 << channel); // set the 'channel'th bit to 0
		tfg.setAttribute(INVERSION, req);
		notifyIObservers(this, getStatus());
	}

	private Integer currentState() throws DeviceException {
		var invAtt = tfg.getAttribute(INVERSION);
		if (!(invAtt instanceof Integer)) {
			throw new DeviceException(getName() + " - Invalid value returned by inversion attribute: " + invAtt);
		}
		return (Integer) invAtt;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		tfg.addIObserver(this);
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof AttributeChange && INVERSION.equals(((AttributeChange)arg).name)) {
			try {
				notifyIObservers(this, getStatus());
			} catch (DeviceException e) {
				logger.error("{} - Error getting status - can't update observers", getName(), e);
			}
		}
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public String getHighName() {
		return highName;
	}

	public void setHighName(String highName) {
		this.highName = highName;
	}

	public String getLowName() {
		return lowName;
	}

	public void setLowName(String lowName) {
		this.lowName = lowName;
	}

	public Etfg getTfg() {
		return tfg;
	}

	public void setTfg(Etfg tfg) {
		this.tfg = tfg;
	}

}
