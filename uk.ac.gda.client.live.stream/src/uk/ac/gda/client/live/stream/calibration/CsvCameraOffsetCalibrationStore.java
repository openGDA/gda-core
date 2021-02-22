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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;


/**
 * A {@link CalibrationStore} composed of {@link CameraOffsetCalibration}s
 * constructed from a CSV lookup table file.
 * <p>
 * The column headings in the CSV file are configurable,
 * but must be given in the following order:
 * <br>
 * ($key), ($x scale), ($y scale), ($x offset), ($y offset),
 */
public class CsvCameraOffsetCalibrationStore implements CalibrationStore {

	private File csvFile;
	private CSVFormat csvFormat;

	private String keyHeading;
	private String xScaleHeading;
	private String yScaleHeading;
	private String xOffsetHeading;
	private String yOffsetHeading;
	private Scannable xAxis;
	private Scannable yAxis;

	private static final Logger logger = LoggerFactory.getLogger(CsvCameraOffsetCalibrationStore.class);

	private final Map<String, CalibratedAxesProvider> store;

	/**
	 * This constructor takes the path of the files, the column header names,
	 * and the two scannables associated with the {@link CameraOffsetCalibration}
	 */
	public CsvCameraOffsetCalibrationStore(String csvFilePath, String keyHeading,
			String xScaleHeading, String yScaleHeading,
			String xOffsetHeading, String yOffsetHeading,
			Scannable xAxis, Scannable yAxis) {

		this.csvFile = new File(csvFilePath);
		this.keyHeading = keyHeading;
		this.xScaleHeading = xScaleHeading;
		this.yScaleHeading = yScaleHeading;
		this.xOffsetHeading = xOffsetHeading;
		this.yOffsetHeading = yOffsetHeading;
		this.xAxis = xAxis;
		this.yAxis = yAxis;

		csvFormat = CSVFormat.DEFAULT
						.withHeader(keyHeading, xScaleHeading, yScaleHeading, xOffsetHeading, yOffsetHeading)
						.withSkipHeaderRecord()
						.withIgnoreSurroundingSpaces(true);

		this.store = createStore();
	}

	@Override
	public CalibratedAxesProvider get(String key) {
		return store.get(key);
	}

	@Override
	public boolean containsKey(String calibrationKey) {
		return store.containsKey(calibrationKey);
	}

	private Map<String, CalibratedAxesProvider> createStore() {

		Map<String, CalibratedAxesProvider> map = new HashMap<>();

		try (FileReader fileReader = new FileReader(csvFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader)) {
			CSVParser csvParser = CSVParser.parse(bufferedReader, csvFormat);

			for (CSVRecord row : csvParser) {
				map.put(row.get(keyHeading), createCalibratedAxesProvider(row));
			}

		} catch (IOException e) {
			logger.error("Error reading CSV file", e);
		}

		return map;
	}

	private CalibratedAxesProvider createCalibratedAxesProvider(CSVRecord row) {
		double xScale = Double.parseDouble(row.get(xScaleHeading));
		double yScale = Double.parseDouble(row.get(yScaleHeading));
		double xOffset = Double.parseDouble(row.get(xOffsetHeading));
		double yOffset = Double.parseDouble(row.get(yOffsetHeading));
		return new CameraOffsetCalibration(xAxis, xScale, xOffset, yAxis, yScale, yOffset);
	}

}
