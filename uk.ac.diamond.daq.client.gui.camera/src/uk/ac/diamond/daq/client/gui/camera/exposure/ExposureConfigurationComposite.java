package uk.ac.diamond.daq.client.gui.camera.exposure;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.diamond.daq.client.gui.camera.PositionValueControlComposite;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.stage.MultipleStagePositioningService;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * GUI element to change a camera exposure
 * 
 * @author Eliot Hall
 * @author Maurizio Nagni
 *
 */
public class ExposureConfigurationComposite extends Composite {
	public ExposureConfigurationComposite(Composite parent, AbstractCameraConfigurationController controller, int style)
			throws DeviceException {
		super(parent, style);

		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(false).applyTo(this);
		GridDataFactory gdf = GridDataFactory.fillDefaults();

		createAdjustPanel(gdf);

		Composite exposureLengthComposite = new ExposureDurationComposite(controller).createComposite(this, SWT.NONE);
		gdf.applyTo(exposureLengthComposite);

		Composite binningCompositeArea = ClientSWTElements.createComposite(this, style);
		new BinningComposite(binningCompositeArea, controller, SWT.NONE);
		gdf.applyTo(binningCompositeArea);
		this.layout(true, true);
	}

	private void createAdjustPanel(GridDataFactory gdf) {
		Button cameraPositionButton = ClientSWTElements.createButton(this, SWT.PUSH, ClientMessages.CAMERA_POSITION,
				ClientMessages.EMPTY_MESSAGE);
		cameraPositionButton.addListener(SWT.Selection, cameraPositionListener);
		gdf.applyTo(cameraPositionButton);
	}

	private Listener cameraPositionListener = e -> {
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
	};
}
