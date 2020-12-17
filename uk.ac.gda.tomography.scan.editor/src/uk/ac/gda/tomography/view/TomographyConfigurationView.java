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

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.rest.ClientRestServices.getExperimentController;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.rcp.views.AcquisitionCompositeFactoryBuilder;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.diamond.daq.mapping.ui.controller.ScanningAcquisitionController;
import uk.ac.diamond.daq.mapping.ui.controller.StageController;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionTypeProperties;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.MultipleScans;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;
import uk.ac.gda.tomography.browser.TomoBrowser;
import uk.ac.gda.tomography.scan.editor.view.RadiographyConfigurationCompositeFactory;
import uk.ac.gda.tomography.scan.editor.view.TomographyConfigurationCompositeFactory;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.selectable.NamedComposite;
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
public class TomographyConfigurationView extends ViewPart {

	public static final String ID = "uk.ac.gda.tomography.view.TomographyConfigurationView";
	private static final Logger logger = LoggerFactory.getLogger(TomographyConfigurationView.class);

	private AcquisitionController<ScanningAcquisition> controller;

	@Override
	public void createPartControl(Composite parent) {
		logger.debug("Creating {}", this);
		// The overall container
		Composite container = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().applyTo(container);
		container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));

		controller = getPerspectiveController();
		getController().setDefaultNewAcquisitionSupplier(newScanningAcquisition());
		controller.createNewAcquisition();

		AcquisitionCompositeFactoryBuilder builder = new AcquisitionCompositeFactoryBuilder();
		builder.addTopArea(getTopArea());
		builder.addBottomArea(getBottomArea());
		builder.addNewSelectionListener(getNewConfigurationListener());
		builder.addSaveSelectionListener(getSaveListener());
		builder.addRunSelectionListener(getRunListener());
		builder.build().createComposite(container, SWT.NONE);
		logger.debug("Created {}", this);
	}

	@Override
	public void setFocus() {
		// Do not necessary
	}

	private ScanningAcquisitionController getPerspectiveController() {
		return (ScanningAcquisitionController) getBean("scanningAcquisitionController",
				AcquisitionsPropertiesHelper.AcquisitionPropertyType.TOMOGRAPHY);
	}

	private StageController getStageController() {
		return getBean(StageController.class);
	}

	private CompositeFactory getTopArea() {
		// Theses are the on-demand composites for the specific acquisition configurations
		List<NamedComposite> configurations = new ArrayList<>();
		configurations.add(new TomographyConfigurationCompositeFactory(getController(), getStageController()));
		configurations.add(new RadiographyConfigurationCompositeFactory());

		return new SelectableContainedCompositeFactory(configurations, ClientMessages.ACQUISITIONS);
	}

	private CompositeFactory getBottomArea() {
		return new AcquisitionsBrowserCompositeFactory<>(new TomoBrowser(getController()));
	}

	private SelectionListener getNewConfigurationListener() {
		return new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean confirmed = UIHelper.showConfirm("Create new configuration? The existing one will be discarded");
				if (confirmed) {
					controller.createNewAcquisition();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// not required
			}
		};
	}

	private SelectionListener getSaveListener() {
		return new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					controller.saveAcquisitionConfiguration();
				} catch (AcquisitionControllerException e) {
					UIHelper.showError("Cannot save the file", e);
					logger.error("Cannot save the file", e);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				// not necessary
			}
		};
	}

	private SelectionListener getRunListener() {
		return new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				Optional.ofNullable(getOutputPath()).ifPresent(this::runAcquisition);
			}

			private URL getOutputPath() {
				return prepareAcquisition();
			}

			private URL prepareAcquisition() {
				try {
					return getExperimentController().prepareAcquisition(controller.getAcquisition().getName());
				} catch (ExperimentControllerException e) {
					UIHelper.showError("Run Acquisition", e, logger);
					return null;
				}
			}

			private void runAcquisition(URL path) {
				getController().getAcquisition().setAcquisitionLocation(path);
				try {
					getController().runAcquisition();
				} catch (AcquisitionControllerException e) {
					UIHelper.showError("Run Acquisition", e, logger);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				logger.debug("widgetDefaultSelected");
			}
		};
	}

	private AcquisitionController<ScanningAcquisition> getController() {
		return controller;
	}

	/**
	 * Creates a new {@link ScanningAcquisition} for a tomography acquisition. Note that the Detectors set by the
	 * {@link ScanningAcquisitionController#createNewAcquisition()}
	 *
	 * @return
	 */
	private Supplier<ScanningAcquisition> newScanningAcquisition() {
		return () -> {
			ScanningAcquisition newConfiguration = new ScanningAcquisition();
			newConfiguration.setUuid(UUID.randomUUID());
			ScanningConfiguration configuration = new ScanningConfiguration();
			newConfiguration.setAcquisitionConfiguration(configuration);

			newConfiguration.setName("Default name");
			ScanningParameters acquisitionParameters = new ScanningParameters();
			configuration.setImageCalibration(new ImageCalibration.Builder().build());

			// *-------------------------------
			// When a new acquisitionType is selected, replaces the acquisition scanPathDocument
			String acquisitionType = "tomography";
			ScanpathDocument.Builder scanpathBuilder =
					AcquisitionTypeProperties.getAcquisitionProperties(acquisitionType)
					.buildScanpathBuilder(AcquisitionTemplateType.ONE_DIMENSION_LINE);
			acquisitionParameters.setScanpathDocument(scanpathBuilder.build());

			MultipleScans.Builder multipleScanBuilder = new MultipleScans.Builder();
			multipleScanBuilder.withMultipleScansType(MultipleScansType.REPEAT_SCAN);
			multipleScanBuilder.withNumberRepetitions(1);
			multipleScanBuilder.withWaitingTime(0);
			configuration.setMultipleScans(multipleScanBuilder.build());
			newConfiguration.getAcquisitionConfiguration().setAcquisitionParameters(acquisitionParameters);

			// --- NOTE---
			// The creation of the acquisition engine and the used detectors documents are delegated to the ScanningAcquisitionController
			// --- NOTE---

			return newConfiguration;
		};
	}
}
