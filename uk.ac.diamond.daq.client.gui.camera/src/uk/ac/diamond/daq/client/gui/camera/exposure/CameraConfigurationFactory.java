package uk.ac.diamond.daq.client.gui.camera.exposure;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.liveview.LiveViewCompositeFactory;
import uk.ac.gda.client.exception.GDAClientException;


/**
 * This wrapper is a temporary solution. A better would require to refactor
 * {@link AbsorptionConfigurationComposite} to directly implement
 * {@link CompositeFactory}
 * 
 * @author Maurizio Nagni
 */
public class CameraConfigurationFactory implements CompositeFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(LiveViewCompositeFactory.class);
	
	@Override
	public Composite createComposite(Composite parent, int style)  {
		try {
			return new CameraConfigurationComposite(parent, SWT.NONE);
		} catch (GDAClientException e) {
			logger.error("Cannot create Camera Configuration", e);
		}
		return null;
	}
}
