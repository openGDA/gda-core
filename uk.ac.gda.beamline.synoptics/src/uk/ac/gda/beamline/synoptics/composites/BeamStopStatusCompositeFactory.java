package uk.ac.gda.beamline.synoptics.composites;

import java.io.IOException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
import gda.rcp.views.CompositeFactory;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

public class BeamStopStatusCompositeFactory implements CompositeFactory {

	static final Logger logger = LoggerFactory.getLogger(BeamStopStatusCompositeFactory.class);

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
		return new BeamStopStatusComposite(parent, style, parent.getDisplay(), label,
				getPvName());
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

}

class BeamStopStatusComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(BeamStopStatusComposite.class);

	private final Color BEAM_STOP_ON_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	private final Color BEAM_STOP_OFF_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	private final String BEAM_STOP_ON_TOOL_TIP = "Beam Stop ON";
	private final String BEAM_STOP_OFF_TOOL_TIP = "Beam Stop OFF";

	private Display display;
	private Color currentColor;
	private Canvas canvas;

	private String pvName;

	public BeamStopStatusComposite(Composite parent, int style, final Display display, String label, String pv) {
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
		this.pvName = pv;

		currentColor = BEAM_STOP_OFF_COLOR;

		int intValue = 0;
		try {
			intValue = LazyPVFactory.newBytePV(pv).get().intValue();
		} catch (IOException e1) {
			logger.error("Failed to get initial state from " + pv, e1);
		}
		if (intValue == 1) {
			currentColor = BEAM_STOP_ON_COLOR;
		} else {
			currentColor = BEAM_STOP_OFF_COLOR;
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
		if (intValue == 1) {
			canvas.setToolTipText(BEAM_STOP_ON_TOOL_TIP);
		} else {
			canvas.setToolTipText(BEAM_STOP_OFF_TOOL_TIP);
		}
		// add monitor
		try {
			LazyPVFactory.newBytePV(pv).addMonitorListener(new PVMonitorListener());
		} catch (IOException e2) {
			logger.error("Failed to add a listener to PV " + pvName, e2);
		}

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

	public class PVMonitorListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent ev) {
			DBR dbr = ev.getDBR();
			if (dbr.isBYTE()) {
				final int value = ((Byte) (((DBR_Byte) dbr).getByteValue()[0])).intValue();
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						if (value == 1) {
							currentColor = BEAM_STOP_ON_COLOR;
							canvas.setToolTipText(BEAM_STOP_ON_TOOL_TIP);
						} else {
							currentColor = BEAM_STOP_OFF_COLOR;
							canvas.setToolTipText(BEAM_STOP_OFF_TOOL_TIP);
						}
					}
				});
			}
			updateBatonCanvas();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}
