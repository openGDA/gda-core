package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SensorROIComposite extends Composite {

	public SensorROIComposite(Composite parent, int style) {
		super(parent, style);

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		Group panel = new Group(this, SWT.SHADOW_NONE);
		panel.setText("Sensor ROI");

		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(panel);

		Label label;

		label = new Label(panel, SWT.LEFT);
		label.setText("Top");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		label = new Label(panel, SWT.LEFT);
		label.setText("Height");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		Text topText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(topText);

		label = new Label(panel, SWT.LEFT);
		label.setText("mm");
		GridDataFactory.swtDefaults().applyTo(label);

		Text heightText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(heightText);

		label = new Label(panel, SWT.LEFT);
		label.setText("mm");
		GridDataFactory.swtDefaults().applyTo(label);

		label = new Label(panel, SWT.LEFT);
		label.setText("Left");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		label = new Label(panel, SWT.LEFT);
		label.setText("Width");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		Text leftText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(leftText);

		label = new Label(panel, SWT.LEFT);
		label.setText("mm");
		GridDataFactory.swtDefaults().applyTo(label);

		Text widthText = new Text(panel, SWT.RIGHT);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(widthText);

		label = new Label(panel, SWT.LEFT);
		label.setText("mm");
		GridDataFactory.swtDefaults().applyTo(label);

		GridDataFactory.fillDefaults().applyTo(panel);
	}

}
