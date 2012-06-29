/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.hrpd.scan;

import gda.device.DeviceException;
import gda.device.Robot;
import gda.device.Scannable;
import gda.device.detector.multichannelscaler.EpicsMultiChannelScaler;
import gda.device.robot.SampleState;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.scan.Scan;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RobotScan Class
 */
public class RobotScan extends CVScanBase implements Scan {

	private static final Logger logger = LoggerFactory.getLogger(RobotScan.class);
	// Sample Changer
	private Robot robot = null;
	private int start = 0;
	private int stop = 0;
	private int step = 0;
	// Sample stage translator
	private Scannable stage = null;
	private double stageStart = 0;
	private double stageStop = 0;
	private double stageStep = 0;
	// two-theta motor
	private Scannable motor;
	private double mstart;
	private double mstop;
	private double time;
	// define detectors for data collection
	private EpicsMultiChannelScaler mcs1 = (EpicsMultiChannelScaler) finder.find("mcs1");
	private EpicsMultiChannelScaler mcs2 = (EpicsMultiChannelScaler) finder.find("mcs2");

	Vector<CVScan> allChildScans = new Vector<CVScan>();

	private static Finder finder = Finder.getInstance();

	/**
	 * @param robot
	 * @param start
	 * @param stop
	 * @param step
	 * @param stage
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param motor
	 * @param mstart
	 * @param mstop
	 * @param time
	 */
	public RobotScan(Scannable robot, int start, int stop, int step, Scannable stage, double tstart, double tstop,
			double tstep, Scannable motor, double mstart, double mstop, double time) {

		if (((Robot) finder.find(robot.getName())) == null) {
			logger.error("Can not find the required object : {}", robot.getName());
			throw new IllegalStateException("Can not find the required object: " + robot.getName());
		}
		this.robot = (Robot) finder.find(robot.getName());
		this.start = start;
		this.stop = stop;
		this.step = step;
		this.stage = stage;
		this.stageStart = tstart;
		this.stageStop = tstop;
		this.stageStep = tstep;
		this.motor = motor;
		this.mstart = mstart;
		this.mstop = mstop;
		this.time = time;

		if (this.robot != null) {
			allScannables.add(this.robot);
		}
		if (this.stage != null) {
			allScannables.add(this.stage);
		}

		CVScan newChildScan = new CVScan(motor, mstart, mstop, time, mcs1, mcs2);
		newChildScan.addParentScannables(allScannables);
		newChildScan.setIsChild(true);
		allChildScans.add(newChildScan);
		numberOfChildScans++;
		newChildScan.setNumberOfChildScans(numberOfChildScans);
		super.setUp();
	}

	/**
	 * @param robot
	 * @param start
	 * @param stop
	 * @param step
	 * @param stage
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public RobotScan(Scannable robot, int start, int stop, int step, Scannable stage, double tstart, double tstop,
			double tstep, Scannable motor, double mstart, double time) {

		if (((Robot) finder.find(robot.getName())) == null) {
			logger.error("Can not find the required object : {}", robot.getName());
			throw new IllegalStateException("Can not find the required object: " + robot.getName());
		}
		this.robot = (Robot) finder.find(robot.getName());
		this.start = start;
		this.stop = stop;
		this.step = step;
		this.stage = stage;
		this.stageStart = tstart;
		this.stageStop = tstop;
		this.stageStep = tstep;
		this.motor = motor;
		this.mstart = mstart;
		this.time = time;
		if (this.robot != null) {
			allScannables.add(this.robot);
		}
		if (this.stage != null) {
			allScannables.add(this.stage);
		}
		CVScan newChildScan = new CVScan(motor, mstart, time);
		newChildScan.addParentScannables(allScannables);
		newChildScan.setIsChild(true);
		allChildScans.add(newChildScan);
		numberOfChildScans++;
		newChildScan.setNumberOfChildScans(numberOfChildScans);
		super.setUp();
	}

	/**
	 * constructor - 10 inputs - sample start stop translator t_start t_stop t_step tth a_start time
	 * 
	 * @param robot
	 * @param start
	 * @param stop
	 * @param stage
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public RobotScan(Scannable robot, int start, int stop, Scannable stage, double tstart, double tstop, double tstep,
			Scannable motor, double mstart, double time) {
		this(robot, start, stop, 1, null, 0, 0, 0, motor, mstart, time);
	}

	/**
	 * constructor - 8 inputs - sample start stop tth a_start a_stop a_step time
	 * 
	 * @param robot
	 * @param start
	 * @param stop
	 * @param step
	 * @param motor
	 * @param mstart
	 * @param mstop
	 * @param time
	 */
	public RobotScan(Scannable robot, int start, int stop, int step, Scannable motor, double mstart, double mstop,
			double time) {
		this(robot, start, stop, step, null, 0, 0, 0, motor, mstart, mstop, time);
	}

