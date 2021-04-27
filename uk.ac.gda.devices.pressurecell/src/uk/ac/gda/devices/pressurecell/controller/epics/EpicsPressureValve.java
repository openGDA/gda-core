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

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static uk.ac.gda.devices.pressurecell.controller.PressureValve.ValveState.FAULT;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
import uk.ac.gda.devices.pressurecell.controller.PressureValve;
import uk.ac.gda.devices.pressurecell.controller.ValveControl;

/**
 * One of the valves that make up the pressure cell. This type of valve is not armable
 * and can only be opened or closed (or reset)
 */
public class EpicsPressureValve extends ConfigurableBase implements PressureValve {
	private static final Logger logger = LoggerFactory.getLogger(EpicsPressureValve.class);

	/**
	 * Functional interface to allow all moves to share an implementation.
	 * Effectively Runnable but with checked IOException.
	 */
	private interface Action {
		void run() throws IOException;
	}

	/** The possible actions that can be requested with this valve */
	private enum Request {
		OPEN, CLOSE;
	}

	/** Readback PV for the current valve state */
	private ReadOnlyPV<ValveState> statusPV;
	/** Control PV for closing and resetting the valve */
	private PV<ValveControl> controlPV;
	/** Control PV for starting open sequence - includes reset of interlocks if required */
	private PV<Integer> openPV;

	/**
	 * The root PV of this valve up to but not including the final ':'
	 * eg BL38P-EA-HPXC-01:V6
	 */
	private String rootPv;
	/** The name of this valve - used mainly for debugging */
	private String name;
	/** Time in seconds to wait before a move is assumed to have failed */
	private long timeout = 5;

	/** The action currently in progress */
	private volatile Request request;

	/** The future representing the current move */
	/* AtomicReference to stop multiple moved being started at once */
	private AtomicReference<CompletableFuture<ValveState>> currentMove = new AtomicReference<>();

	@Override
	public void configure() throws FactoryException {
		requireNonNull(rootPv, "Root PV must be set");
		requireNonNull(name, "Name must be set");
		statusPV = LazyPVFactory.newReadOnlyEnumPV(getRootPv() + ":STA", ValveState.class);
		controlPV = LazyPVFactory.newEnumPV(getRootPv() + ":CON", ValveControl.class);
		openPV = LazyPVFactory.newIntegerPV(getRootPv() + ":OPENSEQ");
		try {
			statusPV.addObserver(this::statusChanged);
		} catch (Exception e) {
			logger.error("Could not add status observer", e);
			throw new FactoryException("Couldn't observe status PV");
		}
		super.configure();
	}

	/**
	 * <h1>Monitor the status of the valve</h1>
	 *
	 * When the status changes, check if a move has been requested and update it if required.
	 * If a move has not been requested, this move must have been triggered for elsewhere (most
	 * likely directly in epics).
	 * <br>
	 * If a move has been requested, check if the new state matches the move and mark it as complete
	 * if it does. This allows threads waiting on the move to complete to continue.
	 *
	 * @param source The source of the update - the epics PV
	 * @param state The new state of the valve
	 */
	private void statusChanged(@SuppressWarnings("unused") Object source, ValveState state) {
		if (state == ValveState.FAULT) {
			logger.error("{} -> Valve in error state", name);
			var move = currentMove.getAndSet(null);
			if (move != null) {
				move.completeExceptionally(new DeviceException(name + " - Valve went into fault state"));
			}
			request = null;
		} else if (state == ValveState.OPENING || state == ValveState.CLOSING) {
			if (request != null) {
				logger.debug("{} - move started", name);
			}
			// else move was started by epics - only log when complete
		} else { // state is either open or closed
			if (request == null) { // we didn't ask for the move
				logger.debug("{} - Valve moved from epics. Now {}", name, state);
			} else if ((state == ValveState.OPEN && request == Request.OPEN)
					|| (state == ValveState.CLOSED && request == Request.CLOSE)) {
				logger.debug("{} - Move complete. Now {}", name, state);
				request = null;
				var move = currentMove.getAndSet(null);
				if (move != null) {
					move.complete(state);
				} else {
					logger.warn("{} - Requested move but no move in progress", name);
				}
			} else {
				logger.warn("{} - Unexpected status. Status: {} but {} requested", name, state, request);
			}
		}
	}

