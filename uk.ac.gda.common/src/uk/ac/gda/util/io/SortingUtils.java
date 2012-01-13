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

package uk.ac.gda.util.io;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import uk.ac.gda.util.list.SortNatural;

/**
 * @author fcp94556
 *
 */
public class SortingUtils {

	/**
	 * 
	 */
	public static final Comparator<File> NATURAL_SORT = new SortNatural<File>(true);
	
	/**
	 * 
	 */
	public static final Comparator<Object> NATURAL_SORT_CASE_INSENSITIVE = new SortNatural<Object>(false);

	private static final Comparator<File> DEFAULT_COMPARATOR = new Comparator<File>() {
			@Override
			public int compare(File one, File two) {
				return one.compareTo(two);
			}
		};

	/**
	 * Lists folders before files
	 * @param dir
	 * @return List<File>
	 */
	public static List<File> getSortedFileList(final File dir) {
		return getSortedFileList(dir, DEFAULT_COMPARATOR);
	}
	
	/**
	 * Lists folders before files
	 * @param dir
	 * @return List<File>
	 */
	public static List<File> getSortedFileList(final File dir, final boolean dirsFirst) {
		return getSortedFileList(dir, DEFAULT_COMPARATOR, dirsFirst);
	}
	
	/**
	 *  Lists folders before files
	 * @param dir
	 * @param comp
	 * @return  List<File>
	 */
	public static List<File> getSortedFileList(final File dir, final Comparator<File> comp) {  
		return getSortedFileList(dir, comp, false);
	}
	
	private static List<File> getSortedFileList(final File dir, final Comparator<File> comp, final boolean dirsFirst) {
	    
		if (!dir.isDirectory())    return null;
	    if (dir.listFiles()==null) return null;

	    final List<File> ret;
	    if (dirsFirst) {
			ret = new ArrayList<File>(dir.listFiles().length);
			
			final List<File> dirs = getSortedFileList(dir.listFiles(new FileFilter() {		
				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			}), comp);
			if (dirs!=null) ret.addAll(dirs);
			
			final List<File> files = getSortedFileList(dir.listFiles(new FileFilter() {		
				@Override
				public boolean accept(File pathname) {
					return !pathname.isDirectory();
				}
			}), comp);
			if (files!=null) ret.addAll(files);
		
	    } else {
	    	ret = getSortedFileList(dir.listFiles(), comp);
	    }
			
		if (ret.isEmpty()) return null;
		return ret;

	}
	/**
	 * @param dir
	 * @param fileFilter 
	 * @param comp
	 * @return  List<File>
	 */
	public static List<File> getSortedFileList(final File dir, FileFilter fileFilter, final Comparator<File> comp) {  
	    return getSortedFileList(dir.listFiles(fileFilter), comp);
	}
	
	/**
	 * @param dir
	 * @param fileFilter
	 * @return List<File> 
	 */
	public static List<File> getSortedFileList(File dir, FileFilter fileFilter) {
	    return getSortedFileList(dir.listFiles(fileFilter), DEFAULT_COMPARATOR);
	}

	/**
	 * @param comp
	 * @return  List<File>
	 */
	private static List<File> getSortedFileList(final File[] fa, final Comparator<File> comp) {
	    
	    if (fa == null || fa.length<1) return null;
	    
	    final List<File> files = new ArrayList<File>(fa.length);
	    files.addAll(Arrays.asList(fa));
	    Collections.sort(files, comp);

	    return files;
	}

	public static void removeIgnoredNames(Collection<String> sets, Collection<Pattern> patterns) {
		if (patterns==null) return;
		if (sets==null)     return;
		for (Iterator<String> it = sets.iterator(); it.hasNext();) {
			final String name = it.next();
			PATTERN_LOOP: for (Pattern pattern : patterns) {
				if (pattern.matcher(name).matches()) {
					it.remove();
					break PATTERN_LOOP;
				}
			}
		}
	}

}



	