	/**
	 * constructor - 7 inputs - sample start stop step tth a_start time
	 * 
	 * @param robot
	 * @param start
	 * @param stop
	 * @param step
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public RobotScan(Scannable robot, int start, int stop, int step, Scannable motor, double mstart, double time) {
		this(robot, start, stop, step, null, 0, 0, 0, motor, mstart, time);
	}

	/**
	 * constructor - 7 inputs - sample start stop tth a_start a_stop time
	 * 
	 * @param robot
	 * @param start
	 * @param stop
	 * @param motor
	 * @param mstart
	 * @param mstop
	 * @param time
	 */
	public RobotScan(Scannable robot, int start, int stop, Scannable motor, double mstart, double mstop, double time) {
		this(robot, start, stop, 1, null, 0, 0, 0, motor, mstart, mstop, time);
	}

	/**
	 * constructor - 6 inputs - sample start stop tth angle-start time
	 * 
	 * @param robot
	 * @param start
	 * @param stop
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public RobotScan(Scannable robot, int start, int stop, Scannable motor, double mstart, double time) {
		this(robot, start, stop, 1, null, 0, 0, 0, motor, mstart, time);
	}

	/**
	 * @param robot
	 * @param start
	 * @param stop
	 * @param motor
	 * @param time
	 */
	public RobotScan(Scannable robot, int start, int stop, Scannable motor, double time) {
		this(robot, start, stop, 1, motor, 0.0, 35.0, time);
	}

	@Override
	public void doCollection() throws Exception {
		try {
			if (!this.isChild) {
				logger.info("Starting robotscan.");
			}
			if (stage != null) {
				// make sure sample position is at 0 for robot sample changer
				double stagepos = Double.parseDouble(stage.getPosition().toString());
				if (stagepos != 0.0) {
					JythonServerFacade.getInstance().print("moving sample position from " + stagepos + " to 0.0");
					stage.asynchronousMoveTo(0);
					// wait until sample stage translation to complete
					try {
						stage.waitWhileBusy();
					} catch (Exception e) {
						// convert to a device exception
						throw new DeviceException(e.getMessage(), e.getCause());
					}
					checkForInterrupts();
				}
				for (int i = start; i <= stop; i = i + step) {
					JythonServerFacade.getInstance().print("Scan sample number : " + i);
					robot.asynchronousMoveTo(i);
					// wait until sample change to complete
					try {
						robot.waitWhileBusy();
					} catch (Exception e) {
						// convert to a device exception
						throw new DeviceException(e.getMessage(), e.getCause());
					}
					checkForInterrupts();
					if (robot.getSampleState() != SampleState.DIFF) {
						// no sample on diffractometer, do next sample
						continue;
					}

					for (double pos = stageStart; pos <= stageStop; pos += stageStep) {
						stage.asynchronousMoveTo(pos);
						// wait until sample stage translation to complete
						try {
							stage.waitWhileBusy();
						} catch (Exception e) {
							// convert to a device exception
							throw new DeviceException(e.getMessage(), e.getCause());
						}
						checkForInterrupts();
						for (CVScan childScan : allChildScans) {
							childScan.run();
						}
						checkForInterrupts();
					}
					// make sure sample position is at 0 for robot sample
					// changer
					stagepos = Double.parseDouble(stage.getPosition().toString());
					if (stagepos != 0.0) {
						JythonServerFacade.getInstance().print("moving sample position from " + stagepos + " to 0.0");
						stage.asynchronousMoveTo(0);
						// wait until sample stage translation to complete
						try {
							stage.waitWhileBusy();
						} catch (Exception e) {
							// convert to a device exception
							throw new DeviceException(e.getMessage(), e.getCause());
						}
						checkForInterrupts();
					}
				}
			} else {
				for (int i = start; i <= stop; i = i + step) {
					JythonServerFacade.getInstance().print("Load sample number : " + i);
					robot.asynchronousMoveTo(i);
					// wait until it is completed.nextSample
					try {
						robot.waitWhileBusy();
					} catch (Exception e) {
						// convert to a device exception
						throw new DeviceException(e.getMessage(), e.getCause());
					}
					if (robot.getSampleState() != SampleState.DIFF) {
						continue;
					}
					checkForInterrupts();
					for (CVScan childScan : allChildScans) {
						childScan.run();
					}
					checkForInterrupts();

				}
			}
		} catch (Exception ex1) {
			interrupted = true;
			throw ex1;
		}
	}
	@Override
	public void prepareForCollection() throws Exception {
		JythonServerFacade.getInstance().print("Starting robot scan with nested CVScan ......");
		if (!isChild()) {
			super.prepareForCollection();
		}
	}	
}
