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

package gda.device.motor;

import gda.configuration.properties.LocalProperties;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read a motor position file
 */
public class ReadMotorPositionFile implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(ReadMotorPositionFile.class);

	private ObjectInputStream in = null;

	private String separator = System.getProperty("file.separator");

	private String filePath = ".";

	private String path = null;

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new ReadMotorPositionFile(args[0]);
	}

	/**
	 * @param fileName
	 */
	public ReadMotorPositionFile(String fileName) {
		try {
			if ((filePath = LocalProperties.get("gda.motordir")) == null)
				filePath = ".";

			path = filePath + separator + fileName;
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)));

			logger.info("Motor position is " + in.readDouble());
			in.close();
		} catch (FileNotFoundException ex) {
			logger.error("Motor Position File " + path + " not found\n");
		} catch (EOFException ex) {
			logger.error("Motor Position File " + path + " is corrupt");
		} catch (IOException e) {
			logger.debug(e.getStackTrace().toString());
		}
	}
}
