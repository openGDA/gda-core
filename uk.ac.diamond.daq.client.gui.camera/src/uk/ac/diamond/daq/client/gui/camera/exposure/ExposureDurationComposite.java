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

package uk.ac.diamond.daq.client.gui.camera.exposure;

import static uk.ac.gda.ui.tool.ClientMessages.CANNOT_LISTEN_CAMERA_PUBLISHER;
import static uk.ac.gda.ui.tool.ClientMessages.EMPTY_MESSAGE;
import static uk.ac.gda.ui.tool.ClientMessages.EXPOSURE;
import static uk.ac.gda.ui.tool.ClientMessages.SECOND_SYMBOL;
import static uk.ac.gda.ui.tool.ClientSWTElements.DEFAULT_TEXT_SIZE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.device.DeviceException;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientResourceManager;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * A {@link Group} to edit a {@code EpicsCameraControl} {@code acquireTime},
 * expressed in milliseconds.
 *
 * <p>
 * This widget dynamically change the {@code cameraControl} it is attached
 * listening to {@link ChangeActiveCameraEvent} events.
 * </p>
 *
 * <p>
 * At the start the component points at the camera defined by
 * {@link CameraHelper#getDefaultCameraProperties()}
 * </p>
 *
 * <p>
 * <b>NOTE:</b> To works correctly this widget requires that the
 * {@code useAcquireTimeMonitor} property in the EpicsCameraControl bean
 * is set to {@code true} (usually in the configuration file).
 * </p>
 *
 * @author Maurizio Nagni
 */
public class ExposureDurationComposite implements CompositeFactory {

	/**
	 * The editable acquisition time
	 */
	private Text exposureText;
	/**
	 * The {@code cameraConfiguration} acquisition time
	 */
	private Label readOut;
	private Optional<CameraControl> cameraControl;

	private Composite container;

	private static final String SECOND = ClientMessagesUtility.getMessage(SECOND_SYMBOL);

	private static final Logger logger = LoggerFactory.getLogger(ExposureDurationComposite.class);

	@Override
	public Composite createComposite(Composite parent, int style) {
		container = createClientCompositeWithGridLayout(parent, style, 1);
		createClientGridDataFactory().applyTo(container);

		createElements(container, style);
		cameraControl = CameraHelper.getCameraControl(CameraHelper.getDefaultCameraProperties().getIndex());
		cameraControl.ifPresent(this::initialiseElements);
		SpringApplicationContextFacade.addDisposableApplicationListener(this, cameraControlSpringEventListener);
		return container;
	}

	private void createElements(Composite parent, int style) {
		Group group = createClientGroup(parent, SWT.NONE, 3, EXPOSURE);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).indent(5, SWT.DEFAULT).applyTo(group);

		exposureText = new ExposureTextField(group, style, () -> cameraControl).getExposure();
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false)
				.hint(DEFAULT_TEXT_SIZE).indent(5, SWT.DEFAULT).applyTo(exposureText);
		Label unit = createClientLabel(group, SWT.BEGINNING, SECOND_SYMBOL,
				FontDescriptor.createFrom(ClientResourceManager.getInstance().getTextDefaultFont()));
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.END).span(2,1).applyTo(unit);


		readOut = createClientLabel(group, SWT.LEFT, EMPTY_MESSAGE,
				FontDescriptor.createFrom(ClientResourceManager.getInstance().getTextDefaultFont()));
		createClientGridDataFactory().indent(5, SWT.DEFAULT).applyTo(readOut);

		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(group, getChangeActiveCameraListener(group));
		} catch (GDAClientException e) {
			UIHelper.showError(CANNOT_LISTEN_CAMERA_PUBLISHER, e, logger);
		}
	}

	private void initialiseElements(CameraControl cameraControl) {
		try {
			updateGUI(cameraControl.getAcquireTime());
		} catch (DeviceException e) {
			logger.warn("Error reading detector exposure {}", e.getMessage());
		}
	}

	private void updateGUI(double exposure) {
		updateReadOut(exposure);
		container.layout(true, true);
	}

	private void updateReadOut(double exposure) {
		readOut.setText(String.format("ReadOut: %s %s", ExposureTextField.decimalFormat.format(exposure), SECOND));
	}

	private ApplicationListener<ChangeActiveCameraEvent> getChangeActiveCameraListener(Composite parent) {
		return CameraHelper.createChangeCameraListener(parent, changeCameraControl);
	}

	private Consumer<ChangeActiveCameraEvent> changeCameraControl = event -> {
		cameraControl = CameraHelper.getCameraControl(event.getActiveCamera().getIndex());	
	};
	
	// At the moment is not possible to use anonymous lambda expression because it
	// generates a class cast exception
	private ApplicationListener<CameraControlSpringEvent> cameraControlSpringEventListener = new ApplicationListener<CameraControlSpringEvent>() {
		@Override
		public void onApplicationEvent(CameraControlSpringEvent event) {
			cameraControl.ifPresent(cc -> {
				if (event.getName().equals(cc.getName())) {
					Display.getDefault().asyncExec(() -> updateModelToGUI(event));
				}
			});
		}

		private void updateModelToGUI(CameraControlSpringEvent e) {
			updateGUI(e.getAcquireTime());
		}
	};
}