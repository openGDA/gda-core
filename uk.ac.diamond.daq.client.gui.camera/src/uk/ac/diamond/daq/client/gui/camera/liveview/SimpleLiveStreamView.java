package uk.ac.diamond.daq.client.gui.camera.liveview;

import static uk.ac.gda.client.live.stream.view.StreamViewUtility.displayAndLogError;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.CameraConfigurationDialog;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.gda.client.live.stream.IConnectionFactory;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Displays the CameraConfiguraiton as view
 * 
 * @author Maurizio Nagni
 *
 */
public class SimpleLiveStreamView extends ViewPart {

	public static final String CAMERA_NAME = "imaging.camera.name";

	@Override
	public void createPartControl(Composite parent) {
		try {
			IActionBars actionBars = getViewSite().getActionBars();
			LivePlottingComposite plottingComposite = new LivePlottingComposite(parent, SWT.NONE, getPartName(),
					getLiveStreamConnection(), actionBars, this);
			plottingComposite.setShowAxes(getCameraConfiguration().getCalibratedAxesProvider() != null);
			plottingComposite.setShowTitle(true);
			plottingComposite.connect();
			GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingComposite);
			// setupRoiProvider();
		} catch (Exception e) {
			return;
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private LiveStreamConnection getLiveStreamConnection() {
		return IConnectionFactory.getLiveStremConnection(getCameraConfiguration(), StreamType.EPICS_ARRAY);
	}

	private CameraConfiguration getCameraConfiguration() {
		return CameraHelper.getCameraConfiguration(CAMERA_NAME);
	}
}
