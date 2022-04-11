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

package uk.ac.diamond.daq.mapping.ui.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.client.AcquisitionManager;
import uk.ac.gda.client.exception.AcquisitionControllerException;
import uk.ac.gda.client.properties.acquisition.AcquisitionKeys;

public class ScanningAcquisitionControllerTest {

	@Test
	public void newScanningAcquisitionCallsNewInAcquisitionManager() throws AcquisitionControllerException {
		ScanningAcquisitionController controller = new ScanningAcquisitionController();

		var keys = mock(AcquisitionKeys.class);
		var acquisition = mock(ScanningAcquisition.class);
		AcquisitionManager acquisitionManager = mock(AcquisitionManager.class);
		when(acquisitionManager.newAcquisition(keys)).thenReturn(acquisition);

		controller.setAcquisitionManager(acquisitionManager);
		controller.newScanningAcquisition(keys);

		verify(acquisitionManager).newAcquisition(keys);
	}

	@Test
	public void initialiseCallsGetInAcquisitionManager() throws AcquisitionControllerException {
		ScanningAcquisitionController controller = new ScanningAcquisitionController();

		var keys = mock(AcquisitionKeys.class);
		AcquisitionManager acquisitionManager = mock(AcquisitionManager.class);
		when(acquisitionManager.getAcquisition(keys)).thenReturn(mock(ScanningAcquisition.class));

		controller.setAcquisitionManager(acquisitionManager);
		controller.initialise(keys);

		verify(acquisitionManager).getAcquisition(keys);

	}

}
