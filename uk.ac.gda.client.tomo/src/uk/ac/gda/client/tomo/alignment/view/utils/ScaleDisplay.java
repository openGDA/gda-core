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

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.Measurement;

/**
 *
 */
public class ScaleDisplay {

	private Measurement pixelMeasurement;

	private Measurement scaleLengthMeasurement;

	private DecimalFormat decFormat = new DecimalFormat("###");

	private double barLengthInPixel;

	private final static double[] POSSIBLE_LENGTHS = new double[] { 50, 20, 10, 5, 2, 1, 0.5, 0.1, 0.05, 0.01, 0.005,
			0.001 };

	private static final Logger logger = LoggerFactory.getLogger(ScaleDisplay.class);

	public ScaleDisplay(Measurement pixelMeasurement, Measurement scaleLengthMeasurement, double barLengthInPixel) {
		this.pixelMeasurement = pixelMeasurement;
		this.scaleLengthMeasurement = scaleLengthMeasurement;
		this.barLengthInPixel = barLengthInPixel;
	}

	public int getBarLengthInPixel() {
		return (int) barLengthInPixel;
	}

	/**
	 * @param maxNumberOfPixels
	 *            the maximum number of pixels of the scale bar that can be displayed.
	 * @return {@link ScaleDisplay}
	 */
	public static ScaleDisplay getScaleDisplay(double maxNumberOfPixels, double objectPixelSize, double binValue,
			double zoomValue) {

		logger.info(String.format("max number of pixels:%1$s  objectpixelsize:%2$s  binValue:%3$s  zoomValue:%4$s",
				maxNumberOfPixels, objectPixelSize, binValue, zoomValue));
		for (double length : POSSIBLE_LENGTHS) {
			int barPixelVal = (int) (zoomValue * length / (binValue * objectPixelSize));
			if (barPixelVal > maxNumberOfPixels) {
				continue;
			}

			double numPixels = length / objectPixelSize;

			logger.info(String.format("numPixels:%1$s  length:%2$s   barPixelValue:%3$s", numPixels, length,
					barPixelVal));

			return new ScaleDisplay(new Measurement.PixelMeasurement(numPixels),
					new Measurement.ScaleLengthMeasurement(length), barPixelVal);
		}
		return null;
	}

	@Override
	public String toString() {
		return String.format("%1$s%2$s / %3$s %4$s", decFormat.format(scaleLengthMeasurement.getMeasurementValue()),
				scaleLengthMeasurement.getMeasurementUnit(), decFormat.format(pixelMeasurement.getMeasurementValue()),
				pixelMeasurement.getMeasurementUnit());
	}
}
