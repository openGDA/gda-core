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

package org.opengda.detector.electronanalyser.nxdetector.plugins;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;
import gda.epics.LazyPVFactory;
import gda.epics.ReadOnlyPV;
import gda.scan.ScanInformation;

public class PVArrayPlugin extends NullNXPlugin {


	private ReadOnlyPV<Double[]> pv;
	private ReadOnlyPV<Integer> dataPointsPV;
	private String dataName;
	private String dataUnit;
	private static final Logger logger=LoggerFactory.getLogger(PVArrayPlugin.class);
//	private RegionScannable regions;
	private String regionName;


	public PVArrayPlugin(String pvName, String datapointspv) {
		pv = LazyPVFactory.newReadOnlyDoubleArrayPV(pvName);
		dataPointsPV=LazyPVFactory.newReadOnlyIntegerPV(datapointspv);
	}

	@Override
	public String getName() {
		return getDataName();
	}
	public String getUnit() {
		return getDataUnit();
	}
	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
	}

	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
	}

	@Override
	public void atCommandFailure() throws Exception {
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}

	@Override
	public Vector<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {

		double[] data;
		Dataset ds;
		try {
			Integer numElements = dataPointsPV.get();
			logger.debug("Number of data points in the spectrum {}", numElements);
			data = ArrayUtils.toPrimitive(pv.get(numElements));
			ds = new DoubleDataset(data, new int[] {data.length});
			logger.debug("Spectrum length: {}", data.length);
			ds.setName(getName());
		} catch (IOException e) {
			throw new RuntimeException("Failed to get data from " +pv.getPvName(), e);
		}

		Vector<NXDetectorDataAppender> appenders = new Vector<NXDetectorDataAppender>();
		appenders.add(new NXDetectorDatasetAppender(ds, getUnit(), regionName));
		return appenders;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public String getDataUnit() {
		return dataUnit;
	}

	public void setDataUnit(String dataUnit) {
		this.dataUnit = dataUnit;
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

}
class NXDetectorDatasetAppender implements NXDetectorDataAppender {


	private static final Logger logger=LoggerFactory.getLogger(NXDetectorDatasetAppender.class);
	private Dataset dataset;
	private String unit;
	private String regionName;

	public NXDetectorDatasetAppender(Dataset ds, String unit, String regionName) {
		dataset=ds;
		this.unit=unit;
		this.regionName=regionName;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException{

		try {
			if (regionName != null) {
				readoutDatasetIntoNXDetectorData(data,dataset,detectorName,regionName);
			} else {
				readoutDatasetIntoNXDetectorData(data,dataset,detectorName, String.valueOf(dataset.getShape()[0]));
			}
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
	private void readoutDatasetIntoNXDetectorData(NXDetectorData data, Dataset ds, String detectorName, String node) throws DeviceException {
		int[] dims = ds.getShape();
		if (dims.length == 0) {
			logger.warn("Dimensions of data from " + detectorName + " are zero length");
			return;
		}

		NexusGroupData ngd = NexusGroupData.createFromDataset(ds);
		if (ds.getDType() == Dataset.FLOAT32) {
			ngd = ngd.asDouble();
		}
		//logger.warn("dimension = {}, data = {}", dims[0],ds.getBuffer());
		data.addData(detectorName, regionName+"_"+ds.getName(), ngd, unit, 1);
	}
}