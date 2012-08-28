package uk.ac.diamond.tomography.reconstruction.views;

import gda.util.OSCommandRunner;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class ParameterView extends ViewPart {
	private static final Logger logger = LoggerFactory
			.getLogger(ParameterView.class);

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "uk.ac.diamond.tomography.reconstruction.views.ParameterView";
	private Composite composite;

	private IEditorPart editorPart;

	private String pathname = "/scratch/test.xml";


	/**
	 * The constructor.
	 */
	public ParameterView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new org.eclipse.swt.layout.GridLayout());
		Button button = new Button(composite, SWT.PUSH);
		GridDataFactory.fillDefaults().applyTo(button);
		button.setText("Go");
		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				editorPart.doSave(null);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					//do nothing
				}
				OSCommandRunner runner = new OSCommandRunner("cat "+pathname, true, null,
						null);
				runner.logOutput();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Read settings file from resource and copy to /tmp

		try {
			IFileStore fileStore = EFS
					.getLocalFileSystem()
					.getStore(
							new File(
									"/home2/gda_trunk/workspace_git/gda-tomography.git/uk.ac.diamond.tomography.reconstruction/resources/settings.xml")
									.toURI());
			IFileStore fileStore1 = EFS
					.getLocalFileSystem()
					.getStore(
							new File(
									pathname)
									.toURI());
			fileStore.copy(fileStore1, EFS.OVERWRITE, null);
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			editorPart = IDE.openEditorOnFileStore(page, fileStore1);
		} catch (Exception e1) {
			logger.error("Unable to open editor for settings file");
		}

	}

	private void showMessage(String message) {
		MessageDialog.openInformation(composite.getShell(), "Parameters",
				message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		composite.setFocus();
	}
}