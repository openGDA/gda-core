/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import gda.TestHelpers;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.ISampleParametersWithMotorPositions;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.SampleParameterMotorPosition;
import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.exafs.ui.dialogs.ParameterCollection;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean.ParameterValue;
import uk.ac.gda.exafs.ui.dialogs.ParametersForScan;

public class SpreadsheetViewTest {

	private String scannableName = "nameOfScannble";

	public B18SampleParameters getSampleParameters() {
		SampleParameterMotorPosition sampleMotorPosition = new SampleParameterMotorPosition(scannableName, 10.01, true);

		B18SampleParameters sampleParameters = new B18SampleParameters();
		sampleParameters.setName("sample_name");
		sampleParameters.addSampleParameterMotorPosition(sampleMotorPosition);
		// set some non zero values for some of the motor positions
		sampleParameters.getUserStageParameters().setAxis2(1.0);
		sampleParameters.getUserStageParameters().setAxis4(112.0);
		sampleParameters.getSXCryoStageParameters().setHeight(15.6);
		sampleParameters.getSXCryoStageParameters().setRot(210.2);

		return sampleParameters;
	}

	@Test
	public void testInvokeMethodFromName() throws Exception {

		B18SampleParameters sampleParameters = getSampleParameters();
		SampleParameterMotorPosition sampleMotorPosition = sampleParameters.getSampleParameterMotorPositions().get(0);
		String scannableName = sampleMotorPosition.getScannableName();

		Object result = ParameterValuesForBean.invokeMethodFromName(sampleParameters, "getName", null);
		assertEquals(sampleParameters.getName(), result);

		result = ParameterValuesForBean.invokeMethodFromName(sampleParameters, ISampleParametersWithMotorPositions.MOTOR_POSITION_GETTER_NAME+"("+scannableName+")", null);
		assertEquals(sampleMotorPosition, result);

		result = ParameterValuesForBean.invokeMethodFromName(sampleParameters, ISampleParametersWithMotorPositions.MOTOR_POSITION_GETTER_NAME+"("+scannableName+")."+SampleParameterMotorPosition.DO_MOVE_GETTER_NAME, null);
		assertEquals(sampleMotorPosition.getDoMove(), result);

		result = ParameterValuesForBean.invokeMethodFromName(sampleParameters, ISampleParametersWithMotorPositions.MOTOR_POSITION_GETTER_NAME+"("+scannableName+")."+SampleParameterMotorPosition.DEMAND_POSITION_GETTER_NAME, null);
		assertEquals(sampleMotorPosition.getDemandPosition(), result);

		result = ParameterValuesForBean.invokeMethodFromName(sampleParameters, "getUserStageParameters.getAxis2", null);
		assertEquals(sampleParameters.getUserStageParameters().getAxis2(), result);

		result = ParameterValuesForBean.invokeMethodFromName(sampleParameters, "getUserStageParameters.getAxis4", null);
		assertEquals(sampleParameters.getUserStageParameters().getAxis4(), result);

		result = ParameterValuesForBean.invokeMethodFromName(sampleParameters, "getSXCryoStageParameters.getHeight", null);
		assertEquals(sampleParameters.getSXCryoStageParameters().getHeight(), result);

		result = ParameterValuesForBean.invokeMethodFromName(sampleParameters, "getSXCryoStageParameters.getRot", null);
		assertEquals(sampleParameters.getSXCryoStageParameters().getRot(), result);
	}

	@Test
	public void testUpdateBeanWIthOverrides() {
		B18SampleParameters sampleParameters = getSampleParameters();

		String newSampleName = "new sample name";
		double newAxisPosition = 15.7;
		double newDemandPosition = 50.56;
		boolean newDoMove = false;

		ParameterValuesForBean newParameterValues = new ParameterValuesForBean();
		newParameterValues.addParameterValue("getName", newSampleName);
		newParameterValues.addParameterValue("getUserStageParameters.getAxis2", newAxisPosition);
		newParameterValues.addParameterValue("getSampleParameterMotorPosition("+scannableName+").getDemandPosition", newDemandPosition);
		newParameterValues.addParameterValue("getSampleParameterMotorPosition("+scannableName+").getDoMove", newDoMove);

		newParameterValues.setValuesOnBean(sampleParameters);

		assertEquals(newSampleName, sampleParameters.getName());
		assertEquals(newAxisPosition, sampleParameters.getUserStageParameters().getAxis2(), 0.0001);
		assertEquals(newDemandPosition, sampleParameters.getSampleParameterMotorPosition(scannableName).getDemandPosition(), 0.0001);
		assertEquals(newDoMove, sampleParameters.getSampleParameterMotorPosition(scannableName).getDoMove());
	}

