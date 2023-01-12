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

package uk.ac.gda.epics.client.mythen.viewfactories;

import gda.device.Scannable;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.ExtensionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsByteArrayAsStringDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;
import uk.ac.gda.epics.client.mythen.views.EpicsDetectorRunableWithProgress;
import uk.ac.gda.epics.client.mythen.views.LivePlotView;
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
 * 	<bean id="mythenliveplotfactory" class="uk.ac.gda.devices.mythen.visualisation.viewfactories.LivePlotViewFactory">
	<property name="plotName" value="MYTHEN"/>
	<property name="xAxisMin" value="0.000"/>
	<property name="xAxisMax" value="100.000"/>
	<property name="eventAdminName" value="eventadmin"/>
	<property name="epicsProgressMonitor" ref="epicsprogressmonitor"/>
	<property name="startListener" ref="startListener"/>
	<property name="exposureTimeListener" ref="exposureTimeListener"/>
	<property name="timeRemainingListener" ref="timeRemainingListener"/>
	<property name="taskName" value="Mythen acquiring"/>
	<property name="stopScannable" ref="stopmythen"/>
 </bean>
	}

 * where ref bean may look like the following:
 * {@code 
	<bean id="stopmythen" class="uk.ac.gda.client.hrpd.typedpvscannables.EpicsEnumPVScannable">
		<property name="name" value="stopmythen"/>
		<property name="pvName" value="BL11I-EA-DET-03:DET:Acquire"/>
		<property name="type" value="gda.device.detector.EpicsAreaDetectorConstants.Acquire"/>
		<property name="local" value="true"/>
	</bean>
   <bean id="timeRemainingListener" class="uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataListener">
   	<property name="pvName" value="BL11I-EA-DET-03:DET:TimeRemaining_RBV"/>
   </bean>
   <bean id="exposureTimeListener" class="uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataListener">
   	<property name="pvName" value="BL11I-EA-DET-03:DET:AcquireTime_RBV"/>
   </bean>
   <bean id="startListener" class="uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener">
   	<property name="pvName" value="BL11I-EA-DET-03:DET:Acquire"/>
   </bean>
   }
 *</pre>
 *</p>
 * <li><code>eventAdminName</code> must be the name of an {@link Scriptcontroller} instance running on the GDA server.</li>
 * <li>for <code>epicsprogressmonitor</code> please see {@link EpicsDetectorRunableWithProgress}</li>
 * @see LivePlotView 
 */
public class LivePlotViewFactory implements FindableExecutableExtension {
	private static final Logger logger=LoggerFactory.getLogger(LivePlotViewFactory.class);
	
	private String plotName;
	private double xAxisMin=0.000;
	private double xAxisMax=100.000;
	private String eventAdminName;
	private IRunnableWithProgress epicsProgressMonitor;
	private EpicsEnumDataListener startListener;	
	
	private EpicsDoubleDataListener exposureTimeListener;
	private EpicsDoubleDataListener timeRemainingListener;
	private EpicsByteArrayAsStringDataListener messageListener;
	private Scannable stopScannable;
	private String taskName;

	private String name;



	@Override
	public Object create() throws CoreException {
		logger.info("Creating Live Plot View");
		LivePlotView liveplotview=new LivePlotView();
		liveplotview.setPlotName(getPlotName());
		liveplotview.setxAxisMin(getxAxisMin());
		liveplotview.setxAxisMax(getxAxisMax());
		liveplotview.setEventAdminName(getEventAdminName());
		liveplotview.setEpicsProgressMonitor(getEpicsProgressMonitor());
		liveplotview.setStartListener(getStartListener());
		liveplotview.setExposureTimeListener(getExposureTimeListener());
		liveplotview.setTimeRemainingListener(getTimeRemainingListener());
		liveplotview.setMessageListener(getMessageListener());
		liveplotview.setStopScannable(getStopScannable());
		liveplotview.setTaskName(getTaskName());
		
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
		if (startListener == null) {
			throw new IllegalArgumentException("The source of data to be plotted cannot be null.");
		}
		if (eventAdminName == null) {
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


	public IRunnableWithProgress getEpicsProgressMonitor() {
		return epicsProgressMonitor;
	}

	public void setEpicsProgressMonitor(IRunnableWithProgress epicsProgressMonitor) {
		this.epicsProgressMonitor = epicsProgressMonitor;
	}

	public Scannable getStopScannable() {
		return stopScannable;
	}

	public void setStopScannable(Scannable stopScannable) {
		this.stopScannable = stopScannable;
	}

	@Override
	public void setName(String name) {
		this.name=name;		
	}

	@Override
	public String getName() {
		return this.name;
	}

	public String getEventAdminName() {
		return eventAdminName;
	}

	public void setEventAdminName(String eventAdminName) {
		this.eventAdminName = eventAdminName;
	}

	public EpicsEnumDataListener getStartListener() {
		return startListener;
	}

	public void setStartListener(EpicsEnumDataListener startListener) {
		this.startListener = startListener;
	}

	public EpicsDoubleDataListener getExposureTimeListener() {
		return exposureTimeListener;
	}

	public void setExposureTimeListener(EpicsDoubleDataListener exposureTimeListener) {
		this.exposureTimeListener = exposureTimeListener;
	}

	public EpicsDoubleDataListener getTimeRemainingListener() {
		return timeRemainingListener;
	}

	public void setTimeRemainingListener(EpicsDoubleDataListener timeRemainingListener) {
		this.timeRemainingListener = timeRemainingListener;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public EpicsByteArrayAsStringDataListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(EpicsByteArrayAsStringDataListener messageListener) {
		this.messageListener = messageListener;
	}

	
}
