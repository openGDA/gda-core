package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AbsorptionConfigurationComposite extends Composite {
	private static final int BOX_SIZE = 15;
	private static final int LABEL_WIDTH = 50;

	public AbsorptionConfigurationComposite(Composite parent, int style) {
		super(parent, style);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		Composite staticViewROIStatisticsComposite = getStaticViewROIStatisticsComposite(this);
		GridDataFactory.fillDefaults().applyTo(staticViewROIStatisticsComposite);

		Label spacer1 = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).grab(true, true).applyTo(spacer1);
	}

	private Composite getStaticViewROIStatisticsComposite(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);

		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(panel);

		RegionBox box1 = new RegionBox(panel, new Color(parent.getDisplay(), 255, 128, 128),
				new Color(parent.getDisplay(), 255, 0, 0), 2);
		GridDataFactory.swtDefaults().hint(BOX_SIZE, BOX_SIZE).align(SWT.CENTER, SWT.CENTER).applyTo(box1);

		Label regionValue1 = new Label(panel, SWT.RIGHT);
		regionValue1.setText("65535");
		GridDataFactory.swtDefaults().hint(LABEL_WIDTH, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER)
				.applyTo(regionValue1);

		Label ratioLabel = new Label(panel, SWT.None);
		ratioLabel.setText("Radio: 0.5");
		GridDataFactory.swtDefaults().span(0, 2).align(SWT.CENTER, SWT.CENTER).applyTo(ratioLabel);

		RegionBox box2 = new RegionBox(panel, new Color(parent.getDisplay(), 128, 255, 128),
				new Color(parent.getDisplay(), 0, 255, 0), 2);
		GridDataFactory.swtDefaults().hint(BOX_SIZE, BOX_SIZE).align(SWT.CENTER, SWT.CENTER).applyTo(box2);

		Label regionValue2 = new Label(panel, SWT.RIGHT);
		regionValue2.setText("32768");
		GridDataFactory.swtDefaults().hint(LABEL_WIDTH, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER)
				.applyTo(regionValue2);

		return panel;
	}
}
