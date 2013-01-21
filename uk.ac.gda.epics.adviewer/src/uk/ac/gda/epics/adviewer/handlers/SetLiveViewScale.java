/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.Activator;


public class SetLiveViewScale extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String serviceName = event.getParameter("uk.ac.gda.beamline.i13i.commandParameters.adcontrollerServiceName");
		Object namedService = Activator.getNamedService(ADController.class, serviceName);
		final ADController adController = (ADController) namedService;
		ProgressMonitorDialog pd = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
		try {
			pd.run(true /* fork */, true /* cancelable */, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					String title = "Setting scaling range to .05 - 0.95 of full range.";

					monitor.beginTask(title, 100);

					try {
						adController.setLiveViewRange(.05, .95);
					} catch (Exception e) {
						throw new InvocationTargetException(e, "Error in " + title);
					}
					monitor.done();
				}

			});
		} catch (Exception e) {
			throw new ExecutionException("Error setting live view range to 0.05 - 0.95 ", e);
		}
		return null;
	}

}
