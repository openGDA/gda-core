package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.absorption.AbsorptionConfigurationComposite;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationMode;
import uk.ac.diamond.daq.client.gui.camera.exposure.ExposureConfigurationComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.HistogramComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;

public class CameraConfigurationDialog {
	private static final Logger log = LoggerFactory.getLogger(CameraConfigurationDialog.class);
	
	private static final int EXPOSURE_TAB_INDEX = 0;
	private static final int ABSORPTION_TAB_INDEX = 1;
	private static final int BUTTON_WIDTH = 80;
	
	private Shell shell;
	private LiveStreamConnection liveStreamConnection;
	private CameraConfigurationController controller;
	private CameraImageComposite cameraImageComposite;
	private CameraConfiguration cameraConfiguration;
	
	private static CameraConfigurationDialog instance;

	public static void show (Display display, CameraConfiguration cameraConfiguration, 
			LiveStreamConnection liveStreamConnection) throws DeviceException {
		if (instance == null) {
			instance = new CameraConfigurationDialog(display, cameraConfiguration, liveStreamConnection);
			instance.shell.addListener(SWT.Dispose, e -> {
				instance.controller.dispose ();
				instance = null;
			});
		}
		instance.shell.open();
	}
	
	private CameraConfigurationDialog(Display display, CameraConfiguration cameraConfiguration,
			LiveStreamConnection liveStreamConnection) throws DeviceException {		
		this.liveStreamConnection = liveStreamConnection;
		this.cameraConfiguration = cameraConfiguration;
		controller = new CameraConfigurationController();
		
		shell = new Shell(display, SWT.TITLE | SWT.RESIZE);
		shell.setText("Imaging Camera Configuration");
		shell.setSize(1000, 800);

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(shell);

		GridLayoutFactory.swtDefaults().applyTo(shell);

		Composite liveViewComposite = createLiveViewComposite(shell);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(liveViewComposite);

		TabFolder tabFolder = createTabFolder();
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).applyTo(tabFolder);

		Composite dialogLoadSaveComposite = createLoadSaveComposite(shell, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).applyTo(dialogLoadSaveComposite);
	}

	private Composite createLiveViewComposite(Composite composite) {
		Composite panel = new Composite(composite, SWT.NONE);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(panel);
		try {
			cameraImageComposite = new CameraImageComposite(panel, controller, liveStreamConnection,
					cameraConfiguration, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(cameraImageComposite);

			HistogramComposite histogramPanel = new HistogramComposite(panel, 
					cameraImageComposite.getPlottingSystem(), SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(histogramPanel);
		} catch (Exception e) {
			Label label;

			label = new Label(panel, SWT.NONE);
			label.setText("No Camera found");
			GridDataFactory.fillDefaults().grab(true, true).applyTo(label);

			label = new Label(panel, SWT.NONE);
			label.setText("No Camera found");
			GridDataFactory.fillDefaults().grab(true, true).applyTo(label);
		}

		return panel;
	}
	
	private TabFolder createTabFolder () throws DeviceException {
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

	private Composite createLoadSaveComposite(Composite parent, int style) {
		Composite panel = new Composite(parent, style);

		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(panel);

		Composite loadSavePanel = new Composite(panel, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(loadSavePanel);
		RowLayoutFactory.swtDefaults().applyTo(loadSavePanel);

		Button loadButton = new Button(loadSavePanel, SWT.PUSH);
		loadButton.setText("Load");
		loadButton.addListener(SWT.Selection, e -> load());
		RowDataFactory.swtDefaults().hint(BUTTON_WIDTH, -1).applyTo(loadButton);

		Button saveButton = new Button(loadSavePanel, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.addListener(SWT.Selection, e -> save());
		RowDataFactory.swtDefaults().hint(BUTTON_WIDTH, -1).applyTo(saveButton);

		// Spacing label between panels
		Label label = new Label(panel, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(label);

		Composite okCancelPanel = new Composite(panel, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(okCancelPanel);
		RowLayoutFactory.swtDefaults().applyTo(okCancelPanel);

		Button closeButton = new Button(okCancelPanel, SWT.PUSH);
		closeButton.setText("Close");
		closeButton.addListener(SWT.Selection, e -> shell.close());
		RowDataFactory.swtDefaults().hint(BUTTON_WIDTH, -1).applyTo(closeButton);
		
		return panel;
	}

	private void load() {
		log.info("I would have produced an open dialog");
	}

	private void save() {
		log.info("I would have produced a save dialog");
	}

}
