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

import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.observable.Observable;
import gda.observable.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;
import org.eclipse.swt.widgets.Label;

public class MinCallbackTimeComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(MinCallbackTimeComposite.class);

	private ValueBox valueBox;

	private Observer<Double> minTimeObserver;

	private Observable<Double> minTimeObservable;

	private NDPluginBase pluginBase;

	public MinCallbackTimeComposite(Composite parent, int style) {
		
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Group grpMaxUpdateRate = new Group(this, SWT.NONE);
		grpMaxUpdateRate.setText("Min Update Time");
		GridLayout gl_grpMaxUpdateRate = new GridLayout(1, false);
		gl_grpMaxUpdateRate.marginHeight = 0;
		gl_grpMaxUpdateRate.marginWidth = 0;
		grpMaxUpdateRate.setLayout(gl_grpMaxUpdateRate);
		
		valueBox = new ValueBox(grpMaxUpdateRate, SWT.NONE);
		valueBox.setLabelWidth(30);
		valueBox.setLabel("Time");
		valueBox.setDecimalPlaces(1);
		valueBox.setMaximum(Double.MAX_VALUE);
		valueBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,1,1));		
		valueBox.setUnit("s");
		
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if(minTimeObservable != null && minTimeObserver != null ){
					minTimeObservable.deleteIObserver(minTimeObserver);
				}
			}
		});
		
		
	}
	
	void setPluginBase(NDPluginBase pluginBase){
		this.pluginBase = pluginBase;
		valueBox.addValueListener(new ValueListener() {
			
			@Override
			public void valueChangePerformed(ValueEvent e) {
				try {
					MinCallbackTimeComposite.this.pluginBase.setMinCallbackTime(e.getDoubleValue());
				} catch (Exception e1) {
					logger.error("Error setting min update time", e1);
				}
			}
			
			@Override
			public String getValueListenerName() {
				return "MinCallbackTime";
			}
		});
		valueBox.on();
		
	}
	
	void setMinTimeObservable(Observable<Double> observable) throws Exception{
		this.minTimeObservable = observable;
		minTimeObserver = new Observer<Double>() {
			
			@Override
			public void update(Observable<Double> source, final Double arg) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						valueBox.off();
						valueBox.setValue(arg);
						valueBox.on();
					}
				});
				
			}
		};
		observable.addObserver(minTimeObserver);
	}

	public void setMinCallbackTime(double minCallbackTime) throws Exception {
		if( pluginBase.getMinCallbackTime_RBV() < minCallbackTime)
			pluginBase.setMinCallbackTime(minCallbackTime);
	}
}
