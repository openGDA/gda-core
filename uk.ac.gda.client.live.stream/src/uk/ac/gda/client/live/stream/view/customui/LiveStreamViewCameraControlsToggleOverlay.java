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

package uk.ac.gda.client.live.stream.view.customui;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.monitor.EpicsMonitor;
import gda.device.scannable.SimplePVScannable;
import gda.factory.FactoryException;
import uk.ac.gda.api.camera.CameraControl;

/**
 * Extension for {@link LiveStreamViewCameraControls} that creates a check box to show and control whether an overlay is
 * activated.
 * <p>
 * There is no GDA object that allows you both to change a PV of type DBR_Enum and to monitor it, so this class must be
 * initialised with two {@link Scannable}s, one for each function, for example a {@link SimplePVScannable} and an
 * {@link EpicsMonitor} respectively.
 */
public class LiveStreamViewCameraControlsToggleOverlay implements LiveStreamViewCameraControlsExtension {
	private static final Logger logger = LoggerFactory.getLogger(LiveStreamViewCameraControlsToggleOverlay.class);

	/**
	 * Value of "overlay used" PV when the overlay is not activated
	 */
	private Object overlayNotUsed = "No";

	/**
	 * Value of "overlay used" PV when the overlay is activated
	 */
	private Object overlayUsed = "Yes";

	/**
	 * {@link Scannable} used to change the value of "overlay used"
	 */
	private final Scannable controlScannable;

	/**
	 * {@link Scannable} used to get & monitor changes to the value of "overlay used"
	 */
	private final Scannable monitorScannable;

	/**
	 * Check box to show & change "overlay used"
	 */
	private Button toggleCheckBox;

	public LiveStreamViewCameraControlsToggleOverlay(Scannable controlScannable, Scannable monitorScannable) {
		this.controlScannable = controlScannable;
		this.monitorScannable = monitorScannable;
	}

	@Override
	public void createUi(Composite composite, CameraControl cameraControl) {
		if (controlScannable == null) {
			logger.error("'Use overlay' control scannable must not be null.");
			return;
		}
		if (monitorScannable == null) {
			logger.error("'Use overlay' monitor scannable must not be null.");
			return;
		}
		try {
			controlScannable.configure();
			monitorScannable.configure();
		} catch (FactoryException e) {
			logger.error("Error configuring 'use overlay' monitor", e);
		}

		toggleCheckBox = new Button(composite, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(toggleCheckBox);
		toggleCheckBox.setText("Overlay");
		toggleCheckBox.setToolTipText("Make overlay visible");
		toggleCheckBox.addSelectionListener(widgetSelectedAdapter(e -> toggleOverlay()));
		monitorScannable.addIObserver((source, arg) -> setToggleState());

		setToggleState();
}

	private void setToggleState() {
		Display.getDefault().asyncExec(() -> {
			try {
				toggleCheckBox.setSelection(isOverlaySelected());
			} catch (DeviceException e) {
				logger.error("Error reading overlay status", e);
			}
		});
	}

	private boolean isOverlaySelected() throws DeviceException {
		return monitorScannable.getPosition().equals(overlayUsed);
	}

	private void toggleOverlay() {
		try {
			final Object newValue = isOverlaySelected() ? overlayNotUsed : overlayUsed;
			controlScannable.asynchronousMoveTo(newValue);
		} catch (DeviceException e) {
			logger.error("Error setting overlay status", e);
		}
	}

	public void setOverlayNotUsed(Object overlayNotUsed) {
		this.overlayNotUsed = overlayNotUsed;
	}

	public void setOverlayUsed(Object overlayUsed) {
		this.overlayUsed = overlayUsed;
	}
}
