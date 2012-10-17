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

package gda.scan;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.continuouscontroller.ConstantVelocityMoveController;
import gda.device.detector.hardwaretriggerable.DummyHardwareTriggerableAreaDetector;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetector;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;


/**
 *
 */
public class ConstantVelocityScanLineTest {

	class TerminalPrinter implements ITerminalPrinter {
		@Override
		public synchronized void print(String text) {
			System.out.print(text);
		}
	}
	
	
	private ContinuouslyScannableViaController mockscn;
	private ConstantVelocityMoveController mockedController;
	
	private HardwareTriggerableDetector mockeddet1;
	private HardwareTriggerableDetector mockeddet2;
	
	private DummyHardwareTriggerableAreaDetector dummydet1;
	private DummyHardwareTriggerableAreaDetector dummydet2;
	
	@Before()
	public void before() throws DeviceException {
		
		InterfaceProvider.setTerminalPrinterForTesting(new TerminalPrinter());

		
		mockedController = mock(ConstantVelocityMoveController.class);
		when(mockedController.getName()).thenReturn("mockedController");
		
		
		mockscn = mock(ContinuouslyScannableViaController.class);
		when(mockscn.getName()).thenReturn("scn");
		when(mockscn.getInputNames()).thenReturn(new String[]{"scn"});
		when(mockscn.getExtraNames()).thenReturn(new String[]{});
		when(mockscn.getOutputFormat()).thenReturn(new String[]{"%.2f"});
		when(mockscn.getLevel()).thenReturn(5);
		when(mockscn.getPosition()).thenReturn(new Double[]{1.1});
		when(mockscn.isBusy()).thenReturn(true);
		// when(scn.checkPositionValid(anyObject()) == null).thenReturn(true);
		when(mockscn.toFormattedString()).thenReturn("scn" + " : " + 1.1);
		
		when(mockscn.getContinuousMoveController()).thenReturn(mockedController);
		
		mockeddet1 = createMockDetector("mockeddet1");
		when(mockeddet1.getHardwareTriggerProvider()).thenReturn(mockedController);
		mockeddet2 = createMockDetector("mockeddet2");
		when(mockeddet2.getHardwareTriggerProvider()).thenReturn(mockedController);
		
		dummydet1 = new DummyHardwareTriggerableAreaDetector("dummydet1");
		dummydet1.setHardwareTriggerProvider(mockedController);
		dummydet2 = new DummyHardwareTriggerableAreaDetector("dummydet2");
		dummydet2.setHardwareTriggerProvider(mockedController);
		
	}

	private HardwareTriggerableDetector createMockDetector(String name) throws DeviceException {
		HardwareTriggerableDetector det = mock(HardwareTriggerableDetector.class);
		when(det.getName()).thenReturn(name);
		when(det.getInputNames()).thenReturn(new String[]{name});
		when(det.getOutputFormat()).thenReturn(new String[]{"%s"});
		when(det.readout()).thenReturn(name + "readout");
		when(det.getCollectionTime()).thenReturn(1.);
		when(det.getLevel()).thenReturn(100);
		return det;
	}
	
	@Test
	public void singleLineScanMockScannablesAndTwoMockDetectors() throws InterruptedException, Exception {
		TestHelpers.setUpTest(ConstantVelocityScanLineTest.class, "singleLineScanMockScannablesAndTwoMockDetectors", true);
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", "DummyDataWriter");

		ConstantVelocityScanLine scan = new ConstantVelocityScanLine(new Object[]{mockscn, 0., 2., 1., mockeddet1, 2., mockeddet2});
		
		scan.runScan();
		InOrder inOrder = inOrder(mockscn, mockedController, mockeddet1, mockeddet2);
		
		inOrder.verify(mockeddet1).setCollectionTime(2.); // must be called in constructor!
		inOrder.verify(mockscn).setOperatingContinuously(true);
		inOrder.verify(mockeddet1).setHardwareTriggering(true);
		inOrder.verify(mockeddet2).setHardwareTriggering(true);
		inOrder.verify(mockeddet1).setNumberImagesToCollect(3);


		inOrder.verify(mockedController).stopAndReset();
		inOrder.verify(mockscn).asynchronousMoveTo(0.);
		inOrder.verify(mockscn).asynchronousMoveTo(1.);
		inOrder.verify(mockscn).asynchronousMoveTo(2.);
		inOrder.verify(mockedController).setStart(0.);
		inOrder.verify(mockedController).setEnd(2.);
		inOrder.verify(mockedController).setStep(1.);
		inOrder.verify(mockedController).prepareForMove();
		
		inOrder.verify(mockeddet1).collectData(); // order unimportant
		inOrder.verify(mockeddet2).collectData(); // order unimportant
		
		inOrder.verify(mockedController).startMove();
		inOrder.verify(mockedController).waitWhileMoving();
		
		verify(mockeddet1, times(1)).collectData();
		verify(mockeddet2, times(1)).collectData();

	}
	
	@Test
	public void singleLineScanMockScannablesAndTwoMockDetectorsIntegrating() throws InterruptedException, Exception {
		TestHelpers.setUpTest(ConstantVelocityScanLineTest.class, "singleLineScanMockScannablesAndTwoMockDetectorsIntegrating", true);
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", "DummyDataWriter");
		when(mockeddet1.integratesBetweenPoints()).thenReturn(true);
		when(mockeddet2.integratesBetweenPoints()).thenReturn(true);
		when(mockscn.getLevel()).thenReturn(5);
		ConstantVelocityScanLine scan = new ConstantVelocityScanLine(new Object[]{mockscn, 0., 2., 1., mockeddet1, 2., mockeddet2});
		
		scan.runScan();
		InOrder inOrder = inOrder(mockscn, mockedController, mockeddet1, mockeddet2);
		
		inOrder.verify(mockeddet1).setCollectionTime(2.); // must be called in constructor!
		inOrder.verify(mockscn).setOperatingContinuously(true);
		inOrder.verify(mockeddet1).setHardwareTriggering(true);
		inOrder.verify(mockeddet2).setHardwareTriggering(true);
		inOrder.verify(mockeddet1).setNumberImagesToCollect(3);

		inOrder.verify(mockedController).stopAndReset();
		
		inOrder.verify(mockscn).asynchronousMoveTo(0.);
		inOrder.verify(mockscn).asynchronousMoveTo(1.);
		inOrder.verify(mockscn).asynchronousMoveTo(2.);
		
		inOrder.verify(mockedController).stopAndReset();
		inOrder.verify(mockedController).setStart(-.5);
		inOrder.verify(mockedController).setEnd(1.5);
		inOrder.verify(mockedController).setStep(1.);
		
		
		inOrder.verify(mockedController).prepareForMove();
		
		inOrder.verify(mockeddet1).collectData(); // order unimportant
		inOrder.verify(mockeddet2).collectData(); // order unimportant
		
		inOrder.verify(mockedController).startMove();
		inOrder.verify(mockedController).waitWhileMoving();
		
		verify(mockeddet1, times(1)).collectData();
		verify(mockeddet2, times(1)).collectData();
	}

	
	// TODO: Check for speed put back for failed scans
}
