package uk.ac.gda.beamline.synoptics.composites;
/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.ISpin;
import gda.observable.IObserver;
import gda.rcp.views.CompositeFactory;

public class SpinStatusCompositeFactory implements CompositeFactory {
	
	static final Logger logger = LoggerFactory.getLogger(SpinStatusCompositeFactory.class);
	
	private String label;
	private ISpin spin;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new SpinStatusComposite(parent, style, parent.getDisplay(), label, spin);
	}

	public ISpin getSpin() {
		return spin;
	}

	public void setSpin(ISpin spin) {
		this.spin = spin;
	}

}

class SpinStatusComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(SpinStatusComposite.class);
	
	private final Color SPIN_ON_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	private final Color SPIN_OFF_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	private final String SPIN_ON_TOOL_TIP="Spin ON!\nRight click - spin control";
	private final String SPIN_OFF_TOOL_TIP="Spin OFF!\nRight click - spin control";
	
	private Display display;
	private Color currentColor;
	private Canvas canvas;
	
	private MenuItem spinOn;
	private MenuItem spinOff;
	private ISpin spin;


	public SpinStatusComposite(Composite parent, int style, final Display display, String label, ISpin spin) {
		super(parent, style);

		GridDataFactory.fillDefaults().applyTo(this);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this);
		
		Group grp = new Group(this, style);
		GridDataFactory.fillDefaults().applyTo(grp);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(grp);
		grp.setText(label);
		grp.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		this.display = display;
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this);
		GridDataFactory.fillDefaults().applyTo(this);
		
		this.spin=spin;
		
		currentColor = SPIN_OFF_COLOR;
		
		try {
			if (spin.getState().equalsIgnoreCase("enabled")) {
				currentColor=SPIN_ON_COLOR;
			} else {
				currentColor=SPIN_OFF_COLOR;
			}
		} catch (DeviceException e1) {
			logger.error("Failed to get spin status", e1);
		}

		canvas = new Canvas(grp, SWT.NONE);
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL);
		gridData.widthHint = 40;
		gridData.heightHint = 40;
		canvas.setLayoutData(gridData);
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setAntialias(SWT.ON);
				gc.setBackground(currentColor);
				gc.setLineWidth(1);
				Rectangle clientArea = canvas.getClientArea();
				final int margin = 4;
				final Point topLeft = new Point(margin, margin);
				final Point size = new Point(clientArea.width - margin * 2, clientArea.height - margin * 2);
				gc.fillOval(topLeft.x, topLeft.y, size.x, size.y);
				gc.drawOval(topLeft.x, topLeft.y, size.x, size.y);
			}
		});
		canvas.setMenu(createPopup(this));
		// initialize tooltip
		try {
			if (spin.getState().equalsIgnoreCase("enabled")) {
				canvas.setToolTipText(SPIN_ON_TOOL_TIP);
				spinOn.setSelection(true);
			} else {
				canvas.setToolTipText(SPIN_OFF_TOOL_TIP);
				spinOff.setSelection(true);
			}
		} catch (DeviceException e1) {
			logger.error("Failed to get spin status", e1);
		}
						
		final IObserver spinObserver = new IObserver() {
			@Override
			public void update(final Object theObserved, final Object changeCode) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						String value = "";
						if (theObserved instanceof ISpin) {
							if (changeCode instanceof String) {
								value = ((String) changeCode);
								if (value.equalsIgnoreCase("Enabled")) {
									currentColor=SPIN_ON_COLOR;
									canvas.setToolTipText(SPIN_ON_TOOL_TIP);
								} else {
									currentColor=SPIN_OFF_COLOR;
									canvas.setToolTipText(SPIN_OFF_TOOL_TIP);
								}
							}
						}
						updateBatonCanvas();
					}
				});
			}
		};
		spin.addIObserver(spinObserver);
	}

	private void updateBatonCanvas() {
		display.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				canvas.redraw();
				canvas.update();
			}
		});
	}
	
	private Menu createPopup(Composite parent) {
		Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
		
		spinOn = new MenuItem(menu, SWT.RADIO);
		spinOn.setText("Spin ON");
		spinOn.addSelectionListener(popupSelectionListener);
		spinOff = new MenuItem(menu, SWT.RADIO);
		spinOff.setText("Spin OFF");
		spinOff.addSelectionListener(popupSelectionListener);
		return menu;
	}
	
	private SelectionListener popupSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			MenuItem selected = null;
			
			if (event.widget instanceof MenuItem) {
				selected = (MenuItem) event.widget;
			}
			else 
				return;
			
			try {
				if (selected.equals(spinOn)) {
					spin.on();
					logger.info("Switch ON beam monitor.");
				} else if (selected.equals(spinOff)) {
					spin.off();
					logger.info("Switch OFF beam monitor.");
				}
			} catch (DeviceException e) {
				logger.error("Failed to control spin", e);
			}
		}
	};

	@Override
	public void dispose() {
		super.dispose();
	}
}
