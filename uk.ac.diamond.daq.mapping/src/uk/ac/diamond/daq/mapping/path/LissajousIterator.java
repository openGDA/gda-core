package uk.ac.diamond.daq.mapping.path;

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;

class LissajousIterator implements Iterator<IPosition> {

	private LissajousModel model;
	private double theta;
	private LissajousGenerator gen;

	private int pointsSoFar = 0;

	public LissajousIterator(LissajousGenerator gen) {
		this.model     = gen.getModel();
		this.gen       = gen;
		this.theta     = -model.getThetaStep();
	}

	@Override
	public boolean hasNext() {

		double[] pos = increment(model, this.theta);
		double t = pos[0];

		if (pointsSoFar >= model.getPoints()) return false;

		if (!gen.containsPoint(pos[1], pos[2])) {
			this.theta = t;
			return hasNext();
		}
		return true;
	}

	private static double[] increment(LissajousModel model, double theta) {

		theta += model.getThetaStep();

		double A = model.getBoundingBox().getFastAxisLength() / 2;
		double B = model.getBoundingBox().getSlowAxisLength() / 2;
		double xCentre = model.getBoundingBox().getFastAxisStart() + A;
		double yCentre = model.getBoundingBox().getSlowAxisStart() + B;

		double x = xCentre + A * Math.sin(model.getA() * theta + model.getDelta());
		double y = yCentre + B * Math.cos(model.getB() * theta);

		return new double[]{theta, x, y};
	}

	@Override
	public Point next() {

		double[] da  = increment(model, theta);
		double theta = da[0];
		double x     = da[1];
		double y     = da[2];

		if (pointsSoFar >= model.getPoints()) return null;
		this.theta = theta;

		if (gen.containsPoint(x, y)) {
			pointsSoFar++;
			return new Point(model.getFastAxisName(), -1, x, model.getSlowAxisName(), -1, y);
		} else {
			return next();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove");
	}
}
