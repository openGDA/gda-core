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

import static org.eclipse.scanning.device.AbstractMetadataField.ATTRIBUTE_NAME_UNITS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXmirror;
import org.eclipse.dawnsci.nexus.NXslit;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.device.MetadataNode;
import org.eclipse.scanning.device.NexusMetadataDevice;
import org.eclipse.scanning.device.ScalarField;
import org.eclipse.scanning.device.ScannableComponentField;
import org.eclipse.scanning.device.ScannableField;
import org.junit.jupiter.api.Test;

import gda.TestHelpers;
import gda.device.Scannable;
import gda.factory.Factory;
import gda.factory.Finder;

class NexusMetadataDeviceTest extends AbstractNexusMetadataDeviceTest<NXmirror> {

	private static final String[] NO_FIELDS = new String[0];

	private static final String INCIDENT_ANGLE_SCANNABLE_NAME = "incidentAngle";

	private static final String EXPECTED_TYPE = "single";
	private static final String EXPECTED_DESCRIPTION = "my mirror";
	private static final String EXPECTED_INTERIOR_ATMOSPHERE = "argon";
	private static final double EXPECTED_M_VALUE = 3.456;
	private static final double EXPECTED_LAYER_THICKNESS = 98.76;

	private static final String BEND_ANGLE_SCANNABLE_NAME = "bend_angle";
	private static final String[] BEND_ANGLE_INPUT_NAMES = { "x", "y" };
	private static final Double[] EXPECTED_BEND_ANGLE = { 31.12, 9.47 };

	private static final String SUBSTRATE_SCANNABLE_NAME = "mirror_substrate";
	private static final String[] SUBSTRATE_EXTRA_NAMES = { "density", "thickness", "roughness" };
	private static final Double EXPECTED_SUBSTRATE_DENSITY = 345.67;
	private static final Double EXPECTED_SUBSTRATE_THICKNESS = 8.63;
	private static final Double EXPECTED_SUBSTRATE_ROUGHNESS = 43.32;
	private static final Double[] SUBSTRATE_POSITION =
		{ EXPECTED_SUBSTRATE_DENSITY, EXPECTED_SUBSTRATE_THICKNESS, EXPECTED_SUBSTRATE_ROUGHNESS };

	private static final String SLIT_X_GAP_SCANNABLE_NAME = "slit_xgap";
	private static final String SLIT_Y_GAP_SCANNABLE_NAME = "slit_ygap";
	private static final String EXPECTED_SLIT_DEPENDS_ON = "phi";
	private static final String EXPECTED_SLIT_X_GAP_ERROR_MESSAGE = "Could not get position for scannable with name: slit_xgap";

	@Override
	protected void setupTestFixtures() throws Exception {
		createMockScannable(INCIDENT_ANGLE_SCANNABLE_NAME, 3.45); // an IScannable, so doesn't need to be findable
		createThrowingScannable(SLIT_X_GAP_SCANNABLE_NAME);
		createMockScannable(SLIT_Y_GAP_SCANNABLE_NAME, 6.15, "mm");

		final Factory factory = TestHelpers.createTestFactory();
		factory.addFindable(createMockBendAngleScannable()); // gda.device.Scananbles, so add to finder
		factory.addFindable(createSubstrateScannable());

		Finder.addFactory(factory);
	}

	private Scannable createMockBendAngleScannable() throws Exception {
		final Scannable bendAngleScannable = mock(Scannable.class);
		when(bendAngleScannable.getName()).thenReturn(BEND_ANGLE_SCANNABLE_NAME);
		when(bendAngleScannable.getInputNames()).thenReturn(BEND_ANGLE_INPUT_NAMES);
		when(bendAngleScannable.getExtraNames()).thenReturn(NO_FIELDS);
		when(bendAngleScannable.getPosition()).thenReturn(EXPECTED_BEND_ANGLE);
		return bendAngleScannable;
	}

	private Scannable createSubstrateScannable() throws Exception {
		final Scannable substrateScannable = mock(Scannable.class);
		when(substrateScannable.getName()).thenReturn(SUBSTRATE_SCANNABLE_NAME);
		when(substrateScannable.getInputNames()).thenReturn(NO_FIELDS);
		when(substrateScannable.getExtraNames()).thenReturn(SUBSTRATE_EXTRA_NAMES);
		when(substrateScannable.getPosition()).thenReturn(SUBSTRATE_POSITION);
		return substrateScannable;
	}

