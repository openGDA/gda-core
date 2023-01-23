/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.scan.datawriter;

import java.io.IOException;

import gda.device.Detector;
import gda.scan.IScanDataPoint;
import uk.ac.gda.client.microfocus.scan.util.RandomLineFileWriter;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public class TwoWayMicroFocusWriterExtender extends MicroFocusWriterExtender {

	private RandomLineFileWriter rgbWriter;

	public TwoWayMicroFocusWriterExtender(int xPoints, int yPoints, double xStepSize, double yStepSize,
			XMLRichBean detectorBean, Detector[] detectors2) {
		super(xPoints, yPoints, xStepSize, yStepSize, detectorBean, detectors2);
	}

	@Override
	protected int getCurrentSDPNumber(IScanDataPoint dataPoint) {
		Double[] xy = dataPoint.getPositionsAsDoubles();
		int[] xyIndex = getXYIndex(dataPoint.getCurrentPointNumber(), xy[1]);
		int correctedDataPointNumber = xyIndex[1] * numberOfXPoints + xyIndex[0];
		return correctedDataPointNumber;
	}

	@Override
	protected void addToRgbFile(int currentPointNumber, String string) throws IOException {
		rgbWriter.addToFile(currentPointNumber, string);

	}

	private int[] getXYIndex(int dataPointNumber, double xValue) {
		int xIndex = dataPointNumber % numberOfXPoints;
		int yIndex = dataPointNumber / numberOfXPoints;
		if (yIndex > 0)// first row is complete
			xIndex = findXIndexByValue(xValue);
		return new int[] { xIndex, yIndex };
	}

	private int findXIndexByValue(double xValue) {
		for (int i = 0; i < xValues.length; i++) {
			if (xValues[i] == xValue)
				return i;
		}
		return -1;
	}

	@Override
	protected void createRgbFile(String string) {
		if (!string.contains("."))
			string = string + ".rgb";
		rgbWriter = new RandomLineFileWriter();
		rgbWriter.createRandomLineFile(string);
	}
}
