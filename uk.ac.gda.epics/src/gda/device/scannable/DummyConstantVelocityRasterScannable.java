/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.scannable;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.continuouscontroller.ConstantVelocityRasterMoveController;
import gda.device.scannable.scannablegroup.ScannableMotionWithScannableFieldsBase;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.scan.ConstantVelocityRasterScan;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Scannable suitable for use in {@link ConstantVelocityRasterScan}s for a dummy controller.
 * <p>
 * WFrom Jython two fields will be made, name.nameX and name.nameY which represent the x and y dimensions of stage
 * respectively. Us e.g.:
 * <p>
 * ConstantVelocityRasterScan([name.nameY, s, s, s, name.nameX, s, s, s, deta, time ...])
 */
public class DummyConstantVelocityRasterScannable extends ScannableMotionWithScannableFieldsBase implements
		ContinuouslyScannableViaController {

	private static final Logger logger = LoggerFactory.getLogger(DummyConstantVelocityRasterScannable.class);

	private Double[] lastRasterTarget = new Double[2];

	/**
	 * @param name
	 */
	public DummyConstantVelocityRasterScannable(String name) {
		setName(name);
		setInputNames(new String[] { name + 'X', name + 'Y' });
		setExtraNames(new String[] {});
		setOutputFormat(new String[] { "%.4f", "%.4f" });

		setContinuousMoveController(new DummyConstantVelocityRasterMoveController());
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws gda.device.DeviceException {
		Double[] xytarget = PositionConvertorFunctions.toDoubleArray(position);
		if (xytarget.length != 2) {
			throw new AssertionError("Target position must have 2 fields, not " + xytarget.length);
		}
		if (isOperatingContinously()) {
			// Record position for the subsequent getPosition() call
			if (xytarget[0] != null) {
				lastRasterTarget[0] = xytarget[0];
			}
			if (xytarget[1] != null) {
				lastRasterTarget[1] = xytarget[1];
			}
		} else {
			throw new IllegalStateException(getName() + " can only be used in a continuous scan");
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		if (isOperatingContinously()) {
			if (lastRasterTarget == null) {
				throw new NullPointerException("lastRasterTargetNotSet	");
			}
			return lastRasterTarget;
		} // else
		throw new IllegalStateException(getName() + " can only be used in a continuous scan");

	}

	@Override
	public boolean isBusy() throws DeviceException {
		// when operating continuously this needs to return false. When not operating continuously, then
		// we will have blocked in asynchMoveto already.
		return false;
	}

	@Override
	public void stop() throws DeviceException {
		try {
			getContinuousMoveController().stopAndReset();
		} catch (InterruptedException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		stop();
	}

	class DummyConstantVelocityRasterMoveController extends DeviceBase implements ConstantVelocityRasterMoveController {

		private boolean hasBeenStarted = false;

		private double xmin;

		private double xmax;

		private double xstep;

		private double ymin;

		private double ymax;

		private double ystep;

		private double periodS;

		public DummyConstantVelocityRasterMoveController() {
		}

		@Override
		public String getName() {
			return "dummy_controller";
		}

		@Override
		public void setTriggerPeriod(double seconds) throws DeviceException {
			periodS = seconds;
			log(".setTriggerPeriod(" + seconds + ")");
		}

		@Override
		public void setStart(double startExternal) throws DeviceException {
			xmin = startExternal;
		}

		@Override
		public double getStart() {
			return xmin;
		}

		@Override
		public void setEnd(double endExternal) throws DeviceException {
			xmax = endExternal;
		}

		@Override
		public double getEnd() {
			return xmax;
		}

		@Override
		public void setStep(double step) throws DeviceException {
			this.xstep = step;
		}

		@Override
		public double getStep() {
			return xstep;
		}
		@Override
		public void setOuterStart(double startExternal) throws DeviceException {
			ymin = startExternal;
		}

		@Override
		public void setOuterEnd(double endExternal) throws DeviceException {
			ymax = endExternal;
		}

		@Override
		public void setOuterStep(double step) throws DeviceException {
			ystep = step;
		}

		@Override
		public void prepareForMove() throws DeviceException, InterruptedException {
			String msg = MessageFormat.format(
					"preparing for raster scan with (xmin={0}, xmax={1}, xstep={2}, ymin={3}, ymax={4}, ystep={5}, periodS={6})",
					xmin, xmax, xstep, ymin, ymax, ystep, periodS);

			InterfaceProvider.getTerminalPrinter().print(msg);
		}

		@Override
		public void startMove() throws DeviceException {
			log(".startMove()");
			hasBeenStarted = true;
		}

		@Override
		public boolean isMoving() throws DeviceException {
			log(".isMoving() *Just returning false as we have no feedback*");
			if (Thread.interrupted())
				throw new DeviceException("Thread interrupted during isMoving()");
			if (hasBeenStarted) {
				try {
					Thread.sleep(1000);  // Bodge!
				} catch (InterruptedException e) {
					throw new DeviceException(e);
				}
			}
			return false;
		}

		@Override
		public void waitWhileMoving() throws DeviceException, InterruptedException {
			if (hasBeenStarted) {
				log(".waitWhileMoving() *Just sleeping 1s as we have no feedback*");
				Thread.sleep(1000);
			}
		}

		@Override
		public void stopAndReset() throws DeviceException, InterruptedException {
			log(".stopAndReset()");
			hasBeenStarted = false;
		}

		// //

		@Override
		public int getNumberTriggers() {
			throw new AssertionError("Assumed unused");
		}

		@Override
		public double getTotalTime() throws DeviceException {
			throw new AssertionError("Assumed unused");
		}

		private void log(String msg) {
			logger.info(getName() + msg);
		}

		@Override
		public void configure() throws FactoryException {
			// do nothing
		}

	}

}
