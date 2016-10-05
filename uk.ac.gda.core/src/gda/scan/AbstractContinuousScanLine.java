/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.scan;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetector;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.jython.InterfaceProvider;

/**
 *
 */
public abstract class AbstractContinuousScanLine extends ConcurrentScan {


	private static final Logger logger = LoggerFactory.getLogger(AbstractContinuousScanLine.class);

	// TODO: handle requirement for repeating a line

	private ContinuousMoveController controller;

	protected Vector<HardwareTriggeredDetector> detectors = new Vector<HardwareTriggeredDetector>();

	protected Vector<ContinuouslyScannableViaController> scannablesToMove = new Vector<ContinuouslyScannableViaController>();

	protected boolean detectorsIntegrateBetweenTriggers;

	public AbstractContinuousScanLine(Object[] args) throws IllegalArgumentException {
		super(args);
		callCollectDataOnDetectors = false;
		extractScannablesToScan();
		extractDetectors();
		if (detectors.size() == 0) {
			throw new IllegalArgumentException("At least one (HardwareTriggeredDetector) detector must be specified (to provide a trigger period or profile).");
		}
		extractContinuousMoveController(scannablesToMove);
		checkDetectorsAllUseTheScanController();
		determineIfDetectorsIntegrateBetweenTriggers();
		// TODO: if anything is a PositionCallableProvider then check the pipeline length is unbounded.
	}

	@Override
	public boolean isReadoutConcurrent() {
		return false;  // should be false even if enabled for beamline
	}

	protected void extractScannablesToScan() {
		for (Scannable scn : allScannables) {
			if ((scn.getInputNames().length + scn.getExtraNames().length) != 0 ) {
				// ignore zero input-output name devices
				if (!(scn instanceof ContinuouslyScannableViaController)) {
					throw new IllegalArgumentException("Scannable " + scn.getName()
							+ " is not ContinuouslyScannableViaController so cannot be used in an AbstractContinuousScanLine");
				}
				scannablesToMove.add((ContinuouslyScannableViaController) scn);
			}
		}
	}

	private void extractDetectors() {
		for (Detector det : allDetectors) {
			if (!(det instanceof HardwareTriggeredDetector)) {
				throw new IllegalArgumentException("Detector " + det.getName()
						+ " is not a HardwareTriggeredDetector so cannot be used in an AbstractContinuousScanLine");
			}
			detectors.add((HardwareTriggeredDetector) det);
		}
	}

	private void extractContinuousMoveController(Vector<ContinuouslyScannableViaController> scannables) {
		// If we have a move controller as a scannable, use it.
		for (ContinuouslyScannableViaController scn : scannables) {
			if (scn instanceof ContinuousMoveController) {
				ContinuousMoveController moveController = (ContinuousMoveController)scn;
				if (getController() == null) {
					logger.warn("Using {} as the ContinuousMoveController.", moveController);
					setController(moveController);
				} else if (getController() == moveController) {
					logger.warn("ContinuousMoveController {} added to scan multiple times.", moveController);
				} else {
					logger.warn("Both {} and {} ContinuousMoveController added to scan!", getController(), moveController);
				}
			}
		}
		for (ContinuouslyScannableViaController scn : scannables) {
			ContinuousMoveController scnsController;
			try {
				scnsController = scn.getContinuousMoveController();
			} catch (ClassCastException e) {
				throw new IllegalArgumentException(scn.getName()
						+ " has a continuous move controller that does not support continuous scanning");
			}
			if (scnsController == null) {
				throw new IllegalArgumentException(scn.getName() + " has no continuous move controller configured.");
			}
			if (getController() == null) {
				setController(scnsController);
			} else {
				if (getController() != scnsController) {
					try {
						scn.setContinuousMoveController(getController());
					} catch (Exception e) {
						throw new IllegalArgumentException(scn.getName()
								+ " has a scan controller which is incompatible with another scannable to be scanned over", e);
					}
				}
			}
		}
	}

