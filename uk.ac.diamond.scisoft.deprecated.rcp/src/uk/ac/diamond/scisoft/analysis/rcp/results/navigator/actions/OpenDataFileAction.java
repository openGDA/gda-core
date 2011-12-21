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

package uk.ac.diamond.scisoft.analysis.rcp.results.navigator.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.AsciiTextView;
import uk.ac.diamond.scisoft.analysis.rcp.views.NexusTreeView;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.DataSetPlotView;


/**
 * Class only deals with nexus and ascii files at the moment - 
 * extend to other files types when time / as needed.
 * 
 * It opens files from the selected object which must be a File, IFile or
 * 
 */
public class OpenDataFileAction extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(OpenDataFileAction.class);
	
	private boolean openView;

	/**
	 * 
	 */
	public OpenDataFileAction() {
		this(false);
	}
		
	/**
	 * @param b
	 */
	public OpenDataFileAction(boolean b) {
		this.openView = b;
	}

	/**
	 * May be called with null event
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    
		final IWorkbenchPage       page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final IStructuredSelection sel  = (IStructuredSelection)page.getSelection();
		
		if (sel == null)                   return Boolean.FALSE;
		if (sel.getFirstElement() == null) return Boolean.FALSE;	
		if (sel.toArray().length!=1)       return Boolean.FALSE;
		
		File file;
		if (sel.getFirstElement() instanceof File ) {
			file = (File)sel.getFirstElement();
		} else if (sel.getFirstElement() instanceof IFile) {
			file = ((IFile)sel.getFirstElement()).getLocation().toFile();
		} else {
			file = new File(sel.getFirstElement().toString());
		}

		return openViewForFile(file);
	}

	public Object openViewForFile(final File file) throws ExecutionException {
		final IWorkbenchPage       page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

		if (file==null)           return Boolean.FALSE;
		if (!file.exists())       return Boolean.FALSE;
	    if (file.isDirectory())   return Boolean.FALSE;
		if (file.isHidden())      return Boolean.FALSE;
		if (file.getName()==null) return Boolean.FALSE;
		if (file.getName().toLowerCase().endsWith(".nxs")) {// Open in nexus view

			IProgressService service = (IProgressService)PlatformUI.getWorkbench().getService(IProgressService.class);

			try {
				// Changed to cancellable as sometimes loading the tree takes ages and you
				// did not mean such to choose the file.
				service.run(true, true, new IRunnableWithProgress() {
	
					@Override
					public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							final NexusTreeView view = (NexusTreeView)getView(NexusTreeView.ID, page);
							if (view!=null) view.loadTree(file.getAbsolutePath(), monitor);
						} catch (Exception ne) {
							logger.error("Cannot open nexus", ne);
						}
						try {
							final DataSetPlotView view = (DataSetPlotView)getView(DataSetPlotView.ID, page);
							if (view!=null) view.setFile(file.getAbsolutePath(), monitor);
						} catch (Exception ne) {
							logger.error("Cannot open nexus", ne);
						}
					}
				});
			} catch (Exception ne) {
				throw new ExecutionException("Cannot find nexus view to open file "+file, ne);
			}

		} else { // Open in text view
			try {
				final AsciiTextView view = (AsciiTextView)getView(AsciiTextView.ID, page);
				if (view == null) return Boolean.FALSE;
				view.load(file);
				page.activate(view);
				view.setFocus();
			} catch (Exception ne) {
				throw new ExecutionException("Cannot find nexus view to open file "+file, ne);
			}

		}
		
		return Boolean.TRUE;
	}

	private IViewPart getView(String id, IWorkbenchPage page) {
		try {
			IViewPart part = page.findView(id);
			if (part!=null) return part;
			if (openView) {
				return page.showView(id);
			}
		} catch (Exception ne) {
			logger.error("Unable to getView for "+ id,ne);
		}
		return null;
	}

}
