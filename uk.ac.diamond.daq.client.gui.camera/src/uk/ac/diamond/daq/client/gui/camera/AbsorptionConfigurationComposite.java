package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AbsorptionConfigurationComposite extends Composite {

	public AbsorptionConfigurationComposite(Composite parent, int style) {
		super(parent, style);

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		StaticViewComposite staticViewComposite = new StaticViewComposite(this, SWT.NONE);
		staticViewComposite.setBackground(new Color(this.getDisplay(), 255, 0, 0));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(staticViewComposite);

		Composite staticViewROIStatisticsComposite = getStaticViewROIStatisticsComposite(this);
		staticViewROIStatisticsComposite.setBackground(new Color(this.getDisplay(), 0, 255, 0));
		GridDataFactory.fillDefaults().applyTo(staticViewROIStatisticsComposite);

		BinningComposite binningComposite = new BinningComposite(this, SWT.NONE);
		binningComposite.setBackground(new Color(this.getDisplay(), 255, 0, 0));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).applyTo(binningComposite);

		Label spacer1 = new Label(this, SWT.NONE);
		spacer1.setText("Spacer 1");
		spacer1.setBackground(new Color(this.getDisplay(), 0, 0, 255));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).grab(true, true).applyTo(spacer1);

		SensorROIComposite sensorROIComposite = new SensorROIComposite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).applyTo(sensorROIComposite);

		Label spacer2 = new Label(this, SWT.NONE);
		spacer2.setText("Spacer 2");
		spacer2.setBackground(new Color(this.getDisplay(), 255, 0, 255));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.END).grab(true, true).applyTo(spacer2);
	}

	private Composite getStaticViewROIStatisticsComposite(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(panel);

		Label label = new Label(panel, SWT.NONE);
		label.setText("Static View\nROI Statistics");
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(label);

		return panel;
	}
}
