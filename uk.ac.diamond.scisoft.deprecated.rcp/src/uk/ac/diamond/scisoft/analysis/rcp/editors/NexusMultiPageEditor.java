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

package uk.ac.diamond.scisoft.analysis.rcp.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.DataSetPlotView;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.IDataSetPlotViewProvider;

public class NexusMultiPageEditor extends MultiPageEditorPart  implements IDataSetPlotViewProvider {

	private static final Logger logger = LoggerFactory.getLogger(NexusMultiPageEditor.class);
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException{
        super.init(site, input);
	    setPartName(input.getName());
    }
	
	/**
	 * It might be necessary to show the tree editor on the first page.
	 * A property can be introduced to change the page order if this is required.
	 */
	@Override
	protected void createPages() {
		try {

			final DataSetPlotEditor dataSetEditor = new DataSetPlotEditor();
			addPage(0, dataSetEditor, getEditorInput());
			setPageText(0, "Plot");

			final NexusTreeEditor treePage = new NexusTreeEditor(false);
			addPage(1, treePage,   getEditorInput());
			setPageText(1, "Tree");
			addPageChangedListener(treePage);

			final DataEditor dataEditor = new DataEditor();
			dataEditor.setPlotter(dataSetEditor.getPlotWindow());
			addPage(2, dataEditor,   getEditorInput());
			setPageText(2, "Data");
			addPageChangedListener(dataEditor);

			
		} catch (PartInitException e) {
			logger.error("Cannot initiate "+getClass().getName()+"!", e);
		}
	}

	/** 
	 * No Save
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	/** 
	 * No Save
	 */
	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	/** 
	 * We are not saving this class
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public DataSetPlotView getDataSetPlotView() {
		return ((DataSetPlotEditor)getEditor(0)).getDataSetPlotView();
	}
	
	@Override
	public void setActivePage(final int ipage) {
		super.setActivePage(ipage);
	}

	@Override
	public IEditorPart getActiveEditor() {
		return super.getActiveEditor();
	}


}
