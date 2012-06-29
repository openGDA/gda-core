/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.hrpd.commands;

import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.hrpd.scan.CVScan;
import gda.hrpd.scan.RobotScan;
import gda.hrpd.scan.RobotScan2D;
import gda.hrpd.scan.StageScan;
import gda.hrpd.scan.TemperatureScan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holder of functions or static methods that support Jython commands to operate scan with scannable objects.
 * These are HRPD extensions to the GDA Jython commands defined in {@link gda.jython.commands.ScannableCommands}.
 */
public class ScanCommands {

	private static final Logger logger = LoggerFactory.getLogger(ScanCommands.class);

	/**
	 * Creates and runs a constant velocity scan using EPICS trajectory scan, Working only with XPS motor controller,
	 * data are collected during scan by the controller, and only write to data file at end of the cvscan.
	 * <p>
	 * 
	 * @param args the scannable objects and their parameters
	 * @throws Exception
	 */
	public static void cvscan(Object... args) throws Exception {
		int repeat = 1;
		int numberArgs = args.length;
		if (numberArgs < 2 || !(args[0] instanceof Scannable)) {
			throw new IllegalArgumentException("usage: cvscan motor1 start [stop] time [motor2 start stop] [detectors]");
		}
		CVScan theScan = null;
		switch (numberArgs) {
		case 2:
			// fixed 2-theta scan from -7 to 39.667 degree
			if ((args[0] instanceof Scannable))
				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()));
			break;
		case 3:
			// specify only tth, time and repeat number
//			if ((args[0] instanceof Scannable))
//				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()));
//			repeat = Integer.parseInt(args[2].toString());
			// specify only tth, start and time
			if ((args[0] instanceof Scannable)) {
				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()));
			}
			break;
		case 4:
			if (args[0] instanceof Scannable) {
				if (args[3] instanceof Scannable) {
					// synchronous theta-2theta scan
					theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
							.parseDouble(args[2].toString()), (Scannable) args[3]);
				} else {
					// specify one motor, its start, time, repeat number
					repeat = Integer.parseInt(args[3].toString());
					theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
							.parseDouble(args[2].toString()));
				}
			}
			break;
		case 5:
			if (args[0] instanceof Scannable) {
				if (args[4] instanceof Detector) {
					// specify one motor, its start, end, time, detector
					theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
							.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()),
							(Detector) args[4]);
				} else if (args[3] instanceof Scannable) {
					// asymmetric synchronous theta-2theta scan
					theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
							.parseDouble(args[2].toString()), (Scannable) args[3], Double.parseDouble(args[4]
							.toString()));
				}
			}
			break;

		case 6:
			if (args[0] instanceof Scannable && (args[3] instanceof Scannable)) {
				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), (Scannable) args[3], Double.parseDouble(args[4].toString()),
						Double.parseDouble(args[5].toString()));
			} else if (args[0] instanceof Scannable && args[4] instanceof Detector && args[5] instanceof Detector) {
				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Detector) args[4],
						(Detector) args[5]);
			} else if (args[4] instanceof Detector && args[5] instanceof DataWriter) {
				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Detector) args[4],
						(DataWriter) args[5]);
			} else {
				throw new IllegalArgumentException(
						"usage: cvscan motor1 start1 time motor2 start2 stop2\n or: cvscan motor start stop time detector1 detector2\n or: cvscan motor start stop time detectr datawriter");
			}
			break;
		case 7:
			if (args[0] instanceof Scannable && (args[3] instanceof Scannable)) {
				repeat = Integer.parseInt(args[6].toString());
				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), (Scannable) args[3], Double.parseDouble(args[4].toString()),
						Double.parseDouble(args[5].toString()));
			} else {
				throw new IllegalArgumentException("usage: cvscan motor1 start1 time motor2 start2 stop2 repeatNumber");
			}
			break;
		case 8:
			if (!(args[0] instanceof Scannable)) {
				throw new IllegalArgumentException(
						"usage: cvscan motor1 start [stop] time [motor2 start stop] [detectors]");
			}
			if (args[7] instanceof Detector) {
				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()),
						(Detector) args[7]);
			} else if (args[7].getClass().isArray() && args[7] instanceof Detector[]) {
				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()),
						(Detector[]) args[7]);
			} 
			break;
		case 9:
			if (!(args[0] instanceof Scannable)) {
				throw new IllegalArgumentException(
						"usage: cvscan motor1 start [stop] time [motor2 start stop] [detectors]");
			}
			if (args[7] instanceof Detector && args[8] instanceof Detector) {
				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()),
						(Detector) args[7], (Detector) args[8]);
			} else if (args[7].getClass().isArray() && args[7] instanceof Detector[] && args[8] instanceof DataWriter) {
				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()),
						(Detector[]) args[7], (DataWriter) args[8]);
			}
			break;
		case 10:
			if (!(args[0] instanceof Scannable)) {
				throw new IllegalArgumentException(
						"usage: cvscan motor1 start [stop] time [motor2 start stop] [detectors]");
			}
			if (args[7] instanceof Detector && args[8] instanceof Detector && args[9] instanceof DataWriter) {
				Detector[] detectors = new Detector[2];
				detectors[0] = (Detector) args[7];
				detectors[1] = (Detector) args[8];
				theScan = new CVScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()), detectors,
						(DataWriter) args[9]);
			}
			break;
		}

		if (theScan != null) {
			for (int i = 0; i < repeat; i++) {
				theScan.runScan();
			}
		} else {
			throw new IllegalArgumentException("usage: cvscan motor1 start [stop] time [motor2 start stop] [detectors]");
		}
	}

	/**
	 * Creates and runs a constant velocity scan within an optional 1-axis sample stage scan inside a robot sample
	 * changer scan, collect data from 2 multi-channel scaler (I11).
	 * <p>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void robotscan(Object... args) throws Exception {
		int numberArgs = args.length;
		if (numberArgs < 5 || !(args[0] instanceof Scannable)) {
			throw new IllegalArgumentException(
					"usage: robotscan sample startNumber stopNumber [step] [translator start stop step] cv-motor startAngle [stopAngle] totalTime");
		}
		RobotScan theScan = null;
		switch (numberArgs) {
		case 5:
			// fixed two-theta cvscan from 0 to 35 degree, sample step=1
			if (args[0] instanceof Scannable && args[3] instanceof Scannable) {
				theScan = new RobotScan((Scannable) args[0], Integer.parseInt(args[1].toString()), Integer
						.parseInt(args[2].toString()), (Scannable) args[3], Double.parseDouble(args[4].toString()));
			}
			break;
		case 6:
			// allow control of tt start angle, sample step=1
			if ((args[0] instanceof Scannable) && (args[3] instanceof Scannable)) {
				theScan = new RobotScan((Scannable) args[0], Integer.parseInt(args[1].toString()), Integer
						.parseInt(args[2].toString()), (Scannable) args[3], Double.parseDouble(args[4].toString()),
						Double.parseDouble(args[5].toString()));
			}
			break;
		case 7:
			// allow control of tt start and stop, sample step=1
			if ((args[0] instanceof Scannable) && (args[3] instanceof Scannable)) {
				theScan = new RobotScan((Scannable) args[0], Integer.parseInt(args[1].toString()), Integer
						.parseInt(args[2].toString()), (Scannable) args[3], Double.parseDouble(args[4].toString()),
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()));
			} else if ((args[0] instanceof Scannable) && (args[4] instanceof Scannable)) {
				// allow sample step control, and tth star angle control
				theScan = new RobotScan((Scannable) args[0], Integer.parseInt(args[1].toString()), Integer
						.parseInt(args[2].toString()), Integer.parseInt(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()));
			}
			break;

		case 8:
			// full control of sample changer and tth
			if (!(args[0] instanceof Scannable) || !(args[4] instanceof Scannable)) {
				throw new IllegalArgumentException(
						"usage: robotscan sample startNumber stopNumber [step] [translator start stop step] cv-motor startAngle [stopAngle] totalTime");
			}
			if (args[0] instanceof Scannable && args[4] instanceof Scannable) {
				theScan = new RobotScan((Scannable) args[0], Integer.parseInt(args[1].toString()), Integer
						.parseInt(args[2].toString()), Integer.parseInt(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()), Double
								.parseDouble(args[7].toString()));
			}
			break;
		case 10:
			if (args[0] instanceof Scannable && args[3] instanceof Scannable && args[7] instanceof Scannable) {
				// sample step=1
				theScan = new RobotScan((Scannable) args[0], Integer.parseInt(args[1].toString()), Integer
						.parseInt(args[2].toString()), (Scannable) args[3], Double.parseDouble(args[4].toString()),
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()),
						(Scannable) args[7], Double.parseDouble(args[8].toString()), Double.parseDouble(args[9]
								.toString()));
			}
			break;
		case 11:
			if (args[0] instanceof Scannable && args[4] instanceof Scannable && args[8] instanceof Scannable) {
				// only allow control of tth start angle
				theScan = new RobotScan((Scannable) args[0], Integer.parseInt(args[1].toString()), Integer
						.parseInt(args[2].toString()), Integer.parseInt(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()), Double
								.parseDouble(args[7].toString()), (Scannable) args[8], Double.parseDouble(args[9]
								.toString()), Double.parseDouble(args[10].toString()));
			}
			break;
		case 12:
			// full control of sample changer, sample stage, and cvscan
			if (args[0] instanceof Scannable && args[4] instanceof Scannable && args[8] instanceof Scannable) {

				theScan = new RobotScan((Scannable) args[0], Integer.parseInt(args[1].toString()), Integer
						.parseInt(args[2].toString()), Integer.parseInt(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()), Double
								.parseDouble(args[7].toString()), (Scannable) args[8], Double.parseDouble(args[9]
								.toString()), Double.parseDouble(args[10].toString()), Double.parseDouble(args[11]
								.toString()));
			}
			break;
		}

		if (theScan != null) {
			theScan.runScan();
		} else {
			throw new IllegalArgumentException(
					"usage: robotscan sample startNumber stopNumber [step] [translator start stop step] cv-motor startAngle [stopAngle] totalTime");
		}
	}

	/**
	 * Creates and runs a 2 dimensional constant velocity scan inside a robot sample changer scan, collect data from 2
	 * multi-channel scaler (I11).
	 * <p>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void robotscan2d(Object... args) throws Exception {
		int numberArgs = args.length;
		if (numberArgs < 9 || !(args[0] instanceof Scannable)) {
			throw new IllegalArgumentException(
					"usage: robotscan2d sample startNumber stopNumber [step] cv-motor1 startAngle totalTime cv-motor2 start stop");
		}
		RobotScan2D theScan = null;
		switch (numberArgs) {
		case 9:
			if (args[0] instanceof Scannable && args[3] instanceof Scannable && args[6] instanceof Scannable) {
				// sample step=1
				theScan = new RobotScan2D((Scannable) args[0], Integer.parseInt(args[1].toString()), Integer
						.parseInt(args[2].toString()), 1, (Scannable) args[3], Double.parseDouble(args[4].toString()),
						Double.parseDouble(args[5].toString()), (Scannable) args[6], Double.parseDouble(args[7]
								.toString()), Double.parseDouble(args[8].toString()));
			}
			break;

		case 10:
			if (args[0] instanceof Scannable && args[4] instanceof Scannable && args[7] instanceof Scannable) {
				// sample step=1
				theScan = new RobotScan2D((Scannable) args[0], Integer.parseInt(args[1].toString()), Integer
						.parseInt(args[2].toString()), Integer.parseInt(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()),
						(Scannable) args[7], Double.parseDouble(args[8].toString()), Double.parseDouble(args[9]
								.toString()));
			}
			break;
		}

		if (theScan != null) {
			theScan.runScan();
		} else {
			throw new IllegalArgumentException(
					"usage: robotscan2d sample startNumber stopNumber [step] cv-motor1 startAngle totalTime cv-motor2 start stop");
		}
	}

	/**
	 * Creates and runs a constant velocity scan inside a sample stage step scan, collect data from 2 multichannel
	 * scaler for I11.
	 * <p>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void stagescan(Object... args) throws Exception {
		int numberArgs = args.length;
		if (numberArgs < 7) {
			throw new IllegalArgumentException(
					"usage: stagescan x xstart xstop xstep [y ystart ystop ystep] cv-motor startAngle [stopAngle] totalTime");
		}
		StageScan theScan = null;
		switch (numberArgs) {
		case 7:
			if ((args[0] instanceof Scannable) && (args[4] instanceof Scannable)) {
				theScan = new StageScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()));
			}
			break;

		case 8:
			if (args[0] instanceof Scannable && args[4] instanceof Scannable) {
				theScan = new StageScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()), Double
								.parseDouble(args[7].toString()));
			}
			break;
		case 11:
			if (args[0] instanceof Scannable && args[4] instanceof Scannable && args[8] instanceof Scannable) {

				theScan = new StageScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()), Double
								.parseDouble(args[7].toString()), (Scannable) args[8], Double.parseDouble(args[9]
								.toString()), Double.parseDouble(args[10].toString()));
			}
			break;
		case 12:
			if (args[0] instanceof Scannable && args[4] instanceof Scannable && args[8] instanceof Scannable) {

				theScan = new StageScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()), Double
								.parseDouble(args[7].toString()), (Scannable) args[8], Double.parseDouble(args[9]
								.toString()), Double.parseDouble(args[10].toString()), Double.parseDouble(args[11]
								.toString()));
			}
			break;
		}

		if (theScan != null) {
			theScan.runScan();
		} else {
			throw new IllegalArgumentException(
					"usage: stagescan x xstart xstop xstep [y ystart ystop ystep] cv-motor startAngle [stopAngle] totalTime");
		}
	}

	/**
	 * Creates and runs a constant velocity scan inside a sample stage step scan, collect data from 2 multichannel
	 * scaler for I11.
	 * <p>
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void tempscan(Object... args) throws Exception {
		int numberArgs = args.length;
		if (numberArgs < 7) {
			throw new IllegalArgumentException(
					"usage: tempscan t-scannable tstart tstop tstep [[trate] waittime] [m-scannable xstart xstop xstep] cv-motor startAngle [stopAngle] totalTime");
		}
		TemperatureScan theScan = null;
		switch (numberArgs) {
		case 7:
			if ((args[0] instanceof Scannable) && (args[4] instanceof Scannable)) {
				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()));
			}
			break;

		case 8:
			if (args[0] instanceof Scannable && args[5] instanceof Scannable) {
				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), Integer
						.parseInt(args[4].toString()), (Scannable) args[5], Double.parseDouble(args[6].toString()),
						Double.parseDouble(args[7].toString()));
			}
			break;
		case 9:
			if ((args[0] instanceof Scannable) && (args[6] instanceof Scannable)) {
				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), Double
						.parseDouble(args[4].toString()), Integer.parseInt(args[5].toString()), (Scannable) args[6],
						Double.parseDouble(args[7].toString()), Double.parseDouble(args[8].toString()));
			}
			break;

		case 10:
			if (args[0] instanceof Scannable && args[6] instanceof Scannable) {
				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), Double
						.parseDouble(args[4].toString()), Integer.parseInt(args[5].toString()), (Scannable) args[6],
						Double.parseDouble(args[7].toString()), Double.parseDouble(args[8].toString()), Double
								.parseDouble(args[9].toString()));
			}
			if (args[0] instanceof Scannable && args[4] instanceof Scannable && args[7] instanceof Scannable) {
				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()),
						(Scannable) args[7], Double.parseDouble(args[8].toString()), Double.parseDouble(args[9]
								.toString()));
			}
			break;
		case 11:
			if (args[0] instanceof Scannable && args[4] instanceof Scannable && args[8] instanceof Scannable) {

				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()), Double
								.parseDouble(args[7].toString()), (Scannable) args[8], Double.parseDouble(args[9]
								.toString()), Double.parseDouble(args[10].toString()));
			}
			if (args[0] instanceof Scannable && args[5] instanceof Scannable && args[8] instanceof Scannable) {

				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), Integer
						.parseInt(args[4].toString()), (Scannable) args[5], Double.parseDouble(args[6].toString()),
						Double.parseDouble(args[7].toString()), (Scannable) args[8], Double.parseDouble(args[9]
								.toString()), Double.parseDouble(args[10].toString()));
			}
			break;
		case 12:
			if (args[0] instanceof Scannable && args[5] instanceof Scannable && args[9] instanceof Scannable) {

				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), Integer
						.parseInt(args[4].toString()), (Scannable) args[5], Double.parseDouble(args[6].toString()),
						Double.parseDouble(args[7].toString()), Double.parseDouble(args[8].toString()),
						(Scannable) args[9], Double.parseDouble(args[10].toString()), Double.parseDouble(args[11]
								.toString()));
			}
			if (args[0] instanceof Scannable && args[6] instanceof Scannable && args[9] instanceof Scannable) {

				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), Double
						.parseDouble(args[4].toString()), Integer.parseInt(args[5].toString()), (Scannable) args[6],
						Double.parseDouble(args[7].toString()), Double.parseDouble(args[8].toString()),
						(Scannable) args[9], Double.parseDouble(args[10].toString()), Double.parseDouble(args[11]
								.toString()));
			}
			break;
		case 13:
			if (args[0] instanceof Scannable && args[6] instanceof Scannable && args[10] instanceof Scannable) {

				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), Double
						.parseDouble(args[4].toString()), Integer.parseInt(args[5].toString()), (Scannable) args[6],
						Double.parseDouble(args[7].toString()), Double.parseDouble(args[8].toString()), Double
								.parseDouble(args[9].toString()), (Scannable) args[10], Double.parseDouble(args[11]
								.toString()), Double.parseDouble(args[12].toString()));
			}
			break;
		case 14:
			if (args[0] instanceof Scannable && args[6] instanceof Scannable && args[10] instanceof Scannable) {

				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), Double
						.parseDouble(args[4].toString()), Integer.parseInt(args[5].toString()), (Scannable) args[6],
						Double.parseDouble(args[7].toString()), Double.parseDouble(args[8].toString()), Double
								.parseDouble(args[9].toString()), (Scannable) args[10], Double.parseDouble(args[11]
								.toString()), Double.parseDouble(args[12].toString()), Double.parseDouble(args[13]
								.toString()));
			}
			if (args[0] instanceof Scannable && args[4] instanceof Scannable && args[8] instanceof Scannable
					&& args[11] instanceof Scannable) {

				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), (Scannable) args[4],
						Double.parseDouble(args[5].toString()), Double.parseDouble(args[6].toString()), Double
								.parseDouble(args[7].toString()), (Scannable) args[8], Double.parseDouble(args[9]
								.toString()), Double.parseDouble(args[10].toString()), (Scannable) args[11], Double
								.parseDouble(args[12].toString()), Double.parseDouble(args[13].toString()));
			}
			break;
		case 15:
			if (args[0] instanceof Scannable && args[5] instanceof Scannable && args[9] instanceof Scannable
					&& args[12] instanceof Scannable) {

				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), Integer
						.parseInt(args[4].toString()), (Scannable) args[5], Double.parseDouble(args[6].toString()),
						Double.parseDouble(args[7].toString()), Double.parseDouble(args[8].toString()),
						(Scannable) args[9], Double.parseDouble(args[10].toString()), Double.parseDouble(args[11]
								.toString()), (Scannable) args[12], Double.parseDouble(args[13].toString()), Double
								.parseDouble(args[14].toString()));
			}
			break;
		case 16:
			if (args[0] instanceof Scannable && args[6] instanceof Scannable && args[10] instanceof Scannable
					&& args[13] instanceof Scannable) {

				theScan = new TemperatureScan((Scannable) args[0], Double.parseDouble(args[1].toString()), Double
						.parseDouble(args[2].toString()), Double.parseDouble(args[3].toString()), Double
						.parseDouble(args[4].toString()), Integer.parseInt(args[5].toString()), (Scannable) args[6],
						Double.parseDouble(args[7].toString()), Double.parseDouble(args[8].toString()), Double
								.parseDouble(args[9].toString()), (Scannable) args[10], Double.parseDouble(args[11]
								.toString()), Double.parseDouble(args[12].toString()), (Scannable) args[13], Double
								.parseDouble(args[14].toString()), Double.parseDouble(args[15].toString()));
			}
			break;
		}

		if (theScan != null) {
			theScan.runScan();
		} else {
			throw new IllegalArgumentException(
					"usage: tempscan t-scannable tstart tstop tstep [[trate] waittime] [m-scannable xstart xstop xstep] cv-motor startAngle [stopAngle] totalTime");
		}
	}
}
