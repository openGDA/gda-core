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

import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_FILENAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_PATH;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_RANK;
import static org.eclipse.scanning.example.malcolm.DummyMalcolmDevice.FILE_EXTENSION_HDF5;
import static org.eclipse.scanning.example.malcolm.DummyMalcolmDevice.UNIQUE_KEYS_DATASET_PATH;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.IMultipleNexusDevice;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.StaticPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDatasetModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDetectorModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test uses the RunnableDeviceService to create a {@link DummyMalcolmDevice}
 * and run it. Creates nexus files according to the {@link DummyMalcolmModel}.
 *
 * @author Matt Taylor
 * @author Matthew Dickie
 *
 */
public class DummyMalcolmDeviceTest extends NexusTest {

	private File malcolmOutputDir;

	@Before
	public void setUp() throws Exception {
		// create a temp directory for the dummy malcolm device to write hdf files into
		malcolmOutputDir = Files.createTempDirectory(DummyMalcolmDeviceTest.class.getSimpleName()).toFile();
		malcolmOutputDir.deleteOnExit();
	}

	@After
	public void teardown() throws Exception {
		// delete the temp directory and all its files
		for (File file : malcolmOutputDir.listFiles()) {
			file.delete();
		}
		malcolmOutputDir.delete();
	}

	private IPointGenerator<?> getGenerator(int... size) throws GeneratorException {
		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setxAxisName("stage_x");
		gmodel.setxAxisPoints(size[size.length - 1]);
		gmodel.setyAxisName("stage_y");
		gmodel.setyAxisPoints(size[size.length - 2]);
		gmodel.setBoundingBox(new BoundingBox(0, 0, 3, 3));

		IPointGenerator<?> gen = pointGenService.createGenerator(gmodel);

		IPointGenerator<?>[] gens = new IPointGenerator<?>[size.length - 1];
		// We add the outer scans, if any
		if (size.length > 2) {
			for (int dim = size.length - 3; dim > -1; dim--) {
				final AxialStepModel model;
				if (size[dim] - 1 > 0) {
					model = new AxialStepModel("neXusScannable" + (dim + 1), 10, 20,
							9.99d / (size[dim] - 1));
				} else {
					// Will generate one value at 10
					model = new AxialStepModel("neXusScannable" + (dim + 1), 10, 20, 30);
				}
				final IPointGenerator<?> step = pointGenService.createGenerator(model);
				gens[dim] = step;
			}
		}
		gens[size.length - 2] = gen;

		gen = pointGenService.createCompoundGenerator(gens);

		return gen;
	}

	public static DummyMalcolmModel createModel() {
		DummyMalcolmModel model = new DummyMalcolmModel();

		DummyMalcolmDetectorModel det1Model = new DummyMalcolmDetectorModel();
		det1Model.setName("detector");
		det1Model.addDataset(new DummyMalcolmDatasetModel("detector", 2, Double.class));
		det1Model.addDataset(new DummyMalcolmDatasetModel("sum", 1, Double.class));

		DummyMalcolmDetectorModel det2Model = new DummyMalcolmDetectorModel();
		det2Model.setName("detector2");
		det2Model.addDataset(new DummyMalcolmDatasetModel("detector", 2, Double.class));

		model.setDetectorModels(Arrays.asList(det1Model, det2Model));
		model.setAxesToMove(Arrays.asList("stage_x", "stage_y" ));
		model.setMonitorNames(Arrays.asList("i0"));

		return model;
	}

	@Test
	public void testDummyMalcolmNexusFiles() throws Exception {
		DummyMalcolmModel model = createModel();
		IMalcolmDevice malcolmDevice = (IMalcolmDevice) (IRunnableDevice<?>) runnableDeviceService.createRunnableDevice(model, false);
		// Cannot set the generator from @PreConfigure in this unit test.
		((AbstractMalcolmDevice) malcolmDevice).setPointGenerator(getGenerator(2, 2));// Generator isn't actually used by the test malcolm device
		((AbstractMalcolmDevice) malcolmDevice).setOutputDir(malcolmOutputDir.getAbsolutePath());
		int scanRank = 2;
		assertNotNull(malcolmDevice);
		malcolmDevice.configure(model);

		malcolmDevice.run(new StaticPosition());

		// Check file has been written with some data
		checkMalcolmNexusFiles(malcolmDevice, scanRank);
	}

	@Test
	public void testMalcolmNexusObjects() throws Exception {
		DummyMalcolmModel model = createModel();
		IMalcolmDevice malcolmDevice = (IMalcolmDevice) (IRunnableDevice<?>) runnableDeviceService.createRunnableDevice(model, false);
		int scanRank = 3;
		// Cannot set the generator from @PreConfigure in this unit test.
		((AbstractMalcolmDevice) malcolmDevice).setPointGenerator(getGenerator(2, 2, 2));// Generator isn't actually used by the test malcolm device
		((AbstractMalcolmDevice) malcolmDevice).setOutputDir(malcolmOutputDir.getAbsolutePath());
		malcolmDevice.configure(model);

		NexusScanInfo nexusScanInfo = new NexusScanInfo();
		nexusScanInfo.setRank(scanRank);
		List<NexusObjectProvider<?>> nexusProviders = ((IMultipleNexusDevice) malcolmDevice).getNexusProviders(nexusScanInfo);

		checkNexusObjectProviders(nexusProviders, model, scanRank);
	}

