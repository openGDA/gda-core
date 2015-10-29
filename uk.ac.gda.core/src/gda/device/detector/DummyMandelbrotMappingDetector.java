/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * A dummy detector which must be set up with references to two Scannables representing X and Y positions. When used in a step scan, this detector generates a
 * value of 0 if the point (x, y) is in the Mandelbrot set, and greater than zero otherwise.
 * <p>
 * Note: values will always be high if used at (x, y) positions more than 2 units away from the origin.
 */
public class DummyMandelbrotMappingDetector extends DetectorBase implements NexusDetector {

	public static final String VALUE_NAME = "mandelbrot_value";

	private int status = IDLE;
	private NXDetectorData data;
	private Scannable xPos;
	private Scannable yPos;
	private int maxIterations = 1001;

	public DummyMandelbrotMappingDetector() {
		super();
		setInputNames(new String[] { /* none */});
		setExtraNames(new String[] { VALUE_NAME });
		setOutputFormat(new String[] { "%s" });
	}

	public void setxPos(Scannable xPos) {
		this.xPos = xPos;
	}

	public void setyPos(Scannable yPos) {
		this.yPos = yPos;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[] { 1, 1 }; // returning { 1 } caused scans to fail; no idea why!
	}

	@Override
	public int getStatus() throws DeviceException {
		return status;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		return data;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public void collectData() throws DeviceException {
		status = BUSY;

		double x = ((Double) xPos.getPosition()).doubleValue();
		double y = ((Double) yPos.getPosition()).doubleValue();

		int value = mandelbrot(x, y, maxIterations, 10.0);
		float[] spectrum = new float[maxIterations];
		for (int i = 0; i < spectrum.length; i++) {
			double diff = i - value;
			spectrum[i] = (float) Math.exp(-(diff * diff) / (2 * maxIterations));
		}
		data = new NXDetectorData(this);
		data.addData(getName(), "data", new NexusGroupData(spectrum), null, Integer.valueOf(1));
		data.addData(getName(), VALUE_NAME, new NexusGroupData(value));
		data.setPlottableValue(VALUE_NAME, Double.valueOf(value));

		status = IDLE;
	}

	private int mandelbrot(final double x, final double y, final int maxIterations, final double escapeRadius) {
		double workingX = 0.0;
		double workingY = 0.0;
		int iteration = 0;
		while (iteration < maxIterations && workingX * workingX + workingY * workingY < escapeRadius * escapeRadius) {
			double tempX = workingX * workingX - workingY * workingY + x;
			workingY = 2 * workingX * workingY + y;
			workingX = tempX;
			iteration++;
		}
		return maxIterations - iteration;
	}
}
