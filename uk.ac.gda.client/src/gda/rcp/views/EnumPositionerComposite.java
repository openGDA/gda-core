/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;

/**
 * Positioner composite for {@link EnumPositioner}s. Shows positions in a drop-down box.
 */
public class EnumPositionerComposite extends AbstractPositionerComposite {

	private static final Logger logger = LoggerFactory.getLogger(EnumPositionerComposite.class);
	private Combo positionsCombo;
	private String[] allPositions;
	private SelectionListener selectionListener;

	public EnumPositionerComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected void createPositionerControl() {
		positionsCombo = new Combo(this, SWT.READ_ONLY);
		selectionListener = widgetSelectedAdapter(this::selectionChanged);
		positionsCombo.addSelectionListener(selectionListener);
	}

	@Override
	protected void updatePositionerControl(Object newPosition, boolean moving) {
		final boolean atFinalPosition = !moving;
		Display.getDefault().asyncExec(() -> {
			if (atFinalPosition) {
				positionsCombo.select(Arrays.asList(allPositions).indexOf(newPosition));
			}
			positionsCombo.setEnabled(atFinalPosition);
		});
	}

	@Override
	public void setScannable(Scannable scannable) {
		if (scannable instanceof EnumPositioner) {
			try {
				EnumPositioner positioner = (EnumPositioner) scannable;
				allPositions = positioner.getPositions();
				positionsCombo.setItems(allPositions);
				positionsCombo.select(Arrays.asList(allPositions).indexOf(positioner.getPosition()));
			} catch (DeviceException e) {
				logger.error("Error determining the current position of {}", scannable.getName(), e);
			}
			super.setScannable(scannable);
		} else {
			throw new IllegalStateException("Scannable '" + scannable.getName() + "' must be of type " + EnumPositioner.class.getSimpleName());
		}
	}

	@Override
	public void dispose() {
		positionsCombo.removeSelectionListener(selectionListener);
		super.dispose();
	}

	private void selectionChanged(SelectionEvent event) {
		final String newPosition = ((Combo)event.widget).getText();
		move(newPosition);
	}

}
