/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.rcp.views;

import gda.device.Scannable;

public class StageCompositeDefinition {
	Scannable scannable;
	String label=null;
	int decimalPlaces = 3;
	double stepSize=1.;
	double smallStep=1.;
	double bigStep=10.;
	boolean useSteps=false;
	boolean singleLineNudge=true;
	boolean singleLine=true;
	boolean resetToZero=false;
	public Scannable getScannable() {
		return scannable;
	}
	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}
	public int getDecimalPlaces() {
		return decimalPlaces;
	}
	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
	}
	public double getStepSize() {
		return stepSize;
	}
	public void setStepSize(double stepSize) {
		this.stepSize = stepSize;
	}
	public double getSmallStep() {
		return smallStep;
	}
	public void setSmallStep(double smallStep) {
		this.smallStep = smallStep;
	}
	public double getBigStep() {
		return bigStep;
	}
	public void setBigStep(double bigStep) {
		this.bigStep = bigStep;
	}

	public boolean isSingleLineNudge() {
		return singleLineNudge;
	}
	public void setSingleLineNudge(boolean singleLineNudge) {
		this.singleLineNudge = singleLineNudge;
	}
	public boolean isSingleLine() {
		return singleLine;
	}
	public void setSingleLine(boolean singleLine) {
		this.singleLine = singleLine;
	}
	public boolean isResetToZero() {
		return resetToZero;
	}
	public void setResetToZero(boolean resetToZero) {
		this.resetToZero = resetToZero;
	}
	public boolean isUseSteps() {
		return useSteps;
	}
	public void setUseSteps(boolean useSteps) {
		this.useSteps = useSteps;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	

}
