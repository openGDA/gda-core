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
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

public class ImagingCameraComposite extends Composite implements DiadConfigurationListener<DiadConfigurationModel> {
	public ImagingCameraComposite(Composite parent, CameraConfiguration cameraConfiguration, int style)
			throws Exception {
		super(parent, style);

		LiveStreamConnection liveStreamConnection = new LiveStreamConnection(cameraConfiguration,
				StreamType.EPICS_ARRAY);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		LiveViewComposite imagingPanel = new LiveViewComposite(this, liveStreamConnection, cameraConfiguration,
				SWT.None);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(imagingPanel);

		HistogramComposite histogramPanel = new HistogramComposite(this, imagingPanel.getPlottingComposite(), SWT.None);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(histogramPanel);

		Composite sensorROIPanel = createSensorROIPanel();
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).grab(true, false).applyTo(sensorROIPanel);

		Composite adjustPanel = createAdjustPanel();
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(adjustPanel);

		Composite buttonPanel = new DiadConfigurationComposite<>(this, this, SWT.None);
		GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.END).applyTo(buttonPanel);
	}

	private Composite createSensorROIPanel() {
		Group panel = new Group(this, SWT.SHADOW_NONE);
		panel.setText("Sensor ROI");

		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(panel);

		Label label;

		label = new Label(panel, SWT.LEFT);
		label.setText("Top");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		label = new Label(panel, SWT.LEFT);
		label.setText("Height");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		Text topText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(topText);

		label = new Label(panel, SWT.LEFT);
		label.setText("mm");
		GridDataFactory.swtDefaults().applyTo(label);

		Text heightText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(heightText);

		label = new Label(panel, SWT.LEFT);
		label.setText("mm");
		GridDataFactory.swtDefaults().applyTo(label);

		label = new Label(panel, SWT.LEFT);
		label.setText("Left");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		label = new Label(panel, SWT.LEFT);
		label.setText("Width");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		Text leftText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(leftText);

		label = new Label(panel, SWT.LEFT);
		label.setText("mm");
		GridDataFactory.swtDefaults().applyTo(label);

		Text widthText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(widthText);

		label = new Label(panel, SWT.LEFT);
		label.setText("mm");
		GridDataFactory.swtDefaults().applyTo(label);

		return panel;
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

	@Override
	public DiadConfigurationModel getModel() {
		return new DiadConfigurationModel("Camera", "0.1") {
		};
	}

	@Override
	public void setModel(DiadConfigurationModel data) {
		// TODO Auto-generated method stub

	}
}
