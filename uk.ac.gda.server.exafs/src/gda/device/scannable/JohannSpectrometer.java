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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.FactoryException;

public class JohannSpectrometer extends XesSpectrometerScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(JohannSpectrometer.class);

	private double detectorAxisLength = 0.0;

	/** Angular separation between central and side analyser [degrees] */
	private double analyserSeparationAngle = 18;

	/** Distance from analyser rail focus to source position */
	private double sourceDisplacement = 115;

	private boolean includeYawCorrection = false;

	public JohannSpectrometer() {
		this.extraNames = new String[] {};
		this.outputFormat = new String[] { "%.4f" };
		minTheta = 65.0;
		maxTheta = 82.0;
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
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		updateActiveGroups();

		double targetBragg = extractDouble(position);
		radius = extractDouble(radiusScannable.getPosition());

		// Make sure observers are notified that a move is taking place
		notifyIObservers(this, ScannableStatus.BUSY);

		// Move detector
		double[] detPos = getDetectorMotorPositions(radius, targetBragg);
		logger.debug("Moving detector to : {}", Arrays.toString(detPos));
		detectorGroup.asynchronousMoveTo(detPos);

		// Move analysers (each one moves to the same set of x, y, rot, pitch positions)
		double[] analyserPos = getAnalyserPosition(radius, targetBragg);
		logger.debug("Moving analysers to : {}", Arrays.toString(analyserPos));
		for(var ent : getAnalyserPositionMap(targetBragg).entrySet()) {
			logger.debug("Moving {} to {}", ent.getKey().getName(),ent.getValue());
			ent.getKey().moveTo(ent.getValue());
		}
	}

	private Map<Scannable,Double> getAnalyserPositionMap(double targetBragg) {
		double[] analyserPosition = getAnalyserPosition(radius, targetBragg);

		Map<Scannable, Double> positions = new LinkedHashMap<>();
		// x, y values are the same for each analyser,
		for(var entry : crystalList) {
			positions.put(entry.getxMotor(), analyserPosition[0]);
			positions.put(entry.getyMotor(), analyserPosition[1]);

			// yaw is zero for central analyser, -ve for minus analyser, +ve for plus analyser
			if (includeYawCorrection) {
				double yaw = analyserPosition[2]*entry.getHorizontalIndex();
				positions.put(entry.getRotMotor(), yaw);
			}
		}
		return positions;
	}

	@Override
	public Map<Scannable, Double> getSpectrometerPositions(double targetBragg) {

		Map<Scannable, Double> positions = new LinkedHashMap<>();
		positions.putAll(getAnalyserPositionMap(targetBragg));

		double[] detectorPosition = getDetectorMotorPositions(radius, targetBragg);
		positions.put(getDetXScannable(), detectorPosition[0]);
		positions.put(getDetYScannable(), detectorPosition[1]);
		positions.put(getDetRotScannable(), detectorPosition[2]);

		return positions;
	}

	/**
	 * Determine Bragg angle from height of central analyser.
	 */
	@Override
	public Object rawGetPosition() throws DeviceException {
		Optional<XesSpectrometerCrystal> centreCrystal = crystalList.stream().filter(cryst -> cryst.getHorizontalIndex() == 0).findFirst();
		if (centreCrystal.isEmpty()) {
			throw new DeviceException("Could not determine height - centre analyser not found");
		}

		double height = extractDouble(centreCrystal.get().getyMotor().getPosition());
		// height = radius*sin(2*braggAngle)*0.5
		double sinTwoTheta = height*2/radius;

		// Check value is not too large
		if (Math.abs(sinTwoTheta) > 1) {
			throw new DeviceException("Cannot compute bragg angle for height "+height+". Value should be <0.5 Rowland circle radius ("+radius*0.5+")");

		}
		double twoTheta = Math.toDegrees(Math.asin(sinTwoTheta));
		// Ensure Bragg angle is > 45 degrees
		return 90-twoTheta*0.5;
	}

	/**
	 * Calculate analyser position relative to the source for given Bragg angle and Rowland circle radius
	 *
	 * @param radius
	 * @param braggAngle
	 * @return
	 */
	private double[] getDxDy(double radius, double braggAngle) {
		double braggRadian = Math.toRadians(braggAngle);
		return new double[] { radius * Math.sin(braggRadian) * Math.sin(braggRadian),
				radius * Math.sin(braggRadian) * Math.cos(braggRadian)};
	}

	/**
	 * Calculate analyser position and orientation for given Bragg angle and Rowland circle radius
	 * @param radius
	 * @param braggAngle
	 * @return x, y, rot, pitch values
	 */
	private double[] getAnalyserPosition(double radius, double braggAngle) {
		double[] dxDy = getDxDy(radius, braggAngle);
		return new double[] {dxDy[0], dxDy[1], getYawAngle(dxDy[0]), 0};
	}

	/**
	 * Calculate detector position and orientation for given Bragg angle and Rowland circle radius
	 * (Rotation angle is w.r.t. the x axis)
	 * @param radius
	 * @param braggAngle
	 * @return x, y, rot values
	 */
	private double[] getDetectorMotorPositions(double radius, double braggAngle)  {
		double[] analyserPos = getAnalyserPosition(radius, braggAngle);
		double[] detectorHeadPosition = {0, analyserPos[1]*2, braggAngle - 90};
		double[] axisPosition = getDetectorAxisPosition(detectorAxisLength, braggAngle);
		logger.debug("Angle = {} deg, detector position = {}, detector axis position = {}", braggAngle, Arrays.toString(analyserPos), Arrays.toString(axisPosition));
		return new double [] {detectorHeadPosition[0] + axisPosition[0], detectorHeadPosition[1] + axisPosition[1], detectorHeadPosition[2]};
	}

	/**
	 * Position of detector rotation axis relative to the detector position.
	 * +ve direction of position is away from analyser (i.e. 'behind' the sample position)
	 * @param axisLength
	 * @param braggAngle
	 * @return rotation axis position relative to detector position - x, y
	 */
	public double[] getDetectorAxisPosition(double axisLength, double braggAngle) {
		double braggRadian = Math.toRadians(braggAngle);
		return new double[] {axisLength*Math.sin(braggRadian), axisLength*Math.cos(braggRadian)};
	}

	public double getDetectorAxisLength() {
		return detectorAxisLength;
	}

	public void setDetectorAxisLength(double detectorAxisLength) {
		this.detectorAxisLength = detectorAxisLength;
	}

	/**
	 * Yaw angle so analyser points towards source
	 *
	 * @param xPosition distance of analyser from source (i.e. dx position returned {@link #getDxDy(double, double)})
	 * @return
	 */
	private double getYawAngle(double xPosition) {
		double sinYaw = sourceDisplacement*Math.sin(Math.toRadians(analyserSeparationAngle))/xPosition;
		return Math.toDegrees(Math.asin(sinYaw));
	}

	public double getAnalyserSeparationAngle() {
		return analyserSeparationAngle;
	}


	public void setAnalyserSeparationAngle(double analyserSeparationAngle) {
		this.analyserSeparationAngle = analyserSeparationAngle;
	}


	public double getSourceDisplacement() {
		return sourceDisplacement;
	}


	public void setSourceDisplacement(double sourceDisplacement) {
		this.sourceDisplacement = sourceDisplacement;
	}


	public boolean isIncludeYawCorrection() {
		return includeYawCorrection;
	}


	public void setIncludeYawCorrection(boolean includeYawCorrection) {
		this.includeYawCorrection = includeYawCorrection;
	}
}