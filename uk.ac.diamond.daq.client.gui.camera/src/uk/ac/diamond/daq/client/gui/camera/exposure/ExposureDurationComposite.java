package uk.ac.diamond.daq.client.gui.camera.exposure;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.device.DeviceException;
import gda.observable.IObserver;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.event.CameraEventUtils;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientBindingElements;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientResourceManager;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.ClientVerifyListener;
import uk.ac.gda.ui.tool.WidgetUtilities;
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
 * {@code useAcquireTimeMonitor} property in the {@link EpicsCameraControl} bean
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

	private static final Logger logger = LoggerFactory.getLogger(ExposureDurationComposite.class);

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite composite = createClientCompositeWithGridLayout(parent, style, 1);
		createClientGridDataFactory().applyTo(composite);

		createElements(composite, style);
		cameraControl = CameraHelper.getCameraControl(CameraHelper.getDefaultCameraProperties().getIndex());
		cameraControl.ifPresent(cc -> {
			initialiseElements(cc);
			ClientBindingElements.addDisposableObserver(composite, cc, cameraControlObserver);
		});
		bindElements();
		return composite;
	}

	private void createElements(Composite parent, int style) {
		Group group = createClientGroup(parent, SWT.NONE, 1, ClientMessages.EXPOSURE);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).indent(5, SWT.DEFAULT).applyTo(group);
		
		exposureText = ClientSWTElements.createClientText(group, style, ClientMessages.EMPTY_MESSAGE,
				Optional.ofNullable(ClientVerifyListener.verifyOnlyIntegerText));
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false)
				.hint(ClientSWTElements.DEFAULT_TEXT_SIZE).indent(5, SWT.DEFAULT).applyTo(exposureText);

		readOut = ClientSWTElements.createClientLabel(group, SWT.LEFT, ClientMessages.EMPTY_MESSAGE, Optional
				.ofNullable(FontDescriptor.createFrom(ClientResourceManager.getInstance().getTextDefaultFont())));
		createClientGridDataFactory().indent(5, SWT.DEFAULT).applyTo(readOut);

		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(group, getChangeActiveCameraListener(group));
		} catch (GDAClientException e) {
			UIHelper.showError(ClientMessages.CANNOT_LISTEN_CAMERA_PUBLISHER, e, logger);
		}
	}

	private void initialiseElements(CameraControl cameraControl) {
		try {
			int exposure = (int) (cameraControl.getAcquireTime() * 1000);
			updateGUI(exposure);
		} catch (DeviceException e) {
			UIHelper.showError("Error reading detector exposure", e, logger);
		}
	}

	private void bindElements() {
		// Sets the acquire time when user pushes return
		WidgetUtilities.addWidgetDisposableListener(exposureText, SWT.DefaultSelection,
				event -> setAcquireTime(event.widget));

		// Set the acquire time when exposureText looses focus
		WidgetUtilities.addControlDisposableFocusListener(exposureText, event -> setAcquireTime(event.widget),
				event -> {
				});
	}

	private void setAcquireTime(Widget widget) {
		cameraControl.ifPresent(c -> {
			try {
				c.setAcquireTime(Double.parseDouble(Text.class.cast(widget).getText()) / 1000);
			} catch (NumberFormatException | DeviceException e) {
				UIHelper.showError("Cannot update acquisition time", e, logger);
			}
		});
	}

	private void updateGUI(int exposure) {
		exposureText.setText(Integer.toString(exposure));
		updateReadOut(exposure);
	}

	private void updateReadOut(int exposure) {
		String unit = "ms";
		String strExposure = Integer.toString(exposure);
		if (exposure > 1000) {
			strExposure = Double.toString(exposure / 1000.);
			unit = "s";
		}
		readOut.setText(String.format("ReadOut: %s %s", strExposure, unit));
	}

	private void updateModelToGUI(CameraControllerEvent e) {
		updateGUI((int) (e.getAcquireTime() * 1000));
	}

	private ApplicationListener<ChangeActiveCameraEvent> getChangeActiveCameraListener(Composite parent) {
		return new ApplicationListener<ChangeActiveCameraEvent>() {
			@Override
			public void onApplicationEvent(ChangeActiveCameraEvent event) {
				// if the event arrives from a component with a different common parent, rejects
				// the event
				if (!event.haveSameParent(parent)) {
					return;
				}
				cameraControl = CameraHelper.getCameraControl(event.getActiveCamera().getIndex());
			}
		};
	}

	private Consumer<CameraControllerEvent> consumeExposure = cce -> Display.getDefault()
			.asyncExec(() -> updateModelToGUI(cce));
	private final IObserver cameraControlObserver = CameraEventUtils.cameraControlEventObserver(consumeExposure);
}