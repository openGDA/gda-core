/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.lima;
/**
 * Area of interest (AOI) settings for Lima CCD.
 */
public interface LimaROIInt {
	/**
	 * return ROI begin X in unit of X binning factor of the detector settings
	 * @return X-Begin
	 */
	int getBeginX();

	/**
	 * return ROI begin Y in unit of Y binning factor of the detector settings
	 * @return Y-Begin
	 */
	int getBeginY();

	/**
	 * return ROI End X in unit of X binning factor of the detector settings
	 * @return X-End
	 */
	int getEndX();

	/**
	 * return ROI End Y in unit of Y binning factor of the detector settings
	 * @return Y-End
	 */
	int getEndY();

	/**
	 * set ROI begin X in unit of X binning factor of the detector settings
	 * @param val - X-Begin
	 */
	void setBeginX(int val);

	/**
	 * set ROI End X in unit of X binning factor of the detector settings
	 * @param val - X-End
	 */
	void setEndX(int val);

	/**
	 * set ROI begin Y in unit of Y binning factor of the detector settings
	 * @param val - Y-Begin
	 */
	void setBeginY(int val);

	/**
	 * set ROI End Y in unit of Y binning factor of the detector settings
	 * @param val - Y-End
	 */
	void setEndY(int val);
}
