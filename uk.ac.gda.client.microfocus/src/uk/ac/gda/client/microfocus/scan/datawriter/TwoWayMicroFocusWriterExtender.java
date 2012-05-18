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

package uk.ac.gda.client.microfocus.scan.datawriter;
import gda.analysis.RCPPlotter;
import gda.analysis.io.ScanFileHolderException;
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
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.vortex.RegionOfInterest;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;

public class TwoWayMicroFocusWriterExtender extends DataWriterExtenderBase {
	private String plotName;
	private int numberOfXPoints = 0;
	private int numberOfYPoints = 0;
	private AbstractDataset dataSet;	
	private double xStepSize;
	private double yStepSize;
	private Detector detectors[];
	private int windowStart = 67;
	private int windowEnd = 1200;
	private String selectedElement = "";
	private Object detectorBean;
	@SuppressWarnings("rawtypes")
	private List[] elementRois;
	private Logger logger = LoggerFactory.getLogger(TwoWayMicroFocusWriterExtender.class);
	private int selectedElementIndex = -1;
	private String detectorBeanFileName;
	private int numberOfSubDetectors;
	private String detectorName;
	private Hashtable<String, Double> roiTable;
	private Hashtable<String, Integer> roiNameMap;
	private StringBuffer roiHeader = new StringBuffer("row  column");
	private FileWriter writer;
	private String[] roiNames;
	private double[][] scalerValues;
	private double[][] detectorValues;
	private double[] xValues;
	private double[] yValues;
	private double zValue;
	private double energyValue;
	private int plottedSoFar = -1;
	private int rowsPlottedSoFar = -1;
	private IScanDataPoint lastDataPoint =null;
	private int plotUpdateFrequency = 5;
	private double minValue = Double.MAX_VALUE;
	private HDF5Loader hdf5Loader ;
	private DataHolder dataHolder ;
	private ILazyDataset  lazyDataset;
	private int spectrumLength = 4096;
	private boolean normalise = false;
	private String normaliseElement = "I0";
	private int normaliseElementIndex = -1;
	private double normaliseValue = 1.0;
	private double lastFilledValue;

	public AbstractDataset getDataSet() {
		return dataSet;
	}

	public String[] getRoiNames() {
		return roiNames;
	}

	public void setRoiNames(String[] roiNames) {
		this.roiNames = roiNames;
	}

	public TwoWayMicroFocusWriterExtender(int xPoints, int yPoints, double xStepSize, double yStepSize) {
		this.numberOfXPoints = xPoints;
		this.numberOfYPoints = yPoints;
		this.setxStepSize(xStepSize);
		this.setyStepSize(yStepSize);
		minValue = Double.MAX_VALUE;
		createDataSet();

	}

