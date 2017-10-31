/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.countertimer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.device.scannable.ScannableBase;


/**
 * Class to allow the output of a {@link TfgScalerWithLogValues} to be
 * configured in a flexible way to produce single values, ratios, or log ratios. <p>
 * Configuration is done using list of {@link OutputConfig} which specifies how each value in the readout is produced
 * (i.e. which scaler channel(s) to use and whether it is a 'raw' value, ratio etc)
 *
 * @since 28/9/2017
 */
public class ScalerOutputProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ScalerOutputProcessor.class);

	private List<OutputConfig> outputConfig = new ArrayList<OutputConfig>();

	private boolean timeChannelRequired;

	private String[] extraNamesTfg;
	private String[] outputFormatsTfg;

	private String timeChannelName = "time";

	/**
	 * Generates inputNames and outputFormat arrays according the output configuration
	 * and applies the a {@link TfgScalerWithLogValues} by calling {@link ScannableBase#setExtraNames(String[])} and {@link ScannableBase#setOutputFormat(String[])}
	 * @param tfgScaler
	 */
	public void configureScalerOutput(TfgScaler tfgScaler) {
		// if not already set, save the current output names and formats
		if (extraNamesTfg==null || outputFormatsTfg==null) {
			saveExtraNamesFormats(tfgScaler);
		}

		List<String> extraNamesList = new ArrayList<String>();
		List<String> formatsList = new ArrayList<String>();

		// Add formats for values produced by inputnames
		for(String name : tfgScaler.getInputNames()) {
			formatsList.add("%.6g");
		}

		// Prepend time channel if required
		timeChannelRequired = tfgScaler.isTimeChannelRequired();
		if (timeChannelRequired) {
			extraNamesList.add(timeChannelName);
			formatsList.add("%.6g");
		}

		for (OutputConfig conf : outputConfig) {
			extraNamesList.add(conf.getLabel());
			formatsList.add(conf.getFormat());
		}
		tfgScaler.setExtraNames(extraNamesList.toArray(new String[0]));
		tfgScaler.setOutputFormat(formatsList.toArray(new String[0]));
	}

	public void saveExtraNamesFormats(Scannable tfgScaler) {
		extraNamesTfg = tfgScaler.getExtraNames().clone();
		outputFormatsTfg = tfgScaler.getOutputFormat().clone();
	}

	public void restoreExtraNamesFormats(Scannable tfgScaler) {
		if (extraNamesTfg != null) {
			tfgScaler.setExtraNames(extraNamesTfg);
		}
		if (outputFormatsTfg != null) {
			tfgScaler.setOutputFormat(outputFormatsTfg);
		}
	}

	/**
	 * Processes the raw scaler values and generates an array of values as specified by output configuration
	 * (i.e. series of single scaler value, ratio of two scaler values, or log ratio)
	 * @param values dark current correct scaler counts for each channel (first value might be time)
	 * @return array of processed scaler values
	 */
	public double[] getScalerReadout(double[] values) {
		List<Double> readoutVals = new ArrayList<Double>();

		int startIndex = 0;
		if (timeChannelRequired) {
			startIndex = 1;
			readoutVals.add(values[0]);
		}

		int maxChannel = values.length - startIndex;

		for (OutputConfig conf : outputConfig) {
			// Get indices of data in 'values' array of each scaler channel
			int ind1 = conf.getChannel1() + startIndex;
			if (ind1 >= values.length) {
				logger.warn("Scaler index for value1 ({}) exceeds array size {}", conf.getChannel1(), maxChannel);
			}
			int ind2 = conf.getChannel2() + startIndex;
			if (ind2 >= values.length && conf.getOperationType() != OutputConfig.READOUT) {
				logger.warn("Scaler index for value2 ({}) exceeds array size {}", conf.getChannel2(), maxChannel);
			}

			// Add data to the list, performing appropriate operation
			switch (conf.getOperationType()) {
				case OutputConfig.READOUT:
					readoutVals.add(values[ind1]);
					break;
				case OutputConfig.DIVIDE:
					readoutVals.add(values[ind1] / values[ind2]);
					break;
				case OutputConfig.DIVIDE_LOG:
					readoutVals.add(Math.log(values[ind1] / values[ind2]));
					break;
			}
		}
		double[] vals = new double[readoutVals.size()];
		int i = 0;
		for(double val : readoutVals) {
			vals[i++] = val;
		}
		return vals;
	}

	public List<OutputConfig> getOutputConfig() {
		return outputConfig;
	}

	public void setOutputConfig(List<OutputConfig> outputConfig) {
		this.outputConfig = outputConfig;
	}

	public String getTimeChannelName() {
		return timeChannelName;
	}

	public void setTimeChannelName(String timeChannelName) {
		this.timeChannelName = timeChannelName;
	}

	/**
	 * Class to specify properties of single output value.
	 * i.e. output format and label, and how to generate the value
	 * Values may be either single scaler, ratio of two scalers, or a log ratio
	 */
	public static class OutputConfig {

		private String label;
		private String format;
		private int channel1;
		private int channel2;
		private int operationType;

		public final static int READOUT = 0;
		public final static int DIVIDE = 1;
		public final static int DIVIDE_LOG = 2;

		public OutputConfig() {
			label="chanel0";
			format="%.4f";
			channel1=0;
			channel2=0;
			operationType = READOUT;
		}

		public OutputConfig(String label, String format, int channel) {
			this.label = label;
			this.format = format;
			channel1 = channel;
			channel2 = 0;
			operationType = READOUT;
		}

		public OutputConfig(String label, String format, int channel1, int channel2, int operation) {
			this.label = label;
			this.format = format;
			this.channel1 = channel1;
			this.channel2 = channel2;
			operationType = operation;
		}

		public String getLabel() {
			return label;
		}

		public String getFormat() {
			return format;
		}

		public int getChannel1() {
			return channel1;
		}

		public int getChannel2() {
			return channel2;
		}

		public int getOperationType() {
			return operationType;
		}


		public void setLabel(String label) {
			this.label = label;
		}

		public void setFormat(String format) {
			this.format = format;
		}

		public void setChannel1(int channel1) {
			this.channel1 = channel1;
		}

		public void setChannel2(int channel2) {
			this.channel2 = channel2;
		}

		public void setOperationType(int operationType) {
			this.operationType = operationType;
		}
	}


}
