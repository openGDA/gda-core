/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.exafs.xes.IXesSpectrometerScannable;
import gda.exafs.xes.XesUtils;
import gda.factory.FactoryException;
import gda.observable.IObserver;

public abstract class XesSpectrometerScannableBase extends ScannableMotionUnitsBase implements IObserver, IXesSpectrometerScannable {

	private static final Logger logger = LoggerFactory.getLogger(XesSpectrometerScannableBase.class);

	/** ScannableGroup containing the XesSpectrometerCrystal objects for the spectrometer. */
	protected ScannableGroup crystalsGroup;

	/** List of spectrometer crystals extracted from crystals ScannableGroup */
	protected List<XesSpectrometerCrystal> crystalList = Collections.emptyList();

	/** ScannableGroup containing Positioners that control which of the crystals are allowed to be moved.
	// If these are left as null allowedToMove is not modified. */
	protected ScannableGroup crystalsAllowedToMove;

	/** ScannableGroup containing the detector x, y and rotation scannables */
	protected ScannableGroup detectorGroup;

	protected Scannable radiusScannable;

	protected double radius;

	protected double minTheta = XesUtils.MIN_THETA;
	protected double maxTheta = XesUtils.MAX_THETA;

	/**
	 * Perform a number of validation and initialisation steps, typically called during Spring configuration by 'configure' method :
	 *
	 * <li> Check that all required scannables have been set
	 * <li> Set {@link #crystalList} using scannables extracted from {@link #crystalsGroup} object.
	 * <li> Set initial rowland circle radius from current value of {@link #radiusScannable}.
	 * <li> Add {@link  #validatePosition(Object[])} as position validator to the scannable
	 *
	 * @throws FactoryException
	 */
	protected void validateAndSetup() throws FactoryException {
		Objects.requireNonNull(radiusScannable, "radius scannable has not been set");
		Objects.requireNonNull(crystalsGroup, "spectrometer crystals group has not been set");
		Objects.requireNonNull(detectorGroup, "detector scannable group has not been set");

		logger.info("Making list to spectrometer crystals");
		crystalList = getCrystalsList();
		if (crystalList.isEmpty()) {
			throw new FactoryException("No XesSpectrometerCrystals found for XesSpectrometer");
		}

		// Try to update the radius value from the radius scannable
		try {
			updateRadiusFromScannable();
		} catch(DeviceException e) {
			logger.warn("Problem setting radius value from {} : {}", radiusScannable.getName(), e);
		}

		this.inputNames = new String[] { getName() };

		// include custom validator to check angular range of demand positions
		addPositionValidator(this::validatePosition);
	}

	/**
	 * Return a list of {@link XesSpectrometerCrystal} objects extracted from
	 * crystals ScannableGroup (set by {@link #setCrystalsGroup(ScannableGroup)}).
	 *
	 * @return List of XesSpecrtometerCrystal objects
	 */
	public List<XesSpectrometerCrystal> getCrystalsList() {
		return getScannablesOfTypeFromGroup(crystalsGroup, XesSpectrometerCrystal.class);
	}

	protected <T> List<T> getScannablesOfTypeFromGroup(ScannableGroup scnGroup, Class<T> classType) {
		if (scnGroup == null) {
			return Collections.emptyList();
		}
		return scnGroup.getGroupMembers()
				.stream()
				.filter(classType::isInstance)
				.map(classType::cast)
				.toList();
	}

	@Override
	public double getRadius() {
		return radius;
	}

	@Override
	public void setRadius(double radius) {
		this.radius = radius;
	}

	protected void updateRadiusFromScannable() throws DeviceException {
		radius = extractDouble(radiusScannable.getPosition());
	}

	protected void tryToUpdateRadiusFromScannable() {
		try {
			updateRadiusFromScannable();
		} catch (DeviceException e) {
			logger.warn("Problem updating radius from {}. Using old radius value instead ({}). ", radiusScannable.getName(), radius, e);
		}
	}

	@Override
	public double getMaxTheta() {
		return maxTheta;
	}

	@Override
	public double getMinTheta() {
		return minTheta;
	}

	@Override
	public List<Scannable> getScannables() {
		Scannable[] scannables = {
				getRadiusScannable(),
				getDetXScannable(), getDetYScannable(), getDetRotScannable()};

		List<Scannable> allScannables = new ArrayList<>();
		crystalList.forEach(c -> allScannables.addAll(c.getGroupMembers()));
		allScannables.addAll(Arrays.asList(scannables));
		return allScannables;
	}