	public void getWindowsfromBean() {
		try {
			detectorBean = BeansFactory.getBean(new File(detectorBeanFileName));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for(Detector detector : detectors){
		if (detector instanceof XspressDetector)
			try {
				XspressDetector xspress = (XspressDetector) detector;
				detectorName = xspress.getName();
				numberOfSubDetectors = xspress.getNumberOfDetectors();
				elementRois = new List[numberOfSubDetectors];
				for (int detectorNo = 0; detectorNo < numberOfSubDetectors; detectorNo++)
					elementRois[detectorNo] = ((XspressParameters) detectorBean).getDetector(detectorNo)
							.getRegionList();
			} catch (DeviceException e) {
				logger.error("Error getting windows from the bean file ", e);
			}
		else if (detector instanceof XmapDetector)
			try {
				XmapDetector xspress = (XmapDetector) detector;
				detectorName = xspress.getName();
				numberOfSubDetectors = xspress.getNumberOfMca();
				elementRois = new List[numberOfSubDetectors];
				for (int detectorNo = 0; detectorNo < numberOfSubDetectors; detectorNo++)
					elementRois[detectorNo] = ((VortexParameters) detectorBean).getDetector(detectorNo).getRegionList();
			} catch (DeviceException e) {
				logger.error("Error getting windows from the bean file ", e);
			}
		}

	}

	private void fillRoiNames() {
		if(roiNames == null)
			return;
		if(null == roiNameMap)
			roiNameMap = new Hashtable<String, Integer>();
		int roiIndex =0;
		for (String roi : roiNames) {
			roiNameMap.put(roi,roiIndex);
			roiIndex++;
		}
		
	}

	public void setWindows(int low, int high) {
		this.windowStart = low;
		this.windowEnd = high;
	}

	public void setDetectors(Detector[] xspress) {
		this.detectors = xspress;
	}

	private void createDataSet() {
		dataSet = AbstractDataset.array(new double[numberOfYPoints][numberOfXPoints]);
		lastFilledValue = -1.0;
		dataSet.fill(lastFilledValue);
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	@SuppressWarnings("static-access")
	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		updateDataSetFromSDP(dataPoint );
		if(plottedSoFar % plotUpdateFrequency == 0 || (plottedSoFar + 1 ) == (numberOfXPoints * numberOfYPoints))
			RCPPlotter.imagePlot(plotName,dataSet);
		lastDataPoint = dataPoint;
		
	}
	/**
	 * This method is made public only for testing . 
	 * @param plottedSoFar
	 */
	public void setPlottedSoFar(int plottedSoFar) {
		this.plottedSoFar = plottedSoFar;
		this.rowsPlottedSoFar = plottedSoFar / numberOfXPoints + 1;
	}

	public void updateDataSetFromSDP(IScanDataPoint dataPoint )throws Exception
	{
		Double[] xy = dataPoint.getPositionsAsDoubles();
		int fillDecrement =0;
		int totalPoints =0;
		Vector<Detector> detFromDP = dataPoint.getDetectors();
		if (dataPoint.getCurrentPointNumber() == 0 && lastDataPoint == null) {
			// this is the first point in the scan
			fillRoiNames();
			totalPoints = numberOfXPoints * numberOfYPoints;
			scalerValues = new double[totalPoints][];
			detectorValues = new double[roiNameMap.size()][];
			xValues = new double[totalPoints];
			yValues = new double[totalPoints];
						//get the list of names from Scaler
			for(Detector det : detFromDP){
				if(det instanceof TfgScaler)
				{
					String[] s = det.getExtraNames();
					for (int i = 0; i < s.length; i++) {
						if (s[i].equals(selectedElement)) {
							selectedElementIndex = i;
							break;
						}
						selectedElementIndex = -1;
					}
					if(isNormalise())
					{
						for (int i = 0; i < s.length; i++) {
							if (s[i].equals(normaliseElement)) {
								normaliseElementIndex  = i;
								break;
							}
							normaliseElementIndex = -1;
						}
					}
					//Build the rgb file column names with scaler names
					roiHeader = new StringBuffer("row  column  ");
					int headerCounter=0;
					for (String h : s) {
						roiHeader.append(h);
						if(++headerCounter != s.length)
							roiHeader.append("  ");
					}
				}
			}	
			
			// create the rgb file
			createRgbFile((new StringTokenizer(dataPoint.getCurrentFilename(), ".")).nextToken());
			//load the dataset for reading the spectrum
			hdf5Loader = new HDF5Loader(dataPoint.getCurrentFilename());
			
			
		}

		if(lastDataPoint == null || (!lastDataPoint.equals(dataPoint) && lastDataPoint.getCurrentFilename().equals(dataPoint.getCurrentFilename()))){
			int[]xyIndex = getXYIndex(dataPoint.getCurrentPointNumber(),xy[1]);
			int correctedDataPointNumber = xyIndex[1]*numberOfXPoints+xyIndex[0];
			xValues[correctedDataPointNumber] = xy[1];
			yValues[correctedDataPointNumber] = xy[0];
			
		double value = 0;
		double windowTotal = 0.0;

		StringBuffer rgbLine = new StringBuffer();
		rgbLine.append(xyIndex[1] + " " + xyIndex[0] + " ");
		NXDetectorData d = null;
		Vector<Object> detectorsData = dataPoint.getDetectorData();
		for (int detDataIndex =0 ; detDataIndex < detectorsData.size() ; detDataIndex++){
			Object obj= detectorsData.get(detDataIndex);
			if (obj instanceof double[] && (detFromDP.get(detDataIndex) instanceof TfgScaler)) {
				double[] scalerData = (double[]) obj;
				if (selectedElementIndex == -1){
					String[] s = detFromDP.get(detDataIndex).getExtraNames();
					for (int i = 0; i < s.length; i++) {
						if (s[i].equals(selectedElement)) {
							selectedElementIndex = i;
							break;
						}
						selectedElementIndex = -1;
					}
				}
				if (selectedElementIndex != -1)
					value = scalerData[selectedElementIndex];
				if(normaliseElementIndex != -1)
					normaliseValue = scalerData[normaliseElementIndex];
				for (double i : scalerData) {
					rgbLine.append(i);
					rgbLine.append(" ");
				}
				scalerValues[correctedDataPointNumber] = scalerData;
				logger.info("The rgb Line with scaler values is " + rgbLine.toString());

			} else if (obj instanceof NXDetectorData) {
				if (roiTable == null || roiTable.size() == 0) {
					roiTable = new Hashtable<String, Double>(roiNames.length);
					for (String s : roiNames) {
						roiTable.put(s, 0.0);
						if(dataPoint.getCurrentPointNumber() == 0)
							roiHeader.append("  " + s);
					}
					
				}
				if (isXspressScan() && ((detFromDP.get(detDataIndex) instanceof XspressDetector) ||(detFromDP.get(detDataIndex) instanceof BufferedDetector) )) {
					d = ((NXDetectorData) obj);
					//double[][] dataArray = new double[numberOfSubDetectors][];
					double[][] dataArray =(double[][])d.getData(detectorName, "MCAs" , "SDS").getBuffer();
					//assuming all detector elements have the same number of roi
					spectrumLength = dataArray[0].length;
					for (int i = 0; i < numberOfSubDetectors; i++) {
						//data = d.getData(detectorName, detectorName + "_element_" + i, "SDS");
						//dataArray[i] = ((double[]) ((NexusGroupData) data).getBuffer());
						@SuppressWarnings("unchecked")
						List<XspressROI> roiList = elementRois[i];
						for (XspressROI roi : roiList) {
							String key = roi.getRoiName();
							if (roiTable.containsKey(key)) {
								this.setWindows(roi.getRegionStart(), roi.getRegionEnd());
								if(detectorValues[roiNameMap.get(key)] == null)
									detectorValues[roiNameMap.get(key)] = new double[totalPoints];
								windowTotal = getWindowedData(dataArray[i]);
								double db = roiTable.get(roi.getRoiName());
								detectorValues[roiNameMap.get(key)][correctedDataPointNumber ] = db + windowTotal;
								roiTable.put(key, db + windowTotal);
								if (roi.getRoiName().equals(selectedElement)) {
									value += windowTotal;
								}
							}
						}
					}

					
					logger.debug("the value for the selected emenet " + selectedElement + " is " + value);
					
				} 
				else if (isXmapScan()&& ((detFromDP.get(detDataIndex) instanceof XmapDetector)||(detFromDP.get(detDataIndex) instanceof BufferedDetector)) ) {
					d = ((NXDetectorData) obj);
					double wholeDataArray[][] = new double[numberOfSubDetectors][]; 
					Object wholeDataArrayObject ;
					try {
						wholeDataArrayObject= d.getData(detectorName, "fullSpectrum", "SDS").getBuffer();
					} catch (Exception e) {
						//elements spectrum is not available as twod array
						wholeDataArrayObject = null;
					}
					for (int j = 0; j < numberOfSubDetectors; j++) {
						Object singleElementSpectrum = null;
						if(wholeDataArrayObject == null){
							singleElementSpectrum = ( d.getData(detectorName,"Element"+j+"_fullSpectrum", "SDS").getBuffer());
						}
						else
						{
							if(wholeDataArrayObject instanceof int[][])
							{
							singleElementSpectrum =((int[][]) wholeDataArrayObject)[j];
							}
							else if(wholeDataArrayObject instanceof double[][])
								singleElementSpectrum = ((double[][]) wholeDataArrayObject)[j];
							else if(wholeDataArrayObject instanceof short[][])
								singleElementSpectrum = ((short[][]) wholeDataArrayObject)[j];
						}
						if(singleElementSpectrum instanceof int[]){
							wholeDataArray[j] = new double[((int[])singleElementSpectrum).length];
							//do an array copy to convert from int to double
							for(int arIndex =0 ; arIndex < ((int[])singleElementSpectrum).length; arIndex++)
								wholeDataArray[j][arIndex] = ((int[])singleElementSpectrum)[arIndex];
						}
						if(singleElementSpectrum instanceof short[]){
							wholeDataArray[j] = new double[((short[])singleElementSpectrum).length];
							//do an array copy to convert from int to double
							for(int arIndex =0 ; arIndex < ((short[])singleElementSpectrum).length; arIndex++)
								wholeDataArray[j][arIndex] = ((short[])singleElementSpectrum)[arIndex];
						}

						else if(singleElementSpectrum instanceof double[]){
							wholeDataArray[j] = (double[]) singleElementSpectrum;
						}
						spectrumLength = wholeDataArray[j].length;
						@SuppressWarnings("unchecked")
						List<RegionOfInterest> roiList = elementRois[j];
						//calculating window total manually instead of using xmap ROIs
						for (RegionOfInterest roi : roiList) {
							String key = roi.getRoiName();
							if (roiTable.containsKey(key)) {
								this.setWindows(roi.getRoiStart(), roi.getRoiEnd());
								if(detectorValues[roiNameMap.get(key)] == null)
									detectorValues[roiNameMap.get(key)] = new double[totalPoints];
								windowTotal = getWindowedData(wholeDataArray[j]);
								double db = roiTable.get(roi.getRoiName());
								detectorValues[roiNameMap.get(key)][correctedDataPointNumber] = db + windowTotal;
								roiTable.put(key, db + windowTotal);
								if (roi.getRoiName().equals(selectedElement)) {
									value += windowTotal;
								}
							}

						}
							
						
						
					}
					
				}
				for (String s : roiNames) {
					rgbLine.append(roiTable.get(s));
					rgbLine.append(" ");
				}
				logger.debug("The y value is " + xy[0]);
				logger.debug("the x value is " + xy[1]);
				logger.debug("the data to plot is " + value);
			}
		}
		if (dataPoint.getCurrentPointNumber() == 0)
			addToRgbFile(roiHeader.toString());
		addToRgbFile(rgbLine.toString().trim());
		if(roiTable != null)
			roiTable.clear();
		if(value < minValue)
		{
			minValue = value ;			
		}
		fillDecrement =(int) minValue /100;
		if(isNormalise())
		{//if normalise is requested plot the normalised value in map and save normalised value internally
			//but write raw value to rgb files
			value = value / normaliseValue;
			
			for(int i =0; i< detectorValues.length;i++ )
				detectorValues[i][correctedDataPointNumber] = detectorValues[i][correctedDataPointNumber] / normaliseValue;
		}
		setPlottedSoFar(dataPoint.getCurrentPointNumber());
		//yIndex,xIndex
		dataSet.set(value, xyIndex[1], xyIndex[0]);
		fillDataSet((minValue - fillDecrement));
		}
	}
	

	private void fillDataSet(double minValue2) {
		for(int xindex = 0 ; xindex < numberOfXPoints ; xindex++)
		{
			if(dataSet.getDouble(new int[]{rowsPlottedSoFar -1,xindex}) == lastFilledValue)
					dataSet.set(minValue2,rowsPlottedSoFar -1, xindex);
		}
		for(int yindex =rowsPlottedSoFar ; yindex < numberOfYPoints ; yindex++)
		{
			for(int xindex = 0 ; xindex < numberOfXPoints ; xindex++)
			{
				dataSet.set(minValue2,yindex, xindex);
			}			
		}
		lastFilledValue = minValue2;
	}

	@SuppressWarnings("static-access")
	public void plotSpectrum(int detNo, int y, int x) throws Exception
	{
		//always make sure the spectrum asked to plot is less than the last data point to prevent crashing of the server
		if(lastDataPoint.getCurrentPointNumber() > (y*numberOfXPoints + x)){
			IDataset slice = getSpectrum(detNo,y,  x);
			if(slice != null)
			{
				try {
					RCPPlotter.plot("McaPlot",slice);
				} catch (DeviceException e) {
					logger.error("Unable to plot the spectrum for "+ x + " " + y, e);
					//TODO create a DisplayException class
					throw new Exception("Unable to plot the spectrum for "+ x + " " + y,e);
					
				}
			}
			else
				throw new Exception("Unable to plot the spectrum for "+ x + " " + y);
		}
		dataHolder = null;
	}

	public IDataset getSpectrum(int detNo, int y, int x){
		IDataset slice = null;
		try {
			dataHolder = hdf5Loader.loadFile();
		
		if(isXspressScan())
		{
			lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/"+ detectorName+"/MCAs");
			slice = lazyDataset.getSlice(new int[]{y, x, detNo,0}, new int[]{y+1, x+1, detNo+1, spectrumLength}, new int[]{1,1,1,1});
		}
		else if(isXmapScan()) {
			lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/"+ detectorName+"/fullSpectrum");
			if(lazyDataset == null)
			{
				lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/"+ detectorName+"/Element"+ detNo+"_fullSpectrum");
				 slice = lazyDataset.getSlice(new int[]{y, x,0}, new int[]{y+1, x+1,  spectrumLength}, new int[]{1,1,1});
			}
			else {
				 slice = lazyDataset.getSlice(new int[]{y, x, detNo,0}, new int[]{y+1, x+1, detNo+1, spectrumLength}, new int[]{1,1,1,1});
			}
		}} catch (ScanFileHolderException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}
		
		if(slice != null)
		{
			ILazyDataset sqSlice = slice.squeeze();
			return (IDataset)sqSlice;
		}
		return slice;
		//IDataset slice = lazyDataset.getSlice(new int[]{y, x, detNo,0}, new int[]{y+1, x+1, detNo+1, spectrumLength}, new int[]{1,1,1,1});
	}
	@SuppressWarnings("static-access")
	public void displayPlot(String selectedElement)throws Exception
	{
		int fillDecrement =0;
		this.setSelectedElement(selectedElement);
		
		//is selected element in the Scaler list
		for(Detector det : detectors){
			if(det instanceof TfgScaler)
			{
				String[] s = det.getExtraNames();
				for (int i = 0; i < s.length; i++) {
				if (s[i].equals(selectedElement)) {
					selectedElementIndex = i;
					break;
				}
					selectedElementIndex = -1;
				
			}
			}
		}
		if(selectedElementIndex != -1)//the selected emenet is a scaler value
		//displaying the map for the scaler
		{
			createDataSet();
			minValue =  Double.MAX_VALUE;
			logger.info("about to fill the data set");
			
			for(int i = 0; i < (rowsPlottedSoFar * numberOfXPoints); i++)
			{
				if(scalerValues[i][selectedElementIndex] != 0.0)
				{
					if(scalerValues[i][selectedElementIndex] < minValue )
					{
						minValue = scalerValues[i][selectedElementIndex] ;
					}
					dataSet.set(scalerValues[i][selectedElementIndex], i/numberOfXPoints , i%numberOfXPoints);
				}
			}
			fillDecrement = (int)minValue /100;
			if(plottedSoFar + 1 != (numberOfXPoints * numberOfYPoints))
				fillDataSet((minValue -fillDecrement));
			RCPPlotter.imagePlot(plotName, dataSet);
			//reset the selected element index
			selectedElementIndex = -1;
			return;
		}
		else if(isXspressScan())
		{
			minValue = Double.MAX_VALUE;
			@SuppressWarnings("unused")
			boolean mapFound = false;
			Integer elementIndex = roiNameMap.get(selectedElement);
			if(elementIndex != null){
				for(int point = 0; point < (rowsPlottedSoFar * numberOfXPoints); point ++)
				{
					if(detectorValues[elementIndex][point ] != 0.0)
					{
						dataSet.set(detectorValues[elementIndex][point ], point/numberOfXPoints, point %numberOfXPoints);
						if(detectorValues[elementIndex][point ] < minValue)
						{
							minValue = detectorValues[elementIndex][point ];
						}
					}
				}
			}
				fillDecrement = (int) minValue/100;
				if(plottedSoFar + 1 != (numberOfXPoints * numberOfYPoints))
					fillDataSet((minValue - fillDecrement));
				RCPPlotter.imagePlot(plotName, dataSet);
		
			return;

		}
		else if( isXmapScan())

		{
			minValue = Double.MAX_VALUE;
			Integer elementIndex = roiNameMap.get(selectedElement);
			if(elementIndex != null){
				for(int point = 0; point < (rowsPlottedSoFar * numberOfXPoints); point ++)
				{
					if(detectorValues[elementIndex][point ] != 0.0)
					{
						dataSet.set(detectorValues[elementIndex][point ], point/numberOfXPoints, point %numberOfXPoints);
						if(detectorValues[elementIndex][point ] < minValue)
						{
							minValue = detectorValues[elementIndex][point ];
						}
					}
				}
			}
			fillDecrement = (int)minValue/100;
			if(plottedSoFar + 1 != (numberOfXPoints * numberOfYPoints))
				fillDataSet((minValue - fillDecrement));
			RCPPlotter.imagePlot(plotName, dataSet);
			return;			
		}
		throw new Exception("unable to determine the detector for the selected element ");
		//is it xspress or the vortex detector
		
	}
	private boolean isXspressScan() {
		for(Detector det : detectors){
			if(det instanceof XspressDetector || det instanceof Xspress2BufferedDetector)
				return true;
		}
		return false;
			
	}

	private boolean isXmapScan() {
		for(Detector det : detectors){
			if(det instanceof XmapDetector || det instanceof XmapBufferedDetector)
				return true;
		}
		return false;
	}

	public double[] getXY(int x, int y)
	{
		double [] xy = new double[3];
		int pointNumber = findPointNumber(y, x);
		xy[0]  = xValues[pointNumber];
		xy[1] = yValues[pointNumber];
		xy[2] = zValue;
 		return xy;
	}
	private int findPointNumber(int y, int x) {
		// TODO Auto-generated method stub
		return (y*numberOfXPoints + x );
	}

	private void addToRgbFile(String string) throws IOException {
		if (writer != null) {
			writer.write(string + "\n");
			writer.flush();
		}
	}
	
	private int[] getXYIndex(int dataPointNumber, double xValue)
	{
		int xIndex = dataPointNumber % numberOfXPoints;
		int yIndex = dataPointNumber / numberOfXPoints;
		if(yIndex > 0)// first row is complete
		{
			xIndex = findXIndexByValue(xValue);
		}
		return new int[]{xIndex, yIndex};
	}

	private int findXIndexByValue(double xValue) {
		for(int i =0; i< xValues.length; i++)
		{
			if(xValues[i] == xValue)
				return i;
		}
		return -1;
	}

	private void createRgbFile(String string) {
		if (!string.contains("."))
			string = string + ".rgb";

		try {
			writer = new FileWriter(new File(string));
		} catch (IOException e) {
			logger.error("unable to create the rgb file " + string);
		}

	}

	private double getWindowedData(double[] data) {
		double total = 0.0;
		for (int i = windowStart; i <= windowEnd; i++) {
			total = total + data[i];
		}
		return total;
	}

	public void setSelectedElement(String selectedElement) {
		this.selectedElement = selectedElement;
	}

	public String getSelectedElement() {
		return selectedElement;
	}

	public void setDetectorBeanFileName(String detectorBeanFileName) {
		this.detectorBeanFileName = detectorBeanFileName;
	}

	public String getDetectorBeanFileName() {
		return detectorBeanFileName;
	}

	@Override
	public void finalize() throws Throwable {
		logger.info("finalize called on MFwriter");
		try {
			writer.close();
		} finally {
			super.finalize();
		}
		writer = null;
		scalerValues = null;
		detectorValues = null;
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

	public void setPlotUpdateFrequency(int plotUpdateFrequency) {
		this.plotUpdateFrequency = plotUpdateFrequency;
	}

	public int getPlotUpdateFrequency() {
		return plotUpdateFrequency;
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

	public double getxStepSize() {
		return xStepSize;
	}

	public void setxStepSize(double xStepSize) {
		this.xStepSize = xStepSize;
	}

	public double getyStepSize() {
		return yStepSize;
	}

	public void setyStepSize(double yStepSize) {
		this.yStepSize = yStepSize;
	}

}
