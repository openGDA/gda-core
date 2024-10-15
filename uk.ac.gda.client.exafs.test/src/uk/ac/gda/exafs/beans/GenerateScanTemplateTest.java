/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import gda.util.GenerateScanTemplate;
import uk.ac.gda.beans.exafs.QEXAFSParameters;

public class GenerateScanTemplateTest {

	public static final String FOLDER_PATH = "testfiles";

	String element = "Fe";
	String edge = "K";
	String crystal = "Si111";

	@Test
	public void testQexafsGenerate() throws Exception {
		QEXAFSParameters paras = generateQexafs(element, edge, crystal);

		assertNotNull("Scan parameters should not be null", paras);
	}

	@Test
	public void testFilenameSet() throws Exception {
		String updatefileName = updateFileName();

		assertEquals("Filename is incorrect -", "Fe_K_8112_Si111_QEXAFS_Parameters.xml", updatefileName);
	}

	@Test
	public void testFileWrite() throws Exception {
		String writeFile = writeFile(updateFileName());

		 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder builder = factory.newDocumentBuilder();
		 // Parse the XML file
		 Document document = builder.parse(writeFile);
		 NodeList nodeList = document.getElementsByTagName("QEXAFSParameters");
		 Node node = nodeList.item(0);
		 Element eElement = (Element) node;
		 String eInit = eElement.getElementsByTagName("initialEnergy").item(0).getTextContent();
		 String eSpeed = eElement.getElementsByTagName("speed").item(0).getTextContent();

		assertEquals("initialEnergy is incorrect -", "6912.0", eInit);
		assertEquals("speed is incorrect -",  "13.969", eSpeed);
	}

	private QEXAFSParameters generateQexafs(String element, String edge, String crystal) throws Exception {
		GenerateScanTemplate genScan = new GenerateScanTemplate();

		return genScan.generateQexafs(element, edge, crystal);
	}

	private String updateFileName() throws Exception {
		GenerateScanTemplate genScan = new GenerateScanTemplate();

		return genScan.updateFileName(generateQexafs(element, edge, crystal), crystal);
	}

	private String writeFile(String filename) throws Exception {
		QEXAFSParameters.writeToXML(generateQexafs(element, edge, crystal), FOLDER_PATH + "/" + filename);

		Path checkFile = Paths.get(FOLDER_PATH + "/" + filename);
		Path fileabsolutePath = checkFile.toAbsolutePath();
		System.out.println(fileabsolutePath);

		return fileabsolutePath.toString();
	}

}
