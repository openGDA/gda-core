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

import static org.eclipse.ui.forms.widgets.ExpandableComposite.EXPANDED;
import static org.eclipse.ui.forms.widgets.ExpandableComposite.TITLE_BAR;
import static org.eclipse.ui.forms.widgets.ExpandableComposite.TWISTIE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
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
	private int numberOfColumns = 5;

	@Override
	public void createWidget(Composite composite) {
		FormToolkit toolkit = new FormToolkit(composite.getDisplay());

		if (!liveControls.isEmpty()) {
			Section section = toolkit.createSection(composite, EXPANDED | TWISTIE | TITLE_BAR);
			section.setLayout(GridLayoutFactory.fillDefaults().create());
			section.setLayoutData(GridDataFactory.fillDefaults().create());
			section.setText("Camera Control");
			section.setExpanded(true);
			section.setEnabled(true);
			section.setVisible(true);
			Composite client = toolkit.createComposite(section, SWT.WRAP);
			client.setLayout(GridLayoutFactory.fillDefaults().numColumns(getNumberOfColumns()).create());
			liveControls.stream().forEach(e -> e.createControl(client));
			toolkit.adapt(client);
			section.setClient(client);
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

	public int getNumberOfColumns() {
		return numberOfColumns;
	}

	public void setNumberOfColumns(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(cameraState, liveControls, numberOfColumns);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CameraControlWidget other = (CameraControlWidget) obj;
		return Objects.equals(cameraState, other.cameraState) && Objects.equals(liveControls, other.liveControls)
				&& numberOfColumns == other.numberOfColumns;
	}

}
