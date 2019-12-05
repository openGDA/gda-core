package uk.ac.diamond.daq.client.gui.camera.liveview;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.client.gui.camera.listener.ROIListener;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;

/**
 * Instantiates the elements for the CameraConfiguration top area  
 * 
 * @author Maurzio Nagni
 */
public class CameraImageComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(CameraImageComposite.class);

	private LivePlottingComposite plottingComposite;
	private IPlottingSystem<Composite> plottingSystem;

	public CameraImageComposite(Composite parent, int style) throws GDAClientException {
		super(parent, style);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		plottingComposite = new LivePlottingComposite(this, SWT.NONE, "Live View", null);
		plottingComposite.setShowTitle(true);
		plottingSystem = plottingComposite.getPlottingSystem();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingComposite);

		DrawableRegion roiSelectionRegion = new DrawableRegion(plottingSystem, SWTResourceManager.getColor(SWT.COLOR_GREEN), "ROI",
		new ROIListener(this, plottingComposite));		
		roiSelectionRegion.setActive(true);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(this);
		logger.debug("CameraImageComposite created");
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}
}
