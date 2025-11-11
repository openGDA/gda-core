/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.AttributeInfo;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;


public interface TangoDevice {
	String get_name();
	DeviceData command_inout(String cmd) throws DevFailed;
	DeviceData command_inout(String cmd, DeviceData argin) throws DevFailed;
	DevState state() throws DevFailed;
	String status() throws DevFailed;
	void write_attribute(DeviceAttribute devattr) throws DevFailed;
	DeviceAttribute read_attribute(String attributeName) throws DevFailed;
	String[] get_attribute_list() throws DevFailed;
	AttributeInfo get_attribute_info(String attributeName) throws DevFailed;
	boolean use_db();
	DbDatum get_property(String propertyName) throws DevFailed;
	void put_property(DbDatum property) throws DevFailed;
	void set_timeout_millis(int millis) throws DevFailed;
}