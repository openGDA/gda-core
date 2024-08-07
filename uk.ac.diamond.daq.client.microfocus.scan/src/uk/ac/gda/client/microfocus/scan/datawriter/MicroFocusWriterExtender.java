/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.scan.datawriter;

import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.countertimer.TfgScaler;
import gda.device.detector.xspress.Xspress2BufferedDetector;
import gda.scan.IScanDataPoint;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.gda.beans.exafs.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.beans.xspress.XspressDetector;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.common.exception.GDAException;
import uk.ac.gda.devices.detector.xspress3.Xspress3;
import uk.ac.gda.devices.detector.xspress3.Xspress3BufferedDetector;
import uk.ac.gda.util.beans.xml.XMLRichBean;

/**
 * Messy class - about the only thing I did not have time to refactor.
 * <p>
 * However this plays a vital role: it caches map data during data collection. It is this data which is sent to the
 * MapPlot when the user changes the selection in the Elements view. This works by their being a reference to this
 * object in the GDA Jython environment and the GUI makes calls to this object via the Jython. Not an ideal design...
 * <p>
 * This also writes the RBG files which are ASCII versions of the map data. These files are *vital* for users to be able
 * to analyse their data using third-party tools. These files are almost more important than the other data files as far
 * as I18 is concerned.
 * <p>
 * There should be an analogous service to this for I08 and I14: some way of caching the data from the latest map and
 * then user clicks on the UI telling such a cache to replot. However I would move this functionality into a distributed
 * object separate from the DataWriterExtender mechanism. However an RBG data writer extender would still be required.
 * <p>
 * Richard Woolliscroft March 2015
 */
public class MicroFocusWriterExtender extends DataWriterExtenderBase {

	private static final DecimalFormat IME_FORMAT = new DecimalFormat("#.###");
	private static final DecimalFormat FORMAT = new DecimalFormat("#");

	protected int numberOfXPoints = 0;
	protected int numberOfYPoints = 0;
	protected double firstX = 0.0;
	protected double firstY = 0.0;
	protected double xStepSize;
	protected double yStepSize;
	protected Detector[] detectors;
	protected String selectedElement = "";
	protected int selectedChannel = 0;
	protected XMLRichBean detectorBean;
	// this warning is showing that how this list is used is not clear - needs a redesign
	@SuppressWarnings("rawtypes")
	protected List[] elementRois;
	protected Logger logger = LoggerFactory.getLogger(MicroFocusWriterExtender.class);
	protected int selectedElementIndex = -1;
	protected int numberOfSubDetectors;
	protected String detectorName;
	protected Map<String, Integer> roiNameMap;
	private Writer writer;
	protected String[] roiNames;
	protected HashMap<String, double[]> scalerValuesCache; // now: <channel name>[buffer array] (to handle multiple
															// TfgScalers) was:[buffer array][element]
	protected String[] rgbColumnNames = new String[] { "row", "column" };
	protected double[][][] detectorValuesCache; // [det chan][element][buffer array]
	protected double[] xValues;
	protected double[] yValues;
	protected double zValue;
	protected double energyValue;
	protected int plottedSoFar = -1;
	protected int yIndex = -1;
	protected IScanDataPoint lastDataPoint = null;
	protected long lastTimePlotWasUpdate = 0;
	protected ILazyDataset lazyDataset;
	protected int spectrumLength = 4096;
	protected boolean normalise = false;
	protected String normaliseElement = "I0";
	protected int normaliseElementIndex = -1;
	protected double normaliseValue = 1.0;
	private String currentHDF5filename;

	public MicroFocusWriterExtender(int xPoints, int yPoints, double xStepSize, double yStepSize,
			XMLRichBean detectorBean, Detector[] detectors2) {
		this.numberOfXPoints = xPoints;
		this.numberOfYPoints = yPoints;
		this.xStepSize = xStepSize;
		this.yStepSize = yStepSize;
		this.yIndex = 0;
		this.detectorBean = detectorBean;
		setDetectors(detectors2);
		getWindowsfromBean();
	}

