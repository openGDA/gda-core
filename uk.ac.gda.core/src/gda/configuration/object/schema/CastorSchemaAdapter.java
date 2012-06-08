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

import java.io.IOException;
import java.util.Vector;

import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Particle;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.XMLType;
import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// README - FUTURE WORK - refactor CastorSchemaAdapter - too much duplication!
/**
 * CastorSchemaAdapter Class
 */
public class CastorSchemaAdapter {
	private static final Logger logger = LoggerFactory.getLogger(CastorSchemaAdapter.class);

	// (Castor) object containing gda schema for instance file
	private Schema gdaSchema = null;

	/**
	 * @param schemaFileName
	 */
	public void LoadSchema(String schemaFileName) {
		try {
			SchemaReader reader = null;

			// load official xml schema
			// README - FUTURE WORK - DOESNT LOAD - "derivationControl" not
			// defined
			// in XML Schema spec
			// maybe various schemas I downloaded arent compatible?!
			// or maybe versions of castor & xerces used dont support the
			// full
			// spec?!
			// reader = new SchemaReader(
			// "C:\\Documents and
			// Settings\\msd43.DL\\Desktop\\XMLSchema.xsd");
			// reader.setValidation(true);
			// Schema xmlSchema = reader.read();

			// load gda schema
			reader = new SchemaReader(schemaFileName);
			reader.setValidation(false);
			gdaSchema = reader.read();
		} catch (IOException ie) {
			logger.debug("LoadSchema IO exception" + ie.getMessage());
			logger.debug(ie.getStackTrace().toString());
		}
	}

	private void getElementDeclAndMetaData(String elementName, ObjectAttributeMetaData metaData) {
		// README - FUTURE WORK - REMOVE OBJECTFACTORY FETCH - REPLACE WITH
		// PROPER
		// ITERATE THRU ELEMENTDECLS ENUM
		// Enumeration e = gdaSchema.getElementDecls();
		// Enumeration ct = gdaSchema.getComplexTypes();
		ElementDecl ed = gdaSchema.getElementDecl("ObjectFactory");

		// split searchString into stack
		String[] s = elementName.split("\\.");

		String[] searchString = new String[s.length + 1];
		searchString[0] = "ObjectFactory";
		System.arraycopy(s, 0, searchString, 1, s.length);

		// README - FUTURE WORK - display whether each field is valid

		// start recursive traversal of schema
		// to locate element and its metadata

		SchemaVisitor visitor = new SchemaVisitor(gdaSchema);
		visitor.findElementDeclMetaData(searchString, ed, metaData);
	}

	/**
	 * @param elementName
	 * @return ObjectAttributeMetaData
	 */
	public ObjectAttributeMetaData getElementMetaData(String elementName) {
		ObjectAttributeMetaData metaData = null;

		getElementDeclAndMetaData(elementName, metaData);

		return metaData;
	}

	// write all root-level object types into array
	// - eg for selection by user
	/**
	 * @return String[]
	 */
	public String[] getRootElementNameList() {
		// README - FUTURE WORK - REMOVE OBJECTFACTORY FETCH - REPLACE WITH
		// PROPER
		// ITERATE THRU ELEMENTDECLS ENUM
		// Enumeration e = gdaSchema.getElementDecls();
		ElementDecl ed = gdaSchema.getElementDecl("ObjectFactory");

		Vector<ElementDecl> v = new Vector<ElementDecl>();

		XMLType t = ed.getType();
		ComplexType ctype = (ComplexType) t;

		int pc = ctype.getParticleCount();

		for (int z = 0; z < pc; z++) {
			Particle p = ctype.getParticle(z);

			if (p instanceof Group) {
				Group g = (Group) p;

				logger.debug("group start " + g.getName());

				int pc1 = g.getParticleCount();

				for (int i = 0; i < pc1; i++) {
					Particle p1 = g.getParticle(i);

					// handleParticle(p1);
					if (p1 instanceof ElementDecl) {
						ElementDecl edl = (ElementDecl) p1;
						v.add(edl);
					}

				}
				logger.debug("group end   " + g.getName());
			}
			/*
			 * else { if(p instanceof ElementDecl) { ElementDecl edl = (ElementDecl) p; v.add(edl); } }
			 */
		}

		// now dump type strings into an array to pass back to caller
		String[] s = new String[v.size()];
		for (int i = 0; i < v.size(); i++) {
			s[i] = v.get(i).getName();
		}

		return s;
	}

	// fetch list of names of subelements for this element
	// README - FUTURE WORK - getSubElementMetaDataList needs to recursively
	// fetch base type attr metadata
	/**
	 * @return ObjectAttributeMetaData[]
	 */
	public ObjectAttributeMetaData[] getSubElementMetaDataList() {
		Vector<ObjectAttributeMetaData> v = new Vector<ObjectAttributeMetaData>();
		/*
		 * ObjectAttributeMetaData metaData = null; ElementDecl elementFound = null;
		 * getElementDeclAndMetaData(elementName, metaData, elementFound); XMLType t = elementFound.getType();
		 * ComplexType ctype = (ComplexType) t; int pc = ctype.getParticleCount(); for (int z = 0; z < pc; z++) {
		 * Particle p = ctype.getParticle(z); if (p instanceof Group) { Group g = (Group) p; Message.info("group start " +
		 * g.getName()); int pc1 = g.getParticleCount(); for (int i = 0; i < pc1; i++) { Particle p1 = g.getParticle(i); //
		 * handleParticle(p1); if (p1 instanceof ElementDecl) { ElementDecl edl = (ElementDecl) p1; String typeName =
		 * ""; XMLType t1 = edl.getType(); if (t1 instanceof SimpleType) { typeName = t1.getName(); } else { typeName =
		 * "CType"; } metaData = new ObjectAttributeMetaData(edl.getName(), typeName, edl.getDefaultValue(),
		 * edl.getMinOccurs(), edl.getMaxOccurs()); v.add(metaData); } } Message.info("group end " + g.getName()); } // //
		 * else { if(p instanceof ElementDecl) { ElementDecl edl = // (ElementDecl) p; v.add(edl); } } // }
		 */
		// copy metadata vector into an array to pass back
		ObjectAttributeMetaData[] mdList = new ObjectAttributeMetaData[v.size()];
		for (int i = 0; i < v.size(); i++) {
			mdList[i] = v.get(i);
		}
		return mdList;
	}
}
