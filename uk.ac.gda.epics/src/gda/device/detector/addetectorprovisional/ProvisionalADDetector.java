/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.addetectorprovisional;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.addetector.filewriter.FileWriter;
import gda.device.detector.addetector.triggering.ADTriggeringStrategy;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.scannable.PositionCallableProvider;
import gda.factory.FactoryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

// TODO FileWrite should be largely (or completely) operated via the ADPlugin interface

/**
 * Creates an ADDetector and delegates to it. Everythin that is overriden in ADDetector is also ovveriden here except
 * for: hiding: <blockquote>
 * 
 * <pre>
 * 
 * 
 * - public void setAdBase(ADBase adBase) {
 * - public void configure() throws FactoryException {
 * - public NDStats getNdStats() {
 * - public NDArray getNdArray() {
 * - public boolean isComputeStats() {
 * - public boolean isComputeCentroid() {
 * - public boolean isReadArray() {
 * - public NexusTreeProvider getMetaDataProvider() {
 * - public void afterPropertiesSet() throws Exception {
 * - public boolean isUsePipeline() {
 * - public boolean isDisableCallbacks() {
 * </pre>
 * 
 * </blockquote> For now all components are set in the constructor. Could make this more spring friendly once stable.
 */

public class ProvisionalADDetector extends gda.device.detector.addetector.ADDetector implements NexusDetector, PositionCallableProvider<NexusTreeProvider> {

	private static final String REMOVED_FROM_PROVISIONAL_ADDETECTOR = "Not supported by provisional ADDetector";
	private List<ADDetectorPlugin<Double[]>> pluginList = new ArrayList<ADDetectorPlugin<Double[]>>();

	public ProvisionalADDetector(String name, ADBase adBase, ADTriggeringStrategy collectionStrategy, FileWriter fileWriter) throws Exception {
		setName(name);
		setAdBase(adBase);
		setCollectionStrategy(collectionStrategy);
		setFileWriter(fileWriter);

		super.setReadArray(false);

		afterPropertiesSet();
		configure();
	}

	// Configuration options:

	public void setPluginList(List<ADDetectorPlugin<Double[]>> adDetectorPluginList) {
		this.pluginList = adDetectorPluginList;

	}

	// public void setReadAcquisitionTime(boolean readAcquisitionTime) {

	// public void setReadAcquisitionPeriod(boolean readAcquisitionPeriod) {

	// public void setReadFilepath(boolean readFilepath) {

	// public void setCheckFileExists(boolean checkFileExists) {

	// public void setDescription(String description) {

	// public void setDetectorType(String detectorType) {

	// public void setDetectorID(String detectorID) {

	// Getters:

	public List<ADDetectorPlugin<Double[]>> getPluginList() {
		return pluginList;
	}

	// return getAdBase();

	// public ADTriggeringStrategy getCollectionStrategy() {

	// public FileWriter getFileWriter() {

	// public NDFile getNdFile() {

	// public String getDescription() throws DeviceException {

	// public String getDetectorID() throws DeviceException {

	// public String getDetectorType() throws DeviceException {

	// public boolean isCheckFileExists() {

	// public boolean isReadAcquisitionTime() {

	// public boolean isReadAcquisitionPeriod() {

	// public boolean isReadFilepath() {

	// public boolean createsOwnFiles() throws DeviceException {

	// Removed:

	@Override
	public void setNdStats(NDStats ndStats) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

//	@Override
//	public void setNdArray(NDArray ndArray) {
//		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
//	}

	@Override
	public void setComputeStats(boolean computeStats) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void setComputeCentroid(boolean computeCentroid) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

//	@Override
//	public void setReadArray(boolean readArray) {
//		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
//	}

