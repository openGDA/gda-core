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

package uk.ac.gda.arpes.ui.views;

import static uk.ac.gda.client.live.stream.view.StreamViewUtility.displayAndLogError;

import java.util.UUID;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.apres.ui.config.MonitoringViewConfiguration;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnectionManager;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;
import uk.ac.gda.client.live.stream.view.StreamType;

public class MonitoringView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(MonitoringView.class);
	LiveStreamConnection liveStreamConnection;
	private LivePlottingComposite plottingComposite;

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);

		MonitoringViewConfiguration viewConfiguration;
		try {
			viewConfiguration = Finder.findLocalSingleton(MonitoringViewConfiguration.class);
		} catch (IllegalArgumentException exception) {
			logger.error("Unable to create monitoring controls, no MonitoringViewConfiguration or more than one MonitoringViewConfiguration was found.", exception);
			return;
		}

		createLivePlot(parent, viewConfiguration);
		createControls(parent, viewConfiguration);
	}

	private void createLivePlot(Composite parent, MonitoringViewConfiguration viewConfiguration) {

		CameraConfiguration cameraConfig = viewConfiguration.getAnalyserLiveStreamConfiguration();
		if (cameraConfig == null) {
			logger.error("No analyser live stream configuration is defined in MonitoringViewConfiguration");
			return;
		}

		// Create the plotting view
		try {
			UUID streamID = LiveStreamConnectionManager.getInstance().getIStreamConnection(cameraConfig, StreamType.MJPEG);
			liveStreamConnection = (LiveStreamConnection) LiveStreamConnectionManager.getInstance().getIStreamConnection(streamID);
			plottingComposite = new LivePlottingComposite(parent, SWT.NONE, getPartName(), getViewSite().getActionBars(), liveStreamConnection, this);
			plottingComposite.setShowAxes(false);
			plottingComposite.setShowTitle(false);
			plottingComposite.activatePlottingSystem();
			GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingComposite);
		} catch (Exception e) {
			displayAndLogError(logger, parent, "Could not create plotting view", e);
			return;
		}
	}

	private void createControls(Composite parent, MonitoringViewConfiguration viewConfiguration) {

		Composite controlsComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(controlsComposite);
		controlsComposite.setLayout(new RowLayout(SWT.VERTICAL));

		if (viewConfiguration.getOverExposureControl() != null) {
			Group overExposureGroup = new Group(controlsComposite, SWT.NONE);
			overExposureGroup.setLayout(new FillLayout(SWT.VERTICAL));
			viewConfiguration.getOverExposureControl().createControl(overExposureGroup);
		}

		if (viewConfiguration.getTemperatureControls() != null && viewConfiguration.getTemperatureControls().getControls() != null) {
			Group temperaturesGroup = new Group(controlsComposite, SWT.NONE);
			temperaturesGroup.setLayout(new FillLayout(SWT.VERTICAL));
			viewConfiguration.getTemperatureControls().getControls().stream().forEach(c -> c.createControl(temperaturesGroup));
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}
}