	public String[] getRoiNames() {
		return roiNames;
	}

	private void getWindowsfromBean() {
		numberOfSubDetectors = getNumberOfEnabledMCA();

		for (Detector detector : detectors) {
			if (detector instanceof XspressDetector || detector instanceof Xspress2BufferedDetector) {
				detectorName = detector.getName();
				DetectorElement detElement = ((XspressParameters) detectorBean).getDetector(0);
				roiNames = new String[detElement.getRegionList().size()];
				for (int roiIndex = 0; roiIndex < roiNames.length; roiIndex++) {
					roiNames[roiIndex] = detElement.getRegionList().get(roiIndex).getRoiName();
				}
				fillRoiNames();
				elementRois = new List[numberOfSubDetectors];
				for (int detectorNo = 0; detectorNo < numberOfSubDetectors; detectorNo++) {
					elementRois[detectorNo] = ((XspressParameters) detectorBean).getDetector(detectorNo)
							.getRegionList();
				}
			} else if (detector instanceof Xspress3 xspress) {
				detectorName = xspress.getName();
				roiNames = new String[((Xspress3Parameters) detectorBean).getDetector(0).getRegionList().size()];
				for (int roiIndex = 0; roiIndex < roiNames.length; roiIndex++) {
					roiNames[roiIndex] = ((Xspress3Parameters) detectorBean).getDetector(0).getRegionList()
							.get(roiIndex).getRoiName();
				}
				fillRoiNames();
				elementRois = new List[numberOfSubDetectors];
				for (int detectorNo = 0; detectorNo < numberOfSubDetectors; detectorNo++) {
					elementRois[detectorNo] = ((Xspress3Parameters) detectorBean).getDetector(detectorNo)
							.getRegionList();
				}
			}
		}
	}

	private int getNumberOfEnabledMCA() {
		if (detectorBean instanceof XspressParameters xspressParameters) {
			int numFilteredDetectors = 0;
			for (int element = 0; element < xspressParameters.getDetectorList().size(); element++)
				if (!xspressParameters.getDetectorList().get(element).isExcluded())
					numFilteredDetectors++;
			return numFilteredDetectors;

		}
		// assume it must be vortex then
		Xspress3Parameters vortexParameters = (Xspress3Parameters) detectorBean;
		int numFilteredDetectors = 0;
		for (int element = 0; element < vortexParameters.getDetectorList().size(); element++)
			if (!vortexParameters.getDetectorList().get(element).isExcluded())
				numFilteredDetectors++;
		return numFilteredDetectors;
	}

	protected void fillRoiNames() {
		if (roiNameMap == null) {
			roiNameMap = new HashMap<>();
		}

		int roiIndex = 0;
		for (String roi : roiNames) {
			roiNameMap.put(roi, roiIndex);
			roiIndex++;
		}
	}

	private void setDetectors(Detector[] xspress) {
		this.detectors = xspress;
	}

