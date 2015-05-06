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

package gda.data.nexus.napi;

import gda.data.nexus.NexusUtils;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map.Entry;

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
import org.nexusformat.AttributeEntry;
import org.nexusformat.NXlink;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexusFileNAPI implements org.eclipse.dawnsci.hdf5.nexus.NexusFile {
	/**
	 * Maximum length of any text string encoded as bytes
	 */
	private static final int MAX_TEXT_LENGTH = 255;
	private static final Logger logger = LoggerFactory.getLogger(org.eclipse.dawnsci.hdf5.nexus.NexusFile.class);
	private String filename;

	private NexusFile file;
	private boolean canWrite = false;

	private TreeFile tree; // acts as a cache

	private boolean debug;

	/**
	 * @param name
	 */
	public NexusFileNAPI(String name) {
		filename = name;
	}

	private void initTree() {
		if (tree == null) {
			tree = TreeFactory.createTreeFile(filename.hashCode(), filename);
		} else {
			throw new IllegalStateException("File is already opened");
		}
	}

	@Override
	public void openToRead() throws NexusException {
		canWrite = false;
		try {
			file = new NexusFile(filename, NexusFile.NXACC_READ);
			initTree();
		} catch (org.nexusformat.NexusException e) {
			logger.error("Cannot open to read: {}", filename, e);
			throw new NexusException("Cannot open to read", e);
		}
	}

	@Override
	public void openToWrite(boolean createIfNecessary) throws NexusException {
		try {
			if (Files.exists(Paths.get(filename))) {
				file = new NexusFile(filename, NexusFile.NXACC_RDWR);
				initTree();
				canWrite = true;
				return;
			} else if (createIfNecessary) {
				file = new NexusFile(filename, NexusFile.NXACC_CREATE5);
				initTree();
				canWrite = true;
				return;
			}
		} catch (org.nexusformat.NexusException e) {
			logger.error("Cannot open to write: {}", filename, e);
			throw new NexusException("Cannot open to write", e);
		}
		logger.error("File not found and not created: {}", filename);
		throw new NexusException("File not found and not created");
	}

	@Override
	public void createAndOpenToWrite() throws NexusException {
		try {
			file = new NexusFile(filename, NexusFile.NXACC_CREATE5);
			initTree();
			canWrite = true;
			return;
		} catch (org.nexusformat.NexusException e) {
			logger.error("Cannot create: {}", filename, e);
			throw new NexusException("Cannot create", e);
		}
	}

	@Override
	public void flush() throws NexusException {
		try {
			file.flush();
		} catch (org.nexusformat.NexusException e) {
			logger.error("Problem flushing: {}", filename, e);
			throw new NexusException("Problem flushing", e);
		}
	}

	@Override
	public void close() throws NexusException {
		try {
			file.close();
			tree = null;
		} catch (org.nexusformat.NexusException e) {
			logger.error("Cannot close: {}", filename, e);
			throw new NexusException("Cannot close", e);
		}
	}

	private void checkClosed(boolean checkForWrite) {
		if (tree == null) {
			throw new IllegalStateException("File has been closed");
		}
		if (checkForWrite && !canWrite) {
			logger.error("Cannot write as opened read-only");
			throw new IllegalStateException("Cannot write as opened read-only");
		}
	}

	@Override
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	@Override
	public GroupNode getGroup(String path, boolean createPathIfNecessary) throws NexusException {
		checkClosed(false);
		String bpath = NexusUtils.stripAugmentedPath(path);
		NodeLink link = tree.findNodeLink(bpath);
		if (link != null) {
			if (link.isDestinationGroup()) {
				GroupNode g = (GroupNode) link.getDestination();
				if (!g.isPopulated()) {
					if (!bpath.endsWith(Node.SEPARATOR)) {
						bpath = bpath + Node.SEPARATOR;
					}
					try {
						file.openpath(bpath);
					} catch (org.nexusformat.NexusException e) {
						logger.error("Cannot open to group: {}", bpath, e);
						throw new NexusException("Cannot open to group", e);
					}
					populate(bpath, g);
				}
				return g;
			}
			logger.error("Path specified is not for a group");
			throw new NexusException("Path specified is not for a group");
		}
		Tuple<String, GroupNode, Node> tuple = openAll(path, createPathIfNecessary, true);
		if (tuple.name == null)
			return null;

		return tuple.group;
	}

	@Override
	public GroupNode getGroup(GroupNode group, String name, String nxClass, boolean createPathIfNecessary) throws NexusException {
		String path = getPath(group);
		if (path == null)
			throw new NullPointerException("Group was null");
		return getGroup(NexusUtils.addToAugmentPath(path, name, nxClass), createPathIfNecessary);
	}

	@Override
	public String getPath(Node node) {
		return TreeUtils.getPath(tree, node);
	}

	private String[][] parseAugmentedPath(String path) {
		if (!path.startsWith(Tree.ROOT)) {
			throw new IllegalArgumentException("Path specified must be absolute");
		}
		String[] parts = path.split(Node.SEPARATOR);
		String[][] pairs = new String[parts.length][];
	
		int i = 0;
		for (String p : parts) {
			pairs[i++] = p.split(NXCLASS_SEPARATOR, 2);
		}
		return pairs;
	}

	private static final String SDS = "SDS";

	class Tuple<S, G, N> {
		S name;
		S clazz;
		S path;
		G group;
		N node;
		public Tuple(S t, S v, S p, G u, N x) {
			this.name = t;
			this.clazz = v;
			this.path = p;
			this.group = u;
			this.node = x;
		}
	}

	/**
	 * Open all nodes in path
	 * @param path
	 * @param createPathIfNecessary
	 * @param toBottom
	 * @return name, NeXus class, group, path (with {@value Node#SEPARATOR}), group, bottom node
	 * @throws NexusException 
	 */
	private Tuple<String, GroupNode, Node> openAll(String path, boolean createPathIfNecessary, boolean toBottom) throws NexusException {
		String[][] pairs = parseAugmentedPath(path);
		int imax = pairs.length;
		if (!toBottom)
			--imax;

		GroupNode group = tree.getGroupNode();
		try {
			file.openpath(Tree.ROOT);
		} catch (org.nexusformat.NexusException e) {
			logger.error("Cannot open root: {}", filename, e);
			throw new NexusException("Cannot open root", e);
		}
		String name = Tree.ROOT;
		StringBuilder cpath = new StringBuilder(Tree.ROOT);
		String ppath = cpath.toString();
		if (!group.isPopulated()) {
			populate(ppath, group);
		}
		String clazz = null;
		int i = 1;
		String[] pair = null;
		for (; i < imax; i++) { // miss out first as it is blank
			pair = pairs[i];
			name = pair[0];
			if (name.isEmpty())
				continue;
			clazz = pair.length < 2 ? "" : pair[1];
			if (!openGroup(name, clazz, createPathIfNecessary)) {
				name = null;
				break;
			}
			cpath.append(name);
			cpath.append(Node.SEPARATOR);
			String gpath = cpath.toString();
			if (!group.isPopulated()) {
				populate(gpath, group);
			}
			if (!group.containsGroupNode(name)) {
				GroupNode g = TreeFactory.createGroupNode(cpath.hashCode());
				if (clazz != null && !clazz.isEmpty())
					g.addAttribute(TreeFactory.createAttribute(tree, name, NXCLASS, clazz, false));
				copyAttributes(name, g);
				group.addGroupNode(tree, ppath, name, g);
			}
			group = group.getGroupNode(name);
			ppath = gpath;
		}
		if (toBottom && !group.isPopulated()) {
			populate(ppath, group);
		}
		Node node = null;
		if (toBottom && name == null && i == imax - 1 && pair != null) {
			name = pair[0];
			NodeLink link = group.getNodeLink(name);
			node = link.getDestination();
			if (link.isDestinationData()) {
				openDataset(name);
			}
		}
		if (toBottom && node == null) {
			node = group;
		}
		if (name != null && !toBottom) {
			name = pairs[imax][0];
		}
		return new Tuple<String, GroupNode, Node>(name, clazz, ppath, group, node);
	}

	/**
	 * Populate group
	 * @param path path to group (ends with {@link Node#SEPARATOR})
	 * @param group
	 * @throws NexusException
	 */
	private void populate(String path, GroupNode group) throws NexusException {
		try {
			@SuppressWarnings("unchecked")
			Hashtable<String, String> map = file.groupdir();
			for (Entry<String, String> e : map.entrySet()) {
				String n = e.getKey();
				String c = e.getValue();
				String npath = path + n;
				if (c.equals(SDS) && openDataset(n)) {
					createDataNode(group, npath, n);
				} else {
					GroupNode g = TreeFactory.createGroupNode(npath.hashCode());
					Attribute a = TreeFactory.createAttribute(tree, n, NXCLASS, c, false);
					g.addAttribute(a);
					copyAttributes(n, g);
					group.addGroupNode(tree, path, n, g);
				}
			}
		} catch (org.nexusformat.NexusException e) {
			logger.error("Problem populating group: {}", path, e);
			throw new NexusException("Problem populating group", e);
		}
	}

	private boolean openGroup(String name, String nxClass, boolean create) throws NexusException {
		try {
			String url = file.isexternalgroup(name, nxClass);
			if (url == null) {
				if (debug) {
					logger.debug("Opening group: {}:{}", name, nxClass);
				}
				file.opengroup(name, nxClass);
			} else {
				return false;
			}
		} catch (org.nexusformat.NexusException e) {
			if (!create || !canWrite) {
				return false;
			}
			if (debug) {
				logger.debug("Creating group: {}:{}", name, nxClass);
			}
			try {
				file.makegroup(name, nxClass);
				file.opengroup(name, nxClass);
			} catch (org.nexusformat.NexusException e1) {
				logger.error("Cannot create group: {}", name, e1);
				throw new NexusException("Cannot create group", e1);
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private void copyAttributes(String name, Node node) throws NexusException {
		Hashtable<String, AttributeEntry> map;
		try {
			map = file.attrdir();
		} catch (org.nexusformat.NexusException e) {
			logger.error("Problem getting attributes: {}", name, e);
			throw new NexusException("Problem getting attributes", e);
		}
		for (Entry<String, AttributeEntry> sa: map.entrySet()) {
			node.addAttribute(createAttribute(name, sa.getKey(), sa.getValue()));
		}
	}

	private Attribute createAttribute(String name, String n, AttributeEntry e) throws NexusException {
		Object s;
		try {
			s = file.getattr(n);
		} catch (org.nexusformat.NexusException e1) {
			logger.error("Problem getting attribute: {}", n, e);
			throw new NexusException("Problem getting attribute", e1);
		}
		Attribute a = TreeFactory.createAttribute(tree, name, n, s, false);
		if (!a.isString()) {
			IDataset d = a.getValue();
			d.setShape(e.dim);
		}
		a.setTypeName(parseTypeName(e.type));
		return a;
	}

	private String parseTypeName(int type) {
		switch (type) {
		case NexusFile.NX_CHAR:
			return "String";
		case NexusFile.NX_INT8:
			return "INT8";
		case NexusFile.NX_INT16:
			return "INT16";
		case NexusFile.NX_INT32:
			return "INT32";
		case NexusFile.NX_INT64:
			return "INT64";
		case NexusFile.NX_FLOAT32:
			return "FLOAT32";
		case NexusFile.NX_FLOAT64:
			return "FLOAT64";
		}
		return "";
	}

	@Override
	public DataNode getData(String path) throws NexusException {
		checkClosed(false);
		NodeLink link = tree.findNodeLink(NexusUtils.stripAugmentedPath(path));
		if (link != null) {
			if (link.isDestinationData())
				return (DataNode) link.getDestination();
			throw new IllegalArgumentException("Path specified is not for a dataset");
		}

		Tuple<String, GroupNode, Node> tuple = openAll(path, false, false);
		String name = tuple.name;
		if (name == null)
			return null;
		if (!openDataset(name))
			return null; // TODO external dataset

		return createDataNode(tuple.group, tuple.path + name, name);
	}

	@Override
	public DataNode getData(GroupNode group, String name) throws NexusException {
		String path = NexusUtils.addToAugmentPath(getPath(group), name, null);
		return getData(path);
	}

	private DataNode createDataNode(GroupNode g, String path, String name) throws NexusException {
		DataNode dataNode = TreeFactory.createDataNode(path.hashCode());
		copyAttributes(name, dataNode);
		String p = path.substring(0, path.length() - name.length());
		g.addDataNode(tree, p, name, dataNode);
		int[] tShape = new int[10];
		int[] args = new int[10];
		try {
			file.getinfo(tShape, args);
		} catch (org.nexusformat.NexusException e) {
			logger.error("Problem getting info on dataset: {}", name, e);
			throw new NexusException("Problem getting info on dataset", e);
		}

		closeDataset(name);

		int dtype = getDtype(args[1]);
		int rank = args[0];
		int[] shape;
		if (dtype == Dataset.STRING && rank == 1) { // for strings, ignore final dimension (NAPI stored them as fixed size strings)
			dataNode.setMaxStringLength(tShape[0]);
			shape = new int[] { 1 };
		} else {
			if (dtype == Dataset.STRING) {
				rank--;
				dataNode.setMaxStringLength(tShape[rank]);
			}
			shape = new int[rank];
			for (int i = 0; i < rank; i++) {
				shape[i] = tShape[i];
			}
		}
		ILazyDataset lazy = null;
		if (canWrite) {
			lazy = new LazyWriteableDataset(name, dtype, shape, null, null, new NAPILazySaver(tree, p, name, shape, dtype));
		} else {
			lazy = new LazyDataset(name, dtype, shape, new NAPILazyLoader(tree, p, name, shape, dtype));
		}
		dataNode.setDataset(lazy);
		return dataNode;
	}

	private int getDtype(int type) {
		switch (type) {
		case NexusFile.NX_BOOLEAN:
			return Dataset.BOOL;
		case NexusFile.NX_CHAR:
			return Dataset.STRING;
		case NexusFile.NX_INT8:
			return Dataset.INT8;
		case NexusFile.NX_INT16:
			return Dataset.INT16;
		case NexusFile.NX_INT32:
			return Dataset.INT32;
		case NexusFile.NX_INT64:
			return Dataset.INT64;
		case NexusFile.NX_FLOAT32:
			return Dataset.FLOAT32;
		case NexusFile.NX_FLOAT64:
			return Dataset.FLOAT64;
		}
		return 0;
	}

	/**
	 * @param name
	 * @return false if it is external
	 * @throws NexusException
	 */
	private boolean openDataset(String name) throws NexusException {
		try {
			String url = file.isexternaldataset(name);
			if (url == null) {
				if (debug) {
					logger.debug("Opening dataset: {}", name);
				}
				file.opendata(name);
			} else {
				return false;
			}
		} catch (org.nexusformat.NexusException e) {
			logger.error("Cannot open dataset: {}", name, e);
			throw new NexusException("Cannot open dataset", e);
		}
		return true;
	}

	private boolean closeDataset(String name) throws NexusException {
		try {
			if (debug) {
				logger.debug("Closing dataset: {}", name);
			}
			file.closedata();
		} catch (org.nexusformat.NexusException e) {
			logger.error("Cannot close dataset: {}", name, e);
			throw new NexusException("Cannot close dataset", e);
		}
		return true;
	}

	@Override
	public DataNode createData(String path, ILazyWriteableDataset data, boolean createPathIfNecessary) throws NexusException {
		return createData(path, data, COMPRESSION_NONE, createPathIfNecessary);
	}

	@Override
	public DataNode createData(String path, ILazyWriteableDataset data, int compression, boolean createPathIfNecessary) throws NexusException {
		checkClosed(true);
		Tuple<String, GroupNode, Node> tuple = openAll(path, createPathIfNecessary, true);
		if (tuple.name == null)
			return null;

		String name = data.getName();
		if (name == null || name.isEmpty()) {
			throw new NullPointerException("Dataset name must be defined");
		}
		int type = getType(data);

		NAPILazySaver saver = new NAPILazySaver(tree, tuple.path, name, data.getShape(), AbstractDataset.getDType(data));
		data.setSaver(saver);
		int rank;
		int[] mShape;
		int[] chunks;
		if (type == NexusFile.NX_CHAR) {
			saver.setMaxTextLength(MAX_TEXT_LENGTH);
			rank = data.getRank() + 1;
			mShape = data.getMaxShape();
			if (mShape != null) {
				mShape = Arrays.copyOf(mShape, rank);
				mShape[rank - 1] = MAX_TEXT_LENGTH;
			}
			chunks = data.getChunking();
			if (chunks != null) {
				chunks = Arrays.copyOf(chunks, rank);
				chunks[rank - 1] = MAX_TEXT_LENGTH;
			}
		} else {
			rank = data.getRank();
			mShape = data.getMaxShape();
			chunks = data.getChunking();
		}
		try {
			if (compression == COMPRESSION_NONE) {
				if (debug) {
					logger.debug("Creating dataset: {}", name);
				}
				file.makedata(name, type, rank, mShape);
			} else {
				int cmp;
				switch (compression) {
				case COMPRESSION_LZW_L1:
					cmp = NexusFile.NX_COMP_LZW_LVL1;
					break;
				default:
					cmp = NexusFile.NX_COMP_NONE;
					break;
				}
				if (debug) {
					logger.debug("Creating compressed dataset: {}", name);
				}
				file.compmakedata(name, type, rank, mShape, cmp, chunks);
			}
		} catch (org.nexusformat.NexusException e) {
			logger.error("Cannot create dataset: {}", name, e);
			throw new NexusException("Cannot create dataset", e);
		}
		String dpath = tuple.path + name;
		DataNode dataNode = TreeFactory.createDataNode(dpath.hashCode());
		tuple.group.addDataNode(tree, tuple.path, name, dataNode);

		dataNode.setDataset(data);
		return dataNode;
	}

	/**
	 * @param data
	 * @return NAPI dataset type
	 */
	static int getType(ILazyDataset data) {
		Class<?> clazz = data.elementClass();
		if (clazz.equals(String.class)) {
			return NexusFile.NX_CHAR;
		} else if (clazz.equals(Byte.class)) {
			return NexusFile.NX_INT8;
		} else if (clazz.equals(Short.class)) {
			return NexusFile.NX_INT16;
		} else if (clazz.equals(Integer.class)) {
			return NexusFile.NX_INT32;
		} else if (clazz.equals(Long.class)) {
			return NexusFile.NX_INT64;
		} else if (clazz.equals(Float.class)) {
			return NexusFile.NX_FLOAT32;
		} else if (clazz.equals(Double.class)) {
			return NexusFile.NX_FLOAT64;
		}
		return 0;
	}

	@Override
	public DataNode createData(GroupNode group, ILazyWriteableDataset data) throws NexusException {
		String path = getPath(group);
		return createData(path, data, true);
	}

	@Override
	public DataNode createData(GroupNode group, ILazyWriteableDataset data, int compression) throws NexusException {
		String path = getPath(group);
		return createData(path, data, compression, true);
	}

	@Override
	public DataNode createData(GroupNode group, IDataset data) throws NexusException {
		String path = getPath(group);
		return createData(path, data, true);
	}

	@Override
	public DataNode createData(String path, IDataset data, boolean createPathIfNecessary) throws NexusException {
		checkClosed(true);
		Tuple<String, GroupNode, Node> tuple = openAll(path, createPathIfNecessary, true);
		if (tuple.name == null)
			return null;

		String name = data.getName();
		if (name == null || name.isEmpty()) {
			throw new NullPointerException("Dataset name must be defined");
		}
		try {
			if (debug) {
				logger.debug("Creating and populating dataset: {}", name);
			}
			file.makedata(name, getType(data), data.getRank(), data.getShape());
			file.opendata(name);
			file.putdata(DatasetUtils.serializeDataset(data));
			file.closedata();
		} catch (org.nexusformat.NexusException e) {
			logger.error("Cannot create and populate dataset: {}", name, e);
			throw new NexusException("Cannot create and populate dataset", e);
		}

		String dpath = tuple.path + name;
		DataNode dataNode = TreeFactory.createDataNode(dpath.hashCode());
		tuple.group.addDataNode(tree, tuple.path, name, dataNode);

		dataNode.setDataset(data);
		return dataNode;
	}

	@Override
	public Attribute createAttribute(String node, IDataset attr) {
		checkClosed(false);
		Attribute a = TreeFactory.createAttribute(tree, node, attr.getName());
		a.setValue(attr);
		return a;
	}

	@Override
	public Attribute createAttribute(Node node, IDataset attr) {
		String path = getPath(node);
		return createAttribute(path.substring(path.lastIndexOf(Node.SEPARATOR)), attr);
	}

	@Override
	public void addAttribute(Node node, Attribute... attribute) throws NexusException {
		String path = getPath(node);
		addAttribute(path, attribute);
	}

	@Override
	public void addAttribute(String path, Attribute... attribute) throws NexusException {
		checkClosed(true);

		Tuple<String, GroupNode, Node> tuple = openAll(path, false, true);
		if (tuple.name == null)
			return;

		try {
			if (debug) {
				logger.debug("Putting attributes in {}:", tuple.name);
			}
			for (Attribute a : attribute) {
				if (debug) {
					logger.debug("\t{}", a.getName());
				}
				tuple.node.addAttribute(a);
				IDataset d = a.getValue();
				if (d.elementClass().equals(String.class)) {
					file.putattr(a.getName(), d.getString().getBytes(), NexusFile.NX_CHAR);
				} else {
					file.putattr(a.getName(), DatasetUtils.serializeDataset(d), getType(d));
				}
			}
		} catch (org.nexusformat.NexusException e) {
			logger.error("Problem adding attributes: {}", attribute, e);
			throw new NexusException("Problem adding attributes", e);
		} finally {
			if (tuple.node instanceof DataNode) {
				closeDataset(tuple.name);
			}
		}
	}

	@Override
	public void link(String source, String destination) throws NexusException {
		checkClosed(true);

		Tuple<String, GroupNode, Node> tuple = openAll(source, false, false);
		String sname = tuple.name;
		if (sname == null) {
			throw new IllegalArgumentException("Source does not exist");
		}
		if (!tuple.group.isPopulated()) {
			populate(source.substring(0, source.length() - sname.length()), tuple.group);
		}
		NodeLink l = tuple.group.getNodeLink(sname);
		NXlink t = null;
		try {
			if (l.isDestinationData()) {
				openDataset(sname);
				t = file.getdataID();
			} else if (l.isDestinationGroup()) {
				file.opengroup(sname, tuple.clazz);
				t = file.getgroupID();
			}
		} catch (org.nexusformat.NexusException e) {
			logger.error("Problem getting source: {}", sname, e);
			throw new NexusException("Problem getting source", e);
		}
		if (t == null) {
			throw new IllegalArgumentException("Could not get link information as source was a link");
		}

		boolean useSourceName = destination.endsWith(Node.SEPARATOR);
		tuple = openAll(destination, true, useSourceName);
		if (!useSourceName)
			sname = tuple.name;

		try {
			if (debug) {
				logger.debug("Creating link: {}", t.targetPath);
			}
			file.makenamedlink(sname, t);
		} catch (org.nexusformat.NexusException e) {
			logger.error("Problem creating link: {}", sname, e);
			throw new NexusException(e);
		}
	}

	@Override
	public void linkExternal(URI source, String destination, boolean isGroup) throws NexusException {
		checkClosed(true);
		boolean useSourceName = destination.endsWith(Node.SEPARATOR);
		Tuple<String, GroupNode, Node> tuple = openAll(destination, true, useSourceName);
		String dname;
		String dclass;
		if (useSourceName) {
			String[][] parts = parseAugmentedPath(source.getFragment());
			String[] part = parts[parts.length - 1];
			dname = part[0];
			dclass = part.length == 1 ? null : part[1];
		} else {
			dname = tuple.name;
			dclass = tuple.clazz;
		}
		try {
			if (debug) {
				logger.debug("Creating external link: {}", source);
			}
			if (isGroup) {
				file.linkexternal(dname, dclass == null ? "" : dclass, source.toString());
			} else {
				file.linkexternaldataset(dname, source.toString());
			}
		} catch (org.nexusformat.NexusException e) {
			logger.error("Problem creating external link: {}", dname, e);
			throw new NexusException("Problem creating external link", e);
		}
	}
}
