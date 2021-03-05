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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXmirror;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.scanning.device.MetadataNode;
import org.eclipse.scanning.device.NexusMetadataDevice;
import org.eclipse.scanning.device.ScalarField;
import org.eclipse.scanning.device.ScannableField;

public class NexusMetadataDeviceTest extends AbstractNexusMetadataDeviceTest<NXmirror> {

	private static final String INCIDENT_ANGLE_SCANNABLE_NAME = "incidentAngle";

	private static final String EXPECTED_TYPE = "single";
	private static final String EXPECTED_DESCRIPTION = "my mirror";
	private static final String EXPECTED_INTERIOR_ATMOSPHERE = "argon";

	@Override
	protected void setupTestFixtures() throws Exception {
		createMockScannable(INCIDENT_ANGLE_SCANNABLE_NAME, 3.45);
	}

	@Override
	protected INexusDevice<NXmirror> setupNexusDevice() throws Exception {
		final NexusMetadataDevice<NXmirror> nexusDevice = new NexusMetadataDevice<>(NexusBaseClass.NX_MIRROR);

		final List<MetadataNode> childNodes = new ArrayList<>();
		childNodes.add(new ScalarField(NXmirror.NX_TYPE, EXPECTED_TYPE));
		childNodes.add(new ScalarField(NXmirror.NX_DESCRIPTION, EXPECTED_DESCRIPTION));
		childNodes.add(new ScalarField(NXmirror.NX_INTERIOR_ATMOSPHERE, EXPECTED_INTERIOR_ATMOSPHERE));
		childNodes.add(new ScannableField(NXmirror.NX_INCIDENT_ANGLE, INCIDENT_ANGLE_SCANNABLE_NAME));
		nexusDevice.setChildNodes(childNodes);

		return nexusDevice;
	}

	@Override
	protected void checkNexusObject(NXmirror mirror) throws Exception {
		assertThat(mirror, is(notNullValue()));
		assertThat(mirror.getTypeScalar(), is(equalTo(EXPECTED_TYPE)));
		assertThat(mirror.getDescriptionScalar(), is(equalTo(EXPECTED_DESCRIPTION)));
		assertThat(mirror.getInterior_atmosphereScalar(), is(equalTo(EXPECTED_INTERIOR_ATMOSPHERE)));
		assertThat(mirror.getIncident_angleScalar(), is(equalTo(getScannableValue(INCIDENT_ANGLE_SCANNABLE_NAME))));
	}

}
