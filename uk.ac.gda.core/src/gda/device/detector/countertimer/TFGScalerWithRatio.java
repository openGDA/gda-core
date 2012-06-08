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

package gda.device.detector.countertimer;

import gda.device.DeviceException;
import gda.device.detector.DarkCurrentDetector;

import org.apache.commons.lang.ArrayUtils;

/**
 * Temporary class for B18.
 * <p>
 * For when the drain currents are being fed into the TFG Scaler rather than the ion chamber output. When using drain
 * current then only It/I0 is required. This class should be used in place of TFGScalerWithLogValues for counterTimer01.
 */
public class TFGScalerWithRatio extends TfgScalerWithDarkCurrent implements DarkCurrentDetector {

	private boolean outputRatio = false; // add ln(I0/It) and ln(I0/Iref) to the output

	public TFGScalerWithRatio() {
		super();
	}

	/**
	 * @return Returns the outputLogValues.
	 */
	public boolean isOutputRatio() {
		return outputRatio;
	}

	/**
	 * When set to true I0/It will be added to the output columns
	 * 
	 * @param outputRatio
	 *            The outputLogValues to set.
	 */

	public void setOutputRatio(boolean outputRatio) {
		this.outputRatio = outputRatio;
	}

	@Override
	public int getTotalChans() throws DeviceException {
		int cols = scaler.getDimension()[0];
		if (numChannelsToRead != null) {
			cols = numChannelsToRead;
		}
		if (timeChannelRequired) {
			cols++;
		}
		if (outputRatio) {
			cols += 2;
		}
		if (isTFGv2()) {
			cols--;
		}
		return cols;
	}

	@Override
	public double[] readout() throws DeviceException {
		double[] output = super.readout();
		
		if (getDarkCurrent() != null) {
			output = adjustForDarkCurrent(output, getCollectionTime());
		}

		if (outputRatio) {
			Double ratio = new Double(0);
			// find which col is which I0, It and Iref
			Double[] values = getI0It(output);

			ratio = values[1] / values[0];

			// always return a numerical value
			if (ratio.isInfinite() || ratio.isNaN()) {
				ratio = 0.0;
			}

			// append to output array
			output = correctCounts(output, values);
			output = ArrayUtils.add(output, ratio);
		}
		return output;
	}


	/*
	 * This should only be called when outputLogValues is set to true.
	 */
	private Double[] getI0It(double[] data) {
		if (timeChannelRequired) {
			return new Double[] { data[1], data[2] };
		}
		return new Double[] { data[0], data[1] };
	}

	private double[] correctCounts(final double[] output, final Double[] counts) {
		final int outIndex = (timeChannelRequired) ? 1 : 0;
		for (int i = 0; i < counts.length; i++) {
			output[i + outIndex] = counts[i];
		}
		return output;
	}
}
