/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.scannable;

import gda.device.DeviceException;
import gda.device.ScannableMotion;
import gda.device.scannable.ScannableMotionBase;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;


/**
 * in anti parallel mode (circular) we have the top phase positive for right circular
 *  
 */
public class I05Apple extends ScannableMotionBase {
	
	public final static String VERTICAL = "LV";
	public final static String HORIZONTAL = "LH";
	public final static String CIRCULAR_LEFT = "CL";
	public final static String CIRCULAR_RIGHT = "CR";

	private ScannableMotion gapScannable;
	private ScannableMotion upperPhaseScannable;
	private ScannableMotion lowerPhaseScannable;
	private String lowerPhaseDemandPV = "";
	private String upperPhaseDemandPV = "";
	
	private boolean moveSequenceRunning = false;
	
	private DeviceException threadException = null;
	
	private TrajectorySolver solver;
	
	private PolynomialFunction horizontalGapPolynomial = new PolynomialFunction(new double[] {0, 1});
	private PolynomialFunction verticalGapPolynomial = new PolynomialFunction(new double[] {0, 1});
	private PolynomialFunction circularGapPolynomial = new PolynomialFunction(new double[] {0, 1});
	private PolynomialFunction circularPhasePolynomial = new PolynomialFunction(new double[] {0, 1});
	private EpicsController epicsController;
	private Channel upperDemandChannel;
	private Channel lowerDemandChannel;
	
	class TrajectorySolver {
		Rectangle2D[] rectangles, smallrectanges;
		double smallvalue = 1e-10;
		
		/**
		 * For the application we treat the problem as a 2D graph with x being phase and y being gap
		 * 
		 * In the implementation we use awt primitives (maybe not be best choice?) and cheat to be able to 
		 * go on the boundary. We make the rectangles small by a a "smallvalue" for testing collisions, but 
		 * size them up for finding the corners.
		 * 
		 * Limitation:
		 * All top corners of the rectangles need to be visible, i.e. they cannot be inside other rectangles.
		 * 
		 * @param exclusionZone an array of rectangles to avoid in the trajectory, going around "the top".
		 */
		public TrajectorySolver(Rectangle2D[] exclusionZone) {
			rectangles = exclusionZone;
			smallrectanges = rectangles.clone();
			for (int i = 0; i < smallrectanges.length; i++) {
				smallrectanges[i].setRect(smallrectanges[i].getMinX()+smallvalue, smallrectanges[i].getMinY(), smallrectanges[i].getWidth()-2*smallvalue, smallrectanges[i].getHeight()-smallvalue);
			}
		}
		
		public List<Line2D> avoidZone(Line2D traj) {
			List<Line2D> result = new ArrayList<Line2D>();
			for(Rectangle2D rect: smallrectanges) {
				Point2D topleft = new Point2D.Double(rect.getMinX(),rect.getMaxY());
				Point2D topright = new Point2D.Double(rect.getMaxX(),rect.getMaxY());
				if (traj.getP1().equals(topright) || traj.getP1().equals(topleft) || traj.getP2().equals(topleft) || traj.getP2().equals(topright)) {
					continue;
				}
				if (traj.intersects(rect)) {
					Line2D[] split = split(traj, rect);
					result.addAll(avoidZone(split[0]));
					result.addAll(avoidZone(split[1]));
					return result;
				}
			}
			if (result.size() == 0)
				result.add(traj);
			return result;
		}
		
		public Line2D[] split(Line2D traj, Rectangle2D rect) {
			Line2D[] result = new Line2D[] { (Line2D) traj.clone(), (Line2D) traj.clone() };
			Point2D midpoint;
			Point2D topleft = new Point2D.Double(rect.getMinX()-smallvalue,rect.getMaxY()+smallvalue);
			Point2D topright = new Point2D.Double(rect.getMaxX()+smallvalue,rect.getMaxY()+smallvalue);
			
			if (traj.getY1() <= traj.getY2()) {
				if (traj.getP1().distance(topleft) <= traj.getP1().distance(topright)) {
					midpoint = topleft;
				} else {
					midpoint = topright;
				}
			} else {
				if (traj.getP2().distance(topleft) <= traj.getP2().distance(topright)) {
					midpoint = topleft;
				} else {
					midpoint = topright;
				}
			}
			result[0].setLine(result[0].getP1(), midpoint);
			result[1].setLine(midpoint, result[1].getP2());
			return result;
		}
	}
	
	public I05Apple() {
		setInputNames(new String[] { "gap", "polarisation" });
		setExtraNames(new String[] { "phase" });
		setOutputFormat(new String[] {"%5.3f", "%s", "%5.3f"});
	}
	
	
	@Override
	public void configure() throws FactoryException {
		super.configure();
		epicsController = EpicsController.getInstance();
		try {
			if (upperPhaseDemandPV != null && !upperPhaseDemandPV.isEmpty())
				upperDemandChannel = epicsController.createChannel(upperPhaseDemandPV);
			if (lowerPhaseDemandPV != null && !lowerPhaseDemandPV.isEmpty())
				lowerDemandChannel = epicsController.createChannel(lowerPhaseDemandPV);
		} catch (CAException e) {
			throw new FactoryException("error connecting to phase demand pvs", e);
		} catch (TimeoutException e) {
			throw new FactoryException("timeout connecting to phase demand pvs", e);
		}
	}

