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

package uk.ac.gda.client.tomo.configuration.viewer;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class TomoConfigContent implements ITomoConfigContent {
	private boolean isSelectedToRun = false;
	private double sampleExposureTime;
	private double flatExposureTime;
	private double sampleDetectorDistance;
	private String sampleDescription;
	private String proposalId;
	private String configId;
	private Integer moduleNumber;
	private double energy;
	private String sampleWeight;
	private String resolution;
	private int framesPerProjection;
	private String scanMode;
	private double runTime;
	private double estEndTime;
	private double timeDivider;
	private boolean shouldDisplay = false;
	private double objectPixelSize;
	private double progress;
	private CONFIG_STATUS status = CONFIG_STATUS.NONE;

	private Set<ScanInformation> scanInformationSet = new HashSet<TomoConfigContent.ScanInformation>();

	public static class ScanInformation {
		private final int scanNumber;
		private final String startTime;
		private final String endTime;

		public ScanInformation(int scanNumber, String startTime, String endTime) {
			this.scanNumber = scanNumber;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		public int getScanNumber() {
			return scanNumber;
		}

		public String getEndTime() {
			return endTime;
		}

		public String getStartTime() {
			return startTime;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj.getClass().equals(ScanInformation.class)) {
				return ((ScanInformation) obj).getScanNumber() == scanNumber;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return scanNumber;
		}

	}

	public enum CONFIG_STATUS {
		NONE("None"), RUNNING("Running"), COMPLETE("Complete"), FAIL("Fail"), STARTING("Starting");
		private final String value;

		CONFIG_STATUS(String value) {
			this.value = value;
		}

		public static CONFIG_STATUS getConfigStatus(String status) {
			for (CONFIG_STATUS cs : values()) {
				if (cs.value.equals(status)) {
					return cs;
				}
			}
			return null;
		}
	}

	@Override
	public double getSampleExposureTime() {
		return sampleExposureTime;
	}

	public void setSampleExposureTime(double sampleExposureTime) {
		this.sampleExposureTime = sampleExposureTime;
	}

	@Override
	public double getFlatExposureTime() {
		return flatExposureTime;
	}

	public void setFlatExposureTime(double flatExposureTime) {
		this.flatExposureTime = flatExposureTime;
	}

	@Override
	public double getSampleDetectorDistance() {
		return sampleDetectorDistance;
	}

	public void setSampleDetectorDistance(double sampleDetectorDistance) {
		this.sampleDetectorDistance = sampleDetectorDistance;
	}

	@Override
	public String getSampleDescription() {
		return sampleDescription;
	}

	public void setSampleDescription(String sampleDescription) {
		this.sampleDescription = sampleDescription;
	}

	public String getProposalId() {
		return proposalId;
	}

	public void setProposalId(String proposalId) {
		this.proposalId = proposalId;
	}

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}

	public void setModuleNumber(Integer moduleNumber) {
		this.moduleNumber = moduleNumber;
	}

	public Integer getModuleNumber() {
		return moduleNumber;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getEnergy() {
		return energy;
	}

	public String getSampleWeight() {
		return sampleWeight;
	}

	public void setSampleWeight(String sampleWeight) {
		this.sampleWeight = sampleWeight;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public int getFramesPerProjection() {
		return framesPerProjection;
	}

	public void setFramesPerProjection(int framesPerProjection) {
		this.framesPerProjection = framesPerProjection;
	}

	public String getScanMode() {
		return scanMode;
	}

	public void setScanMode(String scanMode) {
		this.scanMode = scanMode;
	}

	@Override
	public double getRunTime() {
		return runTime;
	}

	public void setRunTime(double runTime) {
		this.runTime = runTime;
	}

	public double getEstEndTime() {
		return estEndTime;
	}

	public void setEstEndTime(double estEndTime) {
		this.estEndTime = estEndTime;
	}

	public double getTimeDivider() {
		return timeDivider;
	}

	public void setTimeDivider(double timeDivider) {
		this.timeDivider = timeDivider;
	}

	public boolean isShouldDisplay() {
		return shouldDisplay;
	}

	public void setShouldDisplay(boolean shouldDisplay) {
		this.shouldDisplay = shouldDisplay;
	}

	public void setModuleObjectPixelSize(double objectPixelSize) {
		this.objectPixelSize = objectPixelSize;
	}

	public double getObjectPixelSize() {
		return objectPixelSize;
	}

	@Override
	public boolean isSelectedToRun() {
		return isSelectedToRun;
	}

	public void setSelectedToRun(boolean isSelectedToRun) {
		this.isSelectedToRun = isSelectedToRun;
	}

	public CONFIG_STATUS getStatus() {
		return status;
	}

	public void setStatus(CONFIG_STATUS status) {
		this.status = status;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

	public void addScanInformation(int scanNumber, String startTime, String endTime) {
		scanInformationSet.add(new ScanInformation(scanNumber, startTime, endTime));
	}

	public Set<ScanInformation> getScanInformationList() {
		return scanInformationSet;
	}

	public int getMostRecentScanNumber() {
		ScanInformation mostRecentScanInformation = getMostRecentScanInformation();
		if (mostRecentScanInformation != null) {
			return mostRecentScanInformation.getScanNumber();
		}
		return -1;
	}

	private ScanInformation getMostRecentScanInformation() {
		ScanInformation scanInformation = null;
		int scanNumber = 0;
		for (ScanInformation sc : scanInformationSet) {
			if (sc.getScanNumber() > scanNumber) {
				scanNumber = sc.getScanNumber();
				scanInformation = sc;
			}
		}
		return scanInformation;
	}

	public String getMostRecentStartTime() {
		ScanInformation mostRecentScanInformation = getMostRecentScanInformation();
		if (mostRecentScanInformation != null) {
			return mostRecentScanInformation.getStartTime();
		}
		return null;
	}

	public String getMostRecentEndTime() {
		ScanInformation mostRecentScanInformation = getMostRecentScanInformation();
		if (mostRecentScanInformation != null) {
			return mostRecentScanInformation.getEndTime();
		}
		return null;
	}
}