/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.client.views;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.images.camera.ImageListener;
import gda.images.camera.MotionJpegOverHttpReceiverSwt;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.views.SidePlotView;
import uk.ac.gda.client.viewer.IColourChangeListener;
import uk.ac.gda.client.viewer.IRectFigureListener;
import uk.ac.gda.client.viewer.ImageViewerComposite;
import uk.ac.gda.epics.client.EPICSClientActivator;
import uk.ac.gda.epics.client.ImageConstants;

/**
 * EPICS Camera Preview view
 *
 * @author rsr31645
 */
public class CameraPreviewView extends ViewPart implements InitializingBean {

	private static final String START_STREAM_BTN_label = "Click to start viewing stream";
	private static final String IOC_NOT_RUNNING_shortdesc = "The IOC may not be running at the moment. Start the IOC";
	private static final String URL_NOT_AVAILABLE = "URL Not Available";
	private static final String STOP_PREVIEW_TOOLTIP = "Stop Preview";
	private static final String START_PREVIEW_TOOLTIP = "Start Preview";
	private static final int ROI_PAGE = 0;
	private static final int PROC_PAGE = ROI_PAGE + 1;
	private static final int SIM_PAGE = PROC_PAGE + 1;
	private static final int STAT_PAGE = SIM_PAGE + 1;
	private static final int TIFF_PAGE = STAT_PAGE + 1;
	private static final int DRAW_PAGE = TIFF_PAGE + 1;
	private static final String LBL_MJPEG_CONTROLS = "MJpeg Controls";
	private static final String LBL_ARRAY_PORT = "Array Port";
	private static final String NO_VIDEO_shortmsg = "There is no video to display. The detector may not be acquiring.";
	private static final String URL_LABEL = "URL";
	private Combo cmbArrayPort;
	private String subsamplePlotViewName;
	private ImageViewerComposite viewer;
	private MotionJpegOverHttpReceiverSwt videoReceiver;
	private ImageListener<ImageData> listener = new VideoListener();
	private Logger logger = LoggerFactory.getLogger(CameraPreviewView.class);
	public static final String ID = "synopticCamera";

	private PageBook imgViewPgBook;
	private Composite imageViewerComposite;
	private Composite noDataComposite;
	private FontRegistry fontRegistry;
	private String viewPartName;
	private PageBook controlsPageBook;
	private Group procPluginPgBookPage;
	private Group roiPluginPgBookPage;
	private Text txtProcScaleVal;
	private Text txtProcOffsetVal;
	private Text txtROIStartX;
	private Text txtROIStartY;
	private Text txtROISizeX;
	private Text txtROISizeY;
	private CameraViewController cameraViewController;
	private Group simPgBookPage;
	private boolean receiverStarted;
	private Composite introViewComposite;
	private Composite retryComposite;

	public CameraPreviewView() {
		setTitleImage(EPICSClientActivator.getDefault().getImageRegistry().get(ImageConstants.IMG_CAM_VIEW));
		fontRegistry = new FontRegistry(Display.getCurrent());
		fontRegistry.put("text-area-text", new FontData[] { new FontData("Arial", 15, SWT.BOLD | SWT.CENTER) });
	}

