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

package gda.device.robot;

import gda.device.DeviceException;
import gda.device.Robot;
import gda.device.robot.CurrentSamplePosition.CurrentSamplePositionListener;
import gda.device.robot.RobotNX100Controller.ErrorListener;
import gda.device.robot.RobotNX100Controller.Job;
import gda.device.robot.RobotSampleState.SampleStateListener;
import gda.device.scannable.ScannableBase;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;

import java.lang.reflect.Array;

import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * I11Robot Class
 */
public class I11Robot extends ScannableBase implements Robot, IObserver {

	/**
	 * logging instance
	 */

	private static final Logger logger = LoggerFactory.getLogger(I11Robot.class);

	private String robotControllerName;

	private String sampleStateControllerName;

	private String nextSampleNumberControllerName;

	private String currentSampleNumberControllerName;
	
	private RobotNX100Controller robotController;

	private RobotSampleState sampleStateController;

	private NextSamplePosition nextSampleNumberController;

	private CurrentSamplePosition currentSampleNumberController;

	private volatile SampleState state = SampleState.UNKNOWN;

	private double sampleNumber = -1.0;

	private String err = "OK";
	private boolean started=false;

	private boolean busy = false;
	/**
	 * Array of strings which specify the format to output when getting the position of this Scannable.
	 */
	protected String[] outputFormat = new String[] { "%5d" };

	private boolean stopInProgress = false;

	private DoorLatchState doorLatch;

	public DoorLatchState getDoorLatch() {
		return doorLatch;
	}

	public void setDoorLatch(DoorLatchState doorLatch) {
		this.doorLatch = doorLatch;
	}

	public boolean isStopInProgress() {
		return stopInProgress;
	}

	public void setStopInProgress(boolean stopInProgress) {
		this.stopInProgress = stopInProgress;
	}

	/**
	 * Constructor
	 */
	public I11Robot() {

	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (robotController == null) {
				if ((robotController = (RobotNX100Controller) Finder.getInstance().find(robotControllerName)) != null) {
					logger.debug("Robot controller {} found", robotControllerName);

				} else {
					logger.error("Robot controller {} not found", robotControllerName);
					throw new FactoryException("Robot controller " + robotControllerName + " not found");
				}
			}
			robotController.addIObserver(this);
			if (sampleStateController == null) {
				if ((sampleStateController = (RobotSampleState) Finder.getInstance().find(sampleStateControllerName)) != null) {
					logger.debug("Robot sample state controller {} found", sampleStateControllerName);

				} else {
					logger.error("Robot sample state controller {} not found", sampleStateControllerName);
					throw new FactoryException("Robot  sample state controller " + sampleStateControllerName + " not found");
				}
			}
			sampleStateController.addIObserver(this);
			if (nextSampleNumberController == null) {
				if ((nextSampleNumberController = (NextSamplePosition) Finder.getInstance().find(nextSampleNumberControllerName)) != null) {
					logger.debug("Robot next sample position controller {} found", nextSampleNumberControllerName);

				} else {
					logger.error("Robot next sample position controller {} not found", nextSampleNumberControllerName);
					throw new FactoryException("Robot next sample position controller " + nextSampleNumberControllerName
							+ " not found");
				}
			}
			nextSampleNumberController.addIObserver(this);
			if (currentSampleNumberController ==null) {
				if ((currentSampleNumberController = (CurrentSamplePosition) Finder.getInstance().find(currentSampleNumberControllerName)) != null) {
					logger.debug("Robot current sample position controller {} found", currentSampleNumberControllerName);

				} else {
					logger
							.error("Robot current sample position controller {} not found",
									currentSampleNumberControllerName);
					throw new FactoryException("Robot current sample position controller "
							+ currentSampleNumberControllerName + " not found");
				}
			}
			currentSampleNumberController.addIObserver(this);
			if (doorLatch ==null) {
					logger.error("Door Latch state object must be provided");
					throw new FactoryException("Cannot find the Door Latch state object.");
			}
			doorLatch.addIObserver(this);
			
			configured = true;
		}
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (!started) start();
		if (doorLatch.getState()==1) {
			recover();
			doorLatch.resetDoorLatch();
		}
		double pos = Double.valueOf(position.toString()).doubleValue();
		if (pos >= RobotNX100Controller.MIN_NUMBER_OF_SAMPLES || pos <= RobotNX100Controller.MAX_NUMBER_OF_SAMPLES) {
			setBusy(true);
			nextSample(pos);
		} else {
			logger.warn("Sample number must be between {} and {}.", RobotNX100Controller.MIN_NUMBER_OF_SAMPLES,
					RobotNX100Controller.MAX_NUMBER_OF_SAMPLES);
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		return getSamplePosition();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return this.busy;
	}

