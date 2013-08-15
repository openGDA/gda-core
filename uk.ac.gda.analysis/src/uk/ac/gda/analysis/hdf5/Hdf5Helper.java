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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.structs.H5G_info_t;
import ncsa.hdf.object.h5.H5Datatype;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;

/**
 * From http://davis.lbl.gov/Manuals/HDF5-1.4.3/Tutor/rdwt.html During a dataset I/O operation, the library transfers
 * raw data between memory and the file. The data in memory can have a datatype different from that of the file and can
 * also be of a different size (i.e., the data in memory is a subset of the dataset elements, or vice versa). Therefore,
 * to perform read or write operations, the application program must specify: The dataset The dataset's datatype in
 * memory The dataset's dataspace in memory The dataset's dataspace in the file The dataset transfer property list (The
 * dataset transfer property list controls various aspects of the I/O operations, such as the number of processes
 * participating in a collective I/O request or hints to the library to control caching of raw data. In this tutorial,
 * we use the default dataset transfer property list.) The data buffer The steps to read from or write to a dataset are
 * as follows: Obtain the dataset identifier. Specify the memory datatype. Specify the memory dataspace. Specify the
 * file dataspace. Specify the transfer properties. Perform the desired operation on the dataset. Close the dataset.
 * Close the dataspace, datatype, and property list if necessary. To read from or write to a dataset, the
 * H5Dread/h5dread_f and H5Dwrite/h5dwrite_f routines are used.
 */
public class Hdf5Helper {
	private static final Logger logger = LoggerFactory.getLogger(Hdf5Helper.class);
	private static final Hdf5Helper INSTANCE = new Hdf5Helper();

	public static Hdf5Helper getInstance() {
		return INSTANCE;
	}

	public enum TYPE {
		GROUP, DATASET;
	}

	public void writeToFileSimple(Hdf5HelperData hData, String fileName, HDF5HelperLocations location,
			String dataSetName) throws Exception {
		writeToFile(hData, fileName, location, dataSetName, null, null, null);
	}

	/*
	 * public boolean writeToFileOld(Hdf5HelperData hData, String fileName, String groupName, String dataSetName, long[]
	 * chunk_dims, boolean[] extendible, long[] offset) throws Exception { boolean success = true; int fileId = -1; int
	 * filespaceId = -1; int datasetId = -1; int groupId = -1; String[] split = groupName.split("/"); int[] groupIds =
	 * new int[split.length]; boolean[] isDataSet = new boolean[groupIds.length]; Arrays.fill(groupIds, -1); if
	 * (extendible != null){ if (extendible.length != hData.dims.length) throw new
	 * IllegalArgumentException("extendible.length != hData.dims.length"); if( offset == null || offset.length !=
	 * extendible.length) throw new IllegalArgumentException("offset == null || offset.length != extendible.length"); }
	 * // Create a new file using default properties. try { if ((new File(fileName)).exists()) { fileId =
	 * H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDWR, HDF5Constants.H5P_DEFAULT); } else { fileId =
	 * H5.H5Fcreate(fileName, HDF5Constants.H5F_ACC_EXCL, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT); } for
	 * (int i = 0; i < split.length; i++) { int loc_id = i == 0 ? fileId : groupIds[i - 1]; String name = split[i];
	 * isDataSet[i] = false; if( name.endsWith(":DS")){ name = name.replace(":DS",""); isDataSet[i] = true; } try { if(
	 * isDataSet[i]){ groupIds[i] = H5.H5Dopen(loc_id, name, HDF5Constants.H5P_DEFAULT); } else {
	 * H5.H5Gget_info_by_name(loc_id, name, HDF5Constants.H5P_DEFAULT); groupIds[i] = H5.H5Gopen(loc_id, name,
	 * HDF5Constants.H5P_DEFAULT); } } catch (Exception ex) { if( !isDataSet[i]){ groupIds[i] = H5.H5Gcreate(loc_id,
	 * name, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT); } else { throw new
	 * Exception("DataSet does not exist " + groupName ); } } } groupId = groupIds[groupIds.length - 1]; // Create
	 * dataspace. Setting maximum size to NULL sets the maximum // size to be the current size. if (hData != null) {
	 * long[] max_dims = null; int cparms = HDF5Constants.H5P_DEFAULT; if (extendible != null) { max_dims = new
	 * long[extendible.length]; for (int i = 0; i < max_dims.length; i++) { max_dims[i] = extendible[i] ?
	 * HDF5Constants.H5S_UNLIMITED : hData.dims[i]; } cparms = H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE); int
	 * status = H5.H5Pset_chunk(cparms, hData.dims.length, chunk_dims); if (status < 0) throw new
	 * Exception("Error setting chunk"); } filespaceId = H5.H5Screate_simple(hData.dims.length, hData.dims, max_dims);
	 * int typeId = hData.native_type; try{ datasetId = H5.H5Dopen(groupId, dataSetName, HDF5Constants.H5P_DEFAULT);
	 * }catch(Exception e){ datasetId = H5.H5Dcreate(groupId, dataSetName, typeId, filespaceId,
	 * HDF5Constants.H5P_DEFAULT,cparms, HDF5Constants.H5P_DEFAULT); } if (extendible != null) { long [] extent = new
	 * long[hData.dims.length]; for( int i=0; i< extent.length;i++){ extent[i] = offset[i] + hData.dims[i]; }
	 * H5.H5Dset_extent(datasetId, extent); int filespace = H5.H5Dget_space(datasetId); int status =
	 * H5.H5Sselect_hyperslab(filespace, HDF5Constants.H5S_SELECT_SET, offset, null, hData.dims, null); if (status < 0)
	 * throw new Exception("Error H5Sselect_hyperslab"); H5.H5Dwrite(datasetId, typeId, filespaceId, filespace,
	 * HDF5Constants.H5P_DEFAULT, hData.data); } else { H5.H5Dwrite(datasetId, typeId, HDF5Constants.H5S_ALL,
	 * HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT, hData.data); } } return success; } catch (Exception ex) { throw
	 * new Exception("Unable to write to file:" + fileName + " at group:" + groupName, ex); } finally { if (datasetId >=
	 * 0) H5.H5Dclose(datasetId); if (filespaceId >= 0) H5.H5Sclose(filespaceId); for (int i = groupIds.length - 1; i >=
	 * 0; i--) { if (groupIds[i] != -1){ if( isDataSet[i]){ H5.H5Dclose(groupIds[i]); } else { H5.H5Gclose(groupIds[i]);
	 * } } } if (fileId >= 0) H5.H5Fclose(fileId); } }
	 */
	private void createAttribute(int loc_id, String attributeName, String attributeValue) throws Exception {

		Hdf5HelperData attr_data = Hdf5HelperData.getInstance(attributeValue);
		int dataspaceId = -1;
		int attributeId = -1;
		try {
			dataspaceId = H5.H5Screate_simple(attr_data.dims.length, attr_data.dims, null);
			int typeId = attr_data.native_type; // H5.H5Tcreate(hData.h5Datatype.getDatatypeClass(),

			attributeId = H5.H5Acreate(loc_id, attributeName, typeId, dataspaceId, HDF5Constants.H5P_DEFAULT,
					HDF5Constants.H5P_DEFAULT);

			int status = H5.H5Awrite(attributeId, typeId, attr_data.data);
			if (status < 0)
				throw new Exception("Unable to write attribute " + attributeName + " of value:" + attributeValue);
		} finally {
			// End access to the dataset and release resources used by it.
			if (attributeId >= 0)
				H5.H5Aclose(attributeId);

			if (dataspaceId >= 0)
				H5.H5Sclose(dataspaceId);

		}
	}

