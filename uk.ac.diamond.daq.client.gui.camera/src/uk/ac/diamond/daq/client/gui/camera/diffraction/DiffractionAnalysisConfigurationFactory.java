package uk.ac.diamond.daq.client.gui.camera.diffraction;

import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;

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

	public DiffractionAnalysisConfigurationFactory(T controller) {
		super();
		this.controller = controller;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new DiffractionAnalysisConfigurationComposite(parent, style);
	}

}
