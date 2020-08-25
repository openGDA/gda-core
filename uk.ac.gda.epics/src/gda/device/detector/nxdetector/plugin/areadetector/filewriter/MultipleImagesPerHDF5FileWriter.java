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

package gda.device.detector.nxdetector.plugin.areadetector.filewriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated Use {@link gda.device.detector.addetector.filewriter.MultipleImagesPerHDF5FileWriter} directly
 * <p>
 * This will be removed in GDA 9.20
 */
@Deprecated
public class MultipleImagesPerHDF5FileWriter extends gda.device.detector.addetector.filewriter.MultipleImagesPerHDF5FileWriter {

	private static final Logger logger = LoggerFactory.getLogger(MultipleImagesPerHDF5FileWriter.class);

	public MultipleImagesPerHDF5FileWriter() {
		logger.warn("Instance of deprecated class: {} created, this will be removed in GDA 9.20", MultipleImagesPerHDF5FileWriter.class);
	}
}
