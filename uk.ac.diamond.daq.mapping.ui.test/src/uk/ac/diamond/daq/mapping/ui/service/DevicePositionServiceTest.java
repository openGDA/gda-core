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

package uk.ac.diamond.daq.mapping.ui.service;

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;
import static gda.configuration.properties.LocalProperties.GDA_PROPERTIES_FILE;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.IScannableMotor;
import gda.device.Scannable;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.client.properties.stage.services.DevicePositionDocumentService;
import uk.ac.gda.test.helpers.ClassLoaderInitializer;
import uk.ac.gda.ui.tool.spring.FinderService;

/**
 * Tests the {@link DevicePositionDocumentService}
 *
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DevicePositionServiceTestConfiguration.class }, initializers = {ClassLoaderInitializer.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class DevicePositionServiceTest {

	@Autowired
	DevicePositionDocumentService helper;

	@Autowired
	FinderService finderService;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/defaultContext");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/defaultContext/properties/_common/common_instance_java.properties");
	}

	/**
	 * Verifies the service can handle {@link IScannableMotor} devices
	 * @throws DeviceException
	 */
	@Test
	public void selectScannableMotor() throws DeviceException {
		IScannableMotor scannableMotor = mock(IScannableMotor.class);
		doReturn("device_one").when(scannableMotor).getName();
		doReturn(2.3).when(scannableMotor).getPosition();

		doReturn(Optional.of(scannableMotor)).when(finderService).getFindableObject("device_one", IScannableMotor.class);

		DevicePositionDocument document = helper.devicePositionAsDocument("device_one", IScannableMotor.class);
		Assert.assertEquals("device_one", document.getDevice());
		Assert.assertEquals(2.3, (double) document.getPosition(), 0);
	}

	/**
	 * Verifies the service can handle {@link EnumPositioner} devices
	 * @throws DeviceException
	 */
	@Test
	public void selectEnumPositioner() throws DeviceException {
		EnumPositioner positioner = mock(EnumPositioner.class);
		doReturn("device_two").when(positioner).getName();
		doReturn("CLOSE").when(positioner).getPosition();
		doReturn(Optional.of(positioner)).when(finderService).getFindableObject("device_two", EnumPositioner.class);

		DevicePositionDocument document = helper.devicePositionAsDocument("device_two", EnumPositioner.class);
		Assert.assertEquals("device_two", document.getDevice());
		Assert.assertEquals("CLOSE", document.getPosition());
	}

	/**
	 * Verifies the service can handle unknown devices
	 */
	@Test
	public void selectUnknown() {
		Scannable positioner = mock(Scannable.class);
		doReturn("mistery_one").when(positioner).getName();

		doReturn(Optional.of(positioner)).when(finderService).getFindableObject("device_two", EnumPositioner.class);

		DevicePositionDocument document = helper.devicePositionAsDocument("device_two", EnumPositioner.class);
		Assert.assertNull(document);
	}
}
