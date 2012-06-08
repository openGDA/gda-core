/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.analyser;

import java.io.Serializable;

/**
 * EpicsMCACalibration Class
 */
public class EpicsMCACalibration implements Serializable {

	private String engineeringUnits;

	private float calibrationOffset;

	private float calibrationSlope;

	private float calibrationQuadratic;

	private float twoThetaAngle;

	/**
	 * Constructor.
	 * 
	 * @param egu
	 * @param calo
	 * @param cals
	 * @param calq
	 * @param tth
	 */
	public EpicsMCACalibration(String egu, float calo, float cals, float calq, float tth) {
		engineeringUnits = egu;
		calibrationOffset = calo;
		calibrationSlope = cals;
		calibrationQuadratic = calq;
		twoThetaAngle = tth;
	}

	/**
	 * @return engineeringUnits
	 */
	public String getEngineeringUnits() {
		return engineeringUnits;
	}

	/**
	 * @param engineeringUnits
	 */
	public void setEngineeringUnits(String engineeringUnits) {
		this.engineeringUnits = engineeringUnits;
	}

	/**
	 * @return calibrationOffset
	 */
	public float getCalibrationOffset() {
		return calibrationOffset;
	}

	/**
	 * @param calibrationOffset
	 */
	public void setCalibrationOffset(float calibrationOffset) {
		this.calibrationOffset = calibrationOffset;
	}

	/**
	 * @return calibrationQuadratic
	 */
	public float getCalibrationQuadratic() {
		return calibrationQuadratic;
	}

	/**
	 * @param calibrationQuadratic
	 */
	public void setCalibrationQuadratic(float calibrationQuadratic) {
		this.calibrationQuadratic = calibrationQuadratic;
	}

	/**
	 * @return calibrationSlope
	 */
	public float getCalibrationSlope() {
		return calibrationSlope;
	}

	/**
	 * @param calibrationSlope
	 */
	public void setCalibrationSlope(float calibrationSlope) {
		this.calibrationSlope = calibrationSlope;
	}

	/**
	 * @return twoThetaAngle
	 */
	public float getTwoThetaAngle() {
		return twoThetaAngle;
	}

	/**
	 * @param twoThetaAngle
	 */
	public void setTwoThetaAngle(float twoThetaAngle) {
		this.twoThetaAngle = twoThetaAngle;
	}
}
