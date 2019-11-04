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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

import uk.ac.gda.ui.tool.ClientMessages;

public class WidgetUtilities {

	private static final Map<Integer, ControlDecoration> decoratorMap;

	static {
		decoratorMap = Collections.synchronizedMap(new HashMap<>());
	}

	private WidgetUtilities() {
		super();

	}

	/**
	 * This Method use to create error message decorator,Its show an error image with message on applied controller field.
	 *
	 * @param control
	 * @param message
	 * @return ControlDecoration
	 */
	public static ControlDecoration addErrorDecorator(final Control control, final String message) {
		if (decoratorMap.containsKey(control.hashCode())) {
			return decoratorMap.get(control.hashCode());
		}
		ControlDecoration txtDecorator = new ControlDecoration(control, SWT.BOTTOM | SWT.TOP);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		txtDecorator.setImage(fieldDecoration.getImage());
		txtDecorator.setDescriptionText(message);
		decoratorMap.put(control.hashCode(), txtDecorator);
		return txtDecorator;
	}
	public static ControlDecoration addErrorDecorator(final Control control, final ClientMessages message) {
		return addErrorDecorator(control, message.name());
	}
	public static void hideDecorator(final Control control) {
		if (control == null || !decoratorMap.containsKey(control.hashCode())) {
			return;
		}
		decoratorMap.remove(control.hashCode()).hide();
		control.pack();
	}
}
