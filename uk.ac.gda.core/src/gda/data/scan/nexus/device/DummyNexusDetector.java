/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package gda.data.scan.nexus.device;

import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.SliceND;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.DummyDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;

public class DummyNexusDetector extends DummyDetector implements NexusDetector {

	public static final String DETECTOR_NAME = "nexusDetector";

	public static final String EXTERNAL_NEXUS_FILE_NAME = "external.nxs";

	// note 'value' causes a name conflict with the monitor's 'value' field when creating the NXdata group with NexusDataWriter
	public static final String FIELD_NAME_VALUE = "value_";
	public static final String FIELD_NAME_SPECTRUM = "spectrum";

	public static final int SPECTRUM_SIZE = 8;
	public static final int[] IMAGE_SIZE = { 8, 8 }; // NOSONAR: suppress mutable array warning

	public static final String NOTE_TEXT = "This is a note";
	public static final long DETECTOR_NUMBER = 1L;
	public static final String SERIAL_NUMBER = "ABC12345XYZ";
	public static final String GAIN_SETTING = "auto";
	public static final double DIAMETER = 52.2;
	public static final String DIAMETER_UNITS = "mm";

	public static final String STRING_ATTR_NAME = "stringAttr";
	public static final String INT_ATTR_NAME = "intAttr";
	public static final String FLOAT_ATTR_NAME = "floatAttr";
	public static final String ARRAY_ATTR_NAME = "arrayAttr";
	public static final String STRING_ATTR_VALUE = "stringVal";
	public static final int INT_ATTR_VALUE = 2;
	public static final double FLOAT_ATTR_VALUE = 5.432;
	public static final double[] ARRAY_ATTR_VALUE = { 1.23, 2.34, 3.45, 4.56, 5.67 };

	public static final String COLLECTION_NAME = "collection";
	public static final String COLLECTION_FIELD_NAME = "fieldName";
	public static final String COLLECTION_ATTR_NAME = "attrName";
	public static final String COLLECTION_FIELD_VALUE = "fieldValue";
	public static final String COLLECTION_ATTR_VALUE = "attrValue";

	public static final String FIELD_NAME_EXTERNAL = "external";
	public static final String[] FIELD_NAMES_EXTRA_NAMES = { "extra1", "extra2", "extra3" }; // NOSONAR suppress modifiable array warning

	private String outputDir = null;
	private int[] scanDimensions = null;
	private boolean firstData = true;
	private ILazyWriteableDataset externalDataset = null;
	private PositionIterator posIter = null;
	private String externalFilePath = null;

	public DummyNexusDetector() {
		setName(DETECTOR_NAME);
		setExtraNames(FIELD_NAMES_EXTRA_NAMES);
	}

	public void setScanDimensions(int[] scanDimensions) {
		this.scanDimensions = scanDimensions;
		posIter = new PositionIterator(scanDimensions);
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		return (NexusTreeProvider) super.readout();
	}

