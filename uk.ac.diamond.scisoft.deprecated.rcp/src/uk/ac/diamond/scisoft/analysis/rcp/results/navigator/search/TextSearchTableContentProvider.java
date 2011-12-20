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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.search.ui.text.AbstractTextSearchResult;

/**
 * TODO: this class should replace JavaSearchTableContentProvider
 * (must generalize type of fResult to AbstractTextSearchResult in JavaSearchContentProvider)
 */
public class TextSearchTableContentProvider implements IStructuredContentProvider {
	protected final Object[] EMPTY_ARRAY= new Object[0];
	private AbstractTextSearchResult fSearchResult;
	private TableViewer fTableViewer;

	/*
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof AbstractTextSearchResult)
			return ((AbstractTextSearchResult) inputElement).getElements();
		return EMPTY_ARRAY;
	}

	/*
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		//nothing
	}

	/*
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fTableViewer= (TableViewer) viewer;
		fSearchResult= (AbstractTextSearchResult) newInput;
	}

	public void elementsChanged(Object[] updatedElements) {
		
		for (int i= 0; i < updatedElements.length; i++) {
			if (fSearchResult.getMatchCount(updatedElements[i]) > 0) {
				if (fTableViewer.testFindItem(updatedElements[i]) != null)
					fTableViewer.refresh(updatedElements[i]);
				else
					fTableViewer.add(updatedElements[i]);
			} else {
				fTableViewer.remove(updatedElements[i]);
			}
		}
	}

	public void clear() {
		//TODO: copied from JavaSearchTableContentProvider
		fTableViewer.refresh();
	}
}
