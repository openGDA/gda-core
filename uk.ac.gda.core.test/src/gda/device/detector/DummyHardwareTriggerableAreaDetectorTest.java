/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.detector.hardwaretriggerable.DummyHardwareTriggerableAreaDetector;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
public class DummyHardwareTriggerableAreaDetectorTest {

	private DummyHardwareTriggerableAreaDetector det;

	@Before
	public void setUp() {
		setupTerminal();
		det = new DummyHardwareTriggerableAreaDetector("det");
		det.simulate = true;
	}
	
	@Test
	public void testOperationInEmulatedStepScan() throws Exception {
		HardwareTriggerProvider triggerProvider = mock(HardwareTriggerProvider.class);
		det.setHardwareTriggerProvider(triggerProvider);
		det.configure();
		
		assertEquals(det.getStatus(), Detector.IDLE);
		assertEquals(det.isHardwareTriggering(), false);
		
		det.atScanLineStart();
		det.setCollectionTime(.2);
		det.collectData();
		det.waitWhileBusy();
		assertEquals("det/image1.img",  det.readout());
		det.setCollectionTime(.2);
		det.collectData();
		det.waitWhileBusy();
		assertEquals("det/image2.img",  det.readout());
		det.atScanLineEnd();
		
		verifyZeroInteractions(triggerProvider);
	}

	@Test
	@Ignore("2010/03/01 Test ignored since it frequently fails with a race condition - test needs reworking")
	public void testOperationInEmulatedScanWithHardwareTriggering() throws DeviceException, InterruptedException {
		HardwareTriggerProvider triggerProvider = mock(HardwareTriggerProvider.class);
		det.setHardwareTriggerProvider(triggerProvider);
		
		det.setHardwareTriggering(true);
		assertEquals(true, det.isHardwareTriggering());
		assertEquals(Detector.IDLE, det.getStatus());
		
		det.atScanLineStart();
		det.setCollectionTime(.2);
		assertEquals(Detector.IDLE, det.getStatus());
		det.waitWhileBusy();
		assertEquals("det/image1.img",  det.readout());	
		det.setCollectionTime(.2);
		assertEquals(Detector.IDLE, det.getStatus());
		det.waitWhileBusy();
		assertEquals("det/image2.img",  det.readout());
		det.atScanLineEnd();
		
		det.collectData();
		assertEquals(det.getStatus(), Detector.BUSY);
		det.update(null, null);
		assertEquals(det.getStatus(), Detector.BUSY);
		det.update(null, null);
		Thread.sleep(1000);
		assertEquals(det.getStatus(), Detector.IDLE);
		
	}
	
	private void setupTerminal() {
		ITerminalPrinter terminalPrinter = new ITerminalPrinter(){
			@Override
			public void print(String text) {
				System.out.println(text);
			}
		};
		InterfaceProvider.setTerminalPrinterForTesting(terminalPrinter );
	}
	
}
