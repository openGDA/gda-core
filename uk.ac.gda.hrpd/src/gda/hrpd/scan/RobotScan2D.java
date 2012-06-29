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
public class RobotScan2D extends CVScanBase implements Scan {

	private static final Logger logger = LoggerFactory.getLogger(RobotScan2D.class);
	// Sample Changer
	private Robot robot = null;
	private int start = 0;
	private int stop = 0;
	private int step = 0;
	// Sample stage translator
	private Scannable tth = null;
	private double tthStart = 0;
	private double time;
	// private double tthStop = 0;
	// private double tthStep = 0;
	// two-theta motor
	private Scannable spos;
	private double sposStart;
	private double sposStop;
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
	 * @param tth
	 * @param tstart
	 * @param time
	 * @param spos
	 * @param mstart
	 * @param mstop
	 */
	public RobotScan2D(Scannable robot, int start, int stop, int step, Scannable tth, double tstart, double time,
			Scannable spos, double mstart, double mstop) {
		if (((Robot) finder.find(robot.getName())) == null) {
			logger.error("Can not find the required object : {}", robot.getName());
			throw new IllegalStateException("Can not find the required object: " + robot.getName());
		}
		this.robot = (Robot) finder.find(robot.getName());
		this.start = start;
		this.stop = stop;
		this.step = step;
		this.tth = tth;
		this.tthStart = tstart;
		this.time = time;
		this.spos = spos;
		this.sposStart = mstart;
		this.sposStop = mstop;

		if (this.robot != null) {
			allScannables.add(robot);
		}

		CVScan newChildScan = new CVScan(tth, tstart, time, spos, mstart, mstop);
		newChildScan.addParentScannables(allScannables);
		newChildScan.setIsChild(true);
		allChildScans.add(newChildScan);
		numberOfChildScans++;
		newChildScan.setNumberOfChildScans(numberOfChildScans);
		super.setUp();
	}

	@Override
	public void doCollection() throws Exception {
		try {
			if (!this.isChild) {
				logger.info("Starting scan.");
			}
			if (spos != null) {
				// make sure sample position is at 0 for robot sample changer
				double stagepos = Double.parseDouble(spos.getPosition().toString());
				if (stagepos != 0.0) {
					JythonServerFacade.getInstance().print("moving sample position from " + stagepos + " to 0.0");
					spos.asynchronousMoveTo(0);
					// wait until sample stage translation to complete
					try {
						spos.waitWhileBusy();
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

					for (CVScan childScan : allChildScans) {
						childScan.run();
					}
					checkForInterrupts();

					// make sure sample position is at 0 for robot sample changer
					stagepos = Double.parseDouble(spos.getPosition().toString());
					if (stagepos != 0.0) {
						JythonServerFacade.getInstance().print("moving sample position from " + stagepos + " to 0.0");
						spos.asynchronousMoveTo(0);
						// wait until sample stage translation to complete
						try {
							spos.waitWhileBusy();
						} catch (Exception e) {
							// convert to a device exception
							throw new DeviceException(e.getMessage(), e.getCause());
						}
						checkForInterrupts();
					}
				}
			}
		} catch (Exception ex1) {
			interrupted = true;
			throw ex1;
		}

	}
	@Override
	public void prepareForCollection() throws Exception {
		JythonServerFacade.getInstance().print("Starting 2 Dimensional robot scan with nested CVScan ......");
		if (!isChild()) {
			super.prepareForCollection();
		}
	}	

}
