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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXmirror;
import org.eclipse.scanning.device.MetadataNode;
import org.eclipse.scanning.device.NexusMetadataDevice;
import org.eclipse.scanning.device.ScalarField;
import org.eclipse.scanning.device.ScannableField;

public class NexusMetadataDeviceTest extends AbstractNexusMetadataDeviceTest<NXmirror> {

	private static final String INCIDENT_ANGLE_SCANNABLE_NAME = "incidentAngle";

	private static final String EXPECTED_TYPE = "single";
	private static final String EXPECTED_DESCRIPTION = "my mirror";
	private static final String EXPECTED_INTERIOR_ATMOSPHERE = "argon";
	private static final double EXPECTED_M_VALUE = 3.456;
	private static final double EXPECTED_LAYER_THICKNESS = 98.76;

	@Override
	protected void setupTestFixtures() throws Exception {
		createMockScannable(INCIDENT_ANGLE_SCANNABLE_NAME, 3.45);
	}

	@Override
	protected INexusDevice<NXmirror> setupNexusDevice() throws Exception {
		final NexusMetadataDevice<NXmirror> nexusDevice = new NexusMetadataDevice<>();
		nexusDevice.setNexusClass("NXmirror");

		final List<MetadataNode> childNodes = new ArrayList<>();
		childNodes.add(new ScalarField(NXmirror.NX_TYPE, EXPECTED_TYPE));
		childNodes.add(new ScalarField(NXmirror.NX_DESCRIPTION, EXPECTED_DESCRIPTION));
		childNodes.add(new ScalarField(NXmirror.NX_INTERIOR_ATMOSPHERE, EXPECTED_INTERIOR_ATMOSPHERE));
		childNodes.add(new ScalarField(NXmirror.NX_M_VALUE, EXPECTED_M_VALUE));
		childNodes.add(new ScalarField(NXmirror.NX_LAYER_THICKNESS, EXPECTED_LAYER_THICKNESS, UNITS_ATTR_VAL_MILLIMETERS));
		childNodes.add(new ScannableField(NXmirror.NX_INCIDENT_ANGLE, INCIDENT_ANGLE_SCANNABLE_NAME, UNITS_ATTR_VAL_DEGREES));
		nexusDevice.setChildNodes(childNodes);

		return nexusDevice;
	}

	@Override
	protected void checkNexusObject(NXmirror mirror) throws Exception {
		assertThat(mirror, is(notNullValue()));
		assertThat(mirror.getDataNodeNames(), containsInAnyOrder(NXmirror.NX_TYPE, NXmirror.NX_DESCRIPTION,
				NXmirror.NX_INTERIOR_ATMOSPHERE, NXmirror.NX_M_VALUE, NXmirror.NX_INCIDENT_ANGLE, NXmirror.NX_LAYER_THICKNESS));
		assertThat(mirror.getTypeScalar(), is(equalTo(EXPECTED_TYPE)));
		assertThat(mirror.getDescriptionScalar(), is(equalTo(EXPECTED_DESCRIPTION)));
		assertThat(mirror.getInterior_atmosphereScalar(), is(equalTo(EXPECTED_INTERIOR_ATMOSPHERE)));
		assertThat(mirror.getIncident_angleScalar(), is(equalTo(getScannableValue(INCIDENT_ANGLE_SCANNABLE_NAME))));
		assertThat(mirror.getAttrString(NXmirror.NX_INCIDENT_ANGLE, ATTRIBUTE_NAME_UNITS), is(equalTo(UNITS_ATTR_VAL_DEGREES)));
		assertThat(mirror.getM_valueScalar(), is(closeTo(EXPECTED_M_VALUE, 1e-15)));
		assertThat(mirror.getLayer_thicknessScalar(), is(closeTo(EXPECTED_LAYER_THICKNESS, 1e-15)));
		assertThat(mirror.getAttrString(NXmirror.NX_LAYER_THICKNESS, ATTRIBUTE_NAME_UNITS), is(equalTo(UNITS_ATTR_VAL_MILLIMETERS)));
	}

}
