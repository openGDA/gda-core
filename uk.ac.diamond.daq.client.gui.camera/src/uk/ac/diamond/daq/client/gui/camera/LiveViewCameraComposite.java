package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;

public class LiveViewCameraComposite extends Composite {
	// private static final Logger log = LoggerFactory.getLogger
	// (LiveViewComposite.class);

	private LivePlottingComposite plottingComposite;

	public LiveViewCameraComposite(Composite parent, LiveStreamConnection liveStreamConnection,
			CameraConfiguration cameraConfiguration, int style) throws Exception {
		super(parent, style);

		Label label;

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		try {
			plottingComposite = new LivePlottingComposite(this, SWT.NONE, "Live View", liveStreamConnection);
			plottingComposite.setShowAxes(cameraConfiguration.getCalibratedAxesProvider() != null);
			plottingComposite.setShowTitle(true);
			if (!liveStreamConnection.isConnected()) {
				plottingComposite.connect();
			}

			GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingComposite);
		} catch (Exception e) {
			throw new Exception("Cannnot connect to camera", e);
		}

		Composite roiPanel = new Composite(this, SWT.None);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(roiPanel);

		GridLayoutFactory.fillDefaults().numColumns(6).applyTo(roiPanel);

		Button roiButton = new Button(roiPanel, SWT.TOGGLE);
		roiButton.setText("ROI");
		GridDataFactory.swtDefaults().span(1, 2).applyTo(roiButton);

		// expanding label
		label = new Label(roiPanel, SWT.LEFT);
		GridDataFactory.swtDefaults().span(1, 2).align(SWT.FILL, SWT.CENTER).applyTo(label);

		label = new Label(roiPanel, SWT.LEFT);
		label.setText("x:");
		GridDataFactory.swtDefaults().applyTo(label);

		Label xLabel = new Label(roiPanel, SWT.LEFT);
		xLabel.setText("---.--mm");
		GridDataFactory.swtDefaults().applyTo(xLabel);

		label = new Label(roiPanel, SWT.LEFT);
		label.setText("width:");
		GridDataFactory.swtDefaults().applyTo(label);

		Label widthLabel = new Label(roiPanel, SWT.LEFT);
		widthLabel.setText("---.--mm");
		GridDataFactory.swtDefaults().applyTo(widthLabel);

		label = new Label(roiPanel, SWT.LEFT);
		label.setText("y:");
		GridDataFactory.swtDefaults().applyTo(label);

		Label yLabel = new Label(roiPanel, SWT.LEFT);
		yLabel.setText("---.--mm");
		GridDataFactory.swtDefaults().applyTo(yLabel);

		label = new Label(roiPanel, SWT.LEFT);
		label.setText("height:");
		GridDataFactory.swtDefaults().applyTo(label);

		Label heightLabel = new Label(roiPanel, SWT.LEFT);
		heightLabel.setText("---.--mm");
		GridDataFactory.swtDefaults().applyTo(heightLabel);

		Composite exposurePanel = new Composite(this, SWT.None);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(exposurePanel);

		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(exposurePanel);

		label = new Label(exposurePanel, SWT.LEFT);
		label.setText("Exposure:");
		GridDataFactory.swtDefaults().applyTo(label);

		Slider exposureSlider = new Slider(exposurePanel, SWT.HORIZONTAL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(exposureSlider);

		Text exposureText = new Text(exposurePanel, SWT.RIGHT | SWT.BORDER);
		exposureText.setText("0");
		GridDataFactory.swtDefaults().hint(25, -1).applyTo(exposureText);

		label = new Label(exposurePanel, SWT.LEFT);
		label.setText("ms");
		GridDataFactory.swtDefaults().applyTo(label);
	}

	public LivePlottingComposite getPlottingComposite() {
		return plottingComposite;
	}
}
