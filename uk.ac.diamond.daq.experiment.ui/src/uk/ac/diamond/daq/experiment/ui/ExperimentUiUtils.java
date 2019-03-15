package uk.ac.diamond.daq.experiment.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ExperimentUiUtils {
	
	private ExperimentUiUtils() {
		throw new IllegalStateException("Static access only");
	}
	
	/**
	 * This GridDataFactory can be applied to controls which should
	 * fill horizontal space
	 */
	public static final GridDataFactory STRETCH = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
	
	public static final void addSpace(Composite composite) {
		new Label(composite, SWT.NONE);
	}


}
