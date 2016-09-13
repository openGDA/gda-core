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

import static com.google.common.base.Preconditions.checkState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;

public class BeforeCheckScannable extends ScannableMotionUnitsBase {

	private static final Logger logger = LoggerFactory.getLogger(BeforeCheckScannable.class);

	protected Scannable delegate;
	protected Scannable beforeCheck;
	protected Object before;

	public BeforeCheckScannable(ScannableMotionUnits delegate,
			Scannable beforeCheck, Object before) {
		this.delegate = delegate;
		this.beforeCheck = beforeCheck;
		this.before = before;
	}

	/**
	 * @throws IllegalStateException if beforeCheck position != before
	 */
	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		logger.debug("checking {} at {} before moving {} to {}", beforeCheck.getName(), before, delegate.getName(), position);
		checkState(beforeCheck.isAt(before),
				"%s must be at %s before moving %s",
						beforeCheck.getName(), before, delegate.getName());

		logger.debug("moving {} to {}", delegate.getName(), position);
		delegate.moveTo(position);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return delegate.isBusy() || beforeCheck.isBusy();
	}

}
