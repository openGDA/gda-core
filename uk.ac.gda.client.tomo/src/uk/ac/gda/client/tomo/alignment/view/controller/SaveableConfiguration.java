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

package uk.ac.gda.client.tomo.alignment.view.controller;

import uk.ac.gda.ui.components.CameraControlComposite.RESOLUTION;
import uk.ac.gda.ui.components.MotionControlComposite.SAMPLE_WEIGHT;

/**
 *
 */
public class SaveableConfiguration {
	public enum AlignmentScanMode {
		Continuous, Step;
	}

	private int moduleNumber;

	private double sampleVerticalPosition;

	private double sampleCenterXPosition;

	private double sampleCenterZPosition;

	private double tiltZPosition;

	private double tiltXPosition;

	private double sampleAcquisitonTime;

	private double flatAcquisitionTime;

	private double sampleDetectorDistance;

	private double energy;

	private int[] roiPoints;

	private SAMPLE_WEIGHT sampleWeight;

	private RESOLUTION resolution3D;

	private double framesPerProjection;

	private String sampleDescription;

	private double acquisitionTimeDivider;

	private double cameraExposure;

	private double cameraBinning;

	private int numProjections;

	private AlignmentScanMode scanMode;

	private double totalRunTime;

	private double moduleX;

	private double moduleZ;

	private double moduleRoll;

	private double sampleBaseX;

	private double tiltX;

	private double tiltZ;

	private double detectorStageX;

	private double detectorStageY;

	private double detectorStageZ;

	private double stitchingAngle;

	private String imageLocationAtThetaPlus90;

	private String imageLocationAtTheta;

	private Double horizontalFieldOfView;

	/**
	 * @return Returns the moduleNumber.
	 */
	public int getModuleNumber() {
		return moduleNumber;
	}

	/**
	 * @param moduleNumber
	 *            The moduleNumber to set.
	 */
	public void setModuleNumber(int moduleNumber) {
		this.moduleNumber = moduleNumber;
	}

	/**
	 * @return Returns the sampleVerticalPosition.
	 */
	public double getSampleVerticalPosition() {
		return sampleVerticalPosition;
	}

	/**
	 * @param sampleVerticalPosition
	 *            The sampleVerticalPosition to set.
	 */
	public void setSampleVerticalPosition(double sampleVerticalPosition) {
		this.sampleVerticalPosition = sampleVerticalPosition;
	}

	/**
	 * @return Returns the sampleCenterXPosition.
	 */
	public double getSampleCenterXPosition() {
		return sampleCenterXPosition;
	}

	/**
	 * @param sampleCenterXPosition
	 *            The sampleCenterXPosition to set.
	 */
	public void setSampleCenterXPosition(double sampleCenterXPosition) {
		this.sampleCenterXPosition = sampleCenterXPosition;
	}

	/**
	 * @return Returns the sampleCenterZPosition.
	 */
	public double getSampleCenterZPosition() {
		return sampleCenterZPosition;
	}

	/**
	 * @param sampleCenterZPosition
	 *            The sampleCenterZPosition to set.
	 */
	public void setSampleCenterZPosition(double sampleCenterZPosition) {
		this.sampleCenterZPosition = sampleCenterZPosition;
	}

	/**
	 * @return Returns the tiltZPosition.
	 */
	public double getTiltZPosition() {
		return tiltZPosition;
	}

	/**
	 * @param tiltZPosition
	 *            The tiltZPosition to set.
	 */
	public void setTiltZPosition(double tiltZPosition) {
		this.tiltZPosition = tiltZPosition;
	}

	/**
	 * @return Returns the tiltXPosition.
	 */
	public double getTiltXPosition() {
		return tiltXPosition;
	}

	/**
	 * @param tiltXPosition
	 *            The tiltXPosition to set.
	 */
	public void setTiltXPosition(double tiltXPosition) {
		this.tiltXPosition = tiltXPosition;
	}

	/**
	 * @return Returns the sampleAcquisitonTime.
	 */
	public double getSampleAcquisitonTime() {
		return sampleAcquisitonTime;
	}

	/**
	 * @param sampleAcquisitonTime
	 *            The sampleAcquisitonTime to set.
	 */
	public void setSampleAcquisitonTime(double sampleAcquisitonTime) {
		this.sampleAcquisitonTime = sampleAcquisitonTime;
	}

	/**
	 * @return Returns the flatAcquisitionTime.
	 */
	public double getFlatAcquisitionTime() {
		return flatAcquisitionTime;
	}

	/**
	 * @param flatAcquisitionTime
	 *            The flatAcquisitionTime to set.
	 */
	public void setFlatAcquisitionTime(double flatAcquisitionTime) {
		this.flatAcquisitionTime = flatAcquisitionTime;
	}

	/**
	 * @return Returns the sampleDetectorDistance.
	 */
	public double getSampleDetectorDistance() {
		return sampleDetectorDistance;
	}

	/**
	 * @param sampleDetectorDistance
	 *            The sampleDetectorDistance to set.
	 */
	public void setSampleDetectorDistance(double sampleDetectorDistance) {
		this.sampleDetectorDistance = sampleDetectorDistance;
	}

	/**
	 * @return Returns the energy.
	 */
	public double getEnergy() {
		return energy;
	}

	/**
	 * @param energy
	 *            The energy to set.
	 */
	public void setEnergy(double energy) {
		this.energy = energy;
	}

	/**
	 * @return Returns the roiPoints.
	 */
	public int[] getRoiPoints() {
		return roiPoints;
	}

	/**
	 * @param roiPoints
	 *            The roiPoints to set.
	 */
	public void setRoiPoints(int[] roiPoints) {
		this.roiPoints = roiPoints;
	}

