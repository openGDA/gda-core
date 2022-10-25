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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.january.dataset.Dataset;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;

/**
 * Extracts dataset from NexusTreeProvider and passes to processors. The dataset is optionally transformed by a
 * {@link DatasetCreator}
 */
public class NexusProviderDatasetProcessor implements NexusTreeProviderProcessor {

	private String className;
	private String dataName;
	private String detName;
	private List<DatasetProcessor> processors;
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
		if (!isEnabled()) {
			return null;
		}
		var dataset = datasetCreator == null ? extractDataset(nexusTreeProvider)
				: datasetCreator.createDataSet(extractDataset(nexusTreeProvider));

		try {
			return getProcessors().stream().filter(DatasetProcessor::isEnabled)
					.map(processor -> processDataset(processor, dataset))
					.reduce(new NXDetectorData(), GDANexusDetectorData::mergeIn);
		} catch (DatasetProcessorException e) {
			throw e.cause;
		}
	}

	protected Dataset extractDataset(GDANexusDetectorData nexusTreeProvider) throws Exception {
		NexusGroupData ngd = nexusTreeProvider.getData(detName, dataName, className);
		if (ngd == null) {
			throw new Exception(
					"No data found for detName:" + detName + " dataName:" + dataName + "+ className:" + className);
		}
		return getDatasetFromNexusGroupData(ngd);
	}

	/**
	 * Wrapper for {@link DatasetProcessor#process(String, String, Dataset)} to convert
	 * Exception into a RuntimeException
	 */
	private GDANexusDetectorData processDataset(DatasetProcessor processor, Dataset dataset) {
		try {
			return processor.process(getDetName(), getDataName(), dataset);
		} catch (Exception e) {
			throw new DatasetProcessorException(e);
		}
	}

	public NexusProviderDatasetProcessor(String detName, String dataName, String className,
			List<DatasetProcessor> processors, DatasetCreator datasetCreator) {
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

	public List<DatasetProcessor> getProcessors() {
		return processors;
	}

	//only to be called before a scan as it effect extraNames and outputFormat
	public void setProcessors(List<DatasetProcessor> newProcessors) {
		this.processors = newProcessors;
	}

	@Override
	public Collection<String> getExtraNames() {
		if( !isEnabled())
			return null;

		Collection<String> allExtraNames = new ArrayList<>();
		for (DatasetProcessor processor : processors) {
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

		Collection<String> totalList = new ArrayList<>();
		for (DatasetProcessor processor : processors) {
			Collection<String> itemList = processor.isEnabled() ? processor.getOutputFormat() : null;
			if (itemList != null) {
				totalList.addAll(itemList);
			}
		}
		return totalList.isEmpty() ? null : totalList;
	}


	@Override
	public boolean isEnabled() {
		for (DatasetProcessor processor : processors) {
			if( processor.isEnabled())
				return true;
		}
		return false;
	}

	/**
	 * Marker to allow {@link DatasetProcessor#process(String, String, Dataset)} be used in a lambda
	 */
	private static final class DatasetProcessorException extends RuntimeException {
		private Exception cause;
		public DatasetProcessorException(Exception cause) {
			super(cause);
			this.cause = cause;
		}
	}

}
