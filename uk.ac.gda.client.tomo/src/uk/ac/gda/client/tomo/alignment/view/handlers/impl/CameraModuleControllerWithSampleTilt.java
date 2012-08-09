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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.device.DeviceException;
import gda.jython.JythonServerFacade;
import gda.observable.IObservable;
import gda.observable.IObserver;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.TomoClientConstants;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleController;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 * This implementation of the {@link ICameraModuleController} is so that the tomography alignment GUI can use the sample
 * stage tilt instead of the camera stage tilt.
 */
public class CameraModuleControllerWithSampleTilt extends CameraModuleController {

	private final static Logger logger = LoggerFactory.getLogger(CameraModuleControllerWithSampleTilt.class);

	private IObservable tomoScriptController;

	@Override
	public void moveModuleTo(CAMERA_MODULE module, final IProgressMonitor monitor) throws Exception {
		final Exception[] exceptions = new Exception[1];
		final SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Module change", 5);
		String moveModuleCmd = String.format(TomoClientConstants.MOVE_MODULE_COMMAND, module.getValue().intValue());
		IObserver observer = new IObserver() {

			@Override
			public void update(Object source, Object arg) {
				if (source.equals(tomoScriptController)) {
					logger.debug("Observing source:{}", source);
					logger.debug("Observing arg:{}", arg);
					if (arg instanceof Exception) {
						Exception ex = (Exception) arg;
						exceptions[0] = ex;
					} else {
						progress.subTask(arg.toString());
					}
				}
			}
		};
		tomoScriptController.addIObserver(observer);
		JythonServerFacade.getInstance().evaluateCommand(moveModuleCmd);
		tomoScriptController.deleteIObserver(observer);
		if (exceptions[0] != null) {
			throw exceptions[0];
		}
	}

	@Override
	public CAMERA_MODULE getModule() throws DeviceException {
		Double cam1XPosition = motorHandler.getCam1XPosition();
		// these two are constant set in the script - for some reason unknown to RS and FY these motor need to be in
		// these positions.
		for (final CAMERA_MODULE module : CAMERA_MODULE.values()) {
			if (CAMERA_MODULE.NO_MODULE != module) {
				double cam1x = lookupTableHandler.lookupCam1X(module);
				if (Math.abs(cam1XPosition - cam1x) <= motorHandler.getCam1XTolerance()) {
					logger.info("Matched cam1x postion");
					return module;
					// try {
					// moveModuleTo(module, null);
					// } catch (Exception e) {
					// logger.error("Problem moving motor to module:" + module.getValue(), e);
					// throw new DeviceException(e);
					//
					// }
				}

			}
		}
		return CAMERA_MODULE.NO_MODULE;
	}

	public IObservable getTomoScriptController() {
		return tomoScriptController;
	}

	public void setTomoScriptController(IObservable tomoScriptController) {
		this.tomoScriptController = tomoScriptController;
	}
}
