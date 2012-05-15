/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gda.MockFactory;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.XmapDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.countertimer.TfgScaler;
import gda.device.detector.xmap.util.XmapNexusFileLoader;
import gda.scan.ScanDataPoint;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.nexusformat.NexusFile;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.RegionOfInterest;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.util.PackageUtils;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class TwoWayMicroFocusWriterExtenderTest {
	
	private ScanDataPoint[] pointsList;
	private Scannable xScannable;
	private Scannable yScannable;
	private XmapDetector xmapDet;
	private TfgScaler scaler;
	private VortexParameters vortexParameters;
	private XmapNexusFileLoader fileLoader;
	private TwoWayMicroFocusWriterExtender mfWriterExtender;
	private int numberOfScanDataPoints;
	private int numberOfXPoints;
	private int numberOfYPoints;
	private double[] feData  = new double[]{4412.0, 4544.0,4339.0,4389.0,4312.0,4399.0,4429.0,4462.0,4409.0,4496.0,4515.0, 4423.0, 4409.0, 4508.0, 4357.0, 4391.0, 4332.0, 4494.0, 4389.0, 4368.0,4417.0, 4464.0};
	private double[] twoWayFeData  = new double[]{4412.0, 4544.0,4339.0,4389.0,4312.0,4399.0,4429.0,4462.0,4409.0,4496.0,4515.0,4464.0, 4417.0,4368.0,4389.0,4494.0,4332.0,4391.0, 4357.0,4508.0,4409.0,4423.0};
	private double []mnData= new double[]{346.0, 352.0,329.0,289.0,318.0,332.0,328.0,305.0,292.0,324.0,345.0,314.0, 316.0, 348.0, 331.0, 322.0, 306.0, 337.0, 365.0, 334.0, 348.0,299.0};
	private double []twoWayMnData= new double[]{346.0, 352.0,329.0,289.0,318.0,332.0,328.0,305.0,292.0,324.0,345.0,299.0, 348.0,334.0,365.0,337.0,306.0,322.0,331.0,348.0,316.0,314.0};
	class NamedObject {
		private String name;

		NamedObject(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
	
	public void configureTwoWayWriter()throws Exception{
		ArrayList<String> beansList = new ArrayList<String>();
		beansList.add("uk.ac.gda.beans.exafs.DetectorParameters");
		beansList.add("uk.ac.gda.beans.exafs.OutputParameters");
		beansList.add("uk.ac.gda.beans.vortex.VortexParameters");
		beansList.add("uk.ac.gda.beans.xspress.XspressParameters");
		BeansFactory beansFactory = new BeansFactory();
		beansFactory.setClassList(beansList);
		mfWriterExtender = new TwoWayMicroFocusWriterExtender(numberOfXPoints, numberOfYPoints, 0.1, 1.0);
		mfWriterExtender.setSelectedElement("I0");
		mfWriterExtender.setDetectors(new Detector[]{scaler, xmapDet});
		mfWriterExtender.setRoiNames(new String[]{"Fe_Ka", "Mn_Ka"});
        mfWriterExtender.setDetectorBeanFileName(PackageUtils.getTestPath(getClass(), "test")+"Vortex_Parameters.xml");
        mfWriterExtender.getWindowsfromBean();
	}
	public void configureScanDataPoints() throws Exception{
		numberOfScanDataPoints = 22;
		numberOfXPoints= 11;
		numberOfYPoints = 2;
		vortexParameters = (VortexParameters) XMLHelpers.createFromXML(VortexParameters.mappingURL, VortexParameters.class, VortexParameters.schemaURL, (new File(PackageUtils.getTestPath(getClass(), "test")+"Vortex_Parameters.xml")));
		fileLoader = new XmapNexusFileLoader(PackageUtils.getTestPath(getClass(), "test")+"i18-6931-0-raster_xmap.h5");
		fileLoader.loadFile();
		pointsList = new ScanDataPoint[numberOfScanDataPoints];
		for(int i =0 ; i < pointsList.length; i++){
			pointsList[i] = new ScanDataPoint();
			pointsList[i].setUniqueName("point"+ i);
			pointsList[i].setNumberOfPoints(numberOfScanDataPoints);
			pointsList[i].setCurrentPointNumber(i);
			pointsList[i].addScannable(yScannable);
			pointsList[i].addScannable(xScannable);			
			pointsList[i].addDetector(scaler);
			pointsList[i].addDetector(xmapDet);
			
			if(i > (numberOfXPoints - 1))
			{
						
				pointsList[i].addScannablePosition(new Double(2.0), yScannable.getOutputFormat());
				pointsList[i].addScannablePosition(new Double((i - numberOfXPoints)/10.0), xScannable.getOutputFormat());
			}
			else{
				pointsList[i].addScannablePosition(new Double(1.0), yScannable.getOutputFormat());
				pointsList[i].addScannablePosition(new Double(i /10.0), xScannable.getOutputFormat());
			}
			pointsList[i].addDetectorData(new double[]{100 +i,200+i, 300+i}, new String[]{"%9d","%9d","%9d"});
			pointsList[i].addDetectorData(getXmapNXdata(i, fileLoader.getData(i)), new String[]{"%s"});
			
			pointsList[i].setCurrentFilename(PackageUtils.getTestPath(getClass(), "test")+"80_name_1.nxs");
		}
		
	}
	public void configureTwoWayScanDataPoints() throws Exception{
		numberOfScanDataPoints = 22;
		numberOfXPoints= 11;
		numberOfYPoints = 2;
		vortexParameters = (VortexParameters) XMLHelpers.createFromXML(VortexParameters.mappingURL, VortexParameters.class, VortexParameters.schemaURL, (new File(PackageUtils.getTestPath(getClass(), "test")+"Vortex_Parameters.xml")));
		fileLoader = new XmapNexusFileLoader(PackageUtils.getTestPath(getClass(), "test")+"i18-6931-0-raster_xmap.h5");
		fileLoader.loadFile();
		pointsList = new ScanDataPoint[numberOfScanDataPoints];
		int reverseCounter =1;
		for(int i =0 ; i < pointsList.length; i++){
			pointsList[i] = new ScanDataPoint();
			pointsList[i].setUniqueName("point"+ i);
			pointsList[i].setNumberOfPoints(numberOfScanDataPoints);
			pointsList[i].setCurrentPointNumber(i);
			pointsList[i].addScannable(yScannable);
			pointsList[i].addScannable(xScannable);			
			pointsList[i].addDetector(scaler);
			pointsList[i].addDetector(xmapDet);
			
			if(i > (numberOfXPoints - 1))
			{
						
				pointsList[i].addScannablePosition(new Double(2.0), yScannable.getOutputFormat());
				pointsList[i].addScannablePosition(new Double((i - reverseCounter)/10.0), xScannable.getOutputFormat());
				reverseCounter +=2;
			}
			else{
				pointsList[i].addScannablePosition(new Double(1.0), yScannable.getOutputFormat());
				pointsList[i].addScannablePosition(new Double(i /10.0), xScannable.getOutputFormat());
			}
			pointsList[i].addDetectorData(new double[]{100 +i,200+i, 300+i}, new String[]{"%9d","%9d","%9d"});
			pointsList[i].addDetectorData(getXmapNXdata(i, fileLoader.getData(i)), new String[]{"%s"});
			
			pointsList[i].setCurrentFilename(PackageUtils.getTestPath(getClass(), "test")+"80_name_1.nxs");
		}
		
	}
	private NXDetectorData getXmapNXdata(int dataPointNumber, short[][] s){
		NXDetectorData output = new NXDetectorData(xmapDet);
		INexusTree detTree = output.getDetTree(xmapDet.getName());

		int numberOfElements = vortexParameters.getDetectorList().size();
		int numberOfROIs = vortexParameters.getDetectorList().get(0).getRegionList().size();

		// items to write to nexus
		double[] summation = null;
		double[] correctedAllCounts = new double[numberOfElements];
		final Double[] times =new Double[]{1.7867731067630825e-08, 2.1322543748867164e-08,2.1412068256345848e-08,2.5302404673324592e-08};
		double[] ocrs = new double[numberOfElements];
		double[] icrs = new double[numberOfElements];
		double[][] roiCounts = new double[numberOfROIs][numberOfElements];
		String[] roiNames = new String[numberOfROIs];
		short detectorData[][] = s;

		for (int element = 0; element < vortexParameters.getDetectorList().size(); element++) {

			DetectorElement thisElement = vortexParameters.getDetectorList().get(element);
			if (thisElement.isExcluded())
				continue;

			// TODO replacae
			final double ocr = 1000.0 + element;
			ocrs[element] = ocr;
			final double icr = 2000.0 + element;
			icrs[element] = icr;
			// REGIONS
			for (int iroi = 0; iroi < thisElement.getRegionList().size(); iroi++) {

				final RegionOfInterest roi = thisElement.getRegionList().get(iroi);

				// TODO calculate roi from the full spectrum data
				double count = calculateROICounts(roi.getRoiStart(), roi.getRoiEnd(), detectorData[element]);
				roiCounts[iroi][element] = count;
				roiNames[iroi] = roi.getRoiName();
			}
				if (summation == null)
					summation = new double[detectorData[element].length];
				for (int i = 0; i < detectorData[element].length; i++) {
					summation[i] += detectorData[element][i];
				}
		
		}

		// add total counts
		final INexusTree counts = output.addData(detTree, "totalCounts", new int[] { numberOfElements },
				NexusFile.NX_FLOAT64, correctedAllCounts, "counts", 1);
		for (int element = 0; element < numberOfElements; element++) {
			DetectorElement thisElement = vortexParameters.getDetectorList().get(element);
			output.setPlottableValue(thisElement.getName(), correctedAllCounts[element]);
		}

		// event processing time.
		String evtProcessTimeAsString = "";
		for (Double ept : times) {
			evtProcessTimeAsString += ept + " ";
		}
		evtProcessTimeAsString = evtProcessTimeAsString.trim();
		counts.addChildNode(new NexusTreeNode("eventProcessingTime", NexusExtractor.AttrClassName, counts,
				new NexusGroupData(evtProcessTimeAsString)));

		// ICR
		output.addData(detTree, "icr", new int[] { numberOfElements }, NexusFile.NX_FLOAT64, icrs, "Hz", 1);

		// OCR
		output.addData(detTree, "ocr", new int[] { numberOfElements }, NexusFile.NX_FLOAT64, ocrs, "Hz", 1);

		// roicounts
		for (int iroi = 0; iroi < numberOfROIs; iroi++) {
			String roiName = roiNames[iroi];
			output.addData(detTree, roiName, new int[] { numberOfElements }, NexusFile.NX_FLOAT64, roiCounts[iroi],
					"counts", 1);
			for (int element = 0; element < numberOfElements; element++) {
				String elementName = vortexParameters.getDetectorList().get(element).getName();
				output.setPlottableValue(elementName + "_" + roiName, roiCounts[iroi][element]);
			}
		}

		// add the full spectrum
		output.addData(detTree, "fullSpectrum", new int[] { numberOfElements, detectorData[0].length },
				NexusFile.NX_INT16, detectorData, "counts", 1);

		// ToDo implement the getROI and readout scanler data
		double ff = 10000.0 + dataPointNumber;
		output.addData(detTree, "FF", new int[] { 1 }, NexusFile.NX_FLOAT64, new Double[] { ff }, "counts", 1);
		output.setPlottableValue("FF", ff);

		if (summation != null)
			output.addData(detTree, "allElementSum", new int[] { summation.length }, NexusFile.NX_FLOAT64, summation,
					"counts", 1);
		return output;

	}
	private double calculateROICounts(int regionLow, int regionHigh, short[] data) {
		double count = 0.0;
		for (int i = regionLow; i <= regionHigh; i++)
			count += data[i];
		return count;
	}

	protected void configureMockScannablesAndDetectors() throws DeviceException {
		xScannable = MockFactory.createMockScannable(Scannable.class, "sc_MicroFocusSampleX",new String[] { "sc_MicroFocusSampleX" }, new String[] {}, new String[] { "%6.4g" }, 5, 0.0);
		yScannable =  MockFactory.createMockScannable(Scannable.class, "sc_MicroFocusSampleY",new String[] { "sc_MicroFocusSampleY" }, new String[] {}, new String[] { "%6.4g" }, 5, 0.0);
		when(xScannable.getOutputFormat()).thenReturn(new String[]{"%6.4g"});
		when(yScannable.getOutputFormat()).thenReturn(new String[]{"%6.4g"});
		xmapDet = mock(XmapDetector.class);
		scaler = mock(TfgScaler.class);
		when(xmapDet.getOutputFormat()).thenReturn(new String[] { "%s" });
		when(xmapDet.getInputNames()).thenReturn(new String[] {});
		when(xmapDet.getExtraNames()).thenReturn(new String[] { "xmapMca" });
		when(xmapDet.getName()).thenReturn( "xmapMca" );
		when(xmapDet.getNumberOfMca()).thenReturn(4);
		when(scaler.getOutputFormat()).thenReturn(new String[] { "%s" });
		when(scaler.getInputNames()).thenReturn(new String[]{"%9d","%9d","%9d"});
		when(scaler.getExtraNames()).thenReturn(new String[] { "I0", "It", "Iother" });
	}
	
	public void setup() throws Exception{
		configureMockScannablesAndDetectors() ;
		configureScanDataPoints();
		configureTwoWayWriter();
		
	}
	public void twoWaySetup() throws Exception{
		configureMockScannablesAndDetectors() ;
		configureTwoWayScanDataPoints();
		configureTwoWayWriter();
		
	}

	@Test
	public void testAddData() throws Exception{
		setup();
		mfWriterExtender.setSelectedElement("Fe_Ka");
		mfWriterExtender.updateDataSetFromSDP(pointsList[0]);
		assertEquals(mfWriterExtender.getDataSet().getDouble(new int[]{0,0}), 4412.0, 0.0);
		mfWriterExtender.updateDataSetFromSDP(pointsList[1]);
		assertEquals(mfWriterExtender.getDataSet().getDouble(new int[]{0,1}), 4544.0, 0.0);
		mfWriterExtender.setSelectedElement("Mn_Ka");
		mfWriterExtender.updateDataSetFromSDP(pointsList[2]);
		assertEquals(mfWriterExtender.getDataSet().getDouble(new int[]{0,3}), 326.0, 0.0);
		for(int i =3 ; i < 14; i++){
			mfWriterExtender.updateDataSetFromSDP(pointsList[i]);
		}
		assertEquals(mfWriterExtender.getDataSet().getDouble(new int[]{1,2}), 348.0, 0.0);
	}
	@Test
	public void testTwoWayAddData() throws Exception{
		twoWaySetup();
		mfWriterExtender.setSelectedElement("Fe_Ka");
		mfWriterExtender.updateDataSetFromSDP(pointsList[0]);
		assertEquals(mfWriterExtender.getDataSet().getDouble(new int[]{0,0}), 4412.0, 0.0);
		mfWriterExtender.updateDataSetFromSDP(pointsList[1]);
		assertEquals(mfWriterExtender.getDataSet().getDouble(new int[]{0,1}), 4544.0, 0.0);
		mfWriterExtender.setSelectedElement("Mn_Ka");
		mfWriterExtender.updateDataSetFromSDP(pointsList[2]);
		assertEquals(mfWriterExtender.getDataSet().getDouble(new int[]{0,3}), 326.0, 0.0);
		for(int i =3 ; i < 18; i++){
			mfWriterExtender.updateDataSetFromSDP(pointsList[i]);
		}
		assertEquals(mfWriterExtender.getDataSet().getDouble(new int[]{1,4}), 337.0, 0.0);
		//correct fillDataset
		//assertEquals(mfWriterExtender.getDataSet().getDouble(new int[]{1,8}), 348.0, 0.0);
		
	}
	@Test
	public void testDisplayPlot() throws Exception
	{
		setup();
		mfWriterExtender.setSelectedElement("Fe_Ka");
		for(int i =0 ; i < pointsList.length; i++){
			mfWriterExtender.updateDataSetFromSDP(pointsList[i]);
		}
		mfWriterExtender.setPlottedSoFar(pointsList.length - 1);
		double[] dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();		
		for(int i =0 ; i <  dataSDP.length; i++){
			assertEquals(dataSDP[i], feData[i], 0.0);
		}
		mfWriterExtender.displayPlot("Mn_Ka");
		dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();
		for(int i =0 ; i < dataSDP.length; i++){
			assertEquals(dataSDP[i], mnData[i], 0.0);
		}
	}
	@Test
	public void testDisplayPlotMidway() throws Exception
	{
		setup();
		int midway = 9;
		mfWriterExtender.setSelectedElement("Fe_Ka");
		for(int i =0 ; i < midway; i++){
			mfWriterExtender.updateDataSetFromSDP(pointsList[i]);
		}
		mfWriterExtender.setPlottedSoFar(midway - 1);
		double[] dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();		
		for(int i =0 ; i < midway; i++){
			assertEquals(dataSDP[i], feData[i], 0.0);
		}
		mfWriterExtender.displayPlot("Mn_Ka");
		for(int i =midway  ; i < pointsList.length; i++){
			mfWriterExtender.updateDataSetFromSDP(pointsList[i]);
		}
		dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();
		for(int i =0 ; i <pointsList.length; i++){
			assertEquals(dataSDP[i], mnData[i], 0.0);
		}
	}
	@Test
	public void testTwoWayDisplayPlotMidway() throws Exception
	{
		twoWaySetup();
		int midway = 13;
		mfWriterExtender.setSelectedElement("Fe_Ka");
		for(int i =0 ; i < midway; i++){
			mfWriterExtender.updateDataSetFromSDP(pointsList[i]);
		}
		mfWriterExtender.setPlottedSoFar(midway - 1);
		double[] dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();		
		for(int i =0 ; i < numberOfXPoints; i++){
			assertEquals(dataSDP[i], twoWayFeData[i], 0.0);
		}
		int dataCounter = numberOfScanDataPoints -1;
		for(int i = numberOfXPoints ; i < midway; i++)
		{
			assertEquals(dataSDP[dataCounter], twoWayFeData[dataCounter], 0.0);
			dataCounter--;
		}
		assertEquals(dataSDP[midway],twoWayFeData[4] - ((int)(twoWayFeData[4] /100)), 0.0);
		mfWriterExtender.displayPlot("Mn_Ka");
		for(int i =midway  ; i < pointsList.length; i++){
			mfWriterExtender.updateDataSetFromSDP(pointsList[i]);
		}
		dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();
		for(int i =0 ; i <pointsList.length; i++){
			assertEquals(twoWayMnData[i],dataSDP[i],  0.0);
		}
	}
	@Test
	public void testPlotSpectrum() throws Exception
	{
		setup();
		mfWriterExtender.setSelectedElement("Fe_Ka");
		for(int i =0 ; i < pointsList.length; i++){
			mfWriterExtender.updateDataSetFromSDP(pointsList[i]);
		}
		IDataset spectrum = mfWriterExtender.getSpectrum(0, 0, 0);
		assertEquals(55.0, spectrum.getDouble(625), 0.0);
		spectrum = mfWriterExtender.getSpectrum(2, 0, 0);
		assertEquals(64.0, spectrum.getDouble(625), 0.0);
		spectrum = mfWriterExtender.getSpectrum(2, 0, 6);
		assertEquals(48.0, spectrum.getDouble(634), 0.0);
		spectrum = mfWriterExtender.getSpectrum(1, 0, 6);
		assertEquals(43.0, spectrum.getDouble(634), 0.0);
	}
	@Test
	public void testFillDataSet() throws Exception
	{
		setup();
		mfWriterExtender.setSelectedElement("Fe_Ka");
		mfWriterExtender.updateDataSetFromSDP(pointsList[0]);
		double[] dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();	
		assertEquals(dataSDP[1], feData[0] - ((int)(feData[0] /100)), 0.0);
		mfWriterExtender.updateDataSetFromSDP(pointsList[1]);
		dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();	
		assertEquals(dataSDP[2], feData[0] - ((int)(feData[0] /100)), 0.0);
		mfWriterExtender.updateDataSetFromSDP(pointsList[2]);
		dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();	
		assertEquals(dataSDP[3], feData[2] - ((int)(feData[2] /100)), 0.0);
		for(int i = 3; i < pointsList.length -1 ; i++)
		{
			mfWriterExtender.updateDataSetFromSDP(pointsList[i]);
		}
		dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();	
		assertEquals(dataSDP[pointsList.length -1], feData[4] - ((int)(feData[4] /100)), 0.0);
	}
	@Test
	public void testTwoWayFillDataSet() throws Exception
	{
		twoWaySetup();
		mfWriterExtender.setSelectedElement("Fe_Ka");
		mfWriterExtender.updateDataSetFromSDP(pointsList[0]);
		double[] dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();	
		assertEquals(dataSDP[1], twoWayFeData[0] - ((int)(twoWayFeData[0] /100)), 0.0);
		mfWriterExtender.updateDataSetFromSDP(pointsList[1]);
		dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();	
		assertEquals(dataSDP[2], twoWayFeData[0] - ((int)(twoWayFeData[0] /100)), 0.0);
		mfWriterExtender.updateDataSetFromSDP(pointsList[2]);
		dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();	
		assertEquals(dataSDP[3], twoWayFeData[2] - ((int)(twoWayFeData[2] /100)), 0.0);
		for(int i = 3; i < 15 ; i++)
		{
			mfWriterExtender.updateDataSetFromSDP(pointsList[i]);
		}
		dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();	
		assertEquals(dataSDP[14], twoWayFeData[4] - ((int)(twoWayFeData[4] /100)), 0.0);
		assertEquals(dataSDP[18], twoWayFeData[18], 0.0);
		for(int i = 15; i < pointsList.length -1 ; i++)
		{
			mfWriterExtender.updateDataSetFromSDP(pointsList[i]);
		}
		dataSDP = (double[])mfWriterExtender.getDataSet().getBuffer();	
		assertEquals(dataSDP[11], twoWayFeData[4] - ((int)(twoWayFeData[4] /100)), 0.0);
		assertEquals(dataSDP[12], twoWayFeData[12], 0.0);
		assertEquals(dataSDP[18], twoWayFeData[18], 0.0);
		assertEquals(dataSDP[21], twoWayFeData[21], 0.0);
		
	}
}
