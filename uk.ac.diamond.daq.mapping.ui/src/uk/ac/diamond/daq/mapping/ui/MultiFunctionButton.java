/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * This widget consists of a button and a dropdown menu which lets the user
 * select the function the button performs. It is meant to duplicate the look
 * and behaviour of button-plus-menu widgets found in SWT toolbars, but as a
 * stand-alone widget that can be drawn on any composite.
 * <p>
 * Functions are added with {@link #addFunction(String, String, Image, Listener)}
 * noting that the addition order is the order the functions are displayed on the menu
 * and the first function is selected by default.
 * <p>
 * Once all functions have been defined, the widget can be created
 * with {@link #draw(Composite)}.
 */
public class MultiFunctionButton {

	private List<ButtonFunction> functions = new ArrayList<>();
	private Button button;

	/**
	 * Add a function this widget could perform. The first function added is selected by default,
	 * and functions are listed on the menu by creation order.
	 *
	 * @param title the menu item text, and the button text if the icon is {@code null}
	 * @param description tooltips for menu item and button itself
	 * @param icon set as image for button and menu item unless {@code null}
	 * @param listener attached to the button as a selection listener - cannot be {@code null}
	 */
	public void addFunction(String title, String description, Image icon, Listener listener) {
		Objects.requireNonNull(listener);
		functions.add(new ButtonFunction(title, description, icon, listener));
	}

	/**
	 * This method draws the widget on given composite once all functions are set
	 * @param parent
	 */
	public void draw(Composite parent) {

		if (functions.isEmpty()) {
			throw new IllegalStateException("No functions set");
		}

		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).spacing(0, 0).applyTo(composite);
		GridDataFactory.fillDefaults().applyTo(composite);

		button = new Button(composite, SWT.PUSH | SWT.FLAT);
		GridDataFactory.fillDefaults().applyTo(button);
		setFunction(functions.get(0));

		final Button arrow = new Button(composite, SWT.FLAT | SWT.ARROW | SWT.DOWN);
		GridDataFactory.fillDefaults().hint(18, SWT.DEFAULT).applyTo(arrow);

		arrow.addListener(SWT.Selection, arrowSelection -> {
			final Menu menu = new Menu(composite.getShell(), SWT.POP_UP);
			for (ButtonFunction mode : functions) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				if (mode.getIcon()!=null) item.setImage(mode.getIcon());
				item.setText(mode.getTitle());
				item.setToolTipText(mode.getDescription());
				item.addListener(SWT.Selection, itemSelection -> {
					setFunction(mode);
					mode.getListener().handleEvent(makeButtonSelectionEvent());
				});
			}
			menu.setVisible(true);
		});

	}

	private Event makeButtonSelectionEvent() {
		Event event = new Event();
		event.display = button.getDisplay();
		event.widget = button;
		event.type = SWT.Selection;
		event.detail = SWT.SELECTED;
		event.item = button;
		return event;
	}

	private void setFunction(ButtonFunction mode) {
		// remove current listener
		for (Listener listener : button.getListeners(SWT.Selection)) {
			button.removeListener(SWT.Selection, listener);
		}

		// add mode listener
		button.addListener(SWT.Selection, mode.getListener());

		// icon/title & tooltip
		if (mode.getIcon() != null) {
			button.setImage(mode.getIcon());
			button.setText("");
		} else {
			button.setImage(null);
			button.setText(mode.getTitle());
		}
		button.setToolTipText(mode.getDescription());
	}

	private final class ButtonFunction {

		private final String title;
		private final String description;
		private final Image icon;
		private final Listener listener;

		private ButtonFunction(String title, String description, Image icon, Listener listener) {
			this.title = title != null ? title : "";
			this.description = description != null ? description : "";
			this.icon = icon;
			this.listener = listener;
		}

		String getTitle() {
			return title;
		}

		String getDescription() {
			return description;
		}

		Image getIcon() {
			return icon;
		}

		Listener getListener() {
			return listener;
		}
	}

}
