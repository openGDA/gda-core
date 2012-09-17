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

package gda.device.detector.mythen.data;

import gda.device.detector.mythen.data.AngularCalibrationParametersFile;
import gda.device.detector.mythen.data.DataConverter;
import gda.device.detector.mythen.data.MythenProcessedDataset;
import gda.device.detector.mythen.data.MythenRawDataset;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link DataConverter}.
 */
public class DataConverterTest {
	
	private static final String REAL_RAW_MYTHEN_DATA_FILENAME = "testfiles/gda/device/detector/mythen/data/Si_15keV_5.raw";
	
	private static final String REAL_PROCESSED_MYTHEN_DATA_FILENAME = "testfiles/gda/device/detector/mythen/data/Si_15keV_5.dat";
	
	private static final String REAL_ANGULAR_CALIBRATION_PARAMETERS_FILENAME = "testfiles/gda/device/detector/mythen/data/ang.off";
	
	private MythenRawDataset rawData;
	
	/**
	 * Position of detector when test data was captured.
	 */
	private static final double DETECTOR_POSITION = 10.0;
	
	private DataConverter converter;
	
	private MythenProcessedDataset actualProcessedData;
	
	/**
	 * Empirically-determined value for delta used when comparing angles
	 * calculated by {@link DataConverter} and those in the 'real' Mythen data
	 * file.
	 */
	private static final double ANGLE_DELTA = 0.00001;
	
	@Before
	public void setUp() {
		// Load some raw data
		File rawDataFile = new File(REAL_RAW_MYTHEN_DATA_FILENAME);
		rawData = new MythenRawDataset(rawDataFile);

		converter = new DataConverter();

		// Load angular calibration parameters
		File angCalParamsFile = new File(REAL_ANGULAR_CALIBRATION_PARAMETERS_FILENAME);
		AngularCalibrationParameters params = new AngularCalibrationParametersFile(angCalParamsFile);
		converter.setAngularCalibrationParameters(params);
		
		converter.setBeamlineOffset(0.08208);
		
		// Load a 'real' Mythen data file
		File actualProcessedDataFile = new File(REAL_PROCESSED_MYTHEN_DATA_FILENAME);
		actualProcessedData = new MythenProcessedDataset(actualProcessedDataFile);
	}

	/**
	 * Tests processing of raw data to ensure the calculated angle/count/error
	 * values match those in a 'real' Mythen data file.
	 */
	@Test
	public void testValuesCalculatedByDataConverter() {
		MythenProcessedDataset processedData = converter.process(rawData, DETECTOR_POSITION);
		compareDatasets(actualProcessedData, processedData);
	}
	
	/**
	 * Tests that the bad channel list is applied by the data converter.
	 */
	@Test
	public void testConverterWithBadChannelList() {
		// Some bad channels will be removed
		converter.setBadChannelProvider(new SimpleBadChannelProvider(setOf(10, 20, 30)));
		
		// Simulate the bad channels being removed from the 'real' data too
		List<MythenProcessedData> actualDataTrimmed = new Vector<MythenProcessedData>(actualProcessedData.getLines());
		actualDataTrimmed.remove(10);
		actualDataTrimmed.remove(19);
		actualDataTrimmed.remove(28);
		actualProcessedData = new MythenProcessedDataset(actualDataTrimmed);
		
		MythenProcessedDataset processedData = converter.process(rawData, DETECTOR_POSITION);
		compareDatasets(actualProcessedData, processedData);
	}
	
	private static void compareDatasets(MythenProcessedDataset actualProcessedData, MythenProcessedDataset processedData) {
		assertEquals("Processed data doesn't have the expected number of lines", actualProcessedData.getLines().size(), processedData.getLines().size());
		for (int i=0; i<actualProcessedData.getLines().size(); i++) {
			MythenProcessedData actualLine = actualProcessedData.getLines().get(i);
			MythenProcessedData newLine = processedData.getLines().get(i);
			assertEquals("Unexpected value for angle on line " + i, actualLine.getAngle(), newLine.getAngle(), ANGLE_DELTA);
			assertEquals("Unexpected value for count on line " + i, actualLine.getCount(), newLine.getCount());
			assertEquals("Unexpected value for error on line " + i, actualLine.getError(), newLine.getError());
		}
	}
	
	/**
	 * Tests that the converter treats a {@code null} bad channel list the same
	 * as an empty list.
	 */
	@Test
	public void testConverterWithNullBadChannelList() {
		converter.setBadChannelProvider(null);
		
		MythenProcessedDataset processedData = converter.process(rawData, DETECTOR_POSITION);
		
		assertEquals("Processed data doesn't have the expected number of lines", actualProcessedData.getLines().size(), processedData.getLines().size());
	}
	
	/**
	 * Tests flat field correction.
	 */
	@Test
	public void testConverterWithFlatFieldCorrection() {
		// Using raw data itself as the flat field data, so all count values in
		// processed data should be the same
		converter.setFlatFieldData(rawData);
		
		// Known bad channels in the raw data
		converter.setBadChannelProvider(new SimpleBadChannelProvider(setOf(7537, 11032, 20456, 21002)));
		
		MythenProcessedDataset processedData = converter.process(rawData, DETECTOR_POSITION);
		double[] counts = processedData.getCountArray();
		for (int i=1; i<counts.length; i++) {
			assertEquals("" + i, counts[0], counts[i], 0);
		}
	}

	private static Set<Integer> setOf(Integer... channels) {
		HashSet<Integer> badChannels = new HashSet<Integer>();
		badChannels.addAll(Arrays.asList(channels));
		return badChannels;
	}
	
}
