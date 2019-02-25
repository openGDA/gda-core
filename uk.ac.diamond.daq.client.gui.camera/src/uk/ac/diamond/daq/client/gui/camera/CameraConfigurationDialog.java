package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.absorption.AbsorptionConfigurationComposite;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationMode;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.exposure.ExposureConfigurationComposite;
import uk.ac.gda.client.live.stream.LiveStreamConnection;

public class CameraConfigurationDialog extends AbstractCameraConfigurationDialog<ImagingCameraConfigurationController> {
	private static final int EXPOSURE_TAB_INDEX = 0;
	private static final int ABSORPTION_TAB_INDEX = 1;
	
	private static CameraConfigurationDialog instance;
	
	public static void show (Display display, LiveStreamConnection liveStreamConnection) throws DeviceException {
		if (instance == null) {
			ImagingCameraConfigurationController controller = new ImagingCameraConfigurationController("imaging_camera_control");
			
			instance = new CameraConfigurationDialog(display, controller, 
					liveStreamConnection, "Camera Configuration");
			instance.shell.addListener(SWT.Dispose, e -> {
				instance.controller.dispose ();
				instance = null;
			});
		}
		instance.shell.open();
	}
	
	private CameraConfigurationDialog(Display display, ImagingCameraConfigurationController controller,
			LiveStreamConnection liveStreamConnection, String title) throws DeviceException {
		super (display, controller, liveStreamConnection, title);
	}
	
	@Override
	protected TabFolder createTabFolder () throws DeviceException {
		TabFolder tabFolder = new TabFolder(shell, SWT.TOP);
		TabItem exposureTab = new TabItem(tabFolder, SWT.NONE, EXPOSURE_TAB_INDEX);
		exposureTab.setText("Exposure");

		ExposureConfigurationComposite exposureConfigurationComposite = new ExposureConfigurationComposite(tabFolder, 
				controller, SWT.NONE);
		exposureTab.setControl(exposureConfigurationComposite);

		TabItem absorptionTab = new TabItem(tabFolder, SWT.NONE, ABSORPTION_TAB_INDEX);
		absorptionTab.setText("Absorption");

		AbsorptionConfigurationComposite absorptionConfigurationComposite = 
				new AbsorptionConfigurationComposite(tabFolder, controller, SWT.NONE);
		absorptionTab.setControl(absorptionConfigurationComposite);

		tabFolder.addListener(SWT.Selection, e -> {
			int index = tabFolder.getSelectionIndex();
			if (index == ABSORPTION_TAB_INDEX) {
				controller.setMode(CameraConfigurationMode.absorption);
			} else if (index == EXPOSURE_TAB_INDEX) {
				controller.setMode(CameraConfigurationMode.exposure);
			}
		});
		
		return tabFolder;
	}

}