	protected int getCurrentSDPNumber(IScanDataPoint dataPoint) {
		return dataPoint.getCurrentPointNumber();
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		Double[] xy = dataPoint.getPositionsAsDoubles();
		int totalPoints = 0;
		List<Detector> detFromDP = dataPoint.getDetectors();

		int currentPointNumber = getCurrentSDPNumber(dataPoint);
		if (currentPointNumber == 0 && lastDataPoint == null) {
			// this is the first point in the scan
			totalPoints = deriveXYArrays(xy, detFromDP);
			deriveROIHeader(detFromDP);
			// create the rgb file
			createRgbFile((new StringTokenizer(dataPoint.getCurrentFilename(), ".")).nextToken());
			// load the dataset for reading the spectrum
			currentHDF5filename = dataPoint.getCurrentFilename();
		}

		if ((lastDataPoint == null || (!lastDataPoint.equals(dataPoint) && lastDataPoint.getCurrentFilename().equals(
				dataPoint.getCurrentFilename())))
				&& (xValues != null || yValues != null)) {

			final Map<String, Double> rgbLineData = new HashMap<>(roiNames.length);

			List<Object> detectorsData = dataPoint.getDetectorData();
			if (detectorsData.size() != detFromDP.size()) {
				logger.error("Inconsistency in ScanDataPoint. There are {} detectors and {} data parts",
						detFromDP.size(), detectorsData.size());
			}

			for (int detDataIndex = 0; detDataIndex < detectorsData.size(); detDataIndex++) {

				Object dataObj = detectorsData.get(detDataIndex);
				Detector detector = detFromDP.get(detDataIndex);

				// if the ionchambers
				if (dataObj instanceof double[] scalerData && (detector instanceof TfgScaler)) {
					addScalerDataToCache(dataPoint, rgbLineData, detector, scalerData);

				} else if (dataObj instanceof NXDetectorData) { // then this must be a fluorescence detector

					if (isXspressScan()
							&& ((detector instanceof XspressDetector) || detector instanceof BufferedDetector)) {
						addXspress2DataToCache(dataPoint, totalPoints, currentPointNumber, rgbLineData, dataObj);

					} else if (isXspress3Scan()) {
						addXspress3DataToCache(dataPoint, totalPoints, currentPointNumber, rgbLineData, dataObj);
					}
				}
			}

			writeRGBLine(dataPoint, rgbLineData);

			normaliseCachedDetectorValues(dataPoint);

			// keep a track of which line we are doing
			if (((currentPointNumber + 1) / (yIndex + 1)) == numberOfXPoints) {
				yIndex++;
			}

			plottedSoFar = dataPoint.getCurrentPointNumber();
			lastDataPoint = dataPoint;

			// only update plot every 2000ms
			long now = System.currentTimeMillis();
			if (now - lastTimePlotWasUpdate > 2000) {
				displayPlot(selectedElement, selectedChannel);
				lastTimePlotWasUpdate = now;
			}
		}
	}

	private void addScalerDataToCache(IScanDataPoint dataPoint, Map<String, Double> rgbLineData,
			Detector detector, double[] scalerData) {
		if (scalerData.length != detector.getExtraNames().length) {
			logger.error("Inconsistency in ScanDataPoint. There are {} parts in the data and {} extra names for {}",
					scalerData.length, detector.getExtraNames(), detector.getName());
		}

		for (int index = 0; index < scalerData.length; index++) {
			String columnName = detector.getExtraNames()[index];
			double data = scalerData[index];
			rgbLineData.put(columnName, data);
		}

		if (normaliseElementIndex != -1 && normaliseElementIndex < scalerData.length) {
			normaliseValue = scalerData[normaliseElementIndex];
		}

		String[] elementNames = detector.getExtraNames();

		for (int channel = 0; channel < elementNames.length; channel++) {
			String elementName = elementNames[channel];
			double value = scalerData[channel];
			scalerValuesCache.get(elementName)[dataPoint.getCurrentPointNumber()] = value;
		}
	}

	private void addXspress2DataToCache(IScanDataPoint dataPoint, int totalPoints, int currentPointNumber,
			Map<String, Double> rgbLineData, Object dataObj) throws IOException {

		addFluoToRGBLineData(dataPoint, rgbLineData);

		NXDetectorData d = ((NXDetectorData) dataObj);
		double[][] dataArray = (double[][]) d.getData(detectorName, "MCAs", "SDS").getBuffer();

		// assuming all detector elements have the same number of roi
		spectrumLength = dataArray[0].length;
		for (int i = 0; i < numberOfSubDetectors; i++) {
			@SuppressWarnings("unchecked")
			List<DetectorROI> roiList = elementRois[i];
			for (DetectorROI roi : roiList) {
				String key = roi.getRoiName();
				if (ArrayUtils.contains(roiNames, key)) {
					if (detectorValuesCache[i][roiNameMap.get(key)] == null)
						detectorValuesCache[i][roiNameMap.get(key)] = new double[totalPoints];
					double windowTotal = getWindowedData(dataArray[i], roi.getRoiStart(), roi.getRoiEnd());
					double rgbElementSum = rgbLineData.get(key);
					rgbLineData.put(key, rgbElementSum + windowTotal);
					detectorValuesCache[i][roiNameMap.get(key)][currentPointNumber] = windowTotal;
				}
			}
		}
	}

