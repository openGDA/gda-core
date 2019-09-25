package uk.ac.diamond.daq.client.gui.camera.diffraction;

import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.controller.DiffractionCameraConfigurationController;

/**
 * This wrapper is a temporary solution. A better would require to refactor
 * {@link DiffractionAnalysisConfigurationComposite} to directly implement
 * {@link CompositeFactory}
 * 
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class DiffractionAnalysisConfigurationFactory<T extends DiffractionCameraConfigurationController>
		implements CompositeFactory {

	private final T controller;

	private static final Logger logger = LoggerFactory.getLogger(DiffractionAnalysisConfigurationFactory.class);

	public DiffractionAnalysisConfigurationFactory(T controller) {
		super();
		this.controller = controller;
	}

	private T getController() {
		return controller;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new DiffractionAnalysisConfigurationComposite(parent, style);
	}

}
