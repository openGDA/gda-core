/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.data.scan.nexus.device;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.InterfaceUtils;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.NexusDetector;

/**
 * An instance of this class wraps a {@link NexusDetector} to implement {@link INexusDevice}.
 */
public class NexusDetectorNexusDevice extends AbstractDetectorNexusDeviceAdapter {

	private static final String NEXUS_LINK_PREFIX = "nxfile://";

	private static final Logger logger = LoggerFactory.getLogger(NexusDetectorNexusDevice.class);

	private NXdetector detGroup;
	private NexusScanInfo scanInfo = null;

	private Map<String, ILazyWriteableDataset> writeableDatasets;

	private List<String> primaryFieldNames = null;
	private Set<String> externalFileNames = null;
	private Map<String, Integer> externalDatasetRanks = null;

	protected NexusDetectorNexusDevice(NexusDetector detector) {
		super(detector);
	}

	private INexusTree getDetectorNexusSubTree(NexusTreeProvider treeProvider) throws NexusException {
		final INexusTree nexusTree = treeProvider.getNexusTree();
		if (nexusTree.getNumberOfChildNodes() != 1) {
			// At present we assume that the tree has a single entry, most detectors do this.
			logger.error("Nexus tree for detector {} has more than one sub-tree. Only the first will be processed.", getDetector().getName());
		}

		final INexusTree detectorSubTree = nexusTree.getChildNode(0);
		if (!detectorSubTree.getName().equals(getDetector().getName())) {
			logger.warn("Detector subtree {} has different name to detector {}", nexusTree.getName(), getDetector().getName());
		}
		if (!detectorSubTree.getNxClass().equals(NexusExtractor.NXDetectorClassName)) {
			// can only handle NXdetector trees. See NexusDataWriter.makeNexusDetectorGroups
			throw new NexusException("Nxclass attribute for detector " + getName() + "must be " + NexusExtractor.NXDetectorClassName + ", was: " + detectorSubTree.getNxClass());
		}

		return detectorSubTree;
	}

	private void writeDataNode(INexusTree dataTreeNode, SliceND scanSlice) throws NexusException {
		final String dataNodePath = dataTreeNode.getNodePath();
		final NexusGroupData data = dataTreeNode.getData();
		if (data == null) {
			logger.error("Current point contains no data for datanode at path: {}", dataNodePath);
			return;
		}

		final ILazyWriteableDataset dataset = writeableDatasets.get(dataNodePath);
		if (dataset == null) {
			logger.error("No dataset found for path: {}", dataNodePath);
			return;
		}

		final Dataset dataToWrite = data.toDataset();
		try {
			IWritableNexusDevice.writeDataset(dataset, dataToWrite, scanSlice);
		} catch (DatasetException | IllegalArgumentException e) {
			throw new NexusException("Could not write data for detector " + getName(), e);
		}
	}

	@Override
	protected void writeMetaDataFields(NXdetector detGroup, Detector detector) throws DeviceException {
		// Override to not write the metadata fields written by this method in the superclass
		// NexusDataWriter does not add these fields for this case, as the same NexusDetector can
		// write multiple NXdetectors, with different names, types and IDs. If these fields are required
		// they should be part of the INexusTree structure returned by the NexusDetector.readout() method
	}

	@Override
	protected void writeDataFields(NexusScanInfo info, NXdetector detGroup) throws NexusException {
		this.detGroup = detGroup;
		this.scanInfo = info;
		this.writeableDatasets = new HashMap<>();
		this.primaryFieldNames = new ArrayList<>();
		this.externalFileNames = new HashSet<>();
		this.externalDatasetRanks = new HashMap<>();

		final INexusTree detTree = getDetectorNexusSubTree((NexusTreeProvider) firstPointData);
		detGroup.setLocal_nameScalar(detTree.getName());

		for (INexusTree subTree : detTree) {
			addNode(detGroup, subTree);
		}
	}

	private void addNode(NXobject group, INexusTree treeNode) throws NexusException {
		// Handle the node depending on the type. Note that although this attribute is
		// called 'nxClass', in the nexus file this only written for groups, whereas INexusTree
		// adds values for attributes, datasets and external links. It is these special values
		// that we need to treat differently.
		final String nxClass = treeNode.getNxClass();
		switch (nxClass) {
			case NexusExtractor.AttrClassName:
				addAttribute(group, treeNode);
				break;
			case NexusExtractor.ExternalSDSLink:
				addExternalDataset(group, treeNode);
				break;
			case NexusExtractor.SDSClassName:
				addDataNode(group, treeNode);
				break;
			default:
				addGroupNode(group, treeNode);
		}
	}

