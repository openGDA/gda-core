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

package uk.ac.gda.api.camera;

import java.io.Serializable;

public class CameraRegionOfInterest implements Serializable {
	private int left;
	private int right;

	private int top;
	private int bottom;

	public CameraRegionOfInterest () {

	}

	public CameraRegionOfInterest (CameraRegionOfInterest regionOfInterest) {
		copy (regionOfInterest);
	}

	public void copy (CameraRegionOfInterest regionOfInterest) {
		left = regionOfInterest.left;
		right = regionOfInterest.right;
		top = regionOfInterest.top;
		bottom = regionOfInterest.bottom;
	}

	public static CameraRegionOfInterest getIntance (int left, int top, int right, int bottom) {
		CameraRegionOfInterest result = new CameraRegionOfInterest();
		if (left <= right) {
			result.left = left;
			result.right = right;
		} else {
			result.left= right;
			result.right = left;
		}

		if (top <= bottom) {
			result.top = top;
			result.bottom = bottom;
		} else {
			result.top= bottom;
			result.bottom = top;
		}
		return result;
	}

	public static CameraRegionOfInterest getInstanceFromWidthHeight (int left, int top, int width, int height) {
		CameraRegionOfInterest result = new CameraRegionOfInterest();
		if (left <= left + width) {
			result.left = left;
			result.right = left + width;
		} else {
			result.left= left + width;
			result.right = left;
		}

		if (top <= top + height) {
			result.top = top;
			result.bottom = top + height;
		} else {
			result.top= top + height;
			result.bottom = top;
		}
		return result;
	}

	public int getLeft() {
		return left;
	}

	public int getRight() {
		return right;
	}

	public int getTop() {
		return top;
	}

	public int getBottom() {
		return bottom;
	}

	public int getWidth() {
		return right - left;
	}

	public int getHeight () {
		return bottom - top;
	}
}
