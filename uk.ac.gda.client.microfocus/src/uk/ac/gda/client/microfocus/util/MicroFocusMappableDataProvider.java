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
import gda.data.nexus.tree.INexusTree;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;

public abstract class MicroFocusMappableDataProvider {

	protected INexusTree mainTree;
	protected String xScannableName;
	protected String[] trajectoryScannableName = null;
	protected String trajectoryCounterTimerName = null;
	protected int yAxisLengthFromFile = 0;
	protected int xAxisLengthFromFile = 0;
	protected HDF5Loader hdf5Loader;
	protected DataHolder dataHolder;
	protected ILazyDataset lazyDataset;
	protected Double[] xarray;
	protected Double[] yarray;
	protected String selectedElement;
	protected String yScannableName;
	protected double zValue;
	protected String zScannableName = "sc_sample_z";
	private AbstractDataset i0data;
	private AbstractDataset itdata;
	public abstract boolean hasPlotData(String elementName);

	public abstract double[][] constructMappableData();

	public abstract void loadBean();

	public abstract double[] getSpectrum(int detectorNo, int y, int x);

	protected String detectorName;
	protected INexusTree detectorNode;
	protected String beanFile;
	private static final Logger logger = LoggerFactory.getLogger(MicroFocusMappableDataProvider.class);

	public MicroFocusMappableDataProvider() {
		super();
		try {
			if (LocalProperties.get("gda.factory.factoryName").equals("b16server")
					|| LocalProperties.get("gda.factory.factoryName").equals("b16")) {

				zScannableName = "z";
			}
		} catch (Exception e) {
			logger.warn("Error finding the Local Propertues for Z scannable name", e);
		}

	}

	public String getTrajectoryCounterTimerName() {
		return trajectoryCounterTimerName;
	}

	public void setTrajectoryCounterTimerName(String trajectoryCounterTimerName) {
		this.trajectoryCounterTimerName = trajectoryCounterTimerName;
	}

	public String[] getTrajectoryScannableName() {
		return trajectoryScannableName;
	}

	public void setTrajectoryScannableName(String trajectoryScannableName[]) {
		this.trajectoryScannableName = trajectoryScannableName;
	}

	public double getZValue() {
		return zValue;
	}

	public void setZValue(double zValue) {
		this.zValue = zValue;
	}

	public String getZScannableName() {
		return zScannableName;
	}

	public void setZScannableName(String zScannableValue) {
		this.zScannableName = zScannableValue;
	}

	public String getXScannableName() {
		return xScannableName;
	}

	public void setXScannableName(String xScannableName) {
		this.xScannableName = xScannableName;
	}

	public String getYScannableName() {
		return yScannableName;
	}

	public void setYScannableName(String yScannableName) {
		this.yScannableName = yScannableName;
	}

	public Double[] getXarray() {
		return xarray;
	}

	public Double[] getYarray() {
		return yarray;
	}

	public String getSelectedElement() {
		return selectedElement;
	}

	public void setSelectedElement(String selectedElement) {
		this.selectedElement = selectedElement;
	}

