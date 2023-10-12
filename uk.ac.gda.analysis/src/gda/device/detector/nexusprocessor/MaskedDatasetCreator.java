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
import gda.jython.InterfaceProvider;

/**
 * Apply a mask to a dataset. The mask is defined by three additive specifications:
 * <li>A list of individual pixel cooordinates</li>
 * <li>A high and low threshold - outliers are masked</li>
 * <li>A Dataset mask - this could be set manually or provided by an external source</li>
 * <p>
 * The mask is cached and only regenerated when {@link #regenerateMask()} is called (the previous dataset will be used).
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

	private boolean enabled = true;


	/*
	 * Thresholds can be set in Spring e.g
	 * <property name="minThreshold">
	 *     <value type="java.lang.Integer">16</value>
	 * </property>
	 */

	private Number minThreshold = Integer.MIN_VALUE; // TODO are these defaults suitable for other datatypes?
	private Number maxThreshold = Integer.MAX_VALUE;

	@Override
	public Dataset createDataSet(Dataset ds)  {
		if(! isEnabled()) {
			return ds;
		}
		if (previousDs == null) {
			// first time so create mask
			createMask(ds);
		}
		previousDs = ds.copy(ds.getClass());
		return ds.imultiply(mask);
	}

	private void createMask(Dataset ds) {
		mask = Comparisons.logicalAnd(Comparisons.greaterThan(ds, minThreshold), Comparisons.lessThan(ds, maxThreshold));
		if (externalMask != null) {
			mask = Comparisons.logicalAnd(mask, externalMask);
		}
		maskedPixels.forEach(pixel -> maskAPixel(pixel, true));
	}

	private void maskAPixel(Tuple2i point, boolean isMasked) {
		mask.set(!isMasked, point.y, point.x);
	}

	public void regenerateMask() {
		if (previousDs != null) {
			createMask(this.previousDs);
			return;
		}
		logger.info("No cached dataset - will not update mask");
	}

	public void addMaskedPixel(int x, int y) {
		if (mask == null) {
			InterfaceProvider.getTerminalPrinter().print("Please enable masking and take a detector image to create a mask before adding pixels.");
			return;
		}
		var tuple = new Point2i(x, y);
		if (maskedPixels.contains(tuple)) {
			return;
		}
		maskedPixels.add(tuple);
		try {
			maskAPixel(tuple, true);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			InterfaceProvider.getTerminalPrinter().print("Pixel outside detector range, could not add to mask.");
			maskedPixels.remove(tuple);
		}
	}

	public List<Tuple2i> getMaskedPixels() {
		return List.copyOf(maskedPixels);
	}

	public void removeMaskedPixel(int x, int y) {
		Point2i pixel = new Point2i(x,y);
		maskedPixels.remove(pixel);
		maskAPixel(pixel, false);
	}

	public void clearMaskedPixels() {
		for (Tuple2i pixel : maskedPixels) {
			maskAPixel(pixel, false);
		}
		maskedPixels.clear();
	}

	/**
	 * If mask is set directly it is assumed to be using conventions of <a href=
	 * "https://manual.nexusformat.org/classes/base_classes/NXdetector.html#nxdetector-pixel-mask-field">NXdetector</a>
	 * @param  externalMask
	 */
	public void setExternalMask(Dataset externalMask) {
		this.externalMask = Comparisons.logicalNot(externalMask);
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
	}

	public Number getMaxThreshold() {
		return maxThreshold;
	}

	public void setMaxThreshold(Number maxThreshold) {
		this.maxThreshold = maxThreshold;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void checkIfMasked(int x, int y) {
		InterfaceProvider.getTerminalPrinter().print(
				String.format("Pixel (%d, %d): %s", x, y, (mask == null || mask.getBoolean(y, x)) ? "Not Masked" : "Masked"));
	}
}
