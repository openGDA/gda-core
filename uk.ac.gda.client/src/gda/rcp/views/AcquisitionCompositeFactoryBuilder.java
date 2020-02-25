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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.ButtonGroupFactoryBuilder;
import uk.ac.gda.ui.tool.ClientMessages;
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

	private CompositeFactory top;
	private CompositeFactory bottom;
	private SelectionListener runSelectionListener;
	private SelectionListener saveSelectionListener;

	public CompositeFactory build() {

		return (parent, style) -> {
				GridLayoutFactory.fillDefaults().applyTo(parent);
				GridDataFactory.fillDefaults().applyTo(parent);

				createTop(parent);

				buttonsGroup(parent);

				Optional.ofNullable(bottom).ifPresent(e -> e.createComposite(parent, style));

				return parent;
		};
	}

	public AcquisitionCompositeFactoryBuilder addTopArea(CompositeFactory compositeFactory) {
		this.top = compositeFactory;
		return this;
	}

	public AcquisitionCompositeFactoryBuilder addBottomArea(CompositeFactory compositeFactory) {
		this.bottom = compositeFactory;
		return this;
	}

	public AcquisitionCompositeFactoryBuilder addRunSelectionListener(SelectionListener selectionListener) {
		this.runSelectionListener = selectionListener;
		return this;
	}

	public AcquisitionCompositeFactoryBuilder addSaveSelectionListener(SelectionListener selectionListener) {
		this.saveSelectionListener = selectionListener;
		return this;
	}

	private void createTop(Composite parent) {
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().applyTo(scrolledComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(scrolledComposite);
		final Composite container = new Composite(scrolledComposite, SWT.NONE);
		container.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		top.createComposite(container, SWT.NONE);
		scrolledComposite.setContent(container);
	}

	private SelectionListener getRunSelectionListener() {
		if (runSelectionListener == null) {
			return dummyLoadListener();
		}
		return runSelectionListener;
	}

	private SelectionListener getSaveSelectionListener() {
		if (saveSelectionListener == null) {
			return dummyLoadListener();
		}
		return saveSelectionListener;
	}

	private void buttonsGroup(Composite parent) {
		ButtonGroupFactoryBuilder builder = new ButtonGroupFactoryBuilder();
		builder.addButton(ClientMessages.SAVE, ClientMessages.SAVE_CONFIGURATION_TP, getSaveSelectionListener(),
				ClientImages.SAVE);
		builder.addButton(ClientMessages.RUN, ClientMessages.RUN_CONFIGURATION_TP, getRunSelectionListener(),
				ClientImages.RUN);
		builder.build().createComposite(parent, SWT.NONE);
	}

	private SelectionListener dummyLoadListener() {
		return new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				UIHelper.showWarning("Action invalid", "No action associated with the button");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				// not necessary
			}
		};
	}
}
