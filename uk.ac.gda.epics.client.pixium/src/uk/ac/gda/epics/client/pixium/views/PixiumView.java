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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
	public static final String ID="uk.ac.gda.epics.client.views.StatusView";

	private static final String REFRESH_CONNECTION_TOOLTIP = "Refresh Connection";

	private static final String DEFAULT_STATUS_VALUE = "0";

	private static final String DETECTOR = "Detector";
	private static final String ACQ_STATUS = "Acquire Status";
	private static final String EXPOSURE = "Base Exposure (s)";
	private static final String ACQ_PERIOD = "Base Acq.Period (s)";
	private static final String COUNTER = "Frame Counter";
	private static final String ARRAY_RATE_FPS = "Array Rate (fps)";
	private static final String EXPOSURE_COUNTER = "Exposure Counter";
	private static final String IMAGE_COUNTER = "Image Counter";
	private static final String ACQ_PROGRESS="Progress";
	
	private static final String CALIBRATION="Calibration";
	private static final String REQUIRED_STATUS="Required";
	private static final String RUN_STATUS="Running";
	private static final String CALIBRATE_START="Calibrate";
	private static final String CALIBRATE_STOP="Stop";
	private static final String PU_MODE = "PU Mode";

	private static final String HDF_FILE_SAVER = "HDF File Saver";
	private static final String CAPTURE = "Capture";
	private static final String ARRAY_X = "X";
	private static final String ARRAY_Y = "Y";
	private static final String TIMESTAMP = "Timestamp";
	
	private Map<String, String> puModeMap=new HashMap<String, String>();

	private PixiumViewController pixiumViewController;

	private String viewPartName;

	private FormToolkit toolkit;
	private ScrolledForm form;

	static final Logger logger = LoggerFactory.getLogger(PixiumView.class);

	
	private Label statusArrayCounter;
	private Label statusArrayRate;
	private Label statusTime;
	private Label statusExp;
	private Label statusImg;
	
	private Text txtExposuretime;
	private Text txtAcqperiod;
	//TODO handle these 2 fields update
	private Composite statusRequiredState;
	private Composite statusRunningState;


	private Label statusHDFFileSaverCaptureState;
	private Label statusHDFFileSaverX;
	private Label statusHDFFileSaverY;
	private Label statusHDFFileSaverTimestamp;

	private Composite statusAcquireState;

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

	public void setTime(final String time) {
		setControlValue(statusTime, time);
	}

	public void setExp(final String exp) {
		setControlValue(statusExp, exp);
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
		cld_calibrationComposite.widthHint = 381;
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

		ExpandableComposite hdfFileSaverComposite = toolkit.createExpandableComposite(formBody, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		ColumnLayoutData cld_hdfFileSaverComposite = new ColumnLayoutData();
		cld_hdfFileSaverComposite.widthHint = 350;
		hdfFileSaverComposite.setLayoutData(cld_hdfFileSaverComposite);
		hdfFileSaverComposite.setText(HDF_FILE_SAVER);
		hdfFileSaverComposite.setLayout(new FillLayout());
		Composite hdfFileSaverClient = createFileSaverComposite(hdfFileSaverComposite);
		hdfFileSaverComposite.setClient(hdfFileSaverClient);
		hdfFileSaverComposite.addExpansionListener(expansionAdapter);

		/* Register the tool bar to add the refresh action */
		registerToolBar();
		/* Calls the update fields in a separate thread - so that the UI is not blocked. */
		Future<Boolean> isSuccessful = pixiumViewController.updateAllFields();
		new Thread(new RunUpdateAllFields(isSuccessful)).start();
		pixiumViewController.addListener(this);
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
			Future<Boolean> isSuccessful = pixiumViewController.updateAllFields();
			BusyIndicator.showWhile(getViewSite().getShell().getDisplay(), new RunUpdateAllFields(isSuccessful));
		}

		@Override
		public String getToolTipText() {
			return REFRESH_CONNECTION_TOOLTIP;
		}
	};
	private GridData fillHorizontalGD_1;
	private Table table;

	/**
	 * Create the detector state composite - EPICS status for the detector ADBase
	 * 
	 * @param detectorComposite
	 */
	private Composite createDetectorComposite(Composite detectorComposite) {
		Composite clientComposite = toolkit.createComposite(detectorComposite);
		clientComposite.setLayout(new GridLayout(2, false));

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
		
		/* Exposure */
		Label lblExposure = toolkit.createLabel(clientComposite, EXPOSURE);
		
		txtExposuretime = toolkit.createText(clientComposite, DEFAULT_STATUS_VALUE, SWT.NONE);
		fillHorizontalGD_1 = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD_1.widthHint = 151;
		txtExposuretime.setForeground(ColorConstants.blue);
		txtExposuretime.setBackground(ColorConstants.button);
		fillHorizontalGD_1.horizontalIndent=3;
		txtExposuretime.setLayoutData(fillHorizontalGD_1);
		txtExposuretime.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode==SWT.CR) {
					//TODO send new value to EPICS PV
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		/* Acq Period */
		Label lblAcqPeriod = toolkit.createLabel(clientComposite, ACQ_PERIOD);
		lblAcqPeriod.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		txtAcqperiod = toolkit.createText(clientComposite, DEFAULT_STATUS_VALUE, SWT.NONE);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		txtAcqperiod.setForeground(ColorConstants.blue);
		txtAcqperiod.setBackground(ColorConstants.button);
		fillHorizontalGD.horizontalIndent=3;
		txtAcqperiod.setLayoutData(fillHorizontalGD);
		txtAcqperiod.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode==SWT.CR) {
					//TODO send new value to EPICS PV
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

		/* acquisition progress */
		Label lblProgress = toolkit.createLabel(clientComposite, ACQ_PROGRESS);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		ProgressBar progressBar = new ProgressBar(clientComposite, SWT.BORDER);
		fillHorizontalGD.horizontalIndent=3;
		progressBar.setLayoutData(fillHorizontalGD);
		//TODO update progress bar
		
		return clientComposite;
	}

	private Composite createCalibrationComposite(Composite detectorComposite) {
		Composite clientComposite = toolkit.createComposite(detectorComposite);
		clientComposite.setLayout(new GridLayout(6, false));

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
		btnCalibrate.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO caput to EPICS PV
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});

		/* RUN */
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
		btnCalibrateStop.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO caput to EPICS PV
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
		/* PU Mode */
		Label lblPUMode = toolkit.createLabel(clientComposite, PU_MODE);

		Combo comboPUMode = new Combo(clientComposite, SWT.NONE);
		GridData gd_comboPUMode = new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1);
		gd_comboPUMode.widthHint = 228;
		comboPUMode.setLayoutData(gd_comboPUMode);
		toolkit.adapt(comboPUMode);
		toolkit.paintBordersFor(comboPUMode);
		comboPUMode.setText("Select PU Mode: Resolution, Min.Exposure, Max.Rate");
		comboPUMode.add("Mode1:  2880x2881, 80ms, 4fps");
		comboPUMode.add("Mode3:  960x961,   1ms,  18.5fps");
		comboPUMode.add("Mode4:  1440x1441, 15ms, 12fps");
		comboPUMode.add("Mode7:  1024x1025, 6ms,  18fps");
		comboPUMode.add("Mode13: 640x641,   1ms,  30fps");
		comboPUMode.add("Mode14: 768x769,   1ms,  24fps");
		comboPUMode.add("Mode15: 672x673,   1ms,  30fps");
		comboPUMode.setToolTipText("PU Mode: Resolution, Min.Exposure, Max.Rate");
		comboPUMode.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO handle mode change to EPICS IOC
				puModeMap.get(e.text.split(":")[0]);
				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		table = toolkit.createTable(clientComposite, SWT.VIRTUAL | SWT.BORDER);
		GridData gd_table = new GridData(SWT.LEFT, SWT.CENTER, true, false, 6, 1);
