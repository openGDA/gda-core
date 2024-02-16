/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.ac.gda.exafs.spreadsheet.SpreadsheetConverter;
import uk.ac.gda.exafs.spreadsheet.SpreadsheetData;
import uk.ac.gda.exafs.spreadsheet.SpreadsheetData.DataType;
import uk.ac.gda.exafs.spreadsheet.SpreadsheetDataHolder;
import uk.ac.gda.exafs.ui.dialogs.ParameterCollection;

public class SpreadsheetConverterTest {

	public static final String FOLDER_PATH = "testfiles/spreadsheet";
	Path xlsFilePath = Paths.get(FOLDER_PATH, "White_Holder_3.xlsx");
	Path csvFilePath = Paths.get(FOLDER_PATH, "White_Holder_3.csv");

	/* test file load (holder) excel data. */
	@Test
	public void testXlsLoad() throws Exception {
		SpreadsheetDataHolder dataHolder = SpreadsheetDataHolder.loadExcelSpreadsheet(xlsFilePath.toString());
		testDataHolder(dataHolder);
	}

	@Test
	public void testCsvLoad() throws IOException {
		SpreadsheetDataHolder dataHolder = SpreadsheetDataHolder.loadCsvSpreadsheet(csvFilePath.toString());
		testDataHolder(dataHolder);
	}

	private void testDataHolder(SpreadsheetDataHolder dataHolder) {
		assertNotNull("DataHolder should not be null", dataHolder);

		// check is correct format, total excel file rows, header column number, sample name cell data.
		assertTrue("Check is correct format - first line content contains [TO BE FILLED BY LOCAL CONTACT]", dataHolder.getRow(0).contains("TO BE FILLED BY LOCAL CONTACT"));
		assertEquals("Values read are incorrect -", 36, dataHolder.getNumRows());
		assertEquals("Values read are incorrect -", 8, dataHolder.getNumColumns(5));
		assertEquals("Values read are incorrect -", "MA_37", dataHolder.getCellData(3, 6));
	}

	private SpreadsheetData data() throws Exception {
		SpreadsheetData data = new SpreadsheetData();
		data.dataConvert(xlsFilePath.toString());
		return data;
	}

	private SpreadsheetConverter convert() throws Exception {
		SpreadsheetConverter converter = new SpreadsheetConverter();
		converter.setSpreadsheetData(data());
		return converter;
	}

	private SpreadsheetData expectedData() {
		SpreadsheetData spreadsheetData = new SpreadsheetData();
		spreadsheetData.setxMotorName("frameX");
		spreadsheetData.setDetector("xspress4Odin");
		spreadsheetData.setMonoCrystal("Si111");
		spreadsheetData.setExperimentNumber("sp32498-1");
		spreadsheetData.setOffsetX(132);
		spreadsheetData.setDeltaX(16);

		List<String> firstrowValue = new ArrayList<>(List.of("Zr", "K", "T", "MA_37", "24C", "A", "1.0", "3.0"));
		spreadsheetData.addRowValues(firstrowValue);
		return spreadsheetData;
	}

	/* test data convert (data), from excel file string to SpreadsheetData. */
	@Test
	public void testDataConvert() throws Exception {
		String xMotor = data().getxMotorName();
		String detector = data().getDetector();
		double xOffset = data().getOffsetX();
		double xDelta = data().getDeltaX();
		List<String> rowValue = data().getRowValues(0);
		String sampleName = data().getRowValue(0, DataType.SAMPLE_NAME).toString();

		// check motor x name, detector, offset x, delta x, first row values, sample name. from data.
		assertEquals("Values read are incorrect -", expectedData().getxMotorName(), xMotor);
		assertEquals("Values read are incorrect -", expectedData().getDetector(), detector);
		assertEquals("Values read are incorrect -", expectedData().getOffsetX(), xOffset, 0.001);
		assertEquals("Values read are incorrect -", expectedData().getDeltaX(), xDelta, 0.001);
		assertEquals("Values read are incorrect -", expectedData().getRowValues(0), rowValue);
		assertEquals("Values read are incorrect -", expectedData().getRowValue(0, DataType.SAMPLE_NAME), sampleName);
	}

	/* test collections generate (converter) */
	@Test
	public void testCollectionsGenerate() throws Exception {
		// check motor x name. from converter.
		assertEquals(expectedData().getxMotorName(), convert().getSpreadsheetData().getxMotorName());

		// ParametersForScan had added all data to Collection.
		ParameterCollection collection;
		collection = convert().getAllParameters();

		assertNotNull("Collection should not be null", collection);
	}

}
