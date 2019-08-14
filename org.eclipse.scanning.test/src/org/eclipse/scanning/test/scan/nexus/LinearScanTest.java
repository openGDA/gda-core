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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LinearScanTest extends BrokerTest{

	private IRunnableDeviceService runnableDeviceService;
	private IPointGeneratorService pointGenService;
	private ILoaderService loaderService;

	private IPublisher<ScanBean> publisher;
	private ISubscriber<EventListener> subscriber;
	private File tmp;

	@Before
	public void setup() throws Exception {
		ServiceTestHelper.setupServices();
		ServiceTestHelper.registerTestDevices();

		runnableDeviceService = ServiceTestHelper.getRunnableDeviceService();
		pointGenService = ServiceTestHelper.getPointGeneratorService();
		loaderService = ServiceTestHelper.getLoaderService();

		this.publisher = ServiceTestHelper.getEventService().createPublisher(uri, EventConstants.STATUS_TOPIC);
		this.subscriber = ServiceTestHelper.getEventService().createSubscriber(uri, EventConstants.STATUS_TOPIC);

		tmp = File.createTempFile("testAScan_", ".nxs");
		tmp.deleteOnExit();
	}

	@After
	public void clean() throws Exception {
        this.publisher.disconnect();
        this.subscriber.disconnect();
        tmp.delete();
	}

	@Test
	public void testSimpleLineScan() throws Exception {
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});
		doScan(roi, 1, new int[]{10,64,64}, create1DModel(10));
	}

	@Test
	public void testWrappedLineScan() throws Exception {
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});
		doScan(roi, 2, new int[]{4,10,64,64}, new StepModel("T", 290, 300, 3), create1DModel(10));
	}

	@Test
	public void testBigWrappedLineScan() throws Exception {
		LinearROI roi = new LinearROI(new double[]{0,0}, new double[]{3,3});
		doScan(roi, 5, new int[]{2,2,2,2,3,64,64}, new StepModel("T1", 290, 291, 1),
				                                   new StepModel("T2", 290, 291, 1),
                                                   new StepModel("T3", 290, 291, 1),
                                                   new StepModel("T4", 290, 291, 1),
                                                    create1DModel(3));
	}

	private IScanPathModel create1DModel(int size) {
        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        model.setPoints(size);
        model.setxAxisName("xNex");
        model.setyAxisName("yNex");
        return model;
	}

	@Test
	public void testSimpleGridScan() throws Exception {

		doScan(null, 2, new int[]{5,5,64,64}, createGridModel());
	}

	@Test
	public void testWrappedGridScan() throws Exception {
		doScan(null, 3, new int[]{4,5,5,64,64}, new StepModel("T", 290, 300, 3), createGridModel());
	}

	@Test
	public void testBigWrappedGridScan() throws Exception {
		doScan(null,6, new int[]{2,2,2,2,2,2,64,64}, new StepModel("T1", 290, 291, 1),
										             new StepModel("T2", 290, 291, 1),
										             new StepModel("T3", 290, 291, 1),
										             new StepModel("T4", 290, 291, 1),
										             createGridModel(2,2));
	}

	private GridModel createGridModel(int... size) {
		if (size==null)    size = new int[]{5,5};
		if (size.length<2) size = new int[]{5,5};
		if (size.length>2) throw new IllegalArgumentException("Two values or no values should be provided!");

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		GridModel model = new GridModel();
		model.setyAxisPoints(size[0]);
		model.setxAxisPoints(size[1]);
		model.setBoundingBox(box);
		model.setxAxisName("xNex");
		model.setyAxisName("yNex");
		return model;
	}

	private void doScan(LinearROI roi, int scanRank, int[]dshape, IScanPathModel... models) throws Exception {
		IRunnableDevice<ScanModel> scanner = createTestScanner(roi, models);
		final List<IPosition> positions = new ArrayList<>();
		subscriber.addListener(new IScanListener() {
			@Override
			public void scanEventPerformed(ScanEvent evt) {
				final IPosition pos = evt.getBean().getPosition();
				positions.add(pos);
			}
		});

		scanner.run(null);

		Thread.sleep(100);
		int size = ((IPointGenerator)scanner.getModel().getPointGenerator()).size();
		assertEquals("The model size was "+size+" and the points found were "+positions.size(), size, positions.size());

		for (IPosition iPosition : positions) {
			assertEquals(scanRank, iPosition.getScanRank());
		}

		final IDataHolder holder = loaderService.getData(scanner.getModel().getFilePath(), null);
		final ILazyDataset mdata = holder.getLazyDataset("/entry/instrument/detector/data");
		assertTrue(mdata!=null);
		assertArrayEquals(dshape, mdata.getShape());
	}

	private IRunnableDevice<ScanModel> createTestScanner(IROI roi,  IScanPathModel... models) throws Exception {
		// Configure a detector with a collection time.
		MandelbrotModel dmodel = new MandelbrotModel();
		dmodel.setExposureTime(0.01);
		dmodel.setName("detector");
		dmodel.setColumns(64);
		dmodel.setRows(64);
		dmodel.setRealAxisName("xNex");
		dmodel.setImaginaryAxisName("yNex");

		IRunnableDevice<MandelbrotModel>	detector = runnableDeviceService.createRunnableDevice(dmodel);

		// Generate the last model using the roi then work back up creating compounds
		final IPointGenerator<?>[] gens = new IPointGenerator[models.length];
		for (int i = 0; i < models.length; i++)  {
			if (i==models.length-1) { // Last one uses roi
				gens[i] = roi!=null ? pointGenService.createGenerator(models[i], roi) : pointGenService.createGenerator(models[i]);
			} else {
				gens[i] = pointGenService.createGenerator(models[i]);
			}
		}

		IPointGenerator<?> gen = pointGenService.createCompoundGenerator(gens);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPointGenerator(gen);
		smodel.setDetectors(detector);

		smodel.setFilePath(tmp.getAbsolutePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = runnableDeviceService.createRunnableDevice(smodel, publisher);

		return scanner;
	}

}
