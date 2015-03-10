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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import gda.device.TangoDeviceProxy;
import gda.device.impl.DummyTangoDeviceImpl;

public class TangoScannableTest {
	private TangoDeviceProxy dev;
	private TangoScannable scannable = new TangoScannable();
	
	public TangoScannableTest() {
		 dev = new TangoDeviceProxy( new DummyTangoDeviceImpl("tango::12345"));
		 try {
			// setup some default values in the dummy device proxy
			dev.write_attribute(new DeviceAttribute("test-double", 3.142));
			dev.write_attribute(new DeviceAttribute("test-short", (short)128));
			dev.write_attribute(new DeviceAttribute("test-long", 65536));
			dev.write_attribute(new DeviceAttribute("test-float", (float) 5.63));
		} catch (DevFailed e) {
			fail("This should not occur");
		}
		scannable.setTangoDeviceProxy(dev);
	}	 

	@Test
	public void testGetPosition() {
		try {
			scannable.setAttributeName("test-double");
			assertEquals(3.142, scannable.getPosition());
			scannable.setAttributeName("test-short");
			assertEquals((short)128, scannable.getPosition());
			scannable.setAttributeName("test-long");
			assertEquals(65536, scannable.getPosition());
			scannable.setAttributeName("test-float");
			assertEquals((float)5.63, scannable.getPosition());
		} catch (Exception e) {
			fail("Exception from getPosition()");
		}
	}

	@Test
	public void testAsynchronousMoveTo() {
		try {
			scannable.setAttributeName("test-double");
			assertEquals(3.142, scannable.getPosition());
			for (int i = 0; i < 10; i++) {
				scannable.asynchronousMoveTo(5.0 * i);
				assertEquals(5.0 * i, scannable.getPosition());
			}
			scannable.setAttributeName("test-short");
			assertEquals((short)128, scannable.getPosition());
			for (int i = 0; i < 10; i++) {
				short sval = new Integer(8*i).shortValue();
				scannable.asynchronousMoveTo(sval);
				assertEquals(sval, scannable.getPosition());
			}
			scannable.setAttributeName("test-long");
			assertEquals(65536, scannable.getPosition());
			for (int i = 0; i < 10; i++) {
				scannable.asynchronousMoveTo(101 * i);
				assertEquals(101 * i, scannable.getPosition());
			}
			scannable.setAttributeName("test-float");
			assertEquals((float)5.63, scannable.getPosition());
			for (int i = 0; i < 10; i++) {
				float fval = (float) 5.63 * i;
				scannable.asynchronousMoveTo(fval);
				assertEquals(fval, scannable.getPosition());
			}
		} catch (Exception e) {
			fail("Exception from getPosition()");
		}		
	}
}
