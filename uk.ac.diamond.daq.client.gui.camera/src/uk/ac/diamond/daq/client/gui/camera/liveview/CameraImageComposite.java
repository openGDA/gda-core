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
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.event.ListenToConnectionEvent;
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

	private final LivePlottingComposite plottingComposite;
	private final IPlottingSystem<Composite> plottingSystem;

	/**
	 * Integrates a {@link LiveStreamConnection} into composite element. Can be used
	 * to display a fixed camera live stream.
	 * 
	 * @param parent               where this Composite will live
	 * @param style                the composite style
	 * @param liveStreamConnection the streaming associated with this composite
	 * @throws GDAClientException if problems occur in the composite creation or
	 *                            with the live stream
	 */
	public CameraImageComposite(Composite parent, int style, LiveStreamConnection liveStreamConnection)
			throws GDAClientException {
		super(parent, style);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(this);

		plottingComposite = new LivePlottingComposite(this, SWT.NONE, "Live View", liveStreamConnection);
		plottingComposite.setShowTitle(true);
		plottingSystem = plottingComposite.getPlottingSystem();
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(plottingComposite);

		// Registers the region into the camera
		SpringApplicationContextProxy.addDisposableApplicationListener(this, registerDrawableRegionListener(this));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(this);
		logger.debug("CameraImageComposite created");
	}

	/**
	 * Integrates a {@link LiveStreamConnection} into composite element but is
	 * handled by the {@code parent} composite. It does not explicitly accept
	 * directly a {@link LiveStreamConnection} because the internal code is
	 * listening for {@link ListenToConnectionEvent} published through Spring by the
	 * {@code parent}
	 * 
	 * @see LiveViewCompositeFactory
	 * 
	 * @param parent               where this Composite will live
	 * @param style                the composite style
	 * @param liveStreamConnection the streaming associated with this composite
	 * @throws GDAClientException if problems occur in the composite creation or
	 *                            with the live stream
	 */
	public CameraImageComposite(Composite parent, int style) throws GDAClientException {
		this(parent, style, null);
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}

	private ApplicationListener<RegisterDrawableRegionEvent> registerDrawableRegionListener(Composite parent) {
		return new ApplicationListener<RegisterDrawableRegionEvent>() {
			@Override
			public void onApplicationEvent(RegisterDrawableRegionEvent event) {
				if (!event.haveSameParent(parent)) {
					return;
				}
				DrawableRegion roiSelectionRegion = new DrawableRegion(plottingSystem, event.getColor(),
						ClientMessagesUtility.getMessage(event.getName()),
						new ROIListener(parent, plottingComposite, event.getRegionID()), event.getRegionID());
				roiSelectionRegion.setActive(true);

				SpringApplicationContextProxy.publishEvent(new DrawableRegionRegisteredEvent(parent, ClientSWTElements.findParentUUID(parent), roiSelectionRegion));
			}
		};
	}
}