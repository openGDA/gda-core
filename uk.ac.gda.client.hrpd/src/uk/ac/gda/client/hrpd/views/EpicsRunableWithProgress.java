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

package uk.ac.gda.client.hrpd.views;

import gda.device.DeviceException;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsIntegerDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsStringDataListener;
import uk.ac.gda.client.hrpd.typedpvscannables.EpicsEnumPVScannable;
/**
 * An implementation of <code>IRunnableWithProgress</code> interface whose instances 
 * are intended to be used to monitor a long-running operation in EPICS IOC.
 * 
 * Rather than directly run the long-running operation, the <code>run</code> method 
 * is used to update progress monitor with data pulled from the monitored PVs provided 
 * by an external EPICS process.
 *  
 * The <code>run</code> method is usually not invoked directly, but rather by
 * passing the instance of <code>EpicsRunableWithProgress</code> to the <code>run</code> 
 * method of an <code>IRunnableContext</code>, which provides the UI for the progress 
 * monitor and Cancel button.
 *<p>
 *Example of Spring configuration:
 *<pre>
 * {@code
 * <bean id="epicsprogressmonitor" class="uk.ac.gda.client.hrpd.views.EpicsRunableWithProgress">
 * 	<property name="totalWorkListener" ref="totalworklistener"/> <!--essential-->
 * 	<property name="workedSoFarListener" ref="worklistener"/> <!--essential-->
 * 	<property name="messageListener" ref="messagelistener"/> <!--optional-->
 * 	<property name="epicsProcessName" value="cvscan"/> <!--optional-->
 * 	<property name="stopScannable" ref="stopscannable"/> <!--optional-->
 * </bean>
 * }
 * </pre>
 * where ref bean may look like the following:
 * <pre> 
 * {@code 
 * <bean id="stopscannable" class="gda.device.scannable.EpicsScannable">
 * 	<property name="pvName" value="BL11I-EA-MAC-01:ABORT"/>
 * </bean>
 * <bean id="messagelistener" class="uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsStringDataListener">
 * 	<property name="pvName" value="BL11I-EA-MAC-01:MESSAGE"/>
 * </bean>
 * <bean id="worklistener" class="uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsIntegerDataListener">
 * 	<property name="pvName" value="BL11I-EA-MAC-01:GPULSES"/>
 * </bean>
 * <bean id="totalworklistener" class="uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsIntegerDataListener">
 * 	<property name="pvName" value="BL11I-EA-MAC-01:NPULSES"/>
 * </bean>
 * }
 * </pre>
 * 
 * @see IRunnableContext
 */
public class EpicsRunableWithProgress implements IRunnableWithProgress, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(EpicsRunableWithProgress.class);
	//Spring configurable properties
	private EpicsIntegerDataListener totalWorkListener; // must have
	private EpicsIntegerDataListener workedSoFarListener; // must have
	private EpicsStringDataListener messageListener; // optional, must handle null
	private String epicsProcessName; //task name
	private EpicsEnumPVScannable stopScannable; // optional if no Cancel,
	
	private int totalWork;
	private int work;
	private int lastWorked;

	public EpicsRunableWithProgress() {
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		try {
			totalWork = getTotalWorkListener().getValue();
			monitor.beginTask(getEpicsProcessName(), totalWork);
			lastWorked = 0;
			work = getWorkedSoFarListener().getValue();
			while (work < totalWork) {
				if (monitor.isCanceled()) {
					if (getStopScannable() != null) {
						try {
							getStopScannable().moveTo(1);
						} catch (DeviceException e) {
							logger.error("Failed to stop EPICS operation.", e);
						}
					}
					throw new InterruptedException(getEpicsProcessName() + " is aborted by progress cancel operation.");
				}
				if (work != lastWorked) {
					monitor.worked(work - lastWorked);
					if (getMessageListener() != null) {
						monitor.subTask(getMessageListener().getValue());
					}
					lastWorked = work;
				}
				Thread.sleep(1000);
				work = getWorkedSoFarListener().getValue();
			}
		} finally {
			monitor.done();
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getTotalWorkListener() == null) {
			throw new IllegalArgumentException("totalWorkListener must not be null.");
		}
		if (getWorkedSoFarListener() == null) {
			throw new IllegalArgumentException("workedSoFarListener must not be null.");
		}
	}

	public String getEpicsProcessName() {
		return epicsProcessName;
	}

	public void setEpicsProcessName(String epicsProcessName) {
		this.epicsProcessName = epicsProcessName;
	}

	public EpicsEnumPVScannable getStopScannable() {
		return stopScannable;
	}

	public void setStopScannable(EpicsEnumPVScannable stopScannable) {
		this.stopScannable = stopScannable;
	}

	public EpicsIntegerDataListener getTotalWorkListener() {
		return totalWorkListener;
	}

	public void setTotalWorkListener(EpicsIntegerDataListener totalWorkListener) {
		this.totalWorkListener = totalWorkListener;
	}

	public EpicsIntegerDataListener getWorkedSoFarListener() {
		return workedSoFarListener;
	}

	public void setWorkedSoFarListener(EpicsIntegerDataListener workedSoFarListener) {
		this.workedSoFarListener = workedSoFarListener;
	}

	public EpicsStringDataListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(EpicsStringDataListener messageListener) {
		this.messageListener = messageListener;
	}

}
