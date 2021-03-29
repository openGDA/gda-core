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

import static uk.ac.gda.ui.tool.ClientMessages.SAVED_SCAN_DEFINITION;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.rcp.views.AcquisitionCompositeFactoryBuilder;
import gda.rcp.views.Browser;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.selectable.SelectableContainedCompositeFactory;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

/**
 * This {@link ViewPart} allows to create, edit and run a {@link ScanningParameters} object for a generic acquisitions configuration.
 *
 * <p>
 * It is based on the {@link AcquisitionCompositeFactoryBuilder} consequently has two elements
 * <ul>
 * <li>
 *  A top area for the acquisition configuration managed by a {@link SelectableContainedCompositeFactory}
 * </li>
 * <li>
 *  A bottom area with a group of button to save/load/run operation and a browser containing the saved {@link ScanningAcquisition}s managed by a {@link AcquisitionsBrowserCompositeFactory} instance
 * </li>
 * </ul>
 * </p>
 *
 * The class is abstract in order to allow specific acquisition type layout, i.e Diffraction or Tomography.
 * @author Maurizio Nagni
 */
public abstract class AcquisitionConfigurationView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(AcquisitionConfigurationView.class);

	@Override
	public void createPartControl(Composite parent) {
		logger.debug("Creating {}", this);
		// The overall container
		Composite container = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().applyTo(container);
		container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		setContextAcquisitionController(createAcquisitionController());

		getAcquisitionController().ifPresent(c -> {
			c.setDefaultNewAcquisitionSupplier(newScanningAcquisition());
			c.createNewAcquisition();
		});

		AcquisitionCompositeFactoryBuilder builder = new AcquisitionCompositeFactoryBuilder();
		builder.addTopArea(getTopArea(builder.getControlButtonsContainerSupplier()));
		builder.addBottomArea(getBottomArea());
		builder.build().createComposite(container, SWT.NONE);
		logger.debug("Created {}", this);
	}

	@Override
	public void setFocus() {
		// Do not necessary
	}

	@Override
	public void dispose() {
		getAcquisitionController()
			.ifPresent(AcquisitionController::releaseResources);
		super.dispose();
	}

	/**
	 * Provides the layout for the acquisition configuration.
	 * <p>
	 * This view layout allows the {@link CompositeFactory} returned by this method to adds any necessary button to control this view.
	 * Consequently the {@code controlButtonsContainerSupplier} passes a {@link Composite} where those buttons can be drawn.
	 * </p>
	 *
	 * @param controlButtonsContainerSupplier a reference to a {@code Composite} instance where draw the control buttons
	 * @return an acquisition configuration factory
	 */
	protected abstract CompositeFactory getTopArea(Supplier<Composite> controlButtonsContainerSupplier);

	/**
	 * Returns the a {@link Composite} displaying the available acquisition configuration objects
	 * @return a {@code Browser} instance
	 */
	protected abstract Browser<?> getBrowser();
	/**
	 * Creates a brand new {@link ScanningAcquisition} document and update the associated {@link #getAcquisitionController()}
	 * <p>
	 * The generated document has to be consisted with the acquisition type this view is related to. This method is usually called by a <i>New</i> button
	 * </p>
	 * @return a new acquisition document
	 */
	protected abstract Supplier<ScanningAcquisition> newScanningAcquisition();

	private void buildSavedComposite(Composite parent) {
		Group group = createClientGroup(parent, SWT.NONE, 1, SAVED_SCAN_DEFINITION);
		createClientGridDataFactory().applyTo(group);
		CompositeFactory cf = new AcquisitionsBrowserCompositeFactory<>(getBrowser());
		Composite browser = cf.createComposite(group, SWT.BORDER);
		createClientGridDataFactory().applyTo(browser);
		standardMarginHeight(browser.getLayout());
		standardMarginWidth(browser.getLayout());
	}

	private CompositeFactory getBottomArea() {
		return (parent, style) -> {
			buildSavedComposite(parent);
			return parent;
		};
	}

	protected abstract AcquisitionController<ScanningAcquisition> createAcquisitionController();

	private void setContextAcquisitionController(AcquisitionController<ScanningAcquisition> controller) {
		Optional.ofNullable(controller)
			.ifPresent(getClientSpringContext()::setAcquisitionController);
	}

	/**
	 * The {@link AcquisitionController} associated with this view.
	 *
	 * @return an acquisition controller
	 */
	protected final Optional<AcquisitionController<ScanningAcquisition>> getAcquisitionController() {
		return getClientSpringContext().getAcquisitionController();
	}

	private ClientSpringContext getClientSpringContext() {
		return SpringApplicationContextFacade.getBean(ClientSpringContext.class);
	}
}
