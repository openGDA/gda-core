package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BinningComposite extends Composite {

	public BinningComposite(Composite parent, int style) {
		super(parent, style);

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		Label label = new Label(this, SWT.NONE);
		label.setText("Binning");
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(label);
	}

}
