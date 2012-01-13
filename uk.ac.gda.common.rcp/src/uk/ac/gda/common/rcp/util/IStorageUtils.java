/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.common.rcp.util;

import java.io.InputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class IStorageUtils {

	/**
	 * Wrapper for {@link IStorage#getContents()} that attempts an 
	 * {@link IResource#refreshLocal(int, IProgressMonitor)} if the initial call
	 * to getContents throws an {@link IResourceStatus#OUT_OF_SYNC_LOCAL}
	 * @param file the IStorage to getContents on
	 * @param monitor optional monitor
	 * @return an input stream containing the contents of this storage
	 * @throws CoreException if the contents of this storage could not be accessed.
	 */
	public static InputStream getContents(IStorage file, IProgressMonitor monitor) throws CoreException {
		try {
			return file.getContents();
		} catch (CoreException ce) {
			if (ce.getStatus().getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
				// try to refresh the file, and then try again to get the contents
				Object adapterObj = file.getAdapter(IResource.class);
				if (adapterObj != null) {
					((IResource)adapterObj).refreshLocal(IResource.DEPTH_ZERO, monitor);
					return file.getContents();
				} 
			}
			throw ce;
		}
	}
	
	/**
	 * Wrapper for {@link IStorage#getContents()} that attempts an 
	 * {@link IResource#refreshLocal(int, IProgressMonitor)} if the initial call
	 * to getContents throws an {@link IResourceStatus#OUT_OF_SYNC_LOCAL}
	 * @param file the IStorage to getContents on
	 * @return an input stream containing the contents of this storage
	 * @throws CoreException if the contents of this storage could not be accessed.
	 */
	public static InputStream getContents(IStorage file) throws CoreException {
		return getContents(file, null);
	}
}
