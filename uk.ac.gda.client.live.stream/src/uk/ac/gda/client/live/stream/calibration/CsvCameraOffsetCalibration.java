/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.calibration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;

/**
 * Creates a new {@link CameraOffsetCalibration} with values provided by a CSV file.
 *
 * Reads a CSV file containing the calibration parameters, updates the CSV file
 * with updated parameters from the GUI and creates a new {@link CameraOffsetCalibration}
 * with the new values.
 *
 */
public class CsvCameraOffsetCalibration implements CalibratedAxesProvider {

    private File csvFile;
    private CSVFormat csvFormat;

    private Scannable xAxis;
    private Scannable yAxis;

    private double xOffset;
    private double yOffset;
    private double xPixelScaling;
    private double yPixelScaling;

    private double xOffsetDefault;
    private double yOffsetDefault;
    private double xPixelScalingDefault;
    private double yPixelScalingDefault;

    private String[] headers;
    private static final String KEY_HEADING = "Calibration";
    private static final String X_OFFSET_HEADING = "X Offset";
    private static final String Y_OFFSET_HEADING = "Y Offset";
    private static final String X_PIXEL_SCALING_HEADING = "X Pixel Scaling";
    private static final String Y_PIXEL_SCALING_HEADING = "Y Pixel Scaling";

    private int[] dataShape;
    private CalibratedAxesProvider calibration;

    private DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    private static final Logger logger = LoggerFactory.getLogger(CsvCameraOffsetCalibration.class);

    public CsvCameraOffsetCalibration(Scannable xAxis, Scannable yAxis, double xOffsetDefault,
        double yOffsetDefault, double xPixelScalingDefault, double yPixelScalingDefault, String csvFilePath) {

        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.csvFile = new File(csvFilePath);

        this.xOffsetDefault = xOffsetDefault;
        this.yOffsetDefault = yOffsetDefault;
        this.xPixelScalingDefault = xPixelScalingDefault;
        this.yPixelScalingDefault = yPixelScalingDefault;


        headers = new String[] {
            KEY_HEADING,
            X_OFFSET_HEADING,
            Y_OFFSET_HEADING,
            X_PIXEL_SCALING_HEADING,
            Y_PIXEL_SCALING_HEADING
        };

        csvFormat = CSVFormat.DEFAULT
            .withHeader(headers)
            .withSkipHeaderRecord()
            .withIgnoreSurroundingSpaces(true);

    	df.setMaximumFractionDigits(340);

        calibration = getCalibration();
    }

    /**
     * Creates new CSV file if it does not exist and sets read and write permissions.
     * The CalibratedAxesProvider object will have default values if the CSV file is empty.
     * Otherwise, its parameters are defined by the last record of the file if
     * the file contains the correct headers.
     *
     * @return {@link CameraOffsetCalibration} object
     */

    private CalibratedAxesProvider getCalibration() {
        try {
            if (csvFile.createNewFile()) {
                setFilePermissions();
                calibration = getDefaultCalibration();
            } else {
            	try(BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile))){
                    List < CSVRecord > records = CSVParser.parse(bufferedReader, csvFormat).getRecords();
                    if (records.isEmpty()) {
                        calibration = getDefaultCalibration();
                    } else {
                        CSVRecord lastRecord = records.get(records.size() - 1);
                        boolean allHeadersSet = Stream.of(headers).allMatch(lastRecord::isSet);
                        if (allHeadersSet) {
                            calibration = getCSVCalibration(lastRecord);
                        } else {
                            logger.error("Incorrect headers.");
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error reading CSV file", e);
        }
        return calibration;
    }

    private CalibratedAxesProvider getDefaultCalibration() {
    	// Adding headers and default values to CSV file
        updateCSVFile("Default", xOffsetDefault, yOffsetDefault, xPixelScalingDefault, yPixelScalingDefault, true);
        return createCalibratedAxesProvider(xOffsetDefault, yOffsetDefault, xPixelScalingDefault, yPixelScalingDefault);
    }

    private CalibratedAxesProvider getCSVCalibration(CSVRecord lastRecord) {
        return createCalibratedAxesProvider(Double.parseDouble(lastRecord.get(X_OFFSET_HEADING)),
        		Double.parseDouble(lastRecord.get(Y_OFFSET_HEADING)),
        		Double.parseDouble(lastRecord.get(X_PIXEL_SCALING_HEADING)),
        		Double.parseDouble(lastRecord.get(Y_PIXEL_SCALING_HEADING)));
    }

    private void updateCSVFile(String calibrationName, double xOffset, double yOffset, double xPixelScaling, double yPixelScaling, boolean addHeaders) {
    	String calibrationParameters = String.join(",",calibrationName, df.format(xOffset), df.format(yOffset), df.format(xPixelScaling), df.format(yPixelScaling));

        try (FileWriter fileWriter = new FileWriter(csvFile, true); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
        	if (addHeaders) {
        		bufferedWriter.write(String.join(",", headers));
                bufferedWriter.write("\n");
        	}
            bufferedWriter.write(calibrationParameters);
            bufferedWriter.write("\n");

        } catch (IOException e) {
            logger.error("Error writing CSV file", e);
        }
    }

    /**
     * New CalibrationAxesProvider object is created from values updated from the supplied values.
     * CSV file containing Camera Offset parameters is updated with the new values in a new row.
     *
     * @param calibrationName time when values were updated
     * @param xOffset offset of camera in x axis
     * @param yOffset offset of camera in y axis
     * @param xPixelScaling number of pixels per scannable unit in x axis
     * @param yPixelScaling number of pixels per scannable unit in y axis
     */
    public void updateCalibrator(String calibrationName, double xOffset, double yOffset, double xPixelScaling, double yPixelScaling) {
        final CalibratedAxesProvider newCalibrator = createCalibratedAxesProvider(xOffset, yOffset, xPixelScaling, yPixelScaling);
        calibration.disconnect();
        newCalibrator.connect();
        if (dataShape != null) {
            newCalibrator.resizeStream(dataShape);
        }
        calibration = newCalibrator;

        updateCSVFile(calibrationName, xOffset, yOffset, xPixelScaling, yPixelScaling, false);
    }

    private CalibratedAxesProvider createCalibratedAxesProvider(double xOffset, double yOffset, double xPixelScaling, double yPixelScaling) {
    	this.xOffset = xOffset;
    	this.yOffset = yOffset;
    	this.xPixelScaling = xPixelScaling;
    	this.yPixelScaling = yPixelScaling;

    	return new CameraOffsetCalibration(xAxis, xPixelScaling, xOffset, yAxis, yPixelScaling, yOffset);
    }

    private void setFilePermissions() {
    	csvFile.setReadable(true, false);
    	csvFile.setWritable(true, false);
    }

    public double getxOffset() {
        return xOffset;
    }

    public double getyOffset() {
        return yOffset;
    }

    public double getxPixelScaling() {
        return xPixelScaling;
    }

    public double getyPixelScaling() {
        return yPixelScaling;
    }

    @Override
    public void connect() {
        calibration.connect();
    }

    @Override
    public void disconnect() {
    	calibration.disconnect();
    }

    @Override
    public IDataset getXAxisDataset() {
        return calibration.getXAxisDataset();
    }

    @Override
    public IDataset getYAxisDataset() {
        return calibration.getYAxisDataset();
    }

    @Override
    public void resizeStream(int[] newShape) {
        dataShape = newShape;
        calibration.resizeStream(newShape);
    }
}
