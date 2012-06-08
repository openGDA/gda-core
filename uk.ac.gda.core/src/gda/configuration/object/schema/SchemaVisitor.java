/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.configuration.object.schema;

import gda.configuration.object.ObjectAttributeMetaData;

import java.util.Enumeration;
import java.util.Stack;

import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Particle;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.SimpleType;
import org.exolab.castor.xml.schema.XMLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SchemaVisitor {
	private static final Logger logger = LoggerFactory.getLogger(SchemaVisitor.class);

	private boolean stillVisiting = false;

	// search-related attributes
	private String[] searchString = null;

	private int searchCount = 0;

	private boolean searchFound = false;

	private ObjectAttributeMetaData metaData = null;

	// stack of element names, sub-elements can reference their parents
	// ie so can determine object attributes and their parent objects
	// (names)
	private Stack<String> elementNameStack = new Stack<String>();

	Schema schema = null;

	SchemaVisitor(Schema theSchema) {
		schema = theSchema;
	}

	// --------------------------------------------------------------------------
	// generic code for visiting schema
	// - final template methods with callbacks - subclasses override
	// callbacks
	// for specific behaviour during visits

	private final void handleParticle(Particle p) {
		if (p instanceof Group) {
			Group g = (Group) p;

			logger.debug("group start " + g.getName());

			int pc1 = g.getParticleCount();

			for (int i = 0; i < pc1; i++) {
				Particle p1 = g.getParticle(i);

				handleParticle(p1);
			}
			logger.debug("group end   " + g.getName());
		} else {
			if (p instanceof ElementDecl) {
				ElementDecl edl = (ElementDecl) p;

				handleElementDeclaration(edl);
			} else {
				logger.debug("handleParticle - unhandled particle type!!");
			}
		}
	}

	private final void handleSimpleType(SimpleType st, ElementDecl ed) {
		logger.debug("simpletype " + st.getName() + " " + st.getClass().getName());

		// simple type callback
		simpleTypeOperation(st, ed);
	}

	private final void handleComplexType(ComplexType ctype, ElementDecl ed) {
		logger.debug("complextype start " + ctype.getName());

		// Enumeration ad = ctype.getAttributeDecls();
		// Enumeration lad = ctype.getLocalAttributeDecls();

		// complex type callback
		complexTypeOperation(ed);

		int pc = ctype.getParticleCount();

		for (int z = 0; z < pc; z++) {
			// only continue searching particles while still not found
			// search item
			if (stillVisiting == true) {
				Particle p = ctype.getParticle(z);

				// recursively handle particle and any children
				handleParticle(p);
			}
		}

		// recursive search parent BaseType heirarchy
		// - if current search item not found
		if (stillVisiting == true) {
			XMLType baseType = ctype.getBaseType();
			handleType(baseType, ed);
		}

		logger.debug("complextype end   " + ctype.getName());
	}

	protected final void handleType(XMLType t, ElementDecl ed) {
		if (t != null) {
			if (t instanceof ComplexType) {
				handleComplexType((ComplexType) t, ed);
			} else {
				if (t instanceof SimpleType) {
					handleSimpleType((SimpleType) t, ed);
				} else {
					logger.debug("handleType - unhandled XML type!!");
				}
			}
		}
	}

	protected final void handleElementDeclaration(ElementDecl ed) {
		XMLType t = ed.getType();

		logger.debug("element " + ed.getName() + " type " /*
															 * + t.getName() + ", " + t.getStructureType() + ", " +
															 * t.toString() + ", "
															 */
				+ t.getClass().getName());

		if (t instanceof ComplexType) {
			// callback for entering this element
			// - returns true to proceed, or false to abort processing this
			// element
			if (elementComplexTypeOperationStart(ed) == false) {
				return;
			}

			// push element name so children can peek it if necessary
			String pushedName = ed.getName();
			elementNameStack.push(pushedName);
		}

		handleType(t, ed);

		if (t instanceof ComplexType) {
			// callback for cleanup for exiting this element
			elementComplexTypeOperationEnd();

			// leaving this elements context, so remove from stack
			/* String poppedName = */elementNameStack.pop();
		}

	}

	// ---------------------------------------------------------------------------
	// "callbacks" - (specific to the searching application)
	// subclasses should override for specific operations during visits

	// "callback" for when SimpleType is visited
	protected void simpleTypeOperation(SimpleType st, ElementDecl ed) {
		String searchName = searchString[searchCount];

		logger.debug(searchName);

		// if match found here, we've found our leaf element
		// but may need to traverse further for nested complex types
		// (eg GenericOE->DOF or DummyMemory->Dimension)
		if (searchName.equalsIgnoreCase(ed.getName())) {
			stillVisiting = false;
			searchFound = true;

			metaData = new ObjectAttributeMetaData(ed.getName(), st.getName(), ed.getDefaultValue(), ed.getMinOccurs(),
					ed.getMaxOccurs());
		}
	}

	// "callback" for when ComplexType is visited
	protected void complexTypeOperation(ElementDecl ed) {
		// GUI may be searching for nested complex type
		// may have reached end of search path - on a complex type
		// => dont continue down to leaf simple type
		if (searchCount == searchString.length) {
			// see if current element matches end of search path
			String searchName = searchString[searchCount - 1];

			logger.debug(searchName);

			if (searchName.equalsIgnoreCase(ed.getName())) {
				stillVisiting = false;
				searchFound = true;

				// we cant return a simple type name eg "string"
				metaData = new ObjectAttributeMetaData(ed.getName(), "CType", ed.getDefaultValue(), ed.getMinOccurs(),
						ed.getMaxOccurs());
				return;
			}
		}
	}

	// return true to continue recursive processing of element
	boolean elementComplexTypeOperationStart(ElementDecl ed) {
		// only enter next level if match found in search string
		if (ed.getName().equalsIgnoreCase(searchString[searchCount]) == false) {
			return false;
		}

		searchCount++;

		return true;
	}

	void elementComplexTypeOperationEnd() {
		searchCount--;
	}

	// ---------------------------------------------------------------------------

	// try to fetch type info from root *complextypes*, if named *element*
	// not
	// found
	// N.B. if complex type, is not proper *element* metadata, since min/max
	// &
	// default not valid!
	// BUT sub-elements of a root complextype is an *element*, so its
	// metadata is
	// valid
	private void findComplexTypeMetaData(String[] searchString) {
		// split searchString into stack
		// String [] searchString = elementName.split("\\.");

		Enumeration<?> e = schema.getComplexTypes();
		while (e.hasMoreElements()) {
			ComplexType ct = (ComplexType) e.nextElement();

			if (ct.getName().equalsIgnoreCase(searchString[1/* 0 */])) {
				if (searchString.length > 2/* 1 */) {
					int pc = ct.getParticleCount();

					for (int z = 0; z < pc; z++) {
						Particle p = ct.getParticle(z);

						if (p instanceof Group) {
							Group g = (Group) p;

							logger.debug("group start " + g.getName());

							int pc1 = g.getParticleCount();

							for (int i = 0; i < pc1; i++) {
								Particle p1 = g.getParticle(i);

								// handleParticle(p1);
								if (p1 instanceof ElementDecl) {
									ElementDecl edl = (ElementDecl) p1;

									String typeName = "";

									XMLType t1 = edl.getType();
									if (t1 instanceof SimpleType) {
										typeName = t1.getName();
									} else {
										typeName = "CType";
									}

									if (edl.getName().equalsIgnoreCase(searchString[2/* 1 */])) {
										metaData = new ObjectAttributeMetaData(edl.getName(), typeName, edl
												.getDefaultValue(), edl.getMinOccurs(), edl.getMaxOccurs());
									}
								}

							}
							logger.debug("group end   " + g.getName());
						}
						/*
						 * else { if(p instanceof ElementDecl) { ElementDecl edl = (ElementDecl) p;
						 * if(edl.getName().equalsIgnoreCase(searchString[1])) { metaData = new
						 * ObjectAttributeMetaData(edl.getName(), ct.getName(), edl.getDefaultValue(),
						 * edl.getMinOccurs(), edl.getMaxOccurs()); } } }
						 */
					}
				} else {
					// we cant return a simple type name eg "string"
					// N.B. default, min & max not valid - since NOT AN
					// ELEMENT!
					metaData = new ObjectAttributeMetaData(ct.getName(), "CType", "0", 1, 1);
				}
				break;
			}

		}
	}

	// ......................................................................

	// Search string should be dot-named path split into array of strings
	// eg ["ObjectFactory","DummyMotor"] should find the element ok & return
	// metadata with "CType"
	// eg ["ObjectFactory","DummyMotor","local"] (an attrib of DM)
	// which should find DummyMotor element, and get type info from going up
	// type
	// tree
	// to DummyMotor type, MotorBase type then DeviceBase type. returning
	// metadata for the sub-element, with correct type filled out
	/**
	 * @param searchString
	 * @param ed
	 * @param metaData
	 */
	public void findElementDeclMetaData(String[] searchString, ElementDecl ed, ObjectAttributeMetaData metaData) {
		stillVisiting = true;

		this.searchString = searchString;
		searchCount = 0;
		searchFound = false;
		this.metaData = null;

		handleElementDeclaration(ed);

		if (searchFound == false || metaData == null) {
			findComplexTypeMetaData(searchString);
		}

		if (searchFound == true) {
			metaData = this.metaData;
		}
	}
}
