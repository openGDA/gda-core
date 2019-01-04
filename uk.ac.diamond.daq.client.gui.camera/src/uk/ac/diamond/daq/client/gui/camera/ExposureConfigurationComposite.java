package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class ExposureConfigurationComposite extends Composite {
	public ExposureConfigurationComposite(Composite parent, int style) {
		super(parent, style);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

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
			CameraPositionComposite dialogPanel = new CameraPositionComposite(positionDialog, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(dialogPanel);

			positionDialog.open();
		});
		RowDataFactory.swtDefaults().applyTo(cameraPositionButton);

		return panel;
	}
}
