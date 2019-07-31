package uk.ac.gda.beamline.synoptics.composites;

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

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.observable.IObserver;
import gda.rcp.views.CompositeFactory;

public class BeamlineMbbinaryShutterCompositeFactory implements CompositeFactory {

	private String label;
	private EnumPositioner shutter;
	private boolean controlPermitted = false;
	private String openString="OPEN";
	private String closeString="CLOSE";

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new BeamlineMbbinaryShutterComposite(parent, style, parent.getDisplay(), label,
				shutter, controlPermitted, openString, closeString);
	}

	public EnumPositioner getShutter() {
		return shutter;
	}

	public void setShutter(EnumPositioner shutter) {
		this.shutter = shutter;
	}

	public boolean isControlPermitted() {
		return controlPermitted;
	}

	public void setControlPermitted(boolean controlPermitted) {
		this.controlPermitted = controlPermitted;
	}

	public String getOpenString() {
		return openString;
	}

	public void setOpenString(String openString) {
		this.openString = openString;
	}

	public String getCloseString() {
		return closeString;
	}

	public void setCloseString(String closeString) {
		this.closeString = closeString;
	}

}

class BeamlineMbbinaryShutterComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(BeamlineMbbinaryShutterComposite.class);

	private final Color OPEN_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	private final Color CLOSE_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	private final String OPEN_TOOL_TIP = "Shutter Open!\nRight click - shutter control";
	private final String CLOSE_TOOL_TIP = "Shutter Close!\nRight click - shutter control";

	private final String OPEN_TOOL_TIP_NO_CONTROL = "Shutter Open!";
	private final String CLOSE_TOOL_TIP_NO_CONTROL = "Shutter Close!";

	private Display display;
	private Color currentColor;
	private Canvas canvas;

	private MenuItem openShutter;
	private MenuItem closeShutter;

	private EnumPositioner shutter;
	private boolean controlPermitted = false;
	private String openString;
	private String closeString;

	public BeamlineMbbinaryShutterComposite(Composite parent, int style, final Display display, String label,
			final EnumPositioner shutter, boolean controlPermitted, String open, String close) {
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

		this.shutter = shutter;
		this.setControlPermitted(controlPermitted);
		this.openString=open;
		this.closeString=close;

		currentColor = CLOSE_COLOR;
		String currentPos = "";
		try {
			currentPos = shutter.getPosition().toString().trim();
		} catch (DeviceException e1) {
			logger.error("failed to get current shutter position from " + shutter.getName(), e1);
		}
		if (currentPos.equalsIgnoreCase(openString)) {
			currentColor = OPEN_COLOR;
		} else if (currentPos.equalsIgnoreCase(closeString)) {
			currentColor = CLOSE_COLOR;
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
		if (isControlPermitted()) {
			canvas.setMenu(createPopup(this));
			// initialize tooltip
			if (currentPos.equalsIgnoreCase(openString)) {
				canvas.setToolTipText(OPEN_TOOL_TIP);
				openShutter.setSelection(true);
			} else if (currentPos.equalsIgnoreCase(closeString)) {
				canvas.setToolTipText(CLOSE_TOOL_TIP);
				closeShutter.setSelection(true);
			}
		} else {
			if (currentPos.equalsIgnoreCase(openString)) {
				canvas.setToolTipText(OPEN_TOOL_TIP_NO_CONTROL);
			} else if (currentPos.equalsIgnoreCase(closeString)) {
				canvas.setToolTipText(CLOSE_TOOL_TIP_NO_CONTROL);
			}
		}
		final IObserver shutterObserver = new IObserver() {
			@Override
			public void update(final Object theObserved, final Object changeCode) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (theObserved instanceof EnumPositioner) {
							if (changeCode instanceof ScannablePositionChangeEvent) {
								final String value =((ScannablePositionChangeEvent) changeCode).newPosition.toString().toLowerCase();
								logger.info("Shutter {} state changed to {}", shutter.getName(), value);
								if (value.equalsIgnoreCase(openString)) {
									currentColor = OPEN_COLOR;
									if (isControlPermitted()) {
										canvas.setToolTipText(OPEN_TOOL_TIP);
										openShutter.setSelection(true);
										closeShutter.setSelection(false);
									} else {
										canvas.setToolTipText(OPEN_TOOL_TIP_NO_CONTROL);
									}
								} else if (value.equalsIgnoreCase(closeString)) {
									currentColor = CLOSE_COLOR;
									if (isControlPermitted()) {
										canvas.setToolTipText(CLOSE_TOOL_TIP);
										openShutter.setSelection(false);
										closeShutter.setSelection(true);
									} else {
										canvas.setToolTipText(CLOSE_TOOL_TIP_NO_CONTROL);
									}
								}
							}
						}
						updateBatonCanvas();
					}
				});
			}
		};
		shutter.addIObserver(shutterObserver);
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

		openShutter = new MenuItem(menu, SWT.RADIO);
		openShutter.setText(openString);
		openShutter.addSelectionListener(popupSelectionListener);
		openShutter.setSelection(false);
		closeShutter = new MenuItem(menu, SWT.RADIO);
		closeShutter.setText(closeString);
		closeShutter.setSelection(false);
		closeShutter.addSelectionListener(popupSelectionListener);
		return menu;
	}

	private SelectionListener popupSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			MenuItem selected = null;

			if (event.widget instanceof MenuItem) {
				selected = (MenuItem) event.widget;
			} else
				return;

			try {
				if (selected.equals(openShutter)) {
					shutter.moveTo(openString);
					logger.info("Open shutter.");
				} else if (selected.equals(closeShutter)) {
					shutter.moveTo(closeString);
					logger.info("Close shutter.");
				}
			} catch (DeviceException e) {
				logger.error("Failed to control shutter " + shutter.getName(), e);
			}
		}
	};

	@Override
	public void dispose() {
		super.dispose();
	}

	public boolean isControlPermitted() {
		return controlPermitted;
	}

	public void setControlPermitted(boolean controlPermitted) {
		this.controlPermitted = controlPermitted;
	}
}
