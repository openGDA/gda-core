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

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class that provides access to sample information in a Excel spreadsheet. The default file name of the spreadsheet is
 * {@code Sample.xls}, which can be configured using java property {@code gda.hrpd.data.sample.info}
 */
public class SampleExperimentSummary {
	private static final Logger logger = LoggerFactory.getLogger(SampleExperimentSummary.class);
	private String carouselNo;
	private String runNumber;
	private String date;
	private String time;
	private String beamline;
	private String project;
	private String experiment;
	private String accumulationTime;

	private String dataDir;
	private String filename;
	private FileInputStream fin;
	private POIFSFileSystem fs;
	private HSSFWorkbook wb;
	private HSSFSheet sheet;

	private int rowOffset = 0;

	/**
	 * @throws InstantiationException
	 */
	public SampleExperimentSummary() throws InstantiationException {
		// check if the data directory has been defined
		dataDir = PathConstructor.createFromDefaultProperty();

		if (this.dataDir == null) {
			// this java property is compulsory - stop the scan
			String error = "java property gda.data.scan.datawriter.datadir not defined - cannot create a new data file";
			logger.error(error);
			throw new InstantiationException(error);
		}
		String sampleInfoFile = LocalProperties.get("gda.hrpd.data.sample.info", "Sample.xls");
		// check if the sample information file is available
		filename = dataDir + File.separator + sampleInfoFile;

		try {
			fin = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			logger.warn("Cannot find sample information file {}.", filename);
		}

		if (fin != null) {

			try {
				fs = new POIFSFileSystem(fin);
				wb = new HSSFWorkbook(fs);
				sheet = wb.getSheetAt(1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * gets sample information for specified sample from spreadsheet 0
	 * 
	 * @param sampleNo
	 */
	public void loadExperimentInfo(int sampleNo) {

		HSSFRow row = sheet.getRow(sampleNo + rowOffset);
		carouselNo = row.getCell(0).getRichStringCellValue().getString();
		runNumber = row.getCell(1).getRichStringCellValue().getString();
		date = row.getCell(2).getRichStringCellValue().getString();
		time = row.getCell(3).getRichStringCellValue().getString();
		beamline = row.getCell(4).getRichStringCellValue().getString();
		project = row.getCell(5).getRichStringCellValue().getString();
		experiment = row.getCell(6).getRichStringCellValue().getString();
		accumulationTime = row.getCell(7).getRichStringCellValue().getString();
	}

	/**
	 * @param sampleNo
	 */
	public void saveExperimentInfo(int sampleNo) {
		HSSFRow row = sheet.getRow(sampleNo + rowOffset);
		HSSFCell cell = row.getCell((short) 1);
		if (cell == null)
			cell = row.createCell((short) 1);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(runNumber));
		cell = row.getCell((short) 2);
		if (cell == null)
			cell = row.createCell((short) 2);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(date));
		cell = row.getCell((short) 3);
		if (cell == null)
			cell = row.createCell((short) 3);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(time));
		cell = row.getCell((short) 4);
		if (cell == null)
			cell = row.createCell((short) 4);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(beamline));
		cell = row.getCell((short) 5);
		if (cell == null)
			cell = row.createCell((short) 5);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(project));
		cell = row.getCell((short) 6);
		if (cell == null)
			cell = row.createCell((short) 6);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(experiment));
		cell = row.getCell((short) 7);
		if (cell == null)
			cell = row.createCell((short) 7);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(accumulationTime));
	}

	/**
	 * 
	 */
	public void close() {
		try {
			fin.close();
		} catch (IOException e) {
			logger.error("Cannot close the file input stream", e);
			e.printStackTrace();
		}
	}

	/**
	 * @return carouselNo
	 */
	public String getCarouselNo() {
		return carouselNo;
	}

	/**
	 * @param carouselNo
	 */
	public void setCarouselNo(String carouselNo) {
		this.carouselNo = carouselNo;
	}

	/**
	 * @return runNumber
	 */
	public String getRunNumber() {
		return runNumber;
	}

	/**
	 * @param runNumber
	 */
	public void setRunNumber(String runNumber) {
		this.runNumber = runNumber;
	}

	/**
	 * @return date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @return time
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param time
	 */
	public void setTime(String time) {
		this.time = time;
	}

	/**
	 * @return beamline
	 */
	public String getBeamline() {
		return beamline;
	}

	/**
	 * @param beamline
	 */
	public void setBeamline(String beamline) {
		this.beamline = beamline;
	}

	/**
	 * @return experiment
	 */
	public String getExperiment() {
		return experiment;
	}

	/**
	 * @param experiment
	 */
	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}

	/**
	 * @return accumulationTime
	 */
	public String getAccumulationTime() {
		return accumulationTime;
	}

	/**
	 * @param accumulationTime
	 */
	public void setAccumulationTime(String accumulationTime) {
		this.accumulationTime = accumulationTime;
	}

	/**
	 * @return rowOffset
	 */
	public int getRowOffset() {
		return rowOffset;
	}

	/**
	 * @param rowOffset
	 */
	public void setRowOffset(int rowOffset) {
		this.rowOffset = rowOffset;
	}

}
