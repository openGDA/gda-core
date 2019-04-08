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

package gda.device.detector.xspress;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DAServer;
import gda.device.detector.DetectorBase;
import gda.device.detector.NexusDetector;
import gda.util.NexusTreeWriter;
import uk.ac.gda.beans.xspress.XspressDetector;

/**
 * To operate the Xspress through TFGv2 in the ContinuousScan framework
 */
public class Xspress2BufferedDetector extends DetectorBase implements BufferedDetector, NexusDetector {
	private static final long serialVersionUID = -6791888656626099507L;
	private static final Logger logger = LoggerFactory.getLogger(Xspress2BufferedDetector.class);
	protected ContinuousParameters continuousParameters = null;
	protected boolean isContinuousMode = false;
	protected Xspress2Detector xspress2system = null;
	private boolean isSlave = true;
	protected int maxNumberOfInts = 750000; // the maximum number of integers to read out from da.server in each bunch
	private int triggerSwitch = 0;
	protected DAServer daserver;
	// frames before this causes a problem 80000 = 2 frames of 9-element reading full mca

	protected boolean useNexusTreeWriter = false;
	protected transient NexusTreeWriter nexusTreeWriter = null;
	private transient List< Pair<Integer,Integer> > readFrameList = new ArrayList<>();
	protected String detectorNexusFilename = "";

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
	public String[] getInputNames() {
		return xspress2system.getInputNames();
	}

	@Override
	public String[] getOutputFormat() {
		return xspress2system.getOutputFormat();
	}

	@Override
	public void clearMemory() throws DeviceException {
		xspress2system.clear();
		xspress2system.start();
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		if (!isContinuousMode)
			return 0;
		String[] cmds = new String[] { "status show-armed", "progress", "status", "full", "lap", "frame" };
		HashMap<String, String> currentVals = new HashMap<>();
		for (String cmd : cmds) {
			currentVals.put(cmd, runDAServerCommand("tfg read " + cmd).toString());
			if (cmd.equals("frame")){
				logger.info("tfg read " + cmd + ": " + currentVals.get(cmd));
			}
		}
		if (currentVals.isEmpty())
			return 0;
		// else either scan not started (return -1) or has finished (return continuousParameters.getNumberDataPoints())
		// if started but nothing collected yet
		if (currentVals.get("status show-armed").equals("EXT-ARMED"))
			return 0;
		// if frame is non-0 then work out the current frame
		if (!currentVals.get("frame").equals("0")) {
			String numFrames = currentVals.get("frame");
			return extractCurrentFrame(Integer.parseInt(numFrames));
		}
		return continuousParameters.getNumberDataPoints();
	}

