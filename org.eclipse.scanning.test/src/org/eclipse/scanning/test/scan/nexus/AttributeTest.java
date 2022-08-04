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

import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertTarget;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.io.IOException;
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

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.IScanAttributeContainer;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttributeTest extends NexusTest {

	protected IEventService eventService;

	private IWritableDetector<MandelbrotModel> detector;

	@BeforeEach
	void before() throws ScanningException, IOException {
		final MandelbrotModel model = createMandelbrotModel();

		detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);
		assertThat(detector, is(notNullValue()));
	}

	@Test
	void testName() throws Exception {
		// All scannables should have their name set ok
		final IRunnableDevice<ScanModel> scanner = createGridScan(detector, 2, 2);
		scanner.run(null);

		checkNexusFile(scanner, 2, 2);
		checkAttribute(scanner, "xNex", "name");
	}

	@Test
	void testDescription() throws Exception {
		final IScannable<?> x = connector.getScannable("xNex");
		if (!(x instanceof IScanAttributeContainer)) throw new Exception("xNex is not "+IScanAttributeContainer.class.getSimpleName());
		final IScanAttributeContainer xc = (IScanAttributeContainer) x;
		xc.setScanAttribute("description", "Reality is a shapeless unity.\nThe mind which distinguishes between aspects of this unity, sees only disunity.\nRemain unconcerned.");

		final IRunnableDevice<ScanModel> scanner = createGridScan(detector, 2, 2);
		scanner.run(null);

		checkNexusFile(scanner, 2, 2);
		checkAttribute(scanner, "xNex", "description");
	}

	@Test
	void testFred() throws Exception {
		final IScannable<?> x = connector.getScannable("xNex");
		if (!(x instanceof IScanAttributeContainer)) throw new Exception("xNex is not "+IScanAttributeContainer.class.getSimpleName());
		final IScanAttributeContainer xc = (IScanAttributeContainer)x;
		xc.setScanAttribute("fred", "Fred this is your conscious speaking.");

		final IRunnableDevice<ScanModel> scanner = createGridScan(detector, 2, 2);
		scanner.run(null);

		checkNexusFile(scanner, 2, 2);
		checkAttribute(scanner, "xNex", "fred");
	}

	@Test
	void testSetMultipleAttributes() throws Exception {
		final IScannable<?> x = connector.getScannable("xNex");
		if (!(x instanceof IScanAttributeContainer)) throw new Exception("xNex is not "+IScanAttributeContainer.class.getSimpleName());
		final IScanAttributeContainer xc = (IScanAttributeContainer)x;

		// @see http://download.nexusformat.org/doc/html/classes/base_classes/NXpositioner.html
		//description: NX_CHAR
		xc.setScanAttribute("description", "Reality is a shapeless unity.\nThe mind which distinguishes between aspects of this unity, sees only disunity.\nRemain unconcerned.");

		// value[n]: NX_NUMBER {units=NX_ANY}
		// raw_value[n]: NX_NUMBER {units=NX_ANY}
		// target_value[n]: NX_NUMBER {units=NX_ANY}
        // tolerance[n]: NX_NUMBER {units=NX_ANY}

		//soft_limit_min: NX_NUMBER {units=NX_ANY}
		xc.setScanAttribute("soft_limit_min", 1);

		// soft_limit_max: NX_NUMBER {units=NX_ANY}
		xc.setScanAttribute("soft_limit_max", 10);

		// velocity: NX_NUMBER {units=NX_ANY}
		xc.setScanAttribute("velocity", 1.2);

		// acceleration_time: NX_NUMBER {units=NX_ANY}
		xc.setScanAttribute("acceleration_time", 0.1);

		// controller_record: NX_CHAR
		xc.setScanAttribute("controller_record", "Homer Simpson");

		final IRunnableDevice<ScanModel> scanner = createGridScan(detector, 2, 2);
		scanner.run(null);

		checkNexusFile(scanner, 2, 2);

		for(String aName : xc.getScanAttributeNames()) {
		    checkAttribute(scanner, "xNex", aName);
		}
	}

	private void checkAttribute(IRunnableDevice<ScanModel> scanner, String sName, String attrName) throws Exception {
		final IScannable<?> s = connector.getScannable(sName);
		if (!(s instanceof IScanAttributeContainer)) throw new Exception(sName+" is not "+IScanAttributeContainer.class.getSimpleName());

		final IScanAttributeContainer sc = (IScanAttributeContainer) s;
		final Object attrValue = sc.getScanAttribute(attrName);

		final String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();
		final NexusFile nf = fileFactory.newNexusFile(filePath);
		nf.openToRead();

		final DataNode node = nf.getData("/entry/instrument/" + sName + "/"+attrName);
		final Dataset sData =  DatasetUtils.sliceAndConvertLazyDataset(node.getDataset());

		if ("name".equals(attrName)) {
			assertThat(sName, is(equalTo(sData.getStringAbs(0))));
		} else if (attrValue instanceof Number) {
			assertThat(((Number) attrValue).doubleValue(), is(closeTo(sData.getElementDoubleAbs(0), 1e-15)));
		} else {
			assertThat(attrValue, is(equalTo(sData.getStringAbs(0))));
		}
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));

		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();

		final LinkedHashMap<String, List<String>> signalFieldAxes = new LinkedHashMap<>();
		// axis for additional dimensions of a datafield, e.g. image
		signalFieldAxes.put(NXdetector.NX_DATA, Arrays.asList("real", "imaginary"));
		signalFieldAxes.put("spectrum", Arrays.asList("spectrum_axis"));
		signalFieldAxes.put("value", Collections.emptyList());

		final String detectorName = scanModel.getDetectors().get(0).getName();
		final NXdetector detector = instrument.getDetector(detectorName);
		// map of detector data field to name of nxData group where that field is the @signal field
		final Map<String, String> expectedDataGroupNames =
				signalFieldAxes.keySet().stream().collect(Collectors.toMap(Function.identity(),
				x -> detectorName + (x.equals(NXdetector.NX_DATA) ? "" : "_" + x)));

		// validate the main NXdata generated by the NexusDataBuilder
		final Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		assertThat(nxDataGroups.size(), is(equalTo(signalFieldAxes.size())));
		assertThat(nxDataGroups.keySet(), containsInAnyOrder(expectedDataGroupNames.values().toArray(String[]::new)));
		for (String nxDataGroupName : nxDataGroups.keySet()) {
			final NXdata nxData = entry.getData(nxDataGroupName);

			final String sourceFieldName = nxDataGroupName.equals(detectorName) ? NXdetector.NX_DATA :
				nxDataGroupName.substring(nxDataGroupName.indexOf('_') + 1);
			assertSignal(nxData, sourceFieldName);
			// check the nxData's signal field is a link to the appropriate source data node of the detector
			DataNode dataNode = detector.getDataNode(sourceFieldName);
			IDataset dataset = dataNode.getDataset().getSlice();

			assertThat(nxData.getDataNode(sourceFieldName), is(sameInstance(dataNode)));

			int[] shape = dataset.getShape();
			for (int i = 0; i < sizes.length; i++)
				assertThat(shape[i], is(equalTo(sizes[i])));

			// Make sure none of the numbers are NaNs. The detector
			// is expected to fill this scan with non-nulls.
			final PositionIterator it = new PositionIterator(shape);
			while (it.hasNext()) {
				int[] next = it.getPos();
				assertThat(Double.isNaN(dataset.getDouble(next)), is(false));
			}

			// Check axes
			final IPosition pos = scanModel.getPointGenerator().iterator().next();
			final Collection<String> scannableNames = pos.getNames();

			// Append _value_demand to each name in list, then add detector axis fields to result
			List<String> expectedAxesNames = Stream.concat(
					scannableNames.stream().map(x -> x + "_value_set"),
					signalFieldAxes.get(sourceFieldName).stream()).collect(Collectors.toList());
			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

			int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();

			int i = -1;
			for (String  scannableName : scannableNames) {
			    i++;
				// Demand values should be 1D
				final NXpositioner positioner = instrument.getPositioner(scannableName);
				assertThat(positioner, is(notNullValue()));

				dataNode = positioner.getDataNode("value_set");
				dataset = dataNode.getDataset().getSlice();
				shape = dataset.getShape();
				assertThat(shape, is(equalTo(new int[] { sizes[i] })));

				String nxDataFieldName = scannableName + "_value_set";
				assertThat(nxData.getDataNode(nxDataFieldName), is(sameInstance(dataNode)));
				assertIndices(nxData, nxDataFieldName, i);
				assertTarget(nxData, nxDataFieldName, rootNode, "/entry/instrument/" + scannableName + "/value_set");

				// Actual values should be scanD
				dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
				dataset = dataNode.getDataset().getSlice();
				shape = dataset.getShape();
				assertThat(shape, is(equalTo(sizes)));

				nxDataFieldName = scannableName + "_" + NXpositioner.NX_VALUE;
				assertThat(nxData.getDataNode(nxDataFieldName), is(sameInstance(dataNode)));
				assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
				assertTarget(nxData, nxDataFieldName, rootNode,
						"/entry/instrument/" + scannableName + "/"
								+ NXpositioner.NX_VALUE);
			}
		}
	}

	private IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<? extends IDetectorModel> detector, int... size) throws Exception {
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
		scanModel.setDetector(detector);
		scanModel.setScanPathModel(compoundModel);

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+scanModel.getFilePath());

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);

		final IPointGenerator<?> fgen = pointGen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquisition scan of size "+fgen.size())));

		return scanner;
	}

}