	@Override
	protected INexusDevice<NXmirror> setupNexusDevice() throws Exception {
		final NexusMetadataDevice<NXmirror> nexusDevice = new NexusMetadataDevice<>();
		nexusDevice.setNexusClass(NexusBaseClass.NX_MIRROR.toString());

		final List<MetadataNode> childNodes = new ArrayList<>();
		childNodes.add(new ScalarField(NXmirror.NX_TYPE, EXPECTED_TYPE));
		childNodes.add(new ScalarField(NXmirror.NX_DESCRIPTION, EXPECTED_DESCRIPTION));
		childNodes.add(new ScalarField(NXmirror.NX_INTERIOR_ATMOSPHERE, EXPECTED_INTERIOR_ATMOSPHERE));
		childNodes.add(new ScalarField(NXmirror.NX_M_VALUE, EXPECTED_M_VALUE));
		childNodes.add(new ScalarField(NXmirror.NX_LAYER_THICKNESS,
				EXPECTED_LAYER_THICKNESS, UNITS_ATTR_VAL_MILLIMETERS));
		childNodes.add(new ScannableField(NXmirror.NX_INCIDENT_ANGLE, INCIDENT_ANGLE_SCANNABLE_NAME, UNITS_ATTR_VAL_DEGREES));
		childNodes.add(new ScannableComponentField(NXmirror.NX_BEND_ANGLE_X, BEND_ANGLE_SCANNABLE_NAME, BEND_ANGLE_INPUT_NAMES[0]));
		childNodes.add(new ScannableComponentField(NXmirror.NX_BEND_ANGLE_Y, BEND_ANGLE_SCANNABLE_NAME, 1));
		childNodes.add(new ScannableComponentField(NXmirror.NX_SUBSTRATE_DENSITY, SUBSTRATE_SCANNABLE_NAME, SUBSTRATE_EXTRA_NAMES[0]));
		childNodes.add(new ScannableComponentField(NXmirror.NX_SUBSTRATE_THICKNESS, SUBSTRATE_SCANNABLE_NAME, 1));
		childNodes.add(new ScannableComponentField(NXmirror.NX_SUBSTRATE_ROUGHNESS, SUBSTRATE_SCANNABLE_NAME, SUBSTRATE_EXTRA_NAMES[2]));
		nexusDevice.setChildNodes(childNodes);

		return nexusDevice;
	}

	@Override
	protected void checkNexusObject(NXmirror mirror) throws Exception {
		assertThat(mirror, is(notNullValue()));
		assertThat(mirror.getDataNodeNames(), containsInAnyOrder(NXmirror.NX_TYPE, NXmirror.NX_DESCRIPTION,
				NXmirror.NX_INTERIOR_ATMOSPHERE, NXmirror.NX_M_VALUE, NXmirror.NX_INCIDENT_ANGLE,
				NXmirror.NX_LAYER_THICKNESS, NXmirror.NX_BEND_ANGLE_X, NXmirror.NX_BEND_ANGLE_Y,
				NXmirror.NX_SUBSTRATE_DENSITY, NXmirror.NX_SUBSTRATE_THICKNESS, NXmirror.NX_SUBSTRATE_ROUGHNESS));
		assertThat(mirror.getTypeScalar(), is(equalTo(EXPECTED_TYPE)));
		assertThat(mirror.getDescriptionScalar(), is(equalTo(EXPECTED_DESCRIPTION)));
		assertThat(mirror.getInterior_atmosphereScalar(), is(equalTo(EXPECTED_INTERIOR_ATMOSPHERE)));
		assertThat(mirror.getIncident_angleScalar(), is(equalTo(getScannableValue(INCIDENT_ANGLE_SCANNABLE_NAME))));
		assertThat(mirror.getAttrString(NXmirror.NX_INCIDENT_ANGLE, ATTRIBUTE_NAME_UNITS), is(equalTo(UNITS_ATTR_VAL_DEGREES)));
		assertThat(mirror.getM_valueScalar(), is(closeTo(EXPECTED_M_VALUE, 1e-15)));
		assertThat(mirror.getLayer_thicknessScalar(), is(closeTo(EXPECTED_LAYER_THICKNESS, 1e-15)));
		assertThat(mirror.getAttrString(NXmirror.NX_LAYER_THICKNESS, ATTRIBUTE_NAME_UNITS), is(equalTo(UNITS_ATTR_VAL_MILLIMETERS)));
		assertThat(mirror.getBend_angle_xScalar(), is(closeTo(EXPECTED_BEND_ANGLE[0], 1e-15)));
		assertThat(mirror.getBend_angle_yScalar(), is(closeTo(EXPECTED_BEND_ANGLE[1], 1e-15)));
		assertThat(mirror.getSubstrate_densityScalar(), is(closeTo(EXPECTED_SUBSTRATE_DENSITY, 1e-15)));
		assertThat(mirror.getSubstrate_thicknessScalar(), is(closeTo(EXPECTED_SUBSTRATE_THICKNESS, 1e-15)));
		assertThat(mirror.getSubstrate_roughnessScalar(), is(closeTo(EXPECTED_SUBSTRATE_ROUGHNESS, 1e-15)));
	}