	protected static Point2D[] trajectoryToPointArray(List<Line2D> traj) {
		Point2D lastPoint = traj.get(0).getP1();
		List<Point2D> result = new ArrayList<Point2D>();
		result.add(lastPoint);
		for (Line2D line2d : traj) {
			if (!lastPoint.equals(line2d.getP1()))
					throw new IllegalArgumentException("found unlinked trajectory");
			lastPoint = line2d.getP2();
			result.add(lastPoint);
		}
		return result.toArray(new Point2D[] {});
	}

	public void checkPhases() throws DeviceException {
		if (upperPhaseScannable.isAt(lowerPhaseScannable.getPosition()))
			return;
		throw new DeviceException("upper and lower phase out of sync");
	}
	
	public double findEnergyForCircularGap(double gap) {
		PolynomialFunction poly = circularGapPolynomial.add(new PolynomialFunction(new double[] { -gap }));
		poly = poly.multiply(poly);
		UnivariateOptimizer optimizer = new BrentOptimizer(1e-6, 1e-4);
		
		UnivariatePointValuePair pointValuePair = optimizer.optimize(new MaxEval(100), 
				new UnivariateObjectiveFunction(poly),
				GoalType.MINIMIZE,
				new SearchInterval(10, 1000));
		
		return pointValuePair.getPoint();
	}
	
	public double getPhaseForGap(double gap, String polarisation) throws DeviceException {
		if (HORIZONTAL.equalsIgnoreCase(polarisation))
			return 0;
		if (VERTICAL.equalsIgnoreCase(polarisation))
			return 70;
		
		Double energy = findEnergyForCircularGap(gap);
		Double phase = circularPhasePolynomial.value(energy);
		if (CIRCULAR_RIGHT.equalsIgnoreCase(polarisation))
			return phase;
		if (CIRCULAR_LEFT.equalsIgnoreCase(polarisation))
			return phase * -1;
		throw new DeviceException("unknown polarisation demanded");
	}
	
	public double getGapFor(double energy, String polarisation) throws DeviceException {
		if (HORIZONTAL.equalsIgnoreCase(polarisation))
			return horizontalGapPolynomial.value(energy);
		if (VERTICAL.equalsIgnoreCase(polarisation))
			return verticalGapPolynomial.value(energy);
		if (CIRCULAR_RIGHT.equalsIgnoreCase(polarisation))
			return circularGapPolynomial.value(energy);
		if (CIRCULAR_LEFT.equalsIgnoreCase(polarisation))
			return circularGapPolynomial.value(energy);
		throw new DeviceException("unknown or unconfigured polarisation demanded");
	}
	
