/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DAServer;

/**
 * Adapter for the TfgScalerWithLogValues class so that it may be used in ContinuousScans.
 * <p>
 * Assumes that the TTL signal driving the TFG time-frames is connected to TFG socket TTL INPUT 0.
 */
public class BufferedScaler extends TfgScalerWithLogValues implements BufferedDetector {
	private static final Logger logger = LoggerFactory.getLogger(BufferedScaler.class);
	private ContinuousParameters parameters;
	private boolean continuousMode;
	private double overrunTime = 0.1;
	private DAServer daserver;
	private int ttlSocket = 0; // the TTL Trig In socket [0-3] default is 0
	private Boolean returnCountRates = false;
	private double[][] framesRead;
	private int numCycles = 1;
	private boolean useInternalTriggeredFrames = false;
	private double frameDeadTime = 1e-6; // Frame dead time (seconds)
	private boolean manualStart = false;

	public BufferedScaler(){
		try {
			framesRead = new double[getNumberFrames()][5];
		} catch (DeviceException e) {
			logger.error("Cannot construct BufferedScaler, something wrong with TFG", e);
		}
	}

	public double[][] getFramesRead() {
		return framesRead;
	}

	public void clearFramesRead(){
		framesRead = null;
	}

	@Override
	public void collectData() throws DeviceException {
		// do nothing when in continuous mode
		if (continuousMode){
			return;
		}
		super.collectData();
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		Integer lastFrame = getNumberFrames() - 1;
		return readoutCorrectedFrames(0, lastFrame);
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		double[][] frame = readoutCorrectedFrames(startFrame, finalFrame);
		framesRead=frame;
		return frame;
	}

	@Override
	public double[] readout() throws DeviceException {
		if (continuousMode){
			Integer numFrames = getNumberFrames();
			// if nothing collected, so 0 frames, then view the contents of the first frame anyway
			if (numFrames == 0) {
				return readoutCorrectedFrames(0, 0)[0];
			}
			return readoutCorrectedFrames(numFrames -1 , numFrames -1)[0];
		}
		return readoutCorrectedFrames(0,0)[0];
	}

	private double[][] readoutCorrectedFrames(int startFrame, int finalFrame) throws DeviceException {
		// make sure performCorrections from TfgScalerWithLogValues is called on every frame
		double[][] frames = readoutFrames(startFrame, finalFrame);
		for (int frame = 0; frame < frames.length; frame++){
			frames[frame] = performCorrections(frames[frame]);
		}
		return frames;
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		this.continuousMode = on;
		if (on) {
			if (useInternalTriggeredFrames) {
				setTimeFramesInternal();
			} else {
				setTimeFrames();
			}
		} else {
			switchOffExtTrigger();
			// set to false, to avoid potential problems with subsequent scans that rely on external
			// triggering but don't set this (new) flag to false
			useInternalTriggeredFrames = false;
		}
	}

	/**
	 * Setup Tfg to generate sequence of time frames (internal trigger, starting immediately)
	 *
	 * @throws DeviceException
	 */
	private void setTimeFramesInternal() throws DeviceException {
		if (parameters == null) {
			throw new DeviceException(getName() + " could not set time frames for continuous scans as parameters not supplied!");
		}
		double timePerPoint = parameters.getTotalTime() / parameters.getNumberDataPoints();
		logger.debug("Setting da.server to generate {} time frames with {} sec per point", parameters.getNumberDataPoints(), timePerPoint);

		StringBuilder buffer = new StringBuilder();
		buffer.append("tfg setup-groups cycles 1\n");
		buffer.append(parameters.getNumberDataPoints() + " " + frameDeadTime +" " + timePerPoint + " 0 0 0 0\n");
		buffer.append("-1 0 0 0 0 0 0");

		daserver.sendCommand(buffer.toString());
		if (!manualStart) {
			startTfg();
		}
	}

	public void startTfg() throws DeviceException {
		daserver.sendCommand("tfg start");
	}

	public void setManualStart(boolean manualStart) {
		this.manualStart = manualStart;
	}

	public boolean isManualStart() {
		return manualStart;
	}

	private void setTimeFrames() throws DeviceException {
		if (parameters == null) {
			throw new DeviceException(getName()
					+ " could not set time frames for continuous scans as parameters not supplied!");
		}

		// tfg setup-trig
		switchOnExtTrigger();

		//Send as a single command. Otherwise DAServer reply timeouts are seen and the 3 commands take about 10s!
		StringBuilder buffer = new StringBuilder();
		buffer.append("tfg setup-groups ext-start cycles "+numCycles+"\n");
		buffer.append(parameters.getNumberDataPoints() + " " + frameDeadTime + " 0.00000001 0 0 0 " + (ttlSocket + 8)+"\n");
		buffer.append("-1 0 0 0 0 0 0");
		// FIXME RJW on qexafs that fail, especially where # points < previous scan, why are # frames incorrect?
		logger.debug("Setting da.server to arm itself for " + parameters.getNumberDataPoints() + " time frames");
		daserver.sendCommand(buffer.toString());
		daserver.sendCommand("tfg arm");
	}

