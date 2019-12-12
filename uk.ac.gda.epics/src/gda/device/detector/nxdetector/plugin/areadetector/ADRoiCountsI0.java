/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.CounterTimer;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NXDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.countertimer.BufferedScaler;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdetector.BufferedNXDetector;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;
import gda.device.detector.nxdetector.roi.RectangularROI;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

/**
 * Calculate FFI0 for an NXDetector (medipix) using ROI counts for FF and ionchambers for I0.
 * (getI0() method is same as used in TfgFFoverI0).
 * @since 3/2/2016
 * @author Iain Hall
 */
public class ADRoiCountsI0 extends NullNXPlugin {
	private static final Logger logger = LoggerFactory.getLogger(ADRoiCountsI0.class);

	private NXDetector nxDetector = null;
	private CounterTimer ct = null;

	private BufferedNXDetector bufferedNxDetector;
	private BufferedScaler bufferedScaler;
	private boolean useBufferedScaler;
	private int frameCount;

	private int i0Channel = -1;
	private String roiCountsName;
	private int scalerChannelToUse;
	private String currentStreamName;

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		roiCountsName = getRoiCountsName();
		useBufferedScaler = getScanUsesBufferedDetector();
		if (useBufferedScaler) {
			scalerChannelToUse = getChannelToUse(bufferedScaler);
			frameCount = 0;
		} else {
			scalerChannelToUse = getChannelToUse(ct);
		}
		currentStreamName = getCurrentStreamName();
		logger.info("Using channel {} for scaler value. Using buffered scaler {}?, Stream name = {}", scalerChannelToUse, useBufferedScaler, currentStreamName);
	}

	public int getI0_channel() {
		return i0Channel;
	}

	public void setI0_channel(int i0_channel) {
		this.i0Channel = i0_channel;
	}

	@Override
	public String getName() {
		return "roi_counts_over_i0";
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList(currentStreamName);
	}

	/**
	 * Return true if bufferedNxDetector is used in the currently running scan; (i.e. it is a continuous scan)
	 * @return
	 */
	private boolean getScanUsesBufferedDetector() {
		ScanInformation scanInfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		if (scanInfo != null && bufferedNxDetector != null && bufferedScaler != null) {
			List<String> detectorNames = Arrays.asList(scanInfo.getDetectorNames());
			return detectorNames.contains(bufferedNxDetector.getName()) && detectorNames.contains(bufferedScaler.getName());
		}
		return false;
	}

	private int getChannelToUse(Scannable scn) {
		int channelToUse = i0Channel;
		if (channelToUse < 0) {
			logger.debug("Getting channel number of first non-time value from {}", scn.getName());
			List<String> extraNames = Arrays.asList(scn.getExtraNames());
			String matchingName = extraNames.stream()
					.filter(name -> !name.equalsIgnoreCase("time"))
					.findFirst()
					.orElse("");

			channelToUse = extraNames.indexOf(matchingName);
		}
		return channelToUse;
	}

	private String getCurrentStreamName() {
		String channelName = "";
		if (useBufferedScaler && bufferedScaler != null) {
			channelName = bufferedScaler.getExtraNames()[scalerChannelToUse];
		} else {
			channelName = ct.getExtraNames()[scalerChannelToUse];
		}
		return "FF"+channelName;
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList("%f");
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		return Arrays.asList((NXDetectorDataAppender) new ADTotalCountsROIAppender());
	}

	/**
	 * Lookup ROI from ADRoiStatsPair plugin on the nxDetector
	 * and return the name of the 'ROI total counts' field.
	 * ROI total counts name = {@code <ROI name>_total}
	 * @return name of 'ROI total counts' field name
	 * @throws DeviceException if no ADRoiStatsPair is found or no ROI has been set.
	 */
	private String getRoiCountsName() throws DeviceException {
		// Find the ADRoiStatsPair plugin
		ADRoiStatsPair statsPair = nxDetector.getPluginList()
				.stream()
				.filter( plugin -> plugin instanceof ADRoiStatsPair)
				.map( plugin -> (ADRoiStatsPair)plugin)
				.findFirst()
				.orElseThrow(() ->
					new DeviceException("Cannot get name of ROI - no ADRoiStatsPair plugin found on " + nxDetector.getName()));

		// Try to get the ROI from the plugin
		logger.debug("Using ADRoiStatsPair plugin {}", statsPair.getName());
		RectangularROI<Integer> roi = statsPair.getRoi();
		if (roi == null) {
			String message = "Cannot get name of ROI - no ROI has been set on the detector";
			InterfaceProvider.getTerminalPrinter().print(message);
			throw new DeviceException(message);
		}

		// Make 'ROI total counts' field name : strip the spaces from the ROI name and append '_total' :
		String name = roi.getName().replace(" ", "").concat("_total");
		logger.debug("ROI name = {}, ROI total counts name = {}", roi.getName(), name);
		return name;
	}

	class ADTotalCountsROIAppender implements NXDetectorDataAppender {

		@Override
		public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {

			double roiCounts = 0;
			double scalerCounts = 0;

			try {
				List<String> extraNames = Arrays.asList(data.getExtraNames());
				int index = extraNames.indexOf(roiCountsName);
				if (index == -1) {
					String message = "Cannot calculate "+currentStreamName+" - no value for FF was produced by "+detectorName+
									 ". Has a ROI been set on the detector?";
					InterfaceProvider.getTerminalPrinter().print(message);
					throw new DeviceException(message);
				}
				Double[] values = data.getDoubleVals();
				// Get first value from detector data - should be total ROI counts
				if (index < values.length) {
					roiCounts = values[index];
				}

				if (useBufferedScaler) {
					scalerCounts = getScalerCounts(bufferedScaler, frameCount);
					frameCount++;
				} else {
					scalerCounts = getScalerCounts(ct, 0);
				}

			} catch (Exception e) {
				throw new DeviceException("Exception reading out roi count from NXDetectorData structure.", e);
			}

			double ffi0 = 1.0;
			if (scalerCounts > 0) {
				ffi0 = roiCounts / scalerCounts;
			}
			logger.info("FF = {}, scaler counts = {}", roiCounts, scalerCounts);

			// Make double appender and it use it for the actual appending
			NXDetectorDataDoubleAppender doubleAppender = new NXDetectorDataDoubleAppender(getInputStreamNames(), Arrays.asList(ffi0));
			doubleAppender.appendTo(data, detectorName);
		}

		/**
		 * Return scaler counts value from buffered scaler
		 * @param frameNumber frame number to read
		 * @return scaler counts value from buffered scaler (channel 'scalerChannelToUse')
		 * @throws DeviceException
		 */
		private Double getScalerCounts(Detector scaler, int frameNumber) throws DeviceException {
			if (scaler == null) {
				logger.warn("CounterTimer has not been set - using 0 for scaler counts");
				return 0.0;
			}
			if (scalerChannelToUse < 0) {
				logger.warn("Channel for scaler counts is < 0 - using 0 for scaler counts");
				return 0.0;
			}

			if (scaler instanceof BufferedScaler) {
				logger.debug("Reading channel {} frame {} from {} for scaler counts", scalerChannelToUse, frameNumber, scaler.getName());
				BufferedScaler bufScaler = (BufferedScaler) scaler;
				return getScalerData(bufScaler.readFrames(frameNumber, frameNumber)[0]);
			} else {
				logger.debug("Using channel {} from {} for scaler counts ", scalerChannelToUse, scaler.getName());
				return getScalerData(scaler.readout());
			}
		}

		/**
		 * Extract channel of scaler data from the Object returned by scaler readout function.
		 * @param scalerReadout
		 * @return scaler counts from channel {@link #scalerChannelToUse}.
		 */
		private double getScalerData(Object scalerReadout) {
			double[] arr = (double[]) scalerReadout;
			return arr[scalerChannelToUse];
		}
	}

	public NXDetector getNxDetector() {
		return nxDetector;
	}

	public void setNxDetector(NXDetector nxDetector) {
		this.nxDetector = nxDetector;
	}

	public CounterTimer getCounterTimer() {
		return ct;
	}

	public void setCounterTimer(CounterTimer i0CounterTimer) {
		this.ct = i0CounterTimer;
	}

	public BufferedNXDetector getBufferedNxDetector() {
		return bufferedNxDetector;
	}

	public void setBufferedNxDetector(BufferedNXDetector bufferedNxDetector) {
		this.bufferedNxDetector = bufferedNxDetector;
	}

	public BufferedScaler getBufferedScaler() {
		return bufferedScaler;
	}

	public void setBufferedScaler(BufferedScaler bufferedScaler) {
		this.bufferedScaler = bufferedScaler;
	}
}
