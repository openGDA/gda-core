package uk.ac.diamond.daq.client.gui.camera.diffraction;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DiffractionAnalysisConfigurationComposite extends Composite {
	public DiffractionAnalysisConfigurationComposite (Composite parent, int style) {
		super(parent, style);
		
		GridLayoutFactory.swtDefaults().applyTo(this);
		
		Label messageLabel = new Label(this, SWT.NONE);
		messageLabel.setText("TO BE DEFINED");
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER);
	}
}
