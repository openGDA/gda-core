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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class IOCStatus extends Composite {

	private Observable<Boolean> connectionStateObservable;
	private Observer<Boolean> observer;
	private Label label;

	public IOCStatus(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Group group = new Group(this, SWT.NONE);
		group.setText("IOC Status");
		group.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		label = new Label(group, SWT.CENTER);
		label.setText("Not Connected");
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if( connectionStateObservable != null && observer!=null)
					connectionStateObservable.deleteIObserver(observer);
			}
		});
	}

	public void setObservable(Observable<Boolean> connectionStateObservable) throws Exception {
		this.connectionStateObservable = connectionStateObservable;
		observer = new Observer<Boolean>() {
			
			@Override
			public void update(Observable<Boolean> source, final Boolean arg) {
				if(isDisposed())
					return;
				Display.getDefault().asyncExec(new Runnable(){

					@Override
					public void run() {
						label.setText(arg? "Connected" : "Disconnected");
					}});
			}
		};
		connectionStateObservable.addObserver(observer);
	}
}