	private void addXspress3DataToCache(IScanDataPoint dataPoint, int totalPoints, int currentPointNumber,
			Map<String, Double> rgbLineData, Object dataObj) throws IOException {

		addFluoToRGBLineData(dataPoint, rgbLineData);

		NXDetectorData d = ((NXDetectorData) dataObj);
		for (int i = 0; i < numberOfSubDetectors; i++) {
			@SuppressWarnings("unchecked")
			List<DetectorROI> roiList = elementRois[i];
			for (DetectorROI roi : roiList) {
				String key = roi.getRoiName();
				if (ArrayUtils.contains(roiNames, key)) {
					if (detectorValuesCache[i][roiNameMap.get(key)] == null)
						detectorValuesCache[i][roiNameMap.get(key)] = new double[totalPoints];
					NexusGroupData groupData = d.getData(detectorName, key, "SDS");
					// in the simulation we get Doubles but live we return doubles. Fix the sim
					double[] dataArray = (double[]) groupData.getBuffer();
					double windowTotal = dataArray[i];
					double rgbElementSum = rgbLineData.get(key);
					rgbLineData.put(key, rgbElementSum + windowTotal);
					detectorValuesCache[i][roiNameMap.get(key)][currentPointNumber] = windowTotal;
				}
			}
		}
	}

	private void writeRGBLine(IScanDataPoint dataPoint, Map<String, Double> rgbLineData) throws IOException {
		StringBuilder rgbLine = new StringBuilder();
		int xindex = dataPoint.getCurrentPointNumber() % numberOfXPoints;
		int yindex = dataPoint.getCurrentPointNumber() / numberOfXPoints;
		rgbLine.append(yindex + " " + xindex + " ");
		for (String s : rgbColumnNames) {
			Double val = rgbLineData.get(s);
			if (val != null) {
				DecimalFormat df = s.contains("ime") ? IME_FORMAT : FORMAT;
				rgbLine.append(df.format(val));
				rgbLine.append(" ");
			}
		}
		addToRgbFile(0, rgbLine.toString().trim());
	}

	private void addFluoToRGBLineData(IScanDataPoint dataPoint, Map<String, Double> rgbLineData)
			throws IOException {
		// make the roiHeader once
		if (dataPoint.getCurrentPointNumber() == 0) {
			rgbColumnNames = ArrayUtils.addAll(rgbColumnNames, roiNames);
			addToRgbFile(0, String.join("  ", rgbColumnNames));
		}

		// add rbgData to the array to start off the counts
		rgbLineData.putAll(Arrays.stream(roiNames).collect(toMap(n -> n, n -> 0.0)));
	}

	/*
	 * if normalise is requested plot the normalised value in map and save normalised value internally but write raw
	 * value to rgb files
	 * @param dataPoint
	 */
	private void normaliseCachedDetectorValues(IScanDataPoint dataPoint) {
		if (isNormalise() && normaliseValue > 0.0) {//
			for (int detChan = 0; detChan < numberOfSubDetectors; detChan++) {
				for (int i = 0; i < detectorValuesCache.length; i++) {
					detectorValuesCache[detChan][i][dataPoint.getCurrentPointNumber()] = detectorValuesCache[detChan][i][dataPoint
							.getCurrentPointNumber()] / normaliseValue;
				}
			}
		}
	}

	private void deriveROIHeader(List<Detector> detFromDP) {
		for (Detector det : detFromDP) {
			if (det instanceof TfgScaler) {
				final String[] extraNames = det.getExtraNames();

				if (isNormalise()) {
					normaliseElementIndex = ArrayUtils.indexOf(extraNames, normaliseElement);
				}
				rgbColumnNames = ArrayUtils.addAll(rgbColumnNames, extraNames);
			}
		}
	}

