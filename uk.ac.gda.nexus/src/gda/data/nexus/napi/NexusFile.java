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

import java.util.Hashtable;

public class NexusFile {
	org.nexusformat.NexusFile file;

	public NexusFile(String name, int flags) throws NexusException {
		try {
			file = new org.nexusformat.NexusFile(name, flags);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return file.equals(obj);
	}

	public void flush() throws NexusException {
		try {
			file.flush();
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void close() throws NexusException {
		try {
			file.close();
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	@Override
	public void finalize() throws Throwable {
		try {
			file.finalize();
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void makegroup(String name, String nxclass) throws NexusException {
		try {
			file.makegroup(name, nxclass);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void opengroup(String name, String nxclass) throws NexusException {
		try {
			file.opengroup(name, nxclass);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void openpath(String path) throws NexusException {
		try {
			file.openpath(path);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void closegroup() throws NexusException {
		try {
			file.closegroup();
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void compmakedata(String name, int type, int rank, int[] dim, int compression_type, int[] iChunk)
			throws NexusException {
		try {
			file.compmakedata(name, type, rank, dim, compression_type, iChunk);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	@Override
	public String toString() {
		return file.toString();
	}

	public void makedata(String name, int type, int rank, int[] dim) throws NexusException {
		try {
			file.makedata(name, type, rank, dim);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void opendata(String name) throws NexusException {
		try {
			file.opendata(name);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void closedata() throws NexusException {
		try {
			file.closedata();
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void getdata(Object array) throws NexusException {
		try {
			file.getdata(array);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void getslab(int[] start, int[] size, Object array) throws NexusException {
		try {
			file.getslab(start, size, array);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void putdata(Object array) throws NexusException {
		try {
			file.putdata(array);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void putslab(Object array, int[] start, int[] size) throws NexusException {
		try {
			file.putslab(array, start, size);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public Object getattr(String name) throws NexusException {
		try {
			return file.getattr(name);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void putattr(String name, Object array, int iType) throws NexusException {
		try {
			file.putattr(name, array, iType);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public Hashtable<String, ?> attrdir() throws NexusException {
		try {
			return file.attrdir();
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void getinfo(int[] iDim, int[] args) throws NexusException {
		try {
			file.getinfo(iDim, args);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public Hashtable<String, String> groupdir() throws NexusException {
		try {
			return file.groupdir();
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public NXlink getdataID() throws NexusException {
		try {
			return new NXlink(file.getdataID());
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void makelink(NXlink target) throws NexusException {
		try {
			file.makelink(target.link);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void makenamedlink(String name, NXlink target) throws NexusException {
		try {
			file.makenamedlink(name, target.link);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void linkexternaldataset(String name, String nxurl) throws NexusException {
		try {
			file.linkexternaldataset(name, nxurl);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public String isexternaldataset(String name) throws NexusException {
		try {
			return file.isexternaldataset(name);
		} catch (org.nexusformat.NexusException e) {
			throw new NexusException(e);
		}
	}

	public void debugstop() {
		file.debugstop();
	}
}
