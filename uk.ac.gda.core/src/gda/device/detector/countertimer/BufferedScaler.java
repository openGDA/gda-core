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
import org.apache.commons.lang.StringUtils;
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

	private Integer ttlSocket; // the TTL Trig In socket [0-3] default is 0
	private int deadPause = 0;

	private Boolean returnCountRates = false;
	private double[][] framesRead;
	private int numCycles = 1;

	private boolean externalTriggeredFrames = true;
	private boolean externalTriggerStart = true;

	private double frameDeadTime = 1e-6; // Frame dead time (seconds)
	private double frameLiveTime = 0; // Frame live time (seconds)

	private boolean frameCountDuringCycles = true;
	private String groupInitialCommand; // Command to be included when configuring DaServer at start of timing group, before setting up the frames

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
			setupTimeFrames(externalTriggerStart, externalTriggeredFrames);
			if (!manualStart) {
				startTfg(externalTriggerStart);
			}
		} else {
			switchOffExtTrigger();
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		// set to false, to avoid potential problems with subsequent scans that rely on external
		// triggering but don't set these flags to true
		setUseExternalTriggers(true);
		super.atScanEnd();
	}

	@Override
	public void stop() throws DeviceException {
		setUseExternalTriggers(true);
		super.stop();
	}

	/**
	 * Setup and send the time frame commands to the Tfg. If externalTriggerFrames = true :
	 * <li>each time frame is externally triggered
	 * <li>the dead and live time of each frame is set from user specified values of frameLiveTime and frameDeadTime.
	 * <li>Trigger options (dead and live pause) are from user specified deadPause value, and value of ttlSocket respectively (0 if ttlSocket is null)
	 *
	 * @param externalTriggerStart - set to true to use external trigger can be used to start the Tfg
	 * @param externalTriggerFrames - set to true to use external trigger to trigger each time frame within the group
	 * @throws DeviceException
	 */
	protected void setupTimeFrames(boolean externalTriggerStart, boolean externalTriggerFrames) throws DeviceException {
		if (parameters == null) {
			throw new DeviceException(getName()
					+ " could not set time frames for continuous scans as parameters not supplied!");
		}
		if (externalTriggerStart) {
			switchOnExtTrigger();
		}
		String command = getDaserverCommand(parameters.getNumberDataPoints(), parameters.getTotalTime(), externalTriggerStart, externalTriggerFrames);

		logger.debug("Setting up time frames on da.server");
		daserver.sendCommand(command);
	}

	protected String getDaserverCommand(int numFrames, double totalTime,  boolean externalTriggerStart, boolean externalTriggerFrames) throws DeviceException {
		logger.debug("Generating Tfg time frame commands : externalTriggerStart = {}, externalFrameTrigger = {}", externalTriggerStart, externalTriggerFrames);

		//Send timing group setup as a single command. Otherwise DAServer reply timeouts are seen and the 3 commands take about 10s!
		StringBuilder buffer = new StringBuilder();

		// Set the optional external start and number of cycles
		buffer.append("tfg setup-groups ");
		if (externalTriggerStart) {
			buffer.append("ext-start ");
		}
		buffer.append("cycles "+numCycles+"\n");

		// Time per point
		double liveTime;
		if (externalTriggerFrames) {
			// Time is manually specified if each frame is externally triggered
			liveTime = frameLiveTime;
		} else {
			// Time from the continuous parameters if using internal triggering
			liveTime = totalTime / numFrames;
		}

		if (StringUtils.isNotEmpty(groupInitialCommand)) {
			buffer.append(groupInitialCommand.strip());
			buffer.append("\n");
		}

		if (externalTriggerFrames) {
			int livePause = ttlSocket == null ? 0 : ttlSocket + 8;
			buffer.append(String.format("%d %.4g %.4g 0 0 %d %d \n", numFrames, frameDeadTime, liveTime, deadPause , livePause));
		} else {
			buffer.append(String.format("%d %.4g %.4g 0 0 %d %d \n", numFrames, frameDeadTime, liveTime, 0, 0));
		}
		buffer.append("-1 0 0 0 0 0 0");

		logger.debug("DAserver command : {}", buffer.toString());

		return buffer.toString();
	}

	public void startTfg(boolean externalTrigger) throws DeviceException {
		// Send commmand to arm/start the tfg
		String startCommand = externalTrigger ? "tfg arm" : "tfg start";
		daserver.sendCommand(startCommand);
	}

	public boolean isManualStart() {
		return manualStart;
	}

	public void setManualStart(boolean manualStart) {
		this.manualStart = manualStart;
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
		int triggerPort = ttlSocket == null ? 0 : ttlSocket;
		daserver.sendCommand("tfg setup-trig start ttl" + triggerPort);
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

		if (!frameCountDuringCycles) {
			int currentCycle = getCurrentCycle();
			logger.debug("Current cycle : {} of {}", currentCycle, numCycles);

			// Tfg is in Idle after all cycles have finished --> return total number of points
			if (getStatus() == 0) {
				return parameters.getNumberDataPoints();
			}

			// Wait while cycles are incrementing
			if (currentCycle < numCycles-1) {
				return 0;
			}
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

	/**
	 * Set the TTL trigger input to use for external start and for live pause
	 * (value = 0...3).
	 *
	 * @param ttlSocket
	 */
	public void setTtlSocket(Integer ttlSocket) {
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

	public double getFrameLiveTime() {
		return frameLiveTime;
	}

	public void setFrameLiveTime(double frameLiveTime) {
		this.frameLiveTime = frameLiveTime;
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

	public int getDeadPause() {
		return deadPause;
	}

	/**
	 * Set the dead pause value - used when using externally triggered frames
	 * (e.g. 8 to continue on rising edge of TTL0, 9 for TTL1 etc).
	 *
	 * @param deadPause
	 */
	public void setDeadPause(int deadPause) {
		this.deadPause = deadPause;
	}

	public boolean isExternalTriggeredFrames() {
		return externalTriggeredFrames;
	}

	/**
	 * Set to true to have each time frame triggered by external source
	 * (Controls the dead pause and live pause values -
	 * @param externalTriggeredFrames
	 */
	public void setExternalTriggeredFrames(boolean externalTriggeredFrames) {
		this.externalTriggeredFrames = externalTriggeredFrames;
	}

	public boolean isExternalTriggerStart() {
		return externalTriggerStart;
	}

	/**
	 * Set to true to have the Tfg start when external TTL trigger signal is received.
	 * (TTL input port to use is set with {@link #setTtlSocket(Integer)}).
	 *
	 * @param externalTriggerStart
	 */
	public void setExternalTriggerStart(boolean externalTriggerStart) {
		this.externalTriggerStart = externalTriggerStart;
	}

	public void setUseExternalTriggers(boolean tf) {
		externalTriggerStart = tf;
		externalTriggeredFrames = tf;
	}

	public boolean isFrameCountDuringCycles() {
		return frameCountDuringCycles;
	}

	/**
	 * If set to false, {@link #getNumberFrames()} will return zero while the Tfg is collecting frames across multiple cycles.
	 *
	 * @param frameCountDuringCycles
	 */
	public void setFrameCountDuringCycles(boolean frameCountDuringCycles) {
		this.frameCountDuringCycles = frameCountDuringCycles;
	}

	public String getGroupInitialCommand() {
		return groupInitialCommand;
	}

	public void setGroupInitialCommand(String groupInitialCommand) {
		this.groupInitialCommand = groupInitialCommand;
	}
}

