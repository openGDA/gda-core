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

package uk.ac.gda.client.live.stream.controls.custom.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.gda.client.live.stream.controls.camera.state.ICameraState;
import uk.ac.gda.client.live.stream.view.customui.AbstractLiveStreamViewCustomUi;
import uk.ac.gda.client.live.stream.view.customui.LiveStreamViewCustomUi;
import uk.ac.gda.client.livecontrol.LiveControl;

/**
 * A {@link LiveStreamViewCustomUi} that uses a list of {@link LiveControl}
 * objects, if given, to populate the 'Camera Control' {@link Section}. It also
 * takes an optional {@link ICameraState} instance to synchronise camera control
 * action start, stop, and frezee with the actual state of camera hardware. It
 * can be further extended with a list of other {@link LiveStreamViewCustomUi}
 * objects.
 * 
 * @author fy65
 * @since 9.15
 *
 */
public class CustomCameraLiveControls extends AbstractLiveStreamViewCustomUi {
	private static final Logger logger = LoggerFactory.getLogger(CustomCameraLiveControls.class);
	private List<LiveControl> liveControls = new ArrayList<>();
	private ICameraState cameraState = null;
	private List<LiveStreamViewCustomUi> customUIs = new ArrayList<>();

	@Override
	public void createUi(Composite composite) {
		FormToolkit toolkit = new FormToolkit(composite.getDisplay());

		if (!liveControls.isEmpty()) {
			Section section = toolkit.createSection(composite,
					Section.EXPANDED | Section.TWISTIE | Section.TITLE_BAR);
			section.setLayout(GridLayoutFactory.fillDefaults().create());
			section.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
			section.setText("Camera Control");
			section.setExpanded(true);
			section.setEnabled(true);
			section.setVisible(true);
			Composite client = toolkit.createComposite(section, SWT.WRAP);
			client.setLayout(GridLayoutFactory.fillDefaults().numColumns(4).create());
			liveControls.stream().forEach(e -> e.createControl(client));
			toolkit.adapt(client);
			section.setClient(client);
		}

		// support additional custom UI
		customUIs.stream().forEach(e -> e.createUi(composite));

		// if present, synchronise camera state in GDA with EPICS control outside GDA
		if (cameraState != null) {
			if (!cameraState.isConnected()) {
				try {
					cameraState.connect();
				} catch (DeviceException e) {
					logger.error("Connect to camera state failed.", e);
				}
			} else {
				logger.info("Camera state is already connected.");
			}
		}
	}

	@Override
	public void dispose() {
		if (cameraState != null) {
			cameraState.dispose();
		}
	}

	public void setLiveControls(List<LiveControl> liveControls) {
		this.liveControls = liveControls;
	}

	public void setCameraState(ICameraState cameraState) {
		this.cameraState = cameraState;
	}

	public void setCustomUIs(List<LiveStreamViewCustomUi> customUIs) {
		this.customUIs = removeMyself(customUIs);
	}

	private List<LiveStreamViewCustomUi> removeMyself(List<LiveStreamViewCustomUi> customUIs2) {
		return customUIs2.stream().filter(e -> this != e).collect(Collectors.toList());
	}

}
