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
import java.util.Hashtable;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public class ScalerMFMappableDataProvider extends MicroFocusMappableDataProvider {

	public ScalerMFMappableDataProvider() {
		super();
	}

	private static final Logger logger = LoggerFactory.getLogger(ScalerMFMappableDataProvider.class);
	private String[] elementNames;
	private List<IonChamberParameters> ionChambersList;
	private int numberOfIonChambers;
	private Hashtable<String, double[]> detectorData;
	private String detectorName;

	@Override
	public void loadData(String fileName) {
		super.loadData(fileName);
		detectorData = new Hashtable<String, double[]>(numberOfIonChambers);
	}

	@Override
	public double[][] constructMappableData() {

		double[][] mapData = new double[yAxisLengthFromFile][xAxisLengthFromFile];
		// HashMap<String, Serializable> map = mainTree.getAttributes();

		double[] data = null;
		if (!detectorData.containsKey(selectedElement)) {
			lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + detectorName + "/" + selectedElement);
			if (lazyDataset == null)
				lazyDataset = dataHolder.getLazyDataset("/entry1/instrument/" + trajectoryCounterTimerName + "/"
						+ selectedElement);
			IDataset slice = lazyDataset.getSlice(new int[] { 0, 0 }, new int[] { yAxisLengthFromFile,
					xAxisLengthFromFile }, new int[] { 1, 1 });
			data = (double[]) DatasetUtils.cast(slice, Dataset.FLOAT64).getBuffer();
			detectorData.put(selectedElement, data);
		} else {
			data = detectorData.get(selectedElement);
		}
		int dataCounter = 0;
		for (int i = 0; i < yarray.length; i++) {
			for (int j = 0; j < xarray.length; j++) {
				mapData[i][j] = data[dataCounter++];
			}
		}
		return mapData;
	}

	@Override
	public void loadBean() {
		Object detectorBean = null;
		try {

			if (beanFile == null) {
				detectorBean = XMLHelpers.getBean(new File(LocalProperties.getConfigDir()
						+ "/templates/Detector_Parameters.xml"));
			} else
				detectorBean = XMLHelpers.getBean(new File(beanFile));
		} catch (Exception e) {
			logger.error("Unable to load bean file in  ScalerMappableDataprovider", e);
		}
		if (detectorBean != null && ((DetectorParameters) detectorBean).getTransmissionParameters() != null) {
			ionChambersList = ((DetectorParameters) detectorBean).getTransmissionParameters().getIonChamberParameters();
			detectorName = ionChambersList.get(0).getDeviceName();
			numberOfIonChambers = ionChambersList.size();
			setElementNames();
		}
	}

	private void setElementNames() {
		elementNames = new String[numberOfIonChambers];
		int i = 0;
		for (IonChamberParameters ion : ionChambersList) {
			elementNames[i] = ion.getName();
			i++;
		}
	}

	public List<IonChamberParameters> getIonChambers() {
		return ionChambersList;
	}

	@Override
	public double[] getSpectrum(int detectorNo, int y, int x) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPlotData(String elementName) {
		for (String s : elementNames)
			if (elementName.equals(s))
				return true;
		return false;
	}

	@Override
	public String[] getElementNames() {
		return elementNames;
	}

	@Override
	public void loadBean(XMLRichBean bean) {
	}

}