	private void checkDetectorsAllUseTheScanController() {
		for (HardwareTriggeredDetector det : detectors) {
			HardwareTriggerProvider triggerProvider = det.getHardwareTriggerProvider();
			if (triggerProvider == null) {
				throw new IllegalArgumentException("Detector " + det.getName()
						+ " has no HardwareTriggerProvider configured.");
			}
			if (triggerProvider != getController()) {
				throw new IllegalArgumentException(MessageFormat.format(
						"Detector {0} is configured with a different continous move controller ({1}) than that of the specified Scannable ({2}).",
						det.getName(), triggerProvider.getName(), getController().getName()));
			}
		}
	}

	private void determineIfDetectorsIntegrateBetweenTriggers() {
		Iterator<HardwareTriggeredDetector> detectorIterator = detectors.iterator();
		detectorsIntegrateBetweenTriggers = detectorIterator.next().integratesBetweenPoints();
		while (detectorIterator.hasNext()) {
			if (detectorIterator.next().integratesBetweenPoints() != detectorsIntegrateBetweenTriggers) {
				throw new IllegalArgumentException(
						"Detectors are inconsistently configured: some are set to integrateBetweenPoints(), but not all");
			}
		}
	}

	@Override
	protected void prepareDevicesForCollection() throws Exception {
		try {
			// 1. Prepare the Scannables and Detectors to be continuously operated
			for (ContinuouslyScannableViaController scn : scannablesToMove) {
				scn.setOperatingContinuously(true);
			}
			logger.info(MessageFormat.format(
					"Requests to move Scannables ({0}) will be collected by the TrajectoryMoveController ({1})",
					scannablesToString(scannablesToMove), getController().getName()));

			setHardwareTriggeringOnAllHardwareTriggerableDetectors(true);
			logger.info(MessageFormat
					.format("Requests to collect data on Detectors ({0}) will be collected by the TrajectoryMoveController ({1})",
							scannablesToString(detectors), getController().getName()));

			for (HardwareTriggeredDetector det : detectors) {
				det.setNumberImagesToCollect(getNumberPoints());
			}

			super.prepareDevicesForCollection();

		} catch (Exception e) {
			logger.info("problem in prepareDevicesForCollection()");
			for (ContinuouslyScannableViaController scn : scannablesToMove) {
				scn.setOperatingContinuously(false);
			}
			setHardwareTriggeringOnAllHardwareTriggerableDetectors(false);

			throw e;
		}
	}

	private void setHardwareTriggeringOnAllHardwareTriggerableDetectors(boolean enable) throws DeviceException {
		for (HardwareTriggeredDetector det : detectors) {
			if (det instanceof HardwareTriggerableDetector) {
				((HardwareTriggerableDetector) det).setHardwareTriggering(enable);
			}
		}
	}

	@Override
	public void doCollection() throws Exception {

		logger.info("Starting AbstractContinuousScanLine for scan: '" + getName() + "' (" + getCommand() + ")" );

		getController().stopAndReset(); // should be ready anyway

		try {

			// TODO: Check the controller is not moving

			// 2. Perform the 'scan'. Scannables must direct their asynchronousMoveTo methods to the controller,
			// and detectors should ingorec alls made to collectData. Detectors will have their collectionTimes set.
			// ScanDataPoints will be created although will likely be incomplete awaiting results from
			// Scannables/Detectors that implement PositionCallableProvider.

			super.doCollection();

			configureControllerPositions(detectorsIntegrateBetweenTriggers);

			configureControllerTriggerTimes();

			// 4a. Prepare the controller and move to the start position
			// (some detectors timeout waiting for a first trigger once armed)
			getController().prepareForMove();

			// 4b. Prepare hardware in parallel and wait for it all to be ready
			armDetectors();

			// 5. Start the move which will result in hardware triggers to the already armed Detectors.
			getController().startMove();

			// 6. Wait for completion (Scannables obtain their status from the controller)
			while(getController().isMoving()){
				getScanDataPointPipeline().checkForException();
				Thread.sleep(50);
			}
			getController().waitWhileMoving();
			for (HardwareTriggeredDetector det : detectors) {
				det.waitWhileBusy();
			}
		} catch (Exception e) {
			String msg = "Problem in doCollection() '" + e.getMessage() + "' so calling " + getController().getName() + " stopAndReset";
			logger.error(msg,e);
			InterfaceProvider.getTerminalPrinter().print(msg);
			getController().stopAndReset();
			logger.info(getController().getName() + "stopAndReset complete");
			throw e;
		} finally {
			for (ContinuouslyScannableViaController scn : scannablesToMove) {
				scn.setOperatingContinuously(false);
			}
			setHardwareTriggeringOnAllHardwareTriggerableDetectors(false);

		}
	}

