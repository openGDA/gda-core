package uk.ac.diamond.daq.client.gui.camera.liveview;

import org.eclipse.jface.layout.GridDataFactory;
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
	private Composite histogram;
	double aspectRatio;

//	private Composite internalArea;
//	private Composite leftCol;
//	private Composite rightCol;

	private static final Logger logger = LoggerFactory.getLogger(LiveViewCompositeFactory.class);

	public LiveViewCompositeFactory(T controller) {
		super();
		this.controller = controller;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite container = ClientSWTElements.createComposite(parent, SWT.NONE, 2);
		try {
			createCameraImageComposite(container);
			createHistogramComposite(container);
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
		cameraImageComposite = new CameraImageComposite(panel, controller, SWT.NONE);
	}

	private void createHistogramComposite(Composite panel) throws Exception {
		histogram = new HistogramComposite(panel, cameraImageComposite.getPlottingSystem(), SWT.NONE);
	}

// This will be activated on a future ticket (to allow internal resize of live/histo components)
//	@Override
//	public Composite createComposite(Composite parent, int style) {
//		internalArea = ClientSWTElements.createComposite(parent, SWT.NONE, 1);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(internalArea);
//		Sash sash = new Sash(internalArea, SWT.VERTICAL);
//
//		try {
//		createCameraImageComposite(internalArea);
//		createHistogramComposite(internalArea);
//		} catch (Exception e) {
//			logger.error("Unable to connect camera", e);
//			Label label;
//			label = new Label(internalArea, SWT.NONE);
//			label.setText("No Camera found");
//			GridDataFactory.fillDefaults().grab(true, true).applyTo(label);
//		}
//
//		leftCol = cameraImageComposite;
//		rightCol = histogram;
//
//		final FormLayout form = new FormLayout();
//		internalArea.setLayout(form);
//
//		FormData button1Data = new FormData();
//		button1Data.left = new FormAttachment(0, 0);
//		button1Data.right = new FormAttachment(sash, 0);
//		button1Data.top = new FormAttachment(0, 0);
//		button1Data.bottom = new FormAttachment(100, 0);
//		leftCol.setLayoutData(button1Data);
//
//		final int limit = 20, percent = 50;
//		final FormData sashData = new FormData();
//		sashData.left = new FormAttachment(percent, 0);
//		sashData.top = new FormAttachment(0, 0);
//		sashData.bottom = new FormAttachment(100, 0);
//		sash.setLayoutData(sashData);
//
//		FormData button2Data = new FormData();
//		button2Data.left = new FormAttachment(sash, 0);
//		button2Data.right = new FormAttachment(100, 0);
//		button2Data.top = new FormAttachment(0, 0);
//		button2Data.bottom = new FormAttachment(100, 0);
//		rightCol.setLayoutData(button2Data);
//
//		sash.addListener(SWT.Selection, e -> {
//			Rectangle sashRect = sash.getBounds();
//			Rectangle shellRect = internalArea.getClientArea();
//			int right = shellRect.width - sashRect.width - limit;
//			e.x = Math.max(Math.min(e.x, right), limit);
//			if (e.x != sashRect.x) {
//				sashData.left = new FormAttachment(0, e.x);
//				internalArea.layout(true, true);
//			}
//		});
//		return internalArea;
//	}
}
