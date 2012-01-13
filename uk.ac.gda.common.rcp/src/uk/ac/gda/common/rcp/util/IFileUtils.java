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

package uk.ac.gda.common.rcp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class IFileUtils {
	
	public static StringBuilder readFile(final IFile file) throws Exception {

		BufferedReader ir = null;
		try {
			ir = new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()));

			// deliberately do not remove BOM here
			int c;
			StringBuilder currentStrBuffer = new StringBuilder();
			final char[] buf = new char[4096];
			while ((c = ir.read(buf, 0, 4096)) > 0) {
				currentStrBuffer.append(buf, 0, c);
			}
			return currentStrBuffer;

		} finally {
			if (ir != null) {
				ir.close();
			}
		}
	}

	public static List<String> parseFile(final IFile file) throws Exception {

		BufferedReader ir = null;
		try {
			ir = new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()));

			// deliberately do not remove BOM here
			String line;
			final List<String> currentStrBuffer = new ArrayList<String>(89);
			while ((line = ir.readLine()) != null) {
				currentStrBuffer.add(line);
			}
			return currentStrBuffer;

		} finally {
			if (ir != null) {
				ir.close();
			}
		}
	}

	public static void saveFile(final IFile file, final List<String> lines) throws Exception {
		BufferedWriter iw = null;
		try {//create an empty InputStream
			PipedInputStream in = new PipedInputStream();

			//create an OutputStream with the InputStream from above as input
			PipedOutputStream out = new PipedOutputStream(in);

			iw = new BufferedWriter(new OutputStreamWriter(out, file.getCharset()));

			// deliberately do not remove BOM here
			for (String line : lines) {
				iw.write(line);
				iw.newLine();
			}
			iw.close();
			
			file.setContents(in, true, true, new NullProgressMonitor());
			
		} finally {
			if (iw != null) {
				iw.close();
			}
		}
	}

	public static long getLength(IResource iResource) throws Exception {
		if (!iResource.exists()) return 0;
		return EFS.getStore(iResource.getLocationURI()).fetchInfo().getLength();
	}

	public static long getLastModified(IResource iResource) throws Exception {
		if (!iResource.exists()) return 0;
		if (iResource instanceof IFile) ((IFile)iResource).getLocalTimeStamp();
		return EFS.getStore(iResource.getLocationURI()).fetchInfo().getLastModified();
	}

	/**
	 * Recursive method which ignores .workspace so that 
	 * we can create links to folders which are the parent of the project.
	 * 
	 * Assumes that the folder path can be traversed and files above workspace are ignored.
	 * 
	 * @param src
	 * @param path
	 * @param monitor
	 * @throws CoreException
	 */
	public static void createLinks(IFolder src, Path path, IProgressMonitor monitor) throws CoreException {
		
		final IPath workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		final File parent = path.toFile();
		final File[] fa   = parent.listFiles();
		for (int i = 0; i < fa.length; i++) {
		    final File file = fa[i];
		    if (!file.isHidden()&&file.isDirectory()) {
		    	final Path    child  = new Path(file.getAbsolutePath());
		    	final IFolder folder = src.getFolder(child.lastSegment());
		    	if (child.isPrefixOf(workspacePath)) {
		    		folder.create(false, true, monitor);
		    		IFileUtils.createLinks(folder, child, monitor);
		    	} else {
		    	    folder.createLink(child, IResource.DEPTH_INFINITE, monitor);
		    	}
		    }
		}
	}

}
