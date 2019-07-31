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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.IScannableMotor;
import gda.device.continuouscontroller.ConstantVelocityMoveController2;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.detector.NXDetector;
import gda.device.detector.addetector.triggering.UnsynchronisedExternalShutterNXCollectionStrategy;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.enumpositioner.ValvePosition;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionStreamIndexer;
import gda.device.zebra.controller.Zebra;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gda.observable.IObserver;

public class ZebraConstantVelocityMoveControllerForQexafs extends FindableConfigurableBase implements ConstantVelocityMoveController2,
		PositionCallableProvider<Double>, ContinuouslyScannableViaController {

	private ZebraConstantVelocityMoveController zebraController;
	private double minimumBraggAccelerationDistance;
	private double minimumScannableMotorAccelerationDistance;
	private double pcGateStartScannableMotor;

	//For now hardcoded value as VMAX PV not available in ScannableMotor object
	private static final double MAX_BRAGG_SPEED = 0.25; // keV/s

	private EnumPositioner sampleShutter;

	private static final Logger logger = LoggerFactory.getLogger(ZebraConstantVelocityMoveControllerForQexafs.class);

	public ZebraConstantVelocityMoveController getZebraConstantVelocityMoveController() {
		return zebraController;
	}

	public void setZebraConstantVelocityMoveController(ZebraConstantVelocityMoveController zebraController) {
		this.zebraController = zebraController;
	}

	public double getMinimumBraggAccelerationDistance() {
		return minimumBraggAccelerationDistance;
	}

	public void setMinimumBraggAccelerationDistance(double minimumBraggAccelerationDistance) {
		this.minimumBraggAccelerationDistance = minimumBraggAccelerationDistance;
	}

	public double getMinimumScannableMotorAccelerationDistance() {
		return minimumScannableMotorAccelerationDistance;
	}

	public void setMinimumScannableMotorAccelerationDistance(double minimumScannableMotorAccelerationDistance) {
		this.minimumScannableMotorAccelerationDistance = minimumScannableMotorAccelerationDistance;
	}

	@Override
	public void setStart(double start) throws DeviceException {
		zebraController.setStart(start);
	}

	@Override
	public double getStart() {
		return zebraController.getStart();
	}

	@Override
	public void setEnd(double end) throws DeviceException {
		zebraController.setEnd(end);

	}

	@Override
	public double getEnd(){
		return zebraController.getEnd();
	}


	@Override
	public void setStep(double step) throws DeviceException {
		zebraController.setStep(step);
	}

	@Override
	public double getStep() {
		return zebraController.getStep();
	}

	public Zebra getZebra(){
		return zebraController.getZebra();
	}

	public ZebraMotorInfoProvider getZebraMotorInfoProvider(){
		return zebraController.getZebraMotorInfoProvider();
	}

	public IScannableMotor getScannableMotor() {
		return zebraController.getScannableMotor();
	}

	@Override
	public void prepareForMove() throws DeviceException, InterruptedException {
		try {

			logger.info("prepare for move");
			final Zebra zebra = getZebra();
			IScannableMotor scannableMotorToMove = getScannableMotor();
			zebra.reset(); // Doing a reset does appear to disarm the zebra before we check it, so we don't need to explicitly
			// disarm. We probably don't need to check either, but to verify that we will leave the check and log message in.

			/*Note that a disarm and waiting for the zebra to no longer be disarmed is not enough. The zebra box will
			* stay armed internally and since a recent zebra support module update, will error when position compare
			* parameters are set.
			*
			* Even if the zebra is saying it is disarmed and you wait 10000ms, you still get this problem.
			*/
			//if we want to check it is disarmed we will need to wait >2s as that is the zebra bus update period
			while (zebra.isPCArmed()) {
				logger.info("Zebra already armed, waiting for disarm...");
				Thread.sleep(10000); // 1000ms did not prevent the problem with pcDisarm(), 10000ms is enough with reset() though!
			}

			//sources must be set first
			zebra.setPCArmSource(Zebra.PC_ARM_SOURCE_SOFT);
			zebra.setPCPulseSource(zebraController.getMode());

			//set motor before setting gates and pulse parameters
			int pcEnc = getZebraMotorInfoProvider().getPcEnc();
			short pcCaptureBitField = ZebraConstantVelocityMoveController.getPcCaptureBitField(pcEnc);
			zebra.setPCCaptureBitField(pcCaptureBitField);
			zebra.setPCEnc(getZebraMotorInfoProvider().getPcEnc()); // Default is Zebra.PC_ENC_ENC1

			final double startBragg = ((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).convertEnergyToBraggAngle(zebraController.getStart());
			final double endBragg = ((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).convertEnergyToBraggAngle(zebraController.getEnd());
			double numberOfPoints = (zebraController.getEnd()-zebraController.getStart())/zebraController.getStep()+1;
			double stepBragg = Math.abs((startBragg - endBragg)/numberOfPoints);

			logger.info("stepBragg: {}", stepBragg);
			//change here the logic as the bragg angle is negative if step >0 negative and if step<0 positive direction
			zebra.setPCDir(getStep() <0 ? Zebra.PC_DIR_POSITIVE : Zebra.PC_DIR_NEGATIVE);
			zebra.setPCGateNumberOfGates(1);

			double pcGateWidth;
			double pcGateStart;
			double gateWidthTime=0.;
			// multiply by 1000 to convert mDeg into Deg

			double requiredScannableMotorSpeed = Math.abs(zebraController.getStart()/1000 - zebraController.getEnd()/1000)/getTotalTime();
			zebraController.setRequiredSpeed(requiredScannableMotorSpeed);

			double accelerationDistanceScannableMotor = getAccelerationDistance(requiredScannableMotorSpeed, scannableMotorToMove, minimumScannableMotorAccelerationDistance);
			scannableMotorToMove.setSpeed(MAX_BRAGG_SPEED);

			switch(zebraController.getMode()){
			case Zebra.PC_PULSE_SOURCE_POSITION:
				zebra.setPCGateSource(0);
				zebra.setPCPulseStart(0.0);

				double width = Math.abs(endBragg - startBragg);
				pcGateStart = startBragg;
				double requiredBraggSpeed = width/getTotalTime();
				double accelerationDistanceBragg = getAccelerationDistance(requiredBraggSpeed, ((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).getBraggScannableMotor(), minimumBraggAccelerationDistance);
				pcGateWidth = width + accelerationDistanceBragg;

				double pulseWidth = 0.001;
				zebra.setPCPulseWidth(pulseWidth);
				if (pulseWidth > stepBragg){
					throw new DeviceException(
						"Inconsistent Zebra parameters: the pulse width is greater than the required pulse step, so Zebra will not emit any pulses! You need to change you scan parameters or ask beamline staff.");
				}
				zebra.setPCPulseStep(stepBragg);

				scannableMotorToMove.asynchronousMoveTo(getStart() - (getStep()>0 ? 1.0 : -1.0)*accelerationDistanceScannableMotor);
				break;
			case Zebra.PC_PULSE_SOURCE_TIME:
				double maxCollectionTimeFromDetectors = 0.;
				double minCollectionTimeFromDetectors = Double.MAX_VALUE;
				double minimumAccelerationTime = Double.MAX_VALUE;

				for( Detector det : zebraController.getDetectors()){
					double collectionTime = det.getCollectionTime();
					maxCollectionTimeFromDetectors = Math.max(maxCollectionTimeFromDetectors, collectionTime);
					minCollectionTimeFromDetectors = Math.min(minCollectionTimeFromDetectors, collectionTime);

					if (det instanceof NXDetector) {
						NXDetector nxdet = (NXDetector) det;
						NXCollectionStrategyPlugin nxcs = nxdet.getCollectionStrategy();
						if (nxcs instanceof UnsynchronisedExternalShutterNXCollectionStrategy) {
							UnsynchronisedExternalShutterNXCollectionStrategy ues = (UnsynchronisedExternalShutterNXCollectionStrategy) nxcs;
							double newMinimumAccelerationTime = ues.getCollectionExtensionTimeS();
							minimumAccelerationTime = Math.min(minimumAccelerationTime, newMinimumAccelerationTime);
							logger.debug("minimum acceleration time for detector '{}' set to {}", det.getName(), minimumAccelerationTime);
						} else {
							logger.warn("Unsupported collection strategy type: {}", nxcs.getClass().getName());
						}
					} else {
						logger.warn("Unsupported detector type: {}", det.getClass().getName());
					}
				}
				if( Math.abs(minCollectionTimeFromDetectors-maxCollectionTimeFromDetectors) > 1e-8){
					 /* To support 2 collection times we need to use the pulse block to offset the triggers from the
					 * main PC pulse used by the det with the longest collection time. The offset is half the difference in the collection
					 * times each from the start
					 */
					throw new IllegalArgumentException("ZebraConstantVelocityMoveController cannot handle 2 collection times");
				}
				double timeUnitConversion;

				// Maximise the resolution of the timing by selecting the fastest timebase for the maximum collection time.
				if (maxCollectionTimeFromDetectors > 200000) {
					throw new IllegalArgumentException("ZebraConstantVelocityMoveController cannot handle collection times over 200000 seconds");
				}
				// Note that max gate width is 214881.9984, so max collection time is 214s at ms resolution, or 59d at s resolution.
				if (maxCollectionTimeFromDetectors > 214){  // Using 20 rather 214 makes it faster to test the switchover.
					zebra.setPCTimeUnit(Zebra.PC_TIMEUNIT_SEC);
					timeUnitConversion = 1;
				} else {
					zebra.setPCTimeUnit(Zebra.PC_TIMEUNIT_MS);
					timeUnitConversion = 1000;
				}
				/***
				 * There are 2 modes of operation:
				 * a)The exposure time of the detector and the distance between pulses are given.
				 * The speed of the motor is simply distance/exposureTime
				 *
				 * b) The exposure time of the detector, the distance between pulses are given AND so is the size of the step of the motor to move during exposure.
				 * The total time between pulses is the exposureTime + detector readout Time.
				 * The distance travelled between pulses is the step size + the speed of the motor * (readout time + inactiveTime)
				 * The distance travelled must equal the specific distance between pulses so
				 * the inactiveTime = ((distance between pulses - exposure step size) / speed of motor) - readout time
				 * inactiveTime must be >= 0.
				 *
				 * Note the first pulse is sent 1/2 exposure step before the start position. The position capture is delay to half way through the exposure = collectionTime/2
				 **/
				boolean exposureStepDefined = getZebraMotorInfoProvider().isExposureStepDefined();
				double exposureStep;
				double pcPulseStepRaw;
				if( exposureStepDefined){
					// case B - The exposure time of the detector, the distance between pulses are given AND so is the size of the step of the motor to move during exposure.
					exposureStep = getZebraMotorInfoProvider().getExposureStep();
					requiredBraggSpeed = getZebraMotorInfoProvider().getExposureStep()/maxCollectionTimeFromDetectors;
					double triggerPeriodFromSpeed = stepBragg/requiredBraggSpeed;
					if( triggerPeriodFromSpeed < zebraController.getTriggerPeriod())
						throw new IllegalArgumentException("ZebraConstantVelocityMoveController exposureStep, step and collectionTime do not give enough readout time for detectors. Increase collectionTime or reduce exposureStep");
					pcPulseStepRaw = triggerPeriodFromSpeed*timeUnitConversion;
				}
				else {
					// case A - The exposure time of the detector and the distance between pulses are given.
					requiredBraggSpeed = (Math.abs(stepBragg)/zebraController.getTriggerPeriod());
					requiredScannableMotorSpeed = (Math.abs(zebraController.getStep())/zebraController.getTriggerPeriod()) / timeUnitConversion;
					exposureStep = maxCollectionTimeFromDetectors*requiredBraggSpeed;
					pcPulseStepRaw = zebraController.getTriggerPeriod()*timeUnitConversion;
				}
				accelerationDistanceBragg = getAccelerationDistance(requiredBraggSpeed, ((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).getBraggScannableMotor(), minimumBraggAccelerationDistance);

				zebra.setPCPulseStep(pcPulseStepRaw);
				Thread.sleep(1);
				// Note that we need to read back values relating to time, so that the we calculate dependent values based on the
				// actual values in use rather than the values we asked for.
				final double pcPulseStepRBVRaw= zebra.getPCPulseStepRBV();
				ZebraConstantVelocityMoveController.checkRBV(pcPulseStepRaw, pcPulseStepRBVRaw, 0.0001, "pcPulseStep");
				double pcPulseStepRBV = pcPulseStepRBVRaw/timeUnitConversion;

				logger.trace("pcPulseStepRaw: {}; exposureStep: {}; requiredSpeed: {}",
						pcPulseStepRaw, exposureStep, requiredBraggSpeed);
				logger.trace("pcPulseStepRBVRaw: {}; pcPulseStepRBV: {}", pcPulseStepRBVRaw, pcPulseStepRBV);

				pcGateStart = startBragg - (stepBragg>0 ? 1.0 : -1.0)*exposureStep/2;
				pcGateStartScannableMotor = getStart() - (getStep()>0 ? 1.0 : -1.0)*exposureStep/2;
				scannableMotorToMove.asynchronousMoveTo(pcGateStartScannableMotor/1000 - (getStep()/1000>0 ? 1.0 : -1.0)*accelerationDistanceScannableMotor);

				zebraController.setRequiredSpeed(requiredScannableMotorSpeed);

				logger.trace("firstPulsePos for energy: {}); accelerationDistance for energy: {}", pcGateStartScannableMotor, accelerationDistanceScannableMotor);
				logger.trace("firstPulsePos for bragg: {}); accelerationDistance for bragg: {}", pcGateStart, accelerationDistanceBragg);

				// Capture positions half way through collection time
				double pcPulseDelayRaw=timeUnitConversion*maxCollectionTimeFromDetectors/2.;
				zebra.setPCPulseDelay(pcPulseDelayRaw);

				double pcPulseWidthRaw;
				if ( !zebraController.isPcPulseGateNotTrigger() ) {
					pcPulseWidthRaw = Math.max(0.01*timeUnitConversion, 0.0001);
				} else {
					pcPulseWidthRaw=maxCollectionTimeFromDetectors*timeUnitConversion-0.0002;
				}

				zebra.setPCPulseWidth(pcPulseWidthRaw);

				Thread.sleep(1); // Remove when the bug in zebra RBV handling is fixed

				double pcPulseWidthRBVRaw = zebra.getPCPulseWidthRBV();
				ZebraConstantVelocityMoveController.checkRBV(pcPulseWidthRaw, pcPulseWidthRBVRaw, 0.0001, "pcPulseWidth");
				double pcPulseWidthRBV = pcPulseWidthRBVRaw/timeUnitConversion;

				double pcPulseDelayRBVRaw = zebra.getPCPulseDelayRBV();
				ZebraConstantVelocityMoveController.checkRBV(pcPulseDelayRaw, pcPulseDelayRBVRaw, 0.0001, "pcPulseDelay");
				double pcPulseDelayRBV = pcPulseDelayRBVRaw/timeUnitConversion;
				//zebra EPICs updated so needed to change the number of triggers as an extra pulse is added due to TFG.
				gateWidthTime = pcPulseDelayRBV +  pcPulseStepRBV*getNumberTriggers() + pcPulseWidthRBV;

				//zebra EPICs updated so needed to change the number of triggers as an extra pulse is added due to TFG.
				if ( zebraController.isPcPulseGateNotTrigger() ) gateWidthTime = pcPulseStepRBV*(getNumberTriggers()+1);

				// Why do we recalculate requiredSpeed here? We have already used it for calculating other things above and this
				// can result in motors running faster than we assumed they would move.
				requiredBraggSpeed = (Math.abs(stepBragg)/pcPulseStepRBV);
				pcGateWidth = Math.abs(((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).convertEnergyToBraggAngle(getEnd() + requiredScannableMotorSpeed * timeUnitConversion * pcPulseWidthRBV * 1.49) - startBragg);

				//For now hardcoded value as VMAX PV not available in ScannableMotor object
				if (requiredBraggSpeed > MAX_BRAGG_SPEED) throw new DeviceException("Bragg speed is greater than maximum allowed speed.");

				 /* To ensure the detector exposure straddles equally across the mid point we should use the PULSE1 block with
				 * PC_PULSE as the input and delay before set to (pulse step size - collection time)/2

				 Code of the form below is needed - but it has to be repeated for each detector with readouttime >0.*/
				zebra.setOutTTL(1, 31); // PULSE1
				zebra.setPulseInput(1, 31); //PC_PULSE
				zebra.setPulseTimeUnit(1, Zebra.PC_TIMEUNIT_SEC);
				zebra.setPulseDelay(1, (pcPulseStepRBVRaw - 10)/2000); //10 is a hardcoded collection time in ms

				break;
			case Zebra.PC_PULSE_SOURCE_EXTERNAL:
				throw new IllegalStateException("PC_PULSE_SOURCE_EXTERNAL is not yet tested");
			default:
				throw new DeviceException("Unacceptable mode " + zebraController.getMode());
			}
			scannableMotorToMove.waitWhileBusy();

			int numberTriggers = getNumberTriggers();
			// add an extra pulse due to TFG configuration in order to end the acquisition of the last point
			zebra.setPCPulseMax(numberTriggers+1);

			zebraController.setupGateAndArm(pcGateStart,pcGateWidth, stepBragg, gateWidthTime );


			if( zebraController.getTimeSeriesCollection() != null){
				for(ZebraCaptureInputStreamCollection ts : zebraController.getTimeSeriesCollection()){
					ts.start(numberTriggers);
				}
			}
			double scannableMotorEndPosition;
			double stepWidthScannableMotorToMove;
			double braggMotorEnd;
			// in zebraConstantMoveController set the end position of the energy and so all the threads will be executed by this object
			braggMotorEnd = getZebra().getPCGateStart()+ getZebra().getPCGateWidthRBV() * (getStep() > 0 ? -1 : 1);
			stepWidthScannableMotorToMove = ((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).convertBraggAngleToEnergy(braggMotorEnd)-pcGateStartScannableMotor;
			scannableMotorEndPosition = pcGateStartScannableMotor/1000 + ((getStep()>0 ? 1.0 : -1.0)*stepWidthScannableMotorToMove/1000 +
					(getStep()>0 ? 1.0 : -1.0)* accelerationDistanceScannableMotor);
			getZebraConstantVelocityMoveController().setScannableMotorEndPosition(scannableMotorEndPosition);
		} catch (Exception e) {
			throw new DeviceException("Error arming the zebra: "+e.getMessage(), e);
		}

	}

	public double getAccelerationDistance(double requiredSpeed, IScannableMotor scannableMotor, double minimumAccelerationDistance) throws DeviceException {
		double minimumAccelerationTime = Double.MAX_VALUE;
		double accelerationDistance = getZebraMotorInfoProvider().distanceToAccToVelocity(requiredSpeed);
		if (accelerationDistance < minimumAccelerationDistance) {
			// Since zebraMotorInfoProvider may use a different ACCL time to the actual motor and we can't get at the
			// value zebraMotorInfoProvider uses anyway, use the actual motor ACCL time instead:
			double timeToVelocity = scannableMotor.getTimeToVelocity();
			double distanceAtVelocity = minimumAccelerationDistance - accelerationDistance;
			double timeAtVelocity = distanceAtVelocity / requiredSpeed;
			double totalTime = timeToVelocity + timeAtVelocity;
			logger.trace("Setting accelerationDistance to minimumAccelerationDistance:"
					+ "timeToVelocity: {}; distanceAtVelocity: {}; timeAtVelocity: {}; totalTime: {}; minimumAccelerationTime: {}",
					timeToVelocity, distanceAtVelocity, timeAtVelocity, totalTime, minimumAccelerationTime);
			accelerationDistance = minimumAccelerationDistance;
		}
		return accelerationDistance;
	}

	@Override
	public void stopAndReset() throws DeviceException, InterruptedException {
		zebraController.stopAndReset();
	}

	@Override
	public void setTriggerPeriod(double seconds) throws DeviceException {
		zebraController.setTriggerPeriod(seconds);
	}

	@Override
	public int getNumberTriggers() {
		return zebraController.getNumberTriggers();
	}

	@Override
	public double getTotalTime() throws DeviceException {
		return zebraController.getTotalTime();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return zebraController.isBusy();
	}

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
		zebraController.setOperatingContinuously(b);
	}

	@Override
	public boolean isOperatingContinously() {
		return zebraController.isOperatingContinously();
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return zebraController.getContinuousMoveController();
	}

	@Override
	public Callable<Double> getPositionCallable() throws DeviceException {
		// TODO: Add the callable value conversion here!!!
		return zebraController.getPositionCallable();
	}

	@Override
	public void setScannableToMove(Collection<ContinuouslyScannableViaController> scannablesToMove) {
		zebraController.setScannableToMove(scannablesToMove);
	}

	@Override
	public void setDetectors(Collection<HardwareTriggeredDetector> detectors) throws DeviceException {
		zebraController.setDetectors(detectors);
	}

	public Double getLastPointAdded() {
		return zebraController.getLastPointAdded();
	}

	public void addPoint(Double point) {
		zebraController.addPoint(point);
	}

	@Override
	public void startMove() throws DeviceException {
		if (sampleShutter != null) {
			logger.info("Opening sample shutter");
			sampleShutter.moveTo(ValvePosition.OPEN);
		}
		logger.info("startMove");
		zebraController.startMove();
	}


	@Override
	public boolean isMoving() throws DeviceException {
		return getZebraConstantVelocityMoveController().isMoving();
	}

	@Override
	public void waitWhileMoving() throws DeviceException, InterruptedException {
		getZebraConstantVelocityMoveController().waitWhileMoving();
	}

	@Override
	public void configure() throws FactoryException {
		zebraController.configure();
	}

	@Override
	public boolean isConfigured() {
		return zebraController.isConfigured();
	}

	@Override
	public boolean isConfigureAtStartup() {
		return zebraController.isConfigureAtStartup();
	}

	@Override
	public void reconfigure() throws FactoryException {
		zebraController.reconfigure();
	}

	@Override
	public Object getPosition() throws DeviceException {
		return zebraController.getPosition();
	}

	@Override
	public void addIObserver(IObserver observer) {
		zebraController.addIObserver(observer);
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		zebraController.setAttribute(attributeName, value);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		zebraController.deleteIObserver(observer);
	}

	@Override
	public String toString() {
		return zebraController.toString();
	}

	@Override
	public void deleteIObservers() {
		zebraController.deleteIObservers();
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		return zebraController.getAttribute(attributeName);
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		zebraController.moveTo(position);
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		zebraController.asynchronousMoveTo(position);
	}

	@Override
	public void close() throws DeviceException {
		zebraController.close();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {
		zebraController.setProtectionLevel(newLevel);
	}

	@Override
	public String checkPositionValid(Object position) throws DeviceException {
		return zebraController.checkPositionValid(position);
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		return zebraController.getProtectionLevel();
	}

	@Override
	public void stop() throws DeviceException {
		zebraController.stop();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		zebraController.waitWhileBusy();
	}

	@Override
	public boolean isAt(Object positionToTest) throws DeviceException {
		return zebraController.isAt(positionToTest);
	}

	@Override
	public void setLevel(int level) {
		zebraController.setLevel(level);
	}

	@Override
	public int getLevel() {
		return zebraController.getLevel();
	}

	@Override
	public String[] getInputNames() {
		return zebraController.getInputNames();
	}

	@Override
	public void setInputNames(String[] names) {
		zebraController.setInputNames(names);
	}

	@Override
	public String[] getExtraNames() {
		return zebraController.getExtraNames();
	}

	@Override
	public void setExtraNames(String[] names) {
		zebraController.setExtraNames(names);
	}

	@Override
	public void setOutputFormat(String[] names) {
		zebraController.setOutputFormat(names);
	}

	@Override
	public String[] getOutputFormat() {
		return zebraController.getOutputFormat();
	}

	@Override
	public void atStart() throws DeviceException {
		zebraController.atScanStart();
	}

	@Override
	public void atEnd() throws DeviceException {
		zebraController.atScanEnd();
	}

	@Override
	public void atScanStart() throws DeviceException {
		zebraController.atScanStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		zebraController.atScanEnd();
		try {
			zebraController.getZebra().reset();
		} catch (IOException e) {
			throw new DeviceException("Problem resetting Zebra:" + e);
		}
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		zebraController.atScanLineStart();
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		zebraController.atScanLineEnd();
	}

	@Override
	public void atPointStart() throws DeviceException {
		zebraController.atPointStart();
	}

	@Override
	public void atPointEnd() throws DeviceException {
		zebraController.atPointEnd();
	}

	@Override
	public void atLevelMoveStart() throws DeviceException {
		zebraController.atLevelMoveStart();
	}

	@Override
	public void atLevelStart() throws DeviceException {
		zebraController.atLevelStart();
	}

	@Override
	public void atLevelEnd() throws DeviceException {
		zebraController.atLevelEnd();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		zebraController.atCommandFailure();
	}

	@Override
	public String toFormattedString() {
		return zebraController.toFormattedString();
	}

	public List<ZebraCaptureInputStreamCollection> getTimeSeriesCollection() {
		return zebraController.getTimeSeriesCollection();
	}

	public PositionStreamIndexer<Double> getPositionSteamIndexer(int index) {
		return zebraController.getPositionSteamIndexer(index);
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController controller) {
		zebraController.setContinuousMoveController(controller);
	}

	@Override
	public int getPointBeingPrepared() {
		return zebraController.getPointBeingPrepared();
	}

	@Override
	public void resetPointBeingPrepared() {
		zebraController.resetPointBeingPrepared();
	}

	public void setSampleShutter(EnumPositioner sampleShutter) {
		this.sampleShutter = sampleShutter;
	}

}
