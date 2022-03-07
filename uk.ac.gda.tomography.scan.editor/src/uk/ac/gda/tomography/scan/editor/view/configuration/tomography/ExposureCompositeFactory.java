/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor.view.configuration.tomography;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.gda.ui.tool.ClientMessages.CONFIGURATION_LAYOUT_ERROR;
import static uk.ac.gda.ui.tool.ClientMessages.EXPOSURE;
import static uk.ac.gda.ui.tool.ClientMessages.EXPOSURE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.GUIComponents.doublePositiveContent;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionChangeEvent;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;

public class ExposureCompositeFactory implements CompositeFactory, Reloadable {

	private static final Logger logger = LoggerFactory.getLogger(ExposureCompositeFactory.class);

	private Text exposureText;
	private Button exposureButton;
	private Composite composite;

	private ScanningParameters parameters;

	public ExposureCompositeFactory() {
		try {
			parameters = SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class).getScanningParameters().orElseThrow();
		} catch (NoSuchElementException e) {
			UIHelper.showWarning("Tomography cannot be instantiated normally", e);
		}
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		composite = createClientGroup(parent, SWT.NONE, 1, EXPOSURE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(composite);

		var elementsComposite = new Composite(composite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).spacing(25, 0).applyTo(elementsComposite);

		ScanningAcquisitionListener acquisitionListener = new ScanningAcquisitionListener();
		SpringApplicationContextFacade.addDisposableApplicationListener(composite, acquisitionListener);

		logger.debug("Creating {}", this);
		try {
			createElements(elementsComposite, SWT.NONE, SWT.BORDER);
			bindElements();
			initialiseElements();
			addWidgetsListener();
			updateExposureTextFromDocument();
			logger.debug("Created {}", this);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}

		return composite;
	}

	private DetectorDocument getDetectorDocument() {
		return parameters.getDetectors().stream().findFirst().orElseThrow();
	}

	@Override
	public void reload() {
		if (composite == null || composite.isDisposed()) return;
		try {
			bindElements();
			initialiseElements();
			refreshParameters();
			updateExposureTextFromDocument();
			composite.getShell().layout(true, true);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}
	}

	private void createElements(Composite parent, int labelStyle, int textStyle) {
		this.exposureText = doublePositiveContent(parent, labelStyle, textStyle,
				EXPOSURE, EXPOSURE_TOOLTIP);
		this.exposureButton = new Button(parent, SWT.NONE);
		exposureButton.setText("Fetch from hardware");
		exposureButton.setToolTipText("Get current camera exposure time");

	}

	private void updateDetectorDocument() {
		var oldDetectorDocument = getDetectorDocument();
		var detectorDocument = new DetectorDocument.Builder()
				.withName(oldDetectorDocument.getName())
				.withMalcolmDetectorName(oldDetectorDocument.getMalcolmDetectorName())
				.withExposure(Double.parseDouble(exposureText.getText()))
				.build();

		parameters.setDetector(detectorDocument);
		publishUpdate();
	}

	private void bindElements() {
		// Nothing to do
	}

	private void initialiseElements() {
		refreshParameters();
	}

	private  void addWidgetsListener() {
		exposureText.addModifyListener(e -> updateDetectorDocument());
		exposureButton.addSelectionListener(widgetSelectedAdapter(e -> exposureText.setText(String.valueOf(getCameraExposure()))));
	}

	private double getCameraExposure(){
		var cameraControlClient = CameraHelper.getCameraConfigurationPropertiesByCameraControlName(getDetectorDocument().getName())
				.map(CameraHelper::createICameraConfiguration)
				.map(ICameraConfiguration::getCameraControlClient)
				.map(Optional::get);
		try {
			if (cameraControlClient.isPresent()) {
				return cameraControlClient.get().getAcquireTime();
			} else {
				return 0.0;
			}
		} catch (GDAClientRestException e) {
			logger.warn("Error reading detector exposure {}", e.getMessage());
			return 0.0;
		}
	}

	private void refreshParameters() {
		parameters = SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class).getScanningParameters().orElseThrow();
	}

	private void publishUpdate() {
		SpringApplicationContextFacade.publishEvent(new ScanningAcquisitionChangeEvent(this));
	}

	private class ScanningAcquisitionListener implements ApplicationListener<ScanningAcquisitionChangeEvent> {
		@Override
		public void onApplicationEvent(ScanningAcquisitionChangeEvent event) {
			if (!(event.getSource() instanceof ExposureCompositeFactory)) {
				refreshParameters();
				Display.getDefault().asyncExec(ExposureCompositeFactory.this::updateExposureTextFromDocument);
			}
		}
	}

	private void updateExposureTextFromDocument() {
		if (exposureText == null || exposureText.isDisposed()) return;
		exposureText.setText(String.valueOf(getDetectorDocument().getExposure()));
	}
}