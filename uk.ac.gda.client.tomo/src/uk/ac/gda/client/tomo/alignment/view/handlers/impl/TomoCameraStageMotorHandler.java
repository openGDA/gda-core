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

import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraStageMotorHandler;

public class TomoCameraStageMotorHandler extends BaseMotorHandler implements ICameraStageMotorHandler {

	private IScannableMotor t3xScannable;

	private IScannableMotor t3m1zScannable;

	private IScannableMotor t3m1yScannable;

	@Override
	public Double getT3XPosition() throws DeviceException {
		return (Double) t3xScannable.getPosition();
	}

	@Override
	public Double getT3M1ZPosition() throws DeviceException {
		return (Double) t3m1zScannable.getPosition();
	}

	@Override
	public Double getT3M1YPosition() throws DeviceException {
		return (Double) t3m1yScannable.getPosition();
	}

	public void setT3xScannable(IScannableMotor t3xScannable) {
		this.t3xScannable = t3xScannable;
	}

	public void setT3m1zScannable(IScannableMotor t3m1zScannable) {
		this.t3m1zScannable = t3m1zScannable;
	}

	public void setT3m1yScannable(IScannableMotor t3m1yScannable) {
		this.t3m1yScannable = t3m1yScannable;
	}

	@Override
	public double getT3M1YTolerance() {
		return t3m1yScannable.getDemandPositionTolerance();
	}

	@Override
	public double getT3XTolerance() {
		return t3xScannable.getDemandPositionTolerance();
	}

	@Override
	public void moveT3XTo(IProgressMonitor monitor, Double t3xMoveToPosition) throws DeviceException,
			InterruptedException {
		moveMotor(monitor, t3xScannable, t3xMoveToPosition);
	}

	@Override
	public void moveT3M1YTo(IProgressMonitor monitor, Double t3m1yMoveToPosition) throws DeviceException,
			InterruptedException {
		moveMotor(monitor, t3m1yScannable, t3m1yMoveToPosition);
	}

	@Override
	public void moveT3M1ZTo(IProgressMonitor monitor, double t3m1zValue) throws DeviceException, InterruptedException {
		moveMotor(monitor, t3m1zScannable, t3m1zValue);
	}

	@Override
	public double getT3m1yOffset() throws DeviceException {
		return t3m1yScannable.getUserOffset();
	}

	@Override
	public double getT3m1zOffset() throws DeviceException {
		return t3m1zScannable.getUserOffset();
	}

	@Override
	public double getT3xOffset() throws DeviceException {
		return t3xScannable.getUserOffset();
	}

	@Override
	public String getT3XMotorName() {
		return t3xScannable.getName();
	}

	@Override
	public String getT3m1ZMotorName() {
		return t3m1zScannable.getName();
	}

	@Override
	public String getT3m1YMotorName() {
		return t3m1yScannable.getName();
	}

	@Override
	public String getCameraStageZMotorName() {
		return t3m1zScannable.getName();
	}

}
