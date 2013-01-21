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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.layout.FillLayout;

public class HistogramStatus extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(HistogramStatus.class);
	private boolean histogramMonitoring = false;
	private Button histogramMonitoringBtn;
	private Label histogramMonitoringLbl;
	private boolean enableRBV;
	private boolean computeHistogram;
	private Button btnGrabOnce;
	private Observable<String> enableObservable;
	private Observable<String> computeHistogramObservable;
	private Observer<String> enableObserver;
	private Observer<String> computeHistogramObserver;
	private Button btnFreezePlot;
	protected boolean freezeSelected;


	public HistogramStatus(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		Group stateGroup = new Group(this, SWT.NONE);
		stateGroup.setText("Histogram Plot");
		GridLayout gl_stateGroup = new GridLayout(2, false);
		gl_stateGroup.marginHeight = 0;
		gl_stateGroup.marginWidth = 0;
		stateGroup.setLayout(gl_stateGroup);
		histogramMonitoringLbl = new Label(stateGroup, SWT.NONE);
		histogramMonitoringLbl.setAlignment(SWT.LEFT);
		histogramMonitoringLbl.setText("running");
		GridData gd_histogramMonitoringLbl = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_histogramMonitoringLbl.widthHint = 77;
		histogramMonitoringLbl.setLayoutData(gd_histogramMonitoringLbl);
		
		btnGrabOnce = new Button(stateGroup, SWT.NONE);
		btnGrabOnce.setText("Grab Once");
		histogramMonitoringBtn = new Button(stateGroup, SWT.PUSH | SWT.CENTER);
		histogramMonitoringBtn.setText("Start");
		GridData gd_histogramMonitoringBtn = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_histogramMonitoringBtn.widthHint = 58;
		histogramMonitoringBtn.setLayoutData(gd_histogramMonitoringBtn);
		
		btnFreezePlot = new Button(stateGroup, SWT.CHECK);
		btnFreezePlot.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnFreezePlot.setText("Freeze Plot");
		btnFreezePlot.addSelectionListener(new SelectionAdapter(){

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				freezeSelected = btnFreezePlot.getSelection();
			}});
		btnFreezePlot.setSelection(false);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if(enableObservable != null && enableObserver != null ){
					enableObservable.deleteIObserver(enableObserver);
				}
				if(computeHistogramObservable != null && computeHistogramObserver != null ){
					computeHistogramObservable.deleteIObserver(computeHistogramObserver);
				}
			}
		});

	}
	
	
	void setEnableObservable(Observable<String> enableObservable) throws Exception{
		this.enableObservable = enableObservable;
		enableObserver = new Observer<String>() {

			@Override
			public void update(Observable<String> source, String arg) {
				handleEnableRBV(arg);
			}
		};
		enableObservable.addObserver(enableObserver);
	}
	
	
	void setComputeHistogramObservable(Observable<String> computeHistogramObservable) throws Exception{
		this.computeHistogramObservable = computeHistogramObservable;
		computeHistogramObserver = new Observer<String>() {

			@Override
			public void update(Observable<String> source, String arg) {
				handleComputeHistogramRBV(arg);
			}
		};
		computeHistogramObservable.addObserver(computeHistogramObserver);
	}
	protected void handleEnableRBV(String arg) {
		enableRBV=arg.equals("Enable");
		scheduleSetStarted();
	}

	private void scheduleSetStarted() {
		Display.getDefault().asyncExec(new Runnable(){

			@Override
			public void run() {
				setStarted(enableRBV && computeHistogram);
			}
			
		});
	}

	protected void handleComputeHistogramRBV(String arg) {
		computeHistogram=arg.equals("Yes");
		scheduleSetStarted();
	}
	private void setStarted(boolean b) {
		histogramMonitoring = b;
		histogramMonitoringBtn.setText(b ? "Stop" : "Start");
		histogramMonitoringLbl.setText(b ? "Running" : "Stopped");
		btnGrabOnce.setEnabled(!histogramMonitoring);
		
	}


	public void addHistogramMonitoringbtnSelectionListener(SelectionListener selectionListener) {
		histogramMonitoringBtn.addSelectionListener(selectionListener);
	}


	public boolean getHistogramMonitoring() {
		return histogramMonitoring;
	}


	public void addGrabOnceSelectionListener(SelectionListener selectionListener) {
		btnGrabOnce.addSelectionListener(selectionListener);
	}


	/* return vaue of local variable so that this function doe snot have to be called on UI thread */
	public boolean isFreezeSelected(){
		return freezeSelected;
	}
}
