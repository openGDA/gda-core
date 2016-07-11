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

package uk.ac.gda.client.microfocus.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.january.DatasetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;


public class VortexMFMappableDataProvider extends MicroFocusMappableDataProvider {

	private static final Logger logger = LoggerFactory.getLogger(VortexMFMappableDataProvider.class);

	private static final String[] detectorNames = new String[]{"xmapMca"};

	private int numberOfdetectorElements;
	private List<DetectorROI>[] elementRois;
	private HashMap<String, Integer> roiNameMap;
	private MapCache mapCache;

	public VortexMFMappableDataProvider() {
		super();
	}

	public List<DetectorROI>[] getElementRois() {
		return elementRois;
	}

	public void setElementRois(List<DetectorROI>[] elementRois) {
		this.elementRois = elementRois;
	}

	@Override
	public void loadData(String fileName) {
		super.loadData(fileName);
		String eleNames[] = getElementNames();

		roiNameMap = new HashMap<String, Integer>();
		for (int i = 0; i < eleNames.length; i++)
			roiNameMap.put(eleNames[i], i);
	}

	@Override
	public double[][] constructMappableData() throws DatasetException {

		logger.debug("getting data for " + selectedElement);
		if (mapCache == null) {
			lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorNames[0] + "/fullSpectrum");
			mapCache = new MapCache(roiNameMap, elementRois, lazyDataset);
		}
		return mapCache.getMap(selectedElement, selectedChannel);
	}

	@Override
	public void loadBean() {
		XMLRichBean vortexBean = null;
		try {
			vortexBean = XMLHelpers.getBean(new File(beanFile));
		} catch (Exception e) {
			logger.error("unable to load the bean file");
		}
		loadBean(vortexBean);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadBean(XMLRichBean vortexBean) {
		if (vortexBean != null) {
			numberOfdetectorElements = ((VortexParameters) vortexBean).getDetectorList().size();
			elementRois = new List[numberOfdetectorElements];
			for (int detectorNo = 0; detectorNo < numberOfdetectorElements; detectorNo++)
				elementRois[detectorNo] = ((VortexParameters) vortexBean).getDetector(detectorNo).getRegionList();
		}
	}

	@Override
	public double[] getSpectrum(int detectorNo, int x, int y) throws DatasetException {

		if (mapCache == null) {
			lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorNames[0] + "/fullSpectrum");
			mapCache = new MapCache(roiNameMap, elementRois, lazyDataset);
		}
		return mapCache.getSpectrum(detectorNo, x, y);
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
