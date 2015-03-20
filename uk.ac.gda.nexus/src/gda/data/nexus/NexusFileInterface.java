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

package gda.data.nexus;

import java.util.Hashtable;

public interface NexusFileInterface {
	public void flush() throws NexusException;

	public void finalize() throws Throwable;

	public void close() throws NexusException;

	public void makegroup(String name, String nxclass) throws NexusException;

	public void opengroup(String name, String nxclass) throws NexusException;

	public void openpath(String path) throws NexusException;

	public void opengrouppath(String path) throws NexusException;

	public String getpath() throws NexusException;

	public void closegroup() throws NexusException;

	public void makedata(String name, int type, int rank, int dim[]) throws NexusException;

	public void makedata(String name, int type, int rank, long dim[]) throws NexusException;

	public void compmakedata(String name, int type, int rank, int dim[], int compression_type, int iChunk[])
			throws NexusException;

	public void compmakedata(String name, int type, int rank, long dim[], int compression_type, long iChunk[])
			throws NexusException;

	public void opendata(String name) throws NexusException;

	public void closedata() throws NexusException;

	public void compress(int compression_type) throws NexusException;

	public void getdata(Object array) throws NexusException;

	public void getslab(int start[], int size[], Object array) throws NexusException;

	public void getslab(long start[], long size[], Object array) throws NexusException;

	public void getattr(String name, Object data, int args[]) throws NexusException;

	public Object getattr(String name) throws NexusException;

	public void putdata(Object array) throws NexusException;

	public void putslab(Object array, int start[], int size[]) throws NexusException;

	public void putslab(Object array, long start[], long size[]) throws NexusException;

	public void putattr(String name, Object array, int iType) throws NexusException;

	public void putattr(String name, Object array, int size[], int iType) throws NexusException;

	public void getinfo(int iDim[], int args[]) throws NexusException;

	public void getinfo(long iDim[], int args[]) throws NexusException;

	public void setnumberformat(int type, String format) throws NexusException;

	public Hashtable<String, String> groupdir() throws NexusException;

	public Hashtable<String, ?> attrdir() throws NexusException;

	public NXlink getgroupID() throws NexusException;

	public NXlink getdataID() throws NexusException;

	public void makelink(NXlink target) throws NexusException;

	public void makenamedlink(String name, NXlink target) throws NexusException;

	public void opensourcepath() throws NexusException;

	public String inquirefile() throws NexusException;

	public void linkexternal(String name, String nxclass, String nxurl) throws NexusException;

	public void linkexternaldataset(String name, String nxurl) throws NexusException;

	public String isexternalgroup(String name, String nxclass) throws NexusException;

	public String isexternaldataset(String name) throws NexusException;
}
