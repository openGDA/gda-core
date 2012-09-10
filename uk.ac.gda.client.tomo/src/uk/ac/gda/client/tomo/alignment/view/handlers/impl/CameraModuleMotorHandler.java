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
import gda.device.IScannableMotor;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraModuleMotorHandler;

public class CameraModuleMotorHandler extends BaseMotorHandler implements ICameraModuleMotorHandler {
	
	private IScannableMotor cam1ZScannable;
	private IScannableMotor cam1XScannable;
	private IScannableMotor cam1RollScannable;
	
	/**
	 * @param cam1ZScannable
	 *            The cam1ZScannable to set.
	 */
	public void setCam1ZScannable(IScannableMotor cam1ZScannable) {
		this.cam1ZScannable = cam1ZScannable;
	}

	public void setCam1XScannable(IScannableMotor cam1xScannable) {
		cam1XScannable = cam1xScannable;
	}

	public void setCam1RollScannable(IScannableMotor cam1RollScannable) {
		this.cam1RollScannable = cam1RollScannable;
	}

	@Override
	public double getCam1XPosition() throws DeviceException {
		return (Double) cam1XScannable.getPosition();
	}

	@Override
	public double getCam1ZPosition() throws DeviceException {
		return (Double) cam1ZScannable.getPosition();
	}

	@Override
	public double getCam1RollPosition() throws DeviceException {
		return (Double) cam1RollScannable.getPosition();
	}
	@Override
	public double getCam1XTolerance() {
		return cam1XScannable.getDemandPositionTolerance();
	}

	@Override
	public double getCam1RollTolerance() {
		return cam1RollScannable.getDemandPositionTolerance();
	}

	@Override
	public double getCam1ZTolerance() {
		return cam1ZScannable.getDemandPositionTolerance();
	}
	@Override
	public void moveCam1Roll(IProgressMonitor monitor, double cam1RollPos) throws DeviceException, InterruptedException {
		moveMotor(monitor, cam1RollScannable, cam1RollPos);
	}

	@Override
	public void moveCam1X(IProgressMonitor monitor, double cam1xPos) throws DeviceException, InterruptedException {
		moveMotor(monitor, cam1XScannable, cam1xPos);
	}

	@Override
	public void moveCam1Z(IProgressMonitor monitor, Double cam1zPos) throws DeviceException, InterruptedException {
		moveMotor(monitor, cam1ZScannable, cam1zPos);
	}

}
