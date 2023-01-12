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

import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Processes raw Mythen data, converting channel numbers to angles.
 */
public class DataConverter {

	private double[] flatFieldCorrections;

	private BadChannelProvider badChannelProvider;

	private FlatFieldDatasetProvider flatfieldProvider;

	private AngularCalibrationParameters angularCalParams;

	private double blOffset;

	private double globaloff = 0.0;

	/**
	 * Sets the flat field data. This data will be used to adjust each channel.
	 * 
	 * @param flatFieldData
	 *            flat field data
	 */
	public void setFlatFieldData(MythenRawDataset flatFieldData) {
		flatFieldCorrections = null;
		if (flatFieldData != null) {
			flatFieldCorrections = calculateFlatFieldScalingFactors(flatFieldData);
		}
	}

	private double[] calculateFlatFieldScalingFactors(MythenRawDataset flatFieldData) {
		final List<MythenRawData> dataLines = flatFieldData.getLines();

		// Calculate the mean
		double flatFieldMean = 0;
		for (MythenRawData line : dataLines) {
			flatFieldMean += line.getCount();
		}
		flatFieldMean /= dataLines.size();

		// Calculate scaling factors
		double[] corrections = new double[dataLines.size()];
		for (int i = 0; i < dataLines.size(); i++) {
			corrections[i] = flatFieldMean / dataLines.get(i).getCount();
		}
		return corrections;
	}

	public void setFlatFieldDatasetProvider(FlatFieldDatasetProvider flatFieldProvider) {
		this.flatfieldProvider = flatFieldProvider;
	}

	/**
	 * Sets the bad channel provider, which is able to supply a list of the detector's bad channels. The bad channels
	 * will not be included in the processed data. Channel numbers start at zero.
	 * 
	 * @param badChannelProvider
	 *            the bad channel provider
	 */
	public void setBadChannelProvider(BadChannelProvider badChannelProvider) {
		this.badChannelProvider = badChannelProvider;
	}

	public BadChannelProvider getBadChannelProvider() {
		return badChannelProvider;
	}

	/**
	 * Sets the angular calibration parameters used to calculate the angle from the channel number.
	 * 
	 * @param params
	 *            the angular calibration parameters
	 */
	public void setAngularCalibrationParameters(AngularCalibrationParameters params) {
		this.angularCalParams = params;
	}

	public AngularCalibrationParameters getAngularCalibrationParameters() {
		return angularCalParams;
	}

	/**
	 * Sets the beamline offset - the angle where the first channel is located when the detector is at zero degrees.
	 * 
	 * @param beamlineOffset
	 *            the beamline offset
	 */
	public void setBeamlineOffset(double beamlineOffset) {
		this.blOffset = beamlineOffset;
	}

	public void setFlatfieldProvider(FlatFieldDatasetProvider flatfieldProvider) {
		this.flatfieldProvider = flatfieldProvider;
	}

	public FlatFieldDatasetProvider getFlatfieldProvider() {
		return flatfieldProvider;
	}

	private double[] getFlatFieldCorrections() {
		// either use the potentially dynamic values from the provider or use the static values stored in memory
		if (flatfieldProvider == null) {
			return this.flatFieldCorrections;
		}
		return calculateFlatFieldScalingFactors(this.flatfieldProvider.getFlatFieldData());
	}

	/**
	 * Processes the supplied raw dataset.
	 * 
	 * @param dataset
	 *            the raw data
	 * @param detectorPosition
	 *            the detector position
	 * @return the processed data
	 */
	public MythenProcessedDataset process(MythenRawDataset dataset, double detectorPosition) {
		List<MythenProcessedData> newData = new Vector<MythenProcessedData>();

		final Set<Integer> badChannels = badChannelProvider == null ? null : badChannelProvider.getBadChannels();
		final double encoder = detectorPosition;
		final double[] flatFieldValues = getFlatFieldCorrections();

		for (MythenRawData data : dataset.getLines()) {
			int channel = data.getChannel();
			if (badChannels == null || !badChannels.contains(channel)) {
				int count = data.getCount();

				// Flat field correction
				if (flatFieldValues != null) {
					count *= flatFieldValues[channel];
				}

				int error = (int) Math.sqrt(count);
				if (angularCalParams instanceof SimpleAngularCalibrationParameters) {
					AngularCalibrationModuleParameters modparams = angularCalParams.getParametersForModule(channel);
					newData.add(new MythenProcessedData(modparams.getCenter(), count, error, channel));
				} else if (angularCalParams != null) {

					int imod = channel / 1280;
					int channelmod = channel % 1280;

					AngularCalibrationModuleParameters modparams = angularCalParams.getParametersForModule(imod);
					double moffset = modparams.getOffset();
					double center = modparams.getCenter();
					double conversion = modparams.getConversion();
					double ang = 2.404350 + moffset + Math.toDegrees(Math.atan((channelmod - center) * conversion))
							+ encoder + globaloff + blOffset;

					newData.add(new MythenProcessedData(ang, count, error, channel));
				} else {
					newData.add(new MythenProcessedData(channel, count, error));
				}
			}
		}

		return new MythenProcessedDataset(newData);
	}

}
