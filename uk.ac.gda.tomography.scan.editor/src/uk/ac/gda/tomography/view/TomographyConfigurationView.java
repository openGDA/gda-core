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

package uk.ac.gda.tomography.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import gda.rcp.views.AcquisitionCompositeFactoryBuilder;
import gda.rcp.views.Browser;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.ui.controller.ScanningAcquisitionController;
import uk.ac.diamond.daq.mapping.ui.experiment.controller.ExperimentScanningAcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.tomography.browser.TomoBrowser;
import uk.ac.gda.tomography.scan.editor.view.configuration.radiography.RadiographyButtonControlledCompositeFactory;
import uk.ac.gda.tomography.scan.editor.view.configuration.tomography.TomographyButtonControlledCompositeFactory;
import uk.ac.gda.ui.tool.AcquisitionConfigurationView;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.document.DocumentFactory;
import uk.ac.gda.ui.tool.selectable.NamedCompositeFactory;
import uk.ac.gda.ui.tool.selectable.SelectableContainedCompositeFactory;

/**
 * This {@link ViewPart} allows to create, edit and run a {@link ScanningParameters} object for tomography related acquisitions.
 *
 * <p>
 * It is based on the {@link AcquisitionCompositeFactoryBuilder} consequently has two elements
 * <ul>
 * <li>
 *  A top composite for the acquisition configuration managed by a {@link SelectableContainedCompositeFactory}
 * </li>
 * <li>
 *  A bottom composite with a group of button to save/load/run operation and a browser containing the saved {@link ScanningAcquisition}s managed by a {@link AcquisitionsBrowserCompositeFactory} instance
 * </li>
 * </ul>
 * </p>
 * @author Maurizio Nagni
 */
public final class TomographyConfigurationView extends AcquisitionConfigurationView {

	public static final String ID = "uk.ac.gda.tomography.view.TomographyConfigurationView";

	private AcquisitionController<ScanningAcquisition> acquisitionController;

	@Override
	protected CompositeFactory getTopArea(Supplier<Composite> controlButtonsContainerSupplier) {
		return new SelectableContainedCompositeFactory(initializeConfiguration(controlButtonsContainerSupplier),
				ClientMessages.ACQUISITIONS);
	}

	@Override
	protected Browser<?> getBrowser() {
		return getAcquisitionController()
				.map(TomoBrowser::new)
				.orElseGet(() -> new TomoBrowser(null));
	}

	/**
	 * Creates a new {@link ScanningAcquisition} for a tomography acquisition. Note that the Detectors set by the
	 * {@link ScanningAcquisitionController#createNewAcquisition()}
	 *
	 * @return a new scanning acquisition
	 */
	@Override
	protected Supplier<ScanningAcquisition> newScanningAcquisition() {
		return SpringApplicationContextFacade.getBean(DocumentFactory.class).newScanningAcquisition(AcquisitionTemplateType.ONE_DIMENSION_LINE, "tomography");
	}

	@Override
	protected AcquisitionController<ScanningAcquisition> createAcquisitionController() {
		return new ExperimentScanningAcquisitionController(AcquisitionPropertyType.TOMOGRAPHY);
	}

	private List<NamedCompositeFactory> initializeConfiguration(Supplier<Composite> controlButtonsContainerSupplier) {
		List<NamedCompositeFactory> configurations = new ArrayList<>();
		getAcquisitionController()
			.ifPresent(c -> {
				configurations.add(new TomographyButtonControlledCompositeFactory(c, controlButtonsContainerSupplier));
				configurations.add(new RadiographyButtonControlledCompositeFactory(c, controlButtonsContainerSupplier));
			});
		return configurations;
	}
}
