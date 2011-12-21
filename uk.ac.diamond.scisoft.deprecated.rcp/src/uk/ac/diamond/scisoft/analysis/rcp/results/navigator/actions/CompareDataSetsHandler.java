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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.util.PlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.util.ComparisonPlotUtils;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.DataSetComparisionDialog;
import uk.ac.diamond.scisoft.analysis.rcp.views.nexus.DataSetPlotView;
import uk.ac.gda.common.rcp.util.EclipseUtils;


public class CompareDataSetsHandler extends AbstractHandler implements IHandler, IObjectActionDelegate {
	
	private static final Logger logger = LoggerFactory.getLogger(CompareDataSetsHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return doCompare();
	}

	@Override
	public void run(IAction action) {
		 doCompare();
	}
	
	private Object doCompare() {
		
		final IWorkbenchPage       page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		final IStructuredSelection sel  = (IStructuredSelection)page.getSelection();
		
		if (sel == null ||sel.toArray().length<=1) {
			MessageDialog.openConfirm(page.getActivePart().getSite().getShell(),
					                  "Please Select Files to Compare",
					                  "Please select more than one file by holding down 'Control' (or 'Ctrl') and choosing two or more files.");
			return Boolean.FALSE;
		}
		
		try {
			final DataSetPlotView    view        =  (DataSetPlotView)page.findView(DataSetPlotView.ID);
			final IWorkbenchPartSite dataSetSite = view!=null ? view.getSite() : page.getActivePart().getSite();
			final DataSetComparisionDialog d = new DataSetComparisionDialog(dataSetSite,
					                                                        sel.toArray());
			if (d.open() == IDialogConstants.CANCEL_ID) return Boolean.FALSE;
			
			final PlotWindow window  = getPlotWindow();
			IProgressService service = (IProgressService)PlatformUI.getWorkbench().getService(IProgressService.class);

			final PlotMode plotMode  = d.getDataSetPlotView().getPlotMode();
			
			// Changed to cancellable as sometimes loading the tree takes ages and you
			// did not mean such to choose the file.
			service.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Extracting and Plotting Comparison Data", 100);
			        try {
						ComparisonPlotUtils.createComparisionPlot(sel.toArray(), d.getSelections(), plotMode, window, monitor);
					} catch (Exception e) {
						logger.error("Cannot plot selected Data Sets", e);
					} 
					monitor.done();
				}
			});
			
			return Boolean.TRUE;
			
		} catch (Exception e) {
			logger.error("Cannot determine data sets from selected files", e);
			return Boolean.FALSE;
		}
	}

	
	protected PlotWindow getPlotWindow() {
		PlotView plotView;
		try {
			final IWorkbenchPage page = EclipseUtils.getActivePage();
			plotView = (PlotView)page.showView("uk.ac.diamond.scisoft.analysis.rcp.plotViewNT1", null, IWorkbenchPage.VIEW_ACTIVATE);
		} catch (PartInitException e) {
			logger.error("Cannot open uk.ac.diamond.scisoft.analysis.rcp.plotViewNT1", e);
			return null;
		}
		return plotView.getPlotWindow();
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

}
