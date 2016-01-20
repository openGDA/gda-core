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
import gda.device.Scannable;
import gda.observable.IObserver;
import gov.aps.jca.event.MonitorListener;

import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsIntegerDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsStringDataListener;

/**
 * A progress monitor for monitoring or reporting an EPICS process progress state. It provides a label displaying the
 * task and subtask name, and a progress indicator to show progress. The progress reporting is driven by EPICS events
 * monitored via {@link MonitorListener} instances,
 * <p>
 * To create an instance of this class, one must provide:
 * <li>a total work listener using <code>setTotalWorkListener(EpicsIntegerDataListener)</code>;</li>
 * <li>a work listener using <code>setWorkedSoFarListener(EpicsIntegerDataListener)</code>;</li> <br>
 * You may optionally provide:
 * <li>a message listener using <code>setMessageListener(EpicsStringDataListener)</code> if there are messages related
 * to the EPICS process;</li>
 * <li>a STOP scannable using <code>setStopScannable(EpicsScannable)</code> if you want the CANCEL operation to stop
 * EPICS process;</li>
 */

public class EpicsProcessProgressMonitor extends Composite implements IObserver, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(EpicsProcessProgressMonitor.class);
	private EpicsIntegerDataListener totalWorkListener; // must have
	private EpicsIntegerDataListener workedSoFarListener; // must have
	private EpicsStringDataListener messageListener; // optional, must handle null
	private Scannable stopScannable; 
	private Button stopButton;
	// The progress monitor
//	private ProgressMonitorPart progressMonitorPart;
	private ProgressIndicator progressIndicator;
	private String taskName;
	private Label fLabel;

	public EpicsProcessProgressMonitor(Composite parent, int style, boolean allowStopButton) {
		super(parent, style);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		this.setLayout(layout);

		// Build the separator line
		Label separator = new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Insert a progress monitor
		Composite progressMonitor=new Composite(this, SWT.NONE);
		GridData data = new GridData (SWT.FILL, SWT.CENTER, true, false);
		progressMonitor.setLayoutData(data);
		progressMonitor.setLayout(new GridLayout(2, false));
		
		progressIndicator=new ProgressIndicator(progressMonitor);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = false;
    	gd.verticalAlignment = GridData.CENTER;
//    	gd.heightHint = progressIndicatorHeight;
        if (allowStopButton) {
        	gd.horizontalSpan=1;
        } else {
        	gd.horizontalSpan=2;
        }
        progressIndicator.setLayoutData(gd);
        if (allowStopButton) {
        	stopButton=createCancelButton(progressMonitor);
        }
        
        fLabel = new Label(this, SWT.LEFT);
        fLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
	}
	
	public void initialise() {
		totalWork=getTotalWorkListener().getValue();
		fLabel.setText(getTaskName()+": "+ getMessageListener().getValue());
	}
	
	private Button createCancelButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.addSelectionListener(listener);
    	final Image stopImage = ImageDescriptor.createFromFile(
    			EpicsProcessProgressMonitor.class, "icons/stop.gif").createImage(getDisplay()); //$NON-NLS-1$
    	button.setImage(stopImage);
    	button.setCursor( getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
    	button.addDisposeListener(new DisposeListener() {
    		@Override
			public void widgetDisposed(DisposeEvent e) {
    			stopImage.dispose();
    		}
    	});
    	button.setEnabled(false);
		button.setToolTipText("Cancel current cvscan"); //$NON-NLS-1$
		return button;
	}
	
	SelectionAdapter listener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				if (getStopScannable() != null) {
					getStopScannable().stop();
				}
				if (stopButton != null) {
					stopButton.setEnabled(false);
				}
			} catch (DeviceException e1) {
				logger.error("Failed to stop EPICS operation.", e1);
			} finally {
				// on cancel operation, must finish beginTask
				progressIndicator.done();
				lastWorkedTo = 0;
			}
		}
	};


	public void addIObservers() {
		if (totalWorkListener != null) {
			totalWorkListener.addIObserver(this);
		}
		if (workedSoFarListener != null) {
			workedSoFarListener.addIObserver(this);
		}
		if (messageListener != null) {
			messageListener.addIObserver(this);
		}
	}

	@Override
	public void dispose() {
		if (totalWorkListener != null) {
			totalWorkListener.deleteIObserver(this);
		}
		if (workedSoFarListener != null) {
			workedSoFarListener.deleteIObserver(this);
		}
		if (messageListener != null) {
			messageListener.deleteIObserver(this);
		}
		super.dispose();
	}

	int lastWorkedTo = 0;
	int totalWork = 0;

	@Override
	public void update(Object source, final Object arg) {
		if (totalWork==0) {
			totalWork=totalWorkListener.getValue();
		}
		if (source == workedSoFarListener && arg instanceof Integer) {
			final int workedSoFar = (int) arg;
			if (workedSoFar==0 || workedSoFar<lastWorkedTo) {
				logger.debug("initial worked: {}, total work {}", workedSoFar, totalWork);
				getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						progressIndicator.beginTask(totalWork);
						progressIndicator.setVisible(true);
						stopButton.setEnabled(true);
					}
				});
			} else if (workedSoFar < totalWork) {
				logger.debug("worked so far: {}", workedSoFar);
				getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						progressIndicator.worked(workedSoFar - lastWorkedTo);
						lastWorkedTo = workedSoFar;
					}
				});
			} else if (workedSoFar == totalWork){
				logger.debug("work completed {}", workedSoFar);
				getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						progressIndicator.done();
						stopButton.setEnabled(false);
						lastWorkedTo = 0;
					}
				});
			}
		} else if (source == messageListener && arg instanceof String) {
			getDisplay().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					fLabel.setText(getTaskName()+ ": " +arg.toString());
				}
			});
		}
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

	public Scannable getStopScannable() {
		return stopScannable;
	}

	public void setStopScannable(Scannable stopScannable) {
		this.stopScannable = stopScannable;
	}

	public EpicsStringDataListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(EpicsStringDataListener messageListener) {
		this.messageListener = messageListener;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (totalWorkListener == null) {
			throw new IllegalArgumentException("totalWorkListener must not be null.");
		}
		if (workedSoFarListener == null) {
			throw new IllegalArgumentException("workedSoFarListener must not be null.");
		}
		if (stopScannable == null) {
			throw new IllegalArgumentException("stopScannable must not be null.");
		}
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}
