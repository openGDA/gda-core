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

package gda.epics.util;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.epics.CAClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class which allows to record EPICS PV value in a file
 */
//deprecated as I do not think it is used.
@Deprecated
public final class EpicsPVs {
	private static final Logger logger = LoggerFactory.getLogger(EpicsPVs.class);

	// file extension to use
	private String fileExtension = "env";

	// where all file will be placed
	private String dataDir = null;

	private NumTracker runs = null;

	private Vector<String> pvVector = new Vector<String>();

	CAClient cac = new CAClient();

	/**
	 * @throws InstantiationException
	 */
	public EpicsPVs() throws InstantiationException {
		dataDir = PathConstructor.createFromDefaultProperty();

		if (dataDir == null) {// this is compulsory - stop
			throw new InstantiationException("datadir not defined - cannot create a new data file");
		}
		try {
			runs = new NumTracker("tmp");
		} catch (IOException e) {
			logger.error("ERROR: Could not instantiate NumTracker in IncrementalFile().");
		}
	}

	/**
	 * @param name
	 * @param pv
	 * @return int
	 */
	public int addPV(String name, String pv) {
		pvVector.addElement(name);
		pvVector.addElement(pv);
		return pvVector.size();
	}

	/**
	 * 
	 */
	public void clearPVs() {
		pvVector.clear();
	}

	/**
	 * save the PVs in a file before next scan (using the next scan number)
	 * 
	 * @return String
	 */
	public String preScanSave() {
		long nextNum = 1L + runs.getCurrentFileNumber();
		String fileName = nextNum + "." + this.fileExtension;
		return savePVs(fileName, false); // create new file, overwrite
		// old one if
		// exist
	}

	/**
	 * save the PVs in a file after current scan (using the current scan number)
	 * 
	 * @return String
	 */
	public String afterScanSave() {
		long currentNum = runs.getCurrentFileNumber();
		String fileName = currentNum + "." + this.fileExtension;
		return savePVs(fileName, true); // append old one if exist, otherwise
		// create new one
	}

	/**
	 * @param fileName
	 * @param append
	 * @return String
	 */
	public String savePVs(String fileName, boolean append) {

		String fullFileName = dataDir + File.separator + fileName;
		String pv = null;
		String name = null;
		String value = null;
		String format = "%15s %25s %50s\n";
		StringBuffer info = new StringBuffer(500);

		Date date = new Date();

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		// DateFormat df2 = DateFormat.getTimeInstance(DateFormat.SHORT);
		info.append(df.format(date));
		info.append("\n  Variable Name                     Value                                       PV reference\n");

		int i = 0;
		while (i < pvVector.size()) {
			name = pvVector.get(i++);
			pv = pvVector.get(i++);

			try {
				value = cac.caget(pv);
			} catch (Exception e) {
				value = "null";
				logger.error("Check EPICS PV name: " + pv,e);
			}

			info.append(String.format(format, name, value, pv));
		}

		info.append("\n\n");
		logger.debug(info.toString());
		File outputFile = new File(fullFileName);
		try {
			FileWriter fw = new FileWriter(outputFile, append);
			fw.write(info.toString());
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fullFileName = null;
		}
		return fullFileName;
	}

}