	@Override
	public boolean isContinuousMode() {
		return continuousMode;
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return parameters;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public void clearMemory() throws DeviceException {
		scaler.clear();
		scaler.start();
	}

	/**
	 * switch off external triggering by the TTL0 input
	 * @throws DeviceException
	 */
	private void switchOffExtTrigger() throws DeviceException {
		daserver.sendCommand("tfg setup-trig start");
	}

	/**
	 * switch on external triggering by the TTL0 input
	 * @throws DeviceException
	 */
	private void switchOnExtTrigger() throws DeviceException {
		daserver.sendCommand("tfg setup-trig start ttl" + ttlSocket);
	}

	/**
	 * @return Return true if tfg is armed and waiting for an external trigger
	 * @throws DeviceException
	 */
	public boolean isWaitingForTrigger() throws DeviceException {
		String armedState = daserver.sendCommand("tfg read status show-armed").toString();
		return "EXT-ARMED".equals(armedState);
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		if (!continuousMode) {
			return 0;
		}

		if (isWaitingForTrigger()) {
			return 0;
		}

		String currentNumFramesString = daserver.sendCommand("tfg read frame").toString();

		// if frame is non-0 then work out the current frame
		if (!currentNumFramesString.equals("0")) {
			try{
				return extractCurrentFrame(Integer.parseInt(currentNumFramesString));
			}
			catch(NumberFormatException e){
				throw new DeviceException(currentNumFramesString);
			}
		}

		return parameters.getNumberDataPoints();
	}

	private int extractCurrentFrame(int frameValue){
		if (isEven(frameValue)) {
			Integer numFrames = frameValue / 2;
			return numFrames;
		}
		Integer numFrames = (frameValue - 1) / 2;
		return numFrames;
	}

	private boolean isEven(int x) {
		return (x % 2) == 0;
	}

	public void setOverrunTime(double overrunTime) {
		this.overrunTime = overrunTime;
	}

	/**
	 * The excess time to collect data for above the movement time. This ensures all motor encoder data has been read.
	 * <p>
	 * Default is 0.1;
	 *
	 * @return double time in seconds
	 */
	public double getOverrunTime() {
		return overrunTime;
	}

	public DAServer getDaserver() {
		return daserver;
	}

	public void setDaserver(DAServer daserver) {
		this.daserver = daserver;
	}

	public void setReturnCountRates(Boolean returnCountRates) {
		if (!timeChannelRequired && returnCountRates){
			timeChannelRequired = true;
			extraNames = (String[]) ArrayUtils.addAll(new String[]{"time"}, this.extraNames);
			outputFormat = (String[]) ArrayUtils.addAll(new String[]{this.outputFormat[0]}, this.outputFormat);
		} else if (timeChannelRequired && this.returnCountRates && !returnCountRates){
			timeChannelRequired = false;
			extraNames = (String[]) ArrayUtils.remove(this.extraNames, 0);
			outputFormat = (String[]) ArrayUtils.remove(this.outputFormat, 0);
		}
		this.returnCountRates = returnCountRates;
	}

	public Boolean getReturnCountRates() {
		return returnCountRates;
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		// as this is only a few integers per frame, its unlikely to ever cause memory issues.
		return 99999;
	}

	public int getTtlSocket() {
		return ttlSocket;
	}

	public void setTtlSocket(int ttlSocket) {
		this.ttlSocket = ttlSocket;
	}

	public int getNumCycles() {
		return numCycles;
	}

	public void setNumCycles(int numCycles) {
		this.numCycles = numCycles;
	}

	/**
	 * Return cycle currently being executed by Tfg by parsing the value of the Cycle field from
	 * the Tfg 'progress' string.
	 * (Note: Parent class implementation returns current lap which is different to current cycle).
	 * return
	 */
	@Override
	public int getCurrentCycle() throws DeviceException {
		String result = daserver.sendCommand("tfg read progress").toString();
		if (result.equals("IDLE")) {
			return 0;
		}
		// Parse da.server progress string and extract cycle number
		// (Typical da.server progress string : "RUNNING: Cycle=   2, Frame=3277, LIVE, Time 13.2771 of 15.0002")

		// Remove , and = characters from da.server string...
		result = result.replace("=", " ").replace(",", " ");
		// split into array at one or more whitespace chars
		String[] splitString = result.split("\\s+");

		String cycle = splitString.length>2 ? splitString[2]:null;

		int cycleNumber=0;
		try {
			cycleNumber = Integer.parseInt(cycle);
		} catch(NumberFormatException nfe) {
			logger.debug("Problem parsing current cycle number from da.server string {}", result);
		}
		return cycleNumber;
	}

	/**
	 * Clear specified block of scaler memory. Intended to be used with multiple cycles, so that
	 * frames of scaler memory used for a cycle can be cleared once they have been read,
	 * ready to be re-used on subsequent cycles.
	 * @param startFrame
	 * @param finalFrame
	 * @throws DeviceException
	 * @author Iain Hall
	 * @since 25/1/2017
	 */
	public void clearMemoryFrames(int startFrame, int finalFrame) throws DeviceException {
		/** numFrames needs be 1 less here than in {@link TfgScaler#readoutFrames(startFrame,finalFrame)}, otherwise 1 too many frames are cleared. */
		int numFrames = finalFrame-startFrame;
		scaler.clear(startFrame, 0, 0, numFrames, 1, 9);
	}

	public boolean getUseInternalTriggeredFrames() {
		return useInternalTriggeredFrames;
	}

	/**
	 * Set to 'true' to make the Tfg to generate time frames without waiting for external triggers. <p>
	 * N.B. This flag gets reset to false at end of each scan, to avoid potential problems with subsequent
	 * scans that rely on external triggering but don't set this (new) flag to false.
	 * @param useInternalTriggeredFrames
	 */
	public void setUseInternalTriggeredFrames(boolean useInternalTriggeredFrames) {
		this.useInternalTriggeredFrames = useInternalTriggeredFrames;
	}

	public double getFrameDeadTime() {
		return frameDeadTime;
	}

	/**
	 * Set the dead frame time. This is the gap to use between adjacent timeframes in sequence of externally
	 * or internally triggered frames.
	 * @param frameDeadTime
	 */
	public void setFrameDeadTime(double frameDeadTime) {
		this.frameDeadTime = frameDeadTime;
	}
}

