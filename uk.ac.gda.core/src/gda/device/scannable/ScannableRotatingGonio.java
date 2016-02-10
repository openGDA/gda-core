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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.factory.FactoryException;
import gda.observable.IObserver;

import org.springframework.beans.factory.InitializingBean;

public class ScannableRotatingGonio extends ScannableMotionUnitsBase implements InitializingBean {
	private ScannableMotionUnits rotScannableMotor;
	private ScannableMotionUnits xScannableMotor;
	private ScannableMotionUnits yScannableMotor;
	private double xScannableScale = 1;
	private double xScannableOffset = 0;
	private double yScannableScale = 1;
	private double yScannableOffset = 0;
	private double rotScannableScale = 1;
	private double rotScannableOffset = 0;
	private boolean reportX = true;

	private IObserver motorObserver;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (rotScannableMotor == null)
			throw new Exception("rot is null");
		if (xScannableMotor == null)
			throw new Exception("x is null");
		if (yScannableMotor == null)
			throw new Exception("y is null");
	}

	@Override
	public void configure() throws FactoryException {
		try {
			motorObserver = new IObserver() {

				@Override
				public void update(Object source, Object arg) {
					notifyIObservers(this, arg);
				}
			};
			rotScannableMotor.addIObserver(motorObserver);
			xScannableMotor.addIObserver(motorObserver);
			yScannableMotor.addIObserver(motorObserver);
			unitsComponent.setHardwareUnitString(xScannableMotor.getUserUnits());
			super.configure();
		} catch (Exception e) {
			throw new FactoryException("Error configuring " + getName(), e);
		}
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		rotScannableMotor.waitWhileBusy();
		xScannableMotor.waitWhileBusy();
		yScannableMotor.waitWhileBusy();
	}

	private double getCurrentX() throws DeviceException {
		return (ScannableUtils.getCurrentPositionArray(xScannableMotor)[0] + xScannableOffset) * xScannableScale;
	}

	private double getCurrentY() throws DeviceException {
		return (ScannableUtils.getCurrentPositionArray(yScannableMotor)[0] + yScannableOffset) * yScannableScale;
	}

	private double getRot() throws DeviceException {
		return (ScannableUtils.getCurrentPositionArray(rotScannableMotor)[0] + rotScannableOffset) * rotScannableScale * Math.PI / 180;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		double xC = getCurrentX();
		double yC = getCurrentY();
		double rot = getRot();
		double sin_rot = Math.sin(rot);
		double cos_rot = Math.cos(rot);
		double x1, y1;
		if (reportX) {
			/**
			 * x1 is new required position, y1 = xCsin(rot) + yCcos(rot) - using current values of x and y xNew = x1cos(rot) + y1sin(rot) yNew = -x1sin(rot) +
			 * y1cos(rot)
			 */
			x1 = ScannableUtils.objectToArray(position)[0];
			y1 = xC * sin_rot + yC * cos_rot;

		} else {
			/**
			 * y1 is new required position, x1 = xCcos(rot) - yCsin(rot) - using current values of x and y xNew = x1cos(rot) + y1sin(rot) yNew = -x1sin(rot) +
			 * y1cos(rot)
			 */
			x1 = xC * cos_rot - yC * sin_rot;
			y1 = ScannableUtils.objectToArray(position)[0];
		}
		double xNew = x1 * cos_rot + y1 * sin_rot;
		double yNew = -x1 * sin_rot + y1 * cos_rot;
		double xNewScaled = xNew / xScannableScale - xScannableOffset;
		double yNewScaled = yNew / yScannableScale - yScannableOffset;
		xScannableMotor.asynchronousMoveTo(xNewScaled);
		yScannableMotor.asynchronousMoveTo(yNewScaled);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		// TODO Auto-generated method stub
		double x = getCurrentX();
		double y = getCurrentY();
		double rot = getRot();
		double sin_rot = Math.sin(rot);
		double cos_rot = Math.cos(rot);
		if (reportX) {
			/**
			 * x1 = xcos(rot) - ysin(rot)
			 */

			return x * cos_rot - y * sin_rot;
		}
		/**
		 * y1 = xsin(rot) + ycos(rot)
		 */

		return x * sin_rot + y * cos_rot;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		// TODO Auto-generated method stub
		return rotScannableMotor.isBusy() || xScannableMotor.isBusy() || yScannableMotor.isBusy();
	}

	public ScannableMotionUnits getRotScannableMotor() {
		return rotScannableMotor;
	}

	public void setRotScannableMotor(ScannableMotionUnits rotScannableMotor) {
		this.rotScannableMotor = rotScannableMotor;
	}

	public ScannableMotionUnits getxScannableMotor() {
		return xScannableMotor;
	}

	public void setxScannableMotor(ScannableMotionUnits xScannableMotor) {
		this.xScannableMotor = xScannableMotor;
	}

	public ScannableMotionUnits getyScannableMotor() {
		return yScannableMotor;
	}

	public void setyScannableMotor(ScannableMotionUnits yScannableMotor) {
		this.yScannableMotor = yScannableMotor;
	}

	public boolean isReportX() {
		return reportX;
	}

	public void setReportX(boolean reportX) {
		this.reportX = reportX;
	}

	public double getxScannableScale() {
		return xScannableScale;
	}

	public void setxScannableScale(double xScannableScale) {
		this.xScannableScale = xScannableScale;
	}

	public double getxScannableOffset() {
		return xScannableOffset;
	}

	public void setxScannableOffset(double xScannableOffset) {
		this.xScannableOffset = xScannableOffset;
	}

	public double getyScannableScale() {
		return yScannableScale;
	}

	public void setyScannableScale(double yScannableScale) {
		this.yScannableScale = yScannableScale;
	}

	public double getyScannableOffset() {
		return yScannableOffset;
	}

	public void setyScannableOffset(double yScannableOffset) {
		this.yScannableOffset = yScannableOffset;
	}

	public double getRotScannableScale() {
		return rotScannableScale;
	}

	public void setRotScannableScale(double rotScannableScale) {
		this.rotScannableScale = rotScannableScale;
	}

	public double getRotScannableOffset() {
		return rotScannableOffset;
	}

	public void setRotScannableOffset(double rotScannableOffset) {
		this.rotScannableOffset = rotScannableOffset;
	}
}
