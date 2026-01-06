/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.factory.FactoryException;
import uk.ac.gda.epics.nexus.device.DetectorDataEntry;

/**
 * Class to be used with scan command, to override some of the complexities
 * present in the parent classes
 */
public class Xspress3MiniSingleChannelDetector extends Xspress3Detector {

	private static final String ARRAY = "Array";

	private static final String TOTAL = "Total";

	private static final String START_AND_SIZE = "StartAndSize";

	private static final Logger logger = LoggerFactory.getLogger(Xspress3MiniSingleChannelDetector.class);

	private boolean useParentClassMethods;
	private int[] recordRois = {};
	private String [] initialExtraNames = {};
	private String [] initialOutputFormats = {};
	private boolean isFirstPoint = true;

	protected final HashMap<String,DetectorDataEntry<?>> detectorDataEntryMap = new HashMap<>();
	protected final HashMap<String,Object> dataMapToWrite = new HashMap<>();

	private final HashMap<Integer,Integer[]>  cachedRoiStartAndSize = new HashMap<>();

	private static final String SUMMED_ARRAY_RECORD_NAME ="SummedArray";
	private static final String SUMMED_TOTAL_ARRAY_RECORD_NAME ="SummedTotal";

	private transient Xspress3MiniController miniController;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		setExtraNames(new String[] { getName(), SUMMED_TOTAL_ARRAY_RECORD_NAME });
		setOutputFormat(new String[] {DEFAULT_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT});
		miniController = (Xspress3MiniController)controller;
		super.configure();
		// cache initial formats
		initialOutputFormats = getOutputFormat();
		initialExtraNames = getExtraNames();
	}

	@Override
	public void collectData() throws DeviceException {
		logger.info("collecting data from Xspress3Mini Fluorescence Detector");
		miniController.setTriggerMode(TRIGGER_MODE.Burst);
		miniController.doStart();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		while(miniController.getStatus() == BUSY) {
			Thread.sleep(50);
		}
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		final int[] sumData = getSummedData()[0];
		final int totalSumDataIntensity =  Arrays.stream(sumData).sum();
		final double[][] roisData = (recordRois.length != 0)? getRoiData(recordRois): null;

		dataMapToWrite.clear();
		if (detectorDataEntryMap.isEmpty()) setDetectorDataEntryMap();

		dataMapToWrite.put(SUMMED_ARRAY_RECORD_NAME,sumData);
		dataMapToWrite.put(SUMMED_TOTAL_ARRAY_RECORD_NAME,totalSumDataIntensity);

		if ((recordRois.length != 0) && (roisData!=null)) {
			for (int index = 0; index<this.recordRois.length;index++) {
				final String roiArrayRecordName = getRoiArrayRecordName(index);
				final String roiTotalRecordName = getRoiTotalRecordName(index);
				final String roiStartSizeRecordName = getRoiStartSizeRecordName(index);
				dataMapToWrite.put(roiArrayRecordName, roisData[index]);
				dataMapToWrite.put(roiTotalRecordName,  Arrays.stream(roisData[index]).sum());
				dataMapToWrite.put(roiStartSizeRecordName, cachedRoiStartAndSize.get(recordRois[index]));
			}
		}
		setDetectorDataEntryMap(dataMapToWrite);
		//disable per scan monitors for subsequent readouts
		detectorDataEntryMap.values().stream().forEach(entry -> entry.setEnabled(!entry.getName().contains(START_AND_SIZE) || isFirstPoint));
		return getDetectorData();
	}

	/**
	 * Get data array for specific ROI, Adding that method here since it's
	 * for a single channel multiple roi device
	 * @param recordRois
	 * @return double[][]
	 * @throws DeviceException
	 */
	public double[][] getRoiData(int[] recordRois) throws DeviceException {
		return miniController.readoutRoiArrayData(recordRois);
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		miniController.setAcquireTime(collectionTime);
	}

	public void setRoiSumStartAndSize(int startX, int sizeX) throws DeviceException {
		/**
		 * Sets :ROISUM1:MinX and :ROISUM1:SizeX, these PV are for summing multiple channels
		 */
		logger.debug("Setting roi sum limits {} - {}", startX, startX+sizeX );
		miniController.setRoiSumStartAndSize(startX, sizeX);
	}

	public void setRoiStartAndSize(int roiNo, int startX, int sizeX) throws DeviceException {
		/**
		 * Sets AreaDetector plugin ROI PVs start and size, roiNo can be 1 to 6
		 */
		logger.debug("Setting roi limits {} - {}", startX, startX+sizeX );
		miniController.setRoiStartAndSize(roiNo, startX, sizeX);
	}

	public int[] getRoiStartAndSize(int roiNo) throws DeviceException {
		/**
		 * Get AreaDetector plugin ROI start and size, roiNo can be 1 to 6
		 */
		logger.debug("Getting roi limits for ROI {}", roiNo);
		return miniController.getRoiStartAndSize(roiNo);
	}

	@Override
	public void atScanStart() throws DeviceException {
		isFirstPoint = true;
		if(useParentClassMethods) {
			super.atScanStart();
		}
		for (int index = 0; index<this.recordRois.length;index++) {
			cachedRoiStartAndSize.put(recordRois[index], IntStream.of(getRoiStartAndSize(recordRois[index])).boxed().toArray( Integer[]::new));
		}
		setDetectorDataEntryMap();
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		if(useParentClassMethods) {
			super.atScanLineStart();
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		if(useParentClassMethods) {
			super.atScanEnd();
		}
		isFirstPoint = false;
	}

	@Override
	public void atPointEnd() throws DeviceException {
		if(useParentClassMethods) {
			super.atPointEnd();
		}
		isFirstPoint = false;
	}

	public void setUseParentClassMethods(boolean useParentClassMethods) {
		this.useParentClassMethods = useParentClassMethods;
	}

	@Override
	public int getStatus() throws DeviceException {
		return miniController.getStatus();
	}

	@Override
	public String[] getExtraNames() {
		return extraNames;
	}

	public void setRecordRois(int[] recordRois) {
		this.recordRois = recordRois;
		updateExtraNamesAndOutputFormatWithRecordRois(recordRois);
	}

	private void updateExtraNamesAndOutputFormatWithRecordRois(int[] recordRois) {
		String[] newExtraNames = Arrays.copyOf(initialExtraNames, initialExtraNames.length+recordRois.length);
		String[] newOutputFormat = Arrays.copyOf(initialOutputFormats, initialOutputFormats.length+recordRois.length);
		for (int i = 0; i<this.recordRois.length;i++) {
			newExtraNames[initialExtraNames.length+i] = getRoiName(recordRois[i])+TOTAL;
			newOutputFormat[initialOutputFormats.length+i] = DEFAULT_OUTPUT_FORMAT;
		}
		setExtraNames(newExtraNames);
		setOutputFormat(newOutputFormat);
	}

	@Override
	public NexusTreeProvider getFileStructure() throws DeviceException{
		logger.info("Setting up initial file structure for device \"{}\"", getName());
		setDetectorDataEntryMap();
		return getDetectorData();
	}

	private NexusTreeProvider getDetectorData() {
		final NXDetectorData detectorData =  new NXDetectorData(this);
		// add detector data
		for (var e : detectorDataEntryMap.entrySet()) {
			DetectorDataEntry<?> dde = e.getValue();
			if (Boolean.TRUE.equals(e.getValue().isEnabled())) {
				INexusTree data = detectorData.addData(getName(), dde.getName(), new NexusGroupData(dde.getValue()),dde.getUnits(),dde.getIsDetectorEntry());
				if (dde.getName().contains(getName())) {
					data.addChildNode(new NexusTreeNode("local_name",NexusExtractor.AttrClassName, data, new NexusGroupData(String.format("%s.%s", getName(), dde.getName()))));
				}
			}
		}
		// set plottable values for all Total
		detectorDataEntryMap.values().stream().filter(entry->entry.getName().contains(TOTAL)).forEach(entry->detectorData.setPlottableValue(entry.getName(),entry.getValue().getDouble()));
		return detectorData;
	}

	protected void setDetectorDataEntryMap(HashMap<?, ?>... data) {
		logger.debug("Configuring detectorDataEntryMap with values of length {}", data.length);
		detectorDataEntryMap.clear();

		detectorDataEntryMap.put(SUMMED_ARRAY_RECORD_NAME,
				new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(IntegerDataset.class, getMCASize()):DatasetFactory.createFromObject(IntegerDataset.class,data[0].get(SUMMED_ARRAY_RECORD_NAME), getMCASize()),SUMMED_ARRAY_RECORD_NAME,"Counts",true));
		detectorDataEntryMap.put(SUMMED_TOTAL_ARRAY_RECORD_NAME,
				new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(IntegerDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(SUMMED_TOTAL_ARRAY_RECORD_NAME), 1),SUMMED_TOTAL_ARRAY_RECORD_NAME,"Counts",true));

		for (int index = 0; index<this.recordRois.length;index++) {
			final String roiArrayRecordName = getRoiArrayRecordName(index);
			final String roiTotalRecordName = getRoiTotalRecordName(index);
			final String roiStartSizeRecordName = getRoiStartSizeRecordName(index);
			detectorDataEntryMap.put(roiArrayRecordName,
					new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(DoubleDataset.class, getMCASize()):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(roiArrayRecordName),getMCASize()),roiArrayRecordName,"Counts",true));
			detectorDataEntryMap.put(roiTotalRecordName,
					new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(IntegerDataset.class, 1):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(roiTotalRecordName), 1),roiTotalRecordName,"Counts",true));
			detectorDataEntryMap.put(roiStartSizeRecordName,
					new DetectorDataEntry<>(data.length==0? DatasetFactory.zeros(IntegerDataset.class, 2):DatasetFactory.createFromObject(DoubleDataset.class,data[0].get(roiStartSizeRecordName), 2),roiStartSizeRecordName,"",true));
		}

		logger.debug("Configuring detectorDataEntryMap finished");
	}

	private String getRoiStartSizeRecordName(int index) {
		return getRoiName(recordRois[index]) + START_AND_SIZE;
	}

	private String getRoiTotalRecordName(int index) {
		return getRoiName(recordRois[index]) + TOTAL;
	}

	private String getRoiArrayRecordName(int index) {
		return getRoiName(recordRois[index]) + ARRAY;
	}

	private String getRoiName(int index) {
		return String.format("roi%1d", index);
	}
}
