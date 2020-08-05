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

package uk.ac.diamond.daq.mapping.ui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.observable.IObserver;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.event.CameraEventUtils;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionEvent;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionPropertiesDocument;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.properties.CameraProperties;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Manages a set of {@link AcquisitionPropertiesDocument} on behalf of a {@link ScanningAcquisitionController}
 *
 * <p>
 * The {@link AcquisitionPropertyType} required by the constructor selects the sets of
 * {@code AcquisitionPropertiesDocument}. Using one {@code AcquisitionPropertiesDocument} this object is able to
 * <ul>
 * <li>identify the acquisition engine to use</li>
 * <li>to read from and write to a number of detectors using the associated {@code CameraControl}s</li>
 * <li>to fill a new {@code ScanningAcquisition} with the {@code AcquisitionEngineDocument} and the associated
 * {@code DetectorDocument}</li>
 * </ul>
 * </p>
 *
 * Please note that this class is restricted to <i>package</i> as it is supposed to be used only by the
 * {@link ScanningAcquisitionController}
 *
 * @author Maurizio Nagni
 *
 */
class ScanningAcquisitionControllerDetectorHelper {
	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionControllerDetectorHelper.class);

	private final List<AcquisitionPropertiesDocument> acquisitionPropertiesDocuments = new ArrayList<>();
	private final Supplier<ScanningAcquisition> acquisitionSupplier;

	/**
	 * These are the camera associated with this acquisition controller. Some specific acquisition may use more than one
	 * camera, as BeamSelector scan in DIAD (K11)
	 */
	private final List<CameraControl> camerasControls = new ArrayList<>();

	/**
	 * Constructs an object to handle detectors for specific {@code AcquisitionType}. See
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/K11+GDA+Properties">Confluence</a>
	 *
	 * @param acquisitionType
	 *            the acquisition type for this controller
	 * @param acquisitionSupplier
	 *            a reference to the {@code ScanningAcquisitionController#getAcquisition()}
	 */
	ScanningAcquisitionControllerDetectorHelper(AcquisitionPropertyType acquisitionType,
			Supplier<ScanningAcquisition> acquisitionSupplier) {
		this.acquisitionSupplier = acquisitionSupplier;
		Optional.ofNullable(AcquisitionsPropertiesHelper.getAcquistionPropertiesDocument(acquisitionType))
				.ifPresent(acquisitionPropertiesDocuments::addAll);
		setCamerasControls();
	}

	/**
	 * Used by the parent {@code ScanningAcquisitionController} to release any resource before being disposed
	 */
	void releaseResources() {
		camerasControls.stream().forEach(item -> item.deleteIObserver(cameraControlObserver));
	}

	/**
	 * Used by the parent {@code ScanningAcquisitionController} to set in a new {@code ScanningAcquisition},
	 * {@code AcquisitionEngineDocument} and {@code DetectorDocument}
	 *
	 * @param acquisition
	 *            the new {@code ScanningAcquisition} instance
	 */
	void applyAcquisitionPropertiesDocument(ScanningAcquisition acquisition) {
		AcquisitionEngineDocument aed = createNewAcquisitionEngineDocument();
		acquisition.setAcquisitionEngine(aed);

		if (camerasControls.isEmpty()) {
			return;
		}
		int index = 0; // for now assumes one detector
		CameraControl cc = camerasControls.get(index);
		try {
			acquisition.getAcquisitionConfiguration().getAcquisitionParameters()
					.setDetector(new DetectorDocument(cc.getName(), cc.getAcquireTime()));
		} catch (DeviceException e) {
			UIHelper.showError("Cannot read exposure time.", e, logger);
		}
	}

	private void setCamerasControls() {
		AcquisitionPropertiesDocument dp = getAcquisitionPropertiesDocument();
		if (dp == null)
			return;

		dp.getCameras().stream()
				.map(CameraHelper::getCameraPropertiesByID)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(CameraProperties::getIndex)
				.map(CameraHelper::getCameraControl)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(cc -> {
					cc.addIObserver(cameraControlObserver);
					camerasControls.add(cc);
				});

	}

	/**
	 * Updates the scanning acquisition detector exposure time an publishes a {@link ScanningAcquisitionEvent}
	 */
	private void updateExposures(CameraControllerEvent cce) {
		ScanningParameters tp = getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
		// To activate when scanningParameters#getDetector will return an array
//		camerasControl.stream().filter(c1 -> {
//			return tp.getDetector().getName().equals(c1.getName());
//		}).findFirst().ifPresent(cc -> {
//			tp.setDetector(new DetectorDocument(tp.getDetector().getName(), cce.getAcquireTime()));
//		});
		tp.setDetector(new DetectorDocument(tp.getDetector().getName(), cce.getAcquireTime()));
		SpringApplicationContextProxy.publishEvent(new ScanningAcquisitionEvent(getAcquisition()));
	}

	private ScanningAcquisition getAcquisition() {
		return acquisitionSupplier.get();
	}

	private Consumer<CameraControllerEvent> consumeExposure = cce -> Display.getDefault()
			.asyncExec(() -> updateExposures(cce));
	private final IObserver cameraControlObserver = CameraEventUtils.cameraControlEventObserver(consumeExposure);

	private AcquisitionEngineDocument createNewAcquisitionEngineDocument() {
		AcquisitionPropertiesDocument dp = getAcquisitionPropertiesDocument();
		AcquisitionEngineDocument.Builder engineBuilder = new AcquisitionEngineDocument.Builder();
		if (dp != null) {
			engineBuilder.withId(dp.getEngine().getId());
			engineBuilder.withType(dp.getEngine().getType());
		}
		return engineBuilder.build();
	}

	private AcquisitionPropertiesDocument getAcquisitionPropertiesDocument() {
		if (acquisitionPropertiesDocuments.isEmpty()) {
			return null;
		}
		int index = 0; // in future may be parametrised
		return acquisitionPropertiesDocuments.get(index);
	}
}
