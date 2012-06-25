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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraMotionController;
import uk.ac.gda.client.tomo.alignment.view.handlers.IModuleLookupTableHandler;
import uk.ac.gda.client.tomo.alignment.view.handlers.IMotorHandler;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 *
 */
public class CameraModuleController implements InitializingBean, ICameraModuleController {

	private static final String MOVING_MODULE_MOTORS_TASKNAME_shortdesc = "Changing module";

	private final static Logger logger = LoggerFactory.getLogger(CameraModuleController.class);

	protected Double sampleTiltX = 0.0615;

	protected Double sampleTiltZ = 0.0;

	protected Double cameraSafeZ = -10.0;

	protected IMotorHandler motorHandler;

	protected IModuleLookupTableHandler lookupTableHandler;

	protected ICameraMotionController cameraMotionController;

	public void setCameraMotionController(ICameraMotionController cameraMotionController) {
		this.cameraMotionController = cameraMotionController;
	}

	/**
	 * @return Returns the sampleTiltX.
	 */
	public Double getSampleTiltX() {
		return sampleTiltX;
	}

	/**
	 * @param sampleTiltX
	 *            The sampleTiltX to set.
	 */
	public void setSampleTiltX(Double sampleTiltX) {
		this.sampleTiltX = sampleTiltX;
	}

	/**
	 * @return Returns the sampleTiltZ.
	 */
	public Double getSampleTiltZ() {
		return sampleTiltZ;
	}

	/**
	 * @param sampleTiltZ
	 *            The sampleTiltZ to set.
	 */
	public void setSampleTiltZ(Double sampleTiltZ) {
		this.sampleTiltZ = sampleTiltZ;
	}

	/**
	 * @param motorHandler
	 *            The motorHandler to set.
	 */
	public void setMotorHandler(IMotorHandler motorHandler) {
		this.motorHandler = motorHandler;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (motorHandler == null) {
			throw new IllegalArgumentException("'motorHandler' needs to be provided");
		}
		if (lookupTableHandler == null) {
			throw new IllegalArgumentException("'lookupTableHandler' needs to be provided");
		}
		if (cameraMotionController == null) {
			throw new IllegalArgumentException("'cameraMotionController' needs to be provided");
		}
	}

	@Override
	public void moveModuleTo(CAMERA_MODULE module, IProgressMonitor monitor) throws Exception {
		SubMonitor progress = SubMonitor.convert(monitor);

		motorHandler.aysncMoveSs1Rx(sampleTiltX);

		motorHandler.aysncMoveSs1Rz(sampleTiltZ);

		Double cam1xPosition = motorHandler.getCam1XPosition();

		double cam1xLookupVal = lookupTableHandler.lookupCam1X(module);
		double cam1zLookupVal = lookupTableHandler.lookupCam1Z(module);
		double cam1RollLookupVal = lookupTableHandler.lookupCam1Roll(module);
		double t3xLookupVal = cameraMotionController.getT3X(module);
		double t3m1yLookupVal = cameraMotionController.getT3M1y(module);

		double offset = Math.abs(cam1xPosition - cam1xLookupVal);
		monitor.beginTask(MOVING_MODULE_MOTORS_TASKNAME_shortdesc, 6);
		if (offset < 0.1) {
			logger.warn("already in module", module.getValue());
			progress.worked(3);
		} else {
			if (!monitor.isCanceled()) {
				motorHandler.moveCam1Z(progress.newChild(1), cameraSafeZ);
			}
			if (!monitor.isCanceled()) {
				motorHandler.moveCam1X(progress.newChild(1), cam1xLookupVal);
			}
			if (!monitor.isCanceled()) {
				motorHandler.moveCam1Z(progress.newChild(1), cam1zLookupVal);
			}
		}
		if (!monitor.isCanceled()) {
			motorHandler.moveCam1Roll(progress.newChild(1), cam1RollLookupVal);

			if (!monitor.isCanceled()) {
				motorHandler.moveT3XTo(progress.newChild(1), t3xLookupVal);

				if (!monitor.isCanceled()) {
					motorHandler.moveT3M1YTo(progress.newChild(1), t3m1yLookupVal);
				}
			}
		}
	}

	/**
	 * @param lookupTableHandler
	 *            The lookupTableHandler to set.
	 */
	public void setLookupTableHandler(IModuleLookupTableHandler lookupTableHandler) {
		this.lookupTableHandler = lookupTableHandler;
	}

	public void setCameraSafeZ(Double cameraSafeZ) {
		this.cameraSafeZ = cameraSafeZ;
	}

	@Override
	public CAMERA_MODULE getModule() throws DeviceException {
		Double cam1XPosition = motorHandler.getCam1XPosition();
		for (final CAMERA_MODULE module : CAMERA_MODULE.values()) {
			if (CAMERA_MODULE.NO_MODULE != module) {
				double cam1x = lookupTableHandler.lookupCam1X(module);
				if (Math.abs(cam1XPosition - cam1x) <= motorHandler.getCam1XTolerance()) {
					logger.info("Matched cam1x postion");
					return module;
				}
			}
		}
		return CAMERA_MODULE.NO_MODULE;
	}

	@Override
	public void stopModuleChange() throws DeviceException {
		motorHandler.stopMotors();
	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}
}
