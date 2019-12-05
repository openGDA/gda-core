package uk.ac.diamond.daq.client.gui.camera;

import java.util.UUID;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Sash;
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

/**
 * Composes the overall layout for the Camera Configuration 
 * 
 * @author Eliot Hall
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public abstract class AbstractCameraConfigurationDialog<T extends AbstractCameraConfigurationController> {

	private static final int BUTTON_WIDTH = 80;

	private final Shell shell;
	private final Composite parent;
	protected final T controller;
	private IConnection liveStreamConnection;
	protected CameraImageComposite cameraImageComposite;

	private Composite internalArea;
	private Sash sash;
	private Composite topArea;
	private Composite bottomArea;

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

//	public void createComposite(boolean closable) throws DeviceException {
//		Composite intParent = getParent() == null ? getShell() : getParent();
//		UUID uuid = UUID.randomUUID();
//		intParent.setData(CompositeFactory.COMPOSITE_ROOT, uuid);
//		intParent.setBackground(intParent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
//
//		CompositeFactory cf = new LiveViewCompositeFactory<>(getController());
//		cf.createComposite(intParent, SWT.NONE);
//
//		cf = createTabFactory();
//		Composite tabs = cf.createComposite(intParent, SWT.NONE);
//		CTabFolder cTab = CTabFolder.class.cast(tabs.getData(TabFolderBuilder.CTAB_FOLDER));
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(cTab);
//		createLoadSaveComposite(intParent, closable);
//	}

	public Composite createComposite(boolean closable) throws DeviceException {
		Composite intParent = getParent() == null ? getShell() : getParent();
		UUID uuid = UUID.randomUUID();
		intParent.setData(CompositeFactory.COMPOSITE_ROOT, uuid);
		intParent.setBackground(intParent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		internalArea = ClientSWTElements.createComposite(intParent, SWT.NONE, 1);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(internalArea);
		sash = new Sash(internalArea, SWT.HORIZONTAL);
		
		CompositeFactory cf = new LiveViewCompositeFactory();
		topArea = cf.createComposite(internalArea, SWT.NONE);
		
		cf = createTabFactory();
		bottomArea = cf.createComposite(internalArea, SWT.NONE);
		CTabFolder cTab = CTabFolder.class.cast(bottomArea.getData(TabFolderBuilder.CTAB_FOLDER));

		final FormLayout form = new FormLayout();
		internalArea.setLayout(form);

		FormData topAreaData = new FormData();
		topAreaData.left = new FormAttachment(0, 0);
		topAreaData.right = new FormAttachment(100, 0);
		topAreaData.top = new FormAttachment(0, 0);
		topAreaData.bottom = new FormAttachment(sash, 0);
		topArea.setLayoutData(topAreaData);

		final int maxPercent = 80;
		final FormData sashData = new FormData();
		sashData.left = new FormAttachment(0, 0);
		sashData.right = new FormAttachment(100, 0);
		sashData.top = new FormAttachment(maxPercent, 0);
		sash.setLayoutData(sashData);
		
		FormData bottomAreaData = new FormData();
		bottomAreaData.top = new FormAttachment(sash, 0);
		bottomAreaData.bottom = new FormAttachment(100, 0);
		bottomAreaData.left = new FormAttachment(0, 0);
		bottomAreaData.right = new FormAttachment(100, 0);
		bottomArea.setLayoutData(bottomAreaData);

		sash.addListener(SWT.Selection, e -> {
			Rectangle sashRect = sash.getBounds();
			Rectangle shellRect = internalArea.getClientArea();
			int newPercent = (int)(((double)sashRect.y)/((double)shellRect.height)*100);
			if (e.y != sashRect.y) {
				sashData.top = new FormAttachment(Math.min(maxPercent, newPercent), 0);
				internalArea.layout(true, true);
				bottomArea.layout(true, true);				
			}
		});

		GridDataFactory.fillDefaults().grab(true, false).applyTo(cTab);
		return internalArea;
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
