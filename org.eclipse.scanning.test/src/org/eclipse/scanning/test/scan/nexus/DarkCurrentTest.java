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

import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertIndices;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertScanNotFinished;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertSignal;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertSolsticeScanGroup;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertTarget;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.junit.Before;
import org.junit.Test;

public class DarkCurrentTest extends NexusTest {

	private IWritableDetector<MandelbrotModel> detector;
	private IWritableDetector<DarkImageModel>  dark;

	@Before
	public void before() throws Exception {
		MandelbrotModel model = createMandelbrotModel();
		detector = (IWritableDetector<MandelbrotModel>)runnableDeviceService.createRunnableDevice(model);
		assertNotNull(detector);

		DarkImageModel dmodel = new DarkImageModel();
		dark =  (IWritableDetector<DarkImageModel>)runnableDeviceService.createRunnableDevice(dmodel);
		assertNotNull(dark);
	}

	/**
	 * This test fails if the chunking is not done by the detector.
	 *
	 * @throws Exception
	 */
	@Test
	public void testDarkImage() throws Exception {
		// Tell configure detector to write 1 image into a 2D scan
		IRunnableDevice<ScanModel> scanner = createGridScan(8, 5);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);

		checkNexusFile(scanner, 8, 5);
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertEquals(DeviceState.ARMED, scanner.getDeviceState());

		NXroot rootNode = getNexusRoot(scanner);

		// check that the scan points have been written correctly
		assertSolsticeScanGroup(rootNode.getEntry(), false, false, sizes);

		Collection<String> positionerNames = scanModel.getPointGenerator().iterator().next().getNames();

		// check the data for the dark detector
		checkDark(rootNode, positionerNames, sizes);