	abstract protected void configureControllerPositions(boolean detectorsIntegrateBetweenTriggers) throws DeviceException, InterruptedException, Exception;

	final protected double extractCommonCollectionTimeFromDetectors() throws DeviceException {
		logger.trace("extractCommonCollectionTimeFromDetectors()");
		HardwareTriggeredDetector firstDetector = detectors.get(0);
		double period = firstDetector.getCollectionTime();
		if(firstDetector instanceof DetectorWithReadoutTime){
			period += ((DetectorWithReadoutTime)firstDetector).getReadOutTime();
		}
		for (HardwareTriggeredDetector det : detectors.subList(1, detectors.size())) {
			double detsPeriod = det.getCollectionTime();
			if(det instanceof DetectorWithReadoutTime){
				detsPeriod += ((DetectorWithReadoutTime)det).getReadOutTime();
			}
			if ((Math.abs(detsPeriod - period) / period) > .1 / 100) {
				throw new DeviceException(
						MessageFormat
								.format("Requested trigger time ({0}) is more than .1% different from the time already requested for this point ({1}).",
										detsPeriod, period));
			}
			period = (period + detsPeriod) / 2.; // average away differences less than .1% to be pedantic
		}
		logger.trace("extractCommonCollectionTimeFromDetectors() returning period={}", period);
		return period;
	}

	protected abstract void configureControllerTriggerTimes() throws DeviceException ;

	private String scannablesToString(Vector<? extends Scannable> scannables) {
		Vector<String> names = new Vector<String>();
		for (Scannable scn : scannables) {
			names.add(scn.getName());
		}
		return Arrays.toString(names.toArray());

	}

	//@SuppressWarnings("null")
	private void armDetectors() throws DeviceException, InterruptedException {

		class ArmDetector implements Callable<Void> {
			public final HardwareTriggeredDetector det;

			public ArmDetector(HardwareTriggeredDetector det) {
				this.det = det;
			}

			@Override
			public Void call() throws Exception {
				try {
					det.collectData();
				} catch (Exception e) {
					logger.error("Detector {} threw an exception in collectData()", det.getName(), e);
					throw new Exception("Problem arming " + det.getName(), e);
				}
				return null;
			}
		}

		// Arm each detector in a new thread
		LinkedList<FutureTask<Void>> futureTasks = new LinkedList<FutureTask<Void>>();
		for (HardwareTriggeredDetector det : detectors) {
			futureTasks.add(new FutureTask<Void>(new ArmDetector(det)));
			(new Thread(futureTasks.getLast(), "AbstractContinuousScanLine.ArmDetector-" + det.getName())).start();
		}

		// Wait for each detector to arm (cancelling other arm-tasks and stopping all detectors on a failure.
		try {
			while(!futureTasks.isEmpty()) {
				FutureTask<Void> task = futureTasks.pop();
				task.get();
			}
		} catch (ExecutionException e) {
			logger.error(e.getClass() + " while arming detectors:", e.getCause());
			cancelRemainingTasks(futureTasks);
			stopDetectors();
			throw new DeviceException("Problem arming detectors: "+ e.getMessage(), e.getCause());
		} catch (InterruptedException e) {
			logger.error("Interrupted while arming detectors", e);
			cancelRemainingTasks(futureTasks);
			stopDetectors();
			throw e;
		}
	}

	private void cancelRemainingTasks(List<FutureTask<Void>> futures) {
		logger.info("cancelling remaining detector preparation tasks");
		for (Future<Void> future : futures) {
			future.cancel(true);
		}
	}

	protected void stopDetectors() throws DeviceException {
		logger.info("stopping detector(s)");
		for (HardwareTriggeredDetector det : detectors) {
			det.stop();
		}
	}

	protected ContinuousMoveController getController() {
		return controller;
	}

	private void setController(ContinuousMoveController controller) {
		this.controller = controller;
	}
}
