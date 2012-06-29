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

package gda.hrpd.sample;

import gda.hrpd.data.ExcelWorkbook;
import gda.jython.JythonServerFacade;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SpreadsheetTest {

	String filename;

	int sampleNo;

	int rowOffset = 1;

	boolean opened;

	private ExcelWorkbook excel;

	private HSSFSheet sampleInfo;

	private Logger logger = LoggerFactory.getLogger(SpreadsheetTest.class);

	/**
	 * @param sampleNo
	 */
	public void loadSampleInfo(int sampleNo) {
		if (!opened) {
			logger.error("Sample Information File is not opened yet.");
			throw new IllegalStateException("Sample Information file " + this.filename + " must be opened first.");
		}
		logger.info("Load sample information from {} for sample number {}", getSampleInfoFile(), sampleNo);
		HSSFRow row = sampleInfo.getRow(sampleNo + rowOffset);
		if (row == null) {
			logger.error("specified sample Number {} does not exist.", sampleNo);
			JythonServerFacade.getInstance().print("No sample information is provided for sample number " + sampleNo);
		} else {
			String carouselNo = excel.getCellValue(row.getCell(0));
			String sampleID = excel.getCellValue(row.getCell(1));
			String sampleName = excel.getCellValue(row.getCell(2));
			String description = excel.getCellValue(row.getCell(3));
			String title = excel.getCellValue(row.getCell(4));
			String comment = excel.getCellValue(row.getCell(5));
			logger.info("Sample information for sample number {} is loaded.", sampleNo);
			System.out.println(carouselNo + "\t" + sampleID + "\t" + sampleName + "\t" + description + "\t" + title
					+ "\t" + comment);
		}
	}

	private String getSampleInfoFile() {
		return filename;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new SpreadsheetTest().run();
	}

	private void run() throws Exception {
		excel = new ExcelWorkbook("/dls/i11/software/gda/config/var/SampleInfo.xls");
		opened = true;
		sampleInfo = excel.getSheet("Sheet1");
		for (int sample = 1; sample <= 16; sample++) {
			loadSampleInfo(sample);
		}
	}

}
