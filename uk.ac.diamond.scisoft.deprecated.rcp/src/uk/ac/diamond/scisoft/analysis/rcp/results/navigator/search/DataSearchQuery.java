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
