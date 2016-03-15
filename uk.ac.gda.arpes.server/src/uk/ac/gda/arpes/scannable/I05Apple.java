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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for controlling the ID on I05 the unique feature is calculating a move path through gap and phase space which avoids the exclusion zone. in anti
 * <p>
 * Circular polarisation we have the top phase positive for right circular
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

	private PolynomialFunction horizontalGapPolynomial = new PolynomialFunction(new double[] { 0, 1 });
	private PolynomialFunction verticalGapPolynomial = new PolynomialFunction(new double[] { 0, 1 });
	private PolynomialFunction circularGapPolynomial = new PolynomialFunction(new double[] { 0, 1 });
	private PolynomialFunction circularPhasePolynomial = new PolynomialFunction(new double[] { 0, 1 });
	private EpicsController epicsController;
	private Channel upperDemandChannel;
	private Channel lowerDemandChannel;
	private double phaseTolerance;

	private static Logger logger = LoggerFactory.getLogger(I05Apple.class);

	class PGMove { // a move is defined by the change in position and whether it's Gap-then-Phase or Phase-then-Gap
		public String moveOrder;
		public Line2D moveVector;

		public PGMove(String mo, Line2D ln) {
			moveOrder = mo;
			moveVector = ln;
		}
	}

	class Quadrant { // identify Quadrant by 1/-1 pair labels for sign of x y coordinates in the given Quadrant
		public int xsign;
		public int ysign;

		public Quadrant(int xs, int ys) {
			xsign = xs;
			ysign = ys;
		}

		public boolean equals(Quadrant other) {
			return (this.xsign == other.xsign && this.ysign == other.ysign);
		}
	}

	class TrajectorySolver {

		/**
		 * For the application we treat the problem as a 2D graph with Phase(=X) and Gap(=Y). Exclusion area avoidance is based on moving Phase(X) and Gap(Y) in
		 * sequence rather than concurrently. Moving concurrently must model or make assumptions about relative speed of Gap & Phase movement, so best avoided.
		 * Avoidance trajectory algorithm is based on splitting into 8 cases: which Phase half-space (+ve or -ve) and which cartesian Quadrant the move *vector*
		 * is in. In all 8 cases except 2, the order of movement doesn't matter, so arbitrarily choose to do Gap then Phase. If its one of these 2 cases, do
		 * Phase then Gap. See getGapPhaseOrder method. exclusionZone: an array of rectangles to avoid in the trajectory, going around "the top". In the
		 * implementation we use awt primitives (maybe not be best choice?) Limitation: getGapPhaseOrder trajectory solver is very specific, assumes exclusion
		 * is of symmetric "tower-of-hanoi" shape about the Phase=0 line
		 */

		Rectangle2D[] rectangles;
		double towerTop = 0.0; // store highest point of tower of rectangles

		Quadrant qd1 = new Quadrant(1, 1); // identify Quadrants by whether X(=Phase) and Y(=Gap) are negative or positive
		Quadrant qd2 = new Quadrant(-1, 1);
		Quadrant qd3 = new Quadrant(-1, -1);
		Quadrant qd4 = new Quadrant(1, -1);

		private int qsign(double x) { // include 0 as part of positive Quadrant
			return Math.signum(x) >= 0.0 ? 1 : -1;
		}

		private boolean phaseSide(double xa, double xb, int side) { // include 0 as part of both phase half-space sides
			boolean res;
			if ((Math.signum(xa) == side && Math.signum(xb) == side) || (Math.signum(xa) == side && xb == 0.0) || (Math.signum(xb) == side && xa == 0.0)) {
				res = true;
			} else {
				res = false;
			}
			return res;
		}

		public TrajectorySolver(Rectangle2D[] exclusionZone) {
			logger.info("Instantiating trajectory solver for ID move");
			rectangles = exclusionZone;
			for (Rectangle2D r : rectangles) { // change to reduction one-liner when we go to Java8
				towerTop = Math.max(towerTop, r.getMaxY());
			}
			logger.info("Highest point of rectangles tower=" + towerTop);
		}

		private List<Line2D> splitMoveAtZeroPhase(Line2D ln) {
			// small performance optimisation: cross zero line near Gap(=Y) start and end points

			Point2D zcp = new Point2D.Double(0.0, Math.max(towerTop, (ln.getP1().getY() + ln.getP2().getY()) / 2.0)); // cross at average Y
			List<Line2D> linePair = new ArrayList<Line2D>();
			linePair.add(new Line2D.Double(ln.getP1(), zcp));
			linePair.add(new Line2D.Double(zcp, ln.getP2()));
			return linePair;
		}

		protected List<PGMove> getGapPhaseOrder(Line2D ln, int recDepth) {

			logger.info("Computing Phase-Gap trajectory: for" + ln.getP1() + "--->" + ln.getP2());

			List<PGMove> res = new ArrayList<PGMove>(); // initialise result as empty list, returning empty indicates unable to produce valid path

			// some defensive checks for undesirable input cases:
			if (ln.getP1().equals(ln.getP2())) { // 1) end points differ
				return res;
			}
			if (recDepth > 1) { // 2) recurse only once
				return res;
			}
			for (Rectangle2D r : rectangles) { // 3) neither end of move is in exclusion zone
				if (r.contains(ln.getP1()) || r.contains(ln.getP2())) {
					return res;
				}
			}

			if ((Math.signum(ln.getP1().getX()) == Math.signum(ln.getP2().getX()) || // move doesn't cross Phase=0 line
					ln.getP1().getX() == 0.0 || // in this context, startpoint or endpoint on the Phase=0 line doesn't count as crossing it
			ln.getP2().getX() == 0.0)) {

				Boolean moveWithinPosPhase = phaseSide(ln.getP1().getX(), ln.getP2().getX(), 1);
				logger.info("move within zero-inclusive PosPhase:" + moveWithinPosPhase.toString());
				Boolean moveWithinNegPhase = phaseSide(ln.getP1().getX(), ln.getP2().getX(), -1);
				logger.info("move within zero-inclusive NegPhase:" + moveWithinNegPhase.toString());

				Quadrant qd = new Quadrant(qsign(ln.getP2().getX() - ln.getP1().getX()), qsign(ln.getP2().getY() - ln.getP1().getY()));
				logger.info("move vector Quadrant XSIGN:" + Integer.toString(qd.xsign) + ", YSIGN:" + Integer.toString(qd.xsign));

				String moveDir = "";
				if (qd.equals(qd1)) {
					moveDir = "GP";
				} else if (qd.equals(qd2)) {
					moveDir = "GP";
				} else if (qd.equals(qd3)) {
					moveDir = moveWithinNegPhase ? "PG" : "GP";
				} else if (qd.equals(qd4)) {
					moveDir = moveWithinPosPhase ? "PG" : "GP";
				}

				res.add(new PGMove(moveDir, ln));
				logger.info("Computed Phase-Gap trajectory: " + moveDir + " " + ln.getP1() + "--->" + ln.getP2());

			} else { // move crosses between negative and positive phase => split the move into two moves

				List<Line2D> lns = splitMoveAtZeroPhase(ln);
				res.add(getGapPhaseOrder(lns.get(0), recDepth + 1).get(0));
				res.add(getGapPhaseOrder(lns.get(1), recDepth + 1).get(0));
				logger.info("Computed Phase-Gap trajectory: FIRST:" + res.get(0).moveOrder + " " + res.get(0).moveVector.getP1() + "--->"
						+ res.get(0).moveVector.getP2() + " THEN:" + res.get(1).moveOrder + " " + res.get(1).moveVector.getP1() + "--->"
						+ res.get(1).moveVector.getP2());
			}
			return res;
		}
	}

	public I05Apple() {
		setInputNames(new String[] { "gap", "polarisation" });
		setExtraNames(new String[] { "phase" });
		setOutputFormat(new String[] { "%5.3f", "%s", "%5.3f" });
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

	/**
	 * This function checks that the phase axis are at the same value to within phaseTolerance.
	 * <p>
	 * This is useful as GDA determined the current polarisation by using the current phase axis position and if these differ the polarisation will be
	 * undefined.
	 *
	 * @throws DeviceException
	 */
	private void checkPhases() throws DeviceException {
		double upperPhasePos = (double) upperPhaseScannable.getPosition();
		double lowerPhasePos = (double) lowerPhaseScannable.getPosition();
		if (Math.abs(upperPhasePos - lowerPhasePos) > phaseTolerance) {
			logger.error("Upper and Lower position differ by more than tollerence ({}). Upper phase: {}, Lower phase: {}", phaseTolerance, upperPhasePos,
					lowerPhasePos);
			throw new DeviceException("Upper and Lower phase out of sync.");
		}
		return;
	}

	private double findEnergyForCircularGap(double gap) {
		PolynomialFunction poly = circularGapPolynomial.add(new PolynomialFunction(new double[] { -gap }));
		poly = poly.multiply(poly);
		UnivariateOptimizer optimizer = new BrentOptimizer(1e-6, 1e-4);

		UnivariatePointValuePair pointValuePair = optimizer.optimize(new MaxEval(100), new UnivariateObjectiveFunction(poly), GoalType.MINIMIZE,
				new SearchInterval(10, 1000));

		return pointValuePair.getPoint();
	}

	protected double getPhaseForGap(double gap, String polarisation) throws DeviceException {
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
	
	private void combinedMove(double newenergy, String newpol) throws DeviceException {
		checkPhases();

		double currentPhase = (Double) lowerPhaseScannable.getPosition();
		double currentGap = (Double) gapScannable.getPosition();
		double newgap = getGapFor(newenergy, newpol);
		double newphase = getPhaseForGap(newgap, newpol);

		final List<PGMove> moveLst;
		moveLst = solver.getGapPhaseOrder(new Line2D.Double(currentPhase, currentGap, newphase, newgap), 0);
		if (!moveLst.isEmpty()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						runPast(moveLst);
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
		} else {
			logger.warn("Trajectory solver returned empty trajectory path: unable to produce valid trajectory, no ID move request issued");
			throw new DeviceException("Trajectory solver unable to produce valid trajectory");
		}
	}

	private void setPhaseDemandsTo(Double phase) throws TimeoutException, CAException, InterruptedException {
		if (upperDemandChannel != null)
			epicsController.caputWait(upperDemandChannel, phase);
		if (lowerDemandChannel != null)
			epicsController.caputWait(lowerDemandChannel, phase);
	}

	private void runPast(List<PGMove> moveLst) throws DeviceException, InterruptedException, TimeoutException, CAException {
		try {
			moveSequenceRunning = true;

			for (PGMove pgMv : moveLst) {

				// to avoid going through exclusion zone run phase and gap in sequence (i.e. not in parallel)
				// whether it's phase-then-gap (PG) or gap-then-phase (GP) depends on direction and
				// location of move in phase-gap space
				if (pgMv.moveOrder.equals("GP")) {
					logger.info("Performing ID move: Gap-then-Phase");

					gapScannable.asynchronousMoveTo(pgMv.moveVector.getP2().getY());
					gapScannable.waitWhileBusy();
					logger.info("Performing ID move:Gap move Completed");

					setPhaseDemandsTo(pgMv.moveVector.getP2().getX());
					lowerPhaseScannable.asynchronousMoveTo(pgMv.moveVector.getP2().getX());
					upperPhaseScannable.asynchronousMoveTo(pgMv.moveVector.getP2().getX());
					lowerPhaseScannable.waitWhileBusy();
					upperPhaseScannable.waitWhileBusy();
					logger.info("Performing ID move:Phase move Completed");

				} else if (pgMv.moveOrder.equals("PG")) {
					logger.info("Performing ID move: Phase-then-Gap");

					setPhaseDemandsTo(pgMv.moveVector.getP2().getX());
					lowerPhaseScannable.asynchronousMoveTo(pgMv.moveVector.getP2().getX());
					upperPhaseScannable.asynchronousMoveTo(pgMv.moveVector.getP2().getX());
					lowerPhaseScannable.waitWhileBusy();
					upperPhaseScannable.waitWhileBusy();
					logger.info("Performing ID move:Phase move Completed");

					gapScannable.asynchronousMoveTo(pgMv.moveVector.getP2().getY());
					gapScannable.waitWhileBusy();
					logger.info("Performing ID move:Gap move Completed");

				}
			}
		} finally {
			moveSequenceRunning = false;
		}
	}

	public String getCurrentPolarisation() throws DeviceException {
		checkPhases();
		Double gap = (Double) gapScannable.getPosition();
		for (String polarisation : new String[] { HORIZONTAL, VERTICAL, CIRCULAR_LEFT, CIRCULAR_RIGHT }) {
			if (lowerPhaseScannable.isAt(getPhaseForGap(gap, polarisation)))
				return polarisation;
			if (VERTICAL.equalsIgnoreCase(polarisation) && lowerPhaseScannable.isAt(-1 * getPhaseForGap(gap, polarisation))) {
				return polarisation;
			}
		}
		throw new DeviceException("found undefined id setting");
	}

	@Override
	public Object getPosition() throws DeviceException {
		// checkPhases(); // don't check - so we have something to report in any case
		String polarisation = "unknown";
		try {
			polarisation = getCurrentPolarisation();
		} catch (Exception e) {
			// ignored
		}
		return new Object[] { gapScannable.getPosition(), polarisation, upperPhaseScannable.getPosition() };
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

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		if (position instanceof Number) {
			combinedMove(((Number) position).doubleValue(), getCurrentPolarisation());
			return;
		}
		double energy;
		String pol = null;
		if (position instanceof List<?>)
			position = ((List<?>) position).toArray();
		try {
			Object[] arr = (Object[]) position;
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

	public double getPhaseTolerance() {
		return phaseTolerance;
	}

	public void setPhaseTolerance(double phaseTolerance) {
		this.phaseTolerance = phaseTolerance;
	}

}
