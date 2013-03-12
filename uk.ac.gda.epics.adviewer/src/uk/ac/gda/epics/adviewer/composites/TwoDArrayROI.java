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

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
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

import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.gda.richbeans.components.scalebox.StandardBox;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

public class TwoDArrayROI extends Composite {
	private static final String SWITCH_ON = "Switch On";
	private static final String SWITCH_OFF = "Switch Off";
	private static final Logger logger = LoggerFactory.getLogger(TwoDArrayROI.class);
	private boolean roiActive = false;
	private Button monitoringBtn;
	private Label monitoringLbl;
	private boolean enableRBV;
	private boolean enableX;
	private boolean enableY;
	private Observable<String> enableObservable;
	private Observer<String> enableObserver;
	private Composite composite_1;
	NDROI ndRoi;
	private ValueBox1<Integer> minX;
	private ValueBox1<Integer> minY;
	private ValueBox1<Integer> sizeX;
	private ValueBox1<Integer> sizeY;
	private Observable<String> enableXObservable;
	private Observer<String> enableXObserver;
	private Observable<String> enableYObservable;
	private Observer<String> enableYObserver;
	private String mpegROIRegionName;
	private IRegion iRegion;

	public TwoDArrayROI(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout(SWT.HORIZONTAL));
		Group stateGroup = new Group(this, SWT.NONE);
		stateGroup.setText("Array Source ROI");
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
		monitoringBtn.setText(SWITCH_OFF);

		minX = new ValueBox1<Integer>(stateGroup, SWT.NONE);
		((GridData) minX.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		minX.setLabel("Min X");
		minX.setLabelWidth(50);
		minX.setDecimalPlaces(0);
		minX.setMaximum(Double.MAX_VALUE);
		minX.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		minX.addValueListener(new ValueAdapter("x value listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				int minx = (int) e.getDoubleValue();
				try {
					ndRoi.setMinX(minx);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					logger.error("TODO put description of error here", e1);
				}
			}
		});

		minY = new ValueBox1<Integer>(stateGroup, SWT.NONE);
		((GridData) minY.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		minY.setLabel("Min Y");
		minY.setLabelWidth(50);
		minY.setDecimalPlaces(0);
		minY.setMaximum(Double.MAX_VALUE);
		minY.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		minY.addValueListener(new ValueAdapter("x value listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				int minY = (int) e.getDoubleValue();
				try {
					ndRoi.setMinY(minY);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					logger.error("TODO put description of error here", e1);
				}
			}
		});

		sizeX = new ValueBox1<Integer>(stateGroup, SWT.NONE);
		((GridData) sizeX.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		sizeX.setLabel("Size X");
		sizeX.setLabelWidth(50);
		sizeX.setDecimalPlaces(0);
		sizeX.setMaximum(Double.MAX_VALUE);
		sizeX.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sizeX.addValueListener(new ValueAdapter("x value listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				int sizeX = (int) e.getDoubleValue();
				try {
					ndRoi.setSizeX(sizeX);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					logger.error("TODO put description of error here", e1);
				}
			}
		});

		sizeY = new ValueBox1<Integer>(stateGroup, SWT.NONE);
		((GridData) sizeY.getControl().getLayoutData()).horizontalAlignment = SWT.LEFT;
		sizeY.setLabel("Size Y");
		sizeY.setLabelWidth(50);
		sizeY.setDecimalPlaces(0);
		sizeY.setMaximum(Double.MAX_VALUE);
		sizeY.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		sizeY.addValueListener(new ValueAdapter("x value listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				int sizeY = (int) e.getDoubleValue();
				try {
					ndRoi.setSizeY(sizeY);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					logger.error("TODO put description of error here", e1);
				}
			}
		});

		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (enableObservable != null && enableObserver != null) {
					enableObservable.deleteIObserver(enableObserver);
				}
				if (enableXObservable != null && enableXObserver != null) {
					enableXObservable.deleteIObserver(enableXObserver);
				}
				if (enableYObservable != null && enableYObserver != null) {
					enableYObservable.deleteIObserver(enableYObserver);
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

	private void setRoiActive(boolean b) throws Exception {
		roiActive = b;
		monitoringBtn.setText(b ? SWITCH_OFF : SWITCH_ON);
		monitoringLbl.setText(b ? "Active" : "Inactive");
		if(iRegion != null){
			iRegion.setVisible(!roiActive);
			if(roiActive){
				RectangularROI roi = new RectangularROI();
				roi.setPoint(new double[] { ndRoi.getMinX_RBV(), ndRoi.getMinY_RBV() });
				roi.setLengths(new double[] {ndRoi.getSizeX_RBV(), ndRoi.getSizeY_RBV() });
				iRegion.setROI(roi);
			}
		}
	}

	public void setNDRoi(NDROI ndroi, AbstractPlottingSystem abstractPlottingSystem) throws Exception {
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
						RectangularROI roi = (RectangularROI) iRegion.getROI();
						ndRoi.setMinX(roi.getIntPoint()[0]);
						ndRoi.setMinY(roi.getIntPoint()[1]);
						ndRoi.setSizeX(roi.getIntLengths()[0]);
						ndRoi.setSizeY(roi.getIntLengths()[1]);
						ndRoi.enableX();
						ndRoi.enableY();
					}
				} catch (Exception ex) {
					logger.error("Error responding to start_stop button", ex);
				}
			}
		});

		if (mpegROIRegionName == null) {
			mpegROIRegionName = RegionUtils.getUniqueName("ROI Range", abstractPlottingSystem);
		}
		iRegion = abstractPlottingSystem.createRegion(mpegROIRegionName, IRegion.RegionType.BOX);
	}

	public void addMonitoringbtnSelectionListener(SelectionListener listener) {
		monitoringBtn.addSelectionListener(listener);
	}

	public boolean getMonitoring() {
		return roiActive;
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
					observable.deleteIObserver(observer);
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
