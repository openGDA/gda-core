/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.continuouscontroller;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * A dummy traectory move controller useful for debugging and for illustration.
 */
public class DummyTrajectoryMoveController extends DeviceBase implements TrajectoryMoveController , SimulatedTriggerObservable {

	private int numberAxes;

	Vector<Double[]> points = new Vector<Double[]>();
	
	double[] triggerDeltas;
	
	private Double triggerPeriod;
	
	private volatile boolean going = false;

	private Thread goingThread;

	
	/**
	 * If true simulate a move when asked to move.
	 */
	public boolean simulate = false;


	public DummyTrajectoryMoveController() {
		stopAndReset();
	}

	
	@Override
	public void configure() throws FactoryException {
		
	}

	@Override
	public String toString() {
		String s = getName() + ":\n";
		if ((triggerPeriod == null) && (triggerDeltas == null)) {
			s += "NO TRIGGER PERIOD OR DELTAS CONFIGURED\n";
		}
		if (triggerPeriod != null) {
			s += "triggerPeriod = " + triggerPeriod +"\n";
		}
		for (int p = 0; p < points.size(); p++) {
			String t =  "";
			if (triggerDeltas != null) {
			try {
				t = triggerDeltas[p] + "s";
			} catch (ArrayIndexOutOfBoundsException e) {
				t = "NO TRIGGER DELTA FOR THIS POINT";
			}
			}
			 s += Arrays.toString(points.get(p)) + " " + t + "\n";
		}
		return s;
	}
	
	@Override
	public int getNumberAxes() {
		return numberAxes;
	}

	@Override
	public void stopAndReset() {
		points = new Vector<Double[]>();
		triggerDeltas = null;
		triggerPeriod = null;
	}

	@Override
	public void addPoint(Double[] point) throws DeviceException {
		if (point.length != numberAxes) {
			throw new DeviceException(MessageFormat.format(
					"DummyTrajectoryMoveController expected {0} dimension point not a {1} dimension one.", numberAxes,
					point.length));
		}
		points.add(point);
	}

	@Override
	public Double[] getLastPointAdded() throws DeviceException{
		try {
			return points.lastElement();
		} catch (NoSuchElementException e) {
			throw new DeviceException(getName() +" cannot return last point added, as None have been added");
		}
	}

	@Override
	public void prepareForMove() throws DeviceException {
		InterfaceProvider.getTerminalPrinter().print(getName() + ".prepareForMove()\n");

	}

	@Override
	public void startMove() {
		if (simulate) {
			goingThread = new Thread(new SimulatedMoveTask());
			goingThread.start();
		} else {
			InterfaceProvider.getTerminalPrinter().print(MessageFormat.format(
					"{0}.startMove() with {1} points and {2} period.\n",
					getName()==null ? "a DummyTrajectoryMoveController" : getName(), points.size(), triggerPeriod));
		}
		
	}

	/**
	 * Wait for the time that the configured move would take, and print the motor posititions as each trigger would be generated.
	 */
	class SimulatedMoveTask implements Runnable {
		@Override
		public void run() {
			InterfaceProvider.getTerminalPrinter().print(getName() + "s moving...\n");
			going = true;
			
			for (int i = 0; i < getNumberTriggers(); i++) {
				double deltaT = (triggerDeltas == null) ? triggerPeriod : triggerDeltas[i];
				Double[] point = points.get(i);
				InterfaceProvider.getTerminalPrinter().print(MessageFormat.format(getName() + " --> {0} ({1} s)\n",
						Arrays.toString(point), deltaT));
				notifyIObservers(this, null); // SimulatedTriggerProvider
				try {
					Thread.sleep((long) (deltaT * 1000));
				} catch (InterruptedException e) {
					if (Thread.interrupted()) {
						InterfaceProvider.getTerminalPrinter().print("DummyTrajectoryMoveController interupted while moving\n");
						going = false;
						return;
					}
				}
			}
			going = false;
			InterfaceProvider.getTerminalPrinter().print(getName() + " move complete\n");
		}
	}
	
	@Override
	public boolean isMoving() {
		return going;
	}

	@Override
	public void waitWhileMoving() throws InterruptedException {
		if (goingThread == null) {
			return;
		}
		goingThread.join();
	}

	@Override
	public int getNumberTriggers() {
		return points.size();
	}

	@Override
	public double getTotalTime() throws DeviceException {
		if (triggerDeltas != null) {
			double t = 0;
			for (double dt : triggerDeltas) {
				t += dt;
			}
			return t;
		}
		if (triggerPeriod != null) {
			return triggerPeriod * getNumberTriggers();
		}
		throw new DeviceException ("Not trigger period or trigger deltas set.");
		
	}

	@Override
	public void setTriggerPeriod(double seconds) throws DeviceException {
		triggerDeltas = null;
		triggerPeriod = seconds;
	}


	@Override
	public void setTriggerDeltas(double[] triggerDeltas) {
		triggerPeriod = null;
		this.triggerDeltas = triggerDeltas;	
	}
	
	public Vector<Double[]> getPointsList() {
		return points;
	}
	
	@Override
	public void setAxisTrajectory(int axisIndex, double[] trajectory) throws DeviceException {
		// TODO: 
		throw new DeviceException("Not implemented");
	}

	public void setNumberAxes(int numberAxes) {
		this.numberAxes = numberAxes;
	}
	
	public List<double[]>readActualPositionsFromHardware() {
		ArrayList<double[]> wobbledPoints = new ArrayList<double[]>(getPointsList().size());
		for (Double[] point : points) {
			wobbledPoints.add(wobble(point));
		}
		return wobbledPoints;
		
	}
	
	private double[] wobble(Double[] a) {
		double[] r = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			r[i] = (a[i] == null) ? 999 : a[i] +.123;
		}
		return r;
	}
}
