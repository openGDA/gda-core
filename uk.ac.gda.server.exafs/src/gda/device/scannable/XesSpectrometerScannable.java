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

package uk.ac.gda.beamline.i20.scannable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannableMotionUnitsBase;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.ScannableStatus;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.exafs.xes.XesUtils;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Controls the I20 XES Secondary Spectrometer.
 * <p>
 * Provides a mapping between the motors which support the detector and analyser crystals, and the energy of the x-rays
 * incident on the detector.
 * <p>
 * Assumes the detector motor has been calibrated in such a way that its position is the same as the Bragg angle.
 */
@ServiceInterface(ScannableMotionUnits.class)
public class XesSpectrometerScannable extends ScannableMotionUnitsBase implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(XesSpectrometerScannable.class);

	private volatile boolean stopCalled = false;
	private volatile Boolean isRunningTrajectoryMovement = false;

	private double bragg = 80;
	private double radius = 1000;
	private double trajectoryStepSize = 0.02; // the size of each bragg angle step when moving the detector.

	private Double[] additionalCrystalHorizontalOffsets = new Double[] { -137., 137. };

	// Positioners that control which of the 3 crystals are allowed to be moved.
	// If these are left as null allowedToMove is set to true.
	private EnumPositioner minusCrystalAllowedToMove;
	private EnumPositioner centreCrystalAllowedToMove;
	private EnumPositioner plusCrystalAllowedToMove;

	private ScannableMotor spectrometer_x; // also known as L
	private ScannableMotor det_y;
	private ScannableMotor det_x;
	private ScannableMotor det_rot;
	private Scannable radiusScannable;

	private ScannableMotor[] xtalxs = new ScannableMotor[2];
	private ScannableMotor[] xtalys = new ScannableMotor[3];
	private ScannableMotor[] xtalbraggs = new ScannableMotor[3];
	private ScannableMotor[] xtaltilts = new ScannableMotor[3];

	/** Scannable groups to store the collections of scannables for each logical part of the spectrometer. */
	private ScannableGroupAllowedToMove minusCrystal;
	private ScannableGroupAllowedToMove centreCrystal;
	private ScannableGroupAllowedToMove plusCrystal;
	private ScannableGroup detector;

	// flag to prevent the warning about the position is an estimate being sent more than once at a time
	private boolean hasGetPositionWarningBeenSent = false;

	public XesSpectrometerScannable() {
		this.inputNames = new String[] { "XES" };
		this.extraNames = new String[] {};
		this.outputFormat = new String[] { "%.4f" };
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		minusCrystal = new ScannableGroupAllowedToMove("minusCrystal",	Arrays.asList(xtalxs[0], xtalys[0], xtaltilts[0], xtalbraggs[0]));
		minusCrystal.configure();

		centreCrystal = new ScannableGroupAllowedToMove("centreCrystal", Arrays.asList(xtalbraggs[1]));
		centreCrystal.configure();

		plusCrystal = new ScannableGroupAllowedToMove("plusCrystal", Arrays.asList(xtalxs[1], xtalys[2], xtaltilts[2], xtalbraggs[2]));
		plusCrystal.configure();

		detector = new ScannableGroup("detector", new Scannable[] {det_x, det_y, det_rot});
		detector.configure();

		setConfigured(true);
		updateActiveGroups();
	}

	/**
	 * Extension of {@link ScannableGroup} which adds an 'allowedToMove' flag. If 'allowedToMove' is set to true,
	 * it will behave as a normal ScannableGroup. If it is set to false :
	 * <li> None of the scannables in the group will move when asynchronousMoveTo is called.
	 * <li> {@link #isBusy()} will return false, and {@link #stop()} will do nothing.
	 */
	private class ScannableGroupAllowedToMove extends ScannableGroup {

		private boolean isAllowedToMove = true;

		public ScannableGroupAllowedToMove(String name, List<Scannable> scannables) throws FactoryException {
			setName(name);
			setGroupMembers(scannables);
		}

		@Override
		public void asynchronousMoveTo(Object position) throws DeviceException {
			if (isAllowedToMove) {
				super.asynchronousMoveTo(position);
			}
		}

		@Override
		public boolean isBusy() throws DeviceException {
			if (isAllowedToMove) {
				return super.isBusy();
			} else {
				return false;
			}
		}

		@Override
		public void stop() throws DeviceException {
			if (isAllowedToMove) {
				super.stop();
			}
		}

		public void setAllowedToMove(boolean isAllowedToMove) {
			this.isAllowedToMove = isAllowedToMove;
		}

		public boolean isAllowedToMove() {
			return isAllowedToMove;
		}
	}

	/**
	 * Set the 'allowedToMove' flag for a scannable group based on position of
	 * allowedToMove enum positioner. If the positioner is null, or DeviceException is thrown,
	 * a default value of 'true' is used.
	 * @param scnGroup
	 * @param positioner
	 */
	private void setGroupActive(ScannableGroupAllowedToMove scnGroup, EnumPositioner positioner) {
		boolean doMove = true;
		if (positioner != null) {
			try {
				String position = positioner.getPosition().toString();
				doMove = Boolean.parseBoolean(position);
			} catch (Exception ex) {
				logger.warn("Problem setting 'allowed to move' from EnumPositioner {}", positioner, ex);
			}
		}
		logger.debug("Setting {}.allowedToMove to {}", scnGroup.getName(), doMove);
		scnGroup.setAllowedToMove(doMove);
	}

	/**
	 * Update the 'allowedToMove' flag for each scannable group
	 * @throws DeviceException
	 */
	private void updateActiveGroups() {
		if(isConfigured()) {
			setGroupActive(minusCrystal, minusCrystalAllowedToMove);
			setGroupActive(centreCrystal, centreCrystalAllowedToMove);
			setGroupActive(plusCrystal, plusCrystalAllowedToMove);
		}
	}

	@Override
	public void stop() throws DeviceException {
		stopCalled = true;

		detector.stop();
		centreCrystal.stop();
		minusCrystal.stop();
		plusCrystal.stop();
		spectrometer_x.stop();

		try {
			detector.waitWhileBusy();
			centreCrystal.waitWhileBusy();
			minusCrystal.waitWhileBusy();
			plusCrystal.waitWhileBusy();
			spectrometer_x.waitWhileBusy();
		} catch (InterruptedException e) {
			throw new DeviceException("InterruptedException while waiting for motors to stop");
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return isRunningTrajectoryMovement ||
				detector.isBusy() ||
				centreCrystal.isBusy() ||
				minusCrystal.isBusy() ||
				plusCrystal.isBusy() ||
				spectrometer_x.isBusy() ||
				xtalys[1].isBusy() || xtaltilts[1].isBusy() ||  radiusScannable.isBusy();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		updateActiveGroups();

		String pos = position.toString();
		double targetBragg = Double.parseDouble(pos);
		double currentPosition = Double.parseDouble(getPosition().toString());
		radius = Double.parseDouble(radiusScannable.getPosition().toString());

		isRunningTrajectoryMovement = false;

		// Calculate detector trajectory points if doing a 'large' movement
		final List<Double[]> trajectoryPoints;
		if (Math.abs(currentPosition - targetBragg) > trajectoryStepSize){
			isRunningTrajectoryMovement = true;
			trajectoryPoints = getDetectorTrajectoryPoints(currentPosition, targetBragg);
		} else {
			trajectoryPoints = null;
		}

		// test the final points are in limits

		Double[] xyThetaValues = getXYTheta(radius, targetBragg);

		// the detector and spectrometer overall position
		double finalSpectrometerX = XesUtils.getL(radius, targetBragg);
		checkPositionValid(spectrometer_x, finalSpectrometerX);

		double[] finalDetectorPosition = {xyThetaValues[0], xyThetaValues[1], xyThetaValues[2]*2};
		checkPositionValid(detector, finalDetectorPosition);

		// the centre crystal
		double[] centreCrystalPosition = {xyThetaValues[2]};
		checkPositionValid(centreCrystal, centreCrystalPosition);

		// the 'minus' crystal
		double[] minusCrystalPosition = XesUtils.getAdditionalCrystalPositions(radius, targetBragg,	additionalCrystalHorizontalOffsets[0]);
		checkPositionValid(minusCrystal, minusCrystalPosition);

		// the 'plus' crystal
		double[] plusCrystalPosition = Arrays.copyOf(minusCrystalPosition, minusCrystalPosition.length);
		plusCrystalPosition[2] *= -1.0;
		checkPositionValid(plusCrystal, plusCrystalPosition);

		// reset the stop flag
		stopCalled = false;

		// only now store the new bragg value
		bragg = targetBragg;

		// now do moves ...
		notifyIObservers(this, ScannableStatus.BUSY);

		spectrometer_x.asynchronousMoveTo(finalSpectrometerX);

		centreCrystal.asynchronousMoveTo(centreCrystalPosition);

		minusCrystal.asynchronousMoveTo(minusCrystalPosition);

		plusCrystal.asynchronousMoveTo(plusCrystalPosition);

		// loop over trajectory points for the detector only
		if (isRunningTrajectoryMovement && trajectoryPoints != null) {
			Async.execute(() -> executeDetectorTrajectory(trajectoryPoints) );
		} else {
			// move detector to final position
			detector.asynchronousMoveTo(finalDetectorPosition);
		}
	}

	/**
	 * Calculate the detector positions (X, Y, 2*theta) required to go between current and target bragg angles
	 * Bragg angle step size of {@link #trajectoryStepSize} are taken between detector positions.
	 * @param currentPosition
	 * @param targetBragg
	 * @return List of (X, Y, theta)  values for points along trajectory
	 * @throws DeviceException
	 */
	private List<Double[]> getDetectorTrajectoryPoints(double currentPosition, double targetBragg) throws DeviceException {

		boolean positiveMove = true;
		if (currentPosition > targetBragg)
			positiveMove = false;

		// create the trajectory points for the detector
		List<Double[]> trajectoryPoints = new ArrayList<>();

		int numPoints = (int) Math.round(Math.abs(currentPosition - targetBragg) / trajectoryStepSize);
		double braggAtNode = currentPosition;
		for (int node = 0; node <= numPoints; node++) {
			Double[] nodeDetectorPositions = getDetectorPosition(radius, braggAtNode);
			if (nodeDetectorPositions[0] == null)
				throw new DeviceException("Could not calculate target positions. Will not perform move");

			trajectoryPoints.add(nodeDetectorPositions);
			if (positiveMove)
				braggAtNode += trajectoryStepSize;
			else
				braggAtNode -= trajectoryStepSize;
		}
		// add the final position
		trajectoryPoints.add(getDetectorPosition(radius, targetBragg));
		return trajectoryPoints;
	}

	/**
	 * Move the detector along trajectory
	 * @param trajectoryPoints list of positions, as calculated by {@link #getDetectorTrajectoryPoints(double, double)}
	 */
	private void executeDetectorTrajectory(final List<Double[]> trajectoryPoints) {
		try {
			for (int node=0; node < trajectoryPoints.size(); node++) {
				if (stopCalled)
					return;
				detector.waitWhileBusy();

				if (stopCalled)
					return;
				detector.asynchronousMoveTo(trajectoryPoints.get(node));
			}
		} catch (InterruptedException e) {
			logger.warn("InterruptedException while running XESEnegry trajectory", e);
		} catch (DeviceException e) {
			logger.warn("DeviceException while running XESEnegry trajectory", e);
		} finally {
//			logger.info("Spectrometer move complete. XES Spectrometer final move positions: X:"+targetDetXArray[numTrajPoints-1]+" Y:"+targetDetYArray[numTrajPoints-1]+" Theta:"+targetXtalThetaArray[numTrajPoints-1]);
			isRunningTrajectoryMovement = false;
		}
	}

	/**
	 * Calculate the detector (x,y) position and angle (2*theta) for given radius and bragg angle
	 * @param radius
	 * @param braggAngle
	 * @return Double [] X, Y, 2*theta
	 */
	private Double[] getDetectorPosition(double radius, double braggAngle) {
		Double[] xytheta = getXYTheta(radius, braggAngle);
		return new Double[] {xytheta[0], xytheta[1], xytheta[2]*2};
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

	/**
	 * Check that a target position is within limits of a scannable. A DeviceException is thrown if it's not.
	 * @param scannable
	 * @param target
	 * @throws DeviceException
	 */
	private void checkPositionValid(ScannableMotor scannable, double target) throws DeviceException {
		Double min = scannable.getLowerMotorLimit();
		Double max = scannable.getUpperMotorLimit();
		if (min != null && max != null && (target > max || target < min)) {
			String message = String.format("Move for %s is not valid. Target position %f outside of motor limits %f, %f",
					scannable.getName(), target, min, max);
			throw new DeviceException(message);
		}
	}

	/**
	 * Check whether the target position array is value for a {@link ScannableGroup}
	 * by calling {{@link #checkPositionValid(ScannableMotor, double)} for each scannable in the group.
	 * @param scannableGroup
	 * @param target
	 * @throws DeviceException
	 */
	private void checkPositionValid(ScannableGroup scannableGroup, double[] target) throws DeviceException {
		List<Scannable> scannables = scannableGroup.getGroupMembers();
		for (int i=0; i<target.length && i<scannables.size(); i++) {
			checkPositionValid((ScannableMotor)scannables.get(i), target[i]);
		}
	}

	private void checkPositionValid(ScannableGroupAllowedToMove scannableGroup, double[] target) throws DeviceException {
		if (scannableGroup.isAllowedToMove()) {
			checkPositionValid((ScannableGroup)scannableGroup, target);
		}
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
		double lPosition = Double.parseDouble(spectrometer_x.getPosition().toString());
		// In the Rowland condition: sin(2*(90-bragg)) = y/L
		double derivedBragg = 90 - (0.5 * Math.toDegrees(Math.asin(yPosition / lPosition)));
		return derivedBragg;
	}

	@Override
	public String toFormattedString() {
		try {
			if (!doesMotorPositionAgreeWithExpectedBraggAngle()) {
				final double position = braggBasedOnDetectorRotation();
				final String formattedPosition = String.format(getOutputFormat()[0], position);
				return getName() + "\t: " + formattedPosition + " " + "deg. NB: this is derived from only the "
						+ det_y.getName() + " and " + spectrometer_x.getName() + " motor positions.";
			} else {
				return super.toFormattedString();
			}
		} catch (Exception e) {
			logger.warn("Exception while deriving the {} position", getName(), e);
			return valueUnavailableString();
		}
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
		return spectrometer_x;
	}

	public void setXtal_x(ScannableMotor xtal_x) {
		this.spectrometer_x = xtal_x;
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
		notifyIObservers(this, ScannableStatus.BUSY);
	}

	public Scannable getRadiusScannable() {
		return radiusScannable;
	}

	public void setRadiusScannable(Scannable radiusScannable) {
		this.radiusScannable = radiusScannable;
	}

	public Double[] getAdditionalCrystalHorizontalOffsets() {
		return additionalCrystalHorizontalOffsets;
	}

	public ScannableGroup getDetectorGroup() {
		return detector;
	}

	public ScannableGroup getMinusCrystalGroup() {
		return minusCrystal;
	}

	public ScannableGroup getPlusCrystalGroup() {
		return plusCrystal;
	}

	public ScannableGroup getCentreCrystalGroup() {
		return centreCrystal;
	}

	public EnumPositioner getMinusCrystalAllowedToMove() {
		return minusCrystalAllowedToMove;
	}

	public void setMinusCrystalAllowedToMove(EnumPositioner minusCrystalAllowedToMove) {
		this.minusCrystalAllowedToMove = minusCrystalAllowedToMove;
	}

	public EnumPositioner getCentreCrystalAllowedToMove() {
		return centreCrystalAllowedToMove;
	}

	public void setCentreCrystalAllowedToMove(EnumPositioner centreCrystalAllowedToMove) {
		this.centreCrystalAllowedToMove = centreCrystalAllowedToMove;
	}

	public EnumPositioner getPlusCrystalAllowedToMove() {
		return plusCrystalAllowedToMove;
	}

	public void setPlusCrystalAllowedToMove(EnumPositioner plusCrystalAllowedToMove) {
		this.plusCrystalAllowedToMove = plusCrystalAllowedToMove;
	}

}