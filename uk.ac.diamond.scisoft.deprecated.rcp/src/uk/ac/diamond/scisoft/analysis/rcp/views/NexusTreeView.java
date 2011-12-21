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

package uk.ac.diamond.scisoft.analysis.rcp.views;

import gda.analysis.io.ScanFileHolderException;
import gda.data.nexus.tree.INexusTree;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.nexus.NexusTreeExplorer;
import uk.ac.diamond.scisoft.analysis.rcp.util.NexusUtils;

public class NexusTreeView extends ViewPart {
	NexusTreeExplorer ntxp;
	Display display;
	FileDialog fileDialog = null;
	
	/**
	 * 
	 */
	public static final String ID = "uk.ac.diamond.scisoft.analysis.rcp.views.NexusTreeView"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(NexusTreeView.class);

	@Override
	public void createPartControl(Composite parent) {
		display = parent.getDisplay();
		ntxp = new NexusTreeExplorer(parent, SWT.NONE, getSite());
		
		// set up the help context
		PlatformUI.getWorkbench().getHelpSystem().setHelp(ntxp, "uk.ac.diamond.scisoft.analysis.rcp.nexusTreeView");

		createActions();

		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Load file given by path into view
	 * @param path
	 */
	public void loadTree(final String path) {
		IProgressService service = (IProgressService) getSite().getService(IProgressService.class);

		try {
			// Changed to cancellable as sometimes loading the tree takes ages and you
			// did not mean to choose the file.
			service.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						loadTree(path, monitor);
					} catch (ScanFileHolderException e) {
						logger.error("Could not load NeXus file: {}", e);
						
					} catch (Exception ee) {
						logger.error("Problem with NeXus loader: is the library path set correctly?", ee);
						
					} finally {
						monitor.done();
					}
				}


			});
		} catch (Exception e) {
			logger.error("Could not open NeXus file", e);
		}
	}

	public void loadTree(final String path, IProgressMonitor monitor) throws Exception {
		monitor.beginTask("Opening NeXus file " + path, 10);
		monitor.worked(1);
		if (monitor.isCanceled()) return;
		
		final INexusTree ntree = NexusUtils.loadTree(path, NexusUtils.getSel(), monitor);
		long start = System.nanoTime();
		if (ntree != null) {
			display.syncExec(new Runnable() {
				@Override
				public void run() {
					ntxp.setNexusTree(ntree);
					ntxp.setFilename(path);
					setPartName((new File(path)).getName());
				}
			});
		}
		logger.info("Setting tree took {}s", (System.nanoTime() - start)*1e-9);
	}

	/**
	 * call to bring up a file dialog
	 */
	public void loadTreeUsingFileDialog() {
		if (fileDialog == null) {
			fileDialog = new FileDialog(getSite().getShell(), SWT.OPEN);
		}

		String [] filterNames = new String [] {"NeXus files", "All Files (*)"};
		String [] filterExtensions = new String [] {"*.nxs;*.h5", "*"};
		fileDialog.setFilterNames(filterNames);
		fileDialog.setFilterExtensions(filterExtensions);
		final String path = fileDialog.open();

		if (path != null) {
			loadTree(path);
		}
	}
	

	@Override
	public void setFocus() {
		ntxp.setFocus();
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
//		IToolBarManager tbManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		@SuppressWarnings("unused")
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		@SuppressWarnings("unused")
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	public void expandAll() {
		ntxp.expandAll();
	}


}
