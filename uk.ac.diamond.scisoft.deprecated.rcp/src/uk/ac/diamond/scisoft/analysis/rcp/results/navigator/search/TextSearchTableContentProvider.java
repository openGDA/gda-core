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
