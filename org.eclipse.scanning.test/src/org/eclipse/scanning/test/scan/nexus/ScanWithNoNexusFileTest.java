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
package org.eclipse.scanning.test.scan.nexus;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.junit.Before;
import org.junit.Test;

/**
 * This test ensures that the scan still runs smoothly when no nexus file is created.
 * In particular, a bug was discovered whereby the write() methods in the detectors were
 * still called even though createNexusObject has not been. This caused a NullPointerException
 * in MandelbrotDetector as the datasets being written to were null.
 *
 * TODO: the fix was to set the writers in AcquisitionDevice to LevelRunner.createEmptyRunner().
 * However, it is arguable that it should be the detectors who check for this instead as it may
 * be possible that they have other things to do on write.
 */
public class ScanWithNoNexusFileTest extends NexusTest {

	private static IWritableDetector<MandelbrotModel> detector;

	@Before
	public void before() throws Exception {

		MandelbrotModel model = createMandelbrotModel();
		detector = (IWritableDetector<MandelbrotModel>)runnableDeviceService.createRunnableDevice(model);
		assertNotNull(detector);
	}

	@Test
	public void test2DNexusScan() throws Exception {
		int[] shape = { 2, 5 };
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, shape); // Outer scan of another scannable, for instance temp.
		scanner.run(null);
	}
	private IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<?> detector, File file, int... size) throws Exception {

		// Create scan points for a grid and make a generator
		final TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel();
		gridModel.setxAxisName("xNex");
		gridModel.setxAxisPoints(size[size.length-1]);
		gridModel.setyAxisName("yNex");
		gridModel.setyAxisPoints(size[size.length-2]);
		gridModel.setBoundingBox(new BoundingBox(0,0,3,3));

		final CompoundModel compoundModel = createNestedStepScans(2, size);
		compoundModel.addModel(gridModel);

		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		scanModel.setDetectors(detector);

		// Do not create a file to scan into, no nexus file should be written
		scanModel.setFilePath(null);
		System.out.println("File writing is not set, so no NeXus file is created.");

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = runnableDeviceService.createRunnableDevice(scanModel, null);

		final IPointGenerator<?> fgen = pointGen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException {
				System.out.println("Running acquisition scan of size "+fgen.size());
			}
		});

		return scanner;
	}

}
