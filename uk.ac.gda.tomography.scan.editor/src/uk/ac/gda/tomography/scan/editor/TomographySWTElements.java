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

package uk.ac.gda.tomography.scan.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.tomography.scan.editor.view.TomographyMessages;
import uk.ac.gda.tomography.scan.editor.view.TomographyMessagesUtility;

/**
 * Utility class for create SWT Tomography standard SWT elements. GridLayout is
 * the default layout for all the elements
 *
 * @author Maurizio Nagni
 */
public final class TomographySWTElements {

	private static final Point DEFAULT_SIZE = new Point(100, 50);
	private static final Point DEFAULT_SPAN = new Point(1, 1);

	/**
	 *
	 */
	private TomographySWTElements() {
		super();
	}

	public static final Point defaultCompositeMargin() {
		return new Point(10, 10);
	}

	public static final Composite createComposite(final Composite parent, int style, int columns) {
		Composite composite = new Composite(parent, style);
		GridLayoutFactory.swtDefaults().numColumns(columns).applyTo(composite);
		return composite;
	}

	public static final Group createGroup(final Composite parent, int numColumns, TomographyMessages message) {
		Group group = new Group(parent, SWT.NONE);
		if (message != null) {
			group.setText(TomographyMessagesUtility.getMessage(message));
		}
		group.setFont(TomographyResourceManager.getInstance().getGroupDefaultFont());
		// group.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(group);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(group);
		return group;
	}

	public static final Label createLabel(final Composite parent, int labelStyle, final TomographyMessages message,
			Point span) {
		return createLabel(parent, labelStyle, TomographyMessagesUtility.getMessage(message), span);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, final TomographyMessages message) {
		return createLabel(parent, labelStyle, TomographyMessagesUtility.getMessage(message), DEFAULT_SPAN);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, String message) {
		return createLabel(parent, labelStyle, message, DEFAULT_SPAN);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, String message, Point span) {
		Label label = new Label(parent, labelStyle);
		label.setText(message);
		label.setFont(TomographyResourceManager.getInstance().getLabelDefaultFont());
		// label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.LEFT, SWT.BOTTOM).span(span).applyTo(label);
		return label;
	}

	public static final Label createEmptyCell(final Composite parent, int labelStyle) {
		return createLabel(parent, labelStyle, "", DEFAULT_SPAN);
	}

	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener) {
		return createText(parent, textStyle, listener, DEFAULT_SIZE, DEFAULT_SPAN);
	}

	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener,
			Point span) {
		return createText(parent, textStyle, listener, DEFAULT_SIZE, span);
	}

	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener,
			Point minSize, Point span) {
		Text text = new Text(parent, textStyle);
		text.setFont(TomographyResourceManager.getInstance().getTextDefaultFont());
		// text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridDataFactory gdf = GridDataFactory.fillDefaults().grab(true, false).align(SWT.LEFT, SWT.TOP);
		if (span != null) {
			gdf.span(span);
		}
		if (minSize != null) {
			gdf.minSize(minSize);
		}
		gdf.applyTo(text);
		if (listener != null) {
			text.addVerifyListener(listener);
		}
		return text;
	}

	public static final Button createButton(final Composite parent, final TomographyMessages message, int style,
			Point span) {
		return createButton(parent, TomographyMessagesUtility.getMessage(message), style, span);
	}

	public static final Button createButton(final Composite parent, final TomographyMessages message, int style) {
		return createButton(parent, TomographyMessagesUtility.getMessage(message), style, DEFAULT_SPAN);
	}

	public static final Button createButton(final Composite parent, String message, int style) {
		return createButton(parent, message, style, DEFAULT_SPAN);
	}

	public static final Button createButton(final Composite parent, String message, int style, Point span) {
		Button button = new Button(parent, style);
		button.setFont(TomographyResourceManager.getInstance().getLabelDefaultFont());
		button.setText(message);
		button.setSize(DEFAULT_SIZE);
		GridDataFactory gf = GridDataFactory.swtDefaults().grab(true, false);
		if (span != null) {
			gf.span(span);
		}
		gf.applyTo(button);
		return button;
	}
}
