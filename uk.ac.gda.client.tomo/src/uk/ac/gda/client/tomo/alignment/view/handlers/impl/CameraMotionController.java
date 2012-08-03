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
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraMotionController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraMotionLookupTableHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.IMotorHandler;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 *
 */
public class CameraMotionController implements ICameraMotionController, InitializingBean {
	private ICameraMotionLookupTableHandler cameraMotionLookupTableHandler;

	private IMotorHandler motorHandler;

	private IObservable tomoScriptController;

	private static final Logger logger = LoggerFactory.getLogger(CameraMotionController.class);

	private static final String MOVE_T3_M1_Z_COMMAND = "tomographyScani13.moveT3M1ZTo(%d, %f)";

	public void setMotorHandler(IMotorHandler motorHandler) {
		this.motorHandler = motorHandler;
	}

	public void setCameraMotionLookupTableHandler(ICameraMotionLookupTableHandler cameraMotionLookupTableHandler) {
		this.cameraMotionLookupTableHandler = cameraMotionLookupTableHandler;
	}

	@Override
	public void moveT3m1ZTo(IProgressMonitor monitor, CAMERA_MODULE module, double t3m1ZValue) throws DeviceException,
			InterruptedException {
		final Exception[] exceptions = new Exception[1];
		final SubMonitor progress = SubMonitor.convert(monitor);
		progress.beginTask("Moving camera stage", 5);
		String moveModuleCmd = String.format(MOVE_T3_M1_Z_COMMAND, module.getValue().intValue(), t3m1ZValue);
		IObserver observer = new IObserver() {

			@Override
			public void update(Object source, Object arg) {
				if (source.equals(getTomoScriptController())) {
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
		getTomoScriptController().addIObserver(observer);
		JythonServerFacade.getInstance().evaluateCommand(moveModuleCmd);
		getTomoScriptController().deleteIObserver(observer);
		if (exceptions[0] != null) {
			throw new DeviceException(exceptions[0]);
		}
	}

	@Override
	public double getT3M1y(CAMERA_MODULE module) throws TimeoutException, CAException, InterruptedException, Exception {
		Double t3m1zPosition = motorHandler.getT3M1ZPosition();
		logger.debug("t3m1zPosition:{}", t3m1zPosition);

		double t3m1zOffset = motorHandler.getT3m1zOffset();
		logger.debug("t3m1zOffset:{}", t3m1zOffset);

		double t3m1ZToReadFromLookup = t3m1zPosition - t3m1zOffset;
		logger.debug("t3m1ZToReadFromLookup:{}", t3m1ZToReadFromLookup);

		double lookupT3m1y = cameraMotionLookupTableHandler.lookupT3M1Y(module, t3m1ZToReadFromLookup);
		logger.debug("lookupT3m1y:{}", lookupT3m1y);

		double t3m1yOffset = motorHandler.getT3m1yOffset();
		logger.debug("t3m1yOffset:{}", t3m1yOffset);

		logger.debug("lookupT3m1y + t3m1yOffset:{}", lookupT3m1y + t3m1yOffset);
		return lookupT3m1y + t3m1yOffset;
	}

	@Override
	public double getT3X(CAMERA_MODULE module) throws TimeoutException, CAException, InterruptedException, Exception {
		Double t3m1zPosition = motorHandler.getT3M1ZPosition();
		logger.debug("t3m1zPosition:{}", t3m1zPosition);

		double t3m1zOffset = motorHandler.getT3m1zOffset();
		logger.debug("t3m1zOffset:{}", t3m1zOffset);

		double t3m1ZToReadFromLookup = t3m1zPosition - t3m1zOffset;
		logger.debug("t3m1ZToReadFromLookup:{}", t3m1ZToReadFromLookup);

		double lookupT3X = cameraMotionLookupTableHandler.lookupT3X(module, t3m1ZToReadFromLookup);
		logger.debug("lookupT3X:{}", lookupT3X);

		double t3xOffset = motorHandler.getT3xOffset();
		logger.debug("t3xOffset:{}", t3xOffset);

		logger.debug("lookupT3X + t3xOffset:{}", lookupT3X + t3xOffset);

		return lookupT3X + t3xOffset;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (motorHandler == null) {
			throw new IllegalArgumentException("'motorHandler' needs to be provided");
		}
		if (cameraMotionLookupTableHandler == null) {
			throw new IllegalArgumentException("'cameraMotionLookupTableHandler' needs to be provided");
		}

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	public IObservable getTomoScriptController() {
		return tomoScriptController;
	}

	public void setTomoScriptController(IObservable tomoScriptController) {
		this.tomoScriptController = tomoScriptController;
	}

}
