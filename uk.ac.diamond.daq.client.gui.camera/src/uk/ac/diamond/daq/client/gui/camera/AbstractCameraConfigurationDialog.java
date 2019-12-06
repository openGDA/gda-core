package uk.ac.diamond.daq.client.gui.camera;

import java.util.UUID;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.rcp.views.CompositeFactory;
import gda.rcp.views.TabFolderBuilder;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.LiveViewCompositeFactory;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;

public abstract class AbstractCameraConfigurationDialog<T extends AbstractCameraConfigurationController> {

	private static final int BUTTON_WIDTH = 80;

	private final Shell shell;
	private final Composite parent;
	protected final T controller;
	private IConnection liveStreamConnection;
	protected CameraImageComposite cameraImageComposite;

	private static final Logger logger = LoggerFactory.getLogger(AbstractCameraConfigurationDialog.class);

	public AbstractCameraConfigurationDialog(Composite composite, T controller, IConnection liveStreamConnection) {
		this(composite, controller);
		this.liveStreamConnection = liveStreamConnection;
	}

	public AbstractCameraConfigurationDialog(Composite composite, T controller) {
		if (Shell.class.isInstance(composite)) {
			this.shell = Shell.class.cast(composite);
			this.parent = null;
			this.controller = controller;
			GridLayoutFactory.fillDefaults().applyTo(shell);			
		} else {
			this.shell = composite.getShell();
			this.parent = composite;
			this.controller = controller;	
		}
	}

	public void createComposite(boolean closable) throws DeviceException {
		Composite intParent = getParent() == null ? getShell() : getParent();
		UUID uuid = UUID.randomUUID();
		intParent.setData(CompositeFactory.COMPOSITE_ROOT, uuid);
		intParent.setBackground(intParent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		CompositeFactory cf = new LiveViewCompositeFactory<>(getController());
		cf.createComposite(intParent, SWT.NONE);

		cf = createTabFactory();
		Composite tabs = cf.createComposite(intParent, SWT.NONE);
		CTabFolder cTab = CTabFolder.class.cast(tabs.getData(TabFolderBuilder.CTAB_FOLDER));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(cTab);
		createLoadSaveComposite(intParent, closable);
	}

	public T getController() {
		return controller;
	}

	public IConnection getLiveStreamConnection() {
		return liveStreamConnection;
	}

	public void setLiveStreamConnection(IConnection liveStreamConnection) {
		this.liveStreamConnection = liveStreamConnection;
	}

	protected abstract CompositeFactory createTabFactory() throws DeviceException;

	private Composite createLoadSaveComposite(Composite parent, boolean closable) {
		Composite panel = ClientSWTElements.createComposite(parent, SWT.NONE, 3);

		Composite loadSavePanel = ClientSWTElements.createComposite(panel, SWT.NONE);
		RowLayoutFactory.swtDefaults().applyTo(loadSavePanel);

		Button loadButton = ClientSWTElements.createButton(loadSavePanel, SWT.PUSH, ClientMessages.LOAD,
				ClientMessages.LOAD);
		loadButton.addListener(SWT.Selection, e -> load());
		RowDataFactory.swtDefaults().hint(BUTTON_WIDTH, -1).applyTo(loadButton);

		Button saveButton = ClientSWTElements.createButton(loadSavePanel, SWT.PUSH, ClientMessages.SAVE,
				ClientMessages.SAVE);
		saveButton.addListener(SWT.Selection, e -> save());
		RowDataFactory.swtDefaults().hint(BUTTON_WIDTH, -1).applyTo(saveButton);

		// Spacing label between panels
		ClientSWTElements.createLabel(panel, SWT.NONE);

		Composite okCancelPanel = ClientSWTElements.createComposite(panel, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(okCancelPanel);
		RowLayoutFactory.swtDefaults().applyTo(okCancelPanel);
		Button closeButton = ClientSWTElements.createButton(okCancelPanel, SWT.PUSH, ClientMessages.CLOSE,
				ClientMessages.CLOSE);
		closeButton.addListener(SWT.Selection, e -> shell.close());
		RowDataFactory.swtDefaults().hint(BUTTON_WIDTH, -1).applyTo(closeButton);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).applyTo(panel);
		if (!closable) {
			closeButton.setVisible(false);
		}
		return panel;
	}

	protected Shell getShell() {
		return this.shell;
	}

	protected Composite getParent() {
		return parent;
	}

	private void load() {
		logger.info("I would have produced an open dialog");
	}

	private void save() {
		logger.info("I would have produced a save dialog");
	}
}
