/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import java.io.Serializable;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;

/**
 * Basic class which wrappers up a lot of nexus calls so that detectors can integrate more easily
 */
public class NXDetectorData implements GDANexusDetectorData, Serializable {

	public static final String DATA_FILE_CLASS_NAME = "data_file";
	public static final String FILE_NAME_NODE_NAME = "file_name";
	private static final String DATA_FILENAME_ATTR_NAME = "data_filename";
	private static final Logger logger = LoggerFactory.getLogger(NXDetectorData.class);
	private INexusTree tree = null;
	private Double[] doubleData = new Double[] { }; //must be empty on construction
	protected String[] extraNames = new String[] {};
	protected String[] outputFormat = new String[] {};

	public NXDetectorData(String[] extraNames, String[] outputFormat, String detectorName) {
		this();

		if (extraNames == null || extraNames.length == 0) {
			this.extraNames = new String[] { detectorName };
		} else {
			this.extraNames = extraNames;
		}

		if (outputFormat == null || outputFormat.length == 0) {
			logger.info("Detector {} does not provide outputFormat", detectorName);
			this.outputFormat = new String[] { "%5.5g" };
		} else {
			this.outputFormat = outputFormat;
		}

		doubleData = new Double[this.extraNames.length];
		for (int i = 0; i < doubleData.length; i++) {
			doubleData[i] = null;
		}
	}

	/**
	 * Passing in the detector helps setting up the plotable data array and does some
	 * useful checking
	 * @param detector
	 */
	public NXDetectorData(Scannable detector) {
		this(detector.getExtraNames(),detector.getOutputFormat(),detector.getName());
		if (detector.getInputNames() != null && detector.getInputNames().length > 0) logger.warn("Dubious detector "+detector.getName()+" with input names.\nAnticipate plotting problems.");
	}

	/**
	 * Basic constructor
	 */
	public NXDetectorData() {
		tree = new NexusTreeNode("", NexusExtractor.NXInstrumentClassName, null);
	}

	/**
	 * Constructor, which is based on an initial NexusTree
	 * @param tree
	 */
	public NXDetectorData(INexusTree tree) {
		this.tree = tree;
	}

	@Override
	public INexusTree getNexusTree() {
		return tree;
	}

	/**
	 * returns the names detectors tree
	 * @param detName if null or empty it returns the first
	 * @return the NexusTree associated with the named detector
	 */
	@Override
	public INexusTree getDetTree(String detName){
		for(INexusTree branch : tree){
			if( branch.getNxClass().equals(NexusExtractor.NXDetectorClassName)){
				if( !StringUtils.hasLength(detName) || branch.getName().equals(detName)){
					return branch;
				}
			}
		}
		//else add item and return that
		NexusTreeNode detTree = new NexusTreeNode(detName, NexusExtractor.NXDetectorClassName, null);
		detTree.setIsPointDependent(true);
		tree.addChildNode(detTree);
		return detTree;
	}

	/**
	 * @param parent
	 * @param data
	 * @return The node added.
	 */
	public INexusTree addData(String parent, NexusGroupData data) {
		return addData(getDetTree(parent), "data", data, null, null);
	}

	/**
	 * @param parent
	 * @param data
	 * @param units
	 * @return The node added.
	 */
	public INexusTree addData(String parent, NexusGroupData data, String units) {
		return addData(getDetTree(parent), "data", data, units, null);
	}

	/**
	 * @param parent
	 * @param data
	 * @return The node added.
	 */
	public INexusTree addData(String parent, String dataName, NexusGroupData data) {
		return addData(getDetTree(parent), dataName, data, null, null);
	}

	/**
	 * @param parent
	 * @param data
	 * @param units
	 * @return The node added.
	 */
	public INexusTree addData(String parent, String dataName, NexusGroupData data, String units) {
		return addData(getDetTree(parent), dataName, data, units, null);
	}

	/**
	 * @param parent
	 * @param data
	 * @param units
	 * @param signalVal
	 * @return The node added.
	 */
	public INexusTree addData(String parent, NexusGroupData data, String units, Integer signalVal) {
		return addData(getDetTree(parent), "data", data, units, signalVal);
	}

	/**
	 *
	 * @param parent
	 * @param dataName
	 * @param data
	 * @param units
	 * @param signalVal
	 * @return The node added.
	 */
	public static INexusTree addData(INexusTree parent, final String dataName, NexusGroupData data, String units,
			Integer signalVal) {
		data.isDetectorEntryData = true;
		NexusTreeNode node = new NexusTreeNode(dataName, NexusExtractor.SDSClassName, parent, data);
		node.setIsPointDependent(true);
		if (units != null) {
			node.addChildNode(new NexusTreeNode("units", NexusExtractor.AttrClassName, node, new NexusGroupData(units)));
		}
		if (signalVal != null) {
			node.addChildNode(new NexusTreeNode("signal", NexusExtractor.AttrClassName, node,
					new NexusGroupData(signalVal)));
		}
		parent.addChildNode(node);
		return node;
	}

