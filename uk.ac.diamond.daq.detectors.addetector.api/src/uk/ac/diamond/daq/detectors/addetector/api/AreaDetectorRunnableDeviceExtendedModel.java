/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.detectors.addetector.api;

public class AreaDetectorRunnableDeviceExtendedModel extends AreaDetectorWritingFilesRunnableDeviceModel {
	private boolean    stopBetweenPoints;
	private boolean    runUserScripts;
	private boolean    darkAuto;
	private int        darkFrequency;
	private float      darkElapseTimeS;
	private float      darkMinRelaxTimeS;

	public boolean isStopBetweenPoints() {
		return stopBetweenPoints;
	}

	public void setStopBetweenPoints(boolean stopBetweenPoints) {
		this.stopBetweenPoints = stopBetweenPoints;
	}

	public boolean isRunUserScripts() {
		return runUserScripts;
	}

	public void setRunUserScripts(boolean runUserScripts) {
		this.runUserScripts = runUserScripts;
	}

	public boolean isDarkAuto() {
		return darkAuto;
	}

	public void setDarkAuto(boolean darkAuto) {
		this.darkAuto = darkAuto;
	}

	public int getDarkFrequency() {
		return darkFrequency;
	}

	public void setDarkFrequency(int darkFrequency) {
		this.darkFrequency = darkFrequency;
	}

	public float getDarkElapseTimeS() {
		return darkElapseTimeS;
	}

	public void setDarkElapseTimeS(float darkElapseTimeS) {
		this.darkElapseTimeS = darkElapseTimeS;
	}

	public float getDarkMinRelaxTimeS() {
		return darkMinRelaxTimeS;
	}

	public void setDarkMinRelaxTimeS(float darkMinRelaxTimeS) {
		this.darkMinRelaxTimeS = darkMinRelaxTimeS;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (darkAuto ? 1231 : 1237);
		result = prime * result + Float.floatToIntBits(darkElapseTimeS);
		result = prime * result + darkFrequency;
		result = prime * result + Float.floatToIntBits(darkMinRelaxTimeS);
		result = prime * result + (runUserScripts ? 1231 : 1237);
		result = prime * result + (stopBetweenPoints ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AreaDetectorRunnableDeviceExtendedModel other = (AreaDetectorRunnableDeviceExtendedModel) obj;
		if (darkAuto != other.darkAuto)
			return false;
		if (Float.floatToIntBits(darkElapseTimeS) != Float.floatToIntBits(other.darkElapseTimeS))
			return false;
		if (darkFrequency != other.darkFrequency)
			return false;
		if (Float.floatToIntBits(darkMinRelaxTimeS) != Float.floatToIntBits(other.darkMinRelaxTimeS))
			return false;
		if (runUserScripts != other.runUserScripts)
			return false;
		if (stopBetweenPoints != other.stopBetweenPoints)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(hashCode())
				+ " [stopBetweenPoints=" + stopBetweenPoints + ", runUserScripts=" + runUserScripts
				+ ", darkAuto=" + darkAuto + ", darkFrequency=" + darkFrequency
				+ ", darkElapseTimeS=" + darkElapseTimeS + ", darkMinRelaxTimeS=" + darkMinRelaxTimeS
				+ ", exposureTime=" + getExposureTime() + ", name=" + getName() + ", timeout=" + getTimeout() + "]";
	}
}
