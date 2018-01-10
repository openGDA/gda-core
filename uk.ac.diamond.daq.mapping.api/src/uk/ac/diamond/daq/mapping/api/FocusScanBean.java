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
 * <em>Note:</em>This bean has a dual purpose, besides describing a particular focus scan,
 * some fields are required to setup focus scanning in general for a beamline.
 *  TODO perhaps this bean should be split into two, one to setup focus scanning on a beamline
 *  e.g. FocusScanSetupBean, and FocusScanBean only containing the fields necessary to setup
 *  a particular scan.
 */
public class FocusScanBean {

	/**
	 * Set in spring to the name of the focus scannable, typically a zone plate, e.g. ZonePlateZ
	 * The UI does not allow this value to be changed.
	 */
	private String focusScannableName;

	/**
	 * This is the centre on the range scanned with the focus scannable,
	 * The Focus scan wizard will set this to the current position of the focus scannable.
	 * <em>Do not set this field in spring.</em>
	 */
	private double focusCentre;

	/**
	 * The size of the range to focus plus or minus the focusCentre (therefore the full range
	 * scanned is actually double this value). This should be set in spring to a default value
	 * which the user can change.
	 */
	private double focusRange;

	/**
	 *  The number of focus steps to scan along the full focus range. This should be set in spring
	 *  to the default value.
	 */
	private int numberOfFocusSteps;

	/**
	 * Describes the 2 dimensional line region to scan in the mapping stage.
	 * <em>Do not set this field in spring.</em>
	 */
	private ILineMappingRegion lineRegion;

	/**
	 * The number of points in the line to scan. This should be set in spring to a default value.
	 */
	private int numberOfLinePoints;

	/**
	 * The detector (possibly a malcolm device) that should be used to perform the scan. This field
	 * can optionally be set in spring to a default value.
	 */
	private IDetectorModel detector;

	/**
	 * The name of the malcolm device to use for focus malcolm scans. This field should be set
	 * in spring if it is required to use a particular malcolm device for focus scans. Note that
	 * this field is used by the focus scan UI to help and does form part of the definition of a focus scan, see note on class Javadoc.
	 */
	private String focusMalcolmDeviceName;

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

	public String getFocusMalcolmDeviceName() {
		return focusMalcolmDeviceName;
	}

	public void setFocusMalcolmDeviceName(String focusMalcolmDeviceName) {
		this.focusMalcolmDeviceName = focusMalcolmDeviceName;
	}

}
