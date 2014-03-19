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
import gda.device.continuouscontroller.ConstantVelocityRasterMoveController;
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
public class ConstantVelocityRasterScanTest {

	class TerminalPrinter implements ITerminalPrinter {
		@Override
		public synchronized void print(String text) {
			System.out.print(text);
		}
	}
	
	
	private ContinuouslyScannableViaController mocky;
	private ContinuouslyScannableViaController mockx;

	private ConstantVelocityRasterMoveController mockedController;
	
	private HardwareTriggerableDetector mockeddet1;
	private HardwareTriggerableDetector mockeddet2;
	
	@Before()
	public void before() throws DeviceException {
		
		InterfaceProvider.setTerminalPrinterForTesting(new TerminalPrinter());

		
		mockedController = mock(ConstantVelocityRasterMoveController.class);
		when(mockedController.getName()).thenReturn("mockedController");
		
		mockx = ConstantVelocityScanLineTest.createMockScannableViaController("mockx", mockedController);
		mocky = ConstantVelocityScanLineTest.createMockScannableViaController("mocky", mockedController);
		
		mockeddet1 = ConstantVelocityScanLineTest.createMockDetector("mockeddet1");
		when(mockeddet1.getHardwareTriggerProvider()).thenReturn(mockedController);
		mockeddet2 = ConstantVelocityScanLineTest.createMockDetector("mockeddet2");
		when(mockeddet2.getHardwareTriggerProvider()).thenReturn(mockedController);
		
	
	}

	@Test
	public void singleLineScanMockScannablesAndTwoMockDetectorsIntegratingWhole() throws InterruptedException, Exception {
		TestHelpers.setUpTest(ConstantVelocityRasterScanTest.class, "singleLineScanMockScannablesAndTwoMockDetectorsIntegrating", true);
		InOrder inOrder = runSequence();
		
		inOrder.verify(mockeddet1).setCollectionTime(2.);
		inOrder.verify(mocky).setOperatingContinuously(true);
		inOrder.verify(mockx).setOperatingContinuously(true);
		inOrder.verify(mockeddet1).setHardwareTriggering(true);
		inOrder.verify(mockeddet2).setHardwareTriggering(true);
		inOrder.verify(mockeddet1).setNumberImagesToCollect(6);
		inOrder.verify(mockedController).stopAndReset();


		inOrder.verify(mocky).asynchronousMoveTo(0.);
		inOrder.verify(mockx).asynchronousMoveTo(10.);
		inOrder.verify(mockx).asynchronousMoveTo(11.);
		
		inOrder.verify(mocky).asynchronousMoveTo(1.);
		inOrder.verify(mockx).asynchronousMoveTo(10.);
		inOrder.verify(mockx).asynchronousMoveTo(11.);
		
		inOrder.verify(mocky).asynchronousMoveTo(2.);
		inOrder.verify(mockx).asynchronousMoveTo(10.);
		inOrder.verify(mockx).asynchronousMoveTo(11.);
		
		inOrder.verify(mockedController).stopAndReset();
		
		// Order unimportant but deterministic
		inOrder.verify(mockedController).setOuterStart(0.);
		inOrder.verify(mockedController).setOuterEnd(2.);
		inOrder.verify(mockedController).setOuterStep(1.);
		inOrder.verify(mockedController).setStart(10); // 9.5); // Please see ConstantVelocityRasterScanTest class comment
		inOrder.verify(mockedController).setEnd(11); // 10.5);
		inOrder.verify(mockedController).setStep(1.);
		
		inOrder.verify(mockedController).prepareForMove();

//		inOrder.verify(mockeddet1).collectData();
//		inOrder.verify(mockeddet2).collectData();

		inOrder.verify(mockedController).startMove();
		
		inOrder.verify(mockedController).waitWhileMoving();
		
		verify(mockeddet1, times(1)).collectData();
		verify(mockeddet2, times(1)).collectData();
	}

	@Test
	public void singleLineScanMockScannablesAndTwoMockDetectorsIntegratingDet1() throws InterruptedException, Exception {
		TestHelpers.setUpTest(ConstantVelocityRasterScanTest.class, "singleLineScanMockScannablesAndTwoMockDetectorsIntegrating", true);
		InOrder inOrder = runSequence();
		inOrder.verify(mockedController).prepareForMove();
		inOrder.verify(mockeddet1).collectData();
//		inOrder.verify(mockeddet2).collectData();
		inOrder.verify(mockedController).startMove();
	}

	@Test
	public void singleLineScanMockScannablesAndTwoMockDetectorsIntegratingDet2() throws InterruptedException, Exception {
		TestHelpers.setUpTest(ConstantVelocityRasterScanTest.class, "singleLineScanMockScannablesAndTwoMockDetectorsIntegrating", true);
		InOrder inOrder = runSequence();
		inOrder.verify(mockedController).prepareForMove();
//		inOrder.verify(mockeddet1).collectData();
		inOrder.verify(mockeddet2).collectData();
		inOrder.verify(mockedController).startMove();
	}
	
	private InOrder runSequence() throws InterruptedException, Exception {
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", "DummyDataWriter");
		when(mockeddet1.integratesBetweenPoints()).thenReturn(true);
		when(mockeddet2.integratesBetweenPoints()).thenReturn(true);
		when(mockx.getLevel()).thenReturn(5);
		when(mocky.getLevel()).thenReturn(5);
		ConstantVelocityScanLine scan = new ConstantVelocityRasterScan(new Object[]{mocky, 0., 2., 1., mockx, 10., 11., 1., mockeddet1, 2., mockeddet2});

		scan.runScan();
		InOrder inOrder = inOrder(mocky, mockx, mockedController, mockeddet1, mockeddet2);
		return inOrder;
	}

}
