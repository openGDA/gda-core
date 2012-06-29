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
import gda.device.Scannable;
import gda.device.detector.multichannelscaler.EpicsMultiChannelScaler;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.scan.Scan;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A nested scan class that supports up to 2-axis or 2-dimensional (x,y) sample stage scan with a nested constant
 * velocity scan. At each stage position a constant velocity scan diffraction pattern is collected.
 */
public class StageScan extends CVScanBase implements Scan {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4673192902580560353L;

	private static final Logger logger = LoggerFactory.getLogger(StageScan.class);
	// Sample Changer
	private Scannable x = null;
	private double xStart = 0;
	private double xStop = 0;
	private double xStep = 0;
	// Sample stage translator
	private Scannable y = null;
	private double yStart = 0;
	private double yStop = 0;
	private double yStep = 0;
	// define detectors for data collection
	private EpicsMultiChannelScaler mcs1 = (EpicsMultiChannelScaler) finder.find("mcs1");
	private EpicsMultiChannelScaler mcs2 = (EpicsMultiChannelScaler) finder.find("mcs2");

	Vector<CVScan> allChildScans = new Vector<CVScan>();

	private static Finder finder = Finder.getInstance();

	/**
	 * Constructor for double axis sample stage constant velocity scan - 12 inputs
	 * 
	 * @param x
	 * @param xstart
	 * @param xstop
	 * @param xstep
	 * @param y
	 * @param ystart
	 * @param ystop
	 * @param ystep
	 * @param motor
	 * @param mstart
	 * @param mstop
	 * @param time
	 */
	public StageScan(Scannable x, double xstart, double xstop, double xstep, Scannable y, double ystart, double ystop,
			double ystep, Scannable motor, double mstart, double mstop, double time) {
		this.x = x;
		this.xStart = xstart;
		this.xStop = xstop;
		this.xStep = xstep;
		this.y = y;
		this.yStart = ystart;
		this.yStop = ystop;
		this.yStep = ystep;
		if (x != null) {
			allScannables.add(x);
		}
		if (y != null) {
			allScannables.add(y);
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
	 * constructor for double axis sample stage constant velocity scan - 11 inputs
	 * 
	 * @param x
	 * @param xstart
	 * @param xstop
	 * @param xstep
	 * @param y
	 * @param ystart
	 * @param ystop
	 * @param ystep
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public StageScan(Scannable x, double xstart, double xstop, double xstep, Scannable y, double ystart, double ystop,
			double ystep, Scannable motor, double mstart, double time) {
		this.x = x;
		this.xStart = xstart;
		this.xStop = xstop;
		this.xStep = xstep;
		this.y = y;
		this.yStart = ystart;
		this.yStop = ystop;
		this.yStep = ystep;
		if (x != null) {
			allScannables.add(x);
		}
		if (y != null) {
			allScannables.add(y);
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
	 * constructor for single axis sample stage constant velocity scan- 8 inputs
	 * 
	 * @param x
	 * @param xstart
	 * @param xstop
	 * @param xstep
	 * @param motor
	 * @param mstart
	 * @param mstop
	 * @param time
	 */
	public StageScan(Scannable x, double xstart, double xstop, double xstep, Scannable motor, double mstart,
			double mstop, double time) {
		this(x, xstart, xstop, xstep, null, 0, 0, 0, motor, mstart, mstop, time);
	}

	/**
	 * constructor for single axis sample stage constant velocity scan- 7 inputs
	 * 
	 * @param x
	 * @param xstart
	 * @param xstop
	 * @param xstep
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public StageScan(Scannable x, double xstart, double xstop, double xstep, Scannable motor, double mstart, double time) {
		this(x, xstart, xstop, xstep, null, 0, 0, 0, motor, mstart, time);
	}

	@Override
	public void doCollection() throws Exception {
		try {
			if (!this.isChild) {
				logger.info("Starting scan.");
			}
			// two axis scan
			if (y != null) {
				for (double xpos = xStart; xpos <= xStop; xpos = xpos + xStep) {
					JythonServerFacade.getInstance().print("Moving " + x.getName() + " to " + xpos);
					x.asynchronousMoveTo(xpos);
					// wait until sample change to complete
					try {
						x.waitWhileBusy();
					} catch (Exception e) {
						// convert to a device exception
						throw new DeviceException(e.getMessage(), e.getCause());
					}
					checkForInterrupts();

					for (double ypos = yStart; ypos <= yStop; ypos += yStep) {
						JythonServerFacade.getInstance().print("Moving " + y.getName() + " to " + ypos);
						y.asynchronousMoveTo(ypos);
						// wait until sample stage translation to complete
						try {
							y.waitWhileBusy();
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
					// move back to original or robot safe position when end
					y.asynchronousMoveTo(0);
					try {
						y.waitWhileBusy();
					} catch (Exception e) {
						// convert to a device exception
						throw new DeviceException(e.getMessage(), e.getCause());
					}
				}
				// move back to original or robot safe position when end
				x.asynchronousMoveTo(0);
				try {
					x.waitWhileBusy();
				} catch (Exception e) {
					// convert to a device exception
					throw new DeviceException(e.getMessage(), e.getCause());
				}
			} else {
				// single axis scan
				for (double pos = xStart; pos <= xStop; pos = pos + xStep) {
					JythonServerFacade.getInstance().print("Moving " + x.getName() + " to " + pos);
					x.asynchronousMoveTo(pos);
					// wait until it is completed.nextSample
					try {
						x.waitWhileBusy();
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
				// move back to original or robot safe position when end
				x.asynchronousMoveTo(0);
				try {
					x.waitWhileBusy();
				} catch (Exception e) {
					// convert to a device exception
					throw new DeviceException(e.getMessage(), e.getCause());
				}
			}

		} catch (Exception ex1) {
			interrupted = true;
			// move back to original or robot safe position when end
			x.asynchronousMoveTo(0);
			if (y != null) {
				y.asynchronousMoveTo(0);
			}
			try {
				x.waitWhileBusy();
			} catch (Exception e) {
				// convert to a device exception
				throw new DeviceException(e.getMessage(), e.getCause());
			}
			if (y != null) {
				try {
					y.waitWhileBusy();
				} catch (Exception e) {
					// convert to a device exception
					throw new DeviceException(e.getMessage(), e.getCause());
				}
			}
			throw ex1;
		}

	}
	@Override
	public void prepareForCollection() throws Exception {
		JythonServerFacade.getInstance().print("Starting stage scan with nested CVScan ......");
		if (!isChild()) {
			super.prepareForCollection();
		}
	}	

}
