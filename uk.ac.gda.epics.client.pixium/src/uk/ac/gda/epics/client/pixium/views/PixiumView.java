/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.client.pixium.views;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.epics.client.EPICSClientActivator;
import uk.ac.gda.epics.client.pixium.ImageConstants;

/**
 * View part used to set parameters and show status of the pixium area detector. 
 * This uses the pixium view controller to return it the right values from EPICS.
 */
public class PixiumView extends ViewPart implements InitializingBean {
	public static final String ID="uk.ac.gda.epics.client.pixium.views.pixiumview";

	private static final String REFRESH_CONNECTION_TOOLTIP = "Refresh Connection";

	private static final String DEFAULT_STATUS_VALUE = "0";

	private static final String DETECTOR = "Detector";
	private static final String ACQ_STATUS = "Acquire Status";
	private static final String EXPOSURE = "Base Exposure (s)";
	private static final String ACQ_PERIOD = "Base Period (s)";
	private static final String COUNTER = "Frame Counter";
	private static final String ARRAY_RATE_FPS = "Array Rate (fps)";
	private static final String EXPOSURE_COUNTER = "Exposure Counter";
	private static final String IMAGE_COUNTER = "Image Counter";
	private static final String ACQ_PROGRESS="Acquire Progress";
	
	private static final String CALIBRATION="Calibration";
	private static final String REQUIRED_STATUS="Required";
	private static final String RUN_STATUS="Running";
	private static final String CALIBRATE_START="Calibrate";
	private static final String CALIBRATE_STOP="Stop";

	private static final String CAPTURE = "Capture Status";
	private static final String ARRAY_X = "X Dimension";
	private static final String ARRAY_Y = "Y Dimension";
	private static final String TIMESTAMP = "Timestamp";
	
	private PixiumViewController pixiumViewController;

	private String viewPartName;

	private FormToolkit toolkit;
	private ScrolledForm form;

	static final Logger logger = LoggerFactory.getLogger(PixiumView.class);

	private Label statusArrayCounter;
	private Label statusArrayRate;
	private Label statusTime; // not updated or implemented in EPICS driver so not used here
	private Label statusExp;
	private Label statusImg;
	
	private Text txtExposuretime;
	private Text txtAcqperiod;

	private Label statusHDFFileSaverCaptureState;
	private Label statusHDFFileSaverX;
	private Label statusHDFFileSaverY;
	private Label statusHDFFileSaverTimestamp;

	private Composite statusAcquireState;
	private Composite statusRequiredState;
	private Composite statusRunningState;

	public PixiumView() {
		setTitleImage(EPICSClientActivator.getDefault().getImageRegistry().get(ImageConstants.IMG_DETECTOR_VIEW));
	}

	public void setArrayCounter(final String arrayCounter) {
		setControlValue(statusArrayCounter, arrayCounter);
	}

	public void setAcqExposure(String acqExposure) {
		setControlValue(txtExposuretime, acqExposure);
	}

	public void setAcqPeriod(String acqPeriod) {
		setControlValue(txtAcqperiod, acqPeriod);
	}

	public void setArrayRate(final String arrayRate) {
		setControlValue(statusArrayRate, arrayRate);
	}

	public void setExp(final String exp) {
		setControlValue(statusExp, exp);
	}

	public void setTime(final String exp) {
		setControlValue(statusTime, exp);
	}
	public void setImg(final String img) {
		setControlValue(statusImg, img);
	}

	public void setFileSaverX(final String fileSaverX) {
		setControlValue(statusHDFFileSaverX, fileSaverX);
	}

	public void setFileSaverY(final String fileSaverY) {
		setControlValue(statusHDFFileSaverY, fileSaverY);
	}

	public void setFileSaverTimeStamp(final String fileSaverTimeStamp) {
		setControlValue(statusHDFFileSaverTimestamp, fileSaverTimeStamp);
	}

	public void setAcquireState(short acquireState) {
		setAcquireControl(acquireState);
	}

