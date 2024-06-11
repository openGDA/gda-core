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

package uk.ac.diamond.daq.mapping.ui.tomography;

import java.text.DecimalFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

/**
 * Constants and functions for use by tomography GUI
 */
public class TomographyUtils {
	public static final String CSV_DELIMITER = ",";
	public static final DecimalFormat DF = new DecimalFormat("#.#####");

	private TomographyUtils() {
		// prevent instantiation
	}

	public static Composite createComposite(Composite parent, int numColumns) {
		final Composite composite = CompositeFactory.newComposite(SWT.NONE).create(parent);
		GridDataFactory.swtDefaults().applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(composite);
		composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		return composite;
	}

	public static Button createDialogButton(Composite parent, String text, String tooltip) {
		final Button button = new Button(parent, SWT.PUSH);
		GridDataFactory.fillDefaults().applyTo(button);
		button.setText(text);
		button.setToolTipText(tooltip);
		return button;
	}

	public static TableViewerColumn createColumn(TableViewer viewer, String title) {
		final TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(80);
		column.getColumn().setText(title);
		return column;
	}

	public static Label createInfoLabel(Composite parent) {
		var gridData = new GridData();
		gridData.widthHint = 200;
		String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
		int fontSize = Display.getCurrent().getSystemFont().getFontData()[0].getHeight();
		var italicFont = new Font(parent.getDisplay(), new FontData(fontName, fontSize, SWT.ITALIC));

		var label = LabelFactory.newLabel(SWT.NONE).create(parent);
		label.setLayoutData(gridData);
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		label.setFont(italicFont);

		return label;
	}
}