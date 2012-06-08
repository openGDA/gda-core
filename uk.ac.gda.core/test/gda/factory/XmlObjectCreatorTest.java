/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.factory;

import gda.util.TestUtils;
import gda.configuration.properties.LocalProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;

import uk.ac.gda.util.io.MacroSupplier;

/**
 *
 */
public class XmlObjectCreatorTest implements Findable{

	/**
	 * Tests that an ObjectFactory file can be read if the XML has no namespace
	 * and the schema has no namespace.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testXmlWithoutNamespaceSchemaWithoutNamespace() throws Exception {
		testGetObjectFactoryWithXmlAndSchemaShouldSucceed(getXmlWithoutNamespace(), getSchemaWithoutNamespace());
	}
	
	/**
	 * Tests that an ObjectFactory file can be read if the XML has a namespace
	 * and the schema has no namespace.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testXmlWithNamespaceSchemaWithoutNamespace() throws Exception {
		testGetObjectFactoryWithXmlAndSchemaShouldSucceed(getXmlWithNamespace(), getSchemaWithoutNamespace());
	}
	
	/**
	 * Tests that an ObjectFactory file with an error causes an exception, when
	 * the schema has no namespace.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInvalidXmlSchemaWithoutNamespace() throws Exception {
		testGetObjectFactoryWithXmlAndSchemaShouldFail(getInvalidXml(), getSchemaWithoutNamespace());
	}
	
	/**
	 * Tests that an ObjectFactory file can be read if the XML has a namespace
	 * and the schema has no namespace.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testXmlWithoutNamespaceSchemaWithNamespace() throws Exception {
		testGetObjectFactoryWithXmlAndSchemaShouldSucceed(getXmlWithoutNamespace(), getSchemaWithNamespace());
	}
	
	/**
	 * Tests that an ObjectFactory file can be read if the XML has a namespace
	 * and the schema has a namespace.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testXmlWithNamespaceSchemaWithNamespace() throws Exception {
		testGetObjectFactoryWithXmlAndSchemaShouldSucceed(getXmlWithNamespace(), getSchemaWithNamespace());
	}
	
	/**
	 * Tests that an ObjectFactory file with an error causes an exception, when
	 * the schema has a namespace.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInvalidXmlSchemaWithNamespace() throws Exception {
		testGetObjectFactoryWithXmlAndSchemaShouldFail(getInvalidXml(), getSchemaWithNamespace());
	}

	protected void testGetObjectFactoryWithXmlAndSchemaShouldSucceed(String xmlFile, String schemaFile) throws Exception {
		XmlObjectCreator oc = new XmlObjectCreator();
		oc.setXmlFile(xmlFile);
		oc.setSchemaFile(schemaFile);
		ObjectFactory of = oc.getFactory();
		Assert.assertEquals(listOf("scannable"), of.getFindableNames());
	}
	
	protected void testGetObjectFactoryWithXmlAndSchemaShouldFail(String xmlFile, String schemaFile) {
		XmlObjectCreator oc = new XmlObjectCreator();
		oc.setXmlFile(xmlFile);
		oc.setSchemaFile(schemaFile);
		try {
			oc.getFactory();
			Assert.fail("This should not have succeeded");
		} catch (FactoryException e) {
			// ignore, this is expected
		}
	}

	private static String getXmlWithoutNamespace() throws IOException {
		return TestUtils.getResourceAsFile(XmlObjectCreatorTest.class, "ObjectFactory_without_namespace.xml").getAbsolutePath();
	}
	
	private static String getXmlWithNamespace() throws IOException {
		return TestUtils.getResourceAsFile(XmlObjectCreatorTest.class, "ObjectFactory_with_namespace.xml").getAbsolutePath();
	}
	
	private static String getInvalidXml() throws IOException {
		return TestUtils.getResourceAsFile(XmlObjectCreatorTest.class, "ObjectFactory_invalid.xml").getAbsolutePath();
	}
	
	private static String getSchemaWithoutNamespace() throws IOException {
		return TestUtils.getResourceAsFile(XmlObjectCreatorTest.class, "schema_without_namespace.xml").getAbsolutePath();
	}
	
	private static String getSchemaWithNamespace() throws IOException {
		return TestUtils.getResourceAsFile(XmlObjectCreatorTest.class, "schema_with_namespace.xml").getAbsolutePath();
	}
	
	/**
	 * Test using test supplied mapping and no schema
	 * @throws Exception 
	 * 
	 */
	@Test
	public void testGetObjectFactory() throws Exception {
		XmlObjectCreator objCreator = new XmlObjectCreator();
		objCreator.setMappingUrl(this.getClass().getResource("objectCreatorTestMapping1.xml"));
		final HashMap<String, String> map = new HashMap<String, String>();
		map.put("elementTag", "ObjectCreatorTest");
		objCreator.setUseDefaultSchema(false);
		objCreator.setMacroSupplier( 
				new MacroSupplier(){
					
					@Override
					public String get(String key) {
						return map.get(key);
							
					}});		
		String s= "<?xml version='1.0' encoding='UTF-8'?><ObjectFactory><name>testing</name><${elementTag}><name>testing1</name></${elementTag}></ObjectFactory>";
		ObjectFactory factory = objCreator.getFactory(new InputSource(new StringReader(s)));
		Assert.assertEquals(this.getClass(), factory.getFindable("testing1").getClass());

		s= "<?xml version='1.0' encoding='UTF-8'?><ObjectFactory><name>testing</name><${elementTag}><name>testing2</name></${elementTag}></ObjectFactory>";
		objCreator.setXmlFile(SaveToFile(s));
		factory = objCreator.getFactory();
		Assert.assertEquals(this.getClass(), factory.getFindable("testing2").getClass());
		
	}