	@Override
	protected Object acquireData() {
		if (firstData) {
			createExternalNexusFile();
			firstData = false;
		}

		final NXDetectorData data = new NXDetectorData(this);

		// add extra names to the NXDetectorData
		final String[] extraNames = getExtraNames();
		for (int i = 0; i < extraNames.length; i++) {
			data.setPlottableValue(extraNames[i], Double.valueOf(i));
		}

		/*
		 * Priorities set to be explictly non-alphabetical, non-order of insertion, with order of insertion being default when priority not set
		 */
		final NexusGroupData valueData = new NexusGroupData(Math.random() * Double.MAX_VALUE);
		data.addData(getName(), FIELD_NAME_VALUE, valueData);

		final NexusGroupData spectrumData = new NexusGroupData(Random.rand(SPECTRUM_SIZE));
		data.addData(getName(), FIELD_NAME_SPECTRUM, spectrumData);

		final NexusGroupData imageData = new NexusGroupData(Random.rand(IMAGE_SIZE));
		data.addData(getName(), NXdetector.NX_DATA, imageData);
		data.setPrioritisedData(getName(), NXdetector.NX_DATA, NexusExtractor.SDSClassName);

		// add an NXnote child group - NXDetectorData has a convenience method for this
		data.addNote(getName(), NOTE_TEXT);

		final INexusTree detTree = data.getDetTree(getName());

		// add some metadata datasets (i.e. per-scan or non point-dependent)
		detTree.addChildNode(new NexusTreeNode(NXdetector.NX_DETECTOR_NUMBER, NexusExtractor.SDSClassName, detTree,
				new NexusGroupData(DETECTOR_NUMBER)));
		detTree.addChildNode(new NexusTreeNode(NXdetector.NX_SERIAL_NUMBER, NexusExtractor.SDSClassName, detTree,
				new NexusGroupData(SERIAL_NUMBER)));

		final INexusTree diameterNode = new NexusTreeNode(NXdetector.NX_DIAMETER, NexusExtractor.SDSClassName, detTree,
				new NexusGroupData(DIAMETER));
		diameterNode.addChildNode(new NexusTreeNode(ATTRIBUTE_NAME_UNITS, NexusExtractor.AttrClassName, diameterNode,
				new NexusGroupData(DIAMETER_UNITS)));
		detTree.addChildNode(diameterNode);

		final NexusTreeNode gainSettingNode = new NexusTreeNode(NXdetector.NX_GAIN_SETTING, NexusExtractor.SDSClassName, detTree,
				new NexusGroupData(GAIN_SETTING));
		gainSettingNode.addChildNode(new NexusTreeNode(ATTRIBUTE_NAME_UNITS, NexusExtractor.AttrClassName, gainSettingNode,
				new NexusGroupData((String) null))); // to check that a null attribute value isn't written
		detTree.addChildNode(gainSettingNode);

		// add some attributes
		detTree.addChildNode(new NexusTreeNode(STRING_ATTR_NAME, NexusExtractor.AttrClassName, detTree,
				new NexusGroupData(STRING_ATTR_VALUE)));
		detTree.addChildNode(new NexusTreeNode(INT_ATTR_NAME, NexusExtractor.AttrClassName, detTree,
				new NexusGroupData(INT_ATTR_VALUE)));
		detTree.addChildNode(new NexusTreeNode(FLOAT_ATTR_NAME, NexusExtractor.AttrClassName, detTree,
				new NexusGroupData(FLOAT_ATTR_VALUE)));
		detTree.addChildNode(new NexusTreeNode(ARRAY_ATTR_NAME, NexusExtractor.AttrClassName, detTree,
				new NexusGroupData(ARRAY_ATTR_VALUE)));

		// add an NXcollection child group with a data node and an attribute
		final INexusTree collectionNode = new NexusTreeNode(COLLECTION_NAME, NexusExtractor.NXCollectionClassName, detTree);
		collectionNode.addChildNode(new NexusTreeNode(COLLECTION_FIELD_NAME, NexusExtractor.SDSClassName, collectionNode,
				new NexusGroupData(COLLECTION_FIELD_VALUE)));
		collectionNode.addChildNode(new NexusTreeNode(COLLECTION_ATTR_NAME, NexusExtractor.AttrClassName, collectionNode,
				new NexusGroupData(COLLECTION_ATTR_VALUE)));
		detTree.addChildNode(collectionNode);

		writeToExternalFile(); // write to dataset in external file

		final String externalTargetPath = "nxfile://" + externalFilePath + "#entry/data/data";
		data.addExternalFileLink(getName(), FIELD_NAME_EXTERNAL, externalTargetPath, false, true, 2);

		return data;
	}

	private void writeToExternalFile() {
		final IDataset dataToWrite = Random.rand(IMAGE_SIZE);

		if (!posIter.hasNext()) {
			// hasNext() actually moves the posIter to the next position(!);
			throw new NoSuchElementException("posIter ran out of positions, shape = " + Arrays.toString(scanDimensions));
		}

		final int[] start = posIter.getPos();
		final int[] stop = Arrays.stream(start).map(pos -> pos + 1).toArray();
		final SliceND scanSlice = new SliceND(scanDimensions, start, stop, null);

		try {
			IWritableNexusDevice.writeDataset(externalDataset, dataToWrite, scanSlice);
		} catch (DatasetException e) {
			throw new RuntimeException("Error writing to external file", e);
		}
	}

	private void createExternalNexusFile() {
		externalFilePath = outputDir + EXTERNAL_NEXUS_FILE_NAME;
		final TreeFile treeFile = NexusNodeFactory.createTreeFile(externalFilePath);
		final NXroot root = NexusNodeFactory.createNXroot();
		treeFile.setGroupNode(root); // the structure of the external file doesn't matter
		final NXentry entry = NexusNodeFactory.createNXentry();
		root.setEntry(entry);
		final NXdata data = NexusNodeFactory.createNXdata();
		entry.setData(data);

		final int datasetRank = scanDimensions.length + IMAGE_SIZE.length;
		externalDataset = data.initializeLazyDataset(NXdata.NX_DATA, datasetRank, Double.class);

		try {
			saveNexusFile(treeFile);
		} catch (NexusException e) {
			throw new RuntimeException("Error creating external nexus file", e);
		}
	}

	public static void saveNexusFile(TreeFile nexusTree) throws NexusException {
		try (NexusFile nexusFile = ServiceHolder.getNexusFileFactory().newNexusFile(nexusTree.getFilename(), true)) {
			nexusFile.createAndOpenToWrite();
			nexusFile.addNode("/", nexusTree.getGroupNode());
			nexusFile.flush();
		}
	}

}