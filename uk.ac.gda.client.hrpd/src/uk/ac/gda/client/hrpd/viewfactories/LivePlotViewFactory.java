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

import gda.device.Scannable;
import gda.rcp.views.FindableExecutableExtension;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.ExtensionFactory;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataArrayListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsIntegerDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsStringDataListener;
import uk.ac.gda.client.hrpd.views.LivePlotView;
/**
 * a factory for creating a {@link LivePlotView} instance. 
 * 
 * an instance of this class can be configured in a Spring bean and contribute to {@link org.eclipse.ui.views} extension-point
 * before workbench itself has started (which required for a view component) using eclipse's {@link ExtensionFactory} mechanism.
 * 
 * Its sole purpose is to deliver the actual view after the eclipse workbench has started and hold the Spring configuration 
 * properties for the actual view to be created.
 *  
 * <p>
 * Example of Spring configuration:
 * <pre>
 * {@code
 * 	<bean id="macliveplotfactory" class="uk.ac.gda.client.hrpd.viewfactories.LivePlotViewFactory">
	<property name="plotName" value="MAC"/>
	<property name="xAxisMin" value="0.000"/>
	<property name="xAxisMax" value="150.000"/>
	<property name="liveDataListeners">
		<list>
			<bean class="org.javatuples.Triplet">
				<constructor-arg index="0" value="mac1"/>
				<constructor-arg index="1" ref="mac1x"/>
				<constructor-arg index="2" ref="mac1y"/>
			</bean>
			<bean class="org.javatuples.Triplet">
				<constructor-arg index="0" value="mac2"/>
				<constructor-arg index="1" ref="mac2x"/>
				<constructor-arg index="2" ref="mac2y"/>
			</bean>
			<bean class="org.javatuples.Triplet">
				<constructor-arg index="0" value="mac3"/>
				<constructor-arg index="1" ref="mac3x"/>
				<constructor-arg index="2" ref="mac3y"/>
			</bean>
			<bean class="org.javatuples.Triplet">
				<constructor-arg index="0" value="mac4"/>
				<constructor-arg index="1" ref="mac4x"/>
				<constructor-arg index="2" ref="mac4y"/>
			</bean>
			<bean class="org.javatuples.Triplet">
				<constructor-arg index="0" value="mac5"/>
				<constructor-arg index="1" ref="mac5x"/>
				<constructor-arg index="2" ref="mac5y"/>
			</bean>
		</list>
	</property>
	<property name="dataUpdatedListener" ref="pulsedone"/>
	<property name="finalDataListener">
		<bean class="org.javatuples.Triplet">
			<constructor-arg index="0" ref="allx"/>
			<constructor-arg index="1" ref="ally"/>
			<constructor-arg index="2" ref="allye"/>
		</bean>
	</property>
	<property name="detectorStateToPlotReducedData" value="Flyback"/>
	<property name="detectorStateToRunProgressService" value="Executing"/>
	<property name="detectorStateListener" ref="state"/>
	<property name="dataFilenameObserverName" value="dataFilenameObserver"/>
	<property name="lowDataBound" value="16501"/>
	<property name="highDataBound" value="65000"/>
	<property name="epicsProgressMonitor" ref="epicsprogressmonitor"/>
	<property name="totalWorkListener" ref="totalworklistener"/>
	<property name="workListener" ref="worklistener"/>
	<property name="messageListener" ref="messagelistener"/>
	<property name="stopScannable" ref="stopscannable"/>
 </bean>
	}
 *</pre>
 *</p>
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
	private String detectorStateToPlotReducedData;
	private String detectorStateToRunProgressService;

	private String dataFilenameObserverName;
	private IRunnableWithProgress epicsProgressMonitor;
	private int lowDataBound;
	private int highDataBound;

	private String name;
	
	private EpicsIntegerDataListener totalWorkListener;
	private EpicsIntegerDataListener workListener;
	private EpicsStringDataListener messageListener;
	private String taskName;
	private Scannable stopScannable;	

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
		liveplotview.setDetectorStateToPlotReducedData(getDetectorStateToPlotReducedData());
		liveplotview.setDetectorStateToRunProgressService(getDetectorStateToRunProgressService());
		liveplotview.setEpicsProgressMonitor(getEpicsProgressMonitor());
		liveplotview.setDataFilenameObserverName(getDataFilenameObserverName());
		liveplotview.setLowDataBound(getLowDataBound());
		liveplotview.setHighDataBound(getHighDataBound());
		liveplotview.setTotalWorkListener(getTotalWorkListener());
		liveplotview.setWorkListener(getWorkListener());
		liveplotview.setMessageListener(getMessageListener());
		liveplotview.setTaskName(getTaskName());
		liveplotview.setStopScannable(getStopScannable());
		
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

	public IRunnableWithProgress getEpicsProgressMonitor() {
		return epicsProgressMonitor;
	}

	public void setEpicsProgressMonitor(IRunnableWithProgress epicsProgressMonitor) {
		this.epicsProgressMonitor = epicsProgressMonitor;
	}

	public EpicsIntegerDataListener getTotalWorkListener() {
		return totalWorkListener;
	}

	public void setTotalWorkListener(EpicsIntegerDataListener totalWorkListener) {
		this.totalWorkListener = totalWorkListener;
	}

	public EpicsIntegerDataListener getWorkListener() {
		return workListener;
	}

	public void setWorkListener(EpicsIntegerDataListener workListener) {
		this.workListener = workListener;
	}

	public EpicsStringDataListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(EpicsStringDataListener messageListener) {
		this.messageListener = messageListener;
	}

	public Scannable getStopScannable() {
		return stopScannable;
	}

	public void setStopScannable(Scannable stopScannable) {
		this.stopScannable = stopScannable;
	}

	public String getDetectorStateToPlotReducedData() {
		return detectorStateToPlotReducedData;
	}

	public void setDetectorStateToPlotReducedData(String detectorStateToPlotReducedData) {
		this.detectorStateToPlotReducedData = detectorStateToPlotReducedData;
	}

	public String getDetectorStateToRunProgressService() {
		return detectorStateToRunProgressService;
	}

	public void setDetectorStateToRunProgressService(String detectorStateToRunProgressService) {
		this.detectorStateToRunProgressService = detectorStateToRunProgressService;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}
