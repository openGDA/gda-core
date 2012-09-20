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

package gda.device.detector;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.xspress.Xspress2System;
import gda.device.detector.xspress.XspressDetector;

/**
 * To operate the Xspress through TFGv2 in the ContinuousScan framework
 */
public class Xspress2BufferedDetector extends DetectorBase implements BufferedDetector, NexusDetector {

	protected ContinuousParameters continuousParameters = null;
	protected boolean isContinuousMode = false;
	protected Xspress2System xspress2system = null;
	private boolean isSlave = true;
	protected int maxNumberOfInts = 750000; // the maximum number of integers to read out from da.server in each bunch
	private int triggerSwitch = 0;
											// of
	// frames before this causes a problem 80000 = 2 frames of 9-element reading full mca

	public int getTriggerSwitch() {
		return triggerSwitch;
	}

	public void setTriggerSwitch(int triggerSwitch) {
		this.triggerSwitch = triggerSwitch;
	}

	private Object[] framesRead;

	public Xspress2BufferedDetector() {
		try {
			framesRead = new Object[getNumberFrames()];
		} catch (DeviceException e) {
			e.printStackTrace();
		}
	}

	public Object[] getFramesRead() {
		return framesRead;
	}

	public void clearFramesRead() {
		framesRead = null;
	}

	@Override
	public String[] getExtraNames() {
		return xspress2system.getExtraNames();
	}

	@Override
	public void clearMemory() throws DeviceException {
		xspress2system.clear();
		xspress2system.start();
	}

	
	@Override
	public boolean isContinuousMode() {
		return isContinuousMode;
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		Integer lastFrame = getNumberFrames() - 1;
		return readFrames(0, lastFrame);
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		Object[] readData = readNexusTrees(startFrame, finalFrame);
		framesRead = readData;
		return readData;
	}

	private NexusTreeProvider[] readNexusTrees(int startFrame, int finalFrame) throws DeviceException {
		return xspress2system.readout(startFrame, finalFrame); // as daserver first frame is 0 not 1
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		isContinuousMode = on;

		if (!isSlave) {
			if (on) {
				setTimeFrames();
			} else {
				switchOffExtTrigger();
			}
		}
	}

	private void switchOffExtTrigger() throws DeviceException {
		xspress2system.getDaServer().sendCommand("tfg setup-trig start"); // disables external triggering
	}

	private void setTimeFrames() throws DeviceException {
		switchOnExtTrigger();
		xspress2system.getDaServer().sendCommand("tfg setup-groups ext-start cycles 1");
		xspress2system.getDaServer().sendCommand(
				continuousParameters.getNumberDataPoints() + " 0.000001 0.00000001 0 0 0 8");
		xspress2system.getDaServer().sendCommand("-1 0 0 0 0 0 0");
		xspress2system.getDaServer().sendCommand("tfg arm");
	}

	private void switchOnExtTrigger() throws DeviceException {
		xspress2system.getDaServer().sendCommand("tfg setup-trig start ttl"+triggerSwitch);
	}

	@Override
	public void collectData() throws DeviceException {
		// do nothing as collections will be triggered by hardware.
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return xspress2system.getDescription();
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return xspress2system.getDetectorID();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return xspress2system.getDetectorType();
	}

	@Override
	public int getStatus() throws DeviceException {
		// TODO is this correct, or should this be based on if a continuous scan is currently underway?
		return xspress2system.getStatus();
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		Integer numFrames = getNumberFrames();
		if (numFrames == 0) {
			// view the contents of the first frame anyway
			return readNexusTrees(0, 0)[0];
		}
		return readNexusTrees(numFrames - 1, numFrames - 1)[0];
	}

	@Override
	public int maximumReadFrames() throws DeviceException {

		int numElements = xspress2system.getNumberOfDetectors();

		if (xspress2system.getReadoutMode().equals(XspressDetector.READOUT_SCALERONLY)) {
			return maxNumberOfInts / (numElements * 4);
		}

		int mcaSize = xspress2system.getCurrentMCASize();

		int frameSize = mcaSize * numElements;

		if (frameSize > maxNumberOfInts) {
			return 1;
		}

		return maxNumberOfInts / frameSize;
	}
	
	@Override
	public int getNumberFrames() throws DeviceException {
		if (!isContinuousMode){
			return 0;
		}
		return xspress2system.getNumberFrames();
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return continuousParameters;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		continuousParameters = parameters;
	}

	public Xspress2System getXspress2system() {
		return xspress2system;
	}

	public void setXspress2system(Xspress2System xspress2system) {
		this.xspress2system = xspress2system;
	}

	public boolean isSlave() {
		return isSlave;
	}

	public void setSlave(boolean isSlave) {
		this.isSlave = isSlave;
	}
}
