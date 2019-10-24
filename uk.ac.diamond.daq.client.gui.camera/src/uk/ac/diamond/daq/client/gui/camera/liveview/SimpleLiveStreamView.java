package uk.ac.diamond.daq.client.gui.camera.liveview;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.gda.client.live.stream.IConnectionFactory;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Displays the CameraConfiguraiton as view
 * 
 * @author Maurizio Nagni
 *
 */
public class SimpleLiveStreamView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		try {
			int cameraIndex = 0;
			IActionBars actionBars = getViewSite().getActionBars();
			LivePlottingComposite plottingComposite = new LivePlottingComposite(parent, SWT.NONE, getPartName(),
					getLiveStreamConnection(cameraIndex), actionBars, this);
			plottingComposite.setShowAxes(getCameraConfiguration(cameraIndex).getCalibratedAxesProvider() != null);
			plottingComposite.setShowTitle(true);
			plottingComposite.connect();
			GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingComposite);
		} catch (Exception e) {
			return;
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private CameraConfiguration getCameraConfiguration(int cameraIndex) {
		return CameraHelper.getCameraConfiguration(cameraIndex);
	}
	
	private LiveStreamConnection getLiveStreamConnection(int cameraIndex) {
		return IConnectionFactory.getLiveStreamConnection(getCameraConfiguration(cameraIndex), StreamType.EPICS_ARRAY);
	}
	
	private LiveStreamConnection getLiveStreamConnection() {
		return IConnectionFactory.getLiveStreamConnection(getCameraConfiguration(0), StreamType.EPICS_ARRAY);
	}
}
