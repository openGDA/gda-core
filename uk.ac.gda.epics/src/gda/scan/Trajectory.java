/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.scan;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;

import java.util.ArrayList;
import java.util.Vector;

/**
 * <p>
 * This class provides functionality for calculating trajectory path given trajectory start, stop, stepSize size (where
 * data is collected), and stepSize time (how fast data is collected). It generates the following parameters required to
 * define the trajectory path for EPICS trajectoryScan.
 * <ul>
 * <li>Total Trajectory Time</li>
 * <li>Total Trajectory element number</li>
 * <li>Total Trajectory output pulse number</li>
 * <li>Starting pulse element number</li>
 * <li>Stopping pulse element number</li>
 * <li>Defined Trajectory Path</li>
 * </ul>
 * </p>
 * <p>
 * This implementation currently only provides calculation for constant velocity path. To provide other type of paths
 * developer must add his/her own path calculation methods.
 * 
 * @see gda.scan.EpicsTrajectoryScanController
 */
public class Trajectory implements Configurable {

	/**
	 * Maximum array size of the defined trajectory path
	 */
	public static final int MAXIMUM_ELEMENT_NUMBER = EpicsTrajectoryScanController.MAXIMUM_ELEMENT_NUMBER;

	/**
	 * Maximum array size of the output pulses or the data points collected during the trajectory scan.
	 */
	public static final int MAXIMUM_PULSE_NUMBER = EpicsTrajectoryScanController.MAXIMUM_PULSE_NUMBER;

	/**
	 * the acceleration time for the motor
	 */
	private double accelerationTime = 1.0;

	/**
	 * number of elements over which the trajectory generates output pulses.
	 */
	private int n;

	/**
	 * number of elements over which motors are accelerated or decelerated
	 */
	private int s = 100;

	// calculated trajectory properties required by EPICS trajectory Scan
	/**
	 * the element at which pulse output starts
	 */
	private int pulseStartElement = 0;

	/**
	 * the element at which pulse output stops
	 */
	private int pulseStopElement = 0;

	/**
	 * total number of elements that defines the required trajectory
	 */
	private int totalElementNumber = MAXIMUM_ELEMENT_NUMBER;

	public int getTotalElementNumber() {
		return totalElementNumber;
	}

	public void setTotalElementNumber(int totalElementNumber) {
		this.totalElementNumber = totalElementNumber;
	}
	/**
	 * total time that takes to complete the trajectory
	 */
	private double totalTime = 0;

	/**
	 * total number of output pulses the defines the required data collection points during the trajectory scan
	 */
	private int totalPulseNumber = MAXIMUM_PULSE_NUMBER;

	public int getTotalPulseNumber() {
		return totalPulseNumber;
	}

	public void setTotalPulseNumber(int totalPulseNumber) {
		this.totalPulseNumber = totalPulseNumber;
	}
	/**
	 * the required or defined trajectory path for a scan
	 */
	private double[] path = new double[EpicsTrajectoryScanController.MAXIMUM_ELEMENT_NUMBER];

	private ArrayList<Double> oscilation = new ArrayList<Double>();

	private TrajectoryScanController controller;

	/**
	 * default constructor required by CASTOR if used.
	 */
	public Trajectory() {
		Finder finder = Finder.getInstance();
		controller = (TrajectoryScanController) finder.find("epicsTrajectoryScanController");
		accelerationTime = controller.getAccelerationTime();
	}

	/**
	 * default constructor required by CASTOR if used.
	 * 
	 * @param offset
	 */
	public Trajectory(@SuppressWarnings("unused") double offset) {
		Finder finder = Finder.getInstance();
		controller = (TrajectoryScanController) finder.find("epicsTrajectoryScanController");
		accelerationTime = controller.getAccelerationTime();
	}

	@Override
	public void configure() throws FactoryException {

	}

