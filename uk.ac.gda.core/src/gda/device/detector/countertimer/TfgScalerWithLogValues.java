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

package gda.device.detector.countertimer;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import gda.device.DeviceException;
import gda.device.detector.countertimer.ScalerOutputProcessor.OutputConfig;

/**
 * A version of TfgScaler for Spectroscopy Ionchambers which assumes it has output channels (time),I0,It,Iref.
 * <p>
 * It has optional additional channels ln(I0/It) and ln(I0/Iref) It also reads the dark current at the scan start.
 */
public class TfgScalerWithLogValues extends TfgScalerWithDarkCurrent {

	public static final String LNI0IT_LABEL = "lnI0It";
	public static final String LNITIREF_LABEL = "lnItIref";

	private boolean outputLogValues = false; // add ln(I0/It) and ln(I0/Iref) to the output

	// Scaler channnel numbers that contain the i0, it, iRef input signals
	private int i0ScalerChannel = 0;
	private int itScalerChannel = 1;
	private int iRefScalerChannel = 2;

	private ScalerOutputProcessor scalerOutputProcessor = new ScalerOutputProcessor();

	private boolean useCustomisedOutput = false;

	public TfgScalerWithLogValues() {
		super();
	}

	/**
	 * @return Returns the outputLogValues.
	 */
	public boolean isOutputLogValues() {
		return outputLogValues;
	}

	/**
	 * When set to true ln(I0/It) and ln(I0/Iref) will be added to the output columns
	 *
	 * @param outputLogValues
	 *            The outputLogValues to set.
	 */

	public void setOutputLogValues(boolean outputLogValues) {
		this.outputLogValues = outputLogValues;

		// adjust the extraNmaes and outputFormat arrays
		if (isConfigured()) {
			return;
		}
		if (outputLogValues) {
			if (!ArrayUtils.contains(extraNames, LNI0IT_LABEL)) {
				extraNames = (String[]) ArrayUtils.add(extraNames, LNI0IT_LABEL);
				outputFormat = (String[]) ArrayUtils.add(outputFormat, "%.5f");
			}
			if (!ArrayUtils.contains(extraNames, LNITIREF_LABEL)) {
				extraNames = (String[]) ArrayUtils.add(extraNames, LNITIREF_LABEL);
				outputFormat = (String[]) ArrayUtils.add(outputFormat, "%.5f");
			}
		} else {
			int numInputs = inputNames.length;
			if (ArrayUtils.contains(extraNames, LNI0IT_LABEL)) {
				int index = ArrayUtils.indexOf(extraNames, LNI0IT_LABEL);
				extraNames = (String[]) ArrayUtils.remove(extraNames, index);
				outputFormat = (String[]) ArrayUtils.remove(outputFormat, index + numInputs);
			}
			if (ArrayUtils.contains(extraNames, LNITIREF_LABEL)) {
				int index = ArrayUtils.indexOf(extraNames, LNITIREF_LABEL);
				extraNames = (String[]) ArrayUtils.remove(extraNames, index);
				outputFormat = (String[]) ArrayUtils.remove(outputFormat, index + numInputs);
			}

		}
	}

	@Override
	public int getTotalChans() throws DeviceException {
		int cols = scaler.getDimension()[0];
		if (numChannelsToRead != null) {
			cols = numChannelsToRead;
		}
		if (timeChannelRequired) {
			cols++;
		}
		if (outputLogValues) {
			cols += 2;
		}
		if (isTFGv2()) {
			cols--;
		}
		return cols;
	}

	@Override
	public double[] readout() throws DeviceException {
		double[] output = super.readout();

		return performCorrections(output);
	}

	protected double[] performCorrections(double[] output) throws DeviceException {
		if (getDarkCurrent() != null) {
			output = adjustForDarkCurrent(output, getCollectionTime());
		}

		if (useCustomisedOutput) {
			output = scalerOutputProcessor.getScalerReadout(output);
		} else if (outputLogValues) {
			output = appendLogValues(output);
		}
		return output;
	}

	@Override
	public double[] readFrame(int frame) throws DeviceException {
		// For legacy XAFS scans the time channel is in column 1
		double[] output = super.readFrame(frame);
		return performCorrections(output);
	}

