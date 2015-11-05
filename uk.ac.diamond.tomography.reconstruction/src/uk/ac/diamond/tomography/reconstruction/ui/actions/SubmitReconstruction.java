/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction.ui.actions;

import java.net.URI;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.commandserver.core.ActiveMQServiceHolder;
import org.dawnsci.commandserver.tomo.beans.TomoBean;
import org.dawnsci.commandserver.ui.view.StatusQueueView;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubmitter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Example class for an action to submit a reconstruction.
 *
 * This action submits a file to be reconstructed using the ActiveMQ server.
 */
public class SubmitReconstruction implements IWorkbenchWindowActionDelegate {

    private Logger logger = LoggerFactory.getLogger(SubmitReconstruction.class);

	private IResource resource;

	@Override
	public void run(IAction action) {

		if (resource==null) return;

		// TODO Probably want a wizard here - see RerunWizard used for Xia2 reruns.
		final boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
				             "Confirm Test Reconstruction",
				             "Would you like to run a test reconstruction on '"+resource.getName()+"'?");

	    if (!ok) return;

        try {
    	    final TomoBean tBean = new TomoBean();
    	    tBean.setProjectName("A test tomography reconstruction");
    	    tBean.setFileName(resource.getLocation().toOSString());
    	    tBean.setRunDirectory("/dls/p45/test/");
    	    tBean.setName("Test Recon "+resource.getName());

    		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.commandserver.ui");
    		final String uri        = store.getString("org.dawnsci.commandserver.URI");
    		final String queueName  = "scisoft.tomo.SUBMISSION_QUEUE";

			final IEventService service = ActiveMQServiceHolder.getEventService();
			final ISubmitter<TomoBean> factory = service.createSubmitter(new URI(uri), queueName);

    		factory.submit(tBean, true);

    		final String secondId = StatusQueueView.createSecondaryId("org.dawnsci.commandserver.tomo", TomoBean.class.getName(), "scisoft.tomo.STATUS_QUEUE", "scisoft.tomo.STATUS_TOPIC", "scisoft.tomo.SUBMISSION_QUEUE");
		    EclipseUtils.getPage().showView(StatusQueueView.ID, secondId, IWorkbenchPage.VIEW_VISIBLE);

        } catch (Exception e) {

        	ErrorDialog.openError(Display.getDefault().getActiveShell(), "Cannot submit reconstruction", "Please contact your support representative.",
        			  new Status(IStatus.ERROR, "uk.ac.diamond.tomography.reconstruction", e.getMessage()));

        	logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection)selection;
			Object f = sel.getFirstElement();
			if (f instanceof IResource) {
				resource = (IResource)f;
			}
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

}
