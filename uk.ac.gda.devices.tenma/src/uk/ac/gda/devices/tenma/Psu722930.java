/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.tenma;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.Findable;
import uk.ac.gda.devices.tenma.api.IPsu722930;
import uk.ac.gda.devices.tenma.api.IPsu722931Controller;

public class Psu722930 implements IPsu722930, Findable {

	private static final Logger logger = LoggerFactory.getLogger(Psu722930.class);

	private String name;

	private double currentStepSize = 0.1;
	private Timer timer = new Timer("Timer");

	IPsu722931Controller controller;

	public Psu722930(IPsu722931Controller controller) {
		this.controller = controller;
	}

	@Override
	public double getCurrentStepSize() {
		return currentStepSize;
	}

	@Override
	public void setCurrentStepSize(double currentStepSize) {
		this.currentStepSize = currentStepSize;
	}

	@Override
	public double getCurrent() throws DeviceException {
		return controller.getCurrent();
	}

	@Override
	public void setCurrent(double current) throws DeviceException {
		controller.setCurrent(current);
	}

	@Override
	public double getVoltage() throws DeviceException {
		return controller.getVoltage();
	}

	@Override
	public void setVoltage(double voltage) throws DeviceException {
		controller.setVoltage(voltage);
	}

	@Override
	public void outputOn() throws DeviceException {
		controller.outputOn();
	}

	@Override
	public void outputOff() throws DeviceException {
		controller.outputOff();
	}

	@Override
	public boolean outputIsOn() throws DeviceException {
		return controller.outputIsOn();
	}

	@Override
	public void rampCurrent(double startCurrent, double endCurrent, double rampTimeInSeconds)
			throws DeviceException, InterruptedException {
		if (startCurrent == endCurrent) {
			setCurrent(startCurrent);
			logger.debug("Start and end current are the same, nothing to do.");
			return;
		}

		// Using a task and timer here so that I/O overheads don't mess up the timing
		RampCurrentTask rampTask = new RampCurrentTask(startCurrent, endCurrent, getCurrentStepSize(),
				rampTimeInSeconds);
		long stepIntervalInMilliseconds = rampTask.getStepIntervalInMilliseconds();
		long rampTimeInMilliseconds = rampTask.getRampTimeInMilliseconds();

		timer.scheduleAtFixedRate(rampTask, 0L, stepIntervalInMilliseconds);

		// Method should not return until execution has finished
		Thread.sleep(rampTimeInMilliseconds + 1000L);
	}

	@Override
	public void rampToZero(double rampTimeInSeconds) throws DeviceException, InterruptedException {
		double current = getCurrent();

		if (current == 0) {
			logger.debug("Current is already at 0, nothing to do.");
			return;
		}

		rampCurrent(current, 0d, rampTimeInSeconds);
	}

	@Override
	public void rampToZero() throws DeviceException, InterruptedException {
		rampToZero(60);
	}

	private class RampCurrentTask extends TimerTask {

		private Queue<Double> currentValues = new LinkedList<>();
		private long rampTimeInMilliseconds;
		private boolean firstStep = true;

		private RampCurrentTask(double startCurrent, double endCurrent, double currentStepSize,
				double rampTimeInSeconds) {
			this.rampTimeInMilliseconds = (long) (rampTimeInSeconds * 1000l);
			createQueue(startCurrent, endCurrent, currentStepSize);
		}

		@Override
		public void run() {
			try {
				controller.setCurrent(currentValues.remove());
				if (firstStep) {
					firstStep = false;
					if (!controller.outputIsOn()) {
						controller.outputOn();
						logger.warn("Tenma PSU turned on automatically due to current ramping.");
					}
				}
			} catch (DeviceException exception) {
				throw new RuntimeException("Communication error while setting current", exception);
			}

			if (currentValues.isEmpty()) {
				cancel();
			}
		}

		private void createQueue(double startCurrent, double endCurrent, double currentStepSize) {
			double step;
			if (endCurrent > startCurrent) {
				step = currentStepSize;
			} else {
				step = -currentStepSize;
			}

			logger.debug("Step is {}", step);

			BigDecimal currentCurrent = BigDecimal.valueOf(startCurrent);
			currentValues.add(currentCurrent.doubleValue());

			do {
				if (Math.abs(endCurrent - currentCurrent.doubleValue()) >= currentStepSize) {
					currentCurrent = currentCurrent.add(BigDecimal.valueOf(step));
				} else {
					currentCurrent = BigDecimal.valueOf(endCurrent);
				}
				currentValues.add(currentCurrent.doubleValue());

			} while (currentCurrent.doubleValue() != endCurrent);

			logger.debug("Queue is {}", currentValues.toString());
		}

		private long getRampTimeInMilliseconds() {
			return rampTimeInMilliseconds;
		}

		private long getStepIntervalInMilliseconds() {
			int numberOfSteps = currentValues.size() - 1;
			return rampTimeInMilliseconds / numberOfSteps;
		}

	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
