/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.currentamplifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.EpicsScannable;

public class CalibratedAmplifier extends EpicsScannable {
	private static final Logger logger = LoggerFactory.getLogger(CalibratedAmplifier.class);

	GainWithScalingAndOffset scalingAndOffset;
	private double upperThreshold = 0.3;
	private long settletime = 150;
	private boolean autoGain = true;
	private boolean unidirectrional = true;


	public boolean isUnidirectrional() {
		return unidirectrional;
	}

	public void setUnidirectrional(boolean unidirectrional) {
		this.unidirectrional = unidirectrional;
	}

	public boolean isAutoGain() {
		return autoGain;
	}

	public void setAutoGain(boolean autoGain) {
		this.autoGain = autoGain;
	}

	public double getUpperThreshold() {
		return upperThreshold;
	}

	public void setUpperThreshold(double upperThreshold) {
		this.upperThreshold = upperThreshold;
	}

	public long getSettletime() {
		return settletime;
	}

	public void setSettletime(long settletime) {
		this.settletime = settletime;
	}

	public GainWithScalingAndOffset getScalingAndOffset() {
		return scalingAndOffset;
	}

	public void setScalingAndOffset(GainWithScalingAndOffset scalingAndOffset) {
		this.scalingAndOffset = scalingAndOffset;
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		throw new DeviceException(getName() + " - I do not move");
	}

	@Override
	public synchronized Object getPosition() throws DeviceException {
		double value;
		short lastStepDirection = 0;
		do {
			value = (Double) super.rawGetPosition();
			if (scalingAndOffset == null)
				return value;
			if (!autoGain)
				break;
			try {
				if (value > upperThreshold) {
					if (isUnidirectrional() && lastStepDirection == 1)
						break;
					scalingAndOffset.decreaseAmplification();
					scalingAndOffset.waitWhileBusy();
					lastStepDirection = -1;
				} else if (value < 0.085 * upperThreshold) {
					if (isUnidirectrional() && lastStepDirection == -1)
						break;
					scalingAndOffset.increaseAmplification();
					scalingAndOffset.waitWhileBusy();
					lastStepDirection = 1;
				} else
					break;
				Thread.sleep(settletime);
			} catch (GainWithScalingAndOffset.OptionsExhausedException e) {
				// this is normal when no beam for overloaded
				break;
			} catch (GainWithScalingAndOffset.MoveProhibitedException e) {
				logger.trace("{} - gain change currently probitited", getName());
				break;
			} catch (DeviceException e) {
				logger.error("{} - exception received trying to adjust gain", getName(), e);
				break;
			} catch (InterruptedException e) {
				throw new DeviceException("{} - interrupted waiting for gain to be set", getName(), e);
			}
		} while (true);
		return scalingAndOffset.getOffset() + value * scalingAndOffset.getScaling();
	}
}