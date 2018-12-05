package uk.ac.diamond.daq.experiment.ui.driver;

import static uk.ac.diamond.daq.experiment.ui.driver.DiadUIUtils.STRETCH;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FixedRateDisplacementEditor implements ProfileEditor {
	
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(composite);
		
		new Label(composite, SWT.NONE).setText("Rate");
		Text rate = new Text(composite, SWT.BORDER);
		STRETCH.applyTo(rate);
		new Label(composite, SWT.NONE).setText("mm/min");
		
		new Label(composite, SWT.NONE).setText("Direction");
		Button expansion = new Button(composite, SWT.RADIO);
		expansion.setText("Expansion");
		STRETCH.applyTo(expansion);
		expansion.setSelection(true);
		new Label(composite, SWT.NONE);
		
		new Label(composite, SWT.NONE);
		Button compression = new Button(composite, SWT.RADIO);
		compression.setText("Compression");
		STRETCH.applyTo(compression);
		new Label(composite, SWT.NONE);
	}

}
