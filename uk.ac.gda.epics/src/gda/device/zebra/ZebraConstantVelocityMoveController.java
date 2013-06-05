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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.ConstantVelocityMoveController;
import gda.device.continuouscontroller.ContinuousMoveController;
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
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ZebraConstantVelocityMoveController extends ScannableBase implements ConstantVelocityMoveController, 
PositionCallableProvider<Double>, ContinuouslyScannableViaController, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(ZebraConstantVelocityMoveController.class);

	double pcPulseDelay = 0.01;
	short pcCaptureBitField = 1;
	Zebra zebra;

	ScannableMotor scannableMotor;
	ZebraMotorInfoProvider zebraMotorInfoProvider;

	private double pcGateWidthRBV;

	private double pcGateStartRBV;

	private int mode=Zebra.PC_MODE_TIME;


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

			requiredSpeed = (Math.abs(step)/triggerPeriod);
			
			scannableMotor.asynchronousMoveTo(start - (step>0 ? 1.0 : -1.0)*zebraMotorInfoProvider.distanceToAccToVelocity(requiredSpeed));

			//sources must be set first
			zebra.setPCGateSource(0 );// Always position
			zebra.setPCArmSource(0);// Soft
			
			zebra.setPCPulseSource(mode);// Position 

			//set motor before setting gates and pulse parameters
			zebra.setPCEnc(zebraMotorInfoProvider.getPcEnc()); // enc1
			zebra.setPCDir(step>0 ? 0:1 );
			zebra.setPCTimeUnit(Zebra.PC_TIMEUNIT_SEC); //s
			
			zebra.setPCGateStart(start);
			zebra.setPCGateNumberOfGates(1);
			
			
			zebra.setPCCaptureBitField(pcCaptureBitField);

			zebra.setPCPulseDelay(0.);
			pcPulseDelayRBV = zebra.getPCPulseDelayRBV();

			switch(mode){
			case Zebra.PC_MODE_POSITION:
				if(true)
					throw new IllegalStateException("PC_MODE_POSITION is not yet tested");
				zebra.setPCPulseWidth(Math.abs(step/2));
				pcPulseWidthRBV = zebra.getPCPulseWidthRBV();
				zebra.setPCPulseStep(Math.abs(step));

				pcPulseStepRBV = zebra.getPCPulseStepRBV();
				
				double gateWidthPosn = pcPulseDelayRBV +  pcPulseStepRBV*(getNumberTriggers()-1) + pcPulseWidthRBV;
				
				zebra.setPCGateWidth(gateWidthPosn);
				
				pcGateWidthRBV = zebra.getPCGateWidthRBV();
				pcGateStartRBV = zebra.getPCGateStartRBV();
				requiredSpeed = (Math.abs(pcPulseStepRBV) / triggerPeriod)*zebraMotorInfoProvider.getConstantVelocitySpeedFactor();
				break;
			case Zebra.PC_MODE_TIME:
				zebra.setPCTimeUnit(Zebra.PC_TIMEUNIT_MS); //s
				zebra.setPCPulseWidth(.1); //.01ms
				pcPulseWidthRBV = zebra.getPCPulseWidthRBV()/1000;
				zebra.setPCPulseStep(triggerPeriod*1000); // in  ms

				pcPulseStepRBV = zebra.getPCPulseStepRBV()/1000;
				
				double gateWidthTime = pcPulseDelayRBV +  pcPulseStepRBV*(getNumberTriggers()-1) + pcPulseWidthRBV;
				requiredSpeed = (Math.abs(step)/pcPulseStepRBV);
				zebra.setPCGateWidth((gateWidthTime * requiredSpeed));
				
				pcGateWidthRBV = zebra.getPCGateWidthRBV();
				pcGateStartRBV = zebra.getPCGateStartRBV();
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
					scannableMotor.moveTo(pcGateStartRBV + (pcGateWidthRBV*(step > 0? 1: -1)+(step>0 ? 1.0 : -1.0)*zebraMotorInfoProvider.distanceToAccToVelocity(requiredSpeed))); 
					
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
		logger.info("isMoving");

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

	public double getPcPulseDelay() {
		return pcPulseDelay;
	}

	public void setPcPulseDelay(double pcPulseDelay) {
		this.pcPulseDelay = pcPulseDelay;
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
		//ensure the callables have all been called
		boolean done=true;
		if( timeSeriesCollection != null){
			for( ZebraCaptureInputStreamCollection ts : timeSeriesCollection){
				done &= ts.isComplete();
			}
		}
		if(!done)
			throw new DeviceException("stopAndReset called before all callables have been processed");
		lastImageNumberStreamIndexer = new PositionStreamIndexer[11];
		timeSeriesCollection = null;
		moveFuture=null;
		numPosCallableReturned=0;
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
}
