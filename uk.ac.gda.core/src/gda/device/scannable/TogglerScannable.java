/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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
import gda.device.Scannable;

/**
 *
 */
public class TogglerScannable extends ScannableBase {

	private Scannable delegate;
	
	public enum Hook {
		AT_SCAN,
		AT_LINE,
		AT_POINT,
		AT_LEVEL
	}

	private Hook hook;

	private Object startValue;
	
	private Object initialValue;

	private Object stopValue = null;

	private Object endValue = null;

	public TogglerScannable(Scannable delegate) {
		this.delegate = delegate;
	}

	public void setHook(Hook hook) {
		this.hook = hook;
	}
	public Hook getHook() {
		return hook;
	}

	public void setStartValue(Object startValue) {
		this.startValue = startValue;
	}
	public Object getStartValue() {
		return startValue;
	}

	/**
	 * Sets the value the toggled scannable should move to at the hooked end-event.
	 * <p>
	 * Leave null to restore to the value before the hooked start-event.
	 * @param endValue
	 */
	public void setEndValue(Object endValue) {
		this.endValue = endValue;
	}
	public Object getEndValue() {
		return endValue;
	}	

	/**
	 * Sets the value the toggled scannable should move to when stop() or atCommandFailure() are called.
	 * <p>
	 * Leave null to use the implementation used by the hooked end-event.
	 * @param stopValue
	 */
	public void setStopValue(Object stopValue) {
		this.stopValue = stopValue;
	}
	public Object getStopValue() {
		return stopValue;
	}

	public void togglePositionStart() throws DeviceException {
		initialValue = delegate.getPosition();
		delegate.moveTo(startValue);
	}

	public void togglePositionEnd() throws DeviceException {
		delegate.moveTo(endValue == null ? initialValue : endValue);
	}

	public void togglePositionStop() throws DeviceException {
		if (stopValue == null) {
			togglePositionEnd();
		} else {
			delegate.moveTo(stopValue);
		}
	}

	@Override
	public void atStart() throws DeviceException {
		atScanStart();
	}

	@Override
	public void atEnd() throws DeviceException {
		atScanEnd();
	}

	@Override
	public void atScanStart() throws DeviceException {
		if (hook == Hook.AT_SCAN) {
			togglePositionStart();
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		if (hook == Hook.AT_SCAN) {
			togglePositionEnd();
		}
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		if (hook == Hook.AT_LINE) {
			togglePositionStart();
		}
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		if (hook == Hook.AT_LINE) {
			togglePositionEnd();
		}
	}

	@Override
	public void atPointStart() throws DeviceException {
		if (hook == Hook.AT_POINT) {
			togglePositionStart();
		}
	}

	@Override
	public void atPointEnd() throws DeviceException {
		if (hook == Hook.AT_POINT) {
			togglePositionEnd();
		}
	}

	@Override
	public void atLevelStart() throws DeviceException {
		if (hook == Hook.AT_LEVEL) {
			togglePositionStart();
		}
	}

	@Override
	public void atLevelEnd() throws DeviceException {
		if (hook == Hook.AT_LEVEL) {
			togglePositionEnd();
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		togglePositionEnd();
	}

	@Override
	public void stop() throws DeviceException {
		togglePositionEnd();
	}

	@Override
	public void asynchronousMoveTo(Object target) throws DeviceException {
		throw new DeviceException("Unsupported operation. Use underlying scannable directly if necessary (getDelegate()).");
	}
	
	@Override
	public void moveTo(Object target) throws DeviceException {
		throw new DeviceException("Unsupported operation. Use underlying scannable directly if necessary (getDelegate()).");
	}
	
	@Override
	public String[] getInputNames() {
		return new String[] {};
	}
	
	@Override
	public String[] getExtraNames() {
		return new String[] {};
	}

	@Override
	public String[] getOutputFormat() {
		return new String[] {};
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return delegate.isBusy();
	}

	@Override
	public String toFormattedString() {
		return delegate.toFormattedString();
	}

	@Override
	public Object getPosition() throws DeviceException {
		//return delegate.getPosition();
		return null;
	}

}