	/**
	 * Check to make sure angle position is valid. i.e. is finite and between minTheta and maxTheta
	 * (This is used {@link ScannableMotionBase#checkPositionValid(Object)} to check demand position is valid).
	 *
	 * @param position
	 * @return null if position is valid
	 * @throws DeviceException
	 */
	protected String validatePosition(Object[] position) throws DeviceException {
		double targetBragg = extractDouble(position);
		if (!Double.isFinite(targetBragg)) {
			throw new DeviceException("Move to "+targetBragg+" degrees is not allowed!");
		}
		if (targetBragg < minTheta || targetBragg > maxTheta) {
			throw new DeviceException("Move to " + targetBragg + " degrees is out of limits. Angle must be between " + minTheta + " and " + maxTheta + " degrees.");
		}
		return null;
	}

	/**
	 * Check that a target position is finite and within limits of a scannable. A DeviceException is thrown if it's not.
	 * @param scannable
	 * @param target
	 * @throws DeviceException
	 */
	protected void checkPositionValid(Scannable scannable, Object target) throws DeviceException {
		for(Double val : ScannableUtils.objectToArray(target)) {
			if (!Double.isFinite(val)) {
				throw new DeviceException("Cannot move "+scannable.getName()+" to '"+val.toString()+"' - invalid position!");
			}
		}

		String positionInvalidMessage = scannable.checkPositionValid(target);
		if (positionInvalidMessage != null) {
			String message = String.format("Move for %s is not valid. %s ", scannable.getName(), positionInvalidMessage);
			throw new DeviceException(message);
		}
	}

	/**
	 * Check that a target position is finite and within limits of a scannable group. A DeviceException is thrown if it's not.
	 * @param scannableGroup
	 * @param target
	 * @throws DeviceException
	 */
	protected void checkPositionValid(XesSpectrometerCrystal scannableGroup, double[] target) throws DeviceException {
		if (scannableGroup.isAllowedToMove()) {
			if (target.length == 1) {
				// Just check the pitch if only one position is present
				checkPositionValid(scannableGroup.getPitchMotor(), target[0]);
			} else {
				checkPositionValid((Scannable)scannableGroup, target);
			}
		}
	}


	/**
	 *
	 * @return ScannableGroup containing all scannables used by spectrometer (i.e. scannables returned by {@link #getScannables()})
	 * @throws DeviceException
	 */
	protected ScannableGroup getGroupedScannables() throws DeviceException {
		try {
			ScannableGroup grp = new ScannableGroup();
			grp.setGroupMembers(getScannables());
			return grp;
		} catch (FactoryException e) {
			throw new DeviceException("Problem create group with all spectrometer scannables", e);
		}
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
	 * Update the 'allowedToMove' flag of each {@link XesSpectrometerCrystal} in the {@link #crystalList}
	 * based on the {@link #crystalsAllowedToMove} positioners.
	 */
	protected void updateActiveGroups() {
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
		return getGroupedScannables().isBusy();
	}

	/**
	 * Extract a double value from position Object
	 * (first value is returned if there is more than one present)
	 *
	 * @param position
	 * @return double
	 */
	protected Double extractDouble(Object position) {
		return ScannableUtils.objectToArray(position)[0];
	}

	@Override
	public void update(Object source, Object arg) {
		notifyIObservers(this, ScannableStatus.BUSY);
	}

	public ScannableGroup getCrystalsGroup() {
		return crystalsGroup;
	}

	public void setCrystalsGroup(ScannableGroup crystalsGroup) {
		this.crystalsGroup = crystalsGroup;
	}

	public ScannableGroup getCrystalsAllowedToMove() {
		return crystalsAllowedToMove;
	}

	public void setCrystalsAllowedToMove(ScannableGroup crystalsAllowedToMove) {
		this.crystalsAllowedToMove = crystalsAllowedToMove;
	}

	public ScannableGroup getDetectorGroup() {
		return detectorGroup;
	}

	public void setDetectorGroup(ScannableGroup detectorGroup) {
		this.detectorGroup = detectorGroup;
	}

	public void setMinTheta(double minTheta) {
		this.minTheta = minTheta;
	}

	public void setMaxTheta(double maxTheta) {
		this.maxTheta = maxTheta;
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

	public Scannable getRadiusScannable() {
		return radiusScannable;
	}

	public void setRadiusScannable(Scannable radiusScannable) {
		this.radiusScannable = radiusScannable;
	}

}
