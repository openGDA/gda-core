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
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionStreamIndexer;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.ScannableUtils;
import gda.device.zebra.controller.Zebra;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;
import gda.scan.ScanBase;

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

	private double pcGateWidthRBV;

	private double pcGateStartRBV;

	private int mode=Zebra.PC_MODE_TIME;

	private double minAccDistance;


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
			zebra.setPCGateSource(0 );// Always position
			zebra.setPCArmSource(0);// Soft
			
			zebra.setPCPulseSource(mode);// Position 

			//set motor before setting gates and pulse parameters
			zebra.setPCEnc(zebraMotorInfoProvider.getPcEnc()); // enc1
			zebra.setPCDir(step>0 ? 0:1 );
			zebra.setPCTimeUnit(Zebra.PC_TIMEUNIT_SEC); //s
			
			zebra.setPCGateNumberOfGates(1);
			
			
			zebra.setPCCaptureBitField(pcCaptureBitField);


			switch(mode){
			case Zebra.PC_MODE_POSITION:
				if(true)
					throw new IllegalStateException("PC_MODE_POSITION is not yet tested");
				break;
			case Zebra.PC_MODE_TIME:
				double maxCollectionTimeFromDetectors = 0.;
				double minCollectionTimeFromDetectors = Double.MAX_VALUE;
				for( Detector det : detectors){
					double collectionTime = det.getCollectionTime();
					maxCollectionTimeFromDetectors = Math.max(maxCollectionTimeFromDetectors, collectionTime);
					minCollectionTimeFromDetectors = Math.min(minCollectionTimeFromDetectors, collectionTime);
				}
				if( Math.abs(minCollectionTimeFromDetectors-maxCollectionTimeFromDetectors) > 1e-8){
					/*
					 * To support 2 collection times we need to use the pulse block to offset the triggers from the 
					 * main PC pulse used by the det with the longest collection time. The offset is half the difference in the collection
					 * times each from the start
					 */
					throw new IllegalArgumentException("ZebraConstantVelocityMoveController cannot handle 2 collection times");
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
				 * 
				 * 
				**/
				boolean exposureStepDefined = zebraMotorInfoProvider.isExposureStepDefined();
				double exposureStep = 0.;
				if( exposureStepDefined){
					// case B - The exposure time of the detector, the distance between pulses are given AND so is the size of the step of the motor to move during exposure. 
					exposureStep = zebraMotorInfoProvider.getExposureStep();
					requiredSpeed = zebraMotorInfoProvider.getExposureStep()/maxCollectionTimeFromDetectors;
					double triggerPeriodFromSpeed = step/requiredSpeed;
					if( triggerPeriodFromSpeed < triggerPeriod )
						throw new IllegalArgumentException("ZebraConstantVelocityMoveController exposureStep, step and collectionTime do not give enough readout time for detectors. Increase collectionTime or reduce exposureStep");
					zebra.setPCPulseStep(triggerPeriodFromSpeed*1000); // in  ms
				} 
				else {
					// case A - The exposure time of the detector and the distance between pulses are given. 
					requiredSpeed = (Math.abs(step)/triggerPeriod);
					exposureStep = maxCollectionTimeFromDetectors*requiredSpeed;
					zebra.setPCPulseStep(triggerPeriod*1000); // in  ms
				}
				
				double firstPulsePos = start - (step>0 ? 1.0 : -1.0)*exposureStep/2;
				double pcPulseStepRBVMS= zebra.getPCPulseStepRBV();
				pcPulseStepRBV = pcPulseStepRBVMS/1000;
				
				//Use at least .5 degrees otherwise we may get error due to encoder noise
				minAccDistance = Math.max(.5, zebraMotorInfoProvider.distanceToAccToVelocity(requiredSpeed));
				scannableMotor.asynchronousMoveTo(firstPulsePos - (step>0 ? 1.0 : -1.0)*minAccDistance);
				zebra.setPCGateStart(firstPulsePos);

				zebra.setPCTimeUnit(Zebra.PC_TIMEUNIT_MS); //ms
				zebra.setPCPulseWidth(.1); //.01ms
				pcPulseWidthRBV = zebra.getPCPulseWidthRBV()/1000;
				
				/*
				 * capture positions half way through collection time
				 */
				
				zebra.setPCPulseDelay(1000.*maxCollectionTimeFromDetectors/2.);
				zebra.setPCPulseWidth(.01); //.01ms
				pcPulseWidthRBV = zebra.getPCPulseWidthRBV()/1000;
				pcPulseDelayRBV = zebra.getPCPulseDelayRBV()/1000.;

				double gateWidthTime = pcPulseDelayRBV +  pcPulseStepRBV*(getNumberTriggers()-1) + pcPulseWidthRBV;
				requiredSpeed = (Math.abs(step)/pcPulseStepRBV);
				zebra.setPCGateWidth((gateWidthTime * requiredSpeed)+minAccDistance);
				
				pcGateWidthRBV = zebra.getPCGateWidthRBV();
				pcGateStartRBV = zebra.getPCGateStartRBV();
				
				/*
				 * To ensure the detector exposure straddles equally across the mid point we should use the PULSE1 block with
				 * PC_PULSE as the input and delay before set to (pulse step size - collection time)/2
				 */
				/* Code of the form below is needed - but it has to be repeated for each detector with readouttime >0.
				zebra.setOutTTL(1, 52); //PULSE1 
				zebra.setPulseInput(1, 31); //PC_PULSE
				zebra.setPulseTimeUnit(1, Zebra.PC_TIMEUNIT_SEC);
				zebra.setPulseDelay(1, (pcPulseStepRBVMS - 10)/2000); //10 is a hardcoded collection time in ms
				*/
				break;
			default:
				throw new DeviceException("Unacceptable mode " + mode);
			}
			scannableMotor.waitWhileBusy();

			int numberTriggers = getNumberTriggers();
			zebra.setPCPulseMax(numberTriggers);
			zebra.pcArm();
			if( timeSeriesCollection != null){
				for(ZebraCaptureInputStreamCollection ts : timeSeriesCollection){
					ts.start(numberTriggers);
				}
			}

		} catch (Exception e) {
			throw new DeviceException("Error arming the zebra", e);
		}

	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		if(mode!= Zebra.PC_MODE_TIME)
			throw new IllegalArgumentException("Only PC_MODE_TIME is supported at the moment");
		this.mode = mode;
	}

	public class ExecuteMoveTask implements Callable<Void> {
		@Override
		public Void call() throws DeviceException, InterruptedException {
			try {
				double speed = scannableMotor.getSpeed();
				try {
					scannableMotor.setSpeed(requiredSpeed);
					scannableMotor.moveTo(pcGateStartRBV + (pcGateWidthRBV*(step > 0? 1: -1)+(step>0 ? 1.0 : -1.0)*minAccDistance)); 
					
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
		logger.debug("isMoving");

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
				ScanBase.checkForInterrupts();
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

}