	public void setCalibrationRequiredState(short state) {
		setCalibrationRequiredControl(state);
	}
	public void setCalibrationRunningState(short state) {
		setCalibrationRunningControl(state);
	}
	public void setPUMode(int mode) {
		for (PUMode pumode : puModes) {
			if (pumode.getPuModeID()==mode) {
				setSelectionValue(viewer, new StructuredSelection(pumode));
			}
		}
	}
	public void setProgressBarState(int i) {
		setProgressBarControl(progressBar, i);
	}
	@Override
	public void createPartControl(Composite parent) {
		logger.info("widgets created for Pixium Detector View");
		toolkit = new FormToolkit(parent.getDisplay());
		toolkit.setBorderStyle(SWT.BORDER);
		form = toolkit.createScrolledForm(parent);

		Composite formBody = form.getBody();
		form.getBody().setLayout(new ColumnLayout());

		ExpandableComposite detectorComposite = toolkit.createExpandableComposite(formBody, SWT.None);
		ColumnLayoutData cld_detectorComposite = new ColumnLayoutData();
		cld_detectorComposite.widthHint = 350;
		detectorComposite.setLayoutData(cld_detectorComposite);
		detectorComposite.setText(DETECTOR);
		detectorComposite.setLayout(new FillLayout());
		Composite detectorClient = createDetectorComposite(detectorComposite);
		detectorComposite.setClient(detectorClient);
		
		ExpandableComposite calibrationComposite = toolkit.createExpandableComposite(formBody,ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED );
		ColumnLayoutData cld_calibrationComposite = new ColumnLayoutData();
		cld_calibrationComposite.widthHint = 350;
		calibrationComposite.setLayoutData(cld_calibrationComposite);
		calibrationComposite.setText(CALIBRATION);
		calibrationComposite.setLayout(new FillLayout());
		Composite calibrationClient = createCalibrationComposite(calibrationComposite);
		calibrationComposite.setClient(calibrationClient);
		calibrationComposite.addExpansionListener(expansionAdapter);
		
		ExpandableComposite xpndblcmpstMore = toolkit.createExpandableComposite(form.getBody(), ExpandableComposite.TWISTIE );
		ColumnLayoutData cld_xpndblcmpstMore = new ColumnLayoutData();
		cld_xpndblcmpstMore.widthHint = 350;
		xpndblcmpstMore.setLayoutData(cld_xpndblcmpstMore);
		xpndblcmpstMore.setText("More ...");
		xpndblcmpstMore.setExpanded(true);
		xpndblcmpstMore.setLayout(new FillLayout());
		Composite detectorStatusClient = createDetectorStatusComposite(xpndblcmpstMore);
		xpndblcmpstMore.setClient(detectorStatusClient);
		xpndblcmpstMore.addExpansionListener(expansionAdapter);

		/* Register the tool bar to add the refresh action */
		registerToolBar();
		/* Calls the update fields in a separate thread - so that the UI is not blocked. */
		Future<Boolean> isSuccessful = getPixiumViewController().updateAllFields();
		new Thread(new RunUpdateAllFields(isSuccessful)).start();
		getPixiumViewController().setPixiumView(this);
		
	}

	private ExpansionAdapter expansionAdapter = new ExpansionAdapter() {
		@Override
		public void expansionStateChanged(ExpansionEvent e) {
			form.reflow(true);
		}
	};

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
					refreshConnection.setEnabled(false);
				} else {
					refreshConnection.setEnabled(true);
				}
			} catch (InterruptedException e) {
				logger.error("IOC May be down", e);
				refreshConnection.setEnabled(true);
			} catch (ExecutionException e) {
				logger.error("IOC May be down", e);
				refreshConnection.setEnabled(true);
			}
		}
	}

	private void registerToolBar() {

		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();

		toolBarManager.add(refreshConnection);
	}

	/**
	 * Refresh connection is enabled if the IOC is down initially. When this action is run - it checks in a separate
	 * thread to create the channels and update the UI with the PV values.
	 */
	private Action refreshConnection = new Action() {
		@Override
		public org.eclipse.jface.resource.ImageDescriptor getImageDescriptor() {
			return EPICSClientActivator.getDefault().getImageRegistry().getDescriptor(ImageConstants.IMG_REFRESH);
		}

		@Override
		public void run() {
			Future<Boolean> isSuccessful = getPixiumViewController().updateAllFields();
			BusyIndicator.showWhile(getViewSite().getShell().getDisplay(), new RunUpdateAllFields(isSuccessful));
		}

		@Override
		public String getToolTipText() {
			return REFRESH_CONNECTION_TOOLTIP;
		}
	};
	private GridData fillHorizontalGD_1;
	private Table table;

	private ProgressBar progressBar;

