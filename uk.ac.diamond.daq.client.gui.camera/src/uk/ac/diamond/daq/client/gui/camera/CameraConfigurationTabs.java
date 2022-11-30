/* Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera;

import static uk.ac.diamond.daq.client.gui.camera.CameraHelper.createChangeCameraListener;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.absorption.AbsorptionComposite;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.diamond.daq.client.gui.camera.exposure.CameraControlsComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.diamond.daq.client.gui.camera.positioning.CameraPositioningComposite;
import uk.ac.diamond.daq.client.gui.camera.roi.SensorSelectionComposite;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

public class CameraConfigurationTabs implements CompositeFactory {
	private static Logger logger = LoggerFactory.getLogger(CameraConfigurationTabs.class);

	private CameraConfigurationProperties camera;
	private CameraImageComposite cameraImageComposite;

	private CTabFolder tabFolder;


	public CameraConfigurationTabs(CameraConfigurationProperties camera, CameraImageComposite cameraImageComposite) {
		this.camera = camera;
		this.cameraImageComposite = cameraImageComposite;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		GridDataFactory.swtDefaults().applyTo(composite);

		tabFolder = new CTabFolder(composite, SWT.BORDER);
		createTabs();
		tabFolder.setSelection(0);
		tabFolder.pack();

		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(composite, getChangeActiveCameraListener(composite));
		} catch (GDAClientException e) {
			UIHelper.showError("Cannot listen to camera publisher", e, logger);
		}

		return composite;
	}

	private void createTabs() {
		if (camera.getCameraControl() != null) {
			createTabItem(new CameraControlsComposite(camera), "Controls");
		}
		if (camera.getMotors()!= null) {
			createTabItem(new CameraPositioningComposite(camera), "Positioning");
		}

		createTabItem(new AbsorptionComposite(cameraImageComposite), "Absorption");

		if (camera.getGdaDetectorName() != null) {
			createTabItem(new SensorSelectionComposite(camera), "ROI");
		}
	}

	private CTabItem createTabItem(CompositeFactory cf, String label) {
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		Composite composite = cf.createComposite(tabFolder, SWT.NONE);
		tabItem.setText(label);
		tabItem.setControl(composite);
		return tabItem;

	}

	private ApplicationListener<ChangeActiveCameraEvent> getChangeActiveCameraListener(Composite parent) {
		return createChangeCameraListener(parent, detectorChange -> {
			if (detectorChange.getActiveCamera() != camera) {
				camera = detectorChange.getActiveCamera();
				for (CTabItem tab: tabFolder.getItems()) {
					if (tab.getControl() != null) {
						tab.getControl().dispose();
					}
					tab.dispose();
				}
				createTabs();
				tabFolder.setSelection(0);
				tabFolder.pack();
			}
		});
	}

}