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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
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
 * View part used to set parameters and show the status of the pixium area detector. 
 * This uses the pixium view controller to return it the right values from EPICS.
 */
public class PixiumView extends ViewPart implements InitializingBean {
	public static final String ID="uk.ac.gda.epics.client.views.StatusView";

	private static final String REFRESH_CONNECTION_TOOLTIP = "Refresh Connection";

	private static final String DEFAULT_STATUS_VALUE = "0";

	private static final String DETECTOR = "Detector";
	private static final String ACQ_STATUS = "Acquire";
	private static final String EXPOSURE = "Exposure";
	private static final String ACQ_PERIOD = "Acq Period";
	private static final String COUNTER = "Counter";
	private static final String ARRAY_RATE_FPS = "Array Rate (fps)";
	private static final String EXPOSURE_COUNTER = "Exposure Counter";
	private static final String IMAGE_COUNTER = "Image Counter";
	private static final String ACQ_PROGRESS="Progress";

	private static final String HDF_FILE_SAVER = "HDF File Saver";
	private static final String CAPTURE = "Capture";
	private static final String ARRAY_X = "X";
	private static final String ARRAY_Y = "Y";
	private static final String TIMESTAMP = "Timestamp";

	private static final String TIFF_FILE_SAVER = "TIFF File Saver";

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

	private Label statusHDFFileSaverCaptureState;
	private Label statusHDFFileSaverX;
	private Label statusHDFFileSaverY;
	private Label statusHDFFileSaverTimestamp;

	private Label statusTiffFileSaverX;
	private Label statusTiffFileSaverY;
	private Label statusTiffFileSaverTimestamp;

	private Composite statusAcquireState;
	private Label statusAcqExposure;
	private Label statusAcqPeriod;

	public PixiumView() {
		setTitleImage(EPICSClientActivator.getDefault().getImageRegistry().get(ImageConstants.IMG_DETECTOR_VIEW));
	}

	public void setArrayCounter(final String arrayCounter) {
		setControlValue(statusArrayCounter, arrayCounter);
	}

	public void setAcqExposure(String acqExposure) {
		setControlValue(statusAcqExposure, acqExposure);
	}