	private void addExternalDataset(NXobject group, INexusTree linkTreeNode) throws NexusException {
		final SymbolicNode externalLink = createExternalLink(linkTreeNode);
		group.addSymbolicNode(linkTreeNode.getName(), externalLink);

		if (isPrimaryField(group, linkTreeNode)) {
			final String fieldName = linkTreeNode.getName();
			final int dataRank = linkTreeNode.getData().externalDataRank;
			if (dataRank < 0) {
				logger.error("No rank set for field {}. This field will not be set as a primary field.", fieldName);
			} else {
				externalFileNames.add(externalLink.getSourceURI().toString());
				externalDatasetRanks.put(fieldName, dataRank + scanInfo.getRank());
				primaryFieldNames.add(linkTreeNode.getName());
			}
		}
	}

	private void addGroupNode(NXobject group, INexusTree groupTreeNode)
			throws NexusException {
		final NXobject groupNode = NexusNodeFactory.createNXobjectForClass(groupTreeNode.getNxClass());
		group.addGroupNode(groupTreeNode.getName(), groupNode);
		for (INexusTree childNode : groupTreeNode) {
			addNode(groupNode, childNode);
		}
	}


	private void addDataNode(NXobject group, INexusTree dataTreeNode) throws NexusException {
		final DataNode dataNode = createDataNode(dataTreeNode);
		group.addDataNode(dataTreeNode.getName(), dataNode);

		if (isPrimaryField(group, dataTreeNode)) {
			primaryFieldNames.add(dataTreeNode.getName());
		}

		// handle any attributes (all child nodes must be attributes)
		for (INexusTree attrNode : dataTreeNode) {
			if (!attrNode.getNxClass().equals(NexusExtractor.AttrClassName)) {
				throw new NexusException("A data node can only contain attributes: " + attrNode.getName());
			}
			addAttribute(dataNode, attrNode);
		}
	}

	/**
	 * A field is a primary field if it is a direct child of the detector group, the flag
	 * {@code isDetectorEntryData} is set, and the point is not point dependent or in a link to
	 * an external data
	 * @param group
	 * @param treeNode
	 * @return
	 */
	private boolean isPrimaryField(NXobject group, INexusTree treeNode) {
		final String nodeType = treeNode.getNxClass();
		return group == detGroup && treeNode.getData() != null && treeNode.getData().isDetectorEntryData &&
				((nodeType.equals(NexusExtractor.SDSClassName) && treeNode.isPointDependent()) ||
						nodeType.equals(NexusExtractor.ExternalSDSLink));
	}

	private void addAttribute(Node parentNode, INexusTree attributeTreeNode) throws NexusException {
		// note: no special handling for 'axis' or 'label'
		final NexusGroupData data = attributeTreeNode.getData();
		final Object value = getAttrValue(data);
		if (value != null && !(value instanceof String && ((String) value).isBlank())) {
			final Attribute attribute = TreeFactory.createAttribute(attributeTreeNode.getName(), value);
			parentNode.addAttribute(attribute);
		}
	}

	private Object getAttrValue(NexusGroupData data) throws NexusException {
		if (data == null || data.getBuffer() == null) {
			throw new NexusException("Attribute node must have data");
		}

		if (data.isChar()) {
			return data.getFirstValue();
		}
		return data.toDataset();
	}

	private DataNode createDataNode(INexusTree treeNode) {
		final DataNode dataNode = NexusNodeFactory.createDataNode();
		final ILazyDataset dataset = createDataset(treeNode);
		dataNode.setDataset(dataset);
		return dataNode;
	}

	private ILazyDataset createDataset(INexusTree dataTreeNode) {
		final NexusGroupData data = dataTreeNode.getData();
		if (!dataTreeNode.isPointDependent()) {
			return data.toDataset(); // the data is per-scan, just write the value now
		}

		// create a lazy writeable dataset for per-point data
		final ILazyWriteableDataset dataset = createLazyDataset(data);
		dataset.setName(dataTreeNode.getName());
		writeableDatasets.put(dataTreeNode.getNodePath(), dataset);

		return dataset;
	}

