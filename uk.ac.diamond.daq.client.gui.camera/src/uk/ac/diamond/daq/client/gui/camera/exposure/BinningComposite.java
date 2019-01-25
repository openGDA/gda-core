package uk.ac.diamond.daq.client.gui.camera.exposure;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationAdapter;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationController;
import uk.ac.gda.api.camera.BinningFormat;

public class BinningComposite extends Composite {
	private static final Logger log = LoggerFactory.getLogger(BinningComposite.class);
	
	private class BinningListener extends CameraConfigurationAdapter {
		@Override
		public void setBinningFormat(BinningFormat binningFormat) {
			log.debug("Binning set as x:{} y:{}", binningFormat.getX(), binningFormat.getY());
			if (binningFormat.getX() != binningFormat.getY()) {
				for (BinningButton binningButton : binningButtons) {
					binningButton.button.setSelection(false);
				}
				return;
			}
			toggleRadioButtons(binningFormat.getX(), false);
		}
	}

	private class BinningButton {
		Button button;
		int pixels;
	}

	private BinningButton[] binningButtons;
	private CameraConfigurationController controller;

	public BinningComposite(Composite parent, CameraConfigurationController controller, int style) throws DeviceException {
		super(parent, style);

		this.controller = controller;
		BinningListener binningListener = new BinningListener();
		controller.addListener(binningListener);

		addListener(SWT.Dispose, e -> controller.removeListener(binningListener));

		GridLayoutFactory.fillDefaults().numColumns(7).applyTo(this);

		Label label = new Label(this, SWT.NONE);
		label.setText("Binning:");
		GridDataFactory.fillDefaults().applyTo(label);

		binningButtons = new BinningButton[3];
		binningButtons[0] = addButton(1);
		binningButtons[1] = addButton(2);
		binningButtons[2] = addButton(4);
		
		binningListener.setBinningFormat(controller.getBinning());
	}

	private BinningButton addButton(int pixels) {
		BinningButton binningButton = new BinningButton();
		binningButton.pixels = pixels;
		binningButton.button = new Button(this, SWT.RADIO);
		binningButton.button.addListener(SWT.Selection, e -> toggleRadioButtons(pixels, true));

		GridDataFactory.swtDefaults().applyTo(binningButton.button);

		Label label = new Label(this, SWT.NONE);
		label.setText("x" + pixels);
		GridDataFactory.swtDefaults().applyTo(label);

		return binningButton;
	}

	private void toggleRadioButtons(int pixels, boolean updateController) {
		for (BinningButton binningButton : binningButtons) {
			if (binningButton.pixels == pixels) {
				log.debug("Setting x{}: true", binningButton.pixels);
				binningButton.button.setSelection(true);
				if (updateController) {
					try {
						BinningFormat binningFormat = new BinningFormat(pixels, pixels);
						controller.setBinning(binningFormat);
					} catch (DeviceException e) {
						MessageDialog.openError(this.getShell(), "Camera Configuration",
								"Error setting camera to bin " + pixels + "x" + pixels + " pixels");
					}
				}
			} else {
				log.debug("Setting x{}: false", binningButton.pixels);
				binningButton.button.setSelection(false);
			}
		}
	}

}