	private int deriveXYArrays(Double[] xy, List<Detector> detFromDP) {
		firstX = xy[1];
		firstY = xy[0];
		int totalPoints = numberOfXPoints * numberOfYPoints;

		scalerValuesCache = new HashMap<>();
		for (Detector detector : detFromDP) {
			if (detector instanceof TfgScaler) {
				scalerValuesCache.putAll(Arrays.stream(detector.getExtraNames())
						.collect(toMap(name -> name, name -> new double[totalPoints])));
			}
		}

		if (roiNameMap != null)
			detectorValuesCache = new double[numberOfSubDetectors][roiNameMap.size()][totalPoints];
		xValues = new double[numberOfXPoints];
		yValues = new double[numberOfYPoints];

		// pre-populate the xValues and yValues arrays to enables plotting
		// these arrays will be gradually overwritten by actual data as it is collected
		for (int xIndex = 0; xIndex < numberOfXPoints; xIndex++) {
			xValues[xIndex] = firstX + (xIndex * xStepSize);
		}
		for (int yIndex = 0; yIndex < numberOfYPoints; yIndex++) {
			yValues[yIndex] = firstY + (yIndex * yStepSize);
		}

		return totalPoints;
	}

	private void plotImage(DoubleDataset dataSet) throws Exception {
		// create these at every point using the latest arrays, as these will be updated as the map progresses
		Dataset xDataset = DatasetFactory.createFromObject(xValues);
		Dataset yDataset = DatasetFactory.createFromObject(yValues);
		// SDAPlotter.imagePlot(MapPlotView.NAME, xDataset, yDataset, dataSet); TODO: Find a better way to get the name that doesn't introduce dependency
		SDAPlotter.imagePlot("MapPlot", xDataset, yDataset, dataSet);
	}

	/**
	 * This is mainly for unit testing
	 *
	 * @param detectorChannel
	 * @param elementIndex
	 * @return double[] the map for the given channel and element
	 */
	public double[] getData(int detectorChannel, int elementIndex) {
		return detectorValuesCache[detectorChannel][elementIndex];
	}

	public void plotSpectrum(int detNo, int x, int y) throws Exception {
		// always make sure the spectrum asked to plot is less than the last data point to prevent crashing of the
		// server

		int point = y * numberOfXPoints + x;
		int current = 0;
		if (lastDataPoint != null) {
			current = lastDataPoint.getCurrentPointNumber();
		}

		if (current >= point) {
			IDataset slice = null;
			// reopen every time to ensure latest data is picked up - seems to be new behaviour in NAPI
			DataHolder dataHolder = new HDF5Loader(currentHDF5filename).loadFile();
			if (isXspressScan()) {
				lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorName + "/MCAs");
				slice = lazyDataset.getSlice(new int[] { y, x, detNo, 0 }, new int[] { y + 1, x + 1, detNo + 1,
						spectrumLength }, new int[] { 1, 1, 1, 1 });
			} else if (isXspress3Scan()) {
				try {
					lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorName + "/MCAs");
					slice = lazyDataset.getSlice(new int[] { y, x, detNo, 0 }, new int[] { y + 1, x + 1, detNo + 1,
							spectrumLength }, new int[] { 1, 1, 1, 1 });
				} catch (Exception ignore) {
					// absorb the exception here as if the MCA does not exist then it will probably be because the row
					// has not completed so the MCA are not available yet.
					slice = DatasetFactory.zeros(4096);
				}
			}

