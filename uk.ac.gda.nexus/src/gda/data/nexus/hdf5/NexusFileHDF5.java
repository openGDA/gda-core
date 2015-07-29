/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.data.nexus.hdf5;

import gda.data.nexus.NexusUtils;

import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import ncsa.hdf.hdf5lib.structs.H5G_info_t;
import ncsa.hdf.hdf5lib.structs.H5L_info_t;
import ncsa.hdf.hdf5lib.structs.H5O_info_t;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyWriteableDataset;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.HDF5LazyLoader;
import uk.ac.diamond.scisoft.analysis.io.HDF5LazySaver;

public class NexusFileHDF5 implements NexusFile {

	//TODO: Clean up and move stuff to helper classes?

	private static Map<Integer, Integer> HDF_TYPES_TO_DATASET_TYPES;
	private static Map<Integer, Integer> DATASET_TYPES_TO_HDF_TYPES;
	private static Set<Integer> UNSIGNED_HDF_TYPES;

	private static int getTypeRepresentation(int nativeHdfTypeId) throws NexusException {
		final int[] types = {HDF5Constants.H5T_NATIVE_DOUBLE,
				HDF5Constants.H5T_NATIVE_FLOAT,
				HDF5Constants.H5T_NATIVE_INT8,
				HDF5Constants.H5T_NATIVE_UINT8,
				HDF5Constants.H5T_NATIVE_INT16,
				HDF5Constants.H5T_NATIVE_UINT16,
				HDF5Constants.H5T_NATIVE_INT32,
				HDF5Constants.H5T_NATIVE_UINT32,
				HDF5Constants.H5T_NATIVE_INT64,
				HDF5Constants.H5T_NATIVE_UINT64,
				HDF5Constants.H5T_C_S1};
		try {
			for (int type : types) {
				if (H5.H5Tequal(nativeHdfTypeId, type)) {
					return type;
				}
			}
		} catch (HDF5LibraryException e) {
			throw new NexusException("Could not compare types", e);
		}
		throw new NexusException("Unknown type");
	}

	static {
		HDF_TYPES_TO_DATASET_TYPES = new HashMap<Integer, Integer>();
		DATASET_TYPES_TO_HDF_TYPES = new HashMap<Integer, Integer>();
		UNSIGNED_HDF_TYPES = new HashSet<Integer>();

		HDF_TYPES_TO_DATASET_TYPES.put(HDF5Constants.H5T_NATIVE_INT8, Dataset.INT8);
		DATASET_TYPES_TO_HDF_TYPES.put(Dataset.INT8, HDF5Constants.H5T_NATIVE_INT8);

		HDF_TYPES_TO_DATASET_TYPES.put(HDF5Constants.H5T_NATIVE_INT16, Dataset.INT16);
		DATASET_TYPES_TO_HDF_TYPES.put(Dataset.INT16, HDF5Constants.H5T_NATIVE_INT16);

		HDF_TYPES_TO_DATASET_TYPES.put(HDF5Constants.H5T_NATIVE_INT32, Dataset.INT32);
		DATASET_TYPES_TO_HDF_TYPES.put(Dataset.INT32, HDF5Constants.H5T_NATIVE_INT32);

		HDF_TYPES_TO_DATASET_TYPES.put(HDF5Constants.H5T_NATIVE_INT64, Dataset.INT64);
		DATASET_TYPES_TO_HDF_TYPES.put(Dataset.INT64, HDF5Constants.H5T_NATIVE_INT64);

		HDF_TYPES_TO_DATASET_TYPES.put(HDF5Constants.H5T_NATIVE_UINT8, Dataset.INT8);
		UNSIGNED_HDF_TYPES.add(HDF5Constants.H5T_NATIVE_UINT8);

		HDF_TYPES_TO_DATASET_TYPES.put(HDF5Constants.H5T_NATIVE_UINT16, Dataset.INT16);
		UNSIGNED_HDF_TYPES.add(HDF5Constants.H5T_NATIVE_UINT16);

		HDF_TYPES_TO_DATASET_TYPES.put(HDF5Constants.H5T_NATIVE_UINT32, Dataset.INT32);
		UNSIGNED_HDF_TYPES.add(HDF5Constants.H5T_NATIVE_UINT32);

		HDF_TYPES_TO_DATASET_TYPES.put(HDF5Constants.H5T_NATIVE_UINT64, Dataset.INT64);
		UNSIGNED_HDF_TYPES.add(HDF5Constants.H5T_NATIVE_UINT64);

		HDF_TYPES_TO_DATASET_TYPES.put(HDF5Constants.H5T_NATIVE_FLOAT, Dataset.FLOAT32);
		DATASET_TYPES_TO_HDF_TYPES.put(Dataset.FLOAT32, HDF5Constants.H5T_NATIVE_FLOAT);

		HDF_TYPES_TO_DATASET_TYPES.put(HDF5Constants.H5T_NATIVE_DOUBLE, Dataset.FLOAT64);
		DATASET_TYPES_TO_HDF_TYPES.put(Dataset.FLOAT64, HDF5Constants.H5T_NATIVE_DOUBLE);

		HDF_TYPES_TO_DATASET_TYPES.put(HDF5Constants.H5T_C_S1, Dataset.STRING);
		DATASET_TYPES_TO_HDF_TYPES.put(Dataset.STRING, HDF5Constants.H5T_C_S1);
	}

	public enum NodeType {
		GROUP(HDF5Constants.H5O_TYPE_GROUP),
		DATASET(HDF5Constants.H5O_TYPE_DATASET);
		public final int value;
		private static Map<Integer, NodeType> map = new HashMap<Integer, NodeType>();

