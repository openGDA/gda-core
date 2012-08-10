/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.countertimer;

import gda.device.DeviceException;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.detector.DAServer;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetector;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionInputStream;
import gda.device.scannable.PositionStreamIndexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardwareTriggeredScaler extends TfgScalerWithLogValues implements HardwareTriggerableDetector, 
PositionCallableProvider<double[]> {
	static final Logger logger = LoggerFactory.getLogger(HardwareTriggeredScaler.class);
	private HardwareTriggerProvider triggerProvider;
	private DAServer daserver;
	private int ttlSocket = 0;
	private boolean hardwareTriggering =true;

	private PositionStreamIndexer<double[]> indexer;
	
	public void setHardwareTriggerProvider(HardwareTriggerProvider triggerProvider) {
		this.triggerProvider = triggerProvider;
	}

	@Override
	public HardwareTriggerProvider getHardwareTriggerProvider() {
		return triggerProvider;
	}

	@Override
	public void setHardwareTriggering(boolean b) throws DeviceException {
		hardwareTriggering = b;		
		if (!hardwareTriggering) {
			switchOffExtTrigger();
		}
	}

	@Override
	public boolean isHardwareTriggering() {
		return hardwareTriggering;
	}

	@Override
	public void arm() throws DeviceException {
		super.start();		
		setTimeFrames();
		
	}

	@Override
	public boolean integratesBetweenPoints() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void collectData() throws DeviceException {
		if (!isHardwareTriggering()) {
			super.collectData();
		}
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		if (!isHardwareTriggering()) {
			super.prepareForCollection();
		}
	}
	@Override
	public void atScanLineStart()throws DeviceException
	{
		this.indexer  = new PositionStreamIndexer<double[]>(new TfgInputStream());
	}
	
	private void setTimeFrames() throws DeviceException {

		// tfg setup-trig
		switchOnExtTrigger();						
		//Send as a single command. Otherwise DAServer reply timeouts are seen and the 3 commands take about 10s!
		StringBuffer buffer = new StringBuffer();
		buffer.append("tfg setup-groups ext-start cycles 1"+"\n");
		buffer.append(triggerProvider.getNumberTriggers() + " 0.000001 0.00000001 0 0 0 " + ttlSocket + 8+"\n");
		buffer.append("-1 0 0 0 0 0 0");
		daserver.sendCommand(buffer.toString());
		daserver.sendCommand("tfg arm");
	}
	/**
	 * switch off external triggering by the TTL0 input
	 */
	private void switchOffExtTrigger() {
		daserver.sendCommand("tfg setup-trig start");
	}

	/**
	 * switch on external triggering by the TTL0 input
	 */
	private void switchOnExtTrigger() {
		daserver.sendCommand("tfg setup-trig start ttl" + ttlSocket);
	}

	@Override
	public Callable<double[]> getPositionCallable() throws DeviceException {
		return indexer.getPositionCallable();
	}
	public int getNumberFrames() throws DeviceException {
		if (!isHardwareTriggering()){ 
			return 0;
		}
		
		String[] cmds = new String[]{"status show-armed","progress","status","full","lap","frame"};
		HashMap <String,String> currentVals = new HashMap<String,String>();
		for (String cmd : cmds){
			currentVals.put(cmd, daserver.sendCommand("tfg read " + cmd).toString());
			logger.info("tfg read "+ cmd + ": " + currentVals.get(cmd));
		}
		
		if (currentVals.isEmpty()){
			return 0;
		}
		
		
		// else either scan not started (return -1) or has finished (return continuousParameters.getNumberDataPoints())
		
		// if started but nothing collected yet
		if (currentVals.get("status show-armed").equals("EXT-ARMED") /*&& currentVals.get("status").equals("IDLE")*/ ){
			return 0;
		}

		// if frame is non-0 then work out the current frame
		if (!currentVals.get("frame").equals("0")){
			String numFrames = currentVals.get("frame");
			try{
			return extractCurrentFrame(Integer.parseInt(numFrames));
			}
			catch(NumberFormatException e){
				throw new DeviceException(numFrames);
			}
		}

		return triggerProvider.getNumberTriggers();
	}


	private boolean isEven(int x) {
		return (x % 2) == 0;
	}
	private int extractCurrentFrame(int frameValue){
		if (isEven(frameValue)) {
			Integer numFrames = frameValue / 2;
			return numFrames;
		}
		Integer numFrames = (frameValue - 1) / 2;
		return numFrames;
	}
	class TfgInputStream implements PositionInputStream<double[]>
	{

		private int readSoFar =0;
		@Override
		public List<double[]> read(int maxToRead) throws NoSuchElementException, InterruptedException,
				DeviceException {
			int totalToRead =  getNumberFrames();
			
			List<double[]> container = new ArrayList<double[]>();
			for(int i =readSoFar  ; i <  totalToRead; i++)
			{
				container.add((HardwareTriggeredScaler.super.readFrame(i)));
			}
			readSoFar = totalToRead;
			return container;
		}
	
	}
}
