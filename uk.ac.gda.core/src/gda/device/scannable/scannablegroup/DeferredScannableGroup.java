/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ControlPoint;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableMotor;
import gda.factory.FactoryException;
import gda.factory.Finder;

/**
 * Controls a collection of Motors so that they moved via the Epics deferred move mechanism.
 * <p>
 * WARNING: Extending CoordinatedScannableGroup, the individual axes will be wrapped in ICoordinatedElementScannables
 * which will hide any methods which are not in the Scannable, ScannableMotion or ScannableMotionUnits interface.
 */
public class DeferredScannableGroup extends CoordinatedScannableGroup {
	private static final Logger logger = LoggerFactory.getLogger(DeferredScannableGroup.class);

	private ControlPoint deferredControlPoint;

	String deferredControlPointName;

	private ControlPoint numberToMoveControlPoint;

	private ControlPoint checkStartControlPoint;

	private int deferOnValue = 1;

	private boolean logDefFlagChangesAsInfo = false;

	/**
	 *
	 */
	public DeferredScannableGroup() {

	}

	@Override
	public void configure() throws FactoryException {
		if (deferredControlPoint == null) {
			deferredControlPoint = (ControlPoint) Finder.getInstance().find(deferredControlPointName);
		}
		super.configure();
	}

	/**
	 * asynchronousMoveTo implemented following recommendations to check whether all moves have called back.
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		ControlPoint moveCp = getNumberToMoveControlPoint();
		ControlPoint startCp = getCheckStartControlPoint();
		boolean validate = false; // perform validation steps using numberToMove and checkStart control points
		if (moveCp != null || startCp != null) {
			if (startCp == null || moveCp == null) {
				throw new DeviceException(
						"Require neither or both numberToMoveControlPoint and checkStartControlPoint to be set");
			}
			validate = true;
		}
		int attemptsRemaining = validate ? 60 : 1;

		while (attemptsRemaining-- > 0) {
			Double[] current = PositionConvertorFunctions.toDoubleArray(getPosition());
			Double[] target = PositionConvertorFunctions.toDoubleArray(position);

			for (int i = 0; i < target.length; i++) {
				if (target[i] == null) {
					continue;
				}
				Scannable scn = ((CoordinatedChildScannable) getGroupMembers().get(i)).delegate;
				double tolerance = scn instanceof ScannableMotor ?
						((ScannableMotor) scn).getDemandPositionTolerance() : 0;
				if (Math.abs(current[i] - target[i]) < tolerance) {
					target[i] = null;
				}
			}

			if (isLogDefFlagChangesAsInfo()) {
				logger.info("[[[" +getName() + ": defer ON");
			}
			setDefer(true);
			if (validate) {
				int toMove = testGroupMove(target);
				logger.info("Telling defer move system that we will move " + toMove + " axes");
				getNumberToMoveControlPoint().setValue(toMove);
			}
			// sometimes a move request can fail due to axis vibration
			// this is *very rare*, but can waste a night of beamtime
			// we handle move request failure and try again a limited number of times
			int moveRequestsRemaining = 10;
			while (moveRequestsRemaining-- > 0) {
				try {
					super.asynchronousMoveTo(target);
					break;
				} catch (Exception e) {
					// A move request can fail if the PID loop gets stuck trying to correct an axis
					// Commence horrible hack to handle axis vibration
					if (moveRequestsRemaining > 0) {
						logger.info("Move request failed, retrying. " + moveRequestsRemaining + " retries left");
						stop();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							throw new DeviceException(e1);
						}
						continue;
					}
					logger.error("Move request failed after all retry attempts; stopping axes and setting defer flag off");
					stop();
					throw new DeviceException("Exception while triggering deferred scannable group move", e);
				}
			}
			if (isLogDefFlagChangesAsInfo()) {
				logger.info("]]]" + getName() + ": defer OFF");
			}
			setDefer(false);
			if (validate) {
				double value = getCheckStartControlPoint().getValue();
				boolean succeeded = Math.abs(value) < 1;
				if (succeeded) {
					break;
				}
				// attempt failed
				if (attemptsRemaining > 0) {
					logger.warn("Deferred move system failed; trying again after 1 second, " +
							attemptsRemaining + " attempts remaining");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						throw new DeviceException(e);
					}
				} else {
					throw new DeviceException("Deferred move system failed");
				}
			}
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		stop();
	}

	/**
	 * @param deferFlag
	 * @throws DeviceException
	 */
	public void setDefer(boolean deferFlag) throws DeviceException {
		if (deferFlag == true) {
			deferredControlPoint.setValue(deferOnValue);
		} else {
			deferredControlPoint.setValue(0);
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		return super.getPosition();
	}

	/**
	 * stop all axes and turn off defer flag
	 */
	@Override
	public void stop() throws DeviceException {
		super.stop(); // stops all scannables
		setDefer(false);
	}

	/**
	 * @return defer on
	 * @throws DeviceException
	 */
	public boolean getDefer() throws DeviceException {
		return (((Double) deferredControlPoint.getPosition()) == 1);
	}

	/**
	 * @return control point
	 */
	public ControlPoint getDeferredControlPoint() {
		return deferredControlPoint;
	}

	/**
	 * @param deferredControlPoint
	 */
	public void setDeferredControlPoint(ControlPoint deferredControlPoint) {
		this.deferredControlPoint = deferredControlPoint;
	}

	/**
	 * @return control point name
	 */
	public String getDeferredControlPointName() {
		return deferredControlPointName;
	}

	/**
	 * @param deferredControlPointName
	 */
	public void setDeferredControlPointName(String deferredControlPointName) {
		this.deferredControlPointName = deferredControlPointName;
	}

	public ControlPoint getNumberToMoveControlPoint() {
		return numberToMoveControlPoint;
	}

	public void setNumberToMoveControlPoint(ControlPoint numberToMoveControlPoint) {
		this.numberToMoveControlPoint = numberToMoveControlPoint;
	}

	public ControlPoint getCheckStartControlPoint() {
		return checkStartControlPoint;
	}

	public void setCheckStartControlPoint(ControlPoint checkStartControlPoint) {
		this.checkStartControlPoint = checkStartControlPoint;
	}

	public boolean isLogDefFlagChangesAsInfo() {
		return logDefFlagChangesAsInfo;
	}

	public void setLogDefFlagChangesAsInfo(boolean logDefFlagChangesAsInfo) {
		this.logDefFlagChangesAsInfo = logDefFlagChangesAsInfo;
	}

	public void setDeferOnValue(int deferOnValue) {
		this.deferOnValue = deferOnValue;
	}

	public int getDeferOnValue() {
		return deferOnValue;
	}
}