	public INexusTree addData(String detName, String dataName, NexusGroupData data, String units, Integer signalVal) {
		return addData(detName, dataName, data, units, signalVal, null, data.isDetectorEntryData);
	}

	/**
	 * Adds the specified data to the named detector
	 * @param detName The name of the detector to add data to
	 * @param data The implementation of NexusGroupData to be reported as the data
	 * @param units  - if not null a units attribute is added
	 * @param signalVal - if not null a signal attribute is added
	 */
	public INexusTree addData(String detName, NexusGroupData data, String units, Integer signalVal, String interpretation) {
		return addData(detName, "data", data, units, signalVal, interpretation, true);
	}

	/**
	 * Adds the specified data to the named detector
	 * @param detName The name of the detector to add data to
	 * @param data The implementation of NexusGroupData to be reported as the data
	 * @param units  - if not null a units attribute is added
	 * @param signalVal - if not null a signal attribute is added
	 * @param interpretation - if not null an interpretation string
	 */
	public INexusTree addData(String detName, String dataName, NexusGroupData data, String units, Integer signalVal, String interpretation) {
		return addData(detName, dataName, data, units, signalVal, interpretation, data.isDetectorEntryData);
	}

	public INexusTree addData(String detName, String dataName, NexusGroupData data, String units, Integer signalVal, String interpretation, boolean isPointDependent) {
		return addData(getDetTree(detName), dataName, data, units, signalVal, interpretation, isPointDependent);
	}

