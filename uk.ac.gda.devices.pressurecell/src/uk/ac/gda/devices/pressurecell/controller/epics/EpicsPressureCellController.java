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

package uk.ac.gda.devices.pressurecell.controller.epics;

import static java.lang.Math.abs;
import static java.util.Objects.requireNonNull;
import static uk.ac.gda.devices.pressurecell.controller.CellStatus.BUSY;
import static uk.ac.gda.devices.pressurecell.controller.CellStatus.IDLE;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import uk.ac.gda.devices.pressurecell.controller.ArmablePressureValve;
import uk.ac.gda.devices.pressurecell.controller.CellStatus;
import uk.ac.gda.devices.pressurecell.controller.PressureCellController;
import uk.ac.gda.devices.pressurecell.controller.PressureValve;

/**
 * Controller to interface with the Epics Pressure Cell
 */
public class EpicsPressureCellController extends ConfigurableBase implements PressureCellController {
	private static final Logger logger = LoggerFactory.getLogger(EpicsPressureCellController.class);

	/**
	 * Root of all PVs used by this controller up to (but not including first ':'.
	 * <br>
	 * eg BL38P-EA-HPXC-01
	 */
	private String rootPv;
	/** Name of this controller - used for logging + debug only */
	private String name;
	/** Pressure tolerance used to determine if a move has completed successfully */
	private double tolerance = 20;

	/** PV for the cell pressure RBV */
	private ReadOnlyPV<Double> cellPressurePV;
	/** PV for the intermediate pressure RBV */
	private ReadOnlyPV<Double> intermediatePressurePV;
	/** PV for the pump pressure RBV */
	private ReadOnlyPV<Double> pumpPressurePV;

	/** PV for the pressure to be set when the pump is started */
	private PV<Double> targetPressurePV;

	/** PV for the cell pressure to be set at the cell prior to a pressure jump */
	private PV<Double> jumpFromPressurePV;
	/**
	 * PV for the intermediate pressure to be set prior to a pressure jump - this is therefore
	 * the pressure that will be reached after a pressure jump when the valve is triggered in
	 * a jump.
	 */
	private PV<Double> jumpToPressurePV;

	/** The valve between the pump and the intermediate chamber */
	private EpicsPressureValve v3;
	/**
	 * One of the valves between the intermediate chamber and the cell. This
	 * is the valve to be used in a positive pressure jumps (jumping to a higher pressure).
	 */
	private EpicsArmablePressureValve v5;
	/**
	 * One of the valves between the intermediate chamber and the cell. This
	 * is the valve to be used in a negative pressure jumps (jumping to a lower pressure).
	 */
	private EpicsArmablePressureValve v6;

	/** Control PV to stop the pump */
	private PV<Integer> stopPumpPV;
	/** Control PV to stop the cell */
	private PV<Integer> stopPV;
	/** Control PV to start the pump towards the target pressure */
	private PV<Integer> goPV;
	/** Control PV to start the procedure to set the high and low pressures for a pressure jump */
	private PV<Integer> setJumpPV;

	/** PV for the cell status */
	private PV<CellStatus> statusPV;
	/**
	 * Reference to a move currently in progress
	 * <br>
	 * The future that is the move lets the methods that start the move to return without
	 * waiting or monitoring the status of the cell. The background status monitor can then
	 * complete the future when the move is complete.
	 * <br>
	 * The future is wrapped in an AtomicReference to prevent moves being triggered while the
	 * previous move has not been completed.
	 */
	private AtomicReference<CompletableFuture<Double>> currentMove = new AtomicReference<>();

	/** The current state of the cell. Kept current by a monitor on the status PV */
	private volatile CellStatus state = CellStatus.IDLE;
	/** The pressure at the pump. Kept current by a monitor on the pump pressure */
	private volatile double pumpPressure;

