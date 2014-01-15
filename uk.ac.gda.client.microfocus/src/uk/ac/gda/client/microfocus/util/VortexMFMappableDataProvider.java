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
import uk.ac.gda.beans.vortex.RegionOfInterest;
import uk.ac.gda.beans.vortex.VortexParameters;

public class VortexMFMappableDataProvider extends MicroFocusMappableDataProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(VortexMFMappableDataProvider.class);
	
	private int numberOfdetectorElements;
	private List<RegionOfInterest>[] elementRois;
	private double[][] vortexData;
	private HashMap<String, Integer> roiNameMap;

	public List<RegionOfInterest>[] getElementRois() {
		return elementRois;
	}

	public void setElementRois(List<RegionOfInterest>[] elementRois) {
		this.elementRois = elementRois;
	}

	public VortexMFMappableDataProvider() {
		super();
	}

	@Override
	public void loadData(String fileName) {
		super.loadData(fileName);
		String eleNames[] = getElementNames();
		vortexData = new double[eleNames.length][];
		roiNameMap = new HashMap<String, Integer>();
		for (int i = 0; i < eleNames.length; i++)
			roiNameMap.put(eleNames[i], i);
	}

	@Override
	public double[][] constructMappableData() {
		logger.debug("getting data for " + selectedElement);
		double[][] mapData = new double[yarray.length][xarray.length];
		Integer selectedElementIndex = roiNameMap.get(selectedElement);
		int noOfDetectors = numberOfdetectorElements;
		if (vortexData[selectedElementIndex] == null) {
			vortexData[selectedElementIndex] = new double[yAxisLengthFromFile * xAxisLengthFromFile];
			lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorName + "/fullSpectrum");
			if (lazyDataset != null) {
				double dataSliceFromFile[][][] = null;
				for (int i = 0; i < yAxisLengthFromFile; i++) {
					dataSliceFromFile = getDataSliceFromFile(i);
					for (int j = 0; j < xAxisLengthFromFile; j++) {
						for (int detectorNo = 0; detectorNo < noOfDetectors; detectorNo++) {
							List<RegionOfInterest> roiList = elementRois[detectorNo];
							for (RegionOfInterest roi : roiList) {
								if (roi.getRoiName().equals(selectedElement)) {
									int windowEnd = roi.getRoiEnd();
									for (int k = roi.getRoiStart(); k <= windowEnd; k++)
											mapData[i][j] += dataSliceFromFile[j][detectorNo][k];
								} else {
									Integer otherElementIndex = roiNameMap.get(roi.getRoiName());
									if (otherElementIndex != null) {
										if (vortexData[otherElementIndex] == null)
											vortexData[otherElementIndex] = new double[yAxisLengthFromFile
													* xAxisLengthFromFile];
										int windowEnd = roi.getRoiEnd();
										for (int k = roi.getRoiStart(); k <= windowEnd; k++)
											vortexData[otherElementIndex][(i * xAxisLengthFromFile) + j] += dataSliceFromFile[j][detectorNo][k];
									}
								}
							}
						}
						vortexData[selectedElementIndex][(i * xAxisLengthFromFile) + j] = mapData[i][j];
					}
				}
			} else {
				// read each detector element individually
				int[][] data = new int[noOfDetectors][];
				int mcaSize = 0;
				for (int yIndex = 0; yIndex < yAxisLengthFromFile; yIndex++) {
					for (int detNo = 0; detNo < noOfDetectors; detNo++) {
						lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorName + "/" + "Element"
								+ detNo + "_fullSpectrum");
						mcaSize = lazyDataset.getShape()[2];
						IDataset slice = lazyDataset.getSlice(new int[] { yIndex, 0, 0 }, new int[] { yIndex + 1,
								xAxisLengthFromFile, mcaSize }, new int[] { 1, 1, 1 });
						ILazyDataset sqSlice = slice.squeeze();
						data[detNo] = (int[]) ((AbstractDataset) sqSlice).getBuffer();
					}
					for (int xIndex = 0; xIndex < xAxisLengthFromFile; xIndex++) {
						for (int detectorNo = 0; detectorNo < noOfDetectors; detectorNo++) {
							List<RegionOfInterest> roiList = elementRois[detectorNo];
							for (RegionOfInterest roi : roiList) {
								if (roi.getRoiName().equals(selectedElement)) {
									int windowEnd = roi.getRoiEnd();
									for (int k = roi.getRoiStart(); k <= windowEnd; k++) {
										mapData[yIndex][xIndex] += data[detectorNo][k + (xIndex * mcaSize)];
									}
									// break;

								} else {
									Integer otherElementIndex = roiNameMap.get(roi.getRoiName());
									if (otherElementIndex != null) {
										if (vortexData[otherElementIndex] == null)
											vortexData[otherElementIndex] = new double[yAxisLengthFromFile
													* xAxisLengthFromFile];
										int windowEnd = roi.getRoiEnd();
										for (int k = roi.getRoiStart(); k <= windowEnd; k++) {
											vortexData[otherElementIndex][(yIndex * xAxisLengthFromFile) + xIndex] += data[detectorNo][k
													+ (xIndex * mcaSize)];
										}
									}
								}
							}
						}
						vortexData[selectedElementIndex][(yIndex * xAxisLengthFromFile) + xIndex] = mapData[yIndex][xIndex];
					}
				}
			}

		} else {
			for (int i = 0; i < yAxisLengthFromFile; i++) {
				for (int j = 0; j < xAxisLengthFromFile; j++) {
					mapData[i][j] = vortexData[selectedElementIndex][(i * xAxisLengthFromFile) + j];
				}
			}
		}
		return mapData;
	}

	private double[][][] getDataSliceFromFile(int i) {
		int shape[] = lazyDataset.getShape();
		IDataset slice = lazyDataset.getSlice(new int[] { i, 0, 0, 0 }, new int[] { i + 1, xAxisLengthFromFile,
				numberOfdetectorElements, shape[3] }, new int[] { 1, 1, 1, 1 });
		ILazyDataset sqSlice = slice.squeeze();
		Object data = ((AbstractDataset) sqSlice).getBuffer();
		int dim[] = sqSlice.getShape();
		return packto3D(data, dim[0], dim[1], dim[2]);
	}

	@SuppressWarnings("unused")
	private double[][] constructElementROI(double[] buffer, int y, int x, int noOfDetectors) {
		double retArray[][] = new double[noOfDetectors][y * x];
		int twoDIncrement = 0;
		for (int k = 0; k < buffer.length;) {
			for (int i = 0; i < noOfDetectors; i++) {
				retArray[i][twoDIncrement] = buffer[k + i];
			}
			twoDIncrement++;
			k = k + noOfDetectors;
		}
		return retArray;
	}

	@Override
	public void loadBean() {
		Object vortexBean = null;
		try {
			vortexBean = BeansFactory.getBean(new File(beanFile));
		} catch (Exception e) {
			logger.error("unable to load the bean file");
		}
		loadBean(vortexBean);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadBean(Object vortexBean) {
		if (vortexBean != null) {
			setDetectorName(((VortexParameters) vortexBean).getDetectorName());
			numberOfdetectorElements = ((VortexParameters) vortexBean).getDetectorList().size();
			elementRois = new List[numberOfdetectorElements];
			for (int detectorNo = 0; detectorNo < numberOfdetectorElements; detectorNo++)
				elementRois[detectorNo] = ((VortexParameters) vortexBean).getDetector(detectorNo).getRegionList();
		}
	}
	
	@Override
	public double[] getSpectrum(int detectorNo, int x, int y) {
		// assuming the data set is 4D array [y,x,detectors, mca]
		lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorName + "/fullSpectrum");
		Object spectrum = null;
		if (lazyDataset != null) {
			int shape[] = lazyDataset.getShape();
			IDataset slice = lazyDataset.getSlice(new int[] { y, x, detectorNo, 0 }, new int[] { y + 1, x + 1,
					detectorNo + 1, shape[3] }, new int[] { 1, 1, 1, 1 });
			ILazyDataset sqSlice = slice.squeeze();
			spectrum = ((AbstractDataset) sqSlice).getBuffer();
		} else {
			lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorName + "/" + "Element" + detectorNo
					+ "_fullSpectrum");
			int shape[] = lazyDataset.getShape();
			IDataset slice = lazyDataset.getSlice(new int[] { y, x, 0 }, new int[] { y + 1, x + 1, shape[2] },
					new int[] { 1, 1, 1 });
			ILazyDataset sqSlice = slice.squeeze();
			spectrum = ((AbstractDataset) sqSlice).getBuffer();
		}
		if (spectrum instanceof int[]) {
			int[] retInt = (int[]) spectrum;
			double[] doubleSpectrum = new double[retInt.length];
			for (int i = 0; i < doubleSpectrum.length; i++) {
				doubleSpectrum[i] = retInt[i];
			}
			return doubleSpectrum;
		}
		logger.debug("the return object is " + spectrum);
		if (spectrum instanceof short[]) {
			short[] retInt = (short[]) spectrum;
			double[] retDouble = new double[retInt.length];
			for (int i = 0; i < retDouble.length; i++) {
				retDouble[i] = retInt[i];
			}
			return retDouble;
		}
		return (double[]) spectrum;
	}

	public double[][][][] packto4D(int[] d1, int ny, int nx, int detIndex, int mcasize) {
		double data4d[][][][] = new double[detIndex][ny][nx][mcasize];
		int index = 0;

		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < nx; j++) {
				for (int dIndex = 0; dIndex < detIndex; dIndex++) {
					for (int k = 0; k < mcasize; k++) {
						data4d[dIndex][i][j][k] = d1[index];
						index++;
					}
				}
			}
		}
		return data4d;
	}

	public double[][][][] packto4D(double[] d1, int ny, int nx, int detIndex, int mcasize) {
		double data4d[][][][] = new double[detIndex][ny][nx][mcasize];
		int index = 0;

		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < nx; j++) {
				for (int dIndex = 0; dIndex < detIndex; dIndex++) {
					for (int k = 0; k < mcasize; k++) {
						data4d[dIndex][i][j][k] = d1[index];
						index++;
					}
				}
			}
		}
		return data4d;
	}

	private double[][][] packto3D(Object data, int ny, int nx, int mcasize) {
		int[] intData = null;
		short[] shortData = null;
		double[] doubleData = null;
		if(data instanceof int[])
			intData = (int[]) data;
		else if(data instanceof short[])
			shortData = (short[]) data;
		else if(data instanceof double[])
			doubleData = (double[]) data;
		double data3d[][][] = new double[ny][nx][mcasize];
		int index = 0;
		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < nx; j++) {
				for (int k = 0; k < mcasize; k++) {
					if(intData!=null)
						data3d[i][j][k] = intData[index];
					else if(shortData!=null)
						data3d[i][j][k] = shortData[index];
					else if(doubleData!=null)
						data3d[i][j][k] = doubleData[index];
					index++;
				}
			}
		}
		return data3d;
	}

	public String[] getElementNames() {
		ArrayList<String> elementRefList = new ArrayList<String>();
		ArrayList<String> elementRefList2 = new ArrayList<String>();
		ArrayList<String> elementsList = new ArrayList<String>();
		List<RegionOfInterest> elementROI = elementRois[0];
		for (RegionOfInterest roi : elementROI) {
			elementRefList.add(roi.getRoiName());
			elementRefList2.add(roi.getRoiName());
		}
		for (int i = 1; i < elementRois.length; i++) {
			elementROI = elementRois[i];
			elementsList.clear();
			for (RegionOfInterest roi : elementROI) {
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
