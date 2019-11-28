package uk.ac.diamond.daq.client.gui.camera.exposure;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.absorption.AbsorptionConfigurationComposite;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.liveview.LiveViewCompositeFactory;

/**
 * This wrapper is a temporary solution. A better would require to refactor
 * {@link AbsorptionConfigurationComposite} to directly implement
 * {@link CompositeFactory}
 * 
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class ROICompositeFactory<T extends AbstractCameraConfigurationController> implements CompositeFactory {

	private final T controller;

	private static final Logger logger = LoggerFactory.getLogger(LiveViewCompositeFactory.class);

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
