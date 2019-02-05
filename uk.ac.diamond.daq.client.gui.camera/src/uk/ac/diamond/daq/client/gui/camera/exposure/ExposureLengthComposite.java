package uk.ac.diamond.daq.client.gui.camera.exposure;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

public class ExposureLengthComposite extends Composite {

	public ExposureLengthComposite(Composite parent, int style) {
		super(parent, style);

		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(this);

		Label label = new Label(this, SWT.LEFT);
		label.setText("Exposure:");
		GridDataFactory.swtDefaults().applyTo(label);

		Slider exposureSlider = new Slider(this, SWT.HORIZONTAL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(exposureSlider);

		Text exposureText = new Text(this, SWT.RIGHT | SWT.BORDER);
		exposureText.setText("0");
		GridDataFactory.swtDefaults().hint(25, -1).applyTo(exposureText);

		label = new Label(this, SWT.LEFT);
		label.setText("ms");
		GridDataFactory.swtDefaults().applyTo(label);
	}

}