	/**
	 * calculate and define the constant velocity trajectory path
	 * 
	 * @param start
	 * @param end
	 * @param time
	 * @return trajectory path
	 */
	public double[] defineCVPath(double start, double end, double time) {
		calculateS(time);
		calculateN();
		calcTotalTime(time);
		// calcNumberOfTrajectoryPulses(start, end, this.stepSize);
		path = calculateCVPath(start, end, this.n, this.s);
		return path;
	}
	/**
	 * defines the oscillation path
	 * 
	 * @param start
	 * @param end
	 * @param time
	 * @return path[]
	 */
	public double[] defineOscillationPath(double start, double end, double time) {
		double[] forpath, backpath;
		double pts = totalElementNumber / time; //traj points per sec
		this.s = (int) (pts + 1); // scurve points, 1 seconds worth
		this.n = (int) ((end - start) * pts / 1.6 + 1); // CV points, 1.6 is maximum velocity of theta
		int ntimes = (totalElementNumber / (this.n * 2 + this.s * 4 - 2) + 1);
		for (int i = 0; i < ntimes; i++) {
			forpath = scurve(start, end, n, s);
			for (int j = 0; j < forpath.length - 1; j++) {
				oscilation.add(forpath[j]);
			}
			backpath = scurve(end, start, n, s);
			for (int j = 0; j < backpath.length - 1; j++) {
				oscilation.add(backpath[j]);
			}
		}

		if (oscilation.size() >= totalElementNumber) {
			for (int i = 0; i < totalElementNumber; i++) {
				path[i] = oscilation.get(i);
			}
		} else {
			throw new IllegalStateException("Oscillation path size " + oscilation.size() + "<" + totalElementNumber);
		}

		return path;
	}
	/**
	 * defines the oscillation path
	 * 
	 * @param start
	 * @param end
	 * @param time
	 * @return path[]
	 */
	public double[] defineOscillationPathWithOffset(double start, double end, double time) {
		double[] forpath, backpath;
		this.n = (int) ((end - start) * (1500 / time));
		this.s = n / 20 + 1;
		double length = (end - start) / 2;
		int ntimes = (totalElementNumber / (this.n * 2 + this.s * 4 - 2) + 1);
		double scale = length / ntimes;
		double off;
		for (int i = 0; i < ntimes; i++) {
			off = i * scale;
			forpath = scurve(start + off, start + off + length, n, s);
			for (int j = 0; j < forpath.length - 1; j++) {
				oscilation.add(forpath[j]);
			}
			backpath = scurve(start + off + length, start + off + scale, n, s);
			for (int j = 0; j < backpath.length - 1; j++) {
				oscilation.add(backpath[j]);
			}
		}

		if (oscilation.size() >= totalElementNumber) {
			for (int i = 0; i < totalElementNumber; i++) {
				path[i] = oscilation.get(i);
			}
		} else {
			throw new IllegalStateException("Oscillation path size " + oscilation.size() + "<" + totalElementNumber);
		}

		return path;
	}

	/**
	 * gets the total element number for this trajectory
	 * 
	 * @return total element number
	 */
	public long getElementNumbers() {
		if (totalElementNumber == 0) {
			throw new IllegalStateException("Trajectory Path Not defined. totalElementNumber = " + totalElementNumber);
		}
		return totalElementNumber;
	}

	/**
	 * gets total output pulse number i.e. data points for this trajectory.
	 * 
	 * @return total pulse number
	 */
	public long getPulseNumbers() {
		if (totalPulseNumber == 0) {
			throw new IllegalStateException("Trajectory Path Not defined. totalPulseNumber = " + totalPulseNumber);
		}
		return totalPulseNumber;
	}

	/**
	 * gets the element number at which output pulse starts.
	 * 
	 * @return element number
	 */
	public long getStartPulseElement() {
		if (pulseStartElement == 0) {
			throw new IllegalStateException("Trajectory Path Not defined.pulseStartElement = " + pulseStartElement);
		}
		return pulseStartElement;
	}

