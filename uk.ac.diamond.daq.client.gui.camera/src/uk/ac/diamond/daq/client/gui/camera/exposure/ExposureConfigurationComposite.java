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

import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.diamond.daq.client.gui.camera.PositionValueControlComposite;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.stage.MultipleStagePositioningService;

public class ExposureConfigurationComposite extends Composite {
	public ExposureConfigurationComposite(Composite parent, AbstractCameraConfigurationController controller, int style) throws DeviceException {
		super(parent, style);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		ExposureLengthComposite exposureLengthComposite = new ExposureLengthComposite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(exposureLengthComposite);
		
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
			
			MultipleStagePositioningService multipleStagePositioningService = 
					Finder.getInstance().find("cameraPositionMultipleStagePositioningService");
			PositionValueControlComposite dialComposite = new PositionValueControlComposite(positionDialog, 
					multipleStagePositioningService, SWT.NONE);
			
			GridDataFactory.fillDefaults().grab(true, true).applyTo(dialComposite);

			positionDialog.open();
		});
		RowDataFactory.swtDefaults().applyTo(cameraPositionButton);

		return panel;
	}
}
