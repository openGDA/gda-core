package uk.ac.diamond.daq.mapping.path;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;

class SpiralIterator implements Iterator<IPosition> {

	// Constant parameters
	private final SpiralGenerator gen;
	private final String xName;
	private final String yName;
	private final double alpha1;
	private final double beta1;
	private final double xCentre;
	private final double yCentre;
	private final double maxRadius;

	// Mutable state
	private SpiralPosition currentPosition;
	private SpiralPosition nextPosition;

	/**
	 * Simple class to hold state about a position along the spiral
	 */
	private class SpiralPosition {
		final int n; // a counter to represent distance along the spiral (NOT the number of points actually generated, if some fall outside the bounding region)
		final double x;
		final double y;

		SpiralPosition(int n, double x, double y) {
			this.n = n;
			this.x = x;
			this.y = y;
		}
	}

	public SpiralIterator(SpiralGenerator gen) {
		SpiralModel model = gen.getModel();
		this.gen = gen;
		this.xName = model.getFastAxisName();
		this.yName = model.getSlowAxisName();
		this.alpha1 = Math.sqrt(4 * Math.PI); // works nicely between about 0.2 and 1.0 times this value
		this.beta1 = model.getScale() / (2 * Math.PI);

		double radiusX = model.getBoundingBox().getFastAxisLength() / 2;
		double radiusY = model.getBoundingBox().getSlowAxisLength() / 2;
		xCentre = model.getBoundingBox().getFastAxisStart() + radiusX;
		yCentre = model.getBoundingBox().getSlowAxisStart() + radiusY;
		maxRadius = Math.sqrt(radiusX * radiusX + radiusY * radiusY);

		currentPosition = new SpiralPosition(-1, Double.NaN, Double.NaN);
	}

	@Override
	public boolean hasNext() {

		if (nextPosition == null) {
			nextPosition = increment(currentPosition);

			if (nextPosition == null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Point next() {

		if (nextPosition == null) {
			nextPosition = increment(currentPosition);

			if (nextPosition == null) {
				throw new NoSuchElementException("No more points available");
			}
		}

		currentPosition = nextPosition;
		nextPosition = null;

		// Ideally, we would create a point with two position coordinates (for X and Y motor positions) but only one
		// index (for the position along the spiral). Because IPosition requires an index for each named axis, we set
		// the X index to the "real" index, and the Y index to 0. This will lead to a scan file with the positions
		// written in a block of size 1 x n, rather than a stack of size n, which currently cannot be visualised
		// properly but is the closest approximation available to the correct structure.
		return new Point(xName, currentPosition.n, currentPosition.x, yName, 0, currentPosition.y);
	}

	// For points algorithm, see /dls_sw/i13-1/scripts/Ptycholib/scan_functions.py#spiral_scan_ROI_positions()
	//
	// In python:
	// alpha1 = sqrt(4 * pi)
	// beta1 = dr/(2 * pi)
	// kk=0.
	// while True:
	//   theta = alpha1 * sqrt(kk)
	//   kk += 1.
	//   r = beta1 * theta
	//   if r > rmax:
	//     break
	//   x,y = r * sin(theta), r * cos(theta)
	private SpiralPosition increment(SpiralPosition lastPosition) {
		int n = lastPosition.n;
		double x, y;
		do {
			n++;
			double theta = alpha1 * Math.sqrt(n);
			double radius = beta1 * theta;

			if (Math.abs(radius) > maxRadius) {
				return null; // no more points possible
			}

			x = xCentre + radius * Math.sin(theta);
			y = yCentre + radius * Math.cos(theta);
		} while (!gen.containsPoint(x, y));

		return new SpiralPosition(n, x, y);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove");
	}
}
