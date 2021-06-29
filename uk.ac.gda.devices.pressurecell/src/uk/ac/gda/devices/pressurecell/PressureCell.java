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
import uk.ac.gda.devices.pressurecell.data.PressureCellDataController;

/**
 * Scannable interface to the pressure cell allowing it to be used in step scans.
 * <br>
 * Access to lower level controls are available via the controller instance
 * from the {@link #getController()} method.
 */
public class PressureCell extends ScannableBase {
	private static final Logger logger = LoggerFactory.getLogger(PressureCell.class);
	public static final String __doc__ = // NOSONAR
			"Pressure Cell scannable letting the pressure cell be used in step scans.\n"
			+ "Access to lower level controls are available via the controller attribute.";
	private PressureCellController controller;
	private PressureCellDataController dataController;
	private Future<?> move = CompletableFuture.completedFuture(null);

	public void setFilePath(String directory, String filename) throws DeviceException {
		dataController.setFilePath(directory, filename);
	}

	public void setAcquire(boolean acquiring) throws DeviceException {
		dataController.setAcquire(acquiring);
		dataController.setDataWriter(acquiring);
	}

	public String getLastFileName() throws DeviceException {
		return dataController.getLastFileName();
	}

	public void setTriggers(int before, int after) throws DeviceException {
		dataController.setTriggers(before, after);
	}

	/** Set pressure at the pump to the same as the intermediate chamber */
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

	public void setJumpPressures(double from, double to) throws DeviceException {
		controller.setJumpToPressure(to);
		controller.setJumpFromPressure(from);
		controller.setJump();
	}

	public void armJumpValve() throws DeviceException {
		double pCell = controller.getCellPressure();
		double pMid = controller.getIntermediatePressure();
		// valves should already be closed but just to make sure
		controller.getV5().close();
		controller.getV6().close();
		controller.getV3().close();
		if (pCell > pMid) {
			// jumping to a lower pressure
			controller.getV5().disarm();
			controller.getV6().arm();
		} else {
			// jumping to a higher pressure
			controller.getV5().arm();
			controller.getV6().disarm();
		}
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

	public PressureCellDataController getDataController() {
		return dataController;
	}

	public void setDataController(PressureCellDataController dataController) {
		this.dataController = dataController;
	}
}
