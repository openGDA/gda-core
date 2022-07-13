/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
