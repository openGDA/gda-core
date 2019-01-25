package uk.ac.diamond.daq.client.gui.camera.absorption;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationAdapter;
import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationController;

public class AbsorptionConfigurationComposite extends Composite {
	private static final int BOX_SIZE = 15;
	private static final int LABEL_WIDTH = 100;
	private static final int UPDATE_BUTTON_WIDTH = 150;
	
	private Label lowRegionLabel;
	private Label highRegionLabel;
	private Label ratioLabel;
	
	private class RatioListener extends CameraConfigurationAdapter {
		@Override
		public void setRatio(int highRegion, int lowRegion, double ratio) {
			highRegionLabel.setText(String.format("High: %5d", highRegion));
			lowRegionLabel.setText(String.format("Low: %5d", lowRegion));
			ratioLabel.setText(String.format("Ratio: %5.3f", ratio));
		}
	}

	public AbsorptionConfigurationComposite(Composite parent, CameraConfigurationController controller, int style) {
		super(parent, style);
		
		RatioListener ratioListener = new RatioListener();
		controller.addListener(ratioListener);
		
		addListener(SWT.Dispose, e -> controller.removeListener(ratioListener));

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);
		
		Button updateButton = new Button(this, SWT.PUSH);
		updateButton.setText("Refresh Image");
		GridDataFactory.swtDefaults().span(2, 1).hint(UPDATE_BUTTON_WIDTH, SWT.DEFAULT).align(SWT.CENTER, SWT.BEGINNING).applyTo(updateButton);

		Composite staticViewROIStatisticsComposite = getStaticViewROIStatisticsComposite(this);
		GridDataFactory.fillDefaults().applyTo(staticViewROIStatisticsComposite);

		Label spacer1 = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).grab(true, true).applyTo(spacer1);
		
		ratioListener.setRatio(65535, 65536, 0);
	}

	private Composite getStaticViewROIStatisticsComposite(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);

		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(panel);

		RegionBox box1 = new RegionBox(panel, new Color(parent.getDisplay(), 255, 128, 128),
				new Color(parent.getDisplay(), 255, 0, 0), 2);
		GridDataFactory.swtDefaults().hint(BOX_SIZE, BOX_SIZE).align(SWT.CENTER, SWT.CENTER).applyTo(box1);

		highRegionLabel = new Label(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().hint(LABEL_WIDTH, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER)
				.applyTo(highRegionLabel);

		ratioLabel = new Label(panel, SWT.None);
		GridDataFactory.swtDefaults().span(0, 2).align(SWT.CENTER, SWT.CENTER).applyTo(ratioLabel);

		RegionBox box2 = new RegionBox(panel, new Color(parent.getDisplay(), 128, 128, 255),
				new Color(parent.getDisplay(), 0, 0, 255), 2);
		GridDataFactory.swtDefaults().hint(BOX_SIZE, BOX_SIZE).align(SWT.CENTER, SWT.CENTER).applyTo(box2);

		lowRegionLabel = new Label(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().hint(LABEL_WIDTH, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER)
				.applyTo(lowRegionLabel);

		return panel;
	}
}