//		ColumnLayoutData cld_table = new ColumnLayoutData();
//		cld_table.widthHint = 352;
//		table.setLayoutData(cld_table);
		gd_table.widthHint=350;
		table.setLayoutData(gd_table);
		table.setHeaderVisible(true);
		toolkit.paintBordersFor(table);
		table.setLinesVisible(true);
//		table.setItemCount(7);
		TableViewer viewer=new TableViewer(table);
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

		viewer.setInput(PUModeProvider.INSTANCE.getPUModes());

		
		//TODO List of PU Modes
		

		return clientComposite;
	}

	private Composite createDetectorStatusComposite(Composite detectorComposite) {
		Composite clientComposite = toolkit.createComposite(detectorComposite);
		clientComposite.setLayout(new GridLayout(4, false));

		/* Counter */
		Label lblCounter = toolkit.createLabel(clientComposite, COUNTER);

		GridData fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		statusArrayCounter = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusArrayCounter.setBackground(ColorConstants.gray);
		statusArrayCounter.setForeground(ColorConstants.lightGreen);
		fillHorizontalGD.horizontalIndent = 3;
		statusArrayCounter.setLayoutData(fillHorizontalGD);

		/* Array rate */
		Label lblArrayRate = toolkit.createLabel(clientComposite, ARRAY_RATE_FPS);
		lblArrayRate.setText("Frame Rate (fps)");

		statusArrayRate = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusArrayRate.setBackground(ColorConstants.gray);
		statusArrayRate.setForeground(ColorConstants.lightGreen);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD.horizontalIndent = 3;
		statusArrayRate.setLayoutData(fillHorizontalGD);

		/* Exposure counter*/
		Label lblExposureCounter = toolkit.createLabel(clientComposite, EXPOSURE_COUNTER);
		lblExposureCounter.setText(EXPOSURE_COUNTER);

		statusExp = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusExp.setForeground(ColorConstants.lightGreen);
		statusExp.setBackground(ColorConstants.gray);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD.horizontalIndent = 3;
		statusExp.setLayoutData(fillHorizontalGD);

		/* Exposure counter*/
		Label lblImageCounter = toolkit.createLabel(clientComposite, IMAGE_COUNTER);
		lblImageCounter.setText(IMAGE_COUNTER);

		statusImg = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusImg.setForeground(ColorConstants.lightGreen);
		statusImg.setBackground(ColorConstants.gray);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD.horizontalIndent = 3;
		statusImg.setLayoutData(fillHorizontalGD);

		return clientComposite;
	}

	private void setAcquireControl(int status) {
		setColourControl(statusAcquireState, status, SWT.COLOR_DARK_GREEN, SWT.COLOR_GREEN);
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

	private Composite createFileSaverComposite(Composite fileSaverComposite) {
		Composite rootComposite = toolkit.createComposite(fileSaverComposite);
		rootComposite.setLayout(new GridLayout(2, false));

		Label lblCaptureStatus = toolkit.createLabel(rootComposite, CAPTURE);
		
		/* Capture status */
		GridData fillHorizontalGD = new GridData();
		Composite borderComposite = toolkit.createComposite(rootComposite);
		borderComposite.setBackground(borderComposite.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginWidth = 2;
		fillLayout.marginHeight = 2;
		borderComposite.setLayout(fillLayout);
		fillHorizontalGD.horizontalIndent = 3;
		fillHorizontalGD.widthHint = 20;
		fillHorizontalGD.heightHint = 20;
		borderComposite.setLayoutData(fillHorizontalGD);
		statusHDFFileSaverCaptureState = toolkit.createLabel(borderComposite, "", SWT.CENTER);

		/* Composite for X and Y so that they appear in the same row */
		Composite xyComposite = toolkit.createComposite(rootComposite);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;

		xyComposite.setLayoutData(gd);
		xyComposite.setLayout(new GridLayout(4, false));
		/* X - Dim0 Size */
		Label lblX = toolkit.createLabel(xyComposite, ARRAY_X);

		statusHDFFileSaverX = toolkit.createLabel(xyComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusHDFFileSaverX.setForeground(ColorConstants.lightGreen);
		statusHDFFileSaverX.setBackground(ColorConstants.gray);
		statusHDFFileSaverX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/* Y - Dim0 Size */
		Label lblY = toolkit.createLabel(xyComposite, ARRAY_Y);

		statusHDFFileSaverY = toolkit.createLabel(xyComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusHDFFileSaverY.setForeground(ColorConstants.lightGreen);
		statusHDFFileSaverY.setBackground(ColorConstants.gray);
		statusHDFFileSaverY.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/* Timestamp */
		Label lblTimestamp = toolkit.createLabel(rootComposite, TIMESTAMP);

		statusHDFFileSaverTimestamp = toolkit.createLabel(rootComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusHDFFileSaverTimestamp.setForeground(ColorConstants.lightGreen);
		statusHDFFileSaverTimestamp.setBackground(ColorConstants.gray);
		statusHDFFileSaverTimestamp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return rootComposite;
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

	/**
	 * @param statusViewController
	 *            The statusViewController to set.
	 */
	public void setStatusViewController(PixiumViewController statusViewController) {
		this.pixiumViewController = statusViewController;
	}

	/**
	 * @return Returns the statusViewController.
	 */
	public PixiumViewController getStatusViewController() {
		return pixiumViewController;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getStatusViewController() == null) {
			throw new IllegalArgumentException("statusViewController should be declared.");
		}
	}

	public void setFileSaverCaptureState(short fileSaverCaptureState) {
		setFileSaverCaptureControl(fileSaverCaptureState);

	}

	@Override
	public void dispose() {
		toolkit.dispose();
		pixiumViewController.removePixiumView(this);
		super.dispose();
	}
	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

}
