/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.epics.connector;

import java.util.Arrays;

/**
 * Colour mode attribute as defined at <a href="http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes.html">Normative types.</a>
 */
enum NTNDArrayColorMode {
	/** Monochromatic image */
	MONO(0),
	/** Bayer pattern image, 1 value per pixel but with colour filter on detector */
	BAYER(1),
	/** RGB image with pixel colour interleave, data array is [3, NX, NY] */
	RGB1(2),
	/**RGB image with row colour interleave, data array is [NX, 3, NY] */
	RGB2(3),
	/** RGB image with plane colour interleave, data array is [NX, NY, 3] */
	RGB3(4),
	/** YUV image, 3 bytes encodes 1 RGB pixel */
	YUV444(5),
	/** YUV image, 4 bytes encodes 2 RGB pixel */
	YUV422(6),
	/** YUV image, 6 bytes encodes 4 RGB pixels */
	YUV411(7);

	private final int integerValue;
	private static final NTNDArrayColorMode[] CACHED_VALUES = values();

	private NTNDArrayColorMode(int value) {
		this.integerValue = value;
	}

	public static NTNDArrayColorMode valueOf(int value) {
        return Arrays.stream(CACHED_VALUES)
            .filter(mode -> mode.integerValue == value)
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }


}