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

package gda.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.AbstractDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.jython.InterfaceProvider;

/**
 * Class that can be used to take data stored in NexusTreeProvider[] and write it to a new or pre-existing Nexus file.
 * It is designed to be used to write Xspress2 data as returned by {Xspress2Detector#readout(int, int)}) into a nexus file.
 * See {Xspress2BufferedDetector#writeDetectorData(NexusTreeProvider[])} and {Xspress2BufferedDetector#readFrames(int, int)}.
 * <p>
 * Flow of data between objects is :  {@code NexusTreeProvider[] --> Map<String, Dataset> dataToWrite--> List<LazyDatasets> lazyDatasetList -> nexusfile}
 *<p>
 * <li> Data to be written to file is added using {@link #addData(NexusTreeProvider[])}.
 * The tree is converted to a map of datasets using {@link #getDatasetMap(List)} and stored the {@link #dataToWrite} list.
 *
 * <li> Data stored in {@link #dataToWrite} is written to Nexus file by using {@link #writeNexusData()}.
 *  The first call to this function will also create/open the Nexus file, make the lazy datasets make the dataset entries in the file.
 *  Compression is used by default for all datasets. The data list is cleared after each write call to {@link #writeNexusData()}.
 *
 *  <li> The NexusTree nodes that will be written to file can be removed from the tree by calling
 *  {@link #removeNodesFromNexusTree(NexusTreeProvider[])}. This will remove the node entries from the tree so that, which can
 *  be useful so that NexusDataWriter doesn't also write the data later in the scan pipeline.
 *
 * @since 22/3/2018
 */
public class NexusTreeWriter {

	private static final Logger logger = LoggerFactory.getLogger(NexusTreeWriter.class);

	/** List of dataset maps; each call to {@link #addData(NexusTreeProvider[])} appends a new map with datasets extracted from the NexusTreeProvider array */
	private List< Map<String, Dataset> > dataToWrite = new ArrayList<>();

	/** Names of datasets that will be added to Nexus file - set in call to {@#addData(NexusTreeProvider[])} **/
	private List<String> datasetNames = new ArrayList<>();

	/** Datasets extracted from nexus tree and used to put the data in Nexus file */
	private List<ILazyWriteableDataset> lazyDatasetList = new ArrayList<>();


	private NexusFile detectorNexusFile = null;

	private String fullpathToDetectorNexusFile = "";
	private String detectorName = "";


	private boolean firstWrite = true;

	private static final String URI_SPACE = "%20";

	private int compressionLevel = NexusFile.COMPRESSION_LZW_L1;

	public NexusTreeWriter() {
	}

	/** Open existing nexus file, create a new one if doesn't exist */
	private NexusFile openCreateNexusFile(String fullNexusFilePath) throws NexusException {
		NexusFile nexusFile = null;
		if (Files.exists(Paths.get(fullNexusFilePath))) {
			logger.debug("Opening Nexus file {}", fullNexusFilePath);
			nexusFile = NexusFileHDF5.openNexusFile(fullNexusFilePath);
		} else {
			logger.debug("Creating new Nexus file {}", fullNexusFilePath);
			nexusFile = NexusFileHDF5.createNexusFile(fullNexusFilePath);
		}
		return nexusFile;
	}

	private void createNexusDatasets(NexusFile nexusFile, List<ILazyWriteableDataset> lazyDatasets) throws NexusException {
		logger.debug("Creating new datasets in Nexus file {}...", nexusFile.getFilePath());
		String path = "/entry1/"+detectorName+"/";
		int count = 0;
		for(ILazyWriteableDataset dset : lazyDatasets) {
			logger.debug("   dataset {}", dset.getName());
			GroupNode detGroup = detectorNexusFile.getGroup(path, true);
			DataNode dataNode = detGroup.getDataNode(dset.getName());
			if (dataNode != null) {
				// Associate lazy dataset with data node already in nexus file
				logger.debug("Using lazy dataset from existing data node {}/{}", path, dset.getName());
				lazyDatasets.set(count, dataNode.getWriteableDataset());
			} else {
				// Create new data node in Nexus file
				logger.debug("Making new data node at {}/{}", path, dset.getName());
				detectorNexusFile.createData(path, dset, compressionLevel, true);
			}
			count++;
		}
	}

