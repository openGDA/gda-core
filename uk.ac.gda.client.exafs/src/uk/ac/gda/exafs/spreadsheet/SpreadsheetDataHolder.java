/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.spreadsheet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpreadsheetDataHolder {
	private static final Logger logger = LoggerFactory.getLogger(SpreadsheetDataHolder.class);

	// Spreadsheet data list of data in each row
	List<List<String>> data = Collections.emptyList();

	public SpreadsheetDataHolder() {
		// data = new ArrayList<>();
	}

	public String getCellData(int column, int row) {
		// Need to check row, column to make sure in bounds of list.
		return data.get(row).get(column);
	}

	public double getCellNumericData(int column, int row) {
		String strData = getCellData(column, row);
		// throws unchecked exception if conversion from string to double is not possible (e.g. due to invalid format)
		// might need to throw it and handle explicitly when converting data.
		return Double.parseDouble(strData);
	}

	public int getNumRows() {
		return data.size();
	}

	public int getNumColumns(int row) {
		return getRow(row).size();
	}

	public List<String> getRow(int row) {
		return data.get(row);
	}

	public void setSpreadsheetData(List<List<String>> data) {
		this.data = new ArrayList<>(data);
	}

	public static SpreadsheetDataHolder loadCsvSpreadsheet(String file) throws IOException {
		return getCsvDataHolder(file);
	}

	private static SpreadsheetDataHolder getCsvDataHolder(String filePath) throws IOException {
		SpreadsheetDataHolder dataHolder = new SpreadsheetDataHolder();
		List<List<String>> allData = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				allData.add(Arrays.asList(values));
			}
			dataHolder.setSpreadsheetData(allData);
		}
		return dataHolder;
	}

	public static SpreadsheetDataHolder loadExcelSpreadsheet(String filePath) throws IOException {
		FileInputStream file = new FileInputStream(filePath);
		Workbook workbook = new XSSFWorkbook(file);
		Sheet sheet = workbook.getSheetAt(0);
		return getDataHolder(sheet);
	}

	private static SpreadsheetDataHolder getDataHolder(Sheet sheet) {
		SpreadsheetDataHolder dataHolder = new SpreadsheetDataHolder();
		List<List<String>> allData = new ArrayList<List<String>>();

		// Iterate through each rows one by one.
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			List<String> dataList = new ArrayList<String>();

			// For each row, iterate through all the columns.
			Iterator<Cell> cellIterator = row.cellIterator();

			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();

				// Check the cell type and format accordingly, add to "dataList".
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC:
				case Cell.CELL_TYPE_STRING:
					dataList.add(cell.toString());
					break;
				default:
					throw new IllegalArgumentException("Data type " + cell.getCellType() + " not recognized");
				}
			}

			// Add row number and "dataList" into "allData".
			allData.add(row.getRowNum(), dataList);
			// Set "allData" into "dataHolder".
			dataHolder.setSpreadsheetData(allData);
		}
		return dataHolder;
	}
}
