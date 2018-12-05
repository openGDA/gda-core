package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

public class ExposureConfigurationComposite extends Composite {
	public ExposureConfigurationComposite(Composite parent, CameraConfiguration cameraConfiguration, int style) {
		super(parent, style);

		LiveStreamConnection liveStreamConnection = new LiveStreamConnection(cameraConfiguration,
				StreamType.EPICS_ARRAY);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		try {
			liveStreamConnection.connect();
			LiveViewCameraComposite imagingPanel = new LiveViewCameraComposite(this, liveStreamConnection,
					cameraConfiguration, SWT.None);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(imagingPanel);

			HistogramComposite histogramPanel = new HistogramComposite(this, imagingPanel.getPlottingComposite(),
					SWT.None);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(histogramPanel);
		} catch (Exception e) {
			Label label;

			label = new Label(this, SWT.NONE);
			label.setText("No Camera found");
			GridDataFactory.fillDefaults().grab(true, true).applyTo(label);

			label = new Label(this, SWT.NONE);
			label.setText("No Camera found");
			GridDataFactory.fillDefaults().grab(true, true).applyTo(label);
		}

		Composite sensorROIPanel = new SensorROIComposite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).grab(true, false).applyTo(sensorROIPanel);

		Composite adjustPanel = createAdjustPanel();
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(adjustPanel);
	}

	private Composite createAdjustPanel() {
		Group panel = new Group(this, SWT.SHADOW_NONE);
		panel.setText("Adjust");

		RowLayoutFactory.swtDefaults().type(SWT.VERTICAL).fill(true).applyTo(panel);

		Button sourceButton = new Button(panel, SWT.PUSH);
		sourceButton.setText("Source");
		RowDataFactory.swtDefaults().applyTo(sourceButton);

		Button cameraPositionButton = new Button(panel, SWT.PUSH);
		cameraPositionButton.setText("Camera Position");
		cameraPositionButton.addListener(SWT.Selection, e -> {
			Shell positionDialog = new Shell(getDisplay());
			positionDialog.setText("Camera Position");
			positionDialog.setSize(250, 130);
			GridLayoutFactory.swtDefaults().numColumns(1).applyTo(positionDialog);
			CameraPositionComposite dialogPanel = new CameraPositionComposite(positionDialog, SWT.None);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(dialogPanel);

			positionDialog.open();
		});
		RowDataFactory.swtDefaults().applyTo(cameraPositionButton);

		return panel;
	}
}
