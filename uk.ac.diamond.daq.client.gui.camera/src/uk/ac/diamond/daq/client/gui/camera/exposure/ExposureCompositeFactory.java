package uk.ac.diamond.daq.client.gui.camera.exposure;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.liveview.LiveViewCompositeFactory;


/**
 * Instantiates a {@link ExposureConfigurationComposite} object
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class ExposureCompositeFactory<T extends AbstractCameraConfigurationController> implements CompositeFactory {
	
	private final T controller;
	
	private static final Logger logger = LoggerFactory.getLogger(LiveViewCompositeFactory.class);
	
	public ExposureCompositeFactory(T controller) {
		super();
		this.controller = controller;
	}

	private T getController() {
		return controller;
	}

	@Override
	public Composite createComposite(Composite parent, int style)  {
		try {
			return new ExposureConfigurationComposite(parent, getController(), SWT.NONE);
		} catch (DeviceException e) {
			logger.error("Error", e);
		}
		return null;
	}
}
