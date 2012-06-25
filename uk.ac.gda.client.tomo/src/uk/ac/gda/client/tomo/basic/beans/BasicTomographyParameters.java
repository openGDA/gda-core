/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.basic.beans;

import java.net.URL;

public class BasicTomographyParameters {
	
	/**
	 * Static for getting the mapping file
	 */
	static public final URL mappingURL = BasicTomographyParameters.class.getResource("ParameterMapping.xml");
	/**
	 * Static for getting the schema file
	 */
	static public final URL schemaURL  = BasicTomographyParameters.class.getResource("ParameterMapping.xsd");
	
	// device names
	private String camera;
	private String theta;
	private String flatFieldTranslation;
	
	
	// Camera Parameters
	private int cameraROIStartX;
	private int cameraROIStartY;
	private int cameraROISizeX;
	private int cameraROISizeY;
	private int cameraBinX;
	private int cameraBinY;
	private double cameraExposureTime;
	
	// Scan parameters
	private double scanStartAngle;
	private double scanEndAngle;
	private int scanNumberOfPointsPerSegment;
	private int scanNumberOfSegments;
	
	// Dark field parametetrs
	private int darkNumberOfImages;
	
	// Flat field parametes	
	private int flatNumberOfImages;
	
	// reconstruction Parameters
	private int reconNumberOfChunks;
	private String reconJobName;
	
	
	public String getCamera() {
		return camera;
	}
	public void setCamera(String camera) {
		this.camera = camera;
	}
	public String getTheta() {
		return theta;
	}
	public void setTheta(String theta) {
		this.theta = theta;
	}
	public String getFlatFieldTranslation() {
		return flatFieldTranslation;
	}
	public void setFlatFieldTranslation(String flatFieldTranslation) {
		this.flatFieldTranslation = flatFieldTranslation;
	}
	public int getCameraROIStartX() {
		return cameraROIStartX;
	}
	public void setCameraROIStartX(int cameraROIStartX) {
		this.cameraROIStartX = cameraROIStartX;
	}
	public int getCameraROIStartY() {
		return cameraROIStartY;
	}
	public void setCameraROIStartY(int cameraROIStartY) {
		this.cameraROIStartY = cameraROIStartY;
	}
	public int getCameraROISizeX() {
		return cameraROISizeX;
	}
	public void setCameraROISizeX(int cameraROISizeX) {
		this.cameraROISizeX = cameraROISizeX;
	}
	public int getCameraROISizeY() {
		return cameraROISizeY;
	}
	public void setCameraROISizeY(int cameraROISizeY) {
		this.cameraROISizeY = cameraROISizeY;
	}
	public int getCameraBinX() {
		return cameraBinX;
	}
	public void setCameraBinX(int cameraBinX) {
		this.cameraBinX = cameraBinX;
	}
	public int getCameraBinY() {
		return cameraBinY;
	}
	public void setCameraBinY(int cameraBinY) {
		this.cameraBinY = cameraBinY;
	}
	public double getCameraExposureTime() {
		return cameraExposureTime;
	}
	public void setCameraExposureTime(double cameraExposureTime) {
		this.cameraExposureTime = cameraExposureTime;
	}
	public double getScanStartAngle() {
		return scanStartAngle;
	}
	public void setScanStartAngle(double scanStartAngle) {
		this.scanStartAngle = scanStartAngle;
	}
	public double getScanEndAngle() {
		return scanEndAngle;
	}
	public void setScanEndAngle(double scanEndAngle) {
		this.scanEndAngle = scanEndAngle;
	}
	public int getScanNumberOfPointsPerSegment() {
		return scanNumberOfPointsPerSegment;
	}
	public void setScanNumberOfPointsPerSegment(int scanNumberOfPointsPerSegment) {
		this.scanNumberOfPointsPerSegment = scanNumberOfPointsPerSegment;
	}
	public int getScanNumberOfSegments() {
		return scanNumberOfSegments;
	}
	public void setScanNumberOfSegments(int scanNumberOfSegments) {
		this.scanNumberOfSegments = scanNumberOfSegments;
	}
	public int getDarkNumberOfImages() {
		return darkNumberOfImages;
	}
	public void setDarkNumberOfImages(int darkNumberOfImages) {
		this.darkNumberOfImages = darkNumberOfImages;
	}
	public int getFlatNumberOfImages() {
		return flatNumberOfImages;
	}
	public void setFlatNumberOfImages(int flatNumberOfImages) {
		this.flatNumberOfImages = flatNumberOfImages;
	}
	public int getReconNumberOfChunks() {
		return reconNumberOfChunks;
	}
	public void setReconNumberOfChunks(int reconNumberOfChunks) {
		this.reconNumberOfChunks = reconNumberOfChunks;
	}
	public String getReconJobName() {
		return reconJobName;
	}
	public void setReconJobName(String reconJobName) {
		this.reconJobName = reconJobName;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((camera == null) ? 0 : camera.hashCode());
		result = prime * result + cameraBinX;
		result = prime * result + cameraBinY;
		long temp;
		temp = Double.doubleToLongBits(cameraExposureTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + cameraROISizeX;
		result = prime * result + cameraROISizeY;
		result = prime * result + cameraROIStartX;
		result = prime * result + cameraROIStartY;
		result = prime * result + darkNumberOfImages;
		result = prime * result + ((flatFieldTranslation == null) ? 0 : flatFieldTranslation.hashCode());
		result = prime * result + flatNumberOfImages;
		result = prime * result + ((reconJobName == null) ? 0 : reconJobName.hashCode());
		result = prime * result + reconNumberOfChunks;
		temp = Double.doubleToLongBits(scanEndAngle);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + scanNumberOfPointsPerSegment;
		result = prime * result + scanNumberOfSegments;
		temp = Double.doubleToLongBits(scanStartAngle);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((theta == null) ? 0 : theta.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicTomographyParameters other = (BasicTomographyParameters) obj;
		if (camera == null) {
			if (other.camera != null)
				return false;
		} else if (!camera.equals(other.camera))
			return false;
		if (cameraBinX != other.cameraBinX)
			return false;
		if (cameraBinY != other.cameraBinY)
			return false;
		if (Double.doubleToLongBits(cameraExposureTime) != Double.doubleToLongBits(other.cameraExposureTime))
			return false;
		if (cameraROISizeX != other.cameraROISizeX)
			return false;
		if (cameraROISizeY != other.cameraROISizeY)
			return false;
		if (cameraROIStartX != other.cameraROIStartX)
			return false;
		if (cameraROIStartY != other.cameraROIStartY)
			return false;
		if (darkNumberOfImages != other.darkNumberOfImages)
			return false;
		if (flatFieldTranslation == null) {
			if (other.flatFieldTranslation != null)
				return false;
		} else if (!flatFieldTranslation.equals(other.flatFieldTranslation))
			return false;
		if (flatNumberOfImages != other.flatNumberOfImages)
			return false;
		if (reconJobName == null) {
			if (other.reconJobName != null)
				return false;
		} else if (!reconJobName.equals(other.reconJobName))
			return false;
		if (reconNumberOfChunks != other.reconNumberOfChunks)
			return false;
		if (Double.doubleToLongBits(scanEndAngle) != Double.doubleToLongBits(other.scanEndAngle))
			return false;
		if (scanNumberOfPointsPerSegment != other.scanNumberOfPointsPerSegment)
			return false;
		if (scanNumberOfSegments != other.scanNumberOfSegments)
			return false;
		if (Double.doubleToLongBits(scanStartAngle) != Double.doubleToLongBits(other.scanStartAngle))
			return false;
		if (theta == null) {
			if (other.theta != null)
				return false;
		} else if (!theta.equals(other.theta))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "BasicTomographyParameters [camera=" + camera + ", cameraBinX=" + cameraBinX + ", cameraBinY="
				+ cameraBinY + ", cameraExposureTime=" + cameraExposureTime + ", cameraROISizeX=" + cameraROISizeX
				+ ", cameraROISizeY=" + cameraROISizeY + ", cameraROIStartX=" + cameraROIStartX + ", cameraROIStartY="
				+ cameraROIStartY + ", darkNumberOfImages=" + darkNumberOfImages + ", flatFieldTranslation="
				+ flatFieldTranslation + ", flatNumberOfImages=" + flatNumberOfImages + ", reconJobName="
				+ reconJobName + ", reconNumberOfChunks=" + reconNumberOfChunks + ", scanEndAngle=" + scanEndAngle
				+ ", scanNumberOfPointsPerSegment=" + scanNumberOfPointsPerSegment + ", scanNumberOfSegments="
				+ scanNumberOfSegments + ", scanStartAngle=" + scanStartAngle + ", theta=" + theta + "]";
	}
	


	public void clear() {
		
	}
	
	
	
}
	
	