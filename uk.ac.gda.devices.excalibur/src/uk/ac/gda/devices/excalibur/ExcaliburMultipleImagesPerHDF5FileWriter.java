/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.excalibur;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.PathConstructor;
import gda.device.detector.addetector.filewriter.MultipleImagesPerHDF5FileWriter;
import gda.epics.CachedLazyPVFactory;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import gda.util.OSCommandRunner;
import gda.util.OSCommandRunner.LOGOPTION;

public class ExcaliburMultipleImagesPerHDF5FileWriter extends MultipleImagesPerHDF5FileWriter {

	private static Logger logger = LoggerFactory.getLogger(ExcaliburMultipleImagesPerHDF5FileWriter.class);

	private String gapEnabledPVName;
	PV<Integer> gapEnabledPV;

	public static final String NODE_HDF5_FILE_NAME = "FileName";

	private int nNodes = 6;
	private String nodePVnamePrefix = "BL13J-EA-EXCBR-01:";
	private String nodePVnamePrefixTemplate = "BL13J-EA-EXCBR-01:%d";
	private String nodePVnameHDFComponent = ":HDF5:";

	private boolean nodeFileFormatToBeSet = true;
	private String nodeFileNamePrefixFormat = "excalibur-%d-";
	private String nodeFileTemplate = "%s%s-%d.hdf";

	// args for the post-collection script
	private boolean scriptEnabled = false;
	private String scriptFileName = "dls-vds-gen.py";
	private String sourceNodeArg = "/entry/instrument/detector/data";
	private String targetNodeArg = "/entry/instrument/detector/data";
	private int stripeSpacingArg = 0;
	private int moduleSpacingArg = 121;
	private int shapeHeightArg = 259;
	private int shapeWidthArg = 2069;
	private boolean useEmptyArg = true;

	private CachedLazyPVFactory cachedPVs;

	private String[] cachedNodeHDF5Suffixes;

	//private Map<String, PV<String>> nameToPVmap = new HashMap<String, PV<String>>();
	private List<PV<String>> nodeFileNamePVs;

	public boolean isNodeFileFormatToBeSet() {
		return nodeFileFormatToBeSet;
	}

	public void setNodeFileFormatToBeSet(boolean nodeFileFormatToBeSet) {
		this.nodeFileFormatToBeSet = nodeFileFormatToBeSet;
	}

	public String getNodeFileTemplate() {
		return nodeFileTemplate;
	}

	public void setNodeFileTemplate(String nodeFileTemplate) {
		this.nodeFileTemplate = nodeFileTemplate;
	}

	public String getScriptFileName() {
		return scriptFileName;
	}

	public void setScriptFileName(String scriptFileName) {
		this.scriptFileName = scriptFileName;
	}

	public boolean isUseEmptyArg() {
		return useEmptyArg;
	}

	public void setUseEmptyArg(boolean useEmptyArg) {
		this.useEmptyArg = useEmptyArg;
	}

	public String getNodeFileNamePrefixFormat() {
		return nodeFileNamePrefixFormat;
	}

	public void setNodeFileNamePrefixFormat(String nodeFileNamePrefixFormat) {
		this.nodeFileNamePrefixFormat = nodeFileNamePrefixFormat;
	}

	public String getNodePVnameHDFComponent() {
		return nodePVnameHDFComponent;
	}

	public void setNodePVnameHDFComponent(String nodePVnameHDFComponent) {
		this.nodePVnameHDFComponent = nodePVnameHDFComponent;
	}

	public String getNodePVnamePrefix() {
		return nodePVnamePrefix;
	}

	public void setNodePVnamePrefix(String nodePVnamePrefix) {
		this.nodePVnamePrefix = nodePVnamePrefix;
	}

	public String getNodePVnamePrefixTemplate() {
		return nodePVnamePrefixTemplate;
	}

	public void setNodePVnamePrefixTemplate(String nodePVnamePrefixTemplate) {
		this.nodePVnamePrefixTemplate = nodePVnamePrefixTemplate;
	}

	public int getNumberOfNodes() {
		return nNodes;
	}

	public void setNumberOfNodes(int numberOfNodes) {
		this.nNodes = numberOfNodes;
	}

	public boolean isScriptEnabled() {
		return scriptEnabled;
	}

	public void setScriptEnabled(boolean scriptEnabled) {
		this.scriptEnabled = scriptEnabled;
	}

	@Override // interface InitializingBean
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();

