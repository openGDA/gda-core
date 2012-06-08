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

/**
 * Formally a base class for Scannable objects written in Jython and instantiated within the Jython environment. This
 * was required when DeviceBase extended PyObject as there was a problem instantiating classes in Jython which were Java
 * classes extending PyObject. This problem has gone away now that DeviceBase does not extend PyObject anymore -so this
 * class is no longer required.
 * <p>
 * Classes should extend ScannableBase or one of the other abstract base classes for Scannables.
 * <p>
 * @see gda.device.scannable.ScannableMotionBase
 */
@Deprecated
public abstract class PseudoDevice extends ScannableMotionBase {

}
