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

package gda.data.nexus;

import java.util.Hashtable;
import java.util.Vector;

import org.nexusformat.NXlink;
import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class used to instrument calls to implementations of NeXusFileInterface
 */
public class NexusFileWrapper implements org.nexusformat.NeXusFileInterface {
	private static final Logger logger = LoggerFactory.getLogger(NexusFileWrapper.class);
	NeXusFileInterface file;
	Vector<String> currentGroupNamePath = new Vector<String>();
	Vector<String> currentGroupNXNamePath = new Vector<String>();
	Vector<String> currentDataPath = new Vector<String>();

	private String getCurrentGroupName() {
		return currentGroupNamePath.size() > 0 ? currentGroupNamePath.lastElement() : "top";
	}

	private String getCurrentGroupNXName() {
		return currentGroupNXNamePath.size() > 0 ? currentGroupNXNamePath.lastElement() : "top";
	}

	private String getCurrentGroup() {
		return getCurrentGroupName() + "-" + getCurrentGroupNXName();
	}

	private String getCurrentData() {
		return currentDataPath.size() > 0 ? currentDataPath.lastElement() : "top-data";
	}

	private String getCurrentAttrLocation() {
		return getCurrentData().equals("top-data") ? getCurrentGroup() : getCurrentData();
	}

	/**
	 * @param file
	 *            to be instrumented
	 */
	public NexusFileWrapper(NeXusFileInterface file) {
		this.file = file;
	}

	@Override
	public void closedata() throws NexusException {
		String currentOpenedData = currentDataPath.lastElement();
		logger.debug("closedata - " + currentOpenedData);
		file.closedata();
		currentDataPath.remove(currentDataPath.size() - 1);
	}

	@Override
	public void closegroup() throws NexusException {
		logger.debug("closegroup - " + getCurrentGroup());
		file.closegroup();
		currentGroupNamePath.remove(currentGroupNamePath.size() - 1);
		currentGroupNXNamePath.remove(currentGroupNXNamePath.size() - 1);
	}

	@Override
	public void opendata(String arg0) throws NexusException {
		logger.debug("opendata - " + arg0);
		file.opendata(arg0);
		currentDataPath.add(arg0);
	}

	@Override
	public void opengroup(String arg0, String arg1) throws NexusException {
		logger.debug("opengroup " + arg0 + " - " + arg1);
		file.opengroup(arg0, arg1);
		currentGroupNamePath.add(arg0);
		currentGroupNXNamePath.add(arg1);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Hashtable attrdir() throws NexusException {
		logger.debug("attrdir - " + getCurrentAttrLocation());
		return file.attrdir();
	}

	@Override
	public void close() throws NexusException {
		file.close();
	}

	@Override
	public void compmakedata(String arg0, int arg1, int arg2, int[] arg3, int arg4, int[] arg5) throws NexusException {
		file.compmakedata(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	@Override
	public void compress(int arg0) throws NexusException {
		file.compress(arg0);
	}

	@Override
	public boolean equals(Object obj) {
		return file.equals(obj);
	}

	@Override
	public void finalize() throws Throwable {
		file.finalize();
	}

	@Override
	public void flush() throws NexusException {
		file.flush();
	}

	@Override
	public void getattr(String arg0, Object arg1, int[] arg2) throws NexusException {
		logger.debug("getattr - " + arg0);
		file.getattr(arg0, arg1, arg2);
	}

	@Override
	public void getdata(Object arg0) throws NexusException {
		logger.debug("get - " + arg0);
		file.getdata(arg0);
	}

	@Override
	public NXlink getdataID() throws NexusException {
		return file.getdataID();
	}

	@Override
	public NXlink getgroupID() throws NexusException {
		return file.getgroupID();
	}

	@Override
	public void getinfo(int[] arg0, int[] arg1) throws NexusException {
		file.getinfo(arg0, arg1);
	}

	@Override
	public void getslab(int[] arg0, int[] arg1, Object arg2) throws NexusException {
		file.getslab(arg0, arg1, arg2);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Hashtable groupdir() throws NexusException {
		logger.debug("groupdir - " + getCurrentGroup());
		return file.groupdir();
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public String inquirefile() throws NexusException {
		return file.inquirefile();
	}

	@Override
	public String isexternalgroup(String arg0, String arg1) throws NexusException {
		return file.isexternalgroup(arg0, arg1);
	}

	@Override
	public void linkexternal(String arg0, String arg1, String arg2) throws NexusException {
		file.linkexternal(arg0, arg1, arg2);
	}

	@Override
	public void makedata(String arg0, int arg1, int arg2, int[] arg3) throws NexusException {
		logger.debug("makedata - " + arg0);
		file.makedata(arg0, arg1, arg2, arg3);
	}

	@Override
	public void makegroup(String arg0, String arg1) throws NexusException {
		logger.debug("makegroup - " + arg0 +" - "+arg1);
		file.makegroup(arg0, arg1);
	}

	@Override
	public void makelink(NXlink arg0) throws NexusException {
		logger.debug("makelink - " + arg0.targetPath);
		file.makelink(arg0);
	}

	@Override
	public void makenamedlink(String arg0, NXlink arg1) throws NexusException {
		file.makenamedlink(arg0, arg1);
	}

	@Override
	public void opengrouppath(String arg0) throws NexusException {
		file.opengrouppath(arg0);
	}

	@Override
	public void openpath(String arg0) throws NexusException {
		file.openpath(arg0);
	}

	@Override
	public void opensourcepath() throws NexusException {
		file.opensourcepath();
	}

	@Override
	public void putattr(String arg0, Object arg1, int arg2) throws NexusException {
		logger.debug("putattr - " + arg0 + " inside " + getCurrentAttrLocation());
		file.putattr(arg0, arg1, arg2);
	}

	@Override
	public void putdata(Object arg0) throws NexusException {
		logger.debug("putdata" + " inside " + getCurrentAttrLocation());
		file.putdata(arg0);
	}

	@Override
	public void putslab(Object arg0, int[] arg1, int[] arg2) throws NexusException {
		logger.debug("putslab");
		file.putslab(arg0, arg1, arg2);
	}

	@Override
	public void setnumberformat(int arg0, String arg1) throws NexusException {
		file.setnumberformat(arg0, arg1);
	}

	@Override
	public String toString() {
		return file.toString();
	}

	@Override
	public void compmakedata(String arg0, int arg1, int arg2, long[] arg3, int arg4, long[] arg5) throws NexusException {
		file.compmakedata(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	@Override
	public void getinfo(long[] arg0, int[] arg1) throws NexusException {
		file.getinfo(arg0, arg1);
	}

	@Override
	public String getpath() throws NexusException {
		return file.getpath();
	}

	@Override
	public void getslab(long[] arg0, long[] arg1, Object arg2) throws NexusException {
		file.getslab(arg0, arg1, arg2);
	}

	@Override
	public String isexternaldataset(String arg0) throws NexusException {
		return file.isexternaldataset(arg0);
	}

	@Override
	public void linkexternaldataset(String arg0, String arg1) throws NexusException {
		file.linkexternaldataset(arg0, arg1);
	}

	@Override
	public void makedata(String arg0, int arg1, int arg2, long[] arg3) throws NexusException {
		logger.debug("makedata - " + arg0);
		file.makedata(arg0, arg1, arg2, arg3);
	}

	@Override
	public void putslab(Object arg0, long[] arg1, long[] arg2) throws NexusException {
		file.putslab(arg0, arg1, arg2);
	}
}