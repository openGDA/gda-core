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
import java.util.function.Supplier;

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
 *  <i>Control buttons</i>: typically allows the user to Run, Save the acquisition actually edited or create a New one
 * </li>
 * <li>
 *  <i>bottom</i>: allows the user to browser other previously saved acquisition configurations
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

	private Optional<AcquisitionCompositeButtonGroupFactoryBuilder> acquisitionButtonGroupFactoryBuilder  = Optional.empty();

	private Composite container;

	private Composite topContainer;
	private Composite controlButtonsContainer;
	private Composite browserContainer;

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

			// The control buttons area. The grab/vertical is false because this composite vertical size should be constant
			controlButtonsContainer = ClientSWTElements.createClientCompositeWithGridLayout(container, SWT.NONE, 1);
			createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(controlButtonsContainer);

			// The bottom area. The grab/vertical is false because this composite vertical size should be constant
			browserContainer = ClientSWTElements.createClientCompositeWithGridLayout(container, SWT.NONE, 1);
			createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(browserContainer);

			top.ifPresent(factory -> createTop(factory, topContainer));

			bottom.ifPresent(factory -> factory.createComposite(browserContainer, style));

			acquisitionButtonGroupFactoryBuilder.ifPresent(a -> a.build().createComposite(getControlButtonsContainer(), SWT.NONE));

			logger.debug("Created {}", this);
			return container;
		};
	}

	public AcquisitionCompositeFactoryBuilder addTopArea(CompositeFactory compositeFactory) {
		this.top = Optional.of(compositeFactory);
		logger.debug("Adding topArea {}", this.top);
		return this;
	}

	public Supplier<Composite> getControlButtonsContainerSupplier() {
		return this::getControlButtonsContainer;
	}

	public AcquisitionCompositeFactoryBuilder addBottomArea(CompositeFactory compositeFactory) {
		this.bottom = Optional.of(compositeFactory);
		logger.debug("Adding bottomArea {}", this.bottom);
		return this;
	}

	/**
	 * @param acquisitionButtonGroupFactoryBuilder
	 * @return a factory builder to assemble the acquistion configuration control
	 *
	 * @deprecated Classes using this method should be refactored to extend AcquisitionConfigurationView.
	 * See TomographyConfigurationView or DiffractionConfigurationView To be removed on GDA 9.21
	 */
	@Deprecated
	public AcquisitionCompositeFactoryBuilder addAcquisitionButtonGroupFactoryBuilder(AcquisitionCompositeButtonGroupFactoryBuilder acquisitionButtonGroupFactoryBuilder) {
		this.acquisitionButtonGroupFactoryBuilder = Optional.of(acquisitionButtonGroupFactoryBuilder);
		return this;
	}

	private Composite getControlButtonsContainer() {
		return controlButtonsContainer;
	}

	private void createTop(CompositeFactory factory, Composite parent) {
		factory.createComposite(parent, SWT.NONE);
	}
}
