/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.eclipse.january.dataset.Dataset;

/**
 * Extracts dataset from NexusTreeprovider and passes to processors
 */
public class NexusProviderDatasetProcessor implements NexusTreeProviderProcessor {

	private String className;
	private String dataName;
	private String detName;
	private List<DataSetProcessor> processors;
	private DatasetCreator datasetCreator;

	public DatasetCreator getDatasetCreator() {
		return datasetCreator;
	}


	public void setDatasetCreator(DatasetCreator datasetCreator) {
		this.datasetCreator = datasetCreator;
	}


	@SuppressWarnings("unused")
	protected Dataset getDatasetFromNexusGroupData(NexusGroupData ngd)  throws Exception {
		return ngd.toDataset(true);
	}


	@Override
	public GDANexusDetectorData process(final GDANexusDetectorData nexusTreeProvider) throws Exception {
		if( !isEnabled())
			return null;
		NexusGroupData ngd = nexusTreeProvider.getData(detName, dataName, className);
		if (ngd == null) {
			throw new Exception("No data found for detName:" + detName + " dataName:" + dataName + "+ className:"
					+ className);
		}
		Dataset dataset = getDatasetFromNexusGroupData(ngd);
		if( datasetCreator != null)
			dataset = datasetCreator.createDataSet(dataset);

		GDANexusDetectorData result=null;
		for (DataSetProcessor processor : processors) {
			GDANexusDetectorData res = processor.isEnabled() ? processor.process(detName, dataName, dataset) : null;
			if (res != null){
				if( result == null)
					result = new NXDetectorData();
				result = result.mergeIn(res);
			}

		}
		return result;
	}

	public NexusProviderDatasetProcessor(String detName, String dataName, String className,
			List<DataSetProcessor> processors, DatasetCreator datasetCreator) {
		super();
		this.className = className;
		this.dataName = dataName;
		this.detName = detName;
		this.processors = processors;
		this.datasetCreator = datasetCreator;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public String getDetName() {
		return detName;
	}

	public void setDetName(String detName) {
		this.detName = detName;
	}

	public List<DataSetProcessor> getProcessors() {
		return processors;
	}

	//only to be called before a scan as it effect extraNames and outputFormat
	public void setProcessors(List<DataSetProcessor> newProcessors) {
		this.processors = newProcessors;
	}

	@Override
	public Collection<String> getExtraNames() {
		if( !isEnabled())
			return null;

		Collection<String> allExtraNames = new Vector<String>();
		for (DataSetProcessor processor : processors) {
			Collection<String> extraNames = processor.isEnabled() ? processor.getExtraNames() : null;
			if (extraNames != null) {
				allExtraNames.addAll(extraNames);
			}
		}
		return allExtraNames.isEmpty() ? null : allExtraNames;
	}

	@Override
	public Collection<String> getOutputFormat() {
		if( !isEnabled())
			return null;

		Collection<String> totalList = new Vector<String>();
		for (DataSetProcessor processor : processors) {
			Collection<String> itemList = processor.isEnabled() ? processor.getOutputFormat() : null;
			if (itemList != null) {
				totalList.addAll(itemList);
			}
		}
		return totalList.isEmpty() ? null : totalList;
	}


	@Override
	public boolean isEnabled() {
		for (DataSetProcessor processor : processors) {
			if( processor.isEnabled())
				return true;
		}
		return false;
	}

}
