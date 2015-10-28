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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.continuouscontroller.TrajectoryMoveController;
import gda.factory.FactoryException;

public class TrajectoryScannableMotor extends ScannableMotor implements ContinuouslyScannableViaController {

	private TrajectoryMoveController controller;

	private boolean operatingContinuousely;

	private int controllerMotorIndex;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		if (controller == null) {
			throw new FactoryException(getName() + " has no controller set.");
		}
	}

	// Continuous
	@Override
	public void setOperatingContinuously(boolean b) {
		operatingContinuousely = b;
	}

	@Override
	public boolean isOperatingContinously() {
		return operatingContinuousely;
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return controller;
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController controller) {
		this.controller = (TrajectoryMoveController) controller;
	}
	// Scannable //
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (isOperatingContinously()) {
			Double[] pointArray = new Double[controller.getNumberAxes()];
			pointArray[getControllerMotorIndex()] =
					PositionConvertorFunctions.toDouble(externalToInternal(position));
			controller.addPoint(pointArray);
		} else {
			super.asynchronousMoveTo(position);
		}
	}
	@Override
	public Object getPosition() throws DeviceException {
		if (isOperatingContinously()) {
			Double[] lastPointAdded = controller.getLastPointAdded();
			if (lastPointAdded == null) {
				// First point is in process of being added
				return super.getPosition();
			}
			Object[] pos = (Object[]) internalToExternal(new Double[]{lastPointAdded[controllerMotorIndex]});
			if (pos == null) {
				// First point is in process of being added
				return super.getPosition();
			}
			return pos[0];
		}
		return super.getPosition();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		if (isOperatingContinously()) {
			return controller.isMoving();
		}
		return super.isBusy();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		if (isOperatingContinously()) {
			controller.waitWhileMoving();
		} else {
			super.waitWhileBusy();
		}
	}
	/**
	 * stop all axes and turn off defer flag, and stopAndReset controller if moving continuously
	 */
	@Override
	public void stop() throws DeviceException {
		super.stop(); // stop all axes and turn off defer flag
		if (isOperatingContinously()) {
			try {
				controller.stopAndReset();
			} catch (InterruptedException e) {
				throw new DeviceException("InterruptedException while stopping and resetting " + controller.getName());
			}
		}
	}

	/**
	 * Identify which motor in the controller this Scannable is associated with. Indexed from 0.
	 * @param controllerMoterIndex
	 */
	public void setControllerMotorIndex(int controllerMoterIndex) {
		this.controllerMotorIndex = controllerMoterIndex;
	}

	public int getControllerMotorIndex() {
		return controllerMotorIndex;
	}


}
