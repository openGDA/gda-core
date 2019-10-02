package uk.ac.diamond.daq.client.gui.camera.liveview;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.scanning.api.event.core.IConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.controller.AbstractCameraConfigurationController;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Displays a live stream and its histogram.
 * 
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class LiveViewCompositeFactory<T extends AbstractCameraConfigurationController> implements CompositeFactory {

	private final T controller;
	protected CameraImageComposite cameraImageComposite;
	private final IConnection liveStreamConnection;
	double aspectRatio;
	
	private static final Logger logger = LoggerFactory.getLogger(LiveViewCompositeFactory.class);
	
	public LiveViewCompositeFactory(T controller, IConnection liveStreamConnection) {
		super();
		this.controller = controller;
		this.liveStreamConnection = liveStreamConnection;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite container = ClientSWTElements.createComposite(parent, SWT.NONE, 2);
		try {
			createCameraImageComposite(container);
			createHistogramComposite(container);
			setAspectRatio(controller.getMaximumSizedROI());
		} catch (Exception e) {
			logger.error("Unable to connect camera", e);

			Label label;

			label = new Label(container, SWT.NONE);
			label.setText("No Camera found");
			GridDataFactory.fillDefaults().grab(true, true).applyTo(label);

			label = new Label(container, SWT.NONE);
			label.setText("No Camera found");
			GridDataFactory.fillDefaults().grab(true, true).applyTo(label);
		}
		GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
		return container;
	}
	
	private void createCameraImageComposite(Composite panel) throws Exception {
		cameraImageComposite = new CameraImageComposite(panel, controller, liveStreamConnection, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(cameraImageComposite);
	}
	
	private void createHistogramComposite(Composite panel) throws Exception {
		HistogramComposite histogramPanel = new HistogramComposite(panel, cameraImageComposite.getPlottingSystem(),
				SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(histogramPanel);
	}
	
	private void setAspectRatio(RectangularROI maxSize) throws Exception {
		aspectRatio = maxSize.getLength(1) / maxSize.getLength(0);
	}
}
