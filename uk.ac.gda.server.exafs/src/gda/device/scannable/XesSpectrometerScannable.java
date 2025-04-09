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
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.exafs.xes.IXesSpectrometerScannable;
import gda.exafs.xes.XesUtils;
import gda.factory.FactoryException;
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
@ServiceInterface(IXesSpectrometerScannable.class)
public class XesSpectrometerScannable extends XesSpectrometerScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(XesSpectrometerScannable.class);

	private volatile boolean stopCalled = false;
	private volatile boolean isRunningTrajectoryMovement = false;

	private double bragg = 80;
	private double trajectoryStepSize = 0.02; // the size of each bragg angle step when moving the detector.

	private double horizontalOffset = 137.0;

	/** Detector y axis angle w.r.t. vertical orientation */
	private double detectorAxisAngle = 0;

	/** Map to convert from spectrometer crystal group, to scannable group that uses DirectDemand value in motor record */
	private Map<Scannable, Scannable> directDemandScannablesMap = Collections.emptyMap();

	/** ScannableGroup that controls the deferred move start/stop PVs */
	private ScannableGroup deferredMoveStartStopGroup;

	/** Set to 'true' to use deferred moves when moving analyser motors */
	private boolean useDeferredMove;

	// flag to prevent the warning about the position is an estimate being sent more than once at a time
	private boolean hasGetPositionWarningBeenSent = false;

	// Y and pitch are reversed for the lower set of analysers
	private static final double[] UPPER_MULTIPLIERS = {1,  1,  1,  1};
	private static final double[] LOWER_MULTIPLIERS = {1, -1,  1, -1};

	/**
	 * Scale factors applied to the analyser x, y, yaw and pitch values :
	 * [x multiplier, y multiplier, yaw multiplier, pitch multiplier]
	 */
	private double[] positionAngleMultiplier = LOWER_MULTIPLIERS;

	/**
	 * Precisions to be applied to analyser x, y, yaw and pitch values before they are applied to the motors
	 * (e.g. 0.01 = round values to nearest 0.01)
	 */
	private double[] analyserDemandPrecision = {0, 0, 0.01, 0};

	/**
	 * Tolerance used to determine whether an analyser is at a demand position :
	 * Before moving analyser check if the current motor x, y, yaw, pitch values are within tolerance of demand position.
	 * Analyser is only moved if any position is not within the tolerance.
	 */
	private double[] analyserPositionTolerance = {0.001, 0.001, 0.001, 0.001};

	/**
	 * Precisions to be applied to detector x, y, angle values before they are applied to the motors
	 * (e.g. 0.01 = round values to nearest 0.01)
	 */
	private double[] detectorDemandPrecision = {0, 0, 0};

	/**
	 * Tolerance used to determine whether if the detector is at a demand position :
	 * Before moving analyser check if current motor x, y and angle values are within tolerance of demand position.
	 * Detector is only moved if any position is not within the tolerance.
	 */
	private double[] detectorPositionTolerance = {0.001, 0.001, 0.001};

	public XesSpectrometerScannable() {
		this.extraNames = new String[] {};
		this.outputFormat = new String[] { "%.4f" };
		minTheta = XesUtils.MIN_THETA;
		maxTheta = XesUtils.MAX_THETA;
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}

		validateAndSetup();

		setConfigured(true);
	}

	@Override
	public void reconfigure() throws FactoryException {
		setConfigured(false);
		configure();
	}

	@Override
	public void stop() throws DeviceException {
		stopCalled = true;
		super.stop();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		if (isRunningTrajectoryMovement) {
			return true;
		}
		return super.isBusy();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		updateActiveGroups();

		double targetBragg = extractDouble(position);
		double currentPosition = extractDouble(getPosition());
		updateRadiusFromScannable();

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

		double[] roundedDetectorPosition = roundPositionValues(finalDetectorPosition, detectorDemandPrecision);
		boolean detectorPositionsWithinTolerance = positionsWithinTolerance(detectorGroup, roundedDetectorPosition, detectorPositionTolerance);

		logger.debug("Detector demand value precisions (x, y, pitch) : {}", Arrays.toString(detectorPositionTolerance));
		logger.debug("Detector to be moved : {}", !detectorPositionsWithinTolerance);

		// Compute the (rounded) demand ax, ay, pitch, yaw values for all the analysers
		// Only analysers that need to be moved are included (i.e. those not already at the correct position for the Bragg angle).
		Map<XesSpectrometerCrystal, double[]> analyserPositions = getAnalyserlDemandPositions(targetBragg);

		logger.debug("Analyser demand value precisions (x, y, yaw, pitch) : {}", Arrays.toString(analyserDemandPrecision));
		logger.debug("Analysers to be moved : {}", analyserPositions.keySet().stream().map(Scannable::getName).toList());

		// reset the stop flag
		stopCalled = false;

		// only now store the new bragg value
		bragg = targetBragg;

		// now do moves ...
		notifyIObservers(this, ScannableStatus.BUSY);

		// If using deferred move, check appropriate scannables have been set up
		// and set the 'defer move' PVs to 'on'
		if (useDeferredMove && !analyserPositions.isEmpty()) {
			validateDeferredMoveScannables();
			setDeferredMoveFlagOn(true);
		}

		// Move the crystal motors
		for(var entry : analyserPositions.entrySet()) {

			Scannable crystalToMove = entry.getKey();

			// Get the Scannable to use for doing deferred move (demand value is set on the :DirectDemand PV in the motor record)
			if (useDeferredMove) {
				crystalToMove = directDemandScannablesMap.get(entry.getKey());
			}

			logger.trace("Moving {} to {}", crystalToMove.getName(), Arrays.toString(entry.getValue()));

			crystalToMove.asynchronousMoveTo(entry.getValue());
		}

		// Set 'defer move' PVs to 'off' to start the move.
		if (useDeferredMove && !analyserPositions.isEmpty()) {
			setDeferredMoveFlagOn(false);
		}

		// Move the detector motors :
		if (!trajectoryPoints.isEmpty()) {
			// loop over trajectory points
			isRunningTrajectoryMovement = true;
			Async.execute(() -> executeDetectorTrajectory(trajectoryPoints) );
		} else {
			// move to final position
			if (!detectorPositionsWithinTolerance) {
				logger.trace("Moving {} to {}", detectorGroup.getName(), Arrays.toString(roundedDetectorPosition));
				detectorGroup.asynchronousMoveTo(roundedDetectorPosition);
			}
		}
	}

	/**
	 * Check to see if the current position of a scannable is within given tolerance of
	 * required position. i.e. return true if : abs(current value - required value) < tolerance
	 * for each value in the scannable position array
	 *
	 * @param scn - scannable returning an array of positions
	 * @param requiredPositions array of required positions
	 * @param tolerances array containing tolerance for each position
	 * @return true if all current positions the scannable are within tolerance of required value
	 * @throws DeviceException
	 */
	boolean positionsWithinTolerance(Scannable scn, double[] requiredPositions, double[] tolerances) throws DeviceException {
		Double[] currentPositions = ScannableUtils.objectToArray(scn.getPosition());
		return IntStream.range(0, currentPositions.length)
				.allMatch(i -> Math.abs(currentPositions[i]-requiredPositions[i]) < tolerances[i]);
	}

	/**
	 * Set the 'defer moves' PV to on or off
	 *
	 * @param deferOn
	 * @throws DeviceException
	 */
	private void setDeferredMoveFlagOn(boolean deferOn) throws DeviceException {
		int[] demandPos = new int[deferredMoveStartStopGroup.getGroupMembers().size()];
		Arrays.fill(demandPos, deferOn ? 1 : 0);
		logger.debug("Setting 'defer move' to : {}", deferOn ? "On" : "Off");
		deferredMoveStartStopGroup.moveTo(demandPos);
	}

	/**
	 * Check scannables for doing deferred move have been set up correctly.
	 * i.e. the {@link #deferredMoveStartStopGroup} has been set, and that the {@link #directDemandScannablesMap}
	 * has an entry for each XesSpectrometerScannable in the crystals list.
	 *
	 * Throw a DeviceException if something is not right
	 *
	 * @throws DeviceException
	 */
	private void validateDeferredMoveScannables() throws DeviceException {
		if (deferredMoveStartStopGroup == null) {
			throw new DeviceException("Deferred move start/stop scannable has not been set");
		}
		for(var crystalScn : getCrystalsList()) {
			if (!directDemandScannablesMap.containsKey(crystalScn)) {
				throw new DeviceException("Cannot find 'direct demand' scannable to use for "+crystalScn.getName());
			}
		}
	}

	/**
	 * Limit the precision of an array of values by rounding to
	 * nearest values in {@link #precision} using :
	 * <p>
	 * rounded value = math.round(position[i]/precision[i])*precision[i].
	 * <p>
	 *
	 * e.g. if precision = 0.01 and position = 10.12157 -> rounded value = 10.12
	 *
	 * @param positions array of values
	 * @return precision array of precisions to be applied when rounding
	 */
	private double[] roundPositionValues(double[] positions, double[] precisions) {
		double[] newValues = new double[positions.length];
		for(int i=0; i<positions.length; i++) {
			if (precisions.length > i && precisions[i] > 0) {
				newValues[i] = Math.round(positions[i]/precisions[i])*precisions[i];
			} else {
				newValues[i] = positions[i];
			}
		}
		return newValues;
	}

	/**
	 * This is used externally, to return all the spectrometer motor values for a given bragg angle
	 *
	 * @param targetBragg
	 * @return map of scannable positions (key = scannable, value = position)
	 */
	@Override
	public Map<Scannable, Double> getSpectrometerPositions(double targetBragg) {
		Map<Scannable, Double> positions = new LinkedHashMap<>();

		tryToUpdateRadiusFromScannable();

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

	/**
	 * Build map of demand positions of all the analysers for given target Bragg angle :
	 * <li> demand position for each analyser is checked to make sure demand positions
	 * are within motor limits (using {@link #checkPositionValid(XesSpectrometerCrystal, double[])}).
	 * <li> Rounded demand position is computed, and compared with current analyser motor positions
	 * <li> Only analyser with motor positions that differ by more than {@link #analyserDemandPrecision} are added to the map
	 * <br>
 	 * Note : the map will be empty if all the crystals are already in the correct position.
	 *
	 * @param targetBragg
	 * @return Map with key=XesSpectrometerCrystal and value = rounded demand position, containing only those analysers
	 * that are not within tolerance of the required position.
	 * @throws DeviceException
	 */
	private Map<XesSpectrometerCrystal, double[]> getAnalyserlDemandPositions(double targetBragg) throws DeviceException {
		Map<XesSpectrometerCrystal, double[]> positions = new LinkedHashMap<>();
		for(var entry : calculateCrystalPositions(targetBragg).entrySet() ) {
			checkPositionValid(entry.getKey(), entry.getValue());

			// generate rounded demand positions
			double[] demandPositions = roundPositionValues(entry.getValue(), analyserDemandPrecision);

			// see if all the current positions are within tolerance of demand value
			boolean positionsWithinTolerance = positionsWithinTolerance(entry.getKey(), demandPositions, analyserPositionTolerance);
			if (entry.getKey().isAllowedToMove() && !positionsWithinTolerance) {
				positions.put(entry.getKey(), demandPositions);
			}
		}
		return positions;
	}

	//Compute the ax, ay, pitch, yaw value for all the crystals
	private Map<XesSpectrometerCrystal, double[]> calculateCrystalPositions(double targetBragg) {
		Map<XesSpectrometerCrystal, double[]> positions = new LinkedHashMap<>();
		crystalList.forEach(crystal -> positions.put(crystal, getCrystalPositions(crystal, targetBragg)));
		return positions;
	}

	private double[] getCrystalPositions(XesSpectrometerCrystal crystal, double braggAngle) {
		double[] values = XesUtils.getAnalyserValues(radius, braggAngle, crystal.getHorizontalIndex()*horizontalOffset);
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
			// Generate the rounded trajectory values
			List<double[]> roundedValues = trajectoryPoints.stream()
					.map(points -> roundPositionValues(points, detectorDemandPrecision))
					.toList();

			logger.info("Starting detector move along trajectory...");
			for (double[] position : roundedValues) {
				if (stopCalled) {
					return;
				}
				detectorGroup.waitWhileBusy();

				if (stopCalled) {
					return;
				}
				logger.debug("Moving detector to : {}", Arrays.toString(position));
				detectorGroup.asynchronousMoveTo(position);
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
	 *
	 * @param radius
	 * @param braggAngle
	 * @return Double [] X, Y, 2*theta
	 */
	private double[] getDetectorPosition(double radius, double braggAngle) {
		double[] xytheta = getXYTheta(radius, braggAngle);

		// Multiply x,y and angle by the x, y, pitch multipliers :
		xytheta[0] *= positionAngleMultiplier[0];
		xytheta[1] *= positionAngleMultiplier[1];
		xytheta[2] *= positionAngleMultiplier[3];

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

	/**
	 * Determine bragg angle from current rotation of detector :
	 * Detector angle = 2*(90 - braggAngle).
	 * @return
	 * @throws NumberFormatException
	 * @throws DeviceException
	 */
	private double braggBasedOnDetectorRotation() throws NumberFormatException, DeviceException {
		double detRotation = extractDouble(getDetRotScannable().getPosition());
		double braggFromDetAngle = 90 - Math.abs(detRotation*0.5);
		logger.debug("Bragg angle from detector angle : {}", braggFromDetAngle);
		return braggFromDetAngle;
	}

	@Override
	public String toFormattedString() {
		try {
			if (!doesMotorPositionAgreeWithExpectedBraggAngle()) {
				final double position = braggBasedOnDetectorRotation();
				final String formattedPosition = String.format(getOutputFormat()[0], position);
				return getName() + "\t: " + formattedPosition + " " + "deg. NB: this is derived from only the "
						+ getDetRotScannable().getName() + " motor angle.";

			} else {
				return super.toFormattedString();
			}
		} catch (Exception e) {
			logger.warn("Exception while deriving the {} position", getName(), e);
			return valueUnavailableString();
		}
	}

	public Double getTrajectoryStepSize() {
		return trajectoryStepSize;
	}

	public void setTrajectoryStepSize(Double trajectoryStepSize) {
		this.trajectoryStepSize = trajectoryStepSize;
	}

	public double getHorizontalCrystalOffset() {
		return horizontalOffset;
	}

	public void setHorizontalCrystalOffset(double horizontalOffset) {
		this.horizontalOffset = horizontalOffset;
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

	public double[] getAnalyserDemandPrecision() {
		return analyserDemandPrecision;
	}

	public void setAnalyserDemandPrecision(double[] analyserDemandPrecisions) {
		this.analyserDemandPrecision = analyserDemandPrecisions;
	}

	public double[] getAnalyserPositionTolerance() {
		return analyserPositionTolerance;
	}

	public void setAnalyserPositionTolerance(double[] analyserPositionTolerance) {
		this.analyserPositionTolerance = analyserPositionTolerance;
	}

	public double[] getDetectorDemandPrecision() {
		return detectorDemandPrecision;
	}

	public void setDetectorDemandPrecision(double[] detectorDemandPrecision) {
		this.detectorDemandPrecision = detectorDemandPrecision;
	}

	public double[] getDetectorPositionTolerance() {
		return detectorPositionTolerance;
	}

	public void setDetectorPositionTolerance(double[] detectorPositionTolerance) {
		this.detectorPositionTolerance = detectorPositionTolerance;
	}

	public Map<Scannable, Scannable> getDirectDemandScannablesMap() {
		return directDemandScannablesMap;
	}

	public void setDirectDemandScannablesMap(Map<Scannable, Scannable> directDemandScannablesMap) {
		this.directDemandScannablesMap = directDemandScannablesMap;
	}

	public ScannableGroup getDeferredMoveStartStopGroup() {
		return deferredMoveStartStopGroup;
	}

	public void setDeferredMoveStartStopGroup(ScannableGroup deferredMoveStartStopGroup) {
		this.deferredMoveStartStopGroup = deferredMoveStartStopGroup;
	}

	public boolean isUseDeferredMove() {
		return useDeferredMove;
	}

	public void setUseDeferredMove(boolean useDeferredMove) {
		this.useDeferredMove = useDeferredMove;
	}

}
