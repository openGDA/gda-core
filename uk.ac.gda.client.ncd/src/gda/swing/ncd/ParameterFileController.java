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

package gda.swing.ncd;

import gda.rcp.ncd.ExptDataModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to implement ParameterFileController
 */
public class ParameterFileController {
	private static final Logger logger = LoggerFactory.getLogger(ParameterFileController.class);

	private TimeFrameConfigure timeFrameConfigure = null;

	private static ParameterFileController instance = new ParameterFileController();

	/**
	 * Returns the singleton instance of this class.
	 * 
	 * @return ParameterFileController
	 */
	public static ParameterFileController getInstance() {
		return instance;
	}

	/**
	 * Constructor to implement ParameterFileController
	 */
	private ParameterFileController() {
	}

	/**
	 * @param timeFrameConfigure
	 */
	public void add(TimeFrameConfigure timeFrameConfigure) {
		this.timeFrameConfigure = timeFrameConfigure;
	}

	/**
	 * @param file
	 */
	public void load(File file) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			ExptDataModel.getInstance().setFileName(file.getName());
			reader.mark(5000);

			if (timeFrameConfigure != null) {
				reader.reset();
				timeFrameConfigure.load(reader);
			}

			reader.close();
		} catch (IOException e) {
			logger.error("ParameterFileController: load: " + e);
		}
	}

	/**
	 * @param file
	 */
	public void save(File file) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			Date date = new Date();
			writer.write("<?xml version='1.0'?>");
			writer.newLine();
			writer.write("<Experiment>");
			writer.newLine();
			writer.write("<!--Created: " + date.toString() + "-->");
			writer.newLine();
			if (timeFrameConfigure != null) {
				timeFrameConfigure.save(writer);
			}
			writer.write("</Experiment>");
			writer.newLine();
			writer.close();
			ExptDataModel.getInstance().setFileName(file.getName());
		} catch (IOException e) {
			logger.error("ParameterFileController: save: " + e);
		}
	}
}
