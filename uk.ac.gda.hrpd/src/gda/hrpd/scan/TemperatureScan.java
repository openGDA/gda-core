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
import gda.device.Temperature;
import gda.device.detector.multichannelscaler.EpicsMultiChannelScaler;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.scan.Scan;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A nested scan class that supports combined sample temperature scan,sample position scan, and a nested constant
 * velocity scan. At each temperature and sample position a constant velocity scan diffraction pattern is collected.
 */
public class TemperatureScan extends CVScanBase implements Scan {

	private static final Logger logger = LoggerFactory.getLogger(TemperatureScan.class);
	// Sample Changer
	private Temperature temp = null;
	private double tempStart = 0;
	private double tempStop = 0;
	private double tempStep = 0;
	private double rampRate = Double.NaN;
	private int waitTime = 0;
	// Sample stage translator
	private Scannable x = null;
	private double xStart = 0;
	private double xStop = 0;
	private double xStep = 0;
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
	 * constructor for temperature - sample position - constant velocity scan with theta rocking - 16 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param trate
	 * @param waittime
	 * @param x
	 * @param xstart
	 * @param xstop
	 * @param xstep
	 * @param motor
	 * @param mstart
	 * @param time
	 * @param theta
	 * @param start
	 * @param stop
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, double trate, int waittime,
			Scannable x, double xstart, double xstop, double xstep, Scannable motor, double mstart, double time,
			Scannable theta, double start, double stop) {
		this.temp = (Temperature) finder.find(temp.getName());
		this.tempStart = tstart;
		this.tempStop = tstop;
		this.tempStep = tstep;
		this.rampRate = trate;
		this.waitTime = waittime;
		this.x = x;
		this.xStart = xstart;
		this.xStop = xstop;
		this.xStep = xstep;
		this.motor = motor;
		this.mstart = mstart;
		this.time = time;

		if (this.temp != null) {
			allScannables.add(this.temp);
		}
		if (this.x != null) {
			allScannables.add(this.x);
		}

		CVScan newChildScan = new CVScan(motor, mstart, time, theta, start, stop);
		newChildScan.addParentScannables(allScannables);
		newChildScan.setIsChild(true);
		allChildScans.add(newChildScan);
		numberOfChildScans++;
		newChildScan.setNumberOfChildScans(numberOfChildScans);
		super.setUp();
	}	
	/**
	 * constructor for temperature - constant velocity scan using default temperature ramp rate- 15 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param waittime
	 * @param x
	 * @param xstart
	 * @param xstop
	 * @param xstep
	 * @param motor
	 * @param mstart
	 * @param time
	 * @param theta
	 * @param start
	 * @param stop
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, int waittime, Scannable x,
			double xstart, double xstop, double xstep, Scannable motor, double mstart, double time, Scannable theta,
			double start, double stop) {
		this(temp, tstart, tstop, tstep, Double.NaN, waittime, x, xstart, xstop, xstep, motor, mstart, time, theta,
				start, stop);
	}
	/**
	 * Constructor for temperature - sample position - constant velocity scan - 14 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param trate
	 * @param waittime
	 * @param x
	 * @param xstart
	 * @param xstop
	 * @param xstep
	 * @param motor
	 * @param mstart
	 * @param mstop
	 * @param time
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, double trate, int waittime,
			Scannable x, double xstart, double xstop, double xstep, Scannable motor, double mstart, double mstop,
			double time) {
		this.temp = (Temperature) finder.find(temp.getName());
		this.tempStart = tstart;
		this.tempStop = tstop;
		this.tempStep = tstep;
		this.rampRate = trate;
		this.waitTime = waittime;
		this.x = x;
		this.xStart = xstart;
		this.xStop = xstop;
		this.xStep = xstep;
		this.motor = motor;
		this.mstart = mstart;
		this.mstop = mstop;
		this.time = time;

		if (this.temp != null) {
			allScannables.add(this.temp);
		}
		if (this.x != null) {
			allScannables.add(this.x);
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
	 * constructor for temperature constant velocity scan using default ramp rate and no wait for temperature
	 * equilibrium - 14 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param x
	 * @param xstart
	 * @param xstop
	 * @param xstep
	 * @param motor
	 * @param mstart
	 * @param time
	 * @param theta
	 * @param start
	 * @param stop
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, Scannable x, double xstart,
			double xstop, double xstep, Scannable motor, double mstart, double time, Scannable theta, double start,
			double stop) {
		this(temp, tstart, tstop, tstep, Double.NaN, 0, x, xstart, xstop, xstep, motor, mstart, time, theta, start,
				stop);
	}

	/**
	 * constructor for temperature - sample position - constant velocity scan - 13 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param trate
	 * @param waittime
	 * @param x
	 * @param xstart
	 * @param xstop
	 * @param xstep
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, double trate, int waittime,
			Scannable x, double xstart, double xstop, double xstep, Scannable motor, double mstart, double time) {
		this.temp = (Temperature) finder.find(temp.getName());
		this.tempStart = tstart;
		this.tempStop = tstop;
		this.tempStep = tstep;
		this.rampRate = trate;
		this.waitTime = waittime;
		this.x = x;
		this.xStart = xstart;
		this.xStop = xstop;
		this.xStep = xstep;
		this.motor = motor;
		this.mstart = mstart;
		this.time = time;
		if (this.temp != null) {
			allScannables.add(this.temp);
		}
		if (this.x != null) {
			allScannables.add(this.x);
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
	 * constructor for temperature - constant velocity scan using default temperature ramp rate, with theta rocking- 12
	 * inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param waittime
	 * @param x
	 * @param xstart
	 * @param xstop
	 * @param xstep
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, int waittime, Scannable x,
			double xstart, double xstop, double xstep, Scannable motor, double mstart, double time) {
		this(temp, tstart, tstop, tstep, Double.NaN, waittime, x, xstart, xstop, xstep, motor, mstart, time);
	}
	/**
	 * constructor for temperature - constant velocity scan- 12 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param trate
	 * @param waittime
	 * @param motor
	 * @param mstart
	 * @param time
	 * @param theta
	 * @param start
	 * @param stop
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, double trate, int waittime,
			Scannable motor, double mstart, double time, Scannable theta, double start, double stop) {
		this(temp, tstart, tstop, tstep, trate, waittime, null, 0, 0, 0, motor, mstart, time, theta, start, stop);
	}
	/**
	 * constructor for temperature constant velocity scan using default ramp rate and no wait for temperature
	 * equilibrium - 11 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param x
	 * @param xstart
	 * @param xstop
	 * @param xstep
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, Scannable x, double xstart,
			double xstop, double xstep, Scannable motor, double mstart, double time) {
		this(temp, tstart, tstop, tstep, Double.NaN, 0, x, xstart, xstop, xstep, motor, mstart, time);
	}
	/**
	 * constructor for temperature - constant velocity scan using current temperature ramp rate- 11 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param waittime
	 * @param motor
	 * @param mstart
	 * @param time
	 * @param theta 
	 * @param start 
	 * @param stop 
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, int waittime, Scannable motor,
			double mstart, double time, Scannable theta, double start, double stop) {
		this(temp, tstart, tstop, tstep, Double.NaN, waittime, null, 0, 0, 0, motor, mstart, time, theta, start, stop);
	}
	/**
	 * constructor for temperature - constant velocity scan- 10 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param trate
	 * @param waittime
	 * @param motor
	 * @param mstart
	 * @param mstop
	 * @param time
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, double trate, int waittime,
			Scannable motor, double mstart, double mstop, double time) {
		this(temp, tstart, tstop, tstep, trate, waittime, null, 0, 0, 0, motor, mstart, mstop, time);
	}
	/**
	 * constructor for temperature constant velocity scan using current ramp rate and no wait for temperature
	 * equilibrium - 10 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param motor
	 * @param mstart
	 * @param time
	 * @param theta 
	 * @param start 
	 * @param stop 
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, Scannable motor, double mstart,
			double time, Scannable theta, double start, double stop) {
		this(temp, tstart, tstop, tstep, Double.NaN, 0, null, 0, 0, 0, motor, mstart, time, theta, start, stop);
	}

	/**
	 * constructor for temperature - constant velocity scan- 9 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param trate
	 * @param waittime
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, double trate, int waittime,
			Scannable motor, double mstart, double time) {
		this(temp, tstart, tstop, tstep, trate, waittime, null, 0, 0, 0, motor, mstart, time);
	}
	/**
	 * constructor for temperature - constant velocity scan using current temperature ramp rate- 8 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param waittime
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, int waittime, Scannable motor,
			double mstart, double time) {
		this(temp, tstart, tstop, tstep, Double.NaN, waittime, null, 0, 0, 0, motor, mstart, time);
	}

	/**
	 * constructor for temperature constant velocity scan using current ramp rate and no wait for temperature
	 * equilibrium - 7 inputs
	 * 
	 * @param temp
	 * @param tstart
	 * @param tstop
	 * @param tstep
	 * @param motor
	 * @param mstart
	 * @param time
	 */
	public TemperatureScan(Scannable temp, double tstart, double tstop, double tstep, Scannable motor, double mstart,
			double time) {
		this(temp, tstart, tstop, tstep, Double.NaN, 0, null, 0, 0, 0, motor, mstart, time);
	}

	@Override
	public void doCollection() throws Exception {
		try {
			if (!this.isChild) {
				logger.info("Starting scan.");
			}
			// two axis scan
			if (x != null) {
				if (tempStart < tempStop) {
					// ramp up
					if (tempStep < 0) {
						throw new IllegalArgumentException(temp.getName()
								+ "'s temperature step must be greater than 0 for ramp up.");
					}
					for (double xpos = tempStart; xpos <= tempStop; xpos += tempStep) {
						JythonServerFacade.getInstance().print("Setting " + temp.getName() + " to " + xpos);
						temp.asynchronousMoveTo(xpos);
						// wait until temperature reaches the value
						try {
							temp.waitWhileBusy();
						} catch (Exception e) {
							// convert to a device exception
							throw new DeviceException(e.getMessage(), e.getCause());
						}
						checkForInterrupts();
						if (this.waitTime != 0) {
							try {
								Thread.sleep(this.waitTime * 1000);
							} catch (InterruptedException e) {
								// noop
							}
						}
						checkForInterrupts();
						if (xStart < xStop) {
							if (xStep < 0) {
								throw new IllegalArgumentException(x.getName()
										+ "'s step must be greater than 0 for ramp up.");
							}
							for (double ypos = xStart; ypos <= xStop; ypos += xStep) {
								JythonServerFacade.getInstance().print("Moving " + x.getName() + " to " + ypos);
								x.asynchronousMoveTo(ypos);
								// wait until sample stage translation to complete
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
						} else {
							if (xStep < 0) {
								throw new IllegalArgumentException(x.getName()
										+ "'s step must be greater than 0 for ramp down.");
							}
							for (double ypos = xStart; ypos >= xStop; ypos -= xStep) {
								JythonServerFacade.getInstance().print("Moving " + x.getName() + " to " + ypos);
								x.asynchronousMoveTo(ypos);
								// wait until sample stage translation to complete
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

				} else {
					// ramp down
					if (tempStep < 0) {
						throw new IllegalArgumentException(temp.getName()
								+ "'s temperature step must be greater than 0 for ramp down.");
					}
					for (double xpos = tempStart; xpos >= tempStop; xpos -= tempStep) {
						JythonServerFacade.getInstance().print("Setting " + temp.getName() + " to " + xpos);
						temp.asynchronousMoveTo(xpos);
						// wait until temperature reaches the value
						try {
							temp.waitWhileBusy();
						} catch (Exception e) {
							// convert to a device exception
							throw new DeviceException(e.getMessage(), e.getCause());
						}
						checkForInterrupts();
						if (this.waitTime != 0) {
							try {
								Thread.sleep(this.waitTime * 1000);
							} catch (InterruptedException e) {
								// noop
							}
						}
						checkForInterrupts();
						if (xStart < xStop) {
							if (xStep < 0) {
								throw new IllegalArgumentException(x.getName()
										+ "'s step must be greater than 0 for ramp up.");
							}
							for (double ypos = xStart; ypos <= xStop; ypos += xStep) {
								JythonServerFacade.getInstance().print("Moving " + x.getName() + " to " + ypos);
								x.asynchronousMoveTo(ypos);
								// wait until sample stage translation to complete
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
						} else {
							if (xStep < 0) {
								throw new IllegalArgumentException(x.getName()
										+ "'s step must be greater than 0 for ramp down.");
							}
							for (double ypos = xStart; ypos >= xStop; ypos -= xStep) {
								JythonServerFacade.getInstance().print("Moving " + x.getName() + " to " + ypos);
								x.asynchronousMoveTo(ypos);
								// wait until sample stage translation to complete
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
				}
			} else {
				// single axis scan
				if (tempStart < tempStop) {
					// ramp up
					if (tempStep < 0) {
						throw new IllegalArgumentException(temp.getName()
								+ "'s temperature step must be greater than 0 for ramp up.");
					}
					for (double pos = tempStart; pos <= tempStop; pos += tempStep) {
						JythonServerFacade.getInstance().print("Setting " + temp.getName() + " to " + pos);
						temp.asynchronousMoveTo(pos);
						// wait until it is completed
						try {
							temp.waitWhileBusy();
						} catch (Exception e) {
							// convert to a device exception
							throw new DeviceException(e.getMessage(), e.getCause());
						}
						checkForInterrupts();
						// wait for temperature equilibrium on sample
						if (this.waitTime != 0) {
							try {
								Thread.sleep(this.waitTime * 1000);
							} catch (InterruptedException e) {
								// noop
							}
						}
						checkForInterrupts();
						// constant velocity scan
						for (CVScan childScan : allChildScans) {
							childScan.run();
						}
						checkForInterrupts();
					}
				} else {
					// ramp down
					if (tempStep < 0) {
						throw new IllegalArgumentException(temp.getName()
								+ "'s temperature step must be greater than 0 for ramp down.");
					}
					for (double pos = tempStart; pos >= tempStop; pos -= tempStep) {
						JythonServerFacade.getInstance().print("Setting " + temp.getName() + " to " + pos);
						temp.asynchronousMoveTo(pos);
						// wait until it is completed
						try {
							temp.waitWhileBusy();
						} catch (Exception e) {
							// convert to a device exception
							throw new DeviceException(e.getMessage(), e.getCause());
						}
						checkForInterrupts();
						// wait for temperature equilibrium on sample
						if (this.waitTime != 0) {
							try {
								Thread.sleep(this.waitTime * 1000);
							} catch (InterruptedException e) {
								// noop
							}
						}
						checkForInterrupts();
						// constant velocity scan
						for (CVScan childScan : allChildScans) {
							childScan.run();
						}
						checkForInterrupts();
					}
				}
			}
		} catch (Exception ex1) {
			interrupted = true;
			// move back to original or robot safe position when end
			if (x != null) {
				x.asynchronousMoveTo(0);
				try {
					x.waitWhileBusy();
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
		JythonServerFacade.getInstance().print("Starting temperature scan with nested CVScan ......");
		if (!isChild()) {
			super.prepareForCollection();
		}
		// set the temperature ramp rate for the scan if provided,else using
		// current value
		if (!Double.isNaN(this.rampRate)) {
			temp.setRampRate(this.rampRate);
		}

	}

}
