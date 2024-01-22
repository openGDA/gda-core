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

import java.util.Collection;

import org.eclipse.january.dataset.Dataset;

import gda.data.nexus.tree.INexusTree;
import gda.device.detector.GDANexusDetectorData;

/**
 * interface for processing a dataset. Process method returns <code>ProcessorResults</code> if extraNames or
 * outputFormat changes that inform observers
 */
public interface DatasetProcessor {

	/**
	 * @return the name of the processor
	 */
	String getName();

	/**
	 * Processes the given dataset, returning a {@link GDANexusDetectorData} containing the
	 * {@link INexusTree}s nodes describing the datasets to be added to the nexus tree.
	 * @param detectorName name of detector
	 * @param dataName name of dataset to process
	 * @param dataset dataset to process
	 * @return a {@link GDANexusDetectorData} with t
	 * @throws Exception
	 */
	GDANexusDetectorData process(String detectorName, String dataName, Dataset dataset) throws Exception;

	/**
	 * @return the names of the datasets to be added
	 */
	Collection<String> getExtraNames();

	/**
	 * @return output formats for the datasets to be added
	 */
	Collection<String> getOutputFormat();

	/**
	 * @return <code>true</code> if this processor is enabled, <code>false</code> otherwise
	 */
	boolean isEnabled();

	void setEnable(boolean enable);

	default void atScanStart() {}

	default void atScanEnd() {}

	default void stop() {}

}
