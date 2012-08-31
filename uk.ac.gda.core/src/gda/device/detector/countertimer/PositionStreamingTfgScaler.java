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
import gda.device.detector.DAServer;
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

public class PositionStreamingTfgScaler extends TfgScalerWithLogValues implements
		PositionCallableProvider<double[]>, PositionInputStream<double[]> {
	private static Logger logger = LoggerFactory.getLogger(PositionStreamingTfgScaler.class);

	protected DAServer daServer = null;
	private PositionStreamIndexer<double[]> indexer;
	private Double[] times;
	private int nextFrameToRead = 0;
	
	public Double[] getTimes() {
		return times;
	}

	public void setTimes(Double[] times) {
		this.times = times;
	}

	public DAServer getDaServer() {
		return daServer;
	}

	public void setDaServer(DAServer daServer) {
		this.daServer = daServer;
	}

	@Override
	public List<double[]> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		List<double[]> listOfTress = new ArrayList<double[]>();
		if (timer.getAttribute("TotalFrames").equals(0)) {
			listOfTress.add(readout());
			return listOfTress;
		}
		int highestFrameNumAvailable = getNumberFrames() - 1;
		if (highestFrameNumAvailable < nextFrameToRead) {
			highestFrameNumAvailable = nextFrameToRead;
		}
		logger.info("readout from " + nextFrameToRead + " to " + highestFrameNumAvailable);
		
		double[][] frames = readoutFrames(nextFrameToRead,highestFrameNumAvailable);
		for (double[] frame : frames){
			double[] corrected = performCorrections(frame);
			listOfTress.add(corrected);
		}
		nextFrameToRead = highestFrameNumAvailable + 1;
		return listOfTress;
	}

	@Override
	public Callable<double[]> getPositionCallable() throws DeviceException {
		return indexer.getPositionCallable();
	}
	
	@Override
	public void atScanLineStart() throws DeviceException {

		timer.clearFrameSets();
		if (times != null && times.length > 0) {
			// create the time frames here
			for (int i = 0; i < times.length; i++) {
				timer.addFrameSet(1, 0, times[i], 0, 0, -1, 0);
			}
			timer.loadFrameSets();
			daServer.sendCommand("tfg arm");
			daServer.sendCommand("tfg start");
			nextFrameToRead = 0;
		}
		indexer = new PositionStreamIndexer<double[]>(this);
		super.atScanLineStart();
	}
	
	@Override
	public void atScanLineEnd() throws DeviceException {
		indexer = null;
		times = new Double[0];
		super.atScanLineEnd();
	}
	
	private int getNumberFrames() throws DeviceException {
		
		String[] cmds = new String[]{"status show-armed","progress","status","full","lap","frame"};
		HashMap <String,String> currentVals = new HashMap<String,String>();
		for (String cmd : cmds){
			currentVals.put(cmd, daServer.sendCommand("tfg read " + cmd).toString());
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

		return Integer.parseInt(timer.getAttribute("TotalFrames").toString());
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
}