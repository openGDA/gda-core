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

import gda.device.detector.areadetector.v17.NDROI;
import gda.observable.Observable;
import gda.observable.Observer;

import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

public class TwoDArrayROI extends Composite {
	private static final String SWITCH_ON = "Start";
	private static final String SWITCH_OFF = "Stop ";
	private static final Logger logger = LoggerFactory.getLogger(TwoDArrayROI.class);
	private boolean roiActive = false;
	private Button monitoringBtn;
	private Label monitoringLbl;
	private boolean enableRBV;
	private boolean enableX;
	private boolean enableY;
	private Observable<String> enableObservable;
	private Observer<String> enableObserver;
	private Composite runningCmp;
	NDROI ndRoi;
	private ValueBox1<Integer> minX;
	private ValueBox1<Integer> minY;
	private ValueBox1<Integer> sizeX;
	private ValueBox1<Integer> sizeY;
	private Observable<String> enableXObservable;
	private Observer<String> enableXObserver;
	private Observable<String> enableYObservable;
	private Observer<String> enableYObserver;
	private IPlottingSystem plottingSystem;
	private Button btnSetToView;
	private Composite minmaxcmp;
	private Composite minmaxAndBtnCmp;

	public TwoDArrayROI(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		Group stateGroup = new Group(this, SWT.NONE);
		stateGroup.setText("Array Source ROI");
		GridLayout gl_stateGroup = new GridLayout(1, false);
		gl_stateGroup.verticalSpacing = 0;
		gl_stateGroup.marginWidth = 0;
		gl_stateGroup.marginHeight = 0;
		stateGroup.setLayout(gl_stateGroup);

		runningCmp = new Composite(stateGroup, SWT.NONE);
		runningCmp.setLayout(new GridLayout(2, false));
		runningCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		monitoringLbl = new Label(runningCmp, SWT.CENTER);
		monitoringLbl.setText("Running__");

		monitoringBtn = new Button(runningCmp, SWT.NONE);
		monitoringBtn.setAlignment(SWT.LEFT);
		monitoringBtn.setText(SWITCH_OFF);
		
		
		minmaxAndBtnCmp = new Composite(stateGroup, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true,false).applyTo(minmaxAndBtnCmp);
		GridLayout gl_composite_2 = new GridLayout(2, false);
		gl_composite_2.marginLeft = 5;
		gl_composite_2.marginHeight = 0;
		gl_composite_2.verticalSpacing = 0;
		gl_composite_2.marginWidth = 0;
		gl_composite_2.horizontalSpacing = 0;
		minmaxAndBtnCmp.setLayout(gl_composite_2);
		
		minmaxcmp = new Composite(minmaxAndBtnCmp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true,false).applyTo(minmaxcmp);

		btnSetToView = new Button(minmaxAndBtnCmp, SWT.NONE);
		btnSetToView.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					setAxesRanges();
					start();
				} catch (Exception e1) {
					logger.error("Error setting to axes ranges", e1);
				}
			}
		});
		btnSetToView.setText("Use\nVisible\nand\nStart");
		GridDataFactory.fillDefaults().applyTo(btnSetToView);
		
		
		
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		gl_composite.horizontalSpacing = 0;
		minmaxcmp.setLayout(gl_composite);

		
		
		
		minX = new ValueBox1<Integer>(minmaxcmp, SWT.NONE);
		((GridData) minX.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		minX.setLabel("Min X");
		minX.setLabelWidth(50);
		minX.setDecimalPlaces(0);
		minX.setMaximum(Double.MAX_VALUE);
		minX.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		new Label(minX, SWT.NONE);
		minX.addValueListener(new ValueAdapter("x value listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				int minx = (int) e.getDoubleValue();
				try {
					ndRoi.setMinX(minx);
				} catch (Exception e1) {
					logger.error("Error setting minX", e1);
				}
			}
		});

		minY = new ValueBox1<Integer>(minmaxcmp, SWT.NONE);
		((GridData) minY.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		minY.setLabel("Min Y");
		minY.setLabelWidth(50);
		minY.setDecimalPlaces(0);
		minY.setMaximum(Double.MAX_VALUE);
		minY.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		new Label(minY, SWT.NONE);
		minY.addValueListener(new ValueAdapter("x value listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				int minY = (int) e.getDoubleValue();
				try {
					ndRoi.setMinY(minY);
				} catch (Exception e1) {
					logger.error("Error setting minY", e1);
				}
			}
		});

		sizeX = new ValueBox1<Integer>(minmaxcmp, SWT.NONE);
		((GridData) sizeX.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		sizeX.setLabel("Size X");
		sizeX.setLabelWidth(50);
		sizeX.setDecimalPlaces(0);
		sizeX.setMaximum(Double.MAX_VALUE);
		sizeX.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		new Label(sizeX, SWT.NONE);
		sizeX.addValueListener(new ValueAdapter("x value listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				int sizeX = (int) e.getDoubleValue();
				try {
					ndRoi.setSizeX(sizeX);
				} catch (Exception e1) {
					logger.error("Error setting sizeX", e1);
				}
			}
		});

		sizeY = new ValueBox1<Integer>(minmaxcmp, SWT.NONE);
		((GridData) sizeY.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		sizeY.setLabel("Size Y");
		sizeY.setLabelWidth(50);
		sizeY.setDecimalPlaces(0);
		sizeY.setMaximum(Double.MAX_VALUE);
		sizeY.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		new Label(sizeY, SWT.NONE);
		sizeY.addValueListener(new ValueAdapter("x value listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				int sizeY = (int) e.getDoubleValue();
				try {
					ndRoi.setSizeY(sizeY);
				} catch (Exception e1) {
					logger.error("Error setting sizeY", e1);
				}
			}
		});

		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (enableObservable != null && enableObserver != null) {
					enableObservable.removeObserver(enableObserver);
				}
				if (enableXObservable != null && enableXObserver != null) {
					enableXObservable.removeObserver(enableXObserver);
				}
				if (enableYObservable != null && enableYObserver != null) {
					enableYObservable.removeObserver(enableYObserver);
				}
			}
		});

	}

	void setEnableObservable(Observable<String> enableObservable) throws Exception {
		this.enableObservable = enableObservable;
		enableObserver = new Observer<String>() {

			@Override
			public void update(Observable<String> source, String arg) {
				handleEnableRBV(arg);
			}
		};
		enableObservable.addObserver(enableObserver);
	}

	void setEnableXObservable(Observable<String> observable) throws Exception {
		this.enableXObservable = observable;
		enableXObserver = new Observer<String>() {

			@Override
			public void update(Observable<String> source, String arg) {
				handleEnableXRBV(arg);
			}
		};
		observable.addObserver(enableXObserver);
	}

	void setEnableYObservable(Observable<String> observable) throws Exception {
		this.enableYObservable = observable;
		enableYObserver = new Observer<String>() {

			@Override
			public void update(Observable<String> source, String arg) {
				handleEnableYRBV(arg);
			}
		};
		observable.addObserver(enableYObserver);
	}

	protected void handleEnableRBV(String arg) {
		enableRBV = arg.equals("Enable");
		scheduleRoiActive();
	}

	private void scheduleRoiActive() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					setRoiActive(enableRBV && enableX && enableY);
				} catch (Exception e) {
					logger.error("Error responding to change in roi activity", e);
				}
			}

		});
	}

	protected void handleEnableXRBV(String arg) {
		enableX = arg.equals("Yes");
		scheduleRoiActive();
	}

	protected void handleEnableYRBV(String arg) {
		enableY = arg.equals("Yes");
		scheduleRoiActive();
	}

	private void setRoiActive(boolean b)  {
		roiActive = b;
		monitoringBtn.setText(b ? SWITCH_OFF : SWITCH_ON);
		monitoringLbl.setText(b ? "Running" : "Inactive");
	}

	public void setNDRoi(NDROI ndroi, IPlottingSystem abstractPlottingSystem) throws Exception {
		this.ndRoi = ndroi;
		minX.setObservable(ndroi.createMinXObservable());
		minY.setObservable(ndroi.createMinYObservable());
		sizeX.setObservable(ndroi.createSizeXObservable());
		sizeY.setObservable(ndroi.createSizeYObservable());
		minX.on();
		minY.on();
		sizeX.on();
		sizeY.on();

		setEnableObservable(ndroi.getPluginBase().createEnableObservable());
		setEnableXObservable(ndroi.createEnableXObservable());
		setEnableYObservable(ndroi.createEnableYObservable());

		monitoringBtn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				
				try {
					if (getMonitoring()) {
						ndRoi.disableX();
						ndRoi.disableY();
					} else {
						TwoDArrayROI.this.start();
					}
				} catch (Exception ex) {
					logger.error("Error responding to start_stop button", ex);
				}
				
			}
		});

		plottingSystem = abstractPlottingSystem;
		
	}

	public void addMonitoringbtnSelectionListener(SelectionListener listener) {
		monitoringBtn.addSelectionListener(listener);
	}

	public boolean getMonitoring() {
		return roiActive;
	}

	public void start() throws Exception {
		setAxesRanges();
		ndRoi.enableX();
		ndRoi.enableY();
	}

	private void setAxesRanges() throws Exception {
		List<IAxis> axes = plottingSystem.getAxes();
		for( IAxis axis : axes){
			double upper = axis.getUpper();
			double lower = axis.getLower();
			if( axis.isYAxis() ){
				ndRoi.setMinY((int) Math.min(lower,upper)  + (roiActive? ndRoi.getMinY_RBV() : 0));
				ndRoi.setSizeY((int) (Math.abs(lower-upper)));
			} else {
				ndRoi.setMinX((int) Math.min(lower,upper) + (roiActive? ndRoi.getMinX_RBV() : 0));
				ndRoi.setSizeX((int) (Math.abs(upper-lower)));
			}
		}
	}
	
}

class ValueBox1<T> extends StandardBox {

	private Observable<T> observable;
	private Observer<T> observer;
	protected T arg;

	public ValueBox1(Composite parent, int style) {
		super(parent, style);
		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (observable != null && observer != null)
					observable.removeObserver(observer);
			}
		});
	}

	public void setObservable(Observable<T> observable) throws Exception {
		this.observable = observable;
		observer = new Observer<T>() {

			private boolean scheduled = false;
			private Runnable runnable;

			@Override
			public void update(Observable<T> source, T arg) {
				ValueBox1.this.arg = arg;
				if (!scheduled) {
					if (runnable == null) {
						runnable = new Runnable() {

							@Override
							public void run() {
								scheduled = false;
								off();
								setValue(ValueBox1.this.arg);
								on();
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
