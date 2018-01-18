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

import gda.device.IBeamMonitor;
import gda.observable.IObserver;
import gda.rcp.views.CompositeFactory;

public class BeamStatusCompositeFactory implements CompositeFactory {
	
	static final Logger logger = LoggerFactory.getLogger(BeamStatusCompositeFactory.class);
	
	private String label;
	private IBeamMonitor beamMonitor;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new BeamStatusComposite(parent, style, parent.getDisplay(), label, beamMonitor);
	}

	public IBeamMonitor getBeamMonitor() {
		return beamMonitor;
	}

	public void setBeamMonitor(IBeamMonitor beamMonitor) {
		this.beamMonitor = beamMonitor;
	}
}

class BeamStatusComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(BeamStatusComposite.class);
	
	private final Color BEAM_ON_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	private final Color BEAM_OFF_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	private final String BEAM_ON_TOOL_TIP="X-ray ON!\nRight click - monitor control";
	private final String BEAM_OFF_TOOL_TIP="X-ray OFF!\nRight click - monitor control";
	
	private Display display;
	private Color currentColor;
	private Canvas beamCanvas;
	
	private MenuItem switchOnMonitor;
	private MenuItem switchOffMonitor;
	private IBeamMonitor bm;


	public BeamStatusComposite(Composite parent, int style, final Display display, String label, IBeamMonitor bm) {
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
		
		this.bm=bm;
		
		currentColor = BEAM_OFF_COLOR;
		
		if (bm.isBeamOn()) {
			currentColor=BEAM_ON_COLOR;
		} else {
			currentColor=BEAM_OFF_COLOR;
		}

		beamCanvas = new Canvas(grp, SWT.NONE);
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL);
		gridData.widthHint = 40;
		gridData.heightHint = 40;
		beamCanvas.setLayoutData(gridData);
		beamCanvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setAntialias(SWT.ON);
				gc.setBackground(currentColor);
				gc.setLineWidth(1);
				Rectangle clientArea = beamCanvas.getClientArea();
				final int margin = 4;
				final Point topLeft = new Point(margin, margin);
				final Point size = new Point(clientArea.width - margin * 2, clientArea.height - margin * 2);
				gc.fillOval(topLeft.x, topLeft.y, size.x, size.y);
				gc.drawOval(topLeft.x, topLeft.y, size.x, size.y);
			}
		});
		beamCanvas.setMenu(createPopup(this));
		// initialize tooltip
		if (bm.isBeamOn()) {
			beamCanvas.setToolTipText(BEAM_ON_TOOL_TIP);
		} else {
			beamCanvas.setToolTipText(BEAM_OFF_TOOL_TIP);
		}
						
		final IObserver beamObserver = new IObserver() {
			@Override
			public void update(final Object theObserved, final Object changeCode) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						boolean value = false;
						if (theObserved instanceof IBeamMonitor) {
							if (changeCode instanceof Boolean) {
								value = ((Boolean) changeCode).booleanValue();
								if (!value) {
									currentColor=BEAM_OFF_COLOR;
									beamCanvas.setToolTipText(BEAM_OFF_TOOL_TIP);
								} else {
									currentColor=BEAM_ON_COLOR;
									beamCanvas.setToolTipText(BEAM_ON_TOOL_TIP);
								}
							}
						}
						updateBatonCanvas();
					}
				});
			}
		};
		bm.addIObserver(beamObserver);
	}

	private void updateBatonCanvas() {
		display.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				beamCanvas.redraw();
				beamCanvas.update();
			}
		});
	}
	
	private Menu createPopup(Composite parent) {
		Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
		
		switchOnMonitor = new MenuItem(menu, SWT.RADIO);
		switchOnMonitor.setText("Monitor ON");
		switchOnMonitor.addSelectionListener(popupSelectionListener);
		switchOffMonitor = new MenuItem(menu, SWT.RADIO);
		switchOffMonitor.setText("Monitor OFF");
		switchOffMonitor.addSelectionListener(popupSelectionListener);
		if (bm.isMonitorOn()) {
			switchOnMonitor.setSelection(true);
		} else {
			switchOffMonitor.setSelection(true);
		}
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
			
			if (selected.equals(switchOnMonitor)) {
				bm.on();
				logger.info("Switch ON beam monitor.");
			} else if (selected.equals(switchOffMonitor)) {
				bm.off();
				logger.info("Switch OFF beam monitor.");
			}
		}
	};

	@Override
	public void dispose() {
		super.dispose();
	}
}