	/**
	 * Create a list of ILazyWritableDatasets from a list of Datasets.<br>
	 * Each dataset comprises one or more 'frames' of data with shape [shapeDim1, shapeDim2 ...],
	 * with the overall dataset having a shape: [numFrames, shapeDim1, shapeDim2, ...]. <br>
	 * Each LazyWritableDataset is initialised to have :
	 * 	<li>Initial shape = [0, shapeDim1, shapeDim2, ...]
	 *  <li>Max shape = [unlimited, shapeDim1, shapeDim2, ...]
	 * @param datasets
	 * @return list of lazyWritableDatasets
	 */
	private List<ILazyWriteableDataset> makeLazyDatasets(Collection<Dataset> datasets) {
		logger.debug("Creating lazy datasets...");
		List<ILazyWriteableDataset> lazyDatasets = new ArrayList<>();
		for(Dataset dataset : datasets) {
			int[] shapeForLazy = dataset.getShape();
			shapeForLazy[0] = 0;

			int[] maxShape = Arrays.copyOf(shapeForLazy, shapeForLazy.length);
			int[] chunking = NexusUtils.estimateChunking(dataset.getShape(), dataset.getItemBytes());
			maxShape[0] = ILazyWriteableDataset.UNLIMITED;

			ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset(dataset.getName(), dataset.getDType(), shapeForLazy, maxShape, null);
			lazy.setChunking(chunking);
			lazyDatasets.add(lazy);
			logger.debug("   dataset {} : max shape {}, chunking = {}", lazy.getName(), Arrays.toString(lazy.getMaxShape()), Arrays.toString(lazy.getChunking()));
		}
		return lazyDatasets;
	}

	/**
	 * Append a dataset to the current contents of a lazy dataset. The inner shape dimensions of the two datasets are assumed to match...
	 * @param lazyDataset
	 * @param dataset
	 * @throws DatasetException
	 */
	private void updateLazyDataset(ILazyWriteableDataset lazyDataset, Dataset dataset) throws DatasetException {
		logger.debug("Updating lazy dataset {}", lazyDataset.getName());
		if (lazyDataset.getShape().length != dataset.getShape().length) {
			logger.warn("Skipping adding lazy dataset '{}' - shape length mismatch (lazy = {}, data = {})", lazyDataset.getName(), lazyDataset.getShape().length, dataset.getShape().length);
			return;
		}

		// Append to existing lazy dataset
		int[] lazyShape = lazyDataset.getShape();
		int currentLazyFrameNumber = lazyShape[0];

		// Use start frame for first start index, all zero for the rest
		int[] start = new int[lazyShape.length];
		start[0] = currentLazyFrameNumber;

		// Use dataset shape for stop index, with first index incremented by start frame
		int[] stop = dataset.getShape();
		stop[0] += currentLazyFrameNumber;

		logger.info("data shape = {}, lazy slice start = {}, stop = {}", Arrays.toString(dataset.getShape()), Arrays.toString(start), Arrays.toString(stop));

		lazyDataset.setSlice(null, dataset, start, stop, null);
	}

	/**
	 * Extract data from NexusTreeProvider array.
	 * Iterates over children of first node and collects data into a map of datasets
	 *
	 * @param nexusTreeArray
	 * @return Map<node name, dataset>
	 */
	public Map<String, Dataset> getDatasetMap(List<NexusTreeProvider> nexusTreeArray) {

		// Get name of detector (first child node of tree)
		detectorName = nexusTreeArray.get(0).getNexusTree().getChildNode(0).getName();

		int numFrames = nexusTreeArray.size();

		logger.debug("Extracting {} frames of data for detector {} from NexusTreeProvider array...", numFrames, detectorName);

		int currentFrame = 0;

		Map<String, Dataset> datasetMap = new LinkedHashMap<>(); // use linked hashmap to retain data in same detector order as in tree

		for (NexusTreeProvider treeProvider : nexusTreeArray) {
			// Get detector node
			INexusTree nxTree = treeProvider.getNexusTree().getNode(detectorName);

			// Loop over child nodes and collect data into datasets...
			int numChildNodes = nxTree.getNumberOfChildNodes();
			for (int i = 0; i < numChildNodes; i++) {

				INexusTree childNode = nxTree.getChildNode(i);

				if (childNode.getAttributes() != null && childNode.getAttributes().get("axis")!=null) {
					// logger.debug("Not adding {} - this is axis data and not change during a scan", childNode.getName());
					continue;
				}

				Dataset datasetFromTree = childNode.getData().toDataset().squeeze();

				// Make new dataset to store data for this node for whole nexusTreeArray
				String dataName = childNode.getName();
				if (!datasetMap.containsKey(dataName)) {
					int[] shape = datasetFromTree.getShape();
					int[] newShape = ArrayUtils.addAll(new int[] { numFrames }, shape);
					Class<? extends AbstractDataset> classType = DoubleDataset.class;
					if (datasetFromTree.getDType() == Dataset.INT32 || datasetFromTree.getDType() == Dataset.INT64) {
						classType = IntegerDataset.class;
					}
					logger.debug("Creating dataset to store {} data. Shape = {}", dataName, Arrays.toString(newShape));
					Dataset dset =  DatasetFactory.zeros(classType, newShape);
					dset.setName(dataName);
					datasetMap.put(dataName, dset);
				}

				Dataset datasetFromMap = datasetMap.get(dataName);

				// Set the start, stop indices for the slice
				int[] datashape = datasetFromMap.getShape();

				// Start : all zeros, first index is frame number
				int[] start = new int[datashape.length];
				start[0] = currentFrame;

				// Stop : first index is framenumber+1, then data shape
				int[] stop = Arrays.copyOf(datashape, datashape.length);
				stop[0] = currentFrame + 1;

				// Store data in the dataset by setting slice
				datasetFromMap.setSlice(datasetFromTree, start, stop, null);
			}
			currentFrame++;
		}
		logger.debug("Finished extracting frames");

		return datasetMap;
	}

