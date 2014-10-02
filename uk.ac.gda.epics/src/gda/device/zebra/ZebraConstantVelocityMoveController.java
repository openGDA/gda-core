/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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
import gda.device.detector.addetector.triggering.SingleExposureUnsynchronisedExternalShutter;
import gda.device.detector.addetector.triggering.UnsynchronisedExternalShutterNXCollectionStrategy;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionStreamIndexer;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.ScannableUtils;
import gda.device.zebra.controller.Zebra;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ZebraConstantVelocityMoveController extends ScannableBase implements ConstantVelocityMoveController2, 
						PositionCallableProvider<Double>, ContinuouslyScannableViaController, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(ZebraConstantVelocityMoveController.class);

	short pcCaptureBitField = 1;
	Zebra zebra;

	ScannableMotor scannableMotor;
	ZebraMotorInfoProvider zebraMotorInfoProvider;

	protected double pcGateWidthRBV;

	protected double pcGateStartRBV;

	private int mode=Zebra.PC_PULSE_SOURCE_TIME;

	private double accelerationDistance;

	private boolean pcPulseGateNotTrigger = false;

	private double minimumAccelerationDistance = 0.5; // If this changes, change the setMinimumAccelerationDistance javadoc.

	public ZebraConstantVelocityMoveController() {
		super();
		setExtraNames(new String[]{"Time"});
		setInputNames(new String[]{});
		setOutputFormat(new String[]{"%5.5g"});
	}

	@SuppressWarnings("unused")
	@Override
	public void prepareForMove() throws DeviceException, InterruptedException {
		try {
			logger.info("prepare for move");

			zebra.pcDisarm();
			//if we want to check it is disarmed we will need to wait >2s as that is the zebra bus update period

			//sources must be set first
			zebra.setPCArmSource(Zebra.PC_ARM_SOURCE_SOFT);
			zebra.setPCPulseSource(mode);

			//set motor before setting gates and pulse parameters
			zebra.setPCCaptureBitField(pcCaptureBitField);
			zebra.setPCEnc(zebraMotorInfoProvider.getPcEnc()); // Default is Zebra.PC_ENC_ENC1
			zebra.setPCDir(step>0 ? Zebra.PC_DIR_POSITIVE : Zebra.PC_DIR_NEGATIVE);
			
			zebra.setPCGateNumberOfGates(1);
			double pcGateWidth=0.;
			double pcGateStart=0.;
			double gateWidthTime=0.;
			switch(mode){
			case Zebra.PC_PULSE_SOURCE_POSITION:
				if(true)
					throw new IllegalStateException("PC_PULSE_SOURCE_POSITION is not yet tested");
				break;
			case Zebra.PC_PULSE_SOURCE_TIME:
				double maxCollectionTimeFromDetectors = 0.;
				double minCollectionTimeFromDetectors = Double.MAX_VALUE;
				double minimumAccelerationTime = Double.MAX_VALUE;
				
				for( Detector det : detectors){
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
					/*
					 * To support 2 collection times we need to use the pulse block to offset the triggers from the 
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
				if (maxCollectionTimeFromDetectors > 20 /* TODO: Should be 200, set to 20 for quicker testing */ ) {
					zebra.setPCTimeUnit(Zebra.PC_TIMEUNIT_SEC);
					timeUnitConversion = 1;
				} else {
					zebra.setPCTimeUnit(Zebra.PC_TIMEUNIT_MS);
					timeUnitConversion = 1000;
				}
				/**
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
				boolean exposureStepDefined = zebraMotorInfoProvider.isExposureStepDefined();
				double exposureStep, pcPulseStepRaw;
				if( exposureStepDefined){
					// case B - The exposure time of the detector, the distance between pulses are given AND so is the size of the step of the motor to move during exposure. 
					exposureStep = zebraMotorInfoProvider.getExposureStep();
					requiredSpeed = zebraMotorInfoProvider.getExposureStep()/maxCollectionTimeFromDetectors;
					double triggerPeriodFromSpeed = step/requiredSpeed;
					if( triggerPeriodFromSpeed < triggerPeriod )
						throw new IllegalArgumentException("ZebraConstantVelocityMoveController exposureStep, step and collectionTime do not give enough readout time for detectors. Increase collectionTime or reduce exposureStep");
					pcPulseStepRaw = triggerPeriodFromSpeed*timeUnitConversion;
				} 
				else {
					// case A - The exposure time of the detector and the distance between pulses are given. 
					requiredSpeed = (Math.abs(step)/triggerPeriod);
					exposureStep = maxCollectionTimeFromDetectors*requiredSpeed;
					pcPulseStepRaw = triggerPeriod*timeUnitConversion;
				}
				zebra.setPCPulseStep(pcPulseStepRaw);
				Thread.sleep(1); // TODO: Remove when the bug in zebra RBV handling is fixed.
				// Note that we need to read back values relating to time, so that the we calculate dependent values based on the
				// actual values in use rather than the values we asked for.
				final double pcPulseStepRBVRaw= zebra.getPCPulseStepRBV();
				checkRBV(pcPulseStepRaw, pcPulseStepRBVRaw, 0.0001, "pcPulseStep");
				pcPulseStepRBV = pcPulseStepRBVRaw/timeUnitConversion;
				
				logger.info("pcPulseStepRaw="+pcPulseStepRaw+" exposureStep="+exposureStep+" requiredSpeed="+requiredSpeed);
				logger.info("pcPulseStepRBVRaw="+pcPulseStepRBVRaw+" pcPulseStepRBV="+pcPulseStepRBV);
				
				double accelerationDistance = zebraMotorInfoProvider.distanceToAccToVelocity(requiredSpeed);
				logger.info("accelerationDistance=" + accelerationDistance + " minimumAccelerationDistance=" + minimumAccelerationDistance);
				if (accelerationDistance < minimumAccelerationDistance) {
					// Since zebraMotorInfoProvider may use a different ACCL time to the actual motor and we can't get at the
					// value zebraMotorInfoProvider uses anyway, use the actual motor ACCL time instead:
					double timeToVelocity = zebraMotorInfoProvider.getActualScannableMotor().getTimeToVelocity();
					double distanceAtVelocity = minimumAccelerationDistance - accelerationDistance;
					double timeAtVelocity = distanceAtVelocity / requiredSpeed;
					double totalTime = timeToVelocity + timeAtVelocity;
					logger.info("Setting accelerationDistance to minimumAccelerationDistance: timeToVelocity=" + timeToVelocity +
							" distanceAtVelocity=" + distanceAtVelocity + " timeAtVelocity=" + timeAtVelocity + 
							" totalTime=" + totalTime + " minimumAccelerationTime=" + minimumAccelerationTime);
					if ((timeToVelocity + timeAtVelocity) > (minimumAccelerationTime*0.9)) // 90% before, 10% after
						throw new IllegalArgumentException("\n Minimum acceleration distance " + minimumAccelerationDistance +
							" takes too long (" + totalTime + "s) at speed " + requiredSpeed + 
							"\n Either increase rock size, decrease collection time or increase CollectionExtensionTime " +
							"\n and take a new dark (currentl extension time=" + minimumAccelerationTime + "s) e.g.:" +
							"\n  <detector>.getCollectionStrategy().setCollectionExtensionTimeS(" + (int)(totalTime*2+1) + ")");
					accelerationDistance = minimumAccelerationDistance;
				}
				
				pcGateStart = start - (step>0 ? 1.0 : -1.0)*exposureStep/2;
				
				scannableMotor.asynchronousMoveTo(pcGateStart - (step>0 ? 1.0 : -1.0)*accelerationDistance);
				
				logger.info("firstPulsePos="+pcGateStart+" accelerationDistance="+accelerationDistance);
				
				// Capture positions half way through collection time
				double pcPulseDelayRaw=timeUnitConversion*maxCollectionTimeFromDetectors/2.;
				zebra.setPCPulseDelay(pcPulseDelayRaw);
				
				double pcPulseWidthRaw;
				if ( !isPcPulseGateNotTrigger() ) {
					pcPulseWidthRaw =0.0001; // This is the minimum on any time unit range
				} else {
					//pcPulseWidthRaw=maxCollectionTimeFromDetectors*timeUnitConversion;
					// TODO: Remove offset when the bug in zebra with PC_PULSE_WID == PC_PULSE_STEP is fixed.
					pcPulseWidthRaw=maxCollectionTimeFromDetectors*timeUnitConversion-0.0002;
				}
				logger.info("isPcPulseGateNotTrigger="+isPcPulseGateNotTrigger()+", maxCollectionTimeFromDetectors="+
						maxCollectionTimeFromDetectors+", pcPulseWidthRaw="+pcPulseWidthRaw);
				zebra.setPCPulseWidth(pcPulseWidthRaw);
				
				Thread.sleep(1); // TODO: Remove when the bug in zebra RBV handling is fixed
				
				double pcPulseWidthRBVRaw = zebra.getPCPulseWidthRBV();
				checkRBV(pcPulseWidthRaw, pcPulseWidthRBVRaw, 0.0001, "pcPulseWidth");
				pcPulseWidthRBV = pcPulseWidthRBVRaw/timeUnitConversion;
				
				double pcPulseDelayRBVRaw = zebra.getPCPulseDelayRBV();
				checkRBV(pcPulseDelayRaw, pcPulseDelayRBVRaw, 0.0001, "pcPulseDelay");
				pcPulseDelayRBV = pcPulseDelayRBVRaw/timeUnitConversion;
				
				gateWidthTime = pcPulseDelayRBV +  pcPulseStepRBV*(getNumberTriggers()-1) + pcPulseWidthRBV;
				// TODO: It appears that the above can now be simplified to pcPulseStepRBV*getNumberTriggers (as below) but this
				//       needs to be tested before being deployed to Trigger detectors.
				if ( isPcPulseGateNotTrigger() ) gateWidthTime = pcPulseStepRBV*getNumberTriggers();
				// How about: gateWidthTime = pcPulseStepRBV*(getNumberTriggers()-1) + min (pcPulseDelayRBV + pcPulseWidthRBV, pcPulseStepRBV);
				
				// Why do we recalculate requiredSpeed here? We have already used it for calculating other things above and this
				// can result in motors running faster than we assumed they would move.
				requiredSpeed = (Math.abs(step)/pcPulseStepRBV);
				pcGateWidth=(gateWidthTime * requiredSpeed)+accelerationDistance;
				// Why do we add accelerationDistance to the gate width? We add it to the moveTo in ExecuteMoveTask anyway
				zebra.setPCGateWidth((gateWidthTime * requiredSpeed)+accelerationDistance);
				Thread.sleep(1); // TODO: Remove when the bug in zebra RBV handling is fixed
				logger.info("New requiredSpeed="+requiredSpeed);
				
				/*
				 * To ensure the detector exposure straddles equally across the mid point we should use the PULSE1 block with
				 * PC_PULSE as the input and delay before set to (pulse step size - collection time)/2
				 */
				/* Code of the form below is needed - but it has to be repeated for each detector with readouttime >0.
				zebra.setOutTTL(1, 52); //PULSE1 
				zebra.setPulseInput(1, 31); //PC_PULSE
				zebra.setPulseTimeUnit(1, Zebra.PC_TIMEUNIT_SEC);
				zebra.setPulseDelay(1, (pcPulseStepRBVRaw - 10)/2000); //10 is a hardcoded collection time in ms
				*/
				break;
			case Zebra.PC_PULSE_SOURCE_EXTERNAL:
				if(true)
					throw new IllegalStateException("PC_PULSE_SOURCE_EXTERNAL is not yet tested");
				break;
			default:
				throw new DeviceException("Unacceptable mode " + mode);
			}
			scannableMotor.waitWhileBusy();

			int numberTriggers = getNumberTriggers();
			zebra.setPCPulseMax(numberTriggers);

			setupGateAndArm(pcGateStart,pcGateWidth, step, gateWidthTime );
			
			
			if( timeSeriesCollection != null){
				for(ZebraCaptureInputStreamCollection ts : timeSeriesCollection){
					ts.start(numberTriggers);
				}
			}

		} catch (Exception e) {
			throw new DeviceException("Error arming the zebra: "+e.getMessage(), e);
		}

	}

	protected void setupGateAndArm(double pcGateStart, double pcGateWidth, @SuppressWarnings("unused") double step2, @SuppressWarnings("unused") double gateWidthTimeInS) throws Exception {
		zebra.setPCGateSource(Zebra.PC_GATE_SOURCE_POSITION);
		zebra.setPCGateStart(pcGateStart);
		zebra.setPCGateWidth(pcGateWidth);
		
		// Note that we need to read back any values relating to a physical motor, as the readback will be quantised to the
		// resolution of the motor.
		pcGateWidthRBV = zebra.getPCGateWidthRBV();
		pcGateStartRBV = zebra.getPCGateStartRBV();
		// We can't run checkRBV() on motor positions as we don't know how much they are going to change.
		
		zebra.pcArm();
	}

	private void checkRBV(double raw, double RBVRaw, double tolerance, String desc) {
		double rawDiff = Math.abs(raw - RBVRaw);
		if (rawDiff > tolerance) {
			throw new IllegalStateException("ZebraConstantVelocityMoveController: Wrote "+raw+" to "+desc+" but read back "+RBVRaw+" (diff="+rawDiff+")");
		}
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		if(mode!= Zebra.PC_PULSE_SOURCE_TIME)
			throw new IllegalArgumentException("Only PC_PULSE_SOURCE_TIME is supported at the moment");
		this.mode = mode;
	}

	public boolean isPcPulseGateNotTrigger() {
		return pcPulseGateNotTrigger;
	}

	public void setPcPulseGateNotTrigger(boolean pcPulseGateNotTrigger) {
		this.pcPulseGateNotTrigger = pcPulseGateNotTrigger;
	}

	public class ExecuteMoveTask implements Callable<Void> {
		@Override
		public Void call() throws DeviceException, InterruptedException {
			try {
				double speed = scannableMotor.getSpeed();
				try {
					scannableMotor.setSpeed(requiredSpeed);
					scannableMotor.moveTo(pcGateStartRBV + ((step>0 ? 1.0 : -1.0)*pcGateWidthRBV +
															(step>0 ? 1.0 : -1.0)*accelerationDistance ));
					
				} finally {
					scannableMotor.setSpeed(speed);
				}
			} catch (DeviceException e) {
				logger.error("Problem in trajectory move Thread (will be thrown in waitWhileMoving()): \n",
						e.getMessage());
				throw e;
			}
			return null;
		}

	}

	private FutureTask<Void> moveFuture;
	private double triggerPeriod;
	private double end;
	private double step;
	private double start;

	@SuppressWarnings("unchecked")
	public PositionStreamIndexer<Double> lastImageNumberStreamIndexer[] = new PositionStreamIndexer[11];

	@Override
	public void startMove() throws DeviceException {
		logger.info("startMove");

		moveFuture = new FutureTask<Void>(new ExecuteMoveTask());
		new Thread(moveFuture, getName() + "_execute_move").start(); // FutureTask implements Runnable

	}

	@Override
	public boolean isMoving() throws DeviceException {
		logger.trace("isMoving");

		return !((moveFuture == null) || (moveFuture.isDone()));
	}

	@Override
	public void waitWhileMoving() throws DeviceException, InterruptedException {
		logger.info("waitWhileMoving");
		if (moveFuture == null) {
			return;
		}
		try {
			boolean done=false;
			while( !done ){
				done = moveFuture.isDone();
				if( timeSeriesCollection != null){
					for( ZebraCaptureInputStreamCollection ts : timeSeriesCollection){
						done &= ts.isComplete();
					}
				}
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			scannableMotor.stop();
			throw e;
		} finally{
			moveFuture=null;
		}
	}

	@Override
	public void stopAndReset() throws DeviceException, InterruptedException {
		logger.info("stopAndReset");
		points = null;
	}

	@Override
	public void setTriggerPeriod(double seconds) throws DeviceException {
		logger.info("setTriggerPeriod:"+seconds);
		triggerPeriod = seconds; //readout need to use readout time;

	}

	@Override
	public int getNumberTriggers() {
		logger.info("getNumberTriggers");
		try {
			return ScannableUtils.getNumberSteps(scannableMotor, new Double(start),new Double(end),new Double(step))+1;
		} catch (Exception e) {
			logger.error("Error getting number of triggers", e);
			return 0;
		}
	}

	@Override
	public double getTotalTime() throws DeviceException {
		logger.info("getTotalTime");
		return (getNumberTriggers() == 0) ? 0 : triggerPeriod * (getNumberTriggers() - 1);
	}

	@Override
	public void configure() throws FactoryException {
		logger.info("configure");
	}

	@Override
	public void setStart(double start) throws DeviceException {
		logger.info("setStart:" + start); 
		this.start = start;
	}

	@Override
	public void setEnd(double end) throws DeviceException {
		logger.info("setEnd:" + end);
		this.end = end;
	}

	@Override
	public void setStep(double step) throws DeviceException {
		logger.info("setStep:"+ step);
		this.step = step;

	}

	// copied from EpicsTrajectoryMoveControllerAdapter - need a base class
	List<Double> points = null;

	public List<ZebraCaptureInputStreamCollection> timeSeriesCollection;

	public void addPoint(Double point) {
		if(points == null){
			points = new ArrayList<Double>();
		}
		points.add(point);
	}

	public Double getLastPointAdded() {
		if (points == null || points.size() == 0) {
			logger.info(getName() + ".getLastPointAdded() returning null, as no points have yet been added");
			return null;
		}
		return points.get(points.size() - 1);
	}


	public Zebra getZebra() {
		return zebra;
	}

	public void setZebra(Zebra zebra) {
		this.zebra = zebra;
	}



	public ScannableMotor getScannableMotor() {
		return scannableMotor;
	}

	public void setScannableMotor(ScannableMotor scannableMotor) {
		this.scannableMotor = scannableMotor;
	}

	public ZebraMotorInfoProvider getZebraMotorInfoProvider() {
		return zebraMotorInfoProvider;
	}

	public void setZebraMotorInfoProvider(ZebraMotorInfoProvider zebraMotorInfoProvider) {
		this.zebraMotorInfoProvider = zebraMotorInfoProvider;
		setScannableMotor(zebraMotorInfoProvider.getActualScannableMotor());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (zebra == null)
			throw new Exception("zebra is not set");
	}

	int numPosCallableReturned = 0;
	int numPosCallableReturned1 = 0;

	private double pcPulseDelayRBV;

	private double pcPulseWidthRBV;

	private double pcPulseStepRBV;

	private double requiredSpeed;

	private boolean operatingContinously=false;

	private Collection<HardwareTriggeredDetector> detectors;


	public PositionStreamIndexer<Double> getPositionSteamIndexer(int index) {
		if( lastImageNumberStreamIndexer[index] == null){
			logger.info("Creating lastImageNumberStreamIndexer " + index);
			ReadOnlyPV<Double[]> rdDblArrayPV = zebra.getEnc1AvalPV();
			switch(index){
			case 0:
				rdDblArrayPV = zebra.getEnc1AvalPV();
				break;
			case 10:
				rdDblArrayPV = zebra.getPCTimePV();
				break;
			}
			if( timeSeriesCollection == null)
				timeSeriesCollection = new Vector<ZebraCaptureInputStreamCollection>();
			
				
			ZebraCaptureInputStreamCollection sc = new ZebraCaptureInputStreamCollection(zebra.getNumberOfPointsDownloadedPV(), rdDblArrayPV);
			lastImageNumberStreamIndexer[index] = new PositionStreamIndexer<Double>(sc);
			timeSeriesCollection.add(sc);
		}
		numPosCallableReturned++;
		return lastImageNumberStreamIndexer[index];
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public void atScanLineStart() throws DeviceException {
		lastImageNumberStreamIndexer = new PositionStreamIndexer[11];
		timeSeriesCollection = null;
		moveFuture=null;
		numPosCallableReturned=0;
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		timeSeriesCollection = null;
	}

	@Override
	public void stop() throws DeviceException {
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
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public Callable<Double> getPositionCallable() throws DeviceException {
		return getPositionSteamIndexer(10).getNamedPositionCallable(getExtraNames()[0], 1);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return 0.; // getPositionCallable will be called during the scan
	}

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
		operatingContinously = b;
		
	}

	@Override
	public boolean isOperatingContinously() {
		return operatingContinously;
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return this;
	}

	public Scannable createScannable(Scannable delegate){
		ContinuousScannable cs = new ContinuousScannable();
		cs.setDelegate(delegate);
		cs.setContinuousMoveController(this);
		return cs;
	}

	@Override
	public void setScannableToMove(Collection<ContinuouslyScannableViaController> scannablesToMove) {
//		this.scannablesToMove = scannablesToMove;
		ContinuouslyScannableViaController[] array = scannablesToMove.toArray(new ContinuouslyScannableViaController[]{});
		ContinuouslyScannableViaController continuouslyScannableViaController = array[0];
		if( ! (continuouslyScannableViaController instanceof ZebraMotorInfoProvider))
			throw new IllegalArgumentException("First scannable is not a ZebraMotorInfoProvider");
		setZebraMotorInfoProvider((ZebraMotorInfoProvider)continuouslyScannableViaController);	
	}

	@Override
	public void setDetectors(Collection<HardwareTriggeredDetector> detectors)  {
		this.detectors = detectors;
	}

	/**
	 * Set the minimum allowable acceleration distance. If this value is less than distanceToAccToVelocity at the
	 * requiredSpeed then it will be used instead of the calculated value.
	 * 
	 * Note: This will result in a change to the time it takes for the motor to get to the start position.
	 * 
	 * This value defaults to .5 ("degrees otherwise we may get error due to encoder noise") but should never be
	 * less than the deadband of the motor, otherwise the motor may already be in the gated area at the start of
	 * the move and the gate (and this pulse) will never be triggered. 
	 * 
	 * @param minimumAccelerationDistance
	 */
	public final void setMinimumAccelerationDistance(double minimumAccelerationDistance) {
		this.minimumAccelerationDistance = minimumAccelerationDistance;
	}

	public double getMinimumAccelerationDistance() {
		return minimumAccelerationDistance;
	}
}
