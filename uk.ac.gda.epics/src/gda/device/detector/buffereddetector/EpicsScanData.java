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

package gda.device.detector.buffereddetector;

import gda.device.ContinuousParameters;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Untested on hardware! This is simply coded against the information in the Controls group wiki for the moment...
 * <p>
 * Operates the Epics Scan Data Template in ContinuousScans when the motion is controlled by an Epics Trajectory
 * template and the detector this class represetns is to be read from an Epics ScanData template.
 */
public class EpicsScanData extends DetectorBase implements BufferedDetector, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsScanData.class);

	public static String counterPV = "COUNTER";
	public static String startReadIndexPV = "STARTINDEX";
	public static String numReadElementsPV = "NOELEMENTS";
	public static String updatePV = "UPDATE";
	public static String numDetectorsPV = "NODETECTORS";
	public static String detNPV = "CH%iDATA";

	private String templateName = "";
	private boolean continuousMode = false;
	private ContinuousParameters parameters = null;

	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private Channel counterChannel;
	private Channel startReadIndexChannel;
	private Channel numReadElementsChannel;
	private Channel updateChannel;
	private Channel numDetectorsChannel;
	private Channel[] detNChannels;

	public EpicsScanData() {
	}

	@Override
	public void configure() throws FactoryException {
		if (templateName == null || templateName.isEmpty()) {
			throw new FactoryException(getName() + " cannot configure as Epics template not defined!");
		}

		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		try {
			counterChannel = channelManager.createChannel(templateName + counterPV);
			startReadIndexChannel = channelManager.createChannel(templateName + startReadIndexPV);
			numReadElementsChannel = channelManager.createChannel(templateName + numReadElementsPV);
			updateChannel = channelManager.createChannel(templateName + updatePV);
			numDetectorsChannel = channelManager.createChannel(templateName + numDetectorsPV);
		} catch (CAException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	@Override
	public void initializationCompleted() {

		try {
			createDetectorChannels();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void createDetectorChannels() throws TimeoutException, CAException, NumberFormatException, InterruptedException {
		int numDetectors = Integer.parseInt(controller.caget(numDetectorsChannel));

		detNChannels = new Channel[numDetectors];

		for (int i = 0; i < numDetectors; i++) {
			String thisChannelName = templateName + String.format(detNPV, i);
			detNChannels[i] = channelManager.createChannel(thisChannelName);
		}
	}

	@Override
	public void clearMemory() throws DeviceException {
		// I don't think this needs to be done for these detectors
	}

	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return parameters;
	}

	private void updateData() throws CAException, InterruptedException {
		controller.caput(updateChannel, 1);
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		try {
			return controller.cagetShort(counterChannel);
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e);
		}
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return continuousMode;
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		int finalFrame = getNumberFrames();
		return readFrames(0, finalFrame - 1);
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		
		int numFramesInMemory = getNumberFrames();
		if (numFramesInMemory < finalFrame) {
			throw new DeviceException("requested frame number " + finalFrame + " does not exist! Only " + numFramesInMemory
					+ " frames available.");
		}

		int numFramesToRead;
		int numDetectors;
		int[][] results;
		try {
			numFramesToRead = finalFrame - startFrame;
			updateData();
			controller.caput(startReadIndexChannel, startFrame);
			controller.caput(numReadElementsChannel, numFramesToRead);
			
			numDetectors = Integer.parseInt(controller.caget(numDetectorsChannel));
			results = new int[numDetectors][numFramesToRead];
			for (int i = 0; i < numDetectors; i++) {
				int[] detectorResults = controller.cagetIntArray(detNChannels[i], numFramesToRead);
				results[i] = detectorResults;
			}
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e);
		}
		
		// flip array to be [frame][detector] rather than [detector][frame]
		int[][] output = new int[numFramesToRead][numDetectors];
		for (int det = 0; det < numDetectors; det++){
			for (int frame = 0; frame < numFramesToRead; frame++){
				output[frame][det] = results[det][frame];
			}
		}
		
		return output;
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		this.continuousMode = on;
		// no need to do any further configuration as this is done by Epics
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		this.parameters = parameters;
	}

	@Override
	public void collectData() throws DeviceException {
		// cannot do for these detectors
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Detector for continuous scans operated via Epics";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Epics Scan Data";
	}

	@Override
	public int getStatus() throws DeviceException {
		return Detector.IDLE;
	}

	@Override
	public Object readout() throws DeviceException {
		// cannot do for these detectors
		return null;
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		//TODO needs testing to find out the real value
		return Integer.MAX_VALUE;
	}

}