	@Override
	public void createPartControl(Composite parent) {

		Composite root = new Composite(parent, SWT.None);
		GridLayout layout = new GridLayout(1, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		root.setLayout(layout);

		/**/
		Composite urlDisplayComposite = new Composite(root, SWT.None);

		urlDisplayComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout(2, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 4;
		layout.marginHeight = 0;
		urlDisplayComposite.setLayout(layout);

		Label urlDisplayLbl = new Label(urlDisplayComposite, SWT.RIGHT);
		urlDisplayLbl.setText(URL_LABEL);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		gd.widthHint = 25;
		urlDisplayLbl.setLayoutData(gd);

		txtUrlStreamer = new Text(urlDisplayComposite, SWT.BORDER);
		txtUrlStreamer.setText(URL_NOT_AVAILABLE);
		txtUrlStreamer.setEditable(false);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalIndent = 15;
		txtUrlStreamer.setLayoutData(layoutData);

		imgViewPgBook = new PageBook(root, SWT.BORDER);
		imgViewPgBook.setLayoutData(new GridData(GridData.FILL_BOTH));

		/**/

		createBlankCompositePage(imgViewPgBook);

		createImageViewerComposite(imgViewPgBook);

		createStartViewingCompositePage(imgViewPgBook);

		createRetryCompositePage(imgViewPgBook);

		imgViewPgBook.showPage(introViewComposite);

		/* Register the play and stop tool bar menus */
		registerToolBarMenu();
		/* Add listeners to check if the part is visible or hidden - when not visible the video receiver is stopped. */
		addPartListener();
		cameraViewController.setCameraView(this);

	}

	private void createRetryCompositePage(PageBook pgBookComposite) {
		retryComposite = new Composite(pgBookComposite, SWT.None);
		retryComposite.setLayout(new GridLayout());

		Text noImageText = new Text(retryComposite, SWT.BORDER | SWT.CENTER);
		noImageText.setEditable(false);
		noImageText.setText(IOC_NOT_RUNNING_shortdesc);
		noImageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		noImageText.setFont(fontRegistry.get("text-area-text"));

		Button btnStartViewingStream = new Button(retryComposite, SWT.BORDER | SWT.CENTER | SWT.PUSH);
		btnStartViewingStream.setText("Retry viewing stream");
		btnStartViewingStream.setImage(EPICSClientActivator.getDefault().getImageRegistry()
				.get(ImageConstants.IMG_PLAY_STREAM));
		btnStartViewingStream.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnStartViewingStream.setFont(fontRegistry.get("text-area-text"));
		btnStartViewingStream.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				Future<Boolean> isSuccessful = cameraViewController.updateCameraViewFields();
				BusyIndicator.showWhile(introViewComposite.getDisplay(), new RunUpdateAllFields(isSuccessful));

			}
		});
	}

	private void createStartViewingCompositePage(Composite pgBookComposite) {
		introViewComposite = new Composite(pgBookComposite, SWT.None);
		introViewComposite.setLayout(new GridLayout());
		Button btnStartViewingStream = new Button(introViewComposite, SWT.BORDER | SWT.CENTER | SWT.PUSH);
		btnStartViewingStream.setText(START_STREAM_BTN_label);
		btnStartViewingStream.setImage(EPICSClientActivator.getDefault().getImageRegistry()
				.get(ImageConstants.IMG_PLAY_STREAM));
		btnStartViewingStream.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnStartViewingStream.setFont(fontRegistry.get("text-area-text"));
		btnStartViewingStream.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				Future<Boolean> isSuccessful = cameraViewController.updateCameraViewFields();
				// new Thread(new RunUpdateAllFields(isSuccessful)).start();
				BusyIndicator.showWhile(introViewComposite.getDisplay(), new RunUpdateAllFields(isSuccessful));
			}
		});
	}


	private void createBlankCompositePage(Composite pgBookComposite) {
		noDataComposite = new Composite(pgBookComposite, SWT.None);
		noDataComposite.setLayout(new GridLayout());
		Text noImageText = new Text(noDataComposite, SWT.BORDER | SWT.CENTER);
		noImageText.setEditable(false);
		noImageText.setText(NO_VIDEO_shortmsg);
		noImageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		noImageText.setFont(fontRegistry.get("text-area-text"));
	}

	private IRectFigureListener rectFigListener = new IRectFigureListener() {

		@Override
		public void performTask(int startX, int startY, int endX, int endY) {
			try {
				cameraViewController.setROIStartX(Integer.toString(startX));
			} catch (Exception e) {
				logger.error("Problem setting ROI Start X", e);
			}
			try {
				cameraViewController.setROIStartY(Integer.toString(startY));
			} catch (Exception e) {
				logger.error("Problem setting ROI Start Y", e);
			}
			try {
				cameraViewController.setROISizeX(Integer.toString(endX - startX));
			} catch (Exception e) {
				logger.error("Problem setting ROI Size X", e);
			}

			try {
				cameraViewController.setROISizeY(Integer.toString(endY - startY));
			} catch (Exception e) {
				logger.error("Problem setting ROI Size Y", e);
			}

			int[] arrayDataForROI = null;
			try {
				arrayDataForROI = cameraViewController.getArrayDataForROI((endX - startX) * (endY - startY));
			} catch (Exception e) {
				logger.error("Problem getting array data", e);
			}
			Dataset integerDS = DatasetFactory.createFromObject(arrayDataForROI, endY - startY, endX - startX);
			try {
				SDAPlotter.imagePlot(subsamplePlotViewName, integerDS);
				// Open the subsample plot view id.
				IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				activePage.showView(subsamplePlotViewId);
				// Close the side plot and histogram plot if they open up
				// FIXME - the below lines don't work because after this happens the view is created.
				// IViewReference viewRef = activePage.findViewReference(SidePlotView.ID, subsamplePlotViewName);
				// activePage.hideView(viewRef);

			} catch (Exception e) {
				logger.error("Problem plotting the image dataset", e);
			}
		}
	};

	private void createImageViewerComposite(Composite pgBookComposite) {
		imageViewerComposite = new Composite(pgBookComposite, SWT.None);

		imageViewerComposite.setLayout(new GridLayout());
		viewer = new ImageViewerComposite(imageViewerComposite, SWT.DOUBLE_BUFFERED | SWT.BORDER);
		viewer.addRectFigureListener(rectFigListener);
		viewer.addColourChangeListener(colourChangeListener);
		viewer.setLayoutData(new GridData(GridData.FILL_BOTH));

		videoReceiver = new MotionJpegOverHttpReceiverSwt();

		/* Composite for controls */
		Group controlGroup = new Group(imageViewerComposite, SWT.None);
		controlGroup.setText(LBL_MJPEG_CONTROLS);
		controlGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		controlGroup.setLayout(new GridLayout(2, false));

		Composite arrayPortComposite = new Composite(controlGroup, SWT.None);
		arrayPortComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		arrayPortComposite.setLayout(new GridLayout(2, false));

		Label lblArrayPort = new Label(arrayPortComposite, SWT.None);
		lblArrayPort.setText(LBL_ARRAY_PORT);
		lblArrayPort.setLayoutData(new GridData());
		cmbArrayPort = new Combo(arrayPortComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbArrayPort.setItems(getPortContents());
		cmbArrayPort.setLayoutData(new GridData());
		cmbArrayPort.addSelectionListener(cmbArrayPortSelListener);

		controlsPageBook = new PageBook(controlGroup, SWT.None);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 120;
		controlsPageBook.setLayoutData(gd);
		/* Index 0 */
		createROIPage(controlsPageBook);
		/* Index 1 */
		createProcPage(controlsPageBook);
		/* Index 2 */
		createAdBasePage(controlsPageBook);

	}

	/**
	 * Uses the {@link Callable} API to invoke the calls to updateFields.
	 */
	public class RunUpdateAllFields implements Runnable {
		private final Future<Boolean> isSuccess;

		public RunUpdateAllFields(Future<Boolean> isSuccess) {
			this.isSuccess = isSuccess;
		}

		@Override
		public void run() {
			try {
				if (isSuccess.get()) {
					startReceiver();
					if (videoReceiver.getImage() == null) {
						showPage(imgViewPgBook, noDataComposite);
					} else {
						loadImageInUIThread(videoReceiver.getImage());
						showPage(imgViewPgBook, imageViewerComposite);
					}
				} else {
					stopReceiver();
				}
			} catch (InterruptedException e) {
				logger.error("IOC May be down", e);
				stopReceiver();
			} catch (ExecutionException e) {
				logger.error("IOC May be down", e);
				stopReceiver();
				showPage(imgViewPgBook, retryComposite);
			} catch (DeviceException e) {
				logger.error("Cannot get image", e);
				stopReceiver();
			}
		}
	}


	private IColourChangeListener colourChangeListener = new IColourChangeListener() {

		@Override
		public void doChangeColours(int redMask, int greenMask, int blueMask, int alphaSelect) {
			// TODO:Ravi set the masks and deal with the colour changes - Ask Mark Basham how to deal with false
			// colours.
			// videoReceiver.setRedMask(redMask);
			// videoReceiver.setGreenMask(greenMask);
			// videoReceiver.setBlueMask(blueMask);
		}

	};

	private void addPartListener() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(partListener);
	}

	IPartListener2 partListener = new IPartListener2() {

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			// startReceiver();
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			stopReceiver();

		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			// Do nothing

		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
			// Do nothing
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
			// Do nothing
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			// Do nothing
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			// Do nothing
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			// Do nothing
			if (SidePlotView.ID.equals(partRef.getId())) {
				if (partRef instanceof IViewReference) {
					IViewReference viewRef = (IViewReference) partRef;
					String secondaryId = viewRef.getSecondaryId();

					if (secondaryId != null && secondaryId.equals(getSubsamplePlotViewName())) {
						IWorkbenchPart part = viewRef.getPart(false);
						if (part instanceof SidePlotView) {
							System.out
									.println("Page ------------------------------------------------------------------------------>>>>>>"
											+ part);
							// ((SidePlotView) part).deactivate(false);
						}
						// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(viewRef);
					}
				}
			}
		}
	};

	private void registerToolBarMenu() {

		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();

		toolBarManager.add(playStreamAction);
		toolBarManager.add(stopStreamAction);
		playStreamAction.setEnabled(!receiverStarted);
		stopStreamAction.setEnabled(receiverStarted);
	}

	/**
	 *
	 */
	private Action playStreamAction = new Action() {
		@Override
		public org.eclipse.jface.resource.ImageDescriptor getImageDescriptor() {
			return EPICSClientActivator.getDefault().getImageRegistry().getDescriptor(ImageConstants.IMG_PLAY_STREAM);
		}

		@Override
		public void run() {
			// start the video receiver.
			Future<Boolean> isSuccessful = cameraViewController.updateCameraViewFields();
			BusyIndicator.showWhile(introViewComposite.getDisplay(), new RunUpdateAllFields(isSuccessful));
		}

		@Override
		public String getToolTipText() {
			return START_PREVIEW_TOOLTIP;
		}
	};

	private Action stopStreamAction = new Action() {
		@Override
		public org.eclipse.jface.resource.ImageDescriptor getImageDescriptor() {
			return EPICSClientActivator.getDefault().getImageRegistry().getDescriptor(ImageConstants.IMG_STOP_STREAM);
		}

		@Override
		public void run() {
			stopReceiver();
		}

		@Override
		public String getToolTipText() {
			return STOP_PREVIEW_TOOLTIP;
		}
	};

	private void startReceiver() {
		if (!receiverStarted) {
			videoReceiver.createConnection();
			videoReceiver.addImageListener(listener);
			playStreamAction.setEnabled(false);
			stopStreamAction.setEnabled(true);
			receiverStarted = true;
		}
	}

	private void stopReceiver() {
		if (receiverStarted) {
			videoReceiver.closeConnection();
			videoReceiver.removeImageListener(listener);
			playStreamAction.setEnabled(true);
			stopStreamAction.setEnabled(false);
			receiverStarted = false;
			showPage(imgViewPgBook, introViewComposite);
		}

	}

	private void showPage(int index) {
		switch (index) {
		case ROI_PAGE:
			showPage(controlsPageBook, roiPluginPgBookPage);
			break;
		case PROC_PAGE:
			showPage(controlsPageBook, procPluginPgBookPage);
			break;
		case SIM_PAGE:
			showPage(controlsPageBook, simPgBookPage);
			break;
		case STAT_PAGE:
			showPage(controlsPageBook, simPgBookPage);
			break;
		case TIFF_PAGE:
			showPage(controlsPageBook, simPgBookPage);
			break;
		case DRAW_PAGE:
			showPage(controlsPageBook, simPgBookPage);
			break;
		}
	}

	private SelectionListener cmbArrayPortSelListener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			Combo combo = (Combo) e.getSource();
			try {
				cameraViewController.setNDArrayPort(combo.getText());
			} catch (Exception e1) {
				logger.error("cannot set nd array port", e1);
			}
			int selectionIndex = combo.getSelectionIndex();

			showPage(selectionIndex);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			System.out.println("Default Selected");
		}
	};

	/**
	 * Composite page for the ADBase
	 *
	 * @param pgbook
	 */
	private void createAdBasePage(PageBook pgbook) {
		simPgBookPage = new Group(pgbook, SWT.NONE);
		simPgBookPage.setText("Controller");
		simPgBookPage.setLayout(new FillLayout());

		Label label = new Label(simPgBookPage, SWT.None);
		label.setText("No controls to be set");

	}

	private void createProcPage(PageBook pgbook) {
		procPluginPgBookPage = new Group(pgbook, SWT.NONE);
		procPluginPgBookPage.setText("Proc");
		procPluginPgBookPage.setLayout(new GridLayout(4, false));

		/* Scale */
		Label lblScale = new Label(procPluginPgBookPage, SWT.None);
		lblScale.setText("Scale");
		lblScale.setLayoutData(new GridData());

		txtProcScaleVal = new Text(procPluginPgBookPage, SWT.LEFT | SWT.BORDER);
		txtProcScaleVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		txtProcScaleVal.addKeyListener(txtKeyListener);
		txtProcScaleVal.addFocusListener(focusAdapter);
		txtProcScaleVal.setToolTipText("Change the scale for the Proc");
		/* Offset */
		Label lblOffset = new Label(procPluginPgBookPage, SWT.None);
		lblOffset.setText("Offset");
		lblOffset.setLayoutData(new GridData());

		txtProcOffsetVal = new Text(procPluginPgBookPage, SWT.LEFT | SWT.BORDER);
		txtProcOffsetVal.setFont(fontRegistry.get("status-text"));

		txtProcOffsetVal.addKeyListener(txtKeyListener);
		txtProcOffsetVal.addFocusListener(focusAdapter);
		txtProcOffsetVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createROIPage(PageBook pgbook) {
		roiPluginPgBookPage = new Group(pgbook, SWT.NONE);
		roiPluginPgBookPage.setText("ROI");
		roiPluginPgBookPage.setLayout(new GridLayout(3, false));

		Label dummyLbl = new Label(roiPluginPgBookPage, SWT.None);
		dummyLbl.setLayoutData(new GridData());

		Label lblXLabel = new Label(roiPluginPgBookPage, SWT.CENTER);
		lblXLabel.setText("X");
		lblXLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblYLabel = new Label(roiPluginPgBookPage, SWT.CENTER);
		lblYLabel.setText("Y");
		lblYLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		/* ROW for Start */
		Label lblRoiStart = new Label(roiPluginPgBookPage, SWT.None);
		lblRoiStart.setText("Start");
		lblRoiStart.setLayoutData(new GridData());

		txtROIStartX = new Text(roiPluginPgBookPage, SWT.LEFT | SWT.BORDER);

		txtROIStartX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtROIStartX.setToolTipText("Change ROI Start X");
		txtROIStartX.addKeyListener(txtKeyListener);
		txtROIStartX.addFocusListener(focusAdapter);
		/**/
		txtROIStartY = new Text(roiPluginPgBookPage, SWT.LEFT | SWT.BORDER);

		txtROIStartY.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtROIStartY.addKeyListener(txtKeyListener);
		txtROIStartY.addFocusListener(focusAdapter);
		/* ROW for Size */
		Label lblRoiSize = new Label(roiPluginPgBookPage, SWT.None);
		lblRoiSize.setText("Size");
		lblRoiSize.setLayoutData(new GridData());
		txtROISizeX = new Text(roiPluginPgBookPage, SWT.LEFT | SWT.BORDER);

		txtROISizeX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtROISizeX.addKeyListener(txtKeyListener);
		txtROISizeX.addFocusListener(focusAdapter);
		/**/
		txtROISizeY = new Text(roiPluginPgBookPage, SWT.LEFT | SWT.BORDER);

		txtROISizeY.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtROISizeY.addKeyListener(txtKeyListener);
		txtROISizeY.addFocusListener(focusAdapter);
	}

	private String[] getPortContents() {
		return cameraViewController.getArrayPorts().toArray(new String[cameraViewController.getArrayPorts().size()]);
	}

	private FocusAdapter focusAdapter = new FocusAdapter() {

		@Override
		public void focusLost(FocusEvent e) {
			Object source = e.getSource();
			if (txtProcOffsetVal == source) {
				try {
					txtProcOffsetVal.setText(cameraViewController.getProcOffset());
					getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
				} catch (Exception e1) {
					logger.error("cannot get Proc Offset", e1);
				}
			} else if (txtProcScaleVal == source) {
				try {
					txtProcScaleVal.setText(cameraViewController.getProcScale());
					getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
				} catch (Exception e1) {
					logger.error("cannot set Proc Scale", e1);
				}
			} else if (txtROISizeX == source) {
				try {
					txtROISizeX.setText(cameraViewController.getROISizeX());
					getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
				} catch (Exception e1) {
					logger.error("cannot setROISizeX", e1);
				}
			} else if (txtROISizeY == source) {
				try {
					txtROISizeY.setText(cameraViewController.getROISizeY());
					getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
				} catch (Exception e1) {
					logger.error("cannot setROISizeY", e1);
				}
			} else if (txtROIStartX == source) {
				try {
					txtROIStartX.setText(cameraViewController.getROIStartX());
					getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
				} catch (Exception e1) {
					logger.error("cannot setROIStartX", e1);
				}
			} else if (txtROIStartY == source) {
				try {
					txtROIStartY.setText(cameraViewController.getROIStartY());
					getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
				} catch (Exception e1) {
					logger.error("cannot setROIStartY", e1);
				}
			}
		}
	};

	/**
	 * Key adapter for the text boxes to validate and persist values
	 */
	private KeyAdapter txtKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			Object source = e.getSource();
			if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
				if (txtProcOffsetVal == source) {
					if (validateDouble(txtProcOffsetVal)) {
						try {
							cameraViewController.setProcOffset(txtProcOffsetVal.getText());
						} catch (Exception e1) {
							logger.error("cannot set Proc Offset", e1);
						}
					}
				} else if (txtProcScaleVal == source) {
					if (validateDouble(txtProcScaleVal)) {
						try {
							cameraViewController.setProcScale(txtProcScaleVal.getText());
						} catch (Exception e1) {
							logger.error("cannot set Proc Scale", e1);
						}
					}
				} else if (txtROISizeX == source) {
					if (validateInteger(txtROISizeX)) {
						try {
							cameraViewController.setROISizeX(txtROISizeX.getText());
						} catch (Exception e1) {
							logger.error("cannot set ROI Size X", e1);
						}
					}
				} else if (txtROISizeY == source) {
					if (validateInteger(txtROISizeY)) {
						try {
							cameraViewController.setROISizeY(txtROISizeY.getText());
						} catch (Exception e1) {
							logger.error("cannot set ROI Size Y", e1);
						}
					}
				} else if (txtROIStartX == source) {
					if (validateInteger(txtROIStartX)) {
						try {
							cameraViewController.setROIStartX(txtROIStartX.getText());
						} catch (Exception e1) {
							logger.error("cannot set ROI Start X", e1);
						}
					}
				} else if (txtROIStartY == source) {
					if (validateInteger(txtROIStartY)) {
						try {
							cameraViewController.setROIStartY(txtROIStartY.getText());
						} catch (Exception e1) {
							logger.error("cannot set ROI Start Y", e1);
						}
					}
				}
			}
		}
	};
	private Text txtUrlStreamer;
	private String subsamplePlotViewId;

	/**
	 * @param txtControl
	 * @return true if the text is a double
	 */
	private boolean validateDouble(Text txtControl) {
		String textVal = txtControl.getText();
		if (!textVal.matches("\\d*\\.?\\d*")) {
			logger.warn(String.format("%1$s is not a valid double value", txtControl.getText()));
			getViewSite().getActionBars().getStatusLineManager()
					.setErrorMessage(String.format("%1$s is not a valid double value", txtControl.getText()));
			return false;
		}
		getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
		return true;
	}

	/**
	 * @param txtControl
	 * @return true if the text is a double
	 */
	private boolean validateInteger(Text txtControl) {
		String textVal = txtControl.getText();
		if (!textVal.matches("\\d*.?\\d*")) {
			logger.warn(String.format("%1$s is not a valid integer value", txtControl.getText()));
			getViewSite().getActionBars().getStatusLineManager()
					.setErrorMessage(String.format("%1$s is not a valid integer value", txtControl.getText()));
			return false;
		}
		getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
		return true;
	}

	@Override
	public void setFocus() {
		viewer.setFocus();
	}

	@Override
	public void dispose() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().removePartListener(partListener);
		videoReceiver.stop();
		viewer.dispose();
		videoReceiver.removeImageListener(listener);
		super.dispose();
	}

	@Override
	public String getPartName() {
		return viewPartName;
	}

	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (viewPartName == null) {
			throw new IllegalArgumentException("'viewPartName' should be provided to the camera view");
		}
		if (cameraViewController == null) {
			throw new IllegalArgumentException("'cameraViewController' should be provided to the camera view");
		}
	}

	public void setCameraViewController(CameraViewController cameraViewController) {
		this.cameraViewController = cameraViewController;
	}

	public CameraViewController getCameraViewController() {
		return cameraViewController;
	}

	private final class VideoListener implements ImageListener<ImageData> {
		private String name;

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
				showPage(imgViewPgBook, noDataComposite);
				return;
			}

			// Other wise if there is an image data then show the image composite.

			imgViewPgBook.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					imgViewPgBook.showPage(imageViewerComposite);

				}
			});
			if (viewer != null) {

				loadImageInUIThread(image);

			}
		}

	}

	private void loadImageInUIThread(final ImageData image) {
		if (imageViewerComposite.getDisplay() != null) {
			imageViewerComposite.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					// Check if there is already an existing image in the imageviewer
					ImageData existingImage = viewer.getImageData();

					viewer.loadImage(image);
					if (existingImage == null) {
						// If the existing image ==null, then that means this is the first image, so call reset view
						viewer.resetView();
					}
				}
			});
		}
	}

	public void setROIStartX(String startX) {
		setTextValue(txtROIStartX, startX);
	}

	public void setROIStartY(String startY) {
		setTextValue(txtROIStartY, startY);
	}

	public void setROISizeX(String sizeX) {
		setTextValue(txtROISizeX, sizeX);
	}

	public void setROISizeY(String sizeY) {
		setTextValue(txtROISizeY, sizeY);
	}

	public void setProcScale(String procScale) {
		setTextValue(txtProcScaleVal, procScale);
	}

	public void setProcOffset(String offset) {
		setTextValue(txtProcOffsetVal, offset);
	}

	/**
	 * Sets the value of the text fields - this needs to be done on the UI thread
	 *
	 * @param control
	 * @param value
	 */
	private void setTextValue(final Text control, final String value) {
		if (control != null && !control.isDisposed()) {
			control.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					control.setText(value);
				}
			});
		}
	}

	private void showPage(final PageBook pgBook, final Composite page) {
		pgBook.getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				pgBook.showPage(page);
			}
		});
	}

	/**
	 * @param ndArrayPort
	 */
	public void setFFMpegNDArrayPort(String ndArrayPort) {
		setComboValue(cmbArrayPort, ndArrayPort);
		int index = getIndex(ndArrayPort);
		showPage(index);
	}

	/**
	 * Sets the value of the combo field - this needs to be done on the UI thread
	 *
	 * @param control
	 * @param value
	 */
	private void setComboValue(final Combo control, final String value) {
		if (control != null) {
			control.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					int indexOfValue = getIndex(value);
					control.select(indexOfValue);
				}

			});
		}
	}

	private int getIndex(String value) {
		String[] portContents = getPortContents();
		for (int count = 0; count < portContents.length; count++) {
			if (value.equals(portContents[count])) {
				return count;
			}
		}
		return -1;
	}

	/**
	 * Updates the stream url and set the video receiver url.
	 *
	 * @param streamUrl
	 */
	public void updateStreamerUrl(String streamUrl) {
		setTextValue(txtUrlStreamer, streamUrl);
		videoReceiver.setUrl(streamUrl);
	}

	public String getSubsamplePlotViewName() {
		return subsamplePlotViewName;
	}

	public void setSubsamplePlotViewName(String subsamplePlotViewName) {
		this.subsamplePlotViewName = subsamplePlotViewName;
	}

	public void setSubsamplePlotViewId(String subsamplePlotViewId) {
		this.subsamplePlotViewId = subsamplePlotViewId;
	}

	public String getSubsamplePlotViewId() {
		return subsamplePlotViewId;
	}
}
