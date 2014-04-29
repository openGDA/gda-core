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

import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.XmapDetector;
import gda.device.detector.BufferedDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.Xspress2BufferedDetector;
import gda.device.detector.countertimer.TfgScaler;
import gda.device.detector.xmap.XmapBufferedDetector;
import gda.device.detector.xspress.XspressDetector;
import gda.scan.IScanDataPoint;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.beans.vortex.RegionOfInterest;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.client.microfocus.util.MicroFocusNexusPlotter;
import uk.ac.gda.client.microfocus.views.scan.MapPlotView;

public class MicroFocusWriterExtender extends DataWriterExtenderBase {

	protected int numberOfXPoints = 0;
	protected int numberOfYPoints = 0;
	protected double firstX = 0.0;
	protected double firstY = 0.0;
	protected double xStepSize;
	protected double yStepSize;
	protected Detector detectors[];
	protected String selectedElement = "";
	protected int selectedChannel = 0;
	protected IRichBean detectorBean;
	// FIXME this warning is showing that how this list is used is not clear - needs a redesign
	@SuppressWarnings("rawtypes")
	protected List[] elementRois;
	protected Logger logger = LoggerFactory.getLogger(MicroFocusWriterExtender.class);
	protected int selectedElementIndex = -1;
	protected String detectorBeanFileName;
	protected int numberOfSubDetectors;
	protected String detectorName;
	protected Hashtable<String, Integer> roiNameMap;
	protected StringBuffer roiHeader = new StringBuffer("row  column");
	protected Writer writer;
	protected String[] roiNames;
	protected double[][] scalerValuesCache; // [buffer array][element]
	protected double[][][] detectorValuesCache; // [det chan][element][buffer array]
	protected double[] xValues;
	protected double[] yValues;
	protected double zValue;
	protected double energyValue;
	protected int plottedSoFar = -1;
	protected int yIndex = -1;
	protected IScanDataPoint lastDataPoint = null;
	protected long lastTimePlotWasUpdate = 0;
	protected HDF5Loader hdf5Loader;
	protected ILazyDataset lazyDataset;
	protected int spectrumLength = 4096;
	protected boolean normalise = false;
	protected String normaliseElement = "I0";
	protected int normaliseElementIndex = -1;
	protected double normaliseValue = 1.0;
	protected boolean active = false;

