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

package uk.ac.gda.epics.client.mythen.views;

import gda.device.DeviceException;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.progress.IProgressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataListener;
import uk.ac.gda.client.hrpd.typedpvscannables.EpicsEnumPVScannable;

/**
 * An implementation of <code>IRunnableWithProgress</code> interface whose instances 
 * are intended to be used to report EPICS area detector acquiring progress.
 * 
 * the <code>run</code> method is implemented here to update progress monitor with data 
 * pulled from EPICS PVs provided by an external EPICS process.
 *  
 * The <code>run</code> method is usually not invoked directly, but rather by
 * passing the instance of <code>EpicsRunableWithProgress</code> to the <code>run</code> 
 * method of an <code>IRunnableContext</code>, which provides the UI for the progress 
 * monitor and Cancel button.
 * 
 * for example to add it to eclipse's {@link IProgressService}:
 * {@code
 * IProgressService service = (IProgressService) workbenchpart.getSite().getService(IProgressService.class);
 * service.run(true, true, epicsdetectormonitor);
 * }
 * Because the actual process is external, this method call must be triggered by EPICS detector start command when used. 
 *<p>
 *Example of Spring configuration:
 *<pre>
 * {@code
<bean id="epicsdetectormonitor" class="uk.ac.gda.devices.mythen.visualisation.views.EpicsDetectorRunableWithProgress">
	<property name="exposureTimeListener" ref="exposureTimeListener"/> <!--essential-->
	<property name="timeRemainingListener" ref="timeRemainingListener"/> <!--essential-->
	<property name="epicsProcessName" value="mythen acquiring"/> <!--essential-->
	<property name="stopScannable" ref="stopscannable"/> <!--essential-->
</bean>
 * }
 * </pre>
 * where ref bean may look like the following:
 * <pre> 
 * {@code 
<bean id="stopscannable" class="gda.device.scannable.EpicsScannable">
	<property name="pvName" value="BL11I-EA-DET-03:DET:Acquire"/>
</bean>
<bean id="timeRemainingListener" class="uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataListener">
	<property name="pvName" value="BL11I-EA-DET-03:DET:TimeRemaining_RBV"/>
</bean>
<bean id="exposureTimeListener" class="uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataListener">
	<property name="pvName" value="BL11I-EA-DET-03:DET:AcquireTime_RBV"/>
</bean>
 * }
 * </pre>
 * 
 * @see IRunnableContext
 */
public class EpicsDetectorRunableWithProgress implements IRunnableWithProgress, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(EpicsDetectorRunableWithProgress.class);
	//Spring configurable properties
	private EpicsDoubleDataListener exposureTimeListener;
	private EpicsDoubleDataListener timeRemainingListener;
	private String epicsProcessName; //task name
	private EpicsEnumPVScannable stopScannable;
	
	public EpicsDetectorRunableWithProgress() {
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			int totalWork = (int) (getExposureTimeListener().getValue()*1000);
			//when exposure time less than 1 second, do not show progress.
			if (totalWork<1000) return; 
			monitor.beginTask(getEpicsProcessName(), totalWork);
			int lastWorked = 0;
			int work = totalWork-(int) (getTimeRemainingListener().getValue()*1000);
			while (work < totalWork) {
				if (monitor.isCanceled()) {
					if (getStopScannable() != null) {
						try {
							getStopScannable().moveTo(0);
						} catch (DeviceException e) {
							logger.error("Failed to stop EPICS operation.", e);
						}
					}
					throw new InterruptedException(getEpicsProcessName() + " is aborted by progress cancel operation.");
				}
				if (work != lastWorked) {
					monitor.worked(work - lastWorked);
					lastWorked = work;
				}
				Thread.sleep(1000);
				work = totalWork-(int) (getTimeRemainingListener().getValue()*1000);
			}
		} finally {
			monitor.done();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getExposureTimeListener() == null) {
			throw new IllegalArgumentException("exposureTimeListener must not be null.");
		}
		if (getTimeRemainingListener() == null) {
			throw new IllegalArgumentException("timeRemainingListener must not be null.");
		}
	}

	public String getEpicsProcessName() {
		return epicsProcessName;
	}

	public void setEpicsProcessName(String epicsProcessName) {
		this.epicsProcessName = epicsProcessName;
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

	public EpicsEnumPVScannable getStopScannable() {
		return stopScannable;
	}

	public void setStopScannable(EpicsEnumPVScannable stopScannable) {
		this.stopScannable = stopScannable;
	}
}
