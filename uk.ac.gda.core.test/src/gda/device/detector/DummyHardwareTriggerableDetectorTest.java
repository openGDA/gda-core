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
import static org.mockito.Mockito.when;
import gda.device.Detector;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.detector.hardwaretriggerable.DummyHardwareTriggerableSimpleDetector;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
public class DummyHardwareTriggerableDetectorTest {

	private DummyHardwareTriggerableSimpleDetector det;

	@Before
	public void setUp() {
		setupTerminal();
		det = new DummyHardwareTriggerableSimpleDetector("det");
	}

	
	
	@Test
	public void testOperationInEmulatedScan() throws Exception {
		assertEquals(det.getStatus(), Detector.IDLE);
		HardwareTriggerProvider triggerProvider = mock(HardwareTriggerProvider.class);
		det.setHardwareTriggerProvider(triggerProvider);
		assertEquals(det.isHardwareTriggering(), false);
		
		det.atScanLineStart();
		det.setCollectionTime(.2);
		det.collectData();
		det.waitWhileBusy();
		assertEquals(Math.cos(.2),  det.getPositionCallable().call(), .0001);
		det.setCollectionTime(.2);
		det.collectData();
		det.waitWhileBusy();
		assertEquals(Math.cos(.4),  det.getPositionCallable().call(), .0001);
		det.atScanLineEnd();
	}
	
	@Test
	public void testOperationInEmulatedScanWithHardwareTriggering() throws Exception {
		HardwareTriggerProvider triggerProvider = mock(HardwareTriggerProvider.class);
		when(triggerProvider.getNumberTriggers()).thenReturn(2);
		det.setHardwareTriggerProvider(triggerProvider);
		
		det.setHardwareTriggering(true);
		det.simulate = true;
		assertEquals(true, det.isHardwareTriggering());
		assertEquals(Detector.IDLE, det.getStatus());
		
		det.atScanLineStart();
		det.setCollectionTime(.2);
		assertEquals(Detector.IDLE, det.getStatus());
		det.waitWhileBusy();
		Callable<Double> positionCallable1 = det.getPositionCallable();
		det.setCollectionTime(.2);
		assertEquals(Detector.IDLE, det.getStatus());
		det.waitWhileBusy();
		Callable<Double> positionCallable2 = det.getPositionCallable();
		det.atScanLineEnd();
		
		det.collectData();
		assertEquals(Detector.BUSY, det.getStatus());
		det.update(null, null);
		assertEquals(Detector.BUSY, det.getStatus());
		det.update(null, null);
		Thread.sleep(1000);
		assertEquals(Detector.IDLE, det.getStatus());
		
				
		assertEquals(Math.cos(.2),  positionCallable1.call(), .0001);
		assertEquals(Math.cos(.4),  positionCallable2.call(), .0001);
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
