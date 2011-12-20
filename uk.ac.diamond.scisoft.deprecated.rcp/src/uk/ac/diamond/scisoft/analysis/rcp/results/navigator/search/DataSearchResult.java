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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;

public class DataSearchResult extends AbstractTextSearchResult implements ISearchResult {

	private String label;
	private ISearchQuery query;

	public DataSearchResult(final String label, final ISearchQuery query) {
		super();
		this.label     = label;
		this.query     = query;
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public ISearchQuery getQuery() {
		return query;
	}

	@Override
	public String getTooltip() {
		return "Search results for "+label;
	}

	@Override
	public IEditorMatchAdapter getEditorMatchAdapter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFileMatchAdapter getFileMatchAdapter() {
		return new IFileMatchAdapter() {
			@Override
			public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
				return null;
			}
			@Override
			public IFile getFile(Object element) {
				return (IFile)element;
			}
		};
	}

}
