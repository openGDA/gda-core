/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.scan.nexus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel.Orientation;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MalcolmFlattenedGridTest extends AbstractMalcolmScanTest {

	private static final int NUM_FAST_AXIS_POINTS = 5;
	private static final int NUM_SLOW_AXIS_POINTS = 5;
	private static final int NUM_GRID_POINTS = NUM_FAST_AXIS_POINTS * NUM_SLOW_AXIS_POINTS;
	private static final int NUM_OUTER_POINTS = 3;

	@Override
	protected DummyMalcolmModel createMalcolmModel() {
		final DummyMalcolmModel model = createMalcolmModelTwoDetectors();
		model.setAxesToMove(List.of(X_AXIS_NAME, Y_AXIS_NAME, ANGLE_AXIS_NAME));
		return model;
	}

	static Stream<Arguments> provideArgs() {
		return Stream.of(
				Arguments.of(X_AXIS_NAME, Y_AXIS_NAME, Orientation.HORIZONTAL, false),
				Arguments.of(Y_AXIS_NAME, X_AXIS_NAME, Orientation.HORIZONTAL, false),
				Arguments.of(X_AXIS_NAME, ANGLE_AXIS_NAME, Orientation.HORIZONTAL, false),
				Arguments.of(X_AXIS_NAME, Y_AXIS_NAME, Orientation.VERTICAL, false),
				Arguments.of(Y_AXIS_NAME, X_AXIS_NAME, Orientation.VERTICAL, false),
				Arguments.of(X_AXIS_NAME, ANGLE_AXIS_NAME, Orientation.VERTICAL, false),
				Arguments.of(X_AXIS_NAME, Y_AXIS_NAME, Orientation.HORIZONTAL, true),
				Arguments.of(Y_AXIS_NAME, X_AXIS_NAME, Orientation.HORIZONTAL, true),
				Arguments.of(X_AXIS_NAME, ANGLE_AXIS_NAME, Orientation.HORIZONTAL, true),
				Arguments.of(X_AXIS_NAME, Y_AXIS_NAME, Orientation.VERTICAL, true),
				Arguments.of(Y_AXIS_NAME, X_AXIS_NAME, Orientation.VERTICAL, true),
				Arguments.of(X_AXIS_NAME, ANGLE_AXIS_NAME, Orientation.VERTICAL, true)
			);
	}

	@ParameterizedTest
	@MethodSource("provideArgs")
	void testFlattenedScan(String xAxisName, String yAxisName, Orientation orientation, boolean outerScan) throws Exception {
		int[] scanShape = new int[] { NUM_SLOW_AXIS_POINTS, NUM_FAST_AXIS_POINTS };
		if (outerScan) scanShape = ArrayUtils.add(scanShape, 0, NUM_OUTER_POINTS);
		final IScanDevice scanner = createMalcolmGridScan(malcolmDevice, output, xAxisName, yAxisName, false, true, orientation, scanShape);
		scanner.run();

		checkSize(scanner, scanShape);
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));
		final int[] flattenedShape = outerScan ? new int[] { NUM_OUTER_POINTS, NUM_GRID_POINTS } : new int[] { NUM_GRID_POINTS };
		checkNexusFile(scanner, false, true, flattenedShape);
	}

}
