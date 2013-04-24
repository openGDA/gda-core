/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.observable.IObserver;
import gda.scan.Trajectory;
import gda.scan.TrajectoryScanController;
import gda.scan.TrajectoryScanController.BuildStatus;
import gda.scan.TrajectoryScanController.ExecuteStatus;
import gda.scan.TrajectoryScanController.ReadStatus;
import gda.scan.TrajectoryScanController.TrajectoryScanProperty;
import gda.util.OutOfRangeException;
import gov.aps.jca.TimeoutException;

//TODO EpicsTrajectoryScannable2 is a terrible name. How does it differ to EpicsTrajectoryScannable??
public class EpicsTrajectoryScannable2 extends ScannableMotionUnitsBase implements ContinuouslyScannable, IObserver {

//	private static final Logger logger = LoggerFactory.getLogger(EpicsTrajectoryScannable2.class);
	@SuppressWarnings("unused")
	private static final int ONEDMODE = 1;
	private static final int TWODMODE = 2;
	protected TrajectoryScanController tracController;
	private ContinuousParameters continuousParameters;
	@SuppressWarnings("unused")
	private int actualPulses;
	@SuppressWarnings("unused")
	private double[] xpositions;
	protected boolean trajMoveComplete;
	private double[] scannablePositions;
	private int mode;
	private boolean trajectoryBuildDone;

	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	@Override
	public void configure() throws FactoryException {
	
		tracController = (TrajectoryScanController)Finder.getInstance().find("epicsTrajectoryScanController");
		tracController.addIObserver(this);
		this.setInputNames(new String[]{this.getName()});
	}
	@Override
	public void continuousMoveComplete() throws DeviceException {
		try {
			tracController.read();
			while (tracController.isReading()){ 
				Thread.sleep(1);
			}
			//check the read status from the controller
			if (tracController.getReadStatus() != ReadStatus.SUCCESS ){
				throw new DeviceException("Unable to get the execution status from the Trajectory Controller");
			}
			actualPulses = tracController.getActualPulses();
			//get the actual xpositions
			xpositions = tracController.getMActual(3);
		} catch (TimeoutException e) {
			throw new DeviceException(getName() + " exception in continuousMoveComplete",e);
		} catch (InterruptedException e) {
			throw new DeviceException(getName() + " exception in continuousMoveComplete",e);
		}
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return continuousParameters;
	}

	@Override
	public void performContinuousMove() throws DeviceException {
		trajMoveComplete = false;
		try {
			tracController.execute();
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in performContinuousMove",e);
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		try {
			return tracController.isBusy();
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in isBusy",e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			tracController.stop();
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in stop",e);
		}
		super.stop();
	}

	
	@Override
	public int prepareForContinuousMove() throws DeviceException {
		// build the trajectory
		if(mode == TWODMODE && trajectoryBuildDone)
			return continuousParameters.getNumberDataPoints();
		
		try {
			for(int i =1 ; i <= TrajectoryScanController.MAX_TRAJECTORY; i++)
			{	
				tracController.setMMove(i, false);				
			}
			tracController.setMMove(3, true);
			Trajectory trajectory = new Trajectory();
			trajectory.setTotalElementNumber(continuousParameters.getNumberDataPoints() + 1);
			trajectory.setTotalPulseNumber(continuousParameters.getNumberDataPoints()+ 1 );
			double timePerPoint = continuousParameters.getTotalTime() / continuousParameters.getNumberDataPoints();
			double[] path = trajectory.defineCVPath(continuousParameters.getStartPosition(), 
					continuousParameters.getEndPosition(), (continuousParameters.getTotalTime() + timePerPoint));
			
			tracController.setMTraj(3,path);
			
			tracController.setNumberOfElements((int)trajectory.getElementNumbers());
			tracController.setNumberOfPulses((int) trajectory.getPulseNumbers());
			tracController.setStartPulseElement((int) trajectory.getStartPulseElement());
			tracController.setStopPulseElement((int) trajectory.getStopPulseElement());

			if (tracController.getStopPulseElement() != (int) (trajectory.getStopPulseElement())){
				tracController.setStopPulseElement((int) (trajectory.getStopPulseElement()));	
			}
			tracController.setTime((trajectory.getTotalTime()));
   
			
			tracController.build();
			while(tracController.isBuilding()){
				//wait for the build to finsih
				Thread.sleep(30);
			}
			//check the build status from epics
			if(tracController.getBuildStatus() != BuildStatus.SUCCESS){
				throw new DeviceException("Unable to build the trajectory with the given start and stop positions and the time");
			}
			trajectoryBuildDone = true;
			return continuousParameters.getNumberDataPoints();
		} catch (TimeoutException e) {
			throw new DeviceException(getName() + " exception in continuousMoveComplete",e);
		} catch (InterruptedException e) {
			throw new DeviceException(getName() + " exception in continuousMoveComplete",e);
		} catch (OutOfRangeException e) {
			throw new DeviceException(getName() + " exception in continuousMoveComplete",e);
		}
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		this.continuousParameters = parameters;

	}
	@Override
	//TO DO discuss with FY
	public void update(Object source, Object arg) {
		if(source instanceof TrajectoryScanProperty)
		{
			if(((TrajectoryScanProperty)source) == TrajectoryScanProperty.EXECUTE )
			{
				if(arg instanceof ExecuteStatus)
				{
					if(((ExecuteStatus)arg) == ExecuteStatus.SUCCESS)
					{
						trajMoveComplete = true;
					}
					else
					{
						trajMoveComplete = false;
					}
							
				}
			}
		}
		
	}
	@Override
	public double calculateEnergy(int frameIndex) {
		double stepSize = (continuousParameters.getEndPosition() -continuousParameters.getStartPosition())/continuousParameters.getNumberDataPoints();
		return (continuousParameters.getStartPosition() + (frameIndex * stepSize));

	}
	@Override
	public Object rawGetPosition() throws DeviceException {
		return tracController.getName();

	}
	@Override
	public void atScanLineEnd() throws DeviceException {
		try {
			scannablePositions = tracController.getMActual(3);
		} catch (TimeoutException e) {
			throw new DeviceException(getName() + " exception in atScanLineEnd",e);
		} catch (InterruptedException e) {
			throw new DeviceException(getName() + " exception in atScanLineEnd",e);
		}

	}
	
	@Override
	public void atScanEnd() throws DeviceException {
		trajectoryBuildDone = false;

	}
	@Override
	public void atCommandFailure() 
	{
		trajectoryBuildDone = false;
	}
	public double[] getScannablePositions() {
		return scannablePositions;
	}
}
