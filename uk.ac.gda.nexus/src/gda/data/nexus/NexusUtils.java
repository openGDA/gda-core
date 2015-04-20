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


import gda.data.nexus.napi.NexusFile;
import gda.data.nexus.napi.NexusFileNAPI;

import java.nio.charset.Charset;

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
	 * @throws NexusException
	 */
	public static NexusFileInterface createNexusFile(String filename) throws NexusException {
		return new NexusFile(filename, NexusGlobals.NXACC_CREATE5);
	}

	/**
	 * Opens a NeXus file and returns the file handle.
	 * 
	 * @param filename
	 * @return NeXus file handle
	 * @throws NexusException
	 */
	public static NexusFileInterface openNexusFile(String filename) throws NexusException {
		return new NexusFile(filename, NexusGlobals.NXACC_RDWR);
	}

	/**
	 * Opens a NeXus file as readonly and returns the file handle.
	 * 
	 * @param filename
	 * @return NeXus file handle
	 * @throws NexusException
	 */
	public static NexusFileInterface openNexusFileReadOnly(String filename) throws NexusException {
		return new NexusFile(filename, NexusGlobals.NXACC_READ);
	}

	/**
	 * Writes the String 'stringToWrite' into a field called 'name' at the current position in the NeXus file.
	 * 
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void writeNexusString(NexusFileInterface file, String name, String value) throws NexusException {
		if(value == null || name == null || name.isEmpty() || value.isEmpty())
			return;
		byte [] bytes = value.getBytes();
		int[] dimArray = new int[] {bytes.length};
		if (file.groupdir().get(name) == null) {
			file.makedata(name, NexusGlobals.NX_CHAR, dimArray.length, dimArray);
		}
		file.opendata(name);
		file.putdata(bytes);
		file.closedata();		
	}

	/**
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void writeNexusStringAttribute(NexusFileInterface file, String name, String value) throws NexusException {
		file.putattr(name, value.getBytes(), NexusGlobals.NX_CHAR);
	}

	/**
	 * @param file
	 * @param name
	 * @param values
	 * @throws NexusException
	 */
	public static void writeNexusIntegerAttribute(NexusFileInterface file, String name, int... values) throws NexusException {
		file.putattr(name, values, NexusGlobals.NX_INT32);
	}

	/**
	 * @param file
	 * @param name
	 * @param values
	 * @throws NexusException
	 */
	public static void writeNexusIntegerAttribute(NexusFileInterface file, String name, Integer[] values) throws NexusException {
		file.putattr(name, values, NexusGlobals.NX_INT32);
	}

	/**
	 * @param file
	 * @param name
	 * @param values
	 * @throws NexusException
	 */
	public static void writeNexusDoubleAttribute(NexusFileInterface file, String name, double... values) throws NexusException {
		file.putattr(name, values, NexusGlobals.NX_FLOAT64);
	}

	/**
	 * @param file
	 * @param name
	 * @param values
	 * @throws NexusException
	 */
	public static void writeNexusDoubleAttribute(NexusFileInterface file, String name, Double[] values) throws NexusException {
		file.putattr(name, values, NexusGlobals.NX_FLOAT64);
	}

	/**
	 * Writes the integer 'value' into a field called 'name' at the current position in the NeXus file.
	 * 
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void writeNexusInteger(NexusFileInterface file, String name, int value) throws NexusException {
		int[] dimArray = new int[1];
		dimArray[0] = 1;
		if (file.groupdir().get(name) == null) {
			file.makedata(name, NexusGlobals.NX_INT32, 1, dimArray);
		}
		file.opendata(name);
		int[] arr = { value };
		file.putdata(arr);
		file.closedata();
	}

	/**
	 * Writes the integer array 'value' into a field called 'name' at the current position in the NeXus file.
	 * 
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void writeNexusIntegerArray(NexusFileInterface file, String name, int[] value) throws NexusException {
		if (value.length == 0)
			return;
		int[] dimArray = new int[1];
		dimArray[0] = value.length;
		if (file.groupdir().get(name) == null) {
			file.makedata(name, NexusGlobals.NX_INT32, 1, dimArray);
		}
		file.opendata(name);
		file.putdata(value);
		file.closedata();
	}

	/**
	 * Writes the long 'value' into a field called 'name' at the current position in the NeXus file.
	 * 
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void writeNexusLong(NexusFileInterface file, String name, int value) throws NexusException {
		int[] dimArray = new int[1];
		dimArray[0] = 1;
		if (file.groupdir().get(name) == null) {
			file.makedata(name, NexusGlobals.NX_INT64, 1, dimArray);
		}
		file.opendata(name);
		long[] arr = { value };
		file.putdata(arr);
		file.closedata();
	}

	/**
	 * Writes the long array 'value' into a field called 'name' at the current position in the NeXus file.
	 * 
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void writeNexusLongArray(NexusFileInterface file, String name, long[] value) throws NexusException {
		if (value.length == 0)
			return;
		int[] dimArray = new int[1];
		dimArray[0] = value.length;
		if (file.groupdir().get(name) == null) {
			file.makedata(name, NexusGlobals.NX_INT64, 1, dimArray);
		}
		file.opendata(name);
		file.putdata(value);
		file.closedata();
	}

	/**
	 * Writes the double 'value' into a field called 'name' at the current position in the NeXus file.
	 * 
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void writeNexusDouble(NexusFileInterface file, String name, double value, String units) throws NexusException {
		int[] dimArray = new int[1];
		dimArray[0] = 1;
		if (file.groupdir().get(name) == null) {
			file.makedata(name, NexusGlobals.NX_FLOAT64, 1, dimArray);
		}
		file.opendata(name);
		double[] dataArray = { value };
		file.putdata(dataArray);
		if (units != null) {
			file.putattr("units", units.getBytes(Charset.forName("UTF-8")), NexusGlobals.NX_CHAR);
		}
		file.closedata();
	}
	
	/**
	 * Writes the double 'value' into a field called 'name' at the current position in the NeXus file.
	 * 
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void writeNexusDouble(NexusFileInterface file, String name, double value) throws NexusException {
		writeNexusDouble(file, name, value, null);
	}

	/**
	 * @param file
	 * @param name
	 * @param value
	 *            a string containing a double value
	 * @throws NexusException
	 */
	public static void writeNexusDouble(NexusFileInterface file, String name, String value) throws NexusException {
		writeNexusDouble(file, name, Double.parseDouble(value));
	}

	/**
	 * Writes the double array 'value' into a field called 'name' at the current position in the NeXus file.
	 * 
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void writeNexusDoubleArray(NexusFileInterface file, String name, double[] value) throws NexusException {
		if (value.length == 0)
			return;
		int[] dimArray = new int[1];
		dimArray[0] = value.length;
		if (file.groupdir().get(name) == null) {
			file.makedata(name, NexusGlobals.NX_FLOAT64, 1, dimArray);
		}
		file.opendata(name);
		file.putdata(value);
		file.closedata();
	}

	/**
	 * Writes the double array 'value' into a field called 'name' at the current position in the NeXus file.
	 * 
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void writeNexusDoubleArray(NexusFileInterface file, String name, Double[] value) throws NexusException {
		if (value != null && value.length > 0) {
			int[] dimArray = new int[1];
			dimArray[0] = value.length;
			if (file.groupdir().get(name) == null) {
				file.makedata(name, NexusGlobals.NX_FLOAT64, 1, dimArray);
			}
			file.opendata(name);
			file.putdata(value);
			file.closedata();
		}
	}

	/**
	 * Writes the boolean 'value' into a field called 'name' at the current position in the NeXus file.
	 * 
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void writeNexusBoolean(NexusFileInterface file, String name, boolean value) throws NexusException {
		int[] dimArray = new int[1];
		dimArray[0] = 1;
		if (file.groupdir().get(name) == null) {
			file.makedata(name, NexusGlobals.NX_BOOLEAN, 1, dimArray);
		}
		file.opendata(name);
		file.putdata(value);
		file.closedata();
	}

	/**
	 * Appends the value 'double' into the field called 'name'. If 'name' does not exist then it will be created.
	 * 
	 * @param file
	 * @param name
	 * @param value
	 * @throws NexusException
	 */
	public static void appendNexusDouble(NexusFileInterface file, String name, double value) throws NexusException {
		int[] dimArray = new int[1];
		dimArray[0] = NexusGlobals.NX_UNLIMITED;
		if (file.groupdir().get(name) == null) {
			file.makedata(name, NexusGlobals.NX_FLOAT64, 1, dimArray);
		}

		file.opendata(name);

		double[] dataArray = { value };
		int[] dataStart = new int[1];
		int[] dataLength = new int[1];
		dataStart[0] = 1;
		dataLength[0] = 1;

		file.putslab(dataArray, dataStart, dataLength);
		file.closedata();
	}

	public static org.eclipse.dawnsci.hdf5.nexus.NexusFile createNXFile(String path) {
		return new NexusFileNAPI(path);
	}
}
