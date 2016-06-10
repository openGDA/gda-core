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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.dataset.DatasetException;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;


public class XspressMFMappableDataProvider extends MicroFocusMappableDataProvider {

	public XspressMFMappableDataProvider() {
		super();
	}

	private static final String[] detectorNames = new String[]{"xspress2system", "raster_xspress"};

	private int numberOfdetectorElements;
	private List<DetectorROI>[] elementRois;
	private HashMap<String, Integer> roiNameMap;
	private static final Logger logger = LoggerFactory.getLogger(XspressMFMappableDataProvider.class);
	private int maxSpectrumLengthForViewing = 4096;

	@Override
	public void loadData(String fileName) {
		super.loadData(fileName);
		lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorNames[0] + "/MCAs");
		if (lazyDataset == null){
			lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorNames[1] + "/MCAs");
		}
	}

	@Override
	public double[][] constructMappableData() throws DatasetException {
		logger.debug("getting data for " + selectedElement + " channel " + selectedChannel);
		double[][] mapData = new double[yarray.length][xarray.length];
		Integer selectedElementIndex = roiNameMap.get(selectedElement);
		DetectorROI roi=  elementRois[selectedChannel].get(selectedElementIndex);

		double[][][] mcas = getMCAMapFromFile(selectedChannel);
		for (int i = 0; i < yAxisLengthFromFile; i++) {
			for (int j = 0; j < xAxisLengthFromFile; j++) {
				double[] roiValues = ArrayUtils.subarray(mcas[i][j], roi.getRoiStart(), roi.getRoiEnd());
				double roiSum = 0;
				for (double value :roiValues ) {
					roiSum += value;
				}
				mapData[i][j] = roiSum;
			}
		}
		return mapData;
	}

	private double[][][] getMCAMapFromFile(int channel) throws DatasetException {
		IDataset slice = lazyDataset.getSlice(new int[] { 0, 0, channel, 0 }, new int[] { yAxisLengthFromFile, xAxisLengthFromFile,
				channel+1, 4096 }, new int[] { 1, 1, 1, 1 });
		IDataset sqSlice = slice.squeeze();
		double[] data = (double[]) DatasetUtils.cast(sqSlice, Dataset.FLOAT64).getBuffer();
		int dim[] = sqSlice.getShape();
		return packto4D(data, dim[0], dim[1], dim[2]);
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
			xspressBean = XMLHelpers.getBean(new File(beanFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (xspressBean != null) {
			numberOfdetectorElements = ((XspressParameters) xspressBean).getDetectorList().size();

			elementRois = new List[numberOfdetectorElements];
			for (int detectorNo = 0; detectorNo < numberOfdetectorElements; detectorNo++)
				elementRois[detectorNo] = ((XspressParameters) xspressBean).getDetector(detectorNo).getRegionList();
			String eleNames[] = getElementNames();
			roiNameMap = new HashMap<String, Integer>();
			for (int i = 0; i < eleNames.length; i++) {
				roiNameMap.put(eleNames[i], i);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadBean(XMLRichBean xspressBean) {
		if (xspressBean != null) {
			numberOfdetectorElements = ((XspressParameters) xspressBean).getDetectorList().size();

			elementRois = new List[numberOfdetectorElements];
			for (int detectorNo = 0; detectorNo < numberOfdetectorElements; detectorNo++)
				elementRois[detectorNo] = ((XspressParameters) xspressBean).getDetector(detectorNo).getRegionList();
			String eleNames[] = getElementNames();
			roiNameMap = new HashMap<String, Integer>();
			for (int i = 0; i < eleNames.length; i++) {
				roiNameMap.put(eleNames[i], i);
			}
		}
	}

	public List<DetectorROI>[] getElementRois() {
		return elementRois;
	}

	@Override
	public double[] getSpectrum(int detectorNo, int x, int y) throws DatasetException {
		int spectrumLength = maxSpectrumLengthForViewing;
		if (lazyDataset != null) {
			int shape[] = lazyDataset.getShape();
			if (shape != null && shape.length == 4) {
				spectrumLength = shape[3];
			}
		}
		IDataset slice = lazyDataset.getSlice(new int[] { y, x, detectorNo, 0 }, new int[] { y + 1, x + 1,
				detectorNo + 1, spectrumLength }, new int[] { 1, 1, 1, 1 });
		if (slice == null){
			return null; // we are out of the limits
		}
		IDataset sqSlice = slice.squeeze();
		return (double[]) DatasetUtils.cast(sqSlice, Dataset.FLOAT64).getBuffer();
	}

	@Override
	public String[] getElementNames() {
		ArrayList<String> elementRefList = new ArrayList<String>();
		ArrayList<String> elementRefList2 = new ArrayList<String>();
		ArrayList<String> elementsList = new ArrayList<String>();
		List<DetectorROI> elementROI = elementRois[0];
		for (DetectorROI roi : elementROI) {
			elementRefList.add(roi.getRoiName());
			elementRefList2.add(roi.getRoiName());
		}

		for (int i = 1; i < elementRois.length; i++) {
			elementROI = elementRois[i];
			elementsList.clear();
			for (DetectorROI roi : elementROI) {
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
}