	/** Names of the datasets that will be written to Nexus file */
	public List<String> getDatasetNames() {
		return datasetNames;
	}

	/** Add some data to be written to file. {@link #writeNexusData()} actually writes stored data to Nexus file */
	public void addData(NexusTreeProvider[] dataFromDetector) {
		Map<String, Dataset> datasetMap = getDatasetMap(Arrays.asList(dataFromDetector));
		dataToWrite.add(datasetMap);

		// Setup the list of dataset names the first time only
		if (firstWrite && datasetNames.isEmpty()) {
			for(String name : datasetMap.keySet()) {
				datasetNames.add(name);
			}
		}
	}

	/** Write data previously stored by calls to {@link #addData(NexusTreeProvider[])} to nexus file */
	public void writeNexusData() throws NexusException, DatasetException {
		if (StringUtils.isEmpty(fullpathToDetectorNexusFile)) {
			logger.warn("writeNexusData called but path to new nexus file has not been set or NexusFile object has not been set");
			return;
		}

		logger.info("Writing data to file {}", fullpathToDetectorNexusFile);

		if (dataToWrite.isEmpty()) {
			logger.warn("No new data added to treewriter - nothing to write");
			return;
		}

		// Open/create the Nexus file, create lazy datasets from the dataset map
		if (firstWrite) {
			detectorNexusFile = openCreateNexusFile(fullpathToDetectorNexusFile);
			lazyDatasetList = makeLazyDatasets(dataToWrite.get(0).values());
			createNexusDatasets(detectorNexusFile, lazyDatasetList);
			firstWrite = false;
		}

		// Append the data to the lazy datasets in Nexus file
		for(Map<String, Dataset> datasetMap : dataToWrite) {
			// Process each Map in the dataToWrite list (each call to 'addData' appends a map of datasets to the list)
			for (ILazyWriteableDataset lazyDataset : lazyDatasetList) {
				Dataset dset = datasetMap.get(lazyDataset.getName());
				logger.info("Updating lazy dataset {}", dset.getName());
				updateLazyDataset(lazyDataset, dset);
			}
		}
		logger.info("Flushing files");
		detectorNexusFile.flush();
		dataToWrite.clear();
		logger.info("Finished");
	}

	public String getScanNexusFilename() {
		try {
			return InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getFilename();
		}catch(NullPointerException npe) {
			return "";
		}
	}

	/**
	 * Add links in the scan Nexus file to point to the detector Nexus file data
	 */
	private void addLinksToScanNexusFile() {
		String scanNexusFileName = getScanNexusFilename();
		if (StringUtils.isEmpty(scanNexusFileName)) {
			logger.warn("Cannot add links to scan nexus file - filename is empty.");
			return;
		}

		try (NexusFile nexus = NexusFileHDF5.openNexusFile(scanNexusFileName) ) {
			addLinksToNexusFile(nexus);
		} catch (NexusException | URISyntaxException e) {
			logger.error("Problem adding links to scan nexus file '{}'", scanNexusFileName, e);
		}
	}

