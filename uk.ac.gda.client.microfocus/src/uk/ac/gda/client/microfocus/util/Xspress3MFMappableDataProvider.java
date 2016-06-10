/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import org.eclipse.dawnsci.analysis.api.dataset.DatasetException;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.AggregateDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.devices.detector.xspress3.Xspress3Detector;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public class Xspress3MFMappableDataProvider extends MicroFocusMappableDataProvider {

	private static final String[] detectorNames = new String[]{"xspress3", "raster_xspress3"};

	private static final Logger logger = LoggerFactory.getLogger(Xspress3MFMappableDataProvider.class);

	private int numberOfdetectorElements;
	private List<DetectorROI>[] elementRois;
	private MapCache mapCache;

	public Xspress3MFMappableDataProvider() {
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
		try {
			fillCache();
		} catch (DatasetException e) {
			logger.error("Problem filling cache", e);
		}
	}

	@Override
	public double[][] constructMappableData() {
		logger.debug("getting data for " + selectedElement);
		return mapCache.getMap(selectedElement, selectedChannel);
	}

	protected void fillCache() throws DatasetException {
		// when GDA writes all the MCAs to the Nexus file
		lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorNames[0] + "/MCAs");
		if (lazyDataset == null){
			lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorNames[1] + "/MCAs");
		}

		// else try the 'old' way where the MCAs are in separate file written by EPICS and linked to Nexus file
		if (lazyDataset == null){
			// derive the number of rows from the FF
			ILazyDataset ffDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorNames[0] + "/FF");
			int numberRows = ffDataset.getShape()[0];
			ILazyDataset[] mcaDataSetsByRow = new ILazyDataset[numberRows];
			for (int row = 0; row < numberRows; row++) {
				mcaDataSetsByRow[row] = dataHolder.getLazyDataset("/entry1/instrument/" + detectorNames[0] + "/"
						+ Xspress3Detector.getNameOfRowSubNode(row));
			}
			lazyDataset = new AggregateDataset(true, mcaDataSetsByRow);
		}

		String eleNames[] = getElementNames();
		HashMap<String, Integer> roiNameMap = new HashMap<String, Integer>();
		for (int i = 0; i < eleNames.length; i++) {
			roiNameMap.put(eleNames[i], i);
		}

		mapCache = new MapCache(roiNameMap, elementRois, lazyDataset);
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
			Xspress3Parameters xs3Parameters = (Xspress3Parameters) vortexBean;
			numberOfdetectorElements = xs3Parameters.getDetectorList().size();
			elementRois = new List[numberOfdetectorElements];
			for (int detectorNo = 0; detectorNo < numberOfdetectorElements; detectorNo++)
				elementRois[detectorNo] = xs3Parameters.getDetector(detectorNo).getRegionList();
		}
	}

	@Override
	public double[] getSpectrum(int detectorNo, int x, int y) throws DatasetException {
		return mapCache.getSpectrum(detectorNo, x, y);
	}

	@Override
	public String[] getElementNames() {
		ArrayList<String> elementRefList = new ArrayList<String>();
		ArrayList<String> elementRefList2 = new ArrayList<String>();
		List<DetectorROI> elementROI = elementRois[0];
		for (DetectorROI roi : elementROI) {
			elementRefList.add(roi.getRoiName());
			elementRefList2.add(roi.getRoiName());
		}
		for (int i = 1; i < elementRois.length; i++) {
			elementROI = elementRois[i];
			ArrayList<String> elementsList = new ArrayList<String>();
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
