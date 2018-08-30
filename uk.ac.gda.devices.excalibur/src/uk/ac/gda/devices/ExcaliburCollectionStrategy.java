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

package uk.ac.gda.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.detector.addetector.triggering.SingleExposureStandard;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import gda.util.OSCommandRunner;
import gda.util.OSCommandRunner.LOGOPTION;

public class ExcaliburCollectionStrategy extends SingleExposureStandard implements InitializingBean{
	private static final Logger logger = LoggerFactory.getLogger(ExcaliburCollectionStrategy.class);

	private PV<String> operationModePV;
	private boolean burst=false;
	private boolean softwareTrigger=false;
	private String operationModePVName = "EXCALIBUR:CONFIG:ACQUIRE:OperationMode";
	private boolean started;

	private int triggerMode=StandardTriggerMode.INTERNAL.ordinal();


	public static final String NODE_HDF5_FILE_NAME = "FileName";

	public int nNodes = 6;
	public String nodePVnamePrefix = "BL13J-EA-EXCBR-01:";
	public String nodePVnamePrefixTemplate = "BL13J-EA-EXCBR-01:%d";
	public String nodePVnameHDFComponent = ":HDF5:";

	public String nodeOutFileNamePrefixFormat = "excalibur-%d-";
	public String vdsOutputFileNameFormat = "excalibur-vds-%d.hdf";

	// args for the post-collection script
	public boolean scriptEnabled = true;
	public String scriptFileName = "/dls_sw/prod/tools/RHEL6-x86_64/defaults/bin/dls-vds-gen.py"; //needs fixing
	public String sourceNodeArg = "/entry/instrument/detector/data";
	public String targetNodeArg = "/entry/instrument/detector/data";
	public int stripeSpacingArg = 0;
	public int moduleSpacingArg = 121;
	public int shapeHeightArg = 259;
	public int shapeWidthArg = 2069;
	public boolean useEmptyArg = true;

	public int getTriggerMode() {
		return triggerMode;
	}

	public void setTriggerMode(int triggerMode) {
		this.triggerMode = triggerMode;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		operationModePV = LazyPVFactory.newStringPV(operationModePVName);
	}

	public ExcaliburCollectionStrategy(ADBase adBase) {
		super(adBase, -1);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImagesPerCollection, ScanInformation scanInfo) throws Exception {
		super.prepareForCollection(collectionTime, 1, scanInfo);
		operationModePV.putWait(burst ? "Burst" : "Normal");
		getAdBase().setImageModeWait(ImageMode.MULTIPLE);
		getAdBase().setTriggerMode(triggerMode);
		int numImagesToTrigger = 1;
		if(!softwareTrigger){
			ScanInformation scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
			if( scanInformation != null){
				int[] dimensions = scanInformation.getDimensions();
				numImagesToTrigger = dimensions[dimensions.length-1];
			}
		}
		getAdBase().setNumImages(numImagesToTrigger);

	}


	@Override
	public void prepareForLine() throws Exception {
		super.prepareForLine();
		started = false;
	}

	@Override
	public void collectData() throws Exception {
		if( softwareTrigger  || !started){
			super.collectData();
			started = true;
		}
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		if( softwareTrigger){
			super.waitWhileBusy();
		}
	}


	@Override
	public void completeLine() throws Exception {
		if( !softwareTrigger ){
			super.waitWhileBusy();
		}
		super.completeLine();
	}

	public boolean isBurst() {
		return burst;
	}

	public void setBurst(boolean burst) {
		this.burst = burst;
	}

	public String getOperationModePVName() {
		return operationModePVName;
	}

	public void setOperationModePVName(String operationModePVName) {
		this.operationModePVName = operationModePVName;
	}

	public boolean isSoftwareTrigger() {
		return softwareTrigger;
	}

	public void setSoftwareTrigger(boolean softwareTrigger) {
		this.softwareTrigger = softwareTrigger;
	}

	@Override
	public void stop() throws Exception {
		logger.error("Stop called for Excalibur but not possible as it will lead to a fault in the FEM comms");
	}

	@Override
	public void completeCollection() throws Exception {
		super.completeCollection();
		if (scriptEnabled) {
			executeScript();
		}
	}

	public void executeScript() throws Exception {
		print("ExcaliburCollectionStrategy: prepareForCollection");

		ScanInformation currScanInfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		int scanNumber = currScanInfo.getScanNumber();
		int nScanPts = currScanInfo.getNumberOfPoints();

		String dataDirPath = PathConstructor.createFromDefaultProperty();
		String inFileNames = "";
		String inFileNamePrefix = "";
		for (int i=0; i < nNodes; i++) {
			inFileNamePrefix = String.format(nodeOutFileNamePrefixFormat, i+1);
			inFileNames += String.format("%s-%d.hdf ", inFileNamePrefix, scanNumber);	//excalibur-1--132515.hdf excalibur-2--132515.hdf ... excalibur-6--132515.hdf
		}

		//String outFileName = String.format("excalibur-vds-%d.hdf", scanNumber); 		//excalibur-132515.hdf
		String outFileName = String.format(vdsOutputFileNameFormat, scanNumber); 		//excalibur-132515.hdf

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
	}

	private void print(String msg) {
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print(msg);
		}
	}
}