	/**
	 * Add links in scanNexusFile to point to all the datasets written to the detector nexus file.
	 * A link is made for each dataset in the lazyDatasetList
	 * @param scanNexusFile
	 * @throws NexusException
	 * @throws URISyntaxException
	 */
	private void addLinksToNexusFile(NexusFile scanNexusFile) throws NexusException, URISyntaxException {
		Path scanNexusFileDirPath = Paths.get(scanNexusFile.getFilePath()).getParent();
		Path detectorNexusFilePath = Paths.get(detectorNexusFile.getFilePath());

		// Return if detector data is already written to the scan nexus file
		if ( Paths.get(scanNexusFile.getFilePath()).equals(detectorNexusFilePath)) {
			logger.info("Not adding links to scan nexus file - detector data is already present in link location");
			return;
		}

		Path pathToDetectorNexusFile = detectorNexusFilePath;
		// Try to construct relative path to detector nexus file from scan nexus file
		try{
			pathToDetectorNexusFile = scanNexusFileDirPath.relativize(detectorNexusFilePath);
		}catch(IllegalArgumentException e) {
			logger.warn("Cannot set relative path to detector Nexus file {} from scan Nexus file {}. "+
						"Using absolute path to detector Nexus file instead.", detectorNexusFile.getFilePath(), scanNexusFile.getFilePath());
		}

		String groupPath = "/entry1/" + detectorName + "/";
		String linkPathForGroup = pathToDetectorNexusFile+"#entry1/"+detectorName+"/";
		for(ILazyDataset dataset : lazyDatasetList) {
			// Path to data in detector nexus file (convert spaces since they aren't allowed in URI used for link...)
 			String linkPathForData = linkPathForGroup + dataset.getName().replace(" ", URI_SPACE);

			// Path to entry to be created containing the link
			String dataPath = groupPath+dataset.getName();
			scanNexusFile.linkExternal(new URI(linkPathForData), dataPath, false);
		}
		scanNexusFile.flush();
	}

	public void atScanStart() {
		closeFile();
		detectorNexusFile = null;
		firstWrite = true;
		clearDatasets();
	}

	public void atScanEnd() {
		try {
			if (StringUtils.isEmpty(fullpathToDetectorNexusFile)) {
				fullpathToDetectorNexusFile = getScanNexusFilename();
			}
			writeNexusData();
		} catch (NexusException |  DatasetException e) {
			logger.error("Problem writing Nexus data at end of scan", e);
		}
		addLinksToScanNexusFile();
		closeFile();
		clearDatasets();
	}

	public void clearDatasets() {
		lazyDatasetList.clear();
		dataToWrite.clear();
		datasetNames.clear();
	}

	public void closeFile() {
		if (detectorNexusFile!=null) {
			try {
				detectorNexusFile.flush();
				detectorNexusFile.close();
			} catch (NexusException e) {
				logger.error("Problem closing Nexus file {}", detectorNexusFile.getFilePath(),  e);
			}
		}
	}

	public List<ILazyWriteableDataset> getLazyDatasets() {
		return lazyDatasetList;
	}
	public String getFullpathToNexusFile() {
		return fullpathToDetectorNexusFile;
	}

	public void setFullpathToNexusFile(String fullpathToNexusFile) {
		this.fullpathToDetectorNexusFile = fullpathToNexusFile;
	}

	public String getDetectorName() {
		return detectorName;
	}

	public NexusFile getNexusFile() {
		return detectorNexusFile;
	}

	/**
	 * Remove child nodes in Nexus tree array. See {@link #removeNodesFromNexusTree(NexusTreeProvider)}.
	 * @param nexusTreeArray
	 */
	public void removeNodesFromNexusTree(NexusTreeProvider[] nexusTreeArray) {
		for(NexusTreeProvider treeProvider : nexusTreeArray) {
			removeNodesFromNexusTree(treeProvider);
		}
	}

	/**
	 * Remove child nodes in Nexus tree whose names match those in the current lazyDatasetList (i.e. datasets written to detector nexus file).
	 * By removing these nodes, when {NexusDataWriter} processes the Nexus tree, it doesn't write the data into the scan nexus file.
	 * @param nexusTreeProvider
	 */
	public void removeNodesFromNexusTree(NexusTreeProvider nexusTreeProvider) {
		for(String datasetName : datasetNames) {
			// Child Nodes must be removed from the Tree in same order as they were added, otherwise get infinite recursion and stack overflow...
			NexusTreeNode treeNode = (NexusTreeNode) nexusTreeProvider.getNexusTree().getNode(detectorName);
			NexusTreeNode childNode = treeNode.findNode(datasetName);
			childNode.setParentNode(null);
			treeNode.removeChildNode(childNode);
		}
	}

	public int getCompressionLevel() {
		return compressionLevel;
	}

	public void setCompressionLevel(int compressionLevel) {
		this.compressionLevel = compressionLevel;
	}
}

