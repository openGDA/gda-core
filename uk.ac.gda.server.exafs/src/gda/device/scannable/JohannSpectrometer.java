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

		// Move detector
		double[] detPos = getDetectorPosition(radius, targetBragg);
		logger.debug("Moving detector to : {}", Arrays.toString(detPos));
		detectorGroup.asynchronousMoveTo(detPos);

		// Move analysers (each one moves to the same set of x, y, rot, pitch positions)
		double[] analyserPos = getAnalyserPosition(radius, targetBragg);
		logger.debug("Moving analysers to : {}", Arrays.toString(analyserPos));
		for(var cryst : crystalList) {
			cryst.asynchronousMoveTo(analyserPos);
		}
	}

	@Override
	public Map<Scannable, Double> getSpectrometerPositions(double targetBragg) {
		double[] analyserPosition = getAnalyserPosition(radius, targetBragg);
		double[] detectorPosition = getDetectorPosition(radius, targetBragg);

		Map<Scannable, Double> positions = new LinkedHashMap<>();

		// x, y, rot, pitch values are the same for each analyser
		for(var entry : crystalList) {
			positions.put(entry.getxMotor(), analyserPosition[0]);
			positions.put(entry.getyMotor(), analyserPosition[1]);
			positions.put(entry.getRotMotor(), analyserPosition[2]);
			positions.put(entry.getPitchMotor(), analyserPosition[3]);
		}

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
		return new double[] {dxDy[0], dxDy[1], 0, 0};
	}

	/**
	 * Calculate detector position and orientation for given Bragg angle and Rowland circle radius
	 * (Rotation angle is w.r.t. the x axis)
	 * @param radius
	 * @param braggAngle
	 * @return x, y, rot values
	 */
	private double[] getDetectorPosition(double radius, double braggAngle)  {
		double[] analyserPos = getDxDy(radius, braggAngle);
		return new double[] {0, analyserPos[1]*2, braggAngle - 90};
	}
}