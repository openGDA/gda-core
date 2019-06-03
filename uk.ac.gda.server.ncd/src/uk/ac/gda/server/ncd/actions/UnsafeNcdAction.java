/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;

public abstract class UnsafeNcdAction extends BaseNcdAction {
	private static final Logger logger = LoggerFactory.getLogger(UnsafeNcdAction.class);

	private final boolean propagateErrors;

	public UnsafeNcdAction(boolean propagate) {
		propagateErrors = propagate;
	}

	@Override
	public void run() {
		try {
			runUnsafe();
		} catch (DeviceException de) {
			logger.error("Error running NcdAction: {}", getName(), de);
			if (propagateErrors) {
				throw new RuntimeException("Error running NcdAction: " + toString(), de);
			}
		}
	}

	public abstract void runUnsafe() throws DeviceException;
}
