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

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import gda.MockFactory;
import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.DummyTrajectoryMoveController;
import gda.device.continuouscontroller.TrajectoryMoveController;
import gda.device.detector.hardwaretriggerable.DummyHardwareTriggerableAreaDetector;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetector;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.factory.FactoryException;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

/**
 *
 */
public class TrajectoryScanLineTest {

	class TerminalPrinter implements ITerminalPrinter {
		@Override
		public synchronized void print(String text) {
			System.out.print(text);
		}
	}
	
	
	private ContinuouslyScannableViaController mockedx;
	private ContinuouslyScannableViaController mockedy;
	private TrajectoryMoveController mockedController;
	private HardwareTriggerableDetector mockeddet1;
	private HardwareTriggerableDetector mockeddet2;
	private ContinuouslyScannableViaController mockedGroup;
	private DummyHardwareTriggerableAreaDetector dummydet1;
	private DummyHardwareTriggerableAreaDetector dummydet2;
	private DummyTrajectoryMoveController dummyController;
	
	@Before()
	public void before() throws DeviceException {
		
		InterfaceProvider.setTerminalPrinterForTesting(new TerminalPrinter());

		
		mockedController = mock(TrajectoryMoveController.class);
		when(mockedController.getName()).thenReturn("mockedController");
		when(mockedController.getNumberAxes()).thenReturn(3);
		when(mockedController.getLastPointAdded()).thenReturn(new Double[]{97., 98., 99.});
		mockedx = MockFactory.createMockScannable(ContinuouslyScannableViaController.class, "xcont", new String[]{"xcont"}, new String[]{}, new String[]{"%.2f"}, 5, new Double[]{1.1});
		when(mockedx.getContinuousMoveController()).thenReturn(mockedController);
		mockedy = MockFactory.createMockScannable(ContinuouslyScannableViaController.class, "ycont", new String[]{"ycont"}, new String[]{}, new String[]{"%.2f"}, 5, new Double[]{2.1});
		when(mockedy.getContinuousMoveController()).thenReturn(mockedController);
		
		mockeddet1 = createMockDetector("mockeddet1");
		when(mockeddet1.getHardwareTriggerProvider()).thenReturn(mockedController);
		mockeddet2 = createMockDetector("mockeddet2");
		when(mockeddet2.getHardwareTriggerProvider()).thenReturn(mockedController);
		
		dummydet1 = new DummyHardwareTriggerableAreaDetector("dummydet1");
		dummydet1.setHardwareTriggerProvider(mockedController);
		dummydet2 = new DummyHardwareTriggerableAreaDetector("dummydet2");
		dummydet2.setHardwareTriggerProvider(mockedController);
		
		dummyController = new DummyTrajectoryMoveController();
		dummyController.setName("dummyController");
		dummyController.setNumberAxes(3);
		
		mockedGroup = mock(ContinuouslyScannableViaController.class);
		when(mockedGroup.getContinuousMoveController()).thenReturn(mockedController);
		when(mockedGroup.getInputNames()).thenReturn(new String[]{"a", "b", "c"});
		when(mockedGroup.getExtraNames()).thenReturn(new String[]{});
		when(mockedGroup.getName()).thenReturn("abc");
		when(mockedGroup.getOutputFormat()).thenReturn(new String[]{"%f", "%f", "%f"});
		when(mockedGroup.getPosition()).thenReturn(new double[]{1, 2, 3});
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
		TestHelpers.setUpTest(TrajectoryScanLineTest.class, "singleLineScanMockScannablesAndTwoMockDetectors", true);
		InOrder inOrder = performSingleLineMockScannableAndTwoDetectors();
		
		inOrder.verify(mockeddet1).setCollectionTime(2.); // must be called in constructor!
		inOrder.verify(mockedx).setOperatingContinuously(true);
		inOrder.verify(mockedy).setOperatingContinuously(true);
		inOrder.verify(mockeddet1).setHardwareTriggering(true);
		inOrder.verify(mockeddet2).setHardwareTriggering(true);

		inOrder.verify(mockeddet1).setNumberImagesToCollect(2);
		inOrder.verify(mockeddet1).prepareForCollection();
		inOrder.verify(mockeddet1).atScanStart();
		
		inOrder.verify(mockedController).stopAndReset();
		inOrder.verify(mockedx).asynchronousMoveTo(0.);
		inOrder.verify(mockedy).asynchronousMoveTo(10.);
		inOrder.verify(mockedx).asynchronousMoveTo(1.);
		inOrder.verify(mockedy).asynchronousMoveTo(11.);

		inOrder.verify(mockedController).prepareForMove();
		// detectors armed in here in parallel and tested below
		inOrder.verify(mockedController).startMove();
		inOrder.verify(mockedController).waitWhileMoving();
		verify(mockeddet1, times(1)).collectData();
		verify(mockeddet2, times(1)).collectData();
		
	}
	
