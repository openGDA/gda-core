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

package uk.ac.gda.client.live.stream.view;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import gda.device.detector.nxdetector.roi.RemoteRectangularROIsProvider;
import gda.factory.FindableBase;

/**
 * A class to be used to hold camera configuration for use with the LiveMJPEGView.
 * <p>
 * For more info on configuring this class see
 * <a href="https://confluence.diamond.ac.uk/x/1wWKAg">Setup Live Stream Camera Views</a>
 *
 * @author James Mudd
 */
public class CameraConfiguration extends FindableBase {

	/** Typically a "nice" name for the camera e.g "Sample microscope" */
	private String displayName;
	/** URL to get the data from the camera needs to be a MJPEG stream */
	private String url;
	/** The PV of the array plugin to use for the EPICS stream e.g. "ws141-AD-SIM-01:ARR"*/
	private String arrayPv;
	/** If true the camera will be treated as RBG not grayscale (Only for MJPEG) */
	private boolean rgb;
	/** Some delay time (Only for MJPEG)*/
	private long sleepTime; // ms
	/** Some cache size (Only for MJPEG)*/
	private int cacheSize; // frames
	/** If set, will allow ROIs drawn on the live stream to be passes to AD plugins in scans*/
	private RemoteRectangularROIsProvider roiProvider;
	/** If set, adds axes to the camera and allows the image to be set in the Map view. */
	private CameraCalibration cameraCalibration;
	private boolean withHistogram = false;

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isRgb() {
		return rgb;
	}
	public void setRgb(boolean rgb) {
		this.rgb = rgb;
	}

	public long getSleepTime() {
		return sleepTime;
	}
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	public int getCacheSize() {
		return cacheSize;
	}
	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public String getArrayPv() {
		return arrayPv;
	}
	public void setArrayPv(String arrayPv) {
		this.arrayPv = arrayPv;
	}

	public RemoteRectangularROIsProvider getRoiProvider() {
		return roiProvider;
	}
	public void setRoiProvider(RemoteRectangularROIsProvider roiProvider) {
		this.roiProvider = roiProvider;
	}

	public CameraCalibration getCameraCalibration() {
		return cameraCalibration;
	}
	public void setCameraCalibration(CameraCalibration cameraCalibration) {
		this.cameraCalibration = cameraCalibration;
	}

	public boolean isWithHistogram() {
		return withHistogram;
	}

	public void setWithHistogram(boolean withHistogram) {
		this.withHistogram = withHistogram;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
