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

import gda.data.nexus.tree.INexusTree;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.nexus.NexusTreeExplorer;
import uk.ac.diamond.scisoft.analysis.rcp.util.NexusUtils;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.DataSetPlotView;
import uk.ac.gda.common.rcp.util.EclipseUtils;

public class NexusTreeEditor extends EditorPart implements IPageChangedListener {
	
	private NexusTreeExplorer ntxp;
	private INexusTree        nexusTree;
	private String            fileName;
	private boolean           isFirstPage;

	public NexusTreeEditor() {
		this(true);
	}
	public NexusTreeEditor(boolean isFirstPage) {
		this.isFirstPage = isFirstPage;
	}

	/**
	 * 
	 */
	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.editors.NexusTreeEditor"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(NexusTreeEditor.class);

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}

	protected void createNexusTree() {
		if (nexusTree != null)
			return;
		try {
			nexusTree = NexusUtils.loadTree(fileName, NexusUtils.getSel(), new NullProgressMonitor());
			if (nexusTree != null && ntxp != null) {
				ntxp.setFilename(fileName);
				ntxp.setNexusTree(nexusTree);
			}
		} catch (Exception e) {
			logger.warn("Could not load NeXus file {}", fileName);
		}
		return;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		ntxp = new NexusTreeExplorer(parent, SWT.NONE, getSite());

		if (isFirstPage) createNexusTree();
	}
	
	@Override
	public void pageChanged(PageChangedEvent event) {
		if (event.getSelectedPage()==this) { // Just selected this page
			createNexusTree();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// do nothing
	}

	@Override
	public void doSaveAs() {
		// do nothing
	}

	@Override
	public void setFocus() {
		ntxp.setFocus();
	}

	@Override
	public void setInput(final IEditorInput input) {
		
		super.setInput(input);

		File f = EclipseUtils.getFile(input);
		if (!f.exists()) {
			logger.warn("File does not exist: {}", input.getName());
			return;			
		}
		this.fileName = f.getAbsolutePath();
		setPartName(f.getName());
		nexusTree = null;
		
		// Do not load tree on init, this breaks lazy loading. This editor may be on a 
		// multi-page editor pane and might not be the first pane, the user may never use it.
		
		// We notify the DataSetPlotView if one is already visible, otherwise we do nothing
		notifyPlotView(input);
	}

	/**
	 * Only talks to this view if there is one, otherwise nothing happens.
	 * @param input
	 */
	private void notifyPlotView(final IEditorInput input) {
		
		final IViewPart part = getEditorSite().getPage().findView(DataSetPlotView.ID);
		if (part!=null) {
		    final DataSetPlotView dView = (DataSetPlotView)part;
		    try {
				dView.setFile(EclipseUtils.getFilePath(input), new NullProgressMonitor());
			} catch (Exception e) {
				logger.error("File "+EclipseUtils.getFilePath(input)+" does not open with "+DataSetPlotView.ID,
						     e);
			}
		}
	}

	public void expandAll() {
		ntxp.expandAll();
	}
	
	@Override
	public void dispose() {
		if (ntxp != null)
			ntxp.dispose();
		super.dispose();
	}
	
	public NexusTreeExplorer getNexusTreeExplorer() {
		return ntxp;
	}
}
