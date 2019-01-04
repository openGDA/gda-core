package uk.ac.diamond.daq.experiment.ui.driver;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;

public class DiadUIUtils {
	
	private DiadUIUtils() {
		throw new IllegalStateException("Static access only");
	}
	
	/**
	 * This GridDataFactory can be applied to controls which should
	 * fill horizontal space
	 */
	public static final GridDataFactory STRETCH = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);


}
