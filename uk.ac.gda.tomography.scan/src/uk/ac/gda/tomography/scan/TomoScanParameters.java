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

package uk.ac.gda.tomography.scan;

public class TomoScanParameters {

	// Scan parameters

	private String title = "Unknown";
	private double exposureTime = 1.0;
	private double minI = -1.0;

	private double inBeamPosition = 0.0;
	private double outOfBeamPosition = 0.0;

	private double start = 0.0;
	private double stop = 180.0;
	private double step = 0.1;

	private int imagesPerDark = 1;
	private int darkFieldInterval = 0;
	private int imagesPerFlat = 1;
	private int flatFieldInterval = 0;

	private boolean flyScan = false;
	private boolean extraFlatsAtEnd = false;

	private int numFlyScans = 1;
	private double flyScanDelay = 0.0;

	private boolean closeShutterAfterLastScan = false;

	// Device setup

	private String rotationStage = "";
	private String linearStage = "";

	private boolean sendDataToTempDirectory = false;

	// Reconstruction

	private String detectorToSampleDistance = "";
	private String detectorToSampleDistanceUnits = "";
	private String xPixelSize = "";
	private String xPixelSizeUnits = "";
	private String yPixelSize = "";
	private String yPixelSizeUnits = "";
	private String approxCentreOfRotation = "";


	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public double getExposureTime() {
		return exposureTime;
	}
	public void setExposureTime(double exposure) {
		this.exposureTime = exposure;
	}
	public double getMinI() {
		return minI;
	}
	public void setMinI(double minI) {
		this.minI = minI;
	}
	public double getInBeamPosition() {
		return inBeamPosition;
	}
	public void setInBeamPosition(double inBeamX) {
		this.inBeamPosition = inBeamX;
	}
	public double getOutOfBeamPosition() {
		return outOfBeamPosition;
	}
	public void setOutOfBeamPosition(double outOfBeamX) {
		this.outOfBeamPosition = outOfBeamX;
	}
	public double getStart() {
		return start;
	}
	public void setStart(double start) {
		this.start = start;
	}
	public double getStop() {
		return stop;
	}
	public void setStop(double stop) {
		this.stop = stop;
	}
	public double getStep() {
		return step;
	}
	public void setStep(double step) {
		this.step = step;
	}
	public int getImagesPerDark() {
		return imagesPerDark;
	}
	public void setImagesPerDark(int imagesPerDark) {
		this.imagesPerDark = imagesPerDark;
	}
	public int getDarkFieldInterval() {
		return darkFieldInterval;
	}
	public void setDarkFieldInterval(int darkFieldInterval) {
		this.darkFieldInterval = darkFieldInterval;
	}
	public int getImagesPerFlat() {
		return imagesPerFlat;
	}
	public void setImagesPerFlat(int imagesPerFlat) {
		this.imagesPerFlat = imagesPerFlat;
	}
	public int getFlatFieldInterval() {
		return flatFieldInterval;
	}
	public void setFlatFieldInterval(int flatFieldInterval) {
		this.flatFieldInterval = flatFieldInterval;
	}
	public boolean isFlyScan() {
		return flyScan;
	}
	public void setFlyScan(boolean flyScan) {
		this.flyScan = flyScan;
	}
	public boolean isExtraFlatsAtEnd() {
		return extraFlatsAtEnd;
	}
	public void setExtraFlatsAtEnd(boolean extraFlatsAtEnd) {
		this.extraFlatsAtEnd = extraFlatsAtEnd;
	}
	public int getNumFlyScans() {
		return numFlyScans;
	}
	public void setNumFlyScans(int numFlyScans) {
		this.numFlyScans = numFlyScans;
	}
	public double getFlyScanDelay() {
		return flyScanDelay;
	}
	public void setFlyScanDelay(double flyScanDelay) {
		this.flyScanDelay = flyScanDelay;
	}
	public boolean isCloseShutterAfterLastScan() {
		return closeShutterAfterLastScan;
	}
	public void setCloseShutterAfterLastScan(boolean closeShutterAfterLastScan) {
		this.closeShutterAfterLastScan = closeShutterAfterLastScan;
	}
	public String getRotationStage() {
		return rotationStage;
	}
	public void setRotationStage(String rotationStage) {
		this.rotationStage = rotationStage;
	}
	public String getLinearStage() {
		return linearStage;
	}
	public void setLinearStage(String linearStage) {
		this.linearStage = linearStage;
	}
	public boolean isSendDataToTempDirectory() {
		return sendDataToTempDirectory;
	}
	public void setSendDataToTempDirectory(boolean sendDataToTempDirectory) {
		this.sendDataToTempDirectory = sendDataToTempDirectory;
	}
	public String getDetectorToSampleDistance() {
		return detectorToSampleDistance;
	}
	public void setDetectorToSampleDistance(String detectorToSampleDistance) {
		this.detectorToSampleDistance = detectorToSampleDistance;
	}
	public String getDetectorToSampleDistanceUnits() {
		return detectorToSampleDistanceUnits;
	}
	public void setDetectorToSampleDistanceUnits(String detectorToSampleDistanceUnits) {
		this.detectorToSampleDistanceUnits = detectorToSampleDistanceUnits;
	}
	public String getxPixelSize() {
		return xPixelSize;
	}
	public void setxPixelSize(String xPixelSize) {
		this.xPixelSize = xPixelSize;
	}
	public String getxPixelSizeUnits() {
		return xPixelSizeUnits;
	}
	public void setxPixelSizeUnits(String xPixelSizeUnits) {
		this.xPixelSizeUnits = xPixelSizeUnits;
	}
	public String getyPixelSize() {
		return yPixelSize;
	}
	public void setyPixelSize(String yPixelSize) {
		this.yPixelSize = yPixelSize;
	}
	public String getyPixelSizeUnits() {
		return yPixelSizeUnits;
	}
	public void setyPixelSizeUnits(String yPixelSizeUnits) {
		this.yPixelSizeUnits = yPixelSizeUnits;
	}
	public String getApproxCentreOfRotation() {
		return approxCentreOfRotation;
	}
	public void setApproxCentreOfRotation(String approxCentreOfRotation) {
		this.approxCentreOfRotation = approxCentreOfRotation;
	}

