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

import gda.device.detector.GDANexusDetectorData;

import java.util.Collection;

import org.eclipse.january.dataset.Dataset;

/**
 * interface for processing a dataset. Process method returns <code>ProcessorResults</code> if extraNames or
 * outputFormat changes that inform observers
 */
public interface DataSetProcessor {

	String getName();

	GDANexusDetectorData process(String detectorName, String dataName, Dataset dataset) throws Exception;

	Collection<String> getExtraNames();

	Collection<String> getOutputFormat();

	boolean isEnabled();

	void setEnable(boolean enable);

}
