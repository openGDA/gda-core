/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17.impl;

import gda.device.DeviceException;

public class NDFileHDF5ExtraDimension extends NDFileHDF5Impl {
	// New HDF writer can write 10 extra dimensions n,x,y,3-9
	private static final String ExtraDimSizeN = "ExtraDimSizeN";
	private static final String ExtraDimSizeN_RBV = "ExtraDimSizeN_RBV";
	private static final String ExtraDimSizeX = "ExtraDimSizeX";
	private static final String ExtraDimSizeX_RBV = "ExtraDimSizeX_RBV";
	private static final String ExtraDimSizeY = "ExtraDimSizeY";
	private static final String ExtraDimSizeY_RBV = "ExtraDimSizeY_RBV";
	private static final String ExtraDimSize3 = "ExtraDimSize3";
	private static final String ExtraDimSize3_RBV = "ExtraDimSize3_RBV";
	private static final String ExtraDimSize4 = "ExtraDimSize4";
	private static final String ExtraDimSize4_RBV = "ExtraDimSize4_RBV";
	private static final String ExtraDimSize5 = "ExtraDimSize5";
	private static final String ExtraDimSize5_RBV = "ExtraDimSize5_RBV";
	private static final String ExtraDimSize6 = "ExtraDimSize6";
	private static final String ExtraDimSize6_RBV = "ExtraDimSize6_RBV";
	private static final String ExtraDimSize7 = "ExtraDimSize7";
	private static final String ExtraDimSize7_RBV = "ExtraDimSize7_RBV";
	private static final String ExtraDimSize8 = "ExtraDimSize8";
	private static final String ExtraDimSize8_RBV = "ExtraDimSize8_RBV";
	private static final String ExtraDimSize9 = "ExtraDimSize9";
	private static final String ExtraDimSize9_RBV = "ExtraDimSize9_RBV";
	private static final String[] ExtraDimSizes = {
		ExtraDimSizeN,
		ExtraDimSizeX,
		ExtraDimSizeY,
		ExtraDimSize3,
		ExtraDimSize4,
		ExtraDimSize5,
		ExtraDimSize6,
		ExtraDimSize7,
		ExtraDimSize8,
		ExtraDimSize9
	};
	private static final String[] ExtraDimSizesRBV = {
		ExtraDimSizeN_RBV,
		ExtraDimSizeX_RBV,
		ExtraDimSizeY_RBV,
		ExtraDimSize3_RBV,
		ExtraDimSize4_RBV,
		ExtraDimSize5_RBV,
		ExtraDimSize6_RBV,
		ExtraDimSize7_RBV,
		ExtraDimSize8_RBV,
		ExtraDimSize9_RBV
	};
	private Integer[] initialExtraDims = new Integer[10];

	public void setExtraDimSize(int dim, int size) throws DeviceException {
		if (dim < 0 || dim > 9) {
			throw new IllegalArgumentException("Dimension must be in range [0-9]");
		}
		if (size < 1) {
			throw new IllegalArgumentException("Extra dimension size cannot be <1");
		}
		try {
			EPICS_CONTROLLER.caput(getChannel(ExtraDimSizes[dim]), size);
		} catch (Exception ex) {
			logger.warn("Cannot setExtraDimSize {} to {}", dim, size, ex);
			throw new DeviceException(String.format("Could not set dimension %d to %d", dim, size), ex);
		}
	}

	public int getExtraDimSize(int dim) throws DeviceException {
		if (dim < 0 || dim > 9) {
			throw new IllegalArgumentException("Dimension must be in range [0-9]");
		}
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ExtraDimSizesRBV[dim]));
		} catch (Exception ex) {
			logger.warn("Cannot getExtraDimSize{}", dim, ex);
			throw new DeviceException(String.format("Could not get dimension %d", dim), ex);
		}
	}

	@Override
	public void setExtraDimensions(int[] actualDims) throws Exception {
		setNumExtraDims(actualDims.length - 1);
		for (int i = 0; i < actualDims.length; i++) {
			setExtraDimSize(actualDims.length - 1 - i, actualDims[i]);
		}
	}

	public void setInitialExtraDims(int[] dims) {
		for (int i = 0; i < dims.length && i < ExtraDimSizes.length; i++) {
			initialExtraDims[i] = dims[i];
		}
	}

	@Override
	public void reset() throws Exception {
		super.reset();
		int extraDims = getInitialNumExtraDims();
		for (int i = 0; i < extraDims; i++) {
			if (initialExtraDims[i] != null) {
				setExtraDimSize(i, initialExtraDims[i]);
			} else {
				logger.error("Not enough initial dimensions. Number expected: {}, dimensions given: {}", extraDims, initialExtraDims);
				throw new IllegalStateException("Not enough intial dimensions");
			}
		}
	}
}
