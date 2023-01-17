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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

	/** Set to true then xMotor of each crystal will be the absolute position */
	private boolean absoluteXPos = false;

	/** ScannableGroup containing Positioners that control which of the crystals are allowed to be moved.
	// If these are left as null allowedToMove is not modified. */
	private ScannableGroup crystalsAllowedToMove;

	private Scannable spectrometerX = null; // also known as L
	private Scannable radiusScannable;

	/** Detector y axis angle w.r.t. vertical orientation */
	private double detectorAxisAngle = 0;

	/** ScannableGroup containing the XesSpectrometerCrystal objects for the spectrometer. */
	private ScannableGroup crystalsGroup;
	/** List of spectrometer crystals extracted from crystals ScannableGroup */
	private List<XesSpectrometerCrystal> crystalList = Collections.emptyList();

	/** ScannableGroup containing the detector x, y and rotation scannables */
	private ScannableGroup detectorGroup;

	// flag to prevent the warning about the position is an estimate being sent more than once at a time
	private boolean hasGetPositionWarningBeenSent = false;

	// Y and pitch are reversed for the upper set of analysers
	private static final double[] UPPER_MULTIPLIERS = {1, -1, 1, -1};
	private static final double[] LOWER_MULTIPLIERS = {1,  1, 1,  1};

	/**
	 * Scale factors applied to the analyser x, y, yaw and pitch values :
	 * [x multiplier, y multiplier, yaw multiplier, pitch multiplier]
	 */
	private double[] positionAngleMultiplier = LOWER_MULTIPLIERS;

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

		if (spectrometerX == null && !absoluteXPos) {
			throw new FactoryException("Not using absolute X positions, but Spectrometer X position scannable has not been set");
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
			logger.info("Not updating 'allowed to move' - number of positioners does not match number of crystals ");
		}
	}

	@Override
	public void stop() throws DeviceException {
		stopCalled = true;

		ScannableGroup groupedScannables = getGroupedScannables();
		groupedScannables.stop();

		try {
			groupedScannables.waitWhileBusy();
		} catch (InterruptedException e) {
			// Reset interrupt status
			Thread.currentThread().interrupt();
			throw new DeviceException("InterruptedException while waiting for motors to stop");
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		if (isRunningTrajectoryMovement) {
			return true;
		}
		return getGroupedScannables().isBusy();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		updateActiveGroups();

		double targetBragg = extractDouble(position);
		double currentPosition = extractDouble(getPosition());
		radius = extractDouble(radiusScannable.getPosition());

		isRunningTrajectoryMovement = false;

		// Calculate detector trajectory points if doing a 'large' movement
		final List<double[]> trajectoryPoints;
		if (Math.abs(currentPosition - targetBragg) > trajectoryStepSize) {
			trajectoryPoints = getDetectorTrajectoryPoints(currentPosition, targetBragg);
		} else {
			trajectoryPoints = Collections.emptyList();
		}

		// Compute the final detector positions
		double[] finalDetectorPosition = getDetectorPosition(radius,  targetBragg);
		// Check the points are within limits
		checkPositionValid(detectorGroup, finalDetectorPosition);

		//Compute the ax, ay, pitch, yaw value for all the crystals
		Map<XesSpectrometerCrystal, double[]> crystalPositions = calculateCrystalPositions(targetBragg);
		// Check the positions are all within limits
		for(var entry : crystalPositions.entrySet()) {
			checkPositionValid(entry.getKey(), entry.getValue());
		}

		// reset the stop flag
		stopCalled = false;

		// only now store the new bragg value
		bragg = targetBragg;

		// now do moves ...
		notifyIObservers(this, ScannableStatus.BUSY);

		// the spectrometer overall X position
		if (!absoluteXPos) {
			double finalSpectrometerX = XesUtils.getL(radius, targetBragg);
			checkPositionValid(spectrometerX, finalSpectrometerX);
			spectrometerX.asynchronousMoveTo(finalSpectrometerX);
		}

		// Move the crystal motors
		for(var entry : crystalPositions.entrySet()) {
			entry.getKey().asynchronousMoveTo(entry.getValue());
		}

		// Move the detector motors :
		if (!trajectoryPoints.isEmpty()) {
			// loop over trajectory points
			isRunningTrajectoryMovement = true;
			Async.execute(() -> executeDetectorTrajectory(trajectoryPoints) );
		} else {
			// move to final position
			detectorGroup.asynchronousMoveTo(finalDetectorPosition);
		}
	}

	/**
	 * This is used externally, to return all the spectrometer motor values for a given bragg angle
	 *
	 * @param targetBragg
	 * @return map of scannale positions (key = scannable, value = position)
	 */
	public Map<Scannable, Double> getSpectrometerPositions(double targetBragg) {
		Map<Scannable, Double> positions = new LinkedHashMap<>();

		// Add position of each scannable in each XesSpectrometerCrystal...
		Map<XesSpectrometerCrystal, double[]> crystalPositions = calculateCrystalPositions(targetBragg);
		for(var entry : crystalPositions.entrySet()) {
			positions.putAll(getCrystalPositionMap(entry.getKey(), entry.getValue()));
		}

		// Add the detector positions
		double[] detectorPositions = getDetectorPosition(radius,  targetBragg);
		positions.put(getDetXScannable(), detectorPositions[0]);
		positions.put(getDetYScannable(), detectorPositions[1]);
		positions.put(getDetRotScannable(), detectorPositions[2]);

		if (!absoluteXPos) {
			positions.put(spectrometerX, XesUtils.getL(radius, targetBragg));
		}
		return positions;
	}

	/**
	 * Generate map of positions of scannables in a {@link XesSpectrometerCrystal} object
	 *
	 * @param crystal XesSpectrometerCrystal object
	 * @param position double[] of positions (x, y, rot, pitch)
	 * @return map of positions (key=scannable name, value=position)
	 */
	private Map<Scannable, Double> getCrystalPositionMap(XesSpectrometerCrystal crystal,  double[] position) {
		Map<Scannable, Double> scnPositions = new LinkedHashMap<>();
		if (position.length ==1) {
			scnPositions.put(crystal.getPitchMotor(), position[0]);
		} else {
			scnPositions.put(crystal.getxMotor(), position[0]);
			scnPositions.put(crystal.getyMotor(), position[1]);
			scnPositions.put(crystal.getRotMotor(), position[2]);
			scnPositions.put(crystal.getPitchMotor(), position[3]);
		}
		return scnPositions;
	}

	//Compute the ax, ay, pitch, yaw value for all the crystals
	private Map<XesSpectrometerCrystal, double[]> calculateCrystalPositions(double targetBragg) {
		Map<XesSpectrometerCrystal, double[]> positions = new LinkedHashMap<>();
		for(XesSpectrometerCrystal crystal : crystalList) {
			double[] crystPosition = getCrystalPositions(crystal, targetBragg);
			if (crystal.getHorizontalIndex() == 0 && !absoluteXPos) {
				// If using relative positions, only check pitch of central crystal
				double[] val = {crystPosition[3]};
				positions.put(crystal, val);
			} else {
				positions.put(crystal, crystPosition);
			}
		}
		return positions;
	}

	private double[] getCrystalPositions(XesSpectrometerCrystal crystal, double braggAngle) {
		double[] values = XesUtils.getAnalyserValues(radius, braggAngle, crystal.getHorizontalIndex()*horizontalOffset);
		if (!absoluteXPos) {
			double xpos = values[0] - XesUtils.getL(radius, braggAngle);
			values[0] = xpos;
		}
		// Apply the scale factors to x, y, yaw, pitch values
		for(int i=0; i<values.length; i++) {
			values[i] = values[i]*positionAngleMultiplier[i];
		}
		return values;
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
	 * The x and y positions will be rotated by angle {@link #detectorAxisAngle}.
	 * @param radius
	 * @param bragg
	 * @return Double[] X,Y,theta
	 */
	private double[] getXYTheta(Double radius, Double bragg ){
		double detX = XesUtils.getDx(radius, bragg);
		double detY = XesUtils.getDy(radius, bragg);
		double theta = XesUtils.getCrystalRotation(bragg);
		// transform x and y to rotated detector frame of reference :
		double[] rotVals = rotate(detectorAxisAngle, detX, detY);
		return new double[]{rotVals[0], rotVals[1], theta};
	}

	public double getDetectorAxisAngle() {
		return detectorAxisAngle;
	}

	/**
	 * Set the angle of the detector translation axis relative to the analyser coordinate system.
	 * @param detectorAxisAngle
	 */
	public void setDetectorAxisAngle(double detectorAxisAngle) {
		this.detectorAxisAngle = detectorAxisAngle;
	}

	/**
	 * Transform x, y coordinate value by applying rotation.
	 *
	 * @param angle new y axis angle relative to the vertical direction.
	 * @param x
	 * @param y
	 * @return double[] containing rotated x, y values.
	 */
	private double[] rotate(double angle, double x, double y) {
		double angleRad = Math.toRadians(angle);
		return new double[]{ x*Math.cos(angleRad) + y*Math.sin(angleRad),
						    -x*Math.sin(angleRad) + y*Math.cos(angleRad)};
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
	private void checkPositionValid(XesSpectrometerCrystal scannableGroup, double[] target) throws DeviceException {
		if (scannableGroup.isAllowedToMove()) {
			if (target.length == 1) {
				// Just check the pitch if only one position is present
				checkPositionValid(scannableGroup.getPitchMotor(), target[0]);
			} else {
				checkPositionValid((Scannable)scannableGroup, target);
			}
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		if (!doesMotorPositionAgreeWithExpectedBraggAngle()) {
			if (!hasGetPositionWarningBeenSent) {
				logger.warn("""
						{} cannot correctly determine its position: detector angle disagrees with expected position.
						Reported position is based on detector rotation.
						If you have not moved XES bragg or energy since restarting the GDA then please ignore this message.""", getName());

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
		double yPosition = extractDouble(getDetYScannable().getPosition());
		double lPosition = getCentralCrystalXPosition();
		double angleFromDetector = 90 - Math.toDegrees(Math.sin(yPosition/lPosition));
		double angleFromCryst = Math.toDegrees(Math.asin(lPosition/radius));
		logger.info("Angle from central crystal : {}, Angle from detector : {}", angleFromCryst, angleFromDetector);

		// In the Rowland condition: sin(2*(90-bragg)) = y/L
		return angleFromCryst;
	}

	/**
	 * Get the horizontal position of the central crystal
	 *
	 * @return
	 * @throws DeviceException
	 */
	private double getCentralCrystalXPosition() throws DeviceException {
		String msgStart ="Could not get X position of central crystal";
		Scannable scn;
		if (absoluteXPos) {
			scn = crystalList.stream()
					.filter(c -> c.getHorizontalIndex() == 0)
					.map(XesSpectrometerCrystal::getxMotor)
					.findFirst()
					.orElseThrow(() ->
						new DeviceException(msgStart+" - no crystal with index=0 found for "+getName()));
		} else {
			if (spectrometerX == null) {
				throw new DeviceException(msgStart+" - using relative x positions but SpectrometerX scannable has not been set");
			}
			scn = spectrometerX;
		}
		logger.debug("Getting central crystal X position from '{}' scannable", scn.getName());
		return extractDouble(scn.getPosition());
	}

	public Scannable getDetXScannable() {
		return detectorGroup.getGroupMembers().get(0);
	}

	public Scannable getDetYScannable() {
		return detectorGroup.getGroupMembers().get(1);
	}

	public Scannable getDetRotScannable() {
		return detectorGroup.getGroupMembers().get(2);
	}

	/**
	 * Extract a double value from position Object
	 * (first value is returned if there is more than one present)
	 *
	 * @param position
	 * @return double
	 */
	private Double extractDouble(Object position) {
		return ScannableUtils.objectToArray(position)[0];
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

	public void setHorizontalCrystalOffset(double horizontalOffset) {
		this.horizontalOffset = horizontalOffset;
	}

	public boolean isAbsoluteXPos() {
		return absoluteXPos;
	}

	public void setAbsoluteXPos(boolean absoluteXPos) {
		this.absoluteXPos = absoluteXPos;
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

	private ScannableGroup getGroupedScannables() throws DeviceException {
		try {
			ScannableGroup grp = new ScannableGroup();
			grp.setGroupMembers(getScannables());
			return grp;
		} catch (FactoryException e) {
			throw new DeviceException("Problem create group with all spectrometer scannables", e);
		}
	}

	/**
	 * Return a list of all the individual scannables across all groups in the spectrometer
	 *
	 * (i.e. radius, detector scannables, scannables for all crystals)
	 *
	 * @return List of scannables
	 */
	public List<Scannable> getScannables() {
		Scannable[] scannables = {
				getRadiusScannable(),
				getDetXScannable(), getDetYScannable(), getDetRotScannable()};

		List<Scannable> allScannables = new ArrayList<>();
		crystalList.forEach(c -> allScannables.addAll(c.getGroupMembers()));
		allScannables.addAll(Arrays.asList(scannables));
		if (spectrometerX != null) {
			allScannables.add(spectrometerX);
		}
		return allScannables;
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

	public void setUpperRow(boolean isUpper) {
		if (isUpper) {
			positionAngleMultiplier = UPPER_MULTIPLIERS;
		} else {
			positionAngleMultiplier = LOWER_MULTIPLIERS;
		}
	}

	public boolean isUpperRow() {
		return Arrays.equals(positionAngleMultiplier, UPPER_MULTIPLIERS);
	}

	public double[] getPositionAngleMultiplier() {
		return positionAngleMultiplier;
	}
}