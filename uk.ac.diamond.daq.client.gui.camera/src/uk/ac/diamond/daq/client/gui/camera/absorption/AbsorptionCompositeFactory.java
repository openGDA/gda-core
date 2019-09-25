package uk.ac.diamond.daq.client.gui.camera.absorption;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;

/**
 * This wrapper is a temporary solution. A better would require to refactor
 * {@link AbsorptionConfigurationComposite} to directly implement
 * {@link CompositeFactory}
 * 
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class AbsorptionCompositeFactory<T extends AbstractCameraConfigurationController> implements CompositeFactory {

	private final T controller;

	public AbsorptionCompositeFactory(T controller) {
		super();
		this.controller = controller;
	}

	private T getController() {
		return controller;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new AbsorptionConfigurationComposite(parent, getController(), SWT.NONE);
	}

}
