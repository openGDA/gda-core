/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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
import gda.exafs.xes.XesUtils;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.observable.IObserver;

/**
 * Controls the I20 XES Secondary Spectrometer.
 * <p>
 * Provides a mapping between the motors which support the detector and analyser crystals, and the energy of the x-rays
 * incident on the detector.
 * <p>
 * Assumes the detector motor has been calibrated in such a way that its position is the same as the Bragg angle. TODO
 * this may need changing...
 */
public class XesSpectrometerScannable extends ScannableMotionUnitsBase implements Scannable, Findable, IObserver {

	ScannableMotor xtal_x; // also known as L
	ScannableMotor det_y;
	ScannableMotor det_x;
	ScannableMotor det_rot;
	DummyPersistentScannable radiusScannable;
	double bragg =80;
	double radius = 1000;

	Boolean[] additionalCrystalsInUse = new Boolean[] { false, false, false, false };

	ScannableMotor[] xtalxs = new ScannableMotor[2];
	ScannableMotor[] xtalys = new ScannableMotor[3];
	ScannableMotor[] xtalbraggs = new ScannableMotor[3];
	ScannableMotor[] xtaltilts = new ScannableMotor[3];

	Double[] additionalCrystalHorizontalOffsets = new Double[] { -137., 137. };

	public XesSpectrometerScannable() {
		this.inputNames = new String[] { "XES" };
		this.extraNames = new String[] {};
		this.outputFormat = new String[] { "%.4f" };
	}

	@Override
	public void configure() throws FactoryException {
	}

	@Override
	public void stop() throws DeviceException {

		xtal_x.stop();
		det_y.stop();
		det_x.stop();
		det_rot.stop();

		xtalxs[0].stop();
		xtalxs[1].stop();

		xtalys[0].stop();
		xtalys[1].stop();
		xtalys[2].stop();

		xtalbraggs[0].stop();
		xtalbraggs[1].stop();
		xtalbraggs[2].stop();

		xtaltilts[0].stop();
		xtaltilts[1].stop();
		xtaltilts[2].stop();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return xtal_x.isBusy() || det_y.isBusy() || det_x.isBusy() || det_rot.isBusy() || xtalxs[0].isBusy()
				|| xtalxs[1].isBusy() || xtalys[0].isBusy() || xtalys[1].isBusy() || xtalys[2].isBusy()
				|| xtalbraggs[0].isBusy() || xtalbraggs[1].isBusy() || xtalbraggs[2].isBusy() || xtaltilts[0].isBusy()
				|| xtaltilts[1].isBusy() || xtaltilts[2].isBusy() || radiusScannable.isBusy();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		notifyIObservers(this, new ScannableStatus(getName(), ScannableStatus.BUSY));
		String pos = position.toString();
		double posVal = Double.parseDouble(pos);
		bragg = posVal;

		radius = Double.parseDouble(radiusScannable.getPosition().toString());

		double targetDetX = XesUtils.getDx(radius, bragg);
		double targetDetY = XesUtils.getDy(radius, bragg);
		double targetL = XesUtils.getL(radius, bragg);
		double targetXtalTheta = XesUtils.getCrystalRotation(bragg);

		checkPositionValid(xtal_x,targetL);
		checkPositionValid(det_x,targetDetX);
		checkPositionValid(det_y,targetDetY);
		checkPositionValid(det_rot,targetXtalTheta * 2);

		checkPositionValid(xtalbraggs[1],targetXtalTheta);

		double[] iTargets = XesUtils
				.getAdditionalCrystalPositions(radius, bragg, additionalCrystalHorizontalOffsets[0]);

		// double R = radius;
		//
		// double braggRad = Math.toRadians(bragg);
		//
		// double sin_bragg = Math.sin(braggRad);
		// double p1 = R*R*Math.pow(sin_bragg,4)-(137*137);
		//
		// double p = Math.sqrt(Math.abs(p1));
		//
		// double tilt = Math.toDegrees(Math.atan(137/(p*sin_bragg)));
		//
		// double topLine = (Math.sqrt((137*137)+(p*p)*Math.pow(sin_bragg, 2)));
		// double bottomLine = (p*Math.cos(braggRad));
		//
		// double pitch = 90 - Math.toDegrees((Math.atan(topLine/bottomLine)));

		// xtalxs[0].asynchronousMoveTo((targetL-iTargets[0])*-1);

		// TODO may need to handle case where there are 5 crystals in the future...

		// the left hand xtal
		checkPositionValid(xtalxs[0],iTargets[0]);
		checkPositionValid(xtalys[0],iTargets[1]);
		checkPositionValid(xtaltilts[0],iTargets[2]);
		checkPositionValid(xtalbraggs[0],iTargets[3]);

		// xtalxs[1].asynchronousMoveTo((targetL-iTargets[0])*-1);

		// the right hand xtal
		checkPositionValid(xtalxs[1],iTargets[0]);
		checkPositionValid(xtalys[2],iTargets[1]);
		checkPositionValid(xtaltilts[2],-iTargets[2]);
		checkPositionValid(xtalbraggs[2],iTargets[3]);
		
		
		// actual moves, once we know everything is OK.
		xtal_x.asynchronousMoveTo(targetL);
		det_x.asynchronousMoveTo(targetDetX);
		det_y.asynchronousMoveTo(targetDetY);
		det_rot.asynchronousMoveTo(targetXtalTheta * 2);

		xtalbraggs[1].asynchronousMoveTo(targetXtalTheta);
		xtalxs[0].asynchronousMoveTo(iTargets[0]);
		xtalys[0].asynchronousMoveTo(iTargets[1]);
		xtaltilts[0].asynchronousMoveTo(iTargets[2]);
		xtalbraggs[0].asynchronousMoveTo(iTargets[3]);


		xtalxs[1].asynchronousMoveTo(iTargets[0]);
		xtalys[2].asynchronousMoveTo(iTargets[1]);
		xtaltilts[2].asynchronousMoveTo(-iTargets[2]);
		xtalbraggs[2].asynchronousMoveTo(iTargets[3]);

	}

