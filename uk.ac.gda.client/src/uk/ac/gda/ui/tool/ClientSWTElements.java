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

package uk.ac.gda.ui.tool;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Utility class for create SWT Client standard SWT elements.
 * GridLayout is the default layout for all the elements
 *
 * @author Maurizio Nagni
 */
public final class ClientSWTElements {

	public static final Point DEFAULT_COMPOSITE_SIZE = new Point(500, SWT.DEFAULT);
	public static final Point DEFAULT_BUTTON_SIZE = new Point(100, 50);
	public static final Point SMALL_BUTTON_SIZE = new Point(60, 30);
	public static final Point DEFAULT_SPAN = new Point(1, 1);

	private ClientSWTElements() {
	}

	public static final Point defaultCompositeMargin() {
		return new Point(10, 10);
	}

	public static final Composite createComposite(final Composite parent, int style, int columns) {
		return createComposite(parent, style, columns, SWT.LEFT, SWT.TOP);
	}

	public static final Composite createComposite(final Composite parent, int style, int columns, int hAlign, int vAlign) {
		Composite composite = new Composite(parent, style);
		GridLayoutFactory glf = GridLayoutFactory.swtDefaults();
		if (columns > 0) {
			glf.numColumns(columns);
		}
		glf.applyTo(composite);
		GridDataFactory.swtDefaults().grab(true, true).align(hAlign, vAlign).applyTo(composite);
		//composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		return composite;
	}

	public static final Composite createComposite(final Composite parent, int style) {
		return createComposite(parent, style, 0);
	}

	public static final Group createGroup(final Composite parent, int numColumns, final ClientMessages message) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(ClientResourceManager.getInstance().getGroupDefaultFont());
		//group.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(group);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.TOP).applyTo(group);
		if (message != null) {
			group.setText(ClientMessagesUtility.getMessage(message));
		}
		return group;
	}

	public static final Label createLabel(final Composite parent, int labelStyle) {
		return createLabel(parent, labelStyle,  ClientMessages.EMPTY_MESSAGE, null);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, final ClientMessages message, final Point span) {
		return createLabel(parent, labelStyle, message, span, null);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, final ClientMessages message) {
		return createLabel(parent, labelStyle, ClientMessagesUtility.getMessage(message), DEFAULT_SPAN, null);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, String message) {
		return createLabel(parent, labelStyle, message, DEFAULT_SPAN, null);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, String message, final FontDescriptor fontDescriptor) {
		return createLabel(parent, labelStyle, message, DEFAULT_SPAN, fontDescriptor);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, final ClientMessages message, final Point span, final FontDescriptor fontDescriptor) {
		return createLabel(parent, labelStyle, ClientMessagesUtility.getMessage(message), span, fontDescriptor);
	}

	public static final Label createLabel(final Composite parent, int labelStyle, String message, final Point span, final FontDescriptor fontDescriptor) {
		Label label = new Label(parent, labelStyle);
		label.setText(message);
		if (Objects.isNull(fontDescriptor)) {
			label.setFont(ClientResourceManager.getInstance().getLabelDefaultFont());
		} else {
			label.setFont(ClientResourceManager.getInstance().getFont(fontDescriptor));
		}

		//label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		GridDataFactory gdf = GridDataFactory.swtDefaults().grab(true, false);
		gdf.align(SWT.LEFT, SWT.BOTTOM);
		if (Objects.nonNull(span)) {
			gdf.span(span);
		}
		gdf.applyTo(label);
		return label;
	}

	public static final Label createEmptyCell(final Composite parent, int labelStyle) {
		return createLabel(parent, labelStyle, "", DEFAULT_SPAN, null);
	}

	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener) {
		return createText(parent, textStyle, listener, null);
	}

	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener, final Point span) {
		return createText(parent, textStyle, listener, span, null);
	}

	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener, final Point span,
			final ClientMessages tooltip) {
		return createText(parent, textStyle, listener, span, tooltip, null);
	}

	public static final Text createText(final Composite parent, int textStyle, final VerifyListener listener, final Point span,
			final ClientMessages tooltip, Point minSize) {
		return createText(parent, textStyle, listener, span, ClientMessagesUtility.getMessage(tooltip), minSize);
	}

	private static final Text createText(final Composite parent, int textStyle, final VerifyListener listener, final Point span, final String tooltip,
			final Point minSize) {
		Text text = new Text(parent, textStyle);
		text.setFont(ClientResourceManager.getInstance().getTextDefaultFont());
		text.setToolTipText(tooltip);
		// text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		GridDataFactory gdf = applySpan(text, span, minSize);
		gdf.align(SWT.LEFT, SWT.TOP);
		gdf.applyTo(text);
		if (listener != null) {
			text.addVerifyListener(listener);
		}
		return text;
	}

	public static final Button createButton(final Composite parent, int style, final ClientMessages message, final ClientMessages tooltip) {
		return createButton(parent, style, message, tooltip, null);
	}

	public static final Button createButton(final Composite parent, int style, final ClientMessages message, final ClientMessages tooltip,
			final Point span) {
		return createButton(parent, style, ClientMessagesUtility.getMessage(message), ClientMessagesUtility.getMessage(tooltip), span);
	}

	private static final Button createButton(final Composite parent, int style, String message, String tooltip, final Point span) {
		Button button = new Button(parent, style);
		button.setFont(ClientResourceManager.getInstance().getLabelDefaultFont());
		button.setText(message);
		button.setToolTipText(tooltip);
		button.setSize(DEFAULT_BUTTON_SIZE);
		applySpan(button, span, null);
		return button;
	}

	public static final Combo createCombo(final Composite parent, int style, final String[] items, final ClientMessages tooltip) {
		Combo combo = new Combo(parent, style);
		combo.setToolTipText(ClientMessagesUtility.getMessage(tooltip));
		combo.setItems(items);
		return combo;
	}

	public static final List createList(final Composite parent, int style, final String[] items, final ClientMessages tooltip, final Point span, final Point minSize) {
		List list = new List (parent, style | SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		list.setToolTipText(ClientMessagesUtility.getMessage(tooltip));
		list.setItems(items);
		applySpan(list, span, minSize);
		return list;
	}

	/**
	 * Retrieves and {@link Image} using the specified path
	 *
	 * @param path	The path to the image file
	 * @return		The retrieved {@link Image}
	 */
	public static Image getImage(String pluginId, String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, path).createImage();
	}

	public static void changeVAlignement(Control control, int vAlignement) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).verticalAlignment = vAlignement;
		}
	}

	public static void changeHAlignment(Control control, int hAlignement) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).horizontalAlignment = hAlignement;
		}
	}

	public static void changeHIndent(Control control, int hIndent) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).horizontalIndent = hIndent;
		}
	}

	public static void changeVerticalAlign(Control control, int vAlignement) {
		if (GridData.class.isInstance(control.getLayoutData())) {
			GridData.class.cast(control.getLayoutData()).verticalAlignment = vAlignement;
		}
	}

	public static void setTooltip(Control control,  final ClientMessages tooltip) {
		control.setToolTipText(ClientMessagesUtility.getMessage(tooltip));
	}

	private static GridDataFactory applySpan(final Control control, final Point span, final Point minSize) {
		GridDataFactory gdf = GridDataFactory.swtDefaults().grab(true, false);
		if (Objects.nonNull(span)) {
			gdf.span(span);
		} else {
			gdf.span(DEFAULT_SPAN);
		}
		if (minSize != null) {
			gdf.minSize(minSize);
		} else {
			gdf.minSize(DEFAULT_BUTTON_SIZE);
		}
		gdf.applyTo(control);
		return gdf;
	}
}
