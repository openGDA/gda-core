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

package uk.ac.diamond.scisoft.analysis.rcp.results.navigator.search;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.results.navigator.util.SearchUtils;

public class DataSearchQuery implements ISearchQuery {

	private static Logger logger = LoggerFactory.getLogger(DataSearchQuery.class);
	
	private String           label;
	private DataSearchResult result;
	private String           searchString;
	private boolean          caseSensitive;
	private boolean          regularExpression;

	public DataSearchQuery(final String searchString, final boolean caseSensitive, final boolean regularExpression) {
		
		this.label  = "Search for files named '"+searchString+"'";
	    this.searchString = searchString;
	    this.caseSensitive= caseSensitive;
	    this.regularExpression=regularExpression;
	    
		this.result = new DataSearchResult(label, this);

	}

	@Override
	public boolean canRerun() {
		return true;
	}

	@Override
	public boolean canRunInBackground() {
		return true;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public ISearchResult getSearchResult() {
		return result;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		
		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject    project = root.getProject("Data");
		try {
			SearchUtils.searchNexus(project, searchString, caseSensitive, regularExpression, result, monitor);
		} catch (CoreException e) {
			logger.error("Search failed",e);
			throw new OperationCanceledException("cannot complete search "+searchString);
		}
		
		return Status.OK_STATUS;
	}

}
