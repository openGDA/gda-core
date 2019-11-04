package uk.ac.diamond.daq.client.gui.camera;

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
import uk.ac.diamond.daq.client.gui.camera.controller.DiffractionCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.diffraction.DiffractionConfigurationCompositeFactory;
import uk.ac.gda.client.live.stream.LiveStreamConnection;

public class DiffractionConfigurationDialog
		extends AbstractCameraConfigurationDialog<DiffractionCameraConfigurationController> {

	private static final int MINIMUM_WIDTH = 960;
	private static final int MINIMUM_HEIGHT = 600;

	private static DiffractionConfigurationDialog instance;

	public static void show(Display display, LiveStreamConnection liveStreamConnection) throws DeviceException {
		DiffractionCameraConfigurationController controller = new DiffractionCameraConfigurationController(
				"diffraction_camera_control", "det_position");

		Point minimumDialogSize = new Point(MINIMUM_WIDTH, MINIMUM_HEIGHT);
		Shell shell = new Shell(display, SWT.TITLE | SWT.RESIZE);
		shell.setText("Camera Configuration");
		shell.setSize(minimumDialogSize);
		shell.setMinimumSize(minimumDialogSize);

		instance = new DiffractionConfigurationDialog(shell, controller, liveStreamConnection);
		shell.addListener(SWT.Dispose, e -> {
			instance.controller.dispose();
			instance = null;
		});
		instance.createComposite(true);
		shell.open();
		shell.setVisible(true);
	}

	private DiffractionConfigurationDialog(Shell shell, DiffractionCameraConfigurationController controller,
			LiveStreamConnection liveStreamConnection) throws DeviceException {
		super(shell, controller, liveStreamConnection);
	}

	@Override
	protected CompositeFactory createTabFactory() throws DeviceException {
		TabFolderBuilder builder = new TabFolderBuilder();
		builder.addTab(createDiffractionConfigurationFactory());
		builder.addTab(createDiffractionAnalysisFactory());
		return builder.build();
	}
	
	protected final TabCompositeFactory createDiffractionConfigurationFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new DiffractionConfigurationCompositeFactory<>(getController());
		group.setCompositeFactory(cf);
		group.setLabel("Exposure");
		return group;
	}
	
	protected final TabCompositeFactory createDiffractionAnalysisFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new DiffractionConfigurationCompositeFactory<>(getController());
		group.setCompositeFactory(cf);
		group.setLabel("Diffraction Analysis");
		return group;
	}
}
