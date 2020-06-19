package uk.ac.diamond.daq.client.gui.camera.settings;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
		Composite container = ClientSWTElements.createComposite(parent, style);

		GridLayoutFactory.swtDefaults().numColumns(4).equalWidth(false).applyTo(container);
		GridDataFactory gdf = GridDataFactory.fillDefaults().grab(true, true);

		// Exposure Component
		Composite exposureLengthComposite = new ExposureDurationComposite().createComposite(container, style);
		gdf.applyTo(exposureLengthComposite);

		// Binning Component
		Composite binningCompositeArea = new BinningCompositeFactory().createComposite(container, style);
		gdf.applyTo(binningCompositeArea);

		return container;
	}
}
