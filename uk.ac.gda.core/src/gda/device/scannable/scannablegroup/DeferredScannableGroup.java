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
	private String deferredControlPointName;

	private int deferOnValue = 1;

	private boolean logDefFlagChangesAsInfo = false;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (deferredControlPoint == null) {
			deferredControlPoint = Finder.find(deferredControlPointName);
		}
		super.configure();
		setConfigured(true);
	}

	/**
	 * asynchronousMoveTo implemented following recommendations to check whether all moves have called back.
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		setDefer(true);
		try {
			super.asynchronousMoveTo(position);
		} catch (Exception e) {
			logger.error("Exception while moving deferred scannable group, stopping all axes in group and setting defer flag off");
			stop();
			throw new DeviceException("Exception while triggering deferred scannable group move:\n " + e.getMessage(), e);
		}
		setDefer(false);
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
		if (deferFlag) {
			logger.info("{}: defer ON", getName());
			deferredControlPoint.setValue(deferOnValue);
		} else {
			logger.info("{}: defer OFF", getName());
			deferredControlPoint.setValue(0);
		}
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
