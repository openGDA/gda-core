/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.scan;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gda.TestHelpers;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.ConstantVelocityMoveController;
import gda.device.detector.NexusDetector;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetector;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockSettings;
import org.mockito.Mockito;

/**
 * Test of creating a multiple scan which is a scan of more than 1 ConcurrentScanChild
 */
public class MultiRegionScanTest {
	class TerminalPrinter implements ITerminalPrinter {
		@Override
		public synchronized void print(String text) {
			System.out.print(text);
		}
	}

	private Detector createMockDetector(Class clazz, MockSettings settings, String name) throws DeviceException {
		Detector det = (Detector) (settings != null ? mock(clazz, settings) : mock(clazz));
		when(det.getName()).thenReturn(name);
		when(det.getInputNames()).thenReturn(new String[] { name });
		when(det.getOutputFormat()).thenReturn(new String[] { "%s" });
		when(det.readout()).thenReturn(name + "readout");
		when(det.getCollectionTime()).thenReturn(1.);
		when(det.getLevel()).thenReturn(100);
		return det;
	}

	@SuppressWarnings("rawtypes")
	private Scannable createMockScannable(Class clazz, MockSettings settings, String name) throws DeviceException {
		@SuppressWarnings("unchecked")
		Scannable mockscn = (Scannable) (settings != null ? mock(clazz, settings) : mock(clazz));
		when(mockscn.getName()).thenReturn(name);
		when(mockscn.getInputNames()).thenReturn(new String[] { name });
		when(mockscn.getExtraNames()).thenReturn(new String[] {});
		when(mockscn.getOutputFormat()).thenReturn(new String[] { "%.2f" });
		when(mockscn.getLevel()).thenReturn(5);
		when(mockscn.getPosition()).thenReturn(new Double[] { 1.1 });
		when(mockscn.isBusy()).thenReturn(true);
		// when(scn.checkPositionValid(anyObject()) == null).thenReturn(true);
		when(mockscn.toFormattedString()).thenReturn(name + " : " + 1.1);
		return mockscn;
	}

	@Before()
	public void before() throws DeviceException {

		InterfaceProvider.setTerminalPrinterForTesting(new TerminalPrinter());

	}

	public void testSingleScan() throws Exception {
		TestHelpers.setUpTest(ConstantVelocityScanLineTest.class, "testSingleScan", true);
		// LocalProperties.set("gda.data.scan.datawriter.dataFormat", "DummyDataWriter");

		/*
		 * Detector det = createMockDetector(Detector.class, "det1"); Scannable scan =
		 * createMockScannable(Scannable.class,
		 * Mockito.withSettings().extraInterfaces(ScanPositionRecordable.class),"scan1");
		 */
		ConstantVelocityMoveController mockedController = mock(ConstantVelocityMoveController.class);
		when(mockedController.getName()).thenReturn("mockedController");

		ContinuouslyScannableViaController cscan = (ContinuouslyScannableViaController) createMockScannable(
				ContinuouslyScannableViaController.class,
				Mockito.withSettings().extraInterfaces(ScanPositionRecordable.class), "cscan1");

		when(cscan.getContinuousMoveController()).thenReturn(mockedController);

		HardwareTriggerableDetector mockeddet1 = (HardwareTriggerableDetector) createMockDetector(
				HardwareTriggerableDetector.class, Mockito.withSettings().extraInterfaces(NexusDetector.class), "mockeddet1");
		when(mockeddet1.getHardwareTriggerProvider()).thenReturn(mockedController);

		MultiRegionScan mrs = new MultiRegionScan();
		mrs.addScan(new ConcurrentScan(new Object[] { cscan, 0., 2., 1., mockeddet1 }));
		mrs.addScan(new ConcurrentScan(new Object[] { cscan, 0., 2., 1., mockeddet1 }));

		ConstantVelocityScanLine cvs = new ConstantVelocityScanLine(new Object[] { cscan, 0., 2., 1., mockeddet1, 2. });

		mrs.addScan(cvs);
		mrs.runScan();
	}

}
