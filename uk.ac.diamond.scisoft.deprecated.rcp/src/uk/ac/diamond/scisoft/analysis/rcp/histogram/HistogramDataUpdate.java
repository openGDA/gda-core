/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.histogram;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.viewers.ISelection;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 *
 */
@Deprecated(since="GDA 8.38")
public class HistogramDataUpdate implements ISelection {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(HistogramDataUpdate.class);
	private Dataset dataset;
	
	/**
	 * Constructor of a HistogramDataUpdate
	 * @param dataset Dataset associated to this HistogramDataUpdate
	 */
	
	public HistogramDataUpdate(IDataset dataset)
	{
		logger.deprecatedClass();
		this.dataset = DatasetUtils.convertToDataset(dataset);
	}
	
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Get the dataset associated to this update
	 * @return associated dataset
	 */
	public Dataset getDataset()
	{
		return dataset;
	}
	

}
