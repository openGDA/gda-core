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
import gda.observable.IObserver;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;
import uk.ac.gda.client.hrpd.typedpvscannables.EpicsEnumPVScannable;
/**
 * A progress monitor composite for monitoring and reporting an EPICS area detector acquiring progress.
 * 
 * The actual monitor process is started by trigger from EPICS detector's 'start' PV. The monitor
 * provides a label displaying the task name, and a progress indicator to show progress.
 * 
 * <p>
 * To use this class, one must provide:
 * <li> an EPICS detector start listener using <code>setStartListener(EpicsIntegerDataListener)</code> and add an instance of this class as observer of it;</li>
 * <li> an EPICS detector exposure time listener using <code>setExposureTimeListener(EpicsDoubleDataListener)</code> which must be configured to poll;</li>
 * <li> an EPICS detector time remaining listener using <code>setTimeRemainingListener(EpicsDoubleDataListener)</code> which must be configured to poll;</li>
 * <li> a STOP scannable using <code>setStopScannable(EpicsScannable)</code> to support CANCEL operation;</li>
 * <li> a task name using <code>setTaskName(String)</code>.
 * </p>
 *  
 */

public class EpicsDetectorProgressMonitor extends Composite implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(EpicsDetectorProgressMonitor.class);
	//Spring configurable properties
	private EpicsEnumDataListener startListener;
	private EpicsDoubleDataListener exposureTimeListener;
	private EpicsDoubleDataListener timeRemainingListener;
	private EpicsEnumPVScannable stopScannable;  
	private String taskName;
	
	private ProgressMonitorPart monitor;
	
	private ExecutorService executor;
	
	public EpicsDetectorProgressMonitor(Composite parent, int style) {
		super(parent, style);
		monitor=new ProgressMonitorPart(parent, null, true);
	}
	
	public void initialise() {
		if (getStartListener()!=null) {
			getStartListener().addIObserver(this);
		} else {
			throw new IllegalStateException("Detector start listener is required, but not set.");
		}
		executor= Executors.newSingleThreadExecutor();
	}
	
	@Override
	public void dispose() {
		executor.shutdown();
		try {
			boolean terminated = executor.awaitTermination(30, TimeUnit.SECONDS);
			if (!terminated) {
				throw new TimeoutException("failed to terminate executor in time.");
			}
		} catch (InterruptedException | TimeoutException e) {
			logger.error("EPICS detector monitor failed in executor shutdown.", e);
			throw new RuntimeException("EPICS detector monitor fail in executor shutdown.", e);
		}
		if (getStartListener()!=null) {
			getStartListener().deleteIObserver(this);
		}
		super.dispose();
	}
	//using Callable instead of Runnable as progress cancel is expected to throw InterruptedException.
	Callable<Object> callable=new Callable<Object>() {
		
		@Override
		public Object call() throws Exception {
			try {
				int totalWork = (int) (getExposureTimeListener().getValue()*1000);
				//when exposure time less than 1 second, do not show progress.
				if (totalWork<1000) return null; 
				monitor.beginTask(getTaskName(), totalWork);
				int lastWorkedTo = 0;
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
						throw new InterruptedException(getTaskName() + " is aborted by progress cancel operation.");
					}
					if (work != lastWorkedTo) {
						monitor.worked(work - lastWorkedTo);
						lastWorkedTo = work;
					}
					Thread.sleep(1000);
					work = totalWork-(int) (getTimeRemainingListener().getValue()*1000);
				}
			} finally {
				monitor.done();
			}
			return null;
		}
	};

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	@Override
	public void update(Object source, Object arg) {
		if (source==getStartListener() && arg instanceof Integer) {
			if (((Integer)arg).intValue()==1) {
				executor.submit(callable);
			}
		}
	}

	private String getTaskName() {
		return this.taskName;
	}

	public EpicsEnumPVScannable getStopScannable() {
		return stopScannable;
	}

	public void setStopScannable(EpicsEnumPVScannable stopScannable) {
		this.stopScannable = stopScannable;
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

	public EpicsEnumDataListener getStartListener() {
		return startListener;
	}

	public void setStartListener(EpicsEnumDataListener startListener) {
		this.startListener = startListener;
	}
}
