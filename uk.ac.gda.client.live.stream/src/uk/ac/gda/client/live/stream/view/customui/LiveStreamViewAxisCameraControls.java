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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.livecontrol.AxisCameraControls;

/**
 * AbstractLiveStreamViewCustomUi wrapper for {@link AxisCameraControls}.
 * The stream url is set automatically from the camera configuration of the enclosing live stream.
 *
 */
public class LiveStreamViewAxisCameraControls extends AbstractLiveStreamViewCustomUi {
	private static final Logger logger = LoggerFactory.getLogger(LiveStreamViewAxisCameraControls.class);
	private AxisCameraControls camControls = new AxisCameraControls();
	private int numColumns = 3;

	@Override
	public void createUi(Composite composite) {
		String urlString = getLiveStreamConnection().getCameraConfig().getUrl();
		try {
			URL url = new URL(urlString);
			camControls.setBaseUrl(urlString.replace(url.getPath(), ""));

			Composite mainComposite = new Composite(composite, SWT.NONE);
			GridLayoutFactory.fillDefaults().numColumns(numColumns).applyTo(mainComposite);
			camControls.createControl(mainComposite);
		} catch (MalformedURLException e) {
			logger.error("Problem creating Axis camera controls from URL {} in camera configuration", urlString, e);
		}
	}

	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

	public void setZoomStep(double zoomStep) {
		camControls.setZoomStep(zoomStep);
	}
	public void setFocusStep(double focusStep) {
		camControls.setFocusStep(focusStep);
	}

	public void setShowFocusControls(boolean showFocusControls) {
		camControls.setShowFocusControls(showFocusControls);
	}

	public void setShowIrisControls(boolean showIrisControls) {
		camControls.setShowIrisControls(showIrisControls);
	}

}
