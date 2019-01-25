package uk.ac.diamond.daq.client.gui.camera.exposure;

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
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationController;

public class ExposureConfigurationComposite extends Composite {
	public ExposureConfigurationComposite(Composite parent, CameraConfigurationController controller, int style) throws DeviceException {
		super(parent, style);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		Composite exposurePanel = createExposureComposit();
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(exposurePanel);
		
		Composite binningComposite = new BinningComposite(this, controller, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(binningComposite);

		Composite sensorROIPanel = new SensorROIComposite(this, controller, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(sensorROIPanel);

		Composite adjustPanel = createAdjustPanel();
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(adjustPanel);
		
		Label spacer = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.FILL).applyTo(spacer);
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
	
	private Composite createExposureComposit () {
		Label label;
		
		Composite panel = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(panel);

		label = new Label(panel, SWT.LEFT);
		label.setText("Exposure:");
		GridDataFactory.swtDefaults().applyTo(label);

		Slider exposureSlider = new Slider(panel, SWT.HORIZONTAL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(exposureSlider);

		Text exposureText = new Text(panel, SWT.RIGHT | SWT.BORDER);
		exposureText.setText("0");
		GridDataFactory.swtDefaults().hint(25, -1).applyTo(exposureText);

		label = new Label(panel, SWT.LEFT);
		label.setText("ms");
		GridDataFactory.swtDefaults().applyTo(label);
		
		return panel;
	}
}