//	private int numExposuresPerImage=1;
//
//	private int numImages=1;

	private List<PUMode> puModes;

	private TableViewer viewer;
	private GridData fillHorizontalGD_2;
	private GridData fillHorizontalGD_3;

	/**
	 * Create the detector state composite - EPICS status for the detector ADBase
	 * 
	 * @param detectorComposite
	 */
	private Composite createDetectorComposite(Composite detectorComposite) {
		Composite clientComposite = toolkit.createComposite(detectorComposite);
		clientComposite.setLayout(new GridLayout(4, false));

		@SuppressWarnings("unused")
		Label lblAcqStatus = toolkit.createLabel(clientComposite, ACQ_STATUS);
		
		/* Composite to contain the status composite so that a border can be displayed. */
		GridData fillHorizontalGD = new GridData();
		Composite borderComposite = toolkit.createComposite(clientComposite);
		borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginWidth = 2;
		fillLayout.marginHeight = 2;
		borderComposite.setLayout(fillLayout);
		fillHorizontalGD.horizontalIndent = 3;
		fillHorizontalGD.widthHint = 20;
		fillHorizontalGD.heightHint = 20;
		borderComposite.setLayoutData(fillHorizontalGD);

		statusAcquireState = toolkit.createComposite(borderComposite);
		
		@SuppressWarnings("unused")
		Label lblCaptureStatus = toolkit.createLabel(clientComposite, CAPTURE);
		
		/* Capture status */
		fillHorizontalGD = new GridData();
		borderComposite = toolkit.createComposite(clientComposite);
		borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		fillLayout = new FillLayout();
		fillLayout.marginWidth = 2;
		fillLayout.marginHeight = 2;
		borderComposite.setLayout(fillLayout);
		fillHorizontalGD.horizontalIndent = 3;
		fillHorizontalGD.widthHint = 20;
		fillHorizontalGD.heightHint = 20;
		borderComposite.setLayoutData(fillHorizontalGD);
		statusHDFFileSaverCaptureState = toolkit.createLabel(borderComposite, "", SWT.CENTER);

		/* Exposure */
		@SuppressWarnings("unused")
		Label lblExposure = toolkit.createLabel(clientComposite, EXPOSURE);
		
		txtExposuretime = toolkit.createText(clientComposite, DEFAULT_STATUS_VALUE, SWT.NONE);
		fillHorizontalGD_1 = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD_1.widthHint = 50;
		txtExposuretime.setForeground(ColorConstants.blue);
		txtExposuretime.setBackground(ColorConstants.button);
		fillHorizontalGD_1.horizontalIndent=3;
		txtExposuretime.setLayoutData(fillHorizontalGD_1);
		txtExposuretime.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyReleased(KeyEvent e) {

			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode==SWT.CR) {
					try {
						getPixiumViewController().setAcqExposure(Double.valueOf(txtExposuretime.getText()).doubleValue());
					} catch (NumberFormatException e1) {
						logger.error("String in exposure time text box is not a number", e1);
					} catch (Exception e1) {
						logger.error("Failed to set exposure time to EPICS pixium area detector.", e1);
					}
				}
			}
		});

		/* Acq Period */
		Label lblAcqPeriod = toolkit.createLabel(clientComposite, ACQ_PERIOD);
		lblAcqPeriod.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		txtAcqperiod = toolkit.createText(clientComposite, DEFAULT_STATUS_VALUE, SWT.NONE);
		fillHorizontalGD_3 = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD_3.widthHint = 50;
		txtAcqperiod.setForeground(ColorConstants.blue);
		txtAcqperiod.setBackground(ColorConstants.button);
		fillHorizontalGD_3.horizontalIndent=3;
		txtAcqperiod.setLayoutData(fillHorizontalGD_3);
		txtAcqperiod.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode==SWT.CR) {
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode==SWT.CR) {
					try {
						getPixiumViewController().setAcqPeriod(Double.valueOf(txtAcqperiod.getText()).doubleValue());
					} catch (NumberFormatException e1) {
						logger.error("String in acquire period text box is not a number", e1);
					} catch (Exception e1) {
						logger.error("Failed to set acquire period to EPICS pixium area detector.", e1);
					}
				}
			}
		});

		/* acquisition progress */
		@SuppressWarnings("unused")
		Label lblProgress = toolkit.createLabel(clientComposite, ACQ_PROGRESS);
		fillHorizontalGD_2 = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD_2.horizontalSpan = 3;
		progressBar = new ProgressBar(clientComposite, SWT.BORDER);
		fillHorizontalGD_2.horizontalIndent=3;
		progressBar.setLayoutData(fillHorizontalGD_2);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
		
		return clientComposite;
	}

	private Composite createCalibrationComposite(Composite detectorComposite) {
		Composite clientComposite = toolkit.createComposite(detectorComposite);
		clientComposite.setLayout(new GridLayout(6, false));

		@SuppressWarnings("unused")
		Label lblRequired = toolkit.createLabel(clientComposite, REQUIRED_STATUS);
		
		/* Composite to contain the status composite so that a border can be displayed. */
		GridData fillHorizontalGD = new GridData();
		Composite borderComposite = toolkit.createComposite(clientComposite);
		borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginWidth = 2;
		fillLayout.marginHeight = 2;
		borderComposite.setLayout(fillLayout);
		fillHorizontalGD.horizontalIndent = 3;
		fillHorizontalGD.widthHint = 20;
		fillHorizontalGD.heightHint = 20;
		borderComposite.setLayoutData(fillHorizontalGD);
		statusRequiredState = toolkit.createComposite(borderComposite);
		
		/* Calibrate button */
		Button btnCalibrate = toolkit.createButton(clientComposite, CALIBRATE_START, SWT.PUSH);
		GridData gd_btnCalibrate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnCalibrate.widthHint = 88;
		btnCalibrate.setLayoutData(gd_btnCalibrate);
		btnCalibrate.addSelectionListener(new SelectionAdapter() {
				@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getPixiumViewController().startCalibration();
				} catch (Exception e1) {
					logger.error("Failed to start pixium calibration.", e1);
				}
			}
		});

		/* RUN */
		@SuppressWarnings("unused")
		Label lblCalibrationRunStatus = toolkit.createLabel(clientComposite, RUN_STATUS);
		
		GridData fillHorizontalGD1 = new GridData();
		Composite borderComposite1 = toolkit.createComposite(clientComposite);
		borderComposite1.setBackground(borderComposite1.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		FillLayout fillLayout1 = new FillLayout();
		fillLayout1.marginWidth = 2;
		fillLayout1.marginHeight = 2;
		borderComposite1.setLayout(fillLayout1);
		fillHorizontalGD1.horizontalIndent = 3;
		fillHorizontalGD1.widthHint = 20;
		fillHorizontalGD1.heightHint = 20;
		borderComposite1.setLayoutData(fillHorizontalGD1);
		statusRunningState = toolkit.createComposite(borderComposite1);
		
		/* Calibrate stop button */
		Button btnCalibrateStop = toolkit.createButton(clientComposite, CALIBRATE_STOP, SWT.PUSH);
		btnCalibrateStop.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					getPixiumViewController().stopCalibration();
				} catch (Exception e1) {
					logger.error("Failed to abort or stop pixium calibration.", e1);
				}
			}
		});
		
		/* PU Mode */
		table = toolkit.createTable(clientComposite, SWT.VIRTUAL | SWT.BORDER);
		GridData gd_table = new GridData(SWT.LEFT, SWT.CENTER, true, false, 6, 1);
		gd_table.widthHint=350;
		table.setLayoutData(gd_table);
		table.setHeaderVisible(true);
		toolkit.paintBordersFor(table);
		table.setLinesVisible(true);
		viewer = new TableViewer(table);
		viewer.setContentProvider(new ArrayContentProvider());
		TableViewerColumn puModeColumn=new TableViewerColumn(viewer, SWT.NONE);
		puModeColumn.getColumn().setText("PU Mode");
		puModeColumn.getColumn().setWidth(80);
		puModeColumn.setLabelProvider(new  ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PUMode mode=(PUMode)element;
				return String.valueOf(mode.getPuModeID());
			}
		});
		TableViewerColumn resolutionColumn=new TableViewerColumn(viewer, SWT.NONE);
		resolutionColumn.getColumn().setText("Resolution");
		resolutionColumn.getColumn().setWidth(100);
		resolutionColumn.setLabelProvider(new  ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PUMode mode=(PUMode)element;
				return mode.getResolution();
			}
		});
		TableViewerColumn minExposureColumn=new TableViewerColumn(viewer, SWT.NONE);
		minExposureColumn.getColumn().setText("Min.Exposure");
		minExposureColumn.getColumn().setWidth(100);
		minExposureColumn.setLabelProvider(new  ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PUMode mode=(PUMode)element;
				return mode.getMinimumExposure();
			}
		});
		TableViewerColumn maxRateColumn=new TableViewerColumn(viewer, SWT.NONE);
		maxRateColumn.getColumn().setText("Max.Rate");
		maxRateColumn.getColumn().setWidth(70);
		maxRateColumn.setLabelProvider(new  ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				PUMode mode=(PUMode)element;
				return mode.getMaximumRate();
			}
		});

		puModes = PUModeProvider.INSTANCE.getPUModes();
		viewer.setInput(puModes);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
			    IStructuredSelection selection = (IStructuredSelection)
			            viewer.getSelection();
			    Object firstElement = selection.getFirstElement();
			    if (firstElement instanceof PUMode) {
			    	PUMode pumode=(PUMode)firstElement;
			    	try {
			    		//TODO make sure not set PU mode if already there.
			    		int modeID=getPixiumViewController().getPUMode();
			    		if (modeID!=pumode.getPuModeID()) {
			    			getPixiumViewController().setPUMode(pumode.getPuModeID());
			    		}
					} catch (Exception e) {
						logger.error("Failed to set PU MOde", e);
					}
			    }
			}
		});

		return clientComposite;
	}

	private Composite createDetectorStatusComposite(Composite detectorComposite) {
		Composite clientComposite = toolkit.createComposite(detectorComposite);
		clientComposite.setLayout(new GridLayout(4, false));

		/* Counter */
		@SuppressWarnings("unused")
		Label lblCounter = toolkit.createLabel(clientComposite, COUNTER);

		GridData fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		statusArrayCounter = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusArrayCounter.setBackground(ColorConstants.black);
		statusArrayCounter.setForeground(ColorConstants.lightGreen);
		statusArrayCounter.setLayoutData(fillHorizontalGD);

		/* Array rate */
		Label lblArrayRate = toolkit.createLabel(clientComposite, ARRAY_RATE_FPS);
		lblArrayRate.setText("Frame Rate (fps)");

		statusArrayRate = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusArrayRate.setBackground(ColorConstants.black);
		statusArrayRate.setForeground(ColorConstants.lightGreen);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD.horizontalIndent = 3;
		statusArrayRate.setLayoutData(fillHorizontalGD);

		/* Exposure counter*/
		Label lblExposureCounter = toolkit.createLabel(clientComposite, EXPOSURE_COUNTER);
		lblExposureCounter.setText(EXPOSURE_COUNTER);

		statusExp = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusExp.setForeground(ColorConstants.lightGreen);
		statusExp.setBackground(ColorConstants.black);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		statusExp.setLayoutData(fillHorizontalGD);

		/* Image counter*/
		Label lblImageCounter = toolkit.createLabel(clientComposite, IMAGE_COUNTER);
		lblImageCounter.setText(IMAGE_COUNTER);

		statusImg = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusImg.setForeground(ColorConstants.lightGreen);
		statusImg.setBackground(ColorConstants.black);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		statusImg.setLayoutData(fillHorizontalGD);
		/* X - Dim0 Size */
		@SuppressWarnings("unused")
		Label lblX = toolkit.createLabel(clientComposite, ARRAY_X);

		statusHDFFileSaverX = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusHDFFileSaverX.setForeground(ColorConstants.lightGreen);
		statusHDFFileSaverX.setBackground(ColorConstants.black);
		statusHDFFileSaverX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/* Y - Dim0 Size */
		@SuppressWarnings("unused")
		Label lblY = toolkit.createLabel(clientComposite, ARRAY_Y);

		statusHDFFileSaverY = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusHDFFileSaverY.setForeground(ColorConstants.lightGreen);
		statusHDFFileSaverY.setBackground(ColorConstants.black);
		statusHDFFileSaverY.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/* Timestamp */
		@SuppressWarnings("unused")
		Label lblTimestamp = toolkit.createLabel(clientComposite, TIMESTAMP);

		statusHDFFileSaverTimestamp = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusHDFFileSaverTimestamp.setForeground(ColorConstants.lightGreen);
		statusHDFFileSaverTimestamp.setBackground(ColorConstants.black);
		GridData gd_statusHDFFileSaverTimestamp = new GridData(GridData.FILL_HORIZONTAL);
		gd_statusHDFFileSaverTimestamp.horizontalSpan = 3;
		statusHDFFileSaverTimestamp.setLayoutData(gd_statusHDFFileSaverTimestamp);

		return clientComposite;
	}

	private void setProgressBarControl(final ProgressBar control, final int value) {
		if (control != null && !control.isDisposed()) {
			control.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!control.isDisposed()) {
						control.setSelection(value);
					}
				}
			});
		}
	}

	private void setAcquireControl(int status) {
		setColourControl(statusAcquireState, status, SWT.COLOR_DARK_GREEN, SWT.COLOR_GREEN);
	}

	private void setCalibrationRequiredControl(int status) {
		setColourControl(statusRequiredState, status, SWT.COLOR_DARK_GREEN, SWT.COLOR_RED);
	}
	private void setCalibrationRunningControl(int status) {
		setColourControl(statusRunningState, status, SWT.COLOR_DARK_GREEN, SWT.COLOR_YELLOW);
	}
	private void setSelectionValue(final TableViewer control, final ISelection selection) {
		if (control != null && !control.getControl().isDisposed()) {
			control.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!control.getControl().isDisposed()) {
						control.setSelection(selection);
					}
				}
			});
		}
	}
	
	private void setControlValue(final Label control, final String value) {
		if (control != null && !control.isDisposed()) {
			control.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!control.isDisposed()) {
						control.setText(value);
					}
				}
			});
		}
	}

	private void setControlValue(final Text control, final String value) {
		if (control != null && !control.isDisposed()) {
			control.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!control.isDisposed()) {
						control.setText(value);
					}
				}
			});
		}
	}
	
	/**
	 * Sets the label background colour to show the status - used for acquire status and capture status.
	 * 
	 * @param control
	 * @param statusInt
	 * @param doneColour
	 * @param activeColour
	 */
	private void setColourControl(final Control control, final int statusInt, final int doneColour,
			final int activeColour) {
		if (control != null && !control.isDisposed()) {
			control.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (!control.isDisposed()) {
						if (statusInt == 0) {
							control.setBackground(control.getDisplay().getSystemColor(doneColour));
						} else if (statusInt == 1) {
							control.setBackground(control.getDisplay().getSystemColor(activeColour));
						}
					}
				}
			});
		}
	}

	private void setFileSaverCaptureControl(int status) {
		setColourControl(statusHDFFileSaverCaptureState, status, SWT.COLOR_DARK_GREEN, SWT.COLOR_YELLOW);
	}

	@Override
	public String getPartName() {
		return viewPartName;
	}

	@Override
	public void setFocus() {
		form.setFocus();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getPixiumViewController() == null) {
			throw new IllegalArgumentException("pixiumViewController should be declared.");
		}
	}

	public void setFileSaverCaptureState(short fileSaverCaptureState) {
		setFileSaverCaptureControl(fileSaverCaptureState);
	}

	@Override
	public void dispose() {
		toolkit.dispose();
		super.dispose();
	}
	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	public PixiumViewController getPixiumViewController() {
		return pixiumViewController;
	}

	public void setPixiumViewController(PixiumViewController pixiumViewController) {
		this.pixiumViewController = pixiumViewController;
	}
}
