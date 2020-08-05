package uk.ac.diamond.daq.client.gui.camera;

import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.UIHelper;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Displays the CameraConfiguration as view.
 * 
 * @author Maurizio Nagni
 *
 */
public class CameraConfigurationView extends ViewPart {

	public static final String CAMERA_CONTROLLER_VIEW = "uk.ac.diamond.daq.client.gui.camera.CameraConfigurationView";

	private static final Logger logger = LoggerFactory.getLogger(CameraConfigurationView.class);

	@Override
	public void createPartControl(Composite parent) {
		CameraConfigurationFactory ccd = new CameraConfigurationFactory();
		ccd.createComposite(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
		// Not necessary
	}

	/**
	 * Creates a {@link Button} to open a CameraConfigurationView. This elements
	 * standardises the look and feel how the view is open. The listener attached to
	 * the button opens the view using the Eclipse generic viewId
	 * 
	 * <pre>
	 * {@link #CAMERA_CONTROLLER_VIEW}:secondaryId
	 * </pre>
	 * 
	 * where the secondaryId is generated internally as random string. This makes
	 * the button suitable to be used to open multiple instances of the
	 * CameraConfigurationView
	 * 
	 * @param parent the button container
	 * @return a {@link Button} instance
	 */
	public static final Button openCameraConfigurationViewButton(Composite parent) {
		Button cameras = ClientSWTElements.createClientButton(parent, SWT.None, ClientMessages.CAMERAS,
				ClientMessages.CAMERA_TP, ClientImages.CAMERA);
		ClientSWTElements.createClientGridDataFactory().indent(5, SWT.DEFAULT).applyTo(cameras);
		cameras.addListener(SWT.Selection, event -> {
			IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				activePage.showView(CameraConfigurationView.CAMERA_CONTROLLER_VIEW, UUID.randomUUID().toString(),
						IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				String errMsg = "Cannot open Camera Controller View";
				UIHelper.showError(errMsg, e);
				logger.error(errMsg, e);
			}
		});
		return cameras;
	}
}
