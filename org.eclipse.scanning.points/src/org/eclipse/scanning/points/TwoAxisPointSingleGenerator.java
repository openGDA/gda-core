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

import java.util.List;

import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.jython.JythonObjectFactory;

public class TwoAxisPointSingleGenerator extends AbstractScanPointGenerator<TwoAxisPointSingleModel> {

	public TwoAxisPointSingleGenerator(TwoAxisPointSingleModel model) {
		super(model);
	}

	@Override
	public PPointGenerator createPythonPointGenerator() {
		final JythonObjectFactory<PPointGenerator> lineGeneratorFactory = ScanPointGeneratorFactory.JTwoAxisLineGeneratorFactory();

		final TwoAxisPointSingleModel model = getModel();

		final List<String> names =  model.getScannableNames();
		final List<String> units = model.getUnits();
		final double[] position = new double[] {model.getX(), model.getY()};

		return lineGeneratorFactory.createObject(names, units, position, position, 1);
	}

}
