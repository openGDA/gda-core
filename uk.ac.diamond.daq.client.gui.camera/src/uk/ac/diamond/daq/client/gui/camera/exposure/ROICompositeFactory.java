package uk.ac.diamond.daq.client.gui.camera.exposure;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;

/**
 * Instantiates a {@link SensorROIComposite} object
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class ROICompositeFactory<T extends AbstractCameraConfigurationController> implements CompositeFactory {

	private final T controller;

	public ROICompositeFactory(T controller) {
		super();
		this.controller = controller;
	}

	private T getController() {
		return controller;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new SensorROIComposite(parent, getController(), SWT.NONE);
	}
}
