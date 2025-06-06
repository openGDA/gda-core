/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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
import static uk.ac.gda.client.live.stream.Activator.createImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraState;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.livecontrol.LiveControl;
import uk.ac.gda.client.widgets.LiveStreamExposureTimeComposite;

/**
 * This class adds camera controls (start/stop/reset/change exposure) to a live stream view
 * <p>
 * It can be added to a {@link CameraConfiguration} as <code>topUi</code> or <code>bottomUi</code>.
 */
public class LiveStreamViewCameraControls extends AbstractLiveStreamViewCustomUi {
	private static Logger logger = LoggerFactory.getLogger(LiveStreamViewCameraControls.class);

	protected CameraControl cameraControl;
	protected Composite mainComposite;
	protected LiveControl cameraGainControl;

	/**
	 * Extensions to allow extra controls to be added to the basic set.<br>
	 * Each extensions will be allocated one column in {@link #mainComposite}.
	 */
	protected List<LiveStreamViewCameraControlsExtension> extensions = Collections.emptyList();

	/**
	 * Scannable to reset camera - the actual camera, not GDA's connection to it (optional)
	 */
	private Scannable cameraResetScannable;

	/**
	 * Allow to set exposure time while camera is Acquiring
	 */
	protected boolean changeExposureWhileCameraAcquiring = false;

	private boolean includeExposureTimeControl = true;

	private boolean includeCameraGain = false;

	private List<Image> images = new ArrayList<>();

	private Button playButton;
	private Button stopButton;
	private LiveStreamExposureTimeComposite exposureTimeComposite;

	public LiveStreamViewCameraControls(CameraControl cameraControl) {
		Objects.requireNonNull(cameraControl, "Camera control must not be null");
		this.cameraControl = cameraControl;
	}

	@Override
	public void createUi(Composite composite) {
		mainComposite = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(6 + extensions.size()).applyTo(mainComposite);

		// Exposure control
		if (includeExposureTimeControl) {
			exposureTimeComposite = new LiveStreamExposureTimeComposite(mainComposite, cameraControl, changeExposureWhileCameraAcquiring);
			GridDataFactory.swtDefaults().applyTo(exposureTimeComposite);
		}

		// Start/stop acquisition
		playButton = new Button(mainComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(playButton);
		playButton.setImage(createImage("play16x16.png"));
		playButton.setToolTipText("Start acquisition");
		playButton.addSelectionListener(widgetSelectedAdapter(this::startAcquiring));

		stopButton = new Button(mainComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(stopButton);
		stopButton.setImage(createImage("control-stop-square.png"));
		stopButton.setToolTipText("Stop acquisition");
		stopButton.addSelectionListener(widgetSelectedAdapter(this::stopAcquiring));

		// Reset button
		if (cameraResetScannable != null) {
			final Button resetButton = new Button(mainComposite, SWT.PUSH);
			GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(resetButton);
			resetButton.setText("Reset camera");
			resetButton.setToolTipText("Reconnect to camera");
			resetButton.addSelectionListener(widgetSelectedAdapter(this::resetCamera));
		}

		if (includeCameraGain) {
			cameraGainControl.createControl(mainComposite);
		}

		// Extensions
		for (LiveStreamViewCameraControlsExtension extension : extensions) {
			extension.createUi(mainComposite, cameraControl);
		}
	}

	public void setCameraControl(CameraControl cameraControl) {
		this.cameraControl = cameraControl;
	}

	@Override
	public void dispose() {
		images.forEach(Image::dispose);
		extensions.forEach(LiveStreamViewCameraControlsExtension::dispose);
		if (mainComposite != null) {
			mainComposite.dispose();
		}
		super.dispose();
	}

	private void resetCamera(@SuppressWarnings("unused") SelectionEvent e) {
		try {
			cameraResetScannable.asynchronousMoveTo(1);
		} catch (DeviceException ex) {
			logger.error("Error resetting camera {}", cameraControl.getName(), ex);
		}
	}

	protected void startAcquiring(@SuppressWarnings("unused") SelectionEvent e) {
		try {
			if (cameraControl.getAcquireState() == CameraState.IDLE ) {
				cameraControl.startAcquiring();
			} else {
				logger.debug("Detector is not idle - not starting it!");
			}
		} catch (Exception ex) {
			logger.error("Error starting data acquisition", ex);
		}
	}

	private void stopAcquiring(@SuppressWarnings("unused") SelectionEvent e) {
		try {
			cameraControl.stopAcquiring();
		} catch (DeviceException ex) {
			logger.error("Error stopping data acquisition", ex);
		}
	}

	protected void enableControls(boolean selection) {
		if (playButton != null) {
			playButton.setEnabled(selection);
		}

		if (stopButton != null) {
			stopButton.setEnabled(selection);
		}

		if (exposureTimeComposite != null) {
			exposureTimeComposite.setEnabled(selection);
		}
	}

	protected void setTooltip(String message) {
		mainComposite.setToolTipText(message);
	}

	public void setCameraResetScannable(Scannable cameraResetScannable) {
		this.cameraResetScannable = cameraResetScannable;
	}

	public void setExtensions(List<LiveStreamViewCameraControlsExtension> extensions) {
		this.extensions = extensions;
	}

	public void setExtension(LiveStreamViewCameraControlsExtension extension) {
		this.extensions = Arrays.asList(extension);
	}

	public void setChangeExposureWhileCameraAcquiring(boolean changeExposureWhileCameraAcquiring) {
		this.changeExposureWhileCameraAcquiring = changeExposureWhileCameraAcquiring;
	}

	public void setIncludeExposureTimeControl(boolean includeExposureTimeControl) {
		this.includeExposureTimeControl = includeExposureTimeControl;
	}

	public void setCameraGainControl(LiveControl cameraGainControl) {
		this.cameraGainControl = cameraGainControl;
	}

	public void setIncludeCameraGain(boolean includeCameraGain) {
		this.includeCameraGain = includeCameraGain;
	}

}