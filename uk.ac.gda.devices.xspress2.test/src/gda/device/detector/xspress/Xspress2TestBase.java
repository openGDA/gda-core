/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.device.detector.xspress;

import static org.junit.Assert.fail;

import gda.device.DeviceException;
import gda.device.detector.DUMMY_XSPRESS2_MODE;
import gda.device.detector.DummyDAServer;
import gda.device.detector.xspress.xspress2data.Xspress2DAServerController;
import gda.device.timer.Etfg;
import gda.factory.FactoryException;

public class Xspress2TestBase {
	final String TestFileFolder = "testfiles/gda/device/detector/xspress/";

	protected Xspress2Detector xspress;
	protected DummyDAServer daserver;
	protected Xspress2DAServerController controller;

	protected void setupDetector() {
		xspress = new Xspress2Detector();
		controller = new Xspress2DAServerController();
		xspress.setController(controller);

		daserver = new DummyDAServer();
		daserver.setName("DummyDAServer");
		daserver.setXspressMode(DUMMY_XSPRESS2_MODE.XSPRESS2_FULL_MCA);
		daserver.connect();
		daserver.setNonRandomTestData(true);
		Etfg tfg = new Etfg();
		tfg.setName("tfg");

		try {
			controller.setDaServer(daserver);
			controller.setTfg(tfg);
			xspress.setConfigFileName(TestFileFolder + "xspressConfig.xml");
			xspress.setDtcConfigFileName(TestFileFolder + "Xspress_DeadTime_Parameters.xml");
			xspress.setName("xspressTest");
			controller.setDaServer(daserver);
			controller.setTfg(tfg);
			controller.setMcaOpenCommand("xspress open-mca");
			controller.setScalerOpenCommand("xspress open-scalers");
			controller.setStartupScript("xspress2 format-run 'xsp1' res-none");
			controller.setXspressSystemName("xsp1");
			xspress.setFullMCABits(8);
			xspress.configure();
		} catch (DeviceException e) {
			fail(e.getMessage());
		} catch (FactoryException e) {
			fail(e.getMessage());
		}
	}
}