	public void combinedMove(double newenergy, String newpol) throws DeviceException {
		checkPhases();
		
		double currentPhase = (Double) lowerPhaseScannable.getPosition();
		double currentGap = (Double) gapScannable.getPosition();
		double newgap = getGapFor(newenergy, newpol);
		double newphase = getPhaseForGap(newgap, newpol);
		
		final Point2D[] pointArray;
		if (solver != null) {
			List<Line2D> avoidZone = solver.avoidZone(new Line2D.Double(currentPhase, currentGap, newphase, newgap));
			pointArray = trajectoryToPointArray(avoidZone);
		} else {
			pointArray = new Point2D[] { new Point2D.Double(currentPhase, currentGap), new Point2D.Double(newphase, newgap) };
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					runPast(pointArray);
				} catch (InterruptedException ie) {
					threadException = new DeviceException("interrupted while moving undulator", ie);
				} catch (DeviceException e) {
					threadException = e;
				} catch (TimeoutException e) {
					threadException = new DeviceException("timeout while talking to undulator", e);
				} catch (CAException e) {
					threadException = new DeviceException("ca exception while moving undulator", e);
				}
				
			}
		}).start();
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// sleeping to allow this device to become busy
		}
	}

	private void setPhaseDemandsTo(Double phase) throws TimeoutException, CAException, InterruptedException {
		if (upperDemandChannel != null)
			epicsController.caputWait(upperDemandChannel, phase);
		if (lowerDemandChannel != null)
			epicsController.caputWait(lowerDemandChannel, phase);
	}
	
	private void runPast(Point2D[] pointArray) throws DeviceException, InterruptedException, TimeoutException, CAException {
		try {
			moveSequenceRunning = true;
		for (int i = 1; i < pointArray.length; i++) {
			setPhaseDemandsTo(pointArray[i].getX());
			gapScannable.asynchronousMoveTo(pointArray[i].getY());
			lowerPhaseScannable.asynchronousMoveTo(pointArray[i].getX());
			upperPhaseScannable.asynchronousMoveTo(pointArray[i].getX());
			lowerPhaseScannable.waitWhileBusy();
			upperPhaseScannable.waitWhileBusy();
			gapScannable.waitWhileBusy();
		}
		} finally {
			moveSequenceRunning = false;
		}
	}

	public String getCurrentPolarisation() throws DeviceException {
		checkPhases();
		Double gap = (Double) gapScannable.getPosition();
		for (String polarisation : new String[] {HORIZONTAL, VERTICAL, CIRCULAR_LEFT, CIRCULAR_RIGHT}) {
			if (lowerPhaseScannable.isAt(getPhaseForGap(gap, polarisation)))
				return polarisation;
			if (VERTICAL.equalsIgnoreCase(polarisation) && lowerPhaseScannable.isAt(-1*getPhaseForGap(gap, polarisation))) {
				return polarisation;
			}
		}
		throw new DeviceException("found undefined id setting");
	}
	
	@Override
	public Object getPosition() throws DeviceException {
//		checkPhases(); // don't check - so we have something to report in any case
		String polarisation = "unknown";
		try {
			polarisation = getCurrentPolarisation();
		} catch (Exception e) {
			// ignored
		}
		return new Object[] { gapScannable.getPosition(), polarisation, upperPhaseScannable.getPosition()};
	}
	
	private synchronized void checkThreadException() throws DeviceException {
		if (threadException == null)
			return;
		DeviceException e2throw = threadException;
		threadException = null;
		throw e2throw;
	}
	
	@Override
	public boolean isBusy() throws DeviceException {
		checkThreadException();
		if (moveSequenceRunning)
			return true;
		return gapScannable.isBusy() || upperPhaseScannable.isBusy() || lowerPhaseScannable.isBusy();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		if (position instanceof Number) {
			combinedMove(((Number) position).doubleValue(), getCurrentPolarisation());
			return;
		}
		double energy; 
		String pol = null;
		if (position instanceof List)
			position = ((List) position).toArray();
		try {
			Object[] arr = (Object []) position;
			if (arr[0] instanceof Number)
				energy = ((Number) arr[0]).doubleValue();
			else 
				energy = Double.parseDouble(arr[0].toString());
			if (arr[1] != null)
				pol = arr[1].toString();
		} catch (Exception e) {
			throw new DeviceException("expecting number energy and string polarisation");
		}
		combinedMove(energy, pol != null ? pol : getCurrentPolarisation());
	}
	
	public void setExclusionZone(Rectangle2D[] zone) {
		if (zone == null || zone.length == 0)
			solver = null;
		else
			solver = new TrajectorySolver(zone);
	}
	
	public Rectangle2D[] getExclusionZone() {
		if (solver == null)
			return null;
		return solver.rectangles;
	}

	public PolynomialFunction getHorizontalGapPolynomial() {
		return horizontalGapPolynomial;
	}

	public void setHorizontalGapPolynomial(PolynomialFunction horizontalGapPolynomial) {
		this.horizontalGapPolynomial = horizontalGapPolynomial;
	}

	public PolynomialFunction getVerticalGapPolynomial() {
		return verticalGapPolynomial;
	}

	public void setVerticalGapPolynomial(PolynomialFunction verticalGapPolynomial) {
		this.verticalGapPolynomial = verticalGapPolynomial;
	}

	public String getLowerPhaseDemandPV() {
		return lowerPhaseDemandPV;
	}

	public void setLowerPhaseDemandPV(String lowerPhaseDemand) {
		this.lowerPhaseDemandPV = lowerPhaseDemand;
	}

	public String getUpperPhaseDemandPV() {
		return upperPhaseDemandPV;
	}

	public void setUpperPhaseDemandPV(String upperPhaseDemand) {
		this.upperPhaseDemandPV = upperPhaseDemand;
	}

	public PolynomialFunction getCircularGapPolynomial() {
		return circularGapPolynomial;
	}

	public void setCircularGapPolynomial(PolynomialFunction circularGapPolynomial) {
		this.circularGapPolynomial = circularGapPolynomial;
	}

	public PolynomialFunction getCircularPhasePolynomial() {
		return circularPhasePolynomial;
	}

	public void setCircularPhasePolynomial(PolynomialFunction circularPhasePolynomial) {
		this.circularPhasePolynomial = circularPhasePolynomial;
	}
	
	public ScannableMotion getGapScannable() {
		return gapScannable;
	}

	public void setGapScannable(ScannableMotion gapScannable) {
		this.gapScannable = gapScannable;
	}

	public ScannableMotion getUpperPhaseScannable() {
		return upperPhaseScannable;
	}

	public void setUpperPhaseScannable(ScannableMotion upperPhaseScannable) {
		this.upperPhaseScannable = upperPhaseScannable;
	}

	public ScannableMotion getLowerPhaseScannable() {
		return lowerPhaseScannable;
	}

	public void setLowerPhaseScannable(ScannableMotion lowerPhaseScannable) {
		this.lowerPhaseScannable = lowerPhaseScannable;
	}
}