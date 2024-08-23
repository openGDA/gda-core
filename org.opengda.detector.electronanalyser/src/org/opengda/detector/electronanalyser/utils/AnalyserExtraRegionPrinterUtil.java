package org.opengda.detector.electronanalyser.utils;

import static gda.jython.InterfaceProvider.getTerminalPrinter;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.scan.ScanDataPoint;
import uk.ac.diamond.daq.concurrent.Async;

/*
 * Helper class to print region progress to user in between scanDataPoints.
 *
 * @author Oli Wenman
 */
public class AnalyserExtraRegionPrinterUtil {

	private static final Logger logger = LoggerFactory.getLogger(AnalyserExtraRegionPrinterUtil.class);
	static final String PLACEHOLDER_DETECTOR_VALUE = "-";

	private List<Scannable> scannables = new ArrayList<>();
	private List<String> scanFieldNames;
	private List<String> regionNames;
	private int regionIndex = 0;
	private String[] regionOutputFormats;
	private boolean printHeaders = true;
	private boolean ready = false;

	public AnalyserExtraRegionPrinterUtil() {
	}

	public void atScanStart(Scannable deviceToIgnore) {
		atScanStart(getDeviceFieldNames(deviceToIgnore).toList(), deviceToIgnore.getOutputFormat());
	}

	public void atScanStart(List<String> regionNames, String[] regionOutputFormats) {
		if(ready) {
			logger.error("Extra region printing is already ready. Either atScanStart called multiple times or atScanEnd not called.");
		}
		if(regionNames.size() != regionOutputFormats.length) {
			throw new IllegalArgumentException(
				String.format("regionNames.length = %i, regionOutputFormats.length = %i. They must be the same length!", regionNames.size(), regionOutputFormats.length)
			);
		}
		this.regionIndex = 0;
		this.ready = true;
		this.regionNames = regionNames;
		this.regionOutputFormats = regionOutputFormats;
		logger.debug("atScanStart - Setup extra printing.");
	}

	private void print(String message) {
		getTerminalPrinter().print(message);
	}

	public void printExtraRegionProgress(double[] regionPositionValues) {
		logger.debug("Printing region progress with position values of {}", Arrays.toString(regionPositionValues));
		if(!ready) {
			logger.error("atScanStart() was not called!");
			return;
		}
		if(regionPositionValues.length != regionNames.size() || regionPositionValues.length != regionOutputFormats.length) {
			logger.error(
				"regionPositionValues.length = {}, regionNames.length = {}, regionOutputFormats.length = {}. They should all be the same length!",
				regionPositionValues.length, regionNames.size(), regionOutputFormats
			);
			return;
		}
		if(regionIndex == regionPositionValues.length - 1) {
			logger.debug("Skipping printing as this is the final region point. Letting framework handle it.");
			regionIndex = 0;
			return;
		}
		//Do not let scan fail due to printing issue.
		try {
			boolean cachedPrintHeaders = printHeaders;
			if (printHeaders) {
				LocalProperties.set(LocalProperties.GDA_SERVER_SCAN_PRINT_SUPPRESS_HEADER, true);
				scannables = setupScannableInformation();
				printHeaders = false;
			}
			String[] positions = getAllPositions(regionPositionValues);
			printFormattedValues(positions, cachedPrintHeaders);
			this.regionIndex++;
		}
		catch (Exception e){
			LocalProperties.set(LocalProperties.GDA_SERVER_SCAN_PRINT_SUPPRESS_HEADER, false);
			logger.error("Failed to print extra region progress correctly.", e);
		}
	}

	private List<Scannable> setupScannableInformation() {
		List<Scannable> scannableList = getObjectsFromNames(
			InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getScannableNames()
		);
		List<Scannable> detectorList = getObjectsFromNames(
			InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getDetectorNames()
		);
		scanFieldNames  = getScannableFieldNames(scannableList, detectorList);
		logger.debug("scanFieldNames: {}", scanFieldNames);
		return scannableList;
	}

	private List<Scannable> getObjectsFromNames(String[] names) {
		List<Scannable> scannableObjectList = new ArrayList<>();
		for (String name : names) {
			Scannable scannableObject = findScannableObject(name);
			if (scannableObject != null){
				scannableObjectList.add(scannableObject);
			}
		}
		return scannableObjectList;
	}

	private Scannable findScannableObject(String name) {
		final Object jythonObject = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(name);
		if(jythonObject instanceof Scannable jythonScannable) {
			return jythonScannable;
		}
		Object finderObject = Finder.find(name);
		if (finderObject instanceof Scannable finderScannable) {
			return finderScannable;
		}
		logger.warn("Unable to find object {}", name);
		return null;
	}

	private List<String> getScannableFieldNames(List<Scannable> scannables, List<Scannable> detectors) {
		return Stream.of(scannables, detectors)
			.flatMap(List::stream)
			.map(this::getDeviceFieldNames)
			.flatMap(Function.identity())
			.collect(toCollection(ArrayList::new));
	}

