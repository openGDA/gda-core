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

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.composites.AcquisitionCompositeButtonGroupFactoryBuilder;
import uk.ac.gda.ui.tool.ClientSWTElements;

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

	private static final Logger logger = LoggerFactory.getLogger(AcquisitionCompositeFactoryBuilder.class);

	private Optional<CompositeFactory> top = Optional.empty();
	private Optional<CompositeFactory> bottom = Optional.empty();

	private Optional<AcquisitionCompositeButtonGroupFactoryBuilder> acquisitionButtonGroupFactoryBuilder;

	private Composite container;
	private Composite topContainer;
	private Composite bottomContainer;

	public CompositeFactory build() {

		return (parent, style) -> {
			logger.debug("Creating {}", this);
			// The main container
			container = ClientSWTElements.createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
			createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(container);
			standardMarginHeight(container.getLayout());
			standardMarginWidth(container.getLayout());

			// The top area
			topContainer = ClientSWTElements.createClientCompositeWithGridLayout(container, SWT.NONE, 1);
			createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(topContainer);

			top.ifPresent(factory -> createTop(factory, topContainer));

			// The bottom area. The grab/vertical is false because this composite vertical size should be constant
			bottomContainer = ClientSWTElements.createClientCompositeWithGridLayout(container, SWT.NONE, 1);
			createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(bottomContainer);
			buttonsGroup(bottomContainer);
			bottom.ifPresent(factory -> factory.createComposite(bottomContainer, style));
			logger.debug("Created {}", this);
			return container;
		};
	}

	public AcquisitionCompositeFactoryBuilder addTopArea(CompositeFactory compositeFactory) {
		this.top = Optional.of(compositeFactory);
		logger.debug("Adding topArea {}", this.top);
		return this;
	}

	public AcquisitionCompositeFactoryBuilder addBottomArea(CompositeFactory compositeFactory) {
		this.bottom = Optional.of(compositeFactory);
		logger.debug("Adding bottomArea {}", this.bottom);
		return this;
	}

	public AcquisitionCompositeFactoryBuilder addAcquisitionButtonGroupFactoryBuilder(AcquisitionCompositeButtonGroupFactoryBuilder acquisitionButtonGroupFactoryBuilder) {
		this.acquisitionButtonGroupFactoryBuilder = Optional.of(acquisitionButtonGroupFactoryBuilder);
		return this;
	}

	private void createTop(CompositeFactory factory, Composite parent) {
		factory.createComposite(parent, SWT.NONE);
	}

	private void buttonsGroup(Composite parent) {
		acquisitionButtonGroupFactoryBuilder.ifPresent(a -> a.build().createComposite(parent, SWT.NONE));
	}
}
