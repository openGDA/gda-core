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

package gda.device.detector;

import java.io.Serializable;

/**
 * A class for passing detector data size parameters. Use this class as we
 * cannot always guarantee that the detector will give a square image.
 * Extendable for a third dimension should the need arise (eg. frames).
 */
public class DataDimension implements Serializable {

	public int rasters = 1;
	public int pixels = 1;

	public DataDimension() {
	}

	public DataDimension(int pixels, int rasters) {
		this.pixels = pixels;
		this.rasters = rasters;
	}

	public void setPixels(int pixels) {
		this.pixels = pixels;
	}

	public int getPixels() {
		return pixels;
	}

	public void setRasters(int rasters) {
		this.rasters = rasters;
	}

	public int getRasters() {
		return rasters;
	}

	/**
	 * Get the detector size.
	 * 
	 * @return the detector size as a Dimension
	 */
	public DataDimension getSize() {
		return new DataDimension(pixels, rasters);
	}

	public void setSize(int pixels, int rasters) {
		this.pixels = pixels;
		this.rasters = rasters;
	}

}
