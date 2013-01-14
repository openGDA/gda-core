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

package uk.ac.gda.client.microfocus.util;

import gda.configuration.properties.LocalProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;

public class XspressMFMappableDataProvider extends MicroFocusMappableDataProvider {

	public XspressMFMappableDataProvider() {
		super();
	}

	private int numberOfdetectorElements;
	private List<XspressROI>[] elementRois;
	private double[][] dataset;
	@SuppressWarnings("unused")
	private double[] data;
	private HashMap<String, Integer> roiNameMap;
	private static final Logger logger = LoggerFactory.getLogger(XspressMFMappableDataProvider.class);
	// TODO User should be able to change the length via preference
	private int maxSpectrumLengthForViewing = 2000;

	@Override
	public void loadData(String fileName) {
		super.loadData(fileName);
		lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorName + "/MCAs");
	}

	@Override
	public double[][] constructMappableData() {
		logger.info("getting data for " + selectedElement);
		int noOfDetectors;
		double dataSliceFromFile[][][] = null;
		double[][] mapData = new double[yarray.length][xarray.length];
		Integer selectedElementIndex = roiNameMap.get(selectedElement);
		noOfDetectors = numberOfdetectorElements;
		if (dataset[selectedElementIndex] == null) {
			dataset[selectedElementIndex] = new double[yAxisLengthFromFile * xAxisLengthFromFile];
			for (int i = 0; i < yAxisLengthFromFile; i++) {
				dataSliceFromFile = getDataSliceFromFile(i);
				for (int j = 0; j < xAxisLengthFromFile; j++) {
					for (int detectorNo = 0; detectorNo < noOfDetectors; detectorNo++) {
						List<XspressROI> roiList = elementRois[detectorNo];
						for (XspressROI roi : roiList) {
							if (roi.getRoiName().equals(selectedElement)) {
								int windowEnd = roi.getRoiEnd();
								for (int k = roi.getRoiStart(); k <= windowEnd; k++) {
									mapData[i][j] += dataSliceFromFile[j][detectorNo][k];
								}
							} else {
								Integer otherElementIndex = roiNameMap.get(roi.getRoiName());
								if (otherElementIndex != null) {
									if (dataset[otherElementIndex] == null)
										dataset[otherElementIndex] = new double[yAxisLengthFromFile
												* xAxisLengthFromFile];
									int windowEnd = roi.getRoiEnd();
									for (int k = roi.getRoiStart(); k <= windowEnd; k++) {
										dataset[otherElementIndex][(i * xAxisLengthFromFile) + j] += dataSliceFromFile[j][detectorNo][k];
									}
								}
							}
						}
					}
					dataset[selectedElementIndex][(i * xAxisLengthFromFile) + j] = mapData[i][j];
				}
			}
		} else {
			for (int i = 0; i < yAxisLengthFromFile; i++) {
				for (int j = 0; j < xAxisLengthFromFile; j++) {
					mapData[i][j] = dataset[selectedElementIndex][(i * xAxisLengthFromFile) + j];
				}
			}

		}
		return mapData;
	}

	private double[][][] getDataSliceFromFile(int i) {
		IDataset slice = lazyDataset.getSlice(new int[] { i, 0, 0, 0 }, new int[] { i + 1, xAxisLengthFromFile,
				numberOfdetectorElements, 4096 }, new int[] { 1, 1, 1, 1 });
		ILazyDataset sqSlice = slice.squeeze();
		double[] data = (double[]) ((AbstractDataset) sqSlice).getBuffer();
		int dim[] = sqSlice.getShape();
		return packto4D(data, dim[0], dim[1], dim[2]);
	}

	@SuppressWarnings("unused")
	private double[] getDataSliceFromFile(int y, int x, int detectorNo, XspressROI roi) {
		IDataset slice = lazyDataset.getSlice(new int[] { y, x, detectorNo, roi.getRoiStart() }, new int[] { y + 1,
				x + 1, detectorNo + 1, roi.getRoiEnd() }, new int[] { 1, 1, 1, 1 });
		ILazyDataset sqSlice = slice.squeeze();
		return (double[]) ((AbstractDataset) sqSlice).getBuffer();
	}

	@SuppressWarnings("unused")
	private double[][][][] packto4D(double[] d1, int ny, int nx, int noOfDetElements, int mcasize) {
		double[][][][] ret = new double[noOfDetElements][ny][nx][mcasize];
		int index = 0;
		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < nx; j++) {
				for (int l = 0; l < noOfDetElements; l++) {
					for (int k = 0; k < mcasize; k++) {
						ret[l][i][j][k] = d1[index];
						index++;
					}
				}
			}
		}
		return ret;
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

	@SuppressWarnings("unchecked")
	@Override
	public void loadBean() {
		Object xspressBean = null;
		try {

			if (beanFile == null)
				xspressBean = BeansFactory.getBean(new File(LocalProperties.getConfigDir()
						+ "/templates/Xspress_Parameters.xml"));
			else
				xspressBean = BeansFactory.getBean(new File(beanFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (xspressBean != null) {
			detectorName = ((XspressParameters) xspressBean).getDetectorName();
			numberOfdetectorElements = ((XspressParameters) xspressBean).getDetectorList().size();

			elementRois = new List[numberOfdetectorElements];
			for (int detectorNo = 0; detectorNo < numberOfdetectorElements; detectorNo++)
				elementRois[detectorNo] = ((XspressParameters) xspressBean).getDetector(detectorNo).getRegionList();
			String eleNames[] = getElementNames();
			dataset = new double[eleNames.length][];
			roiNameMap = new HashMap<String, Integer>();
			for (int i = 0; i < eleNames.length; i++) {
				roiNameMap.put(eleNames[i], i);
			}
		}
	}

	public List<XspressROI>[] getElementRois() {
		return elementRois;
	}

	@Override
	public double[] getSpectrum(int detectorNo, int y, int x) {
		int spectrumLength = maxSpectrumLengthForViewing;
		if (lazyDataset != null) {
			int shape[] = lazyDataset.getShape();
			if (shape != null && shape.length == 4) {
				if (maxSpectrumLengthForViewing <= shape[3])
					spectrumLength = maxSpectrumLengthForViewing;
				else
					spectrumLength = shape[3];
			}
		}
		IDataset slice = lazyDataset.getSlice(new int[] { y, x, detectorNo, 0 }, new int[] { y + 1, x + 1,
				detectorNo + 1, spectrumLength }, new int[] { 1, 1, 1, 1 });
		ILazyDataset sqSlice = slice.squeeze();
		return (double[]) ((AbstractDataset) sqSlice).getBuffer();
	}

	public String[] getElementNames() {
		ArrayList<String> elementRefList = new ArrayList<String>();
		ArrayList<String> elementRefList2 = new ArrayList<String>();
		ArrayList<String> elementsList = new ArrayList<String>();
		List<XspressROI> elementROI = elementRois[0];
		for (XspressROI roi : elementROI) {
			elementRefList.add(roi.getRoiName());
			elementRefList2.add(roi.getRoiName());
		}

		for (int i = 1; i < elementRois.length; i++) {
			elementROI = elementRois[i];
			elementsList.clear();
			for (XspressROI roi : elementROI) {
				elementsList.add(roi.getRoiName());
			}
			for (String s : elementRefList) {
				if (!elementsList.contains(s))
					elementRefList2.remove(s);
			}
			elementRefList = elementRefList2;
		}
		return elementRefList.toArray(new String[elementRefList.size()]);
	}

	@Override
	public boolean hasPlotData(String elementName) {
		String[] elementNames = getElementNames();
		for (String element : elementNames) {
			if (elementName.equals(element))
				return true;

		}
		return false;
	}
}
