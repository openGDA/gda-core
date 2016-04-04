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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A dummy detector which must be set up with references to two Scannables representing X and Y positions. When used in a step scan, this detector generates a
 * value of 0 if the point (x, y) is in the Mandelbrot set, and greater than zero otherwise.
 * <p>
 * Note: values will always be high if used at (x, y) positions more than 2 units away from the origin.
 */
public class DummyMandelbrotMappingDetector extends DetectorBase implements NexusDetector {

	public enum OutputDimensions { ONE_D, TWO_D }

	public static final String VALUE_NAME = "mandelbrot_value";

	// Constants
	private static final int MAX_ITERATIONS = 500;
	private static final double ESCAPE_RADIUS = 10.0;
	private static final int COLUMNS = 301; // for the 2D Julia dataset
	private static final int ROWS = 241; // for the 2D Julia dataset
	private static final int POINTS = 1000; // for the 1D Julia dataset
	private static final double MAX_REAL_COORDINATE = 1.5; // for the 1D and 2D Julia datasets
	private static final double MAX_IMAGINARY_COORDINATE = 1.2; // for the 2D Julia dataset

	// Internal state
	private volatile int status = IDLE;
	private volatile NXDetectorData data;
	private ExecutorService executor;

	// Configurable fields
	private Scannable realAxisPositioner;
	private Scannable imaginaryAxisPositioner;
	private OutputDimensions outputDimensions = OutputDimensions.TWO_D;

	public DummyMandelbrotMappingDetector() {
		super();
		setInputNames(new String[] { /* none */});
		setExtraNames(new String[] { VALUE_NAME });
		setOutputFormat(new String[] { "%s" });
	}

	public void setRealAxisPositioner(Scannable xPos) {
		this.realAxisPositioner = xPos;
	}

	public void setImaginaryAxisPositioner(Scannable yPos) {
		this.imaginaryAxisPositioner = yPos;
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
		if (status == IDLE) {
			return data;
		}
		// readout() was called too soon, or perhaps there was an error in the calculation thread
		throw new DeviceException("Detector is not ready to readout yet");
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public void atScanStart() throws DeviceException {
		executor = Executors.newSingleThreadExecutor();
		status = IDLE;
	}

	@Override
	public void atScanEnd() throws DeviceException {
		executor.shutdownNow();
		try {
			if (executor.awaitTermination(1, TimeUnit.SECONDS)) {
				// Executor finished cleanly
				executor = null;
			} else {
				throw new DeviceException("Timed out waiting for executor to terminate");
			}
		} catch (InterruptedException e) {
			throw new DeviceException("Interrupted while waiting for executor to terminate", e);
		}
	}

	@Override
	public void collectData() throws DeviceException {

		if (executor == null) {
			throw new DeviceException("This detector must be used in a scan");
		}
		if (status != IDLE) {
			throw new DeviceException("Detector is not ready to collect data");
		}
		status = BUSY;

		final long startTime = System.nanoTime();
		final long targetDuration = (long) getCollectionTime() * 1000000000; // nanoseconds
		final double a = ((Double) realAxisPositioner.getPosition()).doubleValue();
		final double b = ((Double) imaginaryAxisPositioner.getPosition()).doubleValue();

		executor.execute(new Runnable() {
			@Override
			public void run() {

				double value = mandelbrot(a, b);

				data = new NXDetectorData(DummyMandelbrotMappingDetector.this);
				data.addData(getName(), VALUE_NAME, new NexusGroupData(value));
				data.setPlottableValue(VALUE_NAME, Double.valueOf(value));

				if (outputDimensions == OutputDimensions.ONE_D) {
					double[] juliaSetLine = calculateJuliaSetLine(a, b, 0.0, 0.0, MAX_REAL_COORDINATE, POINTS);
					data.addData(getName(), "data", new NexusGroupData(juliaSetLine), null, Integer.valueOf(1));
				} else if (outputDimensions == OutputDimensions.TWO_D) {
					double[][] juliaSet = calculateJuliaSet(a, b, COLUMNS, ROWS);
					data.addData(getName(), "data", new NexusGroupData(juliaSet), null, Integer.valueOf(1));
				}

				long currentTime = System.nanoTime();
				long duration = currentTime - startTime;
				if (duration < targetDuration) {
					long millisToWait = (targetDuration - duration) / 1000000;
					try {
						Thread.sleep(millisToWait);
					} catch (InterruptedException e) {
						// No cleanup needed since this task is about to finish; just restore interrupted status
						Thread.currentThread().interrupt();
					}
				}

				status = IDLE;
			}
		});
	}

	/**
	 * Fill a Julia set around the origin for the value C = a + bi
	 */
	private double[][] calculateJuliaSet(final double a, final double b, int columns, int rows) {
		final double xStart = -MAX_REAL_COORDINATE;
		final double xStop = MAX_REAL_COORDINATE;
		final double yStart = -MAX_IMAGINARY_COORDINATE;
		final double yStop = MAX_IMAGINARY_COORDINATE;
		final double yStep = (yStop - yStart) / (rows - 1);
		double y;
		double[][] juliaSet = new double[rows][columns];
		for (int yIndex = 0; yIndex < rows; yIndex++) {
			y = yStart + yIndex * yStep;
			juliaSet[yIndex] = calculateJuliaSetLine(a, b, y, xStart, xStop, columns);
		}
		return juliaSet;
	}

	/**
	 * Fill a Julia set line between xStart and xStop at the given y value, for the value C = a + bi
	 */
	private double[] calculateJuliaSetLine(final double a, final double b, final double y, final double xStart, final double xStop, final int numPoints) {
		final double xStep = (xStop - xStart) / (numPoints - 1);
		double x;
		double[] juliaSetLine = new double[numPoints];
		for (int xIndex = 0; xIndex < numPoints; xIndex++) {
			x = xStart + xIndex * xStep;
			juliaSetLine[xIndex] = julia(x, y, a, b);
		}
		return juliaSetLine;
	}

	/**
	 * Iterations of f(z) = z^2 + C, where z = x + yi, C = a + bi, and initial z = 0
	 */
	private double mandelbrot(final double a, final double b) {
		return julia(0.0, 0.0, a, b);
	}

	/**
	 * Iterations of f(z) = z^2 + C, where z = x + yi and C = a + bi
	 */
	private double julia(double x, double y, final double a, final double b) {
		int iteration = 0;
		double xSquared, ySquared, tempX;
		double escapeRadiusSquared = ESCAPE_RADIUS * ESCAPE_RADIUS;
		do {
			xSquared = x * x;
			ySquared = y * y;
			tempX = xSquared - ySquared + a;
			y = 2 * x * y + b;
			x = tempX;
			iteration++;
		} while (iteration < MAX_ITERATIONS && xSquared + ySquared < escapeRadiusSquared);

		double modulus = Math.sqrt(x * x + y * y);

		// If modulus > 1.0, normalise the result
		// (Theoretically, I think this should make the value roughly independent of MAX_ITERATIONS and ESCAPE_RADIUS)
		if (modulus > 1.0) {
			return iteration - (Math.log(Math.log(modulus)) / Math.log(2.0));
		}
		// Otherwise just return the iteration count
		return iteration;
	}
}
