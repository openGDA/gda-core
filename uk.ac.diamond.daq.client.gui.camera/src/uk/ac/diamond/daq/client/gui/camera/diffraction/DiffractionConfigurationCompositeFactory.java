package uk.ac.diamond.daq.client.gui.camera.diffraction;

import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.client.exception.GDAClientException;

public class DiffractionConfigurationCompositeFactory<T extends DiffractionCameraConfigurationController> implements CompositeFactory {

	private final T controller;
	
	private static final Logger logger = LoggerFactory.getLogger(DiffractionConfigurationCompositeFactory.class);
	
	public DiffractionConfigurationCompositeFactory(T controller) {
		this.controller = controller;
	}
	
	private T getController() {
		return controller;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		try {
			return new DiffractionConfigurationComposite(parent, getController(), style);
		} catch (GDAClientException e) {
			logger.error("Error", e);
		}
		return null;
	}

}