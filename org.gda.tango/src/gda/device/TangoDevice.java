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
	public String get_name();
	public DeviceData command_inout(String cmd) throws DevFailed;
	public DeviceData command_inout(String cmd, DeviceData argin) throws DevFailed;
	public DevState state() throws DevFailed;
	public String status() throws DevFailed;
	public void write_attribute(DeviceAttribute devattr) throws DevFailed;
	public DeviceAttribute read_attribute(String attributeName) throws DevFailed;
	public String[] get_attribute_list() throws DevFailed;
	public AttributeInfo get_attribute_info(String attributeName) throws DevFailed;
	public boolean use_db();
	public DbDatum get_property(String propertyName) throws DevFailed;
	public void put_property(DbDatum property) throws DevFailed;
	public void set_timeout_millis(int millis) throws DevFailed;
}