	private ILazyWriteableDataset createLazyDataset(NexusGroupData data) {
		final ILazyWriteableDataset lazyDataset = data.toLazyDataset();
		final int[] dataDimensions = lazyDataset.getShape();
		// TODO the code to get the max shape is simpler than NexusDataWriter, is that ok?
		final int[] maxShape = createMaxShape(dataDimensions);
		lazyDataset.setMaxShape(maxShape);

		lazyDataset.setChunking(createChunking(data, dataDimensions, maxShape));
		lazyDataset.setFillValue(getFillValue(InterfaceUtils.getElementClass(data.getInterface())));

		return lazyDataset;
	}

	private int[] createMaxShape(int[] dataDimensions) {
		// treat a 1-d dataset of size 1 as scalar data
		final int dataRank = (dataDimensions.length == 1 && dataDimensions[0] == 1) ? 0 : dataDimensions.length;
		final int datasetRank = scanInfo.getShape().length + dataRank;
		int[] maxShape = new int[datasetRank];
		Arrays.fill(maxShape, ILazyWriteableDataset.UNLIMITED);
		return maxShape;
	}

	private int[] createChunking(NexusGroupData data, final int[] dataDimensions, final int[] maxShape) {
		if (dataDimensions.length == 0) {
			NexusUtils.estimateChunking(maxShape, InterfaceUtils.getItemBytes(1, data.getInterface()));
		}

		return scanInfo.createChunk(dataDimensions);
	}

	private SymbolicNode createExternalLink(INexusTree treeNode) throws NexusException {
		final NexusGroupData data = treeNode.getData();
		// data object contains link of format nxfile://nexusfilePath#pathWithinFile
		final String link = ((String[]) data.getBuffer())[0];
		if (!link.startsWith(NEXUS_LINK_PREFIX)) {
			throw new NexusException("Invalid format for external link " + StringUtils.quote(link));
		}
		final String[] parts = link.substring(NEXUS_LINK_PREFIX.length()).split("#");
		if (parts.length != 2) {
			throw new NexusException("Invalid format for external link " + StringUtils.quote(link));
		}

		final Path extFilePath = Paths.get(parts[0]).toAbsolutePath();
		final Path scanFilePath = Paths.get(scanInfo.getFilePath()).toAbsolutePath();
		final Path extFileRelPath = scanFilePath.getParent().relativize(extFilePath);

		String nodePath = parts[1];
		if (!nodePath.startsWith(Tree.ROOT)) {
			nodePath = Tree.ROOT + nodePath;
		}

		try {
			final URI uri = new URI(extFileRelPath.toString());
			return NexusNodeFactory.createSymbolicNode(uri, nodePath);
		} catch (URISyntaxException e) {
			throw new NexusException("External file path '" + extFileRelPath + "' cannot be converted to a URI");
		}
	}

	@Override
	protected String getPrimaryDataFieldName() {
		return primaryFieldNames.isEmpty() ? null : primaryFieldNames.get(0);
	}

	@Override
	protected void configureNexusWrapper(NexusObjectWrapper<NXdetector> nexusWrapper, NexusScanInfo info)
			throws NexusException {
		super.configureNexusWrapper(nexusWrapper, info);
		// the first primary field is added above, the rest are added as 'additional' primary data fields
		// note: these are the fields that NXdata groups are created for
		primaryFieldNames.stream().skip(1).forEach(nexusWrapper::addAdditionalPrimaryDataFieldName);
		externalFileNames.forEach(nexusWrapper::addExternalFileName);
		externalDatasetRanks.forEach(nexusWrapper::setExternalDatasetRank);
	}

	private void writeNode(INexusTree treeNode, SliceND scanSlice) throws NexusException {
		if (!treeNode.isPointDependent()) return;

		final String nxClass = treeNode.getNxClass();
		switch (nxClass) {
			case NexusExtractor.AttrClassName:
				break; // do nothing
			case NexusExtractor.ExternalSDSLink:
				break; // do nothing
			case NexusExtractor.SDSClassName:
				writeDataNode(treeNode, scanSlice);
				break;
			default:
				for (INexusTree childNode : treeNode) {
					writeNode(childNode, scanSlice);
				}
		}
	}

	@Override
	public void writePosition(Object data, SliceND scanSlice) throws NexusException {
		final INexusTree detTree = getDetectorNexusSubTree((NexusTreeProvider) data);
		for (INexusTree subTree : detTree) {
			writeNode(subTree, scanSlice);
		}
	}

	@Override
	public void scanEnd() throws NexusException {
		super.scanEnd();
		detGroup = null;
		scanInfo = null;
		writeableDatasets = null;
		primaryFieldNames = null;
		externalFileNames = null;
		externalDatasetRanks = null;
	}

}
