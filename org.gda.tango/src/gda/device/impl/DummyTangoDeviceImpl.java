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

package gda.device.impl;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.AttributeInfo;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.TangoDs.TangoConst;
import gda.device.DummyDbDatum;
import gda.device.DummyDeviceAttribute;
import gda.device.TangoDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyTangoDeviceImpl implements TangoDevice {
	private static final Logger logger = LoggerFactory.getLogger(DummyTangoDeviceImpl.class);
	private String deviceName;
	private boolean usedb = false;
	private HashMap<String, DummyDeviceAttribute> attributeMap = new HashMap<String, DummyDeviceAttribute>();
	private HashMap<String, DummyDbDatum> propertyMap = new HashMap<String, DummyDbDatum>();
	private DevState state;
	private String status;
	private static boolean fail = false;
		
	public DummyTangoDeviceImpl(String deviceName) {
		super();
		this.deviceName = deviceName;
		usedb = (deviceName.contains("#dbase=no")) ? false : true;
		setState(DevState.ON);
	}

	@Override
	public String get_name() {
		return deviceName;
	}

	@Override
	public DeviceData command_inout(String cmd, DeviceData argin) throws DevFailed {
		if (fail) {
			fail = false;
			throw new DevFailed ("Command " + cmd + " failed", null);
		}
		if ("getImage".equals(cmd)) {
			int width = (int) read_attribute("image_width").extractULong();
			int height = (int) read_attribute("image_height").extractULong();
			DeviceData image = new DeviceData();
			image.insert(generateImage(width, height));
			return image;
		} else if ("ReadScalers".equals(cmd)) {
			int[] args = new int[6];
			args = argin.extractLongArray();
			int size = args[3] * args[4] * args[5];
			int[] rawData = new int[size];
			for (int i = 0; i< size; i++) {
				rawData[i] = (int) ((Math.random() + 1)*10);
			}
			DeviceData data = new DeviceData();
			data.insert(rawData);
			return data;
		}
		logger.info("Command {} executed", cmd);
		return null;
		
	}
	
	@Override
	public DeviceData command_inout(String cmd) throws DevFailed {
		if (fail) {
			fail = false;
			throw new DevFailed ("Command " + cmd + " failed", null);
		}
		logger.info("Command {} executed", cmd);
		return null;
	}
	
	@Override
	public DevState state() {
		return state;		
	}
	
	@Override
	public String status() {
		return status;
	}
	
	@Override
	public void write_attribute(DeviceAttribute attr) throws DevFailed {
		String key = attr.getName();
		if (attributeMap.containsKey(key)) {
			attributeMap.remove(key);
		}
		attributeMap.put(key, new DummyDeviceAttribute(attr));
		logger.debug("Attribute {} written", key);
	}
	
	@Override
	public DeviceAttribute read_attribute(String attributeName) {
		DeviceAttribute devAttr = null;
		if (attributeMap.containsKey(attributeName)) {
			DummyDeviceAttribute dummyDevAttr = attributeMap.get(attributeName);
			if (dummyDevAttr != null) {
				devAttr = dummyDevAttr.getDeviceAttribute();
			}
		}
		return devAttr;

	}
	@Override
	public String[] get_attribute_list() throws DevFailed {
		Set<String> set = attributeMap.keySet();
		return set.toArray(new String[1]);
	}

	@Override
	public AttributeInfo get_attribute_info(String attributeName) throws DevFailed {
		DeviceAttribute devAttr = null;
		AttributeInfo attrInfo = null;
		if (attributeMap.containsKey(attributeName)) {
			devAttr = attributeMap.get(attributeName).getDeviceAttribute();	
			attrInfo = new AttributeInfo(attributeName,
                    null,
                    devAttr.getDataFormat(),
                    devAttr.getType(),
                    devAttr.getDimX(),
                    devAttr.getDimY(),
                    "description",
                    "label",
                    "unit",
                    "standard_unit",
                    "display_unit",
                    "format",
                    "min_value",
                    "max_value",
                    "min_alarm",
                    "max_alarm",
                    "writable_attr_name",
                    null);
		}
		return attrInfo;
	}

	@Override
	public boolean use_db() {
		return usedb;
	}
	
	@Override
	public DbDatum get_property(String propertyName) throws DevFailed {
		DbDatum dbDatum = null;
		if (usedb) {
			if (propertyMap.containsKey(propertyName)) {
				DummyDbDatum dummyDbDatum = propertyMap.get(propertyName);
				if (dummyDbDatum != null) {
					dbDatum = dummyDbDatum.getDbDatum();
				}
			}
		}
		return dbDatum;
	}

	@Override
	public void put_property(DbDatum property) throws DevFailed {
		String key = property.name;
		if (propertyMap.containsKey(key)) {
			propertyMap.remove(key);
		}
		propertyMap.put(key, new DummyDbDatum(property));
		logger.debug("Property {} written", key);
	}

	@Override
	public void set_timeout_millis(int millis) throws DevFailed {
		logger.debug("set_timeout_millis: {}ms", millis);
	}

/*	@Override
	public void isAvailable() throws DeviceException {
	}
*/
	// Methods used to setup for junit testing
	
	public void setState(DevState state) {
		this.state = state;
		status = TangoConst.Tango_DevStateName[state.value()];
	}
	
	public void setFail() {
		fail = true;
	}
	
	public ArrayList<DummyDeviceAttribute> getAttributeMap() {
		ArrayList<DummyDeviceAttribute> list = new ArrayList<DummyDeviceAttribute>();
		for (DummyDeviceAttribute attribute : attributeMap.values()) {
			list.add(attribute);
		}
		return list;
	}

	public void setAttributeMap(ArrayList<DummyDeviceAttribute> attributes) {
		for (DummyDeviceAttribute attribute : attributes) {
			attributeMap.put(attribute.getAttributeName(), attribute);
		}
	}

	public ArrayList<DummyDbDatum> getPropertyMap() {
		ArrayList<DummyDbDatum> list = new ArrayList<DummyDbDatum>();
		for (DummyDbDatum property : propertyMap.values()) {
			list.add(property);
		}
		return list;
	}

	public void setPropertyMap(ArrayList<DummyDbDatum> properties) {
		for (DummyDbDatum property : properties) {
			propertyMap.put(property.getPropertyName(), property);
		}
	}

	static public byte[] generateImage(int width, int height) {
		byte[] bytebuf = new byte[width*height*4];
		int[] buffer = new int[width*height];
		Random rand = new Random();    
		int bimg_center_x = rand.nextInt() % (int) (width * 0.05);
		if (rand.nextInt() % 2 == 0) {
			bimg_center_x *= -1;
		}

		int bimg_center_y = rand.nextInt() % (int) (height * 0.05);
		if (rand.nextInt() % 2 == 0) {
			bimg_center_y *= -1;
		}

		int bimg_x_offset_to_zero = ((width - 1) / 2) + bimg_center_x;
		int bimg_y_offset_to_zero = ((height - 1) / 2) + bimg_center_y;
		int limit = Math.max(width, height) / 8;
		int noise = rand.nextInt() % (int) (limit * 0.2);
		if (rand.nextInt() % 2 == 0) {
			noise *= -1;
		}
		limit += noise;
		// Fill image buffer
		int i, j, x=0, y=0, value;
		for (i = -limit; i < limit; i++) {
			y = i + bimg_y_offset_to_zero;
			if (y >= 0 && y < height) {
				for (j = -limit; j < limit; j++) {
					x = j + bimg_x_offset_to_zero;
					if (x >= 0 && x < width) {
						value = (int) Math.sqrt((i * i + j * j));
						buffer[(y * width + x)] = ((value < limit) ? limit - value : 0);
					}
				}
			}
		}
		int k=0;
		for (int l=0; l<width*height; l++) {
			bytebuf[k++] = (byte) (buffer[l] & 0xff);
			bytebuf[k++] = (byte) (buffer[l] >> 8 & 0xff);
			bytebuf[k++] = (byte) (buffer[l] >> 16 & 0xff);
			bytebuf[k++] = (byte) (buffer[l] >> 24 & 0xff);
		}
		return bytebuf;

	}
}
