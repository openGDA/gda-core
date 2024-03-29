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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDiamondScanGroup;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertNXentryMetadata;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertScanNotFinished;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertTarget;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.SYSTEM_PROPERTY_NAME_ENTRY_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.InterfaceUtils;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScanMetadataTest extends NexusTest {

	private static final String EXPECTED_ENTRY_NAME = "myEntry";

	private IWritableDetector<MandelbrotModel> detector;

	@BeforeAll
	static void setupClass() {
		System.setProperty(SYSTEM_PROPERTY_NAME_ENTRY_NAME, EXPECTED_ENTRY_NAME);
	}

	@AfterAll
	static void tearDownClass() {
		System.clearProperty(SYSTEM_PROPERTY_NAME_ENTRY_NAME);
	}

	@BeforeEach
	void before() throws ScanningException, IOException {
		MandelbrotModel model = createMandelbrotModel();

		detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);
		assertThat(detector, is(Matchers.notNullValue()));
		detector.addRunListener(IRunListener.createRunPerformedListener(
				event -> System.out.println("Ran mandelbrot detector @ "+event.getPosition())));
	}

	@Test
	void testScanMetadata() throws Exception {
		final List<ScanMetadata> scanMetadata = new ArrayList<>();
		ScanMetadata entryMetadata = new ScanMetadata(MetadataType.ENTRY);
		entryMetadata.addField(NXentry.NX_TITLE, "Scan Metadata Test Entry");
//		entryMetadata.addField(NXentry.NX_EXPERIMENT_IDENTIFIER, "i05-1"); // this is now written in the nexus file by the visit id
		entryMetadata.addField(NXentry.NX_ENTRY_IDENTIFIER, 12345678l);
		scanMetadata.add(entryMetadata);

		final ScanMetadata instrumentMetadata = new ScanMetadata(MetadataType.INSTRUMENT);
		instrumentMetadata.addField("beamline", "i05-1");
		scanMetadata.add(instrumentMetadata);

		final ScanMetadata sampleMetadata = new ScanMetadata(MetadataType.SAMPLE);
		sampleMetadata.addField(NXsample.NX_CHEMICAL_FORMULA, "H2O");
		sampleMetadata.addField(NXsample.NX_TEMPERATURE, 22.0);
		sampleMetadata.addField(NXsample.NX_DESCRIPTION, "Test sample");
		scanMetadata.add(sampleMetadata);

		final IRunnableDevice<ScanModel> scanner = createGridScan(detector, scanMetadata, 2, 2);
		final NXroot root = getNexusRoot(scanner);
		assertThat(root.getAllEntry().keySet(), contains(EXPECTED_ENTRY_NAME));
		assertScanNotFinished(getNexusRoot(scanner).getEntry(EXPECTED_ENTRY_NAME));
		scanner.run(null);

		checkNexusFile(scanner, scanMetadata, 2, 2);
	}

	private IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<? extends IDetectorModel> detector,
			List<ScanMetadata> scanMetadata, int... size) throws Exception {

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
		scanModel.setDetector(detector);
		scanModel.setScanMetadata(scanMetadata);

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+scanModel.getFilePath());

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);

		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquisition scan of size " + pointGen.size())));

		return scanner;
	}

	private void checkMetadata(NXentry entry, List<ScanMetadata> scanMetadataList) {
		for (ScanMetadata scanMetadata : scanMetadataList) {
			final MetadataType type = scanMetadata.getType();
			final NXobject object = getNexusObjectForMetadataType(entry, type);

			final Map<String, Object> metadataFields = scanMetadata.getFields();
			for (String metadataFieldName : metadataFields.keySet()) {
				final Object expectedValue = scanMetadata.getFieldValue(metadataFieldName);

				final Dataset dataset = DatasetUtils.convertToDataset(object.getDataset(metadataFieldName));
				assertThat(dataset, is(notNullValue()));
				assertThat(dataset.getSize(), is(1));
				assertThat(dataset, is(instanceOf(InterfaceUtils.getInterface(expectedValue))));
				assertThat(dataset.getObjectAbs(0), is(Matchers.equalTo(expectedValue)));
			}
		}
	}

	private NXobject getNexusObjectForMetadataType(NXentry entry, MetadataType type) {
		if (type == null) {
			return entry;
		}

		switch (type) {
			case ENTRY:
				return entry;
			case INSTRUMENT:
				return entry.getInstrument();
			case SAMPLE:
				return entry.getSample();
			case USER:
				return entry.getUser();
			default:
				throw new IllegalArgumentException("Unknown metadata type " + type);
		}
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner,
			List<ScanMetadata> scanMetadata, int... sizes) throws Exception {

		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));

		final NXroot rootNode = getNexusRoot(scanner);
		assertThat(rootNode.getAllEntry().keySet(), contains(EXPECTED_ENTRY_NAME));
		final NXentry entry = rootNode.getEntry(EXPECTED_ENTRY_NAME);
		checkMetadata(entry, scanMetadata);
		// check that the scan points have been written correctly
		assertNXentryMetadata(entry);
		assertDiamondScanGroup(entry, false, false, sizes);
		assertSolsticeScanMetadata(entry, scanModel.getScanPathModel());

		final NXinstrument instrument = entry.getInstrument();

		final LinkedHashMap<String, List<String>> signalFieldAxes = new LinkedHashMap<>();
		// axis for additional dimensions of a datafield, e.g. image
		signalFieldAxes.put(NXdetector.NX_DATA, Arrays.asList("real", "imaginary"));
		signalFieldAxes.put("spectrum", Arrays.asList("spectrum_axis"));
		signalFieldAxes.put("value", emptyList());

		final String detectorName = scanModel.getDetectors().get(0).getName();
		final NXdetector detector = instrument.getDetector(detectorName);
		// map of detector data field to name of nxData group where that field
		// is the @signal field
		final Map<String, String> expectedDataGroupNames =
				signalFieldAxes.keySet().stream().collect(toMap(Function.identity(),
				x -> detectorName + (x.equals(NXdetector.NX_DATA) ? "" : "_" + x)));

		// validate the main NXdata generated by the NexusDataBuilder
		final Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		assertThat(nxDataGroups.size(), is(equalTo(signalFieldAxes.size())));
		assertThat(nxDataGroups.keySet(), containsInAnyOrder(expectedDataGroupNames.values().toArray(String[]::new)));
		for (String nxDataGroupName : nxDataGroups.keySet()) {
			final NXdata nxData = entry.getData(nxDataGroupName);

			final String sourceFieldName = nxDataGroupName.equals(detectorName) ? NXdetector.NX_DATA
					: nxDataGroupName.substring(nxDataGroupName.indexOf('_') + 1);
			assertSignal(nxData, sourceFieldName);
			// check the nxData's signal field is a link to the appropriate source data node of the detector
			DataNode dataNode = detector.getDataNode(sourceFieldName);
			IDataset dataset = dataNode.getDataset().getSlice();
			assertSame(dataNode, nxData.getDataNode(sourceFieldName));
			assertTarget(nxData, sourceFieldName, rootNode, "/" + EXPECTED_ENTRY_NAME + "/instrument/" +
					detectorName + "/" + sourceFieldName);

			// check that the other primary data fields of the detector haven't been added to this NXdata
			for (String primaryDataFieldName : signalFieldAxes.keySet()) {
				if (!primaryDataFieldName.equals(sourceFieldName)) {
					assertThat(nxData.getDataNode(primaryDataFieldName), is(nullValue()));
				}
			}

			final int[] shape = dataset.getShape();
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
			final List<String> expectedAxesNames = Stream.concat(
					scannableNames.stream().map(x -> x + "_value_set"),
					signalFieldAxes.get(sourceFieldName).stream()).collect(Collectors.toList());
			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

			final int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
			int i = -1;
			for (String  scannableName : scannableNames) {
			    i++;
				final NXpositioner positioner = instrument.getPositioner(scannableName);
				assertThat(positioner, is(notNullValue()));

				dataNode = positioner.getDataNode("value_set");
				dataset = dataNode.getDataset().getSlice();
				assertThat(dataset.getShape(), is(equalTo(new int[] { sizes[i] })));

				final String demandValueFieldName = scannableName + "_value_set";
				assertThat(dataNode, is(sameInstance(nxData.getDataNode(demandValueFieldName))));
				assertIndices(nxData, demandValueFieldName, i);
				assertTarget(nxData, demandValueFieldName, rootNode,
						"/" + EXPECTED_ENTRY_NAME + "/instrument/" + scannableName + "/value_set");

				// Actual values should be scanD
				dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
				dataset = dataNode.getDataset().getSlice();
				assertThat(dataset.getShape(), is(equalTo(sizes)));

				final String valueFieldName = scannableName + "_" + NXpositioner.NX_VALUE;
				assertThat(dataNode, is(sameInstance(nxData.getDataNode(valueFieldName))));
				assertIndices(nxData, valueFieldName, defaultDimensionMappings);
				assertTarget(nxData, valueFieldName, rootNode,
						"/"+ EXPECTED_ENTRY_NAME + "/instrument/" + scannableName + "/" + NXpositioner.NX_VALUE);
			}
		}
	}

}
