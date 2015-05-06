/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data.nexus;


import gda.data.nexus.napi.NexusFileNAPI;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyWriteableDataset;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;

/**
 * Utility methods for dealing with NeXus files.
 */
public class NexusUtils {

//	private static final Logger logger = LoggerFactory.getLogger(NexusUtils.class);

	/**
	 * Returns the number of dimensions (rank) required for the data section in a NeXus file. e.g. A single channel
	 * counterTimer would have a rank=1. A multi channel counterTimer would have a rank=2. A two dimensional detector
	 * (CCD) would have a rank=3.
	 * @param detectorDataDimensions 
	 * @return int
	 */
	public static int getRank(int[] detectorDataDimensions) {
		int rank = 0;

		// The rank is just the dimensionality of the data and the scan dimension - hence the +1
		rank = detectorDataDimensions.length+1;
		return rank;
	}
	
	/**
	 * Returns the number of dimensions (rank) required for the data section in a NeXus file. e.g. A single channel
	 * counterTimer would have a rank=1. A multi channel counterTimer would have a rank=2. A two dimensional detector
	 * (CCD) would have a rank=3.
	 * 
	 * @param width
	 * @param height
	 * @return int
	 */
	public static int getRank(int width, int height)  {
		int rank = 0;

		if (width > 1) {
			if (height > 1) {
				rank = 3;
			} else {
				rank = 2;
			}
		} else {
			rank = 1;
		}
		return rank;
	}

	/**
	 * Returns the dimensions array for a given width/height of data that can be used by the NeXus API to create a data
	 * section.
	 * 
	 * @param dimension
	 * @return int[]
	 */
	public static int[] getDim(int[] dimension) {
		int[] iDim = new int[dimension.length + 1];
		iDim[0] = NexusGlobals.NX_UNLIMITED;

		for (int i = 0; i < dimension.length; i++) {
			iDim[i + 1] = dimension[i];
		}

		return iDim;
	}

		
	/**
	 * Creates a NeXus file and returns the file handle. If file already exists then it will be overwritten.
	 * 
	 * @param filename
	 * @return NeXus file handle
	 * @throws gda.data.nexus.napi.NexusException 
	 */
	public static NexusFileInterface createNexusFile(String filename) throws gda.data.nexus.napi.NexusException {
		return new gda.data.nexus.napi.NexusFile(filename, NexusGlobals.NXACC_CREATE5);
	}

	/**
	 * Opens a NeXus file and returns the file handle.
	 * 
	 * @param filename
	 * @return NeXus file handle
	 * @throws gda.data.nexus.napi.NexusException 
	 */
	public static NexusFileInterface openNexusFile(String filename) throws gda.data.nexus.napi.NexusException {
		return new gda.data.nexus.napi.NexusFile(filename, NexusGlobals.NXACC_RDWR);
	}

	/**
	 * Opens a NeXus file as read-only and returns the file handle.
	 * 
	 * @param filename
	 * @return NeXus file handle
	 * @throws gda.data.nexus.napi.NexusException 
	 */
	public static NexusFileInterface openNexusFileReadOnly(String filename) throws gda.data.nexus.napi.NexusException {
		return new gda.data.nexus.napi.NexusFile(filename, NexusGlobals.NXACC_READ);
	}

	/**
	 * Create a (top-level) NeXus augmented path
	 * @param name
	 * @param nxClass
	 * @return augmented path
	 */
	public static String createAugmentPath(String name, String nxClass) {
		StringBuilder b = new StringBuilder();
		if (!name.startsWith(Tree.ROOT))
			b.append(Tree.ROOT);
		return addToAugmentPath(b, name, nxClass).toString();
	}

	/**
	 * Add to a NeXus augmented path
	 * @param path
	 * @param name
	 * @param nxClass
	 * @return augmented path
	 */
	public static String addToAugmentPath(String path, String name, String nxClass) {
		return addToAugmentPath(new StringBuilder(path), name, nxClass).toString();
	}

	/**
	 * Add to a NeXus augmented path
	 * @param path
	 * @param name
	 * @param nxClass
	 * @return augmented path
	 */
	public static StringBuilder addToAugmentPath(StringBuilder path, String name, String nxClass) {
		if (name == null) {
			throw new IllegalArgumentException("Name must not be null");
		}
		if (path.lastIndexOf(Node.SEPARATOR) != path.length() - 1) {
			path.append(Node.SEPARATOR);
		}
		path.append(name);
		if (nxClass != null) {
			path.append(NexusFile.NXCLASS_SEPARATOR).append(nxClass);
		}
		return path;
	}

	/**
	 * Create a plain path by stripping out NXclasses 
	 * @param augmentedPath
	 * @return plain path
	 */
	public static String stripAugmentedPath(String augmentedPath) {
		int i;
		while ((i = augmentedPath.indexOf(NexusFile.NXCLASS_SEPARATOR)) >= 0) {
			int j = augmentedPath.indexOf(Node.SEPARATOR, i);
			augmentedPath = j >= 0 ? augmentedPath.substring(0, i) + augmentedPath.substring(j)
					: augmentedPath.substring(0, i);
		}
		return augmentedPath;
	}

	/**
	 * Get name of last part
	 * @param path
	 * @return name or null if path does not contain any {@value Node#SEPARATOR} or ends in that
	 */
	public static String getName(String path) {
		if (path.endsWith(Node.SEPARATOR) || !path.contains(Node.SEPARATOR))
			return null;
		return path.substring(path.indexOf(Node.SEPARATOR) + 1);
	}

