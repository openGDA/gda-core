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

package uk.ac.gda.client.microfocus.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.io.NexusLoader;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;


public class MicroFocusNexusReader {
	private static final Logger logger = LoggerFactory.getLogger(MicroFocusNexusReader.class);
	private double[][][][] dataret = null;
//	private Object xspressBean;
	private INexusTree detectorNode;
	private Double[] xarray;
	private Double[] yarray;
	private double[] data;
	private double[][]vortexdetectorData;
	private String detectorName;
	public String getDetectorName() {
		return detectorName;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	@SuppressWarnings("rawtypes")
	private List[] elementRois;
	private INexusTree mainTree;
	private String xScannableName;
	private String yScannableName;
	private int numberOfdetectorElements;
	private String plottingWindowName;
	@SuppressWarnings("unused")
	private String detectorBeanFileName;


	/**
	 * Load a MicroFocus Nexus file and read in the x and y axis values
	 * @param FileName
	 */
	public void loadData(String FileName) {
		NexusLoader nl = new NexusLoader(FileName, true);

		try {
			INexusTree tree = nl.loadTree(null);
			System.out.println("loaded the nexus file");
			mainTree = tree.getChildNode("entry1", "NXentry");
			INexusTree tmptree = mainTree.getChildNode("instrument", "NXinstrument");
			NexusGroupData xdata = tmptree.getChildNode(
					xScannableName, "NXpositioner").getChildNode(xScannableName, "SDS").getData();

			NexusGroupData ydata = tmptree.getChildNode(
					yScannableName, "NXpositioner").getChildNode(yScannableName, "SDS").getData();
			double[] x = (double[]) xdata.releaseData();
			double[] y = (double[]) ydata.releaseData();
			//x and y values from file will be
			//x = {0.0, 2.0, 4.0,6.0, 0.0, 2.0, 4.0,6.0,0.0, 2.0, 4.0,6.0}
			//y = { 0.0,0.0,0.0,0.0, 2.0 , 2.0 , 2.0 , 2.0 , 4.0, 4.0, 4.0}
			ArrayList<Double> xList = new ArrayList<Double>();
			ArrayList<Double> yList = new ArrayList<Double>();
			double xtmp = x[0];
			xList.add(xtmp);
			for(int i =1; i<x.length; i++)
			{

				if(x[i] == xtmp)
					break;
				xList.add(x[i]);
			}
			double ytmp = y[0];
			yList.add(ytmp);
			for(int j =1; j < y.length; j++)
			{
				if(y[j] != ytmp)
				{
					yList.add(y[j]);
					ytmp = y[j];
				}

			}
			xarray = new Double[xList.size()];
			yarray =new Double[yList.size()];
			xarray = xList.toArray(xarray);
			yarray = yList.toArray(yarray);
		} catch (ScanFileHolderException e) {
			logger.error("Error Reading the Nexus file",e);
		} catch (Exception ed) {
			logger.error("Error Reading the Nexus file",ed);
		}

	}

	/**
	 * Get the xaxis values
	 * used for testing
	 * @return Double[]
	 */
	public Double[] getXValues()
	{
		return xarray;
	}
	/*
	 * Used only for testing
	 */
	public Double[] getYValues()
	{
		return yarray;
	}

	/**
	 * Get the data for the detector Element from the loaded data
	 *
	 */
	public double[][][] getElementData(int detectorNo) {
		detectorNode = mainTree
				.getChildNode(detectorName+"_element_"+detectorNo, "NXdata");

		NexusGroupData ngdata = detectorNode.getChildNode("data", "SDS")
				.getData();
		int dim[] = ngdata.dimensions;
		data = (double[]) ngdata.releaseData();
		if (dim.length == 3) {
			dataret[detectorNo] = packto4D(data, dim[0], dim[1], dim[2]);
		} else
			System.out.println("incorrect data");
		return dataret[detectorNo];
	}

	private double[][][] packto4D(double[] d1, int ny, int nx, int mcasize) {
		double[][][] ret = new double[ny][nx][mcasize];
		int index = 0;
		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < nx; j++) {
				for (int k = 0; k < mcasize; k++) {
					ret[i][j][k] = d1[index];
					index++;
				}
			}
		}
		return ret;
	}


	/**
	 * Read all the Element windows from the bean file
	 * @param fileName
	 * @return list of all elements rois
	 */
	@SuppressWarnings("unchecked")
	public  List<DetectorROI>[] getWindowsfromBean(String ...fileName) {
		XspressParameters xspressBean = null;
		try {
			if(fileName.length == 0)
				xspressBean  = (XspressParameters) XMLHelpers.getBean(new File(LocalProperties.getConfigDir()
						+ "/templates/Xspress_Parameters.xml"));
			else
				xspressBean = (XspressParameters) XMLHelpers.getBean(new File(fileName[0]));
		} catch (Exception e) {
			logger.warn("Could not open Xspress bean to extract the ROIs",e);
			return null;
		}

		detectorName = xspressBean.getDetectorName();
		numberOfdetectorElements = xspressBean.getDetectorList().size();
		int noOfDetectors;
		noOfDetectors = numberOfdetectorElements;
		dataret = new double[noOfDetectors][][][];
		elementRois = new List[noOfDetectors];
		for (int detectorNo = 0; detectorNo < noOfDetectors; detectorNo++)
			elementRois[detectorNo] = xspressBean
			.getDetector(detectorNo).getRegionList();
		return elementRois;

	}

	@SuppressWarnings("unchecked")
	public List<DetectorROI>[] getWindowsfromVortexBean(String ...fileName)
	{
		VortexParameters vortexBean = null;
		try {
			if(fileName.length == 0)
				vortexBean = (VortexParameters) XMLHelpers.getBean(new File(LocalProperties.getConfigDir()
						+ "/templates/Vortex_Parameters.xml"));
			else
				vortexBean = (VortexParameters) XMLHelpers.getBean(new File(fileName[0]));
		} catch (Exception e) {
			logger.warn("Could not open Vortex bean to extract the ROIs",e);
			return null;
		}
		if(vortexBean != null)
		{
			detectorName = vortexBean.getDetectorName();
			numberOfdetectorElements = vortexBean.getDetectorList().size();
			vortexdetectorData = new double[numberOfdetectorElements][];
			elementRois = new List[numberOfdetectorElements];
			for (int detectorNo = 0; detectorNo < numberOfdetectorElements; detectorNo++)
				elementRois[detectorNo] = vortexBean.getDetector(detectorNo).getRegionList();
		}

		return elementRois;

	}


	public void plotElement(String elementName, @SuppressWarnings("unused") String fileName) {
		double[][] mapData = constructMappableData(elementName);
		DoubleDataset plotSet = DatasetFactory.createFromObject(DoubleDataset.class, mapData);
		try {
			SDAPlotter.imagePlot(plottingWindowName, plotSet);
		} catch (Exception e) {
			logger.error("Error plotting the dataset in MicroFocusNexusReader", e);
		}

	}


	public double[][] constructMappableData(String elementName)
	{
		if (detectorName.equals("xspress2system"))
		{
			getWindowsfromBean((LocalProperties.getConfigDir()
					+ "/templates/Xspress_Parameters.xml"));
			return constructMappableDatafromXspress(elementName);
		}
		else if(detectorName.equals("xmapMca"))
		{
			getWindowsfromVortexBean((LocalProperties.getConfigDir()
					+ "/templates/Vortex_Parameters.xml"));
			return constructMappableDatafromXmap(elementName);
		}
		return constructMappableDatafromCounter(elementName);
	}

	public void setDetectorBeanFileName(String string) {
		this.detectorBeanFileName = string;

	}

	public double[][] constructMappableDatafromXmap(String elementName) {
		double[][] mapData = new double[yarray.length][xarray.length];
		vortexdetectorData = new double[numberOfdetectorElements][];
		detectorNode =  mainTree
				.getChildNode(detectorName, "NXdata");
		int elementCounter = 0;
		for (int i = 0; i < yarray.length; i++) {
			for (int j = 0; j < xarray.length; j++) {
				for (int detectorNo = 0; detectorNo < numberOfdetectorElements; detectorNo++) {
					if(vortexdetectorData[detectorNo] == null)
					{
						NexusGroupData d = detectorNode.getChildNode("Element"+detectorNo +"_"+elementName, "SDS").getData();
						vortexdetectorData[detectorNo] = (double[])d.releaseData();
						logger.info("the data obtained is " + d.isDetectorEntryData);
					}
					mapData[i][j] += vortexdetectorData[detectorNo][elementCounter];

				}
				elementCounter++;
			}
		}
		return mapData;
	}

	/**
	 * Construct a 2D array for the given element
	 * @param elementName
	 * @return 2d double array
	 */
	@SuppressWarnings("unchecked")
	public double[][] constructMappableDatafromXspress(String elementName) {
		int noOfDetectors;
		double[][] mapData = new double[yarray.length][xarray.length];
		noOfDetectors = numberOfdetectorElements;
		for (int i = 0; i < yarray.length; i++) {
			for (int j = 0; j < xarray.length; j++) {
				for (int detectorNo = 0; detectorNo < noOfDetectors; detectorNo++) {
					List <DetectorROI>roiList = elementRois[detectorNo];
					for (DetectorROI roi : roiList) {
						if (roi.getRoiName().equals(elementName)) {
							if(dataret[detectorNo] == null)
								getElementData(detectorNo);
							int windowEnd = roi.getRoiEnd();
							for(int k = roi.getRoiStart(); k <= windowEnd; k++)
							{
								mapData[i][j] += dataret[detectorNo][i][j][k];
							}
							break;

						}
					}
				}
			}
		}
		return mapData;
	}

	public double[][] constructMappableDatafromCounter(String elementName)
	{
		double[][] mapData = new double[yarray.length][xarray.length];
		detectorNode = mainTree
				.getChildNode(detectorName, "NXdata");
		NexusGroupData ngdata = (detectorNode.getChildNode(elementName, "SDS").getData());
		double[] detectorData = (double[])ngdata.releaseData();
		int dataCounter = 0;
		for( int i =0 ; i< yarray.length; i++)
		{
			for (int j =0; j< xarray.length; j++)
			{
				mapData[i][j] = detectorData[dataCounter++];
			}
		}
		return mapData;
	}

	public void setXScannableName(String xScannableName) {
		this.xScannableName = xScannableName;
	}

	public String getXScannableName() {
		return xScannableName;
	}

	public void setYScannableName(String yScannableName) {
		this.yScannableName = yScannableName;
	}

	public String getYScannableName() {
		return yScannableName;
	}

	public static void main(String args[]) {
		MicroFocusNexusReader mnr = new MicroFocusNexusReader();
		mnr
		.plotElement("fe","/home/nv23/workspaces/gdatrunk_sep09ws/configurations/diamond/i18/users/data/2010/sp0/i18-57.nxs");

	}

	public void setPlottingWindowName(String plottingWindowName) {
		this.plottingWindowName = plottingWindowName;
	}

	public String getPlottingWindowName() {
		return plottingWindowName;
	}
}
