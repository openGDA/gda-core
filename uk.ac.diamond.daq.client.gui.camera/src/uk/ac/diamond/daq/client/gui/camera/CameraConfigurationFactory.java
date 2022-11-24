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

package uk.ac.diamond.daq.client.gui.camera;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.HistogramComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.StreamControlCompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.liveview.StreamControlData;
import uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * Composes instantiates the Composites for the Camera Configuration
 *
 * @author Maurizio Nagni
 *
 */
public class CameraConfigurationFactory implements CompositeFactory {

	protected CameraImageComposite cameraImageComposite;

	// The overall container for this components
	private Composite container;

	private Composite viewStream;
	private Composite viewHisto;
	private Composite tabsContainer;

	private StreamController streamController;
	private List<CameraConfigurationProperties> cameras;
	private Optional<CameraConfigurationProperties> defaultCamera;


	@Override
	public Composite createComposite(Composite parent, int style) {
		UUID uuid = UUID.randomUUID();

		configureCameras();
		configureController(uuid);
		configureLayout(parent, style, uuid);

		try {
			createCameraImageComposite(viewStream);
		} catch (Exception e) {
			UIHelper.showError("Cannot create CameraConfiguration", e);
			return null;
		}

		createHistogramComposite(viewHisto);
		createCameraConfigurationTabs(tabsContainer);
		return container;
	}

	private void configureLayout(Composite parent, int style, UUID uuid) {
	    container = createClientCompositeWithGridLayout(parent, SWT.NONE, 100);
	    createClientGridDataFactory().applyTo(container);
	    container.setData(CompositeFactory.COMPOSITE_ROOT, uuid);
		container.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

	    // -- TOP MENU --
		Composite menuBar = createClientCompositeWithGridLayout(container, SWT.NONE, 1);
		createClientGridDataFactory().span(100, 1).applyTo(menuBar);
		CompositeFactory cf = new StreamControlCompositeFactory(streamController, cameras);
		cf.createComposite(menuBar, SWT.NONE);

	    // -- SPLITS VERTIALLY THE CONTAINER --
		SashForm centralForm = new SashForm(container, SWT.HORIZONTAL);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).span(100, 1).applyTo(centralForm);

	    // -- DEFINES THE LEFT COLUMN --
		Composite leftColumn = createClientCompositeWithGridLayout(centralForm, SWT.BORDER, 1);
		createClientGridDataFactory().applyTo(leftColumn);

		viewStream = createClientCompositeWithGridLayout(leftColumn, style, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).span(1, 85).applyTo(viewStream);

		Composite viewStats = createClientGroup(leftColumn, SWT.NONE, 1, ClientMessages.EMPTY_MESSAGE);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).span(1, 15).applyTo(viewStats);

	    // -- DEFINES THE RIGHT COLUMN --
		Composite rightColumn = createClientCompositeWithGridLayout(centralForm, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(rightColumn);

	    // -- SPLITS HORIZONTALLY THE RIGHT COLUMN --
		SashForm verticalForm = new SashForm(rightColumn, SWT.VERTICAL | SWT.BORDER);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(verticalForm);

		viewHisto = createClientCompositeWithGridLayout(verticalForm, SWT.BORDER, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(viewHisto);

		tabsContainer = createClientCompositeWithGridLayout(verticalForm, SWT.BORDER, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(tabsContainer);

		// SETS THE LEFT/RIGHT WEIGHTS
		centralForm.setWeights(5, 3);
		// SETS THE LEFT COLUMN TOP/BOTTOM WEIGHTS
		verticalForm.setWeights(7, 2);
	}

	private void configureCameras() {
		// get all camera properties
		var camerasProperties = CameraHelper.getAllCameraConfigurationProperties();
		// create camera configurations based on camera properties
		var camerasConfiguration = camerasProperties.stream()
				.map(CameraHelper::createICameraConfiguration)
				.map(ICameraConfiguration::getCameraConfiguration)
				.filter(Optional::isPresent).map(Optional::get)
				.toList();

		cameras = camerasProperties.stream()
				.filter(property -> camerasConfiguration.stream()
						.anyMatch(config -> config.getName().equals(property.getConfiguration())))
				.toList();

		defaultCamera = cameras.stream().findFirst();
	}

	private void configureController(UUID uuid) {
		if (defaultCamera.isPresent()) {
			streamController = new StreamController(new StreamControlData(defaultCamera.get(), StreamType.EPICS_ARRAY), uuid);
		}
	}

	private void createCameraImageComposite(Composite panel) throws GDAClientException {
		cameraImageComposite = new CameraImageComposite(panel, SWT.NONE);
	}

	private void createHistogramComposite(Composite parent) {
		new HistogramComposite(cameraImageComposite.getPlottingSystem()).createComposite(parent);
	}

	private void createCameraConfigurationTabs(Composite parent) {
		if (defaultCamera.isPresent()) {
			new CameraConfigurationTabs(defaultCamera.get(), cameraImageComposite).createComposite(parent, SWT.NONE);
		}
	}
}