	private void checkPositionValid(ScannableMotor scannable, double target) throws DeviceException{
		Double min = scannable.getLowerMotorLimit();
		Double max  = scannable.getUpperMotorLimit();
		
		if (min != null && max != null  && (target > max || target < min)){
			throw new DeviceException("Move not valid. " + target + " outside of limits of " + scannable.getName() + " motor.");
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return bragg;
	}

	public Double getRadius() {
		return radius;
	}

	public void setRadius(Double rowlandRadius) {
		this.radius = rowlandRadius;
	}

	public void setDet_y(ScannableMotor det_y) {
		this.det_y = det_y;
	}

	public ScannableMotor getDet_x() {
		return det_x;
	}

	public void setDet_x(ScannableMotor det_x) {
		this.det_x = det_x;
	}

	public ScannableMotor getDet_rot() {
		return det_rot;
	}

	public void setDet_rot(ScannableMotor det_rot) {
		this.det_rot = det_rot;
	}

	public ScannableMotor getXtal_x() {
		return xtal_x;
	}

	public void setXtal_x(ScannableMotor xtal_x) {
		this.xtal_x = xtal_x;
	}

	public Boolean[] getAdditionalCrystalsInUse() {
		return additionalCrystalsInUse;
	}

	public void setAdditionalCrystalsInUse(Boolean[] additionalCrystalsInUse) {
		this.additionalCrystalsInUse = additionalCrystalsInUse;
	}

	public ScannableMotor getXtal_minus1_x() {
		return xtalxs[0];
	}

	public void setXtal_minus1_x(ScannableMotor Xtal_minus1_x) {
		this.xtalxs[0] = Xtal_minus1_x;
	}

	public ScannableMotor getXtal_minus1_y() {
		return xtalys[0];
	}

	public void setXtal_minus1_y(ScannableMotor Xtal_minus1_y) {
		this.xtalys[0] = Xtal_minus1_y;
	}

	public ScannableMotor getXtal_minus1_pitch() {
		return xtalbraggs[0];
	}

	public void setXtal_minus1_pitch(ScannableMotor Xtal_minus1_pitch) {
		this.xtalbraggs[0] = Xtal_minus1_pitch;
	}

	public ScannableMotor getXtal_minus1_rot() {
		return xtaltilts[0];
	}

	public void setXtal_minus1_rot(ScannableMotor Xtal_minus1_rot) {
		this.xtaltilts[0] = Xtal_minus1_rot;
	}

	public ScannableMotor getXtal_central_y() {
		return xtalys[1];
	}

	public void setXtal_central_y(ScannableMotor Xtal_central_y) {
		this.xtalys[1] = Xtal_central_y;
	}

	public ScannableMotor getXtal_central_pitch() {
		return xtalbraggs[1];
	}

	public void setXtal_central_pitch(ScannableMotor Xtal_central_pitch) {
		this.xtalbraggs[1] = Xtal_central_pitch;
	}

	public ScannableMotor getXtal_central_rot() {
		return xtaltilts[1];
	}

	public void setXtal_central_rot(ScannableMotor Xtal_central_rot) {
		this.xtaltilts[1] = Xtal_central_rot;
	}

	public ScannableMotor getxtal_plus1_x() {
		return xtalxs[1];
	}

	public void setxtal_plus1_x(ScannableMotor xtal_plus1_x) {
		this.xtalxs[1] = xtal_plus1_x;
	}

	public ScannableMotor getxtal_plus1_y() {
		return xtalys[2];
	}

	public void setxtal_plus1_y(ScannableMotor xtal_plus1_y) {
		this.xtalys[2] = xtal_plus1_y;
	}

	public ScannableMotor getxtal_plus1_pitch() {
		return xtalbraggs[2];
	}

	public void setxtal_plus1_pitch(ScannableMotor xtal_plus1_pitch) {
		this.xtalbraggs[2] = xtal_plus1_pitch;
	}

	public ScannableMotor getxtal_plus1_rot() {
		return xtaltilts[2];
	}

	public void setxtal_plus1_rot(ScannableMotor xtal_plus1_rot) {
		this.xtaltilts[2] = xtal_plus1_rot;
	}

	public ScannableMotor getDet_y() {
		return det_y;
	}

	@Override
	public void update(Object source, Object arg) {
		notifyIObservers(this, new ScannableStatus(getName(), ScannableStatus.BUSY));
	}

	public DummyPersistentScannable getRadiusScannable() {
		return radiusScannable;
	}

	public void setRadiusScannable(DummyPersistentScannable radiusScannable) {
		this.radiusScannable = radiusScannable;
	}

}
