/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Pair;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.DatasetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.util.NexusTreeWriter;

public class NexusTreeWriterHelper {

	private static final Logger logger = LoggerFactory.getLogger(NexusTreeWriterHelper.class);

	private NexusTreeWriter nexusTreeWriter = null;
	private List<Pair<Integer, Integer>> readFrameList = new ArrayList<>();
	private String detectorNexusFilename = "";
	private boolean writeToScanNexusFile = true;

	public void addDetectorData(NexusTreeProvider[] nexusTreeArray, int startFrame, int finalFrame) {
		Pair<Integer, Integer> frameRange = Pair.create(startFrame,  finalFrame);
		if (readFrameList.contains(frameRange)) {
			logger.info("Not adding frames {} ... {} - they have already been processed", startFrame, finalFrame);
			return;
		}

		readFrameList.add(frameRange);

		// Store the frames of data in the writer
		nexusTreeWriter.addData(nexusTreeArray);

		//update the nexus tree array to remove the stored nodes
		nexusTreeWriter.removeNodesFromNexusTree(nexusTreeArray);

	}

	public NexusTreeProvider[] getCopy(NexusTreeProvider[] nexusTreeArray, int startIndex) {
		int numFrames = nexusTreeArray.length - startIndex;
		NexusTreeProvider[] detData = new NexusTreeProvider[numFrames];
		for(int i=0; i<detData.length; i++) {
			detData[i] = nexusTreeArray[i+startIndex];
		}
		return detData;
	}

	public void writeData() throws DeviceException {
		// Try to get name of nexus file written by NexusDataWriter
		String nexusFilePath = getDetectorNexusFilename();
		if (writeToScanNexusFile) {
			nexusFilePath = nexusTreeWriter.getScanNexusFilename();
		}

		if (writeToScanNexusFile && !Paths.get(nexusFilePath).toFile().exists()) {
			logger.info("Not writing data. File hasn't been created yet");
			return;
		}

		try {
			nexusTreeWriter.setFullpathToNexusFile(nexusFilePath);
			nexusTreeWriter.writeNexusData();
		} catch (NexusException | DatasetException e) {
			logger.error("Problem adding data for {} to nexus file {}", nexusTreeWriter.getDetectorName(), nexusFilePath, e);
			throw new DeviceException(e);
		}
	}

	public void atScanStart() {
		nexusTreeWriter = new NexusTreeWriter();
		readFrameList.clear();
		nexusTreeWriter.atScanStart();
	}

	public void atScanEnd() {
		readFrameList.clear();
		if (nexusTreeWriter != null) {
			nexusTreeWriter.atScanEnd();
			nexusTreeWriter = null;
		}
	}

	public void setDetectorNexusFilename(String detectorNexusFilename) {
		this.detectorNexusFilename = detectorNexusFilename;
	}

	public String getDetectorNexusFilename() {
		return detectorNexusFilename;
	}

	public boolean isWriteToScanNexusFile() {
		return writeToScanNexusFile;
	}

	public void setWriteToScanNexusFile(boolean writeToScanNexusFile) {
		this.writeToScanNexusFile = writeToScanNexusFile;
	}
}
