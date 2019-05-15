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

	private Scannable delegate;
	private Scannable beforeAfter;
	private Object before;
	private Object after;

	public BeforeAfterScannable() {

	}

	public BeforeAfterScannable(ScannableMotionUnits delegate,
			Scannable beforeAfter, Object before, Object after) {
		this.setDelegate(delegate);
		this.setBeforeAfter(beforeAfter);
		this.setBefore(before);
		this.setAfter(after);
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		if (getBefore() != null) {
			logger.trace("moving {} to {} before", getBeforeAfter().getName(), getBefore());
			getBeforeAfter().moveTo(getBefore()); // moveTo internally calls waitWhileBusy
		}
		try {
			// in case beforeAfter.waitWhileBusy returns before finished moving
			Thread.sleep(delayBeforeMovingDelegate);

			logger.trace("moving {} to {}", getDelegate().getName(), position);
			getDelegate().moveTo(position);
			if (getAfter()!=null) {
				logger.trace("moving {} to {} after", getBeforeAfter().getName(), getAfter());
				getBeforeAfter().moveTo(getAfter());
			}
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
		return getDelegate().getPosition();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return getDelegate().isBusy() || getBeforeAfter().isBusy();
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

	public Scannable getDelegate() {
		return delegate;
	}

	public void setDelegate(Scannable delegate) {
		this.delegate = delegate;
	}

	public Scannable getBeforeAfter() {
		return beforeAfter;
	}

	public void setBeforeAfter(Scannable beforeAfter) {
		this.beforeAfter = beforeAfter;
	}

	public Object getBefore() {
		return before;
	}

	public void setBefore(Object before) {
		this.before = before;
	}

	public Object getAfter() {
		return after;
	}

	public void setAfter(Object after) {
		this.after = after;
	}

}
