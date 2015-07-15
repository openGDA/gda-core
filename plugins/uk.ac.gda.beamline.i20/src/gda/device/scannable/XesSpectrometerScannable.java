/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.exafs.xes.XesUtils;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.observable.IObserver;
import uk.ac.gda.util.ThreadManager;

/**
 * Controls the I20 XES Secondary Spectrometer.
 * <p>
 * Provides a mapping between the motors which support the detector and analyser crystals, and the energy of the x-rays
 * incident on the detector.
 * <p>
 * Assumes the detector motor has been calibrated in such a way that its position is the same as the Bragg angle.
 */
public class XesSpectrometerScannable extends ScannableMotionUnitsBase implements Scannable, Findable, IObserver {

	private static final Logger logger = LoggerFactory.getLogger(XesSpectrometerScannable.class);

	private volatile boolean stopCalled = false;
	private volatile Boolean isRunningTrajectoryMovement = false;

	double bragg = 80;
	double radius = 1000;
	double trajectoryStepSize = 0.02; // the size of each bragg angle step when moving the detector.

	Double[] additionalCrystalHorizontalOffsets = new Double[] { -137., 137. };
	Boolean[] additionalCrystalsInUse = new Boolean[] { false, false, false, false };

	ScannableMotor xtal_x; // also known as L
	ScannableMotor det_y;
	ScannableMotor det_x;
	ScannableMotor det_rot;
	DummyPersistentScannable radiusScannable;

	ScannableMotor[] xtalxs = new ScannableMotor[2];
	ScannableMotor[] xtalys = new ScannableMotor[3];
	ScannableMotor[] xtalbraggs = new ScannableMotor[3];
	ScannableMotor[] xtaltilts = new ScannableMotor[3];

	private Double[] targetDetXArray;
	private Double[] targetDetYArray;
	private Double[] targetXtalThetaArray;
	private Integer numTrajPoints;

	// flag to prevent the warning about the position is an estimate being sent more than once at a time
	private boolean hasGetPositionWarningBeenSent = false;

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
		stopCalled = true;

		xtal_x.stop();
		det_x.stop();
		det_y.stop();
		det_rot.stop();

		xtalbraggs[1].stop();
		xtalxs[0].stop();
		xtalys[0].stop();
		xtaltilts[0].stop();
		xtalbraggs[0].stop();

		xtalxs[1].stop();
		xtalys[2].stop();
		xtaltilts[2].stop();
		xtalbraggs[2].stop();

