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

package uk.ac.gda.client.hrpd.viewfactories;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataArrayListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsIntegerDataListener;
import uk.ac.gda.client.hrpd.views.LivePlotView;
import gda.rcp.views.FindableExecutableExtension;
/**
 * a factory for creating {@link LivePlotView} instance. 
 * @see LivePlotView 
 */
public class LivePlotViewFactory implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(LivePlotViewFactory.class);
	private String plotName;
	private double xAxisMin=0.000;
	private double xAxisMax=150.000;
	private List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> liveDataListeners = new ArrayList<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>>();
	private EpicsIntegerDataListener dataUpdatedListener;
	private Triplet<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> finalDataListener;
	private EpicsEnumDataListener detectorStateListener;
	private String dataFilenameObserverName;
	private int lowDataBound;
	private int highDataBound;
	
	private String name;
	


	@Override
	public void setName(String name) {
		this.name=name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Object create() throws CoreException {
		logger.info("Creating Live Plot View");
		LivePlotView liveplotview=new LivePlotView();
		liveplotview.setPlotName(getPlotName());
		liveplotview.setxAxisMin(getxAxisMin());
		liveplotview.setxAxisMax(getxAxisMax());
		liveplotview.setLiveDataListeners(getLiveDataListeners());
		liveplotview.setDataUpdatedListener(getDataUpdatedListener());
		liveplotview.setFinalDataListener(getFinalDataListener());
		liveplotview.setDetectorStateListener(getDetectorStateListener());
		liveplotview.setDataFilenameObserverName(getDataFilenameObserverName());
		liveplotview.setLowDataBound(getLowDataBound());
		liveplotview.setHighDataBound(getHighDataBound());
		return liveplotview;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (plotName == null) {
			throw new IllegalArgumentException("plotName must be set for the Live Plot View.");
		}
		if (liveDataListeners == null) {
			throw new IllegalArgumentException("The source of data to be plotted cannot be null.");
		}
		if (dataUpdatedListener == null) {
			throw new IllegalArgumentException("trigger for data plotting cannot be null.");
		}
	}
	
	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public double getxAxisMin() {
		return xAxisMin;
	}

	public void setxAxisMin(double xAxisMin) {
		this.xAxisMin = xAxisMin;
	}

	public double getxAxisMax() {
		return xAxisMax;
	}

	public void setxAxisMax(double xAxisMax) {
		this.xAxisMax = xAxisMax;
	}

	public List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> getLiveDataListeners() {
		return liveDataListeners;
	}

	public void setLiveDataListeners(
			List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> liveDataListeners) {
		this.liveDataListeners = liveDataListeners;
	}

	public EpicsIntegerDataListener getDataUpdatedListener() {
		return dataUpdatedListener;
	}

	public void setDataUpdatedListener(EpicsIntegerDataListener dataUpdatedListener) {
		this.dataUpdatedListener = dataUpdatedListener;
	}

	public Triplet<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> getFinalDataListener() {
		return finalDataListener;
	}

	public void setFinalDataListener(Triplet<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> finalDataListener) {
		this.finalDataListener = finalDataListener;
	}

	public EpicsEnumDataListener getDetectorStateListener() {
		return detectorStateListener;
	}

	public void setDetectorStateListener(EpicsEnumDataListener detectorStateListener) {
		this.detectorStateListener = detectorStateListener;
	}

	public String getDataFilenameObserverName() {
		return dataFilenameObserverName;
	}

	public void setDataFilenameObserverName(String dataFilenameObserverName) {
		this.dataFilenameObserverName = dataFilenameObserverName;
	}

	public int getLowDataBound() {
		return lowDataBound;
	}

	public void setLowDataBound(int lowDataBound) {
		this.lowDataBound = lowDataBound;
	}

	public int getHighDataBound() {
		return highDataBound;
	}

	public void setHighDataBound(int highDataBound) {
		this.highDataBound = highDataBound;
	}
}
