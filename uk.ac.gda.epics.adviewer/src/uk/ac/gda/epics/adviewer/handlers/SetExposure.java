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
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.Activator;
import uk.ac.gda.epics.adviewer.Ids;


public class SetExposure extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String serviceName = event.getParameter(Ids.COMMAND_PARAMTER_ADCONTROLLER_SERVICE_NAME);
		Object namedService = Activator.getNamedService(ADController.class, serviceName);
		final ADController adController = (ADController) namedService;
		double acquireTime;
		try {
			acquireTime = adController.getAdBase().getAcquireTime_RBV();
		} catch (Exception e1) {
			throw new ExecutionException("Error getting current acquireTime", e1);
		}
		InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Set exposure time",
				"Enter a new value for the exposure time in secs:", Double.toString(acquireTime),
				new IInputValidator() {

					@Override
					public String isValid(String newText) {
						try {
							Double.valueOf(newText);
						} catch (Exception e) {
							return "Value is not recognised as a number '" + newText + "'";
						}
						return null;
					}

				});
		if (dlg.open() == Window.OK) {
			final String value = dlg.getValue();
			ProgressMonitorDialog pd = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
			try {
				pd.run(true /* fork */, true /* cancelable */, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						String title = "Setting exposure time to " + value;

						monitor.beginTask(title, 100);

						try {
							adController.setExposure(Double.valueOf(value));
						} catch (Exception e) {
							throw new InvocationTargetException(e, "Error in " + title);
						}
						monitor.done();
					}

				});
			} catch (Exception e) {
				throw new ExecutionException("Error setting acquireTime to " + value, e);
			}
		}
		return null;
	}

}
