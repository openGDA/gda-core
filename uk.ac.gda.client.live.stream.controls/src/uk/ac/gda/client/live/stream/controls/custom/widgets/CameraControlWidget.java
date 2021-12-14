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

package uk.ac.gda.client.live.stream.controls.custom.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FindableBase;
import uk.ac.gda.client.live.stream.controls.camera.state.ICameraState;
import uk.ac.gda.client.live.stream.view.ICustomWidget;
import uk.ac.gda.client.livecontrol.LiveControl;

public class CameraControlWidget extends FindableBase implements ICustomWidget {
private static final Logger logger=LoggerFactory.getLogger(CameraControlWidget.class);
	private List<LiveControl> liveControls = new ArrayList<>();
	private ICameraState cameraState = null;

	@Override
	public void createWidget(Composite composite) {
		if (!liveControls.isEmpty()) {
			Group cameraControlGroup = new Group(composite, SWT.BORDER);
			cameraControlGroup.setLayout(new GridLayout(4, false));
			cameraControlGroup.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
			cameraControlGroup.setText("Camera Control");
			for (LiveControl control : liveControls) {
				control.createControl(cameraControlGroup);
			}
		}
		if (cameraState !=null) {
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

	public void setLiveControls(List<LiveControl> liveControls) {
		this.liveControls = liveControls;
	}

	/**
	 * @param cameraState
	 *            The cameraState to set.
	 */
	public void setCameraState(ICameraState cameraState) {
		this.cameraState = cameraState;
	}

	@Override
	public void disposeWidget() {
		if (cameraState !=null) {
			cameraState.dispose();
		}
	}

}
