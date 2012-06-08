/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.epics.interfaceSpec;

import gda.epics.PVProvider;

public class GDAEpicsInterfacePVProvider implements PVProvider {
	private String deviceName;
	private String fieldName;
	
	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	@Override
	public String getPV() throws Exception{
		Device device = GDAEpicsInterfaceReader.getDeviceFromType(null, deviceName);
		if( device != null){
			Field field = device.getField(fieldName);
			if(field != null){
				return field.getPV();
			}
		}
		throw new Exception("Unable to find pv for device "+ deviceName + " field " + fieldName);
	}

}
