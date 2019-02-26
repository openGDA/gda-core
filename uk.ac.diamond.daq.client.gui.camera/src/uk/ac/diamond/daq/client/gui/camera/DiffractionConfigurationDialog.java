package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.DiffractionCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.diffraction.DiffractionAnalysisConfigurationComposite;
import uk.ac.diamond.daq.client.gui.camera.diffraction.DiffractionConfigurationComposite;
import uk.ac.gda.client.live.stream.LiveStreamConnection;

public class DiffractionConfigurationDialog extends AbstractCameraConfigurationDialog<DiffractionCameraConfigurationController> {
	private static final int EXPOSURE_TAB_INDEX = 0;
	private static final int DIFFRACTION_TAB_INDEX = 1;

	private static DiffractionConfigurationDialog instance;
		
	public static void show (Display display, LiveStreamConnection liveStreamConnection) throws DeviceException {
		if (instance == null) {
			DiffractionCameraConfigurationController controller = new DiffractionCameraConfigurationController(
					"diffraction_camera_control", "det_position");
			
			instance = new DiffractionConfigurationDialog(display, controller, liveStreamConnection, 
					"Diffraction Configuration");
			instance.shell.addListener(SWT.Dispose, e -> {
				instance.controller.dispose ();
				instance = null;
			});
		}
		instance.shell.open();
		instance.shell.setVisible(true);
	}
	
	private DiffractionConfigurationDialog(Display display, DiffractionCameraConfigurationController controller,
			LiveStreamConnection liveStreamConnection, String title) throws DeviceException {
		super (display, controller, liveStreamConnection, title);
	}

	@Override
	protected TabFolder createTabFolder () throws DeviceException {
		TabFolder tabFolder = new TabFolder(shell, SWT.TOP);
		TabItem exposureTab = new TabItem(tabFolder, SWT.NONE, EXPOSURE_TAB_INDEX);
		exposureTab.setText("Exposure");

		DiffractionConfigurationComposite diffractionConfigurationComposite = 
				new DiffractionConfigurationComposite(tabFolder, controller, SWT.NONE);
		exposureTab.setControl(diffractionConfigurationComposite);

		TabItem diffractionTab = new TabItem(tabFolder, SWT.NONE, DIFFRACTION_TAB_INDEX);
		diffractionTab.setText("Diffraction Analysis");

		DiffractionAnalysisConfigurationComposite diffractionAnalysisConfigurationComposite = 
				new DiffractionAnalysisConfigurationComposite(tabFolder, SWT.NONE);
		diffractionTab.setControl(diffractionAnalysisConfigurationComposite);
		
		return tabFolder;
	}
}
