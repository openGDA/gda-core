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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import uk.ac.gda.tomography.controller.TomographyControllerException;
import uk.ac.gda.tomography.model.EndAngle;
import uk.ac.gda.tomography.model.ImageCalibration;
import uk.ac.gda.tomography.model.MultipleScans;
import uk.ac.gda.tomography.model.MultipleScansType;
import uk.ac.gda.tomography.model.RangeType;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.model.StartAngle;
import uk.ac.gda.tomography.model.TomographyScanParameters;
import uk.ac.gda.tomography.service.TomographyServiceException;

public class TomographyScanParametersTest {

	private File tempFile;

	@Before
	public void before() throws TomographyServiceException {
	}

	@After
	public void after() throws TomographyServiceException {

	}

//	@Test
//	public void loadConfigurationFromString() throws TomographyControllerException {
//		String jsonData = getResourceAsString("/resources/simpleTomographyConfiguration.json");
//		controller.loadData(jsonData);
//		TomographyScanParameters data = controller.getData();
//		Assert.assertEquals(12, data.getNumberOfProjections());
//	}

	@Test
	public void loadConfigurationFromFile() throws TomographyControllerException {
		TomographyScanParameters conf = new TomographyScanParameters();
		conf.setScanType(ScanType.FLY);

		StartAngle startAngle = new StartAngle();
		startAngle.setStart(2.3);
		startAngle.setUseCurrentAngle(false);
		conf.setStart(startAngle);

		EndAngle endAngle = new EndAngle();
		endAngle.setRangeType(RangeType.RANGE_360);
		endAngle.setNumberRotation(3);
		endAngle.setCustomAngle(25.2);
		conf.setEnd(endAngle);

		ImageCalibration imageCalibration = new ImageCalibration();
		imageCalibration.setAfterAcquisition(true);
		imageCalibration.setBeforeAcquisition(false);
		imageCalibration.setNumberDark(2);
		imageCalibration.setNumberFlat(2);
		conf.setImageCalibration(imageCalibration);

		MultipleScans multipleScans = new MultipleScans();
		multipleScans.setMultipleScansType(MultipleScansType.SWITCHBACK_SCAN);
		multipleScans.setNumberRepetitions(3);
		multipleScans.setWaitingTime(1000);
		conf.setMultipleScans(multipleScans);


		Gson gson = new Gson();
		System.out.println(gson.toJson(conf));
	}

}