		static {
			for (NodeType nt : NodeType.values()) {
				map.put(nt.value, nt);
			}
		}

		NodeType(int value) {
			this.value = value;
		}

		public static NodeType valueOf(int type) {
			return map.get(type);
		}
	}

	final class ParsedNode {
		public final String name;
		public final String nxClass;
		ParsedNode(String name, String nxClass) {
			this.name = name;
			this.nxClass = nxClass;
		}
	}

	//TODO: Better name?
	final class NodeData {
		public final String name;
		public final String nxClass;
		public final String path;
		public final Node node;
		public final NodeType type;
		NodeData(String name, String nxClass, String path, Node node, NodeType type) {
			this.name = name;
			this.nxClass = nxClass;
			this.path = path;
			this.node = node;
			this.type = type;
		}
	}

	//TODO: Logging
	private static final Logger logger = LoggerFactory.getLogger(NexusFile.class);

	private int fileId = -1;

	private String fileName;

	private TreeFile tree;

	private Map<Long, Node> nodeMap; //used to remember node locations in file for detecting hard links

	private boolean writeable = false;

	public NexusFileHDF5(String path) {
		fileName = path;
	}

	private void initializeTree() {
		if (tree == null) {
			tree = TreeFactory.createTreeFile(fileName.hashCode(), fileName);
			nodeMap = new HashMap<Long, Node>();
		} else {
			throw new IllegalStateException("File is already open");
		}
	}

	private ParsedNode[] parseAugmentedPath(String path) {
		if (!path.startsWith(Tree.ROOT)) {
			throw new IllegalArgumentException("Path must be absolute");
		}
		String[] parts = path.split(Node.SEPARATOR);
		ParsedNode[] nodes = new ParsedNode[parts.length];
		int i = 0;
		for (String p : parts) {
			String[] pair = p.split(NXCLASS_SEPARATOR, 2);
			nodes[i++] = new ParsedNode(pair[0], pair.length > 1 ? pair[1] : "");
		}
		return nodes;
	}

	private void assertOpen() {
		if (tree == null) {
			throw new IllegalStateException("File has been closed");
		}
	}

	private void assertCanWrite() {
		assertOpen();
		if (!writeable) {
			throw new IllegalStateException("Cannot write as opened as read-only");
		}
	}

	@Override
	public void openToRead() throws NexusException {
		try {
			fileId = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
		} catch (HDF5LibraryException e) {
			throw new NexusException("Cannot open to read", e);
		}
		initializeTree();
	}

	@Override
	public void openToWrite(boolean createIfNecessary) throws NexusException {
		if (new java.io.File(fileName).exists()) {
			try {
				fileId = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDWR, HDF5Constants.H5P_DEFAULT);
			} catch (HDF5LibraryException e) {
				throw new NexusException("Cannot open to write", e);
			}
		} else if (createIfNecessary) {
			try {
				fileId = H5.H5Fcreate(fileName, HDF5Constants.H5F_ACC_EXCL, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
			} catch (HDF5LibraryException e) {
				throw new NexusException("Cannot create to write", e);
			}
		} else {
			throw new NexusException("File not found and not created");
		}
		initializeTree();
		writeable = true;
	}

