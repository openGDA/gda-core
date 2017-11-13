/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

import org.eclipse.scanning.api.device.models.IDetectorModel;

/**
 * A bean representing a focus scan. The following fields should be configured in spring:
 * <ul>
 *   <li><em>focusScannableName</em>: The name of the focus scannable (normally a zone plate);</li>
 *   <li><em>Number of focus steps</em>: The default number of focus steps to take in the scan;</li>
 *   <li><em>Number of line points</em>: The default number of points in the line to scan. The
 *      line is scanned at each focus step.</li>
 * </ul>
 *
 * In particular focusScannableName must be set as it cannot be set in the user interface.
 */
public class FocusScanBean {

	private String focusScannableName;

	private double focusCentre;

	private double focusRange;

	private int numberOfFocusSteps;

	private ILineMappingRegion lineRegion;

	private int numberOfLinePoints;

	private IDetectorModel detector;

	public String getFocusScannableName() {
		return focusScannableName;
	}

	public void setFocusScannableName(String focusScannableName) {
		this.focusScannableName = focusScannableName;
	}

	public double getFocusCentre() {
		return focusCentre;
	}

	public void setFocusCentre(double focusCentre) {
		this.focusCentre = focusCentre;
	}

	public double getFocusRange() {
		return focusRange;
	}

	public void setFocusRange(double focusRange) {
		this.focusRange = focusRange;
	}

	public int getNumberOfFocusSteps() {
		return numberOfFocusSteps;
	}

	public void setNumberOfFocusSteps(int numberOfFocusSteps) {
		this.numberOfFocusSteps = numberOfFocusSteps;
	}

	public int getNumberOfLinePoints() {
		return numberOfLinePoints;
	}

	public void setNumberOfLinePoints(int numberOfLinePoints) {
		this.numberOfLinePoints = numberOfLinePoints;
	}

	public ILineMappingRegion getLineRegion() {
		return lineRegion;
	}

	public void setLineRegion(ILineMappingRegion lineRegion) {
		this.lineRegion = lineRegion;
	}

	public IDetectorModel getDetector() {
		return detector;
	}

	public void setDetector(IDetectorModel detector) {
		this.detector = detector;
	}

}
