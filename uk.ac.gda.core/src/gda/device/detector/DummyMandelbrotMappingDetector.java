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

	public enum OutputDimensions { ONE_D, TWO_D }

	public static final String VALUE_NAME = "mandelbrot_value";

	private static final int COLUMNS = 301;
	private static final int ROWS = 241;
	private static final int POINTS = 1000;
	private static final double MAX_X = 1.5;
	private static final double MAX_Y = 1.2;

	// Internal state
	private volatile int status = IDLE;
	private volatile NXDetectorData data;

	// Configurable fields
	private Scannable xPos;
	private Scannable yPos;
	private OutputDimensions outputDimensions = OutputDimensions.TWO_D;

	// TODO make this configurable (using some kind of detector parameters?)
	private int maxIterations = 501;

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

	public void setOutputDimensions(OutputDimensions outputDimensions) {
		this.outputDimensions = outputDimensions;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		if (outputDimensions == OutputDimensions.ONE_D) {
			return new int[] { POINTS };
		} else if (outputDimensions == OutputDimensions.TWO_D) {
			return new int[] { COLUMNS, ROWS };
		} else {
			throw new IllegalStateException("Unknown number of dimensions!");
		}
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

		final double a = ((Double) xPos.getPosition()).doubleValue();
		final double b = ((Double) yPos.getPosition()).doubleValue();

		// TODO normalise the value somehow?
		int value = maxIterations - mandelbrot(a, b, maxIterations, 10.0);

		data = new NXDetectorData(this);
		data.addData(getName(), VALUE_NAME, new NexusGroupData(value));
		data.setPlottableValue(VALUE_NAME, Double.valueOf(value));

		if (outputDimensions == OutputDimensions.ONE_D) {
			int[] juliaSetLine = calculateJuliaSetLine(a, b, 0.0, 0.0, MAX_X, POINTS);
			data.addData(getName(), "data", new NexusGroupData(juliaSetLine), null, Integer.valueOf(1));
		} else if (outputDimensions == OutputDimensions.TWO_D) {
			int[][] juliaSet = calculateJuliaSet(a, b, COLUMNS, ROWS);
			data.addData(getName(), "data", new NexusGroupData(juliaSet), null, Integer.valueOf(1));
		}

		status = IDLE;
	}

	/**
	 * Fill a Julia set around the origin for the value C = a + bi
	 */
	private int[][] calculateJuliaSet(final double a, final double b, int columns, int rows) {
		final double xStart = -MAX_X;
		final double xStop = MAX_X;
		final double yStart = -MAX_Y;
		final double yStop = MAX_Y;
		final double yStep = (yStop - yStart) / rows;
		double y;
		int[][] juliaSet = new int[rows][columns];
		for (int yIndex = 0; yIndex < rows; yIndex++) {
			y = yStart + yIndex * yStep;
			juliaSet[yIndex] = calculateJuliaSetLine(a, b, y, xStart, xStop, columns);
		}
		return juliaSet;
	}

	/**
	 * Fill a Julia set line between xStart and xStop at the given y value, for the value C = a + bi
	 */
	private int[] calculateJuliaSetLine(final double a, final double b, final double y, final double xStart, final double xStop, final int numPoints) {
		final double xStep = (xStop - xStart) / numPoints;
		double x;
		int[] juliaSetLine = new int[numPoints];
		for (int xIndex = 0; xIndex < numPoints; xIndex++) {
			x = xStart + xIndex * xStep;
			// TODO normalise the value somehow?
			juliaSetLine[xIndex] = maxIterations - julia(x, y, a, b, maxIterations, 10.0);
		}
		return juliaSetLine;
	}

	/**
	 * Iterations of f(z) = z^2 + C, where z = x + yi, C = a + bi, and initial z = 0
	 */
	private int mandelbrot(final double a, final double b, final int maxIterations, final double escapeRadius) {
		return julia(0.0, 0.0, a, b, maxIterations, escapeRadius);
	}

	/**
	 * Iterations of f(z) = z^2 + C, where z = x + yi and C = a + bi
	 */
	private int julia(double x, double y, final double a, final double b, final int maxIterations, final double escapeRadius) {
		int iteration = 0;
		while (iteration < maxIterations && x * x + y * y < escapeRadius * escapeRadius) {
			double tempX = x * x - y * y + a;
			y = 2 * x * y + b;
			x = tempX;
			iteration++;
		}
		return iteration;
	}
}
