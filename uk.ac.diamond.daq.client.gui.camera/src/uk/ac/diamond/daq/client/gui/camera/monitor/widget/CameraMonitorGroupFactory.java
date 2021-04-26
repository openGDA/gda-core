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

package uk.ac.diamond.daq.client.gui.camera.monitor.widget;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * Builds a set of {@link CameraMonitorButton}s based on cameras parsed by the {@link CameraHelper}
 *
 * @author Maurizio Nagni
 */
public class CameraMonitorGroupFactory implements CompositeFactory {

	private Composite container;

	private static final Logger logger = LoggerFactory.getLogger(CameraMonitorGroupFactory.class);

	@Override
	public Composite createComposite(Composite parent, int style) {
		int cols = CameraHelper.getAllCameraConfigurationProperties().size();
		container = createClientCompositeWithGridLayout(parent, SWT.NONE, cols);
		createClientGridDataFactory().align(SWT.CENTER, SWT.BEGINNING).applyTo(container);

		Label labelName = createClientLabel(container, SWT.NONE, ClientMessages.CAMERAS_MONITORS);
		createClientGridDataFactory().align(SWT.CENTER, SWT.BEGINNING).span(cols, 1).applyTo(labelName);

		CameraHelper.getAllCameraConfigurationProperties().stream()
			.filter(p -> CameraHelper.getCameraMonitors().contains(p.getId()))
			.map(CameraHelper::createICameraConfiguration)
			.forEach(this::createButton);
		return container;
	}

	private void createButton(ICameraConfiguration cameraConfiguration) {
		try {
			new CameraMonitorButton(container, cameraConfiguration);
		} catch (GDAClientException e) {
			logger.error(e.getMessage());
		}
	}
}