	/**
	 * @param b
	 */
	public void setBusy(boolean b) {
		this.busy = b;
	}
	
	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while (isBusy()) {
			Thread.sleep(100);
		}
	}

	@Override
	public void atScanStart() throws DeviceException {
		start();
		//recover(); // ensure any left-over sample is cleared from diffractometer
	}

	@Override
	public void atScanEnd() throws DeviceException {
		clearSample();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// do nothing
		}
		//finish();
	}

	@Override
	public void start() throws DeviceException {
		robotController.clearError();
		robotController.servoOn();
		error();
		robotController.release();
		error();
		started=true;
	}

	@Override
	public void clearSample() throws DeviceException {
		setBusy(true);
		if (state == SampleState.DIFF) {
			runJob(Job.PICKD);
			runJob(Job.PLACEC);
		} else if (state == SampleState.INJAWS) {
			runJob(Job.PLACEC);
		} else if (state == SampleState.CAROUSEL) {
			// do nothing
		} else {
			logger.error("Robot Sample is in an UNKNOWN state. Please investigate");
			throw new DeviceException("clear sample from Diffractometer failed.");
		}
		//recover(); // ensure any left-over sample is cleared from diffractometer
		runJob(Job.TABLEIN);
		setBusy(false);
		JythonServerFacade.getInstance().print("Clear Sample from Diffractometer completed.");
	}

	/**
	 * runs the job
	 * 
	 * @param job
	 * @throws DeviceException
	 */
	private void runJob(Job job) throws DeviceException {
		robotController.setJob(job);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// do nothing
		}
		robotController.start();
		error(job);
	}

	/**
	 * polls error code from Robot. {@inheritDoc}
	 * 
	 * @see gda.device.Robot#getError()
	 */
	@Override
	public String getError() throws DeviceException {
		return robotController.getError();
	}

	/**
	 * finish robot action and power it down. {@inheritDoc}
	 * 
	 * @see gda.device.Robot#finish()
	 */
	@Override
	public void finish() throws DeviceException {
		robotController.servoOff();
		error();
		started = false;
	}

	/**
	 * polls sample position number from robot {@inheritDoc}
	 * 
	 * @see gda.device.Robot#getSamplePosition()
	 */
	@Override
	public double getSamplePosition() throws DeviceException {
		return currentSampleNumberController.getActualSamplePosition();
	}

	/**
	 * polls the sample state from robot {@inheritDoc}
	 * 
	 * @see gda.device.Robot#getSampleState()
	 */
	@Override
	public SampleState getSampleState() throws DeviceException {
		return sampleStateController.getSampleState();
	}

	@Override
	public void nextSample() throws DeviceException {
		double n = sampleNumber + 1.0;
		nextSample(n);
	}

	@Override
	public void nextSample(double n) throws DeviceException {
		setBusy(true);
		//poll sample state before changing sample as event update not reliable anymore.
		state=sampleStateController.getSampleState();
		// clear sample from diffractometer before put on next sample
		if (state == SampleState.DIFF) {
			runJob(Job.PICKD);
			runJob(Job.PLACEC);
		} else if (state == SampleState.INJAWS) {
			runJob(Job.PLACEC);
		} else if (state == SampleState.CAROUSEL) {
			// do nothing
		} else if (state == SampleState.UNKNOWN) 
		{
			logger.error("UNKNOWN Sample state from robot, exit");
			throw new DeviceException("UNKNOWN Sample state from robot");
		}
		else 
		{
			logger.error("Trying a pick from carousel with sample={}, state={}, exit", n, state.value());
			throw new DeviceException("Trying a pick from carousel with sample=" + n + ", state=" + state.value());
		}
		// put on next sample
		if (state == SampleState.CAROUSEL) {
			nextSampleNumberController.setSamplePosition(n);
			runJob(Job.PICKC);
			if (state == SampleState.INJAWS) {
				runJob(Job.PLACED);
			} else {
				runJob(Job.TABLEIN);
				JythonServerFacade.getInstance().print("There is no sample at this position "+ n);
				logger.warn("{}: No sample at sample holder position {}.", getName(), n);
			}
		}
		setBusy(false);
	}

	@Override
	public void recover() throws DeviceException {
		setBusy(true);
		runJob(Job.RECOVER);
		robotController.clearError();
		setBusy(false);
	}

	@Override
	public void stop() throws DeviceException {
		if (!started) return;
		if (stopInProgress ) return; // do not call stop twice at the same time, synchronized would not do as it queues the request.
		setStopInProgress(true);
		robotController.hold();
		JythonServerFacade.getInstance().print("Stopping the Robot. Please wait until stop is DONE.");
		logger.info("Emergence stop of robot");
		robotController.release();
		clearSample(); // on emergency stop we want sample go back to original position on the carousel.
		JythonServerFacade.getInstance().print("Sample changer stopped. You may now open the Hutch Door.");
		logger.info("Stop completed. You may now open the Hutch Door.");
		setStopInProgress(false);
	}

	/**
	 * check if error code is empty, print error message to users, then terminate.
	 */
	private void error() {
		if (!err.equalsIgnoreCase("OK")) {
			JythonServerFacade.getInstance().print("Error : " + err + ", Report to Engineer.");
			throw new IllegalStateException("EPICS Robot controller report error code " + err);
		}
	}

	/**
	 * check if error code is empty, print error message to users, then terminate.
	 * 
	 * @param job
	 */
	private void error(Job job) {
		if (!err.equalsIgnoreCase("OK")) {
			JythonServerFacade.getInstance().print("Error : " + err + ", Report to Engineer.");
			throw new IllegalStateException("EPICS Robot controller report error code " + err);
		}

		JythonServerFacade.getInstance().print("Job: " + job.name() + " done.");

	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof SampleStateListener) {
			state = (SampleState) changeCode;
		} else if (theObserved instanceof CurrentSamplePositionListener) {
			sampleNumber = (Double) changeCode;
		} else if (theObserved instanceof ErrorListener) {
			err = (String) changeCode;
		}

	}

	@Override
	public String toString() {
		String myString = this.getName() + " : ";
		try {
			Object position = this.getPosition();

			if (position == null) {
				logger.warn("getPosition() from " + this.getName() + " returns NULL.");
				return myString += " NOT AVAILABLE";
			}
			// print out simple version if only one inputName and
			// getPosition and getReportingUnits do not return arrays.
			if (!(position.getClass().isArray() || position instanceof PySequence)) {
				if (position instanceof String) {
					myString += position.toString();
				} else {
					myString += String.format(outputFormat[0], position);
				}
			} else {
				if (position instanceof PySequence) {
					for (int i = 0; i < ((PySequence) position).__len__(); i++) {
						if (i > 0) {
							myString += " ";
						}
						myString += String.format(outputFormat[i], ((PySequence) position).__finditem__(i).toString());
					}
				} else {
					for (int i = 0; i < Array.getLength(position); i++) {
						if (i > 0) {
							myString += " ";
						}
						myString += String.format(outputFormat[i], Array.get(position, i).toString());
					}
				}
			}
		} catch (NumberFormatException e) {
			logger.error("Number Format Exception ", e);
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error("Array Index out of bounds ", e);
		} catch (IllegalArgumentException e) {
			logger.error("Illegal Argument ", e);
		} catch (DeviceException e) {
			logger.error("Device Exception ", e);
		}
		return myString;
	}

	/**
	 * @return robotControllerName
	 */
	public String getRobotControllerName() {
		return robotControllerName;
	}

	/**
	 * @param robotControllerName
	 */
	public void setRobotControllerName(String robotControllerName) {
		this.robotControllerName = robotControllerName;
	}

	/**
	 * @return sampleStateControllerName
	 */
	public String getSampleStateControllerName() {
		return sampleStateControllerName;
	}

	/**
	 * @param sampleStateControllerName
	 */
	public void setSampleStateControllerName(String sampleStateControllerName) {
		this.sampleStateControllerName = sampleStateControllerName;
	}

	/**
	 * @return nextSampleNumberControllerName
	 */
	public String getNextSampleNumberControllerName() {
		return nextSampleNumberControllerName;
	}

	/**
	 * @param nextSampleNumberControllerName
	 */
	public void setNextSampleNumberControllerName(String nextSampleNumberControllerName) {
		this.nextSampleNumberControllerName = nextSampleNumberControllerName;
	}

	/**
	 * @return currentSampleNumberControllerName
	 */
	public String getCurrentSampleNumberControllerName() {
		return currentSampleNumberControllerName;
	}

	/**
	 * @param currentSampleNumberControllerName
	 */
	public void setCurrentSampleNumberControllerName(String currentSampleNumberControllerName) {
		this.currentSampleNumberControllerName = currentSampleNumberControllerName;
	}
	/**
	 * return the robot controller instance.
	 * @return the robot controller instance
	 */
	public RobotNX100Controller getRobotController() {
		return robotController;
	}
	/**
	 * sets the robot controller instance
	 * @param robotController
	 */
	public void setRobotController(RobotNX100Controller robotController) {
		this.robotController = robotController;
	}
	/**
	 * returns the sample state controller instance
	 * @return the sample state controller instance
	 */
	public RobotSampleState getSampleStateController() {
		return sampleStateController;
	}
	/**
	 * sets the sample state controller instance
	 * @param sampleStateController
	 */
	public void setSampleStateController(RobotSampleState sampleStateController) {
		this.sampleStateController = sampleStateController;
	}
	/**
	 * returns the next sample position controller instance
	 * @return the next sample position controller instance
	 */
	public NextSamplePosition getNextSampleNumberController() {
		return nextSampleNumberController;
	}
	/**
	 * sets the next sample position controller instance
	 * @param nextSampleNumberController
	 */
	public void setNextSampleNumberController(NextSamplePosition nextSampleNumberController) {
		this.nextSampleNumberController = nextSampleNumberController;
	}
	/**
	 * returns the current sample position controller instance
	 * @return the current sample position controller instance
	 */
	public CurrentSamplePosition getCurrentSampleNumberController() {
		return currentSampleNumberController;
	}
	/**
	 * gets the current sample position controller instance
	 * @param currentSampleNumberController
	 */
	public void setCurrentSampleNumberController(CurrentSamplePosition currentSampleNumberController) {
		this.currentSampleNumberController = currentSampleNumberController;
	}

	public double getSampleNumber() {
		return sampleNumber;
	}

	public void setSampleNumber(double sampleNumber) {
		this.sampleNumber = sampleNumber;
	}

}
