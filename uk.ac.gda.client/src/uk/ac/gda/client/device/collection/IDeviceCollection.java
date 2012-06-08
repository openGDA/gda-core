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

package uk.ac.gda.client.device.collection;

import gda.factory.Findable;

/**
 * This interface represents a collection of devices such as
 * scannables, motors,etc. A device collection typically represents devices
 * that are logically grouped to provide specific functionality, for 
 * example, a FluorescenceDeviceCollection.
 * <p>
 * An example implementation can be found in uk.ac.gda.example plug-in
 * <p>
 */
public interface IDeviceCollection extends Findable {

}
