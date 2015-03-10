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

package gda.data.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import gda.device.TangoDeviceProxy;
import gda.device.impl.DummyTangoDeviceImpl;

public class TangoMetadataEntryTest {
	private TangoDeviceProxy dev;
	private TangoMetadataEntry tme = new TangoMetadataEntry();
	
	public TangoMetadataEntryTest() {
		 dev = new TangoDeviceProxy(new DummyTangoDeviceImpl("tango::12345"));
		 try {
			// setup some default values in the dummy device proxy
			dev.write_attribute(new DeviceAttribute("test-double", 3.142));
			dev.write_attribute(new DeviceAttribute("test-boolean", true));
			dev.write_attribute(new DeviceAttribute("test-short", (short)128));
			dev.write_attribute(new DeviceAttribute("test-long", 65536));
			dev.write_attribute(new DeviceAttribute("test-float", (float) 5.63));
			dev.write_attribute(new DeviceAttribute("test-string", "test-string"));
		} catch (DevFailed e) {
			fail("This should not occur");
		}
		tme.setTangoDeviceProxy(dev);
		 
	}

//	@Test
	public void testConfigure() {
		fail("Not yet implemented");
	}

	@Test
	public void testReadActualValue() {
		try {
			tme.setAttributeName("test-double");
			assertEquals("3.142", tme.readActualValue());
			tme.setAttributeName("test-boolean");
			assertEquals("true", tme.readActualValue());
			tme.setAttributeName("test-short");
			assertEquals("128", tme.readActualValue());
			tme.setAttributeName("test-long");
			assertEquals("65536", tme.readActualValue());
			tme.setAttributeName("test-float");
			assertEquals("5.63", tme.readActualValue());
			tme.setAttributeName("test-string");
			assertEquals("test-string", tme.readActualValue());
		} catch (Exception e) {
			fail("Exception from readActualValue()");
		}
	}

}
