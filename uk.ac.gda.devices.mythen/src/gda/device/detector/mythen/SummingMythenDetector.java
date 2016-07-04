/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.mythen;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.analysis.Plotter;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.device.DeviceException;
import gda.device.detector.mythen.data.MythenDataFileUtils;
import gda.device.detector.mythen.data.MythenDataFileUtils.FileType;
import gda.device.detector.mythen.data.MythenSum;

public class SummingMythenDetector extends MythenDetectorImpl {

	private static final Logger logger = LoggerFactory.getLogger(SummingMythenDetector.class);

	protected double step = 0.004;

	/**
	 * Sets the angle step to use when summing the data.
	 */
	public void setStep(double step) {
		this.step = step;
	}

	public double getStep() {
		return step;
	}

	protected String panelName = "Mythen";

	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
		sumProcessedData();
	}

	protected void sumProcessedData()  throws DeviceException {
		final int numDatasets = (int) collectionNumber;
		logger.info(String.format("Going to sum %d dataset(s)", numDatasets));

		// Build filename of each processed data file
		String[] filenames = new String[numDatasets];
		for (int i=1; i<=numDatasets; i++) {
			String filename = buildFilename(i, FileType.PROCESSED);
			File processedFile = new File(getDataDirectory(), filename);
			filenames[i-1] = processedFile.getAbsolutePath();
		}

		// Load all processed data files
		logger.info("Loading processed data...");
		double[][][] allData = MythenDataFileUtils.readMythenProcessedDataFiles(filenames);
		logger.info("Done");

		// Sum the data
		logger.info("Summing data...");
		double[][] summedData = MythenSum.sum(allData, numberOfModules, dataConverter.getBadChannelProvider(), step);
		logger.info("Done");

		// Save the summed data
		File summedDataFile = new File(getDataDirectory(), buildFilename("summed", FileType.PROCESSED));
		logger.info(String.format("Saving summed data to %s", summedDataFile.getAbsolutePath()));
		try {
			MythenDataFileUtils.saveProcessedDataFile(summedData, summedDataFile.getAbsolutePath());
			logger.info("Summed data saved successfully");
		} catch (IOException e) {
			final String msg = String.format(
				"Unable to save summed data to %s, but all individual data files have been saved successfully",
				summedDataFile);
			logger.error(msg, e);
			throw new DeviceException(msg, e);
		}

		// Register summed data file
		FileRegistrarHelper.registerFile(summedDataFile.getAbsolutePath());

		// Plot summed data
		final int numChannels = summedData.length;
		double[] angles = new double[numChannels];
		double[] counts = new double[numChannels];
		for (int i=0; i<numChannels; i++) {
			angles[i] = summedData[i][0];
			counts[i] = summedData[i][1];
		}
		String name2 = FilenameUtils.getName(summedDataFile.getAbsolutePath());
		Dataset anglesDataset = DatasetFactory.createFromObject(angles);
		anglesDataset.setName("angle");
		Dataset countsDataset = DatasetFactory.createFromObject(counts);
		countsDataset.setName(name2);
		Plotter.plot(panelName, anglesDataset, countsDataset);
	}

}
