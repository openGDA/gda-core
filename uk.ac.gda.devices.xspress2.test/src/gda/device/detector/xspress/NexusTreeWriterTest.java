/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.device.detector.xspress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.junit.Before;
import org.junit.Test;

import gda.TestHelpers;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.util.NexusTreeWriter;

public class NexusTreeWriterTest extends Xspress2TestBase {

	@Before
	public void setDetector() {
		setupDetector();
	}
	private NexusTreeProvider[] nexusTreeArray;

	private NexusTreeWriter runTreeWriter(String testFile, int numReadouts, int numRepetitions) throws NexusException, DatasetException, DeviceException {
		nexusTreeArray = xspress.readout(0, numReadouts-1);
		NexusTreeWriter nexusWriter = new NexusTreeWriter();
		nexusWriter.setFullpathToNexusFile(testFile);
		for(int i=0; i<numRepetitions; i++) {
			nexusWriter.addData(nexusTreeArray);
			nexusWriter.writeNexusData();
		}
		nexusWriter.closeFile();
		return nexusWriter;
	}

	@Test
	public void checkNumberOfLazyDatasets() throws Exception {
		String testScratchDir = TestHelpers.setUpTest(this.getClass(), "correctNumberLazyDatasets", true);
		String testFile = Paths.get(testScratchDir, "xspress2Data.nxs").toAbsolutePath().toString();

		NexusTreeWriter nexusWriter = runTreeWriter(testFile, 5, 1);
		List<ILazyWriteableDataset> lazyDatasets = nexusWriter.getLazyDatasets();

		// Detector name is correct
		assertEquals(xspress.getName(), nexusWriter.getDetectorName());

		// Check that number of lazy datasets extracted matches number of child nodes in tree
		int numChildNodes =  nexusTreeArray[0].getNexusTree().getNode(xspress.getName()).getNumberOfChildNodes();
		assertEquals(numChildNodes, lazyDatasets.size());
	}

	/**
	 * Check that datasets written to Nexus file match the lazy datasets.
	 * @throws Exception
	 */
	@Test
	public void checkNexusDatasetsMatchLazyDatasets() throws Exception {
		String testScratchDir = TestHelpers.setUpTest(this.getClass(), "testNexusDatasetsMatchLazyDatasets", true);
		String testFile = Paths.get(testScratchDir, "xspress2Data.nxs").toAbsolutePath().toString();

		NexusTreeWriter nexusWriter = runTreeWriter(testFile, 10, 5);
		List<ILazyWriteableDataset> lazyDatasets = nexusWriter.getLazyDatasets();

		try (NexusFile nexusFile = nexusWriter.getNexusFile()) {
			nexusFile.openToRead();
			for (ILazyWriteableDataset lazyDataset : lazyDatasets) {
				// Load dataset from file, check it matches the lazy dataset (checks contents, data type, shape etc..)
				String pathToDataset = "/entry1/" + xspress.getName() + "/" + lazyDataset.getName();
				IDataset datasetFromFile = nexusFile.getData(pathToDataset).getDataset().getSlice((SliceND) null).squeeze();
				IDataset lazyDatasetFullSlice = lazyDataset.getSlice((SliceND) null).squeeze();
				assertEquals(lazyDatasetFullSlice, datasetFromFile);
			}
		}
	}

	/**
	 * Check that data is added correctly to the LazyDatasets from the dataset map when {@link NexusTreeWriter#writeNexusData} is called.
	 * @throws Exception
	 */
	@Test
	public void checkLazyDatasetsMatchDatasetMap() throws Exception {
		String testScratchDir = TestHelpers.setUpTest(this.getClass(), "checkLazyDatasetsMatchDatasetMap", true);
		String testFile = Paths.get(testScratchDir, "xspress2Data.nxs").toAbsolutePath().toString();

		nexusTreeArray = xspress.readout(0, 9);
		NexusTreeWriter nexusWriter = new NexusTreeWriter();
		Map<String, Dataset> datasetMap = nexusWriter.getDatasetMap(Arrays.asList(nexusTreeArray));

		nexusWriter.setFullpathToNexusFile(testFile);
		nexusWriter.addData(nexusTreeArray);
		nexusWriter.writeNexusData();
		nexusWriter.closeFile();

		for(ILazyWriteableDataset lazyDataset : nexusWriter.getLazyDatasets()) {
			assertTrue(datasetMap.containsKey(lazyDataset.getName()));
			assertEquals(datasetMap.get(lazyDataset.getName()), lazyDataset.getSlice((SliceND)null).squeeze());
		}
	}

	/**
	 * Check that the Map of datasets extracted from NexusTreeProvider array is correct (i.e. values in datasets all match).
	 * @throws Exception
	 */
	@Test
	public void checkDatasetMapIsCorrectForNexusTreeProvider() throws Exception {
		nexusTreeArray = xspress.readout(0, 9);
		NexusTreeWriter nexusWriter = new NexusTreeWriter();
		Map<String, Dataset> datasetMap = nexusWriter.getDatasetMap(Arrays.asList(nexusTreeArray));

		for (int i=0; i<nexusTreeArray.length; i++) {
			// Get detector node
			INexusTree nxTree = nexusTreeArray[i].getNexusTree().getNode(xspress.getName());

			// Loop over child nodes of Nexus tree, compare contents with datasets stored map
			int numChildNodes = nxTree.getNumberOfChildNodes();
			for (int j = 0; j < numChildNodes; j++) {
				INexusTree childNode = nxTree.getChildNode(j);
				Dataset datasetFromTree = childNode.getData().toDataset().squeeze();

				//Get corresponding 'row' of values from dataset in map
				assertTrue(datasetMap.containsKey(childNode.getName()));
				Dataset dataFromMap = datasetMap.get(childNode.getName());
				int[] start = new int[dataFromMap.getShape().length];
				start[0]=i;
				int[] stop = dataFromMap.getShape();
				stop[0] = i+1;
				Dataset dataFromMapSlice = dataFromMap.getSlice(start, stop, null).squeeze();

				assertEquals(datasetFromTree, dataFromMapSlice);
			}
		}
	}
}
