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

package uk.ac.gda.client.liveplot.mjpeg;

import gda.factory.Findable;

/**
 * A class to be used to hold camera configuration for use with the LiveMJPEGView
 *
 * @author James Mudd
 */
public class CameraConfiguration implements Findable {

	private String name; // Set to id by spring stuff
	private String displayName; // Typically a "nice" name for the camera e.g "Sample microscope"
	private String url; // url to get the data from the camera typically MJPEG stream
	private String arrayPv; // The PV of the array plugin to use for the stream eg ws141-AD-SIM-01:ARR
	private boolean rgb; // If true the camera will be treated as RBG not grayscale
	private long sleepTime; // ms
	private int cacheSize; // frames

	// TODO add more Java Doc for these properties
	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arrayPv == null) ? 0 : arrayPv.hashCode());
		result = prime * result + cacheSize;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (rgb ? 1231 : 1237);
		result = prime * result + (int) (sleepTime ^ (sleepTime >>> 32));
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		CameraConfiguration other = (CameraConfiguration) obj;
		if (arrayPv == null) {
			if (other.arrayPv != null)
				return false;
		} else if (!arrayPv.equals(other.arrayPv))
			return false;
		if (cacheSize != other.cacheSize)
			return false;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (rgb != other.rgb)
			return false;
		if (sleepTime != other.sleepTime)
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}