	@Test
	void testDuplicateFields() {
		// check that an exception is thrown if the list of fields has duplicate names
		final NexusMetadataDevice<NXmirror> nexusDevice = new NexusMetadataDevice<>();
		nexusDevice.setNexusClass(NexusBaseClass.NX_MIRROR.toString());

		final List<MetadataNode> childNodes = new ArrayList<>();
		childNodes.add(new ScalarField(NXmirror.NX_TYPE, EXPECTED_TYPE));
		childNodes.add(new ScalarField(NXmirror.NX_DESCRIPTION, EXPECTED_DESCRIPTION));
		childNodes.add(new ScalarField(NXmirror.NX_INTERIOR_ATMOSPHERE, EXPECTED_INTERIOR_ATMOSPHERE));
		childNodes.add(new ScalarField(NXmirror.NX_M_VALUE, EXPECTED_M_VALUE));
		childNodes.add(new ScalarField(NXmirror.NX_DESCRIPTION, "new description"));
		childNodes.add(new ScalarField(NXmirror.NX_LAYER_THICKNESS, EXPECTED_LAYER_THICKNESS, UNITS_ATTR_VAL_MILLIMETERS));
		childNodes.add(new ScannableField(NXmirror.NX_INCIDENT_ANGLE, INCIDENT_ANGLE_SCANNABLE_NAME, UNITS_ATTR_VAL_DEGREES));
		assertThrows(IllegalArgumentException.class, () -> nexusDevice.setChildNodes(childNodes));
	}

	@Test
	void testOverwriteField() throws Exception {
		// check that an exception is thrown if the list of fields has duplicate names
		final NexusMetadataDevice<NXmirror> nexusDevice = new NexusMetadataDevice<>();
		nexusDevice.setNexusClass(NexusBaseClass.NX_MIRROR.toString());

		final List<MetadataNode> childNodes = new ArrayList<>();
		childNodes.add(new ScalarField(NXmirror.NX_TYPE, EXPECTED_TYPE));
		childNodes.add(new ScalarField(NXmirror.NX_DESCRIPTION, EXPECTED_DESCRIPTION));
		childNodes.add(new ScalarField(NXmirror.NX_INTERIOR_ATMOSPHERE, EXPECTED_INTERIOR_ATMOSPHERE));
		childNodes.add(new ScalarField(NXmirror.NX_M_VALUE, EXPECTED_M_VALUE));
		childNodes.add(new ScalarField(NXmirror.NX_LAYER_THICKNESS, EXPECTED_LAYER_THICKNESS, UNITS_ATTR_VAL_MILLIMETERS));
		childNodes.add(new ScannableField(NXmirror.NX_INCIDENT_ANGLE, INCIDENT_ANGLE_SCANNABLE_NAME, UNITS_ATTR_VAL_DEGREES));
		nexusDevice.setChildNodes(childNodes);

		final String newDescription = "new description";
		final String newType = "multi";
		final String newInteriorAtmosphere = "vacuum";
		final double newSubstrateThickness = 52.15;
		final String newSubstrateMaterial = "zinc";
		nexusDevice.setScalarField(NXmirror.NX_TYPE, newType);
		nexusDevice.setScalarField(NXmirror.NX_DESCRIPTION, newDescription);
		nexusDevice.setScalarField(NXmirror.NX_INTERIOR_ATMOSPHERE, newInteriorAtmosphere);
		nexusDevice.addScalarField(NXmirror.NX_SUBSTRATE_THICKNESS, newSubstrateThickness, UNITS_ATTR_VAL_MILLIMETERS);
		nexusDevice.addScalarField(NXmirror.NX_SUBSTRATE_MATERIAL, newSubstrateMaterial);
		nexusDevice.removeNode(NXmirror.NX_LAYER_THICKNESS);
		nexusDevice.removeNode(NXmirror.NX_M_VALUE);

		final NexusObjectProvider<NXmirror> nexusProvider = nexusDevice.getNexusProvider(null);
		assertThat(nexusProvider, is(notNullValue()));
		checkNexusProvider(nexusProvider);

		final NXmirror mirror = nexusProvider.getNexusObject();
		assertThat(mirror, is(notNullValue()));
		assertThat(mirror.getDataNodeNames(), containsInAnyOrder(NXmirror.NX_TYPE, NXmirror.NX_DESCRIPTION,
				NXmirror.NX_INTERIOR_ATMOSPHERE, NXmirror.NX_INCIDENT_ANGLE,
				NXmirror.NX_SUBSTRATE_THICKNESS, NXmirror.NX_SUBSTRATE_MATERIAL));
		assertThat(mirror.getTypeScalar(), is(equalTo(newType)));
		assertThat(mirror.getDescriptionScalar(), is(equalTo(newDescription)));
		assertThat(mirror.getInterior_atmosphereScalar(), is(equalTo(newInteriorAtmosphere)));
		assertThat(mirror.getIncident_angleScalar(), is(equalTo(getScannableValue(INCIDENT_ANGLE_SCANNABLE_NAME))));
		assertThat(mirror.getAttrString(NXmirror.NX_INCIDENT_ANGLE, ATTRIBUTE_NAME_UNITS), is(equalTo(UNITS_ATTR_VAL_DEGREES)));
		assertThat(mirror.getSubstrate_thicknessScalar(), is(closeTo(newSubstrateThickness, 1e-15)));
		assertThat(mirror.getSubstrate_materialScalar(), is(equalTo(newSubstrateMaterial)));
	}

