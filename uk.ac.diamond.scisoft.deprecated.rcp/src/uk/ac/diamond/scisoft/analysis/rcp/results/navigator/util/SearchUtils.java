/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.rcp.results.navigator.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

public class SearchUtils {

	/**
	 * 
	 * @param top
	 * @param searchString
	 * @param caseSensitive
	 * @param regularExpression
	 * @param result
	 * @throws CoreException 
	 */
	public static void searchNexus(final IContainer top, 
			                       final String     searchString, 
			                       final boolean    caseSensitive,
			                       final boolean    regularExpression, 
			                       final AbstractTextSearchResult result,
			                       final IProgressMonitor monitor) throws CoreException {
		
		if (monitor.isCanceled()) return;
        
		final IResource[] members = top.members();
        for (int i = 0; i < members.length; i++) {
			
    		if (monitor.isCanceled()) return;
        	if (members[i].isHidden()) continue;
        	if (members[i] instanceof IFile) {
				if (SearchUtils.isMatchedFile(members[i].getName(), searchString, caseSensitive, regularExpression)) {
					if (members[i].getName().toLowerCase().endsWith(".nxs")) {
						result.addMatch(new Match(members[i],0,0));
					}
				}
			} else if (members[i] instanceof IContainer) {
				SearchUtils.searchNexus((IContainer)members[i], searchString, caseSensitive, regularExpression, result, monitor);
			}
		}
		
	}
	
	private static boolean isMatchedFile(final String  name,
					                           String  searchString,
					                     final boolean caseSensitive,
					                     final boolean regularExpression) {
		
		if (!regularExpression) {
			return name.indexOf(searchString)>-1;
		}
		
		final Pattern pattern = Pattern.compile(searchString, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		final Matcher matcher = pattern.matcher(name);
		final boolean matched = matcher.matches();
		return matched;
	}

}
