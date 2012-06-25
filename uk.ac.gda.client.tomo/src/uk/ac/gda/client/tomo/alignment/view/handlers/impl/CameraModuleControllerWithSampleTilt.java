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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleController;
import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;

/**
 * This implementation of the {@link ICameraModuleController} is so that the tomography alignment GUI can use the sample
 * stage tilt instead of the camera stage tilt.
 */
public class CameraModuleControllerWithSampleTilt extends CameraModuleController {

	private final static Logger logger = LoggerFactory.getLogger(CameraModuleControllerWithSampleTilt.class);

	@Override
	public void moveModuleTo(CAMERA_MODULE module, IProgressMonitor monitor) throws Exception {

		SubMonitor progress = SubMonitor.convert(monitor);

		motorHandler.aysncMoveSs1Rx(sampleTiltX);

		motorHandler.aysncMoveSs1Rz(sampleTiltZ);

		Double cam1xPosition = motorHandler.getCam1XPosition();

		double cam1xLookupVal = lookupTableHandler.lookupCam1X(module);
		double cam1zLookupVal = lookupTableHandler.lookupCam1Z(module);
		double ss1RzLookupVal = lookupTableHandler.lookupSs1Rz(module);
		double t3xLookupVal = cameraMotionController.getT3X(module);
		double t3m1yLookupVal = cameraMotionController.getT3M1y(module);

		double offset = Math.abs(cam1xPosition - cam1xLookupVal);
		monitor.beginTask("", 6);
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
			while (motorHandler.isSs1RzBusy()) {
				monitor.setTaskName(String.format("Waiting for %1$s to complete motion", motorHandler.getSs1RzName()));
				Thread.sleep(100);
			}
			while (motorHandler.isSs1RxBusy()) {
				monitor.setTaskName(String.format("Waiting for %1$s to complete motion", motorHandler.getSs1RxName()));
				Thread.sleep(100);
			}
			motorHandler.moveSs1Rz(progress.newChild(1), ss1RzLookupVal);
		}
		monitor.setTaskName("");
		if (!monitor.isCanceled()) {
			motorHandler.moveT3XTo(progress.newChild(1), t3xLookupVal);
		}

		if (!monitor.isCanceled()) {
			motorHandler.moveT3M1YTo(progress.newChild(1), t3m1yLookupVal);
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
}
