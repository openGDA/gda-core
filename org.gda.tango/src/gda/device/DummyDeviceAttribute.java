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

package gda.device;

import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoDs.TangoConst;

public class DummyDeviceAttribute {
	private String attributeName;
	private String attributeValue;
	private int attributeDataType;
	private DeviceAttribute attribute = null;
	
	public DummyDeviceAttribute() {
	}
	
	public DummyDeviceAttribute(DeviceAttribute attribute) {
		this.attribute = attribute;
	}

	public DummyDeviceAttribute(String attributeName, String attributeValue, int attributeDataType) {
		super();
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
		this.attributeDataType = attributeDataType;
		getDeviceAttribute();
	}

	public DeviceAttribute getDeviceAttribute() {
		if (attribute == null) {
			if (attributeDataType == TangoConst.Tango_DEV_SHORT) {
				attribute = new DeviceAttribute(attributeName, Short.parseShort(attributeValue));
			} else if (attributeDataType == TangoConst.Tango_DEV_LONG) {
				attribute = new DeviceAttribute(attributeName, Integer.parseInt(attributeValue));
			} else if (attributeDataType == TangoConst.Tango_DEV_LONG64) {
				attribute = new DeviceAttribute(attributeName, Long.parseLong(attributeValue));
			} else if (attributeDataType == TangoConst.Tango_DEV_DOUBLE) {
				attribute = new DeviceAttribute(attributeName, Double.parseDouble(attributeValue));
			} else if (attributeDataType == TangoConst.Tango_DEV_FLOAT) {
				attribute = new DeviceAttribute(attributeName, Float.parseFloat(attributeValue));
			} else if (attributeDataType == TangoConst.Tango_DEV_USHORT) {
				attribute = new DeviceAttribute(attributeName, Short.parseShort(attributeValue));
			} else if (attributeDataType == TangoConst.Tango_DEV_ULONG) {
				attribute = new DeviceAttribute(attributeName, Integer.parseInt(attributeValue));
				attribute.insert_ul(Integer.parseInt(attributeValue));  // This works, don't know why! Tango_DEV_ULONG is 7 but insert_ul puts 8 as the attributeDataType
			} else if (attributeDataType == TangoConst.Tango_DEV_BOOLEAN) {
				attribute = new DeviceAttribute(attributeName, Boolean.parseBoolean(attributeValue));
			} else {
				attribute = new DeviceAttribute(attributeName, attributeValue);
			}
		}
		return attribute;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getattributeValue() {
		return attributeValue;
	}

	public void setattributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	public int getAttributeDataType() {
		return attributeDataType;
	}

	public void setAttributeDataType(int attributeDataType) {
		this.attributeDataType = attributeDataType;
	}

	String asSimpleString(){
		return "DummyDeviceAttribute [attributeName=" + attributeName + ", attributeValue=" + attributeValue
				+ ", attributeDataType=" + attributeDataType + ", attribute=" + attribute + "]";
	}
	@Override
	public String toString() {
		getDeviceAttribute();
		if(attribute ==null)
			return asSimpleString();
		
		try{
			String name = attribute.getName();
			int type = attribute.getType();
			String suffix=null;
			switch( type){
			case TangoConst.Tango_DEV_SHORT:
				suffix =  Short.toString(attribute.extractShort());
				break;
			case TangoConst.Tango_DEV_LONG:
				suffix =  Long.toString(attribute.extractLong());
				break;
			case TangoConst.Tango_DEV_ULONG:
				suffix =  Long.toString(attribute.extractLong());
				break;
			case TangoConst.Tango_DEV_STRING:
				suffix =  attribute.extractString();
				break;
			default:
				suffix = "type:[" + Integer.toString(type) + "]";
			}
			return super.toString() + "name:[" + name + "[ val:[" + suffix +"]";
		}
		catch (Exception e) {
			//do nothing
		}
		return asSimpleString();
	}

	
}
