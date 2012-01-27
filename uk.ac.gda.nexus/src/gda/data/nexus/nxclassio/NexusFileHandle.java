/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.data.nexus.nxclassio;

import gda.data.nexus.extractor.NexusExtractorException;

import java.util.Hashtable;
import java.util.Set;
import java.util.Map.Entry;

import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

/**
 * Class to handle access to NexusFiles
 *
 */
public class NexusFileHandle {
	/**
	 * @param dimensions
	 * @return Total size of a buffer large enough to hold all the data 
	 */
	static public int calcTotalLength(int[] dimensions) {
		int totalLength = 1;
		for (int i = 0; i < dimensions.length; i++) {
			totalLength *= dimensions[i];
		}
		return totalLength;
	}
	Boolean create = false;
	NexusPath currentNexusPath = null;
	private NexusFile file = null;
	final String fileName;

	final Boolean writeable;

	/**
	 * @param fileName
	 * @param writeable
	 */
	public NexusFileHandle(String fileName, boolean writeable) {
		this.fileName = fileName;
		this.writeable = writeable;
		this.create = writeable;
	}

	synchronized void close() throws NexusException {
		if (file != null) {
			file.close();
			file = null;
		}
		currentNexusPath = null;
	}

	private synchronized void closeData() throws NexusException {
		if (file != null) {
			if (currentNexusPath != null) {
				if (!currentNexusPath.getDataSetName().isEmpty()) {
					file.closedata();
				}
				try {
					for (@SuppressWarnings("unused")
					final NexusGroup group : currentNexusPath.getGroups()) {
						file.closegroup();
					}
				} catch (NexusException ex) {
					close(); // get out of here
					throw ex;
				}

			}
			currentNexusPath = null;
		}
	}

	synchronized void getdata(NexusPath nexusPath, double[] buffer) throws NexusException, NexusExtractorException {
		openData(nexusPath, false);
		file.getdata(buffer);
	}

	synchronized void getinfo(NexusPath nexusPath, int[] iDim, int[] iStart) throws NexusException,
			NexusExtractorException {
		openData(nexusPath, false);
		file.getinfo(iDim, iStart);
	}

	@SuppressWarnings("unchecked")
	synchronized String getNameForClass(NexusPath nexusPath, String className) throws NexusExtractorException {
		try {
			openData(nexusPath, false);
			Hashtable<String, String> dir = file.groupdir();
			Set<Entry<String, String>> set = dir.entrySet();
			for (Entry<String, String> entry : set) {
				if (entry.getValue().equals(className))
					return entry.getKey();
			}
		} catch (Exception ex) {
			throw new NexusExtractorException("Unable to find item of class " + className + " at "
					+ nexusPath.toString(), ex);
		}
		throw new NexusExtractorException("Unable to find item of class " + className + " at " + nexusPath.toString());

	}

	synchronized String getString(NexusPath nexusPath) throws NexusException, NexusExtractorException {
		openData(nexusPath, false);
		int[] iDim = new int[20];
		int[] iStart = new int[2];
		file.getinfo(iDim, iStart);
		int dimension = iDim[0]; // it is an n by 1 array rather than a 1 by n
		byte[] bytes = new byte[dimension];
		file.getdata(bytes);
		return new String(bytes);
	}

	@SuppressWarnings("unchecked")
	synchronized void openData(NexusPath nexusPath, boolean create) throws NexusException, NexusExtractorException {
		openfile();
		if (currentNexusPath == null) {
			try {
				for (NexusGroup group : nexusPath.getGroups()) {
					if (create) {
						Hashtable<String, String> dir = file.groupdir();
						String className = dir.get(group.getName());
						if (className == null) {
							file.makegroup(group.getName(), group.getNXclass());
						} else if (!className.equals(group.getNXclass())) {
							throw new NexusExtractorException("Unable to open " + nexusPath
									+ " as other class of element with same name exists");
						}
					}
					file.opengroup(group.getName(), group.getNXclass());
				}
			} catch (NexusException ex) {
				close(); // get out of here
				throw ex;
			}
			if (!nexusPath.getDataSetName().isEmpty()) {
				file.opendata(nexusPath.getDataSetName());
			}
			currentNexusPath = NexusPath.getInstance(nexusPath);
		} else if (!currentNexusPath.equals(nexusPath)) {
			closeData();
			try {
				for (NexusGroup group : nexusPath.getGroups()) {
					if (create) {
						Hashtable<String, String> dir = file.groupdir();
						String className = dir.get(group.getName());
						if (className == null) {
							file.makegroup(group.getName(), group.getNXclass());
						} else if (!className.equals(group.getNXclass())) {
							throw new NexusExtractorException("Unable to open " + nexusPath
									+ " as other class of element with same name exists");
						}
					}
					file.opengroup(group.getName(), group.getNXclass());
				}
			} catch (NexusException ex) {
				close(); // get out of here
				throw ex;
			}
			if (!nexusPath.getDataSetName().isEmpty()) {
				file.opendata(nexusPath.getDataSetName());
			}
			currentNexusPath = NexusPath.getInstance(nexusPath);
		}

	}

	private synchronized void openfile() throws NexusException {
		if (file == null) {
			file = new NexusFile(fileName, writeable ? (create ? NexusFile.NXACC_CREATE5 : NexusFile.NXACC_RDWR)
					: NexusFile.NXACC_READ);
			create = false; // only create once
			currentNexusPath = null;
		}
	}

	private synchronized void setData(NexusPath nexusPath, String dataName, int type, int[] dims, Object data)
			throws NexusException, NexusExtractorException {
		NexusPath path = NexusPath.getInstance(nexusPath);
		path.setDataSetName(null);
		openData(path, true);
		file.makedata(dataName, type, dims.length, dims);
		path.setDataSetName(dataName);
		openData(path, false);
		file.putdata(data);
	}

	synchronized void setDoubleData(NexusPath nexusPath, String dataName, int[] dims, double[] data)
			throws NexusException, NexusExtractorException {
		setData(nexusPath, dataName, NexusFile.NX_FLOAT64, dims, data);
	}

	synchronized void setString(NexusPath nexusPath, String dataName, String value) throws NexusException,
			NexusExtractorException {
		byte[] buf = value.getBytes();
		int[] dims = new int[] { buf.length };
		setData(nexusPath, dataName, NexusFile.NX_CHAR, dims, buf);
	}
}
