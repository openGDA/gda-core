/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.zebra;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionStreamIndexer;
import gda.device.scannable.PositionStreamIndexerProvider;
import gda.device.scannable.ScannableBase;
import gda.device.zebra.ZebraConstantVelocityMoveController.MaxCollectionTimeAndMinAccelerationTime;
import gda.device.zebra.controller.Zebra;
import gda.epics.ReadOnlyPV;

/**
 * The ZebraMonitorController is intended to be used to monitor a Zebra which is not being used to control a move.
 */
public class ZebraMonitorController extends ScannableBase implements ContinuousMoveController,
						PositionCallableProvider<Double>, PositionStreamIndexerProvider<Double>,
						ContinuouslyScannableViaController, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(ZebraMonitorController.class);

	private ZebraConstantVelocityMoveController zebraCVMoveController;
	private Zebra zebra;

	private int pcCaptureBitField=0;
	private List<ZebraCaptureInputStreamCollection> timeSeriesCollection;
	@SuppressWarnings("unchecked")
	private PositionStreamIndexer<Double> lastImageNumberStreamIndexer[] = new PositionStreamIndexer[
		ZebraConstantVelocityMoveController.PRIMARY_INDEXERS+ZebraConstantVelocityMoveController.SECONDARY_INDEXERS+1];

	public ZebraMonitorController() {
		super();
		setExtraNames(new String[]{"MonitorTime"});
		setInputNames(new String[]{});
		setOutputFormat(new String[]{"%5.5g"});
	}

	// interface ContinuousMoveController

	@Override
	public void prepareForMove() throws DeviceException, InterruptedException {
		logger.trace("prepareForMove() zebraCVMoveController.isOperatingContinously()={}", zebraCVMoveController.isOperatingContinously());
		try {
			/* Since we shouldn't attempt set up the next point until the current one is finished, wait for the
			 * Zebra to become disarmed.
			 */
			int waitingForDisarm = 1;
			while (zebra.isPCArmed()) {
				logger.info("Zebra not yet disarmed, waiting for disarm... ({})", waitingForDisarm);
				if (waitingForDisarm % 10 == 0) zebra.reset();
				Thread.sleep(100);
				waitingForDisarm++;
			}

			final int numberTriggers=getNumberTriggers();

			// Sources must be set first
			zebra.setPCArmSource(Zebra.PC_ARM_SOURCE_SOFT);
			zebra.setPCGateSource(Zebra.PC_GATE_SOURCE_EXTERNAL);
			zebra.setPCPulseSource(Zebra.PC_PULSE_SOURCE_TIME);
			zebra.setPCCaptureBitField(pcCaptureBitField);

			// For a position compare we need one gate with multiple pulses, for an externally
			// triggered gate we need a single pulse for each with multiple gates.
			zebra.setPCGateNumberOfGates(numberTriggers);
			zebra.setPCPulseMax(1);

			logger.debug("setPCArmSource(Zebra.PC_ARM_SOURCE_SOFT), setPCGateSource(Zebra.PC_GATE_SOURCE_EXTERNAL), setPCPulseSource(Zebra.PC_PULSE_SOURCE_TIME)");
			logger.debug("setPCCaptureBitField({}), setPCGateNumberOfGates({}), zebraToMonitor.setPCPulseMax(1)", pcCaptureBitField, numberTriggers);

			final double maxCollectionTimeFromDetectors = getMaxCollectionTimeFromDetectors();

			logger.trace("prepareForMove() maxCollectionTimeFromDetectors={}", maxCollectionTimeFromDetectors);

			final double timeUnitConversion = ZebraConstantVelocityMoveController.getTimeUnitConversionAndSetTimeUnit(maxCollectionTimeFromDetectors, zebra);

			double pcPulseStepRaw, pcPulseDelayRaw, pcPulseWidthRaw;
			pcPulseStepRaw=zebraCVMoveController.getTriggerPeriod()*timeUnitConversion;

			final double checkStep = Math.abs(maxCollectionTimeFromDetectors*timeUnitConversion - pcPulseStepRaw);
			if (checkStep > ZebraConstantVelocityMoveController.DEFAULT_PC_PULSE_GATE_TRIM) {
				logger.warn("Mismatch between Monitor collection time and move controller trigger period, "+
						"maxCollectionTimeFromDetectors={}, triggerPeriod={}, checkStep={}",
						maxCollectionTimeFromDetectors, zebraCVMoveController.getTriggerPeriod(), checkStep);
			}
			if (numberTriggers > 1) {
				// If we only have multiple triggers, we have to trim the delay and width times.
				pcPulseDelayRaw=pcPulseStepRaw-ZebraConstantVelocityMoveController.DEFAULT_PC_PULSE_GATE_TRIM;
				pcPulseWidthRaw=pcPulseStepRaw-ZebraConstantVelocityMoveController.DEFAULT_PC_PULSE_GATE_TRIM;
			} else {
				// If we only have one trigger, we can get the full readout by extending the step instead.
				pcPulseDelayRaw=pcPulseStepRaw;
				pcPulseWidthRaw=pcPulseStepRaw;
				pcPulseStepRaw=pcPulseStepRaw+ZebraConstantVelocityMoveController.DEFAULT_PC_PULSE_GATE_TRIM;
			}
			zebra.setPCPulseStep(pcPulseStepRaw);
			zebra.setPCPulseDelay(pcPulseDelayRaw);
			zebra.setPCPulseWidth(pcPulseWidthRaw);

			final double pcPulseStepRBVRaw= zebra.getPCPulseStepRBV();
			ZebraConstantVelocityMoveController.checkRBV(pcPulseStepRaw, pcPulseStepRBVRaw, 0.0001, "pcPulseStep");

			final double pcPulseWidthRBVRaw = zebra.getPCPulseWidthRBV();
			ZebraConstantVelocityMoveController.checkRBV(pcPulseWidthRaw, pcPulseWidthRBVRaw, 0.0001, "pcPulseWidth");

			final double pcPulseDelayRBVRaw = zebra.getPCPulseDelayRBV();
			ZebraConstantVelocityMoveController.checkRBV(pcPulseDelayRaw, pcPulseDelayRBVRaw, 0.0001, "pcPulseDelay");

			logger.info("pcPulseStepRaw={}, pcPulseStepRBVRaw={}, pcPulseDelayRaw={}, pcPulseDelayRBVRaw={}, pcPulseWidthRaw={}, pcPulseWidthRBVRaw={}, ",
							pcPulseStepRaw, pcPulseStepRBVRaw, pcPulseDelayRaw, pcPulseDelayRBVRaw, pcPulseWidthRaw, pcPulseWidthRBVRaw);

			zebra.pcArm();

			if( timeSeriesCollection != null){
				for(ZebraCaptureInputStreamCollection ts : timeSeriesCollection){
					ts.start(numberTriggers);
				}
			}
		} catch (Exception e) {
			logger.error("Error in prepareForMove() of {}", getName(), e);
			throw new DeviceException("Error arming the zebra: "+e.getMessage(), e);
		}
	}

	@Override
	public void startMove() throws DeviceException {
		logger.trace("startMove()");
		throw new IllegalStateException("Only the ZebraConstantVelocityMoveController " + zebraCVMoveController.getName() + " can start a move!");
	}

	@Override
	public boolean isMoving() throws DeviceException {
		logger.trace("isMoving()");
		return zebraCVMoveController.isMoving();
	}

	@Override
	public void waitWhileMoving() throws DeviceException, InterruptedException {
		logger.trace("waitWhileMoving()");
		zebraCVMoveController.waitWhileMoving();
	}

	@Override
	public void stopAndReset() throws DeviceException, InterruptedException {
		logger.trace("stopAndReset()");
		try {
			zebra.reset();
		} catch (IOException e) {
			throw new DeviceException("problem resetting EPICS zebra", e);
		}
	}

	// interface HardwareTriggerProvider

	@Override
	public void setTriggerPeriod(double seconds) throws DeviceException {
		logger.trace("setTriggerPeriod({})", seconds);
		throw new IllegalStateException("setTriggerPeriod"); //zebraCVMoveController.setTriggerPeriod(seconds);
	}

	@Override
	public int getNumberTriggers() {
		logger.trace("getNumberTriggers() zebraCVMoveController.isOperatingContinously()={}", zebraCVMoveController.isOperatingContinously());
		final int numberTriggers=zebraCVMoveController.getNumberTriggers();
		if (numberTriggers > 1) logger.warn("numberTriggers {} > 1, configuration not tested", numberTriggers);
		return numberTriggers;
	}

	@Override
	public double getTotalTime() throws DeviceException {
		logger.trace("getTotalTime() zebraCVMoveController.isOperatingContinously()={}", zebraCVMoveController.isOperatingContinously());
		return zebraCVMoveController.getTotalTime();
	}

	// interface PositionCallableProvider<T> {

	@Override
	public Callable<Double> getPositionCallable() throws DeviceException {
		logger.trace("getPositionCallable()");
		return getPositionSteamIndexer(10).getNamedPositionCallable(getExtraNames()[0], 1);
	}

	// interface PositionStreamIndexerProvider

	/** Get a PositionStreamIndexer for a Zebra position capture stream.
	 *
	 * Since each stream can only be used once, allow secondary clients to
	 * access copies of position streams by using an offset on the index.
	 *
	 * Indices 0 to 4 are ENC1-4, 6 to 9 are DIV1-4 and 10 is Capture time.
	 * Primary indices 0 to 10 are mapped onto secondary indices 11 to 21.
	 */
	@Override
	public PositionStreamIndexer<Double> getPositionSteamIndexer(int index) {
		logger.trace("getPositionSteamIndexer({})... pointBeingPrepared={}", index, zebraCVMoveController.getPointBeingPrepared());

		final int pcCaptureIndex = index <= ZebraConstantVelocityMoveController.PRIMARY_INDEXERS ?
				index : index - ZebraConstantVelocityMoveController.PRIMARY_INDEXERS - 1;

		if (pcCaptureIndex < 10) { // There is no bit for Time, since it is always enabled
			logger.debug("|= pcCaptureIndex={}, pcCaptureBitField={} ...", pcCaptureIndex, pcCaptureBitField);
			pcCaptureBitField |= 1 << pcCaptureIndex;
			logger.debug("...pcCaptureIndex={}, pcCaptureBitField={}", pcCaptureIndex, pcCaptureBitField);
		}
		if( lastImageNumberStreamIndexer[index] == null){
			logger.info("Creating lastImageNumberStreamIndexer[{}] using PCCapturePV({})", index, pcCaptureIndex);
			final ReadOnlyPV<Double[]> rdDblArrayPV = zebra.getPcCapturePV(pcCaptureIndex);

			if( timeSeriesCollection == null) {
				timeSeriesCollection = new Vector<ZebraCaptureInputStreamCollection>();
				logger.trace("getPositionSteamIndexer() New timeSeriesCollection");
			}

			ZebraCaptureInputStreamCollection sc = new ZebraCaptureInputStreamCollection(zebra.getNumberOfPointsDownloadedPV(), rdDblArrayPV);
			lastImageNumberStreamIndexer[index] = new PositionStreamIndexer<Double>(sc);
			timeSeriesCollection.add(sc);
			logger.trace("timeSeriesCollection now {}", timeSeriesCollection);
		}
		logger.trace("...getPositionSteamIndexer({}) returning {}", index, lastImageNumberStreamIndexer[index]);
		return lastImageNumberStreamIndexer[index];
	}
	// interface ContinuouslyScannableViaController extends Scannable {

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
		logger.trace("setOperatingContinuously({})", b);
		if (b != zebraCVMoveController.isOperatingContinously()) {
			logger.warn("Request to setOperatingContinuously({}) but {}.isOperatingContinously()={}.",
				b, zebraCVMoveController.getName(), zebraCVMoveController.isOperatingContinously());
		}
	}

	@Override
	public boolean isOperatingContinously() {
		logger.trace("isOperatingContinously()");
		return zebraCVMoveController.isOperatingContinously();
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		logger.trace("getContinuousMoveController()");
		return zebraCVMoveController.getContinuousMoveController();
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController controller) {
		throw new IllegalArgumentException("setContinuousMoveController("+controller.getName()+") not supported on "+this.getName());
	}

	// interface Scannable

	@Override
	public void atScanStart() throws DeviceException {
		logger.trace("atScanStart()...");
		try {
			/* Ensure that the zebra is reset and thus disarmed before the first point in a scan so we don't have to
			 * call reset in every prepareForMove call.
			 */
			zebra.reset();
		} catch (IOException e) {
			throw new DeviceException(e.getMessage(), e);
		}
		super.atScanStart();
		logger.trace("...atScanStart()");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void atScanLineStart() throws DeviceException {
		logger.trace("atScanLineStart()");
		lastImageNumberStreamIndexer = new PositionStreamIndexer[
			ZebraConstantVelocityMoveController.PRIMARY_INDEXERS+ZebraConstantVelocityMoveController.SECONDARY_INDEXERS+1];
		timeSeriesCollection = null;
		logger.trace("atScanLineStart() Reset timeSeriesCollection");
		pcCaptureBitField=0;
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		timeSeriesCollection = null;
		logger.trace("atScanLineEnd() Reset timeSeriesCollection");
	}

	@Override
	public void stop() throws DeviceException {
		logger.trace("stop() pointBeingPrepared={}", zebraCVMoveController.getPointBeingPrepared());
		//ensure the callables have all been called
		boolean done=true;
		if( timeSeriesCollection != null){
			for( ZebraCaptureInputStreamCollection ts : timeSeriesCollection){
				done &= ts.isComplete();
			}
		}
		if(!done)
			throw new DeviceException("stop called before all callables have been processed");
		timeSeriesCollection = null;
		logger.trace("stop() Reset timeSeriesCollection");
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		logger.trace("atCommandFailure() pointBeingPrepared={}", zebraCVMoveController.getPointBeingPrepared());
		super.atCommandFailure();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return zebraCVMoveController.isBusy();
	}

	// interface InitializingBean

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.trace("afterPropertiesSet()");
		if (zebraCVMoveController == null)
			throw new Exception("zebraCVMoveController is not set");
		if (zebra == null)
			throw new Exception("zebraToMonitor is not set");
	}

	// Helper methods

	private double getMaxCollectionTimeFromDetectors() throws DeviceException {
		logger.trace("getNumberTriggers() zebraCVMoveController.isOperatingContinously()={}", zebraCVMoveController.isOperatingContinously());
		if (zebraCVMoveController.isOperatingContinously()) {
			final MaxCollectionTimeAndMinAccelerationTime maxCollectionTimeAndMinAccelerationTime =
					ZebraConstantVelocityMoveController.getMaxCollectionTimeAndMinAccelerationTimeFromDetectors(
							zebraCVMoveController.getDetectors(), zebraCVMoveController.getPointBeingPrepared());
			return maxCollectionTimeAndMinAccelerationTime.maxCollectionTimeFromDetectors;
		} else {
			logger.info("{} is not operating continuously, assuming 1", zebraCVMoveController.getName());
			return 1;
		}

	}

	// Class methods

	public ZebraConstantVelocityMoveController getZebraCVMoveController() {
		return zebraCVMoveController;
	}

	public void setZebraCVMoveController(ZebraConstantVelocityMoveController zebraCVMoveController) {
		this.zebraCVMoveController = zebraCVMoveController;
	}

	public Zebra getZebra() {
		return zebra;
	}

	public void setZebra(Zebra zebraToMonitor) {
		this.zebra = zebraToMonitor;
	}
}
