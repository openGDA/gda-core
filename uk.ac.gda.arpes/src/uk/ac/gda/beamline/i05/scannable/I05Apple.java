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

package uk.ac.gda.beamline.i05.scannable;

import gda.device.DeviceException;
import gda.device.ScannableMotion;
import gda.device.scannable.ScannableMotionBase;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class I05Apple extends ScannableMotionBase {
	
	public final static String VERTICAL = "LV";
	public final static String HORIZONTAL = "LH";
	public final static String CIRCULAR_LEFT = "CL";
	public final static String CIRCULAR_RIGHT = "CR";

	ScannableMotion gapScannable;
	ScannableMotion upperPhaseScannable;
	ScannableMotion lowerPhaseScannable;
	boolean moveSequenceRunning = false;
	
	DeviceException threadException = null;
	
	TrajectorySolver solver;

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
		setInputNames(new String[] { "energy", "polarisation"});
		setExtraNames(new String[] {});
		setOutputFormat(new String[] {"%8.5f", "%s"});
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
	
	public static double sigmoid(double k0, double k1, double k2, double k3, double x) {
		return k0 + k1 / (1 + Math.exp(-(x-k2)/k3));
	}
	
	public void checkPhases() throws DeviceException {
		if (!upperPhaseScannable.isAt(lowerPhaseScannable.getPosition()))
			throw new DeviceException("upper and lower phase out of sync");
	}
	
	public double getPhaseForGap(String polarisation, double gap) throws DeviceException {
		if (HORIZONTAL.equalsIgnoreCase(polarisation))
			return 0;
		if (VERTICAL.equalsIgnoreCase(polarisation)) {
			if (((Double) lowerPhaseScannable.getPosition()) > 0.0) {
				return 70;
			} 
			return -70;
		}
		
		Double phase = sigmoid(-682.768, 746.135,-144.152, 47.4809, gap);
		if (CIRCULAR_RIGHT.equalsIgnoreCase(polarisation))
			return phase;
		if (CIRCULAR_LEFT.equalsIgnoreCase(polarisation))
			return phase * -1;
		throw new DeviceException("unknown polarisation demanded");
	}

	public String getCurrentPolarisation() throws DeviceException {
		checkPhases();
		Double gap = (Double) gapScannable.getPosition();
		for (String polarisation : new String[] {HORIZONTAL, VERTICAL, CIRCULAR_LEFT, CIRCULAR_RIGHT}) {
			if (lowerPhaseScannable.isAt(getPhaseForGap(polarisation, gap)))
				return polarisation;
			if (VERTICAL.equalsIgnoreCase(polarisation) && lowerPhaseScannable.isAt(-1*getPhaseForGap(polarisation, gap))) {
				return polarisation;
			}
		}
		throw new DeviceException("found undefined id setting");
	}
	
	private double g(double x, double h, double j, double k, double l, double m) {
		return h * Math.log((x-j)*l) + k + m *x;
	}
	
	private double getGapFor(double energy, String polarisation) throws DeviceException {
		if (HORIZONTAL.equalsIgnoreCase(polarisation))
			return g(energy,18.8494,-1.05604,-53.5488,6.34718,0.0373835);
		if (VERTICAL.equalsIgnoreCase(polarisation)) {
			return g(energy,7.39726,12.8912,-18.9967,27.2954,0.0553008);
		}
//		if (CIRCULAR_RIGHT.equalsIgnoreCase(polarisation))
//			return phase;
//		if (CIRCULAR_LEFT.equalsIgnoreCase(polarisation))
//			return phase * -1;
		throw new DeviceException("unknown polarisation demanded");
	}
	
	public void combinedMove(double newenergy, String newpol) throws DeviceException {
		checkPhases();
		
		double currentPhase = (Double) lowerPhaseScannable.getPosition();
		double currentGap = (Double) gapScannable.getPosition();
		double newgap = getGapFor(newenergy, newpol);
		double newphase = getPhaseForGap(newpol, newgap);
		
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
				}
				
			}
		}).start();
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// sleeping to allow this device to become busy
		}
	}

	private void runPast(Point2D[] pointArray) throws DeviceException, InterruptedException {
		try {
			moveSequenceRunning = true;
		for (int i = 1; i < pointArray.length; i++) {
			lowerPhaseScannable.asynchronousMoveTo(pointArray[i].getX());
			upperPhaseScannable.asynchronousMoveTo(pointArray[i].getX());
			gapScannable.asynchronousMoveTo(pointArray[i].getY());
			lowerPhaseScannable.waitWhileBusy();
			upperPhaseScannable.waitWhileBusy();
			gapScannable.waitWhileBusy();
		}
		} finally {
			moveSequenceRunning = false;
		}
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
	
	@Override
	public Object getPosition() throws DeviceException {
		checkPhases();
		return new Object[] { gapScannable.getPosition(), getCurrentPolarisation() };
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
}