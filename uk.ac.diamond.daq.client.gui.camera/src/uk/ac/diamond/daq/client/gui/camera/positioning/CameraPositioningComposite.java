package uk.ac.diamond.daq.client.gui.camera.positioning;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.springframework.context.ApplicationListener;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.MotorCompositeFactory;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Assembles different {@link Composite} as control panel for a camera. Listen
 * to {@link ChangeActiveCameraEvent} events to update the components
 * accordingly.
 * 
 * @author Maurizio Nagni
 */
public class CameraPositioningComposite implements CompositeFactory {

	private Composite motorCompositeArea;
	private Optional<Integer> activeCameraIndex;

	public CameraPositioningComposite() {
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite container = ClientSWTElements.createComposite(parent, style);
		activeCameraIndex = Optional.ofNullable(CameraHelper.getDefaultCameraProperties().getIndex());
		GridDataFactory gdf = GridDataFactory.fillDefaults().grab(true, true);

		// Motors Components
		motorCompositeArea = ClientSWTElements.createComposite(container, style);
		buildMotorsGUI();
		gdf.applyTo(motorCompositeArea);
		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(container,
					getChangeCameraListener(container));
		} catch (GDAClientException e) {
			UIHelper.showError("Cannot add camera change listener to CameraConfiguration", e);
		}
		return container;
	}

	private void buildMotorsGUI() {
		Arrays.stream(motorCompositeArea.getChildren()).forEach(Widget::dispose);
		getICameraConfiguration()
				.ifPresent(c -> c.getCameraProperties().getMotorProperties().stream().forEach(motor -> {
					MotorCompositeFactory mc = new MotorCompositeFactory(motor);
					mc.createComposite(motorCompositeArea, SWT.HORIZONTAL);
				}));
		motorCompositeArea.layout(true, true);
	}

	private Optional<ICameraConfiguration> getICameraConfiguration() {
		return activeCameraIndex.map(CameraHelper::createICameraConfiguration);
	}

	private ApplicationListener<ChangeActiveCameraEvent> getChangeCameraListener(Composite container) {
		return new ApplicationListener<ChangeActiveCameraEvent>() {
			@Override
			public void onApplicationEvent(ChangeActiveCameraEvent event) {
				if (!event.haveSameParent(container)) {
					return;
				}
				activeCameraIndex = Optional.of(event.getActiveCamera().getIndex());
				buildMotorsGUI();
			}
		};
	}
}
