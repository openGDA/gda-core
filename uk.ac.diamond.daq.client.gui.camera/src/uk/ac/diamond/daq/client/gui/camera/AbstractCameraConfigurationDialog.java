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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.HistogramComposite;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;

public abstract class AbstractCameraConfigurationDialog<C extends AbstractCameraConfigurationController> {
	private static final Logger log = LoggerFactory.getLogger(AbstractCameraConfigurationDialog.class);

	private static final int BUTTON_WIDTH = 80;

	protected Shell shell;
	protected C controller;
	private LiveStreamConnection liveStreamConnection;
	protected CameraImageComposite cameraImageComposite;
	private CameraConfiguration cameraConfiguration;

	public AbstractCameraConfigurationDialog (Display display, C controller, CameraConfiguration cameraConfiguration, 
			LiveStreamConnection liveStreamConnection, String title) throws DeviceException {	
		this.controller = controller;
		this.liveStreamConnection = liveStreamConnection;
		this.cameraConfiguration = cameraConfiguration;
		
		shell = new Shell(display, SWT.TITLE | SWT.RESIZE);
		shell.setText(title);
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
	
	protected abstract TabFolder createTabFolder () throws DeviceException;
		
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
