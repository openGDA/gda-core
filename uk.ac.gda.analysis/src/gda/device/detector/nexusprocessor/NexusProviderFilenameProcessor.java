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

import java.io.File;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

/**
 * Class to get create a dataset from an external filename in a nexus file
 */

public class NexusProviderFilenameProcessor extends NexusProviderDatasetProcessor {

	/**
	 *
	 * @param detName - name of detector in NexusTree. If null or empty the first detector entry is used
	 * @param dataName
	 * @param className
	 * @param processors
	 * @param datasetCreator
	 */
	public NexusProviderFilenameProcessor(String detName, String dataName, String className,
			List<DataSetProcessor> processors, DatasetCreator datasetCreator) {
		super(detName, dataName, className, processors, datasetCreator);
	}

	int dataset_index=0;



	public int getDataset_index() {
		return dataset_index;
	}

	public void setDataset_index(int dataset_index) {
		this.dataset_index = dataset_index;
	}

	@Override
	protected Dataset getDatasetFromNexusGroupData(NexusGroupData ngd) throws Exception {
		if (!isEnabled())
			return null;
		if (ngd.isChar()) {
			Dataset files = ngd.toDataset();
			if (files.getSize() == 1) {
				String path = files.getStringAbs(0);
				if (!(new File(path)).exists()) {
					Thread.sleep(1000);
				}
				IDataHolder data=null;
				while( data == null){
					data = LoaderFactory.getData(path);
					if(data == null){
						//TODO if( data == null)
						//	logger.warn("Unable to find data at '" + path + "' within existing file:'" + path +"'");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							throw new InterruptedException("Interrupted whilst reading '" + path + "' from within existing file:'" + path +"'");
						}
					}
				}
				return DatasetUtils.convertToDataset(data.getDataset(dataset_index));
			}
		}
		return null;
	}

}