	private int extractCurrentFrame(int frameValue) {
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

	private Object runDAServerCommand(String command) throws DeviceException {
		Object obj = null;
		if (daserver.isConnected()) {
			if ((obj = daserver.sendCommand(command)) == null)
				throw new DeviceException("Null reply received from daserver during " + command);
			return obj;
		}
		return null;
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

		// Write the detector data
		if (useNexusTreeWriter) {
			writeDetectorData((NexusTreeProvider[]) readData, startFrame, finalFrame);
		}
		framesRead = readData;
		return readData;
	}

	@Override
	public void atScanStart() {
		if (useNexusTreeWriter) {
			nexusTreeWriter = new NexusTreeWriter();
			readFrameList.clear();
			nexusTreeWriter.setFullpathToNexusFile(detectorNexusFilename);
			nexusTreeWriter.atScanStart();
		}
	}

	@Override
	public void stop() {
		atScanEnd();
	}

	@Override
	public void atScanEnd() {
		if (useNexusTreeWriter && nexusTreeWriter!=null) {
			readFrameList.clear();
			nexusTreeWriter.atScanEnd();
			nexusTreeWriter = null;
		}
	}

	public void writeDetectorData(NexusTreeProvider[] nexusTreeArray, int startFrame, int finalFrame) {
		if (nexusTreeWriter==null) {
			logger.info("Nexus tree writer has not been set");
			return;
		}

		// Check we are not writing same range of frames again.
		Pair<Integer, Integer> frameRange = Pair.create(startFrame,  finalFrame);
		if (readFrameList.contains(frameRange)) {
			return;
		}
		readFrameList.add(frameRange);

		// Store the frames of data in the writer
		nexusTreeWriter.addData(nexusTreeArray);

		//update the nexus tree array to remove the stored nodes
		nexusTreeWriter.removeNodesFromNexusTree(nexusTreeArray);

		// Try to get name of nexus file written by NexusDataWriter
		String scanNexusFilename = getFilenameForXspress();
		if (StringUtils.isEmpty(scanNexusFilename)) {
			return;
		}

		// Only append data to existing file created by NexusDataWriter (i.e. don't create a new one).
		if (!Paths.get(scanNexusFilename).toFile().exists()) {
			return;
		}

		try {
			nexusTreeWriter.setFullpathToNexusFile(scanNexusFilename);
			nexusTreeWriter.writeNexusData();
		} catch (NexusException | DatasetException e) {
			logger.error("Problem adding Xspress2 data to nexus file", e);
		}
	}

	private NexusTreeProvider[] readNexusTrees(int startFrame, int finalFrame) throws DeviceException {
		return xspress2system.readout(getName(), startFrame, finalFrame); // as daserver first frame is 0 not 1
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		isContinuousMode = on;
		String deviceName = getDescription();

		if (!isSlave) {
			if (on) {
				try {
					setTimeFrames();
				} catch (DeviceException e) {
					logger.error("Problem setting time frames on device " +deviceName, e);
				}
			} else {
				try {
					switchOffExtTrigger();
				} catch (DeviceException e) {
					logger.error("Unable to set trigger on device " +deviceName, e);
				}
			}
		}
	}

	private void switchOffExtTrigger() throws DeviceException {
		daserver.sendCommand("tfg setup-trig start"); // disables external triggering
	}

	private void setTimeFrames() throws DeviceException {
		switchOnExtTrigger();
		daserver.sendCommand("tfg setup-groups ext-start cycles 1");
		daserver.sendCommand(continuousParameters.getNumberDataPoints() + " 0.000001 0.00000001 0 0 0 8");
		daserver.sendCommand("-1 0 0 0 0 0 0");
		daserver.sendCommand("tfg arm");
	}

	private void switchOnExtTrigger() throws DeviceException {
		daserver.sendCommand("tfg setup-trig start ttl"+triggerSwitch);
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
		// view the contents of the first frame anyway
		if (numFrames == 0)
			return readNexusTrees(0, 0)[0];
		return readNexusTrees(numFrames - 1, numFrames - 1)[0];
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		maxNumberOfInts = 750000*10;
		int numElements = xspress2system.getNumberOfDetectors();
		if (xspress2system.getReadoutMode().equals(XspressDetector.READOUT_SCALERONLY))
			return maxNumberOfInts / (numElements * 4);
		int mcaSize = xspress2system.getCurrentMCASize();
		int frameSize = mcaSize * numElements;
		if (frameSize > maxNumberOfInts)
			return 1;
		return maxNumberOfInts / frameSize;
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return continuousParameters;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		continuousParameters = parameters;
	}

	public Xspress2Detector getXspress2system() {
		return xspress2system;
	}

	public void setXspress2system(Xspress2Detector xspress2system) {
		this.xspress2system = xspress2system;
	}

	public boolean isSlave() {
		return isSlave;
	}

	public void setSlave(boolean isSlave) {
		this.isSlave = isSlave;
	}

	public DAServer getDaserver() {
		return daserver;
	}

	public void setDaserver(DAServer daserver) {
		this.daserver = daserver;
	}

	public boolean getUseNexusTreeWriter() {
		return useNexusTreeWriter;
	}

	public void setUseNexusTreeWriter(boolean useTreeWriter) {
		this.useNexusTreeWriter = useTreeWriter;
	}

	public String getDetectorNexusFilename() {
		return detectorNexusFilename;
	}

	public void setDetectorNexusFilename(String detectorNexusFilename) {
		this.detectorNexusFilename = detectorNexusFilename;
	}

	/**
	 * Name of Nexus file to write detector data to, either :
	 * <li> Returns value of {@link #getDetectorNexusFilename} if this has been set, or
	 * <li> Path to GDA scan nexus file.
	 * @return Name of nexus file to write detector data to.
	 */
	private String getFilenameForXspress() {
		if (StringUtils.isEmpty(detectorNexusFilename)) {
			return nexusTreeWriter.getScanNexusFilename();
		}
		return detectorNexusFilename;
	}
}