	@Test
	void testCannotGetScannablePosition_failOnError() {
		final NexusMetadataDevice<NXslit> nexusDevice = new NexusMetadataDevice<>();
		nexusDevice.setNexusClass(NexusBaseClass.NX_SLIT.toString());

		final List<MetadataNode> childNodes = new ArrayList<>();
		childNodes.add(new ScannableField(NXslit.NX_X_GAP, SLIT_X_GAP_SCANNABLE_NAME, true));
		childNodes.add(new ScannableField(NXslit.NX_Y_GAP, SLIT_Y_GAP_SCANNABLE_NAME, true));
		childNodes.add(new ScalarField(NXslit.NX_DEPENDS_ON, EXPECTED_SLIT_DEPENDS_ON));
		nexusDevice.setChildNodes(childNodes);

		final NexusException e = assertThrows(NexusException.class, () -> nexusDevice.getNexusProvider(null));
		assertThat(e.getMessage(), is(equalTo(EXPECTED_SLIT_X_GAP_ERROR_MESSAGE)));
	}

	@Test
	void testCannotGetScannablePosition_continueOnError() throws Exception {
		final NexusMetadataDevice<NXslit> nexusDevice = new NexusMetadataDevice<>();
		nexusDevice.setNexusClass(NexusBaseClass.NX_SLIT.toString());

		final List<MetadataNode> childNodes = new ArrayList<>();
		childNodes.add(new ScannableField(NXslit.NX_X_GAP, SLIT_X_GAP_SCANNABLE_NAME, false));
		childNodes.add(new ScannableField(NXslit.NX_Y_GAP, SLIT_Y_GAP_SCANNABLE_NAME, false));
		childNodes.add(new ScalarField(NXslit.NX_DEPENDS_ON, EXPECTED_SLIT_DEPENDS_ON));
		nexusDevice.setChildNodes(childNodes);

		final NexusObjectProvider<NXslit> nexusProvider = nexusDevice.getNexusProvider(null);
		assertThat(nexusProvider, is(notNullValue()));
		assertThat(nexusProvider.getName(), is(equalTo(nexusDevice.getName())));

		final NXslit slit = nexusProvider.getNexusObject();
		assertThat(slit, is(notNullValue()));
		assertThat(slit.getDataNodeNames(), containsInAnyOrder(NXslit.NX_X_GAP, NXslit.NX_Y_GAP, NXslit.NX_DEPENDS_ON));
		assertThat(slit.getX_gap().getSlice().getString(), is(equalTo(EXPECTED_SLIT_X_GAP_ERROR_MESSAGE)));
		assertThat(slit.getY_gapScalar(), is(equalTo(getScannableValue(SLIT_Y_GAP_SCANNABLE_NAME))));
		assertThat(slit.getDepends_onScalar(), is(equalTo(EXPECTED_SLIT_DEPENDS_ON)));
	}

	@Test
	void collectionNameGetsSetOnNexusObjectProvider() throws Exception {
		var collectionName = "configuration_summary";
		final NexusMetadataDevice<NXslit> nexusDevice = new NexusMetadataDevice<>();
		nexusDevice.setNexusClass(NexusBaseClass.NX_SLIT.toString());
		nexusDevice.setCollectionName(collectionName);

		var provider = nexusDevice.getNexusProvider(null);
		assertThat(provider.getCollectionName(), is(equalTo(collectionName)));
	}

}