	private NXentry getNexusEntry(String filePath) throws Exception {
		INexusFileFactory fileFactory = org.eclipse.dawnsci.nexus.ServiceHolder
				.getNexusFileFactory();
		try (NexusFile nf = fileFactory.newNexusFile(filePath)) {
				nf.openToRead();
			TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
			NXroot root = (NXroot) nexusTree.getGroupNode();
			return root.getEntry();
		}
	}

	private void checkMalcolmNexusFiles(IMalcolmDevice malcolmDevice, int scanRank)
			throws MalcolmDeviceException, Exception {
		MalcolmTable table = malcolmDevice.getDatasets();
		Map<String, NXentry> nexusEntries = new HashMap<>();
		for (Map<String, Object> datasetRow : table) {
			String filename = (String) datasetRow.get(DATASETS_TABLE_COLUMN_FILENAME);

			// load the nexus entry for the file (may be cached from a previous dataset)
			NXentry entry = nexusEntries.get(filename);
			if (entry == null) {
				entry = getNexusEntry(malcolmDevice.getOutputDir() + "/" + filename);
				nexusEntries.put(filename, entry);
			}
			assertNotNull(entry);

			String path = (String) datasetRow.get(DATASETS_TABLE_COLUMN_PATH);

			String[] pathSegments = path.split("/");
			assertEquals("", pathSegments[0]); // first element is empty as path starts with '/'
			assertEquals("entry", pathSegments[1]);
			// find the parent group
			GroupNode groupNode = entry;
			for (int i = 2; i < pathSegments.length - 1; i++) {
				groupNode = groupNode.getGroupNode(pathSegments[i]);
			}
			assertNotNull(groupNode);

			// check the datanode is not null and has the expected rank
			DataNode dataNode = groupNode.getDataNode(pathSegments[pathSegments.length - 1]);
			assertNotNull(dataNode);

			int datasetRank = ((Integer) datasetRow.get(DATASETS_TABLE_COLUMN_RANK)).intValue();
			assertEquals(datasetRank, dataNode.getRank());

			// assert that the uniquekeys dataset is present
			String[] uniqueKeysPathSegments = UNIQUE_KEYS_DATASET_PATH.split("/");
			NXcollection ndAttributesCollection = entry.getCollection(uniqueKeysPathSegments[2]);
			assertNotNull(ndAttributesCollection);
			DataNode uniqueKeysDataNode = ndAttributesCollection.getDataNode(uniqueKeysPathSegments[3]);
			assertNotNull(uniqueKeysDataNode);
			assertEquals(scanRank, uniqueKeysDataNode.getRank());
		}
	}

	private void checkNexusObjectProviders(List<NexusObjectProvider<?>> nexusProviders,
			DummyMalcolmModel model, int scanRank) {
		// convert list into a map keyed by name
		final Map<String, NexusObjectProvider<?>> nexusObjectMap = nexusProviders.stream().collect(
				Collectors.toMap(n -> n.getName(), Function.identity()));
		for (IMalcolmDetectorModel detectorModel : model.getDetectorModels()) {
			final String deviceName = detectorModel.getName();
			final NexusObjectProvider<?> nexusProvider = nexusObjectMap.get(deviceName);
			final NXobject nexusObject = nexusProvider.getNexusObject();
			assertNotNull(nexusProvider);
			assertNotNull(nexusObject);
			final String expectedFileName = malcolmOutputDir.getName() + "/" + detectorModel.getName() + FILE_EXTENSION_HDF5;
			assertArrayEquals(new Object[] { expectedFileName }, nexusProvider.getExternalFileNames().toArray());

			boolean isFirst = true;
			for (DummyMalcolmDatasetModel datasetModel : ((DummyMalcolmDetectorModel) detectorModel).getDatasets()) {
				final String targetDatasetName = datasetModel.getName();
				final String linkName = isFirst ? NXdata.NX_DATA : targetDatasetName;
				final SymbolicNode externalLinkNode = nexusObject.getSymbolicNode(linkName);
				assertNotNull(externalLinkNode);
				assertEquals(scanRank + datasetModel.getRank(), nexusProvider.getExternalDatasetRank(linkName));
				assertEquals(expectedFileName, externalLinkNode.getSourceURI().toString());

				// check the nexus provider which describes how to add the device to the tree
				// (in particular how NXdata groups should be built) is configured correctly
				if (isFirst) {
					assertEquals(linkName, nexusProvider.getPrimaryDataFieldName());
					isFirst = false;
				} else {
					assertTrue(nexusProvider.getAdditionalPrimaryDataFieldNames().contains(targetDatasetName));
				}
				assertEquals(String.format("/entry/%s/%s", targetDatasetName, targetDatasetName), externalLinkNode.getPath());
			}
		}
	}

}