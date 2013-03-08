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

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.continuouscontroller.ConstantVelocityMoveController;
import gda.device.scannable.PositionStreamIndexer;
import gda.device.scannable.ScannableUtils;
import gda.device.zebra.controller.Zebra;
import gda.factory.FactoryException;
import gda.scan.ScanBase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ZebraConstantVelocityMoveController extends DeviceBase implements ConstantVelocityMoveController,
		InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(ZebraConstantVelocityMoveController.class);

	double pcPulseDelay = 0.01;
	short pcCaptureBitField = 1;
	Zebra zebra;
	short pcEnc = 0;

	ZebraScannableMotor zSM;

	private double pcGateWidthRBV;

	private double pcGateStartRBV;

	private int mode=Zebra.PC_MODE_POSITION;


	@Override
	public void prepareForMove() throws DeviceException, InterruptedException {
		try {
			logger.info("prepare for move");

			boolean operatingContinously = zSM.isOperatingContinously();
			try {
				zebra.pcDisarm();
				//if we want to check it is disarmed we will need to wait >2s as that is the zebra bus update period

				if (operatingContinously)
					zSM.setOperatingContinuously(false);
				zSM.asynchronousMoveTo(start - step); //TODO desiredSpeed*desiredSpeed/(2*acceleration)

				//sources must be set first
				zebra.setPCGateSource(step > 0 ? 0 : 3);// Posn +ve/-ve
				zebra.setPCArmSource(0);// Soft
				
				zebra.setPCPulseSource(mode);// Position 

				//set motor before setting gates and pulse parameters
				zebra.setPCEnc(pcEnc); // enc1
				zebra.setPCTimeUnit(Zebra.PC_TIMEUNIT_SEC); //s
				
				zebra.setPCGateStart(start);
				zebra.setPCGateNumberOfGates(1);
				
				
				zebra.setPCCaptureBitField(pcCaptureBitField);

				zebra.setPCPulseDelay(0.);
				zebra.setPCPulseWidth(Math.abs(step/2));
				pcPulseDelayRBV = zebra.getPCPulseDelayRBV();
				pcPulseWidthRBV = zebra.getPCPulseWidthRBV();

				switch(mode){
				case Zebra.PC_MODE_POSITION:
					zebra.setPCPulseStep(Math.abs(step));

					pcPulseStepRBV = zebra.getPCPulseStepRBV();
					
					double gateWidthPosn = pcPulseDelayRBV +  pcPulseStepRBV*(getNumberTriggers()-1) + pcPulseWidthRBV;
					
					zebra.setPCGateWidth(gateWidthPosn);
					
					pcGateWidthRBV = zebra.getPCGateWidthRBV();
					pcGateStartRBV = zebra.getPCGateStartRBV();
					requiredSpeed = (Math.abs(pcPulseStepRBV) / triggerPeriod)*zSM.getConstantVelocitySpeedFactor();
					break;
				case Zebra.PC_MODE_TIME:
					zebra.setPCPulseStep(triggerPeriod+.01); //TODO plus readout time

					pcPulseStepRBV = zebra.getPCPulseStepRBV();
					
					double gateWidthTime = pcPulseDelayRBV +  pcPulseStepRBV*(getNumberTriggers()-1) + pcPulseWidthRBV;
					requiredSpeed = (Math.abs(step)/pcPulseStepRBV);
					zebra.setPCGateWidth((gateWidthTime * requiredSpeed)/zSM.getConstantVelocitySpeedFactor());
					
					pcGateWidthRBV = zebra.getPCGateWidthRBV();
					pcGateStartRBV = zebra.getPCGateStartRBV();
					break;
				default:
					throw new DeviceException("Unacceptable mode " + mode);
				}
				
				zSM.waitWhileBusy();
			
			} finally {
				zSM.setOperatingContinuously(operatingContinously);
			}

			zebra.pcArm();
			int numberTriggers = getNumberTriggers();
			//get number of triggers the zebra is expected to generate
/*			double pcGateWidth = zebra.getPCGateWidth();
			int num = (int) ((pcGateWidth-pcPulseDelayRBV)/(pcPulseStepRBV) + 0.5 +1);
			if( num < numberTriggers || num < numPosCallableReturned){
				throw new DeviceException("inconsistent pulse numbers numZebra:" + num + " num(start,end,step):" + numberTriggers + " numPosCallableReturned:"+numPosCallableReturned);
			}
*/			timeSeriesCollection.start(numberTriggers);

		} catch (Exception e) {
			throw new DeviceException("Error arming the zebra", e);
		}

	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public class ExecuteMoveTask implements Callable<Void> {
		@Override
		public Void call() throws DeviceException, InterruptedException {
			try {
				double speed = zSM.getSpeed();
				boolean operatingContinously = zSM.isOperatingContinously();
				try {
					if (operatingContinously)
						zSM.setOperatingContinuously(false);
					zSM.setSpeed(requiredSpeed);
					zSM.moveTo(pcGateStartRBV + (pcGateWidthRBV*(step > 0? 1: -1))); 
					
				} finally {
					zSM.setSpeed(speed);
					zSM.setOperatingContinuously(operatingContinously);
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

	public PositionStreamIndexer<Double> lastImageNumberStreamIndexer;

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
			boolean complete=false;
			while( !done || !complete){
				done = moveFuture.isDone();
				complete = timeSeriesCollection.isComplete();
				ScanBase.checkForInterrupts();
			}
		} catch (InterruptedException e) {
			zSM.stop();
			throw e;
		} finally{
			moveFuture=null;
		}
	}

	@Override
	public void stopAndReset() throws DeviceException, InterruptedException {
		logger.info("stopAndReset");
		
	}

	@Override
	public void setTriggerPeriod(double seconds) throws DeviceException {
		logger.info("setTriggerPeriod");
		triggerPeriod = seconds; //readout need to use readout time;

	}

	@Override
	public int getNumberTriggers() {
		logger.info("getNumberTriggers");
		try {
			return ScannableUtils.getNumberSteps(zSM, new Double(start),new Double(end),new Double(step))+1;
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
		logger.info("setStart"); 
		this.start = start;
	}

	@Override
	public void setEnd(double end) throws DeviceException {
		logger.info("setEnd");
		this.end = end;
	}

	@Override
	public void setStep(double step) throws DeviceException {
		logger.info("setStep");
		this.step = step;

	}

	// copied from EpicsTrajectoryMoveControllerAdapter - need a base class
	List<Double> points = null;

	public ZebraCaptureInputStreamCollection timeSeriesCollection;

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

	public ZebraScannableMotor getzSM() {
		return zSM;
	}

	public void setzSM(ZebraScannableMotor zSM) {
		this.zSM = zSM;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (zebra == null)
			throw new Exception("zebra is not set");
	}

	int numPosCallableReturned = 0;

	private double pcPulseDelayRBV;

	private double pcPulseWidthRBV;

	private double pcPulseStepRBV;

	private double requiredSpeed;
	public Callable<Double> getPositionCallable() {
		if( lastImageNumberStreamIndexer == null){
			logger.info("Creating lastImageNumberStreamIndexer");
			timeSeriesCollection = new ZebraCaptureInputStreamCollection(zebra.getNumberOfPointsCapturedPV(),
					zebra.getEnc1AvalPV());
			lastImageNumberStreamIndexer = new PositionStreamIndexer<Double>(timeSeriesCollection);
		}
		numPosCallableReturned++;
		return lastImageNumberStreamIndexer.getNamedPositionCallable(zSM.getName(),1);
	}

	public void atScanLineStart() {
		lastImageNumberStreamIndexer=null;
		points = null;
		moveFuture=null;
		numPosCallableReturned=0;
	}

}