	/**
	 * Create a lazy writeable dataset
	 * @param name
	 * @param dtype
	 * @param shape
	 * @param maxShape
	 * @param chunks
	 * @return lazy writeable dataset
	 */
	public static ILazyWriteableDataset createLazyWriteableDataset(String name, int dtype, int[] shape, int[] maxShape, int[] chunks) {
		return new LazyWriteableDataset(name, dtype, shape, maxShape, chunks, null);
	}

	/**
	 * Write the string into a field called 'name' at the group in the NeXus file.
	 * 
	 * @param file
	 * @param group
	 * @param name
	 * @param value
	 * @throws NexusException 
	 */
	public static DataNode writeString(NexusFile file, GroupNode group, String name, String value) throws NexusException {
		return write(file, group, name, value);
	}

	/**
	 * Write the integer into a field called 'name' at the group in the NeXus file.
	 * 
	 * @param file
	 * @param group
	 * @param name
	 * @param value
	 * @throws NexusException 
	 */
	public static DataNode writeInteger(NexusFile file, GroupNode group, String name, int value) throws NexusException {
		return write(file, group, name, value);
	}

	/**
	 * Write the integer array into a field called 'name' at the group in the NeXus file.
	 * 
	 * @param file
	 * @param group
	 * @param name
	 * @param value
	 * @throws NexusException 
	 */
	public static DataNode writeIntegerArray(NexusFile file, GroupNode group, String name, int[] value) throws NexusException {
		return write(file, group, name, value);
	}

	/**
	 * Write the double into a field called 'name' at the group in the NeXus file.
	 * 
	 * @param file
	 * @param group
	 * @param name
	 * @param value
	 * @throws NexusException 
	 */
	public static DataNode writeDouble(NexusFile file, GroupNode group, String name, double value) throws NexusException {
		return write(file, group, name, value);
	}

	/**
	 * Write the double into a field called 'name' at the group in the NeXus file.
	 * 
	 * @param file
	 * @param group
	 * @param name
	 * @param value
	 * @throws NexusException 
	 */
	public static DataNode writeDoubleArray(NexusFile file, GroupNode group, String name, double[] value) throws NexusException {
		return write(file, group, name, value);
	}

	/**
	 * Write the double into a field called 'name' at the group in the NeXus file.
	 * 
	 * @param file
	 * @param group
	 * @param name
	 * @param value
	 * @throws NexusException 
	 */
	public static DataNode writeDoubleArray(NexusFile file, GroupNode group, String name, Double[] value) throws NexusException {
		return write(file, group, name, value);
	}

	/**
	 * Write the double into a field called 'name' at the group in the NeXus file.
	 * 
	 * @param file
	 * @param group
	 * @param name
	 * @param value
	 * @param units
	 * @throws NexusException 
	 */
	public static DataNode writeDouble(NexusFile file, GroupNode group, String name, double value, String units) throws NexusException {
		DataNode node = write(file, group, name, value);
		if (units != null) {
			writeStringAttribute(file, node, "units", units);
		}
		return node;
	}

	/**
	 * Write the object into a field called 'name' at the group in the NeXus file.
	 * 
	 * @param file
	 * @param group
	 * @param name
	 * @param value
	 * @return data node
	 * @throws NexusException 
	 */
	public static DataNode write(NexusFile file, GroupNode group, String name, Object value) throws NexusException {
		if (value == null || name == null || name.isEmpty())
			return null;
	
		Dataset a = DatasetFactory.createFromObject(value);
		a.setName(name);
		return file.createData(group, a);
	}

	/**
	 * @param file
	 * @param node
	 * @param name
	 * @param value
	 * @throws NexusException 
	 */
	public static void writeStringAttribute(NexusFile file, Node node, String name, String value) throws NexusException {
		writeAttribute(file, node, name, value);
	}

	/**
	 * @param file
	 * @param node
	 * @param name
	 * @param value
	 * @throws NexusException 
	 */
	public static void writeIntegerAttribute(NexusFile file, Node node, String name, int... value) throws NexusException {
		writeAttribute(file, node, name, value);
	}

	/**
	 * @param file
	 * @param node
	 * @param name
	 * @param value
	 * @throws NexusException 
	 */
	public static void writeDoubleAttribute(NexusFile file, Node node, String name, double... value) throws NexusException {
		writeAttribute(file, node, name, value);
	}

	/**
	 * @param file
	 * @param node
	 * @param name
	 * @param value
	 * @throws NexusException 
	 */
	public static void writeDoubleAttribute(NexusFile file, Node node, String name, Double... value) throws NexusException {
		writeAttribute(file, node, name, value);
	}

	/**
	 * @param file
	 * @param node
	 * @param name
	 * @param value
	 * @throws NexusException 
	 */
	public static void writeAttribute(NexusFile file, Node node, String name, Object value) throws NexusException {
		if (value == null || name == null || name.isEmpty())
			return;

		Dataset a = DatasetFactory.createFromObject(value);
		a.setName(name);
		Attribute attr = file.createAttribute(node, a);
		file.addAttribute(node, attr);
	}

	public static NexusFile createNXFile(String path) throws NexusException {
		NexusFileNAPI file = new NexusFileNAPI(path);
		file.createAndOpenToWrite();
		return file;
	}
}