	/**
	 * gets the element number at which output pulse stops.
	 * 
	 * @return element number
	 */
	public long getStopPulseElement() {
		if (pulseStopElement == 0) {
			throw new IllegalStateException("Trajectory Path Not defined. pulseStopElement = " + pulseStopElement);
		}
		return pulseStopElement;
	}

	/**
	 * gets the total trajectory time
	 * 
	 * @return total time
	 */
	public double getTotalTime() {
		if (totalTime == 0) {
			throw new IllegalStateException("Trajectory Path Not defined. totalTime = " + totalTime);
		}
		return totalTime;
	}

	/**
	 * gets the defined trajectory path
	 * 
	 * @return double array defining the path
	 */
	public double[] getPath() {
		if (path != null) {
			throw new IllegalStateException("Trajectory Path Not defined. path = " + path);
		}
		return path;
	}

	private int calculateS(double time) {
		s = (int) (accelerationTime * totalElementNumber / (2 * accelerationTime + time) + 0.5);
		// set the element at which pulse starts
		pulseStartElement = s + 1;
		return s;
	}

	/**
	 * calculates the number of elements in the constant velocity section of the trajectory.
	 * 
	 * @return number of constant velocity elements in the trajectory
	 */
	private long calculateN() {
		// numver of elements for constant velocity section
		n = totalElementNumber - 2 * s;
		// set the element at which pulse stops
		pulseStopElement = totalElementNumber - this.s - 1;
		return n;
	}

	/**
	 * calculates the trajectory path from given start, end, number of constant velocity points, and number of
	 * acceleration/deceleration points
	 * 
	 * @param start
	 * @param end
	 * @param n
	 * @param s
	 * @return array of double - the trajectory path
	 */
	private double[] calculateCVPath(double start, double end, long n, long s) {
		int x = -1;
		int totalNumElements = (int) (s + n + s + 1);
		path = new double[totalNumElements];

		for (x = 0; x < s; x++) {
			path[x] = (end - start) * (x * x - s * s) / (2 * s * (n - 1)) + start;
		}

		for (x = (int) s; x < s + n; x++) {
			path[x] = (end - start) * (x - s) / (n - 1) + start;
		}

		for (x = (int) (s + n + 1); x < s + n + s + 1; x++) {
			path[x - 1] = (end - start) * ((s + n) * (s + n) + (4 * s + 2 * n) * (x - s - n) - x * x)
					/ (2 * s * (n - 1)) + end;
		}
		if (x < totalNumElements) {
			for (int i = x; i < totalNumElements; i++) {
				path[i] = 0;
			}
		}
		return path;
	}
	
	private double[] scurve(double start, double end, long n, long s) {
		int x = -1;
		Vector<Double> spath = new Vector<Double>();
		for (x = 0; x < s; x++) {
			spath.add((end - start) * (x * x - s * s) / (2 * s * (n - 1)) + start);
		}

		for (x = (int) s; x < s + n; x++) {
			spath.add((end - start) * (x - s) / (n - 1) + start);
		}

		for (x = (int) (s + n + 1); x < s + n + s + 1; x++) {
			spath.add((end - start) * ((s + n) * (s + n) + (4 * s + 2 * n) * (x - s - n) - x * x)
					/ (2 * s * (n - 1)) + end);
		}
		double[] scurve = new double[spath.size()];
		for (int i=0;i<spath.size();i++) {
			scurve[i] = spath.get(i);
		}
		return scurve;
	}

	/**
	 * calculate the total time for this trajectory
	 * 
	 * @param time
	 * @return total trajectory time
	 */
	private double calcTotalTime(double time) {
		totalTime = (time + 2 * accelerationTime);
		return totalTime;
	}

	/**
	 * gets the acceleration time of the motor
	 * 
	 * @return acceleration time
	 */
	public double getAccelerationTime() {
		return accelerationTime;
	}

	/**
	 * sets the acceleration time of the motor
	 * 
	 * @param accelerationTime
	 */
	public void setAccelerationTime(double accelerationTime) {
		this.accelerationTime = accelerationTime;
	}

}