	@Override
	public String toString() {
		return "TomoScanParameters [title=" + title + ", exposureTime=" + exposureTime + ", minI=" + minI + ", inBeamPosition=" + inBeamPosition
				+ ", outOfBeamPosition=" + outOfBeamPosition + ", start=" + start + ", stop=" + stop + ", step=" + step + ", imagesPerDark=" + imagesPerDark
				+ ", darkFieldInterval=" + darkFieldInterval + ", imagesPerFlat=" + imagesPerFlat + ", flatFieldInterval=" + flatFieldInterval + ", flyScan="
				+ flyScan + ", extraFlatsAtEnd=" + extraFlatsAtEnd + ", numFlyScans=" + numFlyScans + ", flyScanDelay=" + flyScanDelay
				+ ", closeShutterAfterLastScan=" + closeShutterAfterLastScan + ", rotationStage=" + rotationStage + ", linearStage=" + linearStage
				+ ", sendDataToTempDirectory=" + sendDataToTempDirectory + ", detectorToSampleDistance=" + detectorToSampleDistance
				+ ", detectorToSampleDistanceUnits=" + detectorToSampleDistanceUnits + ", xPixelSize=" + xPixelSize + ", xPixelSizeUnits=" + xPixelSizeUnits
				+ ", yPixelSize=" + yPixelSize + ", yPixelSizeUnits=" + yPixelSizeUnits + ", approxCentreOfRotation=" + approxCentreOfRotation + "]";
	}

}
