/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.impl.NDUtils;
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.jython.InterfaceProvider;
import gda.observable.Observable;
import gda.observable.Observer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

public abstract class ADControllerBase implements ADController, InitializingBean {
	static final Logger logger = LoggerFactory.getLogger(ADControllerBase.class);

	protected int ffmpegImageOutHeightMax = 1000;
	protected int ffmpegImageOutWidthMax = 1000;
	protected int imageHistSize = 256;
	private double arrayMinCallbackTime = 1.0;
	private double histogramMinCallbackTime = 1.0;
	private String imageNDArrayPortInput;

	private String serviceName;

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (StringUtils.isEmpty(serviceName))
			throw new Exception("ServiceName is not defined ");
		if (ffmpegImageOutHeightMax < 1)
			throw new IllegalArgumentException("ffmpegImageOutHeightMax < 1");
		if (ffmpegImageOutWidthMax < 1)
			throw new IllegalArgumentException("ffmpegImageOutWidthMax < 1");
		
	}

	protected int[] getDataDimensions(NDPluginBase pluginBase) throws Exception {
		int nDimensions = pluginBase.getNDimensions_RBV();
		int[] dimFromEpics = new int[3];
		dimFromEpics[0] = pluginBase.getArraySize2_RBV();
		dimFromEpics[1] = pluginBase.getArraySize1_RBV();
		dimFromEpics[2] = pluginBase.getArraySize0_RBV();

		int[] dims = java.util.Arrays.copyOfRange(dimFromEpics, 3 - nDimensions, 3);
		return dims;
	}

	@Override
	public ImageData getImageData() throws Exception {

		int[] dims = getDataDimensions(getImageNDArray().getPluginBase());

		int expectedNumPixels = dims[0];
		for (int i = 1; i < dims.length; i++) {
			expectedNumPixels = expectedNumPixels * dims[i];
		}
		if (dims.length == 0) {
			throw new Exception("Dimensions of data from " + getDetectorName() + " are zero length");
		}
		Object imageData;

		short dataType = getImageNDArray().getPluginBase().getDataType_RBV();
		switch (dataType) {
		case NDPluginBase.UInt8: {
			byte[] b = new byte[] {};
			b = getImageNDArray().getByteArrayData(expectedNumPixels);
			if (expectedNumPixels > b.length)
				throw new DeviceException("Data size is not valid");
			{
				short cd[] = new short[expectedNumPixels];
				for (int i = 0; i < expectedNumPixels; i++) {
					cd[i] = (short) (b[i] & 0xff);
				}
				imageData = cd;
			}
		}
			break;
		case NDPluginBase.Int8: {
			byte[] b = getImageNDArray().getByteArrayData(expectedNumPixels);
			if (expectedNumPixels > b.length)
				throw new DeviceException("Data size is not valid");
			imageData = b;
			break;
		}
		case NDPluginBase.Int16: {
			short[] s = getImageNDArray().getShortArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
						+ expectedNumPixels);

			imageData = s;
		}
			break;
		case NDPluginBase.UInt16: {
			short[] s = getImageNDArray().getShortArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
						+ expectedNumPixels);

			int cd[] = new int[expectedNumPixels];
			for (int i = 0; i < expectedNumPixels; i++) {
				cd[i] = (s[i] & 0xffff);
			}
			imageData = cd;
		}
			break;
		case NDPluginBase.UInt32: // TODO should convert to INT64 if any numbers are negative
		case NDPluginBase.Int32: {
			int[] s = getImageNDArray().getIntArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
						+ expectedNumPixels);

			imageData = s;
		}
			break;
		case NDPluginBase.Float32:
		case NDPluginBase.Float64: {
			float[] s = getImageNDArray().getFloatArrayData(expectedNumPixels);
			if (expectedNumPixels > s.length)
				throw new DeviceException("Data size is not valid length read:" + s.length + " expected:"
						+ expectedNumPixels);

			imageData = s;
		}
			break;
		default:
			throw new DeviceException("Type of data is not understood :" + dataType);
		}

		ImageData data = new ImageData();
		data.dimensions = dims;
		data.data = imageData;
		return data;
	}

	@Override
	public void setExposure(double d) throws Exception {

		String formatCmd = getSetExposureTimeCmd();
		if (formatCmd != null) {
			final String cmd = String.format(formatCmd, d);
			try {
				String result = InterfaceProvider.getCommandRunner().evaluateCommand(cmd);
				if (result == null)
					throw new Exception("Error executing command '" + cmd + "'");
			} catch (Exception e) {
				logger.error("Error setting exposure time", e);
			}

		} else {
			getAdBase().setAcquireTime(d);
			getAdBase().setImageMode(ImageMode.CONTINUOUS.ordinal());
			getAdBase().startAcquiring();
		}
	}

	@Override
	public String getSetExposureTimeCmd() {
		return setExposureTimeCmd;
	}

	private String setExposureTimeCmd;

	private double imageMin;

	public void setSetExposureTimeCmd(String setExposureTimeCmd) {
		this.setExposureTimeCmd = setExposureTimeCmd;
	}

	@Override
	public void startFfmpegStream() throws Exception {
		FfmpegStream ffmpegStream = getFfmpegStream();
		NDProcess ndProcess = getLiveViewNDProc();
		if (ndProcess.getEnableOffsetScale_RBV() != 1)
			ndProcess.setEnableOffsetScale(1);
		if (ndProcess.getDataTypeOut_RBV() != NDProcess.DatatypeOut_UInt8)
			ndProcess.setDataTypeOut(NDProcess.DatatypeOut_UInt8);

		ndProcess.setEnableHighClip(1);
		ndProcess.setEnableLowClip(1);
		ndProcess.setHighClip(255);
		ndProcess.setLowClip(0);
		NDPluginBase procBase = ndProcess.getPluginBase();
		if (!procBase.isCallbacksEnabled_RBV())
			procBase.enableCallbacks();

		ffmpegStream.setMAXW(getFfmpegImageOutWidthMax());
		ffmpegStream.setMAXH(getFfmpegImageOutHeightMax());
		ffmpegStream.setQUALITY(100.);
		NDPluginBase ffmpegBase = ffmpegStream.getPluginBase();
		String procPortName_RBV = procBase.getPortName_RBV();
		String ndArrayPort_RBV = ffmpegBase.getNDArrayPort_RBV();
		if (ndArrayPort_RBV == null || !ndArrayPort_RBV.equals(procPortName_RBV))
			ffmpegBase.setNDArrayPort(procPortName_RBV);
		if (!ffmpegBase.isCallbacksEnabled_RBV())
			ffmpegBase.enableCallbacks();

	}

	@Override
	public void stopFfmpegStream() throws Exception {
		NDProcess ndProcess = getLiveViewNDProc();
		NDPluginBase procBase = ndProcess.getPluginBase();
		if (procBase.isCallbacksEnabled_RBV())
			procBase.disableCallbacks();

		FfmpegStream ffmpegStream = getFfmpegStream();
		NDPluginBase ffmpegBase = ffmpegStream.getPluginBase();
		if (ffmpegBase.isCallbacksEnabled_RBV())
			ffmpegBase.disableCallbacks();
	}

	@Override
	public void stopExposure() throws Exception {
		getAdBase().stopAcquiring();
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public void setLiveViewRange(final double range_min, final double range_max) throws Exception {
		// set a monitor on the imageStats array counter, switch on imageStats and histogram if off
		// in monitor remove future updates, return to previous state

		final int histSize = getImageHistSize();
		double histMin = getImageMin();
		double histMax = getImageMax();
		getImageNDStats().setHistSize(histSize);
		getImageNDStats().setHistMin(histMin);
		getImageNDStats().setHistMax(histMax);
		double step = (histMax - histMin) / histSize;
		double[] range = new double[histSize];
		range[0] = histMin;
		for (int i = 1; i < histSize; i++) {
			range[i] = range[i - 1] + step;
		}
		final DoubleDataset xData = new DoubleDataset(range);

		final boolean wasEnabled = getImageNDStats().getPluginBase().isCallbacksEnabled_RBV();
		final short histogramWasComputed = getImageNDStats().getComputeHistogram_RBV();

		getImageNDStats().setComputeHistogram(1);
		getImageNDStats().getPluginBase().enableCallbacks();

		final int counterBefore = getImageNDStats().getPluginBase().getArrayCounter_RBV();
		final Observable<Integer> obsvble = getImageNDStats().getPluginBase().createArrayCounterObservable();
		obsvble.addObserver(new Observer<Integer>() {

			boolean done = false;

			@Override
			public void update(Observable<Integer> source, Integer arg) {
				if (arg == counterBefore || done)
					return;
				done = true;
				obsvble.removeObserver(this);
				try {
					if (histogramWasComputed != 0) {
						getImageNDStats().setComputeHistogram(histogramWasComputed);

					}
					if (!wasEnabled) {
						getImageNDStats().getPluginBase().disableCallbacks();
					}

					double[] yData = getImageNDStats().getHistogram_RBV(histSize);

					if (yData.length != xData.getSize()) {
						throw new Exception("Length of histogram does not match histSize");
					}
					double min = 0.;
					double max = getImageMax();
					DoubleDataset ds = new DoubleDataset(yData);
					int j = getPosToIncludeFractionOfPopulation(ds, range_max);
					if (j >= 0) {
						max = xData.getDouble(j);
					}
					j = getPosToIncludeFractionOfPopulation(ds, range_min);
					if (j >= 0) {
						min = xData.getDouble(j);
					}
					double offset = -min;
					double scale = 255.0 / (max - min);
					getLiveViewNDProc().setScale(scale);
					getLiveViewNDProc().setOffset(offset);
					getLiveViewNDProc().setEnableOffsetScale(1);
				} catch (Exception e) {
					logger.error("Error autoscaling the live view", e);
				}

			}

			private int getPosToIncludeFractionOfPopulation(DoubleDataset yData, Double fractionOfPopulationToInclude) {
				Double sum = (Double) yData.sum();
				double[] data = yData.getData();
				int popIncluded = 0;
				int j = 0;
				double popRequired = sum * fractionOfPopulationToInclude;
				while (popIncluded < popRequired && j < data.length) {
					popIncluded += data[j];
					if (popIncluded < popRequired)
						j++;
				}
				return Math.min(j, data.length - 1);
			}
		});

		if (histogramWasComputed != 0) {
			getImageNDStats().setComputeHistogram(1);
		}
		if (!wasEnabled) {
			getImageNDStats().getPluginBase().enableCallbacks();
		}

	}

	@Override
	public double getHistogramMinCallbackTime() {
		return histogramMinCallbackTime;
	}

	public void setHistogramMinCallbackTime(double histogramMinCallbackTime) {
		this.histogramMinCallbackTime = histogramMinCallbackTime;
	}

	@Override
	public ImageDescriptor getTwoDarrayViewImageDescriptor() {
		return null;
	}

	@Override
	public ImageDescriptor getLiveViewImageDescriptor() {
		return null;
	}

	@Override
	public ImageDescriptor getHistogramViewImageDescriptor() {
		return null;
	}

/*	@Override
	public int getFfmpegImageInOffsetX() throws Exception {
		return 0;
	}

	@Override
	public int getFfmpegImageInOffsetY() throws Exception {
		return 0;
	}
*/
	@Override
	public int getFfmpegImageInHeight() throws Exception {
		return 0;
	}

	@Override
	public int getFfmpegImageInWidth() throws Exception {
		return 0;
	}

	@Override
	public double getImageMax() throws Exception {
		return NDUtils.getImageMaxFromDataType(getAdBase().getDataType_RBV());
	}

	@Override
	public int getFfmpegImageOutWidthMax() {
		return ffmpegImageOutWidthMax;
	}

	@Override
	public int getFfmpegImageOutHeightMax() {
		return ffmpegImageOutHeightMax;
	}

	public void setFfmpegImageOutWidthMax(int ffmpegImageOutWidthMax) {
		this.ffmpegImageOutWidthMax = ffmpegImageOutWidthMax;
	}

	public void setFfmpegImageOutHeightMax(int ffmpegImageOutHeightMax) {
		this.ffmpegImageOutHeightMax = ffmpegImageOutHeightMax;
	}


	
	
	@Override
	public int getImageHistSize() throws Exception {
		return imageHistSize;
	}

	public void setImageHistSize(int imageHistSize) {
		this.imageHistSize = imageHistSize;
	}
	
	@Override
	public double getImageMin() {
		return imageMin;
	}

	public void setImageMin(double imageMin) {
		this.imageMin = imageMin;
	}

	boolean connectToPlotServer = false;

	@Override
	public boolean isConnectToPlotServer() {
		return connectToPlotServer;
	}

	public void setConnectToPlotServer(boolean connectToPlotServer) {
		this.connectToPlotServer = connectToPlotServer;
	}	

	@Override
	public String getImageNDArrayPortInput() throws Exception {
		return imageNDArrayPortInput != null ? imageNDArrayPortInput : getAdBase().getPortName_RBV();
	}

	public void setImageNDArrayPortInput(String imageNDArrayPortInput) {
		this.imageNDArrayPortInput = imageNDArrayPortInput;
	}

	@Override
	public double getArrayMinCallbackTime() {
		return arrayMinCallbackTime;
	}

	public void setArrayMinCallbackTime(double arrayMinCallbackTime) {
		this.arrayMinCallbackTime = arrayMinCallbackTime;
	}

	String detectorName="undefined";

	@Override
	public String getDetectorName() {
		return detectorName;
	}
	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	
}