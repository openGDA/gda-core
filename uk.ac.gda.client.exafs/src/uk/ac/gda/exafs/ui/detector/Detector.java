/*-
 * Copyright © 2013 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.exafs.ui.detector;

import gda.jython.InterfaceProvider;
import gda.jython.gui.JythonGuiConstants;
import gda.jython.scriptcontroller.ScriptExecutor;
import gda.jython.scriptcontroller.corba.impl.ScriptcontrollerAdapter;
import gda.observable.IObserver;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.plotting.jreality.util.PlotColorUtility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.exafs.ui.data.ScanObject;

public class Detector {
	private static final String EXAFS_SCRIPT_OBSERVER = "ExafsScriptObserver";// name of ScriptController that must be in the system for uploading to device to work
	private static final Logger logger = LoggerFactory.getLogger(DetectorEditor.class);
	protected SashFormPlotComposite sashPlotFormComposite;
	protected String serverCommand;
	private Action uploadAction;
	protected Counts counts;
	protected Plot plot;
	private IWorkbenchPartSite site;
	
	public Detector(String serverCommand, IWorkbenchPartSite site, Composite parent, String path) {
		counts = new Counts();
		this.site = site;
		this.serverCommand = serverCommand;
	}

	protected Action createUpLoadAction(final String path) {
		uploadAction = new Action("Configure") {
			@Override
			public void run() {
				try {
					upload(path);
				} catch (Exception ne) {
					logger.error("Cannot configure Detector", ne);
				}
			}
		};
		uploadAction.setText("Configure");
		uploadAction.setToolTipText("Applies the configuration settings to the detector.");
		return uploadAction;
	}
	
	protected void upload(final String path) throws Exception {
		IExperimentObject experimentObject = ExperimentFactory.getExperimentEditorManager().getSelectedScan();
		final IOutputParameters outputBean;
		if (experimentObject != null && experimentObject instanceof ScanObject)
			outputBean = ((ScanObject) experimentObject).getOutputParameters();
		else
			outputBean = null;
		final boolean ok = MessageDialog.openConfirm(site.getShell(),"Confirm Configure",
			"Are you sure you would like to permanently change the detector configuration?\n\n" + 
			"Please note, this will overwrite the detector configuration and ask the detector to reconfigure." + 
			"\n\n(A local copy of the file has been saved if you choose to cancel.)");
		if (!ok)
			return;

		IProgressService service = (IProgressService) site.getService(IProgressService.class);
		service.run(true, false, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Configure Detector", 100);
				try {
					Map<String, Serializable> data = new HashMap<String, Serializable>(1);
					data.put("XMLFileNameToLoad", path);
					data.put("OutputParametersToLoad", outputBean);
					monitor.worked(10);
					ScriptExecutor.Run(EXAFS_SCRIPT_OBSERVER, createObserver(), data, serverCommand + "(XMLFileNameToLoad,OutputParametersToLoad)", JythonGuiConstants.TERMINALNAME);
					monitor.worked(50);
					String configureResult = InterfaceProvider.getCommandRunner().evaluateCommand(serverCommand + ".getConfigureResult()");
					sashPlotFormComposite.appendStatus(configureResult, logger);
				} catch (Exception e) {
					logger.error("Internal error cannot get data from detector.", e);
				} finally {
					monitor.done();
				}
			}
		});
	}

	protected IObserver createObserver() {
		return new IObserver() {
			@Override
			public void update(Object theObserved, Object changeCode) {
				if (theObserved instanceof ScriptcontrollerAdapter)
					if (changeCode instanceof String)
						sashPlotFormComposite.appendStatus((String) changeCode, logger);
			}
		};
	}

	protected java.awt.Color getChannelColor(int i) {
		return PlotColorUtility.getDefaultColour(i);
	}

	public SashFormPlotComposite getSashPlotFormComposite() {
		return sashPlotFormComposite;
	}
	
}