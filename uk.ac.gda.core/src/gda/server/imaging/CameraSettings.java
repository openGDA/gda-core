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

package gda.server.imaging;

import java.io.Serializable;

/**
 * Object which holds the camera settings. It is serializable so the current settings can be sent over a socket for
 * clients to update their GUI.
 */
public class CameraSettings implements Serializable {
	/**
	 * autoExposureValue
	 */
	public int autoExposureValue = 50;

	/**
	 * autoExposureMaximum
	 */
	public int autoExposureMaximum = 100;

	/**
	 * autoExposureMinimum
	 */
	public int autoExposureMinimum = 0;

	/**
	 * brightnessValue
	 */
	public int brightnessValue = 50;

	/**
	 * brightnessMaximum
	 */
	public int brightnessMaximum = 100;

	/**
	 * brightnessMinimum
	 */
	public int brightnessMinimum = 0;

	/**
	 * gainValue
	 */
	public int gainValue = 50;

	/**
	 * gainMaximum
	 */
	public int gainMaximum = 100;

	/**
	 * gainMinimum
	 */
	public int gainMinimum = 0;

	/**
	 * gammaValue
	 */
	public int gammaValue = 50;

	/**
	 * gammaMaximum
	 */
	public int gammaMaximum = 100;

	/**
	 * gammaMinimum
	 */
	public int gammaMinimum = 0;

	/**
	 * hueValue
	 */
	public int hueValue = 50;

	/**
	 * hueMaximum
	 */
	public int hueMaximum = 100;

	/**
	 * hueMinimum
	 */
	public int hueMinimum = 0;

	/**
	 * irisValue
	 */
	public int irisValue = 50;

	/**
	 * irisMaximum
	 */
	public int irisMaximum = 100;

	/**
	 * irisMinimum
	 */
	public int irisMinimum = 0;

	/**
	 * saturationValue
	 */
	public int saturationValue = 50;

	/**
	 * saturationMaximum
	 */
	public int saturationMaximum = 100;

	/**
	 * saturationMinimum
	 */
	public int saturationMinimum = 0;

	/**
	 * sharpnessValue
	 */
	public int sharpnessValue = 50;

	/**
	 * sharpnessMaximum
	 */
	public int sharpnessMaximum = 100;

	/**
	 * sharpnessMinimum
	 */
	public int sharpnessMinimum = 0;

	/**
	 * shutterValue
	 */
	public int shutterValue = 50;

	/**
	 * shutterMaximum
	 */
	public int shutterMaximum = 100;

	/**
	 * shutterMinimum
	 */
	public int shutterMinimum = 0;

	/**
	 * whiteBalanceValue
	 */
	public int whiteBalanceValue = 50;

	/**
	 * whiteBalanceMaximum
	 */
	public int whiteBalanceMaximum = 100;

	/**
	 * whiteBalanceMinimum
	 */
	public int whiteBalanceMinimum = 0;
}
