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

package org.eclipse.scanning.test.points;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.SinglePointModel;
import org.junit.Test;

public class SinglePointTest extends AbstractGeneratorTest {

	static final String FAST_NAME = "x";
	static final String SLOW_NAME = "y";
	static final double FAST_COORDINATE = -5.37;
	static final double SLOW_COORDINATE = 3;

	@Test
	public void onlyOnePointGenerated() throws GeneratorException {

		final SinglePointModel model = new SinglePointModel();
		model.setFastAxisName(FAST_NAME);
		model.setSlowAxisName(SLOW_NAME);
		model.setX(FAST_COORDINATE);
		model.setY(SLOW_COORDINATE);

		final IPointGenerator<SinglePointModel> generator = service.createGenerator(model);

		assertThat(generator.getRank(), is(1));

		final List<IPosition> point = generator.createPoints();

		// single point...
		assertThat(point.size(), is(1));

		IPosition thePoint = point.iterator().next();

		// ...with expected axes and coordinates
		assertThat(thePoint.getNames(), containsInAnyOrder(FAST_NAME, SLOW_NAME));
		assertThat(thePoint.get(FAST_NAME), is(FAST_COORDINATE));
		assertThat(thePoint.get(SLOW_NAME), is(SLOW_COORDINATE));
	}

	@Test
	public void coordinatesTakenFromROI() throws GeneratorException {

		final PointROI roi = new PointROI(FAST_COORDINATE, SLOW_COORDINATE);
		final SinglePointModel model = new SinglePointModel();
		model.setFastAxisName(FAST_NAME);
		model.setSlowAxisName(SLOW_NAME);

		// note that we are not setting x and y on model

		final IPointGenerator<SinglePointModel> generator = service.createGenerator(model, roi);
		final IPosition point = generator.getFirstPoint();

		assertThat(point.get(FAST_NAME), is(FAST_COORDINATE));
		assertThat(point.get(SLOW_NAME), is(SLOW_COORDINATE));
	}

}
