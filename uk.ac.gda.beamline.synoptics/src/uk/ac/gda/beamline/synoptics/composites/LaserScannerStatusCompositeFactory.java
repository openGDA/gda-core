package uk.ac.gda.beamline.synoptics.composites;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;

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

import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.rcp.views.CompositeFactory;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.event.MonitorListener;
import uk.ac.diamond.daq.concurrent.Async;

public class LaserScannerStatusCompositeFactory implements CompositeFactory {

	private String label;
	private String pvName;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new LaserScannerStatusComposite(parent, style, parent.getDisplay(), label, getPvName());
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

}

class LaserScannerStatusComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(LaserScannerStatusComposite.class);

	private static final Color LASER_SCANNER_CLEAR_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	private static final Color LASER_SCANNER_TRIGGERED_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
	private static final String LASER_SCANNER_CLEAR_TOOL_TIP = "Laser Scanner Clear";
	private static final String LASER_SCANNER_TRIGGERED_TOOL_TIP = "Laser Scanner Triggered";

	private Display mdisplay;
	private Color currentColor;
	private Canvas canvas;

	private ScheduledFuture<?> colorChange;

	private PV<Integer> newIntegerPV;

	private MonitorListener listener;

	public LaserScannerStatusComposite(Composite parent, int style, final Display display, String label, String pv) {
		super(parent, style);

		GridDataFactory.fillDefaults().applyTo(this);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this);

		Group grp = new Group(this, style);
		GridDataFactory.fillDefaults().applyTo(grp);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(grp);
		grp.setText(label);
		grp.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		this.mdisplay = display;
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this);
		GridDataFactory.fillDefaults().applyTo(this);

		currentColor = LASER_SCANNER_CLEAR_COLOR;

		int intValue = initialisation(pv);

		canvas = new Canvas(grp, SWT.NONE);
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL);
		gridData.widthHint = 40;
		gridData.heightHint = 40;
		canvas.setLayoutData(gridData);
		canvas.addPaintListener(e -> {
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
		});

		if (intValue == 0) {
			canvas.setToolTipText(LASER_SCANNER_CLEAR_TOOL_TIP);
		}
		if (intValue != 0) {
			canvas.setToolTipText(LASER_SCANNER_TRIGGERED_TOOL_TIP);
		}
	}

	private int initialisation(String pv) {
		int intValue = 0;
		if (LocalProperties.isDummyModeEnabled()) {
			logger.debug("Dummy mode uses ");
			colorChange = Async.scheduleAtFixedRate(() -> {
				mdisplay.asyncExec(() -> {
					currentColor = currentColor == LASER_SCANNER_CLEAR_COLOR ? LASER_SCANNER_TRIGGERED_COLOR : LASER_SCANNER_CLEAR_COLOR;
					canvas.redraw();
					canvas.update();
				});
			}, 5, 5, TimeUnit.SECONDS);
		} else {
			newIntegerPV = LazyPVFactory.newIntegerPV(pv);
			try {
				intValue = newIntegerPV.get().intValue();
			} catch (IOException e1) {
				logger.error("Failed to get initial state from " + pv, e1);
			}

			if (intValue != 0) {
				currentColor = LASER_SCANNER_TRIGGERED_COLOR;
			}
			// add monitor
			try {
				listener = ev -> {
					DBR dbr = ev.getDBR();
					if (dbr.isBYTE()) {
						final int value = ((Byte) (((DBR_Byte) dbr).getByteValue()[0])).intValue();
						mdisplay.asyncExec(() -> {
							if (value == 0) {
								currentColor = LASER_SCANNER_CLEAR_COLOR;
								canvas.setToolTipText(LASER_SCANNER_CLEAR_TOOL_TIP);
							}
							if (value != 0) {
								currentColor = LASER_SCANNER_TRIGGERED_COLOR;
								canvas.setToolTipText(LASER_SCANNER_TRIGGERED_TOOL_TIP);
							}
						});
					}
					mdisplay.asyncExec(() -> {
						canvas.redraw();
						canvas.update();
					});
				};
				newIntegerPV.addMonitorListener(listener);
			} catch (IOException e2) {
				logger.error("Failed to add a listener to PV {}", pv, e2);
			}
		}
		return intValue;
	}

	@Override
	public void dispose() {
		if (colorChange != null && !colorChange.isCancelled()) {
			colorChange.cancel(true);
		}
		if (newIntegerPV != null && listener != null) {
			newIntegerPV.removeMonitorListener(listener);
		}
		super.dispose();
	}
}