	@Override
	public ValveState getState() throws DeviceException {
		try {
			return statusPV.get();
		} catch (IOException e) {
			throw new DeviceException("Could not access valve status", e);
		}
	}

	@Override
	public void open() throws DeviceException {
		logger.debug("{} - Opening valve", name);
		move(Request.OPEN, () -> openPV.putWait(1), ValveState.OPEN);
	}

	@Override
	public void close() throws DeviceException {
		logger.debug("{} - Closing valve", name);
		move(Request.CLOSE, () -> controlPV.putWait(ValveControl.CLOSE), ValveState.CLOSED);
	}

	/**
	 * <h1>Move the valve to a new state</h1>
	 *
	 * The PV used to set the state of the valve does not block. There is also no feedback
	 * for a valve failing to move (eg if interlocks are not reset).
	 * <br>
	 * To ensure that any failures are reported, this creates a new Future representing the requested
	 * move, then requests the move.
	 * <br>
	 * The status is being monitored so when it changes the future can be completed and its get method
	 * will return. If the move times out, the move is assumed to have failed and an exception is raised.
	 *
	 * @param req The requested action type. Used by the status monitor to determine if a move is complete
	 * @param action The action required to trigger the move
	 * @param targetState The end state. Used to determine if a move is required and whether it was successful if so.
	 * @throws DeviceException If anything goes wrong during the move.
	 * <ul>
	 *  <li>If the valve is already moving</li>
	 *  <li>If the valve is in a fault state</li>
	 *  <li>If the move times out</li>
	 *  <li>If the valve is not in the requested state after the move</li>
	 *  <li>If there is an error in Epics</li>
	 *  </ul>
	 */
	private void move(Request req, Action action, ValveState targetState) throws DeviceException {
		ValveState current = getState();
		if (current == targetState) {
			logger.debug("{} - valve already {}", name, targetState);
			return;
		} else if (current == FAULT) {
			throw new DeviceException(name + " - Valve in fault state");
		}
		try {
			var move = new CompletableFuture<ValveState>();
			if (currentMove.compareAndSet(null, move)) {
				request = req;
				action.run();
				if (move.get(timeout, SECONDS) != targetState) {
					// reset move so that future moves don't fail
					currentMove.set(null);
					request = null;
					throw new DeviceException(name + " - state not " + targetState + " after move: " + move.get());
				}
			} else {
				throw new DeviceException(name + " - valve already moving");
			}
		} catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Could not set valve to " + targetState, e);
		}
	}

	@Override
	public void reset() throws DeviceException {
		try {
			logger.debug("{} - Resetting valve", name);
			if (currentMove.getAndSet(null) != null) {
				logger.warn("{} - Incomplete action discarded", name);
			}
			request = null;
			controlPV.putWait(ValveControl.RESET);
		} catch (IOException e) {
			throw new DeviceException("Could not reset valve", e);
		}
	}

	@Override
	public String toString() {
		String stateString;
		try {
			stateString = getState().name();
		} catch (DeviceException e) {
			stateString = "UNKNOWN";
		}
		return name + ": " + rootPv + " (" + stateString + ")";
	}

	public String getRootPv() {
		return rootPv;
	}

	public void setRootPv(String rootPv) {
		this.rootPv = rootPv;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTimeout() {
		return timeout;
	}

	/**
	 * Set the timeout to use when waiting for valve to move
	 * @param timeout time in seconds to wait before a move is assumed to have failed
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/** Message shown to users when running help(valveName) from Jython */
	public String __doc__() {
		return getName() + ": EpicsPressureValve\n"
				+ "Valve making up part of the PressureCell\n"
				+ getName() + ".open() - open the valve\n"
				+ getName() + ".close() - close the valve\n"
				+ getName() + ".reset() - reset the valve.\n"
				+ "    This may be required if the valve has previously failed to move";
	}
}