	public static INexusTree addData(INexusTree parent, String dataName, NexusGroupData data, String units, Integer signalVal, String interpretation, boolean isPointDependent) {
		NexusTreeNode node = new NexusTreeNode(dataName, NexusExtractor.SDSClassName, null, data);
		node.setIsPointDependent(data.isDetectorEntryData || isPointDependent);
		if (units != null) {
			node.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, node, new NexusGroupData(units)));
		}
		if (signalVal != null) {
			node.addChildNode(new NexusTreeNode("signal",NexusExtractor.AttrClassName, node, new NexusGroupData(signalVal)));
		}
		if (interpretation != null) {
			node.addChildNode(new NexusTreeNode("interpretation",NexusExtractor.AttrClassName, node,
					new NexusGroupData(interpretation)));
		}
		parent.addChildNode(node);
		return node;
	}

	/**
	 * Add the string as a NXNote - used when the detector writes data to a file itself to be agglomerated into the NExus file later.
	 * @param detName  The name of the detector to add data to
	 * @param filename filename
	 */
	public void addFileName(String detName, String filename){
		INexusTree detTree = getDetTree(detName);
		NexusTreeNode node = new NexusTreeNode(DATA_FILE_CLASS_NAME, NexusExtractor.NXNoteClassName, null,null);
		NexusGroupData file_name_sds = new NexusGroupData(NexusGroupData.MAX_TEXT_LENGTH, filename);
		NexusTreeNode file_name = new NexusTreeNode(FILE_NAME_NODE_NAME, NexusExtractor.SDSClassName, null,file_name_sds);
		file_name.addChildNode(new NexusTreeNode(DATA_FILENAME_ATTR_NAME,NexusExtractor.AttrClassName, file_name,new NexusGroupData(1)));
		node.setIsPointDependent(true);
		file_name.setIsPointDependent(true);
		node.addChildNode( file_name);
		detTree.addChildNode(node);
	}

	/**
	 * Adds a DATA_FILE_SDS_NAME SDS item for the collection of filenames as a child of the portion of the tree for a detector.
	 *
	 * This is useful when a detector creates its own data file or a collection of data files
	 *
	 * @param detName  The name of the detector to add data to
	 * @param nodeName The name of the created
	 * @param filenames filenames collection of filenames to add to section
	 * @param isPointDependent boolean to signal is the data is to be provided for each point in a scan.
	 * @param isDetectorEntryData  Flag to indicate that when writing this value to a file the entry is to linked to the
	 * NXEntry/NXDetector section as a variable of the scan
	 */
	public NexusTreeNode addFileNames(String detName, String nodeName, String[] filenames, boolean isPointDependent, boolean isDetectorEntryData){
		NexusGroupData file_name_sds = new NexusGroupData(NexusGroupData.MAX_TEXT_LENGTH, filenames);
		NexusTreeNode file_name = new NexusTreeNode(nodeName, NexusExtractor.SDSClassName, null,file_name_sds);
		file_name.setIsPointDependent(isPointDependent);
		file_name_sds.isDetectorEntryData=isDetectorEntryData;
		file_name.addChildNode(new NexusTreeNode(DATA_FILENAME_ATTR_NAME,NexusExtractor.AttrClassName, file_name,new NexusGroupData(1)));
		INexusTree detTree = getDetTree(detName);
		detTree.addChildNode(file_name);
		return file_name;
	}

	/**
	 * create exactly one file link under a node named "data".
	 * @param detName
	 * @param hdfFileName
	 */
	public void addScanFileLink(String detName, String hdfFileName) {
		INexusTree detTree = getDetTree(detName);
//		NexusGroupData dummy_sds = new NexusGroupData("dummy");
		NexusTreeNode link = new NexusTreeNode("data", NexusExtractor.ExternalSDSLink, null, new NexusGroupData(hdfFileName));
		link.setIsPointDependent(false);
//		link.addChildNode(new NexusTreeNode("napimount", NexusExtractor.AttrClassName, link, new NexusGroupData(hdfFileName)));
		detTree.addChildNode(link);
	}

	/**
	 * create one node called 'linkname' for each external file link.
	 * @param detName
	 * @param linknodename
	 * @param fileName - must be plain full path file name.
	 */
	public void addExternalFileLink(String detName, String linknodename, String fileName, boolean isPointDependent, boolean isDetectorEntryData) {
		INexusTree detTree = getDetTree(detName);
//		NexusGroupData dummy_sds = new NexusGroupData("dummy");
		NexusGroupData groupData = new NexusGroupData( fileName );
		NexusTreeNode link = new NexusTreeNode(linknodename, NexusExtractor.ExternalSDSLink, null, groupData);
		link.setIsPointDependent(isPointDependent);
		groupData.isDetectorEntryData = isDetectorEntryData;
//		link.addChildNode(new NexusTreeNode("napimount", NexusExtractor.AttrClassName, link, new NexusGroupData(hdfFileName)));
		detTree.addChildNode(link);
	}

	/**
	 * Adds the specified Axis to the named detector
	 * @param detName The name of the detector to add data to
	 * @param name The name of the Axis
	 * @param axis_sds The implementation of NexusGroupData to be reported as the axis data
	 * @param axisValue The dimension which this axis relates to <b>from the detector point of view</b>,
	 * 						i.e. 1 is the first detector axis, scan dimensions will be added as required
	 * 						by the DataWriter
	 * @param primaryValue The importance of this axis, 1 is the most relevant, then 2 etc.
	 * @param units The units the axis is specified in
	 * @param isPointDependent If this data should be added to the nexus at every point set this to true, if its a one off, make this false
	 */
	public void addAxis(String detName, String name, NexusGroupData axis_sds, Integer axisValue, Integer primaryValue, String units,
			boolean isPointDependent) {
		axis_sds.isDetectorEntryData = true;
		NexusTreeNode axis = new NexusTreeNode(name,NexusExtractor.SDSClassName, tree, axis_sds);
		axis.addChildNode(new NexusTreeNode("axis",NexusExtractor.AttrClassName, axis,new NexusGroupData(axisValue)));
		axis.addChildNode(new NexusTreeNode("primary",NexusExtractor.AttrClassName, axis,new NexusGroupData(primaryValue)));
		axis.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, axis,new NexusGroupData(units)));
		axis.setIsPointDependent(isPointDependent);
		getDetTree(detName).addChildNode(axis);
	}

	/**
	 * Adds the specified Axis to the named detector
	 * @param detName The name of the detector to add data to
	 * @param name The name of the Axis
	 * @param axisSDS The values of the Axis, i.e the actual values
	 * @param units The units the axis is specified in
	 * @param isPointDependent If this data should be added to the nexus at every point set this to true, if its a one off, make this false
	 */
	public void addElement(String detName, String name, NexusGroupData axisSDS, String units, boolean isPointDependent) {
		NexusTreeNode axis = new NexusTreeNode(name,NexusExtractor.SDSClassName, tree, axisSDS);
		axis.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, axis,new NexusGroupData(units)));
		axis.setIsPointDependent(isPointDependent);
		getDetTree(detName).addChildNode(axis);
	}

	/**
	 * Adds a simple note to the detector
	 * @param detName the name of the detector
	 * @param note the string contents of the note
	 */
	public void addNote(String detName, String note) {
		addNote(getDetTree(detName), note);
	}

	/**
	 * Adds a simple note to the detector
	 * @param detTree
	 * @param note
	 */
	public void addNote(INexusTree detTree, String note) {
		NexusTreeNode noteNode = new NexusTreeNode("note", NexusExtractor.NXNoteClassName, tree);
		noteNode.addChildNode(new NexusTreeNode("type", NexusExtractor.SDSClassName, noteNode, new NexusGroupData("text/plain")));
		// just add the note as description, then I don't have to worry about character encoding
		noteNode.addChildNode(new NexusTreeNode("description", NexusExtractor.SDSClassName, noteNode, new NexusGroupData(note)));

		detTree.addChildNode(noteNode);
	}

	/**
	 * @param detName
	 * @param dataName name of the child whose data is to be returned. If null or empty the first detector entry is used
	 * @param className class name of the child whose data is to be returned e.g. NexusExtractor.SDSClassName
	 * @return NexusGroupData
	 */
	@Override
	public NexusGroupData getData(String detName, String dataName, String className) {
		INexusTree detTree = getDetTree(detName);

		for(int i = 0; i < detTree.getNumberOfChildNodes(); i++) {
			INexusTree dataTree = detTree.getChildNode(i);
			if(dataTree.getName().equals(dataName) && dataTree.getNxClass().equals(className)) {
				return dataTree.getData();
			}
		}

		return null;
	}

	/**
	 * Sets the Double in the PlottableDataArray the is associated with the
	 * passed in extraName to the value handed in.
	 *
	 * @param forExtraName
	 * @param value
	 */
	public void setPlottableValue(String forExtraName, Double value) {

		for (int i = 0; i < extraNames.length; i++) {
			if (extraNames[i].equals(forExtraName)) {
				doubleData[i] = value;
				return;
			}
		}
		logger.error("extraName "+forExtraName+" not registered for detector");
	}

	@Override
	public Double[] getDoubleVals() {
		return doubleData ;
	}

	/**
	 * Use this to set the plottable Double values, if you prefer that over the
	 * convenience methods that use the scannable constructor of this class.
	 * Consider checking your extraNames and providing an outputFormat as well then.
	 *
	 * Using this method makes it difficult in inheriting classes to provide additional
	 * information, hence the use is not recommended. Use setPlottableValue instead.
	 *
	 * @param vals
	 */
	@Deprecated
	public void setDoubleVals(Double[] vals) {
		this.doubleData = vals;
	}

	@Override
	public String toString() {

		// if the doubleData has never been filled then the string this method would return may be misleading, so return nothing
		if (doubleData ==null || doubleData.length == 0 || doubleData[0] == null){
			return "";
		}

		StringBuilder output = new StringBuilder();
		try {
			String[] formatted = ScannableUtils.getFormattedCurrentPositionArray(doubleData, doubleData.length, outputFormat);
			for (String string : formatted) {
				if( !StringUtils.hasLength(string))
					throw new IllegalArgumentException("A position has zero length");
				if (output.length()>0) output.append("\t");
				output.append(string);
			}
		} catch (DeviceException e) {
			output.append("formaterror");
			logger.warn("exception formatting PlottableData",e);
		}
		return output.toString();
	}

	private void setOutputFormat(String[] outputFormat) {
		this.outputFormat = outputFormat;
	}

	@Override
	public String[] getOutputFormat() {
		return outputFormat;
	}

	@Override
	public String[] getExtraNames() {
		return extraNames;
	}

	private void setExtraNames(String[] extraNames) {
		this.extraNames = extraNames;
	}

	@Override
	public GDANexusDetectorData mergeIn(GDANexusDetectorData ntp) {
		if (ntp != null) {
			for (INexusTree branch : ntp.getNexusTree()) {
				if (branch.getNxClass().equals(NexusExtractor.NXDetectorClassName)) {
					INexusTree detTree = getDetTree(branch.getName());
					for (INexusTree detTreeBranch : branch) {
						detTree.addChildNode(detTreeBranch);
					}
				}
			}
			Double[] extraValstoAdd = ntp.getDoubleVals();
			if (extraValstoAdd != null) {
				Double[] existingVals = getDoubleVals();
				Double[] extendedVals = Arrays.copyOf(existingVals, existingVals.length + extraValstoAdd.length);
				for (int i = 0; i < extraValstoAdd.length; i++) {
					extendedVals[i + existingVals.length] = extraValstoAdd[i];
				}
				setDoubleVals(extendedVals);
			}
			String[] outputFormatstoAdd = ntp.getOutputFormat();
			if (outputFormatstoAdd != null) {
				String[] existingVals = getOutputFormat();
				String[] extendedVals = Arrays.copyOf(existingVals, existingVals.length + outputFormatstoAdd.length);
				for (int i = 0; i < outputFormatstoAdd.length; i++) {
					extendedVals[i + existingVals.length] = outputFormatstoAdd[i];
				}
				setOutputFormat(extendedVals);
			}
			String[] extraNamestoAdd = ntp.getExtraNames();
			if (extraNamestoAdd != null) {
				String[] existingVals = getExtraNames();
				String[] extendedVals = Arrays.copyOf(existingVals, existingVals.length + extraNamestoAdd.length);
				for (int i = 0; i < extraNamestoAdd.length; i++) {
					extendedVals[i + existingVals.length] = extraNamestoAdd[i];
				}
				setExtraNames(extendedVals);
			}
		}
		return this;
	}
}
