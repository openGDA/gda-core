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

import static uk.ac.gda.ui.tool.ClientMessages.CONFIGURATION_LAYOUT_ERROR;
import static uk.ac.gda.ui.tool.ClientMessages.EXPOSURE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;

import java.util.NoSuchElementException;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.device.DeviceException;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionChangeEvent;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.widgets.DetectorExposureWidget;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;

public class ExposureCompositeFactory implements CompositeFactory, Reloadable {

	private static final Logger logger = LoggerFactory.getLogger(ExposureCompositeFactory.class);

	private Composite composite;
	private DetectorExposureWidget exposureWidget;

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
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(composite);

		var elementsComposite = new Composite(composite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(true).applyTo(elementsComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(elementsComposite);

		ScanningAcquisitionListener acquisitionListener = new ScanningAcquisitionListener();
		SpringApplicationContextFacade.addDisposableApplicationListener(composite, acquisitionListener);

		exposureWidget = new DetectorExposureWidget(elementsComposite, this::updateDetectorDocument, this::getDetectorExposure);

		refreshParameters();
		updateExposureFromDocument();

		return composite;
	}

	private DetectorDocument getDetectorDocument() {
		return parameters.getDetectors().stream().findFirst().orElseThrow();
	}

	@Override
	public void reload() {
		try {
			refreshParameters();
			updateExposureFromDocument();
			composite.getShell().layout(true, true);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}
	}

	private void updateDetectorDocument(double exposure) {
		var oldDetectorDocument = getDetectorDocument();
		var detectorDocument = new DetectorDocument.Builder()
				.withId(oldDetectorDocument.getId())
				.withMalcolmDetectorName(oldDetectorDocument.getMalcolmDetectorName())
				.withExposure(exposure)
				.build();

		parameters.setDetector(detectorDocument);
		publishUpdate();
	}

	private double getDetectorExposure(){
		return CameraHelper.getCameraControlByCameraID(getDetectorDocument().getId())
			.map(this::getDetectorExposure).orElse(0.0);
	}

	private double getDetectorExposure(CameraControl control) {
		try {
			return control.getAcquireTime();
		} catch (DeviceException e) {
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
				Display.getDefault().asyncExec(ExposureCompositeFactory.this::updateExposureFromDocument);
			}
		}
	}

	private void updateExposureFromDocument() {
		if (composite == null || composite.isDisposed()) return;
		exposureWidget.updateFromModel(getDetectorDocument().getExposure());
	}
}