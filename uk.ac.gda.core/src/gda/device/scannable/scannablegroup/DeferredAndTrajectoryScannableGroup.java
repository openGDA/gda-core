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

package gda.device.scannable.scannablegroup;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.continuouscontroller.TrajectoryMoveController;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableMotor;
import gda.factory.FactoryException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class DeferredAndTrajectoryScannableGroup extends DeferredScannableGroup implements
		ContinuouslyScannableViaController {

	private TrajectoryMoveController controller;

	private boolean operatingContinuousely;

	public DeferredAndTrajectoryScannableGroup() {

	}

	@Override
	public void setGroupMembers(List<Scannable> groupMembers) {
		for (Scannable scn : groupMembers) {
			if (!(scn instanceof ScannableMotor)) {
				throw new IllegalArgumentException("groupMembers must be ScannableMotors ");
			}
		}
		super.setGroupMembers(groupMembers);
	}
	
	public ArrayList<ScannableMotor> getScannableMotors() {
		ArrayList<ScannableMotor> members = new ArrayList<ScannableMotor>();
		for (Scannable wrappedScannable : getGroupMembers()) {
			Scannable scannableMotor = ((CoordinatedChildContinuousScannableMotionUnits) wrappedScannable).getPhysicalScannable();
			members.add((ScannableMotor) scannableMotor);
		}
		return members;
	}
	
	@Override
	public void configure() throws FactoryException {
		assertGroupMembersAllHaveOnlyOneInputField();
		if (controller == null) {
			throw new FactoryException(getName() + " has no controller set.");
		}
		assertControllerHasCorrectNumberOfAxes();
		super.configure();
	}

	@Override
	protected ICoordinatedChildScannable wrapScannable(Scannable delegate) {
		if (ScannableMotionUnits.class.isAssignableFrom(delegate.getClass())) {
			return new CoordinatedChildContinuousScannableMotionUnits(delegate, this);
		} else if (ScannableMotion.class.isAssignableFrom(delegate.getClass())) {
			return new CoordinatedChildContinuousScannableMotion(delegate, this);
		}
		// else it is at least Scannable
		return new CoordinatedChildContinuousScannable(delegate, this);
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

	public void setContinuousMoveController(ContinuousMoveController controller) {
		this.controller = (TrajectoryMoveController) controller;

	}

	// Scannable //
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (isOperatingContinously()) {
			Double[] posForScannableMotors = PositionConvertorFunctions
					.toDoubleArray(externalToInternal(parsePointFromPosition(position)));
			checkGroupMembersPositionValids(posForScannableMotors);
			Double[] posForUnderlyingMotors = positionForScannablesToUnderlyingMotors(posForScannableMotors);
			controller.addPoint(posForUnderlyingMotors);
		} else {
			super.asynchronousMoveTo(position);
		}
	}

	private void checkGroupMembersPositionValids(Double[] posForScannableMotors) throws DeviceException {
		ArrayList<ScannableMotor> scannableMotors = getScannableMotors();
		for (int i = 0; i < scannableMotors.size(); i++) {
			Double externalPosition = posForScannableMotors[i];
			if (externalPosition != null) {
				String report = scannableMotors.get(i).checkPositionValid(externalPosition);
				if (report != null) {
					throw new DeviceException(report);
				}
			}
		}
	}

	private Double[] positionForScannablesToUnderlyingMotors(Double[] posForScannableMotors) {
		Double[] posForUnderlyingMotors = new Double[posForScannableMotors.length];
		ArrayList<ScannableMotor> scannableMotors = getScannableMotors();
		for (int i = 0; i < scannableMotors.size(); i++) {
			Object motorPos = scannableMotors.get(i).externalToInternal(posForScannableMotors[i]);
			posForUnderlyingMotors[i] = PositionConvertorFunctions.toDouble(motorPos);
		}
		return posForUnderlyingMotors;
	}
	
	public Double[] positionForUnderlyingMotorsToScannable(Double[] posForUnderlyingMotors) {
		Double[] posForScannableMotors = new Double[posForUnderlyingMotors.length];
		ArrayList<ScannableMotor> scannableMotors = getScannableMotors();
		for (int i = 0; i < scannableMotors.size(); i++) {
			Object scannablePos = scannableMotors.get(i).internalToExternal(posForUnderlyingMotors[i]);
			posForScannableMotors[i] = PositionConvertorFunctions.toDouble(scannablePos);
		}
		return posForScannableMotors;
	}

	@Override
	public Object getPosition() throws DeviceException {
		if (isOperatingContinously()) {
			Object[] pos = (Object[]) internalToExternal(controller.getLastPointAdded());
			if (pos == null) {
				// First point is in process of being added
				return super.getPosition();
			}
			if (containsNull(pos)) {
				Object[] physical = PositionConvertorFunctions.toObjectArray(super.getPosition());
				for (int i = 0; i < pos.length; i++) {
					if (pos[i] == null) {
						pos[i] = physical[i];
					}
				}
			}
			return pos;
		}
		return super.getPosition();
	}

	private boolean containsNull(Object[] a) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getPositionWhileMovingContinuousely(ICoordinatedScannableGroupChildScannable childScannable)
			throws DeviceException {
		int index = getGroupMembers().indexOf(childScannable);
		Double[] pos = controller.getLastPointAdded();
		if (pos != null) {
			if (pos[index] != null) {
				return pos[index];
			}
		}
		// otherwise getPhysical position watching out for infinite recursion!
		return childScannable.getPhysicalScannable().getPosition();
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

	private Double[] parsePointFromPosition(Object position) throws DeviceException {
		Double[] targetPosition = gda.device.scannable.ScannableUtils.objectToArray(position);
		if (targetPosition.length != getNumberAxes()) {
			throw new DeviceException(MessageFormat.format(
					"Position does not have correct number of fields. Expected = {0} actual = {1} position= {2}",
					getNumberAxes(), targetPosition.length, position.toString()));
		}
		return targetPosition;
	}

	//
	private int getNumberAxes() {
		return getGroupMembers().size();
	}

	private void assertGroupMembersAllHaveOnlyOneInputField() throws FactoryException {
		for (Scannable scn : getGroupMembers()) {
			if ((scn.getInputNames().length != 1) || (scn.getExtraNames().length != 0)) {
				throw new FactoryException("DeferredAndTrajectoryScannableGroup " + getName()
						+ " must contain members with one input field and extra fields");
			}
		}
	}

	void assertControllerHasCorrectNumberOfAxes() throws FactoryException {
		if (getNumberAxes() != controller.getNumberAxes()) {
			throw new FactoryException(MessageFormat.format(
					"DeferredAndTrajectoryScannableGroup {0} must contain a controller with {1} not {2} axes",
					getName(), getNumberAxes(), controller.getNumberAxes()));
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
}

class CoordinatedChildContinuousScannable extends CoordinatedChildScannable implements
		ContinuouslyScannableViaController {

	public CoordinatedChildContinuousScannable(Scannable delegate, ICoordinatedParentScannable group) {
		super(delegate, group);
	}

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
		((ContinuouslyScannableViaController) group).setOperatingContinuously(b);
	}

	@Override
	public boolean isOperatingContinously() {
		return ((ContinuouslyScannableViaController) group).isOperatingContinously();
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return ((ContinuouslyScannableViaController) group).getContinuousMoveController();
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController controller) {
		throw new IllegalArgumentException("setContinuousMoveController("+controller.getName()+") not supported on "+this.getName());
	}

	@Override
	public Object getPosition() throws DeviceException {
		if (isOperatingContinously()) {
			return group.getPositionWhileMovingContinuousely(this);
		}
		return super.getPosition();
	}

}

class CoordinatedChildContinuousScannableMotion extends CoordinatedChildScannableMotion implements
		ContinuouslyScannableViaController {

	public CoordinatedChildContinuousScannableMotion(Scannable delegate, ICoordinatedParentScannable group) {
		super(delegate, group);
	}

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
		((ContinuouslyScannableViaController) group).setOperatingContinuously(b);
	}

	@Override
	public boolean isOperatingContinously() {
		return ((ContinuouslyScannableViaController) group).isOperatingContinously();
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return ((ContinuouslyScannableViaController) group).getContinuousMoveController();
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController controller) {
		throw new IllegalArgumentException("setContinuousMoveController("+controller.getName()+") not supported on "+this.getName());
	}

	@Override
	public Object getPosition() throws DeviceException {
		if (isOperatingContinously()) {
			return group.getPositionWhileMovingContinuousely(this);
		}
		return super.getPosition();
	}

}

class CoordinatedChildContinuousScannableMotionUnits extends CoordinatedChildScannableMotionUnits implements
		ContinuouslyScannableViaController {

	public CoordinatedChildContinuousScannableMotionUnits(Scannable delegate, ICoordinatedParentScannable group) {
		super(delegate, group);
	}

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
		((ContinuouslyScannableViaController) group).setOperatingContinuously(b);
	}

	@Override
	public boolean isOperatingContinously() {
		return ((ContinuouslyScannableViaController) group).isOperatingContinously();
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return ((ContinuouslyScannableViaController) group).getContinuousMoveController();
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController controller) {
		throw new IllegalArgumentException("setContinuousMoveController("+controller.getName()+") not supported on "+this.getName());
	}

	@Override
	public Object getPosition() throws DeviceException {
		if (isOperatingContinously()) {
			return group.getPositionWhileMovingContinuousely(this);
		}
		return super.getPosition();
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (group.isTargeting()) {
			group.setChildTarget(this, position);
		} else {
			delegate.asynchronousMoveTo(position);
		}
	}

}
