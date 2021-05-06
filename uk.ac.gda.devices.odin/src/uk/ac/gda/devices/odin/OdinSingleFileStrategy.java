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

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.jython.InterfaceProvider;

/**
 * This strategy is for the common case of writing a single hdf5 file per scan.
 * Since Odin is designed for hardware triggered scans, the detector must be set to
 * use Single mode which requires that the ofset and uid of the datawriter is set for each point.
 * <p>
 * Note that there is also considerable overhead/delay as Odin arms the detector for each point in the scan.
 * A hardware triggered approach would be recommended.
 */
public class OdinSingleFileStrategy implements OdinStrategy {

	private static final String COUNT_TIME_NAME = "count_time";

	private static final String FRAME_NO_NAME = "frameNo";

	private static final String[] EMPTY_STRING_ARRAY = new String[] {};

	private static final String IMAGE_MODE = "Single";
	private static final String TRIGGER_MODE = "Internal";

	private final OdinDetectorController controller;
	private String filePrefix;
	private String fileDirectory;

	public OdinSingleFileStrategy(OdinDetectorController controller) {
		this.controller = controller;
	}


	@Override
	public void prepareWriterForScan(String detName, int scanNumber, double collectionTime) throws DeviceException {
		int noPoints = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getNumberOfPoints();
		controller.prepareDataWriter(noPoints);
		controller.prepareCamera(1, collectionTime, 0.0, IMAGE_MODE, TRIGGER_MODE);
		fileDirectory = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		filePrefix = String.format("%sScan%d", detName, scanNumber);
		controller.setDataOutput(fileDirectory, filePrefix);
		controller.startRecording();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Use zero-based frame number for Odin offset as expected by the filewriter. For
	 * Uid use one-based indexing to allow SWMR processing to determine when frames are written.
	 */
	@Override
	public void prepareWriterForPoint(int pointNumber) throws DeviceException {
		controller.setOffsetAndUid(pointNumber - 1, pointNumber);
		controller.startCollection();
	}



	@Override
	public String[] getInputNames() {
		return EMPTY_STRING_ARRAY;
	}

	@Override
	public String[] getExtraNames() {
		return new String[] {FRAME_NO_NAME, COUNT_TIME_NAME};
	}

	@Override
	public String[] getOutputFormat() {
		return new String[] {"%.0f", "%.2f"};
	}


	@Override
	public NXDetectorData getNXDetectorData(String detName, double acquireTime, int scanPoint) {
		NXDetectorData data;
		data = new NXDetectorData(getExtraNames(), getOutputFormat(), detName);
		addDoubleItemToNXData(data, detName, FRAME_NO_NAME, Double.valueOf(controller.getNumFramesCaptured()));
		addDoubleItemToNXData(data, detName, COUNT_TIME_NAME, acquireTime);
		String filename = controller.getLatestFilename();
		if (scanPoint == 1) {
			data.addExternalFileLink(detName, "data", "nxfile://" + filename + "#data", 2);
		}
		return data;
	}

	// TODO this is duplicated in the other strategy
	private void addDoubleItemToNXData(NXDetectorData data, String detName, String name, Double val) {
		INexusTree valdata = data.addData(detName, name, new NexusGroupData(val));
		valdata.addChildNode(new NexusTreeNode("local_name", NexusExtractor.AttrClassName, valdata,
				new NexusGroupData(String.format("%s.%s", detName, name))));
		data.setPlottableValue(name, val);
	}


	@Override
	public int getStatus() {
		// This could be wrong, maybe will have to check only acquire status
		return controller.getStatus();
	}


	/**
	 * For this strategy only wait for acquire to finish
	 * The data writer will be busy until the end of the scan
	 */
	@Override
	public void waitWhileBusy(int scanPointNumber) {
		controller.waitWhileAcquiring();
		controller.waitForWrittenFrames(scanPointNumber);

	}


}
