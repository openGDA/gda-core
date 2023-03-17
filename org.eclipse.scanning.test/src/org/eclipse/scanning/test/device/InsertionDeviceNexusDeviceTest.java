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

package org.eclipse.scanning.test.device;

import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertUnits;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.scanning.device.InsertionDeviceNexusDevice;
import org.eclipse.scanning.device.InsertionDeviceNexusDevice.InsertionDeviceType;
import org.eclipse.scanning.device.MetadataAttribute;
import org.eclipse.scanning.device.MetadataNode;
import org.eclipse.scanning.device.ScalarField;
import org.eclipse.scanning.device.ScalarMetadataAttribute;
import org.eclipse.scanning.device.ScannableField;
import org.eclipse.scanning.device.ScannableMetadataAttribute;

import gda.TestHelpers;
import gda.factory.Factory;
import gda.factory.Finder;

class InsertionDeviceNexusDeviceTest extends AbstractNexusMetadataDeviceTest<NXinsertion_device> {

	private static final String GAP_SCANNABLE_NAME = "gap";
	private static final String TAPER_SCANNABLE_NAME = "taper";
	private static final String HARMONIC_SCANNABLE_NAME = "harmonic";

	private static final String BANDWIDTH_SCANNABLE_NAME = "bandwidth";
	private static final double INSERTION_DEVICE_LENGTH = 3.5;
	private static final String CUSTOM_FIELD_NAME = "custom";
	private static final String CUSTOM_FIELD_VALUE = "customValue";

	private static final String DEFAULT_PATH = "path/to/default";

	private static final String SCANNABLE_ATTR_NAME = "scannableAttr";
	private static final String ATTR_SCANNABLE_NAME = "attrScannable";
	private static final double ATTR_SCANNABLE_VALUE = 5.23;

	@Override
	protected void setupTestFixtures() throws Exception {
		final Factory factory = TestHelpers.createTestFactory();
		factory.addFindable(createMockScannable(GAP_SCANNABLE_NAME, 2.3, UNITS_ATTR_VAL_MILLIMETERS));
		factory.addFindable(createMockScannable(TAPER_SCANNABLE_NAME, 7.24, UNITS_ATTR_VAL_DEGREES));
		factory.addFindable(createMockScannable(HARMONIC_SCANNABLE_NAME, 2l, null));
		factory.addFindable(createMockScannable(BANDWIDTH_SCANNABLE_NAME, 15.2, UNITS_ATTR_VAL_GEV));
		factory.addFindable(createMockScannable(ATTR_SCANNABLE_NAME, ATTR_SCANNABLE_VALUE));
		Finder.addFactory(factory);
	}

	@Override
	protected INexusDevice<NXinsertion_device> setupNexusDevice() throws Exception {
		final InsertionDeviceNexusDevice insertionDevice = new InsertionDeviceNexusDevice();
		insertionDevice.setName("insertionDevice");

		// set up fields with setters
		insertionDevice.setType(InsertionDeviceType.WIGGLER.toString());
		insertionDevice.setGapScannableName(GAP_SCANNABLE_NAME);
		insertionDevice.setTaperScannableName(TAPER_SCANNABLE_NAME);
		insertionDevice.setHarmonicScannableName(HARMONIC_SCANNABLE_NAME);

		// set up custom fields
		final List<MetadataNode> customFields = new ArrayList<>();
		customFields.add(new ScannableField(NXinsertion_device.NX_BANDWIDTH, BANDWIDTH_SCANNABLE_NAME));
		customFields.add(new ScalarField(NXinsertion_device.NX_LENGTH, INSERTION_DEVICE_LENGTH, UNITS_ATTR_VAL_MILLIMETERS));
		customFields.add(new ScalarField(CUSTOM_FIELD_NAME, CUSTOM_FIELD_VALUE));
		insertionDevice.setCustomNodes(customFields);

		// set up attributes
		final List<MetadataAttribute> attributes = new ArrayList<>();
		attributes.add(new ScalarMetadataAttribute(NXinsertion_device.NX_ATTRIBUTE_DEFAULT, DEFAULT_PATH));
		attributes.add(new ScannableMetadataAttribute(SCANNABLE_ATTR_NAME, ATTR_SCANNABLE_NAME));
		insertionDevice.setAttributes(attributes);

		return insertionDevice;
	}

	@Override
	protected void checkNexusObject(NXinsertion_device insertionDevice) throws Exception {
		assertThat(insertionDevice.getDataNodeNames(), containsInAnyOrder(NXinsertion_device.NX_TYPE,
				NXinsertion_device.NX_GAP, NXinsertion_device.NX_TAPER, NXinsertion_device.NX_HARMONIC,
				NXinsertion_device.NX_BANDWIDTH, NXinsertion_device.NX_LENGTH, CUSTOM_FIELD_NAME));

		assertThat(insertionDevice.getTypeScalar(), is(InsertionDeviceType.WIGGLER.toString()));
		assertThat(insertionDevice.getGapScalar(), is(equalTo(getScannableValue(GAP_SCANNABLE_NAME))));
		assertUnits(insertionDevice, NXinsertion_device.NX_GAP, UNITS_ATTR_VAL_MILLIMETERS);
		assertThat(insertionDevice.getTaperScalar(), is(equalTo(getScannableValue(TAPER_SCANNABLE_NAME))));
		assertUnits(insertionDevice, NXinsertion_device.NX_TAPER, UNITS_ATTR_VAL_DEGREES);
		assertThat(insertionDevice.getHarmonicScalar(), is(equalTo(getScannableValue(HARMONIC_SCANNABLE_NAME))));
		assertUnits(insertionDevice, NXinsertion_device.NX_HARMONIC, null);
		assertThat(insertionDevice.getBandwidthScalar(), is(equalTo(getScannableValue(BANDWIDTH_SCANNABLE_NAME))));
		assertUnits(insertionDevice, NXinsertion_device.NX_BANDWIDTH, UNITS_ATTR_VAL_GEV);

		assertThat(insertionDevice.getLengthScalar(), is(equalTo(INSERTION_DEVICE_LENGTH)));
		assertUnits(insertionDevice, NXinsertion_device.NX_LENGTH, UNITS_ATTR_VAL_MILLIMETERS);
		assertThat(insertionDevice.getDataset(CUSTOM_FIELD_NAME),
				is(equalTo(DatasetFactory.createFromObject(CUSTOM_FIELD_VALUE))));

		assertThat(insertionDevice.getAttributeNames(), containsInAnyOrder(NexusConstants.NXCLASS,
				NXinsertion_device.NX_ATTRIBUTE_DEFAULT, SCANNABLE_ATTR_NAME));
		assertThat(insertionDevice.getAttributeDefault(), is(equalTo(DEFAULT_PATH)));
		assertThat(insertionDevice.getAttribute(SCANNABLE_ATTR_NAME).getValue(),
				is(equalTo(DatasetFactory.createFromObject(ATTR_SCANNABLE_VALUE))));
	}

}
