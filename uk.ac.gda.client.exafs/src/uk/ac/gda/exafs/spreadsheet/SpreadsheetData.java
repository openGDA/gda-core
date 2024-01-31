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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SpreadsheetData {

	private String xMotorName;
	private String yMotorName;
	private String detector;
	private String monoCrystal;
	private String experimentNumber;
	private double offsetX;
	private double offsetY;
	private double deltaX;
	private double deltaY;

	private List<List<String>> allRowValues = new ArrayList<>();
	private List<String> headers = new ArrayList<>();

	public enum DataType {
		ELEMENT, EDGE, DETECTION_MODE, SAMPLE_NAME, SAMPLE_COMMENT, X_INDEX, Y_INDEX, REPETITIONS
	}

	private Map<DataType, Integer> dataTypeColumnMap = new EnumMap<>(DataType.class);

	public SpreadsheetData() {
		setDefaultMapValues();
	}

	public void setDefaultMapValues() {
		dataTypeColumnMap.clear();
		int i = 0;
		for (var val : DataType.values()) {
			dataTypeColumnMap.put(val, i++);
		}
	}

	public boolean dataConvert(String filePath) throws Exception {
		boolean isCorrect = false;
		SpreadsheetDataHolder dataHolder = null;
		if (filePath.contains("csv")) {
			dataHolder = SpreadsheetDataHolder.loadCsvSpreadsheet(filePath);
		} else {
			dataHolder = SpreadsheetDataHolder.loadExcelSpreadsheet(filePath);
		}

		if (dataHolder.getRow(0).contains("TO BE FILLED BY LOCAL CONTACT")) {
			isCorrect = true;
			setData(dataHolder);
		}
		return isCorrect;
	}

	private void setData(SpreadsheetDataHolder dataHolder) {
		// Set "spreadsheetData" from "dataHolder" to get cell data.
		setxMotorName(dataHolder.getCellData(0, 2));
		setyMotorName(dataHolder.getCellData(1, 2));
		setDetector(dataHolder.getCellData(2, 2));
		setMonoCrystal(dataHolder.getCellData(3, 2));
		setExperimentNumber(dataHolder.getCellData(1, 4));
		setOffsetX(dataHolder.getCellNumericData(4, 2));
		setOffsetY(dataHolder.getCellNumericData(5, 2));
		setDeltaX(dataHolder.getCellNumericData(6, 2));
		setDeltaY(dataHolder.getCellNumericData(7, 2));

		setHeaders(dataHolder.getRow(5));
		clearRowValues();

		for (int i = 6; i < dataHolder.getNumRows(); i++) {
			addRowValues(dataHolder.getRow(i));
		}
	}

	public Integer columnConvert(String data) {
		char startChar = 'A';
		char charValue = data.toUpperCase().toCharArray()[0];
		int num = charValue - startChar;

		return num;
	}

	public String getRowValue(int rowNumber, DataType type) {
		return allRowValues.get(rowNumber).get(dataTypeColumnMap.get(type));
	}

	public int getNumRows() {
		return allRowValues.size();
	}

	public List<String> getRowValues(int rowNumber) {
		return allRowValues.get(rowNumber);
	}

	public void addRowValues(List<String> values) {
		allRowValues.add(values);
	}

	public void clearRowValues() {
		allRowValues.clear();
	}

	public String getxMotorName() {
		return xMotorName;
	}

	public void setxMotorName(String xMotorName) {
		this.xMotorName = xMotorName;
	}

	public String getyMotorName() {
		return yMotorName;
	}

	public void setyMotorName(String yMotorName) {
		this.yMotorName = yMotorName;
	}

	public String getDetector() {
		return detector;
	}

	public void setDetector(String detector) {
		this.detector = detector;
	}

	public String getMonoCrystal() {
		return monoCrystal;
	}

	public void setMonoCrystal(String monoCrystal) {
		this.monoCrystal = monoCrystal;
	}

	public String getExperimentNumber() {
		return experimentNumber;
	}

	public void setExperimentNumber(String experimentNumber) {
		this.experimentNumber = experimentNumber;
	}

	public double getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(double offsetX) {
		this.offsetX = offsetX;
	}

	public double getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(double offsetY) {
		this.offsetY = offsetY;
	}

	public double getDeltaX() {
		return deltaX;
	}

	public void setDeltaX(double deltaX) {
		this.deltaX = deltaX;
	}

	public double getDeltaY() {
		return deltaY;
	}

	public void setDeltaY(double deltaY) {
		this.deltaY = deltaY;
	}

	public List<List<String>> getAllRowValues() {
		return allRowValues;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}
}