	public void setAcqPeriod(String acqPeriod) {
		setControlValue(statusAcqPeriod, acqPeriod);
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

	public void setMJpegX(final String mjpegX) {
		setControlValue(statusTiffFileSaverX, mjpegX);
	}

	public void setMJpegY(final String mjpegY) {
		setControlValue(statusTiffFileSaverY, mjpegY);
	}

	public void setMJpegTimeStamp(final String mjpegTimestamp) {
		setControlValue(statusTiffFileSaverTimestamp, mjpegTimestamp);
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

	@Override
	public void createPartControl(Composite parent) {
		logger.info("widgets created for Status view");
		toolkit = new FormToolkit(parent.getDisplay());
		toolkit.setBorderStyle(SWT.BORDER);
		form = toolkit.createScrolledForm(parent);

		Composite formBody = form.getBody();

		formBody.setLayout(new GridLayout());
		/* Acq Status */

		ExpandableComposite detectorComposite = toolkit.createExpandableComposite(formBody, SWT.None);
		detectorComposite.setText(DETECTOR);
		GridData detectorCompositeGD = new GridData(GridData.FILL_HORIZONTAL);
		detectorComposite.setLayoutData(detectorCompositeGD);
		detectorComposite.setLayout(new FillLayout());
		Composite detectorClient = createDetectorComposite(detectorComposite);
		detectorComposite.setClient(detectorClient);

		ExpandableComposite hdfFileSaverComposite = toolkit.createExpandableComposite(formBody,
				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		hdfFileSaverComposite.setText(HDF_FILE_SAVER);
		GridData fileSaverCompositeGD = new GridData(GridData.FILL_HORIZONTAL);
		fileSaverCompositeGD.horizontalSpan = 2;
		hdfFileSaverComposite.setLayoutData(fileSaverCompositeGD);
		hdfFileSaverComposite.setLayout(new FillLayout());
		Composite hdfFileSaverClient = createFileSaverComposite(hdfFileSaverComposite);
		hdfFileSaverComposite.setClient(hdfFileSaverClient);
		hdfFileSaverComposite.addExpansionListener(expansionAdapter);

		ExpandableComposite tiffFileSaverComposite = toolkit.createExpandableComposite(formBody, ExpandableComposite.TWISTIE
				| ExpandableComposite.EXPANDED);
		tiffFileSaverComposite.setText(TIFF_FILE_SAVER);
		fileSaverCompositeGD = new GridData(GridData.FILL_HORIZONTAL);
		fileSaverCompositeGD.horizontalSpan = 2;
		tiffFileSaverComposite.setLayoutData(fileSaverCompositeGD);
		tiffFileSaverComposite.setLayout(new FillLayout());
		Composite tiffFileSaverClient = createFileSaverComposite(tiffFileSaverComposite);
		tiffFileSaverComposite.setClient(tiffFileSaverClient);
		tiffFileSaverComposite.addExpansionListener(expansionAdapter);
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

	/**
	 * Create the detector state composite - EPICS status for the detector ADBase
	 * 
	 * @param detectorComposite
	 */
	private Composite createDetectorComposite(Composite detectorComposite) {
		Composite clientComposite = toolkit.createComposite(detectorComposite);
		clientComposite.setLayout(new GridLayout(2, false));

		Label lblAcqStatus = toolkit.createLabel(clientComposite, ACQ_STATUS);

		GridData gridData = new GridData();
		lblAcqStatus.setLayoutData(gridData);
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

		gridData = new GridData();
		lblExposure.setLayoutData(gridData);

		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		statusAcqExposure = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		fillHorizontalGD.horizontalIndent = 3;
		statusAcqExposure.setLayoutData(fillHorizontalGD);

		/* Acq Period */
		Label lblAcqPeriod = toolkit.createLabel(clientComposite, ACQ_PERIOD);

		gridData = new GridData();
		lblAcqPeriod.setLayoutData(gridData);

		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		statusAcqPeriod = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		fillHorizontalGD.horizontalIndent = 3;
		statusAcqPeriod.setLayoutData(fillHorizontalGD);

		/* Counter */
		Label lblCounter = toolkit.createLabel(clientComposite, COUNTER);

		gridData = new GridData();
		lblCounter.setLayoutData(gridData);

		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		statusArrayCounter = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		fillHorizontalGD.horizontalIndent = 3;
		statusArrayCounter.setLayoutData(fillHorizontalGD);

		/* Array rate */
		Label lblArrayRate = toolkit.createLabel(clientComposite, ARRAY_RATE_FPS);
		lblArrayRate.setText(ARRAY_RATE_FPS);
		GridData gd = new GridData();
		lblArrayRate.setLayoutData(gd);

		statusArrayRate = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD.horizontalIndent = 3;
		statusArrayRate.setLayoutData(fillHorizontalGD);

		/* Exposure counter*/
		Label lblExposureCounter = toolkit.createLabel(clientComposite, EXPOSURE_COUNTER);
		lblExposureCounter.setText(EXPOSURE_COUNTER);
		gd = new GridData();
		lblExposureCounter.setLayoutData(gd);

		statusExp = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD.horizontalIndent = 3;
		statusExp.setLayoutData(fillHorizontalGD);

		/* Exposure counter*/
		Label lblImageCounter = toolkit.createLabel(clientComposite, IMAGE_COUNTER);
		lblImageCounter.setText(IMAGE_COUNTER);
		gd = new GridData();
		lblImageCounter.setLayoutData(gd);

		statusImg = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD.horizontalIndent = 3;
		statusImg.setLayoutData(fillHorizontalGD);

		/* acquisition progress */
		Label lblDataType = toolkit.createLabel(clientComposite, ACQ_PROGRESS);

		lblDataType.setLayoutData(new GridData());
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD.horizontalIndent = 3;
		ProgressBar progressBar = new ProgressBar(clientComposite, SWT.BORDER);
		progressBar.setLayoutData(fillHorizontalGD);

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
		GridData gridData = new GridData();
		lblCaptureStatus.setLayoutData(gridData);
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
		lblX.setLayoutData(new GridData());

		statusHDFFileSaverX = toolkit.createLabel(xyComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusHDFFileSaverX.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/* Y - Dim0 Size */
		Label lblY = toolkit.createLabel(xyComposite, ARRAY_Y);
		lblY.setLayoutData(new GridData());

		statusHDFFileSaverY = toolkit.createLabel(xyComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
		statusHDFFileSaverY.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/* Timestamp */
		Label lblTimestamp = toolkit.createLabel(rootComposite, TIMESTAMP);
		lblTimestamp.setLayoutData(new GridData());

		statusHDFFileSaverTimestamp = toolkit.createLabel(rootComposite, DEFAULT_STATUS_VALUE, SWT.BORDER);
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
