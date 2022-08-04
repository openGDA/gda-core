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

import static java.util.stream.Collectors.toList;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertScanNotFinished;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MandelbrotExampleTest extends NexusTest {

	private static IWritableDetector<MandelbrotModel> detector;

	@BeforeEach
	void before() throws Exception {
		final MandelbrotModel model = createMandelbrotModel();
		detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);
		assertThat(detector, is(notNullValue()));
	}

	@Test
	void test2ConsecutiveSmallScans() throws Exception {
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, false, 2, 2);
		scanner.run(null);

		scanner = createGridScan(detector, output, false, 2, 2);
		scanner.run(null);
	}

	/**
	 * This test fails if the chunking is not done by the detector.
	 *
	 * @throws Exception
	 */
	@Test
	void testWriteTime2Dvs3D() throws Exception {

		// Tell configure detector to write 1 image into a 2D scan
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, false, 3, 2);
		ScanModel mod = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		IPosition first = mod.getPointGenerator().iterator().next();
		detector.run(first);

		long before = System.currentTimeMillis();
		detector.write(first);
		long after = System.currentTimeMillis();
		long diff2 = (after-before);
		System.out.println("Writing 1 image in 3D stack took: "+diff2+" ms");

		File soutput = File.createTempFile("test_mandel_nexus", ".nxs");
		soutput.deleteOnExit();
		scanner = createGridScan(detector, soutput, false, 10, 3, 2);
		mod = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		first = mod.getPointGenerator().iterator().next();
		detector.run(first);

		before = System.currentTimeMillis();
		detector.write(first);
		after = System.currentTimeMillis();
		long diff3 = (after-before);
		System.out.println("Writing 1 image in 4D stack took: "+diff3+" ms");

		assertThat((double) diff3, is(lessThan(Math.max(20, diff2 * 1.5))));
	}

	@Test
	void test2DGridScan() throws Exception {
		testGridScan(false, 3, 2);
	}

	@Test
	void test2DSnakeGridScan() throws Exception {
		testGridScan(true, 3, 2);
	}

	@Test
	void test2DSnakeWithOddNumberOfLinesGridScan() throws Exception {
		testGridScan(true, 7, 5);
	}

	@Test
	void test2DGridScanWithCircularRegion() throws Exception {
		testGridScanWithCircularRegion(false, 3, 5);
	}

	@Test
	void test2DSnakeGridScanWithCircularRegion() throws Exception {
		testGridScanWithCircularRegion(true, 3, 5);
	}

	@Test
	void test3DSpiralScan() throws Exception {
		final IRunnableDevice<ScanModel> scanner = createSpiralScan(detector, output); // Outer scan of another scannable, for instance temp.
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);
		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);

		final NXdata nXdata = nxDataGroups.get(nxDataGroups.keySet().iterator().next());
		//3d spiral, outer should be 0, inner should both be 1
		Attribute att = nXdata.getAttribute("neXusScannable1_value_set_indices");
		String e = att.getFirstElement();
		assertThat(Integer.parseInt(e), is(0));

		att = nXdata.getAttribute("xNex" + "_value_set_indices");
		e = att.getFirstElement();
		assertThat(Integer.parseInt(e), is(1));

		att = nXdata.getAttribute("yNex" + "_value_set_indices");
		e = att.getFirstElement();
		assertThat(Integer.parseInt(e), is(1));
	}

	@Test
	void test2DGridScanNoImage() throws Exception {
		detector.getModel().setSaveImage(false);
		try {
			final IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, false, new int[]{8,5}); // Outer scan of another scannable, for instance temp.
			assertScanNotFinished(getNexusRoot(scanner).getEntry());
			scanner.run(null);

			final NXroot rootNode = getNexusRoot(scanner);
			final NXentry entry = rootNode.getEntry();
			final Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);

			final List<DataNode> signalFields = nxDataGroups.values().stream().map(data -> data.getDataNode(data.getAttributeSignal())).collect(toList());
			assertThat(signalFields.stream().anyMatch(f -> f.getDataset().getRank() == 4), is(false));
		} finally {
			detector.getModel().setSaveImage(true);
		}

	}

	@Test
	void test3DGridScan() throws Exception {
		testGridScan(false, 3, 2, 5);
	}

	@Test
	void test3DSnakeGridScanEvenNumRows() throws Exception {
		testGridScan(true, 3, 2, 5);
	}

	@Test
	void test3DSnakeGridScanOddNumRows() throws Exception {
		testGridScan(true, 3, 3, 5);
	}

	@Test
	void test3DGridScanWithCircularRegion() throws Exception {
		testGridScanWithCircularRegion(false, 3, 3, 5);
	}

	@Test
	void test3DSnakeGridScanWithCircularRegion() throws Exception {
		testGridScanWithCircularRegion(true, 3, 3, 5);
	}

	@Test
	void test4DSnakeGridScanWithCircularRegion() throws Exception {
		testGridScanWithCircularRegion(true, 2, 3, 3, 5);
	}

	// TODO Why does this not pass?
	//@Test
	public void test3DGridScanLarge() throws Exception {
		long before = System.currentTimeMillis();
		testGridScan(false,300,2, 5);
		long after = System.currentTimeMillis();
		long diff  = after-before;
		assertThat(diff, is(lessThan(20000l)));
	}

	@Test
	void test4DGridScan() throws Exception {
		testGridScan(false,3,3,2, 2);
	}

	@Test
	void test5DGridScan() throws Exception {
		testGridScan(false, 2, 2, 2, 2, 2);
	}

	@Test
	void test8DGridScan() throws Exception {
		testGridScan(false, 2, 2, 2, 2, 2, 2, 2, 2);
	}

	private void testGridScan(boolean snake, int... shape) throws Exception {
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, snake, shape); // Outer scan of another scannable, for instance temp.
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);

		// Check we reached ready (it will normally throw an exception on error)
		checkNexusFile(scanner, snake, shape); // Step model is +1 on the size
	}

	private void testGridScanWithCircularRegion(boolean snake, int... shape) throws Exception {
		final IROI region = new CircularROI(2, 1, 1);

		final int circularShape = 7; // size of inner grid scan in circular region
		// note: this assumes the last two shape dimensions are 3 and 5

		final IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, region, snake, shape);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);

		final int[] datasetShape = new int[shape.length - 1];
		System.arraycopy(shape, 0, datasetShape, 0, shape.length - 2);
		datasetShape[datasetShape.length - 1] = circularShape;

		checkNexusFile(scanner, snake, true, datasetShape);
	}

}
