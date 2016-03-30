/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.analysis.hdf5;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5LibraryException;
import hdf.object.h5.H5Datatype;

public class Hdf5HelperData {

	public long[] dims;
	public Object data;
	public H5Datatype h5Datatype;
	public long native_type;

	/**
	 * @param dims
	 * @param data
	 * @param h5Datatype
	 */
	public Hdf5HelperData(long[] dims, Object data, H5Datatype h5Datatype, long h5tNativeInt16) {
		super();
		this.dims = dims;
		this.data = data;
		this.h5Datatype = h5Datatype;
		this.native_type = h5tNativeInt16;
	}

	public Hdf5HelperData(long[] dims, Object data, long native_type) {
		super();
		this.dims = dims;
		this.data = data;
		this.h5Datatype = new H5Datatype(native_type);
		this.native_type = native_type;
	}

	public Hdf5HelperData(short data) {
		this( new long[]{1}, new short[]{data});
	}
	public Hdf5HelperData(int data) {
		this( new long[]{1}, new int[]{data});
	}
	public Hdf5HelperData(double data) {
		this( new long[]{1}, new double[]{data});
	}

	public Hdf5HelperData(long[] dims, short[] data) {
		this(dims, data, new H5Datatype(HDF5Constants.H5T_NATIVE_INT16),HDF5Constants.H5T_NATIVE_INT16);
	}

	public Hdf5HelperData(long[] dims, int[] data) {
		this(dims, data, new H5Datatype(HDF5Constants.H5T_NATIVE_INT32),HDF5Constants.H5T_NATIVE_INT32);
	}

	public Hdf5HelperData(long[] dims, double[] data) {
		this(dims, data, new H5Datatype(HDF5Constants.H5T_NATIVE_DOUBLE),HDF5Constants.H5T_NATIVE_DOUBLE);
	}

	public Hdf5HelperData( double[] data) {
		this(new long[]{data.length}, data, new H5Datatype(HDF5Constants.H5T_NATIVE_DOUBLE),HDF5Constants.H5T_NATIVE_DOUBLE);
	}

	public Hdf5HelperData( Dataset ads) {
		this( getShapeAsLongs(ads.getShape()), ads.getBuffer(),  getH5DataType(ads));
	}

	private static long getH5DataType(Dataset ads) {
		int dtype = ads.getDtype();
		switch (dtype) {
		case Dataset.BOOL:
			throw new IllegalArgumentException("BOOL not yet supported");
		case Dataset.INT8:
		case Dataset.ARRAYINT8:
			return HDF5Constants.H5T_NATIVE_INT8;
		case Dataset.INT16:
		case Dataset.ARRAYINT16:
			return HDF5Constants.H5T_NATIVE_INT16;
		case Dataset.RGB:
			throw new IllegalArgumentException("RGB not yet supported");
		case Dataset.INT32:
		case Dataset.ARRAYINT32:
			return HDF5Constants.H5T_NATIVE_INT32;
		case Dataset.INT64:
		case Dataset.ARRAYINT64:
			return HDF5Constants.H5T_NATIVE_INT64;
		case Dataset.FLOAT32:
		case Dataset.ARRAYFLOAT32:
			return HDF5Constants.H5T_NATIVE_FLOAT;
		case Dataset.FLOAT64:
		case Dataset.ARRAYFLOAT64:
			return HDF5Constants.H5T_NATIVE_DOUBLE;
		case Dataset.COMPLEX64:
			throw new IllegalArgumentException("COMPLEX64 not yet supported");
		case Dataset.COMPLEX128:
			throw new IllegalArgumentException("COMPLEX128 not yet supported");
		default:
			throw new IllegalArgumentException(dtype + " not yet supported");
		}
	}

	static public long [] getShapeAsLongs(int [] shape){
		long [] lshape = new long[shape.length];
		for( int i=0; i < shape.length;i++){
			lshape[i] = shape[i];
		}
		return lshape;
	}

	static public Hdf5HelperData getInstance(String s) throws HDF5LibraryException {
		byte[] bytes = s.getBytes();
		long typeId = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
		H5.H5Tset_size(typeId, bytes.length);
		return new Hdf5HelperData(new long[] { 1 }, bytes, new H5Datatype(typeId), typeId);
	}

	/**
	 * @return Assumes data is a byte array
	 */
	public String getAsString() {
		byte[] buf = (byte[]) data;
		return new String(buf).trim();
	}
}