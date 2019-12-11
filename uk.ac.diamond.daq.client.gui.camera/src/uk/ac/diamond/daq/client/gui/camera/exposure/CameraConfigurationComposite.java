package uk.ac.diamond.daq.client.gui.camera.exposure;

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.springframework.context.ApplicationListener;

import uk.ac.diamond.daq.client.gui.camera.CameraComboItem;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.gda.client.composites.MotorCompositeFactory;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Assembles different {@link Composite} as control panel for a camera. Listen
 * to {@link ChangeActiveCameraEvent} events to update the components
 * accordingly.
 * 
 * @author Maurizio Nagni
 */
public class CameraConfigurationComposite extends Composite {

	private Composite motorCompositeArea;
	private CameraComboItem cameraCombo;
	private AbstractCameraConfigurationController cameraController;
	private final UUID uuidRoot;

	private ApplicationListener<ChangeActiveCameraEvent> changeCameraListener = new ApplicationListener<ChangeActiveCameraEvent>() {
		@Override
		public void onApplicationEvent(ChangeActiveCameraEvent event) { 
			if (!processEvent(event, uuidRoot)) {
				return;
			}
			updateCamera(event.getActiveCamera().getIndex());
			buildMotorsGUI();
		}

		private boolean processEvent(ChangeActiveCameraEvent event, UUID uuidRoot) {
			return event.getRootComposite() != null && event.getRootComposite().equals(uuidRoot);
		}
	};
	
	public CameraConfigurationComposite(Composite parent, int style) throws GDAClientException {
		super(parent, style);
		uuidRoot = ClientSWTElements.findParentUUID(getParent());
		updateCamera(CameraHelper.getDefaultCameraProperties().getIndex());

		GridLayoutFactory.swtDefaults().numColumns(4).equalWidth(false).applyTo(this);
		GridDataFactory gdf = GridDataFactory.fillDefaults();

		// Exposure Component
		Composite exposureLengthComposite = new ExposureDurationComposite(cameraController).createComposite(this,
				SWT.NONE);
		gdf.applyTo(exposureLengthComposite);

		// Binning Component
		Composite binningCompositeArea = ClientSWTElements.createComposite(this, style);
		new BinningComposite(binningCompositeArea, cameraController, SWT.NONE);
		gdf.applyTo(binningCompositeArea);

		// Motors Components
		motorCompositeArea = ClientSWTElements.createComposite(this, style);
		buildMotorsGUI();
		gdf.applyTo(motorCompositeArea);
		SpringApplicationContextProxy.addApplicationListener(changeCameraListener);
	}

	private void buildMotorsGUI() {
		Arrays.stream(motorCompositeArea.getChildren()).forEach(Widget::dispose);
		CameraHelper.getCameraProperties(cameraCombo.getIndex()).getMotorProperties().stream().forEach(motor -> {
			MotorCompositeFactory mc = new MotorCompositeFactory(motor);
			mc.createComposite(motorCompositeArea, SWT.NONE);
		});
		motorCompositeArea.layout(true, true);
	}

	private void updateCamera(int cameraIndex) {
		cameraCombo = CameraHelper.getCameraComboItems().get(cameraIndex);
		cameraController = CameraHelper.getCameraControlInstance(cameraIndex);
	}
}