	private Stream<String> getDeviceFieldNames(Scannable device) {
		if (device instanceof Detector det) {
			if (!ArrayUtils.isEmpty(device.getExtraNames())) {
				return Arrays.stream(det.getExtraNames());
			} else {
				return Stream.of(det.getName());
			}
		}
		return Stream.concat(Arrays.stream(device.getInputNames()), Arrays.stream(device.getExtraNames()));
	}

	private void printFormattedValues(String[] values, boolean printHeader) {
		// work out the lengths of the header string and the lengths of each element from the toString method
		// and pad each to adjust
		String headerString = String.join(ScanDataPoint.DELIMITER, scanFieldNames).trim();
		String dataString   = String.join(ScanDataPoint.DELIMITER, values);

		String[] headerElements = headerString.split(ScanDataPoint.DELIMITER);
		String[] dataElements = dataString.split(ScanDataPoint.DELIMITER);

		for (int i = 0; i < headerElements.length; i++) {
			int headerLength = headerElements[i].length();
			int dataLength = dataElements[i].length();

			int maxLength = dataLength > headerLength ? dataLength : headerLength;
			String format = "%" + maxLength + "s";

			headerElements[i] = String.format(format, headerElements[i].trim());
			dataElements[i] = String.format(format, dataElements[i].trim());
		}
		if (printHeader) {
			print(String.join(ScanDataPoint.DELIMITER, headerElements));
		}
		print(String.join(ScanDataPoint.DELIMITER, dataElements));
	}

	public String[] getAllPositions(double[] regionPositionValues) {
		String[] positions = getBaseStringPositions(regionPositionValues);
		try {
			for (Object scannableObject : scannables) {
				if (scannableObject instanceof ScannableBase scannable) {
					List<String> header = getDeviceFieldNames(scannable).toList();
					String[] scannableFormat = scannable.getOutputFormat();
					String[] scannablePositions = ScannableUtils.getFormattedCurrentPositionArray(getScannableCurrentPosition(scannable), scannableFormat.length, scannableFormat);

					int subArrayStartIndex = Collections.indexOfSubList(scanFieldNames, header);
					int sunArrayEndIndex = subArrayStartIndex + scannablePositions.length;

					for (int i = subArrayStartIndex ; i < sunArrayEndIndex; i++) {
						positions[i] = scannablePositions[i - subArrayStartIndex];
					}
				}
			}
		}
		catch (DeviceException e) {
			logger.warn("Error getting scannable posiiton", e);
		}
		return positions;
	}

	private Object getScannableCurrentPosition(Scannable scannable) {
		Object position = null;
		int timeoutSeconds = 5;
		Future<?> future = Async.submit(() -> {
			try {
				return scannable.getPosition();
			} catch (DeviceException e) {
				logger.error("Error occured getting position for {}", scannable.getName(), e);
				return null;
			}
		});
		//Get the scannable position but within a time limit. If takes too long, log timeout and cancel thread
		try {
			if(future != null) position = future.get(timeoutSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException | TimeoutException e) {
			logger.error("Getting position for {} was timed out after {} seconds", scannable.getName(), timeoutSeconds, e);
		}
		finally {
			if (future != null) future.cancel(true);
		}
		return position;
	}

	private String[] getBaseStringPositions(double[] regionPositionValues) {
		String[] positions = new String[scanFieldNames.size()];

		//Add space adding for any small length input/extra names
		Arrays.fill(positions, "     " + PLACEHOLDER_DETECTOR_VALUE);

		for (int i = 0; i < regionNames.size() ; i++) {

			String regionName = regionNames.get(i);
			int allValuesIndex = scanFieldNames.indexOf(regionName);

			if (i <= regionIndex) {
				positions[allValuesIndex] = String.format(regionOutputFormats[i], regionPositionValues[i]);
			}
			else {
				int length = calculateRegionOutputStringLength(regionOutputFormats[i -1]);
				String placeholder = " ".repeat(length);
				placeholder = placeholder.substring(0, placeholder.length() -1) + "-";

				//Calculate string length via output format
				positions[allValuesIndex] = placeholder;
			}
		}
		return positions;
	}

	private int calculateRegionOutputStringLength(String output) {
		Pattern regex = Pattern.compile("(\\d+(?:\\.\\d+)?)");
		Matcher matcher = regex.matcher(output);
		matcher.find();
		//Assumes scientific notation.
		//e.g 1.2345E+06, +2 is for number on left side of decimal and decimal
		//                +4 is for E+XY
		return 2 + Integer.valueOf(matcher.group()) + 4;
	}

	public void atScanEnd() {
		logger.debug("atScanEnd called.");
		LocalProperties.set(LocalProperties.GDA_SERVER_SCAN_PRINT_SUPPRESS_HEADER, false);
		printHeaders = true;
		ready = false;
		regionIndex = 0;
		scannables.clear();
	}
}