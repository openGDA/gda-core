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

package gda.device.motor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DbDatum;
import fr.esrf.TangoApi.DeviceAttribute;
import gda.device.TangoDeviceProxy;
import gda.device.impl.DummyTangoDeviceImpl;

import org.junit.Test;

public class TangoMotorTest {

	private TangoDeviceProxy dev;
	private TangoMotor motor = new TangoMotor();
	
	public TangoMotorTest() {
		 dev = new TangoDeviceProxy( new DummyTangoDeviceImpl("tango::12345"));
		 try {
			// setup some default values in the dummy device proxy
			dev.write_attribute(new DeviceAttribute("Steps_per_unit", 3.0));
			dev.write_attribute(new DeviceAttribute("FirstVelocity", 10));
			dev.write_attribute(new DeviceAttribute("Acceleration", 15));
			dev.write_attribute(new DeviceAttribute("Velocity", 25));
			dev.write_attribute(new DeviceAttribute("Backlash", 3.5));
			dev.write_attribute(new DeviceAttribute("Position", 7.5));
			dev.write_attribute(new DeviceAttribute("PresetPosition", 5.0));
			dev.write_attribute(new DeviceAttribute("Home_position", -0.5));
			dev.write_attribute(new DeviceAttribute("HardLimitLow", false));
			dev.write_attribute(new DeviceAttribute("HardLimitHigh", false));
			dev.write_attribute(new DeviceAttribute("Home_side", true));
			dev.write_attribute(new DeviceAttribute("StepSize", 1.5));
			dev.put_property(new DbDatum("Calibrated", false));			
		} catch (DevFailed e) {
			fail("This should not occur");
		}
		motor.setTangoDeviceProxy(dev);
	}	 

	@Test
	public void testGetPosition() {
		try {
			assertEquals(new Double(7.5), new Double(motor.getPosition()));
		} catch (Exception e) {
			fail("Exception from getPosition()");
		}
	}

	@Test
	public void testMove() {
		try {
			motor.moveBy(5.25);
			assertEquals(new Double(12.75), new Double(motor.getPosition()));
			motor.moveTo(7.5);
			assertEquals(new Double(7.5), new Double(motor.getPosition()));
		} catch (Exception e) {
			fail("Exception from getPosition()");
		}
	}
	@Test
	public void testGetSpeed() {
		try {
			motor.setSpeed(50.0);
			assertEquals(new Double(50.0), new Double(motor.getSpeed()));
			motor.setSpeed(25.0);
			assertEquals(new Double(25.0), new Double(motor.getSpeed()));
		} catch (Exception e) {
			fail("Exception from getPosition()");
		}
	}

	@Test
	public void testGetAttribute() {
		try {
			assertEquals(1.5, motor.getAttribute("StepSize"));
			assertEquals(15, motor.getAttribute("Acceleration"));
			assertEquals(3.5, motor.getAttribute("Backlash"));
			assertEquals(10, motor.getAttribute("FirstVelocity"));
			assertEquals(3.0, motor.getAttribute("Steps_per_unit"));
			assertEquals(false, motor.getAttribute("HardLimitLow"));
			assertEquals(false, motor.getAttribute("HardLimitHigh"));
			assertEquals(true, motor.getAttribute("Home_Side"));
			assertEquals(-0.5, motor.getAttribute("Home_position"));
			assertEquals(5.0, motor.getAttribute("PresetPosition"));
		} catch (Exception e) {
			fail("Exception from getAttribute()");
		}
	}

	@Test
	public void testSetAttribute() {
		try {
			motor.setAttribute("StepSize", 3.3);
			assertEquals(3.3, motor.getAttribute("StepSize"));
			motor.setAttribute("StepSize", 1.5);
			assertEquals(1.5, motor.getAttribute("StepSize"));

			motor.setAttribute("Acceleration", 18);
			assertEquals(18, motor.getAttribute("Acceleration"));
			motor.setAttribute("Acceleration", 15);
			assertEquals(15, motor.getAttribute("Acceleration"));

			motor.setAttribute("Backlash", 8.6);
			assertEquals(8.6, motor.getAttribute("Backlash"));
			motor.setAttribute("Backlash", 3.5);
			assertEquals(3.5, motor.getAttribute("Backlash"));
			
			motor.setAttribute("FirstVelocity", 14);
			assertEquals(14, motor.getAttribute("FirstVelocity"));
			motor.setAttribute("FirstVelocity", 10);
			assertEquals(10, motor.getAttribute("FirstVelocity"));
			
			motor.setAttribute("Steps_per_unit", 5.5);
			assertEquals(5.5, motor.getAttribute("Steps_per_unit"));
			motor.setAttribute("Steps_per_unit", 3.0);
			assertEquals(3.0, motor.getAttribute("Steps_per_unit"));

			motor.setAttribute("Home_position", 0.5);
			assertEquals(0.5, motor.getAttribute("Home_position"));
			motor.setAttribute("Home_position", -0.5);
			assertEquals(-0.5, motor.getAttribute("Home_position"));

			motor.setAttribute("PresetPosition", 5.5);
			assertEquals(5.5, motor.getAttribute("PresetPosition"));
			motor.setAttribute("PresetPosition", 5.0);
			assertEquals(5.0, motor.getAttribute("PresetPosition"));
		} catch (Exception e) {
			fail("Exception from getAttribute()");
		}
	}
}
