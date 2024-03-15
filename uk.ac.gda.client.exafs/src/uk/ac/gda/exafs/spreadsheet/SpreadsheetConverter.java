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

package uk.ac.gda.exafs.spreadsheet;

import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.ISampleParametersWithMotorPositions;
import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.SampleParameterMotorPosition;
import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.exafs.spreadsheet.SpreadsheetData.DataType;
import uk.ac.gda.exafs.ui.dialogs.ParameterCollection;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean.BeanTypeNames;
import uk.ac.gda.exafs.ui.dialogs.ParametersForScan;

public class SpreadsheetConverter {

	private String templatePath = "xml/Templates";

	private String sampleFilename = "Sample_Parameters.xml";
	private String transmissionFilename = "Transm_Detector_Parameters.xml";

	private String fluoFilenameTemplate = "%s_%s_%s_Xspress4.xml";

	private String scanFilename = "%s_%s_%s_QEXAFS_Parameters.xml";
	private String outputFilename = "Output_Parameters.xml";

	private Map<BeanTypeNames, String> parameterTypes = new EnumMap<>(BeanTypeNames.class);

	private SpreadsheetData spreadsheetData;
	private static final String TRANSMISSION = "T";

	public SpreadsheetConverter() {
		parameterTypes.put(BeanTypeNames.SCAN, QEXAFSParameters.class.getCanonicalName());
		parameterTypes.put(BeanTypeNames.DETECTOR, DetectorParameters.class.getCanonicalName());
		parameterTypes.put(BeanTypeNames.SAMPLE, B18SampleParameters.class.getCanonicalName());
		parameterTypes.put(BeanTypeNames.OUTPUT, OutputParameters.class.getCanonicalName());
	}

	public String getTemplatePath() {
		return templatePath;
	}

	public void setTemplatePath(String templatePath) {
		// templatePath should be the full path (incl. /xml/Templates) to the template directory.
		this.templatePath = templatePath;
	}

	private String getParameterFullPath(String name) {
		return Paths.get(getTemplatePath(), name).toString();
	}

	public SpreadsheetData getSpreadsheetData() {
		return spreadsheetData;
	}

	public void setSpreadsheetData(SpreadsheetData spreadsheetData) {
		this.spreadsheetData = spreadsheetData;
	}

	private String getFluoFilename(String element, String edge, String crystal) {
		return String.format(fluoFilenameTemplate, element, edge, crystal);
	}

	private String getMotorDemandPositionMethod(String motorName) {
		return ISampleParametersWithMotorPositions.MOTOR_POSITION_GETTER_NAME + "(" + motorName + ")."
				+ SampleParameterMotorPosition.DEMAND_POSITION_GETTER_NAME;
	}

	private String getMotorDoMoveMethod(String motorName) {
		return ISampleParametersWithMotorPositions.MOTOR_POSITION_GETTER_NAME + "(" + motorName + ")."
				+ SampleParameterMotorPosition.DO_MOVE_GETTER_NAME;
	}

