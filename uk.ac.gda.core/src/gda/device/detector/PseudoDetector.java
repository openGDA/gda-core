/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Formerly a base class for detector objects written in Jython and instantiated within the Jython environment. This was
 * required when DeviceBase extended PyObject as there was a problem instantiating classes in Jython which were Java
 * classes extending PyObject. This problem has gone away now that DeviceBase does not extend PyObject anymore -so this
 * class is no longer required.
 *
 * @see gda.device.detector.DetectorBase
 * @deprecated Classes should extend {@link DetectorBase} or one of its subclasses.
 */
@Deprecated
public abstract class PseudoDetector extends DetectorBase {
	private static final Logger logger = LoggerFactory.getLogger(PseudoDetector.class);

	public PseudoDetector() {
		// Log the actual class name so we can see what the classes extending this are
		logger.warn("DAQ-3008 - '{}' extends PseudoDetector, which is deprecated and will be removed in GDA 9.19. Use DetectorBase instead.",
				this.getClass().getName());
	}
}
