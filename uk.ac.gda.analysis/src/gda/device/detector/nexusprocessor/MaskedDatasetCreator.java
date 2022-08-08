/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2i;
import javax.vecmath.Tuple2i;

import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FindableBase;

/**
 * Apply a mask to a dataset. The mask is defined by three additive specifications:
 * <li>A list of individual pixel cooordinates</li>
 * <li>A high and low threshold - outliers are masked</li>
 * <li>A Dataset mask - this could be set manually or provided by an external source</li>
 * <p>
 * The mask is cached and only regenerated when one of the parameters change or when {@link #regenerateMask()}
 * is called (the previous dataset will be used).
 *
 */
public class MaskedDatasetCreator extends FindableBase implements DatasetCreator {

	private static final Logger logger = LoggerFactory.getLogger(MaskedDatasetCreator.class);

	/**
	 * Actual mask to apply, generated with the three inputs
	 */
	private Dataset mask;

	private Dataset externalMask;

	private Dataset previousDs;

	private List<Tuple2i> maskedPixels = new ArrayList<>();

	// TODO is this suitable for other datatypes?
	private Number minThreshold = Integer.MIN_VALUE;
	private Number maxThreshold = Integer.MAX_VALUE;

	@Override
	public Dataset createDataSet(Dataset ds)  {
		if (previousDs == null) {
			// first time so create mask
			createMask(ds);
		}
		previousDs = ds;
		return ds.imultiply(mask);
	}

	private void createMask(Dataset ds) {
		mask = Comparisons.logicalAnd(Comparisons.greaterThan(ds, minThreshold), Comparisons.lessThan(ds, maxThreshold));
		if (externalMask != null) {
			mask = Comparisons.logicalAnd(mask, externalMask);
		}
		maskedPixels.forEach(this::maskAPixel);
	}

	private void maskAPixel(Tuple2i point) {
		mask.set(false, point.x, point.y);
	}

	public void regenerateMask() {
		if (previousDs != null) {
			createMask(this.previousDs);
			return;
		}
		logger.info("No cached dataset - will not update mask");
	}

	public void addMaskedPixel(int x, int y) {
		var tuple = new Point2i(x, y);
		if (!maskedPixels.contains(tuple)) {
			maskedPixels.add(tuple);
		}
		regenerateMask();
	}

	public List<Tuple2i> getMaskedPixels() {
		return List.copyOf(maskedPixels);
	}

	public void removeMaskedPixel(int x, int y) {
		maskedPixels.remove(new Point2i(x,y));
		regenerateMask();
	}

	public void clearMaskedPixels() {
		maskedPixels.clear();
		regenerateMask();
	}

	/**
	 * If mask is set directly it is assumed to be using conventions of <a href=
	 * "https://manual.nexusformat.org/classes/base_classes/NXdetector.html#nxdetector-pixel-mask-field">NXdetector</a>
	 * @param  externalMask
	 */
	public void setExternalMask(Dataset externalMask) {
		this.externalMask = Comparisons.logicalNot(externalMask);
		regenerateMask();
	}

	public Number getMinThreshold() {
		return minThreshold;
	}

	public void setThreshold(Number min, Number max) {
		setMinThreshold(min);
		setMaxThreshold(max);
	}

	public void setMinThreshold(Number minThreshold) {
		this.minThreshold = minThreshold;
		regenerateMask();
	}

	public Number getMaxThreshold() {
		return maxThreshold;
	}

	public void setMaxThreshold(Number maxThreshold) {
		this.maxThreshold = maxThreshold;
		regenerateMask();
	}

}