	@Test
	public void singleLineScanMockScannablesAndTwoMockDetectorsArmsDet1() throws InterruptedException, Exception {
		TestHelpers.setUpTest(TrajectoryScanLineTest.class, "singleLineScanMockScannablesAndTwoMockDetectors_ArmsDet1", true);
		InOrder inOrder = performSingleLineMockScannableAndTwoDetectors();

		inOrder.verify(mockedController).prepareForMove();
		inOrder.verify(mockeddet1).collectData();
		inOrder.verify(mockedController).startMove();
	}

	@Test
	public void singleLineScanMockScannablesAndTwoMockDetectorsArmsDet2() throws InterruptedException, Exception {
		TestHelpers.setUpTest(TrajectoryScanLineTest.class, "singleLineScanMockScannablesAndTwoMockDetectors_ArmsDet2", true);
		InOrder inOrder = performSingleLineMockScannableAndTwoDetectors();
		

		inOrder.verify(mockedController).prepareForMove();
		inOrder.verify(mockeddet2).collectData();
		inOrder.verify(mockedController).startMove();
	}
	
	private InOrder performSingleLineMockScannableAndTwoDetectors() throws InterruptedException, Exception {
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "DummyDataWriter");
		
		TrajectoryScanLine scan = new TrajectoryScanLine(new Object[]{mockedx, 0., 1., 1., mockedy, 10., 1., mockeddet1, 2., mockeddet2});
		
