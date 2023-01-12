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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.observable.IObserver;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsByteArrayAsStringDataListener;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsEnumDataListener;
/**
 * A progress monitor composite for monitoring and reporting an EPICS area detector acquiring progress.
 * 
 * The actual monitor process is started by trigger from EPICS detector's 'start' PV. The monitor
 * provides a label displaying the task name, and a progress indicator to show progress.
 * 
 * As the EPICS area detector does not implement the real time remaining PV, the progress indicator uses
 * indeterminate Progress Bar in this case.
 * 
 * <p>
 * To use this class, one must provide:
 * <li> an EPICS detector start listener using <code>setStartListener(EpicsIntegerDataListener)</code> and add an instance of this class as observer of it;</li>
 * <li> an EPICS detector status message listener using <code>setMessageListener(EpicsStringDataListener)</code> which must be configured to poll;</li>
 * <li> a STOP scannable using <code>setStopScannable(EpicsScannable)</code> to support CANCEL operation;</li>
 * <li> a task name using <code>setTaskName(String)</code>.
 * </p>
 *  
 */

public class EpicsDetectorProgressMonitor extends Composite implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(EpicsDetectorProgressMonitor.class);
	//Spring configurable properties
	private EpicsEnumDataListener startListener;
	private EpicsByteArrayAsStringDataListener messageListener; // optional, must handle null
	private Scannable stopScannable;  
	private Button stopButton;

	// The progress monitor
	private ProgressIndicator progressIndicator;
	private String taskName;
	private Label fLabel;
	
	public EpicsDetectorProgressMonitor(Composite parent, int style, boolean allowStopButton) {
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
	
	private Button createCancelButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.addSelectionListener(listener);
    	final Image stopImage = ImageDescriptor.createFromFile(
    			EpicsDetectorProgressMonitor.class, "icons/stop.gif").createImage(getDisplay()); //$NON-NLS-1$
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
			}
		}
	};
	public void initialise() {
		if (getStartListener()!=null) {
			getStartListener().addIObserver(this);
		} else {
			throw new IllegalStateException("Detector start listener is required, but not set.");
		}
		if (getMessageListener()!=null) {
			getMessageListener().addIObserver(this);
		} else {
			throw new IllegalStateException("Detector start listener is required, but not set.");
		}
		fLabel.setText(getTaskName() + ": "+getMessageListener().getValue());
	}
	
	@Override
	public void dispose() {
		if (getStartListener()!=null) {
			getStartListener().deleteIObserver(this);
		}
		if (getMessageListener()!=null) {
			getMessageListener().deleteIObserver(this);
		}
		super.dispose();
	}


	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	@Override
	public void update(Object source, final Object arg) {
		if (source==getStartListener() && arg instanceof Short) {
			if (((Short)arg).intValue()==1) {
				getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						progressIndicator.beginAnimatedTask();
						stopButton.setEnabled(true);
					}
				});
			} else if (((Short)arg).intValue()==0) {
				getDisplay().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						progressIndicator.done();	
						stopButton.setEnabled(false);
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

	private String getTaskName() {
		return this.taskName;
	}

	public Scannable getStopScannable() {
		return stopScannable;
	}

	public void setStopScannable(Scannable stopScannable) {
		this.stopScannable = stopScannable;
	}

	public EpicsEnumDataListener getStartListener() {
		return startListener;
	}

	public void setStartListener(EpicsEnumDataListener startListener) {
		this.startListener = startListener;
	}

	public EpicsByteArrayAsStringDataListener getMessageListener() {
		return messageListener;
	}

	public void setMessageListener(EpicsByteArrayAsStringDataListener messageListener) {
		this.messageListener = messageListener;
	}
}