		cachedPVs = new CachedLazyPVFactory(nodePVnamePrefix);
		//nodeFileNamePVs = new ArrayList<PV<String>>();
		this.cachedNodeHDF5Suffixes = new String[nNodes];
		for (int i=0; i < nNodes; i++) {
			this.cachedNodeHDF5Suffixes[i] = getNodeHDF5Suffix(i+1, NODE_HDF5_FILE_NAME);
			//nameToPVmap.put(String.format("%s%d", "fileNameOnNode", (i+1)), LazyPVFactory.newStringFromWaveformPV(String.format("%s%s", nodePVnamePrefix, cachedNodeHDF5Suffixes[i])));
		}
	}

	public String getNodeHDF5Suffix(int nodeNum, String endSuffix) {
		return String.format("%d%s%s", nodeNum, nodePVnameHDFComponent, endSuffix);	//1:HDF5:FileName		//should be called once only and the output cached
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		print("ExcaliburMultipleImagesPerHDF5FileWriter: prepareForCollection");
		//setup chunking based on gap being enabled or not
		if( gapEnabledPV == null)
			gapEnabledPV = LazyPVFactory.newIntegerPV(gapEnabledPVName);

		boolean gapEnabled = gapEnabledPV.get() != 0;
		//setChunkSize0(gapEnabled ? 1 : 4);
		//setChunkSize1(gapEnabled ? 259:256);
		//setChunkSize2(gapEnabled ? 2069: 2048);
		//setDsetSize1(gapEnabled ? 1796: 1536);
		//setDsetSize2(gapEnabled ? 2069: 2048);
		super.prepareForCollection(numberImagesPerCollection, scanInfo);

		if (nodeFileFormatToBeSet) {
			//cachedPVs.getPVStringAsBytes(getNodeHDF5Suffix(1, "FileName")).putWait(value);
			PV<String> fileNamePVForNode1 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:1:HDF5:FileName");
			//fileNamePVForNode1.putWait("excalibur-1-");
			fileNamePVForNode1.putWait(String.format(nodeFileNamePrefixFormat, 1));
			PV<String> fileNamePVForNode2 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:2:HDF5:FileName");
			//fileNamePVForNode2.putWait("excalibur-2-");
			fileNamePVForNode2.putWait(String.format(nodeFileNamePrefixFormat, 2));
			PV<String> fileNamePVForNode3 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:3:HDF5:FileName");
			//fileNamePVForNode3.putWait("excalibur-3-");
			fileNamePVForNode3.putWait(String.format(nodeFileNamePrefixFormat, 3));
			PV<String> fileNamePVForNode4 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:4:HDF5:FileName");
			//fileNamePVForNode4.putWait("excalibur-4-");
			fileNamePVForNode4.putWait(String.format(nodeFileNamePrefixFormat, 4));
			PV<String> fileNamePVForNode5 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:5:HDF5:FileName");
			//fileNamePVForNode5.putWait("excalibur-5-");
			fileNamePVForNode5.putWait(String.format(nodeFileNamePrefixFormat, 5));
			PV<String> fileNamePVForNode6 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:6:HDF5:FileName");
			//fileNamePVForNode6.putWait("excalibur-6-");
			fileNamePVForNode6.putWait(String.format(nodeFileNamePrefixFormat, 6));

			PV<String> fileTemplatePVForNode1 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:1:HDF5:FileTemplate");
			if ( !fileTemplatePVForNode1.get().equals(nodeFileTemplate)) {
				//fileTemplatePVForNode1.putWait("excalibur-1-");
				//fileTemplatePVForNode1.putWait("%s%s-%d.hdf");
				fileTemplatePVForNode1.putWait(nodeFileTemplate);
			}

			PV<String> fileTemplatePVForNode2 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:2:HDF5:FileTemplate");
			if ( !fileTemplatePVForNode2.get().equals(nodeFileTemplate)) {
				//fileTemplatePVForNode2.putWait("excalibur-2-");
				//fileTemplatePVForNode2.putWait("%s%s-%d.hdf");
				fileTemplatePVForNode2.putWait(nodeFileTemplate);
			}

			PV<String> fileTemplatePVForNode3 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:3:HDF5:FileTemplate");
			if ( !fileTemplatePVForNode3.get().equals(nodeFileTemplate)) {
				//fileTemplatePVForNode3.putWait("excalibur-3-");
				//fileTemplatePVForNode3.putWait("%s%s-%d.hdf");
				fileTemplatePVForNode3.putWait(nodeFileTemplate);
			}

			PV<String> fileTemplatePVForNode4 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:4:HDF5:FileTemplate");
			if ( !fileTemplatePVForNode4.get().equals(nodeFileTemplate)) {
				//fileTemplatePVForNode4.putWait("excalibur-4-");
				//fileTemplatePVForNode4.putWait("%s%s-%d.hdf");
				fileTemplatePVForNode4.putWait(nodeFileTemplate);
			}

			PV<String> fileTemplatePVForNode5 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:5:HDF5:FileTemplate");
			if ( !fileTemplatePVForNode5.get().equals(nodeFileTemplate)) {
				//fileTemplatePVForNode5.putWait("excalibur-5-");
				//fileTemplatePVForNode5.putWait("%s%s-%d.hdf");
				fileTemplatePVForNode5.putWait(nodeFileTemplate);
			}

			PV<String> fileTemplatePVForNode6 = LazyPVFactory.newStringFromWaveformPV("BL13J-EA-EXCBR-01:6:HDF5:FileTemplate");
			if ( !fileTemplatePVForNode6.get().equals(nodeFileTemplate)) {
				//fileTemplatePVForNode6.putWait("excalibur-6-");
				//fileTemplatePVForNode6.putWait("%s%s-%d.hdf");
				fileTemplatePVForNode6.putWait(nodeFileTemplate);
			}
		}
	}

	public String getGapEnabledPVName() {
		return gapEnabledPVName;
	}

	public void setGapEnabledPVName(String gapEnabledPVName) {
		this.gapEnabledPVName = gapEnabledPVName;
	}

	@Override
	public void completeCollection() throws Exception {
		print("ExcaliburMultipleImagesPerHDF5FileWriter: completeCollection");
		ScanInformation currScanInfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		int scanNumber = currScanInfo.getScanNumber();
		int nScanPts = currScanInfo.getNumberOfPoints();

		String dataDirPath = PathConstructor.createFromDefaultProperty();
		String inFileNames = "";
		String inFileNamePrefix = "";
		for (int i=0; i < nNodes; i++) {
			inFileNamePrefix = String.format(nodeFileNamePrefixFormat, i+1);
			inFileNames += String.format("%s-%d.hdf ", inFileNamePrefix, scanNumber);	//excalibur-1--132515.hdf excalibur-2--132515.hdf ... excalibur-6--132515.hdf
		}

		String outFileName = String.format("excalibur-%d.hdf", scanNumber); 		//excalibur-132515.hdf
		int shapeFramesArg = nScanPts; 												//numberImagesPerCollection?

		PV<Integer> counterDepthPV = LazyPVFactory.newIntegerPV("BL13J-EA-EXCBR-01:CONFIG:ACQUIRE:CounterDepth");
		int counterDepth = counterDepthPV.get();
		String dataTypeArg = counterDepth < 3 ? "int16" : "int32";
		//super.completeCollection();
		StringBuffer vdsCmd = new StringBuffer(String.format(
				"%s %s -f %s --source_node %s --target_node %s -s %d -m %d --shape %d %d %d -t %s -o %s", scriptFileName, dataDirPath, inFileNames, sourceNodeArg, targetNodeArg,
				stripeSpacingArg, moduleSpacingArg, shapeFramesArg, shapeHeightArg, shapeWidthArg, dataTypeArg, outFileName));

		if (useEmptyArg) {
			vdsCmd.append(" --empty");
		}

		//this.getFileNameTemplate();
		//this.getFileTemplate();
		logger.info("VDS command to be run: {}", vdsCmd);
		print("VDS command to be run: " + vdsCmd.toString());
		if (scriptEnabled) {
			OSCommandRunner.runNoWait(vdsCmd.toString(), LOGOPTION.ALWAYS, null);
		}
		super.completeCollection();
	}

	private void print(String msg) {
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print(msg);
		}
	}

	/*@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		NXDetectorDataAppender dataAppender;
		if(isEnabled())
		{
			//wait until the NumCaptured_RBV is equal to or exceeds maxToRead.
			checkErrorStatus();
			try {
				getNdFile().getPluginBase().checkDroppedFrames();
			} catch (Exception e) {
				throw new DeviceException("Error in " + getName(), e);
			}
			if (firstReadoutInScan) {
				dataAppender = new NXDetectorDataFileLinkAppender(expectedFullFileName);
				numToBeCaptured=1;
				numCaptured=0;
			}
			else {
				dataAppender = new NXDetectorDataNullAppender();
				numToBeCaptured++;
			}
			while( numCaptured< numToBeCaptured){
				try {
					numCaptured = getNdFilePHDF5().getNumCaptured_RBV();
				} catch (Exception e) {
					throw new DeviceException("Error in getCapture_RBV" + getName(), e);
				}
				Thread.sleep(50);
			}
		} else {
			dataAppender = new NXDetectorDataNullAppender();
		}
		firstReadoutInScan = false;
		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		appenders.add(dataAppender);
		return appenders;
	}*/
}