	@Override
	public void configure() throws FactoryException {
		requireNonNull(rootPv);
		cellPressurePV = LazyPVFactory.newReadOnlyDoublePV(rootPv + ":PP3:PRES");
		intermediatePressurePV = LazyPVFactory.newReadOnlyDoublePV(rootPv + ":PP2:PRES");
		pumpPressurePV = LazyPVFactory.newReadOnlyDoublePV(rootPv + ":PP1:PRES");

		targetPressurePV = LazyPVFactory.newDoublePV(rootPv + ":CTRL:TARGET");

		jumpFromPressurePV = LazyPVFactory.newDoublePV(rootPv + ":CTRL:JUMPF");
		jumpToPressurePV = LazyPVFactory.newDoublePV(rootPv + ":CTRL:JUMPT");

		stopPumpPV = LazyPVFactory.newIntegerPV(rootPv + ":SP:AUTO");
		stopPV = LazyPVFactory.newIntegerPV(rootPv + ":CTRL:STOP");
		goPV = LazyPVFactory.newIntegerPV(rootPv + ":CTRL:GO");
		setJumpPV = LazyPVFactory.newIntegerPV(rootPv + ":CTRL:SETJUMP");

		statusPV = LazyPVFactory.newEnumPV(rootPv + ":CTRL:GOTOBUSY", CellStatus.class);
		try {
			statusPV.addObserver(this::statusChanged);
		} catch (Exception e) {
			logger.error("{} - Could not monitor cell status", name, e);
		}
		try {
			pumpPressurePV.addObserver((s, p) -> pumpPressure = p);
		} catch (Exception e) {
			logger.error("{} - Could not add monitor pump pressure", name, e);
		}
	}

	/**
	 * Update method called by the monitor on the status PV
	 * <br>
	 * Relied on by the move mechanism to complete move futures when the expected state
	 * is reached.
	 * @param src The PV sending the update
	 * @param newStatus the new status that triggered the update.
	 */
	private void statusChanged(@SuppressWarnings("unused") Object src, CellStatus newStatus) {
		if (state == IDLE && newStatus == BUSY) {
			if (currentMove.compareAndSet(null, new CompletableFuture<>())) {
				logger.debug("{} - Move initiated from outside GDA", getName());
			} else {
				logger.debug("{} - Has started move", name);
			}
		} else if (state == BUSY && newStatus == IDLE) {
			logger.debug("{} - Has completed move", name);
			var move = currentMove.getAndSet(null);
			if (move != null) {
				move.complete(pumpPressure);
			}
		}
		state = newStatus;
	}

	@Override
	public double getCellPressure() throws DeviceException {
		try {
			return cellPressurePV.get();
		} catch (IOException e) {
			throw new DeviceException("Could not access cell pressure PV", e);
		}
	}

	@Override
	public double getIntermediatePressure() throws DeviceException {
		try {
			return intermediatePressurePV.get();
		} catch (IOException e) {
			throw new DeviceException("Could not access intermediate pressure PV", e);
		}
	}