	public ParametersForScan getParameters(int row) {

		boolean transmissionMode = spreadsheetData.getRowValue(row, DataType.DETECTION_MODE).equals(TRANSMISSION);

		// Scan
		ParameterValuesForBean scanParams = new ParameterValuesForBean();
		scanParams.setBeanType(parameterTypes.get(BeanTypeNames.SCAN));
		String setScanFilename = scanFilename.formatted(spreadsheetData.getRowValue(row, DataType.ELEMENT),
				spreadsheetData.getRowValue(row, DataType.EDGE), spreadsheetData.getMonoCrystal());
		scanParams.setBeanFileName(getParameterFullPath(setScanFilename));

		// Detector
		ParameterValuesForBean detectorParams = new ParameterValuesForBean();
		detectorParams.setBeanType(parameterTypes.get(BeanTypeNames.DETECTOR));
		if (transmissionMode) {
			detectorParams.setBeanFileName(getParameterFullPath(transmissionFilename));
		} else {
			String fluoFilename = getFluoFilename(spreadsheetData.getRowValue(row, DataType.ELEMENT),
					spreadsheetData.getRowValue(row, DataType.EDGE), spreadsheetData.getMonoCrystal());
			detectorParams.setBeanFileName(fluoFilename);
		}

		// Sample
		ParameterValuesForBean sampleParams = new ParameterValuesForBean();
		sampleParams.setBeanType(parameterTypes.get(BeanTypeNames.SAMPLE));
		sampleParams.setBeanFileName(getParameterFullPath(sampleFilename));

		String name = spreadsheetData.getRowValue(row, DataType.SAMPLE_NAME);
		sampleParams.addParameterValue("getName", name);

		// Calculate the positions for the motors.
		String xIndexLetter = spreadsheetData.getRowValue(row, DataType.X_INDEX);
		int xIndex = spreadsheetData.columnConvert(xIndexLetter);
		int yIndex = (int) Double.parseDouble(spreadsheetData.getRowValue(row, DataType.Y_INDEX)) - 1;

		double xpos = spreadsheetData.getOffsetX() + xIndex * spreadsheetData.getDeltaX();
		sampleParams.addParameterValue(getMotorDemandPositionMethod(spreadsheetData.getxMotorName()), xpos);
		sampleParams.addParameterValue(getMotorDoMoveMethod(spreadsheetData.getxMotorName()), Boolean.TRUE.toString());

		double ypos = spreadsheetData.getOffsetY() + yIndex * spreadsheetData.getDeltaY();
		sampleParams.addParameterValue(getMotorDemandPositionMethod(spreadsheetData.getyMotorName()), ypos);
		sampleParams.addParameterValue(getMotorDoMoveMethod(spreadsheetData.getyMotorName()), Boolean.TRUE.toString());

		sampleParams.addParameterValue("getDescription1", spreadsheetData.getRowValue(row, DataType.SAMPLE_COMMENT));

		sampleParams.addParameterValue("getSampleWheelParameters.getFilter",
				spreadsheetData.getRowValue(row, DataType.ELEMENT) + " Foil");
		sampleParams.addParameterValue("getSampleWheelParameters.isWheelEnabled", Boolean.TRUE.toString());

		// Output
		ParameterValuesForBean outputParams = new ParameterValuesForBean();
		outputParams.setBeanType(parameterTypes.get(BeanTypeNames.OUTPUT));
		outputParams.setBeanFileName(getParameterFullPath(outputFilename));

		// Repetitions
		ParametersForScan paramsForScan = new ParametersForScan();
		int repetitions = (int) Double.parseDouble(spreadsheetData.getRowValue(row, DataType.REPETITIONS));
		paramsForScan.setNumberOfRepetitions(repetitions);
		paramsForScan.addValuesForScanBean(scanParams);
		paramsForScan.addValuesForScanBean(detectorParams);
		paramsForScan.addValuesForScanBean(sampleParams);
		paramsForScan.addValuesForScanBean(outputParams);

		return paramsForScan;
	}

	public ParameterCollection getAllParameters() {
		ParameterCollection collection = new ParameterCollection();
		for (int i = 0; i < spreadsheetData.getNumRows(); i++) {
			collection.addParametersForScan(getParameters(i));
		}

		return collection;
	}

	public String getSampleFilename() {
		return sampleFilename;
	}

	public void setSampleFilename(String sampleFilename) {
		this.sampleFilename = sampleFilename;
	}

	public String getTransmissionFilename() {
		return transmissionFilename;
	}

	public void setTransmissionFilename(String transmissionFilename) {
		this.transmissionFilename = transmissionFilename;
	}

	public String getFluoFilenameTemplate() {
		return fluoFilenameTemplate;
	}

	public void setFluoFilenameTemplate(String fluoFilenameTemplate) {
		this.fluoFilenameTemplate = fluoFilenameTemplate;
	}

	public String getScanFilename() {
		return scanFilename;
	}

	public void setScanFilename(String scanFilename) {
		this.scanFilename = scanFilename;
	}

	public String getOutputFilename() {
		return outputFilename;
	}

	public void setOutputFilename(String outputFilename) {
		this.outputFilename = outputFilename;
	}
}
