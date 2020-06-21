/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.rcp.views;

import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.client.composites.ButtonGroupFactoryBuilder;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Generic layout for an acquisition configuration.
 * It splits horizontally the composite in three areas:
 * <ol>
 * <li>
 *  <i>top</i>: contains the acquisition configuration elements
 * </li>
 * <li>
 *  <i>Save/Run buttons</i>: allows the user to Run or Save the acquisition actually edited
 * </li>
 * <li>
 *  <i>bottom</i>: allows the user to browser other available acquisition configurations
 * </li>
 * </ol>
 *
 * <p>Documented further on <a href="https://confluence.diamond.ac.uk/x/pyKeBg">Confluence</a>
 *
 * @author Maurizio Nagni
 */
public class AcquisitionCompositeFactoryBuilder {

	private Optional<CompositeFactory> top = Optional.empty();
	private Optional<CompositeFactory> bottom = Optional.empty();

	private Optional<SelectionListener> runListener = Optional.empty();
	private Optional<SelectionListener> newListener = Optional.empty();
	private Optional<SelectionListener> saveListener = Optional.empty();

	public CompositeFactory build() {

		return (parent, style) -> {
				Composite composite = ClientSWTElements.createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
				ClientSWTElements.createGridDataFactory().align(SWT.FILL, SWT.FILL).applyTo(composite);

				top.ifPresent(factory -> createTop(factory, composite));

				buttonsGroup(composite);

				bottom.ifPresent(factory -> factory.createComposite(composite, style));

				return parent;
		};
	}

	public AcquisitionCompositeFactoryBuilder addTopArea(CompositeFactory compositeFactory) {
		this.top = Optional.of(compositeFactory);
		return this;
	}

	public AcquisitionCompositeFactoryBuilder addBottomArea(CompositeFactory compositeFactory) {
		this.bottom = Optional.of(compositeFactory);
		return this;
	}

	public AcquisitionCompositeFactoryBuilder addRunSelectionListener(SelectionListener selectionListener) {
		this.runListener = Optional.of(selectionListener);
		return this;
	}

	public AcquisitionCompositeFactoryBuilder addNewSelectionListener(SelectionListener selectionListener) {
		this.newListener = Optional.of(selectionListener);
		return this;
	}

	public AcquisitionCompositeFactoryBuilder addSaveSelectionListener(SelectionListener selectionListener) {
		this.saveListener = Optional.of(selectionListener);
		return this;
	}

	private void createTop(CompositeFactory factory, Composite parent) {
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().applyTo(scrolledComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(scrolledComposite);
		final Composite container = new Composite(scrolledComposite, SWT.NONE);
		container.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		factory.createComposite(container, SWT.NONE);
		scrolledComposite.setContent(container);
	}

	private void buttonsGroup(Composite parent) {
		ButtonGroupFactoryBuilder builder = new ButtonGroupFactoryBuilder();

		newListener.ifPresent(listener -> builder.addButton(ClientMessages.NEW, ClientMessages.NEW_CONFIGURATION_TP,
										  listener, ClientImages.ADD));
		saveListener.ifPresent(listener -> builder.addButton(ClientMessages.SAVE, ClientMessages.SAVE_CONFIGURATION_TP,
										   listener, ClientImages.SAVE));
		runListener.ifPresent(listener -> builder.addButton(ClientMessages.RUN, ClientMessages.RUN_CONFIGURATION_TP,
										  listener, ClientImages.RUN));

		builder.build().createComposite(parent, SWT.NONE);
	}

}