	public MicroFocusWriterExtender(int xPoints, int yPoints, double xStepSize, double yStepSize,
			String detectorFileName, Detector[] detectors2) {
		this.numberOfXPoints = xPoints;
		this.numberOfYPoints = yPoints;
		this.xStepSize = xStepSize;
		this.yStepSize = yStepSize;
		this.yIndex = 0;
		logger.info("The number of X and Y points are " + this.numberOfXPoints + " " + this.numberOfYPoints);
		setDetectorBeanFileName(detectorFileName);
		setDetectors(detectors2);
		getWindowsfromBean();
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String[] getRoiNames() {
		return roiNames;
	}

	public void setRoiNames(String[] roiNames) {
		this.roiNames = roiNames;
	}

	private void getWindowsfromBean() {
		try {
			detectorBean = BeansFactory.getBeanObject(null, detectorBeanFileName);
		} catch (Exception e) {
			logger.error("Error loading bean from " + detectorBeanFileName, e);
		}
		numberOfSubDetectors = getNumberOfEnabledMCA();

		for (Detector detector : detectors) {
			if (detector instanceof XspressDetector) {
				XspressDetector xspress = (XspressDetector) detector;
				detectorName = xspress.getName();
				roiNames = new String[((XspressParameters) detectorBean).getDetector(0).getRegionList().size()];
				for (int roiIndex = 0; roiIndex < roiNames.length; roiIndex++) {
					roiNames[roiIndex] = ((XspressParameters) detectorBean).getDetector(0).getRegionList()
							.get(roiIndex).getRoiName();
				}
				fillRoiNames();
				elementRois = new List[numberOfSubDetectors];
				for (int detectorNo = 0; detectorNo < numberOfSubDetectors; detectorNo++)
					elementRois[detectorNo] = ((XspressParameters) detectorBean).getDetector(detectorNo)
							.getRegionList();
			} else if (detector instanceof XmapDetector) {
				XmapDetector xspress = (XmapDetector) detector;
				detectorName = xspress.getName();
				roiNames = new String[((VortexParameters) detectorBean).getDetector(0).getRegionList().size()];
				for (int roiIndex = 0; roiIndex < roiNames.length; roiIndex++) {
					roiNames[roiIndex] = ((VortexParameters) detectorBean).getDetector(0).getRegionList().get(roiIndex)
							.getRoiName();
				}
				fillRoiNames();
				elementRois = new List[numberOfSubDetectors];
				for (int detectorNo = 0; detectorNo < numberOfSubDetectors; detectorNo++)
					elementRois[detectorNo] = ((VortexParameters) detectorBean).getDetector(detectorNo).getRegionList();
			}
		}
	}

	private int getNumberOfEnabledMCA() {
		if (detectorBean instanceof XspressParameters) {
			XspressParameters xspressParameters = (XspressParameters) detectorBean;
			int numFilteredDetectors = 0;
			for (int element = 0; element < xspressParameters.getDetectorList().size(); element++)
				if (!xspressParameters.getDetectorList().get(element).isExcluded())
					numFilteredDetectors++;
			return numFilteredDetectors;

		}
		// assume it must be vortex then
		VortexParameters vortexParameters = (VortexParameters) detectorBean;
		int numFilteredDetectors = 0;
		for (int element = 0; element < vortexParameters.getDetectorList().size(); element++)
			if (!vortexParameters.getDetectorList().get(element).isExcluded())
				numFilteredDetectors++;
		return numFilteredDetectors;
	}

	private void fillRoiNames() {
		if (null == roiNameMap)
			roiNameMap = new Hashtable<String, Integer>();
		int roiIndex = 0;
		for (String roi : roiNames) {
			roiNameMap.put(roi, roiIndex);
			roiIndex++;
		}
	}

	private void setDetectors(Detector[] xspress) {
		this.detectors = xspress;
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		Double[] xy = dataPoint.getPositionsAsDoubles();
		int totalPoints = 0;
		Vector<Detector> detFromDP = dataPoint.getDetectors();
		if (lastDataPoint == null || dataPoint.getCurrentPointNumber() == 0) {
			// this is the first point in the scan
			totalPoints = deriveXYArrays(xy);
			deriveROIHeader(detFromDP);
			// create the rgb file
			createRgbFile((new StringTokenizer(dataPoint.getCurrentFilename(), ".")).nextToken());
			// load the dataset for reading the spectrum
			hdf5Loader = new HDF5Loader(dataPoint.getCurrentFilename());
		}

		if ((lastDataPoint == null || (!lastDataPoint.equals(dataPoint) && lastDataPoint.getCurrentFilename().equals(
				dataPoint.getCurrentFilename())))
				&& (xValues != null || yValues != null)) {
			
			Hashtable<String, Double> rgbLineData = new Hashtable<String, Double>(roiNames.length);
			NXDetectorData d = null;
			Vector<Object> detectorsData = dataPoint.getDetectorData();
			if (detectorsData.size() != detFromDP.size()){
				logger.error("Inconsistency in ScanDataPoint. There are " + detFromDP.size() + " detectors and " + detectorsData.size() + " data parts");
			}
			
			for (int detDataIndex = 0; detDataIndex < detectorsData.size(); detDataIndex++) {
				Object dataObj = detectorsData.get(detDataIndex);
				Detector detector = detFromDP.get(detDataIndex);
				// if the ionchambers
				if (dataObj instanceof double[] && ( detector instanceof TfgScaler)) {
					double[] scalerData = (double[]) dataObj;
					if (scalerData.length != detector.getExtraNames().length) {
						logger.error("Inconsistency in ScanDataPoint. There are " + scalerData.length
								+ " parts in the data and " + detector.getExtraNames().length + " extra names for "
								+ detector.getName());
					}
					
					for (int index = 0; index < scalerData.length; index++) {
						String columnName = detector.getExtraNames()[index];
						double data = scalerData[index];
						rgbLineData.put(columnName, data);
					}
					
					if (normaliseElementIndex != -1 && normaliseElementIndex < scalerData.length){
						normaliseValue = scalerData[normaliseElementIndex];
					}
					scalerValuesCache[dataPoint.getCurrentPointNumber()] = scalerData;
					
				} else if (dataObj instanceof NXDetectorData) {  // then this must be a fluorescence detector
					// make the roiHeader once
					if (dataPoint.getCurrentPointNumber() == 0) {
						for (String s : roiNames) {
							// add the fluo det columns to the rbg header
							if (dataPoint.getCurrentPointNumber() == 0) {
								roiHeader.append("  " + s);
							}
						}
					}
					// add rbgData to the array to start off the counts
					for (String s : roiNames) {
						// add the fluo det columns to the rbg header
						rgbLineData.put(s, 0.0);
					}
					
					if (isXspressScan()
							&& ((detector instanceof XspressDetector) || detector instanceof BufferedDetector)) {
						d = ((NXDetectorData) dataObj);
						double[][] dataArray = (double[][]) d.getData(detectorName, "MCAs", "SDS").getBuffer();
						// assuming all detector elements have the same number of roi
						spectrumLength = dataArray[0].length;
						for (int i = 0; i < numberOfSubDetectors; i++) {
							@SuppressWarnings("unchecked")
							List<XspressROI> roiList = elementRois[i];
							for (XspressROI roi : roiList) {
								String key = roi.getRoiName();
								if (ArrayUtils.contains(roiNames, key)) {
									if (detectorValuesCache[i][roiNameMap.get(key)] == null)
										detectorValuesCache[i][roiNameMap.get(key)] = new double[totalPoints];
									double windowTotal = getWindowedData(dataArray[i], roi.getRoiStart(),
											roi.getRoiEnd());
									double rgbElementSum = rgbLineData.get(key);
									rgbLineData.put(key, rgbElementSum + windowTotal);
									detectorValuesCache[i][roiNameMap.get(key)][dataPoint.getCurrentPointNumber()] = windowTotal;
								}
							}
						}

					} else if (isXmapScan()
							&& ((detector instanceof XmapDetector) || (detector instanceof BufferedDetector))) {
						d = ((NXDetectorData) dataObj);
						double wholeDataArray[][] = new double[numberOfSubDetectors][];
						Object wholeDataArrayObject;
						try {
							wholeDataArrayObject = d.getData(detectorName, "fullSpectrum", "SDS").getBuffer();
						} catch (Exception e) {
							// elements spectrum is not available as twod array
							wholeDataArrayObject = null;
						}
						for (int j = 0; j < numberOfSubDetectors; j++) {
							Object singleElementSpectrum = null;
							if (wholeDataArrayObject == null) {
								singleElementSpectrum = (d
										.getData(detectorName, "Element" + j + "_fullSpectrum", "SDS").getBuffer());
							} else {
								if (wholeDataArrayObject instanceof int[][]) {
									singleElementSpectrum = ((int[][]) wholeDataArrayObject)[j];
								} else if (wholeDataArrayObject instanceof double[][])
									singleElementSpectrum = ((double[][]) wholeDataArrayObject)[j];
								else if (wholeDataArrayObject instanceof short[][])
									singleElementSpectrum = ((short[][]) wholeDataArrayObject)[j];
							}
							if (singleElementSpectrum instanceof int[]) {
								wholeDataArray[j] = new double[((int[]) singleElementSpectrum).length];
								// do an array copy to convert from int to double
								for (int arIndex = 0; arIndex < ((int[]) singleElementSpectrum).length; arIndex++)
									wholeDataArray[j][arIndex] = ((int[]) singleElementSpectrum)[arIndex];
							}
							if (singleElementSpectrum instanceof short[]) {
								wholeDataArray[j] = new double[((short[]) singleElementSpectrum).length];
								// do an array copy to convert from int to double
								for (int arIndex = 0; arIndex < ((short[]) singleElementSpectrum).length; arIndex++)
									wholeDataArray[j][arIndex] = ((short[]) singleElementSpectrum)[arIndex];
							}

							else if (singleElementSpectrum instanceof double[]) {
								wholeDataArray[j] = (double[]) singleElementSpectrum;
							}
							spectrumLength = wholeDataArray[j].length;
							@SuppressWarnings("unchecked")
							// FIXME needs a redesign to prevent this unchecked warning
							List<RegionOfInterest> roiList = elementRois[j];
							// calculating window total manually instead of using xmap ROIs
							for (RegionOfInterest roi : roiList) {
								String key = roi.getRoiName();
								if (ArrayUtils.contains(roiNames, key)) {
									if (detectorValuesCache[j][roiNameMap.get(key)] == null)
										detectorValuesCache[j][roiNameMap.get(key)] = new double[totalPoints];
									double windowTotal = getWindowedData(wholeDataArray[j], roi.getRoiStart(),
											roi.getRoiEnd());
									double rgbElementSum = rgbLineData.get(key);
									rgbLineData.put(key, rgbElementSum + windowTotal);
									detectorValuesCache[j][roiNameMap.get(key)][dataPoint.getCurrentPointNumber()] = windowTotal;
								}
							}
						}
					}
//					logger.debug("The y value is " + xy[0]);
//					logger.debug("the x value is " + xy[1]);
				}
			}

			// FIXME into new method after merge to master
			// so what goes into RGB files? An average or a single detector channel or what?
			StringBuffer rgbLine = new StringBuffer();
			int xindex = dataPoint.getCurrentPointNumber() % numberOfXPoints;
			int yindex = dataPoint.getCurrentPointNumber() / numberOfXPoints;
			rgbLine.append(yindex + " " + xindex + " ");
			String[] rbgColumns = roiHeader.toString().split(" +");
			for (String s : rbgColumns) {
				Double val = rgbLineData.get(s);
				if (val != null){
					DecimalFormat df = new DecimalFormat("#");
					rgbLine.append(df.format(val));
					rgbLine.append(" ");
				}
			}
			int lineNumber = dataPoint.getCurrentPointNumber();
			if (lineNumber == 0) {
				addToRgbFile(lineNumber, roiHeader.toString());
			}
			addToRgbFile(lineNumber, rgbLine.toString().trim());

			normaliseDetectorValues(dataPoint);

			// keep a track of which line we are doing
			if (((dataPoint.getCurrentPointNumber() + 1) / (yIndex + 1)) == numberOfXPoints) {
				yIndex++;
			}

			plottedSoFar = dataPoint.getCurrentPointNumber();

			// only update plot every 500ms
			long now = System.currentTimeMillis();
			if (now - lastTimePlotWasUpdate > 500) {
				displayPlot(selectedElement, selectedChannel);
				lastTimePlotWasUpdate = now;
			}
			lastDataPoint = dataPoint;
		}
	}

	/*
	 * if normalise is requested plot the normalised value in map and save normalised value internally but write raw
	 * value to rgb files
	 * 
	 * @param dataPoint
	 */
	private void normaliseDetectorValues(IScanDataPoint dataPoint) {
		if (isNormalise() && normaliseValue > 0.0) {//
			for (int detChan = 0; detChan < numberOfSubDetectors; detChan++) {
				for (int i = 0; i < detectorValuesCache.length; i++) {
					detectorValuesCache[detChan][i][dataPoint.getCurrentPointNumber()] = detectorValuesCache[detChan][i][dataPoint
							.getCurrentPointNumber()] / normaliseValue;
				}
			}
		}
	}

	private void deriveROIHeader(Vector<Detector> detFromDP) {
		// get the list of names from Scalers
		roiHeader = new StringBuffer("row  column");
		for (Detector det : detFromDP) {
			if (det instanceof TfgScaler) {
				
				String[] s = det.getExtraNames();
				for (int i = 0; i < s.length; i++) {
					if (s[i].equals(selectedElement)) {
						selectedElementIndex = i;
						break;
					}
					selectedElementIndex = -1;
				}
				if (isNormalise()) {
					for (int i = 0; i < s.length; i++) {
						if (s[i].equals(normaliseElement)) {
							normaliseElementIndex = i;
							break;
						}
						normaliseElementIndex = -1;
					}
				}
				// Build the rgb file column names with scaler names
				int headerCounter = 0;
				roiHeader.append("  ");
				for (String h : s) {
					roiHeader.append(h);
					if (++headerCounter != s.length)
						roiHeader.append("  ");
				}
			}
		}
	}

	private int deriveXYArrays(Double[] xy) {
		firstX = xy[1];
		firstY = xy[0];
		int totalPoints = numberOfXPoints * numberOfYPoints;
		scalerValuesCache = null;
		scalerValuesCache = new double[totalPoints][];
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
		AbstractDataset xDataset = AbstractDataset.array(xValues);
		AbstractDataset yDataset = AbstractDataset.array(yValues);
		SDAPlotter.imagePlot(MapPlotView.NAME, xDataset, yDataset, dataSet);
	}

	public void plotSpectrum(int detNo, int x, int y) throws Exception {
		// always make sure the spectrum asked to plot is less than the last data point to prevent crashing of the
		// server

		int point = y * numberOfXPoints + x;
		int current = 0;
		if (lastDataPoint != null){
			current = lastDataPoint.getCurrentPointNumber();
		}

		if (current >= point) {
			IDataset slice = null;
			DataHolder dataHolder = hdf5Loader.loadFile();
			if (isXspressScan()) {
				lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorName + "/MCAs");
				slice = lazyDataset.getSlice(new int[] { y, x, detNo, 0 }, new int[] { y + 1, x + 1, detNo + 1,
						spectrumLength }, new int[] { 1, 1, 1, 1 });
			} else if (isXmapScan()) {
				lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorName + "/fullSpectrum");
				if (lazyDataset == null) {
					lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorName + "/Element" + detNo
							+ "_fullSpectrum");
					slice = lazyDataset.getSlice(new int[] { y, x, 0 }, new int[] { y + 1, x + 1, spectrumLength },
							new int[] { 1, 1, 1 });
				} else {
					slice = lazyDataset.getSlice(new int[] { y, x, detNo, 0 }, new int[] { y + 1, x + 1, detNo + 1,
							spectrumLength }, new int[] { 1, 1, 1, 1 });
				}
			}

