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

import gda.device.ControlPoint;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.FactoryException;
import gda.factory.Finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeferredScannable extends ScannableMotionUnitsBase {
	private static final Logger logger = LoggerFactory.getLogger(DeferredScannable.class);
	Scannable controlPointScannable;
	public Scannable getControlPointScannable() {
		return controlPointScannable;
	}

	public void setControlPointScannable(Scannable controlPointScannable) {
		this.controlPointScannable = controlPointScannable;
	}

	ControlPoint deferredControlPoint;
	String deferredControlPointName;
	private String controlPointScannableName;

	/**
	 *
	 */
	public DeferredScannable() {

	}

	@Override
	public void configure() throws FactoryException {
		if (deferredControlPoint == null) {
			deferredControlPoint = (ControlPoint) Finder.getInstance().find(deferredControlPointName);
		}
		if (controlPointScannable == null) {
			controlPointScannable = (Scannable) Finder.getInstance().find(controlPointScannableName);
		}
		this.inputNames = new String[] { getName() };
		super.configure();
	}

	public String getControlPointScannableName() {
		return controlPointScannableName;
	}

	public void setControlPointScannableName(String controlPointScannableName) {
		this.controlPointScannableName = controlPointScannableName;
	}

	/**
	 * asynchronousMoveTo implemented following recommendations to check whether all moves have called back.
	 */
	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		setDefer(true);
		try {
			controlPointScannable.asynchronousMoveTo(position);
		} catch (Exception e) {
			logger.error("Exception while moving deferred scannable group, stopping all axes in group and setting defer flag off",
							e);
			stop();
			throw new DeviceException("Exception while triggering deferred scannable group move", e);
		}
		setDefer(false);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return controlPointScannable.getPosition();
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
			deferredControlPoint.setValue(1);
		} else {
			deferredControlPoint.setValue(0);
		}
	}


	/**
	 * stop all axes and turn off defer flag
	 */
	@Override
	public void stop() throws DeviceException {
		controlPointScannable.stop();
		setDefer(false);
	}
	@Override
	public boolean isBusy() throws DeviceException {
		return controlPointScannable.isBusy();
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
}
