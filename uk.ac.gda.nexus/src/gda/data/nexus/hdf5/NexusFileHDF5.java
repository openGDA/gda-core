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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
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

	private static final Logger logger = LoggerFactory.getLogger(NexusFile.class);

	private int fileId = -1;

	private String fileName;

	private TreeFile tree;

	private boolean writeable = false;

	public NexusFileHDF5(String path) {
		fileName = path;
	}

	private void initializeTree() {
		if (tree == null) {
			tree = TreeFactory.createTreeFile(fileName.hashCode(), fileName);
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
					//TODO: copy attributes from link to g
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
		GroupNode g = TreeFactory.createGroupNode(oid);
		if (nxClass != null && !nxClass.isEmpty()) {
			g.addAttribute(TreeFactory.createAttribute(NXCLASS, nxClass, false));
		}
		//TODO copy Attributes from node in file to tree node (g)
		group.addGroupNode(path, name, g);
	}

	private void populateGroupNode(String path, GroupNode group) throws NexusException {
		//TODO: Populate with stuff
		return;
	}

	private NodeData getGroupNode(String augmentedPath, boolean createPathIfNecessary) throws NexusException {
		NodeData node = getNode(augmentedPath, createPathIfNecessary);
		if (node.type != NodeType.GROUP) {
			throw new NexusException("Found dataset node instead of group node");
		}
		return node;
	}

	//TODO: When would this be used?
	private NodeData getDatasetNode(String augmentedPath) throws NexusException {
		NodeData node = getNode(augmentedPath, false);
		if (node.type != NodeType.DATASET) {
			throw new NexusException("Found group node instead of dataset node");
		}
		return node;
	}

	//TODO: What does this function actually *want* to do?
	//Should *NOT* call H5Xopen on anything without also closing it
	private NodeData getNode(String augmentedPath, boolean createPathIfNecessary) throws NexusException {
		ParsedNode[] parsedNodes = parseAugmentedPath(augmentedPath);
		if (parsedNodes.length < 1) {
			throw new IllegalArgumentException("Invalid path specified");
		}
		StringBuilder pathBuilder = new StringBuilder(Tree.ROOT);
		String parentPath = pathBuilder.toString();
		GroupNode group = tree.getGroupNode();
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
		int[] iChunks = chunks == null ? null : longArrayToIntArray(chunks);;
		DataNode dataNode = TreeFactory.createDataNode(path.hashCode());
		//TODO: copy attributes
		String parentPath = path.substring(0, path.length() - name.length());
		parentNode.addDataNode(parentPath, name, dataNode);
		dataNode.setUnsigned(unsigned);
		//TODO: String stuff
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
		return dataNode;
	}

	@Override
	public DataNode getData(String path) throws NexusException {
		assertOpen();

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
		return createDataNode((GroupNode) parentNodeData.node, path, dataName, shape, datasetType, unsigned, maxShape, chunks);
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
		String parentPath = parentNode.path + parentNode.name;
		String dataName = data.getName();
		String dataPath = parentPath + Node.SEPARATOR + dataName;
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
					H5.H5Tset_size(datatypeId, HDF5Constants.H5T_VARIABLE);
					H5.H5Tset_size(datatypeId, HDF5Constants.H5T_CSET_UTF8);
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
		// TODO Auto-generated method stub
		assertOpen();
		return null;
	}

	@Override
	public void addAttribute(String path, Attribute... attribute) throws NexusException {
		// TODO Auto-generated method stub
		assertCanWrite();
	}

	@Override
	public void addAttribute(Node node, Attribute... attribute) throws NexusException {
		String path = getPath(node);
		addAttribute(path, attribute);
	}

	@Override
	public void link(String source, String destination) throws NexusException {
		// TODO Auto-generated method stub
		assertCanWrite();
	}

	@Override
	public void linkExternal(URI source, String destination, boolean isGroup) throws NexusException {
		// TODO Auto-generated method stub
		assertCanWrite();
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
