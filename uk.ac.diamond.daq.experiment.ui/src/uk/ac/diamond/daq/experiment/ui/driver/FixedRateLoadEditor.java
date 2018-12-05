package uk.ac.diamond.daq.experiment.ui.driver;

import static uk.ac.diamond.daq.experiment.ui.driver.DiadUIUtils.STRETCH;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FixedRateLoadEditor implements ProfileEditor {

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(composite);
		
		new Label(composite, SWT.NONE).setText("Rate");
		Text rate = new Text(composite, SWT.BORDER);
		STRETCH.applyTo(rate);
		new Label(composite, SWT.NONE).setText("N/min");
		
		new Label(composite, SWT.NONE).setText("Sample stiffness");
		Text stiffness = new Text(composite, SWT.BORDER);
		STRETCH.applyTo(stiffness);
		new Label(composite, SWT.NONE).setText("N/m");
	}

}