	/**
	 * @param hData
	 *            - data for current slab
	 * @param fileName
	 * @param location
	 * @param dataSetName
	 * @param chunk_dims
	 *            - the dimensions of each slab to be written
	 * @param extendible
	 *            - array to indicate which dimension is extendible
	 * @param offset
	 *            - offset for current slab
	 * @throws Exception
	 */
	public void writeToFile(Hdf5HelperData hData, String fileName, HDF5HelperLocations location, String dataSetName,
			long[] chunk_dims, boolean[] extendible, long[] offset) throws Exception {

		int fileId = -1;
		int filespaceId = -1;
		int datasetId = -1;
		int groupId = -1;
		int[] groupIds = new int[location.size()];
		// boolean[] isDataSet = new boolean[groupIds.length];
		Arrays.fill(groupIds, -1);

		if (extendible != null) {
			if (extendible.length != hData.dims.length)
				throw new IllegalArgumentException("extendible.length != hData.dims.length");
			if (offset == null || offset.length != extendible.length)
				throw new IllegalArgumentException("offset == null || offset.length != extendible.length");
		}

		// Create a new file using default properties.
		try {
			if ((new File(fileName)).exists()) {
				fileId = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDWR, HDF5Constants.H5P_DEFAULT);
			} else {
				fileId = H5.H5Fcreate(fileName, HDF5Constants.H5F_ACC_EXCL, HDF5Constants.H5P_DEFAULT,
						HDF5Constants.H5P_DEFAULT);
			}
			for (int i = 0; i < location.size(); i++) {
				int loc_id = i == 0 ? fileId : groupIds[i - 1];
				String name = location.get(i).name;
				/*
				 * isDataSet[i] = false; if( name.endsWith(":DS")){ name = name.replace(":DS",""); isDataSet[i] = true;
				 * }
				 */try {
					/*
					 * if( isDataSet[i]){ groupIds[i] = H5.H5Dopen(loc_id, name, HDF5Constants.H5P_DEFAULT); } else {
					 */H5.H5Gget_info_by_name(loc_id, name, HDF5Constants.H5P_DEFAULT);
					groupIds[i] = H5.H5Gopen(loc_id, name, HDF5Constants.H5P_DEFAULT);
					// }
				} catch (Exception ex) {
					// if( !isDataSet[i]){
					groupIds[i] = H5.H5Gcreate(loc_id, name, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT,
							HDF5Constants.H5P_DEFAULT);

					String attributeName = location.get(i).attributeName;
					if (attributeName != null && attributeName.length() != 0) {
						createAttribute(groupIds[i], attributeName, location.get(i).attributeValue);
					}

					/*
					 * } else { throw new Exception("DataSet does not exist " + location ); }
					 */}
			}
			groupId = groupIds[groupIds.length - 1];

			// Create dataspace. Setting maximum size to NULL sets the maximum
			// size to be the current size.
			if (hData != null) {
				long[] max_dims = null;
				int cparms = HDF5Constants.H5P_DEFAULT;
				if (extendible != null) {
					max_dims = new long[extendible.length];
					for (int i = 0; i < max_dims.length; i++) {
						max_dims[i] = extendible[i] ? HDF5Constants.H5S_UNLIMITED : hData.dims[i];
					}
					cparms = H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE);
					int status = H5.H5Pset_chunk(cparms, hData.dims.length, chunk_dims);
					if (status < 0)
						throw new Exception("Error setting chunk");
				}
				filespaceId = H5.H5Screate_simple(hData.dims.length, hData.dims, max_dims);
				int typeId = hData.native_type;

				try {
					datasetId = H5.H5Dopen(groupId, dataSetName, HDF5Constants.H5P_DEFAULT);
				} catch (Exception e) {
					datasetId = H5.H5Dcreate(groupId, dataSetName, typeId, filespaceId, HDF5Constants.H5P_DEFAULT,
							cparms, HDF5Constants.H5P_DEFAULT);
				}

				if (extendible != null) {

					// ensure dataspace size is big enough for the new item
					int filespace = H5.H5Dget_space(datasetId);
					if (filespace < 0)
						throw new Exception("Unable to open the filespace");

					int ndims = H5.H5Sget_simple_extent_ndims(filespace);
					long[] dims = new long[ndims];
					long[] maxdims = new long[ndims];
					H5.H5Sget_simple_extent_dims(filespace, dims, maxdims);

					if (dims.length != offset.length)
						throw new Exception("dims.length != offset.length");
					if (dims.length != hData.dims.length)
						throw new Exception("dims.length != hData.dims.length");

					// need to extend current size rather than just use offset and current item as it
					// will reduce the dataset set if the new extent is less than existing value.
					long[] extent = new long[hData.dims.length];
					boolean extend = false;
					for (int i = 0; i < extent.length; i++) {
						extent[i] = Math.max(dims[i], offset[i] + hData.dims[i]);
						extend |= extent[i] > dims[i];
						if (extent[i] > maxdims[i] && maxdims[i] != -1)
							throw new Exception("Cannot extend dataspace beyond limit set at creation");
					}
					if (extend) {
						H5.H5Dset_extent(datasetId, extent);
						H5.H5Sclose(filespace);
						filespace = H5.H5Dget_space(datasetId);
						if (filespace < 0)
							throw new Exception("Unable to open the filespace");
					}

					int status = H5.H5Sselect_hyperslab(filespace, HDF5Constants.H5S_SELECT_SET, offset, null,
							hData.dims, null);
					if (status < 0)
						throw new Exception("Error H5Sselect_hyperslab");
					H5.H5Dwrite(datasetId, typeId, filespaceId, filespace, HDF5Constants.H5P_DEFAULT, hData.data);

				} else {
					H5.H5Dwrite(datasetId, typeId, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
							HDF5Constants.H5P_DEFAULT, hData.data);
				}
			}
		} catch (Exception ex) {
			throw new Exception("Unable to write to file:" + fileName + " at group:" + location, ex);
		} finally {
			if (datasetId >= 0)
				H5.H5Dclose(datasetId);
			if (filespaceId >= 0)
				H5.H5Sclose(filespaceId);
			for (int i = groupIds.length - 1; i >= 0; i--) {
				if (groupIds[i] != -1) {
					/*
					 * if( isDataSet[i]){ H5.H5Dclose(groupIds[i]); } else {
					 */H5.H5Gclose(groupIds[i]);
					// }
				}
			}
			if (fileId >= 0)
				H5.H5Fclose(fileId);
		}
	}

	public void createLocation(String fileName, TYPE holder, HDF5HelperLocations location) throws Exception {

		int fileId = -1;
		int[] groupIds = new int[location.size()];
		boolean[] isDataSet = new boolean[groupIds.length];
		Arrays.fill(groupIds, -1);

		// Create a new file using default properties.
		try {
			if ((new File(fileName)).exists()) {
				fileId = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDWR, HDF5Constants.H5P_DEFAULT);
			} else {
				fileId = H5.H5Fcreate(fileName, HDF5Constants.H5F_ACC_EXCL, HDF5Constants.H5P_DEFAULT,
						HDF5Constants.H5P_DEFAULT);
			}
			for (int i = 0; i < location.size(); i++) {
				int loc_id = i == 0 ? fileId : groupIds[i - 1];
				String name = location.get(i).name;
				isDataSet[i] = false;
				if (holder.equals(TYPE.DATASET) && i == location.size() - 1) {
					isDataSet[i] = true;
				}
				try {
					if (isDataSet[i]) {
						groupIds[i] = H5.H5Dopen(loc_id, name, HDF5Constants.H5P_DEFAULT);
					} else {
						H5.H5Gget_info_by_name(loc_id, name, HDF5Constants.H5P_DEFAULT);
						groupIds[i] = H5.H5Gopen(loc_id, name, HDF5Constants.H5P_DEFAULT);
					}
				} catch (Exception ex) {
					if (!isDataSet[i]) {
						groupIds[i] = H5.H5Gcreate(loc_id, name, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT,
								HDF5Constants.H5P_DEFAULT);
					} else {
						throw new Exception("DataSet does not exist " + location);
					}
				}
			}
			return;
		} catch (Exception ex) {
			throw new Exception("Unable to write to file:" + fileName + " at group:" + location, ex);
		} finally {
			for (int i = groupIds.length - 1; i >= 0; i--) {
				if (groupIds[i] != -1) {
					if (isDataSet[i]) {
						H5.H5Dclose(groupIds[i]);
					} else {
						H5.H5Gclose(groupIds[i]);
					}
				}
			}
			if (fileId >= 0)
				H5.H5Fclose(fileId);
		}
	}

	public AbstractDataset createDataSet(Hdf5HelperData hData, boolean extend) throws NullPointerException {
		int datatypeClass = hData.h5Datatype.getDatatypeClass();
		int datatypeSize = hData.h5Datatype.getDatatypeSize();
		int dtype = HDF5Loader.getDtype(datatypeClass, datatypeSize);
		int dims[] = new int[hData.dims.length];
		for (int i = 0; i < hData.dims.length; i++)
			dims[i] = (int) hData.dims[i];

		return HDF5Loader.createDataset(hData.data, dims, dtype, extend);
	}

	/*
	 * sstart = new long[rank]; // source start sstride = new long[rank]; // source steps dsize = new long[rank]; //
	 * destination size block = null; for( int i=0;i<rank;i++){ sstart[i]=0; sstride[i]=1; dsize[i] = dims[i]; } int len
	 * = 1; for (int i = 0; i < dsize.length; i++) { len *= dsize[i]; } long[] data_maxdims=null; long[] data_dims = new
	 * long[]{len};//dims; int data_rank=1;//rank; if( data == null){ int len = 1; for (int i = 0; i < dims.length; i++)
	 * { len *= dims[i]; } data = H5Datatype.allocateArray(mem_type_id, len); }
	 */
	public Hdf5HelperData readDataSetAll(String fileName, String location, String dataSetName, boolean getData)
			throws Exception {
		try{
			return readDataSet(fileName, location, dataSetName, null, null, null, null, null, null, 0, null, getData);
		} 
		catch(Exception e){
			logger.error("Error reading " + location + "/" + dataSetName + " from " + fileName, e);
			throw e;
		}
	}

	/**
	 * 
	 * @param fileName 
	 * @param location
	 * @return  list of names of H5 DATASETS within the group specified by the location 
	 * @throws Exception
	 */
	public String [] getListOfDatasets(String fileName, String location) throws Exception {
		Vector<String> names = new Vector<String>();
		int fileId = -1;
		try {
			fileId = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
			if (fileId < 0) {
				throw new Exception("Unable to open file `" + fileName + "`");
			}
			int groupId = H5.H5Gopen(fileId, location, HDF5Constants.H5P_DEFAULT);
			if (groupId <= 0) {
				throw new Exception("Unable to open location " + location);
			}
			try{
				H5G_info_t h5Gget_info = H5.H5Gget_info(groupId);
				int nelems = (int) h5Gget_info.nlinks;
				if (nelems > 0) {
					try {
						int[] oTypes = new int[nelems];
						int[] lTypes = new int[nelems];
						long[] oids = new long[nelems];
						String[] oNames = new String[nelems];
						H5.H5Gget_obj_info_all(fileId, location, oNames, oTypes, lTypes, oids, HDF5Constants.H5_INDEX_NAME);
						// Iterate through the file to see members of the group
						for (int i = 0; i < nelems; i++) {
							if (oNames[i] != null && oTypes[i] == HDF5Constants.H5O_TYPE_DATASET ) {
								names.add(oNames[i]);
							}
						}
					} catch (HDF5Exception ex) {
						throw new Exception("Could not get objects info from group", ex);
					}
				}
			} finally {
				H5.H5Gclose(groupId);
			}
		} finally {
			if (fileId >= 0)
				H5.H5Fclose(fileId);
		}
		return names.toArray(new String[0]);
	}
	public long lenFromDims(long[] dims) {
		long length = 1;
		for (int i = 0; i < dims.length; i++) {
			length *= dims[i];
		}
		return length;
	}

	public Object AllocateMemory(int native_mem_type, long[] data_dims) throws Exception {
		long lenFromDims = lenFromDims(data_dims);
		if (lenFromDims > Integer.MAX_VALUE)
			throw new Exception("Requested size of memory > Integer.MAX_VALUE." + lenFromDims);
		Object data = H5Datatype.allocateArray(native_mem_type, (int) lenFromDims);
		if (data == null)
			throw new Exception("Unable to allocate memory :" + lenFromDims);
		return data;

	}

	public Hdf5HelperData readDataSet(String fileName, String groupName, String dataSetName, long[] sstart,
			long[] sstride, long[] dsize) throws Exception {
		Hdf5HelperData data2 = Hdf5Helper.getInstance().readDataSetAll(fileName, groupName, dataSetName, false);
		long[] dims = data2.dims;
		if (sstart.length != dims.length)
			throw new IllegalArgumentException("sstart.length != dims.length");
		if (sstride.length != dims.length)
			throw new IllegalArgumentException("sstride.length != dims.length");
		if (dsize.length != dims.length)
			throw new IllegalArgumentException("dsize.length != dims.length");
		long[] block = null;
		long length = lenFromDims(dsize);
		long[] data_maxdims = null;
		long[] data_dims = new long[] { length };
		int mem_type_id = data2.native_type;
		Object data =  H5Datatype.allocateArray(mem_type_id, (int) length);
		return readDataSet(fileName, groupName, dataSetName, sstart, sstride, dsize, block, data_maxdims, data_dims,
				mem_type_id, data, true);
	}

	public Hdf5HelperData readDataSet(String fileName, String groupName, String dataSetName, long[] sstart, // source
																											// start
			long[] sstride, // source steps
			long[] dsize, // destination size
			long[] block, // = null;
			long[] data_maxdims, // = null
			long[] data_dims, int native_mem_type, Object data, boolean getData) throws Exception {
		int fileId = -1;
		try {
			fileId = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
			if (fileId < 0) {
				throw new IllegalArgumentException("Unable to open file `" + fileName + "`");
			}
			int groupId = H5.H5Gopen(fileId, groupName, HDF5Constants.H5P_DEFAULT);
			if (groupId <= 0) {
				throw new IllegalArgumentException("Unable to open group " + groupName);
			}
			try {
				int datasetId = H5.H5Dopen(groupId, dataSetName, HDF5Constants.H5P_DEFAULT);
				if (datasetId <= 0)
					throw new IllegalArgumentException("Unable to open dataSetName " + dataSetName);
				try {
					int dataspaceId = H5.H5Dget_space(datasetId);
					if (dataspaceId <= 0)
						throw new IllegalArgumentException("Unable to open dataspace ");
					try {
						int xfer_plist_id = HDF5Constants.H5P_DEFAULT;

						if (sstart != null) {
							if (data == null) {
								data = AllocateMemory(native_mem_type, data_dims);
							}
							/*
							 * Define hyperslab in the dataset.
							 */

							int status = H5.H5Sselect_hyperslab(dataspaceId, HDF5Constants.H5S_SELECT_SET, sstart,
									sstride, dsize, block);
							if (status < 0)
								throw new Exception("Error calling H5Sselect_hyperslab:" + status);

							/*
							 * Read data from hyperslab in the file into the hyperslab in memory and display.
							 */
							// status ok if not -1
							/*
							 * Define the memory dataspace.
							 */

							int mem_dataspace_id = H5.H5Screate_simple(data_dims.length, data_dims, data_maxdims);
							status = H5.H5Sselect_all(mem_dataspace_id);
							if (status < 0)
								throw new Exception("Error calling H5Sselect_all:" + status);

							H5Datatype h5Datatype = new H5Datatype(native_mem_type);
							int native_type = H5.H5Tget_native_type(native_mem_type);
							status = H5.H5Dread(datasetId, native_mem_type, mem_dataspace_id, dataspaceId,
									xfer_plist_id, data);
							if (status < 0)
								throw new Exception("Error calling H5Dread:" + status);
							return new Hdf5HelperData(dsize, data, h5Datatype, native_type);
						}
						int rank = H5.H5Sget_simple_extent_ndims(dataspaceId);
						long[] dims = new long[rank];
						H5.H5Sget_simple_extent_dims(dataspaceId, dims, null);
						int len = 1;
						for (int i = 0; i < dims.length; i++) {
							len *= dims[i];
						}

						int mem_type_id = H5.H5Dget_type(datasetId);// todo ensure it is closed in a finally block
						try {
							H5Datatype h5Datatype = new H5Datatype(mem_type_id);
							int native_type = H5.H5Tget_native_type(mem_type_id);
							if (data != null || getData) {
								if (data == null)
									data = H5Datatype.allocateArray(mem_type_id, len);
								H5.H5Dread(datasetId, mem_type_id, HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
										HDF5Constants.H5P_DEFAULT, data);

							}
							return new Hdf5HelperData(dims, data, h5Datatype, native_type);
						} finally {
							if (mem_type_id > 0)
								H5.H5Tclose(mem_type_id);
						}

					} finally {
						H5.H5Sclose(dataspaceId);
					}
				} finally {
					H5.H5Dclose(datasetId);
				}
			} finally {
				H5.H5Gclose(groupId);
			}
		} finally {
			if (fileId >= 0)
				H5.H5Fclose(fileId);
		}
	}

	public Hdf5HelperData readAttribute(String fileName, TYPE attribHolder, String attributeHolderName,
			String attributeName) throws Exception {
		if (StringUtils.isEmpty(fileName)) {
			throw new IllegalArgumentException("fileName is empty");
		}
		if (StringUtils.isEmpty(attributeHolderName)) {
			throw new IllegalArgumentException("attributeHolderName is empty");
		}
		if (StringUtils.isEmpty(attributeName)) {
			throw new IllegalArgumentException("attributeName is empty");
		}
		if (attribHolder == null) {
			throw new IllegalArgumentException("attribHolder is null");
		}
		// Open an existing dataset.
		int fileId = -1;
		int attribHolderId = -1;
		int attributeId = -1;
		int filetypeId = -1;
		int dataspaceId = -1;
		int memtype_id = -1;
		try {
			fileId = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
			if (fileId < 0) {
				throw new IllegalArgumentException("Unable to open file " + fileName);
			}
			try {
				if (TYPE.GROUP.equals(attribHolder)) {
					attribHolderId = H5.H5Gopen(fileId, attributeHolderName, HDF5Constants.H5P_DEFAULT);
				} else if (TYPE.DATASET.equals(attribHolder)) {
					attribHolderId = H5.H5Dopen(fileId, attributeHolderName, HDF5Constants.H5P_DEFAULT);
				}
			} catch (Exception e) {
				throw new Exception("Error opening attributeHolder " + attributeHolderName, e);
			}

			try {
				attributeId = H5.H5Aopen_by_name(attribHolderId, ".", attributeName, HDF5Constants.H5P_DEFAULT,
						HDF5Constants.H5P_DEFAULT);
			} catch (Exception e) {
				throw new Exception("Error opening attribute " + attributeName, e);
			}
			dataspaceId = H5.H5Aget_space(attributeId);
			if (dataspaceId <= 0)
				throw new IllegalArgumentException("Unable to open dataspace ");

			int rank = H5.H5Sget_simple_extent_ndims(dataspaceId);
			long[] dims = new long[rank];
			H5.H5Sget_simple_extent_dims(dataspaceId, dims, null);
			int len = 1;
			for (int i = 0; i < dims.length; i++) {
				len *= dims[i];
			}

			int mem_type_id = H5.H5Aget_type(attributeId);// todo ensure it is closed in a finally block
			H5Datatype h5Datatype = new H5Datatype(mem_type_id);
			int native_type = H5.H5Tget_native_type(mem_type_id);
			Object data = H5Datatype.allocateArray(mem_type_id, len);
			H5.H5Aread(attributeId, mem_type_id, data);

			return new Hdf5HelperData(dims, data, h5Datatype, native_type);

		} finally {

			// End access to the dataset and release resources used by it.
			if (attributeId >= 0)
				H5.H5Aclose(attributeId);

			if (attribHolderId >= 0) {
				if (TYPE.GROUP.equals(attribHolder)) {
					H5.H5Gclose(attribHolderId);
				} else if (TYPE.DATASET.equals(attribHolder)) {
					H5.H5Dclose(attribHolderId);
				}
			}

			if (dataspaceId >= 0)
				H5.H5Sclose(dataspaceId);

			if (filetypeId >= 0)
				H5.H5Tclose(filetypeId);

			if (memtype_id >= 0)
				H5.H5Tclose(memtype_id);
			if (fileId >= 0)
				H5.H5Fclose(fileId);
		}
	}

	public String readAttributeAsString(String fileName, TYPE attribHolder, String attributeHolderName,
			String attributeName) throws Exception {
		if (StringUtils.isEmpty(fileName)) {
			throw new IllegalArgumentException("fileName is empty");
		}
		if (StringUtils.isEmpty(attributeHolderName)) {
			throw new IllegalArgumentException("attributeHolderName is empty");
		}
		if (StringUtils.isEmpty(attributeName)) {
			throw new IllegalArgumentException("attributeName is empty");
		}
		if (attribHolder == null) {
			throw new IllegalArgumentException("attribHolder is null");
		}
		String result = null;
		// Open an existing dataset.
		int fileId = -1;
		int attribHolderId = -1;
		int attributeId = -1;
		int filetypeId = -1;
		int dataspaceId = -1;
		int memtype_id = -1;
		try {
			fileId = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDONLY, HDF5Constants.H5P_DEFAULT);
			if (fileId < 0) {
				throw new IllegalArgumentException("Unable to open file " + fileName);
			}
			try {
				if (TYPE.GROUP.equals(attribHolder)) {
					attribHolderId = H5.H5Gopen(fileId, attributeHolderName, HDF5Constants.H5P_DEFAULT);
				} else if (TYPE.DATASET.equals(attribHolder)) {
					attribHolderId = H5.H5Dopen(fileId, attributeHolderName, HDF5Constants.H5P_DEFAULT);
				}
			} catch (Exception e) {
				throw new Exception("Error opening attributeHolder " + attributeHolderName, e);
			}

			try {
				attributeId = H5.H5Aopen_by_name(attribHolderId, ".", attributeName, HDF5Constants.H5P_DEFAULT,
						HDF5Constants.H5P_DEFAULT);
			} catch (Exception e) {
				throw new Exception("Error opening attribute " + attributeName, e);
			}

			int sdim = -1;
			filetypeId = H5.H5Aget_type(attributeId);

			sdim = H5.H5Tget_size(filetypeId);
			sdim++; // Make room for null terminator

			// Get dataspace and allocate memory for read buffer.
			dataspaceId = H5.H5Aget_space(attributeId);

			long[] dims = new long[2];
			H5.H5Sget_simple_extent_dims(dataspaceId, dims, null);

			// Allocate space for data.
			byte[][] dset_data = new byte[(int) dims[0]][sdim];
			StringBuffer[] str_data = new StringBuffer[(int) dims[0]];

			// Create the memory datatype.
			memtype_id = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
			H5.H5Tset_size(memtype_id, sdim);

			// Read data.
			int aread = H5.H5Aread(attributeId, memtype_id, dset_data);
			if (aread < 0)
				throw new Exception("Error in H5Aread");

			byte[] tempbuf = new byte[sdim];
			for (int indx = 0; indx < (int) dims[0]; indx++) {
				for (int jndx = 0; jndx < sdim; jndx++) {
					tempbuf[jndx] = dset_data[indx][jndx];
				}
				str_data[indx] = new StringBuffer(new String(tempbuf).trim());
			}

			// Output the data to the screen.
			for (int indx = 0; indx < dims[0]; indx++) {
				result = str_data[indx].toString();
			}

		} finally {
			// End access to the dataset and release resources used by it.
			if (attributeId >= 0)
				H5.H5Aclose(attributeId);

			if (attribHolderId >= 0) {
				if (TYPE.GROUP.equals(attribHolder)) {
					H5.H5Gclose(attribHolderId);
				} else if (TYPE.DATASET.equals(attribHolder)) {
					H5.H5Dclose(attribHolderId);
				}
			}

			if (dataspaceId >= 0)
				H5.H5Sclose(dataspaceId);
			if (filetypeId >= 0)
				H5.H5Tclose(filetypeId);

			if (memtype_id >= 0)
				H5.H5Tclose(memtype_id);
			if (fileId >= 0)
				H5.H5Fclose(fileId);
		}

		return result;
	}
	public void writeAttribute(String fileName, TYPE attribHolder, HDF5HelperLocations location, String attributeName,
			long data) throws Exception {
		writeAttribute(fileName, attribHolder, location, attributeName, new Hdf5HelperData(data));
	}
	public void writeAttribute(String fileName, TYPE attribHolder, HDF5HelperLocations location, String attributeName,
			double data) throws Exception {
		writeAttribute(fileName, attribHolder, location, attributeName, new Hdf5HelperData(data));
	}
	public void writeAttribute(String fileName, TYPE attribHolder, HDF5HelperLocations location, String attributeName,
			int data) throws Exception {
		writeAttribute(fileName, attribHolder, location, attributeName, new Hdf5HelperData(data));
	}
	public void writeAttribute(String fileName, TYPE attribHolder, HDF5HelperLocations location, String attributeName,
			short data) throws Exception {
		writeAttribute(fileName, attribHolder, location, attributeName, new Hdf5HelperData(data));
	}

	public void writeAttribute(String fileName, TYPE attribHolder, HDF5HelperLocations location, String attributeName,
			Hdf5HelperData hData) throws Exception {
		if (StringUtils.isEmpty(fileName)) {
			throw new IllegalArgumentException("fileName is empty");
		}
		if (StringUtils.isEmpty(attributeName)) {
			throw new IllegalArgumentException("attributeName is empty");
		}
		if (attribHolder == null) {
			throw new IllegalArgumentException("attribHolder is null");
		}
		/*
		 * if (ATTRIB_HOLDER.DATASET.equals(attribHolder)) { throw new
		 * IllegalArgumentException("dataset holder not supported"); }
		 */// ensure file and group exists
		createLocation(fileName, attribHolder, location);

		// Open an existing dataset.
		int fileId = -1;
		int attribHolderId = -1;
		int attributeId = -1;
		int dataspaceId = -1;
		int memtype_id = -1;
		String attributeHolderName = location.getLocationForOpen();
		try {
			fileId = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDWR, HDF5Constants.H5P_DEFAULT);
			if (fileId < 0) {
				throw new IllegalArgumentException("Unable to open file " + fileName);
			}
			try {
				if (TYPE.GROUP.equals(attribHolder)) {
					attribHolderId = H5.H5Gopen(fileId, attributeHolderName, HDF5Constants.H5P_DEFAULT);
				} else if (TYPE.DATASET.equals(attribHolder)) {
					attribHolderId = H5.H5Dopen(fileId, attributeHolderName, HDF5Constants.H5P_DEFAULT);
				}
			} catch (Exception e) {
				throw new Exception("Error opening attributeHolder " + attributeHolderName, e);
			}

			// Create dataspace. Setting maximum size to NULL sets the maximum
			// size to be the current size.
			dataspaceId = H5.H5Screate_simple(hData.dims.length, hData.dims, null);
			int typeId = hData.native_type; // H5.H5Tcreate(hData.h5Datatype.getDatatypeClass(),

			attributeId = H5.H5Acreate(attribHolderId, attributeName, typeId, dataspaceId, HDF5Constants.H5P_DEFAULT,
					HDF5Constants.H5P_DEFAULT);

			int status = H5.H5Awrite(attributeId, typeId, hData.data);

			if (status < 0)
				throw new Exception("Error writin attribute value");

		} finally {
			if (memtype_id >= 0)
				H5.H5Tclose(memtype_id);

			// End access to the dataset and release resources used by it.
			if (attributeId >= 0)
				H5.H5Aclose(attributeId);

			if (dataspaceId >= 0)
				H5.H5Sclose(dataspaceId);

			if (attribHolderId >= 0) {
				if (TYPE.GROUP.equals(attribHolder)) {
					H5.H5Gclose(attribHolderId);
				} else if (TYPE.DATASET.equals(attribHolder)) {
					H5.H5Dclose(attribHolderId);
				}
			}

			if (fileId >= 0)
				H5.H5Fclose(fileId);
		}
	}

	public void setAxisIndexingToMatchGDA(String fileName) throws Exception {
		Hdf5HelperData helperData = Hdf5HelperData.getInstance("GDA ");
		writeToFileSimple(helperData, fileName, HDF5NexusLocation.makeNXEntry(), "program_name");
	}

	/*
	 * Method to read data sets from files and concatenate them together
	 */
	public void concatenateDataSetsFromFiles(List<String> sourceFileNames, HDF5HelperLocations location,
			String dataSetName, String resultFileName) throws Exception {
		for (int i = 0; i < sourceFileNames.size(); i++) {
			// get data for filename
			Hdf5HelperData data2 = readDataSetAll(sourceFileNames.get(i), location.getLocationForOpen(), dataSetName,
					true);

			long[] dimsToWrite = data2.dims;
			if (dimsToWrite[0] > 1) {
				dimsToWrite = new long[dimsToWrite.length + 1];
				System.arraycopy(data2.dims, 0, dimsToWrite, 1, data2.dims.length);
				dimsToWrite[0] = 1;
				data2 = new Hdf5HelperData(dimsToWrite, data2.data, data2.h5Datatype, data2.native_type);
			}
			boolean[] extendible = new boolean[dimsToWrite.length];
			Arrays.fill(extendible, false);
			long[] chunk_dims = dimsToWrite;
			extendible[0] = true;
			long[] offset = new long[dimsToWrite.length];
			Arrays.fill(offset, 0);
			offset[0] = i;
			writeToFile(data2, resultFileName, location, dataSetName, chunk_dims, extendible, offset);
		}
	}

	/*
	 * Method to read data sets from files and concatenate them together
	 */
	public void concatenateDataSets(List<Hdf5HelperData> data, HDF5HelperLocations location, String dataSetName,
			String resultFileName) throws Exception {
		for (int i = 0; i < data.size(); i++) {
			// get data for filename
			Hdf5HelperData data2 = data.get(i);

			long[] dimsToWrite = data2.dims;
			if (dimsToWrite[0] > 1) {
				dimsToWrite = new long[dimsToWrite.length + 1];
				System.arraycopy(data2.dims, 0, dimsToWrite, 1, data2.dims.length);
				dimsToWrite[0] = 1;
				data2 = new Hdf5HelperData(dimsToWrite, data2.data, data2.h5Datatype, data2.native_type);

			}
			boolean[] extendible = new boolean[dimsToWrite.length];
			Arrays.fill(extendible, false);
			long[] chunk_dims = dimsToWrite;
			extendible[0] = true;
			long[] offset = new long[dimsToWrite.length];
			Arrays.fill(offset, 0);
			offset[0] = i;

			writeToFile(data2, resultFileName, location, dataSetName, chunk_dims, extendible, offset);
		}
	}

}
