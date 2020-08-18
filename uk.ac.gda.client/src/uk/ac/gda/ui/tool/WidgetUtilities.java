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
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

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

	/**
	 * Adds a {@link Listener} to a {@link Widget} and removes it before the {@code Widget} is disposed.
	 * @param widget  the element to which the listener is add
	 * @param eventType the type of event to listen for
	 * @param listener the type of event to listen for
	 */
	public static final void addWidgetDisposableListener(Widget widget, int eventType, Listener listener) {
		widget.addListener(eventType, listener);
		widget.addDisposeListener(ev -> widget.removeListener(eventType, listener));
	}

	/**
	 * Adds a {@link SelectionListener} to a {@link Button} and removes it before the {@code Button} is disposed.
	 * @param button  the control to which the listener is add
	 * @param listener the listen to add to the control
	 */
	public static final void addWidgetDisposableListener(Button button, SelectionListener listener) {
		button.addSelectionListener(listener);
		button.addDisposeListener(ev -> button.removeSelectionListener(listener));
	}

	/**
	 * Adds a {@link FocusListener} to a {@link Control} and removes it before the {@code Control} is disposed.
	 * @param control  the element to which the listener is add
	 * @param focusLost the listener when {@code control} looses focus
	 * @param focusGained the listener when {@code control} gains focus
	 */
	public static final void addControlDisposableFocusListener(Control control, Consumer<FocusEvent> focusLost, Consumer<FocusEvent> focusGained) {
		FocusListener listener = new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				focusLost.accept(e);
			}

			@Override
			public void focusGained(FocusEvent e) {
				focusGained.accept(e);
			}
		};
		control.addFocusListener(listener);
		control.addDisposeListener(ev -> control.removeFocusListener(listener));
	}

	/**
	 * Utility to cast a {@link Widget#getData(String)} object
	 * @param widget the widget from where extract the data
	 * @return the object associated with CameraControlSpringEvent listener, otherwise {@code null}
	 */
	/**
	 * Retrieve and cast data from {@link Widget#getData(String)}
	 * @param <T>
	 * @param widget the widget from where require the data
	 * @param clazz the expected returned object class
	 * @param dataKey the ket to retrieve the data
	 * @return the required object if exists, otherwise {@code null}
	 */
	public static <T> T getDataObject(Widget widget, Class<T> clazz, String dataKey) {
		return Optional.ofNullable(widget.getData(dataKey))
				.map(clazz::cast)
				.orElseGet(() -> null);
	}
}
