/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.device.ui.model;

import java.lang.reflect.Method;
import java.util.Collection;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FieldUtils;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.util.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

class ModelFieldLabelProvider extends EnableIfColumnLabelProvider {
	private Image ticked;
	private Image unticked;

	private final ModelViewer<?> viewer;

	public ModelFieldLabelProvider(ModelViewer<?> viewer) {
		this.viewer = viewer;
	}

	@Override
	public void dispose() {
		if (ticked != null) {
			ticked.dispose();
		}
		if (unticked != null) {
			unticked.dispose();
		}
		super.dispose();
	}

	@Override
	public Color getForeground(Object ofield) {
		final Color ret = super.getForeground(ofield);
		if (ret != null) {
			return ret;
		}
		if (ofield instanceof FieldValue && viewer.isValidationError((FieldValue) ofield)) {
			return Display.getDefault().getSystemColor(SWT.COLOR_RED);
		}
		return null;
	}

	/**
	 * The <code>LabelProvider</code> implementation of this <code>ILabelProvider</code> method returns
	 * <code>null</code>.<br>
	 * Subclasses may override.
	 */
	@Override
	public Image getImage(Object ofield) {
		if (ofield == null) {
			return null;
		}

		final FieldValue field = (FieldValue) ofield;
		final Object element = field.get();
		if (element instanceof Boolean) {
			if (ticked == null) {
				ticked = Activator.getImageDescriptor("icons/ticked.png").createImage();
			}
			if (unticked == null) {
				unticked = Activator.getImageDescriptor("icons/unticked.gif").createImage();
			}
			boolean val = (Boolean) element;
			return val ? ticked : unticked;
		}
		return null;
	}

	/**
	 * The <code>LabelProvider</code> implementation of this <code>ILabelProvider</code> method returns the element's
	 * <code>toString</code> string.<br>
	 * Subclasses may override.
	 * <p>
	 * This renderer is called by the table and some cell editors.<br>
	 * It does not always get asked to render a FieldValue
	 */
	@Override
	public String getText(Object ofield) {
		if (ofield == null) {
			return "";
		}

		final StringBuilder buf = new StringBuilder();
		try {
			if (ofield instanceof FieldValue) {
				appendFieldText(buf, (FieldValue) ofield);
			} else {
				appendCompoundText(buf, null, ofield);
			}
		} catch (Exception ne) {
			// Do not keep logging this exception, it's a table render action and would repeat in the log file for no
			// benefit.
			buf.append(ne.getMessage());
		}
		return buf.toString();
	}

	private void appendFieldText(StringBuilder buf, FieldValue ofield) {
		final Object element = ofield.get();
		if (element == null || element instanceof Boolean) {
			return;
		}

		if (element.getClass() != null && element.getClass().isArray()) {
			buf.append(StringUtils.toString(element));
		} else {
			appendLabel(buf, ofield, element);
		}
	}

	private void appendLabel(StringBuilder buf, FieldValue field, Object element) {
		buf.append(element.toString());
		buf.append(getUnit(field));
	}

	private String getLabel(FieldValue field, Object element) {
		final StringBuilder buf = new StringBuilder();
		appendLabel(buf, field, element);
		return buf.toString();
	}

	private void appendCompoundText(StringBuilder buf, final String compoundLabel, Object element) throws Exception {
		try {
		    final Method ts = element.getClass().getMethod("toString");
		    if (ts.getDeclaringClass()==element.getClass()) {
		        buf.append(ts.invoke(element)); // They made a special impl of toString for us to use
		        return;
		    }
		} catch (Exception ignored) {
		    // We continue to the model's fields.
		}

		final Collection<FieldValue> fields = FieldUtils.getModelFields( element );
		if (compoundLabel != null && compoundLabel.length() > 0) {
			String replace = compoundLabel;
			for (FieldValue fieldValue : fields) {
				final String with = "${" + fieldValue.getName() + "}";
				if (replace.contains(with)) {
					final String value = getLabel(fieldValue, fieldValue.get());
					replace = replace.replace(with, value);
				}
			}
			buf.append(replace);
		} else {
			buf.append("[");
			for (FieldValue fieldValue : fields) {
				buf.append(fieldValue.getDisplayName().trim());
				buf.append("=");
				appendLabel(buf, fieldValue, fieldValue.get());
				buf.append(", ");
			}
			buf.append("]");
		}
	}

	private String getUnit(FieldValue field) {
		final FieldDescriptor anot = field.getAnnotation();
		if (anot != null && anot.unit().length() > 0) {
			return " " + anot.unit();
		}
		return "";
	}
}
