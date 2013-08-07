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

package uk.ac.gda.devices.vgscienta;


import gda.device.DeviceException;
import gda.device.detector.addetector.triggering.SimpleAcquire;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.impl.ADBaseImpl;
import gda.epics.connection.EpicsController;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlexibleFrameStrategy extends SimpleAcquire implements MonitorListener, IObservable {
	static final Logger logger = LoggerFactory.getLogger(FlexibleFrameStrategy.class);

	private ObservableComponent oc = new ObservableComponent();
	
	private int maxNumberOfFrames = 1;
	private int currentFrame = -1;
	private int highestFrame = 0;
	private boolean wethinkweareincharge = false;
	
	private NDProcess proc;
	private EpicsController epicsController;
	
	public FlexibleFrameStrategy(ADBase base, double time, NDProcess ndProcess) throws CAException, InterruptedException, TimeoutException {
		super(base, time);
		proc = ndProcess;
		epicsController = EpicsController.getInstance();
		epicsController.setMonitor(epicsController.createChannel(((ADBaseImpl) getAdBase()).getBasePVName() + ADBase.ArrayCounter_RBV), this);
	}

	@Override
	public void collectData() throws Exception {
		getAdBase().setArrayCounter(0);
		proc.getPluginBase().setArrayCounter(0);
		proc.setResetFilter(1);
		currentFrame = 0;
		highestFrame = 0;
		wethinkweareincharge = true;
		interactWithDeviceIfRequired();
		getAdBase().startAcquiring();
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		if (wethinkweareincharge) {
			if (arg0.getDBR() instanceof DBR_Int) {
				currentFrame = ((DBR_Int) arg0.getDBR()).getIntValue()[0];
				interactWithDeviceIfRequired();
				logger.debug(String.format("processed updates for frame %d.",currentFrame));
			}
		}
	}
	
	private void interactWithDeviceIfRequired() {
		if (!wethinkweareincharge)
			return;
		
		try {
			if (currentFrame == (maxNumberOfFrames - 1)) {
					getAdBase().setImageMode(0);
			} else if (currentFrame >= maxNumberOfFrames) {
					completeCollection();
			} else {
					getAdBase().setImageMode(2);
			}
		} catch (Exception e) {
			logger.error("Exception received controlling analyser exposure, sweeps out of control!", e);
		}
		
		notifyObservers();
	}
	
	@Override
	public void completeCollection() throws Exception {
		if (!wethinkweareincharge) 
			return;
		wethinkweareincharge = false;
		highestFrame = currentFrame;
		currentFrame = -1;
		super.completeCollection();
		notifyObservers();
	}
	
	public int getMaxNumberOfFrames() {
		return maxNumberOfFrames;
	}

	public void setMaxNumberOfFrames(int maxNumberOfFrames) {
		if (maxNumberOfFrames < 1)
			throw new IllegalArgumentException("must collect at least one frame");
		if (maxNumberOfFrames < currentFrame)
			throw new IllegalArgumentException("cannot reduce number of frames when I already collected more");
		this.maxNumberOfFrames = maxNumberOfFrames;
		interactWithDeviceIfRequired();
	}
	
	@Override
	public double getAcquireTime() throws Exception {
		return super.getAcquireTime() * proc.getNumFiltered_RBV();
	}

	public int getLastAcquired() throws Exception {
		return proc.getNumFiltered_RBV();
	}
	
	public int getCurrentFrame() {
		return currentFrame;
	}

	@Override
	public void addIObserver(IObserver observer) {
		oc.addIObserver(observer);		
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		oc.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		oc.deleteIObservers();		
	}
	
	private void notifyObservers() {
		oc.notifyIObservers(this, new FrameUpdate(currentFrame, maxNumberOfFrames));
	}
	
	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		super.waitWhileBusy();
		// at this point we should be stopped, but might not have processed the last frame/sweep
		if (currentFrame > highestFrame)
			highestFrame = currentFrame;
		try {
			int number = 0;
			while(proc.getNumFiltered_RBV() < highestFrame || proc.getPluginBase().getArrayCounter_RBV() < highestFrame) {
				Thread.sleep(25);
				logger.debug(String.format("waiting (%d)",number));
				if (number++ > 40*30) {
					throw new DeviceException("timout waiting for IOC processing");
				}
			}
			logger.info(String.format("At the end of waiting we have: highestFrame: %d,  numFilter %d, arrayCounterRBV %d", highestFrame, proc.getNumFiltered_RBV(), proc.getPluginBase().getArrayCounter_RBV()));
		} catch (InterruptedException e) {
			throw e;
		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException("error waiting for IOC processing", e);
		}
	}
}