	@Override
	public void createAndOpenToWrite() throws NexusException {
		try {
			fileId = H5.H5Fcreate(fileName, HDF5Constants.H5F_ACC_TRUNC, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		} catch (HDF5LibraryException e) {
			throw new NexusException("Cannot create to write", e);
		}
		initializeTree();
		writeable = true;
	}

	@Override
	public void setDebug(boolean debug) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPath(Node node) {
		return TreeUtils.getPath(tree,  node);
	}

	@Override
	public GroupNode getGroup(String augmentedPath, boolean createPathIfNecessary) throws NexusException {
		assertOpen();
		String plainPath = NexusUtils.stripAugmentedPath(augmentedPath);
		NodeLink link = tree.findNodeLink(plainPath);
		if (link != null) {
			if (link.isDestinationGroup()) {
				GroupNode g = (GroupNode) link.getDestination();
				if (!g.isPopulated()) {
					if (!plainPath.endsWith(Node.SEPARATOR)) {
						plainPath += Node.SEPARATOR;
					}
					populateGroupNode(plainPath, g);
				}
				return g;
			}
			throw new NexusException("Path specified is not for a group");
		}
		NodeData node = getGroupNode(augmentedPath, createPathIfNecessary);
		//NodeData node = getNode(augmentedPath, createPathIfNecessary);
		return (GroupNode) (node.name == null ? null : (node.node));
	}

	@Override
	public GroupNode getGroup(GroupNode group,
			String name,
			String nxClass,
			boolean createPathIfNecessary) throws NexusException {
		String path = getPath(group);
		if (path == null) {
			throw new IllegalArgumentException("Group path is null");
		}
		return getGroup(NexusUtils.addToAugmentPath(path,  name,  nxClass), createPathIfNecessary);
	}

	private void createGroupNode(long oid, GroupNode group, String path, String name, String nxClass)
			throws NexusException {
		GroupNode g;
		long fileAddr = getLinkTarget(path + Node.SEPARATOR + name);
		if (!nodeMap.containsKey(fileAddr)) {
			g = TreeFactory.createGroupNode(oid);
			if (nxClass != null && !nxClass.isEmpty()) {
				g.addAttribute(TreeFactory.createAttribute(NXCLASS, nxClass, false));
			}
			cacheAttributes(path + Node.SEPARATOR + name, g);
			if (fileAddr != IS_EXTERNAL_LINK &&  !testForExternalLink(path)) {
				//if our node is an external link we cannot cache its file location
				//we cannot handle hard links in external files
				nodeMap.put(fileAddr, g);
			}
			//TODO copy Attributes from node in file to tree node (g)
		} else {
			g = (GroupNode)nodeMap.get(fileAddr);
		}
		group.addGroupNode(path, name, g);
	}

	private void cacheAttributes(String path, Node node) throws NexusException {
		Charset utf8 = Charset.forName("UTF-8");
		try {
			H5O_info_t objInfo = H5.H5Oget_info_by_name(fileId, path, HDF5Constants.H5P_DEFAULT);
			long numAttrs = objInfo.num_attrs;
			for (long i = 0; i < numAttrs; i++) {
				Dataset dataset;
				//boolean unsigned = false;
				long[] shape = null;
				long[] maxShape = null;
				Integer datasetType;
				try (HDF5Resource attrResource = new HDF5AttributeResource(
						H5.H5Aopen_by_idx(fileId, path, HDF5Constants.H5_INDEX_NAME, HDF5Constants.H5_ITER_INC, i,
								HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT))) {
					int attrId = attrResource.getResource();
					String[] name = new String[1];
					H5.H5Aget_name(attrId, name);
					if (node.containsAttribute(name[0]) && writeable) {
						//we don't need to read an attribute we already have
						continue;
					}
					try (HDF5Resource spaceResource = new HDF5DataspaceResource( H5.H5Aget_space(attrId) );
							HDF5Resource typeResource = new HDF5DatatypeResource( H5.H5Aget_type(attrId) );
							HDF5Resource nativeTypeResource = new HDF5DatatypeResource( H5.H5Tget_native_type(typeResource.getResource()) )) {
						final int spaceId = spaceResource.getResource();
						final int nativeTypeId = nativeTypeResource.getResource();
						final int dataclass = H5.H5Tget_class(nativeTypeId);
						final int datatypeSize = H5.H5Tget_size(nativeTypeId);
						//TODO: handle strings
						if (dataclass == HDF5Constants.H5T_STRING) {
							datasetType = Dataset.STRING;
							//unsigned = false;
						} else {
							int typeRepresentation = getTypeRepresentation(nativeTypeId);
							//unsigned = UNSIGNED_HDF_TYPES.contains(typeRepresentation);
							datasetType = HDF_TYPES_TO_DATASET_TYPES.get(typeRepresentation);
						}
						if (datasetType == null) {
							throw new NexusException("Unknown data type");
						}
						final int nDims = H5.H5Sget_simple_extent_ndims(spaceId);
						shape = new long[nDims];
						maxShape = new long[nDims];
						H5.H5Sget_simple_extent_dims(spaceId, shape, maxShape);
						final int[] iShape = longArrayToIntArray(shape);
						if (dataclass == HDF5Constants.H5T_STRING) {
							//TODO: support reading VL strings
							int strCount = 1;
							for (int d : iShape) {
								strCount *= d;
							}
							byte[] buffer = new byte[strCount * datatypeSize];
							H5.H5Aread(attrId, nativeTypeId, buffer);
							String[] strings = new String[strCount];
							int strIndex = 0;
							for (int j = 0; j < buffer.length; j += datatypeSize) {
								int strLength = 0;
								//Java doesn't strip null bytes during string construction
								for (int k = j; k < j + datatypeSize && buffer[k] != '\0'; k++) strLength++;
								strings[strIndex++] = new String(buffer, j, strLength, utf8);
							}
							dataset = DatasetFactory.createFromObject(strings).reshape(iShape);
						} else {
							dataset = DatasetFactory.zeros(iShape, datasetType);
							Serializable buffer = dataset.getBuffer();
							H5.H5Aread(attrId, nativeTypeId, buffer);
						}
						dataset.setName(name[0]);
						node.addAttribute(createAttribute(dataset));
					}
				}
			}
		} catch (HDF5Exception e) {
			throw new NexusException("Could not retrieve node attributes");
		}
		return;
	}

	private void populateGroupNode(String path, GroupNode group) throws NexusException {
		//TODO: verify soft/external/hard links work
		cacheAttributes(path, group);
		try {
			H5G_info_t groupInfo = H5.H5Gget_info_by_name(fileId, path, HDF5Constants.H5P_DEFAULT);
			for (long i = 0; i < groupInfo.nlinks; i++) {
				//we have to open the object itself to handle external links
				//H5.H5Lget_name_by_idx(fileId, "X", ....) will fail if X is an external link node, as will similar methods
				try (HDF5Resource objResource = new HDF5ObjectResource( H5.H5Oopen(fileId, path, HDF5Constants.H5P_DEFAULT) )) {
					int objId = objResource.getResource();
					String linkName = H5.H5Lget_name_by_idx(objId, ".", HDF5Constants.H5_INDEX_NAME,
							HDF5Constants.H5_ITER_INC, i, HDF5Constants.H5P_DEFAULT);
					String childPath = path + linkName;
					H5O_info_t objectInfo = H5.H5Oget_info_by_name(fileId, childPath, HDF5Constants.H5P_DEFAULT);
					String nxClass = "";
					//TODO getNxClass attribute
					if (objectInfo.type == HDF5Constants.H5O_TYPE_GROUP) {
						createGroupNode(childPath.hashCode(), group, path, linkName, nxClass);
					} else if (objectInfo.type == HDF5Constants.H5O_TYPE_DATASET) {
						group.addDataNode(childPath, linkName, getDataNodeFromFile(childPath, group, linkName));
					} else {
						throw new NexusException("Unhandled object type");
					}
				}
			}
		} catch (HDF5LibraryException e) {
			throw new NexusException("Could not process over child links", e);
		}
		return;
	}

	private NodeData getGroupNode(String augmentedPath, boolean createPathIfNecessary) throws NexusException {
		NodeData node = getNode(augmentedPath, createPathIfNecessary);
		if (node.type != NodeType.GROUP) {
			throw new NexusException("Found dataset node instead of group node");
		}
		return node;
	}

	/**
	 * Try to retrieve or create the target node (and parents), building cache as necessary.
	 * Cannot create data nodes
	 */
	private NodeData getNode(String augmentedPath, boolean createPathIfNecessary) throws NexusException {
		ParsedNode[] parsedNodes = parseAugmentedPath(augmentedPath);
		StringBuilder pathBuilder = new StringBuilder(Tree.ROOT);
		String parentPath = pathBuilder.toString();
		GroupNode group = tree.getGroupNode();
		if (parsedNodes.length < 1) {
			return new NodeData(Tree.ROOT, null, null, group, NodeType.GROUP);
		}
		Node node = group;
		ParsedNode parsedNode = parsedNodes[0];
		NodeType type = null;
		//traverse to target node
		for (int i = 0; i < parsedNodes.length; i++) {
			parsedNode = parsedNodes[i];
			if (parsedNode.name.isEmpty()) {
				continue;
			}
			parentPath = pathBuilder.toString();
			pathBuilder.append(parsedNode.name);
			pathBuilder.append(Node.SEPARATOR);
			String path = pathBuilder.toString();
			type = getNodeType(path);
			if (type == null || type == NodeType.GROUP) {
				//make sure the group actually exists
				int nodeId;
				nodeId = openGroup(path, parsedNode.nxClass, createPathIfNecessary);
				closeNode(nodeId);
				type = NodeType.GROUP;
			} else if (type == NodeType.DATASET) {
				//should be last iteration of loop (final path segment)
				if (i < parsedNodes.length - 1) {
					throw new NexusException("Dataset node should only be final path segment");
				}
				//dataset node *must* exist on disk because we are not creating
				//so just establish in cache and return that object
				if (group.containsDataNode(parsedNode.name)) {
					node = group.getDataNode(parsedNode.name);
				} else {
					node = getData(path);
					group.addDataNode(parentPath, parsedNode.name, (DataNode) node);
				}
				break;
			} else {
				throw new NexusException("Unhandled node type");
			}
			//establish group in our cache
			try {
				H5L_info_t linkInfo = (H5.H5Lget_info(fileId, path, HDF5Constants.H5P_DEFAULT));
				if (linkInfo.type == HDF5Constants.H5L_TYPE_SOFT) {
					String[] name = new String[2];
					H5.H5Lget_val(fileId, path, name, HDF5Constants.H5P_DEFAULT);
					path = name[0];
					if (!group.containsGroupNode(parsedNode.name)) {
						NodeData linkedNode = getGroupNode(path, false);
						group.addGroupNode(parentPath, parsedNode.name, (GroupNode)linkedNode.node);
					}
				}
			} catch (HDF5LibraryException e) {
				throw new NexusException("Could not query if path is soft link", e);
			}
			if (!group.containsGroupNode(parsedNode.name)) {
				createGroupNode(path.hashCode(), group, parentPath, parsedNode.name, parsedNode.nxClass);
			}
			group = group.getGroupNode(parsedNode.name);
			if (!group.isPopulated()) {
				//make sure cached node looks like node on files
				populateGroupNode(path, group);
			}
			node = group;
		}
		return new NodeData(parsedNode.name, parsedNode.nxClass, parentPath, node, type);
	}

	private NodeType getNodeType(String absolutePath) throws NexusException {
		//TODO: inspect cache first
		try {
			if (!H5.H5Lexists(fileId, absolutePath, HDF5Constants.H5P_DEFAULT)) {
				return null;
			}
			H5O_info_t info = H5.H5Oget_info_by_name(fileId, absolutePath, HDF5Constants.H5P_DEFAULT);
			NodeType type = NodeType.valueOf(info.type);
			if (type == null) {
				throw new NexusException("Unsupported object type");
			}
			return type;
		} catch (HDF5LibraryException e) {
			throw new NexusException("Could not get object information", e);
		}
	}

	private int openNode(String absolutePath) throws NexusException {
		if (!absolutePath.startsWith(Tree.ROOT)) {
			throw new IllegalArgumentException("Group path must be absolute");
		}
		try {
			return H5.H5Oopen(fileId, absolutePath, HDF5Constants.H5P_DEFAULT);
		} catch (HDF5LibraryException e) {
			throw new NexusException("Cannot open object", e);
		}
	}

	private int openGroup(String absolutePath, String nxClass, boolean create) throws NexusException {
		//TODO: Add nxClass as attribute
		NodeType type = getNodeType(absolutePath);
		int groupId;

		if (type == null) {
			if (create && writeable) {
				try {
					groupId = H5.H5Gcreate(fileId, absolutePath,
							HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
				} catch (HDF5LibraryException e) {
					throw new NexusException("Cannot create group", e);
				}
				addAttribute(absolutePath, TreeFactory.createAttribute(NXCLASS, nxClass, false));
			} else {
				throw new NexusException("Group does not exist and cannot be created");
			}
		} else if (type != NodeType.GROUP) {
			throw new NexusException("Specified path is not a group");
		} else {
			groupId = openNode(absolutePath);
		}
		return groupId;
	}

	private int openDataset(String absolutePath) throws NexusException {
		//TODO: Potentially handle external datasets (are they transparent? if they are do we want to cache their data?)
		NodeType type = getNodeType(absolutePath);
		if (type != NodeType.DATASET) {
			throw new NexusException("Path does not refer to dataset");
		}
		return openNode(absolutePath);
	}

	private void closeNode(int nodeId) throws NexusException {
		try {
			H5.H5Oclose(nodeId);
		} catch (HDF5LibraryException e) {
			throw new NexusException("Cannot close node", e);
		}
	}

	private DataNode createDataNode(GroupNode parentNode, String path, String name,
			long[] shape, int datasetType, boolean unsigned, long[] maxShape, long[] chunks) throws NexusException {
		int[] iShape = shape == null ? null : longArrayToIntArray(shape);
		int[] iMaxShape = maxShape == null ? null : longArrayToIntArray(maxShape);
		int[] iChunks = chunks == null ? null : longArrayToIntArray(chunks);
		DataNode dataNode = TreeFactory.createDataNode(path.hashCode());
		cacheAttributes(path, dataNode);
		String parentPath = path.substring(0, path.length() - name.length());
		parentNode.addDataNode(parentPath, name, dataNode);
		dataNode.setUnsigned(unsigned);
		ILazyDataset lazyDataset = null;
		int itemSize = 1;
		boolean extendUnsigned = false;
		byte fill = 0;
		if (writeable) {
			lazyDataset = new LazyWriteableDataset(name, datasetType, iShape, null, null, 
					new HDF5LazySaver(null, fileName, path, name, iShape, itemSize,
							datasetType, extendUnsigned, iMaxShape, iChunks, fill));
		} else {
			lazyDataset = new LazyDataset(name, datasetType, iShape, 
					new HDF5LazyLoader(null, fileName, path, name, iShape, itemSize,
							datasetType, extendUnsigned));
		}
		dataNode.setDataset(lazyDataset);
		if (!testForExternalLink(path)) {
			nodeMap.put(getLinkTarget(path), dataNode);
		}
		return dataNode;
	}

	private DataNode getDataNodeFromFile(String path, GroupNode parentNode, String dataName) throws NexusException {
		if (!testForExternalLink(path)) {
			DataNode node = (DataNode) nodeMap.get(getLinkTarget(path));
			if (node != null) {
				return node;
			}
		}
		long[] shape = null;
		long[] maxShape = null;
		long[] chunks = null;
		Integer datasetType;
		boolean unsigned = false;
		try (HDF5Resource hdfDataset = new HDF5DatasetResource(openDataset(path));
				HDF5Resource hdfDataspace = new HDF5DataspaceResource( H5.H5Dget_space(hdfDataset.getResource()) );
				HDF5Resource hdfDatatype = new HDF5DatatypeResource( H5.H5Dget_type(hdfDataset.getResource()) );
				HDF5Resource hdfNativetype = new HDF5DatatypeResource( H5.H5Tget_native_type(hdfDatatype.getResource()) )) {
			final int datasetId = hdfDataset.getResource();
			int typeRepresentation;
			final int dataspaceId = hdfDataspace.getResource();
			final int nativeTypeId = hdfNativetype.getResource();
			final int dataClass = H5.H5Tget_class(nativeTypeId);
			if (dataClass == HDF5Constants.H5T_STRING || H5.H5Tis_variable_str(nativeTypeId)) {
				//TODO: This is questionable
				typeRepresentation = HDF5Constants.H5T_C_S1;
			} else {
				typeRepresentation = getTypeRepresentation(nativeTypeId);
			}
			unsigned = UNSIGNED_HDF_TYPES.contains(typeRepresentation);
			datasetType = HDF_TYPES_TO_DATASET_TYPES.get(typeRepresentation);
			if (datasetType == null) {
				throw new NexusException("Unknown data type");
			}
			int nDims = H5.H5Sget_simple_extent_ndims(dataspaceId);
			try (HDF5Resource hdfProperties = new HDF5PropertiesResource(H5.H5Dget_create_plist(datasetId))) {
				int propertiesId = hdfProperties.getResource();
				if (H5.H5Pget_layout(propertiesId) == HDF5Constants.H5D_CHUNKED) {
					chunks = new long[nDims];
					H5.H5Pget_chunk(propertiesId, nDims, chunks);
				} else {
					//H5D_COMPACT || H5D_CONTIGUOUS can have chunk array set to the shape
					//or can leave null and dataset stuff assumes the same
				}
			}
			shape = new long[nDims];
			maxShape = new long[nDims];
			H5.H5Sget_simple_extent_dims(dataspaceId, shape, maxShape);
		} catch (HDF5Exception e) {
			throw new NexusException("Error reading dataspace", e);
		}
		return createDataNode(parentNode, path, dataName, shape, datasetType, unsigned, maxShape, chunks);
	}

	@Override
	public DataNode getData(String path) throws NexusException {
		assertOpen();

		//check if the data node itself is a symlink
			//*
		long fileAddr = getLinkTarget(path);
		DataNode dataNode = (DataNode) nodeMap.get(fileAddr);
		if (dataNode != null) {
			return dataNode;
		}

		path = NexusUtils.stripAugmentedPath(path);
		NodeLink link = tree.findNodeLink(path);
		if (link != null) {
			if (link.isDestinationData()) {
				return (DataNode) link.getDestination();
			}
			throw new IllegalArgumentException("Path specified is not for a dataset");
		}

		String dataName = NexusUtils.getName(path);
		//TODO: make sure this works
		String parentPath = path.substring(0, path.lastIndexOf(dataName));
		NodeData parentNodeData = getGroupNode(parentPath, false);
		if (parentNodeData.name == null) {
			return null;
		}

		NodeType nodeType = getNodeType(path);
		if (nodeType == null) {
			throw new NexusException("Path does not point to any object");
		} else if (nodeType != NodeType.DATASET) {
			throw new NexusException("Path points to non-dataset object");
		}

		dataNode = getDataNodeFromFile(path, (GroupNode)parentNodeData.node, dataName);
		return dataNode;
	}

	@Override
	public DataNode getData(GroupNode group, String name) throws NexusException {
		String path = NexusUtils.addToAugmentPath(getPath(group), name, null);
		return getData(path);
	}

	@Override
	public DataNode createData(String path, ILazyWriteableDataset data, int compression, boolean createPathIfNecessary)
			throws NexusException {
		assertCanWrite();
		NodeData parentNode = getGroupNode(path, createPathIfNecessary);
		if (parentNode.name == null) {
			return null;
		}
		String parentPath = path.endsWith(Node.SEPARATOR) ? path : path + Node.SEPARATOR;
		String dataName = data.getName();
		String dataPath = parentPath + dataName;
		if (dataName == null || dataName.isEmpty()) {
			throw new IllegalArgumentException("Dataset name must be defined");
		}
		if (isPathValid(dataPath)) {
			throw new NexusException("Object already exists at specified location");
		}
		int itemSize = 1;
		int dataType = AbstractDataset.getDType(data);
		int[] iShape = data.getShape();
		int[] iMaxShape = data.getMaxShape();
		int[] iChunks = data.getChunking();
		byte[] fillValue = {0};

		long[] shape = intArrayToLongArray(iShape);
		long[] maxShape = intArrayToLongArray(iMaxShape);
		long[] chunks = intArrayToLongArray(iChunks);
		if (!Arrays.equals(shape, maxShape)) {
			//we *must* configure chunking
			chunks = new long[shape.length];
			Arrays.fill(chunks, 1);
		}
		//TODO: compression
		int hdfType = getHDF5Type(data);
		try {
			try (HDF5Resource hdfDatatype = new HDF5DatatypeResource(H5.H5Tcopy(hdfType));
					HDF5Resource hdfDataspace = new HDF5DataspaceResource(H5.H5Screate_simple(shape.length, shape, maxShape));
					HDF5Resource hdfProperties = new HDF5PropertiesResource(H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE))) {

				final int hdfPropertiesId = hdfProperties.getResource();
				final int hdfDatatypeId = hdfDatatype.getResource();
				final int hdfDataspaceId = hdfDataspace.getResource();
				if (chunks != null) {
					H5.H5Pset_fill_value(hdfPropertiesId, hdfDatatypeId, fillValue);
					//these have to be set in this order
					H5.H5Pset_layout(hdfPropertiesId, HDF5Constants.H5D_CHUNKED);
					H5.H5Pset_chunk(hdfPropertiesId, chunks.length, chunks);
				}
				int datasetId = H5.H5Dcreate(fileId, dataPath, hdfDatatypeId, hdfDataspaceId,
						HDF5Constants.H5P_DEFAULT, hdfPropertiesId, HDF5Constants.H5P_DEFAULT);
				H5.H5Dclose(datasetId);
			}
		} catch (HDF5Exception e) {
			throw new NexusException("Could not create dataset", e);
		}

		HDF5LazySaver saver = new HDF5LazySaver(null, fileName, parentPath, dataName,
				iShape, itemSize, dataType, false, iMaxShape, iChunks, fillValue);
		data.setSaver(saver);

		DataNode dataNode = TreeFactory.createDataNode(dataPath.hashCode());
		((GroupNode)parentNode.node).addDataNode(parentPath, dataName, dataNode);
		dataNode.setDataset(data);
		long fileAddr = getLinkTarget(dataPath);
		nodeMap.put(fileAddr, dataNode);
		return dataNode;
	}

	@Override
	public DataNode createData(String path, ILazyWriteableDataset data, boolean createPathIfNecessary)
			throws NexusException {
		return createData(path, data, COMPRESSION_NONE, createPathIfNecessary);
	}

	@Override
	public DataNode createData(GroupNode group, ILazyWriteableDataset data, int compression) throws NexusException {
		String path = getPath(group);
		return createData(path, data, compression, true);
	}

	@Override
	public DataNode createData(GroupNode group, ILazyWriteableDataset data) throws NexusException {
		String path = getPath(group);
		return createData(path, data, true);
	}

	@Override
	public DataNode createData(String path, IDataset data, boolean createPathIfNecessary) throws NexusException {
		assertCanWrite();

		NodeData parentNode = getGroupNode(path, createPathIfNecessary);
		String dataName = data.getName();
		if (dataName == null || dataName.isEmpty()) {
			throw new IllegalArgumentException("Dataset name must be defined");
		}

		String dataPath = parentNode.path + parentNode.name + Node.SEPARATOR + dataName;
		if (isPathValid(dataPath)) {
			throw new NexusException("Object already exists at specified location");
		}

		//TODO: Scalar dataspaces? i.e. rank 0
		boolean stringDataset = data.elementClass().equals(String.class);//ngd.isChar();
		final long[] shape = data.getRank() == 0 ? new long[] {1} : intArrayToLongArray(data.getShape());

		int type = getHDF5Type(data);

		try {
			try (HDF5Resource hdfDatatype = new HDF5DatatypeResource(H5.H5Tcopy(type));
					HDF5Resource hdfDataspace = new HDF5DataspaceResource(
							H5.H5Screate_simple(shape.length, shape, (long[])null))) {

				final int datatypeId = hdfDatatype.getResource();
				final int dataspaceId = hdfDataspace.getResource();
				if (stringDataset) {
					H5.H5Tset_cset(datatypeId, HDF5Constants.H5T_CSET_UTF8);
					H5.H5Tset_size(datatypeId, HDF5Constants.H5T_VARIABLE);
				}
				try (HDF5Resource hdfDataset = new HDF5DatasetResource(
						H5.H5Dcreate(fileId, dataPath, datatypeId, dataspaceId,
								HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT));) {

					final int dataId = hdfDataset.getResource();
					if (stringDataset) {
						String[] strings = (String[])DatasetUtils.serializeDataset(data);
						H5.H5DwriteString(dataId, datatypeId, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, strings);
					} else {
						Serializable buffer = DatasetUtils.serializeDataset(data);
						H5.H5Dwrite(dataId, datatypeId, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, buffer);
					}
				}
			}
		} catch (HDF5Exception e) {
			throw new NexusException("Could not create dataset", e);
		}
		DataNode dataNode = TreeFactory.createDataNode(dataPath.hashCode());
		long fileAddr = getLinkTarget(dataPath);
		nodeMap.put(fileAddr, dataNode);
		dataNode.setDataset(data);
		((GroupNode) parentNode.node).addDataNode(dataPath, dataName, dataNode);
		return dataNode;
	}

	@Override
	public DataNode createData(GroupNode group, IDataset data) throws NexusException {
		String path = getPath(group);
		return createData(path, data, true);
	}

	@Override
	public Attribute createAttribute(IDataset attr) {
		assertOpen();
		Attribute a = TreeFactory.createAttribute(attr.getName());
		a.setValue(attr);
		return a;
	}

	@Override
	public void addAttribute(String path, Attribute... attribute) throws NexusException {
		assertCanWrite();
		Charset utf8 = Charset.forName("UTF-8");
		for (Attribute attr : attribute) {
			String attrName = attr.getName();
			if (attrName == null || attrName.isEmpty()) {
				throw new IllegalArgumentException("Attribute must have a name");
			}
			IDataset attrData = attr.getValue();
			int baseHdf5Type = getHDF5Type(attrData);
			final long[] shape = attrData.getRank() == 0 ? new long[] {1} : intArrayToLongArray(attrData.getShape());
			try {
				try (HDF5Resource typeResource = new HDF5DatatypeResource(H5.H5Tcopy(baseHdf5Type));
						HDF5Resource spaceResource = new HDF5DataspaceResource(
								H5.H5Screate_simple(shape.length, shape, shape))) {

					int datatypeId = typeResource.getResource();
					int dataspaceId = spaceResource.getResource();
					boolean stringDataset = attrData.elementClass().equals(String.class);
					Serializable buffer = DatasetUtils.serializeDataset(attrData);
					if (stringDataset) {
						String[] strings = (String[]) buffer;
						int strCount = strings.length;
						int maxLength = 0;
						byte[][] stringbuffers = new byte[strCount][];
						int i = 0;
						for (String str : strings) {
							stringbuffers[i] = str.getBytes(utf8);
							int l = stringbuffers[i].length;
							if (l > maxLength) maxLength = l;
							i++;
						}
						maxLength++; //we require null terminators
						buffer = new byte[maxLength * strCount];
						int offset = 0;
						for (byte[] str: stringbuffers) {
							System.arraycopy(str, 0, buffer, offset, str.length);
							offset += maxLength;
						}

						H5.H5Tset_cset(datatypeId, HDF5Constants.H5T_CSET_UTF8);
						H5.H5Tset_size(datatypeId, maxLength);
					}
					try (HDF5Resource attributeResource = new HDF5AttributeResource(
							H5.H5Acreate_by_name(fileId, path, attrName, datatypeId, dataspaceId,
									HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT))) {

						if (stringDataset) {
							H5.H5Awrite(attributeResource.getResource(), datatypeId, buffer);
						} else {
							H5.H5Awrite(attributeResource.getResource(), datatypeId, buffer);
						}
					}
				}
			} catch (HDF5Exception e) {
				throw new NexusException("Could not create attribute", e);
			}
			Node node = getNode(path, false).node;
			node.addAttribute(attr);
		}
	}

	@Override
	public void addAttribute(Node node, Attribute... attribute) throws NexusException {
		String path = getPath(node);
		addAttribute(path, attribute);
	}

	private void createSoftLink(String source, String destination) throws NexusException {
		boolean useNameAtSource = destination.endsWith(Node.SEPARATOR);
		String linkName = destination;
		if (!useNameAtSource) {
			destination = destination.substring(0, destination.lastIndexOf(Node.SEPARATOR));
			if (destination.isEmpty()) destination = Tree.ROOT;
		} else {
			int index = source.lastIndexOf(Node.SEPARATOR);
			linkName += source.substring(index);
		}
		getGroupNode(destination, true);
		try {
			H5.H5Lcreate_soft(source, fileId, linkName, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		} catch (HDF5LibraryException e) {
			throw new NexusException("Could not create hard link", e);
		}
	}

	private void createHardLink(String source, String destination) throws NexusException {
		boolean useNameAtSource = destination.endsWith(Node.SEPARATOR);
		NodeData sourceData = getNode(source, false);
		if (sourceData.name == null) {
			throw new IllegalArgumentException("Source does not exist");
		}
		String linkName = destination;
		String nodeName;
		if (!useNameAtSource) {
			destination = destination.substring(0, destination.lastIndexOf(Node.SEPARATOR));
			nodeName = source.substring(source.lastIndexOf(Node.SEPARATOR));
			if (destination.isEmpty()) destination = Tree.ROOT;
		} else {
			int index = source.lastIndexOf(Node.SEPARATOR);
			nodeName = source.substring(index);
			linkName += nodeName;
		}

		GroupNode destNode = (GroupNode)getGroupNode(destination, true).node;
		switch(sourceData.type) {
		case DATASET:
			destNode.addDataNode(destination, nodeName, (DataNode) sourceData.node);
			break;
		case GROUP:
			destNode.addGroupNode(destination, nodeName, (GroupNode) sourceData.node);
			break;
		}
		try {
			H5.H5Lcreate_hard(fileId, source, fileId, linkName, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		} catch (HDF5LibraryException e) {
			throw new NexusException("Could not create hard link", e);
		}
	}

	@Override
	public void link(String source, String destination) throws NexusException {
		assertCanWrite();
		createHardLink(source, destination);
	}

	@Override
	public void linkExternal(URI source, String destination, boolean isGroup) throws NexusException {
		//creates a soft link *at* destination *to* source
		assertCanWrite();
		boolean useNameAtSource = destination.endsWith(Node.SEPARATOR);
		String linkName = destination;
		//TODO: add class attribute
		String linkClass;
		String externalFileName = source.getPath();
		String externalNexusPath = source.getFragment();
		if (externalFileName == null || externalFileName.isEmpty()) {
			createSoftLink(externalNexusPath, destination);
			return;
		}
		if (!externalNexusPath.startsWith(Tree.ROOT)) {
			externalNexusPath = Tree.ROOT + externalNexusPath;
		}

		if (!useNameAtSource) {
			destination = destination.substring(0, destination.lastIndexOf(Node.SEPARATOR));
			if (destination.isEmpty()) destination = Tree.ROOT;
		}
		NodeData destinationGroup = getGroupNode(destination, true);
		linkClass = destinationGroup.nxClass;
		if (useNameAtSource) {
			int index = externalNexusPath.lastIndexOf(Node.SEPARATOR);
			linkName += externalNexusPath.substring(index);
		}
		try {
			H5.H5Lcreate_external(externalFileName, externalNexusPath, fileId, linkName, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
		} catch (HDF5LibraryException e) {
			throw new NexusException("Could not create external link", e);
		}
	}

	@Override
	public void flush() throws NexusException {
		if (fileId < 0) {
			return;
		}
		try {
			H5.H5Fflush(fileId, HDF5Constants.H5F_SCOPE_GLOBAL);
		} catch (HDF5LibraryException e) {
			throw new NexusException("Cannot flush file", e);
		}
		return;
	}

	@Override
	public void close() throws NexusException {
		if (fileId < 0) {
			return;
		}
		try {
			//TODO: check everything is really closed
			H5.H5Fclose(fileId);
			fileId = -1;
			tree = null;
			nodeMap = null;
			writeable = false;
		} catch (HDF5LibraryException e) {
			throw new NexusException("Cannot close file", e);
		}
	}

	@Override
	public boolean isPathValid(String path) {
		try {
			return H5.H5Lexists(fileId, path, HDF5Constants.H5P_DEFAULT);
		} catch (HDF5LibraryException e) {
			return false;
		}
	}

	private static long IS_EXTERNAL_LINK = -4370;

	private boolean testForExternalLink(String path) throws NexusException {
		//TODO: might want to cache results
		ParsedNode[] parsedNodes = parseAugmentedPath(path);
		StringBuilder currentPath = new StringBuilder(Tree.ROOT);
		for (ParsedNode node : parsedNodes) {
			if (node.name.isEmpty()) {
				continue;
			}
			currentPath.append(Node.SEPARATOR);
			currentPath.append(node.name);
			if (getLinkTarget(currentPath.toString()) == IS_EXTERNAL_LINK) {
				return true;
			}
		}
		return false;
	}
	private long getLinkTarget(String path) throws NexusException {
		try {
			H5L_info_t linkInfo = H5.H5Lget_info(fileId, path, HDF5Constants.H5P_DEFAULT);
			if (linkInfo.type == HDF5Constants.H5L_TYPE_SOFT) {
				String[] name = new String[2];
				H5.H5Lget_val(fileId, path, name, HDF5Constants.H5P_DEFAULT);
				return getLinkTarget(name[0]);
			} else if (linkInfo.type == HDF5Constants.H5L_TYPE_HARD) {
				//return nodeMap.get(linkInfo.address_val_size);
				return linkInfo.address_val_size;
			} else if (linkInfo.type == HDF5Constants.H5L_TYPE_EXTERNAL) {
				return IS_EXTERNAL_LINK;
			}
			throw new NexusException("Unhandled link type");
		} catch (HDF5LibraryException e) {
			throw new NexusException("Could not get link target", e);
		}
	}

	private static int getHDF5Type(ILazyDataset data) {
		Class<?> clazz = data.elementClass();
		if (clazz.equals(String.class)) {
			return HDF5Constants.H5T_C_S1;
		} else if (clazz.equals(Byte.class)) {
			return HDF5Constants.H5T_NATIVE_INT8;
		} else if (clazz.equals(Short.class)) {
			return HDF5Constants.H5T_NATIVE_INT16;
		} else if (clazz.equals(Integer.class)) {
			return HDF5Constants.H5T_NATIVE_INT32;
		} else if (clazz.equals(Long.class)) {
			return HDF5Constants.H5T_NATIVE_INT64;
		} else if (clazz.equals(Float.class)) { 
			return HDF5Constants.H5T_NATIVE_FLOAT;
		} else if (clazz.equals(Double.class)) {
			return HDF5Constants.H5T_NATIVE_DOUBLE;
		}
		throw new IllegalArgumentException("Invalid datatype requested");
	}

	private static long[] intArrayToLongArray(int[] intArray) {
		if (intArray == null) return null;
		long[] longArray = new long[intArray.length];
		for (int i = 0; i < intArray.length; i++) {
			longArray[i] = intArray[i];
		}
		return longArray;
	}

	private static int[] longArrayToIntArray(long[] longArray) {
		int[] intArray = new int[longArray.length];
		for (int i = 0; i < intArray.length; i++) {
			long value = longArray[i];
			if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
				throw new IllegalArgumentException("Cannot convert to int array without data loss");
			}
			intArray[i] = (int)value;
		}
		return intArray;
	}
}
