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

package gda.hrpd.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;

/**
 * class that provides access to sample information in a Excel spreadsheet. The default file name of the spreadsheet is
 * {@code Sample.xls}, which can be configured using java property {@code gda.hrpd.data.sample.info}. This spreadsheet
 * contains two pages.
 */
public class ExcelReader {
	private static final Logger logger = LoggerFactory.getLogger(ExcelReader.class);

	private MultiValueMap mvm;

	private String dataDir;
	private String filename;
	private FileInputStream fin;
	private POIFSFileSystem fs;
	private HSSFWorkbook wb;
	private HSSFSheet sheet;

	private String sampleInfoFile;

	/**
	 * constructor that creates an multimap object for holding sample information loaded in from an Excel spreadsheet
	 * file specified by java property {@code gda.hrpd.data.sample.info} in the data directory specified by another java
	 * property {@code gda.data.scan.datawriter.datadir}. The default Excel spreadsheet file name is {@code Sample.xls}
	 *
	 * @throws InstantiationException
	 */
	public ExcelReader() throws InstantiationException {
		// check if the data directory has been defined
		dataDir = InterfaceProvider.getPathConstructor().createFromDefaultProperty();

		if (this.dataDir == null) {
			// this is compulsory - stop the scan
			String error = "java property gda.data.scan.datawriter.datadir not defined - cannot create a new data file";
			logger.error(error);
			throw new InstantiationException(error);
		}
		sampleInfoFile = LocalProperties.get("gda.hrpd.data.sample.info", "Sample.xls");
		// check if the sample information file is available
		filename = dataDir + File.separator + sampleInfoFile;
		try {
			fin = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			logger.warn("Cannot find sample information file {}.", filename);
			JythonServerFacade.getInstance().print("please specify the sample information file name.");
		}
		openSpreadsheet(null);
		mvm = new MultiValueMap();
		readData();
	}

	/**
	 * constructor that creates an multimap object for holding sample information loaded in from an Excel spreadsheet
	 * file specified. If full path name is not specified, it is expecting the sample information file resides in the
	 * data directory specified by java property {@code gda.data.scan.datawriter.datadir}.
	 *
	 * @param filename
	 * @throws InstantiationException
	 */
	public ExcelReader(String filename) throws InstantiationException {
		if (filename.startsWith("/")) {
			// full path is provided.
			this.filename = filename;
		} else {
			// check if the data directory has been defined
			dataDir = InterfaceProvider.getPathConstructor().createFromProperty("gda.data.scan.datawriter.datadir");

			if (this.dataDir == null) {
				// this java property is compulsory - stop the scan
				String error = "java property gda.data.scan.datawriter.datadir not defined - please load your sample information file there.";
				logger.error(error);
				throw new InstantiationException(error);
			}
			// construct full path to the sample information file
			this.filename = dataDir + File.separator + filename;
		}
		try {
			fin = new FileInputStream(this.filename);
		} catch (FileNotFoundException e) {
			logger.warn("Cannot find sample information file {}.", filename);
			JythonServerFacade.getInstance().print("please specify the sample information file name.");
		}
		openSpreadsheet(null);
		mvm = new MultiValueMap();
		readData();
	}

	/**
	 * load data from spreadsheet to the map, or initialise the multimap
	 */
	public void readData() {
		mvm.clear();

		int i = 0;
		for (Iterator<Row> rit = sheet.rowIterator(); rit.hasNext();) {
			Row row = rit.next();
			for (Iterator<Cell> cit = row.cellIterator(); cit.hasNext();) {
				Cell cell = cit.next();
				if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
					mvm.put(i, cell.getRichStringCellValue().toString());
				} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					mvm.put(i, String.valueOf(cell.getNumericCellValue()));
				} else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
					mvm.put(i, "");
				}
			}
			i++;
		}
		logger.debug("Read row {}", i);
	}

	/**
	 * open or reopen the specified spreadsheet file for work sheet 0.
	 *
	 * @param filename
	 */
	public void openSpreadsheet(String filename) {
		if (filename == null) {
			filename = this.filename;
		}
		if (!filename.equalsIgnoreCase(this.filename)) {
			close();
			try {
				fin = new FileInputStream(filename);
			} catch (FileNotFoundException e) {
				logger.warn("Cannot find sample information file {}.", filename);
				JythonServerFacade.getInstance().print("please specify the URL for the sample information file.");
			}
		}
		if (fin != null) {

			try {
				fs = new POIFSFileSystem(fin);
				wb = new HSSFWorkbook(fs);
				sheet = wb.getSheetAt(0);
			} catch (IOException e) {
				logger.error("Error opening spreadsheet", e);
			}
		}
	}

	/**
	 * close file input stream to the spreadsheet
	 */
	public void close() {
		try {
			fin.close();
		} catch (IOException e) {
			logger.error("Error closing spreadsheet", e);
		}
	}

	/**
	 * @return sample info file
	 */
	public String getSampleInfoFile() {
		return sampleInfoFile;
	}

	/**
	 * @param sampleInfoFile
	 */
	public void setSampleInfoFile(String sampleInfoFile) {
		this.sampleInfoFile = sampleInfoFile;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("testing Excel file reading....");
		ExcelReader er;
		try {
			er = new ExcelReader();
			MultiValueMap sampleData = er.getMvm();
			for (int i = 0; i < sampleData.size(); i++) {
				Collection<?> list = sampleData.getCollection(i);
				for (Object o : list) {
					System.out.print(o + "\t");
				}
				System.out.println();
			}
			// for (Object o : sampleData.keySet()) {
			// System.out.println(sampleData.size(o));
			// Collection sample = sampleData.getCollection(o);
			// System.out.println(sample.size());
			// for (Iterator it=sample.iterator(); it.hasNext();) {
			// Object element = it.next();
			// System.out.print(element.toString() + "\t");
			// }
			// System.out.println();
			// }
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @return mvm
	 */
	public MultiValueMap getMvm() {
		return mvm;
	}

	/**
	 * @param mvm
	 */
	public void setMvm(MultiValueMap mvm) {
		this.mvm = mvm;
	}
}
