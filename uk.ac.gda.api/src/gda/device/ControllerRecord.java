/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device;

/**
 * A device which is associated with a controller record, e.g. an EPICS process variable.
 */
public interface ControllerRecord {

	/**
	 * Gets the name of the controller record, e.g. EPICS process variable name.
	 * For example, in NeXus this value can be written as the value of the
	 * {@code controller_record} field for the {@code NXpositioner} for the scannable.
	 *
	 * @return controller record name
	 * @see <a href="https://manual.nexusformat.org/classes/base_classes/NXpositioner.html#nxpositioner-controller-record-field">Nexus documentation for NXpositioner</a>
	 */
	public String getControllerRecordName();

}
