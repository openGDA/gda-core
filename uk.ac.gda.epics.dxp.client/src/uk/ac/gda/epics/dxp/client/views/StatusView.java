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

package uk.ac.gda.epics.dxp.client.views;

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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.epics.dxp.client.Activator;
import uk.ac.gda.epics.dxp.client.ImageConstants;

/**
 * View part used to show the status of the area detector. This uses the status view controller to return it the right
 * values from EPICS.
 */
public class StatusView extends ViewPart implements InitializingBean {

	private static final String REFRESH_CONNECTION_TOOLTIP = "Refresh Connection";

	private static final String DEFAULT_STATUS_VALUE = "0";

	private static final String DETECTOR = "EDXD Detector Status";

	private static final String ACQ_STATUS = "Acquire";

	private static final String LIVE_TIME = "Live Time [s]";

	private static final String REALTIME = "Real Time [s]";

	private static final String AVERAGE_DEAD_TIME = "Average Dead Time [%]";

	private static final String INSTANT_DEAD_TIME = "Instant Dead Time [%]";

	private StatusViewController statusViewController;

	private String viewPartName;

	private Label statusDetectorDataType;
	private FormToolkit toolkit;
	private ScrolledForm form;

	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	static final Logger logger = LoggerFactory.getLogger(StatusView.class);
	private Label statusInstantDeadTime;
	private Label statusAvgDeadTime;
	private Label statusTime;

	private Composite statusAcquireState;

	private Label statusRealTime;

	private Label statusLiveTime;

	public StatusView() {
		setTitleImage(Activator.getDefault().getImageRegistry().get(ImageConstants.IMG_STATUS_VIEW));
	}

	public void setInstantDeadTime(final String instantDeadTime) {
		setControlValue(statusInstantDeadTime, instantDeadTime);
	}

	public void setDeadTime(String deadTime) {
		setControlValue(statusAvgDeadTime, deadTime);
	}

	public void setRealTime(final String realTime) {
		setControlValue(statusRealTime, realTime);
	}

	public void setLiveTime(final String liveTime) {
		setControlValue(statusLiveTime, liveTime);
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
		toolkit = new FormToolkit(parent.getDisplay());
		toolkit.setBorderStyle(SWT.BORDER);
		form = toolkit.createScrolledForm(parent);

		Composite formBody = form.getBody();

		formBody.setLayout(new FillLayout());
		/* Acq Status */

		form.setText(DETECTOR);
		toolkit.decorateFormHeading(form.getForm());

		Composite detectorStatusComposite = createDetectorStatusComposite(formBody);

		form.setMinSize(detectorStatusComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		form.reflow(true);
		/* Register the tool bar to add the refresh action */
		registerToolBar();
		/* Calls the update fields in a separate thread - so that the UI is not blocked. */
		Future<Boolean> isSuccessful = statusViewController.updateAllFields();
		new Thread(new RunUpdateAllFields(isSuccessful)).start();
		statusViewController.addListener(this);
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
			return Activator.getDefault().getImageRegistry().getDescriptor(ImageConstants.IMG_REFRESH);
		}

		@Override
		public void run() {
			Future<Boolean> isSuccessful = statusViewController.updateAllFields();
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
	private Composite createDetectorStatusComposite(Composite detectorComposite) {
		Composite clientComposite = toolkit.createComposite(detectorComposite);
		GridLayout layout = new GridLayout(2, false);

		clientComposite.setLayout(layout);
		/* Acquire state */
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

		/* Real time */
		Label lblRealTime = toolkit.createLabel(clientComposite, REALTIME);

		gridData = new GridData();
		lblRealTime.setLayoutData(gridData);

		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);

		statusRealTime = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER | SWT.CENTER);
		fillHorizontalGD.horizontalIndent = 3;
		statusRealTime.setLayoutData(fillHorizontalGD);

		/* Live Time */
		Label lblLiveTime = toolkit.createLabel(clientComposite, LIVE_TIME);

		gridData = new GridData();
		lblLiveTime.setLayoutData(gridData);

		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		statusLiveTime = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER | SWT.CENTER);
		fillHorizontalGD.horizontalIndent = 3;
		statusLiveTime.setLayoutData(fillHorizontalGD);

		/* Instant Dead Time */
		Label lblInstantDeadTime = toolkit.createLabel(clientComposite, INSTANT_DEAD_TIME);

		gridData = new GridData();
		lblInstantDeadTime.setLayoutData(gridData);

		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		statusInstantDeadTime = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER | SWT.CENTER);
		fillHorizontalGD.horizontalIndent = 3;
		statusInstantDeadTime.setLayoutData(fillHorizontalGD);

		/* Average Dead Time */
		Label lblArrayRate = toolkit.createLabel(clientComposite, AVERAGE_DEAD_TIME);
		lblArrayRate.setText(AVERAGE_DEAD_TIME);
		GridData gd = new GridData();
		lblArrayRate.setLayoutData(gd);

		statusAvgDeadTime = toolkit.createLabel(clientComposite, DEFAULT_STATUS_VALUE, SWT.BORDER | SWT.CENTER);
		fillHorizontalGD = new GridData(GridData.FILL_HORIZONTAL);
		fillHorizontalGD.horizontalIndent = 3;
		statusAvgDeadTime.setLayoutData(fillHorizontalGD);

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
	public void setStatusViewController(StatusViewController statusViewController) {
		this.statusViewController = statusViewController;
	}

	/**
	 * @return Returns the statusViewController.
	 */
	public StatusViewController getStatusViewController() {
		return statusViewController;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getStatusViewController() == null) {
			throw new IllegalArgumentException("statusViewController should be declared.");
		}
	}

	@Override
	public void dispose() {
		toolkit.dispose();
		statusViewController.removeStatusView(this);
		super.dispose();
	}

}