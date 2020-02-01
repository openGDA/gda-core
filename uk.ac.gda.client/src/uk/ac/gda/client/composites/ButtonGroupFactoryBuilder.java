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

package uk.ac.gda.client.composites;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Facilitates the creation of an horizontal buttons group.
 * {@link #addButton(ClientMessages, ClientMessages, SelectionListener, ClientImages)} allows to compose the group
 * specifying, text, tooltip, image and selectionListener.
 *
 * An example is
 *
 * <pre>
 * {
 * 	&#64;code
 * 	ButtonGroupFactoryBuilder builder = new ButtonGroupFactoryBuilder();
 * 	builder.addButton(ClientMessages.LOAD, ClientMessages.LOAD_CONFIGURATION_TP, saveListener(), ClientImages.OPEN);
 * 	builder.addButton(ClientMessages.SAVE, ClientMessages.SAVE_CONFIGURATION_TP, loadListener(), ClientImages.SAVE);
 * 	builder.addButton(ClientMessages.RUN, ClientMessages.RUN_CONFIGURATION_TP, runListener(), ClientImages.RUN);
 * 	builder.build().createComposite(parent, SWT.NONE);
 * }
 * </pre>
 *
 * where <code>saveListener()</code>, <code>loadListener()</code>, <code>runListener()</code> are
 * {@link SelectionListener} provided by you.
 *
 *
 * @author Maurizio Nagni
 */
public class ButtonGroupFactoryBuilder {

	private final List<ButtonElements> buttonElements = new ArrayList<>();

	/**
	 * Adds a new {@link Button} to the group.
	 *
	 * @param message the button text
	 * @param tooltip the button tooltip
	 * @param listener the button listener to a selection event
	 * @param image the button image, eventually <code>null</code>
	 *
	 * @return a {@link CompositeFactory}
	 */
	public ButtonGroupFactoryBuilder addButton(ClientMessages message, ClientMessages tooltip,
			SelectionListener listener, ClientImages image) {
		buttonElements.add(new ButtonElements(message, tooltip, listener, image));
		return this;
	}

	/**
	 * Builds the {@link CompositeFactory}
	 *
	 * @return the compositeFactory
	 */
	public CompositeFactory build() {
		return new ButtonGroupCompositeFactory(buttonElements);
	}

	private class ButtonGroupCompositeFactory implements CompositeFactory {
		private final List<ButtonElements> buttonElements;
		private Composite container;

		public ButtonGroupCompositeFactory(List<ButtonElements> buttonElements) {
			this.buttonElements = Collections.unmodifiableList(buttonElements);
		}

		@Override
		public Composite createComposite(Composite parent, int style) {
			container = ClientSWTElements.createGroup(parent, buttonElements.size(), ClientMessages.EMPTY_MESSAGE, null,
					GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER));
			buttonElements.stream().forEachOrdered(this::createButton);
			return container;
		}

		private void createButton(ButtonElements bElement) {
			bElement.buildButton(container);
		}
	}

	private class ButtonElements {
		private final ClientMessages message;
		private final ClientMessages tooltip;
		private final SelectionListener listener;
		private final ClientImages image;

		public ButtonElements(ClientMessages message, ClientMessages tooltip, SelectionListener listener,
				ClientImages image) {
			super();
			this.message = Optional.ofNullable(message).orElse(ClientMessages.EMPTY_MESSAGE);
			this.tooltip = Optional.ofNullable(tooltip).orElse(ClientMessages.EMPTY_MESSAGE);
			this.listener = listener;
			this.image = image;
		}

		public void buildButton(Composite parent) {
			ClientSWTElements.createButton(parent, SWT.NONE, message, tooltip, image).addSelectionListener(listener);
		}
	}
}
