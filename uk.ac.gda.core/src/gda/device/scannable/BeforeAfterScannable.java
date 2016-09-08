/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.scannable;

import static com.google.common.base.Preconditions.checkArgument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;

public class BeforeAfterScannable extends ScannableMotionUnitsBase {

	private static final Logger logger = LoggerFactory.getLogger(BeforeAfterScannable.class);

	protected Scannable delegate;
	protected Scannable beforeAfter;
	protected Object before;
	protected Object after;

	public BeforeAfterScannable(ScannableMotionUnits delegate,
			Scannable beforeAfter, Object before, Object after) {
		this.delegate = delegate;
		this.beforeAfter = beforeAfter;
		this.before = before;
		this.after = after;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		logger.debug("moving {} to {} before", beforeAfter.getName(), before);
		beforeAfter.moveTo(before); // moveTo internally calls waitWhileBusy

		try {
			// in case beforeAfter.waitWhileBusy returns before finished moving
			Thread.sleep(delayBeforeMovingDelegate);

			logger.debug("moving {} to {}", delegate.getName(), position);
			delegate.moveTo(position);

			logger.debug("moving {} to {} after", beforeAfter.getName(), after);
			beforeAfter.moveTo(after);
		}
		catch (InterruptedException e) {
			// Restore the interrupted status
			Thread.currentThread().interrupt();

			// For compatibility with ScannableBase.moveTo:
			// convert to a device exception
			throw new DeviceException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return delegate.getPosition();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return delegate.isBusy() || beforeAfter.isBusy();
	}

	///////////////////////////////////////////////////////////////////////////

	private long delayBeforeMovingDelegate = 0;

	public long getDelayBeforeMovingDelegate() {
		return delayBeforeMovingDelegate;
	}

	/**
	 * Minimum time to wait between initial move of 'beforeAfter' and before moving 'delegate'.
	 */
	public void setDelayBeforeMovingDelegate(long milliseconds) {
		checkArgument(milliseconds >= 0, "milliseconds must be a positive integer");
		delayBeforeMovingDelegate = milliseconds;
	}

}
