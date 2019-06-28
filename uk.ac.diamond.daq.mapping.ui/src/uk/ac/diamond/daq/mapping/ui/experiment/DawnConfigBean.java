/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

public class DawnConfigBean {

	private static final String appName = "dawn";
	private String processingFile;
	private String detectorName;
	private String xmx = "1024m";
	private int numberOfCores = 1;
	private boolean monitorForOverwrite = false;

	public String getProcessingFile() {
		return processingFile;
	}

	public void setProcessingFile(String processingFile) {
		this.processingFile = processingFile;
	}

	public String getDetectorName() {
		return detectorName;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	public String getXmx() {
		return xmx;
	}

	public void setXmx(String xmx) {
		this.xmx = xmx;
	}

	public int getNumberOfCores() {
		return numberOfCores;
	}

	public void setNumberOfCores(int numberOfCores) {
		this.numberOfCores = numberOfCores;
	}

	public boolean isMonitorForOverwrite() {
		return monitorForOverwrite;
	}

	public void setMonitorForOverwrite(boolean monitorForOverwrite) {
		this.monitorForOverwrite = monitorForOverwrite;
	}

	public static String getAppname() {
		return appName;
	}
}
