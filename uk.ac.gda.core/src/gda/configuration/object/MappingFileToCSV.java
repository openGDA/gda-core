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

package gda.configuration.object;

import java.io.FileOutputStream;
import java.io.IOException;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.mapping.xml.ClassMapping;
import org.exolab.castor.mapping.xml.MapTo;
import org.exolab.castor.mapping.xml.MappingRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads in a Castor mapping file and writes out CSV file (comma separated values) for importing into a spreadsheet - ie
 * Excel. For each class, writes out info, including class attributes. Also writes out column headers. Spreadsheet is to
 * be filled out with documentation/metadata by developers.
 */
public class MappingFileToCSV {
	private static final Logger logger = LoggerFactory.getLogger(MappingFileToCSV.class);

	/**
	 * @param fileName
	 *            the name of a Castor mapping file to load in.
	 * @param outFileName
	 *            the name of a CSV file to write out.
	 */
	public static void dumpMappingFileDataToCSV(String fileName, String outFileName) {
		try {
			String tmp = "";
			String comma = ",";
			String newline = "\r\n";

			// read in mapping file
			Mapping mapping = new Mapping();
			mapping.loadMapping(fileName);

			// create output file
			FileOutputStream f = new FileOutputStream(outFileName);

			// write column headers to file
			String headers = "Class,(XML) Name,Parent Class/Attribute,"
					+ "Attr Type (or class),units,range/enumeration,default value,"
					+ "optional/compulsory,deprecated,description/usage/context" + newline;
			f.write(headers.getBytes());

			MappingRoot root = mapping.getRoot();
			ClassMapping[] cm = root.getClassMapping();

			for (int i = 0; i < root.getClassMappingCount(); i++) {
				// Dump class name
				String name = cm[i].getName();
				f.write(name.getBytes());

				// Dump XML name bound to class
				MapTo mt = cm[i].getMapTo();
				if (mt != null) {
					String xmlName = mt.getXml();
					tmp = comma + xmlName;
					f.write(tmp.getBytes());
				}

				// Dump "extends" attribute (parent class)
				ClassMapping cmex = (ClassMapping) cm[i].getExtends();
				if (cmex != null) {
					tmp = comma + cmex.getName();
					f.write(tmp.getBytes());
				}

				f.write(comma.getBytes());

				f.write(newline.getBytes());
			}

		} catch (MappingException me) {
			logger.debug("dumpMappingFileDataToCSV mapping exception" + me.getMessage());
			logger.debug(me.getStackTrace().toString());
		} catch (IOException ie) {
			logger.debug("dumpMappingFileDataToCSV IO exception" + ie.getMessage());
			logger.debug(ie.getStackTrace().toString());
		}
	}
}
