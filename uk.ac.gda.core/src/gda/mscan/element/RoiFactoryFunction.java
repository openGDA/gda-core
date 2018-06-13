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

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROIBase;

/**
 * Functional Interface providing the template for factory functions for DAWN ROIs.
 * @since GDA 9.9
 */
@FunctionalInterface
public interface RoiFactoryFunction {
	/**
	 * Template for ROI factory functions
	 * @param roiParameters					The numeric parameters relevant to the specified subclass of {@link ROIBase}
	 * @return								The {@link IROI} of the object created by implementation of the function
	 * @throws IllegalArgumentException		If the required number of parameters are not supplied
	 */
	IROI createROI(final List<Number> roiParameters);
}
