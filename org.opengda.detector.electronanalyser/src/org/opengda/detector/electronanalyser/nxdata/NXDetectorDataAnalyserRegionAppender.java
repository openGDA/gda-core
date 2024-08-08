package org.opengda.detector.electronanalyser.nxdata;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.datawriter.NexusDataWriter;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;

/**
 * A {@link NXDetectorDataAppender} for collecting data from VGScienta Electron analyser.
 * It is compatible with the standard GDA scan {@link NexusDataWriter} and supports data collections over
 * multiple analyser regions of different data sizes and produce single nexus data file per scan.
 *
 * It also provides plots of total intensity count over the scanned scannable, i.e. excitation energies for energy scan.
 *
 * @author fy65
 *
 */
public class NXDetectorDataAnalyserRegionAppender implements NXDetectorDataAppender {
	private final Logger logger = LoggerFactory.getLogger(NXDetectorDataAnalyserRegionAppender.class);
	private final double[] intensities;
	private final String[] regionNames;

	public NXDetectorDataAnalyserRegionAppender(String regionName, double intensity) {
		this(new String[] {regionName}, new double[] {intensity});
	}

	public NXDetectorDataAnalyserRegionAppender(String[] regionNames, double[] intensities) {
		this.intensities = intensities;
		this.regionNames = regionNames;

		if (intensities.length != regionNames.length) {
			throw new IllegalArgumentException(
				String.format("intensities (length = %s) must have the same length as region names (length = %s)", intensities.length, regionNames.length)
			);
		}
		logger.info("Created with regions: {}, intensities: {}", Arrays.toString(regionNames), Arrays.toString(intensities));
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
		for (int i = 0; i < intensities.length ; i++) {
			final String regionName = regionNames[i];
			final double intensity = intensities[i];
			data.setPlottableValue(regionName, intensity);
			logger.info("Adding region {} with intensity value of {} to plot", regionName, intensity);
		}
	}
}
