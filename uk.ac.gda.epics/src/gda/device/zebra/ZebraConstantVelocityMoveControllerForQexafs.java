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
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.ConstantVelocityMoveController2;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.detector.NXDetector;
import gda.device.detector.addetector.triggering.UnsynchronisedExternalShutterNXCollectionStrategy;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionStreamIndexer;
import gda.device.scannable.ScannableMotor;
import gda.device.zebra.controller.Zebra;
import gda.factory.FactoryException;
import gda.observable.IObserver;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ZebraConstantVelocityMoveControllerForQexafs implements Scannable,
		ConstantVelocityMoveController2, PositionCallableProvider<Double>, ContinuouslyScannableViaController {

	private String name;
	private ZebraConstantVelocityMoveController zebraController;
	private ScannableMotor scannableMotorToMove;
	private double requiredScannableMotorSpeed;
	private double requiredBraggSpeed;
	private double pcPulseStepRBV;
	private double minimumBraggAccelerationDistance;
	private double minimumScannableMotorAccelerationDistance;
	private double pcGateStartScannableMotor;
	private double accelerationDistanceScannableMotor;
	//For now hardcoded value as VMAX PV not available in ScannableMotor object
	private static final double maxBraggSpeed = 0.5;

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
		// TODO Auto-generated method stub
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

	/*public void setScannableMotor(ScannableMotor scannableMotor){
		if (scannableMotor instanceof ZebraScannableMotorForQexafs){
			zebraController.setScannableMotor(scannableMotor);
			this.scannableMotor = (ZebraScannableMotorForQexafs) scannableMotor;
		} else throw new IllegalArgumentException("Scannable is not a ZebraScannableMotorForQexafs");
	}
	*/

	public ScannableMotor getScannableMotor() {
		return zebraController.getScannableMotor();
	}

	/*public ZebraScannableMotorForQexafs getScannableMotor(){
		return (ZebraScannableMotorForQexafs) zebraController.getScannableMotor();
	}
	*/
	@SuppressWarnings("unused")
	@Override
	public void prepareForMove() throws DeviceException, InterruptedException {
		try {
			Zebra zebra;
			double stepBragg;
			double startBragg;
			double endBragg;
			double accelerationDistanceBragg;
			double pcPulseDelayRBV;
			double pcPulseWidthRBV;

			logger.info("prepare for move");
			zebra = getZebra();
			scannableMotorToMove = getScannableMotor();
			zebra.reset(); // Doing a reset does appear to disarm the zebra before we check it, so we don't need to explicitly
			// disarm. We probably don't need to check either, but to verify that we will leave the check and log message in.

			 /*Note that a disarm and waiting for the zebra to no longer be disarmed is not enough. The zebra box will
				* stay armed internally and since a recent zebra support module update, will error when position compare
				* parameters are set.
				*
				* Even if the zebra is saying it is disarmed and you wait 10000ms, you still get this problem.
				*/
			//zebra.pcDisarm();
			//if we want to check it is disarmed we will need to wait >2s as that is the zebra bus update period
			while (zebra.isPCArmed()) {
				logger.info("Zebra already armed, waiting for disarm...");
				Thread.sleep(10000); // 1000ms did not prevent the problem with pcDisarm(), 10000ms is enough with reset() though!
			}
			// TODO: Remove the above code block and comments once we have demonstrated that it is never called.

			//sources must be set first
			zebra.setPCArmSource(Zebra.PC_ARM_SOURCE_SOFT);
			zebra.setPCPulseSource(zebraController.getMode());

			//set motor before setting gates and pulse parameters
			int pcEnc = getZebraMotorInfoProvider().getPcEnc();
			short pcCaptureBitField = zebraController.getPcCaptureBitField(pcEnc);
			zebra.setPCCaptureBitField(pcCaptureBitField);
			zebra.setPCEnc(getZebraMotorInfoProvider().getPcEnc()); // Default is Zebra.PC_ENC_ENC1

			startBragg = ((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).convertEnergyToBraggAngle(zebraController.getStart());
			endBragg = ((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).convertEnergyToBraggAngle(zebraController.getEnd());
			double numberOfPoints = (zebraController.getEnd()-zebraController.getStart())/zebraController.getStep()+1;
			stepBragg = Math.abs((startBragg - endBragg)/numberOfPoints);

			logger.info("stepBragg:"+stepBragg);
			//change here the logic as the bragg angle is negative if step >0 negative and if step<0 positive direction
			zebra.setPCDir(getStep() <0 ? Zebra.PC_DIR_POSITIVE : Zebra.PC_DIR_NEGATIVE);
			zebra.setPCGateNumberOfGates(1);

			double pcGateWidth=0.;
			double pcGateStart=0.;
			double gateWidthTime=0.;
			// multiply by 1000 to convert mDeg into Deg

			requiredScannableMotorSpeed = Math.abs(zebraController.getStart()/1000 - zebraController.getEnd()/1000)/getTotalTime();
			zebraController.setRequiredSpeed(requiredScannableMotorSpeed);

			accelerationDistanceScannableMotor = getAccelerationDistance(requiredScannableMotorSpeed, scannableMotorToMove, minimumScannableMotorAccelerationDistance);
			//if (requiredScannableMotorSpeed > getScannableMotor().get)

			switch(zebraController.getMode()){
			case Zebra.PC_PULSE_SOURCE_POSITION:
				zebra.setPCGateSource(0);
				zebra.setPCPulseStart(0.0);

				double width = Math.abs(endBragg - startBragg);
				pcGateStart = startBragg;
				requiredBraggSpeed = width/getTotalTime();
				accelerationDistanceBragg = getAccelerationDistance(requiredBraggSpeed, ((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).getBraggScannableMotor(), minimumBraggAccelerationDistance);
				pcGateWidth = width + accelerationDistanceBragg;

				double pulseWidth = 0.001;
				zebra.setPCPulseWidth(pulseWidth);
				if (pulseWidth > stepBragg){
					throw new DeviceException(
						"Inconsistent Zebra parameters: the pulse width is greater than the required pulse step, so Zebra will not emit any pulses! You need to change you scan parameters or ask beamline staff.");
				}
				zebra.setPCPulseStep(stepBragg);

				//scannableMotorToMove.asynchronousMoveTo(pcGateStart - 20);
				scannableMotorToMove.asynchronousMoveTo(getStart() - (getStep()>0 ? 1.0 : -1.0)*accelerationDistanceScannableMotor);
				//scannableMotorToMove.asynchronousMoveTo(getStart()/1000-0.1);
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
								logger.info("Detector " + det.getName() + " returned newMinimumAccelerationTime=" +
										newMinimumAccelerationTime + " so minimumAccelerationTime now " + minimumAccelerationTime +
										" (" + ues.getClass().getName() + ")");
							} else {
								logger.info("Detector " + det.getName() + " collection strategy is not an " +
										"UnsynchronisedExternalShutterNXCollectionStrategy: " + nxcs.getClass().getName());
							}
						} else {
							logger.info("Detector " + det.getName() + " is not an NXdetector! " + det.getClass().getName());
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
					if (maxCollectionTimeFromDetectors > 214){  //Using 20 rather 214 makes it faster to test the switchover.  ) {
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
					double exposureStep, pcPulseStepRaw;
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
						requiredScannableMotorSpeed = (Math.abs(zebraController.getStep())/zebraController.getTriggerPeriod());
						zebraController.setRequiredSpeed(requiredScannableMotorSpeed);
						exposureStep = maxCollectionTimeFromDetectors*requiredBraggSpeed;
						pcPulseStepRaw = zebraController.getTriggerPeriod()*timeUnitConversion;
					}
					accelerationDistanceBragg = getAccelerationDistance(requiredBraggSpeed, ((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).getBraggScannableMotor(), minimumBraggAccelerationDistance);

					zebra.setPCPulseStep(pcPulseStepRaw);
					Thread.sleep(1); // TODO: Remove when the bug in zebra RBV handling is fixed.
					// Note that we need to read back values relating to time, so that the we calculate dependent values based on the
					// actual values in use rather than the values we asked for.
					final double pcPulseStepRBVRaw= zebra.getPCPulseStepRBV();
					zebraController.checkRBV(pcPulseStepRaw, pcPulseStepRBVRaw, 0.0001, "pcPulseStep");
					pcPulseStepRBV = pcPulseStepRBVRaw/timeUnitConversion;

					logger.info("pcPulseStepRaw="+pcPulseStepRaw+" exposureStep="+exposureStep+" requiredSpeed="+requiredBraggSpeed);
					logger.info("pcPulseStepRBVRaw="+pcPulseStepRBVRaw+" pcPulseStepRBV="+pcPulseStepRBV);

					pcGateStart = startBragg - (stepBragg>0 ? 1.0 : -1.0)*exposureStep/2;
					pcGateStartScannableMotor = getStart() - (getStep()>0 ? 1.0 : -1.0)*exposureStep/2;
					scannableMotorToMove.asynchronousMoveTo(pcGateStartScannableMotor/1000 - (getStep()/1000>0 ? 1.0 : -1.0)*accelerationDistanceScannableMotor);
					//scannableMotorToMove.asynchronousMoveTo(pcGateStartScannableMotor/1000 - 0.100);

					logger.info("firstPulsePos for Energy="+pcGateStartScannableMotor+" accelerationDistance for Energy="+accelerationDistanceScannableMotor);
					logger.info("firstPulsePos for Bragg="+pcGateStart+" accelerationDistance for Bragg="+accelerationDistanceBragg);
					// Capture positions half way through collection time
					double pcPulseDelayRaw=timeUnitConversion*maxCollectionTimeFromDetectors/2.;
					zebra.setPCPulseDelay(pcPulseDelayRaw);

					double pcPulseWidthRaw;
					if ( !zebraController.isPcPulseGateNotTrigger() ) {
						pcPulseWidthRaw = Math.max(0.01*timeUnitConversion, 0.0001);
					} else {
						//pcPulseWidthRaw=maxCollectionTimeFromDetectors*timeUnitConversion;
						// TODO: Remove offset when the bug in zebra with PC_PULSE_WID == PC_PULSE_STEP is fixed.
						pcPulseWidthRaw=maxCollectionTimeFromDetectors*timeUnitConversion-0.0002;
					}
					logger.info("isPcPulseGateNotTrigger="+zebraController.isPcPulseGateNotTrigger()+", maxCollectionTimeFromDetectors="+
							maxCollectionTimeFromDetectors+", pcPulseWidthRaw="+pcPulseWidthRaw);
					zebra.setPCPulseWidth(pcPulseWidthRaw);

					Thread.sleep(1); // TODO: Remove when the bug in zebra RBV handling is fixed

					double pcPulseWidthRBVRaw = zebra.getPCPulseWidthRBV();
					zebraController.checkRBV(pcPulseWidthRaw, pcPulseWidthRBVRaw, 0.0001, "pcPulseWidth");
					pcPulseWidthRBV = pcPulseWidthRBVRaw/timeUnitConversion;

					double pcPulseDelayRBVRaw = zebra.getPCPulseDelayRBV();
					zebraController.checkRBV(pcPulseDelayRaw, pcPulseDelayRBVRaw, 0.0001, "pcPulseDelay");
					pcPulseDelayRBV = pcPulseDelayRBVRaw/timeUnitConversion;
					//zebra EPICs updated so needed to change the number of triggers as an extra pulse is added due to TFG.
					gateWidthTime = pcPulseDelayRBV +  pcPulseStepRBV*getNumberTriggers() + pcPulseWidthRBV;
					// TODO: It appears that the above can now be simplified to pcPulseStepRBV*getNumberTriggers (as below) but this
					//       needs to be tested before being deployed to Trigger detectors.
					//zebra EPICs updated so needed to change the number of triggers as an extra pulse is added due to TFG.
					if ( zebraController.isPcPulseGateNotTrigger() ) gateWidthTime = pcPulseStepRBV*(getNumberTriggers()+1);
					// How about: gateWidthTime = pcPulseStepRBV*(getNumberTriggers()-1) + min (pcPulseDelayRBV + pcPulseWidthRBV, pcPulseStepRBV);

					// Why do we recalculate requiredSpeed here? We have already used it for calculating other things above and this
					// can result in motors running faster than we assumed they would move.
					requiredBraggSpeed = (Math.abs(stepBragg)/pcPulseStepRBV);
					pcGateWidth=(gateWidthTime * requiredBraggSpeed)+accelerationDistanceBragg;
					// Why do we add accelerationDistance to the gate width? We add it to the moveTo in ExecuteMoveTask anyway

					double stepScannableMotor = ((((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).convertBraggAngleToEnergy((pcGateStart+pcGateWidth)*1000))-(((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).convertBraggAngleToEnergy(pcGateStart*1000)))/(numberOfPoints-1);
					//requiredScannableMotorSpeed should be in keV/s but pcPulseStepRBV is in s and stepScannableMotor in eV
					logger.info("StepScannableMotor:"+stepScannableMotor);
					requiredScannableMotorSpeed = (stepScannableMotor/1000)/(pcPulseStepRBV);
					// Note that this setPCGateWidth is pointless, since we haven't setPCGateSource yet (and setupGateAndArm does a setPCGateWidth later)
					zebra.setPCGateWidth((gateWidthTime * requiredBraggSpeed)+accelerationDistanceBragg);
					Thread.sleep(1); // TODO: Remove when the bug in zebra RBV handling is fixed
					// Note, all Zebra support modules deployed at version 0.2 or later should be Ok, and should not need this cludge.
					// Reviewing the zebra support modules deployed on dls_sw, not are earlier than 1.3, so this should be safe to remove now.
					logger.info("New Bragg requiredSpeed="+requiredBraggSpeed);
					//if (requiredBraggSpeed >((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).getBraggScannableMotor().

					//For now hardcoded value as VMAX PV not available in ScannableMotor object
					if (requiredBraggSpeed > maxBraggSpeed) throw new DeviceException("Bragg speed is greater than maximum allowed speed.");

					 /* To ensure the detector exposure straddles equally across the mid point we should use the PULSE1 block with
					 * PC_PULSE as the input and delay before set to (pulse step size - collection time)/2

					 Code of the form below is needed - but it has to be repeated for each detector with readouttime >0.*/
				zebra.setOutTTL(1, 31); // PULSE1
					zebra.setPulseInput(1, 31); //PC_PULSE
					zebra.setPulseTimeUnit(1, Zebra.PC_TIMEUNIT_SEC);
					zebra.setPulseDelay(1, (pcPulseStepRBVRaw - 10)/2000); //10 is a hardcoded collection time in ms

					break;
				case Zebra.PC_PULSE_SOURCE_EXTERNAL:
					if(true)
						throw new IllegalStateException("PC_PULSE_SOURCE_EXTERNAL is not yet tested");
					break;
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
				getZebraConstantVelocityMoveController().setRequiredSpeed(requiredScannableMotorSpeed);
				double scannableMotorEndPosition;
				double stepWidthScannableMotorToMove;
				double braggMotorEnd;
				// in zebraConstantMoveController set the end position of the energy and so all the threads will be executed by this object
				braggMotorEnd = getZebra().getPCGateStart()+ getZebra().getPCGateWidthRBV();
				stepWidthScannableMotorToMove = ((ZebraScannableMotorForQexafs) getZebraMotorInfoProvider()).convertBraggAngleToEnergy(braggMotorEnd*1000)-pcGateStartScannableMotor;
				scannableMotorEndPosition = pcGateStartScannableMotor/1000 + ((getStep()>0 ? 1.0 : -1.0)*stepWidthScannableMotorToMove/1000 +
						(getStep()>0 ? 1.0 : -1.0)* accelerationDistanceScannableMotor);
				getZebraConstantVelocityMoveController().setScannableMotorEndPosition(scannableMotorEndPosition);
			} catch (Exception e) {
				throw new DeviceException("Error arming the zebra: "+e.getMessage(), e);
			}

	}


	public double getAccelerationDistance(double requiredSpeed,ScannableMotor scannableMotor,double minimumAccelerationDistance) throws DeviceException{
		double minimumAccelerationTime = Double.MAX_VALUE;
		double accelerationDistance = getZebraMotorInfoProvider().distanceToAccToVelocity(requiredSpeed);
		logger.info("accelerationDistance=" + accelerationDistance + " minimumAccelerationDistance=" + minimumAccelerationDistance);
		if (accelerationDistance < minimumAccelerationDistance) {
			// Since zebraMotorInfoProvider may use a different ACCL time to the actual motor and we can't get at the
			// value zebraMotorInfoProvider uses anyway, use the actual motor ACCL time instead:
			double timeToVelocity = scannableMotor.getTimeToVelocity();
			double distanceAtVelocity = minimumAccelerationDistance - accelerationDistance;
			double timeAtVelocity = distanceAtVelocity / requiredSpeed;
			double totalTime = timeToVelocity + timeAtVelocity;
			logger.info("Setting accelerationDistance to minimumAccelerationDistance: timeToVelocity=" + timeToVelocity +
				" distanceAtVelocity=" + distanceAtVelocity + " timeAtVelocity=" + timeAtVelocity +
				" totalTime=" + totalTime + " minimumAccelerationTime=" + minimumAccelerationTime);
			/*if ((timeToVelocity + timeAtVelocity) > (minimumAccelerationTime*0.9)) // 90% before, 10% after
				throw new IllegalArgumentException("\n Minimum acceleration distance " + minimumBraggAccelerationDistance +
						" takes too long (" + totalTime + "s) at speed " + requiredBraggSpeed +
						"\n Either increase rock size, decrease collection time or increase CollectionExtensionTime " +
						"\n and take a new dark (currently extension time=" + minimumAccelerationTime + "s) e.g.:" +
						"\n  <detector>.getCollectionStrategy().setCollectionExtensionTimeS(" + (int)(totalTime*2+1) + ")");*/
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
	public void setName(String name) {
		this.name = name;
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
	public String getName() {
		return name;
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

}
