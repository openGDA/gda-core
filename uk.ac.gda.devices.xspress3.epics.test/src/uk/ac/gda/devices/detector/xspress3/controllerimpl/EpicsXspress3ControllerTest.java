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

package uk.ac.gda.devices.detector.xspress3.controllerimpl;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.devices.detector.xspress3.TRIGGER_MODE;
import uk.ac.gda.devices.detector.xspress3.Xspress3Detector;

/**
 * To be used as a test against hardware.
 * <p>
 * Requires an Xspress3 unit operated via Epics. A pulse generator should be
 * plugged into the Xspress3 unit to provide gating signals.
 * <p>
 * Before running this test:
 * <ul>
 * <li>start the IOC which talks to the Xspress3 unit.</li>
 * <li>In EDM, connect to the hardware.</li>
 * <li>Edit the JCALibrary.properties in this folder to point to the machine
 * running the IOC.</li>
 * <li>Edit the template value in this class</li>
 * </ul>
 * <p>
 * Not a unit test as this has a dependency on hardware.
 * <p>
 *
 * @author rjw82
 *
 */
public class EpicsXspress3ControllerTest {

	private static EpicsXspress3Controller x3c;
	private static Xspress3Detector x3d;

	private static final String IOC_TEMPLATE = "BL18B-EA-XSP3-01";

	public static void main(String[] args) {
		try {
			// set properties file
			LocalProperties.set("gov.aps.jca.JCALibrary.properties",
					EpicsXspress3ControllerTest.class.getResource("JCALibrary.properties").getFile());

			// create Controller
			x3c = new EpicsXspress3Controller();
			x3c.setEpicsTemplate(IOC_TEMPLATE);
			x3c.configure();
			x3c.setTriggerMode(TRIGGER_MODE.TTl_Veto_Only);
			x3c.setPerformROICalculations(true);
			x3c.setPerformROIUpdates(true);
			x3c.setNumberOfChannels(4); // this defines the extraNames and
			// plottable values

			// create detector
			x3d = new Xspress3Detector();
			x3d.setName("x3d");
			x3d.setController(x3c);
//			x3d.setNumTrackerExtension("");
			x3d.setFirstChannelToRead(0);
			x3d.configure();

			// set up rois
			DetectorROI[] rois = new DetectorROI[1];
			rois[0] = new DetectorROI();
			rois[0].setRoiStart(100);
			rois[0].setRoiEnd(200);
			x3d.setRegionsOfInterest(rois);

			// now to simulate a step scan where using simple detector class
			x3d.setFilePath("/tmp/"); // TODO how to do this in a real scan??
			x3d.setWriteHDF5Files(true);

			x3d.setNumberOfFramesToCollect(1);

			x3d.atScanStart();
			x3d.atScanLineStart();
			for (int i = 0; i < 10; i++) {
				x3d.collectData();
				System.out.println("waiting for acquire to end");
				while (x3d.isBusy()) {
					Thread.sleep(100);
				}
				System.out.println("acquire finished");
				System.out.println(x3d.readout().toString());
			}

			// Now simulate a continuous scan in which the veto pulses come from
			// a source outside of any TFG unit
			// If a TFG was in use the use the Xspress3System detector class
			// instead.

			x3d.setFilePath("/tmp/");
			x3d.setWriteHDF5Files(true);
			x3d.setNumberOfFramesToCollect(50);
			x3d.setFilePrefix("Test_Continuous");
			x3d.atScanStart();
			x3d.atScanLineStart();
			x3d.collectData();
			while (x3d.isBusy()) {
				// ScanBase.checkForInterrupts();
				Thread.sleep(100);
			}
			System.out.println("acquire finished");
			for (int i = 0; i < 50; i++) {
				System.out.println(x3d.readout());
			}

			// now check that files have been created in /tmp of the machine
			// running the IOC

			// TODO review work done tonight
			// test in a scan in I20 simulation
			// create a duumy config for offline testing and test again
			// look at the X3System version and see if OK.

		} catch (FactoryException e) {
			e.printStackTrace();
		} catch (DeviceException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}