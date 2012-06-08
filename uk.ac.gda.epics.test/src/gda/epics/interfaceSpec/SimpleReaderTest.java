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

package gda.epics.interfaceSpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Vector;

import org.junit.Test;

/**
 * SimpleReaderTest Class
 */
public class SimpleReaderTest {

	final static String testFileFolder = "src/gda/epics/interfaceSpec";
	final static String goodXML = testFileFolder + "/gda-interface.xml";
	final static String goodSchema = testFileFolder + "/genericBeamlineSchema.xsd";

	private void testGetValidDevice(String type) {

		_SimpleFields fields = new _SimpleFields();
		{
			_SimpleAttributes attributes = new _SimpleAttributes();
			attributes.add(new _SimpleAttribute("desc", "F1"));
			attributes.add(new _SimpleAttribute("pv", "Simple-FILT-01:F1TRIGGER"));
			attributes.add(new _SimpleAttribute("ro", "false"));
			attributes.add(new _SimpleAttribute("type", "binary"));
			fields.add(new _SimpleField("F1", attributes));
		}
		{
			_SimpleAttributes attributes = new _SimpleAttributes();
			attributes.add(new _SimpleAttribute("desc", "F2"));
			attributes.add(new _SimpleAttribute("pv", "Simple-FILT-01:F2TRIGGER"));
			attributes.add(new _SimpleAttribute("ro", "false"));
			attributes.add(new _SimpleAttribute("type", "binary"));
			fields.add(new _SimpleField("F2", attributes));
		}
		Vector<Attribute> attributes = new Vector<Attribute>();
		
		Device devExpected = new _SimpleDevice("xiaArray.1", "xiaArray", attributes, fields );
		Device dev = null;
		try {
			SimpleReader simpleReader = new SimpleReader(testFileFolder + "/cutdown_gda-interface.xml", null);
			dev = simpleReader.getDevice(type, "xiaArray.1");
			assertNotNull(dev);
		} catch (Exception e) {
			assertFalse(e.getMessage(), true);
		}
		assertEquals(utils.dumpDevice(devExpected), utils.dumpDevice(dev));
	}

	/**
	 */
	@Test
	public void testGetValidDeviceUsingFullNameAndType() {
		testGetValidDevice("xiaArray");
	}

	/**
	 * @throws InterfaceException 
	 */
	@Test
	public void testDescValidDeviceUsingFullNameAndType() throws InterfaceException {
		SimpleReader simpleReader = new SimpleReader(testFileFolder + "/cutdown_gda-interface.xml", null);
		Device dev = simpleReader.getDevice("xiaArray", "xiaArray.1");
		assertEquals("xiaArrayDescription",dev.getAttributeValue(SimpleReader.DESC_ATTRIBUTE_NAME));
		assertEquals("xiaArrayDescription",dev.getDescription());
	}

	/**
	 * 
	 */
	@Test
	public void testGetValidDeviceUsingFullNameOnly1() {
		testGetValidDevice(null);
	}

	/**
	 * 
	 */
	@Test
	public void testGetValidDeviceUsingFullNameOnly2() {
		testGetValidDevice("");
	}

	/**
	 * @throws InterfaceException 
	 * 
	 */
	@Test
	public void testGetNonExistentDevice() throws InterfaceException {
		String name = "doesNotExist";
		String type = "xiaArray";

		SimpleReader simpleReader = new SimpleReader(goodXML, null);
		assertNull("A non-existent device was not reported as such", simpleReader.getDevice(type, name));
	}

	/**
	 * @throws InterfaceException 
	 * 
	 */
	@Test
	public void testGetNonExistentDeviceType() throws InterfaceException {
		String name = "xiaArray.1";
		String type = "doesNotExist";

		SimpleReader simpleReader = new SimpleReader(goodXML, null);
		assertNull("A non-existent device was not reported as such", simpleReader.getDevice(type, name));
	}

	/**
	 * @throws InterfaceException 
	 * 
	 */
	@Test
	public void testGetDeviceWithTypeUnSpecified() throws InterfaceException {
		String name = "doesNotExist";
		SimpleReader simpleReader = new SimpleReader(goodXML, null);
		assertNull("A non-existent device was not reported as such", simpleReader.getDevice("", name));
		assertNull("A non-existent device was not reported as such", simpleReader.getDevice(null, name));
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	@Test
	public void testValidationOK() {
		try {
			new SimpleReader(goodXML, goodSchema);
			assertTrue(true);
		} catch (Exception e) {
			assertFalse("A valid xml document was not reported as invalid", true);
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	@Test
	public void testValidationBad() {
		try {
			new SimpleReader(testFileFolder + "/bad_gda-interface.xml", goodSchema);
			assertFalse("An invalid xml document was not reported as invalid", true);
		} catch (Exception e) {
			assertTrue(true);
		}

	}
}

class _SimpleAttribute implements Attribute {
	private final String name, value;

	_SimpleAttribute(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

}

class _SimpleAttributes extends Vector<_SimpleAttribute> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6722376235262372958L;
}

class _SimpleField implements Field {
	private final String name;
	private final _SimpleAttributes attributes;
	private final Vector<String> attributeNames;

	_SimpleField(String name, _SimpleAttributes attributes) {
		this.name = name;
		this.attributes = attributes;
		attributeNames = new Vector<String>();
		for (Attribute attribute : attributes) {
			attributeNames.add(attribute.getName());
		}
	}

	@Override
	public Attribute getAttribute(String attributeName) {
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals(attributeName))
				return attribute;
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPV() {
		return getAttribute(Xml.pv_name).getValue();

	}

	@Override
	public boolean isReadOnly() {
		return getAttribute(Xml.ro_name).getValue().equals(Xml.isReadonly_value);
	}

	@Override
	public Iterator<String> getAttributeNames() {
		return attributeNames.iterator();
	}

	@Override
	public String getType() {
		return getAttribute(Xml.type_name).getValue();
	}
}




class _SimpleFields extends Vector<_SimpleField> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3170191421245247251L;

}

class _SimpleDevice implements Device {
	private final String name;
	private final String type;
	private final _SimpleFields fields;
	private final Vector<String> fieldNames;
	private final Vector<Attribute> attributes;	

	_SimpleDevice(String name, String type,  Vector<Attribute> attributes , _SimpleFields fields) {
		this.name = name;
		this.type = type;
		this.fields = fields;
		fieldNames = new Vector<String>();
		for (Field field : fields) {
			fieldNames.add(field.getName());
		}
		this.attributes = new Vector<Attribute>();
		for(Attribute attribute : attributes){
			this.attributes.add(attribute);
		}		
	}

	@Override
	public Field getField(String fieldName) {
		for (Field field : fields) {
			if (field.getName().equals(fieldName))
				return field;
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public Iterator<String> getFieldNames() {
		return fieldNames.iterator();
	}


	@Override
	public String getAttributeValue(String attributeName) {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}
}