	/**
	 * @return Returns the sampleWeight.
	 */
	public SAMPLE_WEIGHT getSampleWeight() {
		return sampleWeight;
	}

	/**
	 * @param sampleWeight
	 *            The sampleWeight to set.
	 */
	public void setSampleWeight(SAMPLE_WEIGHT sampleWeight) {
		this.sampleWeight = sampleWeight;
	}

	/**
	 * @return Returns the selection3D.
	 */
	public RESOLUTION getResolution3D() {
		return resolution3D;
	}

	/**
	 * @param resolution3d
	 *            The selection3D to set.
	 */
	public void setResolution3D(RESOLUTION resolution3d) {
		resolution3D = resolution3d;
	}

	/**
	 * @return Returns the framesPerProjection.
	 */
	public double getFramesPerProjection() {
		return framesPerProjection;
	}

	/**
	 * @param framesPerProjection
	 *            The framesPerProjection to set.
	 */
	public void setFramesPerProjection(double framesPerProjection) {
		this.framesPerProjection = framesPerProjection;
	}

	/**
	 * @return Returns the sampleDescription.
	 */
	public String getSampleDescription() {
		return sampleDescription;
	}

	/**
	 * @param sampleDescription
	 *            The sampleDescription to set.
	 */
	public void setSampleDescription(String sampleDescription) {
		this.sampleDescription = sampleDescription;
	}

	/**
	 * @return Returns the acquisitionTimeDivider.
	 */
	public double getAcquisitionTimeDivider() {
		return acquisitionTimeDivider;
	}

	/**
	 * @param acquisitionTimeDivider
	 *            The acquisitionTimeDivider to set.
	 */
	public void setAcquisitionTimeDivider(double acquisitionTimeDivider) {
		this.acquisitionTimeDivider = acquisitionTimeDivider;
	}

	/**
	 * @return Returns the cameraExposure.
	 */
	public double getCameraExposure() {
		return cameraExposure;
	}

	/**
	 * @param cameraExposure
	 *            The cameraExposure to set.
	 */
	public void setCameraExposure(double cameraExposure) {
		this.cameraExposure = cameraExposure;
	}

	/**
	 * @return Returns the cameraBinning.
	 */
	public double getCameraBinning() {
		return cameraBinning;
	}

	/**
	 * @param cameraBinning
	 *            The cameraBinning to set.
	 */
	public void setCameraBinning(double cameraBinning) {
		this.cameraBinning = cameraBinning;
	}

	/**
	 * @return Returns the numProjections.
	 */
	public int getNumProjections() {
		return numProjections;
	}

	/**
	 * @param numProjections
	 *            The numProjections to set.
	 */
	public void setNumProjections(int numProjections) {
		this.numProjections = numProjections;
	}

	/**
	 * @return Returns the scanMode.
	 */
	public AlignmentScanMode getScanMode() {
		return scanMode;
	}

	/**
	 * @param scanMode
	 *            The scanMode to set.
	 */
	public void setScanMode(AlignmentScanMode scanMode) {
		this.scanMode = scanMode;
	}

	/**
	 * @return Returns the totalRunTime.
	 */
	public double getTotalRunTime() {
		return totalRunTime;
	}

	/**
	 * @param totalRunTime
	 *            The totalRunTime to set.
	 */
	public void setTotalRunTime(double totalRunTime) {
		this.totalRunTime = totalRunTime;
	}

	public double getModuleRoll() {
		return moduleRoll;
	}

	public void setModuleRoll(double moduleRoll) {
		this.moduleRoll = moduleRoll;
	}

	public double getModuleZ() {
		return moduleZ;
	}

	public void setModuleZ(double moduleZ) {
		this.moduleZ = moduleZ;
	}

	public double getModuleX() {
		return moduleX;
	}

	public void setModuleX(double moduleX) {
		this.moduleX = moduleX;
	}

	public double getSampleBaseX() {
		return sampleBaseX;
	}

	public void setSampleBaseX(double sampleBaseX) {
		this.sampleBaseX = sampleBaseX;
	}

	public double getTiltX() {
		return tiltX;
	}

	public void setTiltX(double tiltX) {
		this.tiltX = tiltX;
	}

	public double getTiltZ() {
		return tiltZ;
	}

	public void setTiltZ(double tiltZ) {
		this.tiltZ = tiltZ;
	}

	public double getDetectorStageX() {
		return detectorStageX;
	}

	public void setDetectorStageX(double detectorStageX) {
		this.detectorStageX = detectorStageX;
	}

	public double getDetectorStageY() {
		return detectorStageY;
	}

	public void setDetectorStageY(double detectorStageY) {
		this.detectorStageY = detectorStageY;
	}

	public double getDetectorStageZ() {
		return detectorStageZ;
	}

	public void setDetectorStageZ(double detectorStageZ) {
		this.detectorStageZ = detectorStageZ;
	}

	public void setStitchingAngle(double thetaValue) {
		this.stitchingAngle = thetaValue;
	}

	public double getStitchingAngle() {
		return stitchingAngle;
	}

	public void setImageAtThetaPlus90(String imageLocationAtThetaPlus90) {
		this.imageLocationAtThetaPlus90 = imageLocationAtThetaPlus90;
	}

	public String getImageLocationAtThetaPlus90() {
		return imageLocationAtThetaPlus90;
	}

	public void setImageAtTheta(String imageLocationAtTheta) {
		this.imageLocationAtTheta = imageLocationAtTheta;
	}

	public String getImageLocationAtTheta() {
		return imageLocationAtTheta;
	}

	public void setModuleHorizontalFieldOfView(Double horizontalFieldOfView) {
		this.horizontalFieldOfView = horizontalFieldOfView;
	}

	public Double getHorizontalFieldOfView() {
		return horizontalFieldOfView;
	}
}