		scan.runScan();
		return inOrder(mockedx, mockedy, mockedController, mockeddet1, mockeddet2);
	}
	
	@Test
	public void singleLineScanWithDeferredAndTrajectoryScannableGroupAndMockDetector () throws Exception  {
		TestHelpers.setUpTest(TrajectoryScanLineTest.class, "singleLineScanWithDeferredAndTrajectoryScannableGroupAndMockDetector", true);
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "DummyDataWriter");
		
		TrajectoryScanLine scan = new TrajectoryScanLine(new Object[]{mockedGroup, new Double[]{0., 0.1, 0.2}, new Double[]{1., 1.1, 1.2}, new Double[]{1., 1., 1.}, mockeddet1, 1});
		scan.runScan();
		
		InOrder inOrder = inOrder(mockedController, mockedGroup);
		inOrder.verify(mockedController).stopAndReset();
		inOrder.verify(mockedGroup).asynchronousMoveTo(new Double[]{0., 0.1, 0.2});
		inOrder.verify(mockedGroup).asynchronousMoveTo(new Double[]{1., 1.1, 1.2});
		inOrder.verify(mockedController).startMove();
		inOrder.verify(mockedController).waitWhileMoving();

	}
	
	@Test
	public void singleLineScanWithDeferredAndTrajectoryScannableGroupWithDummyControllerAndDetectors () throws Exception  {
		TestHelpers.setUpTest(TrajectoryScanLineTest.class, "singleLineScanWithDeferredAndTrajectoryScannableGroupWithDummyControllerAndDetectors", true);
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "DummyDataWriter");
		InterfaceProvider.setTerminalPrinterForTesting(new TerminalPrinter());
		
		//mockedGroup.setContinuousMoveController(dummyController);

		dummydet1.setHardwareTriggerProvider(dummyController);
		dummydet2.setHardwareTriggerProvider(dummyController);
		when(mockedGroup.getContinuousMoveController()).thenReturn(dummyController);
		dummyController.simulate = true;
		dummyController.addIObserver(dummydet1);
		dummyController.addIObserver(dummydet2);
		dummydet1.simulate = true;
		dummydet2.simulate = true;
		
		
		
		TrajectoryScanLine scan = new TrajectoryScanLine(new Object[]{mockedGroup, new Double[]{0., 0.1, 0.2}, new Double[]{1., 1.1, 1.2}, new Double[]{1., 1., 1.}, dummydet1, 2., dummydet2, 2.});
		scan.runScan();
		System.out.println("***");
		System.out.print(dummyController);
		System.out.println("***");

	}
	
	@Test
	public void singleLineScanWithDeferredAndTrajectoryScannableGroupAndMockDetectorIntegrating () throws Exception  {
		TestHelpers.setUpTest(TrajectoryScanLineTest.class, "singleLineScanWithDeferredAndTrajectoryScannableGroupAndMockDetectorIntegrating", true);
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "DummyDataWriter");
		when(mockeddet1.integratesBetweenPoints()).thenReturn(true);
		
		TrajectoryScanLine scan = new TrajectoryScanLine(new Object[]{mockedGroup, new Double[]{0., 0.1, 0.2}, new Double[]{1., 1.1, 1.2}, new Double[]{1., 1., 1.}, mockeddet1, 1});
		scan.runScan();
		List<Map<Scannable, double[]>> triggers = scan.generateTrajectoryForDetectorsThatIntegrateBetweenTriggers();
		assertArrayEquals(new double[] {-0.5, -0.4, -0.3}, triggers.get(0).get(mockedGroup), .0001);
		assertArrayEquals(new double[] {0.5, 0.6, 0.7}, triggers.get(1).get(mockedGroup), .0001);
		assertArrayEquals(new double[] {1.5, 1.6, 1.7}, triggers.get(2).get(mockedGroup), .0001);
		
		InOrder inOrder = inOrder(mockedController, mockedGroup);
		inOrder.verify(mockedController, times(2)).stopAndReset(); // ignore the bin centres sent here
		//inOrder.verify(mockedController).stopAndReset(); // the triggers
		inOrder.verify(mockedGroup).asynchronousMoveTo(new Double[]{-0.5, -0.4, -0.3});
		inOrder.verify(mockedGroup).asynchronousMoveTo(new Double[]{0.5, 0.6000000000000001, 0.7});
		inOrder.verify(mockedGroup).asynchronousMoveTo(new Double[]{1.5, 1.6, 1.7});
		inOrder.verify(mockedController).startMove();
		inOrder.verify(mockedController).waitWhileMoving();

	}
	
	@Test
	public void singleLineScanMockScannablesAndTwoMockDetectorsIntegrating() throws InterruptedException, Exception {
		TestHelpers.setUpTest(TrajectoryScanLineTest.class, "singleLineScanMockScannablesAndTwoMockDetectorsIntegrating", true);
		InOrder inOrder = performSingleLineScanMockScannablesAndTwoMockDetectorsIntegrating();
		
		inOrder.verify(mockedx).setOperatingContinuously(true);
		inOrder.verify(mockedy).setOperatingContinuously(true);
		inOrder.verify(mockeddet1).setHardwareTriggering(true);
		inOrder.verify(mockeddet2).setHardwareTriggering(true);
		inOrder.verify(mockeddet1).setNumberImagesToCollect(2);
		inOrder.verify(mockedController).stopAndReset();
		inOrder.verify(mockedx).asynchronousMoveTo(0.);
		inOrder.verify(mockedy).asynchronousMoveTo(10.);
		inOrder.verify(mockedx).asynchronousMoveTo(1.);
		inOrder.verify(mockedy).asynchronousMoveTo(11.);
		
		inOrder.verify(mockedController).stopAndReset();
		inOrder.verify(mockedx).atLevelMoveStart();
		inOrder.verify(mockedx).asynchronousMoveTo(-.5);
		inOrder.verify(mockedy).atLevelMoveStart();
		inOrder.verify(mockedy).asynchronousMoveTo(9.5);
		inOrder.verify(mockedx).atLevelMoveStart();
		inOrder.verify(mockedx).asynchronousMoveTo(.5);
		inOrder.verify(mockedy).atLevelMoveStart();
		inOrder.verify(mockedy).asynchronousMoveTo(10.5);
		inOrder.verify(mockedx).atLevelMoveStart();
		inOrder.verify(mockedx).asynchronousMoveTo(1.5);
		inOrder.verify(mockedy).atLevelMoveStart();
		inOrder.verify(mockedy).asynchronousMoveTo(11.5);
		
		
		inOrder.verify(mockedController).prepareForMove();
		// detectors armed in here in parallel and tested below
		inOrder.verify(mockedController).startMove();
		inOrder.verify(mockedController).waitWhileMoving();
		verify(mockeddet1, times(1)).collectData();
		verify(mockeddet2, times(1)).collectData();
	}
	
	@Test
	public void singleLineScanMockScannablesAndTwoMockDetectorsIntegratingArmsDet1() throws InterruptedException, Exception {
		TestHelpers.setUpTest(TrajectoryScanLineTest.class, "singleLineScanMockScannablesAndTwoMockDetectorsIntegrating", true);
		InOrder inOrder = performSingleLineScanMockScannablesAndTwoMockDetectorsIntegrating();
		
		inOrder.verify(mockedController).prepareForMove();
		inOrder.verify(mockeddet1).collectData();
		inOrder.verify(mockedController).startMove();
	}

	@Test
	public void singleLineScanMockScannablesAndTwoMockDetectorsIntegratingArmsDet2() throws InterruptedException, Exception {
		TestHelpers.setUpTest(TrajectoryScanLineTest.class, "singleLineScanMockScannablesAndTwoMockDetectorsIntegrating", true);
		InOrder inOrder = performSingleLineScanMockScannablesAndTwoMockDetectorsIntegrating();
		
		inOrder.verify(mockedController).prepareForMove();
		inOrder.verify(mockeddet2).collectData();
		inOrder.verify(mockedController).startMove();
	}
	
	private InOrder performSingleLineScanMockScannablesAndTwoMockDetectorsIntegrating() throws InterruptedException,
			Exception, DeviceException {
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "DummyDataWriter");
		when(mockeddet1.integratesBetweenPoints()).thenReturn(true);
		when(mockeddet2.integratesBetweenPoints()).thenReturn(true);
		when(mockedx.getLevel()).thenReturn(5);
		when(mockedy.getLevel()).thenReturn(6);
		TrajectoryScanLine scan = new TrajectoryScanLine(new Object[]{mockedx, 0., 1., 1., mockedy, 10., 1., mockeddet1, 2., mockeddet2});
		
		scan.runScan();
		InOrder inOrder = inOrder(mockedx, mockedy, mockedController, mockeddet1, mockeddet2);
		
		inOrder.verify(mockeddet1).setCollectionTime(2.); // must be called in constructor!
		return inOrder;
	}
}
