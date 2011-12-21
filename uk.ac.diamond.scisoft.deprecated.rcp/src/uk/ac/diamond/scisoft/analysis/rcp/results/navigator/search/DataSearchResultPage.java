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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.rcp.results.navigator.DataNavigator;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.ui.file.IFileConst;
import uk.ac.gda.ui.file.IFileTreeColumnProvider;
import uk.ac.gda.ui.file.IFileTreeLabelProvider;
import uk.ac.gda.util.list.SortNatural;

public class DataSearchResultPage extends AbstractTextSearchViewPage implements ISearchResultPage {

	private TextSearchTableContentProvider fContentProvider;
	
	public DataSearchResultPage() {
	    super(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
	}
	
	@Override
	protected void clear() {
		if (fContentProvider != null) fContentProvider.clear();
	}

	@Override
	protected void configureTableViewer(TableViewer viewer) {
		
		viewer.getTable().setHeaderVisible(true);
		
		final TableViewerColumn name = new TableViewerColumn(viewer, SWT.NONE, 0);
		name.setLabelProvider(new IFileTreeColumnProvider());
		name.getColumn().setText("Name");
		name.getColumn().setWidth(150);
		
		final TableViewerColumn path = new TableViewerColumn(viewer, SWT.NONE, 1);
		path.setLabelProvider(new IFileTreeColumnProvider());
		path.getColumn().setText("Folder");
		path.getColumn().setWidth(150);

		final TableViewerColumn size = new TableViewerColumn(viewer, SWT.NONE, 2);
		size.setLabelProvider(new IFileTreeColumnProvider());
		size.getColumn().setText("Size");
		size.getColumn().setWidth(150);

		viewer.setLabelProvider(new IFileTreeLabelProvider(IFileConst.PATH,IFileConst.SIZE));
		
		fContentProvider= new TextSearchTableContentProvider();
		viewer.setContentProvider(fContentProvider);
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final DataNavigator  view        =  (DataNavigator)EclipseUtils.getActivePage().findView(DataNavigator.ID);
				if (view!=null) {
					view.setSelected(event.getSelection());
				}
			}			
		});
	}

	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		throw new IllegalStateException("Doesn't support tree mode."); //$NON-NLS-1$
	}

	@Override
	protected void elementsChanged(Object[] objects) {
		
		if (objects==null||objects.length<2) {
			if (fContentProvider != null)
				fContentProvider.elementsChanged(objects);
			return;
		}
		
		final List<Object> obs = Arrays.asList(objects);
		Collections.sort(obs, new SortNatural<Object>(true));
		
		if (fContentProvider != null)
			fContentProvider.elementsChanged(obs.toArray(new Object[obs.size()]));
	}

}
