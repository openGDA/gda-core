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

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.INexusTree;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public abstract class MicroFocusMappableDataProvider {

	private static final Logger logger = LoggerFactory.getLogger(MicroFocusMappableDataProvider.class);

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
	protected Integer selectedChannel;
	protected String yScannableName;
	protected double zValue;
	protected String zScannableName = "sc_sample_z";
//	protected String detectorName;
	protected INexusTree detectorNode;
	protected String beanFile;
	private Dataset i0data;
	private Dataset itdata;

	public MicroFocusMappableDataProvider() {
		super();
	}

	public boolean hasPlotData(String elementName) {
		String[] elementNames = getElementNames();
		for (String element : elementNames) {
			if (elementName.equals(element))
				return true;
		}
		return false;
	}

	public abstract String[] getElementNames();

	public abstract double[][] constructMappableData();

	public abstract void loadBean();

	public abstract double[] getSpectrum(int channelNum, int xPixel, int yPixel);

	public abstract void loadBean(XMLRichBean bean);


	/**
	 * Load a MicroFocus Nexus file and read in the x and y axis values
	 */
	public void loadData(String fileName) {
		try {
			hdf5Loader = new HDF5Loader(fileName);
			dataHolder = hdf5Loader.loadFile();

			String[] namesList = dataHolder.getNames();
			String names = "";
			for (int i = 0; i < namesList.length; i++) {
				String name = namesList[i];
				names = names + name + ", ";
			}

			extractI0Data(names);
			extractItData(names);

			ILazyDataset xscannableDS = extractXScannableData(names);
			Dataset xdata = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(xscannableDS));
			xAxisLengthFromFile = xdata.getShape()[1];

			ILazyDataset yscannableDS = extractYScannableData(names);
			Dataset ydata = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(yscannableDS));
			yAxisLengthFromFile = ydata.getShape()[0];

			double[] x = (double[]) xdata.getBuffer();
			double[] y = (double[]) ydata.getBuffer();

			ILazyDataset zscannableDS = extractZScannableData(names);

			Dataset zdata = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(zscannableDS));
			zValue = Double.parseDouble(zdata.getString(0));

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

	private ILazyDataset extractZScannableData(String names) {
		ILazyDataset zscannableDS = null;

		// If there are multiple table 3 y values, this must have been a table 3 map.
		// Therefore, in that case, we are interested in the table 3 z value.
		if (names.contains("/entry1/instrument/Stage3/table_y")) {
			ILazyDataset stage3yscannableDS = dataHolder.getLazyDataset("/entry1/instrument/Stage3/table_y");
			if (stage3yscannableDS.getSize() > 1 && names.contains("/entry1/instrument/Stage3/table_z"))
				return dataHolder.getLazyDataset("/entry1/instrument/Stage3/table_z");
		}

		if (names.contains("/entry1/instrument/sc_sample_z/sc_sample_z"))
			zscannableDS = dataHolder.getLazyDataset("/entry1/instrument/sc_sample_z/sc_sample_z");
		else if (names.contains("/entry1/instrument/Sample_Stage/sc_sample_z"))
			zscannableDS = dataHolder.getLazyDataset("/entry1/instrument/Sample_Stage/sc_sample_z");
		else if (names.contains("/entry1/instrument/table_z/table_z"))
			zscannableDS = dataHolder.getLazyDataset("/entry1/instrument/table_z/table_z");
		else if (names.contains("/entry1/instrument/SampleMotors/" + zScannableName))
			zscannableDS = dataHolder.getLazyDataset("/entry1/instrument/SampleMotors/" + zScannableName);

		return zscannableDS;
	}

	private ILazyDataset extractYScannableData(String names) {
		ILazyDataset yscannableDS = null;

		if (names.contains("/entry1/instrument/sc_MicroFocusSampleY/sc_MicroFocusSampleY"))
			yscannableDS = dataHolder
					.getLazyDataset("/entry1/instrument/sc_MicroFocusSampleY/sc_MicroFocusSampleY");
		else if (names.contains("/entry1/instrument/sc_MicroFocusSampleY"))
			yscannableDS = dataHolder.getLazyDataset("/entry1/instrument/sc_MicroFocusSampleY");
		else if (names.contains("/entry1/instrument/table_y/table_y"))
			yscannableDS = dataHolder.getLazyDataset("/entry1/instrument/table_y/table_y");
		else if (names.contains("/entry1/instrument/SampleMotors/" + yScannableName))
			yscannableDS = dataHolder.getLazyDataset("/entry1/instrument/SampleMotors/" + yScannableName);

		if (names.contains("/entry1/instrument/Stage3/table_y")) {
			ILazyDataset table3yscannableDS = dataHolder.getLazyDataset("/entry1/instrument/Stage3/table_y");
			// Perhaps we ought to return the table 3 dataset. Let's see...
			if (yscannableDS == null)
				return table3yscannableDS;
			if (yscannableDS.getSize() == 1 && table3yscannableDS.getSize() > 1) {
				// The table 1 dataset contains a single value (i.e. is a zero-dimensional array)
				// but the table 3 dataset contains many values. Therefore we suspect that we are
				// interested in the table 3 dataset rather than the table 1 dataset.
				return table3yscannableDS;
			}
		}

		return yscannableDS;
	}

	private void extractItData(String names) {
		if (names.contains("/entry1/counterTimer01/It")) {
			ILazyDataset itDS = dataHolder.getLazyDataset("/entry1/counterTimer01/It");
			itdata = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(itDS));
		} else if (names.contains("/entry1/raster_counterTimer01/It")) {
			ILazyDataset itDS = dataHolder.getLazyDataset("/entry1/raster_counterTimer01/It");
			itdata = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(itDS));
		} else if (names.contains("/entry1/counterTimer01/It")) {
			ILazyDataset itDS = dataHolder.getLazyDataset("/entry1/instrument/counterTimer01/It");
			itdata = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(itDS));
		} else if (names.contains("/entry1/raster_counterTimer01/It")) {
			ILazyDataset itDS = dataHolder.getLazyDataset("/entry1/instrument/raster_counterTimer01/It");
			itdata = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(itDS));
		} else if (names.contains("/entry1/qexafs_counterTimer01/It")) {
			ILazyDataset itDS = dataHolder.getLazyDataset("/entry1/qexafs_counterTimer01/It");
			itdata = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(itDS));
		}
	}

	private void extractI0Data(String names) {
		if (names.contains("/entry1/counterTimer01/I0")) {
			ILazyDataset i0DS = dataHolder.getLazyDataset("/entry1/counterTimer01/I0");
			i0data = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(i0DS));
		} else if (names.contains("/entry1/raster_counterTimer01/I0")) {
			ILazyDataset i0DS = dataHolder.getLazyDataset("/entry1/raster_counterTimer01/I0");
			i0data = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(i0DS));
		} else if (names.contains("/entry1/instrument/counterTimer01/I0")) {
			ILazyDataset i0DS = dataHolder.getLazyDataset("/entry1/instrument/counterTimer01/I0");
			i0data = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(i0DS));
		} else if (names.contains("/entry1/instrument/raster_counterTimer01/I0")) {
			ILazyDataset i0DS = dataHolder.getLazyDataset("/entry1/instrument/raster_counterTimer01/I0");
			i0data = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(i0DS));
		} else if (names.contains("/entry1/qexafs_counterTimer01/I0")) {
			ILazyDataset itDS = dataHolder.getLazyDataset("/entry1/qexafs_counterTimer01/I0");
			i0data = DatasetUtils.convertToDataset(getDatasetFromLazyDataset(itDS));
		}
	}

	private ILazyDataset extractXScannableData(String names) {
		// hack warning!!! yuck....
		// TODO when writing these files in the first place we need some redirection with standard names for x-axis
		// and y-axis of maps and not using the scannable names hardcoded here which can change

		ILazyDataset xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/" + xScannableName + "/" + xScannableName);
		if (xscannableDS == null) {

			if (names.contains("/entry1/instrument/trajectoryX/value"))
				xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/trajectoryX/value");
			else if (names.contains("/entry1/instrument/traj3SampleX/traj3SampleX"))
				xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/traj3SampleX/traj3SampleX");
			else if (names.contains("/entry1/instrument/traj1SampleX/traj1SampleX"))
				xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/traj1SampleX/traj1SampleX");
			else if (names.contains("/entry1/instrument/traj1ContiniousX/value"))
				xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/traj1ContiniousX/value");
			else if (names.contains("/entry1/instrument/traj1ContiniousX/traj1ContiniousX"))
				xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/traj1ContiniousX/traj1ContiniousX");
			else if (names.contains("/entry1/instrument/traj3ContiniousX/value"))
				xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/traj3ContiniousX/value");
			else if (names.contains("/entry1/instrument/traj3ContiniousX/traj3ContiniousX"))
				xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/traj3ContiniousX/traj3ContiniousX");
			else if (names.contains("/entry1/instrument/table_x/table_x"))
				xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/table_x/table_x");
			else if (names.contains("/entry1/instrument/SampleMotors/" + xScannableName))
				xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/SampleMotors/" + xScannableName);
		}

		if (names.contains("/entry1/instrument/Stage3/table_x")) {
			ILazyDataset table3xscannableDS = dataHolder.getLazyDataset("/entry1/instrument/Stage3/table_x");
			// Perhaps we ought to return the table 3 dataset. Let's see...
			if (xscannableDS == null)
				return table3xscannableDS;
			if (xscannableDS.getSize() == 1 && table3xscannableDS.getSize() > 1) {
				// The table 1 dataset contains a single value (i.e. is a zero-dimensional array)
				// but the table 3 dataset contains many values. Therefore we suspect that we are
				// interested in the table 3 dataset rather than the table 1 dataset.
				return table3xscannableDS;
			}
		}

		return xscannableDS;
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

	public void setBeanFilePath(String file) {
		beanFile = file;
	}

	public Dataset getI0data() {
		return i0data;
	}

	public Dataset getItdata() {
		return itdata;
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

	public Integer getSelectedChannel() {
		return selectedChannel;
	}

	public void setSelectedChannel(Integer selectedChannel) {
		this.selectedChannel = selectedChannel;
	}
}
