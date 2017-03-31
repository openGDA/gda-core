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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Boolean.hashCode(stopBetweenPoints);
		result = prime * result + Boolean.hashCode(runUserScripts);
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
		if (stopBetweenPoints != other.stopBetweenPoints)
			return false;
		if (runUserScripts != other.runUserScripts)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(hashCode())
				+ " [stopBetweenPoints=" + stopBetweenPoints + ", runUserScripts=" + runUserScripts
				+ ", exposureTime=" + getExposureTime() + ", name=" + getName() + ", timeout=" + getTimeout() + "]";
	}
}
