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

package uk.ac.gda.server.ncd.epics;

import gda.device.DeviceException;
import gda.device.scannable.EpicsScannable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScaledAmplifiedMonitor extends EpicsScannable {
	private static final Logger logger = LoggerFactory.getLogger(ScaledAmplifiedMonitor.class);

	ScalingAndOffsetFromCurrAmp scalingAndOffset;
	private double upperThreshold = 0.3;
	private long settletime = 150;
	private boolean autoGain = true;
	
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

	public ScalingAndOffsetFromCurrAmp getScalingAndOffset() {
		return scalingAndOffset;
	}

	public void setScalingAndOffset(ScalingAndOffsetFromCurrAmp scalingAndOffset) {
		this.scalingAndOffset = scalingAndOffset;
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		throw new DeviceException("I do not move");
	}
	
	@Override
	public Object getPosition() throws DeviceException {
		double value;
		do {
			value = (Double) super.rawGetPosition();
			if (scalingAndOffset == null)
				return value;
			if (!autoGain)
				break;
			try {
				if (value > upperThreshold) {
					scalingAndOffset.decreaseAmplification();
					scalingAndOffset.waitWhileBusy();
				} else if (value < 0.1 * upperThreshold) {
					scalingAndOffset.increaseAmplification();
					scalingAndOffset.waitWhileBusy();
				} else 
					break;
				Thread.sleep(settletime);
			} catch (ScalingAndOffsetFromCurrAmp.OptionsExhausedException e) {
				// this is normal when no beam for overloaded
				break;
			} catch (ScalingAndOffsetFromCurrAmp.MoveProhibitedException e) {
				logger.debug("gain change currently probitited");
				break;
			} catch (DeviceException e) {
				logger.info("exception received trying to adjust gain", e);
				break;
			} catch (InterruptedException e) {
				throw new DeviceException("interrupted waiting for gain to be set", e);
			} 
		} while (true);
		return scalingAndOffset.getOffset() + value * scalingAndOffset.getScaling();
	}
}