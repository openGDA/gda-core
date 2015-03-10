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

package gda.device.detector.pilatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import gda.device.TangoDeviceProxy;
import gda.device.impl.DummyTangoDeviceImpl;

import org.junit.Test;

public class TangoPilatusDetectorTest {

	private TangoDeviceProxy dev;
	private TangoLimaDetector detector = new TangoLimaDetector();
	private DummyTangoDeviceImpl dummyTangoDevice;

	public TangoPilatusDetectorTest() {
		dummyTangoDevice = new DummyTangoDeviceImpl("tango::12345");
		dev = new TangoDeviceProxy(dummyTangoDevice);
		try {
			dev.write_attribute(new DeviceAttribute("acq_trigger_mode", "EXTERNAL_GATE"));
			dev.write_attribute(new DeviceAttribute("latency_time", 4.0));
			dev.write_attribute(new DeviceAttribute("acq_status", "ready"));
			dev.write_attribute(new DeviceAttribute("acq_nb_frames", 25));
			dev.write_attribute(new DeviceAttribute("acq_mode", "SINGLE"));
		} catch (DevFailed e) {
			fail("This should not occur");
		}
		detector.setLimaTangoDeviceProxy(dev);
	}

	@Test
	public void testReadDeviceAttributes() {
		try {
			assertEquals(25, detector.readNbFrames());
			assertEquals("EXTERNAL_GATE", detector.readTriggerMode());
			assertEquals(4.0, detector.readLatencyTime(),0.0);
			assertEquals("SINGLE", detector.readAcqMode());
		} catch (Exception e) {
			fail("Exception from readDeviceAttributes()");
		}
	}

	@Test
	public void testState() throws DevFailed {
		dummyTangoDevice.setState(DevState.STANDBY);
		assertEquals("STANDBY", dev.status());
		dummyTangoDevice.setState(DevState.UNKNOWN);
		assertEquals("UNKNOWN", dev.status());
		dummyTangoDevice.setState(DevState.ON);
		assertEquals("ON", dev.status());
	}

	// @Test
	public void testSetAttribute() {
		try {
			detector.setAttribute("StepSize", 3.3);
			assertEquals(3.3, detector.getAttribute("StepSize"));
			detector.setAttribute("StepSize", 1.5);
			assertEquals(1.5, detector.getAttribute("StepSize"));

			detector.setAttribute("Acceleration", 18);
			assertEquals(18, detector.getAttribute("Acceleration"));
			detector.setAttribute("Acceleration", 15);
			assertEquals(15, detector.getAttribute("Acceleration"));

			detector.setAttribute("Backlash", 8.6);
			assertEquals(8.6, detector.getAttribute("Backlash"));
			detector.setAttribute("Backlash", 3.5);
			assertEquals(3.5, detector.getAttribute("Backlash"));

			detector.setAttribute("FirstVelocity", 14);
			assertEquals(14, detector.getAttribute("FirstVelocity"));
			detector.setAttribute("FirstVelocity", 10);
			assertEquals(10, detector.getAttribute("FirstVelocity"));

			detector.setAttribute("Steps_per_unit", 5.5);
			assertEquals(5.5, detector.getAttribute("Steps_per_unit"));
			detector.setAttribute("Steps_per_unit", 3.0);
			assertEquals(3.0, detector.getAttribute("Steps_per_unit"));

			detector.setAttribute("Home_position", 0.5);
			assertEquals(0.5, detector.getAttribute("Home_position"));
			detector.setAttribute("Home_position", -0.5);
			assertEquals(-0.5, detector.getAttribute("Home_position"));

			detector.setAttribute("PresetPosition", 5.5);
			assertEquals(5.5, detector.getAttribute("PresetPosition"));
			detector.setAttribute("PresetPosition", 5.0);
			assertEquals(5.0, detector.getAttribute("PresetPosition"));
		} catch (Exception e) {
			fail("Exception from getAttribute()");
		}
	}
}
