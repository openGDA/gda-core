/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.spring.propertyeditors;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyEditor;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

/**
 * A {@link PropertyEditorRegistrar} that registers the {@link PropertyEditor}s
 * in the {@code gda.spring.propertyeditors} package.
 */
public class GdaPropertyEditorRegistrar implements PropertyEditorRegistrar {

	@Override
	public void registerCustomEditors(PropertyEditorRegistry registry) {
		registry.registerCustomEditor(double[][].class, new TwoDDoubleArrayPropertyEditor());
		registry.registerCustomEditor(RealMatrix.class, new RealMatrixPropertyEditor());
		registry.registerCustomEditor(Point.class, new PointPropertyEditor());
		registry.registerCustomEditor(Dimension.class, new DimensionPropertyEditor());
		registry.registerCustomEditor(RealVector.class, new RealVectorPropertyEditor());
	}

}
