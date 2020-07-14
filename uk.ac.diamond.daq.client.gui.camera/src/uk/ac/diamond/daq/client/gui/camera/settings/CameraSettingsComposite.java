package uk.ac.diamond.daq.client.gui.camera.settings;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.binning.BinningCompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.diamond.daq.client.gui.camera.exposure.ExposureDurationComposite;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Assembles different {@link Composite} as control panel for a camera. Listen
 * to {@link ChangeActiveCameraEvent} events to update the components
 * accordingly.
 * 
 * @author Maurizio Nagni
 */
public class CameraSettingsComposite implements CompositeFactory {

	public CameraSettingsComposite() {
		super();
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite container = createClientCompositeWithGridLayout(parent, style, 2);

		// Exposure Component
		Composite exposureLengthComposite = new ExposureDurationComposite().createComposite(container, style);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(exposureLengthComposite);

		// Binning Component
		Composite binningCompositeArea = new BinningCompositeFactory().createComposite(container, style);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(binningCompositeArea);

		return container;
	}
}
