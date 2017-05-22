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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.scan.IScanDataPoint;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.python.core.PyBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentCommands;
import uk.ac.gda.client.tomo.alignment.view.handlers.IAutofocusController;

public class AutoFocusController implements IAutofocusController {

	private static final Logger logger = LoggerFactory.getLogger(AutoFocusController.class);

	private IObservable tomoScriptController;

	public void setTomoScriptController(IObservable tomoScriptController) {
		this.tomoScriptController = tomoScriptController;
	}

	@Override
	public String doAutoFocus(IProgressMonitor monitor, double acquireTime) throws InterruptedException {
		final PyBaseException[] exceptions = new PyBaseException[1];
		final SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("", 30);
		try {
			String autofocusCmd = String.format(TomoAlignmentCommands.AUTO_FOCUS, acquireTime);
			AutofocusObserver autofocusObserver = new AutofocusObserver(progress);

			tomoScriptController.addIObserver(autofocusObserver);
			InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(autofocusObserver);
			//
			JythonServerFacade.getInstance().evaluateCommand(autofocusCmd);
			//
			int tries = 0;
			while (autofocusObserver.getResult() == null && tries < 20) {
				Thread.sleep(100);
				tries++;
			}

			InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(autofocusObserver);
			tomoScriptController.deleteIObserver(autofocusObserver);

			logger.debug("Autofocus complete:{}", autofocusObserver.getResult());
			return autofocusObserver.getResult();
			// FIXME - Should move cam1_z to found position

			// FIXME - Return value to UI
		} finally {
			progress.done();
			monitor.done();
		}
	}

	private class AutofocusObserver implements IObserver, IScanDataPointObserver {

		private final IProgressMonitor progress;

		private String result;

		public AutofocusObserver(IProgressMonitor monitor) {
			this.progress = monitor;
		}

		public String getResult() {
			return result;
		}

		@Override
		public void update(Object source, Object arg) {
			if (source.equals(tomoScriptController)) {
				logger.debug("Observing source:{}", source);
				logger.debug("Observing arg:{}", arg);
				if (arg instanceof PyBaseException) {
					PyBaseException ex = (PyBaseException) arg;
					logger.debug("Exception from scripts:{}", ex);
					// exceptions[0] = ex;
				} else if (arg instanceof IScanDataPoint) {
					final IScanDataPoint scanDataPoint = (IScanDataPoint) arg;
					double currentPoint = scanDataPoint.getCurrentPointNumber();
					double numberOfPoints = scanDataPoint.getNumberOfPoints();
					double progressOfTask = currentPoint / numberOfPoints * 100;
					progress.subTask("Scan: " + progressOfTask);
				} else {
					if (arg instanceof String) {
						String string = arg.toString();
						if (string.startsWith("Complete:")) {
							result = string.substring("Complete:".length());
						}
					}
					progress.subTask(arg.toString());
					progress.worked(1);
				}
			}

		}

	}
}