			if (slice != null) {
				IDataset sqSlice = slice.squeeze();
				try {
					// SDAPlotter.plot(MicroFocusNexusPlotter.MCA_PLOTTER, sqSlice); TODO: Find a better way to get the name that doesn't introduce dependency
					SDAPlotter.plot("MCA Plot", sqSlice);
				} catch (DeviceException e) {
					logger.error("Unable to plot the spectrum for {} {}",x, y, e);
					throw new Exception("Unable to plot the spectrum for " + x + " " + y, e);
				}
			} else
				throw new Exception("Unable to plot the spectrum for " + x + " " + y);
		}
	}

	public Double[] getXYPositions(int xIndex, int yIndex) {
		return new Double[] { xValues[xIndex], yValues[yIndex] };
	}

	public void displayPlot(String selectedElement, Integer detectorChannel) throws Exception {
		this.setSelectedElement(selectedElement);
		this.setSelectedChannel(detectorChannel);

		DoubleDataset dataSetToDisplay = DatasetFactory.zeros(DoubleDataset.class, numberOfYPoints, numberOfXPoints);
		dataSetToDisplay.fill(Double.NaN);

		// nothing selected yet
		if (selectedElement.isEmpty()) {
			return;
		}
		// the selected element is a scaler value displaying the map for the scaler
		else if (selectedElementIndex == -1 && scalerValuesCache.containsKey(selectedElement)) {
			double[] selectedScalerChannelData = scalerValuesCache.get(selectedElement);
			for (int i = 0; i <= plottedSoFar; i++) {
				dataSetToDisplay.setAbs(i, selectedScalerChannelData[i]);
			}
			plotImage(dataSetToDisplay);
			return;
		} else if (isXspressScan() || isXspress3Scan()) {
			for (int point = 0; point <= plottedSoFar; point++) {
				dataSetToDisplay.set(detectorValuesCache[detectorChannel][selectedElementIndex][point], point
						/ numberOfXPoints, point % numberOfXPoints);
			}
			plotImage(dataSetToDisplay);
			return;
		}
		throw new GDAException("unable to determine the detector for the selected element ");
	}

	private void setSelectedElementIndexFromString(String selectedElement) {
		final Integer elementKey = roiNameMap.get(selectedElement);
		selectedElementIndex = elementKey == null ? -1 : elementKey;
	}

	protected boolean isXspressScan() {
		for (Detector det : detectors) {
			if (det instanceof XspressDetector || det instanceof Xspress2BufferedDetector)
				return true;
		}
		return false;
	}

	protected boolean isXspress3Scan() {
		for (Detector det : detectors) {
			if (det instanceof Xspress3 || det instanceof Xspress3BufferedDetector)
				return true;
		}
		return false;
	}

	protected void addToRgbFile(@SuppressWarnings("unused") int unused, String string) throws IOException {
		if (writer != null) {
			writer.write(string + "\n");
			writer.flush();
		}
	}

	protected void createRgbFile(String string) {
		if (!string.contains("."))
			string = string + ".rgb";
		try {
			File file = new File(string.substring(0, string.lastIndexOf("/")));
			boolean exists = file.exists();
			if (!exists) {
				file.mkdir();
			}
			writer = new FileWriter(new File(string));
		} catch (IOException e) {
			logger.error("unable to create the rgb file {}", string);
		}
	}

	private double getWindowedData(double[] data, int windowStart, int windowEnd) {
		double total = 0.0;
		for (int i = windowStart; i <= windowEnd; i++) {
			total = total + data[i];
		}
		return total;
	}

	public void closeWriter() throws IOException {
		if (writer != null)
			writer.close();
	}

	public void setSelectedElement(String selectedElement) {
		this.selectedElement = selectedElement;
		setSelectedElementIndexFromString(selectedElement);
	}

	public String getSelectedElement() {
		return selectedElement;
	}

	public int getSelectedChannel() {
		return selectedChannel;
	}

	public void setSelectedChannel(int selectedChannel) {
		this.selectedChannel = selectedChannel;
	}

	public void setZValue(double zValue) {
		this.zValue = zValue;
	}

	public double getZValue() {
		return zValue;
	}

	public void setEnergyValue(double energyValue) {
		this.energyValue = energyValue;
	}

	public double getEnergyValue() {
		return energyValue;
	}

	public void setNormalise(boolean normalise) {
		this.normalise = normalise;
	}

	public boolean isNormalise() {
		return normalise;
	}

	public void setNormaliseElement(String normaliseElement) {
		this.normaliseElement = normaliseElement;
	}

	public String getNormaliseElement() {
		return normaliseElement;
	}
}
