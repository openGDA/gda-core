/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.detector.multichannelscaler;

/**
 * Help class to do MAC detector to scaler Channel mapping
 */
public class Mca {
	private String name;
	private Integer scalerChannel;
	private String detectorName;

	/**
	 * @return detector name
	 */
	public String getDetectorName() {
		return detectorName;
	}

	/**
	 * @param detectorName
	 */
	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return scaler channel
	 */
	public Integer getScalerChannel() {
		return scalerChannel;
	}

	/**
	 * @param scalerChannel
	 */
	public void setScalerChannel(Integer scalerChannel) {
		this.scalerChannel = scalerChannel;
	}
}
