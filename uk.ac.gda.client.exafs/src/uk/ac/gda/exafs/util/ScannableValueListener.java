/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.util;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import gda.factory.Finder;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Link;

import uk.ac.gda.richbeans.beans.IFieldWidget;

/**
 * Sets the value of the defined motor name in the IFieldWidget when the mouseDown action is run.
 */
public class ScannableValueListener implements MouseListener, SelectionListener {

	private String scannableName;
	private IFieldWidget ui;

	public static void createLinkedLabel(final Link label, final String scannableName, final IFieldWidget x) {
		label.addSelectionListener(new ScannableValueListener(scannableName, x));
		label.setToolTipText("Connected to '" + scannableName + "'. Click to take current value.");
	}

	private ScannableValueListener(final String scannableName, final IFieldWidget ui) {
		this.scannableName = scannableName;
		this.ui = ui;
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
	}

	@Override
	public void mouseDown(MouseEvent e) {
		setValue();
	}

	@Override
	public void mouseUp(MouseEvent e) {
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		setValue();
	}

	private void setValue() {

		final Scannable scannable = (Scannable) Finder.getInstance().find(scannableName);
		if (scannable == null) {
			ui.setValue("Scannable " + scannableName + " cannot be found");
			return;
		}
		String[] position;
		try {
			position = ScannableUtils.getFormattedCurrentPositionArray(scannable);
		} catch (DeviceException e) {
			ui.setValue("Scannable " + scannableName + " position cannot be resolved.");
			return;
		}
		String strPosition = ArrayUtils.toString(position);
		strPosition = strPosition.substring(1, strPosition.length() - 1);
		ui.setValue(strPosition);
	}

}
