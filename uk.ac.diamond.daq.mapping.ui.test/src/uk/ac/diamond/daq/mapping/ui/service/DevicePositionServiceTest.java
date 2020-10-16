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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.device.enumpositioner.EpicsEnumPositioner;
import gda.factory.Findable;
import uk.ac.diamond.daq.mapping.ui.services.position.DevicePositionDocumentService;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument.ValueType;
import uk.ac.gda.ui.tool.spring.FinderService;

/**
 * Tests the {@link DevicePositionDocumentService}
 *
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DevicePositionServiceTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class DevicePositionServiceTest {

	@Autowired
	DevicePositionDocumentService helper;

	@Autowired
	FinderService finderService;

	/**
	 * Verifies the service can handle {@link IScannableMotor} devices
	 * @throws DeviceException
	 */
	@Test
	public void selectScannableMotor() throws DeviceException {
		IScannableMotor scannableMotor = mock(IScannableMotor.class);
		doReturn("device_one").when(scannableMotor).getName();
		doReturn(2.3).when(scannableMotor).getPosition();

		doReturn(Optional.of(scannableMotor)).when(finderService).getFindableObject("device_one");

		DevicePositionDocument document = helper.devicePositionAsDocument("device_one");
		Assert.assertEquals("device_one", document.getDevice());
		Assert.assertEquals(2.3, document.getPosition(), 0);
		Assert.assertEquals(ValueType.NUMERIC, document.getValueType());
		Assert.assertNull(document.getLabelledPosition());
	}

	/**
	 * Verifies the service can handle {@link EpicsEnumPositioner} devices
	 * @throws DeviceException
	 */
	@Test
	public void selectEpicsEnumPositioner() throws DeviceException {
		EpicsEnumPositioner positioner = mock(EpicsEnumPositioner.class);
		doReturn("device_two").when(positioner).getName();
		doReturn("CLOSE").when(positioner).getPosition();
		doReturn(Optional.of(positioner)).when(finderService).getFindableObject("device_two");

		DevicePositionDocument document = helper.devicePositionAsDocument("device_two");
		Assert.assertEquals("device_two", document.getDevice());
		Assert.assertEquals("CLOSE", document.getLabelledPosition());
		Assert.assertEquals(ValueType.LABELLED, document.getValueType());
		Assert.assertEquals(0.0, document.getPosition(), 0.0);
	}

	/**
	 * Verifies the service can handle unknown devices
	 */
	@Test
	public void selectUnknown() {
		Findable positioner = mock(Findable.class);
		doReturn("mistery_one").when(positioner).getName();

		doReturn(Optional.of(positioner)).when(finderService).getFindableObject("device_two");

		DevicePositionDocument document = helper.devicePositionAsDocument("device_two");
		Assert.assertNull(document);
	}
}
