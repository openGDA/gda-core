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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.notNullValue;

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
import org.eclipse.dawnsci.nexus.INexusDevice;
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
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.StaticPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDatasetModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDetectorModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
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

	private IPointGenerator<CompoundModel> getGenerator(int... size) throws GeneratorException {
		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setxAxisName("stage_x");
		gmodel.setxAxisPoints(size[size.length - 1]);
		gmodel.setyAxisName("stage_y");
		gmodel.setyAxisPoints(size[size.length - 2]);
		gmodel.setBoundingBox(new BoundingBox(0, 0, 3, 3));

		final CompoundModel cModel = createNestedStepScans(2, size);
		cModel.addModel(gmodel);

		return pointGenService.createCompoundGenerator(cModel);
	}

	public static DummyMalcolmModel createModel() {
		final DummyMalcolmModel model = new DummyMalcolmModel();

		final DummyMalcolmDetectorModel det1Model = new DummyMalcolmDetectorModel();
		det1Model.setName("detector");
		det1Model.addDataset(new DummyMalcolmDatasetModel("detector", 2, Double.class));
		det1Model.addDataset(new DummyMalcolmDatasetModel("sum", 1, Double.class));

		final DummyMalcolmDetectorModel det2Model = new DummyMalcolmDetectorModel();
		det2Model.setName("detector2");
		det2Model.addDataset(new DummyMalcolmDatasetModel("detector", 2, Double.class));

		model.setDetectorModels(Arrays.asList(det1Model, det2Model));
		model.setAxesToMove(Arrays.asList("stage_x", "stage_y" ));
		model.setMonitorNames(Arrays.asList("i0"));

		return model;
	}

	@Test
	public void testDummyMalcolmNexusFiles() throws Exception {
		final DummyMalcolmModel model = createModel();
		final IMalcolmDevice malcolmDevice = TestDetectorHelpers.createDummyMalcolmDetector();
		// Cannot set the generator from @PreConfigure in this unit test.
		((AbstractMalcolmDevice) malcolmDevice).setPointGenerator(getGenerator(2, 2));// Generator isn't actually used by the test malcolm device
		((AbstractMalcolmDevice) malcolmDevice).setOutputDir(malcolmOutputDir.getAbsolutePath());
		final int scanRank = 2;
		assertThat(malcolmDevice, is(notNullValue()));
		malcolmDevice.configure(model);

		malcolmDevice.run(new StaticPosition());

		// Check file has been written with some data
		checkMalcolmNexusFiles(malcolmDevice, scanRank);
	}

	@Test
	public void testMalcolmNexusObjects() throws Exception {
		final DummyMalcolmModel model = createModel();
		final IMalcolmDevice malcolmDevice = TestDetectorHelpers.createDummyMalcolmDetector();
		int scanRank = 3;
		// Cannot set the generator from @PreConfigure in this unit test.
		((AbstractMalcolmDevice) malcolmDevice).setPointGenerator(getGenerator(2, 2, 2));// Generator isn't actually used by the test malcolm device
		((AbstractMalcolmDevice) malcolmDevice).setOutputDir(malcolmOutputDir.getAbsolutePath());
		malcolmDevice.configure(model);

		final NexusScanInfo nexusScanInfo = new NexusScanInfo();
		nexusScanInfo.setRank(scanRank);
		final List<NexusObjectProvider<?>> nexusProviders = ((INexusDevice<?>) malcolmDevice).getNexusProviders(nexusScanInfo);

		checkNexusObjectProviders(nexusProviders, model, scanRank);
	}

	private NXentry getNexusEntry(String filePath) throws Exception {
		final INexusFileFactory fileFactory = org.eclipse.dawnsci.nexus.ServiceHolder.getNexusFileFactory();
		try (NexusFile nf = fileFactory.newNexusFile(filePath)) {
			nf.openToRead();
			final TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
			final NXroot root = (NXroot) nexusTree.getGroupNode();
			return root.getEntry();
		}
	}

	private void checkMalcolmNexusFiles(IMalcolmDevice malcolmDevice, int scanRank)
			throws MalcolmDeviceException, Exception {
		final MalcolmTable table = malcolmDevice.getDatasets();
		final Map<String, NXentry> nexusEntries = new HashMap<>();
		for (Map<String, Object> datasetRow : table) {
			final String filename = (String) datasetRow.get(DATASETS_TABLE_COLUMN_FILENAME);

			// load the nexus entry for the file (may be cached from a previous dataset)
			NXentry entry = nexusEntries.get(filename);
			if (entry == null) {
				entry = getNexusEntry(malcolmDevice.getOutputDir() + "/" + filename);
				nexusEntries.put(filename, entry);
			}
			assertThat(entry, is(notNullValue()));

			final String path = (String) datasetRow.get(DATASETS_TABLE_COLUMN_PATH);

			final String[] pathSegments = path.split("/");
			assertThat(pathSegments[0], isEmptyString()); // first element is empty as path starts with '/'
			assertThat(pathSegments[1], is(equalTo("entry")));
			// find the parent group
			GroupNode groupNode = entry;
			for (int i = 2; i < pathSegments.length - 1; i++) {
				groupNode = groupNode.getGroupNode(pathSegments[i]);
			}
			assertThat(groupNode, is(notNullValue()));

			// check the datanode is not null and has the expected rank
			final DataNode dataNode = groupNode.getDataNode(pathSegments[pathSegments.length - 1]);
			assertThat(dataNode, is(notNullValue()));

			int datasetRank = ((Integer) datasetRow.get(DATASETS_TABLE_COLUMN_RANK)).intValue();
			assertThat(dataNode.getRank(), is(datasetRank));

			// assert that the uniquekeys dataset is present
			final String[] uniqueKeysPathSegments = UNIQUE_KEYS_DATASET_PATH.split("/");
			final NXcollection ndAttributesCollection = entry.getCollection(uniqueKeysPathSegments[2]);
			assertThat(ndAttributesCollection, is(notNullValue()));
			final DataNode uniqueKeysDataNode = ndAttributesCollection.getDataNode(uniqueKeysPathSegments[3]);
			assertThat(uniqueKeysDataNode, is(notNullValue()));
			assertThat(uniqueKeysDataNode.getRank(), is(scanRank));
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
			assertThat(nexusProvider, is(notNullValue()));
			assertThat(nexusObject, is(notNullValue()));
			final String expectedFileName = malcolmOutputDir.getName() + "/" + detectorModel.getName() + FILE_EXTENSION_HDF5;
			assertThat(nexusProvider.getExternalFileNames().toArray(), is(equalTo(new Object[] { expectedFileName } )));

			boolean isFirst = true;
			for (DummyMalcolmDatasetModel datasetModel : ((DummyMalcolmDetectorModel) detectorModel).getDatasets()) {
				final String targetDatasetName = datasetModel.getName();
				final String linkName = isFirst ? NXdata.NX_DATA : targetDatasetName;
				final SymbolicNode externalLinkNode = nexusObject.getSymbolicNode(linkName);
				assertThat(externalLinkNode, is(notNullValue()));
				assertThat(nexusProvider.getExternalDatasetRank(linkName), is(scanRank + datasetModel.getRank()));

				assertThat(externalLinkNode.getSourceURI().toString(), is(equalTo(expectedFileName)));

				// check the nexus provider which describes how to add the device to the tree
				// (in particular how NXdata groups should be built) is configured correctly
				if (isFirst) {
					assertThat(nexusProvider.getPrimaryDataFieldName(), is(equalTo(linkName)));
					isFirst = false;
				} else {
					assertThat(nexusProvider.getAdditionalPrimaryDataFieldNames().contains(targetDatasetName), is(true));
				}
				assertThat(externalLinkNode.getPath(), is(equalTo(String.format("/entry/%s/%s", targetDatasetName, targetDatasetName))));
			}
		}
	}

}
