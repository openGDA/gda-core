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

package uk.ac.diamond.daq.mapping.document.acquisition;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.document.DocumentTestBase;
import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.common.exception.GDAException;

public class AcquisitionBaseTest extends DocumentTestBase {

	@Test
	public void serializeThenDeserializeDiffractionParameterAcquisition() throws GDAException {
		Acquisition<?> acquisition = new ScanningAcquisition();
		final String json = getDocumentMapper().convertToJSON(acquisition);
		final Acquisition<?> read = getDocumentMapper().convertFromJSON(json, AcquisitionBase.class);
		assertTrue(read instanceof ScanningAcquisition);
	}

	@Test
	public void serializeDiffractionParameterAcquisition() throws GDAException {
		var acquisition = new ScanningAcquisition();
		var acquisitionConfiguration = new ScanningConfiguration();
		acquisition.setAcquisitionConfiguration(acquisitionConfiguration);

		var acquisitionParameters = new ScanningParameters();
		acquisitionConfiguration.setAcquisitionParameters(acquisitionParameters);
		String document = serialiseDocument(acquisition);
		assertThat(document, containsString("\"documentType\" : \"scanningAcquisition\""));
	}

	@Test
	public void deserializeScanningAcquisition() throws GDAException {
		Acquisition<?> modelDocument = deserialiseDocument("test/resources/acquisitions/simpleScanningAcquisition.json",
				ScanningAcquisition.class);

		assertEquals("SimpleTest", modelDocument.getDescription());
		assertEquals(ScanningConfiguration.class, modelDocument.getAcquisitionConfiguration().getClass());
		assertEquals(ScanningParameters.class,
				modelDocument.getAcquisitionConfiguration().getAcquisitionParameters().getClass());
		ScanningParameters dp = ScanningParameters.class
				.cast(modelDocument.getAcquisitionConfiguration().getAcquisitionParameters());
		assertTrue(dp.getStartPosition().isEmpty());
	}

	@Test
	public void deserializeScanningAcquisition2() throws GDAException {
		Acquisition<?> modelDocument = deserialiseDocument("test/resources/acquisitions/simpleScanningAcquisition2.json",
				ScanningAcquisition.class);

		assertEquals("SimpleTest", modelDocument.getDescription());
		assertEquals(ScanningConfiguration.class, modelDocument.getAcquisitionConfiguration().getClass());
		assertEquals(ScanningParameters.class,
				modelDocument.getAcquisitionConfiguration().getAcquisitionParameters().getClass());
	}

	@Test
	public void deserializeEmptyAcquisition() throws GDAException {
		Acquisition<?> modelDocument = deserialiseDocument("test/resources/acquisitions/emptyAcquisition.json",
				AcquisitionBase.class);
		assertTrue(modelDocument instanceof ScanningAcquisition);
	}

	@Test(expected=GDAException.class)
	public void deserializeUnknownEmptyAcquisition() throws GDAException {
		deserialiseDocument("test/resources/acquisitions/emptyUnknownAcquisition.json",
				AcquisitionBase.class);
	}
}
