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

package uk.ac.gda.devices.pressurecell;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.devices.pressurecell.controller.PressureCellController;

public class PressureCell extends ScannableBase {
	private static final Logger logger = LoggerFactory.getLogger(PressureCell.class);
	private PressureCellController controller;
	private Future<?> move = CompletableFuture.completedFuture(null);

	/** Set pressure at the pump to the same as the intemediate chamber */
	private void matchIntermediatePressure() throws DeviceException, InterruptedException {
		logger.debug("{} - Matching intermediate pressure", getName());
		if (!controller.getV3().isOpen()) {
			controller.setTargetPressure(controller.getIntermediatePressure());
			controller.go();
		}
	}

	/**
	 * Set the pressure at the pump to be the same as that at the cell.
	 * As Valves cannot be opened if the difference in pressure on either side
	 * is too great, this requires the pressure to initially be matched to the
	 * intermediate chamber before the cell pressure can be reached.
	 */
	private void matchCellPressure() throws DeviceException, InterruptedException {
		logger.debug("{} - Matching cell pressure", getName());
		matchIntermediatePressure();
		controller.getV3().open();
		if (!controller.getV5().isOpen() && !controller.getV6().isOpen()) {
			controller.setTargetPressure(controller.getCellPressure());
			controller.go();
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return controller.getCellPressure();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		Double target = ScannableUtils.objectToDouble(position);
		move = Async.submit(() -> {
			matchCellPressure();
			controller.getV5().open();
			controller.setTargetPressure(target);
			controller.go();
			return null;
		});
	}

	public PressureCellController getController() {
		return controller;
	}

	public void setController(PressureCellController controller) {
		this.controller = controller;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return !move.isDone() || controller.isBusy();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		waitWhileBusy(-1);
	}

	/**
	 * {@inheritDoc}
	 *
	 * If timeout < 0, this will block indefinitely and is equivalent to {@link #waitWhileBusy()}
	 */
	@Override
	public void waitWhileBusy(double timeoutInSeconds) throws DeviceException, InterruptedException {
		long millis = (long)(1000 * timeoutInSeconds);
		try {
			if (timeoutInSeconds < 0) {
				move.get();
			} else {
				move.get(millis, MILLISECONDS);
			}
		} catch (TimeoutException | ExecutionException e) {
			throw new DeviceException(getName() + " - Exception waiting for move to complete", e);
		}
		// This could potentially wait for double the timeout but it is unlikely:
		// * This should return immediately if the move is complete.
		// * If the move timed out, it would have thrown.
		controller.waitForIdle(millis, MILLISECONDS);
	}
}
