package uk.ac.diamond.daq.client.gui.camera.listener;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.client.gui.camera.event.ROIChangeEvent;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Listen to {@link ROIEvent} messages and republishes as {@link ROIChangeEvent}
 * messages using Spring
 * 
 * @author Maurizio Nagni
 *
 */
public class ROIListener implements IROIListener {

	private final Composite parent;
	private final LivePlottingComposite livePlottingComposite;

	/**
	 * 
	 * @param parent                the Composite containing the ROI element. This
	 *                              parent is used as reference to discriminate
	 *                              between multiple {@link ROIChangeEvent}
	 * @param livePlottingComposite the graphical object where the ROI has been drawn
	 */
	public ROIListener(Composite parent, LivePlottingComposite livePlottingComposite) {
		super();
		this.parent = parent;
		this.livePlottingComposite = livePlottingComposite;
	}

	@Override
	public void roiDragged(ROIEvent event) {
		// Do nothing
	}

	/** 
	 * Intercepts {@link ROIEvent} nd publishes {@link ROIChangeEvent} using Spring	
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.dawnsci.plotting.api.region.IROIListener#roiChanged(org.eclipse.dawnsci.plotting.api.region.ROIEvent)
	 */
	@Override
	public void roiChanged(ROIEvent event) {
		if (livePlottingComposite.getITrace() == null) {
			return;
		}
		RectangularROI roi = ((RectangularROI) event.getROI().getBounds()).copy();
		ROIChangeEvent intEvent = new ROIChangeEvent(parent, roi, livePlottingComposite.getITrace().getData());
		SpringApplicationContextProxy.publishEvent(intEvent);
	}

	@Override
	public void roiSelected(ROIEvent event) {
		// do nothing
	}
}