			if (slice != null) {
				IDataset sqSlice = slice.squeeze();
				try {
					SDAPlotter.plot(MicroFocusNexusPlotter.MCA_PLOTTER, sqSlice);
				} catch (DeviceException e) {
					logger.error("Unable to plot the spectrum for " + x + " " + y, e);
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

		DoubleDataset dataSetToDisplay = new DoubleDataset(numberOfYPoints, numberOfXPoints);
		dataSetToDisplay.fill(Double.NaN);

		// the selected element is a scaler value displaying the map for the scaler
		if (selectedElementIndex == -1) {
			int scalerIndex = selectedElement.equalsIgnoreCase("i0") ? 1 : 2;
			for (int i = 0; i <= plottedSoFar; i++) {
				dataSetToDisplay.setAbs(i,scalerValuesCache[i][scalerIndex]);
//				dataSetToDisplay.set(scalerValuesCache[i][scalerIndex], i / numberOfXPoints, i % numberOfXPoints);
			}
			plotImage(dataSetToDisplay);
			return;
		} else if (isXspressScan() || isXmapScan()) {
			for (int point = 0; point <= plottedSoFar; point++) {
				dataSetToDisplay.set(detectorValuesCache[detectorChannel][selectedElementIndex][point], point
						/ numberOfXPoints, point % numberOfXPoints);
			}
			plotImage(dataSetToDisplay);
			return;
		}
		throw new Exception("unable to determine the detector for the selected element ");
	}

	private void setSelectedElementIndexFromString(String selectedElement) {
		Integer elementKey = roiNameMap.get(selectedElement);
		if (elementKey == null)
			elementKey = -1;
		selectedElementIndex = elementKey;
		return;
	}

	private boolean isXspressScan() {
		for (Detector det : detectors) {
			if (det instanceof XspressDetector || det instanceof Xspress2BufferedDetector)
				return true;
		}
		return false;
	}

	private boolean isXmapScan() {
		for (Detector det : detectors) {
			if (det instanceof XmapDetector || det instanceof XmapBufferedDetector)
				return true;
		}
		return false;
	}

	protected void addToRgbFile(@SuppressWarnings("unused") int lineNumber, String string) throws IOException {
		active = true;
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
			logger.error("unable to create the rgb file " + string);
		}
	}

	private double getWindowedData(double[] data, int windowStart, int windowEnd) {
		double total = 0.0;
		for (int i = windowStart; i <= windowEnd; i++) {
			total = total + data[i];
		}
		return total;
	}

	public void closeWriter() throws Throwable {
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

	public void setDetectorBeanFileName(String detectorBeanFileName) {
		this.detectorBeanFileName = detectorBeanFileName;
	}

	public String getDetectorBeanFileName() {
		return detectorBeanFileName;
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
