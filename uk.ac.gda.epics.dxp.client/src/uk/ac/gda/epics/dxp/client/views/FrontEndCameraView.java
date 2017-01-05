/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.dxp.client.views;

import gda.images.camera.ImageListener;
import gda.images.camera.MotionJpegOverHttpReceiverSwt;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.composites.FixedImageViewerComposite;
import uk.ac.gda.client.tomo.composites.FullImageComposite;
import uk.ac.gda.edxd.common.IEdxdAlignment;
import uk.ac.gda.epics.dxp.client.BeamlineHutch;
import uk.ac.gda.epics.dxp.client.viewfactories.EDXDAlignmentDetectorSetupViewFactory;

public class FrontEndCameraView extends ViewPart {

	private IEdxdAlignment edxdAlignment;

	private static final Logger logger = LoggerFactory.getLogger(FrontEndCameraView.class);
	public static final String ID = "uk.ac.gda.epics.dxp.client.cameraview";
	private FullImageComposite imgViewerComposite;
	private MotionJpegOverHttpReceiverSwt imgVideoReceiver;
	private VideoListener imgVideoListener;
	private Text txtUrl;

	public FrontEndCameraView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		FormToolkit formToolkit = new FormToolkit(getViewSite().getShell().getDisplay());

		formToolkit.setBackground(ColorConstants.white);

		Composite rootComposite = formToolkit.createComposite(parent);
		GridLayout gl = new GridLayout();
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		rootComposite.setLayout(gl);

		Composite topRowComposite = formToolkit.createComposite(rootComposite);
		topRowComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		topRowComposite.setLayout(new GridLayout(5, false));

		Label lblUrl = formToolkit.createLabel(topRowComposite, "URL:");
		lblUrl.setLayoutData(new GridData());

		txtUrl = formToolkit.createText(topRowComposite, "", SWT.BORDER);
		txtUrl.setEditable(false);
		txtUrl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button btnStartStream = formToolkit.createButton(topRowComposite, "Start", SWT.None);
		Button btnStopStream = formToolkit.createButton(topRowComposite, "Stop", SWT.None);
		final Button btnCrossHair = formToolkit.createButton(topRowComposite, "CrossHair", SWT.None);
		btnCrossHair.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnCrossHair)) {
					imgViewerComposite.showCrossWire2();
				}
			}
		});

		imgViewerComposite = new FullImageComposite(rootComposite, SWT.None);
		imgViewerComposite.setBackground(ColorConstants.white);
		
		imgViewerComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		imgVideoReceiver = new MotionJpegOverHttpReceiverSwt();
		imgVideoListener = new VideoListener(imgViewerComposite);

		getViewSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(EDXDAlignmentDetectorSetupViewFactory.ID, detectorViewSelectionListener);

		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage != null) {
				IViewPart detView = activePage.findView(EDXDAlignmentDetectorSetupViewFactory.ID);
				if (detView != null) {
					ISelection selection = detView.getViewSite().getSelectionProvider().getSelection();
					if (selection instanceof DetectorViewSelection) {
						DetectorViewSelection detViewSelection = (DetectorViewSelection) selection;
						updateDetectorViewSelection(detViewSelection);
					}
				}
			}
		}

	}

	private ISelectionListener detectorViewSelectionListener = new ISelectionListener() {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof DetectorViewSelection) {
				DetectorViewSelection detectorViewSelection = (DetectorViewSelection) selection;
				updateDetectorViewSelection(detectorViewSelection);
			}
		}

	};

	private void updateDetectorViewSelection(DetectorViewSelection detectorViewSelection) {
		BeamlineHutch activeHutch = detectorViewSelection.getActiveHutch();
		logger.debug("active hutch:{}", activeHutch.getValue());
		String mpegUrl = null;
		switch (activeHutch) {
		case EH1:
			try {
				edxdAlignment.startEh1Camera();
			} catch (Exception e2) {
				logger.error("Unable to start Eh1 camera", e2);
			}
			try {
				mpegUrl = edxdAlignment.getEh1MpegUrl();
			} catch (Exception e) {
				logger.error("Unable to get eh1 mpeg url", e);
			}

			break;
		case EH2:
			try {
				edxdAlignment.startEh2Camera();
			} catch (Exception e1) {
				logger.error("Unable to start Eh2 camera", e1);
			}
			try {
				mpegUrl = edxdAlignment.getEh2MpegUrl();
			} catch (Exception e) {
				logger.error("Unable to get eh2 mpeg url", e);
			}
			break;
		default:
			break;
		}

		if (mpegUrl != null) {
			txtUrl.setText(mpegUrl);
			stopFullVideoReceiver();
			imgVideoReceiver.setUrl(mpegUrl);
			startFullVideoReceiver();
		} else {
			stopFullVideoReceiver();
			txtUrl.setText("");
		}
	}

	private boolean isVideoReceiverStarted = false;

	private void startFullVideoReceiver() {
		if (!isVideoReceiverStarted) {
			imgVideoReceiver.addImageListener(imgVideoListener);
			try {
				imgVideoReceiver.createConnection();
			} catch (Exception ex) {
				logger.error("Cannot start video:", ex);
			}
			isVideoReceiverStarted = true;
		}
	}

	/**
	 * Stops the video receiver.
	 */
	protected void stopFullVideoReceiver() {
		imgVideoReceiver.closeConnection();
		imgVideoReceiver.removeImageListener(imgVideoListener);
		isVideoReceiverStarted = false;
	}

	/**
	 * VideoListener class to listen to image updates from the MotionMJPegreceiver.
	 */
	private final class VideoListener implements ImageListener<ImageData> {
		private String name;
		private final FixedImageViewerComposite imgViewer;

		public VideoListener(FixedImageViewerComposite imageViewer) {
			imgViewer = imageViewer;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void processImage(final ImageData image) {
			if (image == null) {
				return;
			}

			// Other wise if there is an image data then show the image composite.
			if (imgViewer != null && !imgViewer.isDisposed()) {
				if (imgViewer != null) {
					if (imgViewerComposite != null && !imgViewerComposite.isDisposed()) {
						imgViewerComposite.getDisplay().asyncExec(new Runnable() {

							@Override
							public void run() {
								try {
									imgViewerComposite.loadMainImage(image);
								} catch (Exception e) {
									logger.error("Unable to load main image ", e);
								}
							}
						});
					}
				}
			}
		}

	}

	@Override
	public void setFocus() {
		imgViewerComposite.setFocus();
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(EDXDAlignmentDetectorSetupViewFactory.ID, detectorViewSelectionListener);
		super.dispose();
	}

	public void setEdxdAlignment(IEdxdAlignment edxdAlignment) {
		this.edxdAlignment = edxdAlignment;
	}

	@Override
	public String getPartName() {
		return "Front end camera";
	}
}