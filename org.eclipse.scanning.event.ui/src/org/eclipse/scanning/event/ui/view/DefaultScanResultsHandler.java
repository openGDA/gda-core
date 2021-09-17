package org.eclipse.scanning.event.ui.view;

import java.io.File;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.ui.IResultHandler;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * Result handler for opening {@link ScanBean}s by opening their results files, as
 * returned by {@link ScanBean#getFilePath()}.
 *
 * @author Matthew Dickie
 */
public class DefaultScanResultsHandler implements IResultHandler<ScanBean> {

	private static final String DATA_PERSPECTIVE_ID = "org.dawnsci.datavis.DataVisPerspective";
	private static final String FILE_OPEN_TOPIC = "org/dawnsci/events/file/OPEN";
	private static final String PATH_KEY = "path";

	@Override
	public boolean isHandled(StatusBean bean) {
		return bean.getRunDirectory() == null &&
				(bean instanceof ScanBean && ((ScanBean) bean).getFilePath() != null);
	}

	@Override
	public boolean open(ScanBean scanBean) throws Exception {
		if (scanBean.getFilePath() == null) {
			return false;
		}

		if (!scanBean.getStatus().isFinal() && !confirmOpen(scanBean)) {
			return false;
		}

		String filePath = scanBean.getFilePath();
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

		// Set the perspective to Data Visualisation Perspective and load the file
		workbench.showPerspective(DATA_PERSPECTIVE_ID, window);

		BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		EventAdmin admin = bundleContext.getService(bundleContext.getServiceReference(EventAdmin.class));
		admin.sendEvent(new Event(FILE_OPEN_TOPIC, Map.of(PATH_KEY, filePath)));
		return true;
	}

	public boolean confirmOpen(ScanBean bean) {
		final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//		return MessageDialog.openQuestion(shell, "'"+bean.getName()+"' incomplete.",
//					"The run of '"+bean.getName()+"' has not completed.\n" +
//					"Would you like to try to open the results anyway?");

		// TODO: we currently do not open scan results for scans that have not finished as they cannot
		// In future, we may wish to add a feature to support this in future. Talk to Jacob Filik
		MessageDialog.openError(shell, "'"+bean.getName()+"' incomplete.",
		"Cannot open scan results.\nThe run of '"+bean.getName()+"' has not completed.");
		return false;
	}

	private String getEditorId(String filePath) {
		IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
		IEditorDescriptor desc = editorRegistry.getDefaultEditor(filePath);
		if (desc == null) {
			desc = editorRegistry.getDefaultEditor(filePath + ".txt");
		}
		return desc.getId();
	}

	private IEditorInput getEditorInput(String filePath) {
		final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(filePath));
		return new FileStoreEditorInput(externalFile);
	}

}
