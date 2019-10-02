package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import gda.device.DeviceException;
import gda.rcp.views.CompositeFactory;
import gda.rcp.views.TabCompositeFactory;
import gda.rcp.views.TabCompositeFactoryImpl;
import gda.rcp.views.TabFolderBuilder;
import uk.ac.diamond.daq.client.gui.camera.absorption.AbsorptionCompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.exposure.ExposureCompositeFactory;
import uk.ac.gda.client.live.stream.LiveStreamConnection;

public class CameraConfigurationDialog extends AbstractCameraConfigurationDialog<ImagingCameraConfigurationController> {
	private static final int MINIMUM_WIDTH = 960;
	private static final int MINIMUM_HEIGHT = 600;

	private static CameraConfigurationDialog instance;

	public static void show(Display display, IConnection liveStreamConnection) throws DeviceException {
		ImagingCameraConfigurationController controller = new ImagingCameraConfigurationController(
				CameraConfigurationView.CAMERA_CONTROL_NAME);

		Point minimumDialogSize = new Point(MINIMUM_WIDTH, MINIMUM_HEIGHT);
		Shell shell = new Shell(display, SWT.TITLE | SWT.RESIZE);
		shell.setText("Camera Configuration");
		shell.setSize(minimumDialogSize);
		shell.setMinimumSize(minimumDialogSize);

		CameraConfigurationDialog ccd = createCameraConfiguration(shell, liveStreamConnection, controller);
		ccd.createComposite(true);
		shell.addListener(SWT.Dispose, e -> {
			ccd.controller.dispose();
		});
		shell.open();
	}

	public static CameraConfigurationDialog createCameraConfiguration(Shell shell, IConnection liveStreamConnection,
			ImagingCameraConfigurationController controller) throws DeviceException {
		if (!LiveStreamConnection.class.isInstance(liveStreamConnection)) {
			throw new DeviceException("The dialog support only LiveStreamConnection class");
		}

		return new CameraConfigurationDialog(shell, controller, LiveStreamConnection.class.cast(liveStreamConnection));
	}

	CameraConfigurationDialog(Shell shell, ImagingCameraConfigurationController controller,
			LiveStreamConnection liveStreamConnection) throws DeviceException {
		super(shell, controller, liveStreamConnection);
	}

	CameraConfigurationDialog(Composite composite, ImagingCameraConfigurationController controller,
			LiveStreamConnection liveStreamConnection) throws DeviceException {
		super(composite, controller, liveStreamConnection);
	}

	
	
	@Override
	protected CompositeFactory createTabFactory(Composite parent) throws DeviceException {
		TabFolderBuilder builder = new TabFolderBuilder();
		builder.addTab(createExposureCompositeFactory());
		builder.addTab(createAbsorptionCompositeFactory());
		return builder.build();
//		
//
//		tabsFactory.createComposite(parent, SWT.NONE);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
//		return null;
	}

	protected final TabCompositeFactory createExposureCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new ExposureCompositeFactory<>(getController());
		group.setCompositeFactory(cf);
		group.setLabel("Exposure");
		return group;
	}

	protected final TabCompositeFactory createAbsorptionCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new AbsorptionCompositeFactory<>(getController());
		group.setCompositeFactory(cf);
		group.setLabel("Absorption");
		return group;
	}
}