	public String getDetectorName() {
		return detectorName;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	protected IDataset getDatasetFromLazyDataset(ILazyDataset xscannableDS) {
		int shape[] = xscannableDS.getShape();
		int startShape[] = new int[shape.length];
		int step[] = new int[shape.length];
		for (int i = 0; i < step.length; i++) {
			step[i] = 1;
		}
		return xscannableDS.getSlice(startShape, shape, step);
	}

	/**
	 * Load a MicroFocus Nexus file and read in the x and y axis values
	 */
	public void loadData(String fileName) {
		try {
			hdf5Loader = new HDF5Loader(fileName);
			dataHolder = hdf5Loader.loadFile();
			
			String location = "/entry1/instrument/" + xScannableName + "/" + xScannableName;
			ILazyDataset xscannableDS = dataHolder.getLazyDataset(location);

			String[] namesList = dataHolder.getNames();
			String names = "";
			for(int i=0;i<namesList.length;i++){
				String name = namesList[i];
				names = names + name+", ";
			}
			
			
			if(names.contains("/entry1/counterTimer01/I0")){
				ILazyDataset i0DS = dataHolder.getLazyDataset("/entry1/counterTimer01/I0");
				i0data = DatasetUtils.convertToAbstractDataset(getDatasetFromLazyDataset(i0DS));
			}
			
			if(names.contains("/entry1/counterTimer01/It")){
				ILazyDataset itDS = dataHolder.getLazyDataset("/entry1/counterTimer01/It");
				itdata = DatasetUtils.convertToAbstractDataset(getDatasetFromLazyDataset(itDS));
			}
			
			if (xscannableDS == null) {
				
				if(names.contains("/entry1/instrument/trajectoryX/value"))
					xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/trajectoryX/value");
				else if(names.contains("/entry1/instrument/traj3SampleX/traj3SampleX"))
					xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/traj3SampleX/traj3SampleX");
				else if(names.contains("/entry1/instrument/traj1SampleX/traj1SampleX"))
					xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/traj1SampleX/traj1SampleX");
				else if(names.contains("/entry1/instrument/traj1ContiniousX/value"))
					xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/traj1ContiniousX/value");
			}

			AbstractDataset xdata = DatasetUtils.convertToAbstractDataset(getDatasetFromLazyDataset(xscannableDS));
			xAxisLengthFromFile = xdata.getShape()[1];

			ILazyDataset yscannableDS = null;
			
			if(names.contains("/entry1/instrument/sc_MicroFocusSampleY/sc_MicroFocusSampleY"))
				yscannableDS = dataHolder.getLazyDataset("/entry1/instrument/sc_MicroFocusSampleY/sc_MicroFocusSampleY");
			else if(names.contains("/entry1/instrument/sc_MicroFocusSampleY"))
				yscannableDS = dataHolder.getLazyDataset("/entry1/instrument/sc_MicroFocusSampleY");
			else if(names.contains("/entry1/instrument/table_y/table_y"))
				yscannableDS = dataHolder.getLazyDataset("/entry1/instrument/table_y/table_y");
			
			AbstractDataset ydata = DatasetUtils.convertToAbstractDataset(getDatasetFromLazyDataset(yscannableDS));
			yAxisLengthFromFile = ydata.getShape()[0];
			double[] x = (double[]) xdata.getBuffer();
			double[] y = (double[]) ydata.getBuffer();
			ILazyDataset zscannableDS = null;
			if(names.contains("/entry1/instrument/sc_sample_z"))
				zscannableDS = dataHolder.getLazyDataset("/entry1/instrument/sc_sample_z");
			else if(names.contains("/entry1/instrument/Sample_Stage/sc_sample_z"))
				zscannableDS = dataHolder.getLazyDataset("/entry1/instrument/Sample_Stage/sc_sample_z");
			
			// zValue is included as part of the scan
			if (zscannableDS != null) {
				AbstractDataset zdata = DatasetUtils.convertToAbstractDataset(getDatasetFromLazyDataset(zscannableDS));
				zValue = Double.parseDouble(zdata.getString(0));
			} else {
				// Read the zvalue from the metadata
				zscannableDS = dataHolder.getLazyDataset("/entry1/instrument/Sample_Stage" + "/" + zScannableName);
				if (zscannableDS != null) {
					AbstractDataset zdata = DatasetUtils
							.convertToAbstractDataset(getDatasetFromLazyDataset(zscannableDS));
					String[] z = (String[]) zdata.getBuffer();
					if (null != z)
						zValue = Double.parseDouble(z[0]);
				}
			}
			// x and y values from file will be
			// x = {0.0, 2.0, 4.0,6.0, 0.0, 2.0, 4.0,6.0,0.0, 2.0, 4.0,6.0}
			// y = { 0.0,0.0,0.0,0.0, 2.0 , 2.0 , 2.0 , 2.0 , 4.0, 4.0, 4.0}
			ArrayList<Double> xList = new ArrayList<Double>();
			ArrayList<Double> yList = new ArrayList<Double>();
			double xtmp = x[0];
			xList.add(xtmp);
			for (int i = 1; i < xAxisLengthFromFile; i++) {

				// if(x[i] == xtmp || Math.abs((x[i]- xtmp)) <= 0.000000000000001)
				// break;
				xList.add(x[i]);
			}
			double ytmp = y[0];
			yList.add(ytmp);
			for (int j = 1; j < y.length; j++) {
				if (y[j] != ytmp) {
					yList.add(y[j]);
					ytmp = y[j];
				}

			}
			xarray = new Double[xList.size()];
			yarray = new Double[yList.size()];
			xarray = xList.toArray(xarray);
			yarray = yList.toArray(yarray);
			if (yarray.length > yAxisLengthFromFile) {
				yarray = (Double[]) ArrayUtils.subarray(yarray, 0, yAxisLengthFromFile);
			}

		} catch (Exception ed) {
			logger.error("Error Reading the Nexus file", ed);
		}

	}

	public void setBeanFilePath(String file) {
		beanFile = file;
	}

	public AbstractDataset getI0data() {
		return i0data;
	}

	public AbstractDataset getItdata() {
		return itdata;
	}

	public abstract void loadBean(Object bean);
}
