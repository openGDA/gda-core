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

package uk.ac.gda.devices.bssc.ui;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.nebula.visualization.widgets.figures.AbstractLinearMarkedFigure;
import org.eclipse.nebula.visualization.widgets.figures.TankFigure;
import org.eclipse.nebula.visualization.widgets.figures.ThermometerFigure;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BSSCStatus extends ViewPart implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(BSSCStatus.class);

	public static final String ID = "uk.ac.gda.devices.bssc.ui.BSSCStatus"; //$NON-NLS-1$
	public String bsscscannablename = "bsscscannable";
	private ThermometerFigure thermo_seu;
	private ThermometerFigure thermo_storage;
	private TankFigure detergent_tank;
	private TankFigure water_tank;
	private TankFigure waste_tank;
	
	private Color tank;
	private Color thermometer;
	private Color red;
	private Color amber;
	
	private Double seu_temperature, storage_temperature, detergent_level, water_level, waste_level;

	public BSSCStatus() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
		amber = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);
		thermometer = Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
		tank = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
		
		Composite container = new Composite(parent, SWT.NONE);
		
		GridLayout gl_container = new GridLayout(5, true);
		gl_container.marginWidth = 15;
		gl_container.verticalSpacing = 15;
		gl_container.horizontalSpacing = 35;
		container.setLayout(gl_container);
		{
			//use LightweightSystem to create the bridge between SWT and draw2D
			Canvas canvas = new Canvas(container, SWT.None);
			canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			final LightweightSystem lws = new LightweightSystem(canvas);		
			
			thermo_seu = new ThermometerFigure();
			thermo_seu.setRange(-20, 60);
			thermo_seu.setLoLevel(-50);
			thermo_seu.setLoloLevel(-80);
			thermo_seu.setHiLevel(60);
			thermo_seu.setHihiLevel(80);
			thermo_seu.setShowMarkers(false);
			thermo_seu.setMajorTickMarkStepHint(20);
			thermo_seu.setFillColor(thermometer);
			
			lws.setContents(thermo_seu);		}
		{
			//use LightweightSystem to create the bridge between SWT and draw2D
			Canvas canvas = new Canvas(container, SWT.None);
			canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			final LightweightSystem lws = new LightweightSystem(canvas);		
			
			thermo_storage = new ThermometerFigure();
			
			thermo_storage.setRange(-20, 60);
			thermo_storage.setLoLevel(-50);
			thermo_storage.setLoloLevel(-80);
			thermo_storage.setHiLevel(60);
			thermo_storage.setHihiLevel(80);
			thermo_storage.setShowMarkers(false);
			thermo_storage.setMajorTickMarkStepHint(20);
			thermo_storage.setFillColor(thermometer);

			lws.setContents(thermo_storage);
		}
		
		{
			Canvas canvas = new Canvas(container, SWT.None);
			canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			final LightweightSystem lws = new LightweightSystem(canvas);
			
			detergent_tank = new TankFigure();
			
			detergent_tank.setRange(0, 100);
			detergent_tank.setLoLevel(-50);
			detergent_tank.setLoloLevel(-80);
			detergent_tank.setHiLevel(60);
			detergent_tank.setHihiLevel(80);
			detergent_tank.setShowMarkers(false);
			detergent_tank.setMajorTickMarkStepHint(20);
			detergent_tank.setFillColor(tank);
			
			lws.setContents(detergent_tank);
		}
		{
			Canvas canvas = new Canvas(container, SWT.None);
			canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			final LightweightSystem lws = new LightweightSystem(canvas);
			
			water_tank = new TankFigure();
			
			water_tank.setRange(0, 100);
			water_tank.setLoLevel(-50);
			water_tank.setLoloLevel(-80);
			water_tank.setHiLevel(60);
			water_tank.setHihiLevel(80);
			water_tank.setShowMarkers(false);
			water_tank.setMajorTickMarkStepHint(20);
			water_tank.setFillColor(tank);
			
			lws.setContents(water_tank);
		}
		{
			Canvas canvas = new Canvas(container, SWT.None);
			canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			final LightweightSystem lws = new LightweightSystem(canvas);
			
			waste_tank = new TankFigure();
			
			waste_tank.setRange(0, 100);
			waste_tank.setLoLevel(-50);
			waste_tank.setLoloLevel(-80);
			waste_tank.setHiLevel(60);
			waste_tank.setHihiLevel(80);
			waste_tank.setShowMarkers(false);
			waste_tank.setMajorTickMarkStepHint(20);
			waste_tank.setFillColor(tank);
			
			lws.setContents(waste_tank);
		}
		
		Label label_0 = new Label(container, SWT.CENTER);
		label_0.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		label_0.setText("Exposure");
		Label label_1 = new Label(container, SWT.CENTER);
		label_1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		label_1.setText("Storage");
		Label label_2 = new Label(container, SWT.CENTER);
		label_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_2.setText("Detergent");
		Label label_3 = new Label(container, SWT.CENTER);
		label_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_3.setText("Water");
		Label label_4 = new Label(container, SWT.CENTER);
		label_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label_4.setText("Waste");
		
		{
			Button btnScanPark = new Button(container, SWT.NONE);
			btnScanPark.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 2, 1));
			btnScanPark.setText("Clean");
			btnScanPark.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					JythonServerFacade.getInstance().runCommand(bsscscannablename+".clean()");
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		{
			Button btnScanPark = new Button(container, SWT.NONE);
			btnScanPark.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
			btnScanPark.setText("Scan + Park");
			btnScanPark.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					JythonServerFacade.getInstance().runCommand(bsscscannablename+".scanAndPark()");
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		{
			Button btnLoad = new Button(container, SWT.NONE);
			btnLoad.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
			btnLoad.setText("Load");
			btnLoad.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					JythonServerFacade.getInstance().runCommand(bsscscannablename+".load()");
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		createActions();
		initializeToolBar();
		initializeMenu();
		
		setupMonitoring();
	}

	private void setupMonitoring() {
		Scannable findable = (Scannable) Finder.getInstance().find(bsscscannablename);
		findable.addIObserver(this);
		try {
			update(findable, findable.getPosition());
		} catch (DeviceException e) {
			logger.error("initialisation from bssc scannable failed", e);
		}
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	@Override
	public void update(Object source, Object arg) {
		if (source instanceof Scannable) {
			try {
				double[] ds = ScannableUtils.positionToArray(arg, (Scannable) source);
				if (ds.length == 3) {
					seu_temperature = null;
					storage_temperature = null;
					detergent_level = (ds[0]);
					water_level = (ds[1]);
					waste_level = (ds[2]);
				} else if (ds.length == 5){
					seu_temperature = ds[0];
					storage_temperature = ds[1];
					detergent_level = (ds[2]);
					water_level = (ds[3]);
					waste_level = (ds[4]);
				} else {
					return;
				}
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateUI();
					}
				});
				} catch (DeviceException e) {
					logger.error("error processing update from bssc scannable", e);
			}
		}
	}
	
	private void updateUI() { 
		for(Object[] tuple : new Object[][] {{seu_temperature, thermo_seu}, {storage_temperature, thermo_storage},{detergent_level, detergent_tank},{water_level, water_tank},{waste_level, waste_tank}}) {
			AbstractLinearMarkedFigure almf = (AbstractLinearMarkedFigure) tuple[1];
			Double val = (Double) tuple[0];
			if (val == null) {
				almf.setVisible(false);
			} else {
				almf.setVisible(true);
				almf.setValue(val);
				if (almf instanceof ThermometerFigure) {
					((ThermometerFigure) almf).setFillColor(thermometer);
				} else if (almf instanceof TankFigure) {
					((TankFigure) almf).setFillColor(tank);
					if (almf == waste_tank) {
						if (val >= 90)
							((TankFigure) almf).setFillColor(amber);
						if (val >= 95)
							((TankFigure) almf).setFillColor(red);
					} else {
						if (val <= 10)
							((TankFigure) almf).setFillColor(amber);
						if (val <= 5)
							((TankFigure) almf).setFillColor(red);
					}
				}
			}
		}
	}
}