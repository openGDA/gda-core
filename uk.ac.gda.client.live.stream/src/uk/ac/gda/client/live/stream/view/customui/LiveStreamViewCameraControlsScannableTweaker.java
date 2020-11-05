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

package uk.ac.gda.client.live.stream.view.customui;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.controlpoint.EpicsControlPoint;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.widgets.PositionControlComposite;

public class LiveStreamViewCameraControlsScannableTweaker implements LiveStreamViewCameraControlsExtension {
	private static final Logger logger = LoggerFactory.getLogger(LiveStreamViewCameraControlsScannableTweaker.class);

	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String displayName;
	private String tweakScannablePV;

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setTweakScannablePV(String tweakScannablePV) {
		this.tweakScannablePV = tweakScannablePV;
	}

	// Interface methods

	@Override
	public void createUi(Composite composite, CameraControl cameraControl) {
		if (tweakScannablePV == null) {
			logger.error("tweakScannable must not be null");
			return;
		}
		Channel tweakReverseChannel, tweakForwardChannel;
		try {
			tweakReverseChannel = EPICS_CONTROLLER.createChannel(tweakScannablePV+".TWR");
			tweakForwardChannel = EPICS_CONTROLLER.createChannel(tweakScannablePV+".TWF");
		}
		catch (Exception e) {
			logger.error("Unable to create tweakScannable channels", e);
			return;
		}

		final Composite mainComposite = new Composite(composite, SWT.NONE);

		if (displayName == null) {
			GridLayoutFactory.fillDefaults().numColumns(3).margins(0, 5).applyTo(mainComposite);
			logger.debug("displayName not set");
		} else {
			GridLayoutFactory.fillDefaults().numColumns(4).margins(0, 5).applyTo(mainComposite);
			final Label tweakDisplayName = new Label(mainComposite, SWT.NONE);
			GridDataFactory.swtDefaults().applyTo(tweakDisplayName);
			tweakDisplayName.setText(displayName);
			tweakDisplayName.setToolTipText("Tweak axis: "+tweakScannablePV);
		}

		final Button tweakReverse = new Button(mainComposite, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(tweakReverse);
		tweakReverse.setText("-");
		tweakReverse.setToolTipText("Tweak Reverse");
		tweakReverse.addSelectionListener(widgetSelectedAdapter(e -> {
			logger.debug("Tweak reverse button pushed: {}", tweakScannablePV);
			try {
				EPICS_CONTROLLER.caput(tweakReverseChannel, 1);
			} catch (Exception e1) {
				logger.error("Failed to tweak reverse", e1);
			}
		}));

		final EpicsControlPoint tweakControlPoint = new EpicsControlPoint();
		tweakControlPoint.setPvNameGetPoint(tweakScannablePV);
		tweakControlPoint.setPvNameSetPoint(tweakScannablePV+".RBV");
		tweakControlPoint.setName(displayName==null ? "" : displayName);
		try {
			tweakControlPoint.configure();
		} catch (FactoryException e) {
			logger.error("Failed to configure tweakControlPoint", e);
		}

		final PositionControlComposite positionReadbackComposite = new PositionControlComposite(mainComposite, SWT.NONE, tweakControlPoint);
		GridDataFactory.swtDefaults().applyTo(positionReadbackComposite);
		positionReadbackComposite.setToolTipText("Motor Position");

		final Button tweakForward = new Button(mainComposite, SWT.PUSH);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(tweakForward);
		tweakForward.setText("+");
		tweakForward.setToolTipText("Tweak Forward");
		tweakForward.addSelectionListener(widgetSelectedAdapter(e -> {
			logger.debug("Tweak forward button pushed: {}", tweakScannablePV);
			try {
				EPICS_CONTROLLER.caput(tweakForwardChannel, 1);
			} catch (Exception e1) {
				logger.error("Failed to tweak forward", e1);
			}
		}));
	}
}
