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

package uk.ac.gda.exafs.beans;

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.junit.Test;

import gda.util.TestUtils;
import uk.ac.gda.beans.exafs.MythenParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class MythenParametersTest {

	private static MythenParameters createFromXML(String filename) throws Exception {
		return XMLHelpers.createFromXML(MythenParameters.mappingURL, MythenParameters.class, MythenParameters.schemaUrl, filename);
	}

	private static void writeToXML(MythenParameters outputParams, String filename) throws Exception {
		XMLHelpers.writeToXML(MythenParameters.mappingURL, outputParams, filename);
	}

	private String getXmlString(MythenParameters params)  {
		return	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<MythenParameters>\n" +
				"    <mythenEnergy>"+params.getMythenEnergy()+"</mythenEnergy>\n" +
				"    <mythenTime>"+params.getMythenTime()+"</mythenTime>\n" +
				"    <mythenFrames>"+params.getMythenFrames()+"</mythenFrames>\n" +
				"</MythenParameters>\n";
	}

	private MythenParameters getTestParameters() {
		MythenParameters params = new MythenParameters();
		params.setMythenEnergy(1000);
		params.setMythenFrames(5);
		params.setMythenTime(2.5);
		return params;
	}

	@Test
	public void testWriteXml() throws Exception {
		String testScratchDirectoryName = TestUtils.setUpTest(MythenParametersTest.class, "testWriteXml", true);
		MythenParameters params = getTestParameters();
		Path filePath = Paths.get(testScratchDirectoryName, "test.xml");
		System.out.println("Writing file to "+filePath);
		writeToXML(params, filePath.toString());

		String fileContent = Files.readString(filePath, Charset.defaultCharset());
		assertEquals(getXmlString(params), fileContent);
	}

	@Test
	public void testReadXml() throws Exception {
		String testScratchDirectoryName = TestUtils.setUpTest(MythenParametersTest.class, "testReadXml", true);
		Path filePath = Paths.get(testScratchDirectoryName, "test.xml");

		MythenParameters params = getTestParameters();
		Files.writeString(filePath, getXmlString(params), Charset.defaultCharset(), StandardOpenOption.CREATE);

		MythenParameters paramsFromFile = createFromXML(filePath.toString());
		assertEquals(params, paramsFromFile);
	}
}
