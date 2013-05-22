/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer.composites;

import gda.observable.Observable;
import gda.observable.Observer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.richbeans.components.scalebox.StandardBox;

public class CameraStatus extends Composite {
	static final Logger logger = LoggerFactory.getLogger(CameraStatus.class);
	private Label lblAcquireState;
	private StandardBox acquireTimeBox;
	private Observable<String> stateObservable;
	private Observer<String> stateObserver;
	private Observable<Double> timeObservable;
	private Observer<Double> timeObserver;
	private Button btnStart;

	public CameraStatus(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Group group = new Group(this, SWT.NONE);
		group.setText("Camera");
		group.setLayout(new GridLayout(2, false));
		
		btnStart = new Button(group, SWT.NONE);
		btnStart.setText("Start");
		
		
		lblAcquireState = new Label(group, SWT.NONE);
		lblAcquireState.setText("acquireState");
		
		acquireTimeBox = new StandardBox(group, SWT.NONE);
		acquireTimeBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		acquireTimeBox.setLabelWidth(60);
		acquireTimeBox.setActive(true);
		acquireTimeBox.setUnit("s");
		acquireTimeBox.setNumericValue(10.0);
		acquireTimeBox.setLabel("Exp.Time");
		acquireTimeBox.setToolTipText("Exposure time in seconds");
		acquireTimeBox.setDecimalPlaces(3);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if( stateObservable != null && stateObserver != null)
					stateObservable.removeObserver(stateObserver);
				if( timeObservable != null && timeObserver != null)
					timeObservable.removeObserver(timeObserver);
			}
		});
	}

	public void setADController(final ADController adController) throws Exception {
		stateObservable = adController.getAdBase().createAcquireStateObservable();
		stateObserver = new Observer<String>() {
			
			@Override
			public void update(Observable<String> source, final String arg) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						boolean acquisitionStopped = arg.equals("Done");
						lblAcquireState.setText(acquisitionStopped ? "Stopped": "Acquiring");
						btnStart.setEnabled(acquisitionStopped);
					}
				});
			}
		};
		stateObservable.addObserver(stateObserver);
		timeObservable =  adController.getAdBase().createAcquireTimeObservable();
		timeObserver = new Observer<Double>() {
			
			@Override
			public void update(Observable<Double> source, final Double arg) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						acquireTimeBox.off();
						acquireTimeBox.setValue(arg);
						acquireTimeBox.on();
					}
				});
				
			}
		};
		timeObservable.addObserver(timeObserver);

		btnStart.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final double exposureTime = acquireTimeBox.getNumericValue();
				ProgressMonitorDialog pd = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
				try {
					pd.run(true /* fork */, true /* cancelable */, new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) {
							String title = "Setting exposure time to " + exposureTime;

							monitor.beginTask(title, 100);
							try {
								adController.setExposure(exposureTime);
							} catch (Exception e) {
								logger.error("Error setting exposureTime ", e);
							}
							monitor.done();
						}
					});
				} catch (Exception e1) {
					logger.error("Error setting exposureTime ", e1);
				}
			}
		});
		acquireTimeBox.on();
	}

}
