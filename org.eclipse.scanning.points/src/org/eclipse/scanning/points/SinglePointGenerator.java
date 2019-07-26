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

package org.eclipse.scanning.points;

import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.ScanPointIterator;
import org.eclipse.scanning.api.points.models.SinglePointModel;
import org.eclipse.scanning.jython.JythonObjectFactory;
import org.python.core.PyList;

public class SinglePointGenerator extends AbstractGenerator<SinglePointModel> {

	@Override
	protected Iterator<IPosition> iteratorFromValidModel() {
		final SinglePointModel model = getModel();

		final PyList names =  new PyList(Arrays.asList(model.getXAxisName(), model.getYAxisName()));
		final PyList units = new PyList(Arrays.asList(model.getXAxisUnits(), model.getYAxisUnits()));
		final double[] position = new double[] {model.getX(), model.getY()};

		// ArrayGenerator might be better suited for a single point, but it only supports a single dimension.
		final JythonObjectFactory<ScanPointIterator> lineGeneratorFactory = ScanPointGeneratorFactory.JLineGenerator2DFactory();
		final ScanPointIterator pyIterator = lineGeneratorFactory.createObject(names, units, position, position, 1);

		return new SpgIterator(pyIterator);
	}

}
