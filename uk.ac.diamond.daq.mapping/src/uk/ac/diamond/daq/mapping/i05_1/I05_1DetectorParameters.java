/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.i05_1;

import org.eclipse.scanning.api.annotation.UiComesAfter;
import org.eclipse.scanning.api.annotation.UiLookup;
import org.eclipse.scanning.api.annotation.UiRequired;
import org.eclipse.scanning.api.annotation.UiSection;

public class I05_1DetectorParameters {

	String lensMode = "Angular 30";
	int passEnergy = 5;
	String acquisitionMode = "Fixed";
	double startEnergy = 34.790;
	double endEnergy = 35.210;
	double stepEnergy = 0.40298;
	double timePerStep = 1;
	int iterations = 1;
	private String detectorName = "DA30 Electron Analyser";

	public String getDetectorName() {
		return detectorName;
	}

	@UiLookup({ "Transmission", "Angular 7NF", "Angular 14", "Angular 30" })
	@UiRequired
	@UiComesAfter("detectorName")
	public String getLensMode() {
		return lensMode;
	}
	public void setLensMode(String lensMode) {
		this.lensMode = lensMode;
	}

	@UiComesAfter("lensMode")
	@UiLookup({ "1", "2", "5", "10", "20" })
	@UiRequired
	public int getPassEnergy() {
		return passEnergy;
	}
	public void setPassEnergy(int passEnergy) {
		this.passEnergy = passEnergy;
	}

	@UiComesAfter("scanMode")
	public double getStartEnergy() {
		return startEnergy;
	}
	public void setStartEnergy(double startEnergy) {
		this.startEnergy = startEnergy;
	}

	@UiComesAfter("startEnergy")
	public double getEndEnergy() {
		return endEnergy;
	}

	public void setEndEnergy(double endEnergy) {
		this.endEnergy = endEnergy;
	}

	@UiRequired
	@UiComesAfter("endEnergy")
	public double getStepEnergy() {
		return stepEnergy;
	}
	public void setStepEnergy(double stepEnergy) {
		this.stepEnergy = stepEnergy;
	}

	@UiRequired
	@UiSection("Exposure")
	@UiComesAfter("stepEnergy")
	public double getTimePerStep() {
		return timePerStep;
	}
	public void setTimePerStep(double timePerStep) {
		this.timePerStep = timePerStep;
	}

	@UiRequired
	@UiComesAfter("timePerStep")
	public int getIterations() {
		return iterations;
	}
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	@UiRequired
	@UiComesAfter("passEnergy")
	@UiSection("Energy range")
	@UiLookup({ "Fixed", "Swept" })
	public String getAcquisitionMode() {
		return acquisitionMode;
	}

	public void setAcquisitionMode(String acquisitionMode) {
		this.acquisitionMode = acquisitionMode;
	}

}
