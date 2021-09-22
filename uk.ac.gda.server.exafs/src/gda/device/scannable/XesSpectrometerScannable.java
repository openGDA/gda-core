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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannableMotionUnitsBase;
import gda.device.scannable.ScannableStatus;
import gda.device.scannable.ScannableUtils;
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
	private volatile boolean isRunningTrajectoryMovement = false;

	private double bragg = 80;
	private double radius = 1000;
	private double trajectoryStepSize = 0.02; // the size of each bragg angle step when moving the detector.

	private double horizontalOffset = 137.0;

	/** ScannableGroup containing Positioners that control which of the crystals are allowed to be moved.
	// If these are left as null allowedToMove is not modified. */
	private ScannableGroup crystalsAllowedToMove;

	private Scannable spectrometerX; // also known as L
	private Scannable radiusScannable;

	/** ScannableGroup containing the XesSpectrometerCrystal objects for the spectrometer. */
	private ScannableGroup crystalsGroup;
	/** List of spectrometer crystals extracted from crystals ScannableGroup */
	private List<XesSpectrometerCrystal> crystalList = Collections.emptyList();

	/** ScannableGroup containing the detector x, y and rotation scannables */
	private ScannableGroup detectorGroup;

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
		Objects.requireNonNull(crystalsGroup, "spectrometer crystals group has not been set");
		Objects.requireNonNull(detectorGroup, "detector scannable group has not been set");
		logger.info("Making list to spectrometer crystals");
		crystalList = getCrystalsList();
		if (crystalList.isEmpty()) {
			throw new FactoryException("No XesSpectrometerCrystals found for XesSpectrometer");
		}

		setConfigured(true);
		updateActiveGroups();
	}

	/**
	 * Set the 'allowedToMove' flag for a scannable group based on position of
	 * allowedToMove enum positioner. If the positioner is null, or DeviceException is thrown,
	 * a default value of 'true' is used.
	 * @param scnGroup
	 * @param positioner
	 */
	private void setGroupActive(XesSpectrometerCrystal scnGroup, EnumPositioner positioner) {
		if (positioner == null) {
			return;
		}
		boolean doMove = true;
		try {
			String position = positioner.getPosition().toString();
			doMove = Boolean.parseBoolean(position);
		} catch (Exception ex) {
			logger.warn("Problem setting 'allowed to move' from EnumPositioner {}", positioner, ex);
		}
		logger.debug("Setting {}.allowedToMove to {}", scnGroup.getName(), doMove);
		scnGroup.setAllowedToMove(doMove);
	}

	/**
	 * Update the 'allowedToMove' flag for each scannable group
	 * @throws DeviceException
	 */
	private void updateActiveGroups() {
		if(!isConfigured()) {
			return;
		}
		List<EnumPositioner> allowedToMovePositioners = getScannablesOfTypeFromGroup(crystalsAllowedToMove, EnumPositioner.class);
		if (allowedToMovePositioners.isEmpty()) {
			logger.info("Not updating allowed to move for crystals (no 'allowed to move' positioners have been set)");
			return;
		}
		if (allowedToMovePositioners.size() == crystalList.size()) {
			for(int i=0; i<allowedToMovePositioners.size(); i++) {
				setGroupActive(crystalList.get(i), allowedToMovePositioners.get(i));
			}
		} else {
			logger.info("Not updating 'allowed to move' - number of positioners and crystals does not match");
		}
	}

	@Override
	public void stop() throws DeviceException {
		stopCalled = true;

		detectorGroup.stop();
		crystalsGroup.stop();
		spectrometerX.stop();

		try {
			detectorGroup.waitWhileBusy();
			crystalsGroup.waitWhileBusy();
			spectrometerX.waitWhileBusy();
		} catch (InterruptedException e) {
			// Reset interrupt status
			Thread.currentThread().interrupt();
			throw new DeviceException("InterruptedException while waiting for motors to stop");
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return isRunningTrajectoryMovement ||
				detectorGroup.isBusy() ||
				crystalsGroup.isBusy() ||
				spectrometerX.isBusy() ||
				radiusScannable.isBusy();
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
		final List<double[]> trajectoryPoints;
		if (Math.abs(currentPosition - targetBragg) > trajectoryStepSize) {
			trajectoryPoints = getDetectorTrajectoryPoints(currentPosition, targetBragg);
		} else {
			trajectoryPoints = Collections.emptyList();
		}

		// test the final points are in limits

		// the detector and spectrometer overall position
		double finalSpectrometerX = XesUtils.getL(radius, targetBragg);
		checkPositionValid(spectrometerX, finalSpectrometerX);

		double[] finalDetectorPosition = getDetectorPosition(radius,  targetBragg);
		checkPositionValid(detectorGroup, finalDetectorPosition);

		Map<XesSpectrometerCrystal, double[]> positions = new HashMap<>();
		for(XesSpectrometerCrystal crystal : crystalList) {
			double[] crystalPositions;
			if (crystal.getHorizontalIndex()==0) {
				// the centre crystal
				double crystalPosition = XesUtils.getCrystalRotation(targetBragg);
				checkPositionValid(crystal.getPitchMotor(), crystalPosition);
				crystalPositions = new double[]{crystalPosition};
			} else {
				// the crystals crystal
				crystalPositions = getCrystalPositions(crystal, targetBragg);
				checkPositionValid(crystal, crystalPositions);
			}
			positions.put(crystal, crystalPositions);

		}

		// reset the stop flag
		stopCalled = false;

		// only now store the new bragg value
		bragg = targetBragg;

		// now do moves ...
		notifyIObservers(this, ScannableStatus.BUSY);

		spectrometerX.asynchronousMoveTo(finalSpectrometerX);

		for(var crystal : crystalList) {
			double[] crystalPosition = positions.get(crystal);
			if (crystalPosition==null) {
				continue;
			}
			if (crystal.getHorizontalIndex()==0) {
				if (crystal.isAllowedToMove()) {
					crystal.getPitchMotor().asynchronousMoveTo(crystalPosition[0]);
				}
			} else {
				crystal.asynchronousMoveTo(crystalPosition);
			}
		}

		// loop over trajectory points for the detector only
		if (!trajectoryPoints.isEmpty()) {
			isRunningTrajectoryMovement = true;
			Async.execute(() -> executeDetectorTrajectory(trajectoryPoints) );
		} else {
			// move detector to final position
			detectorGroup.asynchronousMoveTo(finalDetectorPosition);
		}
	}

	private double[] getCrystalPositions(XesSpectrometerCrystal crystal, double braggAngle) {
		return XesUtils.getAdditionalCrystalPositions(radius, braggAngle, crystal.getHorizontalIndex()*horizontalOffset);
	}
	/**
	 * Calculate the detector positions (X, Y, 2*theta) required to go between current and target bragg angles
	 * Bragg angle step size of {@link #trajectoryStepSize} are taken between detector positions.
	 * @param currentPosition
	 * @param targetBragg
	 * @return List of (X, Y, theta)  values for points along trajectory
	 */
	private List<double[]> getDetectorTrajectoryPoints(double currentPosition, double targetBragg) {

		boolean positiveMove = true;
		if (currentPosition > targetBragg)
			positiveMove = false;

		// create the trajectory points for the detector
		List<double[]> trajectoryPoints = new ArrayList<>();

		int numPoints = (int) Math.round(Math.abs(currentPosition - targetBragg) / trajectoryStepSize);
		double braggAtNode = currentPosition;
		logger.debug("Calculating detector trajector between Bragg angles {} and {} using {} points", currentPosition, targetBragg, numPoints);
		for (int node = 0; node <= numPoints; node++) {
			double[] nodeDetectorPositions = getDetectorPosition(radius, braggAtNode);
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
	private void executeDetectorTrajectory(final List<double[]> trajectoryPoints) {
		try {
			logger.info("Starting detector move along trajectory...");
			for (int node=0; node < trajectoryPoints.size(); node++) {
				if (stopCalled)
					return;
				detectorGroup.waitWhileBusy();

				if (stopCalled)
					return;
				detectorGroup.asynchronousMoveTo(trajectoryPoints.get(node));
			}
		} catch (InterruptedException e) {
			// An interrupt means the scan wishes to abort, the thread should be
			// re-interrupted so the scanning engine aborts smoothly.
			// See: https://alfred.diamond.ac.uk/documentation/manuals/GDA_Developer_Guide/master/java_development.html#handling-interrupts
			Thread.currentThread().interrupt();
			logger.warn("InterruptedException while running XESEnergy trajectory", e);
		} catch (DeviceException e) {
			logger.warn("DeviceException while running XESEnergy trajectory", e);
		} finally {
			isRunningTrajectoryMovement = false;
			logger.info("Spectrometer detector trajectory move finished.");
			try {
				logger.info("Final detector position : {}", ScannableUtils.getFormattedCurrentPosition(detectorGroup));
			} catch(DeviceException e) {
				logger.warn("Problem getting final detector position", e);
			}
		}
	}

	/**
	 * Calculate the detector (x,y) position and angle (2*theta) for given radius and bragg angle
	 * @param radius
	 * @param braggAngle
	 * @return Double [] X, Y, 2*theta
	 */
	private double[] getDetectorPosition(double radius, double braggAngle) {
		double[] xytheta = getXYTheta(radius, braggAngle);
		return new double[] {xytheta[0], xytheta[1], xytheta[2]*2};
	}

	/**
	 * The detector motor angle will be 2*theta.  The analyser crystals should be at theta.
	 *
	 * @param radius
	 * @param bragg
	 * @return Double[] X,Y,theta
	 */
	private double[] getXYTheta(Double radius, Double bragg ){
		double detX = XesUtils.getDx(radius, bragg);
		double detY = XesUtils.getDy(radius, bragg);
		double theta = XesUtils.getCrystalRotation(bragg);
		return new double[]{detX,detY,theta};
	}

	/**
	 * Check that a target position is within limits of a scannable. A DeviceException is thrown if it's not.
	 * @param scannable
	 * @param target
	 * @throws DeviceException
	 */
	private void checkPositionValid(Scannable scannable, Object target) throws DeviceException {
		String positionInvalidMessage = scannable.checkPositionValid(target);
		if (positionInvalidMessage != null) {
			String message = String.format("Move for %s is not valid. %s ", scannable.getName(), positionInvalidMessage);
			throw new DeviceException(message);
		}
	}

	/**
	 * Check that a target position is within limits of a scannable group. A DeviceException is thrown if it's not.
	 * @param scannableGroup
	 * @param target
	 * @throws DeviceException
	 */
	private void checkPositionValid(XesSpectrometerCrystal scannableGroup, Object target) throws DeviceException {
		if (scannableGroup.isAllowedToMove()) {
			checkPositionValid((Scannable) scannableGroup, target);
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
		double yPosition = Double.parseDouble(getDetYScannable().getPosition().toString());
		double lPosition = Double.parseDouble(spectrometerX.getPosition().toString());
		// In the Rowland condition: sin(2*(90-bragg)) = y/L
		double derivedBragg = 90 - (0.5 * Math.toDegrees(Math.asin(yPosition / lPosition)));
		return derivedBragg;
	}

	private Scannable getDetXScannable() {
		return detectorGroup.getGroupMembers().get(0);
	}

	private Scannable getDetYScannable() {
		return detectorGroup.getGroupMembers().get(1);
	}

	private Scannable getDetRotScannable() {
		return detectorGroup.getGroupMembers().get(2);
	}

	@Override
	public String toFormattedString() {
		try {
			if (!doesMotorPositionAgreeWithExpectedBraggAngle()) {
				final double position = braggBasedOnDetectorRotation();
				final String formattedPosition = String.format(getOutputFormat()[0], position);
				return getName() + "\t: " + formattedPosition + " " + "deg. NB: this is derived from only the "
						+ getDetYScannable().getName() + " and " + spectrometerX.getName() + " motor positions.";
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

	public Scannable getSpectrometerX() {
		return spectrometerX;
	}

	public void setSpectrometerX(Scannable spectrometerX) {
		this.spectrometerX = spectrometerX;
	}

	public Double getTrajectoryStepSize() {
		return trajectoryStepSize;
	}

	public void setTrajectoryStepSize(Double trajectoryStepSize) {
		this.trajectoryStepSize = trajectoryStepSize;
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

	public double getHorizontalCrystalOffset() {
		return horizontalOffset;
	}

	public ScannableGroup getDetectorGroup() {
		return detectorGroup;
	}

	/**
	 * Set the ScannableGroup containing the scannables controlling the detector x, y and rotation
	 *
	 * @param detectorGroup
	 */
	public void setDetectorGroup(ScannableGroup detectorGroup) {
		this.detectorGroup = detectorGroup;
	}

	/**
	 * Set the ScannableGroup containing the {@link XesSpectrometerCrystal} objects for the crystals in the spectrometer
	 *
	 * @param crystalsGroup
	 */
	public void setCrystalsGroup(ScannableGroup crystalsGroup) {
		this.crystalsGroup = crystalsGroup;
	}

	/**
	 * @return ScannableGroup containing the {@link XesSpectrometerCrystal}s used by the Spectrometer
	 */
	public ScannableGroup getCrystalsGroup() {
		return crystalsGroup;
	}

	public ScannableGroup getCrystalsAllowedToMove() {
		return crystalsAllowedToMove;
	}

	/**
	 * Set the ScannableGroup containing EnumPositioners controlling whether each of the {@link XesSpectrometerCrystal}s
	 * for the spectrometer (set by call to {@link #setCrystalsGroup(ScannableGroup)}) is allowed to move.
	 * If this is not set, all crystals present will by moved when changing Bragg angle.
	 *
	 * @param crystalsAllowedToMove
	 */
	public void setCrystalsAllowedToMove(ScannableGroup crystalsAllowedToMove){
		this.crystalsAllowedToMove = crystalsAllowedToMove;
	}

	private List<Scannable> getScannables() {
		Scannable[] scannables = {
				getRadiusScannable(),
				getDetXScannable(), getDetYScannable(), getDetRotScannable(),
				getSpectrometerX()};

		List<Scannable> allScannables = new ArrayList<>();
		allScannables.addAll(Arrays.asList(scannables));
		crystalList.forEach(c -> allScannables.addAll(c.getGroupMembers()));

		return allScannables;
	}

	/**
	 * Generate map containing position of each scannable in the spectrometer
	 * @return map with key=scannable name, value=scannable position
	 * @throws DeviceException
	 */
	public Map<String, Double> getScannablePositions() throws DeviceException {
		Map<String, Double> map = new LinkedHashMap<>();
		for(Scannable scn : getScannables()) {
			map.put(scn.getName(), ScannableUtils.getCurrentPositionArray(scn)[0]);
		}
		return map;
	}

	/**
	 * Return a list of {@link XesSpectrometerCrystal} objects extracted from
	 * crystals ScannableGroup (set by {@link #setCrystalsGroup(ScannableGroup)}).
	 *
	 * @return Lis of XesSpecrtometerCrystal objects
	 */
	public List<XesSpectrometerCrystal> getCrystalsList() {
		return getScannablesOfTypeFromGroup(crystalsGroup, XesSpectrometerCrystal.class);
	}

	private <T> List<T> getScannablesOfTypeFromGroup(ScannableGroup scnGroup, Class<T> classType) {
		if (scnGroup == null) {
			return Collections.emptyList();
		}
		return scnGroup.getGroupMembers()
				.stream()
				.filter(classType::isInstance)
				.map(classType::cast)
				.collect(Collectors.toList());
	}
}