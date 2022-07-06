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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import uk.ac.gda.util.list.SortNatural;

/**
 * Sorting utils for eclipse specific things.
 */
public class ISortingUtils {
	
	public static final Comparator<IResource> DEFAULT_COMPARATOR = new Comparator<IResource>() {
		@Override
		public int compare(IResource one, IResource two) {
			return one.getLocation().toString().compareTo(two.getLocation().toString());
		}
	};
	
	public static final Comparator<IResource> NATURAL_COMPARATOR = new SortNatural<IResource>(true);
	/**
	 * Sort file list
	 * @param dir 
	 * @param comp 
	 * @return list
	 * @throws CoreException 
	 */
	public static List<IResource> getSortedFileList(final IContainer            dir,
												    final Comparator<IResource> comp) throws CoreException {
	    
		final IResource[] fa = dir.members();
		if (fa == null || fa.length<1) return null;
	    
	    final List<IResource> files = new ArrayList<IResource>(fa.length);
	    files.addAll(Arrays.asList(fa));
	    Collections.sort(files, comp);

	    return files;
	}
	

	public static Collection<IResource> getSortedFileListIgnoreHidden(final IContainer            dir,
			                                                          final Comparator<IResource> comp) throws CoreException{
		final IResource[] fa = dir.members();
		if (fa == null || fa.length<1) return null;
	    
	    final List<IResource> files = new ArrayList<IResource>(fa.length);
	    for (int i = 0; i < fa.length; i++) {
	    	
	    	IResource member = fa[i];
			if (!member.exists())   continue;
			if (member.isHidden())  continue;
			if (member.isPhantom()) continue;
			if (member.getLocation().toFile().isHidden()) continue;
			if (!member.isAccessible()) continue;
	    	files.add(member);
		}
	    Collections.sort(files, comp);

	    return files;
	}


	/**
	 * Sorted list of folders
	 * @param dir
	 * @return sorted folder list
	 * @throws CoreException 
	 */
	public static List<IFolder> getSortedFolderList(IContainer dir) throws CoreException {
		
		final IResource[] fa = dir.members();
		if (fa == null || fa.length<1) return null;
		
	    final List<IFolder> folders = new ArrayList<IFolder>(7);
	    for (int i = 0; i < fa.length; i++) {
			if (fa[i] instanceof IFolder) {
				folders.add((IFolder)fa[i]);
			}
		}

	    Collections.sort(folders, DEFAULT_COMPARATOR);
        return folders;
	}


	/**
	 * ending -> null allowed (gets all files)
	 * @param folder
	 * @param ending
	 * @return list of files with given ending, does not return <code>null</code>
	 * @throws CoreException 
	 */
	public static List<IFile> getSortedFileList(final IFolder folder, final String ending) throws CoreException {
		
		final IResource[] fa = folder.members();
		if (fa == null || fa.length<1) return null;
		
	    final List<IFile> files = new ArrayList<IFile>(7);
	    for (int i = 0; i < fa.length; i++) {
			if (fa[i] instanceof IFile && (ending==null||fa[i].getName().endsWith(ending))) {
				files.add((IFile)fa[i]);
			}
		}

	    Collections.sort(files, DEFAULT_COMPARATOR);
        return files;
	}


}
