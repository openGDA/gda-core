/*-
 * Copyright © 2012 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.epics.adviewer.composites;

import gda.images.camera.DummySwtVideoReceiver;
import gda.images.camera.MotionJpegOverHttpReceiverSwt;
import gda.images.camera.VideoReceiver;
import gda.observable.Observable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.epics.adviewer.ADController;
import uk.ac.gda.epics.adviewer.composites.imageviewer.ImagePositionListener;
import uk.ac.gda.epics.adviewer.composites.imageviewer.NewImageListener;
import uk.ac.gda.epics.adviewer.composites.imageviewer.SWT2DOverlayProvider;

public class MJPeg extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(MJPeg.class);

	private ADController config;

	private boolean liveMonitoring = false;
	private Button liveMonitoringBtn;
	private Label livwMonitoringLbl;

	private CameraComposite cameraComposite;

	public MJPeg(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new GridLayout(2, false));
		Composite left = new Composite(this, SWT.NONE);
		GridData gd_left = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_left.widthHint = 208;
		left.setLayoutData(gd_left);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(left);
		left.setLayout(new GridLayout(1, false));

		statusComposite = new IOCStatus(left, SWT.NONE);
		GridData gd_grpIocStatus1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpIocStatus1.widthHint = 155;
		statusComposite.setLayoutData(gd_grpIocStatus1);

		cameraStatus = new CameraStatus(left, SWT.NONE);

		Group stateGroup = new Group(left, SWT.NONE);
		GridData gd_stateGroup = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_stateGroup.widthHint = 153;
		stateGroup.setLayoutData(gd_stateGroup);
		stateGroup.setText(" Live View");
		GridLayout gl_stateGroup = new GridLayout(2, false);
		gl_stateGroup.marginWidth = 0;
		gl_stateGroup.marginHeight = 0;
		stateGroup.setLayout(gl_stateGroup);
		livwMonitoringLbl = new Label(stateGroup, SWT.CENTER);
		GridData gd_livwMonitoringLbl = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_livwMonitoringLbl.widthHint = 80;
		livwMonitoringLbl.setLayoutData(gd_livwMonitoringLbl);
		liveMonitoringBtn = new Button(stateGroup, SWT.PUSH | SWT.CENTER);
		liveMonitoringBtn.setText("Stop");
		liveMonitoringBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (liveMonitoring) {
						stop();
					} else {
						start();
					}
				} catch (Exception ex) {
					logger.error("Error responding to start_stop button", ex);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		composite = new Composite(left, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.widthHint = 160;
		composite.setLayoutData(gd_composite);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);

		lblTime = new Label(composite, SWT.NONE);
		lblTime.setToolTipText("Time of last image");
		lblTime.setText("Last image");

		txtTime = new Text(composite, SWT.BORDER);
		txtTime.setText("Waiting ....");

		lblRates = new Label(composite, SWT.NONE);
		lblRates.setText("Rate /s");

		txtRate = new Text(composite, SWT.BORDER);
		txtRate.setText("Unknown");

		Composite right = new Composite(this, SWT.NONE);
		GridData gd_right = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
		gd_right.widthHint = 320;
		right.setLayoutData(gd_right);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(right);
		right.setLayout(new GridLayout(1, false));

		cameraComposite = new CameraComposite(right, SWT.NONE, parent.getDisplay(), new NewImageListener() {

			DateFormat df = new SimpleDateFormat("hh:mm:ss");
			long timeOfLastImage = System.currentTimeMillis();

			@Override
			public void handlerNewImageNotification(ImageData lastImage2) throws Exception {
				if (isDisposed())
					return;
				// On the first image, ensure we reset the display to match incoming image dimensions
				long ctime = System.currentTimeMillis();
				txtTime.setText(df.format(new Date(ctime)));
				txtRate.setText(String.format("%3.3g", 1000.0 / (ctime - timeOfLastImage)));
				timeOfLastImage = ctime;
			}
		});
		cameraComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		GridDataFactory.fillDefaults().grab(true, true).applyTo(cameraComposite);

		/*
		 * @Override public void imageDragged(IImagePositionEvent event) { } }, fig);
		 */
		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				try {
					stop();
				} catch (Exception ex) {
					logger.error("Error stopping histogram computation", ex);
				}
				if (videoReceiver != null) {
					videoReceiver.closeConnection();
					videoReceiver = null;
				}
				if (cameraComposite != null) {
					cameraComposite.setVideoReceiver(null);
					cameraComposite = null;
				}
			}
		});
	}

	public IFigure getTopFigure() {
		return cameraComposite == null ? null : cameraComposite.getTopFigure();
	}

	public void addImagePositionListener(ImagePositionListener newListener, SWT2DOverlayProvider swtProvider) {
		if (cameraComposite != null)
			cameraComposite.getViewer().getPositionTool().addImagePositionListener(newListener, swtProvider);
	}

	public void removeImagePositionListener(ImagePositionListener listener) {
		if (cameraComposite != null)
			cameraComposite.getViewer().getPositionTool().removeImagePositionListener(listener);
	}

	public void setADController(ADController config) {
		this.config = config;
		try {
			Observable<Boolean> connectionStateObservable = config.getFfmpegStream().getPluginBase()
					.createConnectionStateObservable();
			statusComposite.setObservable(connectionStateObservable);
		} catch (Exception e1) {
			logger.error("Error monitoring ioc status", e1);
		}
		try {
			cameraStatus.setADController(config);
		} catch (Exception e2) {
			logger.error("Error monitoring camera", e2);
		}
		try {
			start();
		} catch (Exception e1) {
			logger.error("Error starting the live stream", e1);
		}
	}

	private VideoReceiver<ImageData> videoReceiver;

	public void stop() throws Exception {
		config.stopFfmpegStream();
		setStarted(false);

	}

	private Label lblTime;
	private Text txtTime;
	private Label lblRates;
	private Text txtRate;
	private Composite composite;
	private IOCStatus statusComposite;
	private CameraStatus cameraStatus;

	public void start() throws Exception {
		if (videoReceiver != null) {
			videoReceiver.closeConnection();
			videoReceiver = null;
			cameraComposite.setVideoReceiver(null);
		}
		config.startFfmpegStream();
		String url = config.getFfmpegStream().getMJPG_URL_RBV();
		if (url.equals("DummySwtVideoReceiver")) {
			DummySwtVideoReceiver dummySwtVideoReceiver = new DummySwtVideoReceiver();
			dummySwtVideoReceiver.setDesiredFrameRate(10);
			videoReceiver = dummySwtVideoReceiver;

		} else {
			MotionJpegOverHttpReceiverSwt motionJpegOverHttpReceiverSwt = new MotionJpegOverHttpReceiverSwt();
			motionJpegOverHttpReceiverSwt.setUrl(url);
			motionJpegOverHttpReceiverSwt.configure();
			motionJpegOverHttpReceiverSwt.start();
			videoReceiver = motionJpegOverHttpReceiverSwt;
		}
		cameraComposite.setVideoReceiver(videoReceiver);
		Display.getCurrent().asyncExec(new Runnable() {
			@Override
			public void run() {
				videoReceiver.start();
			}
		});

		setStarted(true);
	}

	private void setStarted(boolean b) {
		liveMonitoring = b;
		liveMonitoringBtn.setText(b ? "Stop" : "Start");
		livwMonitoringLbl.setText(b ? "Running" : "Stopped");
	}

	public Control getCanvas() {
		return cameraComposite.getViewer().getCanvas();
	}

	public ImageData getImageData() {
		return cameraComposite == null ? null : cameraComposite.getViewer().getImageData();
	}

	public void zoomFit() {
		if (cameraComposite != null)
			cameraComposite.zoomFit();

	}

}

