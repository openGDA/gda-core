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
import uk.ac.diamond.daq.mapping.ui.properties.DetectorHelper;
import uk.ac.diamond.daq.mapping.ui.properties.DetectorHelper.AcquisitionType;
import uk.ac.diamond.daq.mapping.ui.properties.DetectorPropertiesDocument;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.properties.CameraProperties;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Keeps a @{@link ScanningAcquisition}'s {@link DetectorDocument} section, updated with the referred detectors.
 * <p>
 * Each {@link AcquisitionType} refers to a list {@link DetectorPropertiesDocument} which are enough to
 * <ul>
 * <li>identify the relative {@link CameraControl}s, add an {@link IObserver} to it and on change publish the
 * {@code ScanninsAcquisition}, in Spring, {@link ScanningAcquisitionEvent}</li>
 * <li>continuously the internal {@code ScanningAcquisition} {@code DetectorDocument}</li>
 * </ul>
 * </p>
 *
 * <p>
 * The published {@code ScanningAcquisitionEvent} contains the {@code acquisition}. In this way other instances
 * containing a second {@code acquisition} may listen to {@code ScanningAcquisitionEvent}, compare the two by their
 * {@link ScanningAcquisition#getUuid()} and eventually update the second.
 * </p>
 *
 * Please note that this class is restricted to <i>package</i> as it is supposed to be used only by the
 * {@link ScanningAcquisitionController}
 *
 * @author Maurizio Nagni
 *
 * @see DetectorHelper
 */
class ScanningAcquisitionControllerDetectorHelper {
	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionControllerDetectorHelper.class);

	private final List<DetectorPropertiesDocument> detectorProperties;
	private final Supplier<ScanningAcquisition> acquisitionSupplier;

	/**
	 * These are the camera associated with this acquisition controller. Some specific acquisition may use more than one
	 * camera, as BeamSelector scan in DIAD (K11)
	 */
	private List<CameraControl> camerasControls;

	/**
	 * Constructs an object to handle detectors for specific {@code AcquisitionType}. See
	 * <a href="https://confluence.diamond.ac.uk/display/DIAD/K11+GDA+Properties">Confluence</a>
	 *
	 * @param acquisitionType
	 *            the acquisition type
	 * @param acquisitionSupplier
	 *            the acquisition configuration supplier
	 */
	public ScanningAcquisitionControllerDetectorHelper(AcquisitionType acquisitionType,
			Supplier<ScanningAcquisition> acquisitionSupplier) {
		this.acquisitionSupplier = acquisitionSupplier;
		this.detectorProperties = DetectorHelper.getAcquistionDetector(acquisitionType).orElse(new ArrayList<>());
		setTemplateDetector();
	}

	private void setTemplateDetector() {
		if (detectorProperties.isEmpty())
			return;
		int index = 0; // in future may be parametrised
		DetectorPropertiesDocument dp = detectorProperties.get(index);

		camerasControls = new ArrayList<>();
		dp.getCameras().stream().map(CameraHelper::getCameraPropertiesByID)
				.filter(Optional::isPresent).map(Optional::get).map(CameraProperties::getIndex)
				.map(CameraHelper::getCameraControl).filter(Optional::isPresent).map(Optional::get)
				.forEach(cc -> {
					cc.addIObserver(cameraControlObserver);
					camerasControls.add(cc);
					try {
						DetectorDocument dd = new DetectorDocument(dp.getDetectorBean(),
								camerasControls.get(0).getAcquireTime());
						getAcquisitionParameters().setDetector(dd);
					} catch (DeviceException e) {
						UIHelper.showError("Cannot read exposure time.", e, logger);
					}
				});
	}

	/**
	 * Updates the scanning parameters detectors exposure time
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

	private ScanningParameters getAcquisitionParameters() {
		return getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
	}

	private Consumer<CameraControllerEvent> consumeExposure = cce -> Display.getDefault()
			.asyncExec(() -> updateExposures(cce));
	private final IObserver cameraControlObserver = CameraEventUtils.cameraControlEventObserver(consumeExposure);
}
