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

import static org.eclipse.scanning.device.Transformation.TransformationType.ROTATION;
import static org.eclipse.scanning.device.Transformation.TransformationType.TRANSLATION;
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

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXtransformations;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.device.DetectorTransformationsAppender;
import org.eclipse.scanning.device.PositionerTransformationsAppender;
import org.eclipse.scanning.device.Transformation;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockNeXusScannable;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.Before;
import org.junit.Test;

public class TransformationsAppenderTest {

	private static final List<Transformation> DETECTOR_TRANSFORMATIONS = List.of(
		new Transformation("theta", ROTATION, "gamma", 1.0, new double[] { 0.0, 0.67, -0.33 },
				new double[] { 10.53, -5.29, 0.82 }, "deg"),
		new Transformation("gamma", ROTATION, "psi", 1.0, new double[] {-0.5, 0.0, 0.67 },
				new double[] { 0.17, 0.0, -0.42 }, "deg"),
		new Transformation("psi", TRANSLATION, ".", 1.0, new double[] { 1.23, 5.32, 17.83 },
				new double[] { 12.32, 6.33, -2.18 }, "deg")
	);

	private static final Transformation POSITIONER_TRANSFORMATION = new Transformation(
			"theta", ROTATION, "phi", 0.0, new double[] { 0.67, 0, -0.33 },
			new double[] { 1.23, 4.56, 7.89  }, "mm");

	private static final int[] EMPTY_SHAPE = {};

	private INexusDeviceService nexusDeviceService;

	private MandelbrotDetector detector;

	private IScannable<?> scannable;

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

		scannable = new MockNeXusScannable(POSITIONER_TRANSFORMATION.getAxisName(), 2.5, 3);
	}

	@Test
	public void testDetectorTransformationsAppender() throws Exception {
		// Arrange
		final DetectorTransformationsAppender appender = new DetectorTransformationsAppender();
		appender.setName(detector.getName());
		appender.setTransformations(DETECTOR_TRANSFORMATIONS);
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

		for (Transformation transformation : DETECTOR_TRANSFORMATIONS) {
			final DataNode valueDataNode = transformationGroup.getDataNode(transformation.getAxisName());
			assertThat(valueDataNode, is(notNullValue()));
			assertThat(valueDataNode.getAttributeNames(), containsInAnyOrder(
					NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON, NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE, NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS));

			final IDataset axisDataset = valueDataNode.getDataset().getSlice();
			assertThat(axisDataset.getShape(), is(equalTo(EMPTY_SHAPE)));
			assertThat(axisDataset.getDouble(), is(closeTo(1.0, 1e-15)));

			assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON).getFirstElement(),
					is(equalTo(transformation.getDependsOn())));
			assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR).getValue(),
					is(equalTo(DatasetFactory.createFromObject(getExpectedVector(transformation.getVector())))));
			assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE).getFirstElement(),
					is(equalTo(transformation.getType().toString().toLowerCase())));
			assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET).getValue(),
					is(equalTo(DatasetFactory.createFromObject(transformation.getOffset()))));
			assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS).getFirstElement(),
					is(equalTo(transformation.getOffsetUnits())));
		}
	}

	@Test
	public void testPositionerTransformationsAppender() throws Exception {
		// Arrange
		final PositionerTransformationsAppender appender = new PositionerTransformationsAppender();
		appender.setName(POSITIONER_TRANSFORMATION.getAxisName());
		appender.setTransformation(POSITIONER_TRANSFORMATION);
		appender.register();

		// Act
		@SuppressWarnings("unchecked")
		final INexusDevice<NXpositioner> positionerNexusDevice = (INexusDevice<NXpositioner>) scannable;
		final INexusDevice<NXpositioner> decoratedNexusDevice =
				nexusDeviceService.decorateNexusDevice(positionerNexusDevice);

		final NexusScanInfo scanInfo = new NexusScanInfo();
		scanInfo.setRank(1);
		final NexusObjectProvider<NXpositioner> nexusObjectProvider = decoratedNexusDevice.getNexusProvider(scanInfo);
		final NXpositioner nxPositioner = nexusObjectProvider.getNexusObject();

		// Assert
		assertThat(nxPositioner, is(notNullValue()));
		final DataNode valueDataNode = nxPositioner.getDataNode(NXpositioner.NX_VALUE);
		assertThat(valueDataNode, is(notNullValue()));

		assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON).getFirstElement(),
				is(equalTo(POSITIONER_TRANSFORMATION.getDependsOn())));
		assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR).getValue(),
				is(equalTo(DatasetFactory.createFromObject(getExpectedVector(POSITIONER_TRANSFORMATION.getVector())))));
		assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE).getFirstElement(),
				is(equalTo(POSITIONER_TRANSFORMATION.getType().toString().toLowerCase())));
		assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET).getValue(),
				is(equalTo(DatasetFactory.createFromObject(POSITIONER_TRANSFORMATION.getOffset()))));
		assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS).getFirstElement(),
				is(equalTo(POSITIONER_TRANSFORMATION.getOffsetUnits())));
	}

	private double[] getExpectedVector(double[] vector) {
		// by default the vector is normalized, i.e. so that sqrt(x^2 + y^2 + z^2) = 1
		final double normalizationFactor = Math.sqrt(Arrays.stream(vector).map(x -> x * x).sum());
		return Arrays.stream(vector).map(x -> x / normalizationFactor).toArray();
	}



}