	protected double[] appendLogValues(double[] output) {
		Double[] logs = new Double[2];
		// find which col is which I0, It and Iref
		Double[] values = getI0ItIRef(output);

		// NOTE Assumes that the order of the data (time, I0, It, Iref...)
		// for dark current does not change.
		logs[0] = Math.log(values[0] / values[1]);
		logs[1] = Math.log(values[1] / values[2]);

		// always return a numerical value
		if (logs[0].isInfinite() || logs[0].isNaN()) {
			logs[0] = 0.0;
		}
		if (logs[1].isInfinite() || logs[1].isNaN()) {
			logs[1] = 0.0;
		}

		// append to output array
		output = correctCounts(output, values);
		output = ArrayUtils.add(output, logs[0]);
		output = ArrayUtils.add(output, logs[1]);
		return output;
	}

	/*
	 * This should only be called when outputLogValues is set to true.
	 */
	private Double[] getI0ItIRef(double[] data) {
		int offset = timeChannelRequired ? 1 : 0;
		return new Double[]{ data[i0ScalerChannel+offset], data[itScalerChannel+offset], data[iRefScalerChannel+offset]};
	}

	private double[] correctCounts(final double[] output, final Double[] counts) {
		final int outIndex = (timeChannelRequired) ? 1 : 0;
		for (int i = 0; i < counts.length; i++) {
			output[i + outIndex] = counts[i];
		}
		return output;
	}

	public int getI0ScalerChannel() {
		return i0ScalerChannel;
	}

	public void setI0ScalerChannel(int i0ScalerChannel) {
		this.i0ScalerChannel = i0ScalerChannel;
	}

	public int getItScalerChannel() {
		return itScalerChannel;
	}

	public void setItScalerChannel(int itScalerChannel) {
		this.itScalerChannel = itScalerChannel;
	}

	public int getiRefScalerChannel() {
		return iRefScalerChannel;
	}

	public void setiRefScalerChannel(int iRefScalerChannel) {
		this.iRefScalerChannel = iRefScalerChannel;
	}
	/**
	 *
	 * @param useCustomisedOutput true - generate readout using {@link ScalerOutputProcessor}, false - use 'normal' readout.
	 * When setting from true to false the previous extraNames, outputformats are restored
	 * (if they have been previously set using {@link #saveExtraNamesFormats()}.
	 */
	public void setUseCustomisedOutput(boolean useCustomisedOutput) {
		// Set the extraNames and outputFormats back to what they were before using customised output.
		if (!useCustomisedOutput && this.useCustomisedOutput) {
			scalerOutputProcessor.restoreExtraNamesFormats(this);
		} else if (useCustomisedOutput) {
			scalerOutputProcessor.configureScalerOutput(this);
		}
		this.useCustomisedOutput = useCustomisedOutput;
	}

	public boolean getUseCustomisedOutput() {
		return useCustomisedOutput;
	}

	/**
	 * Save the current output formats and extra names;
	 */
	public void saveExtraNamesFormats() {
		scalerOutputProcessor.saveExtraNamesFormats(this);
	}

	public void restoreExtraNamesFormats() {
		scalerOutputProcessor.restoreExtraNamesFormats(this);
	}

	public List<OutputConfig> getScalerOutputConfig() {
		return scalerOutputProcessor.getOutputConfig();
	}

	public void setScalerOutputConfig(List<OutputConfig> outputConfig) {
		scalerOutputProcessor.setOutputConfig(outputConfig);
	}

	public void addScalerOutputConfig(OutputConfig conf) {
		scalerOutputProcessor.getOutputConfig().add(conf);
	}

	public void clearScalerOutputConfig() {
		scalerOutputProcessor.getOutputConfig().clear();
	}

	public void configureScalerOutputProcessor() {
		scalerOutputProcessor.configureScalerOutput(this);
	}

	public ScalerOutputProcessor getScalerOutputProcessor() {
		return scalerOutputProcessor;
	}

	public void setTimeChannelName(String name) {
		scalerOutputProcessor.setTimeChannelName(name);
	}

	public String getTimeChannelName() {
		return scalerOutputProcessor.getTimeChannelName();
	}
}
