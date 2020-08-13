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

import gda.device.IScannableMotor;
import gda.rcp.views.AcquisitionCompositeFactoryBuilder;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.diamond.daq.mapping.ui.controller.ScanningAcquisitionController;
import uk.ac.diamond.daq.mapping.ui.controller.StageController;
import uk.ac.diamond.daq.mapping.ui.properties.DetectorHelper.AcquisitionType;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.MultipleScans;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;
import uk.ac.gda.tomography.browser.TomoBrowser;
import uk.ac.gda.tomography.scan.editor.view.TomographyConfigurationCompositeFactory;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * This Composite allows to edit a {@link ScanningParameters} object.
 *
 * @author Maurizio Nagni
 */
public class TomographyConfigurationView extends ViewPart {

	public static final String ID = "uk.ac.gda.tomography.view.TomographyConfigurationView";
	private static final Logger logger = LoggerFactory.getLogger(TomographyConfigurationView.class);

	private AcquisitionController<ScanningAcquisition> controller;

	@Override
	public void createPartControl(Composite parent) {
		controller = getPerspectiveController();
		getController().setDefaultNewAcquisitionSupplier(newScanningAcquisition());
		controller.createNewAcquisition();

		AcquisitionCompositeFactoryBuilder builder = new AcquisitionCompositeFactoryBuilder();
		builder.addTopArea(getTopArea());
		builder.addBottomArea(getBottomArea());
		builder.addNewSelectionListener(getNewConfigurationListener());
		builder.addSaveSelectionListener(getSaveListener());
		builder.addRunSelectionListener(getRunListener());
		builder.build().createComposite(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
		// Do not necessary
	}

	private ScanningAcquisitionController getPerspectiveController() {
		return (ScanningAcquisitionController)SpringApplicationContextProxy.getBean("scanningAcquisitionController", AcquisitionType.TOMOGRAPHY);
	}

	private StageController getStageController() {
		return SpringApplicationContextProxy.getBean(StageController.class);
	}

	private CompositeFactory getTopArea() {
		return new TomographyConfigurationCompositeFactory(getController(), getStageController());
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
				try {
					getController().getAcquisition().setAcquisitionLocation(getOutputPath());
					getController().runAcquisition();
				} catch (AcquisitionControllerException e) {
					UIHelper.showError("Run Acquisition", e.getMessage());
					logger.error("Cannot run the acquisition", e);
				} catch (ExperimentControllerException e) {
					UIHelper.showError("Run Acquisition", e.getMessage());
					logger.error(e.getMessage(), e);
				}
			}

			private URL getOutputPath() throws ExperimentControllerException {
				if (getExperimentController().isPresent()) {
					return getExperimentController().get().prepareAcquisition(controller.getAcquisition().getName());
				}
				return null;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				logger.debug("widgetDefaultSelected");
			}
		};
	}

	private Optional<ExperimentController> getExperimentController() {
		return SpringApplicationContextProxy.getOptionalBean(ExperimentController.class);
	}

	private AcquisitionController<ScanningAcquisition> getController() {
		return controller;
	}

	/**
	 * Creates a new {@link ScanningAcquisition} for a tomography acquisition.
	 * Note that the Detectors set by the {@link ScanningAcquisitionController#createNewAcquisition()}
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
			configuration.setImageCalibration(new ImageCalibration());

			// *-------------------------------
			ScanpathDocument.Builder scanpathBuilder = new ScanpathDocument.Builder();
			scanpathBuilder.withModelDocument(AcquisitionTemplateType.ONE_DIMENSION_LINE);
			ScannableTrackDocument.Builder scannableTrackBuilder = new ScannableTrackDocument.Builder();
			scannableTrackBuilder.withStart(0.0);
			scannableTrackBuilder.withStop(180.0);
			scannableTrackBuilder.withPoints(1);
			IScannableMotor ism = getStageController().getStageDescription().getMotors()
					.get(StageDevice.MOTOR_STAGE_ROT_Y);
			scannableTrackBuilder.withScannable(ism.getName());
			List<ScannableTrackDocument> scannableTrackDocuments = new ArrayList<>();
			scannableTrackDocuments.add(scannableTrackBuilder.build());
			scanpathBuilder.withScannableTrackDocuments(scannableTrackDocuments);
			acquisitionParameters.setScanpathDocument(scanpathBuilder.build());

			MultipleScans.Builder multipleScanBuilder = new MultipleScans.Builder();
			multipleScanBuilder.withMultipleScansType(MultipleScansType.REPEAT_SCAN);
			multipleScanBuilder.withNumberRepetitions(1);
			multipleScanBuilder.withWaitingTime(0);
			configuration.setMultipleScans(multipleScanBuilder.build());
			// *-------------------------------

			newConfiguration.getAcquisitionConfiguration().setAcquisitionParameters(acquisitionParameters);
			return newConfiguration;
		};
	}
}
