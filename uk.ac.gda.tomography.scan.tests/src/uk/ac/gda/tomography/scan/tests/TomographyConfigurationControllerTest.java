/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import uk.ac.gda.tomography.controller.TomographyControllerException;
import uk.ac.gda.tomography.model.RangeType;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.model.TomographyScanParameters;
import uk.ac.gda.tomography.scan.editor.TomographyConfigurationController;
import uk.ac.gda.tomography.service.TomographyService;
import uk.ac.gda.tomography.service.TomographyServiceException;

public class TomographyConfigurationControllerTest {

	private TomographyConfigurationController controller;
	private TomographyService service;
	private String jsonData;
	private File tempFile;

	@Before
	public void before() throws TomographyServiceException {
		// Mockito would be better but now I do not want to import packages here
		service = Mockito.mock(TomographyService.class);
		controller = new TomographyConfigurationController(service);
	}

	@After
	public void after() throws TomographyServiceException {
		if (tempFile != null) {
			tempFile.delete();
		}
	}

	@Test
	public void loadConfigurationFromString() throws TomographyControllerException {
		String jsonData = getResourceAsString("/resources/simpleTomographyConfiguration.json");
		controller.loadData(jsonData);
		testPojo(controller.getData());
	}

	@Test
	public void loadConfigurationFromFile() throws TomographyControllerException {
		File jsonData = getResourceAsFile("/resources/simpleTomographyConfiguration.json");
		controller.loadData(jsonData);
		testPojo(controller.getData());
	}

	private void testPojo(TomographyScanParameters data) {
		Assert.assertEquals("tomoTest", data.getName());
		Assert.assertEquals(ScanType.FLY, data.getScanType());
		Assert.assertEquals(2.3, data.getStart().getStart(), 0.0);
		Assert.assertEquals(0.0, data.getStart().getCurrentAngle(), 0.0);
		Assert.assertEquals(false, data.getStart().isUseCurrentAngle());
		Assert.assertEquals(3, data.getEnd().getNumberRotation());
		Assert.assertEquals(RangeType.RANGE_360, data.getEnd().getRangeType());
		Assert.assertEquals(25.2, data.getEnd().getCustomAngle(), 0.0);
	}

	@Test
	public void loadConfigurationFromIDialogSettings() throws TomographyControllerException {
		// TBD
	}

	@Test
	public void saveConfigurationToString() throws TomographyControllerException {
	}

	@Test
	public void saveConfigurationToFile() throws TomographyControllerException {
		// TBD
	}

	@Test
	public void saveConfigurationToIDialogSettings() throws TomographyControllerException {
		// TBD
	}

	private String getResourceAsString(String resource) {
		tempFile = getResourceAsFile(resource);
		try {
			return new String(Files.readAllBytes(Paths.get(tempFile.toURI())));
		} catch (IOException e) {
			Assert.fail("Cannot load the resource");
		}
		return null;
	}

	private File getResourceAsFile(String resource) {
		try {
			tempFile = File.createTempFile(UUID.randomUUID().toString(), "tmp");
			URL url = this.getClass().getResource(resource);
			Files.copy(Paths.get(url.getPath()), new FileOutputStream(tempFile));
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}
		return tempFile;
	}

}