		try {
			xtal_x.waitWhileBusy();
			det_x.waitWhileBusy();
			det_y.waitWhileBusy();
			det_rot.waitWhileBusy();

			xtalbraggs[1].waitWhileBusy();
			xtalxs[0].waitWhileBusy();
			xtalys[0].waitWhileBusy();
			xtaltilts[0].waitWhileBusy();
			xtalbraggs[0].waitWhileBusy();

			xtalxs[1].waitWhileBusy();
			xtalys[2].waitWhileBusy();
			xtaltilts[2].waitWhileBusy();
			xtalbraggs[2].waitWhileBusy();
		} catch (InterruptedException e) {
			throw new DeviceException("InterruptedException while waiting for motors to stop");
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return isRunningTrajectoryMovement || det_y.isBusy() || det_x.isBusy() || xtal_x.isBusy() || det_rot.isBusy() || xtalxs[0].isBusy()
				|| xtalxs[1].isBusy() || xtalys[0].isBusy() || xtalys[1].isBusy() || xtalys[2].isBusy()
				|| xtalbraggs[0].isBusy() || xtalbraggs[1].isBusy() || xtalbraggs[2].isBusy() || xtaltilts[0].isBusy()
				|| xtaltilts[1].isBusy() || xtaltilts[2].isBusy() || radiusScannable.isBusy();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		String pos = position.toString();
		double targetBragg = Double.parseDouble(pos);
		double currentPosition = Double.parseDouble(getPosition().toString());
		radius = Double.parseDouble(radiusScannable.getPosition().toString());

		isRunningTrajectoryMovement = false;
		targetDetXArray = null;
		targetDetYArray = null;
		targetXtalThetaArray = null;
		numTrajPoints = null;

		// test if it a 'large' movement, defined as more than 10 times the step size
		if (Math.abs(currentPosition - targetBragg) > trajectoryStepSize){
			isRunningTrajectoryMovement = true;

			boolean positiveMove = true;
			if (currentPosition > targetBragg)
				positiveMove = false;

			// create the trajectory points for the detector
			numTrajPoints = (int) Math.round(Math.abs(currentPosition - targetBragg) / trajectoryStepSize);
			targetDetXArray = new Double[numTrajPoints+2];
			targetDetYArray = new Double[numTrajPoints+2];
			targetXtalThetaArray = new Double[numTrajPoints+2];

			double braggAtNode = currentPosition;
			for (int node = 0; node <= numTrajPoints; node++) {
				Double[] nodeDetectorPositions = getXYTheta(radius, braggAtNode);
				targetDetXArray[node] = nodeDetectorPositions[0];
				if (targetDetXArray[node] == null)
					throw new DeviceException("Could not calculate target positions. Will not perform move");
				targetDetYArray[node] = nodeDetectorPositions[1];
				targetXtalThetaArray[node] = nodeDetectorPositions[2]*2;
				if (positiveMove)
					braggAtNode += trajectoryStepSize;
				else
					braggAtNode -= trajectoryStepSize;
			}
			Double[] finalNodeDetectorPositions = getXYTheta(radius, targetBragg);
			targetDetXArray[numTrajPoints+1] = finalNodeDetectorPositions[0];
			targetDetYArray[numTrajPoints+1] = finalNodeDetectorPositions[1];
			targetXtalThetaArray[numTrajPoints+1] = finalNodeDetectorPositions[2]*2;
		}

		// test the final points are in limits

		Double[] finalDetectorPositions = getXYTheta(radius, targetBragg);
		double targetL = XesUtils.getL(radius, targetBragg);

		checkPositionValid(det_x, finalDetectorPositions[0]);
		checkPositionValid(det_y, finalDetectorPositions[1]);
		checkPositionValid(det_rot, finalDetectorPositions[2] * 2);
		checkPositionValid(xtal_x, targetL);

		checkPositionValid(xtalbraggs[1], finalDetectorPositions[2]);

		double[] iTargets = XesUtils.getAdditionalCrystalPositions(radius, targetBragg,
				additionalCrystalHorizontalOffsets[0]);

		// the left hand xtal
		checkPositionValid(xtalxs[0], iTargets[0]);
		checkPositionValid(xtalys[0], iTargets[1]);
		checkPositionValid(xtaltilts[0], iTargets[2]);
		checkPositionValid(xtalbraggs[0], iTargets[3]);

		// the right hand xtal
		checkPositionValid(xtalxs[1], iTargets[0]);
		checkPositionValid(xtalys[2], iTargets[1]);
		checkPositionValid(xtaltilts[2], -iTargets[2]);
		checkPositionValid(xtalbraggs[2], iTargets[3]);

		// reset the stop flag
		stopCalled = false;

		// only now store the new bragg value
		bragg = targetBragg;

		// now do moves
		notifyIObservers(this, new ScannableStatus(getName(), ScannableStatus.BUSY));

		// central crystal
		xtal_x.asynchronousMoveTo(targetL);
		xtalbraggs[1].asynchronousMoveTo(finalDetectorPositions[2]);

		// crystal on the left
		xtalxs[0].asynchronousMoveTo(iTargets[0]);
		xtalys[0].asynchronousMoveTo(iTargets[1]);
		xtaltilts[0].asynchronousMoveTo(iTargets[2]);
		xtalbraggs[0].asynchronousMoveTo(iTargets[3]);

		// crystal on the right
		xtalxs[1].asynchronousMoveTo(iTargets[0]);
		xtalys[2].asynchronousMoveTo(iTargets[1]);
		xtaltilts[2].asynchronousMoveTo(-iTargets[2]);
		xtalbraggs[2].asynchronousMoveTo(iTargets[3]);

		// loop over points for the detector only
		if (isRunningTrajectoryMovement && numTrajPoints != null && targetDetXArray != null && targetDetYArray != null
				&& targetXtalThetaArray != null) {
			ThreadManager.getThread(new Runnable() {
				@Override
				public void run() {
					int node = 0;
					try {
						for (; node < targetDetXArray.length; node++) {
							if (stopCalled)
								return;
							det_x.waitWhileBusy();
							det_y.waitWhileBusy();
							det_rot.waitWhileBusy();
							if (stopCalled)
								return;
							det_x.asynchronousMoveTo(targetDetXArray[node]);
							det_y.asynchronousMoveTo(targetDetYArray[node]);
							det_rot.asynchronousMoveTo(targetXtalThetaArray[node]);
						}
					} catch (InterruptedException e) {
						logger.warn("InterruptedException while running XESEnegry trajectory", e);
					} catch (DeviceException e) {
						logger.warn("DeviceException while running XESEnegry trajectory", e);
					} finally {
//						logger.info("Spectrometer move complete. XES Spectrometer final move positions: X:"+targetDetXArray[numTrajPoints-1]+" Y:"+targetDetYArray[numTrajPoints-1]+" Theta:"+targetXtalThetaArray[numTrajPoints-1]);
						isRunningTrajectoryMovement = false;
					}
				}
			}).start();
		} else {
			// detector
			det_x.asynchronousMoveTo(finalDetectorPositions[0]);
			det_y.asynchronousMoveTo(finalDetectorPositions[1]);
			det_rot.asynchronousMoveTo(finalDetectorPositions[2] * 2);
		}
	}

