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
import uk.ac.diamond.daq.client.gui.camera.absorption.AbsorptionCompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.exposure.ExposureCompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.exposure.ROICompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.liveview.StreamControlCompositeFactory;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * 
 * 
 * @author Maurizio Nagni
 * @author Eliot Hall
 *
 */
public class CameraConfigurationDialog extends AbstractCameraConfigurationDialog<ImagingCameraConfigurationController> {
	private static final int MINIMUM_WIDTH = 960;
	private static final int MINIMUM_HEIGHT = 900;

	public static void show(Display display, ImagingCameraConfigurationController controller) throws DeviceException {
		Point minimumDialogSize = new Point(MINIMUM_WIDTH, MINIMUM_HEIGHT);
		Shell shell = new Shell(display, SWT.TITLE | SWT.RESIZE);
		shell.setText(ClientMessagesUtility.getMessage(ClientMessages.CAMERA_CONFIGURATION));
		shell.setSize(minimumDialogSize);
		shell.setMinimumSize(minimumDialogSize);
		CameraConfigurationDialog ccd = new CameraConfigurationDialog(shell, controller);
		ccd.createComposite(true);
		shell.addListener(SWT.Dispose, e -> {
			ccd.controller.dispose();
		});
		shell.open();
	}

	public CameraConfigurationDialog(Composite composite, ImagingCameraConfigurationController controller) {
		super(composite, controller);
	}
	
	@Override
	protected CompositeFactory createTabFactory() throws DeviceException {
		TabFolderBuilder builder = new TabFolderBuilder();
		builder.addTab(createStreamControlCompositeFactory());
		builder.addTab(createExposureCompositeFactory());
		builder.addTab(createAbsorptionCompositeFactory());
		builder.addTab(createROICompositeFactory());
		return builder.build();
	}	
	
	protected final TabCompositeFactory createStreamControlCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new StreamControlCompositeFactory();
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.STREAM));
		return group;
	}
	
	protected final TabCompositeFactory createROICompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new ROICompositeFactory<>(getController());
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.ROI));
		return group;
	}
	
	protected final TabCompositeFactory createExposureCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new ExposureCompositeFactory<>(getController());
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.CAMERA));
		return group;
	}

	protected final TabCompositeFactory createAbsorptionCompositeFactory() {
		TabCompositeFactoryImpl group = new TabCompositeFactoryImpl();
		CompositeFactory cf = new AbsorptionCompositeFactory<>(getController());
		group.setCompositeFactory(cf);
		group.setLabel(ClientMessagesUtility.getMessage(ClientMessages.ABSORPTION));
		return group;
	}
}