/**
 * @author Pratik Shah
 */
class RaisedBorder extends MarginBorder {

	private static final Insets DEFAULT_INSETS = new Insets(1, 1, 1, 1);

	/**
	 * @see org.eclipse.draw2d.Border#getInsets(IFigure)
	 */
	@Override
	public Insets getInsets(IFigure figure) {
		return insets;
	}

	public RaisedBorder() {
		this(DEFAULT_INSETS);
	}

	public RaisedBorder(Insets insets) {
		super(insets);
	}

	public RaisedBorder(int t, int l, int b, int r) {
		super(t, l, b, r);
	}

	@Override
	public boolean isOpaque() {
		return true;
	}

	/**
	 * @see org.eclipse.draw2d.Border#paint(IFigure, Graphics, Insets)
	 */
	@Override
	public void paint(IFigure figure, Graphics g, Insets insets) {
		g.setLineStyle(Graphics.LINE_SOLID);
		g.setLineWidth(1);
		g.setForegroundColor(ColorConstants.buttonDarker);
		Rectangle r = getPaintRectangle(figure, insets);
		r.resize(-1, -1);
		g.drawLine(r.x, r.y, r.right(), r.y);
		g.drawLine(r.x, r.y, r.x, r.bottom());
		g.setForegroundColor(ColorConstants.buttonDarker);
		g.drawLine(r.x, r.bottom(), r.right(), r.bottom());
		g.drawLine(r.right(), r.y, r.right(), r.bottom());
	}

}