	private ParametersForScan getParameters() {
		ParameterValuesForBean sampleParams = new ParameterValuesForBean();
		sampleParams.setBeanType(ISampleParameters.class.getName());

		sampleParams.setBeanFileName("sample.xml");
		sampleParams.addParameterValue("getName", "sample name");
		sampleParams.addParameterValue("getUserStageParameters.getAxis2", 20.2);
		sampleParams.addParameterValue("getSampleParameterMotorPosition("+scannableName+").getDemandPosition", 99.9);
		sampleParams.addParameterValue("getSampleParameterMotorPosition("+scannableName+").getDoMove", true);

		ParameterValuesForBean scanParams = new ParameterValuesForBean();
		scanParams.setBeanType(IScanParameters.class.getName());
		scanParams.setBeanFileName("qexafs.xml");

		ParameterValuesForBean outputParams = new ParameterValuesForBean();
		outputParams.setBeanType(IOutputParameters.class.getName());

		outputParams.setBeanFileName("output.xml");

		ParameterValuesForBean detectorParams = new ParameterValuesForBean();
		detectorParams.setBeanType(IDetectorParameters.class.getName());
		detectorParams.setBeanFileName("detector.xml");

		ParametersForScan paramsForScan = new ParametersForScan();
		paramsForScan.setValuesForScanBeans(Arrays.asList(scanParams, detectorParams, sampleParams, outputParams));
		return paramsForScan;
	}

	private ParameterCollection getParamCollection() {
		ParameterCollection allParams = new ParameterCollection();
		allParams.addParametersForScan(getParameters());
		DetectorParameters.class.getPackage().getName();
		for(int i=0; i<10; i++) {
			ParametersForScan paramsForScan = getParameters();
			// change the name of the scan and position of motor
			ParameterValuesForBean sampleParamBean = paramsForScan.getParameterValuesForScanBeans().get(2);
			ParameterValue p = sampleParamBean.getParameterValue("getName");
			p.setNewValue("sample "+i);
			p = sampleParamBean.getParameterValue("getSampleParameterMotorPosition("+scannableName+").getDemandPosition");
			p.setNewValue(i + 10.2);
			paramsForScan.setNumberOfRepetitions(i+1);
			allParams.addParametersForScan(paramsForScan);
		}
		return allParams;
	}

	@Test
	public void testWriteReadXml() throws Exception {
		String testDir = TestHelpers.setUpTest(SpreadsheetViewTest.class, "testWriteReadXml", true);
		Path xmlFilePath = Paths.get(testDir, "testParams.xml").toAbsolutePath();

		ParameterCollection allParams = new ParameterCollection(Arrays.asList(getParameters()));

		assertNotNull("XML string should not be null", allParams.toXML());

		allParams.saveToFile(xmlFilePath.toString());
		assertTrue("XML file "+xmlFilePath.toString()+" was not written", Files.exists(xmlFilePath));

		List<ParametersForScan> parametersForScans = ParameterCollection.loadFromFile(xmlFilePath.toString());

		// Check re-serialized bean matches original file contents
		String xmlFileString = FileUtils.readFileToString(xmlFilePath.toFile(), Charset.defaultCharset());
		assertEquals(xmlFileString, new ParameterCollection(parametersForScans).toXML());

		assertEquals(allParams.getParametersForScans().get(0).getParameterValuesForScanBeans(),
				parametersForScans.get(0).getParameterValuesForScanBeans());
	}

	@Test
	public void testXmlSerialization() throws IOException {
		ParameterCollection allParams = getParamCollection();
		String xmlParamString = allParams.toXML();
		assertNotNull("XML string should not be null", xmlParamString);
		assertEquals(allParams.getParametersForScans(), ParameterCollection.fromXML(xmlParamString));
	}

	@Test
	public void testCsvSerialization() throws IOException {
		ParameterCollection allParams = getParamCollection();
		String csvParamString = allParams.toCSV();
		List<ParametersForScan> paramsFromCsv = ParameterCollection.fromCSV(csvParamString);
		assertEquals(allParams.getParametersForScans(), paramsFromCsv);
	}

	@Test
	public void testXmlFileSaveLoad() throws Exception {
		String testDir = TestHelpers.setUpTest(this.getClass(), "testXmlFileSaving", true);
		ParameterCollection allParams = getParamCollection();
		Path fullPath = Paths.get(testDir, "allParams.xml");
		allParams.saveToFile(fullPath.toString());
		assertTrue(Files.exists(fullPath));

		List<ParametersForScan> paramsForScans = ParameterCollection.loadFromFile(fullPath.toString());
		assertEquals(allParams.getParametersForScans(), paramsForScans);
	}

	@Test
	public void testCsvFileSaveLoad() throws Exception {
		String testDir = TestHelpers.setUpTest(this.getClass(), "testCsvFileSaveLoad", true);
		ParameterCollection allParams = getParamCollection();
		Path fullPath = Paths.get(testDir, "allParams.csv");
		allParams.saveCsvToFile(fullPath.toString());
		assertTrue(Files.exists(fullPath));

		List<ParametersForScan> paramsForScans = ParameterCollection.loadCsvFromFile(fullPath.toString());
		assertEquals(allParams.getParametersForScans(), paramsForScans);
	}
}