	/**
	 * The detector motor angle will be 2*theta.  The analyser crystals should be at theta.
	 *
	 * @param radius
	 * @param bragg
	 * @return Double[] X,Y,theta
	 */
	private Double[] getXYTheta(Double radius, Double bragg ){
		double detX = XesUtils.getDx(radius, bragg);
		double detY = XesUtils.getDy(radius, bragg);
		double theta = XesUtils.getCrystalRotation(bragg);
		return new Double[]{detX,detY,theta};
	}

	private void checkPositionValid(ScannableMotor scannable, double target) throws DeviceException {
		Double min = scannable.getLowerMotorLimit();
		Double max = scannable.getUpperMotorLimit();
		if (min != null && max != null && (target > max || target < min))
			throw new DeviceException("Move not valid. " + target + " outside of limits of " + scannable.getName()	+ " motor.");
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		if (!doesMotorPositionAgreeWithExpectedBraggAngle()) {
			if (!hasGetPositionWarningBeenSent) {
				logger.warn(getName()
					+ " cannot correctly determine its position: detector angle disagrees with expected position.\nReported position is based on detector rotation.\nIf you have not moved XES bragg or energy since restarting the GDA then please ignore this message");
				hasGetPositionWarningBeenSent = true;
			}
			return braggBasedOnDetectorRotation();
		}
		hasGetPositionWarningBeenSent = false;
		return bragg;
	}

	private boolean doesMotorPositionAgreeWithExpectedBraggAngle() throws DeviceException {
		return Math.abs(braggBasedOnDetectorRotation() - bragg) < 1;
	}

	private double braggBasedOnDetectorRotation() throws NumberFormatException, DeviceException {
		double yPosition = Double.parseDouble(det_y.getPosition().toString());
		double lPosition = Double.parseDouble(xtal_x.getPosition().toString());
		// In the Rowland condition: sin(2*(90-bragg)) = y/L
		double derivedBragg = 90 - (0.5 * Math.toDegrees(Math.asin(yPosition / lPosition)));
		return derivedBragg;
	}

	@Override
	public String toFormattedString() {
		try {
			if (!doesMotorPositionAgreeWithExpectedBraggAngle()) {
				double position = braggBasedOnDetectorRotation();
				String formattedPosition = String.format(getOutputFormat()[0], position);
				return getName() + "\t: " + formattedPosition + " " + "deg. NB: this is derived from only the "
						+ det_y.getName() + " and " + xtal_x.getName() + " motor positions.";
			}
		} catch (Exception e) {
			logger.error("Exception while deriving the " + getName() + " position", e);
			super.toFormattedString();
		}

		return super.toFormattedString();
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

	public Double getTrajectoryStepSize() {
		return trajectoryStepSize;
	}

	public void setTrajectoryStepSize(Double trajectoryStepSize) {
		this.trajectoryStepSize = trajectoryStepSize;
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