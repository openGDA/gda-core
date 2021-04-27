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

package uk.ac.gda.devices.pressurecell.controller.dummy;

import static uk.ac.gda.devices.pressurecell.controller.CellStatus.BUSY;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.gda.devices.pressurecell.controller.ArmablePressureValve;
import uk.ac.gda.devices.pressurecell.controller.CellStatus;
import uk.ac.gda.devices.pressurecell.controller.PressureCellController;
import uk.ac.gda.devices.pressurecell.controller.PressureValve;

public class DummyPressureCellController implements PressureCellController {
	private static final Logger logger = LoggerFactory.getLogger(DummyPressureCellController.class);

	private static final double ATMOSPHERE = 1; // all pressures are in bar

	private double cellPressure = ATMOSPHERE;
	private double chamberPressure = ATMOSPHERE;
	private double pumpPressure = ATMOSPHERE;

	private double targetPressure = ATMOSPHERE;

	private PressureValve v3;
	private ArmablePressureValve v5;
	private ArmablePressureValve v6;

	private double jumpToPressure = ATMOSPHERE;
	private double jumpFromPressure = ATMOSPHERE;

	private CellStatus status;

	@Override
	public double getCellPressure() {
		return cellPressure;
	}

	@Override
	public double getIntermediatePressure() {
		return chamberPressure;
	}

	@Override
	public double getPumpPressure() {
		return pumpPressure;
	}

	@Override
	public PressureValve getV3() {
		return v3;
	}

	@Override
	public ArmablePressureValve getV5() {
		return v5;
	}

	@Override
	public ArmablePressureValve getV6() {
		return v6;
	}

	@Override
	public double getTargetPressure() {
		return targetPressure;
	}

	@Override
	public void setTargetPressure(double target) {
		logger.debug("Setting target pressure to {}", target);
		targetPressure = target;
	}

	@Override
	public double getJumpToPressure() {
		return jumpToPressure;
	}

	@Override
	public void setJumpToPressure(double target) {
		logger.debug("Setting jump to pressure to {}", target);
		jumpToPressure = target;
	}

	@Override
	public double getJumpFromPressure() {
		return jumpFromPressure;
	}

	@Override
	public void setJumpFromPressure(double target) {
		logger.debug("Setting jump from pressure to {}", target);
		jumpFromPressure = target;
	}

	@Override
	public void stopPump() {
		logger.debug("Stopping pump");
	}

	@Override
	public void stop() {
		logger.debug("Stopping pressure controller");
	}

	@Override
	public CellStatus getGoToBusy() {
		return status;
	}

	@Override
	public void waitForIdle(long timeout, TimeUnit unit) {
		// don't wait in dummy mode
	}

	@Override
	public void go() {
		cellPressure = targetPressure;
	}

	@Override
	public void setJump() throws DeviceException {
		cellPressure = jumpFromPressure;
		chamberPressure = jumpToPressure;
		v3.close();
		v5.close();
		v6.close();
	}

	@Override
	public Future<Void> asyncGo() {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public boolean isBusy() {
		return status == BUSY;
	}
}
