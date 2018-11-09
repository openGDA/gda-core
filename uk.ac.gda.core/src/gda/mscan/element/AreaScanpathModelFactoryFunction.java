/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.mscan.element;

import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.scan.models.ScanModel;

import gda.device.Scannable;

/**
 * Functional Interface providing the template for factory functions for Scan path models that carve out paths that
 * cover a two dimensional area.
 * @since GDA 9.9
 */
@FunctionalInterface
public interface AreaScanpathModelFactoryFunction {
	/**
	 * Template for area based Scan path model factory functions
	 * @param scannables					The {@link Scannable}s that map to the axes of movement.
	 * @param scanParameters				The numeric parameters relevant to the specified {@link ScanModel}
	 * @param bboxParameters				The numeric parameters describing the {@link BoundingBox} of the specified
	 * 										{@link IROI}
	 * @param mutatorUses					The {@link Mutator}s to be applied to the scan path with any parameters
	 * @return								A model constructed using the supplied parameters
	 * @throws IllegalArgumentException		If the required number of scannables or parameters are not supplied
	 */
	public IScanPathModel createScanpathModel(final List<Scannable> scannables,
						 final List<Number> scanParameters,
						 final List<Number> bboxParameters,
						 final Map<Mutator, List<Number>> mutatorUses) throws IllegalArgumentException;
}
