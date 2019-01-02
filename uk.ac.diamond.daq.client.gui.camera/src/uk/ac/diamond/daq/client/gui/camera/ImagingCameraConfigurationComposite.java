package uk.ac.diamond.daq.client.gui.camera;

import gda.device.detector.NexusDetector;
import gda.device.detector.addetector.ADDetector;
import gda.factory.Finder;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

public class ImagingCameraConfigurationComposite extends Composite {

	public ImagingCameraConfigurationComposite(Composite parent, CameraConfiguration cameraConfiguration, int style) {
		super(parent, style);

		GridLayoutFactory.swtDefaults().applyTo(this);

		Composite liveViewComposite = createLiveViewComposite(this, cameraConfiguration);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(liveViewComposite);

		TabFolder tabFolder = new TabFolder(this, SWT.TOP);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tabFolder);

		TabItem exposureTab = new TabItem(tabFolder, SWT.NONE);
		exposureTab.setText("Exposure");

		ExposureConfigurationComposite exposureConfigurationComposite = new ExposureConfigurationComposite(tabFolder,
				style);
		exposureTab.setControl(exposureConfigurationComposite);

		TabItem absorptionTab = new TabItem(tabFolder, SWT.NONE);
		absorptionTab.setText("Absorption");

		AbsorptionConfigurationComposite absorptionConfigurationComposite = new AbsorptionConfigurationComposite(
				tabFolder, SWT.NONE);
		absorptionTab.setControl(absorptionConfigurationComposite);

		DialogLoadSaveComposite dialogLoadSaveComposite = new DialogLoadSaveComposite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).applyTo(dialogLoadSaveComposite);
	}

	private Composite createLiveViewComposite(Composite parent, CameraConfiguration cameraConfiguration) {
		LiveStreamConnection liveStreamConnection = new LiveStreamConnection(cameraConfiguration,
				StreamType.EPICS_ARRAY);

		Composite panel = new Composite(parent, SWT.NONE);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(panel);
		try {
			liveStreamConnection.connect();
			LiveViewCameraComposite imagingPanel = new LiveViewCameraComposite(panel, liveStreamConnection,
					cameraConfiguration, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(imagingPanel);

			HistogramComposite histogramPanel = new HistogramComposite(panel, imagingPanel.getPlottingComposite(),
					SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(histogramPanel);

			NexusDetector adBase = Finder.getInstance().find("xreye_addetector");
			Text text = new Text(panel, SWT.NONE);
			text.addModifyListener(e -> {
				text.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
				try {
					double value = Double.parseDouble(text.getText());
					((ADDetector) adBase).getAdBase().setAcquireTime(value);
				} catch (Exception ex) {
					text.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
				}
			});
			GridDataFactory.swtDefaults().span(2, 1).grab(true, false).applyTo(text);

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

}
