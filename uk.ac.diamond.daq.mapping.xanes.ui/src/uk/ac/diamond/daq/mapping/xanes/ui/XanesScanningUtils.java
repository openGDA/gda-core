/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.xanes.ui;

import java.util.List;

import org.eclipse.scanning.api.points.models.IScanPathModel;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;

public class XanesScanningUtils {

	private XanesScanningUtils() {
		// prevent instantiation
	}

	/**
	 * Get scan wrapper for scannable in OuterScannables section
	 *
	 * @param mappingBean
	 *            current mapping bean
	 * @param scannableName
	 *            name of the scannable to return
	 * @return {@link IScanModelWrapper} for the scannable
	 */
	public static IScanModelWrapper<IScanPathModel> getOuterScannable(IMappingExperimentBean mappingBean, String scannableName) {
		if (scannableName != null && scannableName.length() > 0) {
			final List<IScanModelWrapper<IScanPathModel>> outerScannables = mappingBean.getScanDefinition().getOuterScannables();
			for (IScanModelWrapper<IScanPathModel> scannable : outerScannables) {
				if (scannable.getName().equals(scannableName)) {
					return scannable;
				}
			}
		}
		return null;
	}
}
