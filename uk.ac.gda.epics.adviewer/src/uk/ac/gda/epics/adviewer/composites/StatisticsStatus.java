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

import java.text.DecimalFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionListener;
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

import uk.ac.gda.richbeans.components.scalebox.StandardBox;

public class StatisticsStatus extends Composite {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(StatisticsStatus.class);
	private boolean monitoring = false;
	private Button monitoringBtn;
	private Label monitoringLbl;
	private boolean enableRBV;
	private boolean compute;
	private Observable<String> enableObservable;
	private Observable<String> computeObservable;
	private Observer<String> enableObserver;
	private Observer<String> computeObserver;
	private ValueBox min, max, mean, total, sigma;
	private Composite composite_1;

	public StatisticsStatus(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		Group stateGroup = new Group(this, SWT.NONE);
		stateGroup.setText("Statistics");
		GridLayout gl_stateGroup = new GridLayout(1, false);
		gl_stateGroup.marginWidth = 0;
		gl_stateGroup.marginHeight = 0;
		stateGroup.setLayout(gl_stateGroup);
		
		composite_1 = new Composite(stateGroup, SWT.NONE);
		composite_1.setLayout(new GridLayout(2, false));
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		monitoringLbl = new Label(composite_1, SWT.CENTER);
		monitoringLbl.setText("Running__");
		
		monitoringBtn = new Button(composite_1, SWT.NONE);
		monitoringBtn.setAlignment(SWT.LEFT);
		monitoringBtn.setText("Start");
		
		min = new ValueBox(stateGroup, SWT.NONE);
		((GridData) min.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		min.setLabel("Min");
		min.setLabelWidth(60);
		min.setDecimalPlaces(1);
		min.setMaximum(Double.MAX_VALUE);
		min.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,1,1));		
		
		max = new ValueBox(stateGroup, SWT.NONE);
		((GridData) max.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		max.setLabel("Max");
		max.setLabelWidth(60);
		max.setDecimalPlaces(1);
		max.setMaximum(Double.MAX_VALUE);
		max.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,1,1));		
		
		mean = new ValueBox(stateGroup, SWT.NONE);
		((GridData) mean.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		mean.setLabelWidth(60);
		mean.setLabel("Mean");
		mean.setDecimalPlaces(1);
		mean.setMaximum(Double.MAX_VALUE);
		mean.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,1,1));		
		
		total = new ValueBox(stateGroup, SWT.NONE);
		((GridData) total.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		total.setLabelWidth(60);
		total.setLabel("Total");
		total.setMaximum(Double.MAX_VALUE);
		total.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,1,1));		
		total.setNumberFormat(new DecimalFormat("0.###E0"));
		total.setDecimalPlaces(3);
		
		sigma = new ValueBox(stateGroup, SWT.NONE);
		((GridData) sigma.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		sigma.setLabelWidth(60);
		sigma.setLabel("Sigma");
		sigma.setMaximum(Double.MAX_VALUE);
		sigma.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,1,1));		
		sigma.setNumberFormat(new DecimalFormat("0.###E0"));
		sigma.setDecimalPlaces(3);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if(enableObservable != null && enableObserver != null ){
					enableObservable.removeObserver(enableObserver);
				}
				if(computeObservable != null && computeObserver != null ){
					computeObservable.removeObserver(computeObserver);
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
	
	
	void setComputeObservable(Observable<String> computeObservable) throws Exception{
		this.computeObservable = computeObservable;
		computeObserver = new Observer<String>() {

			@Override
			public void update(Observable<String> source, String arg) {
				handleComputeRBV(arg);
			}
		};
		computeObservable.addObserver(computeObserver);
	}
	protected void handleEnableRBV(String arg) {
		enableRBV=arg.equals("Enable");
		scheduleSetStarted();
	}

	private void scheduleSetStarted() {
		Display.getDefault().asyncExec(new Runnable(){

			@Override
			public void run() {
				setStarted(enableRBV && compute);
			}
			
		});
	}

	protected void handleComputeRBV(String arg) {
		compute=arg.equals("Yes");
		scheduleSetStarted();
	}
	private void setStarted(boolean b) {
		monitoring = b;
		monitoringBtn.setText(b ? "Stop" : "Start");
		monitoringLbl.setText(b ? "Running" : "Stopped");
	}


	public void setMinObservable(Observable<Double> observable) throws Exception {
		min.setObservable(observable);
	}


	public void setMaxObservable(Observable<Double> observable) throws Exception {
		max.setObservable(observable);
	}


	public void setMeanObservable(Observable<Double> observable)  throws Exception {
		mean.setObservable(observable);
	}


	public void setTotalObservable(Observable<Double> observable)  throws Exception {
		total.setObservable(observable);
	}


	public void setSigmaObservable(Observable<Double> observable)  throws Exception {
		sigma.setObservable(observable);
	}

	public void addMonitoringbtnSelectionListener(SelectionListener listener) {
		monitoringBtn.addSelectionListener(listener);
	}

	public boolean getMonitoring() {
		return monitoring;
	}
}

class ValueBox extends StandardBox{

	private Observable<Double> observable;
	private Observer<Double> observer;
	protected Double arg;

	public ValueBox(Composite parent, int style) {
		super(parent, style);
		addDisposeListener( new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if(observable != null && observer != null )
					observable.removeObserver(observer);
			}
		});
	}

	public void setObservable(Observable<Double> observable) throws Exception {
		this.observable = observable;
		observer = new Observer<Double>() {

			private boolean scheduled=false;
			private Runnable runnable;

			@Override
			public void update(Observable<Double> source, Double arg) {
				ValueBox.this.arg = arg;
				if( !scheduled ){
					if( runnable == null){
						runnable = new Runnable(){

							@Override
							public void run() {
								scheduled=false;
								setValue(ValueBox.this.arg);
							}
						};
					}
					Display.getDefault().asyncExec(runnable);
					
				}
			}
		};
		observable.addObserver(observer);
	}
	
}
