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

package uk.ac.gda.ui.views.synoptic;

import gda.factory.FindableBase;

public class SynopticPerspectiveConfiguration extends FindableBase {

	private String simulatedPositionsView;
	private String spectrometerPicture;
	private String allCrystalControls;
	private String materialControls;
	private String detectorControls;

	private String calibrationControls;
	private String offsetView;

	public String getSimulatedPositionsView() {
		return simulatedPositionsView;
	}
	public void setSimulatedPositionsView(String simulatedPositionsView) {
		this.simulatedPositionsView = simulatedPositionsView;
	}
	public String getSpectrometerPicture() {
		return spectrometerPicture;
	}
	public void setSpectrometerPicture(String spectrometerPicture) {
		this.spectrometerPicture = spectrometerPicture;
	}
	public String getAllCrystalControls() {
		return allCrystalControls;
	}
	public void setAllCrystalControls(String allCrystalControls) {
		this.allCrystalControls = allCrystalControls;
	}
	public String getMaterialControls() {
		return materialControls;
	}
	public void setMaterialControls(String materialControls) {
		this.materialControls = materialControls;
	}
	public String getDetectorControls() {
		return detectorControls;
	}
	public void setDetectorControls(String detectorControls) {
		this.detectorControls = detectorControls;
	}
	public String getCalibrationControls() {
		return calibrationControls;
	}
	public void setCalibrationControls(String calibrationControls) {
		this.calibrationControls = calibrationControls;
	}
	public String getOffsetView() {
		return offsetView;
	}
	public void setOffsetView(String offsetView) {
		this.offsetView = offsetView;
	}
}