		// check the images from the mandelbrot detector
		checkImages(rootNode, positionerNames, sizes);
	}

	private void checkDark(NXroot rootNode, Collection<String> positionerNames, int... sizes) throws NexusException, ScanningException, DatasetException {
		String detectorName = dark.getName();
		NXentry entry = rootNode.getEntry();
		NXdetector detector = entry.getInstrument().getDetector(detectorName);
		assertNotNull(detector);
		IDataset ds = detector.getDataNode(NXdetector.NX_DATA).getDataset().getSlice();
		int[] shape = ds.getShape();

		double size = 1;
		for (double i : sizes)size*=i;
		size = size / (((AbstractRunnableDevice<DarkImageModel>)dark).getModel().getFrequency());
		assertEquals(shape[0], (int)size);

		checkNXdata(rootNode, detectorName, positionerNames);
	}

	private void checkImages(NXroot rootNode, Collection<String> positionerNames, int... sizes) throws NexusException, ScanningException, DatasetException {
		String detectorName = detector.getName();
		NXentry entry = rootNode.getEntry();
		NXinstrument instrument = entry.getInstrument();
		NXdetector detector = instrument.getDetector(detectorName);
		assertNotNull(detector);
		IDataset ds = detector.getDataNode(NXdetector.NX_DATA).getDataset().getSlice();

		int[] shape = ds.getShape();
		for (int i = 0; i < sizes.length; i++) assertEquals(sizes[i], shape[i]);

		// Make sure none of the numbers are NaNs. The detector
		// is expected to fill this scan with non-nulls.
        final PositionIterator it = new PositionIterator(shape);
        while(it.hasNext()) {
		int[] next = it.getPos();
		assertFalse(Double.isNaN(ds.getDouble(next)));
        }

		// Check axes
        // Demand values should be 1D
		int i = -1;
		for (String  positionerName : positionerNames) {

		    i++;
		NXpositioner positioner = instrument.getPositioner(positionerName);
		assertNotNull(positioner);
		ds = positioner.getDataNode(NXpositioner.NX_VALUE + "_set").getDataset().getSlice();
		shape = ds.getShape();
			assertEquals(1, shape.length);
		    assertEquals(sizes[i], shape[0]);

		// Actual values should be scanD
		ds = positioner.getDataNode(NXpositioner.NX_VALUE).getDataset().getSlice();
		shape = ds.getShape();
		assertArrayEquals(sizes, shape);
		}

        checkNXdata(rootNode, detectorName, positionerNames);
	}

	private void checkNXdata(NXroot rootNode, String detectorName, Collection<String> scannableNames) {
		NXentry entry = rootNode.getEntry();

		LinkedHashMap<String, List<String>> detectorDataFields = new LinkedHashMap<>();
		if (detectorName.equals("dkExmpl")) {
			detectorDataFields.put(NXdetector.NX_DATA, Arrays.asList("."));
		} else if (detectorName.equals("mandelbrot")) {
			detectorDataFields.put(NXdetector.NX_DATA, Arrays.asList("real", "imaginary"));
			detectorDataFields.put("spectrum", Arrays.asList("spectrum_axis"));
			detectorDataFields.put("value", Collections.emptyList());
		}

		Map<String, String> expectedDataGroupNamesForDevice =
				detectorDataFields.keySet().stream().collect(Collectors.toMap(Function.identity(),
						x -> detectorName + (x.equals(NXdetector.NX_DATA) ? "" : "_" + x)));

		Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		List<String> dataGroupNamesForDevice = nxDataGroups.keySet().stream()
				.filter(name -> name.startsWith(detectorName)).collect(Collectors.toList());
		assertEquals(detectorDataFields.size(), dataGroupNamesForDevice.size());
		assertTrue(dataGroupNamesForDevice.containsAll(
				expectedDataGroupNamesForDevice.values()));

		for (String dataFieldName : expectedDataGroupNamesForDevice.keySet()) {
			String nxDataGroupName = expectedDataGroupNamesForDevice.get(dataFieldName);
			NXdata nxData = nxDataGroups.get(nxDataGroupName);
			assertNotNull(nxData);

			// check the default data field for the NXdata group
			String sourceFieldName = nxDataGroupName.equals(detectorName) ? NXdetector.NX_DATA :
				nxDataGroupName.substring(nxDataGroupName.indexOf('_') + 1);
			assertSignal(nxData, sourceFieldName);

			assertSame(nxData.getDataNode(sourceFieldName),
					entry.getInstrument().getDetector(detectorName).getDataNode(sourceFieldName));
			assertTarget(nxData, sourceFieldName, rootNode, "/entry/instrument/" + detectorName
					+ "/" + sourceFieldName);

			// append _value_demand to each name in list
			List<String> expectedAxesNames = Stream.concat(
					scannableNames.stream().map(x -> x + "_value_set"),
					detectorDataFields.get(sourceFieldName).stream()).collect(Collectors.toList());
			// add placeholder value "." for each additional dimension

			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));
			int[] defaultDimensionMappings = IntStream.range(0, scannableNames.size()).toArray();

			// check the value_demand and value fields for each scannable
			int i = -1;
			for (String  positionerName : scannableNames) {

			    i++;
				NXpositioner positioner = entry.getInstrument().getPositioner(positionerName);

				// check value_demand data node
				String demandFieldName = positionerName + "_" + NXpositioner.NX_VALUE + "_set";
				assertSame(nxData.getDataNode(demandFieldName),
						positioner.getDataNode("value_set"));
				assertIndices(nxData, demandFieldName, i);
				assertTarget(nxData, demandFieldName, rootNode, "/entry/instrument/" + positionerName
						+ "/value_set");

				// check value data node
				String valueFieldName = positionerName + "_" + NXpositioner.NX_VALUE;
				assertSame(nxData.getDataNode(valueFieldName),
						positioner.getDataNode(NXpositioner.NX_VALUE));
				assertIndices(nxData, valueFieldName, defaultDimensionMappings);
				assertTarget(nxData, valueFieldName, rootNode, "/entry/instrument/" + positionerName
						+ "/" + NXpositioner.NX_VALUE);
			}
		}
	}

	private IRunnableDevice<ScanModel> createGridScan(int... size) throws Exception {

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
		scanModel.setDetectors(detector, dark);

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+scanModel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = runnableDeviceService.createRunnableDevice(scanModel, null);

		final IPointGenerator<?> fgen = pointGen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException{
				System.out.println("Running acquisition scan of size "+fgen.size());
			}
		});

		return scanner;
	}

}