	@Override
	public double getPumpPressure() throws DeviceException {
		try {
			return pumpPressurePV.get();
		} catch (IOException e) {
			throw new DeviceException("Could not access pump pressure PV", e);
		}
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
	public double getTargetPressure() throws DeviceException {
		try {
			return targetPressurePV.get();
		} catch (IOException e) {
			throw new DeviceException("Could not access target pressure PV", e);
		}
	}

	@Override
	public void setTargetPressure(double target) throws DeviceException {
		try {
			logger.debug("{} - Setting target pressure to {}", name, target);
			targetPressurePV.putWait(target);
		} catch (IOException e) {
			throw new DeviceException("Could not set target pressure", e);
		}
	}

	@Override
	public double getJumpToPressure() throws DeviceException {
		try {
			return jumpToPressurePV.get();
		} catch (IOException e) {
			throw new DeviceException("Could not access 'jump to' pressure PV", e);
		}
	}

	@Override
	public void setJumpToPressure(double target) throws DeviceException {
		try {
			logger.debug("{} - Setting 'jump to' pressure to {}", name, target);
			jumpToPressurePV.putWait(target);
		} catch (IOException e) {
			throw new DeviceException("Could not set 'jump to' pressure", e);
		}
	}

	@Override
	public double getJumpFromPressure() throws DeviceException {
		try {
			return jumpFromPressurePV.get();
		} catch (IOException e) {
			throw new DeviceException("Could not access 'jump from' pressure PV", e);
		}
	}

	@Override
	public void setJumpFromPressure(double target) throws DeviceException {
		try {
			logger.debug("{} - Setting 'jump from' pressure to {}", name, target);
			jumpFromPressurePV.putWait(target);
		} catch (IOException e) {
			throw new DeviceException("Could not set 'jump from' pressure", e);
		}
	}

	@Override
	public void stopPump() throws DeviceException {
		try {
			logger.debug("{} - Stopping pump", name);
			stopPumpPV.putWait(1);
		} catch (IOException e) {
			throw new DeviceException("Could not stop pressure cell pump", e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			logger.debug("{} - Stopping controller", name);
			stopPV.putWait(1);
		} catch (IOException e) {
			throw new DeviceException("Could not stop pressure cell", e);
		}
	}

	@Override
	public boolean isBusy() {
		return state == BUSY || currentMove.get() != null;
	}

	@Override
	public CellStatus getGoToBusy() throws DeviceException {
		try {
			return statusPV.get();
		} catch (IOException e) {
			throw new DeviceException("Could not get pressure cell status", e);
		}
	}

	@Override
	public void waitForIdle(long timeout, TimeUnit unit) throws DeviceException, InterruptedException {
		try {
			var move = currentMove.get();
			if (move != null) {
				if (timeout < 0) {
					move.get();
				} else {
					move.get(timeout, unit);
				}
			}
		} catch (IllegalStateException | ExecutionException | TimeoutException e) {
			throw new DeviceException("Error while waiting for pressure cell", e);
		}
	}

	/**
	 * {@inheritDoc}
	 * <br>
	 * Creates a future for this move and waits for it to be completed by the monitor
	 * on the status PV.
	 */
	// See #statusChanged method for other side of this implementation
	@Override
	public void go() throws DeviceException, InterruptedException {
		try {
			logger.debug("{} - Starting controller towards target pressure", name);
			var move = new CompletableFuture<Double>();
			if (currentMove.compareAndSet(null, move)) {
				var target = getTargetPressure();
				goPV.putWait(1);
				var endPressure = move.get();
				if (abs(endPressure-target) > tolerance) {
					throw new DeviceException(getName() + " - Move completed but pressure was not at target (pressure: " + endPressure + ", target: " + target + ")");
				}
			} else {
				throw new DeviceException(getName() + " - Device is already busy");
			}
		} catch (IOException e) {
			throw new DeviceException("Could not start pressure cell", e);
		} catch (ExecutionException e) {
			throw new DeviceException("Error while waiting for move to complete", e);
		}
	}

	@Override
	public Future<Void> asyncGo() throws DeviceException {
		try {
			logger.debug("{} - Starting controller towards target pressure", name);
			var move = new CompletableFuture<Double>();
			if (currentMove.compareAndSet(null, move)) {
				var target = getTargetPressure();
				goPV.putWait(1);
				return move.thenApply(p -> {
					if (abs(p - target) > tolerance) {
						throw new IllegalStateException(getName() + " - Move completed but pressure was not at target (pressure: " + p + ", target: " + target + ")");
					}
					return null;
				});
			} else {
				throw new DeviceException(getName() + " - Device already busy");
			}
		} catch (IOException e) {
			throw new DeviceException("Could not start pressure cell", e);
		}
	}

	@Override
	public void resetValves() throws DeviceException {
		v5.disarm();
		v6.disarm();
		v3.reset();
		v5.reset();
		v6.reset();
		v3.close();
		v5.close();
		v6.close();
	}

	@Override
	public void setJump() throws DeviceException {
		try {
			logger.debug("{} - Starting controller towards jump pressures", name);
			setJumpPV.putWait(1);
		} catch (IOException e) {
			throw new DeviceException("Could not set pressure cell to jump pressure", e);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRootPv() {
		return rootPv;
	}

	public void setRootPv(String rootPv) {
		this.rootPv = rootPv;
	}

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	public double getTolerance() {
		return this.tolerance;
	}

	public void setV3(EpicsPressureValve valve) {
		this.v3 = valve;
	}

	public void setV5(EpicsArmablePressureValve valve) {
		this.v5 = valve;
	}

	public void setV6(EpicsArmablePressureValve valve) {
		this.v6 = valve;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + rootPv + ")";
	}
}
