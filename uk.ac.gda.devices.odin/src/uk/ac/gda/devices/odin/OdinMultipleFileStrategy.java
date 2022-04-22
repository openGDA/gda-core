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

package uk.ac.gda.devices.odin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NXDetectorDataWithFilepathForSrs;
import gda.jython.InterfaceProvider;
import uk.ac.gda.devices.odin.control.OdinDetectorController;

/**
 * This handles collection where each point in the scan writes a hdf5 file containing a single frame.
 */
public class OdinMultipleFileStrategy implements OdinStrategy {

	private static final String[] EMPTY_STRING_ARRAY = new String[] {};

	private String imageMode = "Single";
	private String triggerMode = "Internal";

	private static final String FILEPATH_EXTRANAME = "filepath";

	private final OdinDetectorController controller;
	private String filePrefix;
	private String fileDirectory;


	public OdinMultipleFileStrategy(OdinDetectorController controller) {
		this.controller = controller;
	}

	@Override
	public void prepareWriterForScan(String detName, int scanNumber, double collectionTime) throws DeviceException {
		controller.setOffsetAndUid(0, 0);
		controller.prepareDataWriter(1);
		controller.prepareCamera(1, collectionTime, 0.0, imageMode, triggerMode);
		fileDirectory = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		filePrefix = String.format("%sScan%dPoint", detName, scanNumber);

	}

	@Override
	public void prepareWriterForPoint(int pointNumber) throws DeviceException {
		String fileName = filePrefix + Integer.toString(pointNumber);
		controller.setDataOutput(fileDirectory, fileName);
		controller.startRecording();
		controller.startCollection();

	}

	@Override
	public NXDetectorData getNXDetectorData(String detName, double acquireTime, int scanPoint) {
			NXDetectorData data;
			data = new NXDetectorDataWithFilepathForSrs(getExtraNames(), getOutputFormat(), detName);
			addDoubleItemToNXData(data, detName, "count_time", acquireTime);
			String filename = controller.getLatestFilename();
			if (filename == null || filename.isEmpty()) {
				throw new IllegalArgumentException("filename is null or zero length");
			}
			// add reference to external file
			assert (data instanceof NXDetectorDataWithFilepathForSrs);
			NXDetectorDataWithFilepathForSrs dataForSrs = (NXDetectorDataWithFilepathForSrs) data;
			NexusTreeNode fileNameNode = dataForSrs.addFileNames(detName, "image_data", new String[] { filename }, true,
					true);
			fileNameNode.addChildNode(
					new NexusTreeNode("signal", NexusExtractor.AttrClassName, fileNameNode, new NexusGroupData(1)));
			// add filename as an NXNote
			dataForSrs.addFileName(detName, filename);
			int indexOf = Arrays.asList(getExtraNames()).indexOf(FILEPATH_EXTRANAME);
			dataForSrs.setFilepathOutputFieldIndex(indexOf);
			data.setPlottableValue(OdinMultipleFileStrategy.FILEPATH_EXTRANAME, 0.0);
			return data;
		}

	private void addDoubleItemToNXData(NXDetectorData data, String detName, String name, Double val) {
		INexusTree valdata = data.addData(detName, name, new NexusGroupData(val));
		valdata.addChildNode(new NexusTreeNode("local_name", NexusExtractor.AttrClassName, valdata,
				new NexusGroupData(String.format("%s.%s", detName, name))));
		data.setPlottableValue(name, val);
	}

	@Override
	public String[] getInputNames() {
		return EMPTY_STRING_ARRAY;
	}

	@Override
	public String[] getExtraNames() {
		List<String> extraNames = new ArrayList<>();
		extraNames.add("count_time");
		extraNames.add(FILEPATH_EXTRANAME);
		return extraNames.toArray(EMPTY_STRING_ARRAY);
	}

	@Override
	public String[] getOutputFormat() {
		List<String> formats = new ArrayList<>();
		formats.add("%.2f");
		formats.add("%.2f");
		return formats.toArray(EMPTY_STRING_ARRAY);
	}

	@Override
	public int getStatus() {
		return controller.getStatus();
	}

	@Override
	public void waitWhileBusy(int scanPointNumber) {
		controller.waitWhileAcquiring();
		controller.waitWhileWriting();
	}

	public String getImageMode() {
		return imageMode;
	}

	public void setImageMode(String imageMode) {
		this.imageMode = imageMode;
	}

	public String getTriggerMode() {
		return triggerMode;
	}

	public void setTriggerMode(String triggerMode) {
		this.triggerMode = triggerMode;
	}
}
