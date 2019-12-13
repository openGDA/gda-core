package uk.ac.diamond.daq.client.gui.camera.liveview;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import uk.ac.diamond.daq.client.gui.camera.event.DrawableRegionRegisteredEvent;
import uk.ac.diamond.daq.client.gui.camera.event.RegisterDrawableRegionEvent;
import uk.ac.diamond.daq.client.gui.camera.roi.ROIListener;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

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

		// Registers the region into the camera  
		SpringApplicationContextProxy.addApplicationListener(registerDrawableRegionListener(this));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(this);
		logger.debug("CameraImageComposite created");
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}

	private ApplicationListener<RegisterDrawableRegionEvent> registerDrawableRegionListener(Composite parent) {
		return new ApplicationListener<RegisterDrawableRegionEvent>() {
			@Override
			public void onApplicationEvent(RegisterDrawableRegionEvent event) {
				if (!event.hasSameParent(parent)) {
					return;
				}
				DrawableRegion roiSelectionRegion = new DrawableRegion(plottingSystem, event.getColor(),
						ClientMessagesUtility.getMessage(event.getName()),
						new ROIListener(parent, plottingComposite, event.getName()));
				roiSelectionRegion.setActive(true);

				SpringApplicationContextProxy.publishEvent(new DrawableRegionRegisteredEvent(parent,
						plottingSystem,
						ClientSWTElements.findParentUUID(parent)));
			}
		};
	}
}