	@Override
	public void setDisableCallbacks(boolean disableCallbacks) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void setUsePipeline(boolean usePipeline) {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	@Override
	public void reset() throws Exception {
		throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	}

	// @Override
	// public void setNdFile(NDFile ndFile) {
	// throw new RuntimeException(REMOVED_FROM_PROVISIONAL_ADDETECTOR);
	// }

	// ////

	@Override
	public void configure() throws FactoryException {
		// Do nothing
	}
	
	
	@Override
	public void setInputNames(String[] names) {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public void setExtraNames(String[] names) {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}

	@Override
	public void setOutputFormat(String[] names) {
		throw new RuntimeException(UNSUPPORTED_PART_OF_SCANNABLE_INTERFACE);
	}
	
	@Override
	protected void configureExtraNamesAndOutputFormat() {
		// Do nothing
	}
	
	@Override
	public String[] getInputNames() {
		return new String[] {};
	}
	
	@Override
	public String[] getExtraNames() {
		List<String> extraNames = new ArrayList<String>();
		if (isReadAcquisitionTime()) {
			extraNames.add("count_time");
		}
		if (isReadAcquisitionPeriod()) {
			extraNames.add("period");
		}
		if (isReadFilepath() && !getFileWriter().isLinkFilepath()) {
			extraNames.add(FILEPATH_EXTRANAME);
		}
		for (ADDetectorPlugin<Double[]> plugin : pluginList) {
			extraNames.addAll(plugin.getInputStreamFieldNames());
		}
		return extraNames.toArray(new String[] {});

	}

	@Override
	public String[] getOutputFormat() {
		List<String> formats = new ArrayList<String>();
		if (isReadAcquisitionTime()) {
			formats.add("%.2f");
		}
		if (isReadAcquisitionPeriod()) {
			formats.add("%.2f");
		}
		if (isReadFilepath() && !getFileWriter().isLinkFilepath()) {
			// used to format the double that is put into the doubleVals array in this case
			formats.add("%.2f"); // TODO I don't follow this - RobW
		}
		for (ADDetectorPlugin<Double[]> plugin : pluginList) {
			formats.addAll(plugin.getInputStreamFormats());
		}
		return formats.toArray(new String[] {});
	}

	@Override
	public void atScanStart() throws DeviceException {

		super.atScanStart(); // may enable/disable array callbacks

		try {

			int numberImagesPerCollection = getCollectionStrategy().getNumberImagesPerCollection(getCollectionTime());
			for (ADDetectorPlugin<?> plugin : pluginList) {
				plugin.prepareForCollection(numberImagesPerCollection);
			}
			getAdBase().setArrayCallbacks(areCallbacksRequired() ? 1 : 0);

		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	boolean areCallbacksRequired() {

		// TODO If the file writer requires them return true

		for (ADDetectorPlugin<?> chain : pluginList) {
			if (chain.willRequireCallbacks()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		for (ADDetectorPlugin<?> plugin : pluginList) {
			try {
				plugin.prepareForLine();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

	@Override
	protected void appendNXDetectorDataFromPlugins(NXDetectorData data) throws Exception {
		
		for (ADDetectorPlugin<Double[]> plugin : pluginList) {
			Double[] doubleVals;
			try {
				Vector<Double[]> read = plugin.read(Integer.MAX_VALUE);
				if (read.size() == 0) {
					throw new AssertionError(plugin.getName() + " input stream returned zero elements.");
				}
				doubleVals = read.get(0);
			} catch (Exception e) {
				throw new DeviceException(e);
			}
			
			addMultipleDoubleItemsToNXData(data, plugin.getInputStreamFieldNames().toArray(new String[0]), doubleVals);
		}
	}


	
	@Override
	public void atScanLineEnd() throws DeviceException {
		for (ADDetectorPlugin<?> plugin : pluginList) {
			try {
				plugin.completeLine();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
		for (ADDetectorPlugin<?> plugin : pluginList) {
			try {
				plugin.completeCollection();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

	@Override
	public void stop() throws DeviceException {
		super.stop();
		for (ADDetectorPlugin<?> plugin : pluginList) {
			try {
				plugin.stop();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		super.atCommandFailure();
		for (ADDetectorPlugin<?> plugin : pluginList) {
			try {
				plugin.atCommandFailure();
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
	}

}