	/**
	 * Test using default mapping and schema
	 * @throws Exception 
	 * 
	 */
	@Test
	public void testGetObjectFactory1() throws Exception {
		XmlObjectCreator objCreator = new XmlObjectCreator();
		objCreator.setUseDefaultSchema(true);
		final HashMap<String, String> map = new HashMap<String, String>();
		map.put("elementTag", "DummyAdc");
		objCreator.setMacroSupplier( 
				new MacroSupplier(){
					
					@Override
					public String get(String key) {
						return map.get(key);
							
					}});		
		String s= "<?xml version='1.0' encoding='UTF-8'?><ObjectFactory><name>testing</name><${elementTag}><name>testing1</name></${elementTag}></ObjectFactory>";
		ObjectFactory factory = objCreator.getFactory(new InputSource(new StringReader(s)));
		Assert.assertEquals(gda.device.adc.DummyAdc.class, factory.getFindable("testing1").getClass());

		s= "<?xml version='1.0' encoding='UTF-8'?><ObjectFactory><name>testing</name><${elementTag}><name>testing2</name></${elementTag}></ObjectFactory>";
		objCreator.setXmlFile(SaveToFile(s));
		factory = objCreator.getFactory();
		Assert.assertEquals(gda.device.adc.DummyAdc.class, factory.getFindable("testing2").getClass());
	}

	/**
	 * Test using default mapping and schema and property substitution
	 * @throws Exception 
	 * 
	 */
	@Test
	public void testGetObjectFactory2() throws Exception {
		XmlObjectCreator objCreator = new XmlObjectCreator();
		objCreator.setUseDefaultSchema(true);
		objCreator.setDoPropertySubstitution(true);
		String s= "<?xml version='1.0' encoding='UTF-8'?><ObjectFactory><name>testing</name><${elementTag}><name>testing1</name></${elementTag}></ObjectFactory>";
		LocalProperties.set("elementTag", "DummyAdc");
		ObjectFactory factory = objCreator.getFactory(new InputSource(new StringReader(s)));
		Assert.assertEquals(gda.device.adc.DummyAdc.class, factory.getFindable("testing1").getClass());

		s= "<?xml version='1.0' encoding='UTF-8'?><ObjectFactory><name>testing</name><${elementTag}><name>testing2</name></${elementTag}></ObjectFactory>";
		objCreator.setXmlFile(SaveToFile(s));
		factory = objCreator.getFactory();
		Assert.assertEquals(gda.device.adc.DummyAdc.class, factory.getFindable("testing2").getClass());

	}

	
	private String SaveToFile(String s) throws IOException{
		File tempFile = File.createTempFile("test", ".dat");
		FileWriter out = new FileWriter(tempFile);
		try {
			tempFile.deleteOnExit();
			out.write(s);
			out.flush();
		} finally {
			out.close();
		}
		return tempFile.getPath();
	}
	
	
	private String name;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	protected static List<String> listOf(String... strings) {
		return Arrays.asList(strings);
	}

}
