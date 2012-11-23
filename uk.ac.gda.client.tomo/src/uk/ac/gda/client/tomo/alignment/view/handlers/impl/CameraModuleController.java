/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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
import gda.util.Sleep;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.python.core.PyBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.tomo.TomoClientConstants;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleMotorHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.IModuleLookupTableHandler;
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;

/**
 *
 */
public class CameraModuleController implements InitializingBean, ICameraModuleController {

	private final static Logger logger = LoggerFactory.getLogger(CameraModuleController.class);

	protected IModuleLookupTableHandler lookupTableHandler;

	private ICameraModuleMotorHandler cameraModuleMotorHandler;

	private IObservable tomoScriptController;

	public void setCameraModuleMotorHandler(ICameraModuleMotorHandler cameraModuleMotorHandler) {
		this.cameraModuleMotorHandler = cameraModuleMotorHandler;
	}

	/**
	 * @param lookupTableHandler
	 *            The lookupTableHandler to set.
	 */
	public void setLookupTableHandler(IModuleLookupTableHandler lookupTableHandler) {
		this.lookupTableHandler = lookupTableHandler;
	}

	@Override
	public void moveModuleTo(CAMERA_MODULE module, final IProgressMonitor monitor) throws Exception {
		final PyBaseException[] exceptions = new PyBaseException[1];
		final SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Module change", 5);
		String moveModuleCmd = String.format(TomoClientConstants.MOVE_MODULE_COMMAND, module.getValue().intValue());
		IObserver observer = new IObserver() {

			@Override
			public void update(Object source, Object arg) {
				if (source.equals(tomoScriptController)) {
					logger.debug("Observing source:{}", source);
					logger.debug("Observing arg:{}", arg);
					if (arg instanceof PyBaseException) {
						PyBaseException ex = (PyBaseException) arg;
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
			throw new IllegalStateException(exceptions[0].message.toString());
		}
	}

	@Override
	public CAMERA_MODULE getModule() throws DeviceException {
		final Integer[] moduleNumArr = new Integer[1];
		IObserver observer = new IObserver() {

			@Override
			public void update(Object source, Object arg) {
				if (source.equals(tomoScriptController)) {
					logger.debug("Observing source:{}", source);
					logger.debug("Observing arg:{}", arg);
					if (arg instanceof PyBaseException) {
						PyBaseException ex = (PyBaseException) arg;
						// exceptions[0] = ex;
					} else if (arg.toString().startsWith("Module:")) {
						String md = arg.toString().substring("Module:".length());
						moduleNumArr[0] = Integer.parseInt(md.trim());
					}
				}
			}
		};
		tomoScriptController.addIObserver(observer);
		JythonServerFacade.getInstance().evaluateCommand(TomoClientConstants.GET_MODULE_COMMAND);
		// if (exceptions[0] != null) {
		// throw new IllegalStateException(exceptions[0].message.toString());
		// }
		while (moduleNumArr[0] == null) {
			Sleep.sleep(100);
		}
		tomoScriptController.deleteIObserver(observer);

		return CAMERA_MODULE.getEnum(moduleNumArr[0]);
	}

	public IObservable getTomoScriptController() {
		return tomoScriptController;
	}

	public void setTomoScriptController(IObservable tomoScriptController) {
		this.tomoScriptController = tomoScriptController;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	// ---

	@Override
	public void stopModuleChange() throws DeviceException {
		// XXX - Maybe this is incorrect.
		JythonServerFacade.getInstance().panicStop();
	}

	@Override
	public String lookupHFOVUnit() throws DeviceException {
		return lookupTableHandler.lookupHFOVUnit();
	}

	@Override
	public Double lookupObjectPixelSize(CAMERA_MODULE module) throws DeviceException {
		return lookupTableHandler.lookupObjectPixelSize(module);
	}

	@Override
	public String lookupObjectPixelSizeUnits() throws DeviceException {
		return lookupTableHandler.lookupObjectPixelSizeUnits();
	}

	@Override
	public Double lookupHFOV(CAMERA_MODULE module) throws DeviceException {
		return lookupTableHandler.lookupHFOV(module);
	}

	@Override
	public Double lookupDefaultExposureTime(CAMERA_MODULE module) throws DeviceException {
		return lookupTableHandler.lookupDefaultExposureTime(module);
	}

	@Override
	public Double getObjectPixelSizeInMM(CAMERA_MODULE cameraModule) {
		return lookupTableHandler.getObjectPixelSizeInMM(cameraModule);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (lookupTableHandler == null) {
			throw new IllegalStateException("'lookupTableHandler' needs to be provided");
		}
	}

	@Override
	public String lookupMagnificationUnit() throws DeviceException {
		return lookupTableHandler.lookupMagnificationUnit();
	}

	@Override
	public Double lookupMagnification(CAMERA_MODULE module) throws DeviceException {
		return lookupTableHandler.lookupMagnification(module);
	}
}
