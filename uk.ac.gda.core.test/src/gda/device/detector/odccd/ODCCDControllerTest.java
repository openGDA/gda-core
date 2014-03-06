/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector.odccd;

import static org.junit.Assert.*;
import gda.device.detector.odccd.ODCCDController;

import org.junit.Test;

/**
 * ODCCDController Test Class
 */
public class ODCCDControllerTest {

	static String host = "i15-control"; // "172.23.115.193"; //use localhost to talk to a simulator, i15-control to talk

	// to a port re-director

	// or use 172.23.115.193 to talk to i15's IS system
	// note that ODCCDController can only be connected to from i15-control
	/**
	 * Test the connection
	 */
	@Test
	public void testConnect() {
		ODCCDController controller = new ODCCDController();
		try {
			controller.connect(host);

			// double temp = controller.temperature();
			String name = controller.getDataName();
			// assertEquals(1.0, temp);
			assertEquals("temperature", name);

			// temp = controller.waterTemperature();
			name = controller.getDataName();
			// assertEquals(2.0, temp);
			assertEquals("water temperature", name);

			// send db ls command
			controller.runScript("call importRunList \"C:/Data/test/gda.run\" \"/root/gda_run\"");
			controller.readInputUntil("importRunList completed.");

			controller.runScript("db ls /root/gda_run;");
			controller.readInputUntil("api:(/root/gda_run)");
			controller.readInputUntil("api:End of list.");
			controller.runScript("db ls /root/gda_run/runs/run1;");
			controller.readInputUntil("api:(/root/gda_run/runs/run1)");
			controller.readInputUntil("api:End of list.");
		} catch (Exception e) {
			fail("Connection failed " + e.getMessage());
		} finally {
			controller.disconnect();
		}
	}

	// @Test
	/**
	 * Test the shutter
	 */
	public void testShutter() {
		ODCCDController controller = new ODCCDController();
		try {
			controller.connect(host);
			String openResponse = controller.openShutter();
			String closeResponse = controller.closeShutter();
			controller.disconnect();
			assertEquals("OPEN", openResponse);
			assertEquals("CLOSED", closeResponse);
		} catch (Exception e) {
			fail("Connection failed " + e.getMessage());
		}
	}

}
