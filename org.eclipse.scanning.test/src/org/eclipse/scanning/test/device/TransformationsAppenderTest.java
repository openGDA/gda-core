/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.device;

import static java.util.stream.Collectors.toList;
import static org.eclipse.scanning.device.TransformationsAppender.TransformationType.ROTATION;
import static org.eclipse.scanning.device.TransformationsAppender.TransformationType.TRANSLATION;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_IMAGINARY_AXIS;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_REAL_AXIS;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_SPECTRUM;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_SPECTRUM_AXIS;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXtransformations;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.device.TransformationsAppender;
import org.eclipse.scanning.device.TransformationsAppender.Transformation;
import org.eclipse.scanning.device.TransformationsAppender.TransformationType;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.Before;
import org.junit.Test;

public class TransformationsAppenderTest {

	private static final String[] AXIS_NAMES = { "theta", "gamma", "psi" };
	private static final String[] DEPENDS_ON = { "gamma", "psi", "." };
	private static final double[][] VECTOR = { { 0.0, 0.67, -0.33 }, {-0.5, 0.0, 0.67 }, { 1.23, 5.32, 17.83 } };
	private static final TransformationType[] TRANSFORMATION_TYPE = { ROTATION, ROTATION, TRANSLATION };
	private static final double[][] OFFSET = { { 10.53, -5.29, 0.82 }, { 0.17, 0.0, -0.42 }, { 12.32, 6.33, -2.18 } };
	private static final String[] OFFSET_UNITS = { "deg", "deg", "mm" };
	private static final double[] TRANSFORMATION_SIZE = { 1.0, 1.0, 1.0 };

	private static final int[] EMPTY_SHAPE = {};

	private INexusDeviceService nexusDeviceService;

	private MandelbrotDetector detector;

	@Before
	public void setUp() throws Exception {
		nexusDeviceService = new NexusDeviceService();
		new ServiceHolder().setNexusDeviceService(nexusDeviceService);

		final MandelbrotModel detModel = new MandelbrotModel();
		detModel.setName("mandelbrot");
		detModel.setRealAxisName("xNex");
		detModel.setImaginaryAxisName("yNex");
		detModel.setColumns(64);
		detModel.setRows(64);

		detector = (MandelbrotDetector) TestDetectorHelpers.createAndConfigureMandelbrotDetector(detModel);
	}

	@Test
	public void testAppendTransformations() throws Exception {
		// Arrange
		final TransformationsAppender<NXdetector> appender = new TransformationsAppender<>();
		appender.setName(detector.getName());
		final List<Transformation> transformations = IntStream.range(0, AXIS_NAMES.length)
				.mapToObj(i -> new Transformation(AXIS_NAMES[i], TRANSFORMATION_TYPE[i], DEPENDS_ON[i],
						TRANSFORMATION_SIZE[i], VECTOR[i], OFFSET[i], OFFSET_UNITS[i]))
				.collect(toList());
		appender.setTransformations(transformations);
		appender.register();

		// Act
		final INexusDevice<NXdetector> decoratedNexusDevice = nexusDeviceService.decorateNexusDevice(detector);

		final NexusScanInfo scanInfo = new NexusScanInfo();
		scanInfo.setRank(2);
		final NexusObjectProvider<NXdetector> nexusObjectProvider = decoratedNexusDevice.getNexusProvider(scanInfo);
		final NXdetector nxDetector = nexusObjectProvider.getNexusObject();

		// Assert
		assertThat(nxDetector, is(notNullValue()));
		assertThat(nxDetector.getDataNodeNames(), containsInAnyOrder(NXdetector.NX_COUNT_TIME, NXdetector.NX_DATA,
				FIELD_NAME_SPECTRUM, FIELD_NAME_VALUE, "exposure_time", "escape_radius", "max_iterations",
				FIELD_NAME_REAL_AXIS, FIELD_NAME_IMAGINARY_AXIS, FIELD_NAME_SPECTRUM_AXIS, "name"));

		final NXtransformations transformationGroup = (NXtransformations) nxDetector.getGroupNode("transformations");
		assertThat(transformationGroup, is(notNullValue()));

		for (int axisIndex = 0; axisIndex < AXIS_NAMES.length; axisIndex++) {
			final DataNode axisDataNode = transformationGroup.getDataNode(AXIS_NAMES[axisIndex]);
			assertThat(axisDataNode, is(notNullValue()));
			assertThat(axisDataNode.getAttributeNames(), containsInAnyOrder(
					NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON, NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE, NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS));

			final IDataset axisDataset = axisDataNode.getDataset().getSlice();
			assertThat(axisDataset.getShape(), is(equalTo(EMPTY_SHAPE)));
			assertThat(axisDataset.getDouble(), is(closeTo(1.0, 1e-15)));

			assertThat(axisDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON).getFirstElement(),
					is(equalTo(DEPENDS_ON[axisIndex])));
			assertThat(axisDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR).getValue(),
					is(equalTo(DatasetFactory.createFromObject(VECTOR[axisIndex]))));
			assertThat(axisDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE).getFirstElement(),
					is(equalTo(TRANSFORMATION_TYPE[axisIndex].toString().toLowerCase())));
			assertThat(axisDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET).getValue(),
					is(equalTo(DatasetFactory.createFromObject(OFFSET[axisIndex]))));
			assertThat(axisDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS).getFirstElement(),
					is(equalTo(OFFSET_UNITS[axisIndex])));
		}
	}

}
