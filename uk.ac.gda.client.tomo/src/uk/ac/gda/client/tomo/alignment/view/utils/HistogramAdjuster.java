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

package uk.ac.gda.client.tomo.alignment.view.utils;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that adjusts the histogram values for a given image data. This takes an image data clones it and every request
 * to update the histogram returns a clone of the {@link ImageData}
 */
public class HistogramAdjuster {

	private static final int BIN_SIZE = 255;

	private static final double BIN_DOUBLE = BIN_SIZE;

	private final static Logger logger = LoggerFactory.getLogger(HistogramAdjuster.class);

	private int lowValue = 0;
	private int maxIntensity = 65536;
	private int highValue = maxIntensity;

	private ImageData imgData;

	/**
	 * This should be the clone of the image data and not the data itself.
	 * 
	 * @param imgData
	 */
	public void setImageData(ImageData imgData) {
		this.imgData = imgData;
	}

	public void dispose() {
		imgData = null;
	}

	public void setMaxIntensity(int maxIntensity) {
		this.maxIntensity = maxIntensity;
	}

	public ImageData updateHistogramHighValue(int highValue) {
		this.highValue = highValue;

		ImageData imgData = updateHistogram();
		return imgData;
	}

	public ImageData updateHistogramLowValue(int lowValue) {
		this.lowValue = lowValue;
		ImageData imgData = updateHistogram();
		return imgData;
	}

	public ImageData updateHistogramValues(double lowValue, double highValue) {
		this.lowValue = (int) lowValue;
		this.highValue = (int) highValue;
		ImageData imgData = updateHistogram();
		return imgData;
	}

	protected ImageData updateHistogram() {
		logger.debug("Updating histogram");
		int[] valArray = new int[BIN_SIZE];
		imgData.palette.getRGBs();

		int cBin = (int) (((double) lowValue / (double) maxIntensity) * BIN_DOUBLE);
		int dBin = (int) (((double) highValue / (double) maxIntensity) * BIN_DOUBLE);

		double width = dBin - cBin;

		int step = (int) (BIN_DOUBLE / width);

		RGB rgb0 = new RGB(0, 0, 0);
		PaletteData palette = imgData.palette;
		int pixel0 = palette.getPixel(rgb0);
		for (int i = 0; i <= cBin; i++) {
			valArray[i] = pixel0;
		}

		// RGB rgb255 = new RGB(dBin - 1, dBin - 1, dBin - 1);
		RGB rgb255 = new RGB(255, 255, 255);
		int pixel255 = palette.getPixel(rgb255);
		for (int i = dBin - 1; i < BIN_SIZE; i++) {
			valArray[i] = pixel255;
		}

		for (int i = cBin + 1; i < dBin - 1; i++) {
			int stepIntensity = (i - cBin) * step;
			RGB rgb = new RGB(stepIntensity, stepIntensity, stepIntensity);

			valArray[i] = palette.getPixel(rgb);
		}

		// Need to consider a clone because once the original data is set below the high values then it can be reverted
		// for values above.
		ImageData imgData = (ImageData) this.imgData.clone();
		// imgDataClone.palette.isDirect = false;
		for (int h = 0; h < imgData.height; h++) {
			for (int w = 0; w < imgData.width; w++) {
				int pixel = imgData.getPixel(w, h);

				// to equate it to a 10 bit number of 1024 shift it right by 14
				int actualIntensityVal = pixel >> 8;
				double offset = (double) actualIntensityVal / (double) maxIntensity;
				int index = (int) (offset * BIN_SIZE);
				imgData.setPixel(w, h, valArray[index]);
			}
		}
		return imgData;
	}

	public double getMaxIntensity() {
		return maxIntensity;
	}

	public double getMinIntensity() {
		return 0;
	}
}
