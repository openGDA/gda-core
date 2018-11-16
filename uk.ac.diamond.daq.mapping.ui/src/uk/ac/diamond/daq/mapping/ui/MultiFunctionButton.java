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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This widget consists of a button and a drop-down menu which lets the user
 * select the function the button performs.
 * <p>
 * Functions are added with {@link #addFunction(String, String, Image, Runnable)}
 * noting that the addition order is the order the functions are displayed on the menu
 * and the first function is selected by default.
 * <p>
 * Once all functions have been defined, the widget can be created
 * with {@link #draw(Composite)}.
 */
public class MultiFunctionButton {

	private List<ButtonFunction> functions = new ArrayList<>();
	private ToolItem button;
	private ButtonFunction selectedFunction;

	/**
	 * Add a function this widget could perform. The first function added is selected by default,
	 * and functions are listed on the menu by creation order.
	 *
	 * @param title the menu item text, and the button text if the icon is {@code null}
	 * @param description tooltips for menu item and button itself
	 * @param icon set as image for button and menu item unless {@code null}
	 * @param runnable run when this function is selected; cannot be {@code null}
	 */
	public void addFunction(String title, String description, Image icon, Runnable runnable) {
		Objects.requireNonNull(runnable, "The runnable associated with this function cannot be null!");
		functions.add(new ButtonFunction(title, description, icon, runnable));
	}

	/**
	 * This method draws the widget on given composite once all functions are set
	 *
	 * @param parent on which to draw this widget
	 */
	public void draw(Composite parent) {

		if (functions.isEmpty()) {
			throw new IllegalStateException("No functions set");
		}

		final Composite composite = new Group(parent, SWT.BORDER);
		composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridLayoutFactory.fillDefaults().applyTo(composite);
		GridDataFactory.fillDefaults().applyTo(composite);

		final ToolBar toolBar = new ToolBar(composite, SWT.FLAT);

		button = new ToolItem(toolBar, SWT.DROP_DOWN);
		button.addListener(SWT.Selection, e -> {
			if (e.detail == SWT.ARROW) { // i.e. clicked on the arrow
				final Menu menu = new Menu(composite.getShell(), SWT.POP_UP);
				for (ButtonFunction mode : functions) {
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					if (mode.getIcon() != null) item.setImage(mode.getIcon());
					item.setText(mode.getTitle());
					item.setToolTipText(mode.getDescription());
					item.addListener(SWT.Selection, itemSelection -> {
						// set as default function...
						setFunction(mode);
						// ...and run it
						selectedFunction.run();
					});
				}
				menu.setVisible(true);
			} else { // the function that's already selected
				selectedFunction.run();
			}
		});

		setFunction(functions.get(0));
	}

	private void setFunction(ButtonFunction mode) {
		selectedFunction = mode;

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
		private final Runnable runnable;

		private ButtonFunction(String title, String description, Image icon, Runnable runnable) {
			this.title = title != null ? title : "";
			this.description = description != null ? description : "";
			this.icon = icon;
			this.runnable = runnable;
		}

		private String getTitle() {
			return title;
		}

		private String getDescription() {
			return description;
		}

		private Image getIcon() {
			return icon;
		}

		private void run() {
			runnable.run();
		}
	}

}
