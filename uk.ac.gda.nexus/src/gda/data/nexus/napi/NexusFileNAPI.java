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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.nexusformat.AttributeEntry;
import org.nexusformat.NXlink;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

public class NexusFileNAPI implements org.eclipse.dawnsci.hdf5.nexus.NexusFile {
	private String filename;

	private NexusFile file;
	private boolean canWrite = false;

	private TreeFile tree; // acts as a cache

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
	public void openToRead() throws IOException {
		canWrite = false;
		try {
			file = new NexusFile(filename, NexusFile.NXACC_READ);
			initTree();
		} catch (NexusException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void openToWrite(boolean createIfNecessary) throws IOException {
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
		} catch (NexusException e) {
			throw new IOException(e);
		}
		throw new IOException("File not found and not created");
	}

	@Override
	public void createAndOpenToWrite() throws IOException {
		try {
			file = new NexusFile(filename, NexusFile.NXACC_CREATE5);
			initTree();
			canWrite = true;
			return;
		} catch (NexusException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void flush() {
		try {
			file.flush();
		} catch (NexusException e) {
		}
	}

	@Override
	public void close() {
		try {
			file.close();
			tree = null;
		} catch (NexusException e) {
		}
	}

	private void checkClosed() {
		if (tree == null) {
			throw new IllegalStateException("File has been closed");
		}
	}

	@Override
	public GroupNode getGroup(String path, boolean createPathIfNecessary) {
		checkClosed();
		NodeLink link = tree.findNodeLink(path);
		if (link != null) {
			if (link.isDestinationGroup())
				return (GroupNode) link.getDestination();
			throw new IllegalArgumentException("Path specified is not for a group");
		}
		Tuple<String, GroupNode> tuple = openAll(path, createPathIfNecessary, true);
		if (tuple.n == null)
			return null;

		return tuple.g;
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
	private static final String NXCLASS = "NX_class";

	class Tuple<T, U> {
		T n;
		T c;
		U g;
		public Tuple(T t, T v, U u) {
			this.n = t;
			this.c = v;
			this.g = u;
		}
	}

	/**
	 * Open all groups in path
	 * @param path
	 * @param createPathIfNecessary
	 * @param toBottom
	 * @return name, NeXus class, group
	 */
	private Tuple<String, GroupNode> openAll(String path, boolean createPathIfNecessary, boolean toBottom) {
		String[][] pairs = parseAugmentedPath(path);
		int imax = pairs.length;
		if (!toBottom)
			--imax;

		GroupNode group = tree.getGroupNode();
		try {
			file.openpath(Tree.ROOT);
		} catch (NexusException e) {
			return null;
		}
		String name = Tree.ROOT;
		StringBuilder cpath = new StringBuilder(Tree.ROOT);
		String ppath = cpath.toString();
		if (!group.isPopulated()) {
			populate(ppath, group);
		}
		String clazz = null;
		for (int i = 1; i < imax; i++) { // miss out first as it is blank
			String[] pair = pairs[i];
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
		if (name != null && !toBottom) {
			name = pairs[imax][0];
		}
		return new Tuple<String, GroupNode>(name, clazz, group);
	}

	private void populate(String path, GroupNode group) {
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
					g.addAttribute(TreeFactory.createAttribute(tree, n, NXCLASS, c, false));
					copyAttributes(n, g);
					group.addGroupNode(tree, path, n, g);
				}
			}
		} catch (NexusException e) {
			return;
		}
	}

	private boolean openGroup(String name, String nxClass, boolean create) {
		try {
			String url = file.isexternalgroup(name, nxClass);
			if (url == null) {
				file.opengroup(name, nxClass);
			} else {
				return false;
			}
		} catch (NexusException e) {
			if (!create || !canWrite) {
				return false;
			}
			try {
				file.makegroup(name, nxClass);
				file.opengroup(name, nxClass);
			} catch (NexusException e1) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private void copyAttributes(String name, Node node) {
		Hashtable<String, AttributeEntry> map;
		try {
			map = file.attrdir();
		} catch (NexusException e) {
			return;
		}
		for (Entry<String, AttributeEntry> sa: map.entrySet()) {
			node.addAttribute(createAttribute(name, sa.getKey(), sa.getValue()));
		}
	}

	private Attribute createAttribute(String name, String n, AttributeEntry e) {
		Object s;
		try {
			s = file.getattr(n);
		} catch (NexusException e1) {
			return null;
		}
		Attribute a = TreeFactory.createAttribute(tree, name, n, s, false);
		IDataset d = a.getValue();
		d.setShape(a.isString() ? new int[0] : e.dim);
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
	public DataNode getData(String path) {
		checkClosed();
		NodeLink link = tree.findNodeLink(path);
		if (link != null) {
			if (link.isDestinationData())
				return (DataNode) link.getDestination();
			throw new IllegalArgumentException("Path specified is not for a dataset");
		}

		Tuple<String, GroupNode> tuple = openAll(path, false, false);
		String name = tuple.n;
		if (name == null)
			return null;
		if (!openDataset(name))
			return null;

		return createDataNode(tuple.g, path, name);
	}

	private DataNode createDataNode(GroupNode g, String path, String name) {
		DataNode dataNode = TreeFactory.createDataNode(path.hashCode());
		copyAttributes(name, dataNode);
		String p = path.substring(0, path.length() - name.length());
		g.addDataNode(tree, p, name, dataNode);
		int[] tShape = new int[10];
		int[] args = new int[10];
		try {
			file.getinfo(tShape, args);
		} catch (NexusException e) {
			return null;
		}

		closeDataset();

		int rank = args[0];
		int dtype = getDtype(args[1]);
		int[] shape = new int[rank];
		for (int i = 0; i < rank; i++) {
			shape[i] = tShape[i];
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

	private boolean openDataset(String name) {
		try {
			String url = file.isexternaldataset(name);
			if (url == null) {
				file.opendata(name);
			} else {
				return false;
			}
		} catch (NexusException e) {
			return false;
		}
		return true;
	}

	private boolean closeDataset() {
		try {
			file.closedata();
		} catch (NexusException e) {
			return false;
		}
		return true;
	}

	@Override
	public DataNode createData(String path, ILazyWriteableDataset data, boolean createPathIfNecessary) {
		checkClosed();
		if (!canWrite) {
			return null;
		}
		Tuple<String, GroupNode> tuple = openAll(path, false, true);
		if (tuple.n == null)
			return null;

		String name = data.getName();
		try {
			file.makedata(name, getType(data), data.getRank(), data.getMaxShape());
		} catch (NexusException e) {
			return null;
		}
		String dpath = path + Node.SEPARATOR + name;
		DataNode dataNode = TreeFactory.createDataNode(dpath.hashCode());
		dataNode.setDataset(data);
		tuple.g.addDataNode(tree, path + Node.SEPARATOR, name, dataNode);

		data.setSaver(new NAPILazySaver(tree, path, name, data.getShape(), AbstractDataset.getDType(data)));
		dataNode.setDataset(data);
		return dataNode;
	}

	private int getType(ILazyDataset data) {
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
	public DataNode createData(GroupNode group, ILazyWriteableDataset data) {
		return createData(TreeUtils.getPath(tree, group), data, true);
	}

	@Override
	public Attribute createAttribute(String node, IDataset attr) {
		checkClosed();
		Attribute a = TreeFactory.createAttribute(tree, node, attr.getName());
		a.setValue(attr);
		return a;
	}

	@Override
	public void addAttribute(Node node, Attribute... attribute) {
		checkClosed();
		if (!canWrite) {
			throw new IllegalStateException("Can not write as opened read-only");
		}
		String path = TreeUtils.getPath(tree, node);
		
		try {
			file.openpath(path);
			for (Attribute a : attribute) {
				node.addAttribute(a);
				IDataset d = a.getValue();
				if (d.elementClass().equals(String.class)) {
					file.putattr(a.getName(), d.getString().getBytes(), NexusFile.NX_CHAR);
				} else {
					file.putattr(a.getName(), DatasetUtils.serializeDataset(d), getType(d));
				}
			}
		} catch (NexusException e) {
			return;
		}
	}

	@Override
	public void link(String source, String destination) {
		checkClosed();
		if (!canWrite) {
			throw new IllegalStateException("Can not write as opened read-only");
		}

		Tuple<String, GroupNode> tuple = openAll(source, false, false);
		String sname = tuple.n;
		if (sname == null) {
			throw new IllegalArgumentException("Source does not exist");
		}
		if (!tuple.g.isPopulated()) {
			populate(source.substring(0, source.length() - sname.length()), tuple.g);
		}
		NodeLink l = tuple.g.getNodeLink(sname);
		NXlink t = null;
		try {
			if (l.isDestinationData()) {
				openDataset(sname);
				t = file.getdataID();
			} else if (l.isDestinationGroup()) {
				file.opengroup(sname, tuple.c);
				t = file.getgroupID();
			}
		} catch (NexusException e) {
		}
		if (t == null) {
			throw new IllegalArgumentException("Could not get link information");
		}

		boolean useSourceName = destination.endsWith(Node.SEPARATOR);
		tuple = openAll(destination, true, useSourceName);
		try {
			if (useSourceName) {
				file.makenamedlink(sname, t);
			} else {
				file.makenamedlink(tuple.n, t);
			}
		} catch (NexusException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void linkExternal(URI source, String destination, boolean isGroup) {
		checkClosed();
		if (!canWrite) {
			throw new IllegalStateException("Can not write as opened read-only");
		}
		boolean useSourceName = destination.endsWith(Node.SEPARATOR);
		Tuple<String, GroupNode> tuple = openAll(destination, true, useSourceName);
		String dname;
		String dclass;
		if (useSourceName) {
			String[][] parts = parseAugmentedPath(source.getFragment());
			String[] part = parts[parts.length - 1];
			dname = part[0];
			dclass = part.length == 1 ? null : part[1];
		} else {
			dname = tuple.n;
			dclass = tuple.c;
		}
		try {
			if (isGroup) {
				file.linkexternal(dname, dclass == null ? "" : dclass, source.toString());
			} else {
				file.linkexternaldataset(dname, source.toString());
			}
		} catch (NexusException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
