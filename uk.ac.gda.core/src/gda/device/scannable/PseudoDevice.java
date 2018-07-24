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

package gda.device.scannable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Formally a base class for Scannable objects written in Jython and instantiated within the Jython environment. This
 * was required when DeviceBase extended PyObject as there was a problem instantiating classes in Jython which were Java
 * classes extending PyObject. This problem has gone away now that DeviceBase does not extend PyObject anymore -so this
 * class is no longer required.
 *
 * @see gda.device.scannable.ScannableMotionBase
 * @deprecated Classes should extend {@link ScannableMotionBase} or one of the other base classes for Scannables.
 */
@Deprecated
public abstract class PseudoDevice extends ScannableMotionBase {
	private static final Logger logger = LoggerFactory.getLogger(PseudoDevice.class);

	public PseudoDevice() {
		// Log the actual class name so we can see what the classes extending this are
		logger.debug("DAQ-1579 - Instantiated a class '{}' extending the deprecated PseudoDevice",
				this.getClass().getName());
	}

}
