/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.rcp.views;

import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
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

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;

public abstract class AbstractColourStateComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(AbstractColourStateComposite.class);

	private Scannable scannable;
	private Group group;
	private Color currentColour;
	private Canvas canvas;

	protected Map<String, Color> stateMap;

	protected AbstractColourStateComposite(Composite parent, int style, String label, int canvasWidth, int canvasHeight,
			Scannable scannable, Map<String, Color> stateMap) {
		super(parent, style);
		RowDataFactory.swtDefaults().applyTo(this);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(this);

		this.scannable = scannable;
		this.stateMap = stateMap;

		group = new Group(this, style);
		GridDataFactory.fillDefaults().applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(group);
		group.setText(label);
		group.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Object position = null;
		try {
			position = this.scannable.getPosition();
		} catch (DeviceException e) {
			logger.warn("Error while getting the position of the scannable.", e);
			return;
		}

		currentColour = getMapValue(position);
		canvas = new Canvas(group, SWT.NONE);
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL);
		gridData.widthHint = canvasWidth;
		gridData.heightHint = canvasHeight;
		canvas.setLayoutData(gridData);
		canvas.setToolTipText(getToolTip(position));
		canvas.addPaintListener(this::paintControl);

		this.scannable.addIObserver(this::updateScannable);
	}

	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		gc.setAntialias(SWT.ON);
		gc.setBackground(currentColour);
		gc.setLineWidth(1);
		Rectangle clientArea = canvas.getClientArea();
		final int margin = 4;
		final Point topLeft = new Point(margin, margin);
		final Point size = new Point(clientArea.width - margin * 2, clientArea.height - margin * 2);
		gc.fillOval(topLeft.x, topLeft.y, size.x, size.y);
		gc.drawOval(topLeft.x, topLeft.y, size.x, size.y);
	}

	public void updateScannable(final Object theObserved, final Object arg) {
		Display.getDefault().asyncExec(() -> {
			if (theObserved instanceof Scannable) {
				Object changeCode;
				if (arg.getClass().isArray()) {
					changeCode = ((Object[]) arg)[0];
				} else {
					changeCode = arg;
				}

				if (changeCode instanceof ScannablePositionChangeEvent) {
					Object newPosition = ((ScannablePositionChangeEvent) changeCode).newPosition;
					currentColour = getMapValue(newPosition);
					canvas.setToolTipText(getToolTip(newPosition));
				} else if (changeCode instanceof String
						|| changeCode instanceof Integer
						|| changeCode instanceof Double
						|| changeCode instanceof Boolean
						|| changeCode instanceof Byte
						|| changeCode instanceof Character
						|| changeCode instanceof Short
						|| changeCode instanceof Long
						|| changeCode instanceof Float) {
					currentColour = getMapValue(changeCode);
					canvas.setToolTipText(getToolTip(changeCode));
				}
			}
			canvas.redraw();
			canvas.update();
		});
	}

	protected abstract String getToolTip(Object position);

	protected abstract String getMapKey(Object position);

	protected abstract Color getMapValue(Object position);
}
