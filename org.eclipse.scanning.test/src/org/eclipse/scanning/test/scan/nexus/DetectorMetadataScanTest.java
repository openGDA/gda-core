/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertScanNotFinished;
import static org.eclipse.scanning.device.Transformation.TransformationType.TRANSLATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXtransformations;
import org.eclipse.dawnsci.nexus.appender.SimpleNexusMetadataAppender;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.device.DetectorTransformationsAppender;
import org.eclipse.scanning.device.NexusMetadataAppender;
import org.eclipse.scanning.device.PositionerTransformationsAppender;
import org.eclipse.scanning.device.Transformation;
import org.eclipse.scanning.device.Transformation.TransformationType;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.BeforeClass;
import org.junit.Test;

public class DetectorMetadataScanTest extends NexusTest {

	private static final int[] EMPTY_SHAPE = new int[0];

	private static IWritableDetector<MandelbrotModel> detector;

	@BeforeClass
	public static void before() throws Exception {
		MandelbrotModel model = createMandelbrotModel();
		detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);
		assertThat(detector, is(notNullValue()));
	}

	@Test
	public void testScanWithSimpleNexusMetadataAppender() throws Exception {
		final Map<String, Object> metadata = createExpectedMetadata();
		final SimpleNexusMetadataAppender<?> metadataAppender = new SimpleNexusMetadataAppender<>(detector.getName());
		metadataAppender.setNexusMetadata(metadata);

		ServiceHolder.getNexusDeviceService().register(metadataAppender);

		final int[] shape = { 8, 5 };
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, false, shape);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);

		// Check we reached ready (it will normally throw an exception on error)
		checkNexusFileWithMetadata(scanner, shape, metadata); // Step model is +1 on the size
	}

	@Test
	public void testScanWithNexusMetadataAppender() throws Exception {
		final Map<String, Object> metadata = createExpectedMetadata();
		final NexusMetadataAppender<?> metadataAppender = new NexusMetadataAppender<>();
		metadataAppender.setName(detector.getName());
		metadata.entrySet().forEach(entry -> metadataAppender.addScalarField(entry.getKey(), entry.getValue()));

		ServiceHolder.getNexusDeviceService().register(metadataAppender);

		final int[] shape = { 8, 5 };
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, false, shape);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);

		// Check we reached ready (it will normally throw an exception on error)
		checkNexusFileWithMetadata(scanner, shape, metadata); // Step model is +1 on the size
	}

	@Test
	public void testScanWithTransformationsAppenders() throws Exception {
		final DetectorTransformationsAppender detectorAppender = new DetectorTransformationsAppender();
		detectorAppender.setName(detector.getName());

		final List<Transformation> transformations = new ArrayList<>();
		transformations.add(new Transformation("theta", TransformationType.ROTATION, "gamma",
				1.0, new double[] { 0.0, 0.67, -0.33}, new double[] { 10.53, -5.29, 0.82 }, "deg"));
		transformations.add(new Transformation("gamma", TransformationType.ROTATION, "psi",
				1.0, new double[] { -0.5, 0.0, 0.67 }, new double[] { 0.17, 0.0, -0.42 }, "deg"));
		transformations.add(new Transformation("psi", TransformationType.TRANSLATION, ".",
				1.0, new double[] { 1.23, 5.32, 17.38 }, new double[] { 12.32, 6.33, -2.18 }, "mm"));
		detectorAppender.setTransformations(transformations);
		ServiceHolder.getNexusDeviceService().register(detectorAppender);

		final PositionerTransformationsAppender xPosAppender = new PositionerTransformationsAppender();
		xPosAppender.setName(X_AXIS_NAME);
		xPosAppender.setTransformation(new Transformation(X_AXIS_NAME, TRANSLATION,
				"yPos", 0.0, new double[] { 0.67, 0, -0.33 }, new double[] { 1.23, -4.56, 7.89 }, "mm"));
		ServiceHolder.getNexusDeviceService().register(xPosAppender);

		final PositionerTransformationsAppender yPosAppender = new PositionerTransformationsAppender();
		yPosAppender.setName(Y_AXIS_NAME);
		yPosAppender.setTransformation(new Transformation(Y_AXIS_NAME, TRANSLATION,
				".", 0.0, new double[] { 0.33, 0, -0.66 }, new double[] { -9.87, 6.54, -3.21 }, "mm"));
		ServiceHolder.getNexusDeviceService().register(yPosAppender);

		final int[] shape = { 8, 5 };
		final IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, false, shape);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);

		checkNexusFileWithTransformations(scanner, shape, transformations,
				yPosAppender.getTransformations().get(0), xPosAppender.getTransformations().get(0));
	}

	private Map<String, Object> createExpectedMetadata() {
		final Map<String, Object> metadata = new HashMap<>();
		metadata.put(NXdetector.NX_DESCRIPTION, "an example dummy detector that uses the mandelbrot set");
		metadata.put(NXdetector.NX_LOCAL_NAME, "mandelbrot");
		metadata.put(NXdetector.NX_TYPE, "dummy");
		metadata.put(NXdetector.NX_DETECTOR_READOUT_TIME, 0.015);
		return metadata;
	}

	private void checkNexusFileWithMetadata(IRunnableDevice<ScanModel> scanner, int[] shape, Map<String, Object> metadata) throws Exception {
		super.checkNexusFile(scanner, false, shape);

		// check that the metadata has been added to the mandlebrot NXdetector object for the instrument
		final NXinstrument instrument = getNexusRoot(scanner).getEntry().getInstrument();
		final NXdetector detectorGroup = instrument.getDetector(detector.getName());
		assertThat(detectorGroup, is(notNullValue()));

		for (Map.Entry<String, Object> entry : metadata.entrySet()) {
			// annoyingly there doesn't seem to be a way to get the scalar value of a field without knowing the type
			DataNode dataNode = detectorGroup.getDataNode(entry.getKey());
			assertThat(dataNode, is(notNullValue()));
			assertEquals(entry.getValue(), dataNode.isString() ?
					detectorGroup.getString(entry.getKey()) : detectorGroup.getNumber(entry.getKey()));
		}
	}

	private void checkNexusFileWithTransformations(IRunnableDevice<ScanModel> scanner, int[] shape,
			List<Transformation> detectorTransformations, Transformation... positionerTransformations) throws Exception {
		super.checkNexusFile(scanner, false, shape);

		final NXinstrument instrument = getNexusRoot(scanner).getEntry().getInstrument();
		final NXdetector detectorGroup = instrument.getDetector(detector.getName());
		assertThat(detectorGroup, is(notNullValue()));

		// check that the NXdetector group has been appended with an NXtransformation group
		// with the expected
		final NXtransformations transformations = (NXtransformations) detectorGroup.getGroupNode("transformations");
		assertThat(transformations, is(notNullValue()));

		assertThat(transformations.getDataNodeNames(), containsInAnyOrder(
				detectorTransformations.stream().map(Transformation::getAxisName).toArray()));
		for (Transformation transformation : detectorTransformations) {
			final String axisName = transformation.getAxisName();
			final DataNode axisDataNode = transformations.getDataNode(axisName);
			assertThat(axisDataNode, is(notNullValue()));
			assertThat(axisDataNode.getAttributeNames(), containsInAnyOrder(
					NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON, NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE, NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS));

			final IDataset axisDataset = axisDataNode.getDataset().getSlice();
			assertThat(axisDataset.getShape(), is(equalTo(EMPTY_SHAPE)));
			assertThat(axisDataset.getDouble(), is(closeTo(1.0, 1e-15)));

			assertThat(transformations.getAxisnameAttributeDepends_on(axisName),
					is(equalTo(transformation.getDependsOn())));
			assertThat(axisDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR).getValue(),
					is(equalTo(DatasetFactory.createFromObject(getExpectedVector(transformation.getVector())))));
			assertThat(transformations.getAxisnameAttributeTransformation_type(axisName),
					is(equalTo(transformation.getType().toString())));
			assertThat(axisDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET).getValue(),
					is(equalTo(DatasetFactory.createFromObject(transformation.getOffset()))));
			assertThat(transformations.getAxisnameAttributeOffset_units(axisName),
					is(equalTo(transformation.getOffsetUnits())));
		}

		for (Transformation transformation : positionerTransformations) {
			final NXpositioner positioner = instrument.getPositioner(transformation.getAxisName());
			assertThat(positioner, is(notNullValue()));

			final DataNode valueDataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
			assertThat(valueDataNode, is(notNullValue()));

			assertThat(valueDataNode.getAttributeNames(), containsInAnyOrder("target", "units",
					NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON, NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE, NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS));

			assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON).getFirstElement(),
					is(equalTo(transformation.getDependsOn())));
			assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR).getValue(),
					is(equalTo(DatasetFactory.createFromObject(getExpectedVector(transformation.getVector())))));
			assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE).getFirstElement(),
					is(equalTo(transformation.getType().toString())));
			assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET).getValue(),
					is(equalTo(DatasetFactory.createFromObject(transformation.getOffset()))));
			assertThat(valueDataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS).getFirstElement(),
					is(equalTo(transformation.getOffsetUnits())));
		}
	}

	private double[] getExpectedVector(double[] vector) {
		// by default the vector is normalized, i.e. so that sqrt(x^2 + y^2 + z^2) = 1
		final double normalizationFactor = Math.sqrt(Arrays.stream(vector).map(x -> x * x).sum());
		return Arrays.stream(vector).map(x -> x / normalizationFactor).toArray();
	}

}
