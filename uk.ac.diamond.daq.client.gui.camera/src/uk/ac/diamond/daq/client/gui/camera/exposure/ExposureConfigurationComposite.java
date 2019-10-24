package uk.ac.diamond.daq.client.gui.camera.exposure;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.diamond.daq.client.gui.camera.PositionValueControlComposite;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.stage.MultipleStagePositioningService;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;

public class ExposureConfigurationComposite extends Composite {
	public ExposureConfigurationComposite(Composite parent, AbstractCameraConfigurationController controller, int style)
			throws DeviceException {
		super(parent, style);

		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(this);
		GridDataFactory gdf = GridDataFactory.fillDefaults();

		Composite exposureLengthComposite = new ExposureDurationComposite(controller).createComposite(this, SWT.NONE);
		gdf.applyTo(exposureLengthComposite);

		Composite binningComposite = new BinningComposite(this, controller, SWT.NONE);
		gdf.applyTo(binningComposite);

		Composite sensorROIPanel = new SensorROIComposite(this, controller, SWT.NONE);
		gdf.applyTo(sensorROIPanel);

		Composite adjustPanel = createAdjustPanel();
		gdf.applyTo(adjustPanel);
	}

	private Composite createAdjustPanel() {
		Group panel = ClientSWTElements.createGroup(this, 1, ClientMessages.ADJUST);
		RowLayoutFactory.swtDefaults().type(SWT.VERTICAL).fill(true).applyTo(panel);

		Button sourceButton = ClientSWTElements.createButton(panel, SWT.PUSH, ClientMessages.SOURCE,
				ClientMessages.EMPTY_MESSAGE);
		RowDataFactory.swtDefaults().applyTo(sourceButton);

		Button cameraPositionButton = ClientSWTElements.createButton(panel, SWT.PUSH, ClientMessages.CAMERA_POSITION,
				ClientMessages.EMPTY_MESSAGE);
		cameraPositionButton.addListener(SWT.Selection, e -> {
			Shell positionDialog = new Shell(getDisplay());
			positionDialog.setText(ClientMessagesUtility.getMessage(ClientMessages.CAMERA_POSITION));
			positionDialog.setSize(250, 130);
			GridLayoutFactory.swtDefaults().numColumns(1).applyTo(positionDialog);

			MultipleStagePositioningService multipleStagePositioningService = Finder.getInstance()
					.find("cameraPositionMultipleStagePositioningService");
			PositionValueControlComposite dialComposite = new PositionValueControlComposite(positionDialog,
					multipleStagePositioningService, SWT.NONE);

			GridDataFactory.fillDefaults().grab(true, true).applyTo(dialComposite);

			positionDialog.open();
		});
		RowDataFactory.swtDefaults().applyTo(cameraPositionButton);

		return panel;
	}
}
