/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.command;

import static org.eclipse.scanning.sequencer.analysis.ClusterProcessingRunnableDevice.PROCESSING_QUEUE_NAME;
import static org.junit.Assert.assertTrue;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.dry.DryRunProcessCreator;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.test.ScanningTestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScanRequestWithProcessingTest extends AbstractScanCommandsTest {

	public ScanRequestWithProcessingTest() {
		super(true);
	}

	@BeforeClass
	public static void createProcessingQueue() throws EventException {

		pjobQueue = eservice.createJobQueue(uri, PROCESSING_QUEUE_NAME.concat(ScanningTestUtils.JVM_UNIQUE_ID), "scisoft.operation.STATUS_TOPIC");
		// we need a runner, but it doesn't have to do anything
		pjobQueue.setRunner(new DryRunProcessCreator(0, 1, 1, 10, false));
		pjobQueue.start();
	}

	@AfterClass
	public static void disconnect()  throws Exception {
		pjobQueue.clearQueue();
		pjobQueue.clearRunningAndCompleted();
		pjobQueue.disconnect();
		servlet.getJobQueue().clearQueue();
		servlet.getJobQueue().clearRunningAndCompleted();
		servlet.disconnect();
	}


	@Test
	@Ignore // DAQ-1444 Test is currently failing
	public void testGridScanWithProcessing() throws Exception {

		String cmd = "sr = scan_request(grid(axes=('yNex', 'xNex'), start=(0, 0), stop=(3, 3), count=(2, 2), snake=False, continuous=False), "
                + "det=[detector('mandelbrot', 0.1), detector('processing', -1, detectorName='mandelbrot', processingFilePath='/tmp/sum.nxs')],"
                + "file='"+path+"' )";
		pi.exec(cmd);
		runAndCheck("sr", "mandelbrot", "processing", false, 10);
	}

	@Test
	@Ignore // DAQ-1444 Test is currently failing
	public void testGridScanWithProcessing2() throws Exception {

		String cmd = "sr = scan_request(grid(axes=('yNex', 'xNex'), start=(0, 0), stop=(3, 3), count=(2, 2), snake=False, continuous=False), "
                + "det=[detector('m', 0.1), detector('p', -1, detectorName='m', processingFilePath='/tmp/sum.nxs')],"
                + "file='"+path+"' )";
		pi.exec(cmd);
		runAndCheck("sr", "m", "p", false, 10);
	}

	@Test
	@Ignore // DAQ-1444 Test is currently failing
	public void testGridScanWithProcessing3() throws Exception {

		String cmd = "sr = scan_request(grid(axes=('yNex', 'xNex'), start=(0, 0), stop=(3, 3), count=(2, 2), snake=False, continuous=False), "
                + "det=[detector('m', 0.1), detector('processing', -1, detectorName='m', processingFilePath='/tmp/sum.nxs')],"
                + "file='"+path+"' )";
		pi.exec(cmd);
		runAndCheck("sr", "m", "processing", false, 10);
	}


	@Test(expected=Exception.class) // Should give a validation exception.
	public void testGridScanWithProcessingNoDetectorName() throws Exception {

		String cmd = "sr = scan_request(grid(axes=('yNex', 'xNex'), start=(0, 0), stop=(3, 3), count=(2, 2), snake=False, continuous=False), "
                + "det=[detector('mandelbrot', 0.1), detector('processing', -1, detectorName=None)],"
                + "file='"+path+"' )";
		pi.exec(cmd);
		runAndCheck("sr", "mandelbrot", "processing", false, 10);
	}

	@Test(expected=Exception.class) // Should give a validation exception.
	public void testGridScanWithProcessingBadDetectorName() throws Exception {

		String cmd = "sr = scan_request(grid(axes=('yNex', 'xNex'), start=(0, 0), stop=(3, 3), count=(2, 2), snake=False, continuous=False), "
                + "det=[detector('mandelbrot', 0.1), detector('processing', -1, detectorName='fred', processingFilePath='/tmp/sum.nxs')],"
                + "file='"+path+"' )";
		pi.exec(cmd);
		runAndCheck("sr", "mandelbrot", "processing", false, 10);
	}

	@Test
	@Ignore // DAQ-1444 Test is currently failing
	public void testSnakedGridScanWithProcessing() throws Exception {

		assertTrue(servlet.isConnected());

		IRunnableDevice<?> device = dservice.getRunnableDevice("processing");
		assertTrue(device.getModel()!=null);

		String cmd = "sr = scan_request(grid(axes=('yNex', 'xNex'), start=(0, 0), stop=(3, 3), count=(2, 2), snake=True), "
                + "det=[detector('mandelbrot', 0.1), detector('processing', -1, detectorName='mandelbrot', processingFilePath='/tmp/sum.nxs')],"
                + "file='"+path+"' )";
		pi.exec(cmd);
		runAndCheck("sr", "mandelbrot", "processing", false, 10);
	}

	@Test
	@Ignore // DAQ-1444 Test is currently failing
	public void testStepScanNoMscanCommand() throws Exception {

		ScanBean bean = createStepScan();
		runAndCheckNoPython(bean, 60);
	}

}