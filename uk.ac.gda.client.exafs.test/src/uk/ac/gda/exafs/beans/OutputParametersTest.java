/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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
import static org.junit.Assert.fail;
import gda.exafs.scan.ExafsValidator;
import gda.util.TestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.runtime.content.IContentDescriber;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.beans.validation.InvalidBeanMessage;
import uk.ac.gda.exafs.ui.describers.OutputDescriber;
import uk.ac.gda.util.PackageUtils;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * class to hold sample parameters
 */
public class OutputParametersTest {
	final static String testScratchDirectoryName = TestUtils
			.generateDirectorynameFromClassname(OutputParametersTest.class.getCanonicalName());


	public static OutputParameters createFromXML(String filename) throws Exception {
		return (OutputParameters) XMLHelpers.createFromXML(OutputParameters.mappingURL, OutputParameters.class, OutputParameters.schemaUrl, filename);
	}

	public static void writeToXML(OutputParameters outputParams, String filename) throws Exception {
		XMLHelpers.writeToXML(OutputParameters.mappingURL, outputParams, filename);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}

	@Test
	public void testDescriber() {
		try {
			InputStream contents = new FileInputStream(new File(PackageUtils.getTestPath(getClass())
					+ "OutputParameters.xml"));
			OutputDescriber describer = new OutputDescriber();
			assertEquals(IContentDescriber.VALID, describer.describe(contents, null));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * test for file not existing
	 */
	@Test
	public void testCreateFromXML_FileDoesNotExist() {
		try {
			createFromXML("DoesNotExists");
			fail("File does not exists");
		} catch (FileNotFoundException ex) {
			// expected behaviour
		} catch (Exception ex) {
			fail("Invalid exception thrown - " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testUnderscoreWorks() {

		OutputParameters o = new OutputParameters();
		o.setAsciiFileName("FeKedge_1");
		o.setAsciiDirectory("ascii_1");
		o.setNexusDirectory("nexus_1");
		o.addSignal(new SignalParameters("Bragg", "bragg", "9.4f", "bragg/I0", "Mono"));
		o.addSignal(new SignalParameters("Time(sec)", "time", "%s", "time*1000", "EpicsClock"));
		o.addSignal(new SignalParameters("Temp", "temp", "%s", "temp", "Eurotherm"));

		ExafsValidator._setCheckingFinables(false);
		
		
		validate(o);

	}

	private void validate(OutputParameters o) {
		List<InvalidBeanMessage> errors = new ExafsValidator() {
			@Override
			public void validate(uk.ac.gda.client.experimentdefinition.IExperimentObject bean)
					throws uk.ac.gda.beans.validation.InvalidBeanException {
				// unused
			}
		}.validateIOutputParameters(o);
		if (errors.size() > 0){
			fail(errors.get(0).getPrimaryMessage());
		}
	}

	@Test
	public void testIllegalCharacters() throws Exception {

		OutputParameters o = new OutputParameters();
		o.setAsciiFileName("FeKedge_$");
		o.setAsciiDirectory("ascii_$");
		o.setNexusDirectory("nexus_$");
		o.addSignal(new SignalParameters("Bragg", "bragg", "9.4f", "bragg/I0", "Mono"));
		o.addSignal(new SignalParameters("Time(sec)", "time", "%s", "time*1000", "EpicsClock"));
		o.addSignal(new SignalParameters("Temp", "temp", "%s", "temp", "Eurotherm"));

		ExafsValidator._setCheckingFinables(false);

		List<InvalidBeanMessage> errors = new ExafsValidator() {
			@Override
			public void validate(uk.ac.gda.client.experimentdefinition.IExperimentObject bean)
					throws uk.ac.gda.beans.validation.InvalidBeanException {
				// unused
			}
		}.validateIOutputParameters(o);

		if (errors.size() == 0){
			throw new Exception("$ character in names passed the checks!");
		}
	}

	/**
	 * test for xml file with output parameters
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateFromXML_withValid() throws Exception {
		OutputParameters expectedValue = new OutputParameters();
		expectedValue.setAsciiFileName("FeKedge");
		expectedValue.setAsciiDirectory("ascii");
		expectedValue.setNexusDirectory("nexus");
		expectedValue.addSignal(new SignalParameters("Bragg", "bragg", "9.4f", "bragg/I0", "Mono"));
		expectedValue.addSignal(new SignalParameters("Time(sec)", "time", "%s", "time*1000", "EpicsClock"));
		expectedValue.addSignal(new SignalParameters("Temp", "temp", "%s", "temp", "Eurotherm"));

		OutputParameters s = createFromXML(PackageUtils.getTestPath(getClass())
				+ "OutputParameters.xml");
		ExafsValidator._setCheckingFinables(false);
		validate(s);
		if (!expectedValue.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
	}

	@Test
	public void testWriteToXML() throws Exception {
		OutputParameters op = new OutputParameters();
		op.setAsciiFileName("FeKedge");
		op.setAsciiDirectory("ascii");
		op.setNexusDirectory("nexus");
		op.addSignal(new SignalParameters("Bragg", "bragg", "9.4f", "bragg/I0", "Mono"));
		op.addSignal(new SignalParameters("Time(sec)", "time", "%s", "time*1000", "EpicsClock"));
		op.addSignal(new SignalParameters("Temp", "temp", "%s", "temp", "Eurotherm"));

		try {
			writeToXML(op, testScratchDirectoryName + "OutputParameters_written.xml");
		} catch (Exception e) {
			fail("Failed to write xml file - " + e.getCause().getMessage());
		}

		OutputParameters s = createFromXML(testScratchDirectoryName + "OutputParameters_written.xml");
		ExafsValidator._setCheckingFinables(false);
		validate(s);
		if (!op.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}
	}
}
